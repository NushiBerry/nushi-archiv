package com.nushi.archiv.client.screen;

import com.nushi.archiv.client.model.ArchivAsset;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
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
    private String browseSortMode = "Newest";
    private boolean browseFavoritesOnly = false;
    private String browseViewMode = "Grid";
    private int selectedImportStep = 1;

    private String selectedLibraryAssetName = null;
    private String libraryActionMessage = "No asset selected";

    private String loadedAssetName = null;
    private final List<String> recentLoadedAssetNames = new ArrayList<>();

    private EditBox browseSearchBox;

    private boolean assetDetailsOpen = false;
    private String detailsAssetName = null;

    private boolean deleteConfirmOpen = false;
    private String deleteConfirmAssetName = null;

    private boolean listMenuOpen = false;
    private String listMenuAssetName = null;

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

    private int browseScrollOffset = 0;
    private int browseMaxScroll = 0;
    private boolean browseScrollbarDragging = false;
    private int browseScrollbarDragOffset = 0;

    private int myAssetsScrollOffset = 0;
    private int myAssetsMaxScroll = 0;
    private boolean myAssetsScrollbarDragging = false;
    private int myAssetsScrollbarDragOffset = 0;

    private final CollectionEntry[] collectionEntries = new CollectionEntry[] {
            new CollectionEntry("Medieval Village Kit", 43, 0xFF6C89B8, 0xFF6D4BC8),
            new CollectionEntry("Nature Props", 28, 0xFF4E7C57, 0xFF3D9B63),
            new CollectionEntry("Cyberpunk Signs", 21, 0xFF3A4F84, 0xFFDA8A2D)
    };

    private static class BrowseToolbarLayout {
        int toolbarY;
        int searchX;
        int searchW;
        int filterX;
        int filterW;
        int sortLabelX;
        int sortBoxX;
        int sortBoxW;
        int viewLabelX;
        int gridX;
        int gridW;
        int listX;
        int listW;
    }

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

    private static class CollectionEntry {
        final String name;
        final int assetCount;
        final int previewColor;
        final int accentColor;

        CollectionEntry(String name, int assetCount, int previewColor, int accentColor) {
            this.name = name;
            this.assetCount = assetCount;
            this.previewColor = previewColor;
            this.accentColor = accentColor;
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

    private static class BrowseListLayout {
        int listX;
        int listY;
        int listW;
        int rowH;
        int rowGap;
    }

    private static class BrowseListRowLayout {
        int previewX;
        int previewY;
        int previewW;
        int previewH;

        int infoX;
        int titleY;
        int versionY;
        int dotsY;

        int dividerX;

        int chipX;
        int chipY;
        int chipW;
        int chipH;

        int favoriteX;
        int favoriteY;
        int favoriteBoxSize;

        int loadX;
        int loadY;
        int loadW;
        int loadH;

        int detailsX;
        int detailsY;
        int detailsW;
        int detailsH;

        int menuDotsX;
        int menuDotsY;

        int menuX;
        int menuY;
        int menuW;
        int menuH;
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

        int menuDotsX;
        int menuDotsY;

        int menuX;
        int menuY;
        int menuW;
        int menuH;
    }

    private static class AssetDetailsModalLayout {
        int panelX;
        int panelY;
        int panelW;
        int panelH;

        int closeX;
        int closeY;
        int closeSize;

        int previewX;
        int previewY;
        int previewW;
        int previewH;

        int chipX;
        int chipY;

        int closeButtonX;
        int closeButtonY;
        int closeButtonW;
        int closeButtonH;

        int loadButtonX;
        int loadButtonY;
        int loadButtonW;
        int loadButtonH;
    }


    private static class ScreenChromeLayout {
        int margin;
        int rootX;
        int rootY;
        int rootW;
        int rootH;
        int headerH;
        int footerH;
        int sidebarW;
        int bodyY;
        int bodyH;
        int contentX;
        int contentY;
        int contentW;
        int contentH;
        int tabY;
        int tabX;
        int tabW;
        int tabH;
        int tabGap;
        int myAssetsX;
        int myAssetsW;
        int importX;
        int settingsX;
        int sidebarItemX;
        int sidebarItemY;
        int sidebarItemW;
        int sidebarItemH;
        int sidebarItemGap;
    }

    private static class ViewportLayout {
        int x;
        int y;
        int w;
        int h;
    }

    private static class ScrollbarLayout {
        int trackX;
        int trackY;
        int trackW;
        int trackH;

        int thumbX;
        int thumbY;
        int thumbW;
        int thumbH;
    }

    public ArchivScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;
        this.mockAssets = new ArrayList<>();
    }

    @Override
    protected void init() {
        ScreenChromeLayout chrome = buildChromeLayout();
        int closeButtonSize = 24;
        int closeX = this.width - 20 - closeButtonSize;
        int closeY = 20;

        this.addRenderableWidget(
                Button.builder(Component.literal("X"), button -> this.onClose())
                        .bounds(closeX, closeY, closeButtonSize, closeButtonSize)
                        .build()
        );

        BrowseToolbarLayout toolbar = buildBrowseToolbarLayout(chrome.contentX, chrome.contentY, chrome.contentW);

        browseSearchBox = new EditBox(
                this.font,
                toolbar.searchX + 4,
                toolbar.toolbarY + 4,
                toolbar.searchW - 8,
                26,
                Component.literal("Search assets")
        );

        browseSearchBox.setBordered(false);
        browseSearchBox.setMaxLength(64);
        browseSearchBox.setTextColor(COLOR_TEXT);
        browseSearchBox.setTextColorUneditable(COLOR_TEXT_DIM);
        browseSearchBox.setHint(Component.literal("Search assets..."));
        browseSearchBox.setResponder(value -> {
            resetBrowseScroll();
            syncLibrarySelectionWithVisibleAssets();
        });

        this.addRenderableWidget(browseSearchBox);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private BrowseToolbarLayout buildBrowseToolbarLayout(int contentX, int contentY, int contentW) {
        BrowseToolbarLayout layout = new BrowseToolbarLayout();

        int innerPadding = 18;
        int toolbarY = contentY + 14;
        int gap = 12;

        int filterW = 120;
        int sortBoxW = 120;
        int gridW = 54;
        int listW = 54;

        int right = contentX + contentW - innerPadding;

        layout.listW = listW;
        layout.listX = right - listW;
        right = layout.listX - 8;

        layout.gridW = gridW;
        layout.gridX = right - gridW;
        right = layout.gridX - 14;

        layout.viewLabelX = right - 42;
        right = layout.viewLabelX - 18;

        layout.sortBoxW = sortBoxW;
        layout.sortBoxX = right - sortBoxW;
        right = layout.sortBoxX - 10;

        layout.sortLabelX = right - 64;
        right = layout.sortLabelX - 18;

        layout.filterW = filterW;
        layout.filterX = right - filterW;
        right = layout.filterX - gap;

        layout.searchX = contentX + innerPadding;
        layout.searchW = Math.max(180, right - layout.searchX);
        layout.toolbarY = toolbarY;

        return layout;
    }

    private ScreenChromeLayout buildChromeLayout() {
        ScreenChromeLayout layout = new ScreenChromeLayout();

        layout.margin = 20;
        layout.rootX = layout.margin;
        layout.rootY = layout.margin;
        layout.rootW = this.width - (layout.margin * 2);
        layout.rootH = this.height - (layout.margin * 2);

        layout.headerH = 58;
        layout.footerH = 24;
        layout.sidebarW = 180;

        layout.bodyY = layout.rootY + layout.headerH;
        layout.bodyH = layout.rootH - layout.headerH - layout.footerH;

        layout.contentX = layout.rootX + layout.sidebarW;
        layout.contentY = layout.bodyY;
        layout.contentW = layout.rootW - layout.sidebarW;
        layout.contentH = layout.bodyH;

        layout.tabY = layout.rootY + 10;
        layout.tabX = layout.rootX + 150;
        layout.tabW = 110;
        layout.tabH = 38;
        layout.tabGap = 8;

        layout.myAssetsX = layout.tabX + (layout.tabW + layout.tabGap);
        layout.myAssetsW = layout.tabW + 10;
        layout.importX = layout.tabX + (layout.tabW + layout.tabGap) * 2 + 10;
        layout.settingsX = layout.tabX + (layout.tabW + layout.tabGap) * 3 + 10;

        layout.sidebarItemX = layout.rootX + 12;
        layout.sidebarItemY = layout.bodyY + 34;
        layout.sidebarItemW = layout.sidebarW - 24;
        layout.sidebarItemH = 34;
        layout.sidebarItemGap = 40;

        return layout;
    }

    private ViewportLayout buildBrowseViewportLayout(ScreenChromeLayout chrome) {
        ViewportLayout layout = new ViewportLayout();
        int innerPadding = 18;
        int toolbarY = chrome.contentY + 14;

        layout.x = chrome.contentX + innerPadding;
        layout.y = toolbarY + 78;
        layout.w = chrome.contentW - (innerPadding * 2) - 16;
        layout.h = (chrome.contentY + chrome.contentH - 16) - layout.y;

        return layout;
    }

    private void resetBrowseScroll() {
        browseScrollOffset = 0;
        browseScrollbarDragging = false;
    }

    private void resetMyAssetsScroll() {
        myAssetsScrollOffset = 0;
        myAssetsScrollbarDragging = false;
    }

    private int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private List<ArchivAsset> getVisibleAssets() {
        List<ArchivAsset> filteredAssets = new ArrayList<>();

        if ("Browse".equals(selectedTopTab)) {
            List<ArchivAsset> sourceAssets = new ArrayList<>(mockAssets);
            sourceAssets.addAll(savedAssets);

            String searchQuery = getBrowseSearchQuery();

            for (ArchivAsset asset : sourceAssets) {
                boolean categoryMatches = selectedCategory.equals("All")
                        || asset.getMacroCategory().equals(selectedCategory);

                boolean favoriteMatches = !browseFavoritesOnly || asset.isFavorite();

                boolean searchMatches = searchQuery.isBlank()
                        || asset.getName().toLowerCase().contains(searchQuery)
                        || asset.getType().toLowerCase().contains(searchQuery)
                        || asset.getMacroCategory().toLowerCase().contains(searchQuery)
                        || asset.getVersion().toLowerCase().contains(searchQuery);

                if (categoryMatches && favoriteMatches && searchMatches) {
                    filteredAssets.add(asset);
                }
            }

            applyBrowseSort(filteredAssets);
            return filteredAssets;
        }

        if ("My Assets".equals(selectedTopTab)) {
            if ("Recent".equals(selectedMyAssetsSection)) {
                List<ArchivAsset> recentAssets = new ArrayList<>();

                for (String recentName : recentLoadedAssetNames) {
                    for (ArchivAsset asset : savedAssets) {
                        if (asset.getName().equals(recentName)) {
                            recentAssets.add(asset);
                            break;
                        }
                    }
                }

                return recentAssets;
            }

            for (ArchivAsset asset : savedAssets) {
                boolean include = switch (selectedMyAssetsSection) {
                    case "All Assets", "Imported" -> true;
                    case "Favorites" -> asset.isFavorite();
                    default -> false;
                };

                if (include) {
                    filteredAssets.add(asset);
                }
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
        int gap = compact ? 12 : 16;
        int detailsGap = compact ? 10 : 16;
        int actionsBarH = 28;
        int actionsBottomMargin = compact ? 8 : 18;
        int actionsGap = saveStepActive ? 28 : (compact ? 8 : 12);
        int fieldGap = 12;
        int titleBlockH = compact ? 48 : 40;

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

    private CardGridLayout buildBrowseGridLayout(int contentX, int contentY, int contentW, int assetCount, int scrollOffset) {
        CardGridLayout layout = new CardGridLayout();

        int innerPadding = 18;
        int scrollbarReserve = 16;
        int toolbarY = contentY + 14;

        layout.cardsAreaX = contentX + innerPadding;
        layout.cardsAreaY = toolbarY + 78 - scrollOffset;
        layout.cardsAreaW = contentW - (innerPadding * 2) - scrollbarReserve;

        layout.cardsGap = 14;
        layout.rowGap = 16;
        layout.columns = 3;
        layout.rows = Math.max(1, (Math.max(assetCount, 1) + layout.columns - 1) / layout.columns);

        layout.cardW = (layout.cardsAreaW - (layout.cardsGap * (layout.columns - 1))) / layout.columns;
        layout.cardH = 150;
        layout.cardsAreaH = (layout.cardH * layout.rows) + (layout.rowGap * (layout.rows - 1));

        return layout;
    }

    private BrowseListLayout buildBrowseListLayout(int contentX, int contentY, int contentW, int scrollOffset) {
        BrowseListLayout layout = new BrowseListLayout();

        int innerPadding = 18;
        int scrollbarReserve = 16;
        int toolbarY = contentY + 14;

        layout.listX = contentX + innerPadding;
        layout.listY = toolbarY + 78 - scrollOffset;
        layout.listW = contentW - (innerPadding * 2) - scrollbarReserve;
        layout.rowH = 68;
        layout.rowGap = 8;

        return layout;
    }

    private int getBrowseListContentHeight(int assetCount, BrowseListLayout layout) {
        if (assetCount <= 0) {
            return 160;
        }

        return (assetCount * layout.rowH) + ((assetCount - 1) * layout.rowGap);
    }

    private BrowseListRowLayout buildBrowseListRowLayout(int x, int y, int width, int height) {
        BrowseListRowLayout layout = new BrowseListRowLayout();

        int padding = 10;
        int rightPadding = 18;
        int menuDotsReserve = 18;
        int gapBetweenButtons = 8;

        // preview
        layout.previewW = 84;
        layout.previewH = 42;
        layout.previewX = x + padding;
        layout.previewY = y + (height - layout.previewH) / 2;

        // infos
        layout.infoX = layout.previewX + layout.previewW + 14;
        layout.titleY = y + 14;
        layout.versionY = y + 28;
        layout.dotsY = y + 42;

        // botões na direita, com folga da borda
        layout.loadW = 70;
        layout.loadH = 22;
        layout.detailsW = 76;
        layout.detailsH = 22;

        layout.detailsX = x + width - rightPadding - menuDotsReserve - layout.detailsW;
        layout.detailsY = y + (height - layout.detailsH) / 2;

        layout.loadX = layout.detailsX - gapBetweenButtons - layout.loadW;
        layout.loadY = layout.detailsY;

        // divisória antes da área de ações
        layout.dividerX = layout.loadX - 18;

        // estrela entre badge e divisória
        layout.favoriteBoxSize = 16;
        layout.favoriteX = layout.dividerX - 24;
        layout.favoriteY = y + 12;

        // badge mais em cima
        layout.chipH = 18;
        layout.chipY = y + 10;

        layout.menuDotsX = x + width - 18;
        layout.menuDotsY = y + (height / 2) - 6;

        layout.menuW = 112;
        layout.menuH = 22;
        layout.menuX = x + width - layout.menuW - 28;
        layout.menuY = y + 8;

        return layout;
    }

    private int getBrowseListVisibleRowCount(int contentY, int contentH, BrowseListLayout layout, int assetCount) {
        int availableBottom = contentY + contentH - 16;
        int maxRows = Math.max(1, (availableBottom - layout.listY + layout.rowGap) / (layout.rowH + layout.rowGap));
        return Math.min(assetCount, maxRows);
    }

    private CardGridLayout buildMyAssetsImportedGridLayout(int contentX, int contentW, int importedTitleY, int assetCount) {
        CardGridLayout layout = new CardGridLayout();

        int innerPadding = 18;

        layout.cardsAreaX = contentX + innerPadding;
        layout.cardsAreaY = importedTitleY + 18;
        layout.cardsAreaW = contentW - (innerPadding * 2);

        layout.cardsGap = 14;
        layout.rowGap = 16;
        layout.columns = 3;
        layout.rows = Math.max(1, (Math.max(assetCount, 1) + layout.columns - 1) / layout.columns);

        layout.cardW = (layout.cardsAreaW - (layout.cardsGap * (layout.columns - 1))) / layout.columns;
        layout.cardH = 150;
        layout.cardsAreaH = (layout.cardH * layout.rows) + (layout.rowGap * (layout.rows - 1));

        return layout;
    }

    private ScrollbarLayout buildMyAssetsScrollbarLayout(int contentX, int contentY, int contentW, int contentH) {
        ScrollbarLayout layout = new ScrollbarLayout();

        layout.trackW = 8;
        layout.trackX = contentX + contentW - 12;
        layout.trackY = contentY + 10;
        layout.trackH = contentH - 20;

        layout.thumbW = 8;

        if (myAssetsMaxScroll <= 0) {
            layout.thumbH = layout.trackH;
            layout.thumbX = layout.trackX;
            layout.thumbY = layout.trackY;
            return layout;
        }

        int viewportH = contentH - 2;
        int contentTotalH = viewportH + myAssetsMaxScroll;

        layout.thumbH = Math.max(24, (layout.trackH * viewportH) / Math.max(contentTotalH, 1));

        int movableTrack = layout.trackH - layout.thumbH;
        layout.thumbY = layout.trackY + (myAssetsScrollOffset * movableTrack) / Math.max(myAssetsMaxScroll, 1);
        layout.thumbX = layout.trackX;

        return layout;
    }

    private ScrollbarLayout buildBrowseScrollbarLayout(int contentX, int contentY, int contentW, int contentH) {
        ScrollbarLayout layout = new ScrollbarLayout();

        int toolbarY = contentY + 14;
        int viewportY = toolbarY + 78;
        int viewportBottom = contentY + contentH - 16;

        layout.trackW = 8;
        layout.trackX = contentX + contentW - 12;
        layout.trackY = viewportY + 2;
        layout.trackH = viewportBottom - viewportY - 4;

        layout.thumbW = 8;

        if (browseMaxScroll <= 0) {
            layout.thumbH = layout.trackH;
            layout.thumbX = layout.trackX;
            layout.thumbY = layout.trackY;
            return layout;
        }

        int viewportH = viewportBottom - viewportY;
        int contentTotalH = viewportH + browseMaxScroll;

        layout.thumbH = Math.max(24, (layout.trackH * viewportH) / Math.max(contentTotalH, 1));

        int movableTrack = layout.trackH - layout.thumbH;
        layout.thumbY = layout.trackY + (browseScrollOffset * movableTrack) / Math.max(browseMaxScroll, 1);
        layout.thumbX = layout.trackX;

        return layout;
    }

    private AssetCardLayout buildAssetCardLayout(int x, int y, int width, int height) {
        AssetCardLayout layout = new AssetCardLayout();

        layout.previewX = x + 1;
        layout.previewY = y + 1;
        layout.previewW = width - 2;

        int minPreviewHeight = 72;
        int minBodyHeight = 56;
        int preferredPreviewHeight = (int) (height * 0.55);

        layout.previewH = Math.min(
                Math.max(minPreviewHeight, preferredPreviewHeight),
                height - minBodyHeight
        );

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

        layout.menuDotsX = x + 8;
        layout.menuDotsY = y + 8;

        layout.menuW = 112;
        layout.menuH = 22;
        layout.menuX = x + 8;
        layout.menuY = y + 24;

        return layout;
    }

    private AssetDetailsModalLayout buildAssetDetailsModalLayout(ArchivAsset asset) {
        AssetDetailsModalLayout layout = new AssetDetailsModalLayout();

        layout.panelW = 420;
        layout.panelH = 312;
        layout.panelX = (this.width - layout.panelW) / 2;
        layout.panelY = (this.height - layout.panelH) / 2;

        layout.closeSize = 20;
        layout.closeX = layout.panelX + layout.panelW - layout.closeSize - 12;
        layout.closeY = layout.panelY + 12;

        layout.previewX = layout.panelX + 16;
        layout.previewY = layout.panelY + 44;
        layout.previewW = layout.panelW - 32;
        layout.previewH = 96;

        int chipW = getChipWidth(asset.getType());
        layout.chipX = layout.panelX + layout.panelW - chipW - 48;
        layout.chipY = layout.panelY + 14;

        layout.closeButtonW = 90;
        layout.closeButtonH = 24;
        layout.loadButtonW = 84;
        layout.loadButtonH = 24;

        layout.closeButtonY = layout.panelY + layout.panelH - 34;
        layout.loadButtonY = layout.closeButtonY;
        layout.closeButtonX = layout.panelX + layout.panelW - 16 - layout.closeButtonW;
        layout.loadButtonX = layout.closeButtonX - 10 - layout.loadButtonW;

        return layout;
    }

    private int getMyAssetsImportedTitleY(int contentY) {
        int titleY = contentY + 16;
        int statsY = titleY + 40;
        int statH = 68;
        int quickY = statsY + statH + 18;
        int quickButtonY = quickY + 14;
        int quickButtonH = 30;

        int currentY = quickButtonY + quickButtonH + 20;

        if ("All Assets".equals(selectedMyAssetsSection)) {
            int collectionCardH = 88;
            currentY += 18 + collectionCardH + 20;

            if (!getRecentAssets(4).isEmpty()) {
                int recentCardH = 62;
                currentY += 18 + recentCardH + 20;
            }
        }

        return currentY;
    }

    private List<ArchivAsset> getRecentAssets(int limit) {
        List<ArchivAsset> recentAssets = new ArrayList<>();

        for (String recentName : recentLoadedAssetNames) {
            for (ArchivAsset asset : savedAssets) {
                if (asset.getName().equals(recentName)) {
                    recentAssets.add(asset);
                    break;
                }
            }

            if (recentAssets.size() >= limit) {
                break;
            }
        }

        return recentAssets;
    }

    private void openAssetDetails(ArchivAsset asset) {
        closeListMenu();
        assetDetailsOpen = true;
        detailsAssetName = asset.getName();
        selectedLibraryAssetName = asset.getName();
        libraryActionMessage = "Viewing details: " + asset.getName();
    }

    private void drawDeleteConfirmModal(GuiGraphics guiGraphics) {
        if (!deleteConfirmOpen) {
            return;
        }

        ArchivAsset asset = getDeleteConfirmAsset();
        if (asset == null) {
            return;
        }

        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);

        int panelW = 340;
        int panelH = 150;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        drawPanel(guiGraphics, panelX, panelY, panelW, panelH, COLOR_PANEL_ALT, 0xFFB54B63);

        guiGraphics.drawString(this.font, "Delete Asset", panelX + 14, panelY + 14, 0xFFFFD7DE);

        String line1 = "Are you sure you want to delete:";
        String line2 = asset.getName() + "?";

        guiGraphics.drawString(this.font, line1, panelX + 14, panelY + 42, COLOR_TEXT_DIM);
        guiGraphics.drawString(this.font, line2, panelX + 14, panelY + 58, COLOR_TEXT);

        guiGraphics.drawString(this.font, "This action cannot be undone.", panelX + 14, panelY + 82, COLOR_TEXT_DIM);

        int buttonY = panelY + panelH - 36;
        int cancelW = 90;
        int deleteW = 96;
        int gap = 10;

        int deleteX = panelX + panelW - 14 - deleteW;
        int cancelX = deleteX - gap - cancelW;

        drawPanel(guiGraphics, cancelX, buttonY, cancelW, 24, COLOR_PANEL, COLOR_BORDER);
        drawPanel(guiGraphics, deleteX, buttonY, deleteW, 24, 0xFF7B2438, 0xFFB54B63);

        guiGraphics.drawString(this.font, "Cancel", cancelX + 23, buttonY + 8, COLOR_TEXT);
        guiGraphics.drawString(this.font, "Delete", deleteX + 25, buttonY + 8, 0xFFFFFFFF);
    }

    private void openDeleteConfirm(ArchivAsset asset) {
        closeListMenu();
        deleteConfirmOpen = true;
        deleteConfirmAssetName = asset.getName();
        selectedLibraryAssetName = asset.getName();
    }

    private void closeDeleteConfirm() {
        deleteConfirmOpen = false;
        deleteConfirmAssetName = null;
    }

    private ArchivAsset getDeleteConfirmAsset() {
        if (deleteConfirmAssetName == null) {
            return null;
        }

        for (ArchivAsset asset : savedAssets) {
            if (asset.getName().equals(deleteConfirmAssetName)) {
                return asset;
            }
        }

        return null;
    }

    private boolean isSavedAsset(ArchivAsset asset) {
        for (ArchivAsset savedAsset : savedAssets) {
            if (savedAsset.getName().equals(asset.getName())) {
                return true;
            }
        }

        return false;
    }

    private void openListMenu(ArchivAsset asset) {
        if (!isSavedAsset(asset)) {
            return;
        }

        listMenuOpen = true;
        listMenuAssetName = asset.getName();
        selectedLibraryAssetName = asset.getName();
    }

    private void closeListMenu() {
        listMenuOpen = false;
        listMenuAssetName = null;
    }

    private boolean isListMenuOpenFor(ArchivAsset asset) {
        return listMenuOpen
                && listMenuAssetName != null
                && listMenuAssetName.equals(asset.getName());
    }

    private void deleteAsset(ArchivAsset asset) {
        if (!isSavedAsset(asset)) {
            return;
        }

        String deletedName = asset.getName();

        savedAssets.removeIf(savedAsset -> savedAsset.getName().equals(deletedName));
        recentLoadedAssetNames.remove(deletedName);

        if (deletedName.equals(loadedAssetName)) {
            loadedAssetName = null;
        }

        if (deletedName.equals(detailsAssetName)) {
            assetDetailsOpen = false;
            detailsAssetName = null;
        }

        if (deletedName.equals(selectedLibraryAssetName)) {
            selectedLibraryAssetName = null;
        }

        closeListMenu();
        syncLibrarySelectionWithVisibleAssets();
        libraryActionMessage = "Deleted: " + deletedName;
    }

    private void loadAsset(ArchivAsset asset) {
        closeListMenu();

        // fecha o modal sem apagar a mensagem final de load
        assetDetailsOpen = false;
        detailsAssetName = null;

        loadedAssetName = asset.getName();
        selectedLibraryAssetName = asset.getName();
        libraryActionMessage = "Loaded: " + asset.getName();

        recentLoadedAssetNames.remove(asset.getName());
        recentLoadedAssetNames.add(0, asset.getName());

        if (recentLoadedAssetNames.size() > 12) {
            recentLoadedAssetNames.remove(recentLoadedAssetNames.size() - 1);
        }
    }

    private void closeAssetDetails() {
        assetDetailsOpen = false;
        detailsAssetName = null;
        libraryActionMessage = selectedLibraryAssetName != null
                ? "Selected: " + selectedLibraryAssetName
                : "No asset selected";
    }

    private ArchivAsset getDetailsAsset() {
        if (detailsAssetName == null) {
            return null;
        }

        for (ArchivAsset asset : savedAssets) {
            if (asset.getName().equals(detailsAssetName)) {
                return asset;
            }
        }

        for (ArchivAsset asset : mockAssets) {
            if (asset.getName().equals(detailsAssetName)) {
                return asset;
            }
        }

        return null;
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
        return recentLoadedAssetNames.size();
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

    private boolean isAssetLoaded(ArchivAsset asset) {
        return loadedAssetName != null && loadedAssetName.equals(asset.getName());
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

    private void cycleBrowseSortMode() {
        browseSortMode = switch (browseSortMode) {
            case "Newest" -> "Oldest";
            case "Oldest" -> "A-Z";
            case "A-Z" -> "Z-A";
            default -> "Newest";
        };
    }

    private void applyBrowseSort(List<ArchivAsset> assets) {
        switch (browseSortMode) {
            case "Oldest" -> {
                List<ArchivAsset> reversed = new ArrayList<>();
                for (int i = assets.size() - 1; i >= 0; i--) {
                    reversed.add(assets.get(i));
                }
                assets.clear();
                assets.addAll(reversed);
            }
            case "A-Z" -> assets.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
            case "Z-A" -> assets.sort((a, b) -> b.getName().compareToIgnoreCase(a.getName()));
            case "Newest" -> {
            }
        }
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

    private String getBrowseFavoritesLabel() {
        return browseFavoritesOnly ? "Favorites only" : "All assets";
    }

    private String getBrowseSearchQuery() {
        if (browseSearchBox == null) {
            return "";
        }

        return browseSearchBox.getValue().trim().toLowerCase();
    }

    private String getBrowseSummaryText(List<ArchivAsset> visibleAssets) {
        String searchLabel = getBrowseSearchQuery().isBlank()
                ? "None"
                : browseSearchBox.getValue().trim();

        return "Category: " + selectedCategory
                + "  •  Filter: " + getBrowseFavoritesLabel()
                + "  •  Sort: " + browseSortMode
                + "  •  View: " + browseViewMode
                + "  •  Search: " + searchLabel
                + "  •  Results: " + visibleAssets.size();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }

        ScreenChromeLayout chrome = buildChromeLayout();
        double mouseX = event.x();
        double mouseY = event.y();

        if (assetDetailsOpen) {
            ArchivAsset asset = getDetailsAsset();
            if (asset != null) {
                AssetDetailsModalLayout modal = buildAssetDetailsModalLayout(asset);

                if (isInside(mouseX, mouseY, modal.closeX, modal.closeY, modal.closeSize, modal.closeSize)) {
                    closeAssetDetails();
                    return true;
                }

                if (isInside(mouseX, mouseY, modal.closeButtonX, modal.closeButtonY, modal.closeButtonW, modal.closeButtonH)) {
                    closeAssetDetails();
                    return true;
                }

                if (isInside(mouseX, mouseY, modal.loadButtonX, modal.loadButtonY, modal.loadButtonW, modal.loadButtonH)) {
                    loadAsset(asset);
                    return true;
                }
            }

            return true;
        }

        if (deleteConfirmOpen) {
            int panelW = 340;
            int panelH = 150;
            int panelX = (this.width - panelW) / 2;
            int panelY = (this.height - panelH) / 2;

            int buttonY = panelY + panelH - 36;
            int cancelW = 90;
            int deleteW = 96;
            int gap = 10;

            int deleteX = panelX + panelW - 14 - deleteW;
            int cancelX = deleteX - gap - cancelW;

            if (isInside(mouseX, mouseY, cancelX, buttonY, cancelW, 24)) {
                closeDeleteConfirm();
                return true;
            }

            if (isInside(mouseX, mouseY, deleteX, buttonY, deleteW, 24)) {
                ArchivAsset asset = getDeleteConfirmAsset();
                if (asset != null) {
                    deleteAsset(asset);
                }
                closeDeleteConfirm();
                return true;
            }

            return true;
        }

        if (browseSearchBox != null && !isInside(mouseX, mouseY, browseSearchBox.getX(), browseSearchBox.getY(), browseSearchBox.getWidth(), browseSearchBox.getHeight())) {
            browseSearchBox.setFocused(false);
            this.setFocused(null);
        }

        boolean insideBrowseTab = isInside(mouseX, mouseY, chrome.tabX, chrome.tabY, chrome.tabW, chrome.tabH);
        boolean insideMyAssetsTab = isInside(mouseX, mouseY, chrome.myAssetsX, chrome.tabY, chrome.myAssetsW, chrome.tabH);
        boolean insideImportTab = isInside(mouseX, mouseY, chrome.importX, chrome.tabY, chrome.tabW, chrome.tabH);
        boolean insideSettingsTab = isInside(mouseX, mouseY, chrome.settingsX, chrome.tabY, chrome.tabW, chrome.tabH);

        if (insideBrowseTab) {
            selectedTopTab = "Browse";
            resetBrowseScroll();
            return true;
        }

        if (insideMyAssetsTab) {
            selectedTopTab = "My Assets";
            resetMyAssetsScroll();
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
            for (int i = 0; i < 4; i++) {
                int currentY = chrome.bodyY + 34 + (i * 40);

                if (isInside(mouseX, mouseY, chrome.rootX + 12, currentY, 156, 34)) {
                    selectedImportStep = i + 1;
                    return true;
                }
            }

            ImportLayout layout = buildImportLayout(chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH);

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
                    resetMyAssetsScroll();
                    return true;
                }
            }
        }

        if ("Browse".equals(selectedTopTab)) {
            for (int i = 0; i < categories.length; i++) {
                int currentY = chrome.sidebarItemY + (i * chrome.sidebarItemGap);

                if (isInside(mouseX, mouseY, chrome.sidebarItemX, currentY, chrome.sidebarItemW, chrome.sidebarItemH)) {
                    selectedCategory = categories[i];
                    resetBrowseScroll();
                    syncLibrarySelectionWithVisibleAssets();
                    return true;
                }
            }

            BrowseToolbarLayout toolbar = buildBrowseToolbarLayout(chrome.contentX, chrome.contentY, chrome.contentW);
            ViewportLayout viewport = buildBrowseViewportLayout(chrome);
            ScrollbarLayout browseScrollbar = buildBrowseScrollbarLayout(chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH);

            if (isInside(mouseX, mouseY, toolbar.searchX, toolbar.toolbarY, toolbar.searchW, 34)) {
                if (browseSearchBox != null) {
                    browseSearchBox.setFocused(true);
                    this.setFocused(browseSearchBox);
                }
                return true;
            }

            if (isInside(mouseX, mouseY, toolbar.gridX, toolbar.toolbarY, toolbar.gridW, 34) && !browseViewMode.equals("Grid")) {
                browseViewMode = "Grid";
                resetBrowseScroll();
                syncLibrarySelectionWithVisibleAssets();
                return true;
            }

            if (isInside(mouseX, mouseY, toolbar.listX, toolbar.toolbarY, toolbar.listW, 34) && !browseViewMode.equals("List")) {
                browseViewMode = "List";
                resetBrowseScroll();
                syncLibrarySelectionWithVisibleAssets();
                return true;
            }

            if (isInside(mouseX, mouseY, toolbar.filterX, toolbar.toolbarY, toolbar.filterW, 34)) {
                browseFavoritesOnly = !browseFavoritesOnly;
                resetBrowseScroll();
                syncLibrarySelectionWithVisibleAssets();
                return true;
            }

            if (isInside(mouseX, mouseY, toolbar.sortBoxX, toolbar.toolbarY, toolbar.sortBoxW, 34)) {
                cycleBrowseSortMode();
                resetBrowseScroll();
                syncLibrarySelectionWithVisibleAssets();
                return true;
            }

            if (browseMaxScroll > 0 && isInside(mouseX, mouseY, browseScrollbar.thumbX, browseScrollbar.thumbY, browseScrollbar.thumbW, browseScrollbar.thumbH)) {
                browseScrollbarDragging = true;
                browseScrollbarDragOffset = (int) mouseY - browseScrollbar.thumbY;
                return true;
            }

            if (browseMaxScroll > 0 && isInside(mouseX, mouseY, browseScrollbar.trackX, browseScrollbar.trackY, browseScrollbar.trackW, browseScrollbar.trackH)) {
                int desiredThumbY = (int) mouseY - (browseScrollbar.thumbH / 2);
                int clampedThumbY = clampInt(
                        desiredThumbY,
                        browseScrollbar.trackY,
                        browseScrollbar.trackY + browseScrollbar.trackH - browseScrollbar.thumbH
                );

                int movableTrack = browseScrollbar.trackH - browseScrollbar.thumbH;
                if (movableTrack > 0) {
                    browseScrollOffset = ((clampedThumbY - browseScrollbar.trackY) * browseMaxScroll) / movableTrack;
                } else {
                    browseScrollOffset = 0;
                }

                return true;
            }

            double browseMouseY = mouseY;

            List<ArchivAsset> visibleAssets = getVisibleAssets();

            if (browseViewMode.equals("List")) {
                BrowseListLayout layout = buildBrowseListLayout(chrome.contentX, chrome.contentY, chrome.contentW, browseScrollOffset);

                for (int i = 0; i < visibleAssets.size(); i++) {
                    ArchivAsset asset = visibleAssets.get(i);
                    int rowY = layout.listY + i * (layout.rowH + layout.rowGap);
                    BrowseListRowLayout rowLayout = buildBrowseListRowLayout(layout.listX, rowY, layout.listW, layout.rowH);
                    boolean savedAsset = isSavedAsset(asset);

                    if (savedAsset && isListMenuOpenFor(asset)
                            && isInside(mouseX, browseMouseY, rowLayout.menuX, rowLayout.menuY, rowLayout.menuW, rowLayout.menuH)) {
                        openDeleteConfirm(asset);
                        return true;
                    }

                    if (savedAsset && isInside(mouseX, browseMouseY, rowLayout.menuDotsX - 4, rowLayout.menuDotsY - 4, 14, 18)) {
                        if (isListMenuOpenFor(asset)) {
                            closeListMenu();
                        } else {
                            openListMenu(asset);
                        }
                        return true;
                    }

                    if (isInside(mouseX, browseMouseY, rowLayout.favoriteX, rowLayout.favoriteY, rowLayout.favoriteBoxSize, rowLayout.favoriteBoxSize)) {
                        closeListMenu();
                        toggleAssetFavorite(asset);
                        syncLibrarySelectionWithVisibleAssets();
                        return true;
                    }

                    boolean selected = isLibraryAssetSelected(asset);
                    if (selected) {
                        if (isInside(mouseX, browseMouseY, rowLayout.loadX, rowLayout.loadY, rowLayout.loadW, rowLayout.loadH)) {
                            loadAsset(asset);
                            return true;
                        }

                        if (isInside(mouseX, browseMouseY, rowLayout.detailsX, rowLayout.detailsY, rowLayout.detailsW, rowLayout.detailsH)) {
                            openAssetDetails(asset);
                            return true;
                        }
                    }

                    if (isInside(mouseX, browseMouseY, layout.listX, rowY, layout.listW, layout.rowH)) {
                        closeListMenu();
                        selectLibraryAsset(asset);
                        return true;
                    }
                }

                if (listMenuOpen) {
                    closeListMenu();
                }
            } else {
                CardGridLayout layout = buildBrowseGridLayout(chrome.contentX, chrome.contentY, chrome.contentW, visibleAssets.size(), browseScrollOffset);

                for (int i = 0; i < visibleAssets.size(); i++) {
                    ArchivAsset asset = visibleAssets.get(i);

                    int column = i % layout.columns;
                    int row = i / layout.columns;
                    int cardX = layout.cardsAreaX + column * (layout.cardW + layout.cardsGap);
                    int cardY = layout.cardsAreaY + row * (layout.cardH + layout.rowGap);

                    AssetCardLayout cardLayout = buildAssetCardLayout(cardX, cardY, layout.cardW, layout.cardH);
                    boolean savedAsset = isSavedAsset(asset);

                    if (savedAsset && isListMenuOpenFor(asset)
                            && isInside(mouseX, browseMouseY, cardLayout.menuX, cardLayout.menuY, cardLayout.menuW, cardLayout.menuH)) {
                        openDeleteConfirm(asset);
                        return true;
                    }

                    if (savedAsset && isInside(mouseX, browseMouseY, cardLayout.menuDotsX - 4, cardLayout.menuDotsY - 4, 14, 18)) {
                        if (isListMenuOpenFor(asset)) {
                            closeListMenu();
                        } else {
                            openListMenu(asset);
                        }
                        return true;
                    }

                    if (isInside(mouseX, browseMouseY, cardLayout.favoriteX, cardLayout.favoriteY, cardLayout.favoriteW, cardLayout.favoriteH)) {
                        closeListMenu();
                        toggleAssetFavorite(asset);
                        syncLibrarySelectionWithVisibleAssets();
                        return true;
                    }

                    boolean selected = isLibraryAssetSelected(asset);
                    if (selected) {
                        if (isInside(mouseX, browseMouseY, cardLayout.overlayX, cardLayout.loadY, cardLayout.overlayW, cardLayout.loadH)) {
                            loadAsset(asset);
                            return true;
                        }

                        if (isInside(mouseX, browseMouseY, cardLayout.overlayX, cardLayout.detailsY, cardLayout.overlayW, cardLayout.detailsH)) {
                            openAssetDetails(asset);
                            return true;
                        }
                    }

                    if (isInside(mouseX, browseMouseY, cardX, cardY, layout.cardW, layout.cardH)) {
                        closeListMenu();
                        selectLibraryAsset(asset);
                        return true;
                    }
                }

                if (listMenuOpen) {
                    closeListMenu();
                }
            }
        }

        if ("My Assets".equals(selectedTopTab)) {
            for (int i = 0; i < myAssetsSections.length; i++) {
                int currentY = chrome.sidebarItemY + (i * chrome.sidebarItemGap);

                if (isInside(mouseX, mouseY, chrome.sidebarItemX, currentY, chrome.sidebarItemW, chrome.sidebarItemH)) {
                    selectedMyAssetsSection = myAssetsSections[i];
                    resetMyAssetsScroll();
                    closeListMenu();
                    return true;
                }
            }

            ScrollbarLayout scrollbar = buildMyAssetsScrollbarLayout(chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH);

            if (myAssetsMaxScroll > 0 && isInside(mouseX, mouseY, scrollbar.thumbX, scrollbar.thumbY, scrollbar.thumbW, scrollbar.thumbH)) {
                myAssetsScrollbarDragging = true;
                myAssetsScrollbarDragOffset = (int) mouseY - scrollbar.thumbY;
                return true;
            }

            if (myAssetsMaxScroll > 0 && isInside(mouseX, mouseY, scrollbar.trackX, scrollbar.trackY, scrollbar.trackW, scrollbar.trackH)) {
                int desiredThumbY = (int) mouseY - (scrollbar.thumbH / 2);
                int clampedThumbY = clampInt(desiredThumbY, scrollbar.trackY, scrollbar.trackY + scrollbar.trackH - scrollbar.thumbH);

                int movableTrack = scrollbar.trackH - scrollbar.thumbH;
                if (movableTrack > 0) {
                    myAssetsScrollOffset = ((clampedThumbY - scrollbar.trackY) * myAssetsMaxScroll) / movableTrack;
                } else {
                    myAssetsScrollOffset = 0;
                }

                return true;
            }

            double myAssetsMouseY = mouseY;
            if (isInside(mouseX, mouseY, chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH)) {
                myAssetsMouseY += myAssetsScrollOffset;
            }

            int innerPadding = 18;
            int titleX = chrome.contentX + innerPadding;
            int titleBaseY = chrome.contentY + 16;
            int statsBaseY = titleBaseY + 40;
            int statH = 68;
            int quickBaseY = statsBaseY + statH + 18;
            int quickButtonBaseY = quickBaseY + 14;
            int quickButtonH = 30;
            int quickGap = 12;
            int quickW = (chrome.contentW - (innerPadding * 2) - (quickGap * 2)) / 3;

            if (isInside(mouseX, myAssetsMouseY, titleX, quickButtonBaseY, quickW, quickButtonH)) {
                beginFreshImportSession();
                return true;
            }

            if (isInside(mouseX, myAssetsMouseY, titleX + quickW + quickGap, quickButtonBaseY, quickW, quickButtonH)) {
                selectedMyAssetsSection = "Collections";
                libraryActionMessage = "Create Collection not implemented yet";
                resetMyAssetsScroll();
                return true;
            }

            if (isInside(mouseX, myAssetsMouseY, titleX + (quickW + quickGap) * 2, quickButtonBaseY, quickW, quickButtonH)) {
                selectedMyAssetsSection = "Local Packs";
                libraryActionMessage = "Local Packs not implemented yet";
                resetMyAssetsScroll();
                return true;
            }
        }

        if ("My Assets".equals(selectedTopTab) && myAssetsShowsImportedGrid()) {
            double myAssetsMouseY = mouseY;
            if (isInside(mouseX, mouseY, chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH)) {
                myAssetsMouseY += myAssetsScrollOffset;
            }

            List<ArchivAsset> visibleAssets = getVisibleAssets();
            int innerPadding = 18;
            int titleBaseY = chrome.contentY + 16;
            int statsBaseY = titleBaseY + 40;
            int statH = 68;
            int quickBaseY = statsBaseY + statH + 18;
            int quickButtonBaseY = quickBaseY + 14;
            int quickButtonH = 30;
            int sectionBaseY = quickButtonBaseY + quickButtonH + 20;
            List<ArchivAsset> recentAssets = getRecentAssets(4);
            int importedTitleBaseY = sectionBaseY;

            if ("All Assets".equals(selectedMyAssetsSection)) {
                int collectionBaseY = sectionBaseY + 18;
                int collectionH = 88;
                importedTitleBaseY = collectionBaseY + collectionH + 20;

                if (!recentAssets.isEmpty()) {
                    int recentTitleBaseY = importedTitleBaseY;
                    int recentCardBaseY = recentTitleBaseY + 18;
                    int recentH = 62;
                    importedTitleBaseY = recentCardBaseY + recentH + 20;
                }
            }

            CardGridLayout layout = buildMyAssetsImportedGridLayout(chrome.contentX, chrome.contentW, importedTitleBaseY, visibleAssets.size());

            for (int i = 0; i < visibleAssets.size(); i++) {
                ArchivAsset asset = visibleAssets.get(i);
                int column = i % layout.columns;
                int row = i / layout.columns;
                int cardX = layout.cardsAreaX + column * (layout.cardW + layout.cardsGap);
                int cardY = layout.cardsAreaY + row * (layout.cardH + layout.rowGap);
                AssetCardLayout cardLayout = buildAssetCardLayout(cardX, cardY, layout.cardW, layout.cardH);

                boolean savedAsset = isSavedAsset(asset);

                if (savedAsset && isListMenuOpenFor(asset)
                        && isInside(mouseX, myAssetsMouseY, cardLayout.menuX, cardLayout.menuY, cardLayout.menuW, cardLayout.menuH)) {
                    openDeleteConfirm(asset);
                    return true;
                }

                if (savedAsset && isInside(mouseX, myAssetsMouseY, cardLayout.menuDotsX - 4, cardLayout.menuDotsY - 4, 14, 18)) {
                    if (isListMenuOpenFor(asset)) {
                        closeListMenu();
                    } else {
                        openListMenu(asset);
                    }
                    return true;
                }

                if (isInside(mouseX, myAssetsMouseY, cardLayout.favoriteX, cardLayout.favoriteY, cardLayout.favoriteW, cardLayout.favoriteH)) {
                    closeListMenu();
                    toggleAssetFavorite(asset);
                    syncLibrarySelectionWithVisibleAssets();
                    return true;
                }

                boolean selected = isLibraryAssetSelected(asset);
                if (selected) {
                    if (isInside(mouseX, myAssetsMouseY, cardLayout.overlayX, cardLayout.loadY, cardLayout.overlayW, cardLayout.loadH)) {
                        loadAsset(asset);
                        return true;
                    }

                    if (isInside(mouseX, myAssetsMouseY, cardLayout.overlayX, cardLayout.detailsY, cardLayout.overlayW, cardLayout.detailsH)) {
                        openAssetDetails(asset);
                        return true;
                    }
                }

                if (isInside(mouseX, myAssetsMouseY, cardX, cardY, layout.cardW, layout.cardH)) {
                    closeListMenu();
                    selectLibraryAsset(asset);
                    return true;
                }
            }

            if (listMenuOpen) {
                closeListMenu();
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        ScreenChromeLayout chrome = buildChromeLayout();

        if ("Browse".equals(selectedTopTab)) {
            ViewportLayout viewport = buildBrowseViewportLayout(chrome);

            if (!isInside(mouseX, mouseY, viewport.x, viewport.y, viewport.w, viewport.h)) {
                return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            }

            if (browseMaxScroll <= 0) {
                return true;
            }

            int scrollStep = 18;
            browseScrollOffset = clampInt(
                    browseScrollOffset - ((int) verticalAmount * scrollStep),
                    0,
                    browseMaxScroll
            );

            return true;
        }

        if ("My Assets".equals(selectedTopTab)) {
            if (!isInside(mouseX, mouseY, chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH)) {
                return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            }

            if (myAssetsMaxScroll <= 0) {
                return true;
            }

            int scrollStep = 18;
            myAssetsScrollOffset = clampInt(
                    myAssetsScrollOffset - ((int) verticalAmount * scrollStep),
                    0,
                    myAssetsMaxScroll
            );

            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.fill(0, 0, this.width, this.height, COLOR_BACKGROUND);

        ScreenChromeLayout chrome = buildChromeLayout();

        boolean browseActive = "Browse".equals(selectedTopTab);
        boolean myAssetsActive = "My Assets".equals(selectedTopTab);
        boolean libraryTabActive = browseActive || myAssetsActive;

        if (browseSearchBox != null) {
            browseSearchBox.visible = browseActive;
            browseSearchBox.active = browseActive;

            if (!browseActive) {
                browseSearchBox.setFocused(false);
            }
        }

        List<ArchivAsset> visibleAssets = getVisibleAssets();

        drawPanel(guiGraphics, chrome.rootX, chrome.rootY, chrome.rootW, chrome.rootH, COLOR_ROOT, COLOR_BORDER);
        drawPanel(guiGraphics, chrome.rootX, chrome.rootY, chrome.rootW, chrome.headerH, COLOR_PANEL, COLOR_BORDER);
        drawPanel(guiGraphics, chrome.rootX, chrome.bodyY, chrome.sidebarW, chrome.bodyH, COLOR_PANEL, COLOR_BORDER);
        drawPanel(guiGraphics, chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH, COLOR_ROOT, COLOR_BORDER);

        guiGraphics.drawString(this.font, "ARCHIV", chrome.rootX + 18, chrome.rootY + 20, COLOR_TEXT);

        drawTopTab(guiGraphics, "Browse", chrome.tabX, chrome.tabY, chrome.tabW, chrome.tabH, browseActive);
        drawTopTab(guiGraphics, "My Assets", chrome.myAssetsX, chrome.tabY, chrome.myAssetsW, chrome.tabH, myAssetsActive);
        drawTopTab(guiGraphics, "Import", chrome.importX, chrome.tabY, chrome.tabW, chrome.tabH, "Import".equals(selectedTopTab));
        drawTopTab(guiGraphics, "Settings", chrome.settingsX, chrome.tabY, chrome.tabW, chrome.tabH, "Settings".equals(selectedTopTab));

        if (browseActive) {
            guiGraphics.drawString(this.font, "CATEGORIES", chrome.rootX + 16, chrome.bodyY + 14, COLOR_TEXT_DIM);

            int categoryY = chrome.sidebarItemY;
            for (int i = 0; i < categories.length; i++) {
                boolean active = categories[i].equals(selectedCategory);
                drawSidebarItem(guiGraphics, categories[i], chrome.sidebarItemX, categoryY, chrome.sidebarItemW, chrome.sidebarItemH, active);
                categoryY += chrome.sidebarItemGap;
            }
        } else if (myAssetsActive) {
            guiGraphics.drawString(this.font, "MY LIBRARY", chrome.rootX + 16, chrome.bodyY + 14, COLOR_TEXT_DIM);

            int sectionY = chrome.sidebarItemY;
            for (int i = 0; i < myAssetsSections.length; i++) {
                boolean active = myAssetsSections[i].equals(selectedMyAssetsSection);
                drawSidebarItem(guiGraphics, myAssetsSections[i], chrome.sidebarItemX, sectionY, chrome.sidebarItemW, chrome.sidebarItemH, active);
                sectionY += chrome.sidebarItemGap;
            }
        } else if (!"Import".equals(selectedTopTab)) {
            guiGraphics.drawString(this.font, "SECTION", chrome.rootX + 16, chrome.bodyY + 14, COLOR_TEXT_DIM);
            guiGraphics.drawString(this.font, selectedTopTab, chrome.rootX + 16, chrome.bodyY + 34, COLOR_TEXT);
        }

        if (browseActive) {
            drawBrowseTab(guiGraphics, chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH, visibleAssets);
        } else if (myAssetsActive) {
            drawMyAssetsTab(guiGraphics, chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH, visibleAssets);
        } else if ("Import".equals(selectedTopTab)) {
            drawImportTab(guiGraphics, chrome.rootX, chrome.rootY, chrome.rootW, chrome.rootH, chrome.bodyY, chrome.bodyH, chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH);
        } else {
            drawTabPlaceholder(guiGraphics, chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH, selectedTopTab);
        }

        drawAssetDetailsPanel(guiGraphics, chrome.rootX, chrome.rootY, chrome.rootW, chrome.bodyY);
        drawDeleteConfirmModal(guiGraphics);

        drawPanel(guiGraphics, chrome.rootX, chrome.rootY + chrome.rootH - chrome.footerH, chrome.rootW, chrome.footerH, COLOR_PANEL, COLOR_BORDER);

        int footerY = chrome.rootY + chrome.rootH - chrome.footerH + 8;
        guiGraphics.drawString(this.font, "WorldEdit: pending", chrome.rootX + 12, footerY, COLOR_SUCCESS);

        String footerMiddleText = libraryTabActive ? libraryActionMessage : "Preview pipeline: planned";
        guiGraphics.drawString(this.font, footerMiddleText, chrome.rootX + 140, footerY, COLOR_TEXT_DIM);

        if (libraryTabActive) {
            int totalAssets = browseActive
                    ? mockAssets.size() + savedAssets.size()
                    : savedAssets.size();

            String assetCountText = visibleAssets.size() + " / " + totalAssets + " assets";
            guiGraphics.drawString(this.font, assetCountText, chrome.rootX + chrome.rootW - 120, footerY, COLOR_TEXT_DIM);
        } else {
            guiGraphics.drawString(this.font, selectedTopTab, chrome.rootX + chrome.rootW - 80, footerY, COLOR_TEXT_DIM);
        }

        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    private void drawAssetDetailsPanel(GuiGraphics guiGraphics, int rootX, int rootY, int rootW, int bodyY) {
        if (!assetDetailsOpen) {
            return;
        }

        ArchivAsset asset = getDetailsAsset();
        if (asset == null) {
            return;
        }

        boolean loaded = isAssetLoaded(asset);
        AssetDetailsModalLayout modal = buildAssetDetailsModalLayout(asset);

        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);
        drawPanel(guiGraphics, modal.panelX, modal.panelY, modal.panelW, modal.panelH, COLOR_PANEL_ALT, COLOR_BORDER_ACTIVE);

        guiGraphics.drawString(this.font, "Asset Details", modal.panelX + 16, modal.panelY + 16, COLOR_TEXT);
        drawChip(guiGraphics, asset.getType(), modal.chipX, modal.chipY, getChipWidth(asset.getType()), 18, asset.getChipColor());

        drawPanel(guiGraphics, modal.closeX, modal.closeY, modal.closeSize, modal.closeSize, COLOR_PANEL, COLOR_BORDER);
        guiGraphics.drawString(this.font, "X", modal.closeX + 6, modal.closeY + 6, COLOR_TEXT);

        drawPanel(guiGraphics, modal.previewX - 1, modal.previewY - 1, modal.previewW + 2, modal.previewH + 2, COLOR_PANEL, COLOR_BORDER);
        guiGraphics.fill(modal.previewX, modal.previewY, modal.previewX + modal.previewW, modal.previewY + modal.previewH, asset.getPreviewColor());

        String previewText = "PREVIEW";
        int previewTextWidth = this.font.width(previewText);
        guiGraphics.drawString(
                this.font,
                previewText,
                modal.previewX + (modal.previewW - previewTextWidth) / 2,
                modal.previewY + (modal.previewH / 2) - 4,
                COLOR_TEXT
        );

        int infoX = modal.panelX + 18;
        int infoY = modal.previewY + modal.previewH + 14;

        guiGraphics.drawString(this.font, asset.getName(), infoX, infoY, COLOR_TEXT);

        String detailsMeta = ".schem  •  " + asset.getVersion();
        guiGraphics.drawString(this.font, detailsMeta, infoX, infoY + 16, COLOR_TEXT_DIM);

        int metaCursorX = infoX + this.font.width(detailsMeta) + 10;
        if (loaded) {
            guiGraphics.drawString(this.font, "• Loaded", metaCursorX, infoY + 16, COLOR_SUCCESS);
            metaCursorX += this.font.width("• Loaded") + 10;
        }
        if (asset.isFavorite()) {
            guiGraphics.drawString(this.font, "★ Favorite", metaCursorX, infoY + 16, 0xFFFFD54A);
        }

        int infoGridY = infoY + 44;
        int labelColor = COLOR_TEXT_DIM;
        int valueColor = COLOR_TEXT;

        guiGraphics.drawString(this.font, "Category", infoX, infoGridY, labelColor);
        guiGraphics.drawString(this.font, asset.getMacroCategory(), infoX + 84, infoGridY, valueColor);

        guiGraphics.drawString(this.font, "Type", infoX + 190, infoGridY, labelColor);
        guiGraphics.drawString(this.font, asset.getType(), infoX + 244, infoGridY, valueColor);

        guiGraphics.drawString(this.font, "Variants", infoX, infoGridY + 18, labelColor);
        guiGraphics.drawString(this.font, String.valueOf(asset.getVariantCount()), infoX + 84, infoGridY + 18, valueColor);

        guiGraphics.drawString(this.font, "Status", infoX + 190, infoGridY + 18, labelColor);
        guiGraphics.drawString(this.font, loaded ? "Loaded" : "Idle", infoX + 244, infoGridY + 18, loaded ? COLOR_SUCCESS : valueColor);

        guiGraphics.fill(modal.panelX + 16, modal.closeButtonY - 12, modal.panelX + modal.panelW - 16, modal.closeButtonY - 11, COLOR_BORDER);

        drawButtonBox(guiGraphics, "Close", modal.closeButtonX, modal.closeButtonY, modal.closeButtonW, modal.closeButtonH, false);
        drawButtonBox(guiGraphics, loaded ? "Loaded" : "Load", modal.loadButtonX, modal.loadButtonY, modal.loadButtonW, modal.loadButtonH, true);
    }

    private void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height, int backgroundColor, int borderColor) {
        guiGraphics.fill(x, y, x + width, y + height, backgroundColor);
        guiGraphics.fill(x, y, x + width, y + 1, borderColor);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, borderColor);
        guiGraphics.fill(x, y, x + 1, y + height, borderColor);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, borderColor);
    }

    private void drawScrollbar(GuiGraphics guiGraphics, ScrollbarLayout layout, boolean active) {
        int trackColor = 0xFF0C1624;
        int trackBorder = COLOR_BORDER;
        int thumbColor = active ? COLOR_BORDER_ACTIVE : 0xFF46627E;
        int thumbBorder = active ? 0xFF73C8FF : 0xFF6D88A3;

        drawPanel(guiGraphics, layout.trackX, layout.trackY, layout.trackW, layout.trackH, trackColor, trackBorder);
        drawPanel(guiGraphics, layout.thumbX, layout.thumbY, layout.thumbW, layout.thumbH, thumbColor, thumbBorder);
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

    private void drawModeBox(GuiGraphics guiGraphics, String label, int x, int y, int width, int height, boolean active) {
        int background = active ? COLOR_PANEL_ALT : COLOR_PANEL;
        int border = active ? COLOR_BORDER_ACTIVE : COLOR_BORDER;
        int textColor = active ? COLOR_TEXT : COLOR_TEXT_DIM;

        drawPanel(guiGraphics, x, y, width, height, background, border);

        int textWidth = this.font.width(label);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - this.font.lineHeight) / 2;

        guiGraphics.drawString(this.font, label, textX, textY, textColor);
    }

    private void drawInfoStrip(GuiGraphics guiGraphics, String label, int x, int y, int width, int height) {
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

    private void drawCollectionCard(GuiGraphics guiGraphics, int x, int y, int width, int height, CollectionEntry entry) {
        drawPanel(guiGraphics, x, y, width, height, COLOR_PANEL, COLOR_BORDER);

        int bannerH = 46;
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + bannerH, entry.previewColor);

        guiGraphics.drawString(this.font, "COLLECTION", x + 12, y + 16, 0xFFFFFFFF);
        guiGraphics.drawString(this.font, entry.name, x + 12, y + bannerH + 12, COLOR_TEXT);
        guiGraphics.drawString(this.font, entry.assetCount + " assets", x + 12, y + bannerH + 26, COLOR_TEXT_DIM);

        int chipW = getChipWidth("Collection");
        drawChip(guiGraphics, "Collection", x + width - chipW - 12, y + height - 24, chipW, 18, entry.accentColor);
    }

    private void drawRecentMiniCard(GuiGraphics guiGraphics, int x, int y, int width, int height, ArchivAsset asset) {
        drawPanel(guiGraphics, x, y, width, height, COLOR_PANEL, COLOR_BORDER);

        int bannerH = 34;
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + bannerH, asset.getPreviewColor());

        String previewText = "PREVIEW";
        int previewWidth = this.font.width(previewText);
        guiGraphics.drawString(this.font, previewText, x + (width - previewWidth) / 2, y + 12, COLOR_TEXT);

        guiGraphics.drawString(this.font, fitTextToWidth(asset.getName(), width - 24), x + 10, y + bannerH + 10, COLOR_TEXT);

        int chipW = getChipWidth(asset.getType());
        drawChip(guiGraphics, asset.getType(), x + width - chipW - 10, y + height - 24, chipW, 18, asset.getChipColor());
    }

    private void drawEmptyState(GuiGraphics guiGraphics, int x, int y, int width, int height, String title, String subtitle) {
        int panelWidth = 320;
        int panelHeight = 90;

        int panelX = x + (width / 2) - (panelWidth / 2);
        int panelY = y + (height / 2) - (panelHeight / 2);

        drawPanel(guiGraphics, panelX, panelY, panelWidth, panelHeight, COLOR_PANEL, COLOR_BORDER);

        int titleWidth = this.font.width(title);
        int subtitleWidth = this.font.width(subtitle);

        guiGraphics.drawString(this.font, title, panelX + (panelWidth / 2) - (titleWidth / 2), panelY + 24, COLOR_TEXT);
        guiGraphics.drawString(this.font, subtitle, panelX + (panelWidth / 2) - (subtitleWidth / 2), panelY + 44, COLOR_TEXT_DIM);
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

        String subtitle = "This section is not implemented yet.";
        String hint = "We will build this tab in the next steps.";

        int titleWidth = this.font.width(tabName);
        int subtitleWidth = this.font.width(subtitle);
        int hintWidth = this.font.width(hint);

        guiGraphics.drawString(this.font, tabName, panelX + (panelWidth / 2) - (titleWidth / 2), panelY + 20, COLOR_TEXT);
        guiGraphics.drawString(this.font, subtitle, panelX + (panelWidth / 2) - (subtitleWidth / 2), panelY + 44, COLOR_TEXT_DIM);
        guiGraphics.drawString(this.font, hint, panelX + (panelWidth / 2) - (hintWidth / 2), panelY + 62, COLOR_TEXT_DIM);
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
        BrowseToolbarLayout toolbar = buildBrowseToolbarLayout(contentX, contentY, contentW);
        int innerPadding = 18;

        drawPanel(guiGraphics, toolbar.searchX, toolbar.toolbarY, toolbar.searchW, 34, COLOR_PANEL, COLOR_BORDER);

        String filterLabel = browseFavoritesOnly ? "Fav Only" : "Filter";
        drawControlBox(guiGraphics, filterLabel, toolbar.filterX, toolbar.toolbarY, toolbar.filterW, 34);

        guiGraphics.drawString(this.font, "Sort by:", toolbar.sortLabelX, toolbar.toolbarY + 12, COLOR_TEXT_DIM);
        drawControlBox(guiGraphics, browseSortMode, toolbar.sortBoxX, toolbar.toolbarY, toolbar.sortBoxW, 34);

        guiGraphics.drawString(this.font, "View:", toolbar.viewLabelX, toolbar.toolbarY + 12, COLOR_TEXT_DIM);
        drawModeBox(guiGraphics, "Grid", toolbar.gridX, toolbar.toolbarY, toolbar.gridW, 34, browseViewMode.equals("Grid"));
        drawModeBox(guiGraphics, "List", toolbar.listX, toolbar.toolbarY, toolbar.listW, 34, browseViewMode.equals("List"));

        int summaryY = toolbar.toolbarY + 42;
        int summaryX = contentX + innerPadding;
        int summaryW = contentW - (innerPadding * 2);
        drawInfoStrip(guiGraphics, getBrowseSummaryText(visibleAssets), summaryX, summaryY, summaryW, 22);

        int viewportX = contentX + innerPadding;
        int viewportY = toolbar.toolbarY + 78;
        int viewportW = contentW - (innerPadding * 2) - 16;
        int viewportH = (contentY + contentH - 16) - viewportY;

        if (browseViewMode.equals("List")) {
            BrowseListLayout measureLayout = buildBrowseListLayout(contentX, contentY, contentW, 0);

            int contentHeight = getBrowseListContentHeight(visibleAssets.size(), measureLayout);
            browseMaxScroll = Math.max(0, contentHeight - viewportH);
            browseScrollOffset = clampInt(browseScrollOffset, 0, browseMaxScroll);

            ScrollbarLayout scrollbar = buildBrowseScrollbarLayout(contentX, contentY, contentW, contentH);
            BrowseListLayout layout = buildBrowseListLayout(contentX, contentY, contentW, browseScrollOffset);

            guiGraphics.enableScissor(viewportX, viewportY, viewportX + viewportW, viewportY + viewportH);

            if (visibleAssets.isEmpty()) {
                drawEmptyState(guiGraphics, viewportX, viewportY, viewportW, viewportH);
            } else {
                for (int i = 0; i < visibleAssets.size(); i++) {
                    ArchivAsset asset = visibleAssets.get(i);
                    int rowY = layout.listY + i * (layout.rowH + layout.rowGap);

                    drawBrowseListRow(guiGraphics, layout.listX, rowY, layout.listW, layout.rowH, asset, isLibraryAssetSelected(asset));
                }
            }

            guiGraphics.disableScissor();
            drawScrollbar(guiGraphics, scrollbar, browseScrollbarDragging);
        } else {
            CardGridLayout measureLayout = buildBrowseGridLayout(contentX, contentY, contentW, visibleAssets.size(), 0);

            int contentHeight = measureLayout.cardsAreaH;
            browseMaxScroll = Math.max(0, contentHeight - viewportH);
            browseScrollOffset = clampInt(browseScrollOffset, 0, browseMaxScroll);

            ScrollbarLayout scrollbar = buildBrowseScrollbarLayout(contentX, contentY, contentW, contentH);
            CardGridLayout layout = buildBrowseGridLayout(contentX, contentY, contentW, visibleAssets.size(), browseScrollOffset);

            guiGraphics.enableScissor(viewportX, viewportY, viewportX + viewportW, viewportY + viewportH);

            if (visibleAssets.isEmpty()) {
                drawEmptyState(guiGraphics, viewportX, viewportY, viewportW, viewportH);
            } else {
                for (int i = 0; i < visibleAssets.size(); i++) {
                    ArchivAsset asset = visibleAssets.get(i);
                    int column = i % layout.columns;
                    int row = i / layout.columns;
                    int cardX = layout.cardsAreaX + column * (layout.cardW + layout.cardsGap);
                    int cardY = layout.cardsAreaY + row * (layout.cardH + layout.rowGap);

                    drawAssetCard(guiGraphics, cardX, cardY, layout.cardW, layout.cardH, asset, isLibraryAssetSelected(asset));
                }
            }

            guiGraphics.disableScissor();
            drawScrollbar(guiGraphics, scrollbar, browseScrollbarDragging);
        }
    }

    private void drawMyAssetsTab(GuiGraphics guiGraphics, int contentX, int contentY, int contentW, int contentH, List<ArchivAsset> visibleAssets) {
        int innerPadding = 18;

        int viewportX = contentX + 1;
        int viewportY = contentY + 1;
        int viewportW = contentW - 2;
        int viewportH = contentH - 2;

        int titleX = contentX + innerPadding;

        // ===== posições BASE (sem scroll) =====
        int titleBaseY = contentY + 16;
        int statsBaseY = titleBaseY + 40;

        int statGap = 12;
        int statW = (contentW - (innerPadding * 2) - (statGap * 3)) / 4;
        int statH = 68;

        int quickBaseY = statsBaseY + statH + 18;
        int quickButtonBaseY = quickBaseY + 14;
        int quickButtonH = 30;
        int quickGap = 12;
        int quickW = (contentW - (innerPadding * 2) - (quickGap * 2)) / 3;

        int sectionBaseY = quickButtonBaseY + quickButtonH + 20;

        List<ArchivAsset> recentAssets = getRecentAssets(4);

        boolean collectionsSection = "Collections".equals(selectedMyAssetsSection);
        boolean allAssetsSection = "All Assets".equals(selectedMyAssetsSection);
        boolean showImportedGrid = myAssetsShowsImportedGrid();

        int importedTitleBaseY = sectionBaseY;

        if (allAssetsSection) {
            int collectionBaseY = sectionBaseY + 18;
            int collectionH = 88;

            importedTitleBaseY = collectionBaseY + collectionH + 20;

            if (!recentAssets.isEmpty()) {
                int recentTitleBaseY = importedTitleBaseY;
                int recentCardBaseY = recentTitleBaseY + 18;
                int recentH = 62;

                importedTitleBaseY = recentCardBaseY + recentH + 20;
            }
        }

        int contentBottomBaseY;

        if (collectionsSection) {
            int collectionBaseY = sectionBaseY + 34;
            int collectionH = 94;
            contentBottomBaseY = collectionBaseY + collectionH + 18;
        } else if (!showImportedGrid) {
            contentBottomBaseY = sectionBaseY + 120;
        } else {
            CardGridLayout measureLayout = buildMyAssetsImportedGridLayout(contentX, contentW, importedTitleBaseY, visibleAssets.size());
            contentBottomBaseY = importedTitleBaseY + 18 + measureLayout.cardsAreaH + 18;
        }

        int viewportBottom = contentY + contentH - 12;
        myAssetsMaxScroll = Math.max(0, contentBottomBaseY - viewportBottom);
        myAssetsScrollOffset = clampInt(myAssetsScrollOffset, 0, myAssetsMaxScroll);

        // ===== deslocamento de render =====
        int scrollRenderY = -myAssetsScrollOffset;

        int titleY = titleBaseY + scrollRenderY;
        int statsY = statsBaseY + scrollRenderY;
        int quickY = quickBaseY + scrollRenderY;
        int quickButtonY = quickButtonBaseY + scrollRenderY;
        int sectionY = sectionBaseY + scrollRenderY;
        int importedTitleY = importedTitleBaseY + scrollRenderY;

        guiGraphics.enableScissor(viewportX, viewportY, viewportX + viewportW, viewportY + viewportH);

        guiGraphics.drawString(this.font, "My Assets", titleX, titleY, COLOR_TEXT);
        guiGraphics.drawString(this.font, "Your personal build library and saved collections.", titleX, titleY + 16, COLOR_TEXT_DIM);

        drawStatCard(guiGraphics, titleX, statsY, statW, statH, String.valueOf(getSavedFavoritesCount()), "Favorites", "Starred assets");
        drawStatCard(guiGraphics, titleX + statW + statGap, statsY, statW, statH, String.valueOf(savedAssets.size()), "Imported", "Imported assets");
        drawStatCard(guiGraphics, titleX + (statW + statGap) * 2, statsY, statW, statH, String.valueOf(getRecentAssetsCount()), "Recent", "Used recently");
        drawStatCard(guiGraphics, titleX + (statW + statGap) * 3, statsY, statW, statH, String.valueOf(collectionEntries.length), "Collections", "Saved collections");

        guiGraphics.drawString(this.font, "Quick Actions", titleX, quickY, COLOR_TEXT);

        drawButtonBox(guiGraphics, "Import Asset", titleX, quickButtonY, quickW, quickButtonH, true);
        drawButtonBox(guiGraphics, "Create Collection", titleX + quickW + quickGap, quickButtonY, quickW, quickButtonH, false);
        drawButtonBox(guiGraphics, "Open Local Folder", titleX + (quickW + quickGap) * 2, quickButtonY, quickW, quickButtonH, false);

        if (collectionsSection) {
            guiGraphics.drawString(this.font, "Collections", titleX, sectionY, COLOR_TEXT);
            guiGraphics.drawString(this.font, "Saved themed asset groups and folders.", titleX, sectionY + 16, COLOR_TEXT_DIM);

            int collectionY = sectionY + 34;
            int collectionGap = 14;
            int collectionW = (contentW - (innerPadding * 2) - (collectionGap * 2)) / 3;
            int collectionH = 94;

            for (int i = 0; i < collectionEntries.length; i++) {
                CollectionEntry entry = collectionEntries[i];
                int cardX = titleX + i * (collectionW + collectionGap);
                drawCollectionCard(guiGraphics, cardX, collectionY, collectionW, collectionH, entry);
            }

            guiGraphics.disableScissor();
            return;
        }

        if (!showImportedGrid) {
            drawEmptyState(
                    guiGraphics,
                    contentX + innerPadding,
                    sectionY,
                    contentW - (innerPadding * 2),
                    160,
                    selectedMyAssetsSection,
                    "This management section is not implemented yet."
            );
            guiGraphics.disableScissor();
            return;
        }

        if (allAssetsSection) {
            guiGraphics.drawString(this.font, "Collections", titleX, sectionY, COLOR_TEXT);

            String viewAllCollections = "View all collections ->";
            guiGraphics.drawString(this.font, viewAllCollections, contentX + contentW - innerPadding - this.font.width(viewAllCollections), sectionY, COLOR_BORDER_ACTIVE);

            int collectionY = sectionY + 18;
            int collectionGap = 14;
            int collectionW = (contentW - (innerPadding * 2) - (collectionGap * 2)) / 3;
            int collectionH = 88;

            for (int i = 0; i < collectionEntries.length; i++) {
                CollectionEntry entry = collectionEntries[i];
                int cardX = titleX + i * (collectionW + collectionGap);
                drawCollectionCard(guiGraphics, cardX, collectionY, collectionW, collectionH, entry);
            }

            if (!recentAssets.isEmpty()) {
                int recentTitleY = collectionY + collectionH + 20;
                guiGraphics.drawString(this.font, "Recently Used", titleX, recentTitleY, COLOR_TEXT);

                String viewAllRecent = "View all recent ->";
                guiGraphics.drawString(this.font, viewAllRecent, contentX + contentW - innerPadding - this.font.width(viewAllRecent), recentTitleY, COLOR_BORDER_ACTIVE);

                int recentCardY = recentTitleY + 18;
                int recentGap = 10;
                int recentW = (contentW - (innerPadding * 2) - (recentGap * 3)) / 4;
                int recentH = 62;

                for (int i = 0; i < recentAssets.size(); i++) {
                    int recentX = titleX + i * (recentW + recentGap);
                    drawRecentMiniCard(guiGraphics, recentX, recentCardY, recentW, recentH, recentAssets.get(i));
                }
            }
        }

        String importedSectionTitle = switch (selectedMyAssetsSection) {
            case "Favorites" -> "Favorite Assets";
            case "Recent" -> "Recent Assets";
            default -> "Imported Assets";
        };

        guiGraphics.drawString(this.font, importedSectionTitle, titleX, importedTitleY, COLOR_TEXT);

        CardGridLayout layout = buildMyAssetsImportedGridLayout(contentX, contentW, importedTitleY, visibleAssets.size());

        if (visibleAssets.isEmpty()) {
            if ("Favorites".equals(selectedMyAssetsSection)) {
                drawEmptyState(
                        guiGraphics,
                        layout.cardsAreaX,
                        layout.cardsAreaY,
                        layout.cardsAreaW,
                        160,
                        "No favorite assets yet",
                        "Star a saved asset to see it here."
                );
            } else if ("Recent".equals(selectedMyAssetsSection)) {
                drawEmptyState(
                        guiGraphics,
                        layout.cardsAreaX,
                        layout.cardsAreaY,
                        layout.cardsAreaW,
                        160,
                        "No recent assets yet",
                        "Load an asset to build your recent history."
                );
            } else {
                drawEmptyState(
                        guiGraphics,
                        layout.cardsAreaX,
                        layout.cardsAreaY,
                        layout.cardsAreaW,
                        160,
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

                drawAssetCard(guiGraphics, cardX, cardY, layout.cardW, layout.cardH, asset, isLibraryAssetSelected(asset));
            }
        }

        guiGraphics.disableScissor();
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
            drawButtonBox(guiGraphics, mockDetailsFilled ? "Clear" : "Auto Fill", detailsActionX, detailsActionY, detailsActionW, detailsActionH, false);
        }

        int formX = innerX + 12;
        int fieldGap = 12;
        int fieldH = compact ? 22 : 28;
        int verticalGap = compact ? 10 : 12;

        int fieldW1 = (leftAreaW - 48) / 3;
        int fieldW2 = fieldW1;
        int fieldW3 = fieldW1;
        int wideFieldW = (leftAreaW - 36) / 2;

        int topPadding = detailsStepActive ? (compact ? 44 : 50) : (compact ? 34 : 38);
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

        int saveX = layout.saveX;
        int cancelX = layout.cancelX;
        int resetX = layout.resetX;

        if (saveStepActive) {
            int actionsGroupX = resetX - 10;
            int actionsGroupY = actionsY - 18;
            int actionsGroupW = (saveX + layout.saveW) - actionsGroupX + 10;
            int actionsGroupH = layout.buttonH + 30;

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

        drawButtonBoxState(guiGraphics, "Reset", resetX, actionsY, layout.resetW, layout.buttonH, false, hasImportState);
        drawButtonBoxState(guiGraphics, "Cancel", cancelX, actionsY, layout.cancelW, layout.buttonH, false, true);
        drawButtonBoxState(guiGraphics, mockAssetSaved ? "Saved" : "Save", saveX, actionsY, layout.saveW, layout.buttonH, true, importReady || mockAssetSaved);

        if (saveStepActive && (importReady || mockAssetSaved)) {
            guiGraphics.fill(saveX - 2, actionsY - 2, saveX + layout.saveW + 2, actionsY, COLOR_BORDER_ACTIVE);
            guiGraphics.fill(saveX - 2, actionsY + layout.buttonH, saveX + layout.saveW + 2, actionsY + layout.buttonH + 2, COLOR_BORDER_ACTIVE);
            guiGraphics.fill(saveX - 2, actionsY - 2, saveX, actionsY + layout.buttonH + 2, COLOR_BORDER_ACTIVE);
            guiGraphics.fill(saveX + layout.saveW, actionsY - 2, saveX + layout.saveW + 2, actionsY + layout.buttonH + 2, COLOR_BORDER_ACTIVE);
        }
    }

    private void drawAssetCard(GuiGraphics guiGraphics, int x, int y, int width, int height, ArchivAsset asset, boolean selected) {
        boolean loaded = isAssetLoaded(asset);

        int border = selected
                ? COLOR_BORDER_ACTIVE
                : (loaded ? COLOR_SUCCESS : COLOR_BORDER);

        drawPanel(guiGraphics, x, y, width, height, COLOR_PANEL, border);

        if (loaded) {
            guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 3, COLOR_SUCCESS);
        }
        AssetCardLayout layout = buildAssetCardLayout(x, y, width, height);

        guiGraphics.fill(layout.previewX, layout.previewY, layout.previewX + layout.previewW, layout.previewY + layout.previewH, asset.getPreviewColor());

        String previewText = "PREVIEW";
        int textWidth = this.font.width(previewText);
        guiGraphics.drawString(this.font, previewText, layout.previewX + (layout.previewW / 2) - (textWidth / 2), layout.previewY + (layout.previewH / 2) - 4, 0xFFFFFFFF);

        guiGraphics.drawString(this.font, asset.isFavorite() ? "★" : "☆", layout.favoriteX + 4, layout.favoriteY + 6, 0xFFFFD45A);

        if (selected) {
            guiGraphics.fill(layout.previewX, layout.previewY, layout.previewX + layout.previewW, layout.previewY + layout.previewH, 0x55000000);

            drawPanel(guiGraphics, layout.overlayX, layout.loadY, layout.overlayW, layout.loadH, 0xFF2F9BE6, 0xFF73C8FF);
            drawPanel(guiGraphics, layout.overlayX, layout.detailsY, layout.overlayW, layout.detailsH, 0xFF1A2638, COLOR_BORDER);

            guiGraphics.drawString(this.font, "Load", x + (width / 2) - 12, layout.loadY + 8, 0xFFFFFFFF);
            guiGraphics.drawString(this.font, "Details", x + (width / 2) - 18, layout.detailsY + 7, 0xFFE5EEF8);
        }

        int infoY = y + layout.previewH + 12;
        int versionY = infoY + 14;
        int dotsY = y + height - 16;

        guiGraphics.drawString(this.font, asset.getName(), x + 12, infoY, COLOR_TEXT);

        String versionText = asset.getVersion();
        guiGraphics.drawString(this.font, versionText, x + 12, versionY, COLOR_TEXT_DIM);

        if (loaded) {
            guiGraphics.drawString(
                    this.font,
                    "• Loaded",
                    x + 12 + this.font.width(versionText) + 8,
                    versionY,
                    COLOR_SUCCESS
            );
        }

        int chipW = getChipWidth(asset.getType());
        int chipX = x + width - 12 - chipW;

        drawChip(guiGraphics, asset.getType(), chipX, infoY - 2, chipW, 18, asset.getChipColor());

        int visibleDots = Math.min(asset.getVariantCount(), 4);

        for (int i = 0; i < visibleDots; i++) {
            drawDot(guiGraphics, x + 12 + (i * 12), dotsY, 0xFF8B6A4A + (i * 0x00111111));
        }

        if (asset.getVariantCount() > 4) {
            int remaining = asset.getVariantCount() - 4;
            guiGraphics.drawString(this.font, "+" + remaining, x + 12 + (visibleDots * 12), dotsY - 1, COLOR_TEXT_DIM);
        }

        if (isSavedAsset(asset)) {
            drawVerticalDots(guiGraphics, layout.menuDotsX, layout.menuDotsY, COLOR_TEXT_DIM);

            if (isListMenuOpenFor(asset)) {
                drawPanel(guiGraphics, layout.menuX, layout.menuY, layout.menuW, layout.menuH, 0xFF2A1820, 0xFFB54B63);
                guiGraphics.drawString(this.font, "Delete Asset", layout.menuX + 10, layout.menuY + 7, 0xFFFFD7DE);
            }
        }
    }

    private void drawBrowseListRow(GuiGraphics guiGraphics, int x, int y, int width, int height, ArchivAsset asset, boolean selected) {
        boolean loaded = isAssetLoaded(asset);

        int borderColor = selected
                ? COLOR_BORDER_ACTIVE
                : (loaded ? COLOR_SUCCESS : COLOR_BORDER);

        drawPanel(guiGraphics, x, y, width, height, COLOR_PANEL, borderColor);

        if (loaded) {
            guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 3, COLOR_SUCCESS);
        }

        BrowseListRowLayout layout = buildBrowseListRowLayout(x, y, width, height);

        // preview
        guiGraphics.fill(
                layout.previewX,
                layout.previewY,
                layout.previewX + layout.previewW,
                layout.previewY + layout.previewH,
                asset.getPreviewColor()
        );

        String previewText = "PREVIEW";
        int previewTextWidth = this.font.width(previewText);
        guiGraphics.drawString(
                this.font,
                previewText,
                layout.previewX + (layout.previewW - previewTextWidth) / 2,
                layout.previewY + (layout.previewH / 2) - 4,
                COLOR_TEXT
        );

        // nome + metadado
        guiGraphics.drawString(this.font, asset.getName(), layout.infoX, layout.titleY, COLOR_TEXT);
        String metaText = ".schem  •  " + asset.getVersion();
        guiGraphics.drawString(this.font, metaText, layout.infoX, layout.versionY, COLOR_TEXT_DIM);

        if (loaded) {
            guiGraphics.drawString(
                    this.font,
                    "• Loaded",
                    layout.infoX + this.font.width(metaText) + 8,
                    layout.versionY,
                    COLOR_SUCCESS
            );
        }

        int variantsToShow = Math.min(asset.getVariantCount(), 4);
        for (int i = 0; i < variantsToShow; i++) {
            drawDot(guiGraphics, layout.infoX + (i * 11), layout.dotsY, 0xFFB79263);
        }

        if (asset.getVariantCount() > 4) {
            int remaining = asset.getVariantCount() - 4;
            guiGraphics.drawString(this.font, "+" + remaining, layout.infoX + (variantsToShow * 11), layout.dotsY - 1, COLOR_TEXT_DIM);
        }

        // badge dinâmica
        int chipW = getChipWidth(asset.getType());
        int chipX = layout.favoriteX - 10 - chipW;

        drawChip(
                guiGraphics,
                asset.getType(),
                chipX,
                layout.chipY,
                chipW,
                layout.chipH,
                asset.getChipColor()
        );

        // estrela mais destacada sem usar scale
        String star = asset.isFavorite() ? "★" : "☆";
        guiGraphics.drawString(this.font, star, layout.favoriteX, layout.favoriteY, 0xFFFFD54A);
        guiGraphics.drawString(this.font, star, layout.favoriteX + 1, layout.favoriteY, 0xFFFFD54A);

        // divisória
        guiGraphics.fill(
                layout.dividerX,
                y + 10,
                layout.dividerX + 1,
                y + height - 10,
                COLOR_BORDER
        );

        // botões à direita, centralizados
        if (selected) {
            drawPanel(guiGraphics, layout.loadX, layout.loadY, layout.loadW, layout.loadH, 0xFF3AA0E6, COLOR_BORDER_ACTIVE);
            drawPanel(guiGraphics, layout.detailsX, layout.detailsY, layout.detailsW, layout.detailsH, 0xFF1A2638, COLOR_BORDER);

            guiGraphics.drawString(
                    this.font,
                    "Load",
                    layout.loadX + (layout.loadW - this.font.width("Load")) / 2,
                    layout.loadY + 7,
                    COLOR_TEXT
            );

            guiGraphics.drawString(
                    this.font,
                    "Details",
                    layout.detailsX + (layout.detailsW - this.font.width("Details")) / 2,
                    layout.detailsY + 7,
                    COLOR_TEXT
            );
        }

        // 3 pontinhos no canto direito, só para assets salvos
        if (isSavedAsset(asset)) {
            drawVerticalDots(guiGraphics, layout.menuDotsX, layout.menuDotsY, COLOR_TEXT_DIM);

            if (isListMenuOpenFor(asset)) {
                drawPanel(guiGraphics, layout.menuX, layout.menuY, layout.menuW, layout.menuH, 0xFF2A1820, 0xFFB54B63);
                guiGraphics.drawString(this.font, "Delete Asset", layout.menuX + 10, layout.menuY + 7, 0xFFFFD7DE);
            }
        }
    }

    private int getChipWidth(String label) {
        return this.font.width(label) + 16;
    }

    private void drawChip(GuiGraphics guiGraphics, String label, int x, int y, int width, int height, int color) {
        drawPanel(guiGraphics, x, y, width, height, color, color);

        int textX = x + 8;
        int textY = y + (height - this.font.lineHeight) / 2;

        guiGraphics.drawString(this.font, label, textX, textY, 0xFFFFFFFF);
    }

    private void drawVerticalDots(GuiGraphics guiGraphics, int x, int y, int color) {
        guiGraphics.fill(x, y, x + 2, y + 2, color);
        guiGraphics.fill(x, y + 5, x + 2, y + 7, color);
        guiGraphics.fill(x, y + 10, x + 2, y + 12, color);
    }

    private void drawDot(GuiGraphics guiGraphics, int x, int y, int color) {
        guiGraphics.fill(x, y, x + 8, y + 8, color);
    }
}