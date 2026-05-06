package com.nushi.archiv.client.preview;

import com.nushi.archiv.client.model.ArchivAsset;
import com.nushi.archiv.client.storage.ArchivLocalLibrary;

import java.util.HashSet;
import java.util.Set;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ArchivPreviewResolver {
    private final ArchivLocalLibrary localLibrary;
    private final ArchivPreviewCache previewCache;
    private final ArchivEmbeddedPreviewExtractor embeddedPreviewExtractor;
    private final Set<String> attemptedEmbeddedExtractions = new HashSet<>();

    public ArchivPreviewResolver(ArchivLocalLibrary localLibrary) {
        this.localLibrary = localLibrary;
        this.previewCache = new ArchivPreviewCache(localLibrary);
        this.embeddedPreviewExtractor = new ArchivEmbeddedPreviewExtractor(localLibrary);
    }

    public Path extractEmbeddedPreviewFromSource(ArchivAsset asset, Path structureSourcePath) {
        try {
            return embeddedPreviewExtractor.extractFromSourcePath(asset, structureSourcePath, previewCache);
        } catch (IOException exception) {
            return null;
        }
    }

    public ArchivPreviewResult resolve(ArchivAsset asset) {
        if (asset == null) {
            return ArchivPreviewResult.placeholder("No asset");
        }

        Path manualPreview = resolveManualPreview(asset);
        if (manualPreview != null) {
            return ArchivPreviewResult.image(ArchivPreviewSource.MANUAL_IMAGE, manualPreview);
        }

        Path embeddedPreview = resolveEmbeddedPreview(asset);
        if (embeddedPreview != null) {
            return ArchivPreviewResult.image(ArchivPreviewSource.EMBEDDED_PREVIEW, embeddedPreview);
        }

        Path generatedPreview = resolveGeneratedPreview(asset);
        if (generatedPreview != null) {
            return ArchivPreviewResult.image(ArchivPreviewSource.GENERATED_PREVIEW, generatedPreview);
        }

        Path animatedPreviewDirectory = resolveAnimatedPreview(asset);
        if (animatedPreviewDirectory != null) {
            return ArchivPreviewResult.animation(animatedPreviewDirectory);
        }

        return ArchivPreviewResult.placeholder("No preview available");
    }

    public ArchivPreviewCache getPreviewCache() {
        return previewCache;
    }

    private Path resolveManualPreview(ArchivAsset asset) {
        String previewImageName = safe(asset.getPreviewImageName());

        if (previewImageName.isBlank()) {
            return null;
        }

        try {
            Path previewsDirectory = localLibrary.getPreviewsDirectory().toAbsolutePath().normalize();
            Path previewPath = previewsDirectory.resolve(previewImageName).toAbsolutePath().normalize();

            if (!previewPath.startsWith(previewsDirectory)) {
                return null;
            }

            if (Files.isRegularFile(previewPath)) {
                return previewPath;
            }
        } catch (Exception ignored) {
            return null;
        }

        return null;
    }

    private Path resolveEmbeddedPreview(ArchivAsset asset) {
        try {
            previewCache.ensureDirectories();

            Path embeddedPreviewPath = previewCache.getEmbeddedPreviewPath(asset);

            if (Files.isRegularFile(embeddedPreviewPath)) {
                return embeddedPreviewPath;
            }

            String extractionKey = previewCache.getPreviewCacheBaseName(asset);

            if (!attemptedEmbeddedExtractions.add(extractionKey)) {
                return null;
            }

            Path extractedPreviewPath = embeddedPreviewExtractor.extractFromAsset(asset, previewCache);

            if (extractedPreviewPath != null && Files.isRegularFile(extractedPreviewPath)) {
                return extractedPreviewPath;
            }
        } catch (IOException ignored) {
            return null;
        }

        return null;
    }

    private Path resolveGeneratedPreview(ArchivAsset asset) {
        try {
            previewCache.ensureDirectories();

            Path generatedPreviewPath = previewCache.getGeneratedPreviewPath(asset);

            if (Files.isRegularFile(generatedPreviewPath)) {
                return generatedPreviewPath;
            }
        } catch (IOException ignored) {
            return null;
        }

        return null;
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
            return null;
        }

        return null;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}