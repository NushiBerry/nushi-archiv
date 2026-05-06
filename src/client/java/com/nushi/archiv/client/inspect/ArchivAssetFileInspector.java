package com.nushi.archiv.client.inspect;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class ArchivAssetFileInspector {
    private static final int TAG_END = 0;
    private static final int TAG_BYTE = 1;
    private static final int TAG_SHORT = 2;
    private static final int TAG_INT = 3;
    private static final int TAG_LONG = 4;
    private static final int TAG_FLOAT = 5;
    private static final int TAG_DOUBLE = 6;
    private static final int TAG_BYTE_ARRAY = 7;
    private static final int TAG_STRING = 8;
    private static final int TAG_LIST = 9;
    private static final int TAG_COMPOUND = 10;
    private static final int TAG_INT_ARRAY = 11;
    private static final int TAG_LONG_ARRAY = 12;

    private static final int MAX_DEPTH = 64;

    public ArchivAssetFileInspection inspect(Path path) {
        if (path == null) {
            return ArchivAssetFileInspection.empty();
        }

        Path normalizedPath = path.toAbsolutePath().normalize();
        String fileName = normalizedPath.getFileName() == null ? "" : normalizedPath.getFileName().toString();
        String extension = getExtension(fileName);
        long fileSizeBytes = getFileSize(normalizedPath);

        if (!Files.isRegularFile(normalizedPath)) {
            return new ArchivAssetFileInspection(
                    normalizedPath,
                    fileName,
                    extension,
                    fileSizeBytes,
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
                    "File not found"
            );
        }

        if (!isSupportedStructureExtension(extension)) {
            return new ArchivAssetFileInspection(
                    normalizedPath,
                    fileName,
                    extension,
                    fileSizeBytes,
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
                    "Unsupported structure file"
            );
        }

        if (isBlueprintExtension(extension)) {
            return new ArchivAssetFileInspection(
                    normalizedPath,
                    fileName,
                    extension,
                    fileSizeBytes,
                    true,
                    false,
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "",
                    "Blueprint inspection not implemented yet"
            );
        }

        try {
            NbtSummary summary = readNbtSummary(normalizedPath);
            Integer width = summary.findFirstNumberNamed("Width");
            Integer height = summary.findFirstNumberNamed("Height");
            Integer length = summary.findFirstNumberNamed("Length");

            if (width == null || height == null || length == null) {
                LitematicDimensions litematicDimensions = summary.findLitematicDimensions();

                if (litematicDimensions != null) {
                    width = litematicDimensions.width();
                    height = litematicDimensions.height();
                    length = litematicDimensions.length();
                }
            }

            Integer regionCount = summary.getRegionCount();
            Integer schematicVersion = firstNonNull(
                    summary.getNumberAsInt("Version"),
                    summary.getNumberAsInt("Schematic.Version"),
                    summary.findFirstNumberEnding(".Version")
            );
            Integer dataVersion = firstNonNull(
                    summary.getNumberAsInt("DataVersion"),
                    summary.getNumberAsInt("MinecraftDataVersion"),
                    summary.findFirstNumberEnding(".DataVersion")
            );

            String internalName = firstNonBlank(
                    summary.getString("Metadata.Name"),
                    summary.getString("Name"),
                    summary.findFirstStringEnding(".Name")
            );

            boolean hasDimensions = width != null && height != null && length != null;

            return new ArchivAssetFileInspection(
                    normalizedPath,
                    fileName,
                    extension,
                    fileSizeBytes,
                    true,
                    true,
                    true,
                    width,
                    height,
                    length,
                    schematicVersion,
                    dataVersion,
                    regionCount,
                    internalName,
                    hasDimensions ? "Structure inspected" : "NBT read, dimensions not found"
            );
        } catch (Exception exception) {
            return new ArchivAssetFileInspection(
                    normalizedPath,
                    fileName,
                    extension,
                    fileSizeBytes,
                    true,
                    false,
                    true,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "",
                    "NBT inspect failed: " + exception.getClass().getSimpleName()
            );
        }
    }

    private NbtSummary readNbtSummary(Path path) throws IOException {
        try (DataInputStream input = openNbtInput(path)) {
            int rootType = input.readUnsignedByte();

            if (rootType != TAG_COMPOUND) {
                throw new IOException("Root NBT tag is not a compound");
            }

            readNbtString(input);

            NbtSummary summary = new NbtSummary();
            readCompoundPayload(input, "", 0, summary);
            return summary;
        }
    }

    private DataInputStream openNbtInput(Path path) throws IOException {
        BufferedInputStream bufferedInput = new BufferedInputStream(Files.newInputStream(path));
        bufferedInput.mark(2);

        int firstByte = bufferedInput.read();
        int secondByte = bufferedInput.read();

        bufferedInput.reset();

        InputStream input = firstByte == 0x1F && secondByte == 0x8B
                ? new GZIPInputStream(bufferedInput)
                : bufferedInput;

        return new DataInputStream(new BufferedInputStream(input));
    }

    private void readCompoundPayload(DataInputStream input, String path, int depth, NbtSummary summary) throws IOException {
        if (depth > MAX_DEPTH) {
            throw new IOException("NBT nesting too deep");
        }

        while (true) {
            int type = input.readUnsignedByte();

            if (type == TAG_END) {
                return;
            }

            String name = readNbtString(input);
            String childPath = path.isBlank() ? name : path + "." + name;

            readPayload(input, type, childPath, depth + 1, summary);
        }
    }

    private void readPayload(DataInputStream input, int type, String path, int depth, NbtSummary summary) throws IOException {
        switch (type) {
            case TAG_BYTE -> {
                byte value = input.readByte();
                summary.putNumber(path, value);
            }
            case TAG_SHORT -> {
                short value = input.readShort();
                summary.putNumber(path, value);
            }
            case TAG_INT -> {
                int value = input.readInt();
                summary.putNumber(path, value);
            }
            case TAG_LONG -> {
                long value = input.readLong();
                summary.putNumber(path, value);
            }
            case TAG_FLOAT -> input.readFloat();
            case TAG_DOUBLE -> input.readDouble();
            case TAG_BYTE_ARRAY -> {
                int length = input.readInt();
                skipFully(input, length);
            }
            case TAG_STRING -> {
                String value = readNbtString(input);
                summary.putString(path, value);
            }
            case TAG_LIST -> readListPayload(input, path, depth, summary);
            case TAG_COMPOUND -> readCompoundPayload(input, path, depth, summary);
            case TAG_INT_ARRAY -> {
                int length = input.readInt();
                skipFully(input, length * 4L);
            }
            case TAG_LONG_ARRAY -> {
                int length = input.readInt();
                skipFully(input, length * 8L);
            }
            default -> throw new IOException("Unsupported NBT tag type: " + type);
        }
    }

    private void readListPayload(DataInputStream input, String path, int depth, NbtSummary summary) throws IOException {
        int elementType = input.readUnsignedByte();
        int length = input.readInt();

        if (length < 0) {
            throw new IOException("Negative NBT list length");
        }

        for (int i = 0; i < length; i++) {
            String itemPath = path + "[" + i + "]";
            readPayload(input, elementType, itemPath, depth + 1, summary);
        }
    }

    private String readNbtString(DataInputStream input) throws IOException {
        int length = input.readUnsignedShort();

        if (length == 0) {
            return "";
        }

        byte[] bytes = new byte[length];
        input.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private void skipFully(InputStream input, long byteCount) throws IOException {
        if (byteCount < 0) {
            throw new IOException("Negative skip length");
        }

        long remaining = byteCount;

        while (remaining > 0) {
            long skipped = input.skip(remaining);

            if (skipped <= 0) {
                if (input.read() == -1) {
                    throw new EOFException("Unexpected end of NBT data");
                }

                skipped = 1;
            }

            remaining -= skipped;
        }
    }

    private boolean isSupportedStructureExtension(String extension) {
        String clean = safe(extension).toLowerCase(Locale.ROOT);
        return clean.equals(".schem")
                || clean.equals(".schematic")
                || clean.equals(".litematic")
                || clean.equals(".blueprint")
                || clean.equals(".bl")
                || clean.equals(".bp");
    }

    private boolean isBlueprintExtension(String extension) {
        String clean = safe(extension).toLowerCase(Locale.ROOT);
        return clean.equals(".blueprint")
                || clean.equals(".bl")
                || clean.equals(".bp");
    }

    private String getExtension(String fileName) {
        String clean = safe(fileName);
        int dotIndex = clean.lastIndexOf(".");

        if (dotIndex < 0 || dotIndex >= clean.length() - 1) {
            return "";
        }

        return clean.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    private long getFileSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException exception) {
            return 0L;
        }
    }

    private static Integer firstNonNull(Integer... values) {
        for (Integer value : values) {
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isBlank()) {
                return value.trim();
            }
        }

        return "";
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private record LitematicDimensions(int width, int height, int length) {
    }

    private static class NbtSummary {
        private final Map<String, Number> numbers = new HashMap<>();
        private final Map<String, String> strings = new HashMap<>();

        void putNumber(String path, Number value) {
            if (path == null || path.isBlank() || value == null) {
                return;
            }

            if (shouldKeepNumber(path)) {
                numbers.put(path, value);
            }
        }

        void putString(String path, String value) {
            if (path == null || path.isBlank() || value == null || value.isBlank()) {
                return;
            }

            if (shouldKeepString(path)) {
                strings.put(path, value);
            }
        }

        Integer getNumberAsInt(String path) {
            Number number = numbers.get(path);

            if (number == null) {
                return null;
            }

            long value = number.longValue();

            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                return null;
            }

            return (int) value;
        }

        String getString(String path) {
            return strings.getOrDefault(path, "");
        }

        Integer findFirstNumberEnding(String suffix) {
            String cleanSuffix = suffix.toLowerCase(Locale.ROOT);

            for (Map.Entry<String, Number> entry : numbers.entrySet()) {
                if (entry.getKey().toLowerCase(Locale.ROOT).endsWith(cleanSuffix)) {
                    long value = entry.getValue().longValue();

                    if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                        return (int) value;
                    }
                }
            }

            return null;
        }

        Integer findFirstNumberNamed(String name) {
            String cleanName = safe(name).toLowerCase(Locale.ROOT);

            if (cleanName.isBlank()) {
                return null;
            }

            for (Map.Entry<String, Number> entry : numbers.entrySet()) {
                String key = entry.getKey().toLowerCase(Locale.ROOT);

                if (key.equals(cleanName) || key.endsWith("." + cleanName)) {
                    long value = entry.getValue().longValue();

                    if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                        return (int) value;
                    }
                }
            }

            return null;
        }

        String findFirstStringEnding(String suffix) {
            String cleanSuffix = suffix.toLowerCase(Locale.ROOT);

            for (Map.Entry<String, String> entry : strings.entrySet()) {
                if (entry.getKey().toLowerCase(Locale.ROOT).endsWith(cleanSuffix)) {
                    return entry.getValue();
                }
            }

            return "";
        }

        Integer getRegionCount() {
            int count = 0;

            for (String path : numbers.keySet()) {
                String lower = path.toLowerCase(Locale.ROOT);

                if (lower.contains(".regions.") && lower.endsWith(".size.x")) {
                    count++;
                }
            }

            return count > 0 ? count : null;
        }

        LitematicDimensions findLitematicDimensions() {
            int maxWidth = 0;
            int maxHeight = 0;
            int maxLength = 0;

            for (String path : numbers.keySet()) {
                String lower = path.toLowerCase(Locale.ROOT);

                if (!lower.contains(".regions.") || !lower.contains(".size.")) {
                    continue;
                }

                if (lower.endsWith(".size.x")) {
                    maxWidth = Math.max(maxWidth, Math.abs(numbers.get(path).intValue()));
                } else if (lower.endsWith(".size.y")) {
                    maxHeight = Math.max(maxHeight, Math.abs(numbers.get(path).intValue()));
                } else if (lower.endsWith(".size.z")) {
                    maxLength = Math.max(maxLength, Math.abs(numbers.get(path).intValue()));
                }
            }

            if (maxWidth > 0 && maxHeight > 0 && maxLength > 0) {
                return new LitematicDimensions(maxWidth, maxHeight, maxLength);
            }

            return null;
        }

        private static boolean shouldKeepNumber(String path) {
            String lower = path.toLowerCase(Locale.ROOT);

            return lower.equals("width")
                    || lower.equals("height")
                    || lower.equals("length")
                    || lower.equals("version")
                    || lower.equals("dataversion")
                    || lower.equals("minecraftdataversion")
                    || lower.endsWith(".width")
                    || lower.endsWith(".height")
                    || lower.endsWith(".length")
                    || lower.endsWith(".version")
                    || lower.endsWith(".dataversion")
                    || lower.endsWith(".minecraftdataversion")
                    || lower.endsWith(".size.x")
                    || lower.endsWith(".size.y")
                    || lower.endsWith(".size.z");
        }

        private static boolean shouldKeepString(String path) {
            String lower = path.toLowerCase(Locale.ROOT);

            return lower.equals("name")
                    || lower.equals("author")
                    || lower.endsWith(".name")
                    || lower.endsWith(".author")
                    || lower.endsWith(".description");
        }
    }
}