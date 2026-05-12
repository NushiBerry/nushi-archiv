package com.nushi.archiv.client.preview;

import com.nushi.archiv.client.inspect.ArchivStructureVoxelReader;
import com.nushi.archiv.client.inspect.ArchivStructureVoxelSnapshot;
import com.nushi.archiv.client.model.ArchivAsset;
import com.nushi.archiv.client.storage.ArchivLocalLibrary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Generates cached preview images for structure assets.
 *
 * Current stable pipeline:
 *   - .bp / .litematic: extract an embedded preview image when the file provides one.
 *   - .schem / .schematic: generate one cached PNG with the AWT isometric fallback renderer.
 *
 * The card UI intentionally reads only cached PNG files. It does not render live 3D previews per frame.
 * This keeps the browser responsive and avoids the unstable PiP/render-thread experiments.
 */
public class ArchivGeneratedPreviewGenerator {

    private final ArchivLocalLibrary localLibrary;
    private final ArchivEmbeddedPreviewExtractor embeddedExtractor;
    private final ArchivIsometricRenderer isometricRenderer;
    private final ArchivStructureVoxelReader voxelReader;

    public ArchivGeneratedPreviewGenerator(ArchivLocalLibrary localLibrary) {
        this.localLibrary = localLibrary;
        this.embeddedExtractor = new ArchivEmbeddedPreviewExtractor(localLibrary);
        this.isometricRenderer = new ArchivIsometricRenderer();
        this.voxelReader = new ArchivStructureVoxelReader();
    }

    public Path generateIfPossible(ArchivAsset asset, ArchivPreviewCache cache) throws IOException {
        if (asset == null || cache == null) {
            return null;
        }

        Path structurePath = resolveStructurePath(asset);
        if (structurePath == null) {
            System.err.println("[Archiv] Structure file not found for: " + asset.getName());
            return null;
        }

        String extension = getExtension(structurePath);
        cache.ensureDirectories();

        if (extension.equals(".bp") || extension.equals(".blueprint") || extension.equals(".bl") || extension.equals(".litematic")) {
            return generateEmbeddedPreview(asset, structurePath, cache);
        }

        if (extension.equals(".schem") || extension.equals(".schematic")) {
            return generateStructurePreview(asset, structurePath, cache.getGeneratedPreviewPath(asset));
        }

        return null;
    }

    private Path generateEmbeddedPreview(ArchivAsset asset, Path structurePath, ArchivPreviewCache cache) {
        Path embeddedPath = cache.getEmbeddedPreviewPath(asset);
        if (Files.isRegularFile(embeddedPath)) {
            return null;
        }

        try {
            Path extracted = embeddedExtractor.extractFromSourcePath(asset, structurePath, cache);
            if (extracted != null) {
                System.out.println("[Archiv] Embedded preview ready: " + asset.getName());
                return extracted;
            }

            System.out.println("[Archiv] No embedded preview in: " + structurePath.getFileName());
            return null;
        } catch (IOException exception) {
            System.err.println("[Archiv] Embedded extraction failed for "
                    + asset.getName() + ": " + exception.getMessage());
            return null;
        }
    }

    private Path generateStructurePreview(ArchivAsset asset, Path structurePath, Path outputPath) {
        if (Files.isRegularFile(outputPath)) {
            return null;
        }

        try {
            ArchivStructureVoxelSnapshot snapshot = voxelReader.read(structurePath);
            if (!snapshot.isReadable()) {
                System.err.println("[Archiv] Voxel read failed for "
                        + asset.getName() + ": " + snapshot.getMessage());
                return null;
            }

            if (snapshot.getNonAirBlocks() == 0) {
                return null;
            }

            System.out.println("[Archiv] Generating cached isometric preview: " + asset.getName());
            return isometricRenderer.render(snapshot, outputPath);
        } catch (Exception exception) {
            System.err.println("[Archiv] Preview generation failed for "
                    + asset.getName() + ": " + exception.getMessage());
            return null;
        }
    }

    private Path resolveStructurePath(ArchivAsset asset) {
        String fileName = safe(asset.getStructureFileName());
        if (fileName.isBlank()) {
            return null;
        }

        try {
            Path assetsDirectory = localLibrary.getAssetsDirectory().toAbsolutePath().normalize();
            Path structurePath = assetsDirectory.resolve(fileName).toAbsolutePath().normalize();
            if (!structurePath.startsWith(assetsDirectory)) {
                return null;
            }
            return Files.isRegularFile(structurePath) ? structurePath : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String getExtension(Path path) {
        String name = path.getFileName() == null
                ? ""
                : path.getFileName().toString().toLowerCase(Locale.ROOT);
        int dotIndex = name.lastIndexOf('.');
        return dotIndex < 0 ? "" : name.substring(dotIndex);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
