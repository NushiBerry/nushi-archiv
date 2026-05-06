package com.nushi.archiv.client.inspect;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class ArchivStructureDataReader {
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

    private static final int TOP_BLOCK_LIMIT = 8;

    public ArchivStructureDataSummary read(Path path) {
        if (path == null) {
            return ArchivStructureDataSummary.empty();
        }

        Path normalizedPath = path.toAbsolutePath().normalize();
        String extension = getExtension(normalizedPath);

        if (!Files.isRegularFile(normalizedPath)) {
            return ArchivStructureDataSummary.failed(normalizedPath, extension, "File not found");
        }

        if (extension.equals(".blueprint") || extension.equals(".bl") || extension.equals(".bp")) {
            return ArchivStructureDataSummary.failed(normalizedPath, extension, "Blueprint data reader not implemented yet");
        }

        if (extension.equals(".schematic")) {
            return readLegacySchematic(normalizedPath, extension);
        }

        if (extension.equals(".schem") || extension.equals(".litematic")) {
            return readModernNbtStructure(normalizedPath, extension);
        }

        return ArchivStructureDataSummary.failed(normalizedPath, extension, "Unsupported format");
    }

    private ArchivStructureDataSummary readModernNbtStructure(Path path, String extension) {
        try {
            Map<String, Object> root = readRootCompound(path);

            Integer width = findFirstNumberNamed(root, "Width");
            Integer height = findFirstNumberNamed(root, "Height");
            Integer length = findFirstNumberNamed(root, "Length");

            if ((width == null || height == null || length == null) && extension.equals(".litematic")) {
                Dimensions dimensions = findLitematicDimensions(root);

                if (dimensions != null) {
                    width = dimensions.width();
                    height = dimensions.height();
                    length = dimensions.length();
                }
            }

            long totalVolume = calculateVolume(width, height, length);

            Map<Integer, String> idToBlockState = findPalette(root);
            byte[] blockData = findModernBlockData(root);

            if (idToBlockState.isEmpty()) {
                return new ArchivStructureDataSummary(
                        path,
                        true,
                        extension,
                        width,
                        height,
                        length,
                        0,
                        totalVolume,
                        0L,
                        List.of(),
                        "Palette not found"
                );
            }

            if (blockData == null || blockData.length == 0) {
                return new ArchivStructureDataSummary(
                        path,
                        true,
                        extension,
                        width,
                        height,
                        length,
                        idToBlockState.size(),
                        totalVolume,
                        0L,
                        List.of(),
                        "Block data not found"
                );
            }

            Map<String, Long> counts = countModernBlockData(blockData, idToBlockState, totalVolume);
            return buildSummary(path, extension, width, height, length, idToBlockState.size(), totalVolume, counts, "Structure data read");
        } catch (Exception exception) {
            return ArchivStructureDataSummary.failed(path, extension, "Structure data read failed: " + exception.getClass().getSimpleName());
        }
    }

    private ArchivStructureDataSummary readLegacySchematic(Path path, String extension) {
        try {
            Map<String, Object> root = readRootCompound(path);

            Integer width = findFirstNumberNamed(root, "Width");
            Integer height = findFirstNumberNamed(root, "Height");
            Integer length = findFirstNumberNamed(root, "Length");

            long totalVolume = calculateVolume(width, height, length);

            byte[] blocks = getByteArray(root, "Blocks");
            byte[] addBlocks = getByteArray(root, "AddBlocks");

            if (blocks == null || blocks.length == 0) {
                return new ArchivStructureDataSummary(
                        path,
                        true,
                        extension,
                        width,
                        height,
                        length,
                        0,
                        totalVolume,
                        0L,
                        List.of(),
                        "Legacy Blocks array not found"
                );
            }

            Map<String, Long> counts = new HashMap<>();

            for (int i = 0; i < blocks.length; i++) {
                int id = blocks[i] & 0xFF;

                if (addBlocks != null) {
                    id |= getAddBlocksNibble(addBlocks, i) << 8;
                }

                String blockName = id == 0 ? "minecraft:air" : "legacy:" + id;
                counts.merge(blockName, 1L, Long::sum);
            }

            return buildSummary(path, extension, width, height, length, 0, totalVolume, counts, "Legacy schematic data read");
        } catch (Exception exception) {
            return ArchivStructureDataSummary.failed(path, extension, "Legacy data read failed: " + exception.getClass().getSimpleName());
        }
    }

    private ArchivStructureDataSummary buildSummary(
            Path path,
            String extension,
            Integer width,
            Integer height,
            Integer length,
            int paletteSize,
            long totalVolume,
            Map<String, Long> counts,
            String message
    ) {
        long nonAirBlocks = 0L;

        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            if (!isAirBlock(entry.getKey())) {
                nonAirBlocks += entry.getValue();
            }
        }

        final long finalNonAirBlocks = nonAirBlocks;

        List<ArchivStructureBlockStat> topBlocks = counts.entrySet()
                .stream()
                .filter(entry -> !isAirBlock(entry.getKey()))
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(TOP_BLOCK_LIMIT)
                .map(entry -> new ArchivStructureBlockStat(
                        entry.getKey(),
                        entry.getValue(),
                        finalNonAirBlocks <= 0L ? 0.0D : (entry.getValue() * 100.0D) / finalNonAirBlocks
                ))
                .toList();

        return new ArchivStructureDataSummary(
                path,
                true,
                extension,
                width,
                height,
                length,
                paletteSize,
                totalVolume,
                nonAirBlocks,
                topBlocks,
                message
        );
    }

    private Map<String, Long> countModernBlockData(byte[] blockData, Map<Integer, String> idToBlockState, long expectedVolume) throws IOException {
        Map<String, Long> counts = new HashMap<>();

        int index = 0;
        long decodedBlocks = 0L;
        long maxBlocks = expectedVolume > 0L ? expectedVolume : Long.MAX_VALUE;

        while (index < blockData.length && decodedBlocks < maxBlocks) {
            VarIntResult result = readVarInt(blockData, index);
            index = result.nextIndex();

            String blockState = idToBlockState.getOrDefault(result.value(), "unknown:" + result.value());
            counts.merge(blockState, 1L, Long::sum);

            decodedBlocks++;
        }

        return counts;
    }

    private VarIntResult readVarInt(byte[] data, int startIndex) throws IOException {
        int value = 0;
        int shift = 0;
        int index = startIndex;

        while (index < data.length) {
            int currentByte = data[index] & 0xFF;
            index++;

            value |= (currentByte & 0x7F) << shift;

            if ((currentByte & 0x80) == 0) {
                return new VarIntResult(value, index);
            }

            shift += 7;

            if (shift > 35) {
                throw new IOException("VarInt too long");
            }
        }

        throw new EOFException("Unexpected end of VarInt block data");
    }

    private Map<Integer, String> findPalette(Map<String, Object> root) {
        Map<String, Object> palette = getCompound(root, "Blocks", "Palette");

        if (palette == null) {
            palette = getCompound(root, "Palette");
        }

        if (palette == null) {
            palette = findFirstCompoundNamed(root, "Palette");
        }

        Map<Integer, String> idToBlockState = new HashMap<>();

        if (palette == null) {
            return idToBlockState;
        }

        for (Map.Entry<String, Object> entry : palette.entrySet()) {
            if (entry.getValue() instanceof Number number) {
                idToBlockState.put(number.intValue(), entry.getKey());
            }
        }

        return idToBlockState;
    }

    private byte[] findModernBlockData(Map<String, Object> root) {
        byte[] data = getByteArray(root, "Blocks", "Data");

        if (data == null) {
            data = getByteArray(root, "Blocks", "BlockData");
        }

        if (data == null) {
            data = getByteArray(root, "BlockData");
        }

        if (data == null) {
            data = getByteArray(root, "Data");
        }

        if (data == null) {
            data = findFirstByteArrayNamed(root, "BlockData");
        }

        if (data == null) {
            data = findFirstByteArrayNamed(root, "Data");
        }

        return data;
    }

    private Map<String, Object> readRootCompound(Path path) throws IOException {
        try (DataInputStream input = openNbtInput(path)) {
            int rootType = input.readUnsignedByte();

            if (rootType != TAG_COMPOUND) {
                throw new IOException("Root NBT tag is not a compound");
            }

            readNbtString(input);

            return readCompoundPayload(input);
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

    private Map<String, Object> readCompoundPayload(DataInputStream input) throws IOException {
        Map<String, Object> compound = new LinkedHashMap<>();

        while (true) {
            int type = input.readUnsignedByte();

            if (type == TAG_END) {
                return compound;
            }

            String name = readNbtString(input);
            compound.put(name, readPayload(input, type));
        }
    }

    private Object readPayload(DataInputStream input, int type) throws IOException {
        return switch (type) {
            case TAG_BYTE -> input.readByte();
            case TAG_SHORT -> input.readShort();
            case TAG_INT -> input.readInt();
            case TAG_LONG -> input.readLong();
            case TAG_FLOAT -> input.readFloat();
            case TAG_DOUBLE -> input.readDouble();
            case TAG_BYTE_ARRAY -> {
                int length = input.readInt();
                byte[] bytes = new byte[length];
                input.readFully(bytes);
                yield bytes;
            }
            case TAG_STRING -> readNbtString(input);
            case TAG_LIST -> readListPayload(input);
            case TAG_COMPOUND -> readCompoundPayload(input);
            case TAG_INT_ARRAY -> {
                int length = input.readInt();
                int[] values = new int[length];

                for (int i = 0; i < length; i++) {
                    values[i] = input.readInt();
                }

                yield values;
            }
            case TAG_LONG_ARRAY -> {
                int length = input.readInt();
                long[] values = new long[length];

                for (int i = 0; i < length; i++) {
                    values[i] = input.readLong();
                }

                yield values;
            }
            default -> throw new IOException("Unsupported NBT tag type: " + type);
        };
    }

    private List<Object> readListPayload(DataInputStream input) throws IOException {
        int elementType = input.readUnsignedByte();
        int length = input.readInt();

        if (length < 0) {
            throw new IOException("Negative NBT list length");
        }

        List<Object> list = new ArrayList<>(Math.min(length, 1024));

        for (int i = 0; i < length; i++) {
            list.add(readPayload(input, elementType));
        }

        return list;
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

    private int getAddBlocksNibble(byte[] addBlocks, int blockIndex) {
        int packedIndex = blockIndex / 2;

        if (packedIndex < 0 || packedIndex >= addBlocks.length) {
            return 0;
        }

        int packed = addBlocks[packedIndex] & 0xFF;

        if ((blockIndex & 1) == 0) {
            return packed & 0x0F;
        }

        return (packed >> 4) & 0x0F;
    }

    private Integer findFirstNumberNamed(Map<String, Object> compound, String name) {
        Object direct = compound.get(name);

        if (direct instanceof Number number) {
            return number.intValue();
        }

        String cleanName = name.toLowerCase(Locale.ROOT);

        for (Map.Entry<String, Object> entry : compound.entrySet()) {
            Object value = entry.getValue();

            if (entry.getKey().equalsIgnoreCase(name) && value instanceof Number number) {
                return number.intValue();
            }

            if (value instanceof Map<?, ?> child) {
                Integer found = findFirstNumberNamed(castCompound(child), cleanName);

                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    private byte[] findFirstByteArrayNamed(Map<String, Object> compound, String name) {
        for (Map.Entry<String, Object> entry : compound.entrySet()) {
            Object value = entry.getValue();

            if (entry.getKey().equalsIgnoreCase(name) && value instanceof byte[] bytes) {
                return bytes;
            }

            if (value instanceof Map<?, ?> child) {
                byte[] found = findFirstByteArrayNamed(castCompound(child), name);

                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    private Map<String, Object> findFirstCompoundNamed(Map<String, Object> compound, String name) {
        for (Map.Entry<String, Object> entry : compound.entrySet()) {
            Object value = entry.getValue();

            if (entry.getKey().equalsIgnoreCase(name) && value instanceof Map<?, ?> child) {
                return castCompound(child);
            }

            if (value instanceof Map<?, ?> child) {
                Map<String, Object> found = findFirstCompoundNamed(castCompound(child), name);

                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    private Dimensions findLitematicDimensions(Map<String, Object> root) {
        Map<String, Object> regions = getCompound(root, "Regions");

        if (regions == null) {
            regions = findFirstCompoundNamed(root, "Regions");
        }

        if (regions == null || regions.isEmpty()) {
            return null;
        }

        int maxWidth = 0;
        int maxHeight = 0;
        int maxLength = 0;

        for (Object regionObject : regions.values()) {
            if (!(regionObject instanceof Map<?, ?> regionMap)) {
                continue;
            }

            Map<String, Object> region = castCompound(regionMap);
            Map<String, Object> size = getCompound(region, "Size");

            if (size == null) {
                continue;
            }

            Integer x = getNumberAsInt(size, "x");
            Integer y = getNumberAsInt(size, "y");
            Integer z = getNumberAsInt(size, "z");

            if (x != null) {
                maxWidth = Math.max(maxWidth, Math.abs(x));
            }

            if (y != null) {
                maxHeight = Math.max(maxHeight, Math.abs(y));
            }

            if (z != null) {
                maxLength = Math.max(maxLength, Math.abs(z));
            }
        }

        if (maxWidth > 0 && maxHeight > 0 && maxLength > 0) {
            return new Dimensions(maxWidth, maxHeight, maxLength);
        }

        return null;
    }

    private Integer getNumberAsInt(Map<String, Object> compound, String key) {
        Object value = compound.get(key);

        if (value instanceof Number number) {
            return number.intValue();
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castCompound(Map<?, ?> map) {
        return (Map<String, Object>) map;
    }

    private Map<String, Object> getCompound(Map<String, Object> compound, String... path) {
        Object current = compound;

        for (String part : path) {
            if (!(current instanceof Map<?, ?> currentMap)) {
                return null;
            }

            current = castCompound(currentMap).get(part);
        }

        if (current instanceof Map<?, ?> result) {
            return castCompound(result);
        }

        return null;
    }

    private byte[] getByteArray(Map<String, Object> compound, String... path) {
        Object current = compound;

        for (String part : path) {
            if (!(current instanceof Map<?, ?> currentMap)) {
                return null;
            }

            current = castCompound(currentMap).get(part);
        }

        return current instanceof byte[] bytes ? bytes : null;
    }

    private long calculateVolume(Integer width, Integer height, Integer length) {
        if (width == null || height == null || length == null) {
            return 0L;
        }

        return Math.max(0L, (long) Math.abs(width) * (long) Math.abs(height) * (long) Math.abs(length));
    }

    private boolean isAirBlock(String blockState) {
        String clean = blockState == null ? "" : blockState.toLowerCase(Locale.ROOT);

        return clean.equals("minecraft:air")
                || clean.equals("minecraft:cave_air")
                || clean.equals("minecraft:void_air")
                || clean.equals("legacy:0")
                || clean.endsWith(":air")
                || clean.endsWith(":cave_air")
                || clean.endsWith(":void_air");
    }

    private String getExtension(Path path) {
        String fileName = path.getFileName() == null ? "" : path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex < 0 || dotIndex >= fileName.length() - 1) {
            return "";
        }

        return fileName.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    private record VarIntResult(int value, int nextIndex) {
    }

    private record Dimensions(int width, int height, int length) {
    }
}