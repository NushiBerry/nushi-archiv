package com.nushi.archiv.client.inspect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class ArchivStructureDataCache {
    private static final String CACHE_VERSION = "1";

    private final Path cacheDirectory;
    private final ArchivStructureDataReader reader;

    public ArchivStructureDataCache(Path gameDirectory, ArchivStructureDataReader reader) {
        this.cacheDirectory = gameDirectory
                .resolve("archiv")
                .resolve("cache")
                .resolve("structure-data");

        this.reader = reader;
    }

    public ArchivStructureDataSummary read(Path sourcePath) {
        if (sourcePath == null) {
            return ArchivStructureDataSummary.empty();
        }

        Path normalizedPath = sourcePath.toAbsolutePath().normalize();

        if (!Files.isRegularFile(normalizedPath)) {
            return reader.read(normalizedPath);
        }

        try {
            Files.createDirectories(cacheDirectory);

            long modifiedMillis = Files.getLastModifiedTime(normalizedPath).toMillis();
            long sizeBytes = Files.size(normalizedPath);
            Path cachePath = getCachePath(normalizedPath);

            ArchivStructureDataSummary cached = readFromCache(cachePath, normalizedPath, modifiedMillis, sizeBytes);

            if (cached != null) {
                return cached;
            }

            ArchivStructureDataSummary fresh = reader.read(normalizedPath);

            if (fresh != null && fresh.isReadable()) {
                writeToCache(cachePath, fresh, modifiedMillis, sizeBytes);
            }

            return fresh;
        } catch (IOException exception) {
            return reader.read(normalizedPath);
        }
    }

    private Path getCachePath(Path sourcePath) {
        String fileName = sourcePath.getFileName() == null
                ? "structure"
                : sourcePath.getFileName().toString();

        String baseName = stripExtension(fileName)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "");

        if (baseName.isBlank()) {
            baseName = "structure";
        }

        String hash = Integer.toHexString(sourcePath.toString().toLowerCase(Locale.ROOT).hashCode());

        return cacheDirectory.resolve(baseName + "_" + hash + ".properties");
    }

    private ArchivStructureDataSummary readFromCache(
            Path cachePath,
            Path sourcePath,
            long modifiedMillis,
            long sizeBytes
    ) {
        if (!Files.isRegularFile(cachePath)) {
            return null;
        }

        Properties properties = new Properties();

        try (InputStream inputStream = Files.newInputStream(cachePath)) {
            properties.load(inputStream);
        } catch (IOException exception) {
            return null;
        }

        if (!CACHE_VERSION.equals(properties.getProperty("cache.version", ""))) {
            return null;
        }

        if (parseLong(properties.getProperty("source.modifiedMillis"), -1L) != modifiedMillis) {
            return null;
        }

        if (parseLong(properties.getProperty("source.sizeBytes"), -1L) != sizeBytes) {
            return null;
        }

        return new ArchivStructureDataSummary(
                sourcePath,
                Boolean.parseBoolean(properties.getProperty("readable", "false")),
                properties.getProperty("format", ""),
                parseIntegerNullable(properties.getProperty("width")),
                parseIntegerNullable(properties.getProperty("height")),
                parseIntegerNullable(properties.getProperty("length")),
                parseInt(properties.getProperty("paletteSize"), 0),
                parseLong(properties.getProperty("totalVolume"), 0L),
                parseLong(properties.getProperty("nonAirBlocks"), 0L),
                decodeTopBlocks(properties.getProperty("topBlocks", "")),
                properties.getProperty("message", "")
        );
    }

    private void writeToCache(
            Path cachePath,
            ArchivStructureDataSummary summary,
            long modifiedMillis,
            long sizeBytes
    ) throws IOException {
        Files.createDirectories(cachePath.getParent());

        Properties properties = new Properties();

        properties.setProperty("cache.version", CACHE_VERSION);
        properties.setProperty("source.modifiedMillis", Long.toString(modifiedMillis));
        properties.setProperty("source.sizeBytes", Long.toString(sizeBytes));

        properties.setProperty("readable", Boolean.toString(summary.isReadable()));
        properties.setProperty("format", summary.getFormat());
        properties.setProperty("width", nullableIntToString(summary.getWidth()));
        properties.setProperty("height", nullableIntToString(summary.getHeight()));
        properties.setProperty("length", nullableIntToString(summary.getLength()));
        properties.setProperty("paletteSize", Integer.toString(summary.getPaletteSize()));
        properties.setProperty("totalVolume", Long.toString(summary.getTotalVolume()));
        properties.setProperty("nonAirBlocks", Long.toString(summary.getNonAirBlocks()));
        properties.setProperty("topBlocks", encodeTopBlocks(summary.getTopBlocks()));
        properties.setProperty("message", summary.getMessage());

        Path tempPath = cachePath.resolveSibling(cachePath.getFileName().toString() + ".tmp");

        try (OutputStream outputStream = Files.newOutputStream(tempPath)) {
            properties.store(outputStream, "Archiv structure data cache");
        }

        try {
            Files.move(tempPath, cachePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            Files.move(tempPath, cachePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String encodeTopBlocks(List<ArchivStructureBlockStat> topBlocks) {
        if (topBlocks == null || topBlocks.isEmpty()) {
            return "";
        }

        List<String> encoded = new ArrayList<>();

        for (ArchivStructureBlockStat stat : topBlocks) {
            String blockState = Base64.getUrlEncoder().withoutPadding().encodeToString(
                    stat.getBlockState().getBytes(StandardCharsets.UTF_8)
            );

            encoded.add(blockState + "," + stat.getCount() + "," + stat.getPercentOfNonAir());
        }

        return String.join(";", encoded);
    }

    private List<ArchivStructureBlockStat> decodeTopBlocks(String encodedText) {
        List<ArchivStructureBlockStat> result = new ArrayList<>();

        if (encodedText == null || encodedText.isBlank()) {
            return result;
        }

        String[] entries = encodedText.split(";");

        for (String entry : entries) {
            String[] parts = entry.split(",");

            if (parts.length != 3) {
                continue;
            }

            try {
                String blockState = new String(
                        Base64.getUrlDecoder().decode(parts[0]),
                        StandardCharsets.UTF_8
                );

                long count = Long.parseLong(parts[1]);
                double percent = Double.parseDouble(parts[2]);

                result.add(new ArchivStructureBlockStat(blockState, count, percent));
            } catch (Exception ignored) {
            }
        }

        return result;
    }

    private String stripExtension(String fileName) {
        String clean = fileName == null ? "" : fileName.trim();
        int dotIndex = clean.lastIndexOf(".");

        if (dotIndex <= 0) {
            return clean;
        }

        return clean.substring(0, dotIndex);
    }

    private String nullableIntToString(Integer value) {
        return value == null ? "" : Integer.toString(value);
    }

    private Integer parseIntegerNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception exception) {
            return fallback;
        }
    }

    private long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (Exception exception) {
            return fallback;
        }
    }
}