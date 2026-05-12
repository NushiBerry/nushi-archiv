package com.nushi.archiv.client.preview;

import com.nushi.archiv.client.model.ArchivAsset;
import com.nushi.archiv.client.storage.ArchivLocalLibrary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Resolves the best available preview for a given asset.
 *
 * Resolution order:
 *   1. Manual image (user-specified preview image name)
 *   2. Embedded preview (PNG extracted from .bp or .litematic NBT)
 *   3. Generated preview (isometric render from voxel snapshot)
 *   4. Placeholder
 *
 * For steps 2 and 3, if no cached file exists yet, the resolver:
 *   - For embedded: attempts extraction on first call (fast, IO-only)
 *   - For generated: submits to ArchivGeneratedPreviewQueue (background thread)
 *     and returns placeholder until the PNG is ready
 *
 * The queue submission only happens once per asset (deduplication is in the queue).
 * On the next frame, if the PNG was written, the resolver will find it in step 3.
 */
public class ArchivPreviewResolver {

    private final ArchivLocalLibrary localLibrary;
    private final ArchivPreviewCache previewCache;
    private final ArchivEmbeddedPreviewExtractor embeddedPreviewExtractor;
    private final ArchivGeneratedPreviewQueue generatedPreviewQueue;

    // Track assets for which we've already attempted embedded extraction
    private final Set<String> attemptedEmbeddedExtractions = new HashSet<>();

    public ArchivPreviewResolver(ArchivLocalLibrary localLibrary,
                                 ArchivGeneratedPreviewQueue generatedPreviewQueue) {
        this.localLibrary = localLibrary;
        this.previewCache = new ArchivPreviewCache(localLibrary);
        this.embeddedPreviewExtractor = new ArchivEmbeddedPreviewExtractor(localLibrary);
        this.generatedPreviewQueue = generatedPreviewQueue;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Resolves the best available preview for the given asset.
     * Non-blocking: always returns immediately. Generation is async.
     */
    public ArchivPreviewResult resolve(ArchivAsset asset) {
        if (asset == null) {
            return ArchivPreviewResult.placeholder("No asset");
        }

        // 1. Manual preview (user explicitly set a preview image name)
        Path manualPreview = resolveManualPreview(asset);
        if (manualPreview != null) {
            return ArchivPreviewResult.image(ArchivPreviewSource.MANUAL_IMAGE, manualPreview);
        }

        // 2. Embedded preview (extracted from .bp or .litematic)
        Path embeddedPreview = resolveEmbeddedPreview(asset);
        if (embeddedPreview != null) {
            return ArchivPreviewResult.image(ArchivPreviewSource.EMBEDDED_PREVIEW, embeddedPreview);
        }

        // 3. Generated preview (isometric render for .schem/.schematic)
        Path generatedPreview = resolveGeneratedPreview(asset);
        if (generatedPreview != null) {
            return ArchivPreviewResult.image(ArchivPreviewSource.GENERATED_PREVIEW, generatedPreview);
        }

        // 4. Animated preview (if applicable)
        Path animatedPreviewDirectory = resolveAnimatedPreview(asset);
        if (animatedPreviewDirectory != null) {
            return ArchivPreviewResult.animation(animatedPreviewDirectory);
        }

        // 5. No preview available yet — request generation async and return placeholder
        requestGenerationAsync(asset);
        return ArchivPreviewResult.placeholder("Generating preview...");
    }

    /**
     * Convenience method: extract embedded preview from a user-provided source path
     * (used during import flow when the source file is not yet in the local library).
     */
    public Path extractEmbeddedPreviewFromSource(ArchivAsset asset, Path structureSourcePath) {
        try {
            return embeddedPreviewExtractor.extractFromSourcePath(asset, structureSourcePath, previewCache);
        } catch (IOException e) {
            System.err.println("[Archiv] extractEmbeddedPreviewFromSource failed: " + e.getMessage());
            return null;
        }
    }

    public ArchivPreviewCache getPreviewCache() {
        return previewCache;
    }

    // -------------------------------------------------------------------------
    // Resolution steps
    // -------------------------------------------------------------------------

    private Path resolveManualPreview(ArchivAsset asset) {
        String previewImageName = safe(asset.getPreviewImageName());
        if (previewImageName.isBlank()) return null;

        try {
            Path previewsDirectory = localLibrary.getPreviewsDirectory().toAbsolutePath().normalize();
            Path previewPath = previewsDirectory.resolve(previewImageName).toAbsolutePath().normalize();

            if (!previewPath.startsWith(previewsDirectory)) return null;
            if (Files.isRegularFile(previewPath)) return previewPath;
        } catch (Exception ignored) {
        }

        return null;
    }

    private Path resolveEmbeddedPreview(ArchivAsset asset) {
        try {
            previewCache.ensureDirectories();

            // Check cache first
            Path embeddedPreviewPath = previewCache.getEmbeddedPreviewPath(asset);
            if (Files.isRegularFile(embeddedPreviewPath)) {
                return embeddedPreviewPath;
            }

            // Attempt extraction once per asset (fast IO, safe to do on render thread)
            String extractionKey = previewCache.getPreviewCacheBaseName(asset);
            if (!attemptedEmbeddedExtractions.add(extractionKey)) {
                return null; // already tried, don't try again
            }

            Path extracted = embeddedPreviewExtractor.extractFromAsset(asset, previewCache);
            if (extracted != null && Files.isRegularFile(extracted)) {
                return extracted;
            }

        } catch (IOException e) {
            System.err.println("[Archiv] Embedded preview resolution error for "
                    + asset.getName() + ": " + e.getMessage());
        }

        return null;
    }

    private Path resolveGeneratedPreview(ArchivAsset asset) {
        try {
            previewCache.ensureDirectories();
            Path generatedPreviewPath = previewCache.getGeneratedPreviewPath(asset);
            return Files.isRegularFile(generatedPreviewPath) ? generatedPreviewPath : null;
        } catch (IOException e) {
            System.err.println("[Archiv] Generated preview resolution error for "
                    + asset.getName() + ": " + e.getMessage());
            return null;
        }
    }

    private Path resolveAnimatedPreview(ArchivAsset asset) {
        try {
            previewCache.ensureDirectories();
            Path animatedPreviewDirectory = previewCache.getAnimatedPreviewDirectory(asset);
            Path manifestPath = animatedPreviewDirectory.resolve("animation.json");

            if (Files.isDirectory(animatedPreviewDirectory) && Files.isRegularFile(manifestPath)) {
                return animatedPreviewDirectory;
            }
        } catch (IOException ignored) {
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // Async generation request
    // -------------------------------------------------------------------------

    private void requestGenerationAsync(ArchivAsset asset) {
        if (generatedPreviewQueue == null) return;

        try {
            previewCache.ensureDirectories();
            boolean accepted = generatedPreviewQueue.request(asset, previewCache);

            if (accepted) {
                System.out.println("[Archiv] Preview generation requested for: " + asset.getName());
            }
        } catch (IOException e) {
            System.err.println("[Archiv] Could not ensure cache directories for "
                    + asset.getName() + ": " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
