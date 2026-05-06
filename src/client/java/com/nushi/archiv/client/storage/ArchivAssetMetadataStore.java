package com.nushi.archiv.client.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.nushi.archiv.client.model.ArchivAsset;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

public class ArchivAssetMetadataStore {
    private static final String ROOT_FOLDER_NAME = "archiv";
    private static final String ASSETS_FOLDER_NAME = "assets";
    private static final String PREVIEWS_FOLDER_NAME = "previews";
    private static final String METADATA_FOLDER_NAME = "metadata";
    private static final String JSON_EXTENSION = ".json";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final Path rootDirectory;
    private final Path assetsDirectory;
    private final Path previewsDirectory;
    private final Path metadataDirectory;

    public ArchivAssetMetadataStore(Path gameDirectory) {
        this.rootDirectory = gameDirectory.resolve(ROOT_FOLDER_NAME);
        this.assetsDirectory = rootDirectory.resolve(ASSETS_FOLDER_NAME);
        this.previewsDirectory = rootDirectory.resolve(PREVIEWS_FOLDER_NAME);
        this.metadataDirectory = rootDirectory.resolve(METADATA_FOLDER_NAME);
    }

    public Path getMetadataDirectory() {
        return metadataDirectory;
    }

    public Path getRootDirectory() {
        return rootDirectory;
    }

    public Path getAssetsDirectory() {
        return assetsDirectory;
    }

    public Path getPreviewsDirectory() {
        return previewsDirectory;
    }

    public void ensureDirectories() throws IOException {
        Files.createDirectories(rootDirectory);
        Files.createDirectories(assetsDirectory);
        Files.createDirectories(previewsDirectory);
        Files.createDirectories(metadataDirectory);
    }

    public Path getMetadataPathForStructureFile(String structureFileName) {
        String baseName = toMetadataBaseName(structureFileName);
        return metadataDirectory.resolve(baseName + JSON_EXTENSION);
    }

    public boolean hasMetadataForStructureFile(String structureFileName) throws IOException {
        ensureDirectories();
        return Files.isRegularFile(getMetadataPathForStructureFile(structureFileName));
    }

    public void saveAsset(ArchivAsset asset) throws IOException {
        ensureDirectories();

        String structureFileName = safeString(asset.getStructureFileName());
        if (structureFileName.isBlank()) {
            structureFileName = safeString(asset.getName());
        }

        AssetMetadata metadata = AssetMetadata.fromAsset(asset);
        metadata.hidden = false;
        metadata.hiddenAtEpochMillis = 0L;

        writeMetadata(getMetadataPathForStructureFile(structureFileName), metadata);
    }

    public void hideAsset(ArchivAsset asset) throws IOException {
        ensureDirectories();

        String structureFileName = safeString(asset.getStructureFileName());
        if (structureFileName.isBlank()) {
            structureFileName = safeString(asset.getName());
        }

        Path metadataPath = getMetadataPathForStructureFile(structureFileName);
        AssetMetadata metadata = readMetadata(metadataPath);

        if (metadata == null) {
            metadata = AssetMetadata.fromAsset(asset);
        }

        // Always keep the current visible metadata in the hidden record too.
        AssetMetadata current = AssetMetadata.fromAsset(asset);
        metadata.name = firstNonBlank(metadata.name, current.name);
        metadata.macroCategory = firstNonBlank(metadata.macroCategory, current.macroCategory);
        metadata.type = firstNonBlank(metadata.type, current.type);
        metadata.version = firstNonBlank(metadata.version, current.version);
        metadata.author = firstNonBlank(metadata.author, current.author);
        metadata.previewColor = metadata.previewColor == null ? current.previewColor : metadata.previewColor;
        metadata.chipColor = metadata.chipColor == null ? current.chipColor : metadata.chipColor;
        metadata.variantCount = metadata.variantCount == null ? current.variantCount : metadata.variantCount;
        metadata.favorite = metadata.favorite == null ? current.favorite : metadata.favorite;
        metadata.highlighted = metadata.highlighted == null ? current.highlighted : metadata.highlighted;
        metadata.tags = metadata.tags == null ? current.tags : cleanTags(metadata.tags);
        metadata.structureFileName = firstNonBlank(metadata.structureFileName, current.structureFileName);
        metadata.structureFileFormat = firstNonBlank(metadata.structureFileFormat, current.structureFileFormat);
        metadata.structureFileSize = firstNonBlank(metadata.structureFileSize, current.structureFileSize);
        metadata.previewImageName = firstNonBlank(metadata.previewImageName, current.previewImageName);
        metadata.previewImageFormat = firstNonBlank(metadata.previewImageFormat, current.previewImageFormat);
        metadata.previewImageRatio = firstNonBlank(metadata.previewImageRatio, current.previewImageRatio);

        metadata.hidden = true;
        if (metadata.hiddenAtEpochMillis == null || metadata.hiddenAtEpochMillis <= 0L) {
            metadata.hiddenAtEpochMillis = System.currentTimeMillis();
        }

        writeMetadata(metadataPath, metadata);
    }

