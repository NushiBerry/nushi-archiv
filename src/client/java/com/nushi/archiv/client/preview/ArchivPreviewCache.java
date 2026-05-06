package com.nushi.archiv.client.preview;

import com.nushi.archiv.client.model.ArchivAsset;
import com.nushi.archiv.client.storage.ArchivLocalLibrary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class ArchivPreviewCache {
    private static final String GENERATED_FOLDER = "generated";
    private static final String EMBEDDED_FOLDER = "embedded";
    private static final String ANIMATED_FOLDER = "animated";

    private final ArchivLocalLibrary localLibrary;

    public ArchivPreviewCache(ArchivLocalLibrary localLibrary) {
        this.localLibrary = localLibrary;
    }

    public Path getPreviewsDirectory() {
        return localLibrary.getPreviewsDirectory();
    }

    public Path getGeneratedDirectory() {
        return getPreviewsDirectory().resolve(GENERATED_FOLDER);
    }

    public Path getEmbeddedDirectory() {
        return getPreviewsDirectory().resolve(EMBEDDED_FOLDER);
    }

    public Path getAnimatedDirectory() {
        return getPreviewsDirectory().resolve(ANIMATED_FOLDER);
    }

    public void ensureDirectories() throws IOException {
        Files.createDirectories(getPreviewsDirectory());
        Files.createDirectories(getGeneratedDirectory());
        Files.createDirectories(getEmbeddedDirectory());
        Files.createDirectories(getAnimatedDirectory());
    }

    public Path getGeneratedPreviewPath(ArchivAsset asset) {
        return getGeneratedDirectory().resolve(getPreviewCacheBaseName(asset) + ".png");
    }

    public Path getEmbeddedPreviewPath(ArchivAsset asset) {
        return getEmbeddedDirectory().resolve(getPreviewCacheBaseName(asset) + ".png");
    }

    public Path getAnimatedPreviewDirectory(ArchivAsset asset) {
        return getAnimatedDirectory().resolve(getPreviewCacheBaseName(asset));
    }

    public String getPreviewCacheBaseName(ArchivAsset asset) {
        String identity = safe(asset.getStructureFileName());

        if (identity.isBlank()) {
            identity = safe(asset.getName());
        }

        if (identity.isBlank()) {
            identity = "unnamed_asset";
        }

        String extension = getFileExtension(identity);

        if (!extension.isBlank() && identity.toLowerCase(Locale.ROOT).endsWith(extension.toLowerCase(Locale.ROOT))) {
            identity = identity.substring(0, identity.length() - extension.length());
        }

        String slug = identity
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "");

        if (slug.isBlank()) {
            slug = "asset";
        }

        String hashSource = safe(asset.getStructureFileName()) + "|" + safe(asset.getName());
        String hash = Integer.toHexString(hashSource.toLowerCase(Locale.ROOT).hashCode());

        return slug + "_" + hash;
    }

    private static String getFileExtension(String fileName) {
        String clean = safe(fileName);
        int dotIndex = clean.lastIndexOf(".");

        if (dotIndex < 0 || dotIndex >= clean.length() - 1) {
            return "";
        }

        return clean.substring(dotIndex);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}