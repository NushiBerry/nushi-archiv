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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class ArchivStructureVoxelReader {
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

    private static final long MAX_VOXEL_VOLUME = 8_000_000L;

    public ArchivStructureVoxelSnapshot read(Path path) {
        if (path == null) {
            return ArchivStructureVoxelSnapshot.empty();
        }

        Path normalizedPath = path.toAbsolutePath().normalize();
        String extension = getExtension(normalizedPath);

        if (!Files.isRegularFile(normalizedPath)) {
            return ArchivStructureVoxelSnapshot.failed(normalizedPath, extension, "File not found");
        }

        if (extension.equals(".schem")) {
            return readModernSchem(normalizedPath, extension);
        }

        if (extension.equals(".schematic")) {
            return readLegacySchematic(normalizedPath, extension);
        }

        return ArchivStructureVoxelSnapshot.failed(
                normalizedPath,
                extension,
                "Voxel reader not implemented for " + extension
        );
    }

    private ArchivStructureVoxelSnapshot readModernSchem(Path path, String extension) {
        try {
            Map<String, Object> root = readRootCompound(path);

            Integer width = findFirstNumberNamed(root, "Width");
            Integer height = findFirstNumberNamed(root, "Height");
            Integer length = findFirstNumberNamed(root, "Length");

            if (width == null || height == null || length == null) {
                return ArchivStructureVoxelSnapshot.failed(path, extension, "Dimensions not found");
            }

            long volume = calculateVolume(width, height, length);

            if (volume <= 0L) {
                return ArchivStructureVoxelSnapshot.failed(path, extension, "Invalid dimensions");
            }

            if (volume > MAX_VOXEL_VOLUME) {
                return ArchivStructureVoxelSnapshot.failed(
                        path,
                        extension,
                        "Structure too large for voxel snapshot: " + volume + " blocks"
                );
            }

            Map<Integer, String> palette = findPalette(root);
            byte[] blockData = findModernBlockData(root);

            if (palette.isEmpty()) {
                return ArchivStructureVoxelSnapshot.failed(path, extension, "Palette not found");
            }

            if (blockData == null || blockData.length == 0) {
                return ArchivStructureVoxelSnapshot.failed(path, extension, "BlockData not found");
            }

            int[] paletteIds = decodeModernBlockData(blockData, (int) volume);
            return buildSnapshot(path, extension, width, height, length, paletteIds, palette);
        } catch (Exception exception) {
            return ArchivStructureVoxelSnapshot.failed(
                    path,
                    extension,
                    "Voxel read failed: " + exception.getClass().getSimpleName()
            );
        }
    }

    private ArchivStructureVoxelSnapshot readLegacySchematic(Path path, String extension) {
        try {
            Map<String, Object> root = readRootCompound(path);

            Integer width = findFirstNumberNamed(root, "Width");
            Integer height = findFirstNumberNamed(root, "Height");
            Integer length = findFirstNumberNamed(root, "Length");

            if (width == null || height == null || length == null) {
                return ArchivStructureVoxelSnapshot.failed(path, extension, "Dimensions not found");
            }

            long volume = calculateVolume(width, height, length);

            if (volume <= 0L) {
                return ArchivStructureVoxelSnapshot.failed(path, extension, "Invalid dimensions");
            }

            if (volume > MAX_VOXEL_VOLUME) {
                return ArchivStructureVoxelSnapshot.failed(
                        path,
                        extension,
                        "Structure too large for voxel snapshot: " + volume + " blocks"
                );
            }

            byte[] blocks = getByteArray(root, "Blocks");
            byte[] addBlocks = getByteArray(root, "AddBlocks");

            if (blocks == null || blocks.length == 0) {
                return ArchivStructureVoxelSnapshot.failed(path, extension, "Legacy Blocks array not found");
            }

            int totalVolume = (int) volume;
            int[] paletteIds = new int[totalVolume];
            Map<Integer, String> palette = new HashMap<>();

            for (int i = 0; i < totalVolume && i < blocks.length; i++) {
                int id = blocks[i] & 0xFF;

                if (addBlocks != null) {
                    id |= getAddBlocksNibble(addBlocks, i) << 8;
                }

                paletteIds[i] = id;
                palette.putIfAbsent(id, id == 0 ? "minecraft:air" : "legacy:" + id);
            }

            return buildSnapshot(path, extension, width, height, length, paletteIds, palette);
        } catch (Exception exception) {
            return ArchivStructureVoxelSnapshot.failed(
                    path,
                    extension,
                    "Legacy voxel read failed: " + exception.getClass().getSimpleName()
            );
        }
    }

    private ArchivStructureVoxelSnapshot buildSnapshot(
            Path path,
            String extension,
            int width,
            int height,
            int length,
            int[] paletteIds,
            Map<Integer, String> palette
    ) {
        long nonAirBlocks = 0L;

        int minX = width;
        int minY = height;
        int minZ = length;
        int maxX = -1;
        int maxY = -1;
        int maxZ = -1;

        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    int index = x + (z * width) + (y * width * length);

                    if (index < 0 || index >= paletteIds.length) {
                        continue;
                    }

                    int paletteId = paletteIds[index];
                    String blockState = palette.getOrDefault(paletteId, "unknown:" + paletteId);

                    if (ArchivStructureVoxelSnapshot.isAirBlock(blockState)) {
                        continue;
                    }

                    nonAirBlocks++;

                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    minZ = Math.min(minZ, z);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                    maxZ = Math.max(maxZ, z);
                }
            }
        }

        return new ArchivStructureVoxelSnapshot(
                path,
                true,
                extension,
                width,
                height,
                length,
                paletteIds,
                palette,
                nonAirBlocks,
                minX,
                minY,
                minZ,
                maxX,
                maxY,
                maxZ,
                "Voxel snapshot read"
        );
    }

    private int[] decodeModernBlockData(byte[] blockData, int expectedVolume) throws IOException {
        int[] paletteIds = new int[expectedVolume];

        int dataIndex = 0;
        int blockIndex = 0;

        while (dataIndex < blockData.length && blockIndex < expectedVolume) {
            VarIntResult result = readVarInt(blockData, dataIndex);
            dataIndex = result.nextIndex();
            paletteIds[blockIndex] = result.value();
            blockIndex++;
        }

        return paletteIds;
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
        Map<String, Object> paletteCompound = getCompound(root, "Blocks", "Palette");

        if (paletteCompound == null) {
            paletteCompound = getCompound(root, "Palette");
        }

        if (paletteCompound == null) {
            paletteCompound = findFirstCompoundNamed(root, "Palette");
        }

        Map<Integer, String> palette = new HashMap<>();

        if (paletteCompound == null) {
            return palette;
        }

        for (Map.Entry<String, Object> entry : paletteCompound.entrySet()) {
            if (entry.getValue() instanceof Number number) {
                palette.put(number.intValue(), entry.getKey());
            }
        }

        return palette;
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

    private Integer findFirstNumberNamed(Map<String, Object> compound, String name) {
        Object direct = compound.get(name);

        if (direct instanceof Number number) {
            return number.intValue();
        }

        for (Map.Entry<String, Object> entry : compound.entrySet()) {
            Object value = entry.getValue();

            if (entry.getKey().equalsIgnoreCase(name) && value instanceof Number number) {
                return number.intValue();
            }

            if (value instanceof Map<?, ?> child) {
                Integer found = findFirstNumberNamed(castCompound(child), name);

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

    private long calculateVolume(Integer width, Integer height, Integer length) {
        if (width == null || height == null || length == null) {
            return 0L;
        }

        return Math.max(0L, (long) Math.abs(width) * (long) Math.abs(height) * (long) Math.abs(length));
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
}