    public boolean isAssetHidden(ArchivAsset asset) throws IOException {
        if (asset == null) {
            return false;
        }

        String structureFileName = safeString(asset.getStructureFileName());
        if (structureFileName.isBlank()) {
            return false;
        }

        return isStructureFileHidden(structureFileName);
    }

    public boolean isStructureFileHidden(String structureFileName) throws IOException {
        ensureDirectories();

        AssetMetadata metadata = readMetadata(getMetadataPathForStructureFile(structureFileName));
        return metadata != null && Boolean.TRUE.equals(metadata.hidden);
    }

    public int purgeExpiredHiddenAssets(long retentionMillis) throws IOException {
        ensureDirectories();

        if (!Files.isDirectory(metadataDirectory)) {
            return 0;
        }

        long now = System.currentTimeMillis();
        int purgedCount = 0;

        try (Stream<Path> paths = Files.list(metadataDirectory)) {
            List<Path> metadataFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(JSON_EXTENSION))
                    .toList();

            for (Path metadataPath : metadataFiles) {
                AssetMetadata metadata = readMetadata(metadataPath);

                if (metadata == null || !Boolean.TRUE.equals(metadata.hidden)) {
                    continue;
                }

                long hiddenAt = metadata.hiddenAtEpochMillis == null ? 0L : metadata.hiddenAtEpochMillis;
                if (hiddenAt <= 0L || now - hiddenAt < retentionMillis) {
                    continue;
                }

                safeDeleteFileInDirectory(assetsDirectory, metadata.structureFileName);
                safeDeleteFileInDirectory(previewsDirectory, metadata.previewImageName);
                Files.deleteIfExists(metadataPath);
                purgedCount++;
            }
        }

