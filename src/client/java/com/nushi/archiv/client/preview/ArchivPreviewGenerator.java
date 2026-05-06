package com.nushi.archiv.client.preview;

import com.nushi.archiv.client.model.ArchivAsset;

public class ArchivPreviewGenerator {
    public ArchivPreviewResult generateStaticPreview(ArchivAsset asset, ArchivPreviewCache cache) {
        return ArchivPreviewResult.placeholder("Static preview generation is not implemented yet");
    }

    public ArchivPreviewResult generateAnimatedPreview(ArchivAsset asset, ArchivPreviewCache cache) {
        return ArchivPreviewResult.placeholder("Animated preview generation is not implemented yet");
    }
}