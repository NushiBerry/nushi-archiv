package com.nushi.archiv.client.screen;

import com.nushi.archiv.client.model.ArchivAsset;
import com.nushi.archiv.client.repository.MockAssetRepository;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ArchivScreen extends Screen {

    private static final int COLOR_BACKGROUND = 0xFF08111D;
    private static final int COLOR_ROOT = 0xFF0C1624;
    private static final int COLOR_PANEL = 0xFF0F1B2D;
    private static final int COLOR_PANEL_ALT = 0xFF13253B;
    private static final int COLOR_BORDER = 0xFF223854;
    private static final int COLOR_BORDER_ACTIVE = 0xFF2DAEFF;
    private static final int COLOR_TEXT = 0xFFF2F7FF;
    private static final int COLOR_TEXT_DIM = 0xFF93A8C1;
    private static final int COLOR_SUCCESS = 0xFF36C275;
    private static final int MOCK_PREVIEW_IMAGE_COLOR = 0xFF6B86B3;
    private static final int MOCK_NO_PREVIEW_IMAGE_COLOR = 0xFF4B6E9A;

    private final Screen parent;
    private final List<ArchivAsset> mockAssets;
    private final List<ArchivAsset> savedAssets = new ArrayList<>();

    private final String[] categories = {
            "All",
            "Medieval",
            "Fantasy",
            "Cyberpunk",
            "Sci-fi",
            "Organic",
            "Nature",
            "Modern",
            "Industrial"
    };

    private final String[] myAssetsSections = {
            "All Assets",
            "Favorites",
            "Imported",
            "Recent",
            "Collections",
            "Local Packs",
            "Shared"
    };

    private String selectedCategory = "All";
    private String selectedTopTab = "Browse";
    private String selectedMyAssetsSection = "All Assets";
    private boolean browseFavoritesOnly = false;
    private int selectedImportStep = 1;

    private String selectedLibraryAssetName = null;
    private String libraryActionMessage = "No asset selected";

    private boolean mockStructureFileSelected = false;
    private final String mockStructureFileName = "stone_tower.schem";
    private final String mockStructureFileFormat = ".schem";
    private final String mockStructureFileSize = "1.24 MB";

    private boolean mockPreviewImageSelected = false;
    private final String mockPreviewImageName = "stone_tower_preview.png";
    private final String mockPreviewImageFormat = ".png";
    private final String mockPreviewImageRatio = "16:9";

    private boolean mockDetailsFilled = false;
    private boolean mockAssetSaved = false;

    private final ImportPreset[] importPresets = new ImportPreset[] {
            new ImportPreset(
                    "Stone Tower",
                    "Medieval",
                    "BuilderX",
                    "Structure",
                    "1.20.1",
                    "Default, Mossy",
                    "tower, stone, medieval",
                    ".schem • 1.24 MB",
                    0xFF2D9CDB
            ),
            new ImportPreset(
                    "Crystal Lamp",
                    "Fantasy",
                    "Arcanist",
                    "Decoration",
                    "1.20.1",
                    "Default, Arcane",
                    "crystal, lamp, fantasy",
                    ".schem • 0.96 MB",
                    0xFF8A5CFF
            ),
            new ImportPreset(
                    "Palm Tree",
                    "Nature",
                    "BuilderX",
                    "Tree",
                    "1.20.1",
                    "Tall, Curved",
                    "tree, palm, beach",
                    ".schem • 0.84 MB",
                    0xFF2DBE73
            ),
            new ImportPreset(
                    "Neon Sign",
                    "Cyberpunk",
                    "NeonFox",
                    "Prop",
                    "1.20.1",
                    "Pink, Blue",
                    "sign, neon, cyberpunk",
                    ".schem • 0.71 MB",
                    0xFFDA8A2D
            )
    };

    private int currentImportPresetIndex = 0;

    private static class ImportLayout {
        int innerX;
        int innerY;
        int innerW;
        int innerH;

        int sectionY;

        int previewColumnW;
        int leftAreaW;

        int topBoxH;
        int structureW;
        int imageW;

        int structureX;
        int imageX;
        int previewX;

        int boxButtonY;

        int detailsY;
        int detailsH;

        int detailsActionX;
        int detailsActionY;
        int detailsActionW;
        int detailsActionH;

        int actionsY;
        int buttonH;
        int saveW;
        int cancelW;
        int resetW;
        int saveX;
        int cancelX;
        int resetX;
    }

    private static class ImportPreset {
        final String name;
        final String macroCategory;
        final String author;
        final String type;
        final String version;
        final String variants;
        final String tags;
        final String fileInfo;
        final int chipColor;

        ImportPreset(
                String name,
                String macroCategory,
                String author,
                String type,
                String version,
                String variants,
                String tags,
                String fileInfo,
                int chipColor
        ) {
            this.name = name;
            this.macroCategory = macroCategory;
            this.author = author;
            this.type = type;
            this.version = version;
            this.variants = variants;
            this.tags = tags;
            this.fileInfo = fileInfo;
            this.chipColor = chipColor;
        }
    }

    private static class CardGridLayout {
        int cardsAreaX;
        int cardsAreaY;
        int cardsAreaW;
        int cardsAreaH;

        int cardsGap;
        int rowGap;
        int columns;
        int rows;
        int cardW;
        int cardH;
    }

    private static class AssetCardLayout {
        int previewX;
        int previewY;
        int previewW;
        int previewH;

        int favoriteX;
        int favoriteY;
        int favoriteW;
        int favoriteH;

        int overlayX;
        int overlayW;

        int loadY;
        int loadH;

        int detailsY;
        int detailsH;
    }

    public ArchivScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;
        this.mockAssets = new ArrayList<>();

        for (ArchivAsset asset : this.mockAssets) {
            if (asset.isHighlighted()) {
                this.selectedLibraryAssetName = asset.getName();
                this.libraryActionMessage = "Selected: " + asset.getName();
                break;
            }
        }

        if (this.selectedLibraryAssetName == null && !this.mockAssets.isEmpty()) {
            this.selectedLibraryAssetName = this.mockAssets.get(0).getName();
            this.libraryActionMessage = "Selected: " + this.selectedLibraryAssetName;
        }
    }

    @Override
    protected void init() {
        int closeButtonSize = 24;
        int closeX = this.width - 20 - closeButtonSize;
        int closeY = 20;

        this.addRenderableWidget(
                Button.builder(Component.literal("X"), button -> this.onClose())
                        .bounds(closeX, closeY, closeButtonSize, closeButtonSize)
                        .build()
        );
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private List<ArchivAsset> getVisibleAssets() {
        List<ArchivAsset> filteredAssets = new ArrayList<>();

        if ("Browse".equals(selectedTopTab)) {
            List<ArchivAsset> sourceAssets = new ArrayList<>(mockAssets);
            sourceAssets.addAll(savedAssets);

            for (ArchivAsset asset : sourceAssets) {
                boolean categoryMatches = selectedCategory.equals("All")
                        || asset.getMacroCategory().equals(selectedCategory);

                boolean favoriteMatches = !browseFavoritesOnly || asset.isFavorite();

                if (categoryMatches && favoriteMatches) {
                    filteredAssets.add(asset);
                }
            }

            return filteredAssets;
        }

        if ("My Assets".equals(selectedTopTab)) {
            for (ArchivAsset asset : savedAssets) {
                boolean include = switch (selectedMyAssetsSection) {
                    case "All Assets", "Imported" -> true;
                    case "Favorites" -> asset.isFavorite();
                    case "Recent" -> true;
                    default -> false;
                };

                if (include) {
                    filteredAssets.add(asset);
                }
            }

            if ("Recent".equals(selectedMyAssetsSection) && filteredAssets.size() > 4) {
                return new ArrayList<>(filteredAssets.subList(0, 4));
            }

            return filteredAssets;
        }

        return filteredAssets;
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width
                && mouseY >= y && mouseY <= y + height;
    }

    private ImportLayout buildImportLayout(int contentX, int contentY, int contentW, int contentH) {
        ImportLayout layout = new ImportLayout();

        boolean compact = true;
        boolean detailsStepActive = selectedImportStep == 3;
        boolean saveStepActive = selectedImportStep == 4;
        boolean compactTopSections = detailsStepActive || saveStepActive;

        int pad = 18;
        int titleBlockH = compact ? 48 : 40;
        int gap = compact ? 12 : 16;
        int detailsGap = compact ? 10 : 16;
        int actionsBarH = 28;
        int actionsBottomMargin = compact ? 8 : 18;
        int actionsGap = saveStepActive ? 28 : (compact ? 8 : 12);
        int fieldGap = 12;

        layout.innerX = contentX + pad;
        layout.innerY = contentY + pad;
        layout.innerW = contentW - (pad * 2);
        layout.innerH = contentH - (pad * 2);

        layout.sectionY = layout.innerY + titleBlockH;

        layout.previewColumnW = compact ? 210 : 220;
        layout.leftAreaW = layout.innerW - layout.previewColumnW - gap;

        layout.topBoxH = compactTopSections ? 72 : (compact ? 118 : 170);

        layout.structureW = (layout.leftAreaW * 58) / 100;
        layout.imageW = layout.leftAreaW - layout.structureW - gap;

        layout.structureX = layout.innerX;
        layout.imageX = layout.structureX + layout.structureW + gap;
        layout.previewX = layout.innerX + layout.leftAreaW + gap;

        layout.boxButtonY = compact ? layout.sectionY + 80 : layout.sectionY + 118;

        layout.detailsY = layout.sectionY + layout.topBoxH + detailsGap;
        layout.actionsY = layout.innerY + layout.innerH - actionsBottomMargin - actionsBarH;
        layout.detailsH = layout.actionsY - actionsGap - layout.detailsY;

        layout.detailsActionW = 92;
        layout.detailsActionH = 20;
        layout.detailsActionX = layout.innerX + layout.leftAreaW - layout.detailsActionW - 12;
        layout.detailsActionY = layout.detailsY + 10;

        layout.buttonH = 28;
        layout.saveW = 88;
        layout.cancelW = 90;
        layout.resetW = 90;

        layout.saveX = layout.innerX + layout.innerW - layout.saveW;
        layout.cancelX = layout.saveX - fieldGap - layout.cancelW;
        layout.resetX = layout.cancelX - fieldGap - layout.resetW;

        return layout;
    }

    private CardGridLayout buildBrowseGridLayout(int contentX, int contentY, int contentW, int contentH) {
        CardGridLayout layout = new CardGridLayout();

        int innerPadding = 18;
        int toolbarY = contentY + 14;

        layout.cardsAreaX = contentX + innerPadding;
        layout.cardsAreaY = toolbarY + 50;
        layout.cardsAreaW = contentW - (innerPadding * 2);
        layout.cardsAreaH = (contentY + contentH - 16) - layout.cardsAreaY;

        layout.cardsGap = 14;
        layout.rowGap = 16;
        layout.columns = 3;
        layout.rows = 2;

        layout.cardW = (layout.cardsAreaW - (layout.cardsGap * (layout.columns - 1))) / layout.columns;
        layout.cardH = (layout.cardsAreaH - (layout.rowGap * (layout.rows - 1))) / layout.rows;

        return layout;
    }

    private CardGridLayout buildMyAssetsImportedGridLayout(int contentX, int contentY, int contentW, int contentH) {
        CardGridLayout layout = new CardGridLayout();

        int innerPadding = 18;
        int titleX = contentX + innerPadding;
        int titleY = contentY + 16;

        int statsY = titleY + 40;
        int statH = 68;
        int quickY = statsY + statH + 18;
        int quickButtonY = quickY + 14;
        int quickButtonH = 30;
        int importedTitleY = quickButtonY + quickButtonH + 20;

        layout.cardsAreaX = titleX;
        layout.cardsAreaY = importedTitleY + 18;
        layout.cardsAreaW = contentW - (innerPadding * 2);
        layout.cardsAreaH = (contentY + contentH - 16) - layout.cardsAreaY;

        layout.cardsGap = 14;
        layout.rowGap = 16;
        layout.columns = 3;
        layout.rows = 1;

        layout.cardW = (layout.cardsAreaW - (layout.cardsGap * (layout.columns - 1))) / layout.columns;
        layout.cardH = 150;

        return layout;
    }

    private AssetCardLayout buildAssetCardLayout(int x, int y, int width, int height) {
        AssetCardLayout layout = new AssetCardLayout();

        layout.previewX = x + 1;
        layout.previewY = y + 1;
        layout.previewW = width - 2;
        layout.previewH = Math.max(80, (int) (height * 0.55));

        layout.favoriteX = x + width - 24;
        layout.favoriteY = y + 4;
        layout.favoriteW = 20;
        layout.favoriteH = 20;

        layout.overlayX = x + 30;
        layout.overlayW = width - 60;

        int overlayGap = 6;
        int loadH = 24;
        int detailsH = 22;
        int totalOverlayH = loadH + overlayGap + detailsH;

        int overlayStartY = layout.previewY + Math.max(8, (layout.previewH - totalOverlayH) / 2);

        layout.loadY = overlayStartY;
        layout.loadH = loadH;

        layout.detailsY = overlayStartY + loadH + overlayGap;
        layout.detailsH = detailsH;

        return layout;
    }

    private boolean isImportReady() {
        return mockStructureFileSelected && mockDetailsFilled;
    }

    private boolean hasImportData() {
        return mockStructureFileSelected || mockPreviewImageSelected || mockDetailsFilled || mockAssetSaved;
    }

    private void resetImportState() {
        mockStructureFileSelected = false;
        mockPreviewImageSelected = false;
        mockDetailsFilled = false;
        mockAssetSaved = false;
        selectedImportStep = 1;
    }

    private void beginFreshImportSession() {
        resetImportState();
        selectedTopTab = "Import";
    }

    private ImportPreset getCurrentImportPreset() {
        return importPresets[currentImportPresetIndex];
    }

    private void advanceImportPreset() {
        currentImportPresetIndex = (currentImportPresetIndex + 1) % importPresets.length;
    }

    private String getNextSavedAssetName() {
        String baseName = getCurrentImportPreset().name;
        int sameNameCount = 0;

        for (ArchivAsset asset : savedAssets) {
            String existingName = asset.getName();

            if (existingName.equals(baseName) || existingName.startsWith(baseName + " #")) {
                sameNameCount++;
            }
        }

        return sameNameCount == 0
                ? baseName
                : baseName + " #" + (sameNameCount + 1);
    }

    private int getImportVariantCount() {
        String[] parts = getCurrentImportPreset().variants.split(",");
        return Math.max(1, parts.length);
    }

    private int getImportChipColor() {
        return getCurrentImportPreset().chipColor;
    }

    private ArchivAsset buildSavedAssetFromImport() {
        ImportPreset preset = getCurrentImportPreset();

        return new ArchivAsset(
                getNextSavedAssetName(),
                preset.macroCategory,
                preset.type,
                preset.version,
                mockPreviewImageSelected ? MOCK_PREVIEW_IMAGE_COLOR : MOCK_NO_PREVIEW_IMAGE_COLOR,
                preset.chipColor,
                getImportVariantCount(),
                false,
                false
        );
    }

    private int getSavedFavoritesCount() {
        int count = 0;

        for (ArchivAsset asset : savedAssets) {
            if (asset.isFavorite()) {
                count++;
            }
        }

        return count;
    }

    private int getRecentAssetsCount() {
        return Math.min(savedAssets.size(), 4);
    }

    private boolean myAssetsShowsImportedGrid() {
        return "All Assets".equals(selectedMyAssetsSection)
                || "Favorites".equals(selectedMyAssetsSection)
                || "Imported".equals(selectedMyAssetsSection)
                || "Recent".equals(selectedMyAssetsSection);
    }

    private ArchivAsset copyAssetWithFavorite(ArchivAsset asset, boolean favorite) {
        return new ArchivAsset(
                asset.getName(),
                asset.getMacroCategory(),
                asset.getType(),
                asset.getVersion(),
                asset.getPreviewColor(),
                asset.getChipColor(),
                asset.getVariantCount(),
                favorite,
                asset.isHighlighted()
        );
    }

    private boolean replaceFavoriteInList(List<ArchivAsset> assets, ArchivAsset target) {
        for (int i = 0; i < assets.size(); i++) {
            ArchivAsset current = assets.get(i);

            if (current.getName().equals(target.getName())) {
                assets.set(i, copyAssetWithFavorite(current, !current.isFavorite()));
                return true;
            }
        }

        return false;
    }

    private void toggleAssetFavorite(ArchivAsset asset) {
        if (replaceFavoriteInList(savedAssets, asset)) {
            return;
        }

        replaceFavoriteInList(mockAssets, asset);
    }

    private boolean isLibraryAssetSelected(ArchivAsset asset) {
        return selectedLibraryAssetName != null && selectedLibraryAssetName.equals(asset.getName());
    }

    private void selectLibraryAsset(ArchivAsset asset) {
        selectedLibraryAssetName = asset.getName();
        libraryActionMessage = "Selected: " + asset.getName();
    }

    private void setLibraryAction(String action, ArchivAsset asset) {
        selectedLibraryAssetName = asset.getName();
        libraryActionMessage = action + ": " + asset.getName();
    }

    private void syncLibrarySelectionWithVisibleAssets() {
        List<ArchivAsset> visibleAssets = getVisibleAssets();

        if (visibleAssets.isEmpty()) {
            selectedLibraryAssetName = null;
            libraryActionMessage = "No asset selected";
            return;
        }

        for (ArchivAsset asset : visibleAssets) {
            if (isLibraryAssetSelected(asset)) {
                libraryActionMessage = "Selected: " + asset.getName();
                return;
            }
        }

        selectLibraryAsset(visibleAssets.get(0));
    }

    private String fitTextToWidth(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int ellipsisWidth = this.font.width(ellipsis);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            String next = result.toString() + text.charAt(i);
            if (this.font.width(next) + ellipsisWidth > maxWidth) {
                break;
            }
            result.append(text.charAt(i));
        }

        return result + ellipsis;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }

        int margin = 20;

        int rootX = margin;
        int rootY = margin;
        int rootW = this.width - (margin * 2);
        int rootH = this.height - (margin * 2);

        int headerH = 58;
        int footerH = 24;
        int sidebarW = 180;

        int tabY = rootY + 10;
        int tabX = rootX + 150;
        int tabW = 110;
        int tabH = 38;
        int tabGap = 8;

        int myAssetsX = tabX + (tabW + tabGap);
        int myAssetsW = tabW + 10;

        int importX = tabX + (tabW + tabGap) * 2 + 10;
        int settingsX = tabX + (tabW + tabGap) * 3 + 10;

        int bodyY = rootY + headerH;
        int bodyH = rootH - headerH - footerH;

        int contentX = rootX + sidebarW;
        int contentY = bodyY;
        int contentW = rootW - sidebarW;
        int contentH = bodyH;

        int itemX = rootX + 12;
        int itemY = bodyY + 34;
        int itemW = sidebarW - 24;
        int itemH = 34;
        int itemGap = 40;

        double mouseX = event.x();
        double mouseY = event.y();

        boolean insideBrowseTab = mouseX >= tabX && mouseX <= tabX + tabW
                && mouseY >= tabY && mouseY <= tabY + tabH;

        boolean insideMyAssetsTab = mouseX >= myAssetsX && mouseX <= myAssetsX + myAssetsW
                && mouseY >= tabY && mouseY <= tabY + tabH;

        boolean insideImportTab = mouseX >= importX && mouseX <= importX + tabW
                && mouseY >= tabY && mouseY <= tabY + tabH;

        boolean insideSettingsTab = mouseX >= settingsX && mouseX <= settingsX + tabW
                && mouseY >= tabY && mouseY <= tabY + tabH;

        if (insideBrowseTab) {
            selectedTopTab = "Browse";
            return true;
        }

        if (insideMyAssetsTab) {
            selectedTopTab = "My Assets";
            return true;
        }

        if (insideImportTab) {
            if (!"Import".equals(selectedTopTab)) {
                beginFreshImportSession();
            } else {
                selectedImportStep = 1;
            }
            return true;
        }

        if (insideSettingsTab) {
            selectedTopTab = "Settings";
            return true;
        }

        if ("Import".equals(selectedTopTab)) {
            int stepX = rootX + 12;
            int stepY = bodyY + 34;
            int stepW = 156;
            int stepH = 34;
            int stepGapLocal = 40;

            for (int i = 0; i < 4; i++) {
                int currentY = stepY + (i * stepGapLocal);

                if (isInside(mouseX, mouseY, stepX, currentY, stepW, stepH)) {
                    selectedImportStep = i + 1;
                    return true;
                }
            }

            ImportLayout layout = buildImportLayout(contentX, contentY, contentW, contentH);

            if (selectedImportStep == 1) {
                if (mockStructureFileSelected) {
                    int replaceButtonW = 108;
                    int replaceButtonH = 24;
                    int replaceButtonX = layout.structureX + layout.structureW - replaceButtonW - 20;
                    int replaceButtonY = layout.sectionY + 34;

                    if (isInside(mouseX, mouseY, replaceButtonX, replaceButtonY, replaceButtonW, replaceButtonH)) {
                        mockStructureFileSelected = false;
                        mockAssetSaved = false;
                        return true;
                    }
                } else {
                    int browseFileButtonX = layout.structureX + (layout.structureW / 2) - 60;

                    if (isInside(mouseX, mouseY, browseFileButtonX, layout.boxButtonY, 120, 28)) {
                        mockStructureFileSelected = true;
                        mockAssetSaved = false;
                        selectedImportStep = 2;
                        return true;
                    }
                }
            }

            if (selectedImportStep == 2) {
                if (mockPreviewImageSelected) {
                    int replaceButtonW = 118;
                    int replaceButtonH = 24;
                    int replaceButtonX = layout.imageX + (layout.imageW / 2) - (replaceButtonW / 2);
                    int replaceButtonY = layout.sectionY + layout.topBoxH - replaceButtonH - 8;

                    if (isInside(mouseX, mouseY, replaceButtonX, replaceButtonY, replaceButtonW, replaceButtonH)) {
                        mockPreviewImageSelected = false;
                        mockAssetSaved = false;
                        return true;
                    }
                } else {
                    int browseImageButtonX = layout.imageX + (layout.imageW / 2) - 60;

                    if (isInside(mouseX, mouseY, browseImageButtonX, layout.boxButtonY, 120, 28)) {
                        mockPreviewImageSelected = true;
                        mockAssetSaved = false;
                        selectedImportStep = 3;
                        return true;
                    }
                }
            }

            if (selectedImportStep == 3) {
                if (isInside(mouseX, mouseY, layout.detailsActionX, layout.detailsActionY, layout.detailsActionW, layout.detailsActionH)) {
                    mockDetailsFilled = !mockDetailsFilled;
                    mockAssetSaved = false;

                    if (mockDetailsFilled) {
                        selectedImportStep = 4;
                    }

                    return true;
                }
            }

            if (isInside(mouseX, mouseY, layout.resetX, layout.actionsY, layout.resetW, layout.buttonH)) {
                if (hasImportData()) {
                    resetImportState();
                    return true;
                }
            }

            if (isInside(mouseX, mouseY, layout.cancelX, layout.actionsY, layout.cancelW, layout.buttonH)) {
                this.onClose();
                return true;
            }

            if (isInside(mouseX, mouseY, layout.saveX, layout.actionsY, layout.saveW, layout.buttonH)) {
                if (isImportReady() && !mockAssetSaved) {
                    ArchivAsset savedAsset = buildSavedAssetFromImport();
                    savedAssets.add(0, savedAsset);
                    advanceImportPreset();

                    selectedLibraryAssetName = savedAsset.getName();
                    libraryActionMessage = "Selected: " + savedAsset.getName();

                    mockAssetSaved = true;
                    selectedImportStep = 4;
                    selectedTopTab = "My Assets";
                    selectedCategory = "All";
                    selectedMyAssetsSection = "All Assets";
                    return true;
                }
            }
        }

        if ("Browse".equals(selectedTopTab)) {
            for (int i = 0; i < categories.length; i++) {
                int currentY = itemY + (i * itemGap);

                if (isInside(mouseX, mouseY, itemX, currentY, itemW, itemH)) {
                    selectedCategory = categories[i];
                    syncLibrarySelectionWithVisibleAssets();
                    return true;
                }
            }
        }

        if ("Browse".equals(selectedTopTab)) {
            int innerPadding = 18;
            int toolbarY = contentY + 14;

            int searchX = contentX + innerPadding;
            int searchW = 280;

            int filterX = searchX + searchW + 12;
            int filterW = 110;

            if (isInside(mouseX, mouseY, filterX, toolbarY, filterW, 34)) {
                browseFavoritesOnly = !browseFavoritesOnly;
                syncLibrarySelectionWithVisibleAssets();
                return true;
            }
        }

        if ("My Assets".equals(selectedTopTab)) {
            for (int i = 0; i < myAssetsSections.length; i++) {
                int currentY = itemY + (i * itemGap);

                if (isInside(mouseX, mouseY, itemX, currentY, itemW, itemH)) {
                    selectedMyAssetsSection = myAssetsSections[i];
                    return true;
                }
            }

            int innerPadding = 18;
            int titleX = contentX + innerPadding;
            int titleY = contentY + 16;

            int statsY = titleY + 40;
            int statH = 68;

            int quickY = statsY + statH + 18;
            int quickButtonY = quickY + 14;
            int quickButtonH = 30;
            int quickGap = 12;
            int quickW = (contentW - (innerPadding * 2) - (quickGap * 2)) / 3;

            if (isInside(mouseX, mouseY, titleX, quickButtonY, quickW, quickButtonH)) {
                beginFreshImportSession();
                return true;
            }
        }

        if ("Browse".equals(selectedTopTab)) {
            CardGridLayout layout = buildBrowseGridLayout(contentX, contentY, contentW, contentH);
            List<ArchivAsset> visibleAssets = getVisibleAssets();

            for (int i = 0; i < visibleAssets.size(); i++) {
                ArchivAsset asset = visibleAssets.get(i);

                int column = i % layout.columns;
                int row = i / layout.columns;

                int cardX = layout.cardsAreaX + column * (layout.cardW + layout.cardsGap);
                int cardY = layout.cardsAreaY + row * (layout.cardH + layout.rowGap);

                AssetCardLayout cardLayout = buildAssetCardLayout(cardX, cardY, layout.cardW, layout.cardH);

                if (isInside(mouseX, mouseY, cardLayout.favoriteX, cardLayout.favoriteY, cardLayout.favoriteW, cardLayout.favoriteH)) {
                    toggleAssetFavorite(asset);
                    syncLibrarySelectionWithVisibleAssets();
                    return true;
                }

                boolean selected = isLibraryAssetSelected(asset);

                if (selected) {
                    if (isInside(mouseX, mouseY, cardLayout.overlayX, cardLayout.loadY, cardLayout.overlayW, cardLayout.loadH)) {
                        setLibraryAction("Load pending", asset);
                        return true;
                    }

                    if (isInside(mouseX, mouseY, cardLayout.overlayX, cardLayout.detailsY, cardLayout.overlayW, cardLayout.detailsH)) {
                        setLibraryAction("Details pending", asset);
                        return true;
                    }
                }

                if (isInside(mouseX, mouseY, cardX, cardY, layout.cardW, layout.cardH)) {
                    selectLibraryAsset(asset);
                    return true;
                }
            }
        }

        if ("My Assets".equals(selectedTopTab) && myAssetsShowsImportedGrid()) {
            CardGridLayout layout = buildMyAssetsImportedGridLayout(contentX, contentY, contentW, contentH);
            List<ArchivAsset> visibleAssets = getVisibleAssets();

            for (int i = 0; i < visibleAssets.size(); i++) {
                ArchivAsset asset = visibleAssets.get(i);

                int column = i % layout.columns;
                int row = i / layout.columns;

                int cardX = layout.cardsAreaX + column * (layout.cardW + layout.cardsGap);
                int cardY = layout.cardsAreaY + row * (layout.cardH + layout.rowGap);

                AssetCardLayout cardLayout = buildAssetCardLayout(cardX, cardY, layout.cardW, layout.cardH);

                if (isInside(mouseX, mouseY, cardLayout.favoriteX, cardLayout.favoriteY, cardLayout.favoriteW, cardLayout.favoriteH)) {
                    toggleAssetFavorite(asset);
                    syncLibrarySelectionWithVisibleAssets();
                    return true;
                }

                boolean selected = isLibraryAssetSelected(asset);

                if (selected) {
                    if (isInside(mouseX, mouseY, cardLayout.overlayX, cardLayout.loadY, cardLayout.overlayW, cardLayout.loadH)) {
                        setLibraryAction("Load pending", asset);
                        return true;
                    }

                    if (isInside(mouseX, mouseY, cardLayout.overlayX, cardLayout.detailsY, cardLayout.overlayW, cardLayout.detailsH)) {
                        setLibraryAction("Details pending", asset);
                        return true;
                    }
                }

                if (isInside(mouseX, mouseY, cardX, cardY, layout.cardW, layout.cardH)) {
                    selectLibraryAsset(asset);
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.fill(0, 0, this.width, this.height, COLOR_BACKGROUND);

        int margin = 20;

        int rootX = margin;
        int rootY = margin;
        int rootW = this.width - (margin * 2);
        int rootH = this.height - (margin * 2);

        int headerH = 58;
        int footerH = 24;
        int sidebarW = 180;

        int bodyY = rootY + headerH;
        int bodyH = rootH - headerH - footerH;

        int contentX = rootX + sidebarW;
        int contentY = bodyY;
        int contentW = rootW - sidebarW;
        int contentH = bodyH;

        boolean browseActive = "Browse".equals(selectedTopTab);
        boolean myAssetsActive = "My Assets".equals(selectedTopTab);
        boolean libraryTabActive = browseActive || myAssetsActive;

        List<ArchivAsset> visibleAssets = getVisibleAssets();

        drawPanel(guiGraphics, rootX, rootY, rootW, rootH, COLOR_ROOT, COLOR_BORDER);
        drawPanel(guiGraphics, rootX, rootY, rootW, headerH, COLOR_PANEL, COLOR_BORDER);
        drawPanel(guiGraphics, rootX, bodyY, sidebarW, bodyH, COLOR_PANEL, COLOR_BORDER);
        drawPanel(guiGraphics, contentX, contentY, contentW, contentH, COLOR_ROOT, COLOR_BORDER);

        guiGraphics.drawString(this.font, "ARCHIV", rootX + 18, rootY + 20, COLOR_TEXT);

        int tabY = rootY + 10;
        int tabX = rootX + 150;
        int tabW = 110;
        int tabH = 38;
        int tabGap = 8;

        drawTopTab(guiGraphics, "Browse", tabX, tabY, tabW, tabH, browseActive);
        drawTopTab(guiGraphics, "My Assets", tabX + (tabW + tabGap), tabY, tabW + 10, tabH, myAssetsActive);
        drawTopTab(guiGraphics, "Import", tabX + (tabW + tabGap) * 2 + 10, tabY, tabW, tabH, "Import".equals(selectedTopTab));
        drawTopTab(guiGraphics, "Settings", tabX + (tabW + tabGap) * 3 + 10, tabY, tabW, tabH, "Settings".equals(selectedTopTab));

        if (browseActive) {
            guiGraphics.drawString(this.font, "CATEGORIES", rootX + 16, bodyY + 14, COLOR_TEXT_DIM);

            int categoryY = bodyY + 34;
            for (int i = 0; i < categories.length; i++) {
                boolean active = categories[i].equals(selectedCategory);
                drawSidebarItem(guiGraphics, categories[i], rootX + 12, categoryY, sidebarW - 24, 34, active);
                categoryY += 40;
            }
        } else if (myAssetsActive) {
            guiGraphics.drawString(this.font, "MY LIBRARY", rootX + 16, bodyY + 14, COLOR_TEXT_DIM);

            int sectionY = bodyY + 34;
            for (int i = 0; i < myAssetsSections.length; i++) {
                boolean active = myAssetsSections[i].equals(selectedMyAssetsSection);
                drawSidebarItem(guiGraphics, myAssetsSections[i], rootX + 12, sectionY, sidebarW - 24, 34, active);
                sectionY += 40;
            }
        } else if (!"Import".equals(selectedTopTab)) {
            guiGraphics.drawString(this.font, "SECTION", rootX + 16, bodyY + 14, COLOR_TEXT_DIM);
            guiGraphics.drawString(this.font, selectedTopTab, rootX + 16, bodyY + 34, COLOR_TEXT);
        }

        if (browseActive) {
            drawBrowseTab(guiGraphics, contentX, contentY, contentW, contentH, visibleAssets);
        } else if (myAssetsActive) {
            drawMyAssetsTab(guiGraphics, contentX, contentY, contentW, contentH, visibleAssets);
        } else if ("Import".equals(selectedTopTab)) {
            drawImportTab(guiGraphics, rootX, rootY, rootW, rootH, bodyY, bodyH, contentX, contentY, contentW, contentH);
        } else {
            drawTabPlaceholder(guiGraphics, contentX, contentY, contentW, contentH, selectedTopTab);
        }

        drawPanel(guiGraphics, rootX, rootY + rootH - footerH, rootW, footerH, COLOR_PANEL, COLOR_BORDER);

        int footerY = rootY + rootH - footerH + 8;
        guiGraphics.drawString(this.font, "WorldEdit: pending", rootX + 12, footerY, COLOR_SUCCESS);

        String footerMiddleText = libraryTabActive ? libraryActionMessage : "Preview pipeline: planned";
        guiGraphics.drawString(this.font, footerMiddleText, rootX + 140, footerY, COLOR_TEXT_DIM);

        if (libraryTabActive) {
            int totalAssets = browseActive
                    ? mockAssets.size() + savedAssets.size()
                    : savedAssets.size();

            String assetCountText = visibleAssets.size() + " / " + totalAssets + " assets";
            guiGraphics.drawString(this.font, assetCountText, rootX + rootW - 120, footerY, COLOR_TEXT_DIM);
        } else {
            guiGraphics.drawString(this.font, selectedTopTab, rootX + rootW - 80, footerY, COLOR_TEXT_DIM);
        }

        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    private void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height, int backgroundColor, int borderColor) {
        guiGraphics.fill(x, y, x + width, y + height, backgroundColor);
        guiGraphics.fill(x, y, x + width, y + 1, borderColor);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, borderColor);
        guiGraphics.fill(x, y, x + 1, y + height, borderColor);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, borderColor);
    }

    private void drawTopTab(GuiGraphics guiGraphics, String label, int x, int y, int width, int height, boolean active) {
        int background = active ? COLOR_PANEL_ALT : COLOR_PANEL;
        int border = active ? COLOR_BORDER_ACTIVE : COLOR_BORDER;
        int textColor = active ? COLOR_BORDER_ACTIVE : COLOR_TEXT;

        drawPanel(guiGraphics, x, y, width, height, background, border);

        if (active) {
            guiGraphics.fill(x, y + height - 2, x + width, y + height, COLOR_BORDER_ACTIVE);
        }

        guiGraphics.drawString(this.font, label, x + 14, y + 14, textColor);
    }

    private void drawSidebarItem(GuiGraphics guiGraphics, String label, int x, int y, int width, int height, boolean active) {
        int background = active ? COLOR_PANEL_ALT : COLOR_PANEL;
        int border = active ? COLOR_BORDER_ACTIVE : COLOR_BORDER;
        int textColor = active ? COLOR_TEXT : COLOR_TEXT_DIM;

        drawPanel(guiGraphics, x, y, width, height, background, border);
        guiGraphics.drawString(this.font, label, x + 12, y + 12, textColor);
    }

    private void drawControlBox(GuiGraphics guiGraphics, String label, int x, int y, int width, int height) {
        drawPanel(guiGraphics, x, y, width, height, COLOR_PANEL, COLOR_BORDER);

        int textY = y + (height - this.font.lineHeight) / 2;
        guiGraphics.drawString(this.font, label, x + 12, textY, COLOR_TEXT_DIM);
    }

    private void drawFieldBox(GuiGraphics guiGraphics, String text, int x, int y, int width, int height, boolean filled) {
        drawPanel(guiGraphics, x, y, width, height, COLOR_PANEL, COLOR_BORDER);

        int textY = y + (height - this.font.lineHeight) / 2;
        int textColor = filled ? COLOR_TEXT : COLOR_TEXT_DIM;

        guiGraphics.drawString(this.font, text, x + 12, textY, textColor);
    }

    private void drawButtonBox(GuiGraphics guiGraphics, String label, int x, int y, int width, int height, boolean primary) {
        int background = primary ? 0xFF2F9BE6 : COLOR_PANEL;
        int border = primary ? 0xFF73C8FF : COLOR_BORDER;
        int textColor = 0xFFFFFFFF;

        drawPanel(guiGraphics, x, y, width, height, background, border);

        int textWidth = this.font.width(label);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - this.font.lineHeight) / 2;

        guiGraphics.drawString(this.font, label, textX, textY, textColor);
    }

    private void drawButtonBoxState(GuiGraphics guiGraphics, String label, int x, int y, int width, int height, boolean primary, boolean enabled) {
        int background;
        int border;
        int textColor;

        if (!enabled) {
            background = 0xFF162233;
            border = 0xFF25384F;
            textColor = 0xFF5F7288;
        } else if (primary) {
            background = 0xFF2F9BE6;
            border = 0xFF73C8FF;
            textColor = 0xFFFFFFFF;
        } else {
            background = COLOR_PANEL;
            border = COLOR_BORDER;
            textColor = 0xFFFFFFFF;
        }

        drawPanel(guiGraphics, x, y, width, height, background, border);

        int textWidth = this.font.width(label);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - this.font.lineHeight) / 2;

        guiGraphics.drawString(this.font, label, textX, textY, textColor);
    }

    private void drawStepPanel(GuiGraphics guiGraphics, int x, int y, int width, int height, boolean active) {
        int border = active ? COLOR_BORDER_ACTIVE : COLOR_BORDER;
        int background = active ? COLOR_PANEL_ALT : COLOR_PANEL;

        drawPanel(guiGraphics, x, y, width, height, background, border);
    }

    private void drawInactiveOverlay(GuiGraphics guiGraphics, int x, int y, int width, int height, boolean inactive) {
        if (!inactive) {
            return;
        }

        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0x4408111D);
    }

    private void drawStatCard(GuiGraphics guiGraphics, int x, int y, int width, int height, String value, String label, String subtitle) {
        drawPanel(guiGraphics, x, y, width, height, COLOR_PANEL, COLOR_BORDER);

        guiGraphics.drawString(this.font, value, x + 16, y + 14, COLOR_TEXT);
        guiGraphics.drawString(this.font, label, x + 16, y + 32, COLOR_TEXT);
        guiGraphics.drawString(this.font, subtitle, x + 16, y + 46, COLOR_TEXT_DIM);
    }

    private void drawEmptyState(GuiGraphics guiGraphics, int x, int y, int width, int height, String title, String subtitle) {
        int panelWidth = 320;
        int panelHeight = 90;

        int panelX = x + (width / 2) - (panelWidth / 2);
        int panelY = y + (height / 2) - (panelHeight / 2);

        drawPanel(guiGraphics, panelX, panelY, panelWidth, panelHeight, COLOR_PANEL, COLOR_BORDER);

        int titleWidth = this.font.width(title);
        int subtitleWidth = this.font.width(subtitle);

        guiGraphics.drawString(
                this.font,
                title,
                panelX + (panelWidth / 2) - (titleWidth / 2),
                panelY + 24,
                COLOR_TEXT
        );

        guiGraphics.drawString(
                this.font,
                subtitle,
                panelX + (panelWidth / 2) - (subtitleWidth / 2),
                panelY + 44,
                COLOR_TEXT_DIM
        );
    }

    private void drawEmptyState(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        drawEmptyState(guiGraphics, x, y, width, height, "No assets found", "Try another category or import new assets.");
    }

    private void drawTabPlaceholder(GuiGraphics guiGraphics, int x, int y, int width, int height, String tabName) {
        int panelWidth = 360;
        int panelHeight = 110;

        int panelX = x + (width / 2) - (panelWidth / 2);
        int panelY = y + (height / 2) - (panelHeight / 2);

        drawPanel(guiGraphics, panelX, panelY, panelWidth, panelHeight, COLOR_PANEL, COLOR_BORDER);

        String title = tabName;
        String subtitle = "This section is not implemented yet.";
        String hint = "We will build this tab in the next steps.";

        int titleWidth = this.font.width(title);
        int subtitleWidth = this.font.width(subtitle);
        int hintWidth = this.font.width(hint);

        guiGraphics.drawString(
                this.font,
                title,
                panelX + (panelWidth / 2) - (titleWidth / 2),
                panelY + 20,
                COLOR_TEXT
        );

        guiGraphics.drawString(
                this.font,
                subtitle,
                panelX + (panelWidth / 2) - (subtitleWidth / 2),
                panelY + 44,
                COLOR_TEXT_DIM
        );

        guiGraphics.drawString(
                this.font,
                hint,
                panelX + (panelWidth / 2) - (hintWidth / 2),
                panelY + 62,
                COLOR_TEXT_DIM
        );
    }

    private String getImportStepLabel() {
        return switch (selectedImportStep) {
            case 1 -> "SELECT FILE";
            case 2 -> "PREVIEW IMAGE";
            case 3 -> "DETAILS";
            case 4 -> "SAVE ASSET";
            default -> "IMPORT";
        };
    }

    private String getImportStepSubtitle() {
        return switch (selectedImportStep) {
            case 1 -> mockStructureFileSelected
                    ? "Step 1 - Structure file selected. You can replace it if needed."
                    : "Step 1 - Choose a .schem or .schematic file.";
            case 2 -> mockPreviewImageSelected
                    ? "Step 2 - Preview image selected. You can replace it if needed."
                    : "Step 2 - Add an optional preview image for the asset.";
            case 3 -> mockDetailsFilled
                    ? "Step 3 - Metadata ready. You can review or clear the values."
                    : "Step 3 - Fill in the metadata and asset details.";
            case 4 -> mockAssetSaved
                    ? "Step 4 - Mock asset saved successfully."
                    : (mockDetailsFilled
                       ? "Step 4 - Review the generated asset card and save it."
                       : "Step 4 - Review everything and save the asset.");
            default -> "Import a new build asset into Archiv.";
        };
    }

    private void drawBrowseTab(GuiGraphics guiGraphics, int contentX, int contentY, int contentW, int contentH, List<ArchivAsset> visibleAssets) {
        int toolbarY = contentY + 14;
        int innerPadding = 18;

        int searchX = contentX + innerPadding;
        int searchW = 280;

        int filterX = searchX + searchW + 12;
        int filterW = 110;

        int sortLabelX = filterX + filterW + 18;
        int sortBoxX = sortLabelX + 60;
        int sortBoxW = 120;

        int viewLabelX = sortBoxX + sortBoxW + 18;
        int gridX = viewLabelX + 42;
        int gridW = 60;
        int listX = gridX + gridW + 8;
        int listW = 60;

        drawControlBox(guiGraphics, "Search assets...", searchX, toolbarY, searchW, 34);
        String filterLabel = browseFavoritesOnly ? "Fav Only" : "Filter";
        drawControlBox(guiGraphics, filterLabel, filterX, toolbarY, filterW, 34);
        guiGraphics.drawString(this.font, "Sort by:", sortLabelX, toolbarY + 12, COLOR_TEXT_DIM);
        drawControlBox(guiGraphics, "Newest", sortBoxX, toolbarY, sortBoxW, 34);
        guiGraphics.drawString(this.font, "View:", viewLabelX, toolbarY + 12, COLOR_TEXT_DIM);
        drawControlBox(guiGraphics, "Grid", gridX, toolbarY, gridW, 34);
        drawControlBox(guiGraphics, "List", listX, toolbarY, listW, 34);

        CardGridLayout layout = buildBrowseGridLayout(contentX, contentY, contentW, contentH);

        if (visibleAssets.isEmpty()) {
            drawEmptyState(guiGraphics, layout.cardsAreaX, layout.cardsAreaY, layout.cardsAreaW, layout.cardsAreaH);
        } else {
            for (int i = 0; i < visibleAssets.size(); i++) {
                ArchivAsset asset = visibleAssets.get(i);

                int column = i % layout.columns;
                int row = i / layout.columns;

                int cardX = layout.cardsAreaX + column * (layout.cardW + layout.cardsGap);
                int cardY = layout.cardsAreaY + row * (layout.cardH + layout.rowGap);

                drawAssetCard(
                        guiGraphics,
                        cardX,
                        cardY,
                        layout.cardW,
                        layout.cardH,
                        asset,
                        isLibraryAssetSelected(asset)
                );
            }
        }
    }

    private void drawMyAssetsTab(GuiGraphics guiGraphics, int contentX, int contentY, int contentW, int contentH, List<ArchivAsset> visibleAssets) {
        int innerPadding = 18;

        int titleX = contentX + innerPadding;
        int titleY = contentY + 16;

        guiGraphics.drawString(this.font, "My Assets", titleX, titleY, COLOR_TEXT);
        guiGraphics.drawString(this.font, "Your personal build library and saved collections.", titleX, titleY + 16, COLOR_TEXT_DIM);

        int statsY = titleY + 40;
        int statGap = 12;
        int statW = (contentW - (innerPadding * 2) - (statGap * 3)) / 4;
        int statH = 68;

        drawStatCard(guiGraphics, titleX, statsY, statW, statH, String.valueOf(getSavedFavoritesCount()), "Favorites", "Starred assets");
        drawStatCard(guiGraphics, titleX + statW + statGap, statsY, statW, statH, String.valueOf(savedAssets.size()), "Imported", "Imported assets");
        drawStatCard(guiGraphics, titleX + (statW + statGap) * 2, statsY, statW, statH, String.valueOf(getRecentAssetsCount()), "Recent", "Used recently");
        drawStatCard(guiGraphics, titleX + (statW + statGap) * 3, statsY, statW, statH, "0", "Collections", "Saved collections");

        int quickY = statsY + statH + 18;
        guiGraphics.drawString(this.font, "Quick Actions", titleX, quickY, COLOR_TEXT);

        int quickButtonY = quickY + 14;
        int quickButtonH = 30;
        int quickGap = 12;
        int quickW = (contentW - (innerPadding * 2) - (quickGap * 2)) / 3;

        drawButtonBox(guiGraphics, "Import Asset", titleX, quickButtonY, quickW, quickButtonH, true);
        drawButtonBox(guiGraphics, "Create Collection", titleX + quickW + quickGap, quickButtonY, quickW, quickButtonH, false);
        drawButtonBox(guiGraphics, "Open Local Folder", titleX + (quickW + quickGap) * 2, quickButtonY, quickW, quickButtonH, false);

        String importedSectionTitle = switch (selectedMyAssetsSection) {
            case "Favorites" -> "Favorite Assets";
            case "Recent" -> "Recent Assets";
            default -> "Imported Assets";
        };

        int importedTitleY = quickButtonY + quickButtonH + 20;
        guiGraphics.drawString(this.font, importedSectionTitle, titleX, importedTitleY, COLOR_TEXT);

        CardGridLayout layout = buildMyAssetsImportedGridLayout(contentX, contentY, contentW, contentH);

        if (!myAssetsShowsImportedGrid()) {
            drawEmptyState(
                    guiGraphics,
                    layout.cardsAreaX,
                    layout.cardsAreaY,
                    layout.cardsAreaW,
                    layout.cardsAreaH,
                    selectedMyAssetsSection,
                    "This management section is not implemented yet."
            );
            return;
        }

        if (visibleAssets.isEmpty()) {
            if ("Favorites".equals(selectedMyAssetsSection)) {
                drawEmptyState(
                        guiGraphics,
                        layout.cardsAreaX,
                        layout.cardsAreaY,
                        layout.cardsAreaW,
                        layout.cardsAreaH,
                        "No favorite assets yet",
                        "Star a saved asset to see it here."
                );
            } else {
                drawEmptyState(
                        guiGraphics,
                        layout.cardsAreaX,
                        layout.cardsAreaY,
                        layout.cardsAreaW,
                        layout.cardsAreaH,
                        "No saved assets yet",
                        "Import an asset and click Save to see it here."
                );
            }
        } else {
            for (int i = 0; i < visibleAssets.size(); i++) {
                ArchivAsset asset = visibleAssets.get(i);

                int column = i % layout.columns;
                int row = i / layout.columns;

                int cardX = layout.cardsAreaX + column * (layout.cardW + layout.cardsGap);
                int cardY = layout.cardsAreaY + row * (layout.cardH + layout.rowGap);

                drawAssetCard(
                        guiGraphics,
                        cardX,
                        cardY,
                        layout.cardW,
                        layout.cardH,
                        asset,
                        isLibraryAssetSelected(asset)
                );
            }
        }
    }

    private void drawImportTab(GuiGraphics guiGraphics, int rootX, int rootY, int rootW, int rootH,
                               int bodyY, int bodyH, int contentX, int contentY, int contentW, int contentH) {

        guiGraphics.drawString(this.font, "IMPORT STEPS", rootX + 16, bodyY + 14, COLOR_TEXT_DIM);

        int stepX = rootX + 12;
        int stepW = 156;
        int stepH = 34;
        int stepGap = 40;

        boolean fileStepActive = selectedImportStep == 1;
        boolean imageStepActive = selectedImportStep == 2;
        boolean detailsStepActive = selectedImportStep == 3;
        boolean saveStepActive = selectedImportStep == 4;
        boolean compactTopSections = detailsStepActive || saveStepActive;

        drawSidebarItem(guiGraphics, "1. Select File", stepX, bodyY + 34, stepW, stepH, selectedImportStep == 1);
        drawSidebarItem(guiGraphics, "2. Preview Image", stepX, bodyY + 34 + stepGap, stepW, stepH, selectedImportStep == 2);
        drawSidebarItem(guiGraphics, "3. Details", stepX, bodyY + 34 + (stepGap * 2), stepW, stepH, selectedImportStep == 3);
        drawSidebarItem(guiGraphics, "4. Save Asset", stepX, bodyY + 34 + (stepGap * 3), stepW, stepH, selectedImportStep == 4);

        ImportLayout layout = buildImportLayout(contentX, contentY, contentW, contentH);

        boolean compact = true;
        int detailsGap = compact ? 10 : 16;

        int innerX = layout.innerX;
        int innerY = layout.innerY;
        int innerW = layout.innerW;

        int sectionY = layout.sectionY;

        int previewColumnW = layout.previewColumnW;
        int leftAreaW = layout.leftAreaW;

        int topBoxH = layout.topBoxH;
        int structureW = layout.structureW;
        int imageW = layout.imageW;

        int structureX = layout.structureX;
        int imageX = layout.imageX;
        int previewX = layout.previewX;

        int detailsY = layout.detailsY;
        int detailsH = layout.detailsH;
        int actionsY = layout.actionsY;

        int boxButtonY = layout.boxButtonY;

        String importStepLabel = getImportStepLabel();
        String importStepSubtitle = getImportStepSubtitle();
        ImportPreset preset = getCurrentImportPreset();

        guiGraphics.drawString(this.font, "Import Asset", innerX, innerY, COLOR_TEXT);
        guiGraphics.drawString(this.font, importStepSubtitle, innerX, innerY + 16, COLOR_TEXT_DIM);

        int stepLabelWidth = this.font.width(importStepLabel);
        guiGraphics.drawString(this.font, importStepLabel, innerX + innerW - stepLabelWidth, innerY, COLOR_BORDER_ACTIVE);

        drawStepPanel(guiGraphics, structureX, sectionY, structureW, topBoxH, fileStepActive);
        drawStepPanel(guiGraphics, imageX, sectionY, imageW, topBoxH, imageStepActive);
        drawStepPanel(guiGraphics, previewX, sectionY, previewColumnW, topBoxH + detailsGap + detailsH, saveStepActive);

        int boxMainTextY = compact ? sectionY + 42 : sectionY + 76;
        int boxSubTextY = compact ? sectionY + 60 : sectionY + 92;

        guiGraphics.drawString(this.font, "1. Structure File", structureX + 12, sectionY + 12, COLOR_TEXT);

        if (compactTopSections) {
            if (mockStructureFileSelected) {
                guiGraphics.drawString(this.font, "Selected: " + mockStructureFileName, structureX + 20, sectionY + 34, COLOR_TEXT);
                guiGraphics.drawString(this.font, mockStructureFileFormat + "  •  " + mockStructureFileSize, structureX + 20, sectionY + 50, COLOR_TEXT_DIM);
            } else {
                guiGraphics.drawString(this.font, "No structure file selected", structureX + 20, sectionY + 34, COLOR_TEXT_DIM);
                guiGraphics.drawString(this.font, "Go back to step 1 to choose one", structureX + 20, sectionY + 50, COLOR_TEXT);
            }
        } else {
            if (mockStructureFileSelected) {
                int selectedInfoX = structureX + 20;
                int selectedInfoY = sectionY + 34;

                int replaceButtonW = 108;
                int replaceButtonH = 24;
                int replaceButtonX = structureX + structureW - replaceButtonW - 20;
                int replaceButtonY = sectionY + 34;

                guiGraphics.drawString(this.font, "Selected file:", selectedInfoX, selectedInfoY, COLOR_TEXT_DIM);
                guiGraphics.drawString(this.font, mockStructureFileName, selectedInfoX, selectedInfoY + 18, COLOR_TEXT);
                guiGraphics.drawString(this.font, mockStructureFileFormat + "  •  " + mockStructureFileSize, selectedInfoX, selectedInfoY + 34, COLOR_TEXT_DIM);

                drawButtonBox(guiGraphics, "Replace File", replaceButtonX, replaceButtonY, replaceButtonW, replaceButtonH, false);
            } else {
                guiGraphics.drawString(this.font, "Drop .schem / .schematic here", structureX + 45, boxMainTextY, COLOR_TEXT);
                guiGraphics.drawString(this.font, "Supports .schem and .schematic files", structureX + 26, boxSubTextY, COLOR_TEXT_DIM);
                drawButtonBox(guiGraphics, "Browse File", structureX + (structureW / 2) - 60, boxButtonY, 120, 28, false);
            }
        }

        drawInactiveOverlay(guiGraphics, structureX, sectionY, structureW, topBoxH, !fileStepActive);

        guiGraphics.drawString(this.font, "2. Preview Image (Optional)", imageX + 12, sectionY + 12, COLOR_TEXT);

        if (compactTopSections) {
            if (mockPreviewImageSelected) {
                int compactTextMaxW = imageW - 40;
                String compactImageName = fitTextToWidth(mockPreviewImageName, compactTextMaxW);

                guiGraphics.drawString(this.font, "Selected image:", imageX + 20, sectionY + 34, COLOR_TEXT_DIM);
                guiGraphics.drawString(this.font, compactImageName, imageX + 20, sectionY + 50, COLOR_TEXT);
            } else {
                guiGraphics.drawString(this.font, "Preview image: optional", imageX + 20, sectionY + 34, COLOR_TEXT_DIM);
                guiGraphics.drawString(this.font, "You can add or replace it later", imageX + 20, sectionY + 50, COLOR_TEXT);
            }
        } else {
            if (mockPreviewImageSelected) {
                int imageInfoX = imageX + 20;
                int imageInfoY = sectionY + 34;
                int imageTextMaxW = imageW - 40;

                String visibleImageName = fitTextToWidth(mockPreviewImageName, imageTextMaxW);

                int replaceButtonW = 118;
                int replaceButtonH = 24;
                int replaceButtonX = imageX + (imageW / 2) - (replaceButtonW / 2);
                int replaceButtonY = sectionY + topBoxH - replaceButtonH - 8;

                guiGraphics.drawString(this.font, "Selected image:", imageInfoX, imageInfoY, COLOR_TEXT_DIM);
                guiGraphics.drawString(this.font, visibleImageName, imageInfoX, imageInfoY + 18, COLOR_TEXT);
                guiGraphics.drawString(this.font, mockPreviewImageFormat + "  •  " + mockPreviewImageRatio, imageInfoX, imageInfoY + 34, COLOR_TEXT_DIM);

                drawButtonBox(guiGraphics, "Replace Image", replaceButtonX, replaceButtonY, replaceButtonW, replaceButtonH, false);
            } else {
                guiGraphics.drawString(this.font, "Drop image here", imageX + 40, boxMainTextY, COLOR_TEXT);
                guiGraphics.drawString(this.font, "PNG or JPG", imageX + 70, boxSubTextY, COLOR_TEXT_DIM);
                drawButtonBox(guiGraphics, "Browse Image", imageX + (imageW / 2) - 60, boxButtonY, 120, 28, false);
            }
        }

        drawInactiveOverlay(guiGraphics, imageX, sectionY, imageW, topBoxH, !imageStepActive);

        guiGraphics.drawString(this.font, "3. Asset Preview", previewX + 12, sectionY + 12, COLOR_TEXT);

        int previewImageX = previewX + 12;
        int previewImageY = sectionY + 30;
        int previewImageW = previewColumnW - 24;
        int previewImageH = compact ? 92 : 110;

        int previewColor = mockPreviewImageSelected ? MOCK_PREVIEW_IMAGE_COLOR : MOCK_NO_PREVIEW_IMAGE_COLOR;
        guiGraphics.fill(previewImageX, previewImageY, previewImageX + previewImageW, previewImageY + previewImageH, previewColor);

        String previewBannerText = mockPreviewImageSelected ? "IMAGE READY" : "PREVIEW";
        int previewBannerWidth = this.font.width(previewBannerText);
        guiGraphics.drawString(
                this.font,
                previewBannerText,
                previewImageX + (previewImageW / 2) - (previewBannerWidth / 2),
                previewImageY + (previewImageH / 2) - 4,
                0xFFFFFFFF
        );

        int previewInfoY = compact ? sectionY + 132 : sectionY + 150;

        String previewName = mockDetailsFilled ? preset.name : "Unnamed Asset";
        String previewType = mockDetailsFilled ? preset.type : "Unknown Type";
        String previewVersion = mockDetailsFilled ? preset.version : "Unknown";
        String previewAuthor = mockDetailsFilled ? preset.author : "Unknown";
        String previewCategory = mockDetailsFilled ? preset.macroCategory : "Uncategorized";
        String previewFormat = mockStructureFileSelected ? mockStructureFileFormat : "No file";
        String previewImageStatus = mockPreviewImageSelected ? mockPreviewImageFormat + "  •  " + mockPreviewImageRatio : "No preview image";

        guiGraphics.drawString(this.font, previewName, previewX + 12, previewInfoY, COLOR_TEXT);
        guiGraphics.drawString(this.font, previewType + "  •  " + previewVersion, previewX + 12, previewInfoY + 16, COLOR_TEXT_DIM);

        guiGraphics.drawString(this.font, "Author", previewX + 12, previewInfoY + 44, COLOR_TEXT_DIM);
        guiGraphics.drawString(this.font, previewAuthor, previewX + 90, previewInfoY + 44, COLOR_TEXT);

        guiGraphics.drawString(this.font, "Category", previewX + 12, previewInfoY + 60, COLOR_TEXT_DIM);
        guiGraphics.drawString(this.font, previewCategory, previewX + 90, previewInfoY + 60, COLOR_TEXT);

        guiGraphics.drawString(this.font, "Format", previewX + 12, previewInfoY + 76, COLOR_TEXT_DIM);
        guiGraphics.drawString(this.font, previewFormat, previewX + 90, previewInfoY + 76, COLOR_TEXT);

        guiGraphics.drawString(this.font, "Image", previewX + 12, previewInfoY + 92, COLOR_TEXT_DIM);
        guiGraphics.drawString(this.font, previewImageStatus, previewX + 90, previewInfoY + 92, COLOR_TEXT);

        drawInactiveOverlay(guiGraphics, previewX, sectionY, previewColumnW, topBoxH + detailsGap + detailsH, !saveStepActive);

        drawStepPanel(guiGraphics, innerX, detailsY, leftAreaW, detailsH, detailsStepActive);
        guiGraphics.drawString(this.font, "4. Asset Details", innerX + 12, detailsY + 12, COLOR_TEXT);

        int detailsActionW = layout.detailsActionW;
        int detailsActionH = layout.detailsActionH;
        int detailsActionX = layout.detailsActionX;
        int detailsActionY = layout.detailsActionY;

        if (detailsStepActive) {
            drawButtonBox(
                    guiGraphics,
                    mockDetailsFilled ? "Clear" : "Auto Fill",
                    detailsActionX,
                    detailsActionY,
                    detailsActionW,
                    detailsActionH,
                    false
            );
        }

        int formX = innerX + 12;
        int fieldGap = 12;
        int fieldH = compact ? 22 : 28;
        int verticalGap = compact ? 10 : 12;

        int fieldW1 = (leftAreaW - 48) / 3;
        int fieldW2 = fieldW1;
        int fieldW3 = fieldW1;
        int wideFieldW = (leftAreaW - 36) / 2;

        int topPadding = detailsStepActive
                ? (compact ? 44 : 50)
                : (compact ? 34 : 38);
        int bottomPadding = compact ? 10 : 14;

        int totalFieldsHeight = (fieldH * 3) + (verticalGap * 2);
        int availableFormHeight = detailsH - topPadding - bottomPadding;
        int extraSpace = Math.max(0, availableFormHeight - totalFieldsHeight);

        int formY1 = detailsY + topPadding + (extraSpace / 2);
        int formY2 = formY1 + fieldH + verticalGap;
        int formY3 = formY2 + fieldH + verticalGap;

        drawFieldBox(guiGraphics, mockDetailsFilled ? preset.name : "Asset Name...", formX, formY1, fieldW1, fieldH, mockDetailsFilled);
        drawFieldBox(guiGraphics, mockDetailsFilled ? preset.macroCategory : "Macro Category...", formX + fieldW1 + fieldGap, formY1, fieldW2, fieldH, mockDetailsFilled);
        drawFieldBox(guiGraphics, mockDetailsFilled ? preset.author : "Author...", formX + fieldW1 + fieldGap + fieldW2 + fieldGap, formY1, fieldW3, fieldH, mockDetailsFilled);

        drawFieldBox(guiGraphics, mockDetailsFilled ? preset.type : "Type...", formX, formY2, fieldW1, fieldH, mockDetailsFilled);
        drawFieldBox(guiGraphics, mockDetailsFilled ? preset.version : "Minecraft Version...", formX + fieldW1 + fieldGap, formY2, fieldW2, fieldH, mockDetailsFilled);
        drawFieldBox(guiGraphics, mockDetailsFilled ? preset.variants : "Variants...", formX + fieldW1 + fieldGap + fieldW2 + fieldGap, formY2, fieldW3, fieldH, mockDetailsFilled);

        drawFieldBox(guiGraphics, mockDetailsFilled ? preset.tags : "Tags...", formX, formY3, wideFieldW, fieldH, mockDetailsFilled);
        drawFieldBox(guiGraphics, mockDetailsFilled ? preset.fileInfo : "File Info...", formX + wideFieldW + fieldGap, formY3, wideFieldW, fieldH, mockDetailsFilled);

        drawInactiveOverlay(guiGraphics, innerX, detailsY, leftAreaW, detailsH, !detailsStepActive);

        int buttonH = layout.buttonH;
        int saveW = layout.saveW;
        int cancelW = layout.cancelW;
        int resetW = layout.resetW;

        int saveX = layout.saveX;
        int cancelX = layout.cancelX;
        int resetX = layout.resetX;

        if (saveStepActive) {
            int actionsGroupX = resetX - 10;
            int actionsGroupY = actionsY - 18;
            int actionsGroupW = (saveX + saveW) - actionsGroupX + 10;
            int actionsGroupH = buttonH + 30;

            drawStepPanel(guiGraphics, actionsGroupX, actionsGroupY, actionsGroupW, actionsGroupH, true);
        }

        boolean importReady = isImportReady();
        boolean hasImportState = hasImportData();

        if (saveStepActive) {
            String saveHint;
            if (mockAssetSaved) {
                saveHint = "Mock asset saved successfully";
            } else if (importReady) {
                saveHint = "Ready to save asset";
            } else {
                saveHint = "Asset is still incomplete";
            }

            guiGraphics.drawString(this.font, saveHint, resetX, actionsY - 14, COLOR_BORDER_ACTIVE);
        }

        drawButtonBoxState(guiGraphics, "Reset", resetX, actionsY, resetW, buttonH, false, hasImportState);
        drawButtonBoxState(guiGraphics, "Cancel", cancelX, actionsY, cancelW, buttonH, false, true);
        drawButtonBoxState(guiGraphics, mockAssetSaved ? "Saved" : "Save", saveX, actionsY, saveW, buttonH, true, importReady || mockAssetSaved);

        if (saveStepActive && (importReady || mockAssetSaved)) {
            guiGraphics.fill(saveX - 2, actionsY - 2, saveX + saveW + 2, actionsY, COLOR_BORDER_ACTIVE);
            guiGraphics.fill(saveX - 2, actionsY + buttonH, saveX + saveW + 2, actionsY + buttonH + 2, COLOR_BORDER_ACTIVE);
            guiGraphics.fill(saveX - 2, actionsY - 2, saveX, actionsY + buttonH + 2, COLOR_BORDER_ACTIVE);
            guiGraphics.fill(saveX + saveW, actionsY - 2, saveX + saveW + 2, actionsY + buttonH + 2, COLOR_BORDER_ACTIVE);
        }
    }

    private void drawAssetCard(GuiGraphics guiGraphics, int x, int y, int width, int height, ArchivAsset asset, boolean selected) {
        int border = selected ? COLOR_BORDER_ACTIVE : COLOR_BORDER;
        drawPanel(guiGraphics, x, y, width, height, COLOR_PANEL, border);

        AssetCardLayout layout = buildAssetCardLayout(x, y, width, height);

        guiGraphics.fill(
                layout.previewX,
                layout.previewY,
                layout.previewX + layout.previewW,
                layout.previewY + layout.previewH,
                asset.getPreviewColor()
        );

        String previewText = "PREVIEW";
        int textWidth = this.font.width(previewText);
        guiGraphics.drawString(
                this.font,
                previewText,
                layout.previewX + (layout.previewW / 2) - (textWidth / 2),
                layout.previewY + (layout.previewH / 2) - 4,
                0xFFFFFFFF
        );

        guiGraphics.drawString(
                this.font,
                asset.isFavorite() ? "★" : "☆",
                layout.favoriteX + 4,
                layout.favoriteY + 6,
                0xFFFFD45A
        );

        if (selected) {
            guiGraphics.fill(
                    layout.previewX,
                    layout.previewY,
                    layout.previewX + layout.previewW,
                    layout.previewY + layout.previewH,
                    0x55000000
            );

            drawPanel(guiGraphics, layout.overlayX, layout.loadY, layout.overlayW, layout.loadH, 0xFF2F9BE6, 0xFF73C8FF);
            drawPanel(guiGraphics, layout.overlayX, layout.detailsY, layout.overlayW, layout.detailsH, 0xFF1A2638, COLOR_BORDER);

            guiGraphics.drawString(this.font, "Load", x + (width / 2) - 12, layout.loadY + 8, 0xFFFFFFFF);
            guiGraphics.drawString(this.font, "Details", x + (width / 2) - 18, layout.detailsY + 7, 0xFFE5EEF8);
        }

        int infoY = y + layout.previewH + 12;
        guiGraphics.drawString(this.font, asset.getName(), x + 12, infoY, COLOR_TEXT);
        guiGraphics.drawString(this.font, asset.getVersion(), x + 12, infoY + 14, COLOR_TEXT_DIM);

        drawChip(guiGraphics, asset.getType(), x + width - 84, infoY - 2, 72, 18, asset.getChipColor());

        int visibleDots = Math.min(asset.getVariantCount(), 4);
        int dotY = infoY + 34;

        for (int i = 0; i < visibleDots; i++) {
            drawDot(guiGraphics, x + 12 + (i * 12), dotY, 0xFF8B6A4A + (i * 0x00111111));
        }

        if (asset.getVariantCount() > 4) {
            int remaining = asset.getVariantCount() - 4;
            guiGraphics.drawString(this.font, "+" + remaining, x + 12 + (visibleDots * 12), infoY + 31, COLOR_TEXT_DIM);
        }
    }

    private void drawChip(GuiGraphics guiGraphics, String label, int x, int y, int width, int height, int color) {
        drawPanel(guiGraphics, x, y, width, height, color, color);
        guiGraphics.drawString(this.font, label, x + 8, y + 6, 0xFFFFFFFF);
    }

    private void drawDot(GuiGraphics guiGraphics, int x, int y, int color) {
        guiGraphics.fill(x, y, x + 8, y + 8, color);
    }
}