        return purgedCount;
    }

    public ArchivAsset applyMetadataIfPresent(ArchivAsset fallbackAsset) throws IOException {
        ensureDirectories();

        String structureFileName = safeString(fallbackAsset.getStructureFileName());

        if (structureFileName.isBlank()) {
            return fallbackAsset;
        }

        AssetMetadata metadata = readMetadata(getMetadataPathForStructureFile(structureFileName));

        if (metadata == null) {
            return fallbackAsset;
        }

        return metadata.toAsset(fallbackAsset);
    }

    private AssetMetadata readMetadata(Path metadataPath) throws IOException {
        if (!Files.isRegularFile(metadataPath)) {
            return null;
        }

        try {
            String json = Files.readString(metadataPath, StandardCharsets.UTF_8);
            return GSON.fromJson(json, AssetMetadata.class);
        } catch (JsonSyntaxException exception) {
            return null;
        }
    }

    private void writeMetadata(Path metadataPath, AssetMetadata metadata) throws IOException {
        String json = GSON.toJson(metadata);
        Files.writeString(metadataPath, json, StandardCharsets.UTF_8);
    }

    private static void safeDeleteFileInDirectory(Path baseDirectory, String fileName) throws IOException {
        String cleanFileName = safeString(fileName);

        if (cleanFileName.isBlank()) {
            return;
        }

        Path normalizedBase = baseDirectory.toAbsolutePath().normalize();
        Path target = normalizedBase.resolve(cleanFileName).normalize();

        if (!target.startsWith(normalizedBase)) {
            return;
        }

        Files.deleteIfExists(target);
    }

    private static String toMetadataBaseName(String structureFileName) {
        String clean = safeString(structureFileName);

        if (clean.isBlank()) {
            clean = "asset";
        }

        int slashIndex = Math.max(clean.lastIndexOf('/'), clean.lastIndexOf('\\'));
        if (slashIndex >= 0 && slashIndex + 1 < clean.length()) {
            clean = clean.substring(slashIndex + 1);
        }

        int dotIndex = clean.lastIndexOf('.');
        if (dotIndex > 0) {
            clean = clean.substring(0, dotIndex);
        }

        clean = clean
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "");

        return clean.isBlank() ? "asset" : clean;
    }

    private static String safeString(String value) {
        return value == null ? "" : value.trim();
    }

    private static String firstNonBlank(String preferred, String fallback) {
        String cleanPreferred = safeString(preferred);
        return cleanPreferred.isBlank() ? safeString(fallback) : cleanPreferred;
    }

    private static List<String> cleanTags(List<String> rawTags) {
        Set<String> result = new LinkedHashSet<>();

        if (rawTags != null) {
            for (String rawTag : rawTags) {
                String tag = safeString(rawTag).replace("#", "");
                if (!tag.isBlank()) {
                    result.add(tag);
                }
            }
        }

        return new ArrayList<>(result);
    }

    private static class AssetMetadata {
        int metadataVersion = 1;

        String name;
        String macroCategory;
        String type;
        String version;
        String author;

        Integer previewColor;
        Integer chipColor;
        Integer variantCount;
        Boolean favorite;
        Boolean highlighted;

        Boolean hidden = false;
        Long hiddenAtEpochMillis = 0L;

        List<String> tags;

        String structureFileName;
        String structureFileFormat;
        String structureFileSize;

        String previewImageName;
        String previewImageFormat;
        String previewImageRatio;

        static AssetMetadata fromAsset(ArchivAsset asset) {
            AssetMetadata metadata = new AssetMetadata();

            metadata.name = safeString(asset.getName());
            metadata.macroCategory = safeString(asset.getMacroCategory());
            metadata.type = safeString(asset.getType());
            metadata.version = safeString(asset.getVersion());
            metadata.author = safeString(asset.getAuthor());

            metadata.previewColor = asset.getPreviewColor();
            metadata.chipColor = asset.getChipColor();
            metadata.variantCount = asset.getVariantCount();
            metadata.favorite = asset.isFavorite();
            metadata.highlighted = asset.isHighlighted();

            metadata.hidden = false;
            metadata.hiddenAtEpochMillis = 0L;

            metadata.tags = cleanTags(asset.getTags());

            metadata.structureFileName = safeString(asset.getStructureFileName());
            metadata.structureFileFormat = safeString(asset.getStructureFileFormat());
            metadata.structureFileSize = safeString(asset.getStructureFileSize());

            metadata.previewImageName = safeString(asset.getPreviewImageName());
            metadata.previewImageFormat = safeString(asset.getPreviewImageFormat());
            metadata.previewImageRatio = safeString(asset.getPreviewImageRatio());

            return metadata;
        }

        ArchivAsset toAsset(ArchivAsset fallbackAsset) {
            return new ArchivAsset(
                    firstNonBlank(name, fallbackAsset.getName()),
                    firstNonBlank(macroCategory, fallbackAsset.getMacroCategory()),
                    firstNonBlank(type, fallbackAsset.getType()),
                    firstNonBlank(version, fallbackAsset.getVersion()),
                    previewColor == null ? fallbackAsset.getPreviewColor() : previewColor,
                    chipColor == null ? fallbackAsset.getChipColor() : chipColor,
                    variantCount == null ? fallbackAsset.getVariantCount() : Math.max(1, variantCount),
                    favorite == null ? fallbackAsset.isFavorite() : favorite,
                    highlighted == null ? fallbackAsset.isHighlighted() : highlighted,
                    firstNonBlank(author, fallbackAsset.getAuthor()),
                    tags == null ? fallbackAsset.getTags() : cleanTags(tags),
                    firstNonBlank(structureFileName, fallbackAsset.getStructureFileName()),
                    firstNonBlank(structureFileFormat, fallbackAsset.getStructureFileFormat()),
                    firstNonBlank(structureFileSize, fallbackAsset.getStructureFileSize()),
                    firstNonBlank(previewImageName, fallbackAsset.getPreviewImageName()),
                    firstNonBlank(previewImageFormat, fallbackAsset.getPreviewImageFormat()),
                    firstNonBlank(previewImageRatio, fallbackAsset.getPreviewImageRatio())
            );
        }
    }
}
