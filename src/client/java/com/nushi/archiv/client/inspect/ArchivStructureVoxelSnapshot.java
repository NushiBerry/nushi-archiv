package com.nushi.archiv.client.inspect;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ArchivStructureVoxelSnapshot {
    private final Path sourcePath;
    private final boolean readable;
    private final String format;
    private final int width;
    private final int height;
    private final int length;
    private final int[] paletteIds;
    private final Map<Integer, String> palette;
    private final long nonAirBlocks;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private final String message;

    public ArchivStructureVoxelSnapshot(
            Path sourcePath,
            boolean readable,
            String format,
            int width,
            int height,
            int length,
            int[] paletteIds,
            Map<Integer, String> palette,
            long nonAirBlocks,
            int minX,
            int minY,
            int minZ,
            int maxX,
            int maxY,
            int maxZ,
            String message
    ) {
        this.sourcePath = sourcePath;
        this.readable = readable;
        this.format = safe(format);
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
        this.length = Math.max(0, length);
        this.paletteIds = paletteIds == null ? new int[0] : paletteIds;
        this.palette = palette == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(palette));
        this.nonAirBlocks = Math.max(0L, nonAirBlocks);
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.message = safe(message);
    }

    public static ArchivStructureVoxelSnapshot empty() {
        return new ArchivStructureVoxelSnapshot(
                null,
                false,
                "",
                0,
                0,
                0,
                new int[0],
                Collections.emptyMap(),
                0L,
                0,
                0,
                0,
                -1,
                -1,
                -1,
                ""
        );
    }

    public static ArchivStructureVoxelSnapshot failed(Path sourcePath, String format, String message) {
        return new ArchivStructureVoxelSnapshot(
                sourcePath,
                false,
                format,
                0,
                0,
                0,
                new int[0],
                Collections.emptyMap(),
                0L,
                0,
                0,
                0,
                -1,
                -1,
                -1,
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return length;
    }

    public int[] getPaletteIds() {
        return paletteIds;
    }

    public Map<Integer, String> getPalette() {
        return palette;
    }

    public int getPaletteSize() {
        return palette.size();
    }

    public long getTotalVolume() {
        return (long) width * (long) height * (long) length;
    }

    public long getNonAirBlocks() {
        return nonAirBlocks;
    }

    public boolean hasUsefulBounds() {
        return maxX >= minX && maxY >= minY && maxZ >= minZ;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public String getMessage() {
        return message;
    }

    public int getBlockStateId(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= length) {
            return -1;
        }

        int index = getIndex(x, y, z);

        if (index < 0 || index >= paletteIds.length) {
            return -1;
        }

        return paletteIds[index];
    }

    public String getBlockState(int x, int y, int z) {
        int id = getBlockStateId(x, y, z);

        if (id < 0) {
            return "";
        }

        return palette.getOrDefault(id, "unknown:" + id);
    }

    public int getIndex(int x, int y, int z) {
        return x + (z * width) + (y * width * length);
    }

    public String getDimensionsText() {
        if (width <= 0 || height <= 0 || length <= 0) {
            return "";
        }

        return width + "×" + height + "×" + length;
    }

    public String getUsefulDimensionsText() {
        if (!hasUsefulBounds()) {
            return "";
        }

        int usefulWidth = maxX - minX + 1;
        int usefulHeight = maxY - minY + 1;
        int usefulLength = maxZ - minZ + 1;

        return usefulWidth + "×" + usefulHeight + "×" + usefulLength;
    }

    public String getCompactInfo() {
        if (!readable) {
            return message;
        }

        if (hasUsefulBounds()) {
            return "Voxel snapshot ready • Bounds " + getUsefulDimensionsText();
        }

        return "Voxel snapshot ready";
    }

    public static boolean isAirBlock(String blockState) {
        String clean = blockState == null ? "" : blockState.toLowerCase(Locale.ROOT);

        return clean.equals("minecraft:air")
                || clean.equals("minecraft:cave_air")
                || clean.equals("minecraft:void_air")
                || clean.equals("legacy:0")
                || clean.endsWith(":air")
                || clean.endsWith(":cave_air")
                || clean.endsWith(":void_air");
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}