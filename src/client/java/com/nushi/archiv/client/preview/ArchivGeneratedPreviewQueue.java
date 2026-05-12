package com.nushi.archiv.client.preview;

import com.nushi.archiv.client.model.ArchivAsset;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Background queue for automatic preview generation.
 *
 * Design:
 *   - Single-threaded executor (one preview at a time — no resource contention)
 *   - Deduplication via runningKeys set (no duplicate jobs for same asset)
 *   - Per-asset cooldown via cooldownMap (avoids hammering on repeated failures)
 *   - Non-blocking: request() returns immediately; caller shows placeholder
 *   - Clear logging: no silently swallowed exceptions
 *   - Safe shutdown: drains queue on screen close
 *
 * Integration:
 *   Called by ArchivPreviewResolver when no cached preview exists.
 *   The resolver checks the cache on each frame — once the PNG is written,
 *   the next call to resolver.resolve() will find it automatically.
 */
public class ArchivGeneratedPreviewQueue {

    /** How long to wait before retrying a failed asset (ms) */
    private static final long COOLDOWN_MS = 30_000L;

    /** How long before allowing a re-request of an asset that was recently queued (ms) */
    private static final long REQUEUE_COOLDOWN_MS = 5_000L;

    /** Max queue depth — if full, new requests are silently dropped */
    private static final int MAX_QUEUE_SIZE = 64;

    private final ArchivGeneratedPreviewGenerator generator;

    // Single thread: prevents multiple simultaneous renders competing for resources
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "archiv-preview-generator");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY); // don't compete with game thread
        return t;
    });

    // Keys of assets currently queued or running
    private final Set<String> pendingKeys = ConcurrentHashMap.newKeySet();

    // Cooldown tracking: assetKey → timestamp of last attempt (ms)
    private final Map<String, Long> cooldownMap = new ConcurrentHashMap<>();

    // Count of active/pending tasks (for queue size limiting)
    private final java.util.concurrent.atomic.AtomicInteger pendingCount =
            new java.util.concurrent.atomic.AtomicInteger(0);

    private volatile boolean shutdown = false;

    public ArchivGeneratedPreviewQueue(ArchivGeneratedPreviewGenerator generator) {
        this.generator = generator;
    }

    /**
     * Requests preview generation for the given asset.
     *
     * Returns true if the request was accepted (queued), false if:
     *   - already pending/running for this asset
     *   - asset is on cooldown (recently failed)
     *   - queue is full
     *   - queue is shut down
     *
     * Non-blocking. Safe to call every frame.
     */
    public boolean request(ArchivAsset asset, ArchivPreviewCache cache) {
        if (asset == null || cache == null || shutdown) {
            return false;
        }

        String key = buildKey(asset);
        if (key.isBlank()) {
            return false;
        }

        // Check cooldown
        Long lastAttempt = cooldownMap.get(key);
        if (lastAttempt != null) {
            long elapsed = System.currentTimeMillis() - lastAttempt;
            long cooldown = isRecentlyQueued(key) ? REQUEUE_COOLDOWN_MS : COOLDOWN_MS;
            if (elapsed < cooldown) {
                return false;
            }
        }

        // Deduplication
        if (!pendingKeys.add(key)) {
            return false;
        }

        // Queue size limit
        if (pendingCount.get() >= MAX_QUEUE_SIZE) {
            pendingKeys.remove(key);
            return false;
        }

        pendingCount.incrementAndGet();
        cooldownMap.put(key, System.currentTimeMillis());

        // Capture for lambda
        final String capturedKey = key;
        final ArchivAsset capturedAsset = asset;

        executor.submit(() -> {
            try {
                System.out.println("[Archiv] Preview queue: generating for " + capturedAsset.getName());
                Path result = generator.generateIfPossible(capturedAsset, cache);

                if (result != null) {
                    System.out.println("[Archiv] Preview queue: done → " + result.getFileName());
                    // Remove cooldown on success so resolver can find the file
                    cooldownMap.remove(capturedKey);
                } else {
                    System.out.println("[Archiv] Preview queue: no preview produced for " + capturedAsset.getName());
                    // Keep cooldown so we don't immediately retry
                }

            } catch (Exception e) {
                System.err.println("[Archiv] Preview queue: error for " + capturedAsset.getName()
                        + " — " + e.getClass().getSimpleName() + ": " + e.getMessage());
                // Cooldown is already set, so we won't retry immediately
            } finally {
                pendingKeys.remove(capturedKey);
                pendingCount.decrementAndGet();
            }
        });

        return true;
    }

    /**
     * Shuts down the queue. Waits up to 2 seconds for in-progress jobs to finish.
     * Call from ArchivScreen.removed().
     */
    public void shutdown() {
        if (shutdown) return;
        shutdown = true;

        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        pendingKeys.clear();
        cooldownMap.clear();
        System.out.println("[Archiv] Preview queue shut down.");
    }

    /**
     * Returns the number of assets currently queued or generating.
     * Useful for debug UI or status display.
     */
    public int getPendingCount() {
        return pendingCount.get();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String buildKey(ArchivAsset asset) {
        String fileName = safe(asset.getStructureFileName());
        String name = safe(asset.getName());
        return (fileName + "|" + name).toLowerCase();
    }

    private boolean isRecentlyQueued(String key) {
        Long t = cooldownMap.get(key);
        if (t == null) return false;
        return System.currentTimeMillis() - t < REQUEUE_COOLDOWN_MS;
    }

    private static String safe(String v) {
        return v == null ? "" : v.trim();
    }
}
