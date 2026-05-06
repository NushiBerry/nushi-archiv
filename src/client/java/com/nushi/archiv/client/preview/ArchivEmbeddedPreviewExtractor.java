package com.nushi.archiv.client.preview;

import com.nushi.archiv.client.model.ArchivAsset;
import com.nushi.archiv.client.storage.ArchivLocalLibrary;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ArchivEmbeddedPreviewExtractor {
    private static final String[] IMAGE_EXTENSIONS = {
            ".png",
            ".jpg",
            ".jpeg"
    };

    private final ArchivLocalLibrary localLibrary;

    public ArchivEmbeddedPreviewExtractor(ArchivLocalLibrary localLibrary) {
        this.localLibrary = localLibrary;
    }

    public Path extractFromAsset(ArchivAsset asset, ArchivPreviewCache cache) throws IOException {
        if (asset == null || cache == null) {
            return null;
        }

        Path structurePath = resolveLocalStructurePath(asset);

        if (structurePath == null) {
            return null;
        }

        return extractFromSourcePath(asset, structurePath, cache);
    }

    public Path extractFromSourcePath(ArchivAsset asset, Path structureSourcePath, ArchivPreviewCache cache) throws IOException {
        if (asset == null || structureSourcePath == null || cache == null) {
            return null;
        }

        cache.ensureDirectories();

        Path targetPath = cache.getEmbeddedPreviewPath(asset);

        if (Files.isRegularFile(targetPath)) {
            return targetPath;
        }

        Path candidate = findAssociatedPreview(structureSourcePath);

        if (candidate == null) {
            return null;
        }

        return copyPreviewToCache(candidate, targetPath);
    }

    private Path resolveLocalStructurePath(ArchivAsset asset) {
        String structureFileName = safe(asset.getStructureFileName());

        if (structureFileName.isBlank()) {
            return null;
        }

        try {
            Path assetsDirectory = localLibrary.getAssetsDirectory().toAbsolutePath().normalize();
            Path structurePath = assetsDirectory.resolve(structureFileName).toAbsolutePath().normalize();

            if (!structurePath.startsWith(assetsDirectory)) {
                return null;
            }

            if (Files.isRegularFile(structurePath) || Files.isDirectory(structurePath)) {
                return structurePath;
            }
        } catch (Exception ignored) {
            return null;
        }

        return null;
    }

    private Path findAssociatedPreview(Path structurePath) throws IOException {
        Path normalizedStructurePath = structurePath.toAbsolutePath().normalize();

        for (Path candidate : buildPreviewCandidates(normalizedStructurePath)) {
            if (isValidPreviewImage(candidate)) {
                return candidate;
            }
        }

        return null;
    }

    private List<Path> buildPreviewCandidates(Path structurePath) throws IOException {
        List<Path> candidates = new ArrayList<>();

        Path parent = Files.isDirectory(structurePath)
                ? structurePath
                : structurePath.getParent();

        if (parent == null) {
            return candidates;
        }

        String fileName = structurePath.getFileName() == null
                ? ""
                : structurePath.getFileName().toString();

        String baseName = stripExtension(fileName);

        List<Path> folders = new ArrayList<>();
        folders.add(parent);
        folders.add(parent.resolve("preview"));
        folders.add(parent.resolve("previews"));
        folders.add(parent.resolve("thumbnail"));
        folders.add(parent.resolve("thumbnails"));
        folders.add(parent.resolve("thumbs"));
        folders.add(parent.resolve("images"));

        for (Path folder : folders) {
            addNamedCandidates(candidates, folder, baseName);
        }

        if (Files.isDirectory(structurePath)) {
            for (String extension : IMAGE_EXTENSIONS) {
                candidates.add(structurePath.resolve("preview" + extension));
                candidates.add(structurePath.resolve("thumbnail" + extension));
                candidates.add(structurePath.resolve("icon" + extension));
                candidates.add(structurePath.resolve("screenshot" + extension));
            }
        }

        return candidates;
    }

    private void addNamedCandidates(List<Path> candidates, Path folder, String baseName) {
        String cleanBaseName = safe(baseName);

        if (cleanBaseName.isBlank()) {
            return;
        }

        for (String extension : IMAGE_EXTENSIONS) {
            candidates.add(folder.resolve(cleanBaseName + extension));
            candidates.add(folder.resolve(cleanBaseName + "_preview" + extension));
            candidates.add(folder.resolve(cleanBaseName + "-preview" + extension));
            candidates.add(folder.resolve(cleanBaseName + ".preview" + extension));
            candidates.add(folder.resolve(cleanBaseName + "_thumbnail" + extension));
            candidates.add(folder.resolve(cleanBaseName + "-thumbnail" + extension));
            candidates.add(folder.resolve(cleanBaseName + ".thumbnail" + extension));
            candidates.add(folder.resolve("preview_" + cleanBaseName + extension));
            candidates.add(folder.resolve("thumbnail_" + cleanBaseName + extension));
        }
    }

    private boolean isValidPreviewImage(Path candidate) {
        if (candidate == null || !Files.isRegularFile(candidate)) {
            return false;
        }

        String fileName = candidate.getFileName() == null
                ? ""
                : candidate.getFileName().toString().toLowerCase(Locale.ROOT);

        boolean supportedExtension = false;

        for (String extension : IMAGE_EXTENSIONS) {
            if (fileName.endsWith(extension)) {
                supportedExtension = true;
                break;
            }
        }

        if (!supportedExtension) {
            return false;
        }

        try {
            BufferedImage image = ImageIO.read(candidate.toFile());
            return image != null && image.getWidth() > 0 && image.getHeight() > 0;
        } catch (IOException exception) {
            return false;
        }
    }

    private Path copyPreviewToCache(Path sourcePath, Path targetPath) throws IOException {
        BufferedImage image = ImageIO.read(sourcePath.toFile());

        if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
            return null;
        }

        Files.createDirectories(targetPath.getParent());

        Path tempPath = targetPath.resolveSibling(targetPath.getFileName().toString() + ".tmp");

        ImageIO.write(image, "png", tempPath.toFile());

        try {
            Files.move(
                    tempPath,
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (IOException exception) {
            Files.move(
                    tempPath,
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        return targetPath;
    }

    private static String stripExtension(String fileName) {
        String clean = safe(fileName);
        int dotIndex = clean.lastIndexOf(".");

        if (dotIndex <= 0) {
            return clean;
        }

        return clean.substring(0, dotIndex);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}