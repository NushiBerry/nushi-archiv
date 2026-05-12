package com.nushi.archiv.client.storage;

import com.nushi.archiv.client.model.ArchivAsset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ArchivLocalLibrary {
    private static final String ROOT_FOLDER_NAME = "archiv";
    private static final String ASSETS_FOLDER_NAME = "assets";
    private static final String PREVIEWS_FOLDER_NAME = "previews";
    private static final String METADATA_FOLDER_NAME = "metadata";

    private static final List<String> SUPPORTED_STRUCTURE_EXTENSIONS = List.of(
            ".schem",
            ".schematic",
            ".litematic",
            ".bp",
            ".blueprint",
            ".bl",
            ".nbt",
            ".mcstructure"
    );

    private final Path rootDirectory;
    private final Path assetsDirectory;
    private final Path previewsDirectory;
    private final Path metadataDirectory;

    public ArchivLocalLibrary(Path gameDirectory) {
        this.rootDirectory = gameDirectory.resolve(ROOT_FOLDER_NAME);
        this.assetsDirectory = rootDirectory.resolve(ASSETS_FOLDER_NAME);
        this.previewsDirectory = rootDirectory.resolve(PREVIEWS_FOLDER_NAME);
        this.metadataDirectory = rootDirectory.resolve(METADATA_FOLDER_NAME);
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

    public Path getMetadataDirectory() {
        return metadataDirectory;
    }

    public void ensureDirectories() throws IOException {
        Files.createDirectories(rootDirectory);
        Files.createDirectories(assetsDirectory);
        Files.createDirectories(previewsDirectory);
        Files.createDirectories(metadataDirectory);
    }

    public List<DetectedStructureFile> scanStructureFiles() throws IOException {
        ensureDirectories();

        List<DetectedStructureFile> detectedFiles = new ArrayList<>();

        try (var paths = Files.walk(assetsDirectory)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(ArchivLocalLibrary::isSupportedStructureFile)
                    .map(this::toDetectedStructureFile)
                    .sorted(Comparator.comparing(DetectedStructureFile::fileName, String.CASE_INSENSITIVE_ORDER))
                    .forEach(detectedFiles::add);
        }

        return detectedFiles;
    }

    public ArchivAsset createUnconfiguredAsset(DetectedStructureFile detectedFile) {
        return new ArchivAsset(
                detectedFile.defaultAssetName(),
                "Uncategorized",
                "Unconfigured",
                "Unknown",
                0xFF4B6E9A,
                0xFF61748B,
                1,
                false,
                true,
                "Unknown",
                List.of(),
                detectedFile.fileName(),
                detectedFile.extension(),
                detectedFile.displaySize(),
                "",
                "",
                ""
        );
    }

    public List<ArchivAsset> scanAsUnconfiguredAssets() throws IOException {
        List<ArchivAsset> assets = new ArrayList<>();

        for (DetectedStructureFile detectedFile : scanStructureFiles()) {
            assets.add(createUnconfiguredAsset(detectedFile));
        }

        return assets;
    }

    private DetectedStructureFile toDetectedStructureFile(Path path) {
        String fileName = path.getFileName().toString();
        String extension = getExtension(fileName);
        long sizeBytes = getFileSizeSafely(path);

        return new DetectedStructureFile(
                path,
                assetsDirectory.relativize(path),
                fileName,
                extension,
                sizeBytes,
                toDisplaySize(sizeBytes),
                toDefaultAssetName(fileName)
        );
    }

    private static boolean isSupportedStructureFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);

        for (String extension : SUPPORTED_STRUCTURE_EXTENSIONS) {
            if (fileName.endsWith(extension)) {
                return true;
            }
        }

        return false;
    }

    private static String getExtension(String fileName) {
        String lowerName = fileName.toLowerCase(Locale.ROOT);

        for (String extension : SUPPORTED_STRUCTURE_EXTENSIONS) {
            if (lowerName.endsWith(extension)) {
                return extension;
            }
        }

        int dotIndex = fileName.lastIndexOf(".");
        return dotIndex >= 0 ? fileName.substring(dotIndex) : "";
    }

    private static long getFileSizeSafely(Path path) {
        try {
            return Files.size(path);
        } catch (IOException exception) {
            return 0L;
        }
    }

    private static String toDisplaySize(long bytes) {
        if (bytes <= 0) {
            return "Unknown";
        }

        double kb = bytes / 1024.0;
        if (kb < 1024.0) {
            return String.format(Locale.ROOT, "%.1f KB", kb);
        }

        double mb = kb / 1024.0;
        if (mb < 1024.0) {
            return String.format(Locale.ROOT, "%.2f MB", mb);
        }

        double gb = mb / 1024.0;
        return String.format(Locale.ROOT, "%.2f GB", gb);
    }

    private static String toDefaultAssetName(String fileName) {
        String nameWithoutExtension = fileName;

        for (String extension : SUPPORTED_STRUCTURE_EXTENSIONS) {
            if (nameWithoutExtension.toLowerCase(Locale.ROOT).endsWith(extension)) {
                nameWithoutExtension = nameWithoutExtension.substring(0, nameWithoutExtension.length() - extension.length());
                break;
            }
        }

        String cleaned = nameWithoutExtension
                .replace("_", " ")
                .replace("-", " ")
                .trim();

        if (cleaned.isBlank()) {
            return "Unconfigured Asset";
        }

        StringBuilder result = new StringBuilder();
        String[] words = cleaned.split("\\s+");

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }

            if (!result.isEmpty()) {
                result.append(" ");
            }

            result.append(word.substring(0, 1).toUpperCase(Locale.ROOT));

            if (word.length() > 1) {
                result.append(word.substring(1));
            }
        }

        return result.isEmpty() ? "Unconfigured Asset" : result.toString();
    }

    public static class DetectedStructureFile {
        private final Path absolutePath;
        private final Path relativePath;
        private final String fileName;
        private final String extension;
        private final long sizeBytes;
        private final String displaySize;
        private final String defaultAssetName;

        public DetectedStructureFile(
                Path absolutePath,
                Path relativePath,
                String fileName,
                String extension,
                long sizeBytes,
                String displaySize,
                String defaultAssetName
        ) {
            this.absolutePath = absolutePath;
            this.relativePath = relativePath;
            this.fileName = fileName;
            this.extension = extension;
            this.sizeBytes = sizeBytes;
            this.displaySize = displaySize;
            this.defaultAssetName = defaultAssetName;
        }

        public Path absolutePath() {
            return absolutePath;
        }

        public Path relativePath() {
            return relativePath;
        }

        public String fileName() {
            return fileName;
        }

        public String extension() {
            return extension;
        }

        public long sizeBytes() {
            return sizeBytes;
        }

        public String displaySize() {
            return displaySize;
        }

        public String defaultAssetName() {
            return defaultAssetName;
        }
    }
}