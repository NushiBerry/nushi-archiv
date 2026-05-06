package com.nushi.archiv.client.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArchivAsset {
    private final String name;
    private final String macroCategory;
    private final String type;
    private final String version;
    private final int previewColor;
    private final int chipColor;
    private final int variantCount;
    private final boolean favorite;
    private final boolean highlighted;

    private final String author;
    private final List<String> tags;
    private final String structureFileName;
    private final String structureFileFormat;
    private final String structureFileSize;
    private final String previewImageName;
    private final String previewImageFormat;
    private final String previewImageRatio;

    public ArchivAsset(
            String name,
            String macroCategory,
            String type,
            String version,
            int previewColor,
            int chipColor,
            int variantCount,
            boolean favorite,
            boolean highlighted
    ) {
        this(
                name,
                macroCategory,
                type,
                version,
                previewColor,
                chipColor,
                variantCount,
                favorite,
                highlighted,
                "Unknown",
                List.of(),
                "",
                ".schem",
                "Unknown",
                "",
                "",
                ""
        );
    }

    public ArchivAsset(
            String name,
            String macroCategory,
            String type,
            String version,
            int previewColor,
            int chipColor,
            int variantCount,
            boolean favorite,
            boolean highlighted,
            String author,
            List<String> tags,
            String structureFileName,
            String structureFileFormat,
            String structureFileSize,
            String previewImageName,
            String previewImageFormat,
            String previewImageRatio
    ) {
        this.name = clean(name, "Unnamed Asset");
        this.macroCategory = clean(macroCategory, "Uncategorized");
        this.type = clean(type, "Unknown Type");
        this.version = clean(version, "Unknown");
        this.previewColor = previewColor;
        this.chipColor = chipColor;
        this.variantCount = Math.max(1, variantCount);
        this.favorite = favorite;
        this.highlighted = highlighted;

        this.author = clean(author, "Unknown");
        this.tags = sanitizeTags(tags);
        this.structureFileName = clean(structureFileName, "");
        this.structureFileFormat = clean(structureFileFormat, "");
        this.structureFileSize = clean(structureFileSize, "");
        this.previewImageName = clean(previewImageName, "");
        this.previewImageFormat = clean(previewImageFormat, "");
        this.previewImageRatio = clean(previewImageRatio, "");
    }

    private static String clean(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private static List<String> sanitizeTags(List<String> rawTags) {
        List<String> result = new ArrayList<>();

        if (rawTags == null) {
            return result;
        }

        for (String rawTag : rawTags) {
            if (rawTag == null) {
                continue;
            }

            String tag = rawTag.trim().replace("#", "");
            if (tag.isEmpty()) {
                continue;
            }

            boolean alreadyExists = false;
            for (String existing : result) {
                if (existing.equalsIgnoreCase(tag)) {
                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists) {
                result.add(tag);
            }
        }

        return result;
    }

    public String getName() {
        return name;
    }

    public String getMacroCategory() {
        return macroCategory;
    }

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public int getPreviewColor() {
        return previewColor;
    }

    public int getChipColor() {
        return chipColor;
    }

    public int getVariantCount() {
        return variantCount;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public String getAuthor() {
        return author;
    }

    public List<String> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public String getStructureFileName() {
        return structureFileName;
    }

    public String getStructureFileFormat() {
        return structureFileFormat;
    }

    public String getStructureFileSize() {
        return structureFileSize;
    }

    public String getPreviewImageName() {
        return previewImageName;
    }

    public String getPreviewImageFormat() {
        return previewImageFormat;
    }

    public String getPreviewImageRatio() {
        return previewImageRatio;
    }
}
