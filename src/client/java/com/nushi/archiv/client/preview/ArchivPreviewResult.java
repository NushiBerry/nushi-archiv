package com.nushi.archiv.client.preview;

import java.nio.file.Path;

public class ArchivPreviewResult {
    private final ArchivPreviewSource source;
    private final Path imagePath;
    private final Path animationDirectory;
    private final String message;

    private ArchivPreviewResult(
            ArchivPreviewSource source,
            Path imagePath,
            Path animationDirectory,
            String message
    ) {
        this.source = source;
        this.imagePath = imagePath;
        this.animationDirectory = animationDirectory;
        this.message = message == null ? "" : message;
    }

    public static ArchivPreviewResult image(ArchivPreviewSource source, Path imagePath) {
        return new ArchivPreviewResult(source, imagePath, null, "");
    }

    public static ArchivPreviewResult animation(Path animationDirectory) {
        return new ArchivPreviewResult(
                ArchivPreviewSource.ANIMATED_PREVIEW,
                null,
                animationDirectory,
                ""
        );
    }

    public static ArchivPreviewResult placeholder(String message) {
        return new ArchivPreviewResult(
                ArchivPreviewSource.PLACEHOLDER,
                null,
                null,
                message
        );
    }

    public ArchivPreviewSource getSource() {
        return source;
    }

    public Path getImagePath() {
        return imagePath;
    }

    public Path getAnimationDirectory() {
        return animationDirectory;
    }

    public String getMessage() {
        return message;
    }

    public boolean hasImage() {
        return imagePath != null;
    }

    public boolean hasAnimation() {
        return animationDirectory != null;
    }

    public boolean isPlaceholder() {
        return source == ArchivPreviewSource.PLACEHOLDER;
    }
}