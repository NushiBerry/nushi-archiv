package com.nushi.archiv.client.inspect;

import java.nio.file.Path;

public class ArchivAssetFileInspection {
    private final Path sourcePath;
    private final String fileName;
    private final String extension;
    private final long fileSizeBytes;

    private final boolean supported;
    private final boolean readable;
    private final boolean nbtLike;

    private final Integer width;
    private final Integer height;
    private final Integer length;

    private final Integer schematicVersion;
    private final Integer dataVersion;
    private final Integer regionCount;

    private final String internalName;
    private final String message;

    public ArchivAssetFileInspection(
            Path sourcePath,
            String fileName,
            String extension,
            long fileSizeBytes,
            boolean supported,
            boolean readable,
            boolean nbtLike,
            Integer width,
            Integer height,
            Integer length,
            Integer schematicVersion,
            Integer dataVersion,
            Integer regionCount,
            String internalName,
            String message
    ) {
        this.sourcePath = sourcePath;
        this.fileName = safe(fileName);
        this.extension = safe(extension);
        this.fileSizeBytes = fileSizeBytes;
        this.supported = supported;
        this.readable = readable;
        this.nbtLike = nbtLike;
        this.width = width;
        this.height = height;
        this.length = length;
        this.schematicVersion = schematicVersion;
        this.dataVersion = dataVersion;
        this.regionCount = regionCount;
        this.internalName = safe(internalName);
        this.message = safe(message);
    }

    public static ArchivAssetFileInspection empty() {
        return new ArchivAssetFileInspection(
                null,
                "",
                "",
                0L,
                false,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                "",
                ""
        );
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getExtension() {
        return extension;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public boolean isSupported() {
        return supported;
    }

    public boolean isReadable() {
        return readable;
    }

    public boolean isNbtLike() {
        return nbtLike;
    }

    public boolean hasDimensions() {
        return width != null && height != null && length != null;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public Integer getLength() {
        return length;
    }

    public Integer getSchematicVersion() {
        return schematicVersion;
    }

    public Integer getDataVersion() {
        return dataVersion;
    }

    public Integer getRegionCount() {
        return regionCount;
    }

    public String getInternalName() {
        return internalName;
    }

    public String getMessage() {
        return message;
    }

    public String getDimensionsText() {
        if (!hasDimensions()) {
            return "";
        }

        return width + "×" + height + "×" + length;
    }

    public String getCompactInfo() {
        StringBuilder builder = new StringBuilder();

        if (hasDimensions()) {
            builder.append(getDimensionsText());
        }

        if (regionCount != null && regionCount > 0) {
            if (!builder.isEmpty()) {
                builder.append(" • ");
            }

            builder.append(regionCount).append(regionCount == 1 ? " region" : " regions");
        }

        if (schematicVersion != null) {
            if (!builder.isEmpty()) {
                builder.append(" • ");
            }

            builder.append("Schem v").append(schematicVersion);
        }

        if (dataVersion != null) {
            if (!builder.isEmpty()) {
                builder.append(" • ");
            }

            builder.append("Data ").append(dataVersion);
        }

        return builder.toString();
    }

    public String getStatusMessage() {
        if (!supported) {
            return "Unsupported file";
        }

        if (!readable) {
            return message.isBlank() ? "Could not read file" : message;
        }

        String compactInfo = getCompactInfo();

        if (!compactInfo.isBlank()) {
            return compactInfo;
        }

        return message.isBlank() ? "File inspected" : message;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}