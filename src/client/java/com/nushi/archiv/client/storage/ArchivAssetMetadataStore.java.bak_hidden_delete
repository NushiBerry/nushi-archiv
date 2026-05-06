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

public class ArchivAssetMetadataStore {
    private static final String ROOT_FOLDER_NAME = "archiv";
    private static final String METADATA_FOLDER_NAME = "metadata";
    private static final String JSON_EXTENSION = ".json";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final Path metadataDirectory;

    public ArchivAssetMetadataStore(Path gameDirectory) {
        this.metadataDirectory = gameDirectory
                .resolve(ROOT_FOLDER_NAME)
                .resolve(METADATA_FOLDER_NAME);
    }

    public Path getMetadataDirectory() {
        return metadataDirectory;
    }

    public void ensureDirectories() throws IOException {
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

        Path metadataPath = getMetadataPathForStructureFile(structureFileName);
        AssetMetadata metadata = AssetMetadata.fromAsset(asset);

        String json = GSON.toJson(metadata);
        Files.writeString(metadataPath, json, StandardCharsets.UTF_8);
    }

    public ArchivAsset applyMetadataIfPresent(ArchivAsset fallbackAsset) throws IOException {
        ensureDirectories();

        String structureFileName = safeString(fallbackAsset.getStructureFileName());

        if (structureFileName.isBlank()) {
            return fallbackAsset;
        }

        Path metadataPath = getMetadataPathForStructureFile(structureFileName);

        if (!Files.isRegularFile(metadataPath)) {
            return fallbackAsset;
        }

        try {
            String json = Files.readString(metadataPath, StandardCharsets.UTF_8);
            AssetMetadata metadata = GSON.fromJson(json, AssetMetadata.class);

            if (metadata == null) {
                return fallbackAsset;
            }

            return metadata.toAsset(fallbackAsset);
        } catch (JsonSyntaxException exception) {
            return fallbackAsset;
        }
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