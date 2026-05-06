package com.nushi.archiv.client.inspect;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ArchivStructureDataSummary {
    private final Path sourcePath;
    private final boolean readable;
    private final String format;
    private final Integer width;
    private final Integer height;
    private final Integer length;
    private final int paletteSize;
    private final long totalVolume;
    private final long nonAirBlocks;
    private final List<ArchivStructureBlockStat> topBlocks;
    private final String message;

    public ArchivStructureDataSummary(
            Path sourcePath,
            boolean readable,
            String format,
            Integer width,
            Integer height,
            Integer length,
            int paletteSize,
            long totalVolume,
            long nonAirBlocks,
            List<ArchivStructureBlockStat> topBlocks,
            String message
    ) {
        this.sourcePath = sourcePath;
        this.readable = readable;
        this.format = safe(format);
        this.width = width;
        this.height = height;
        this.length = length;
        this.paletteSize = Math.max(0, paletteSize);
        this.totalVolume = Math.max(0L, totalVolume);
        this.nonAirBlocks = Math.max(0L, nonAirBlocks);
        this.topBlocks = topBlocks == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(topBlocks));
        this.message = safe(message);
    }

    public static ArchivStructureDataSummary empty() {
        return new ArchivStructureDataSummary(
                null,
                false,
                "",
                null,
                null,
                null,
                0,
                0L,
                0L,
                Collections.emptyList(),
                ""
        );
    }

    public static ArchivStructureDataSummary failed(Path sourcePath, String format, String message) {
        return new ArchivStructureDataSummary(
                sourcePath,
                false,
                format,
                null,
                null,
                null,
                0,
                0L,
                0L,
                Collections.emptyList(),
                message
        );
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public boolean isReadable() {
        return readable;
    }

    public String getFormat() {
        return format;
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

    public int getPaletteSize() {
        return paletteSize;
    }

    public long getTotalVolume() {
        return totalVolume;
    }

    public long getNonAirBlocks() {
        return nonAirBlocks;
    }

    public List<ArchivStructureBlockStat> getTopBlocks() {
        return topBlocks;
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

    public String getTopBlockText() {
        if (topBlocks.isEmpty()) {
            return "";
        }

        ArchivStructureBlockStat topBlock = topBlocks.get(0);
        return topBlock.getShortBlockName();
    }

    public String getCompactInfo() {
        if (!readable) {
            return message;
        }

        StringBuilder builder = new StringBuilder();

        if (nonAirBlocks > 0) {
            builder.append(formatCount(nonAirBlocks)).append(" blocks");
        }

        if (paletteSize > 0) {
            if (!builder.isEmpty()) {
                builder.append(" • ");
            }

            builder.append(paletteSize).append(" palette");
        }

        String topBlock = getTopBlockText();
        if (!topBlock.isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(" • ");
            }

            builder.append("Top: ").append(topBlock);
        }

        if (!builder.isEmpty()) {
            return builder.toString();
        }

        return message;
    }

    private static String formatCount(long value) {
        if (value >= 1_000_000L) {
            return String.format(Locale.ROOT, "%.1fM", value / 1_000_000.0D);
        }

        if (value >= 1_000L) {
            return String.format(Locale.ROOT, "%.1fk", value / 1_000.0D);
        }

        return Long.toString(value);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}