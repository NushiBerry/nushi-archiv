package com.nushi.archiv.client.model;

// Esta classe representa um asset do Archiv.
// Por enquanto ela é um modelo simples, usado só para mockar dados da interface.
// Mais pra frente ela pode crescer para carregar dados reais de .schem, .bp, manifest.json etc.
public class ArchivAsset {

    private final String name;
    private final String macroCategory;
    private final String type;
    private final String version;
    private final int previewColor;
    private final int chipColor;
    private final int variantCount;
    private final boolean favorite;
    private final boolean highlighted;

    public ArchivAsset(
            String name,
            String macroCategory,
            String type,
            String version,
            int previewColor,
            int chipColor,
            int variantCount,
            boolean favorite,
            boolean highlighted
    ) {
        this.name = name;
        this.macroCategory = macroCategory;
        this.type = type;
        this.version = version;
        this.previewColor = previewColor;
        this.chipColor = chipColor;
        this.variantCount = variantCount;
        this.favorite = favorite;
        this.highlighted = highlighted;
    }

    public String getName() {
        return name;
    }

    public String getMacroCategory() {
        return macroCategory;
    }

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public int getPreviewColor() {
        return previewColor;
    }

    public int getChipColor() {
        return chipColor;
    }

    public int getVariantCount() {
        return variantCount;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public boolean isHighlighted() {
        return highlighted;
    }
}