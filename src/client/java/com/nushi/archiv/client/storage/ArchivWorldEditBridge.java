package com.nushi.archiv.client.storage;

import com.nushi.archiv.client.model.ArchivAsset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.stream.Stream;

public class ArchivWorldEditBridge {
    private static final String CONFIG_FOLDER_NAME = "config";
    private static final String WORLDEDIT_FOLDER_NAME = "worldedit";
    private static final String SCHEMATICS_FOLDER_NAME = "schematics";

    private final Path gameDirectory;
    private final Path schematicsDirectory;

    public ArchivWorldEditBridge(Path gameDirectory) {
        this.gameDirectory = gameDirectory;
        this.schematicsDirectory = gameDirectory
                .resolve(CONFIG_FOLDER_NAME)
                .resolve(WORLDEDIT_FOLDER_NAME)
                .resolve(SCHEMATICS_FOLDER_NAME);
    }

    public Path getGameDirectory() {
        return gameDirectory;
    }

    public Path getSchematicsDirectory() {
        return schematicsDirectory;
    }

    public void ensureDirectories() throws IOException {
        Files.createDirectories(schematicsDirectory);
    }

    public LoadResult loadLocalAsset(ArchivLocalLibrary localLibrary, ArchivAsset asset) throws IOException {
        ensureDirectories();

        if (asset == null) {
            return LoadResult.failure("Load failed: asset unavailable", null, null);
        }

        String structureFileName = safeString(asset.getStructureFileName());

        if (structureFileName.isBlank()) {
            return LoadResult.failure("Load failed: no structure file", null, null);
        }

        Path sourcePath = findStructureSource(localLibrary, structureFileName);

        if (sourcePath == null || !Files.isRegularFile(sourcePath)) {
            return LoadResult.failure("Load failed: source file not found", null, null);
        }

        String targetFileName = getTargetFileName(asset, sourcePath);
        Path targetPath = schematicsDirectory.resolve(targetFileName);

        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

        return LoadResult.success(
                "Loaded to WorldEdit: " + targetPath.getFileName(),
                sourcePath,
                targetPath
        );
    }

    private Path findStructureSource(ArchivLocalLibrary localLibrary, String structureFileName) throws IOException {
        if (localLibrary == null) {
            return null;
        }

        Path assetsDirectory = localLibrary.getAssetsDirectory();
        Path normalizedAssetsDirectory = assetsDirectory.toAbsolutePath().normalize();

        Path directPath = assetsDirectory.resolve(structureFileName).normalize();
        Path normalizedDirectPath = directPath.toAbsolutePath().normalize();

        if (normalizedDirectPath.startsWith(normalizedAssetsDirectory) && Files.isRegularFile(normalizedDirectPath)) {
            return normalizedDirectPath;
        }

        String expectedFileName = Path.of(structureFileName).getFileName().toString();

        try (Stream<Path> paths = Files.walk(assetsDirectory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equalsIgnoreCase(expectedFileName))
                    .findFirst()
                    .map(path -> path.toAbsolutePath().normalize())
                    .orElse(null);
        }
    }

    private String getTargetFileName(ArchivAsset asset, Path sourcePath) {
        String extension = getExtension(sourcePath.getFileName().toString());

        if (extension.isBlank()) {
            extension = safeString(asset.getStructureFileFormat());
        }

        if (extension.isBlank()) {
            extension = ".schem";
        }

        String baseName = sanitizeFileName(asset.getName());

        if (baseName.isBlank()) {
            baseName = sanitizeFileName(sourcePath.getFileName().toString());
        }

        if (baseName.toLowerCase(Locale.ROOT).endsWith(extension.toLowerCase(Locale.ROOT))) {
            return baseName;
        }

        return baseName + extension;
    }

    private static String getExtension(String fileName) {
        String clean = safeString(fileName);
        int dotIndex = clean.lastIndexOf(".");

        if (dotIndex < 0 || dotIndex >= clean.length() - 1) {
            return "";
        }

        return clean.substring(dotIndex);
    }

    private static String sanitizeFileName(String value) {
        String clean = safeString(value);

        int slashIndex = Math.max(clean.lastIndexOf('/'), clean.lastIndexOf('\\'));
        if (slashIndex >= 0 && slashIndex + 1 < clean.length()) {
            clean = clean.substring(slashIndex + 1);
        }

        int dotIndex = clean.lastIndexOf(".");
        if (dotIndex > 0) {
            clean = clean.substring(0, dotIndex);
        }

        clean = clean
                .trim()
                .replaceAll("[^a-zA-Z0-9._ -]+", "_")
                .replaceAll("\\s+", " ")
                .replaceAll("_+", "_")
                .trim();

        return clean.isBlank() ? "archiv_asset" : clean;
    }

    private static String safeString(String value) {
        return value == null ? "" : value.trim();
    }

    public static class LoadResult {
        private final boolean success;
        private final String message;
        private final Path sourcePath;
        private final Path targetPath;
        private final String worldEditLoadCommand;

        private LoadResult(boolean success, String message, Path sourcePath, Path targetPath, String worldEditLoadCommand) {
            this.success = success;
            this.message = message;
            this.sourcePath = sourcePath;
            this.targetPath = targetPath;
            this.worldEditLoadCommand = worldEditLoadCommand;
        }

        public static LoadResult success(String message, Path sourcePath, Path targetPath) {
            return new LoadResult(
                    true,
                    message,
                    sourcePath,
                    targetPath,
                    targetPath == null ? "" : "//schem load " + targetPath.getFileName()
            );
        }

        public static LoadResult failure(String message, Path sourcePath, Path targetPath) {
            return new LoadResult(false, message, sourcePath, targetPath, "");
        }

        public boolean success() {
            return success;
        }

        public String message() {
            return message;
        }

        public Path sourcePath() {
            return sourcePath;
        }

        public Path targetPath() {
            return targetPath;
        }

        public String worldEditLoadCommand() {
            return worldEditLoadCommand;
        }
    }
}