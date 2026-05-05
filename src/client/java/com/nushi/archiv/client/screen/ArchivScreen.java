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

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

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

    private static final List<String> DEFAULT_MACRO_CATEGORIES = List.of(
            "Medieval",
            "Fantasy",
            "Cyberpunk",
            "Sci-fi",
            "Organic",
            "Nature",
            "Modern",
            "Industrial"
    );

    private static final List<String> DEFAULT_ASSET_TYPES = List.of(
            "Structure",
            "Decoration",
            "Tree",
            "Prop",
            "Terrain",
            "Interior",
            "Vehicle",
            "Redstone"
    );

    private static final List<String> DEFAULT_MINECRAFT_VERSIONS = List.of(
            "1.20.1",
            "1.20.4",
            "1.21",
            "1.21.1"
    );

    private final List<String> macroCategories = new ArrayList<>(DEFAULT_MACRO_CATEGORIES);
    private final List<String> assetTypes = new ArrayList<>(DEFAULT_ASSET_TYPES);
    private final List<String> minecraftVersions = new ArrayList<>(DEFAULT_MINECRAFT_VERSIONS);

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

    private final String[] settingsSections = {
            "General",
            "Metadata",
            "Interface",
            "Integrations",
            "Storage",
            "Controls",
            "About"
    };
    private final String[] metadataGroups = {
            "Macro Categories",
            "Types",
            "Minecraft Versions",
            "Variant Presets",
            "Tags / Keywords",
            "Materials"
    };

    private String selectedSettingsSection = "Metadata";
    private String selectedMetadataGroup = "Macro Categories";
    private int selectedMacroCategoryIndex = 0;
    private int selectedAssetTypeIndex = 0;
    private int selectedMinecraftVersionIndex = 0;

    private String browseSortMode = "Newest";
    private boolean browseFavoritesOnly = false;
    private String browseViewMode = "Grid";
    private int selectedImportStep = 1;

    private String importSelectedMacroCategory = "Medieval";
    private String importSelectedType = "Structure";
    private String importSelectedVersion = "1.20.1";
    private int importSelectedVariantCount = 2;

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

    private boolean collectionPickerOpen = false;
    private String pendingCollectionAssetName = null;

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

    private int myAssetsCollectionsScrollX = 0;
    private int myAssetsCollectionsMaxScrollX = 0;

    private int metadataGroupsScrollOffset = 0;
    private int metadataGroupsMaxScroll = 0;
    private boolean metadataGroupsScrollbarDragging = false;
    private int metadataGroupsScrollbarDragOffset = 0;

    private int metadataOptionsScrollOffset = 0;
    private int metadataOptionsMaxScroll = 0;
    private boolean metadataOptionsScrollbarDragging = false;
    private int metadataOptionsScrollbarDragOffset = 0;

    private final List<CollectionEntry> collectionEntries = new ArrayList<>();
    private String selectedCollectionName = null;

    private boolean createCollectionModalOpen = false;
    private EditBox collectionNameBox;
    private EditBox collectionTagBox;
    private EditBox collectionDescriptionBox;

    private boolean editAssetModalOpen = false;
    private String editAssetOriginalName = null;
    private String editAssetSelectedCategory = "Medieval";
    private String editAssetSelectedType = "Structure";
    private String editAssetSelectedVersion = "1.20.1";
    private int editAssetVariantCount = 1;
    private String editAssetDropdownOpen = null;

    private EditBox editAssetNameBox;
    private EditBox editAssetCategoryBox;
    private EditBox editAssetTypeBox;
    private EditBox editAssetVersionBox;
    private EditBox editAssetVariantsBox;

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

    private static class ImportDetailsFormLayout {
        int fieldW1;
        int fieldW2;
        int fieldW3;
        int wideFieldW;
        int fieldH;

        int nameX;
        int nameY;
        int categoryX;
        int categoryY;
        int authorX;
        int authorY;

        int typeX;
        int typeY;
        int versionX;
        int versionY;
        int variantsX;
        int variantsY;

        int tagsX;
        int tagsY;
        int fileX;
        int fileY;
    }

    private static class SettingsLayout {
        int innerX;
        int innerY;
        int innerW;
        int innerH;

        int groupX;
        int groupY;
        int groupW;
        int groupItemH;
        int groupGap;

        int listX;
        int listY;
        int listW;
        int listItemH;
        int listGap;

        int editorX;
        int editorY;
        int editorW;
        int editorH;

        int addX;
        int addY;
        int addW;
        int duplicateX;
        int duplicateY;
        int duplicateW;
        int removeX;
        int removeY;
        int removeW;
        int resetX;
        int resetY;
        int resetW;
        int cancelX;
        int cancelY;
        int cancelW;
        int saveX;
        int saveY;
        int saveW;
        int buttonH;
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
        private String name;
        private String tag;
        private String description;

        final int previewColor;
        final int accentColor;

        private final List<String> assetNames = new ArrayList<>();

        CollectionEntry(String name, String tag, String description, int previewColor, int accentColor) {
            this.name = name;
            this.tag = tag;
            this.description = description;
            this.previewColor = previewColor;
            this.accentColor = accentColor;
        }

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        String getTag() {
            return tag;
        }

        void setTag(String tag) {
            this.tag = tag;
        }

        String getDescription() {
            return description;
        }

        void setDescription(String description) {
            this.description = description;
        }

        int getAssetCount() {
            return assetNames.size();
        }

        List<String> getAssetNames() {
            return assetNames;
        }

        boolean containsAsset(String assetName) {
            return assetNames.contains(assetName);
        }

        void addAsset(String assetName) {
            if (!assetNames.contains(assetName)) {
                assetNames.add(assetName);
            }
        }

        void removeAsset(String assetName) {
            assetNames.remove(assetName);
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

    private static class CreateCollectionModalLayout {
        int panelX;
        int panelY;
        int panelW;
        int panelH;

        int closeX;
        int closeY;
        int closeSize;

        int fieldX;
        int fieldW;
        int fieldH;

        int nameY;
        int tagY;
        int descriptionY;

        int cancelX;
        int cancelY;
        int cancelW;
        int createX;
        int createY;
        int createW;
        int buttonH;
    }

    private static class EditAssetModalLayout {
        int panelX;
        int panelY;
        int panelW;
        int panelH;

        int closeX;
        int closeY;
        int closeSize;

        int headerIconX;
        int headerIconY;
        int headerIconSize;

        int leftX;
        int leftY;
        int leftW;
        int rightX;
        int rightY;
        int rightW;

        int basicX;
        int basicY;
        int basicW;
        int basicH;

        int variantsBoxX;
        int variantsBoxY;
        int variantsBoxW;
        int variantsBoxH;

        int filesX;
        int filesY;
        int filesW;
        int filesH;

        int previewPanelX;
        int previewPanelY;
        int previewPanelW;
        int previewPanelH;

        int previewX;
        int previewY;
        int previewW;
        int previewH;

        int fieldH;
        int fieldGap;

        int nameX;
        int nameY;
        int nameW;
        int categoryX;
        int categoryY;
        int categoryW;
        int typeX;
        int typeY;
        int typeW;
        int versionX;
        int versionY;
        int versionW;
        int authorX;
        int authorY;
        int authorW;
        int tagsX;
        int tagsY;
        int tagsW;

        int variantRowX;
        int variantRowY;
        int variantRowW;
        int variantRowH;
        int variantRowGap;
        int addVariantX;
        int addVariantY;
        int addVariantW;
        int removeVariantX;
        int removeVariantY;
        int removeVariantW;

        int structureFileX;
        int structureFileY;
        int structureFileW;
        int previewFileX;
        int previewFileY;
        int previewFileW;
        int fileFieldH;

        int favoriteToggleX;
        int favoriteToggleY;
        int visibleToggleX;
        int visibleToggleY;

        int deleteX;
        int deleteY;
        int deleteW;

        int cancelX;
        int cancelY;
        int cancelW;
        int saveX;
        int saveY;
        int saveW;
        int buttonH;

        // Legacy aliases kept so old initialization code can still reference them safely.
        int fieldX;
        int fieldW;
        int nameLegacyY;
        int categoryLegacyY;
        int typeLegacyY;
        int versionLegacyY;
        int variantsLegacyY;
    }

    public ArchivScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;
        this.mockAssets = new ArrayList<>();
        normalizeMetadataSelections();
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
        CreateCollectionModalLayout collectionModal = buildCreateCollectionModalLayout();

        collectionNameBox = new EditBox(
                this.font,
                collectionModal.fieldX + 6,
                collectionModal.nameY + 1,
                collectionModal.fieldW - 12,
                collectionModal.fieldH - 2,
                Component.literal("Collection Name")
        );
        collectionNameBox.setBordered(false);
        collectionNameBox.setMaxLength(40);
        collectionNameBox.setTextColor(COLOR_TEXT);
        collectionNameBox.setTextColorUneditable(COLOR_TEXT_DIM);
        collectionNameBox.setHint(Component.literal("Name (required)"));

        collectionTagBox = new EditBox(
                this.font,
                collectionModal.fieldX + 1,
                collectionModal.tagY + 1,
                collectionModal.fieldW - 2,
                collectionModal.fieldH - 2,
                Component.literal("Collection Tag")
        );
        collectionTagBox.setBordered(false);
        collectionTagBox.setMaxLength(24);
        collectionTagBox.setTextColor(COLOR_TEXT);
        collectionTagBox.setTextColorUneditable(COLOR_TEXT_DIM);
        collectionTagBox.setHint(Component.literal("Tag (optional)"));

        collectionDescriptionBox = new EditBox(
                this.font,
                collectionModal.fieldX + 1,
                collectionModal.descriptionY + 1,
                collectionModal.fieldW - 2,
                collectionModal.fieldH - 2,
                Component.literal("Collection Description")
        );
        collectionDescriptionBox.setBordered(false);
        collectionDescriptionBox.setMaxLength(80);
        collectionDescriptionBox.setTextColor(COLOR_TEXT);
        collectionDescriptionBox.setTextColorUneditable(COLOR_TEXT_DIM);
        collectionDescriptionBox.setHint(Component.literal("Description (optional)"));

        this.addRenderableWidget(collectionNameBox);
        this.addRenderableWidget(collectionTagBox);
        this.addRenderableWidget(collectionDescriptionBox);

        EditAssetModalLayout editModal = buildEditAssetModalLayout();

        editAssetNameBox = new EditBox(
                this.font,
                editModal.fieldX + 6,
                editModal.nameY + 1,
                editModal.fieldW - 12,
                editModal.fieldH - 2,
                Component.literal("Asset Name")
        );
        editAssetNameBox.setBordered(false);
        editAssetNameBox.setMaxLength(48);
        editAssetNameBox.setTextColor(COLOR_TEXT);
        editAssetNameBox.setTextColorUneditable(COLOR_TEXT_DIM);
        editAssetNameBox.setHint(Component.literal("Asset name"));

        editAssetCategoryBox = new EditBox(
                this.font,
                editModal.fieldX + 6,
                editModal.categoryY + 1,
                editModal.fieldW - 12,
                editModal.fieldH - 2,
                Component.literal("Asset Category")
        );
        editAssetCategoryBox.setBordered(false);
        editAssetCategoryBox.setMaxLength(32);
        editAssetCategoryBox.setTextColor(COLOR_TEXT);
        editAssetCategoryBox.setTextColorUneditable(COLOR_TEXT_DIM);
        editAssetCategoryBox.setHint(Component.literal("Category"));

        editAssetTypeBox = new EditBox(
                this.font,
                editModal.fieldX + 6,
                editModal.typeY + 1,
                editModal.fieldW - 12,
                editModal.fieldH - 2,
                Component.literal("Asset Type")
        );
        editAssetTypeBox.setBordered(false);
        editAssetTypeBox.setMaxLength(32);
        editAssetTypeBox.setTextColor(COLOR_TEXT);
        editAssetTypeBox.setTextColorUneditable(COLOR_TEXT_DIM);
        editAssetTypeBox.setHint(Component.literal("Type"));

        editAssetVersionBox = new EditBox(
                this.font,
                editModal.fieldX + 6,
                editModal.versionY + 1,
                editModal.fieldW - 12,
                editModal.fieldH - 2,
                Component.literal("Minecraft Version")
        );
        editAssetVersionBox.setBordered(false);
        editAssetVersionBox.setMaxLength(24);
        editAssetVersionBox.setTextColor(COLOR_TEXT);
        editAssetVersionBox.setTextColorUneditable(COLOR_TEXT_DIM);
        editAssetVersionBox.setHint(Component.literal("Minecraft version"));

        editAssetVariantsBox = new EditBox(
                this.font,
                editModal.fieldX + 6,
                editModal.variantRowY + 1,
                editModal.fieldW - 12,
                editModal.fieldH - 2,
                Component.literal("Variant Count")
        );
        editAssetVariantsBox.setBordered(false);
        editAssetVariantsBox.setMaxLength(3);
        editAssetVariantsBox.setTextColor(COLOR_TEXT);
        editAssetVariantsBox.setTextColorUneditable(COLOR_TEXT_DIM);
        editAssetVariantsBox.setHint(Component.literal("Variants"));

        this.addRenderableWidget(editAssetNameBox);
        this.addRenderableWidget(editAssetCategoryBox);
        this.addRenderableWidget(editAssetTypeBox);
        this.addRenderableWidget(editAssetVersionBox);
        this.addRenderableWidget(editAssetVariantsBox);

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

    private boolean isShiftHeld() {
        if (this.minecraft == null) {
            return false;
        }

        return InputConstants.isKeyDown(this.minecraft.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(this.minecraft.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
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

            if ("Collections".equals(selectedMyAssetsSection)) {
                return getSelectedCollectionAssets();
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

    private ImportDetailsFormLayout buildImportDetailsFormLayout(ImportLayout layout) {
        ImportDetailsFormLayout form = new ImportDetailsFormLayout();

        boolean compact = true;
        boolean detailsStepActive = selectedImportStep == 3;

        int innerX = layout.innerX;
        int leftAreaW = layout.leftAreaW;
        int detailsY = layout.detailsY;
        int detailsH = layout.detailsH;

        int formX = innerX + 12;
        int fieldGap = 12;
        int verticalGap = compact ? 10 : 12;

        form.fieldH = compact ? 22 : 28;
        form.fieldW1 = (leftAreaW - 48) / 3;
        form.fieldW2 = form.fieldW1;
        form.fieldW3 = form.fieldW1;
        form.wideFieldW = (leftAreaW - 36) / 2;

        int topPadding = detailsStepActive ? (compact ? 44 : 50) : (compact ? 34 : 38);
        int bottomPadding = compact ? 10 : 14;

        int totalFieldsHeight = (form.fieldH * 3) + (verticalGap * 2);
        int availableFormHeight = detailsH - topPadding - bottomPadding;
        int extraSpace = Math.max(0, availableFormHeight - totalFieldsHeight);

        int formY1 = detailsY + topPadding + (extraSpace / 2);
        int formY2 = formY1 + form.fieldH + verticalGap;
        int formY3 = formY2 + form.fieldH + verticalGap;

        form.nameX = formX;
        form.nameY = formY1;
        form.categoryX = formX + form.fieldW1 + fieldGap;
        form.categoryY = formY1;
        form.authorX = formX + form.fieldW1 + fieldGap + form.fieldW2 + fieldGap;
        form.authorY = formY1;

        form.typeX = formX;
        form.typeY = formY2;
        form.versionX = formX + form.fieldW1 + fieldGap;
        form.versionY = formY2;
        form.variantsX = formX + form.fieldW1 + fieldGap + form.fieldW2 + fieldGap;
        form.variantsY = formY2;

        form.tagsX = formX;
        form.tagsY = formY3;
        form.fileX = formX + form.wideFieldW + fieldGap;
        form.fileY = formY3;

        return form;
    }

    private SettingsLayout buildSettingsLayout(int contentX, int contentY, int contentW, int contentH) {
        SettingsLayout layout = new SettingsLayout();

        int pad = 12;
        int gap = 12;

        layout.innerX = contentX + pad;
        layout.innerY = contentY + pad;
        layout.innerW = contentW - (pad * 2);
        layout.innerH = contentH - (pad * 2);

        int titleBlockH = 42;
        int actionBarH = 26;
        int actionGap = 8;

        int panelTop = layout.innerY + titleBlockH;
        int panelBottom = layout.innerY + layout.innerH - actionBarH - actionGap;
        int panelH = Math.max(180, panelBottom - panelTop);

        layout.groupX = layout.innerX;
        layout.groupY = panelTop;
        layout.groupW = clampInt((layout.innerW * 27) / 100, 170, 235);
        layout.groupItemH = 34;
        layout.groupGap = 6;

        layout.editorW = clampInt((layout.innerW * 36) / 100, 280, 370);

        int minimumListW = 190;
        int requiredWidth = layout.groupW + layout.editorW + (gap * 2) + minimumListW;
        if (requiredWidth > layout.innerW) {
            int excess = requiredWidth - layout.innerW;
            int editorShrink = Math.min(excess, Math.max(0, layout.editorW - 250));
            layout.editorW -= editorShrink;
            excess -= editorShrink;

            int groupShrink = Math.min(excess, Math.max(0, layout.groupW - 160));
            layout.groupW -= groupShrink;
        }

        layout.editorX = layout.innerX + layout.innerW - layout.editorW;
        layout.editorY = panelTop;
        layout.editorH = panelH;

        layout.listX = layout.groupX + layout.groupW + gap;
        layout.listY = panelTop;
        layout.listW = layout.editorX - layout.listX - gap;
        layout.listItemH = 32;
        layout.listGap = 6;

        layout.buttonH = 24;

        int editorButtonGap = 6;
        int editorButtonInnerW = layout.editorW - 24;
        int editorButtonW = Math.max(72, (editorButtonInnerW - (editorButtonGap * 2)) / 3);

        layout.addW = editorButtonW;
        layout.duplicateW = editorButtonW;
        layout.removeW = editorButtonInnerW - layout.addW - layout.duplicateW - (editorButtonGap * 2);

        layout.addX = layout.editorX + 12;
        layout.addY = layout.editorY + layout.editorH - layout.buttonH - 10;
        layout.duplicateX = layout.addX + layout.addW + editorButtonGap;
        layout.duplicateY = layout.addY;
        layout.removeX = layout.duplicateX + layout.duplicateW + editorButtonGap;
        layout.removeY = layout.addY;

        layout.saveW = 150;
        layout.cancelW = 118;
        layout.resetW = 140;

        layout.saveX = layout.innerX + layout.innerW - layout.saveW;
        layout.saveY = layout.innerY + layout.innerH - layout.buttonH - 2;
        layout.cancelX = layout.saveX - 10 - layout.cancelW;
        layout.cancelY = layout.saveY;
        layout.resetX = layout.cancelX - 10 - layout.resetW;
        layout.resetY = layout.saveY;

        return layout;
    }

    private int getSettingsPanelBottom(SettingsLayout layout) {
        return layout.editorY + layout.editorH;
    }

    private int getSettingsPanelButtonY(SettingsLayout layout) {
        return getSettingsPanelBottom(layout) - 34;
    }

    private int getMetadataGroupsViewportX(SettingsLayout layout) {
        return layout.groupX + 10;
    }

    private int getMetadataGroupsViewportY(SettingsLayout layout) {
        return layout.groupY + 32;
    }

    private int getMetadataGroupsViewportW(SettingsLayout layout) {
        return layout.groupW - 20;
    }

    private int getMetadataGroupsViewportH(SettingsLayout layout) {
        return Math.max(24, getSettingsPanelButtonY(layout) - 8 - getMetadataGroupsViewportY(layout));
    }

    private int getMetadataGroupsContentHeight(SettingsLayout layout) {
        if (metadataGroups.length <= 0) {
            return 0;
        }

        return metadataGroups.length * layout.groupItemH + (metadataGroups.length - 1) * layout.groupGap;
    }

    private int getMetadataOptionsViewportX(SettingsLayout layout) {
        return layout.listX + 10;
    }

    private int getMetadataOptionsViewportY(SettingsLayout layout) {
        return layout.listY + 66;
    }

    private int getMetadataOptionsViewportW(SettingsLayout layout) {
        return layout.listW - 20;
    }

    private int getMetadataOptionsViewportH(SettingsLayout layout) {
        return Math.max(24, getSettingsPanelButtonY(layout) - 8 - getMetadataOptionsViewportY(layout));
    }

    private int getMetadataOptionsContentHeight(SettingsLayout layout, int optionCount) {
        if (optionCount <= 0) {
            return 96;
        }

        return optionCount * layout.listItemH + (optionCount - 1) * layout.listGap;
    }

    private void updateMetadataScrollLimits(SettingsLayout layout) {
        metadataGroupsMaxScroll = Math.max(0, getMetadataGroupsContentHeight(layout) - getMetadataGroupsViewportH(layout));
        metadataGroupsScrollOffset = clampInt(metadataGroupsScrollOffset, 0, metadataGroupsMaxScroll);

        List<String> options = getSelectedMetadataOptions();
        metadataOptionsMaxScroll = Math.max(0, getMetadataOptionsContentHeight(layout, options.size()) - getMetadataOptionsViewportH(layout));
        metadataOptionsScrollOffset = clampInt(metadataOptionsScrollOffset, 0, metadataOptionsMaxScroll);
    }

    private ScrollbarLayout buildMetadataGroupsScrollbarLayout(SettingsLayout layout) {
        ScrollbarLayout scrollbar = new ScrollbarLayout();

        int viewportY = getMetadataGroupsViewportY(layout);
        int viewportH = getMetadataGroupsViewportH(layout);
        int contentH = getMetadataGroupsContentHeight(layout);

        scrollbar.trackW = 6;
        scrollbar.trackX = layout.groupX + layout.groupW - 13;
        scrollbar.trackY = viewportY;
        scrollbar.trackH = viewportH;
        scrollbar.thumbW = 6;
        scrollbar.thumbX = scrollbar.trackX;

        if (metadataGroupsMaxScroll <= 0 || contentH <= 0) {
            scrollbar.thumbH = scrollbar.trackH;
            scrollbar.thumbY = scrollbar.trackY;
            return scrollbar;
        }

        scrollbar.thumbH = Math.max(18, (scrollbar.trackH * viewportH) / Math.max(contentH, 1));
        int movableTrack = Math.max(1, scrollbar.trackH - scrollbar.thumbH);
        scrollbar.thumbY = scrollbar.trackY + (metadataGroupsScrollOffset * movableTrack) / Math.max(metadataGroupsMaxScroll, 1);
        return scrollbar;
    }

    private ScrollbarLayout buildMetadataOptionsScrollbarLayout(SettingsLayout layout) {
        ScrollbarLayout scrollbar = new ScrollbarLayout();

        List<String> options = getSelectedMetadataOptions();
        int viewportY = getMetadataOptionsViewportY(layout);
        int viewportH = getMetadataOptionsViewportH(layout);
        int contentH = getMetadataOptionsContentHeight(layout, options.size());

        scrollbar.trackW = 6;
        scrollbar.trackX = layout.listX + layout.listW - 13;
        scrollbar.trackY = viewportY;
        scrollbar.trackH = viewportH;
        scrollbar.thumbW = 6;
        scrollbar.thumbX = scrollbar.trackX;

        if (metadataOptionsMaxScroll <= 0 || contentH <= 0) {
            scrollbar.thumbH = scrollbar.trackH;
            scrollbar.thumbY = scrollbar.trackY;
            return scrollbar;
        }

        scrollbar.thumbH = Math.max(18, (scrollbar.trackH * viewportH) / Math.max(contentH, 1));
        int movableTrack = Math.max(1, scrollbar.trackH - scrollbar.thumbH);
        scrollbar.thumbY = scrollbar.trackY + (metadataOptionsScrollOffset * movableTrack) / Math.max(metadataOptionsMaxScroll, 1);
        return scrollbar;
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

        layout.menuW = 156;
        layout.menuH = 66;
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
        int scrollbarReserve = 16;

        layout.cardsAreaX = contentX + innerPadding;
        layout.cardsAreaY = importedTitleY + 18;
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

        layout.menuW = 156;
        layout.menuH = 66;
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

    private CreateCollectionModalLayout buildCreateCollectionModalLayout() {
        CreateCollectionModalLayout layout = new CreateCollectionModalLayout();

        layout.panelW = 430;
        layout.panelH = 250;
        layout.panelX = (this.width - layout.panelW) / 2;
        layout.panelY = (this.height - layout.panelH) / 2;

        layout.closeSize = 20;
        layout.closeX = layout.panelX + layout.panelW - layout.closeSize - 12;
        layout.closeY = layout.panelY + 12;

        layout.fieldX = layout.panelX + 16;
        layout.fieldW = layout.panelW - 32;
        layout.fieldH = 26;

        layout.nameY = layout.panelY + 56;
        layout.tagY = layout.nameY + 42;
        layout.descriptionY = layout.tagY + 42;

        layout.buttonH = 24;
        layout.cancelW = 90;
        layout.createW = 112;

        layout.cancelY = layout.panelY + layout.panelH - 36;
        layout.createY = layout.cancelY;

        layout.createX = layout.panelX + layout.panelW - 16 - layout.createW;
        layout.cancelX = layout.createX - 10 - layout.cancelW;

        return layout;
    }

    private EditAssetModalLayout buildEditAssetModalLayout() {
        EditAssetModalLayout layout = new EditAssetModalLayout();

        // Dense editor layout tuned for Minecraft GUI Scale 2.
        // It keeps the reference structure, but gives Basic, Variants and Files
        // enough breathing room instead of stacking every block vertically.
        layout.panelW = clampInt(this.width - 14, 700, 860);
        layout.panelH = clampInt(this.height - 8, 430, 560);
        layout.panelX = (this.width - layout.panelW) / 2;
        layout.panelY = (this.height - layout.panelH) / 2;

        layout.closeSize = 22;
        layout.closeX = layout.panelX + layout.panelW - layout.closeSize - 12;
        layout.closeY = layout.panelY + 10;

        layout.headerIconSize = 32;
        layout.headerIconX = layout.panelX + 16;
        layout.headerIconY = layout.panelY + 12;

        layout.buttonH = 24;
        layout.cancelW = 92;
        layout.saveW = 118;
        layout.deleteW = 112;

        layout.saveX = layout.panelX + layout.panelW - 14 - layout.saveW;
        layout.cancelX = layout.saveX - 10 - layout.cancelW;
        layout.cancelY = layout.panelY + layout.panelH - 32;
        layout.saveY = layout.cancelY;
        layout.deleteX = layout.panelX + 14;
        layout.deleteY = layout.cancelY;

        int contentTop = layout.panelY + 60;
        int contentBottom = layout.cancelY - 14;
        int contentH = Math.max(318, contentBottom - contentTop);
        int gap = 8;

        layout.rightW = clampInt((layout.panelW * 28) / 100, 210, 242);
        layout.leftX = layout.panelX + 14;
        layout.leftY = contentTop;
        layout.leftW = layout.panelW - 28 - layout.rightW - gap;
        layout.rightX = layout.leftX + layout.leftW + gap;
        layout.rightY = contentTop;

        layout.basicX = layout.leftX;
        layout.basicY = contentTop;
        layout.basicW = layout.leftW;

        int availablePanelsH = contentH - gap;
        layout.basicH = clampInt((availablePanelsH * 55) / 100, 194, 228);
        int bottomH = availablePanelsH - layout.basicH;

        int minimumBottomH = 144;
        if (bottomH < minimumBottomH) {
            int deficit = minimumBottomH - bottomH;
            layout.basicH = Math.max(178, layout.basicH - deficit);
            bottomH = availablePanelsH - layout.basicH;
        }

        bottomH = Math.max(minimumBottomH, bottomH);

        int bottomY = layout.basicY + layout.basicH + gap;
        int bottomGap = 8;
        int variantsPreferredW = (layout.leftW * 57) / 100;

        layout.variantsBoxX = layout.leftX;
        layout.variantsBoxY = bottomY;
        layout.variantsBoxW = clampInt(variantsPreferredW, 260, layout.leftW - 218);
        layout.variantsBoxH = bottomH;

        layout.filesX = layout.variantsBoxX + layout.variantsBoxW + bottomGap;
        layout.filesY = bottomY;
        layout.filesW = layout.leftX + layout.leftW - layout.filesX;
        layout.filesH = bottomH;

        layout.previewPanelX = layout.rightX;
        layout.previewPanelY = contentTop;
        layout.previewPanelW = layout.rightW;
        layout.previewPanelH = contentBottom - contentTop;

        layout.previewX = layout.previewPanelX + 10;
        layout.previewY = layout.previewPanelY + 32;
        layout.previewW = layout.previewPanelW - 20;
        layout.previewH = clampInt((layout.previewPanelH * 36) / 100, 108, 146);

        layout.fieldH = 23;
        layout.fieldGap = 40;

        int innerX = layout.basicX + 12;
        int innerW = layout.basicW - 24;
        int colGap = 10;
        int colW = (innerW - colGap) / 2;

        int row1Y = layout.basicY + 54;
        int rowGap = clampInt((layout.basicH - 88 - layout.fieldH) / 3, 36, 42);
        int row2Y = row1Y + rowGap;
        int row3Y = row2Y + rowGap;
        int row4Y = row3Y + rowGap;

        int maxRow4Y = layout.basicY + layout.basicH - layout.fieldH - 12;
        if (row4Y > maxRow4Y) {
            row4Y = maxRow4Y;
        }

        layout.nameX = innerX;
        layout.nameY = row1Y;
        layout.nameW = colW;
        layout.categoryX = innerX + colW + colGap;
        layout.categoryY = row1Y;
        layout.categoryW = colW;
        layout.typeX = innerX;
        layout.typeY = row2Y;
        layout.typeW = colW;
        layout.versionX = innerX + colW + colGap;
        layout.versionY = row2Y;
        layout.versionW = colW;
        layout.authorX = innerX;
        layout.authorY = row3Y;
        layout.authorW = innerW;
        layout.tagsX = innerX;
        layout.tagsY = row4Y;
        layout.tagsW = innerW;

        layout.variantRowX = layout.variantsBoxX + 12;
        layout.variantRowY = layout.variantsBoxY + 48;
        layout.variantRowW = layout.variantsBoxW - 24;
        layout.variantRowH = 23;
        layout.variantRowGap = 7;
        layout.addVariantW = 108;
        layout.removeVariantW = 116;
        layout.removeVariantX = layout.variantsBoxX + layout.variantsBoxW - 12 - layout.removeVariantW;
        layout.addVariantX = layout.removeVariantX - 8 - layout.addVariantW;
        if (layout.addVariantX < layout.variantRowX) {
            layout.addVariantX = layout.variantRowX;
            layout.removeVariantX = layout.addVariantX + layout.addVariantW + 8;
        }
        layout.addVariantY = layout.variantsBoxY + layout.variantsBoxH - layout.buttonH - 10;
        layout.removeVariantY = layout.addVariantY;

        layout.fileFieldH = 22;
        int fileInnerX = layout.filesX + 12;
        int fileInnerW = layout.filesW - 24;
        layout.structureFileX = fileInnerX;
        layout.structureFileY = layout.filesY + 52;
        layout.structureFileW = fileInnerW;
        layout.previewFileX = fileInnerX;
        layout.previewFileY = layout.structureFileY + 40;
        layout.previewFileW = fileInnerW;

        int toggleY = layout.filesY + layout.filesH - 28;
        layout.favoriteToggleX = fileInnerX + 78;
        layout.favoriteToggleY = toggleY;
        layout.visibleToggleX = layout.filesX + layout.filesW - 44;
        layout.visibleToggleY = toggleY;

        // Legacy aliases used by existing EditBox setup and older click calculations.
        layout.fieldX = layout.nameX;
        layout.fieldW = layout.nameW;
        layout.nameLegacyY = layout.nameY;
        layout.categoryLegacyY = layout.categoryY;
        layout.typeLegacyY = layout.typeY;
        layout.versionLegacyY = layout.versionY;
        layout.variantsLegacyY = layout.variantRowY;

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
        collectionPickerOpen = false;
    }

    private void closeListMenu() {
        listMenuOpen = false;
        listMenuAssetName = null;
        collectionPickerOpen = false;
    }

    private boolean isListMenuOpenFor(ArchivAsset asset) {
        return listMenuOpen
                && listMenuAssetName != null
                && listMenuAssetName.equals(asset.getName());
    }

    private boolean isCollectionPickerOpenFor(ArchivAsset asset) {
        return isListMenuOpenFor(asset) && collectionPickerOpen;
    }

    private void openCollectionPicker(ArchivAsset asset) {
        if (!isSavedAsset(asset)) {
            return;
        }

        listMenuOpen = true;
        listMenuAssetName = asset.getName();
        collectionPickerOpen = true;
    }

    private ArchivAsset getSavedAssetByName(String assetName) {
        if (assetName == null) {
            return null;
        }

        for (ArchivAsset asset : savedAssets) {
            if (asset.getName().equals(assetName)) {
                return asset;
            }
        }

        return null;
    }

    private void openEditAssetModal(ArchivAsset asset) {
        if (!isSavedAsset(asset)) {
            return;
        }

        closeListMenu();

        editAssetModalOpen = true;
        editAssetOriginalName = asset.getName();
        editAssetDropdownOpen = null;

        if (editAssetNameBox != null) {
            editAssetNameBox.setValue(asset.getName());
            editAssetNameBox.setFocused(true);
            this.setFocused(editAssetNameBox);
        }

        editAssetSelectedCategory = getSafeOption(macroCategories, asset.getMacroCategory());
        editAssetSelectedType = getSafeOption(assetTypes, asset.getType());
        editAssetSelectedVersion = getSafeOption(minecraftVersions, asset.getVersion());
        editAssetVariantCount = clampInt(asset.getVariantCount(), 1, 99);

        if (editAssetCategoryBox != null) {
            editAssetCategoryBox.setValue(editAssetSelectedCategory);
            editAssetCategoryBox.setFocused(false);
        }

        if (editAssetTypeBox != null) {
            editAssetTypeBox.setValue(editAssetSelectedType);
            editAssetTypeBox.setFocused(false);
        }

        if (editAssetVersionBox != null) {
            editAssetVersionBox.setValue(editAssetSelectedVersion);
            editAssetVersionBox.setFocused(false);
        }

        if (editAssetVariantsBox != null) {
            editAssetVariantsBox.setValue(String.valueOf(editAssetVariantCount));
            editAssetVariantsBox.setFocused(false);
        }

        libraryActionMessage = "Editing: " + asset.getName();
    }

    private void closeEditAssetModal() {
        editAssetModalOpen = false;
        editAssetOriginalName = null;
        editAssetDropdownOpen = null;

        if (editAssetNameBox != null) editAssetNameBox.setFocused(false);
        if (editAssetCategoryBox != null) editAssetCategoryBox.setFocused(false);
        if (editAssetTypeBox != null) editAssetTypeBox.setFocused(false);
        if (editAssetVersionBox != null) editAssetVersionBox.setFocused(false);
        if (editAssetVariantsBox != null) editAssetVariantsBox.setFocused(false);

        this.setFocused(null);
    }

    private boolean assetNameExistsExcept(String name, String ignoredName) {
        for (ArchivAsset asset : savedAssets) {
            if (asset.getName().equalsIgnoreCase(name) && !asset.getName().equals(ignoredName)) {
                return true;
            }
        }

        return false;
    }

    private int parseVariantCount(String value, int fallback) {
        try {
            return clampInt(Integer.parseInt(trimToEmpty(value)), 1, 99);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private int getChipColorForEditedAsset(String type, int fallback) {
        String normalized = trimToEmpty(type).toLowerCase();

        return switch (normalized) {
            case "structure" -> 0xFF2D9CDB;
            case "decoration" -> 0xFF8A5CFF;
            case "tree", "nature" -> 0xFF2DBE73;
            case "prop" -> 0xFFDA8A2D;
            default -> fallback;
        };
    }

    private ArchivAsset buildEditedAsset(ArchivAsset oldAsset, String newName) {
        String category = getSafeOption(macroCategories, editAssetSelectedCategory);
        String type = getSafeOption(assetTypes, editAssetSelectedType);
        String version = getSafeOption(minecraftVersions, editAssetSelectedVersion);

        int variantCount = clampInt(editAssetVariantCount, 1, 99);
        int chipColor = getChipColorForEditedAsset(type, oldAsset.getChipColor());

        return new ArchivAsset(
                newName,
                category,
                type,
                version,
                oldAsset.getPreviewColor(),
                chipColor,
                variantCount,
                oldAsset.isFavorite(),
                oldAsset.isHighlighted()
        );
    }

    private void replaceAssetNameReferences(String oldName, String newName) {
        for (CollectionEntry entry : collectionEntries) {
            List<String> assetNames = entry.getAssetNames();

            for (int i = 0; i < assetNames.size(); i++) {
                if (assetNames.get(i).equals(oldName)) {
                    assetNames.set(i, newName);
                }
            }
        }

        for (int i = 0; i < recentLoadedAssetNames.size(); i++) {
            if (recentLoadedAssetNames.get(i).equals(oldName)) {
                recentLoadedAssetNames.set(i, newName);
            }
        }

        if (oldName.equals(loadedAssetName)) {
            loadedAssetName = newName;
        }

        if (oldName.equals(detailsAssetName)) {
            detailsAssetName = newName;
        }

        if (oldName.equals(deleteConfirmAssetName)) {
            deleteConfirmAssetName = newName;
        }

        if (oldName.equals(selectedLibraryAssetName)) {
            selectedLibraryAssetName = newName;
        }

        if (oldName.equals(listMenuAssetName)) {
            listMenuAssetName = newName;
        }
    }

    private void saveEditedAsset() {
        ArchivAsset oldAsset = getSavedAssetByName(editAssetOriginalName);

        if (oldAsset == null) {
            libraryActionMessage = "Asset not found";
            closeEditAssetModal();
            return;
        }

        String newName = trimToEmpty(editAssetNameBox.getValue());

        if (newName.isBlank()) {
            libraryActionMessage = "Asset name is required";
            return;
        }

        if (assetNameExistsExcept(newName, oldAsset.getName())) {
            libraryActionMessage = "Asset name already exists";
            return;
        }

        ArchivAsset editedAsset = buildEditedAsset(oldAsset, newName);

        for (int i = 0; i < savedAssets.size(); i++) {
            if (savedAssets.get(i).getName().equals(oldAsset.getName())) {
                savedAssets.set(i, editedAsset);
                break;
            }
        }

        replaceAssetNameReferences(oldAsset.getName(), newName);

        selectedLibraryAssetName = newName;
        libraryActionMessage = "Edited: " + newName;

        closeEditAssetModal();
        syncLibrarySelectionWithVisibleAssets();
    }

    private void addAssetToCollection(ArchivAsset asset, CollectionEntry targetCollection) {
        closeListMenu();

        if (targetCollection == null) {
            libraryActionMessage = "Collection not found";
            return;
        }

        boolean alreadyAdded = targetCollection.containsAsset(asset.getName());
        targetCollection.addAsset(asset.getName());
        selectedCollectionName = targetCollection.getName();

        libraryActionMessage = alreadyAdded
                ? asset.getName() + " is already in " + targetCollection.getName()
                : "Added " + asset.getName() + " to " + targetCollection.getName();
    }

    private int getAssetCollectionPickerItemCount() {
        return collectionEntries.size() + 1; // collections + create new
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
        normalizeMetadataSelections();
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

    private int getImportPresetVariantCount(ImportPreset preset) {
        String[] parts = preset.variants.split(",");
        return Math.max(1, parts.length);
    }

    private int getImportVariantCount() {
        return getCurrentImportVariantCount();
    }

    private ArchivAsset buildSavedAssetFromImport() {
        ImportPreset preset = getCurrentImportPreset();
        String type = getCurrentImportType();

        return new ArchivAsset(
                getNextSavedAssetName(),
                getCurrentImportCategory(),
                type,
                getCurrentImportVersion(),
                mockPreviewImageSelected ? MOCK_PREVIEW_IMAGE_COLOR : MOCK_NO_PREVIEW_IMAGE_COLOR,
                getChipColorForEditedAsset(type, preset.chipColor),
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

    private int getCollectionCount() {
        return collectionEntries.size();
    }

    private String getNextCollectionName() {
        int index = collectionEntries.size() + 1;
        return "New Collection #" + index;
    }

    private int getCollectionPreviewColorByIndex(int index) {
        int[] colors = {
                0xFF6C89B8,
                0xFF4E7C57,
                0xFF3A4F84,
                0xFF6A5A9A,
                0xFF5C7A8F
        };

        return colors[index % colors.length];
    }

    private int getCollectionAccentColorByIndex(int index) {
        int[] colors = {
                0xFF6D4BC8,
                0xFF3D9B63,
                0xFFDA8A2D,
                0xFF2D9CDB,
                0xFF8A5CFF
        };

        return colors[index % colors.length];
    }

    private void createCollection() {
        int index = collectionEntries.size();

        String name = getNextCollectionName();
        int previewColor = getCollectionPreviewColorByIndex(index);
        int accentColor = getCollectionAccentColorByIndex(index);

        collectionEntries.add(new CollectionEntry(
                name,
                "",
                "",
                previewColor,
                accentColor
        ));

        selectedCollectionName = name;
        selectedMyAssetsSection = "Collections";
        libraryActionMessage = "Created collection: " + name;
        resetMyAssetsScroll();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private int getBrowseCategoryCount() {
        return macroCategories.size() + 1;
    }

    private String getBrowseCategoryAt(int index) {
        if (index <= 0) {
            return "All";
        }

        int macroIndex = index - 1;
        if (macroIndex >= 0 && macroIndex < macroCategories.size()) {
            return macroCategories.get(macroIndex);
        }

        return "All";
    }

    private int getSelectedIndex(List<String> options, String value) {
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).equals(value)) {
                return i;
            }
        }

        return 0;
    }

    private String getSafeOption(List<String> options, String currentValue) {
        if (options.isEmpty()) {
            return "Uncategorized";
        }

        for (String option : options) {
            if (option.equals(currentValue)) {
                return option;
            }
        }

        return options.get(0);
    }

    private String cycleOption(List<String> options, String currentValue) {
        if (options.isEmpty()) {
            return currentValue;
        }

        int currentIndex = getSelectedIndex(options, currentValue);
        int nextIndex = (currentIndex + 1) % options.size();
        return options.get(nextIndex);
    }

    private void normalizeMetadataSelections() {
        if (macroCategories.isEmpty()) {
            macroCategories.add("Uncategorized");
        }
        if (assetTypes.isEmpty()) {
            assetTypes.add("Structure");
        }
        if (minecraftVersions.isEmpty()) {
            minecraftVersions.add("1.20.1");
        }

        if (!"All".equals(selectedCategory) && !macroCategories.contains(selectedCategory)) {
            selectedCategory = "All";
        }

        importSelectedMacroCategory = getSafeOption(macroCategories, importSelectedMacroCategory);
        importSelectedType = getSafeOption(assetTypes, importSelectedType);
        importSelectedVersion = getSafeOption(minecraftVersions, importSelectedVersion);

        editAssetSelectedCategory = getSafeOption(macroCategories, editAssetSelectedCategory);
        editAssetSelectedType = getSafeOption(assetTypes, editAssetSelectedType);
        editAssetSelectedVersion = getSafeOption(minecraftVersions, editAssetSelectedVersion);

        selectedMacroCategoryIndex = clampInt(selectedMacroCategoryIndex, 0, macroCategories.size() - 1);
        selectedAssetTypeIndex = clampInt(selectedAssetTypeIndex, 0, assetTypes.size() - 1);
        selectedMinecraftVersionIndex = clampInt(selectedMinecraftVersionIndex, 0, minecraftVersions.size() - 1);
    }

    private boolean isMetadataGroupEditable(String group) {
        return "Macro Categories".equals(group)
                || "Types".equals(group)
                || "Minecraft Versions".equals(group);
    }

    private boolean isSelectedMetadataGroupEditable() {
        return isMetadataGroupEditable(selectedMetadataGroup);
    }

    private int getMetadataGroupCount(String group) {
        return switch (group) {
            case "Types" -> assetTypes.size();
            case "Minecraft Versions" -> minecraftVersions.size();
            case "Variant Presets" -> 15;
            case "Tags / Keywords" -> 128;
            case "Materials" -> 43;
            default -> macroCategories.size();
        };
    }

    private String getMetadataGroupSubtitle(String group) {
        return switch (group) {
            case "Types" -> "Asset content types";
            case "Minecraft Versions" -> "Supported MC versions";
            case "Variant Presets" -> "Predefined variants";
            case "Tags / Keywords" -> "Searchable labels";
            case "Materials" -> "Common materials";
            default -> "High-level content themes";
        };
    }

    private String getMetadataGroupIcon(String group) {
        return switch (group) {
            case "Types" -> "<>";
            case "Minecraft Versions" -> "[]";
            case "Variant Presets" -> "◇";
            case "Tags / Keywords" -> "#";
            case "Materials" -> "□";
            default -> "▣";
        };
    }

    private int getMetadataAccentColor(String value, int index) {
        String normalized = trimToEmpty(value).toLowerCase();

        return switch (normalized) {
            case "medieval", "structure" -> 0xFF3B82F6;
            case "fantasy", "decoration" -> 0xFF8A5CFF;
            case "cyberpunk", "prop" -> 0xFFE05DFF;
            case "sci-fi", "sci fi", "vehicle" -> 0xFF2DAEFF;
            case "organic", "tree", "nature" -> 0xFF36C275;
            case "modern", "interior" -> 0xFF61748B;
            case "industrial", "redstone", "terrain" -> 0xFFFF8A2D;
            default -> switch (index % 6) {
                case 0 -> 0xFF3B82F6;
                case 1 -> 0xFF8A5CFF;
                case 2 -> 0xFF2DAEFF;
                case 3 -> 0xFF36C275;
                case 4 -> 0xFFFF8A2D;
                default -> 0xFF61748B;
            };
        };
    }

    private String getMetadataOptionIcon(String value, int index) {
        String normalized = trimToEmpty(value).toLowerCase();

        return switch (normalized) {
            case "medieval" -> "♜";
            case "fantasy" -> "⚔";
            case "cyberpunk" -> "▥";
            case "sci-fi", "sci fi" -> "✦";
            case "organic" -> "☘";
            case "nature", "tree" -> "▲";
            case "modern" -> "▤";
            case "industrial", "redstone" -> "⚙";
            case "structure" -> "▣";
            case "decoration" -> "✧";
            case "prop" -> "◆";
            case "terrain" -> "▰";
            case "interior" -> "⌂";
            case "vehicle" -> "▻";
            default -> isSelectedMetadataGroupEditable() ? "•" : "○";
        };
    }

    private List<String> getSelectedMetadataOptions() {
        return switch (selectedMetadataGroup) {
            case "Types" -> assetTypes;
            case "Minecraft Versions" -> minecraftVersions;
            case "Macro Categories" -> macroCategories;
            default -> new ArrayList<>();
        };
    }

    private int getSelectedMetadataIndex() {
        return switch (selectedMetadataGroup) {
            case "Types" -> selectedAssetTypeIndex;
            case "Minecraft Versions" -> selectedMinecraftVersionIndex;
            case "Macro Categories" -> selectedMacroCategoryIndex;
            default -> 0;
        };
    }

    private void setSelectedMetadataIndex(int index) {
        List<String> options = getSelectedMetadataOptions();
        int safeIndex = options.isEmpty() ? 0 : clampInt(index, 0, options.size() - 1);

        switch (selectedMetadataGroup) {
            case "Types" -> selectedAssetTypeIndex = safeIndex;
            case "Minecraft Versions" -> selectedMinecraftVersionIndex = safeIndex;
            case "Macro Categories" -> selectedMacroCategoryIndex = safeIndex;
            default -> {
            }
        }
    }

    private String getSelectedMetadataSingularLabel() {
        return switch (selectedMetadataGroup) {
            case "Types" -> "type";
            case "Minecraft Versions" -> "version";
            case "Variant Presets" -> "variant preset";
            case "Tags / Keywords" -> "tag";
            case "Materials" -> "material";
            default -> "category";
        };
    }

    private String getNextMetadataOptionName() {
        List<String> options = getSelectedMetadataOptions();
        String baseName = switch (selectedMetadataGroup) {
            case "Types" -> "New Type";
            case "Minecraft Versions" -> "1.20.";
            default -> "New Category";
        };

        int index = options.size() + 1;
        String candidate = "Minecraft Versions".equals(selectedMetadataGroup)
                ? baseName + index
                : baseName + " #" + index;

        while (options.contains(candidate)) {
            index++;
            candidate = "Minecraft Versions".equals(selectedMetadataGroup)
                    ? baseName + index
                    : baseName + " #" + index;
        }

        return candidate;
    }

    private String getUniqueMetadataOptionName(String baseName) {
        List<String> options = getSelectedMetadataOptions();
        String candidate = baseName;
        int index = 2;

        while (options.contains(candidate)) {
            candidate = baseName + " #" + index;
            index++;
        }

        return candidate;
    }

    private void addMetadataOption() {
        if (!isSelectedMetadataGroupEditable()) {
            libraryActionMessage = selectedMetadataGroup + " editor is planned for a later pass";
            return;
        }

        List<String> options = getSelectedMetadataOptions();
        String newOption = getNextMetadataOptionName();
        options.add(newOption);
        setSelectedMetadataIndex(options.size() - 1);
        normalizeMetadataSelections();
        libraryActionMessage = "Added " + getSelectedMetadataSingularLabel() + ": " + newOption;
    }

    private void duplicateSelectedMetadataOption() {
        if (!isSelectedMetadataGroupEditable()) {
            libraryActionMessage = selectedMetadataGroup + " duplication is planned";
            return;
        }

        List<String> options = getSelectedMetadataOptions();

        if (options.isEmpty()) {
            addMetadataOption();
            return;
        }

        int index = getSelectedMetadataIndex();
        String source = options.get(index);
        String duplicated = getUniqueMetadataOptionName(source + " Copy");

        options.add(index + 1, duplicated);
        setSelectedMetadataIndex(index + 1);
        normalizeMetadataSelections();
        libraryActionMessage = "Duplicated " + getSelectedMetadataSingularLabel() + ": " + duplicated;
    }

    private void removeSelectedMetadataOption() {
        if (!isSelectedMetadataGroupEditable()) {
            libraryActionMessage = selectedMetadataGroup + " removal is planned";
            return;
        }

        List<String> options = getSelectedMetadataOptions();

        if (options.size() <= 1) {
            libraryActionMessage = "At least one " + getSelectedMetadataSingularLabel() + " is required";
            return;
        }

        int index = getSelectedMetadataIndex();
        String removed = options.remove(index);
        setSelectedMetadataIndex(Math.max(0, index - 1));
        normalizeMetadataSelections();

        libraryActionMessage = "Removed " + getSelectedMetadataSingularLabel() + ": " + removed;
    }

    private void resetMetadataDefaults() {
        macroCategories.clear();
        macroCategories.addAll(DEFAULT_MACRO_CATEGORIES);

        assetTypes.clear();
        assetTypes.addAll(DEFAULT_ASSET_TYPES);

        minecraftVersions.clear();
        minecraftVersions.addAll(DEFAULT_MINECRAFT_VERSIONS);

        selectedMacroCategoryIndex = 0;
        selectedAssetTypeIndex = 0;
        selectedMinecraftVersionIndex = 0;

        normalizeMetadataSelections();
        libraryActionMessage = "Metadata settings reset to defaults";
    }

    private void applyImportPresetMetadata() {
        ImportPreset preset = getCurrentImportPreset();
        importSelectedMacroCategory = getSafeOption(macroCategories, preset.macroCategory);
        importSelectedType = getSafeOption(assetTypes, preset.type);
        importSelectedVersion = getSafeOption(minecraftVersions, preset.version);
        importSelectedVariantCount = getImportPresetVariantCount(preset);
    }

    private String getCurrentImportCategory() {
        return getSafeOption(macroCategories, importSelectedMacroCategory);
    }

    private String getCurrentImportType() {
        return getSafeOption(assetTypes, importSelectedType);
    }

    private String getCurrentImportVersion() {
        return getSafeOption(minecraftVersions, importSelectedVersion);
    }

    private int getCurrentImportVariantCount() {
        return clampInt(importSelectedVariantCount, 1, 99);
    }

    private boolean collectionNameExists(String name) {
        for (CollectionEntry entry : collectionEntries) {
            if (entry.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private String getUniqueCollectionName(String baseName) {
        if (!collectionNameExists(baseName)) {
            return baseName;
        }

        int index = 2;
        while (collectionNameExists(baseName + " #" + index)) {
            index++;
        }

        return baseName + " #" + index;
    }

    private boolean isCreateCollectionFormValid() {
        return collectionNameBox != null && !trimToEmpty(collectionNameBox.getValue()).isBlank();
    }

    private boolean isEditAssetFormValid() {
        return editAssetNameBox != null && !trimToEmpty(editAssetNameBox.getValue()).isBlank();
    }

    private void openCreateCollectionModal() {
        createCollectionModalOpen = true;

        if (collectionNameBox != null) {
            collectionNameBox.setValue("");
            collectionNameBox.setFocused(true);
            this.setFocused(collectionNameBox);
        }

        if (collectionTagBox != null) {
            collectionTagBox.setValue("");
            collectionTagBox.setFocused(false);
        }

        if (collectionDescriptionBox != null) {
            collectionDescriptionBox.setValue("");
            collectionDescriptionBox.setFocused(false);
        }

        closeListMenu();
    }

    private void closeCreateCollectionModal() {
        createCollectionModalOpen = false;

        if (collectionNameBox != null) {
            collectionNameBox.setFocused(false);
        }
        if (collectionTagBox != null) {
            collectionTagBox.setFocused(false);
        }
        if (collectionDescriptionBox != null) {
            collectionDescriptionBox.setFocused(false);
        }

        this.setFocused(null);
        pendingCollectionAssetName = null;
    }

    private void createCollectionFromModal() {
        String rawName = trimToEmpty(collectionNameBox.getValue());
        String tag = trimToEmpty(collectionTagBox.getValue());
        String description = trimToEmpty(collectionDescriptionBox.getValue());

        if (rawName.isBlank()) {
            libraryActionMessage = "Collection name is required";
            return;
        }

        String finalName = getUniqueCollectionName(rawName);
        int index = collectionEntries.size();

        int previewColor = getCollectionPreviewColorByIndex(index);
        int accentColor = getCollectionAccentColorByIndex(index);

        CollectionEntry newEntry = new CollectionEntry(
                finalName,
                tag,
                description,
                previewColor,
                accentColor
        );

        collectionEntries.add(newEntry);
        selectedCollectionName = finalName;
        selectedMyAssetsSection = "Collections";

        ArchivAsset pendingAsset = getSavedAssetByName(pendingCollectionAssetName);

        if (pendingAsset != null) {
            newEntry.addAsset(pendingAsset.getName());
            libraryActionMessage = "Created " + finalName + " and added " + pendingAsset.getName();
        } else {
            libraryActionMessage = "Created collection: " + finalName;
        }

        resetMyAssetsScroll();
        pendingCollectionAssetName = null;
        closeCreateCollectionModal();
    }

    private CollectionEntry getSelectedCollectionEntry() {
        if (selectedCollectionName == null) {
            return null;
        }

        for (CollectionEntry entry : collectionEntries) {
            if (entry.getName().equals(selectedCollectionName)) {
                return entry;
            }
        }

        return null;
    }

    private CollectionEntry getCollectionByName(String collectionName) {
        if (collectionName == null) {
            return null;
        }

        for (CollectionEntry entry : collectionEntries) {
            if (entry.getName().equals(collectionName)) {
                return entry;
            }
        }

        return null;
    }

    private List<ArchivAsset> getSelectedCollectionAssets() {
        List<ArchivAsset> result = new ArrayList<>();

        CollectionEntry selected = getSelectedCollectionEntry();
        if (selected == null) {
            return result;
        }

        for (String assetName : selected.getAssetNames()) {
            for (ArchivAsset asset : savedAssets) {
                if (asset.getName().equals(assetName)) {
                    result.add(asset);
                    break;
                }
            }
        }

        return result;
    }

    private CollectionEntry getDefaultTargetCollection() {
        CollectionEntry selected = getSelectedCollectionEntry();

        if (selected != null) {
            return selected;
        }

        if (!collectionEntries.isEmpty()) {
            return collectionEntries.get(0);
        }

        return null;
    }

    private void addAssetToDefaultCollection(ArchivAsset asset) {
        closeListMenu();

        CollectionEntry targetCollection = getDefaultTargetCollection();

        if (targetCollection == null) {
            libraryActionMessage = "Create a collection before adding assets";
            openCreateCollectionModal();
            return;
        }

        boolean alreadyAdded = targetCollection.containsAsset(asset.getName());

        targetCollection.addAsset(asset.getName());
        selectedCollectionName = targetCollection.getName();

        libraryActionMessage = alreadyAdded
                ? asset.getName() + " is already in " + targetCollection.getName()
                : "Added " + asset.getName() + " to " + targetCollection.getName();
    }

    private int getAssetCollectionRollupW() {
        return 176;
    }

    private int getAssetCollectionRollupX(int menuX, int menuW, int rollupW) {
        int preferredX = menuX + menuW + 4;
        int rightLimit = this.width - 24;

        if (preferredX + rollupW <= rightLimit) {
            return preferredX;
        }

        return menuX - rollupW - 4;
    }

    private int getAssetCollectionRollupY(int menuY) {
        return menuY + 22;
    }

    private boolean isHoveringAddToCollectionRow(double mouseX, double mouseY, int menuX, int menuY, int menuW) {
        int itemH = 22;
        return isInside(mouseX, mouseY, menuX, menuY + itemH, menuW, itemH);
    }

    private boolean isInsideCollectionRollup(double mouseX, double mouseY, int menuX, int menuY, int menuW) {
        int itemH = 22;
        int rollupW = getAssetCollectionRollupW();
        int rollupX = getAssetCollectionRollupX(menuX, menuW, rollupW);
        int rollupY = getAssetCollectionRollupY(menuY);
        int rollupH = getAssetCollectionPickerItemCount() * itemH;

        return isInside(mouseX, mouseY, rollupX, rollupY, rollupW, rollupH);
    }

    private boolean shouldShowCollectionRollup(double mouseX, double mouseY, int menuX, int menuY, int menuW) {
        return isHoveringAddToCollectionRow(mouseX, mouseY, menuX, menuY, menuW)
                || isInsideCollectionRollup(mouseX, mouseY, menuX, menuY, menuW);
    }

    private boolean handleAssetContextMenuClick(double mouseX, double mouseY, ArchivAsset asset, int menuX, int menuY, int menuW) {
        if (!isSavedAsset(asset) || !isListMenuOpenFor(asset)) {
            return false;
        }

        int itemH = 22;

        if (isInside(mouseX, mouseY, menuX, menuY, menuW, itemH)) {
            openEditAssetModal(asset);
            return true;
        }

        if (isInside(mouseX, mouseY, menuX, menuY + itemH, menuW, itemH)) {
            return true;
        }

        if (shouldShowCollectionRollup(mouseX, mouseY, menuX, menuY, menuW)) {
            int rollupW = getAssetCollectionRollupW();
            int rollupX = getAssetCollectionRollupX(menuX, menuW, rollupW);
            int rollupY = getAssetCollectionRollupY(menuY);

            for (int i = 0; i < collectionEntries.size(); i++) {
                int rowY = rollupY + (i * itemH);

                if (isInside(mouseX, mouseY, rollupX, rowY, rollupW, itemH)) {
                    addAssetToCollection(asset, collectionEntries.get(i));
                    return true;
                }
            }

            int createY = rollupY + (collectionEntries.size() * itemH);

            if (isInside(mouseX, mouseY, rollupX, createY, rollupW, itemH)) {
                pendingCollectionAssetName = asset.getName();
                openCreateCollectionModal();
                return true;
            }
        }

        if (isInside(mouseX, mouseY, menuX, menuY + (itemH * 2), menuW, itemH)) {
            openDeleteConfirm(asset);
            return true;
        }

        return false;
    }

    private List<String> getEditAssetDropdownOptions(String dropdownName) {
        if ("category".equals(dropdownName)) {
            return macroCategories;
        }

        if ("type".equals(dropdownName)) {
            return assetTypes;
        }

        if ("version".equals(dropdownName)) {
            return minecraftVersions;
        }

        return new ArrayList<>();
    }

    private int getEditAssetDropdownX(EditAssetModalLayout modal, String dropdownName) {
        if ("type".equals(dropdownName)) {
            return modal.typeX;
        }

        if ("version".equals(dropdownName)) {
            return modal.versionX;
        }

        return modal.categoryX;
    }

    private int getEditAssetDropdownY(EditAssetModalLayout modal, String dropdownName) {
        if ("type".equals(dropdownName)) {
            return modal.typeY + modal.fieldH + 2;
        }

        if ("version".equals(dropdownName)) {
            return modal.versionY + modal.fieldH + 2;
        }

        return modal.categoryY + modal.fieldH + 2;
    }

    private int getEditAssetDropdownW(EditAssetModalLayout modal, String dropdownName) {
        if ("type".equals(dropdownName)) {
            return modal.typeW;
        }

        if ("version".equals(dropdownName)) {
            return modal.versionW;
        }

        return modal.categoryW;
    }

    private void setEditAssetDropdownValue(String dropdownName, String value) {
        if ("category".equals(dropdownName)) {
            editAssetSelectedCategory = getSafeOption(macroCategories, value);
            libraryActionMessage = "Category: " + editAssetSelectedCategory;
            return;
        }

        if ("type".equals(dropdownName)) {
            editAssetSelectedType = getSafeOption(assetTypes, value);
            libraryActionMessage = "Type: " + editAssetSelectedType;
            return;
        }

        if ("version".equals(dropdownName)) {
            editAssetSelectedVersion = getSafeOption(minecraftVersions, value);
            libraryActionMessage = "Version: " + editAssetSelectedVersion;
        }
    }

    private void toggleEditAssetDropdown(String dropdownName) {
        if (dropdownName == null) {
            editAssetDropdownOpen = null;
            return;
        }

        editAssetDropdownOpen = dropdownName.equals(editAssetDropdownOpen) ? null : dropdownName;

        if (editAssetNameBox != null) {
            editAssetNameBox.setFocused(false);
        }

        this.setFocused(null);
    }

    private boolean handleEditAssetDropdownClick(double mouseX, double mouseY, EditAssetModalLayout modal) {
        if (editAssetDropdownOpen == null) {
            return false;
        }

        List<String> options = getEditAssetDropdownOptions(editAssetDropdownOpen);
        int rowH = 24;
        int x = getEditAssetDropdownX(modal, editAssetDropdownOpen);
        int y = getEditAssetDropdownY(modal, editAssetDropdownOpen);
        int w = getEditAssetDropdownW(modal, editAssetDropdownOpen);
        int h = Math.max(rowH, options.size() * rowH);

        if (!isInside(mouseX, mouseY, x, y, w, h)) {
            return false;
        }

        if (!options.isEmpty()) {
            int index = clampInt((int) ((mouseY - y) / rowH), 0, options.size() - 1);
            setEditAssetDropdownValue(editAssetDropdownOpen, options.get(index));
        }

        editAssetDropdownOpen = null;
        return true;
    }

    private boolean myAssetsShowsImportedGrid() {
        return "All Assets".equals(selectedMyAssetsSection)
                || "Favorites".equals(selectedMyAssetsSection)
                || "Imported".equals(selectedMyAssetsSection)
                || "Recent".equals(selectedMyAssetsSection)
                || "Collections".equals(selectedMyAssetsSection);
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

    private void drawClippedString(GuiGraphics guiGraphics, String text, int x, int y, int maxWidth, int color) {
        guiGraphics.drawString(this.font, fitTextToWidth(text, maxWidth), x, y, color);
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

    private boolean isBrowseOverlayOpen() {
        return assetDetailsOpen || deleteConfirmOpen;
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

        if (createCollectionModalOpen) {
            CreateCollectionModalLayout modal = buildCreateCollectionModalLayout();

            if (isInside(mouseX, mouseY, modal.closeX, modal.closeY, modal.closeSize, modal.closeSize)) {
                closeCreateCollectionModal();
                return true;
            }

            if (isInside(mouseX, mouseY, modal.cancelX, modal.cancelY, modal.cancelW, modal.buttonH)) {
                closeCreateCollectionModal();
                return true;
            }

            if (isInside(mouseX, mouseY, modal.createX, modal.createY, modal.createW, modal.buttonH)) {
                createCollectionFromModal();
                return true;
            }

            if (collectionNameBox != null && isInside(mouseX, mouseY, collectionNameBox.getX(), collectionNameBox.getY(), collectionNameBox.getWidth(), collectionNameBox.getHeight())) {
                collectionNameBox.setFocused(true);
                if (collectionTagBox != null) collectionTagBox.setFocused(false);
                if (collectionDescriptionBox != null) collectionDescriptionBox.setFocused(false);
                this.setFocused(collectionNameBox);
                return super.mouseClicked(event, doubleClick);
            }

            if (collectionTagBox != null && isInside(mouseX, mouseY, collectionTagBox.getX(), collectionTagBox.getY(), collectionTagBox.getWidth(), collectionTagBox.getHeight())) {
                collectionTagBox.setFocused(true);
                if (collectionNameBox != null) collectionNameBox.setFocused(false);
                if (collectionDescriptionBox != null) collectionDescriptionBox.setFocused(false);
                this.setFocused(collectionTagBox);
                return super.mouseClicked(event, doubleClick);
            }

            if (collectionDescriptionBox != null && isInside(mouseX, mouseY, collectionDescriptionBox.getX(), collectionDescriptionBox.getY(), collectionDescriptionBox.getWidth(), collectionDescriptionBox.getHeight())) {
                collectionDescriptionBox.setFocused(true);
                if (collectionNameBox != null) collectionNameBox.setFocused(false);
                if (collectionTagBox != null) collectionTagBox.setFocused(false);
                this.setFocused(collectionDescriptionBox);
                return super.mouseClicked(event, doubleClick);
            }

            this.setFocused(null);
            return true;
        }

        if (editAssetModalOpen) {
            EditAssetModalLayout modal = buildEditAssetModalLayout();
            ArchivAsset asset = getSavedAssetByName(editAssetOriginalName);

            if (isInside(mouseX, mouseY, modal.closeX, modal.closeY, modal.closeSize, modal.closeSize)) {
                closeEditAssetModal();
                return true;
            }

            if (isInside(mouseX, mouseY, modal.cancelX, modal.cancelY, modal.cancelW, modal.buttonH)) {
                closeEditAssetModal();
                return true;
            }

            if (isInside(mouseX, mouseY, modal.saveX, modal.saveY, modal.saveW, modal.buttonH)) {
                saveEditedAsset();
                return true;
            }

            if (asset != null && isInside(mouseX, mouseY, modal.deleteX, modal.deleteY, modal.deleteW, modal.buttonH)) {
                closeEditAssetModal();
                openDeleteConfirm(asset);
                return true;
            }

            if (handleEditAssetDropdownClick(mouseX, mouseY, modal)) {
                return true;
            }

            if (editAssetNameBox != null && isInside(mouseX, mouseY, editAssetNameBox.getX(), editAssetNameBox.getY(), editAssetNameBox.getWidth(), editAssetNameBox.getHeight())) {
                editAssetDropdownOpen = null;
                editAssetNameBox.setFocused(true);
                this.setFocused(editAssetNameBox);
                return super.mouseClicked(event, doubleClick);
            }

            if (isInside(mouseX, mouseY, modal.categoryX, modal.categoryY, modal.categoryW, modal.fieldH)) {
                toggleEditAssetDropdown("category");
                return true;
            }

            if (isInside(mouseX, mouseY, modal.typeX, modal.typeY, modal.typeW, modal.fieldH)) {
                toggleEditAssetDropdown("type");
                return true;
            }

            if (isInside(mouseX, mouseY, modal.versionX, modal.versionY, modal.versionW, modal.fieldH)) {
                toggleEditAssetDropdown("version");
                return true;
            }

            if (isInside(mouseX, mouseY, modal.addVariantX, modal.addVariantY, modal.addVariantW, modal.buttonH)) {
                editAssetDropdownOpen = null;
                editAssetVariantCount = clampInt(editAssetVariantCount + 1, 1, 99);
                libraryActionMessage = "Variants: " + editAssetVariantCount;
                return true;
            }

            if (isInside(mouseX, mouseY, modal.removeVariantX, modal.removeVariantY, modal.removeVariantW, modal.buttonH)) {
                editAssetDropdownOpen = null;
                editAssetVariantCount = clampInt(editAssetVariantCount - 1, 1, 99);
                libraryActionMessage = "Variants: " + editAssetVariantCount;
                return true;
            }

            if (isInside(mouseX, mouseY, modal.panelX, modal.panelY, modal.panelW, modal.panelH)) {
                if (editAssetDropdownOpen != null) {
                    editAssetDropdownOpen = null;
                    return true;
                }

                if (editAssetNameBox != null) {
                    editAssetNameBox.setFocused(false);
                }
                this.setFocused(null);
                return true;
            }

            closeEditAssetModal();
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

        if ("Settings".equals(selectedTopTab)) {
            for (int i = 0; i < settingsSections.length; i++) {
                int currentY = chrome.sidebarItemY + (i * chrome.sidebarItemGap);

                if (isInside(mouseX, mouseY, chrome.sidebarItemX, currentY, chrome.sidebarItemW, chrome.sidebarItemH)) {
                    selectedSettingsSection = settingsSections[i];
                    metadataGroupsScrollbarDragging = false;
                    metadataOptionsScrollbarDragging = false;
                    closeListMenu();
                    return true;
                }
            }

            SettingsLayout settingsLayout = buildSettingsLayout(chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH);

            if ("Metadata".equals(selectedSettingsSection)) {
                updateMetadataScrollLimits(settingsLayout);

                metadataGroupsScrollbarDragging = false;
                metadataOptionsScrollbarDragging = false;

                int groupListX = getMetadataGroupsViewportX(settingsLayout);
                int groupListY = getMetadataGroupsViewportY(settingsLayout);
                int groupListW = getMetadataGroupsViewportW(settingsLayout);
                int groupListH = getMetadataGroupsViewportH(settingsLayout);

                if (isInside(mouseX, mouseY, groupListX, groupListY, groupListW, groupListH)) {
                    for (int i = 0; i < metadataGroups.length; i++) {
                        int groupY = groupListY + i * (settingsLayout.groupItemH + settingsLayout.groupGap) - metadataGroupsScrollOffset;

                        if (isInside(mouseX, mouseY, groupListX, groupY, groupListW, settingsLayout.groupItemH)) {
                            selectedMetadataGroup = metadataGroups[i];
                            metadataOptionsScrollOffset = 0;
                            libraryActionMessage = "Metadata group: " + selectedMetadataGroup;
                            return true;
                        }
                    }
                }

                List<String> options = getSelectedMetadataOptions();
                int optionListX = getMetadataOptionsViewportX(settingsLayout);
                int optionListY = getMetadataOptionsViewportY(settingsLayout);
                int optionListW = getMetadataOptionsViewportW(settingsLayout);
                int optionListH = getMetadataOptionsViewportH(settingsLayout);

                if (isInside(mouseX, mouseY, optionListX, optionListY, optionListW, optionListH)) {
                    for (int i = 0; i < options.size(); i++) {
                        int rowY = optionListY + i * (settingsLayout.listItemH + settingsLayout.listGap) - metadataOptionsScrollOffset;

                        if (isInside(mouseX, mouseY, optionListX, rowY, optionListW, settingsLayout.listItemH)) {
                            setSelectedMetadataIndex(i);
                            libraryActionMessage = "Selected " + getSelectedMetadataSingularLabel() + ": " + options.get(i);
                            return true;
                        }
                    }
                }

                if (isInside(mouseX, mouseY, settingsLayout.addX, settingsLayout.addY, settingsLayout.addW, settingsLayout.buttonH)) {
                    addMetadataOption();
                    return true;
                }

                if (isInside(mouseX, mouseY, settingsLayout.duplicateX, settingsLayout.duplicateY, settingsLayout.duplicateW, settingsLayout.buttonH)) {
                    duplicateSelectedMetadataOption();
                    return true;
                }

                if (isInside(mouseX, mouseY, settingsLayout.removeX, settingsLayout.removeY, settingsLayout.removeW, settingsLayout.buttonH)) {
                    removeSelectedMetadataOption();
                    return true;
                }

                if (isInside(mouseX, mouseY, settingsLayout.resetX, settingsLayout.resetY, settingsLayout.resetW, settingsLayout.buttonH)) {
                    resetMetadataDefaults();
                    metadataGroupsScrollOffset = 0;
                    metadataOptionsScrollOffset = 0;
                    return true;
                }

                if (isInside(mouseX, mouseY, settingsLayout.cancelX, settingsLayout.cancelY, settingsLayout.cancelW, settingsLayout.buttonH)) {
                    libraryActionMessage = "Settings changes kept in current session";
                    return true;
                }

                if (isInside(mouseX, mouseY, settingsLayout.saveX, settingsLayout.saveY, settingsLayout.saveW, settingsLayout.buttonH)) {
                    libraryActionMessage = "Settings ready - persistence will be added with JSON later";
                    return true;
                }
            }

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
                ImportDetailsFormLayout detailsForm = buildImportDetailsFormLayout(layout);

                if (isInside(mouseX, mouseY, detailsForm.categoryX, detailsForm.categoryY, detailsForm.fieldW2, detailsForm.fieldH)) {
                    mockDetailsFilled = true;
                    mockAssetSaved = false;
                    importSelectedMacroCategory = cycleOption(macroCategories, importSelectedMacroCategory);
                    libraryActionMessage = "Import category: " + importSelectedMacroCategory;
                    return true;
                }

                if (isInside(mouseX, mouseY, detailsForm.typeX, detailsForm.typeY, detailsForm.fieldW1, detailsForm.fieldH)) {
                    mockDetailsFilled = true;
                    mockAssetSaved = false;
                    importSelectedType = cycleOption(assetTypes, importSelectedType);
                    libraryActionMessage = "Import type: " + importSelectedType;
                    return true;
                }

                if (isInside(mouseX, mouseY, detailsForm.versionX, detailsForm.versionY, detailsForm.fieldW2, detailsForm.fieldH)) {
                    mockDetailsFilled = true;
                    mockAssetSaved = false;
                    importSelectedVersion = cycleOption(minecraftVersions, importSelectedVersion);
                    libraryActionMessage = "Import version: " + importSelectedVersion;
                    return true;
                }

                if (isInside(mouseX, mouseY, detailsForm.variantsX, detailsForm.variantsY, detailsForm.fieldW3, detailsForm.fieldH)) {
                    mockDetailsFilled = true;
                    mockAssetSaved = false;

                    int halfW = detailsForm.fieldW3 / 2;
                    if (mouseX < detailsForm.variantsX + halfW) {
                        importSelectedVariantCount = clampInt(importSelectedVariantCount - 1, 1, 99);
                    } else {
                        importSelectedVariantCount = clampInt(importSelectedVariantCount + 1, 1, 99);
                    }

                    libraryActionMessage = "Import variants: " + importSelectedVariantCount;
                    return true;
                }

                if (isInside(mouseX, mouseY, layout.detailsActionX, layout.detailsActionY, layout.detailsActionW, layout.detailsActionH)) {
                    mockDetailsFilled = !mockDetailsFilled;
                    mockAssetSaved = false;

                    if (mockDetailsFilled) {
                        applyImportPresetMetadata();
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
            for (int i = 0; i < getBrowseCategoryCount(); i++) {
                int currentY = chrome.sidebarItemY + (i * chrome.sidebarItemGap);

                if (isInside(mouseX, mouseY, chrome.sidebarItemX, currentY, chrome.sidebarItemW, chrome.sidebarItemH)) {
                    selectedCategory = getBrowseCategoryAt(i);
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

                    if (handleAssetContextMenuClick(mouseX, browseMouseY, asset, rowLayout.menuX, rowLayout.menuY, rowLayout.menuW)) {
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

                    boolean hovered = isInside(mouseX, browseMouseY, layout.listX, rowY, layout.listW, layout.rowH);
                    if (hovered) {
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

                    if (handleAssetContextMenuClick(mouseX, browseMouseY, asset, cardLayout.menuX, cardLayout.menuY, cardLayout.menuW)) {
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

                    boolean hovered = isInside(mouseX, browseMouseY, cardX, cardY, layout.cardW, layout.cardH);
                    if (hovered) {
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
                openCreateCollectionModal();
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
            if (!isInside(mouseX, mouseY, chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH)) {
                return super.mouseClicked(event, doubleClick);
            }

            List<ArchivAsset> visibleAssets = getVisibleAssets();

            int innerPadding = 18;
            int scrollbarReserve = 16;
            int usableContentW = chrome.contentW - scrollbarReserve;

            int titleX = chrome.contentX + innerPadding;

            int titleBaseY = chrome.contentY + 16;
            int statsBaseY = titleBaseY + 40;
            int statH = 68;

            int quickBaseY = statsBaseY + statH + 18;
            int quickButtonBaseY = quickBaseY + 14;
            int quickButtonH = 30;

            int sectionBaseY = quickButtonBaseY + quickButtonH + 20;

            List<ArchivAsset> recentAssets = getRecentAssets(4);

            boolean allAssetsSection = "All Assets".equals(selectedMyAssetsSection);
            boolean collectionsSection = "Collections".equals(selectedMyAssetsSection);

            int importedTitleBaseY = sectionBaseY;

            if (allAssetsSection) {
                importedTitleBaseY = sectionBaseY;

                if (!collectionEntries.isEmpty()) {
                    int collectionBaseY = sectionBaseY + 18;
                    int collectionH = 88;
                    importedTitleBaseY = collectionBaseY + collectionH + 20;
                }

                if (!recentAssets.isEmpty()) {
                    int recentTitleBaseY = importedTitleBaseY;
                    int recentCardBaseY = recentTitleBaseY + 18;
                    int recentH = 62;

                    importedTitleBaseY = recentCardBaseY + recentH + 20;
                }
            }

            if (collectionsSection) {
                int collectionBaseY = sectionBaseY + 34;
                int collectionH = 94;
                int collectionInfoGap = 16;
                int collectionInfoH = 54;

                importedTitleBaseY = collectionBaseY + collectionH + collectionInfoGap + collectionInfoH + 20;
            }

            int scrollRenderY = -myAssetsScrollOffset;
            int importedTitleY = importedTitleBaseY + scrollRenderY;

            if (allAssetsSection || collectionsSection) {
                int collectionSectionBaseY = quickButtonBaseY + quickButtonH + 20;

                int collectionY = collectionsSection
                        ? collectionSectionBaseY + 34 + scrollRenderY
                        : collectionSectionBaseY + 18 + scrollRenderY;

                int collectionGap = 14;
                int collectionW = (usableContentW - (innerPadding * 2) - (collectionGap * 2)) / 3;
                int collectionH = collectionsSection ? 94 : 88;

                for (int i = 0; i < collectionEntries.size(); i++) {
                    CollectionEntry entry = collectionEntries.get(i);

                    int cardX = titleX + i * (collectionW + collectionGap);

                    if (collectionsSection) {
                        cardX -= myAssetsCollectionsScrollX;
                    }

                    if (isInside(mouseX, mouseY, cardX, collectionY, collectionW, collectionH)) {
                        boolean wasCollectionsSection = collectionsSection;

                        selectedCollectionName = entry.getName();
                        selectedMyAssetsSection = "Collections";
                        libraryActionMessage = "Opened collection: " + entry.getName();

                        if (!wasCollectionsSection) {
                            resetMyAssetsScroll();
                        }

                        return true;
                    }
                }
            }

            CardGridLayout layout = buildMyAssetsImportedGridLayout(
                    chrome.contentX,
                    chrome.contentW,
                    importedTitleY,
                    visibleAssets.size()
            );

            for (int i = 0; i < visibleAssets.size(); i++) {
                ArchivAsset asset = visibleAssets.get(i);

                int column = i % layout.columns;
                int row = i / layout.columns;

                int cardX = layout.cardsAreaX + column * (layout.cardW + layout.cardsGap);
                int cardY = layout.cardsAreaY + row * (layout.cardH + layout.rowGap);

                AssetCardLayout cardLayout = buildAssetCardLayout(cardX, cardY, layout.cardW, layout.cardH);

                boolean savedAsset = isSavedAsset(asset);

                if (handleAssetContextMenuClick(mouseX, mouseY, asset, cardLayout.menuX, cardLayout.menuY, cardLayout.menuW)) {
                    return true;
                }

                if (savedAsset && isInside(mouseX, mouseY, cardLayout.menuDotsX - 4, cardLayout.menuDotsY - 4, 14, 18)) {
                    if (isListMenuOpenFor(asset)) {
                        closeListMenu();
                    } else {
                        openListMenu(asset);
                    }
                    return true;
                }

                if (isInside(mouseX, mouseY, cardLayout.favoriteX, cardLayout.favoriteY, cardLayout.favoriteW, cardLayout.favoriteH)) {
                    closeListMenu();
                    toggleAssetFavorite(asset);
                    syncLibrarySelectionWithVisibleAssets();
                    return true;
                }

                boolean hovered = isInside(mouseX, mouseY, cardX, cardY, layout.cardW, layout.cardH);
                if (hovered) {
                    if (isInside(mouseX, mouseY, cardLayout.overlayX, cardLayout.loadY, cardLayout.overlayW, cardLayout.loadH)) {
                        loadAsset(asset);
                        return true;
                    }

                    if (isInside(mouseX, mouseY, cardLayout.overlayX, cardLayout.detailsY, cardLayout.overlayW, cardLayout.detailsH)) {
                        openAssetDetails(asset);
                        return true;
                    }
                }

                if (isInside(mouseX, mouseY, cardX, cardY, layout.cardW, layout.cardH)) {
                    closeListMenu();
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

        if ("Settings".equals(selectedTopTab) && "Metadata".equals(selectedSettingsSection)) {
            SettingsLayout settingsLayout = buildSettingsLayout(chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH);
            updateMetadataScrollLimits(settingsLayout);

            int scrollStep = 24;

            if (isInside(mouseX, mouseY, getMetadataGroupsViewportX(settingsLayout), getMetadataGroupsViewportY(settingsLayout), getMetadataGroupsViewportW(settingsLayout), getMetadataGroupsViewportH(settingsLayout))) {
                metadataGroupsScrollOffset = clampInt(
                        metadataGroupsScrollOffset - ((int) verticalAmount * scrollStep),
                        0,
                        metadataGroupsMaxScroll
                );
                return true;
            }

            if (isInside(mouseX, mouseY, getMetadataOptionsViewportX(settingsLayout), getMetadataOptionsViewportY(settingsLayout), getMetadataOptionsViewportW(settingsLayout), getMetadataOptionsViewportH(settingsLayout))) {
                metadataOptionsScrollOffset = clampInt(
                        metadataOptionsScrollOffset - ((int) verticalAmount * scrollStep),
                        0,
                        metadataOptionsMaxScroll
                );
                return true;
            }
        }

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

            if (isShiftHeld() && ("All Assets".equals(selectedMyAssetsSection) || "Collections".equals(selectedMyAssetsSection))) {
                int innerPadding = 18;
                int titleBaseY = chrome.contentY + 16;
                int statsBaseY = titleBaseY + 40;
                int statH = 68;
                int quickBaseY = statsBaseY + statH + 18;
                int quickButtonBaseY = quickBaseY + 14;
                int quickButtonH = 30;
                int sectionBaseY = quickButtonBaseY + quickButtonH + 20;

                int collectionY = "Collections".equals(selectedMyAssetsSection)
                        ? sectionBaseY + 34 - myAssetsScrollOffset
                        : sectionBaseY + 18 - myAssetsScrollOffset;

                int collectionH = "Collections".equals(selectedMyAssetsSection) ? 94 : 88;
                int collectionViewportX = chrome.contentX + innerPadding;
                int collectionViewportW = (chrome.contentW - 16) - (innerPadding * 2);

                if (isInside(mouseX, mouseY, collectionViewportX, collectionY, collectionViewportW, collectionH)) {
                    int horizontalStep = 22;
                    myAssetsCollectionsScrollX = clampInt(
                            myAssetsCollectionsScrollX - ((int) verticalAmount * horizontalStep),
                            0,
                            myAssetsCollectionsMaxScrollX
                    );
                    return true;
                }
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
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        ScreenChromeLayout chrome = buildChromeLayout();

        if (metadataGroupsScrollbarDragging && "Settings".equals(selectedTopTab) && "Metadata".equals(selectedSettingsSection)) {
            SettingsLayout settingsLayout = buildSettingsLayout(chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH);
            updateMetadataScrollLimits(settingsLayout);
            ScrollbarLayout scrollbar = buildMetadataGroupsScrollbarLayout(settingsLayout);

            int desiredThumbY = (int) event.y() - metadataGroupsScrollbarDragOffset;
            int clampedThumbY = clampInt(desiredThumbY, scrollbar.trackY, scrollbar.trackY + scrollbar.trackH - scrollbar.thumbH);
            int movableTrack = Math.max(1, scrollbar.trackH - scrollbar.thumbH);
            metadataGroupsScrollOffset = ((clampedThumbY - scrollbar.trackY) * metadataGroupsMaxScroll) / movableTrack;
            return true;
        }

        if (metadataOptionsScrollbarDragging && "Settings".equals(selectedTopTab) && "Metadata".equals(selectedSettingsSection)) {
            SettingsLayout settingsLayout = buildSettingsLayout(chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH);
            updateMetadataScrollLimits(settingsLayout);
            ScrollbarLayout scrollbar = buildMetadataOptionsScrollbarLayout(settingsLayout);

            int desiredThumbY = (int) event.y() - metadataOptionsScrollbarDragOffset;
            int clampedThumbY = clampInt(desiredThumbY, scrollbar.trackY, scrollbar.trackY + scrollbar.trackH - scrollbar.thumbH);
            int movableTrack = Math.max(1, scrollbar.trackH - scrollbar.thumbH);
            metadataOptionsScrollOffset = ((clampedThumbY - scrollbar.trackY) * metadataOptionsMaxScroll) / movableTrack;
            return true;
        }

        if (browseScrollbarDragging && "Browse".equals(selectedTopTab)) {
            ScrollbarLayout scrollbar = buildBrowseScrollbarLayout(
                    chrome.contentX,
                    chrome.contentY,
                    chrome.contentW,
                    chrome.contentH
            );

            int desiredThumbY = (int) event.y() - browseScrollbarDragOffset;
            int clampedThumbY = clampInt(
                    desiredThumbY,
                    scrollbar.trackY,
                    scrollbar.trackY + scrollbar.trackH - scrollbar.thumbH
            );

            int movableTrack = scrollbar.trackH - scrollbar.thumbH;
            if (movableTrack > 0) {
                browseScrollOffset = ((clampedThumbY - scrollbar.trackY) * browseMaxScroll) / movableTrack;
            } else {
                browseScrollOffset = 0;
            }

            return true;
        }

        if (myAssetsScrollbarDragging && "My Assets".equals(selectedTopTab)) {
            ScrollbarLayout scrollbar = buildMyAssetsScrollbarLayout(
                    chrome.contentX,
                    chrome.contentY,
                    chrome.contentW,
                    chrome.contentH
            );

            int desiredThumbY = (int) event.y() - myAssetsScrollbarDragOffset;
            int clampedThumbY = clampInt(
                    desiredThumbY,
                    scrollbar.trackY,
                    scrollbar.trackY + scrollbar.trackH - scrollbar.thumbH
            );

            int movableTrack = scrollbar.trackH - scrollbar.thumbH;
            if (movableTrack > 0) {
                myAssetsScrollOffset = ((clampedThumbY - scrollbar.trackY) * myAssetsMaxScroll) / movableTrack;
            } else {
                myAssetsScrollOffset = 0;
            }

            return true;
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            if (metadataGroupsScrollbarDragging) {
                metadataGroupsScrollbarDragging = false;
                return true;
            }

            if (metadataOptionsScrollbarDragging) {
                metadataOptionsScrollbarDragging = false;
                return true;
            }

            if (browseScrollbarDragging) {
                browseScrollbarDragging = false;
                return true;
            }

            if (myAssetsScrollbarDragging) {
                myAssetsScrollbarDragging = false;
                return true;
            }
        }

        return super.mouseReleased(event);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.fill(0, 0, this.width, this.height, COLOR_BACKGROUND);

        ScreenChromeLayout chrome = buildChromeLayout();

        boolean browseActive = "Browse".equals(selectedTopTab);
        boolean myAssetsActive = "My Assets".equals(selectedTopTab);
        boolean libraryTabActive = browseActive || myAssetsActive;

        if (browseSearchBox != null) {
            boolean browseSearchEnabled = browseActive && !isBrowseOverlayOpen();

            browseSearchBox.visible = browseSearchEnabled;
            browseSearchBox.active = browseSearchEnabled;

            if (!browseSearchEnabled) {
                browseSearchBox.setFocused(false);

                if (this.getFocused() == browseSearchBox) {
                    this.setFocused(null);
                }
            }
        }

        boolean collectionModalActive = createCollectionModalOpen;

        if (collectionNameBox != null) {
            collectionNameBox.visible = collectionModalActive;
            collectionNameBox.active = collectionModalActive;

            if (!collectionModalActive) {
                collectionNameBox.setFocused(false);
            }
        }

        if (collectionTagBox != null) {
            collectionTagBox.visible = collectionModalActive;
            collectionTagBox.active = collectionModalActive;

            if (!collectionModalActive) {
                collectionTagBox.setFocused(false);
            }
        }

        if (collectionDescriptionBox != null) {
            collectionDescriptionBox.visible = collectionModalActive;
            collectionDescriptionBox.active = collectionModalActive;

            if (!collectionModalActive) {
                collectionDescriptionBox.setFocused(false);
            }
        }

        boolean editAssetModalActive = editAssetModalOpen;

        if (editAssetNameBox != null) {
            editAssetNameBox.visible = editAssetModalActive;
            editAssetNameBox.active = editAssetModalActive;

            if (!editAssetModalActive) {
                editAssetNameBox.setFocused(false);
            }
        }

        if (editAssetCategoryBox != null) {
            editAssetCategoryBox.visible = false;
            editAssetCategoryBox.active = false;
            editAssetCategoryBox.setFocused(false);
        }

        if (editAssetTypeBox != null) {
            editAssetTypeBox.visible = false;
            editAssetTypeBox.active = false;
            editAssetTypeBox.setFocused(false);
        }

        if (editAssetVersionBox != null) {
            editAssetVersionBox.visible = false;
            editAssetVersionBox.active = false;
            editAssetVersionBox.setFocused(false);
        }

        if (editAssetVariantsBox != null) {
            editAssetVariantsBox.visible = false;
            editAssetVariantsBox.active = false;
            editAssetVariantsBox.setFocused(false);
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
            for (int i = 0; i < getBrowseCategoryCount(); i++) {
                String category = getBrowseCategoryAt(i);
                boolean active = category.equals(selectedCategory);
                drawSidebarItem(guiGraphics, category, chrome.sidebarItemX, categoryY, chrome.sidebarItemW, chrome.sidebarItemH, active);
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
        } else if ("Settings".equals(selectedTopTab)) {
            guiGraphics.drawString(this.font, "SETTINGS", chrome.rootX + 16, chrome.bodyY + 14, COLOR_TEXT_DIM);

            int sectionY = chrome.sidebarItemY;
            for (String section : settingsSections) {
                boolean active = section.equals(selectedSettingsSection);
                drawSidebarItem(guiGraphics, section, chrome.sidebarItemX, sectionY, chrome.sidebarItemW, chrome.sidebarItemH, active);
                sectionY += chrome.sidebarItemGap;
            }
        } else if (!"Import".equals(selectedTopTab)) {
            guiGraphics.drawString(this.font, "SECTION", chrome.rootX + 16, chrome.bodyY + 14, COLOR_TEXT_DIM);
            guiGraphics.drawString(this.font, selectedTopTab, chrome.rootX + 16, chrome.bodyY + 34, COLOR_TEXT);
        }

        if (browseActive) {
            drawBrowseTab(guiGraphics, chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH, visibleAssets, mouseX, mouseY);
        } else if (myAssetsActive) {
            drawMyAssetsTab(guiGraphics, chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH, visibleAssets, mouseX, mouseY);
        } else if ("Import".equals(selectedTopTab)) {
            drawImportTab(guiGraphics, chrome.rootX, chrome.rootY, chrome.rootW, chrome.rootH, chrome.bodyY, chrome.bodyH, chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH);
        } else if ("Settings".equals(selectedTopTab)) {
            drawSettingsTab(guiGraphics, chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH);
        } else {
            drawTabPlaceholder(guiGraphics, chrome.contentX, chrome.contentY, chrome.contentW, chrome.contentH, selectedTopTab);
        }

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

        if (createCollectionModalOpen) {
            drawCreateCollectionModal(guiGraphics);
        }

        if (editAssetModalOpen) {
            drawEditAssetModal(guiGraphics);
        }

        super.render(guiGraphics, mouseX, mouseY, delta);

        drawAssetDetailsPanel(guiGraphics, chrome.rootX, chrome.rootY, chrome.rootW, chrome.bodyY);
        drawDeleteConfirmModal(guiGraphics);
    }

    private void drawCreateCollectionModal(GuiGraphics guiGraphics) {
        if (!createCollectionModalOpen) {
            return;
        }

        CreateCollectionModalLayout modal = buildCreateCollectionModalLayout();

        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);
        drawPanel(guiGraphics, modal.panelX, modal.panelY, modal.panelW, modal.panelH, COLOR_PANEL_ALT, COLOR_BORDER_ACTIVE);

        guiGraphics.drawString(this.font, "Create Collection", modal.panelX + 16, modal.panelY + 16, COLOR_TEXT);

        drawPanel(guiGraphics, modal.closeX, modal.closeY, modal.closeSize, modal.closeSize, COLOR_PANEL, COLOR_BORDER);
        guiGraphics.drawString(this.font, "X", modal.closeX + 6, modal.closeY + 6, COLOR_TEXT);

        guiGraphics.drawString(this.font, "Name", modal.fieldX, modal.nameY - 12, COLOR_TEXT_DIM);
        drawPanel(guiGraphics, modal.fieldX, modal.nameY, modal.fieldW, modal.fieldH, COLOR_PANEL, COLOR_BORDER);

        guiGraphics.drawString(this.font, "Tag", modal.fieldX, modal.tagY - 12, COLOR_TEXT_DIM);
        drawPanel(guiGraphics, modal.fieldX, modal.tagY, modal.fieldW, modal.fieldH, COLOR_PANEL, COLOR_BORDER);

        guiGraphics.drawString(this.font, "Description", modal.fieldX, modal.descriptionY - 12, COLOR_TEXT_DIM);
        drawPanel(guiGraphics, modal.fieldX, modal.descriptionY, modal.fieldW, modal.fieldH, COLOR_PANEL, COLOR_BORDER);

        drawButtonBox(guiGraphics, "Cancel", modal.cancelX, modal.cancelY, modal.cancelW, modal.buttonH, false);
        drawButtonBoxState(guiGraphics, "Create", modal.createX, modal.createY, modal.createW, modal.buttonH, true, isCreateCollectionFormValid());
    }

    private void drawEditSectionHeader(GuiGraphics guiGraphics, String number, String title, int x, int y, int width) {
        drawPanel(guiGraphics, x, y, 16, 16, 0xFF10243A, COLOR_BORDER_ACTIVE);
        guiGraphics.drawString(this.font, number, x + 5, y + 5, COLOR_BORDER_ACTIVE);
        guiGraphics.drawString(this.font, title, x + 24, y + 5, COLOR_BORDER_ACTIVE);
        guiGraphics.fill(x + 24 + this.font.width(title) + 8, y + 8, x + width - 10, y + 9, 0x44223854);
    }

    private void drawEditLabel(GuiGraphics guiGraphics, String label, int x, int y) {
        guiGraphics.drawString(this.font, label, x, y, COLOR_TEXT_DIM);
    }

    private void drawEditStaticField(GuiGraphics guiGraphics, String value, int x, int y, int width, int height) {
        drawPanel(guiGraphics, x, y, width, height, COLOR_PANEL, COLOR_BORDER);
        drawClippedString(guiGraphics, value, x + 10, y + (height - this.font.lineHeight) / 2, width - 20, COLOR_TEXT);
    }

    private void drawEditSelectorField(GuiGraphics guiGraphics, String value, String dropdownName, int x, int y, int width, int height) {
        boolean open = dropdownName.equals(editAssetDropdownOpen);
        int border = open ? COLOR_BORDER_ACTIVE : COLOR_BORDER;
        drawPanel(guiGraphics, x, y, width, height, COLOR_PANEL, border);

        String icon = switch (dropdownName) {
            case "type" -> getMetadataOptionIcon(value, getSelectedIndex(assetTypes, value));
            case "version" -> "▣";
            default -> getMetadataOptionIcon(value, getSelectedIndex(macroCategories, value));
        };

        guiGraphics.drawString(this.font, icon, x + 10, y + (height - this.font.lineHeight) / 2, open ? COLOR_BORDER_ACTIVE : COLOR_TEXT_DIM);
        drawClippedString(guiGraphics, value, x + 26, y + (height - this.font.lineHeight) / 2, width - 48, COLOR_TEXT);
        guiGraphics.drawString(this.font, open ? "⌃" : "⌄", x + width - 18, y + (height - this.font.lineHeight) / 2, open ? COLOR_BORDER_ACTIVE : COLOR_TEXT_DIM);
    }

    private void drawEditDropdownList(GuiGraphics guiGraphics, EditAssetModalLayout modal) {
        if (editAssetDropdownOpen == null) {
            return;
        }

        List<String> options = getEditAssetDropdownOptions(editAssetDropdownOpen);
        if (options.isEmpty()) {
            return;
        }

        String currentValue = switch (editAssetDropdownOpen) {
            case "type" -> editAssetSelectedType;
            case "version" -> editAssetSelectedVersion;
            default -> editAssetSelectedCategory;
        };

        int rowH = 24;
        int x = getEditAssetDropdownX(modal, editAssetDropdownOpen);
        int y = getEditAssetDropdownY(modal, editAssetDropdownOpen);
        int w = getEditAssetDropdownW(modal, editAssetDropdownOpen);
        int h = options.size() * rowH;

        drawPanel(guiGraphics, x, y, w, h, 0xFF0B1524, COLOR_BORDER_ACTIVE);

        for (int i = 0; i < options.size(); i++) {
            String option = options.get(i);
            int rowY = y + i * rowH;
            boolean selected = option.equals(currentValue);

            if (selected) {
                guiGraphics.fill(x + 1, rowY + 1, x + w - 1, rowY + rowH, 0xFF173659);
            }

            if (i > 0) {
                guiGraphics.fill(x + 1, rowY, x + w - 1, rowY + 1, COLOR_BORDER);
            }

            String icon = "version".equals(editAssetDropdownOpen)
                    ? "▣"
                    : getMetadataOptionIcon(option, i);
            int textY = rowY + (rowH - this.font.lineHeight) / 2;
            guiGraphics.drawString(this.font, icon, x + 10, textY, selected ? COLOR_BORDER_ACTIVE : COLOR_TEXT_DIM);
            drawClippedString(guiGraphics, option, x + 28, textY, w - 56, selected ? COLOR_TEXT : COLOR_TEXT_DIM);

            if (selected) {
                guiGraphics.drawString(this.font, "✓", x + w - 18, textY, COLOR_SUCCESS);
            }
        }
    }

    private void drawEditVariantRow(GuiGraphics guiGraphics, String name, String status, int x, int y, int width, int height, boolean active) {
        int background = active ? 0xFF122A3F : COLOR_PANEL;
        int border = active ? COLOR_BORDER_ACTIVE : COLOR_BORDER;
        drawPanel(guiGraphics, x, y, width, height, background, border);

        int centerY = y + (height - this.font.lineHeight) / 2;
        drawDragHandle(guiGraphics, x + 8, y + (height - 12) / 2, active ? COLOR_BORDER_ACTIVE : COLOR_TEXT_DIM);

        int iconSize = 17;
        int iconX = x + 28;
        int iconY = y + (height - iconSize) / 2;
        drawPanel(guiGraphics, iconX, iconY, iconSize, iconSize, 0xFF10243A, COLOR_BORDER);
        guiGraphics.drawString(this.font, "▣", iconX + 5, iconY + 5, COLOR_TEXT_DIM);

        int checkW = 18;
        int editW = 18;
        int rightX = x + width - checkW - 4;
        drawPanel(guiGraphics, rightX, y + 3, checkW, height - 6, 0xFF2F9BE6, 0xFF73C8FF);
        guiGraphics.drawString(this.font, "✓", rightX + 5, centerY, 0xFFFFFFFF);

        int editX = rightX - editW - 4;
        drawPanel(guiGraphics, editX, y + 3, editW, height - 6, COLOR_PANEL_ALT, COLOR_BORDER);
        guiGraphics.drawString(this.font, "✎", editX + 5, centerY, COLOR_TEXT_DIM);

        int statusW = active ? 54 : 0;
        if (active) {
            int statusX = editX - statusW - 8;
            drawPanel(guiGraphics, statusX, y + 4, statusW, height - 8, 0xFF153B29, 0xFF226B45);
            guiGraphics.drawString(this.font, status, statusX + 8, centerY, COLOR_SUCCESS);
        }

        int nameX = iconX + iconSize + 10;
        int nameMaxW = active
                ? Math.max(40, editX - statusW - 18 - nameX)
                : Math.max(40, editX - 10 - nameX);
        drawClippedString(guiGraphics, name, nameX, centerY, nameMaxW, COLOR_TEXT);
    }

    private void drawEditFileField(GuiGraphics guiGraphics, String label, String value, int x, int y, int width, int height) {
        guiGraphics.drawString(this.font, label, x, y - 14, COLOR_TEXT_DIM);
        drawPanel(guiGraphics, x, y, width, height, COLOR_PANEL, COLOR_BORDER);

        int centerY = y + (height - this.font.lineHeight) / 2;
        guiGraphics.drawString(this.font, "□", x + 8, centerY, COLOR_TEXT_DIM);
        drawClippedString(guiGraphics, value, x + 24, centerY, width - 56, COLOR_TEXT);

        drawPanel(guiGraphics, x + width - 28, y, 28, height, COLOR_PANEL_ALT, COLOR_BORDER);
        guiGraphics.drawString(this.font, "...", x + width - 20, centerY, COLOR_TEXT_DIM);
    }

    private void drawEditPreviewPanel(GuiGraphics guiGraphics, EditAssetModalLayout modal, ArchivAsset asset) {
        drawPanel(guiGraphics, modal.previewPanelX, modal.previewPanelY, modal.previewPanelW, modal.previewPanelH, COLOR_PANEL, COLOR_BORDER);
        guiGraphics.drawString(this.font, "◉ PREVIEW", modal.previewPanelX + 12, modal.previewPanelY + 12, COLOR_BORDER_ACTIVE);

        int previewColor = asset == null ? MOCK_NO_PREVIEW_IMAGE_COLOR : asset.getPreviewColor();
        drawPanel(guiGraphics, modal.previewX - 1, modal.previewY - 1, modal.previewW + 2, modal.previewH + 2, COLOR_ROOT, COLOR_BORDER);
        guiGraphics.fill(modal.previewX, modal.previewY, modal.previewX + modal.previewW, modal.previewY + modal.previewH, previewColor);
        String previewText = "PREVIEW";
        guiGraphics.drawString(this.font, previewText, modal.previewX + (modal.previewW - this.font.width(previewText)) / 2, modal.previewY + modal.previewH / 2 - 4, COLOR_TEXT);

        int metaY = modal.previewY + modal.previewH + 10;
        String type = getSafeOption(assetTypes, editAssetSelectedType);
        drawChip(guiGraphics, type, modal.previewPanelX + 12, metaY, getChipWidth(type), 18, getChipColorForEditedAsset(type, asset == null ? 0xFF2D9CDB : asset.getChipColor()));

        int dotX = modal.previewPanelX + modal.previewPanelW - 90;
        drawDot(guiGraphics, dotX, metaY + 5, 0xFF8DA1B8);
        drawDot(guiGraphics, dotX + 14, metaY + 5, 0xFF5D6875);
        drawDot(guiGraphics, dotX + 28, metaY + 5, 0xFF2E7B45);
        drawDot(guiGraphics, dotX + 42, metaY + 5, 0xFF8A733D);
        guiGraphics.drawString(this.font, "+" + Math.max(0, editAssetVariantCount - 1), dotX + 58, metaY + 4, COLOR_TEXT_DIM);

        int lineY = metaY + 32;
        guiGraphics.fill(modal.previewPanelX + 12, lineY - 8, modal.previewPanelX + modal.previewPanelW - 12, lineY - 7, COLOR_BORDER);

        guiGraphics.drawString(this.font, "□  .schem", modal.previewPanelX + 16, lineY, COLOR_TEXT_DIM);
        guiGraphics.drawString(this.font, "▣  " + editAssetSelectedVersion, modal.previewPanelX + 112, lineY, COLOR_TEXT_DIM);
        guiGraphics.drawString(this.font, getMetadataOptionIcon(editAssetSelectedCategory, getSelectedIndex(macroCategories, editAssetSelectedCategory)) + "  " + editAssetSelectedCategory, modal.previewPanelX + 16, lineY + 22, COLOR_TEXT_DIM);
        guiGraphics.drawString(this.font, "▤  " + editAssetVariantCount + " variants", modal.previewPanelX + 112, lineY + 22, COLOR_TEXT_DIM);

        int infoBoxY = lineY + 50;
        int availableInfoH = modal.previewPanelY + modal.previewPanelH - infoBoxY - 12;
        int infoBoxH = Math.max(42, availableInfoH);
        drawPanel(guiGraphics, modal.previewPanelX + 12, infoBoxY, modal.previewPanelW - 24, infoBoxH, COLOR_ROOT, COLOR_BORDER);

        guiGraphics.drawString(this.font, "File Size", modal.previewPanelX + 22, infoBoxY + 12, COLOR_TEXT_DIM);
        guiGraphics.drawString(this.font, mockStructureFileSize, modal.previewPanelX + modal.previewPanelW - 78, infoBoxY + 12, COLOR_TEXT);

        if (infoBoxH >= 54) {
            guiGraphics.fill(modal.previewPanelX + 16, infoBoxY + 28, modal.previewPanelX + modal.previewPanelW - 16, infoBoxY + 29, COLOR_BORDER);
            guiGraphics.drawString(this.font, "Updated", modal.previewPanelX + 22, infoBoxY + 38, COLOR_TEXT_DIM);
            guiGraphics.drawString(this.font, "Today", modal.previewPanelX + modal.previewPanelW - 58, infoBoxY + 38, COLOR_TEXT);
        }

        if (infoBoxH >= 76) {
            guiGraphics.fill(modal.previewPanelX + 16, infoBoxY + 56, modal.previewPanelX + modal.previewPanelW - 16, infoBoxY + 57, COLOR_BORDER);
            guiGraphics.drawString(this.font, "Status", modal.previewPanelX + 22, infoBoxY + 66, COLOR_TEXT_DIM);
            guiGraphics.drawString(this.font, "• Ready", modal.previewPanelX + modal.previewPanelW - 62, infoBoxY + 66, COLOR_SUCCESS);
        } else {
            guiGraphics.drawString(this.font, "• Ready", modal.previewPanelX + modal.previewPanelW - 62, infoBoxY + infoBoxH - 16, COLOR_SUCCESS);
        }
    }

    private void drawEditAssetModal(GuiGraphics guiGraphics) {
        if (!editAssetModalOpen) {
            return;
        }

        EditAssetModalLayout modal = buildEditAssetModalLayout();
        ArchivAsset asset = getSavedAssetByName(editAssetOriginalName);

        guiGraphics.fill(0, 0, this.width, this.height, 0xAA000000);
        drawPanel(guiGraphics, modal.panelX, modal.panelY, modal.panelW, modal.panelH, 0xF00B1624, COLOR_BORDER_ACTIVE);

        drawPanel(guiGraphics, modal.headerIconX, modal.headerIconY, modal.headerIconSize, modal.headerIconSize, COLOR_PANEL, COLOR_BORDER_ACTIVE);
        guiGraphics.drawString(this.font, "▣", modal.headerIconX + 10, modal.headerIconY + 12, COLOR_BORDER_ACTIVE);

        guiGraphics.drawString(this.font, "Edit Asset", modal.headerIconX + modal.headerIconSize + 14, modal.panelY + 18, COLOR_TEXT);
        String subtitle = asset == null ? "Editing saved asset metadata." : "Editing: " + asset.getName();
        guiGraphics.drawString(this.font, fitTextToWidth(subtitle, modal.panelW - 120), modal.headerIconX + modal.headerIconSize + 14, modal.panelY + 34, COLOR_TEXT_DIM);

        drawPanel(guiGraphics, modal.closeX, modal.closeY, modal.closeSize, modal.closeSize, COLOR_PANEL, COLOR_BORDER);
        guiGraphics.drawString(this.font, "X", modal.closeX + 7, modal.closeY + 7, COLOR_TEXT);

        drawPanel(guiGraphics, modal.basicX, modal.basicY, modal.basicW, modal.basicH, 0xDD0F1B2D, COLOR_BORDER);
        drawEditSectionHeader(guiGraphics, "1", "BASIC INFORMATION", modal.basicX + 12, modal.basicY + 12, modal.basicW - 24);

        drawEditLabel(guiGraphics, "Asset Name", modal.nameX, modal.nameY - 12);
        drawPanel(guiGraphics, modal.nameX, modal.nameY, modal.nameW, modal.fieldH, COLOR_PANEL, COLOR_BORDER);
        guiGraphics.drawString(this.font, "□", modal.nameX + modal.nameW - 18, modal.nameY + 7, COLOR_TEXT_DIM);

        drawEditLabel(guiGraphics, "Macro Category", modal.categoryX, modal.categoryY - 12);
        drawEditSelectorField(guiGraphics, editAssetSelectedCategory, "category", modal.categoryX, modal.categoryY, modal.categoryW, modal.fieldH);

        drawEditLabel(guiGraphics, "Type", modal.typeX, modal.typeY - 12);
        drawEditSelectorField(guiGraphics, editAssetSelectedType, "type", modal.typeX, modal.typeY, modal.typeW, modal.fieldH);

        drawEditLabel(guiGraphics, "Minecraft Version", modal.versionX, modal.versionY - 12);
        drawEditSelectorField(guiGraphics, editAssetSelectedVersion, "version", modal.versionX, modal.versionY, modal.versionW, modal.fieldH);

        drawEditLabel(guiGraphics, "Author", modal.authorX, modal.authorY - 12);
        drawEditStaticField(guiGraphics, "BuilderX", modal.authorX, modal.authorY, modal.authorW, modal.fieldH);

        drawEditLabel(guiGraphics, "Tags", modal.tagsX, modal.tagsY - 12);
        drawPanel(guiGraphics, modal.tagsX, modal.tagsY, modal.tagsW, modal.fieldH, COLOR_PANEL, COLOR_BORDER);
        int tagX = modal.tagsX + 8;
        String[] tags = {"tower", "medieval", "build", "stone"};
        for (String tag : tags) {
            int tagW = this.font.width(tag) + 20;
            if (tagX + tagW > modal.tagsX + modal.tagsW - 58) {
                break;
            }
            drawPanel(guiGraphics, tagX, modal.tagsY + 3, tagW, 16, 0xFF14263A, COLOR_BORDER);
            guiGraphics.drawString(this.font, tag, tagX + 6, modal.tagsY + 7, COLOR_TEXT);
            guiGraphics.drawString(this.font, "x", tagX + tagW - 10, modal.tagsY + 7, COLOR_TEXT_DIM);
            tagX += tagW + 5;
        }
        drawClippedString(guiGraphics, "Add tag...", tagX + 4, modal.tagsY + 7, modal.tagsX + modal.tagsW - tagX - 32, COLOR_TEXT_DIM);
        guiGraphics.drawString(this.font, "⌄", modal.tagsX + modal.tagsW - 18, modal.tagsY + 7, COLOR_TEXT_DIM);

        drawPanel(guiGraphics, modal.variantsBoxX, modal.variantsBoxY, modal.variantsBoxW, modal.variantsBoxH, 0xDD0F1B2D, COLOR_BORDER);
        drawEditSectionHeader(guiGraphics, "2", "VARIANTS (" + editAssetVariantCount + " VARIANTS)", modal.variantsBoxX + 12, modal.variantsBoxY + 12, modal.variantsBoxW - 24);
        drawClippedString(guiGraphics, "Later: link assets or add .schem / .schematic / .blueprint files.",
                modal.variantsBoxX + 12, modal.variantsBoxY + 31, modal.variantsBoxW - 24, COLOR_TEXT_DIM);

        int rowCount = Math.min(editAssetVariantCount, 2);
        if (rowCount >= 1) {
            drawEditVariantRow(guiGraphics, "Default", "Active", modal.variantRowX, modal.variantRowY, modal.variantRowW, modal.variantRowH, true);
        }
        if (rowCount >= 2) {
            drawEditVariantRow(guiGraphics, "Ruined", "", modal.variantRowX, modal.variantRowY + modal.variantRowH + modal.variantRowGap, modal.variantRowW, modal.variantRowH, false);
        }
        if (editAssetVariantCount > 2) {
            guiGraphics.drawString(this.font, "+" + (editAssetVariantCount - 2) + " more variants", modal.variantRowX + 8, modal.variantRowY + (modal.variantRowH + modal.variantRowGap) * 2 + 8, COLOR_TEXT_DIM);
        }
        drawButtonBox(guiGraphics, "+ Add Variant", modal.addVariantX, modal.addVariantY, modal.addVariantW, modal.buttonH, false);
        drawButtonBoxState(guiGraphics, "Remove Selected", modal.removeVariantX, modal.removeVariantY, modal.removeVariantW, modal.buttonH, false, editAssetVariantCount > 1);

        drawPanel(guiGraphics, modal.filesX, modal.filesY, modal.filesW, modal.filesH, 0xDD0F1B2D, COLOR_BORDER);
        drawEditSectionHeader(guiGraphics, "3", "FILES / OPTIONS", modal.filesX + 12, modal.filesY + 12, modal.filesW - 24);
        drawEditFileField(guiGraphics, "Structure file (.schem)", "stone_tower_v1.schem", modal.structureFileX, modal.structureFileY, modal.structureFileW, modal.fileFieldH);
        drawEditFileField(guiGraphics, "Preview image", mockPreviewImageSelected ? mockPreviewImageName : "stone_tower_preview.png", modal.previewFileX, modal.previewFileY, modal.previewFileW, modal.fileFieldH);

        if (modal.filesH >= 118) {
            int optionY = modal.filesY + modal.filesH - 30;
            int rightOptionX = modal.filesX + Math.max(118, modal.filesW / 2);
            guiGraphics.drawString(this.font, "☆ Favorite", modal.structureFileX, optionY + 7, COLOR_TEXT_DIM);
            drawMockToggle(guiGraphics, modal.favoriteToggleX, optionY + 3, asset != null && asset.isFavorite());
            guiGraphics.drawString(this.font, "◉ Visible", rightOptionX, optionY + 7, COLOR_TEXT_DIM);
            drawMockToggle(guiGraphics, modal.visibleToggleX, optionY + 3, true);
        }

        drawEditPreviewPanel(guiGraphics, modal, asset);

        guiGraphics.fill(modal.panelX + 16, modal.cancelY - 11, modal.panelX + modal.panelW - 16, modal.cancelY - 10, COLOR_BORDER);
        drawButtonBox(guiGraphics, "Delete Asset", modal.deleteX, modal.deleteY, modal.deleteW, modal.buttonH, false);
        guiGraphics.drawString(this.font, "!", modal.deleteX + 10, modal.deleteY + 8, 0xFFFF5D7A);
        drawButtonBox(guiGraphics, "Cancel", modal.cancelX, modal.cancelY, modal.cancelW, modal.buttonH, false);
        drawButtonBoxState(guiGraphics, "Save Changes", modal.saveX, modal.saveY, modal.saveW, modal.buttonH, true, isEditAssetFormValid());

        drawEditDropdownList(guiGraphics, modal);
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

    private void drawSelectorField(GuiGraphics guiGraphics, String text, int x, int y, int width, int height) {
        drawPanel(guiGraphics, x, y, width, height, COLOR_PANEL, COLOR_BORDER);

        String label = fitTextToWidth(text + "  >", width - 20);
        int textY = y + (height - this.font.lineHeight) / 2;
        guiGraphics.drawString(this.font, label, x + 10, textY, COLOR_TEXT);
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

    private void drawAssetContextMenu(GuiGraphics guiGraphics, int x, int y, int width, ArchivAsset asset, int mouseX, int mouseY) {
        int itemH = 22;
        int height = itemH * 3;

        boolean editHovered = isInside(mouseX, mouseY, x, y, width, itemH);
        boolean addHovered = isHoveringAddToCollectionRow(mouseX, mouseY, x, y, width);
        boolean deleteHovered = isInside(mouseX, mouseY, x, y + (itemH * 2), width, itemH);
        boolean showRollup = shouldShowCollectionRollup(mouseX, mouseY, x, y, width);

        drawPanel(guiGraphics, x, y, width, height, 0xFF142136, COLOR_BORDER_ACTIVE);

        if (editHovered) {
            guiGraphics.fill(x + 1, y + 1, x + width - 1, y + itemH, 0xFF25364F);
        }

        guiGraphics.drawString(this.font, "Edit Asset", x + 10, y + 7, COLOR_TEXT);

        guiGraphics.fill(x + 1, y + itemH, x + width - 1, y + itemH + 1, COLOR_BORDER);

        if (addHovered || showRollup) {
            guiGraphics.fill(x + 1, y + itemH + 1, x + width - 1, y + (itemH * 2) - 1, 0xFF25364F);
        }

        guiGraphics.drawString(this.font, "Add to Collection  >", x + 10, y + itemH + 7, COLOR_TEXT);

        guiGraphics.fill(x + 1, y + (itemH * 2), x + width - 1, y + (itemH * 2) + 1, COLOR_BORDER);

        if (deleteHovered) {
            guiGraphics.fill(x + 1, y + (itemH * 2) + 1, x + width - 1, y + height - 1, 0xFF25364F);
        }

        guiGraphics.drawString(this.font, "Delete Asset", x + 10, y + (itemH * 2) + 7, 0xFFFFD7DE);

        if (!showRollup) {
            return;
        }

        int rollupW = getAssetCollectionRollupW();
        int rollupX = getAssetCollectionRollupX(x, width, rollupW);
        int rollupY = getAssetCollectionRollupY(y);
        int totalItems = getAssetCollectionPickerItemCount();
        int rollupH = totalItems * itemH;

        drawPanel(guiGraphics, rollupX, rollupY, rollupW, rollupH, 0xFF142136, COLOR_BORDER_ACTIVE);

        int currentY = rollupY;

        for (int i = 0; i < collectionEntries.size(); i++) {
            CollectionEntry entry = collectionEntries.get(i);

            boolean rowHovered = isInside(mouseX, mouseY, rollupX, currentY, rollupW, itemH);

            if (rowHovered) {
                guiGraphics.fill(rollupX + 1, currentY + 1, rollupX + rollupW - 1, currentY + itemH, 0xFF25364F);
            }

            String label = fitTextToWidth(entry.getName(), rollupW - 20);
            guiGraphics.drawString(this.font, label, rollupX + 10, currentY + 7, COLOR_TEXT);

            currentY += itemH;
            guiGraphics.fill(rollupX + 1, currentY, rollupX + rollupW - 1, currentY + 1, COLOR_BORDER);
        }

        boolean createHovered = isInside(mouseX, mouseY, rollupX, currentY, rollupW, itemH);

        if (createHovered) {
            guiGraphics.fill(rollupX + 1, currentY + 1, rollupX + rollupW - 1, currentY + itemH, 0xFF25364F);
        }

        guiGraphics.drawString(this.font, "Create New Collection", rollupX + 10, currentY + 7, COLOR_BORDER_ACTIVE);
    }

    private void drawCollectionCard(GuiGraphics guiGraphics, int x, int y, int width, int height, CollectionEntry entry, boolean selected) {
        int borderColor = selected ? COLOR_BORDER_ACTIVE : COLOR_BORDER;
        int backgroundColor = selected ? COLOR_PANEL_ALT : COLOR_PANEL;

        drawPanel(guiGraphics, x, y, width, height, backgroundColor, borderColor);

        if (selected) {
            guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 3, COLOR_BORDER_ACTIVE);
        }

        int bannerH = 46;
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + bannerH, entry.previewColor);

        guiGraphics.drawString(this.font, "COLLECTION", x + 12, y + 16, 0xFFFFFFFF);
        guiGraphics.drawString(this.font, entry.getName(), x + 12, y + bannerH + 12, COLOR_TEXT);

        String metaText = entry.getTag() == null || entry.getTag().isBlank()
                ? entry.getAssetCount() + " assets"
                : "#" + entry.getTag() + "  •  " + entry.getAssetCount() + " assets";

        guiGraphics.drawString(this.font, fitTextToWidth(metaText, width - 24), x + 12, y + bannerH + 26, COLOR_TEXT_DIM);

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

    private void drawSettingsTab(GuiGraphics guiGraphics, int contentX, int contentY, int contentW, int contentH) {
        SettingsLayout layout = buildSettingsLayout(contentX, contentY, contentW, contentH);

        if (!"Metadata".equals(selectedSettingsSection)) {
            guiGraphics.drawString(this.font, "Settings", layout.innerX, layout.innerY, COLOR_TEXT);
            guiGraphics.drawString(this.font, "Customize Archiv to fit your build workflow.", layout.innerX, layout.innerY + 16, COLOR_TEXT_DIM);

            int panelW = Math.min(560, layout.innerW - 20);
            int panelH = 150;
            int panelX = layout.innerX;
            int panelY = layout.groupY;

            drawPanel(guiGraphics, panelX, panelY, panelW, panelH, COLOR_PANEL, COLOR_BORDER);
            guiGraphics.drawString(this.font, selectedSettingsSection, panelX + 14, panelY + 16, COLOR_TEXT);
            guiGraphics.drawString(this.font, "This settings section is planned for a later pass.", panelX + 14, panelY + 40, COLOR_TEXT_DIM);
            guiGraphics.drawString(this.font, "For now, Metadata controls the shared values used by Import and Edit Asset.", panelX + 14, panelY + 58, COLOR_TEXT_DIM);
            guiGraphics.drawString(this.font, "Next passes can reuse this same Settings layout.", panelX + 14, panelY + 76, COLOR_TEXT_DIM);
            return;
        }

        drawMetadataSettings(guiGraphics, layout);
    }

    private void drawMetadataGroupTile(GuiGraphics guiGraphics, String group, int x, int y, int width, int height, boolean active) {
        int background = active ? COLOR_PANEL_ALT : COLOR_PANEL;
        int border = active ? COLOR_BORDER_ACTIVE : COLOR_BORDER;
        int accent = getMetadataAccentColor(group, getMetadataGroupCount(group));

        drawPanel(guiGraphics, x, y, width, height, background, border);

        if (active) {
            guiGraphics.fill(x, y + 1, x + 3, y + height - 1, COLOR_BORDER_ACTIVE);
        }

        int iconSize = 24;
        int iconX = x + 12;
        int iconY = y + (height - iconSize) / 2;
        drawPanel(guiGraphics, iconX, iconY, iconSize, iconSize, 0xFF10243A, accent);

        String icon = getMetadataGroupIcon(group);
        guiGraphics.drawString(this.font, icon, iconX + (iconSize - this.font.width(icon)) / 2, iconY + 8, COLOR_TEXT);

        int textX = iconX + iconSize + 12;
        guiGraphics.drawString(this.font, fitTextToWidth(group, width - 88), textX, y + 7, active ? COLOR_TEXT : COLOR_TEXT_DIM);
        guiGraphics.drawString(this.font, fitTextToWidth(getMetadataGroupSubtitle(group), width - 88), textX, y + 21, COLOR_TEXT_DIM);

        String count = String.valueOf(getMetadataGroupCount(group));
        int badgeW = Math.max(18, this.font.width(count) + 10);
        drawPanel(guiGraphics, x + width - badgeW - 10, y + 8, badgeW, 18, active ? 0xFF1D5F91 : 0xFF162233, active ? COLOR_BORDER_ACTIVE : COLOR_BORDER);
        guiGraphics.drawString(this.font, count, x + width - badgeW - 10 + (badgeW - this.font.width(count)) / 2, y + 13, COLOR_TEXT);
    }

    private void drawMetadataOptionRow(GuiGraphics guiGraphics, String option, int index, int x, int y, int width, int height, boolean active) {
        int background = active ? COLOR_PANEL_ALT : COLOR_PANEL;
        int border = active ? COLOR_BORDER_ACTIVE : COLOR_BORDER;
        int accent = getMetadataAccentColor(option, index);

        drawPanel(guiGraphics, x, y, width, height, background, border);

        if (active) {
            guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 3, COLOR_BORDER_ACTIVE);
        }

        // Drag handle visual only for now. The real reorder behavior comes in the next pass.
        drawDragHandle(guiGraphics, x + 10, y + (height / 2) - 5, active ? COLOR_BORDER_ACTIVE : COLOR_TEXT_DIM);

        int iconSize = 21;
        int iconX = x + 26;
        int iconY = y + (height - iconSize) / 2;
        drawPanel(guiGraphics, iconX, iconY, iconSize, iconSize, 0xFF10243A, accent);

        String icon = getMetadataOptionIcon(option, index);
        guiGraphics.drawString(this.font, icon, iconX + (iconSize - this.font.width(icon)) / 2, iconY + 7, COLOR_TEXT);

        int eyeX = x + width - 25;
        guiGraphics.drawString(this.font, fitTextToWidth(option, width - 96), iconX + iconSize + 10, y + 11, active ? COLOR_TEXT : COLOR_TEXT_DIM);
        guiGraphics.drawString(this.font, "○", eyeX, y + 11, COLOR_BORDER_ACTIVE);
    }

    private void drawMetadataEditorField(GuiGraphics guiGraphics, String label, String value, int x, int y, int width) {
        guiGraphics.drawString(this.font, label, x, y, COLOR_TEXT_DIM);
        drawPanel(guiGraphics, x + 92, y - 5, width - 92, 24, COLOR_PANEL, COLOR_BORDER);
        guiGraphics.drawString(this.font, fitTextToWidth(value, width - 110), x + 102, y + 2, COLOR_TEXT);
    }

    private void drawUsagePill(GuiGraphics guiGraphics, String label, int x, int y, int width) {
        drawPanel(guiGraphics, x, y, width, 18, COLOR_PANEL, COLOR_BORDER);
        guiGraphics.drawString(this.font, "✓", x + 7, y + 5, COLOR_SUCCESS);
        guiGraphics.drawString(this.font, fitTextToWidth(label, width - 24), x + 20, y + 5, COLOR_TEXT_DIM);
    }

    private void drawMetadataInfoBox(GuiGraphics guiGraphics, String line1, String line2, int x, int y, int width, int height) {
        drawPanel(guiGraphics, x, y, width, height, 0xFF10243A, COLOR_BORDER_ACTIVE);
        guiGraphics.drawString(this.font, "i", x + 12, y + 12, COLOR_BORDER_ACTIVE);
        guiGraphics.drawString(this.font, fitTextToWidth(line1, width - 44), x + 32, y + 10, COLOR_TEXT_DIM);
        guiGraphics.drawString(this.font, fitTextToWidth(line2, width - 44), x + 32, y + 26, COLOR_TEXT_DIM);
    }

    private void drawMockToggle(GuiGraphics guiGraphics, int x, int y, boolean active) {
        int bg = active ? 0xFF2F9BE6 : 0xFF162233;
        int border = active ? 0xFF73C8FF : COLOR_BORDER;
        drawPanel(guiGraphics, x, y, 32, 16, bg, border);

        int knobX = active ? x + 18 : x + 2;
        guiGraphics.fill(knobX, y + 2, knobX + 12, y + 14, 0xFFF2F7FF);
    }

    private void drawMetadataSettings(GuiGraphics guiGraphics, SettingsLayout layout) {
        updateMetadataScrollLimits(layout);

        guiGraphics.drawString(this.font, "Metadata", layout.innerX, layout.innerY, COLOR_TEXT);
        guiGraphics.drawString(this.font, "Configure categories, types, versions, and other metadata used across Archiv.", layout.innerX, layout.innerY + 18, COLOR_TEXT_DIM);

        int panelBottom = getSettingsPanelBottom(layout);
        int bottomButtonY = getSettingsPanelButtonY(layout);

        // left: metadata groups
        drawPanel(guiGraphics, layout.groupX, layout.groupY, layout.groupW, layout.editorH, COLOR_PANEL, COLOR_BORDER);
        guiGraphics.drawString(this.font, "Metadata Groups", layout.groupX + 12, layout.groupY + 12, COLOR_TEXT);

        int groupListX = getMetadataGroupsViewportX(layout);
        int groupListY = getMetadataGroupsViewportY(layout);
        int groupListW = getMetadataGroupsViewportW(layout);
        int groupListH = getMetadataGroupsViewportH(layout);

        guiGraphics.enableScissor(groupListX, groupListY, groupListX + groupListW, groupListY + groupListH);
        for (int i = 0; i < metadataGroups.length; i++) {
            String group = metadataGroups[i];
            int groupY = groupListY + i * (layout.groupItemH + layout.groupGap) - metadataGroupsScrollOffset;

            if (groupY + layout.groupItemH < groupListY || groupY > groupListY + groupListH) {
                continue;
            }

            drawMetadataGroupTile(guiGraphics, group, groupListX, groupY, groupListW, layout.groupItemH, group.equals(selectedMetadataGroup));
        }
        guiGraphics.disableScissor();

        drawButtonBoxState(guiGraphics, "+ New Group", layout.groupX + 10, bottomButtonY, layout.groupW - 20, 24, false, false);

        // center: selected group list
        drawPanel(guiGraphics, layout.listX, layout.listY, layout.listW, layout.editorH, COLOR_PANEL, COLOR_BORDER);
        guiGraphics.drawString(this.font, selectedMetadataGroup, layout.listX + 12, layout.listY + 12, COLOR_TEXT);

        int searchX = layout.listX + 12;
        int searchY = layout.listY + 34;
        int searchW = Math.max(90, layout.listW - 78);
        drawPanel(guiGraphics, searchX, searchY, searchW, 24, COLOR_ROOT, COLOR_BORDER);

        String searchPlaceholder = switch (selectedMetadataGroup) {
            case "Types" -> "Search types...";
            case "Minecraft Versions" -> "Search versions...";
            case "Variant Presets" -> "Search variants...";
            case "Tags / Keywords" -> "Search tags...";
            case "Materials" -> "Search materials...";
            default -> "Search categories...";
        };
        drawClippedString(guiGraphics, searchPlaceholder, searchX + 10, searchY + 8, searchW - 20, COLOR_TEXT_DIM);

        drawPanel(guiGraphics, searchX + searchW + 8, searchY, 24, 24, COLOR_PANEL_ALT, COLOR_BORDER);
        guiGraphics.drawString(this.font, "v", searchX + searchW + 17, searchY + 8, COLOR_TEXT_DIM);
        drawPanel(guiGraphics, searchX + searchW + 38, searchY, 24, 24, COLOR_PANEL_ALT, COLOR_BORDER);
        guiGraphics.drawString(this.font, ":", searchX + searchW + 48, searchY + 8, COLOR_TEXT_DIM);

        List<String> options = getSelectedMetadataOptions();
        int selectedIndex = getSelectedMetadataIndex();

        int optionListX = getMetadataOptionsViewportX(layout);
        int optionListY = getMetadataOptionsViewportY(layout);
        int optionListW = getMetadataOptionsViewportW(layout);
        int optionListH = getMetadataOptionsViewportH(layout);

        guiGraphics.enableScissor(optionListX, optionListY, optionListX + optionListW, optionListY + optionListH);
        if (options.isEmpty()) {
            drawPanel(guiGraphics, optionListX, optionListY, optionListW, 96, COLOR_ROOT, COLOR_BORDER);
            guiGraphics.drawString(this.font, selectedMetadataGroup, optionListX + 12, optionListY + 22, COLOR_TEXT);
            drawClippedString(guiGraphics, "This metadata group is visual-only for now.", optionListX + 12, optionListY + 42, optionListW - 24, COLOR_TEXT_DIM);
            drawClippedString(guiGraphics, "We'll connect editing in a later pass.", optionListX + 12, optionListY + 58, optionListW - 24, COLOR_TEXT_DIM);
        } else {
            for (int i = 0; i < options.size(); i++) {
                int rowY = optionListY + i * (layout.listItemH + layout.listGap) - metadataOptionsScrollOffset;

                if (rowY + layout.listItemH < optionListY || rowY > optionListY + optionListH) {
                    continue;
                }

                drawMetadataOptionRow(guiGraphics, options.get(i), i, optionListX, rowY, optionListW, layout.listItemH, i == selectedIndex);
            }
        }
        guiGraphics.disableScissor();

        String addLabel = switch (selectedMetadataGroup) {
            case "Types" -> "+ Add Type";
            case "Minecraft Versions" -> "+ Add Version";
            case "Variant Presets" -> "+ Add Variant";
            case "Tags / Keywords" -> "+ Add Tag";
            case "Materials" -> "+ Add Material";
            default -> "+ Add Category";
        };
        drawButtonBoxState(guiGraphics, addLabel, layout.listX + 10, bottomButtonY, layout.listW - 20, 24, false, isSelectedMetadataGroupEditable());

        // right: metadata editor
        drawPanel(guiGraphics, layout.editorX, layout.editorY, layout.editorW, layout.editorH, COLOR_PANEL, COLOR_BORDER);
        guiGraphics.drawString(this.font, "Metadata Editor", layout.editorX + 12, layout.editorY + 12, COLOR_TEXT);

        String selectedName = options.isEmpty()
                ? selectedMetadataGroup
                : options.get(clampInt(selectedIndex, 0, options.size() - 1));
        boolean editable = isSelectedMetadataGroupEditable() && !options.isEmpty();
        int accent = getMetadataAccentColor(selectedName, selectedIndex);

        int fieldX = layout.editorX + 12;
        int fieldW = layout.editorW - 24;
        int editorBodyY = layout.editorY + 34;
        int editorBodyBottom = Math.max(editorBodyY + 16, layout.addY - 8);

        guiGraphics.enableScissor(fieldX, editorBodyY, fieldX + fieldW, editorBodyBottom);

        guiGraphics.drawString(this.font, "Selected:", fieldX, editorBodyY + 2, COLOR_TEXT_DIM);
        drawClippedString(guiGraphics, selectedName, fieldX + 56, editorBodyY + 2, fieldW - 62, COLOR_BORDER_ACTIVE);

        int cursorY = editorBodyY + 28;
        drawMetadataEditorField(guiGraphics, "Display Name", selectedName, fieldX, cursorY, fieldW);
        cursorY += 30;
        drawMetadataEditorField(guiGraphics, "Slug", trimToEmpty(selectedName).toLowerCase().replace(" ", "-").replace("/", "-"), fieldX, cursorY, fieldW);
        cursorY += 38;

        guiGraphics.drawString(this.font, "Icon", fieldX, cursorY, COLOR_TEXT_DIM);
        drawPanel(guiGraphics, fieldX, cursorY + 12, 70, 24, COLOR_PANEL, COLOR_BORDER);
        guiGraphics.drawString(this.font, getMetadataOptionIcon(selectedName, selectedIndex), fieldX + 12, cursorY + 20, COLOR_TEXT);
        guiGraphics.drawString(this.font, "v", fieldX + 54, cursorY + 20, COLOR_TEXT_DIM);

        int accentX = fieldX + 92;
        guiGraphics.drawString(this.font, "Accent Color", accentX, cursorY, COLOR_TEXT_DIM);
        drawPanel(guiGraphics, accentX, cursorY + 12, fieldW - 92, 24, COLOR_PANEL, COLOR_BORDER);
        guiGraphics.fill(accentX + 10, cursorY + 18, accentX + 42, cursorY + 30, accent);
        drawClippedString(guiGraphics, "#" + Integer.toHexString(accent).substring(2).toUpperCase(), accentX + 52, cursorY + 20, fieldW - 162, COLOR_TEXT);
        guiGraphics.drawString(this.font, "v", fieldX + fieldW - 18, cursorY + 20, COLOR_TEXT_DIM);
        cursorY += 52;

        guiGraphics.drawString(this.font, "Description", fieldX, cursorY, COLOR_TEXT_DIM);
        int descH = 42;
        drawPanel(guiGraphics, fieldX, cursorY + 12, fieldW, descH, COLOR_PANEL, COLOR_BORDER);
        String descLine1 = switch (selectedMetadataGroup) {
            case "Types" -> "Asset type used by cards and filters.";
            case "Minecraft Versions" -> "Supported version used in metadata.";
            case "Variant Presets" -> "Preset labels for common variants.";
            case "Tags / Keywords" -> "Search labels used to find assets.";
            case "Materials" -> "Common materials for future filters.";
            default -> "High-level theme used to organize assets.";
        };
        String descLine2 = editable
                ? "Available in Browse, Import and Edit Asset."
                : "This group is planned for a later pass.";
        drawClippedString(guiGraphics, descLine1, fieldX + 10, cursorY + 22, fieldW - 20, COLOR_TEXT_DIM);
        drawClippedString(guiGraphics, descLine2, fieldX + 10, cursorY + 36, fieldW - 20, COLOR_TEXT_DIM);
        cursorY += 66;

        if (cursorY + 34 <= editorBodyBottom) {
            guiGraphics.drawString(this.font, "Used In", fieldX, cursorY, COLOR_TEXT_DIM);
            int pillGap = 6;
            int pillW = Math.max(62, (fieldW - (pillGap * 2)) / 3);
            drawUsagePill(guiGraphics, "Browse", fieldX, cursorY + 14, pillW);
            drawUsagePill(guiGraphics, "Import", fieldX + pillW + pillGap, cursorY + 14, pillW);
            drawUsagePill(guiGraphics, "Edit", fieldX + (pillW + pillGap) * 2, cursorY + 14, pillW);
            cursorY += 42;
        }

        if (cursorY + 20 <= editorBodyBottom) {
            guiGraphics.drawString(this.font, "Active", fieldX, cursorY, COLOR_TEXT_DIM);
            drawMockToggle(guiGraphics, layout.editorX + layout.editorW - 46, cursorY - 4, true);
            cursorY += 30;
        }

        if (cursorY + 44 <= editorBodyBottom) {
            String infoLine1 = editable
                    ? "Active and available across Archiv."
                    : "Planned for the expanded metadata system.";
            String infoLine2 = editable
                    ? "Changes affect Import, Edit and Browse."
                    : "Categories, Types and Versions are editable now.";
            drawMetadataInfoBox(guiGraphics, infoLine1, infoLine2, fieldX, cursorY, fieldW, 44);
        }

        guiGraphics.disableScissor();

        drawButtonBoxState(guiGraphics, "+ Add", layout.addX, layout.addY, layout.addW, layout.buttonH, true, editable);
        drawButtonBoxState(guiGraphics, "Duplicate", layout.duplicateX, layout.duplicateY, layout.duplicateW, layout.buttonH, false, editable);
        drawButtonBoxState(guiGraphics, "Remove", layout.removeX, layout.removeY, layout.removeW, layout.buttonH, false, editable && options.size() > 1);

        drawButtonBox(guiGraphics, "Reset to Default", layout.resetX, layout.resetY, layout.resetW, layout.buttonH, false);
        drawButtonBox(guiGraphics, "Cancel", layout.cancelX, layout.cancelY, layout.cancelW, layout.buttonH, false);
        drawButtonBox(guiGraphics, "Save Changes", layout.saveX, layout.saveY, layout.saveW, layout.buttonH, true);
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

    private void drawBrowseTab(GuiGraphics guiGraphics, int contentX, int contentY, int contentW, int contentH, List<ArchivAsset> visibleAssets, int mouseX, int mouseY) {
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

                    boolean hovered = isInside(mouseX, mouseY, layout.listX, rowY, layout.listW, layout.rowH);
                    drawBrowseListRow(guiGraphics, layout.listX, rowY, layout.listW, layout.rowH, asset, hovered, mouseX, mouseY);
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

                    boolean hovered = isInside(mouseX, mouseY, cardX, cardY, layout.cardW, layout.cardH);
                    drawAssetCard(guiGraphics, cardX, cardY, layout.cardW, layout.cardH, asset, hovered, mouseX, mouseY);
                }
            }

            guiGraphics.disableScissor();
            drawScrollbar(guiGraphics, scrollbar, browseScrollbarDragging);
        }
    }

    private void drawMyAssetsTab(GuiGraphics guiGraphics, int contentX, int contentY, int contentW, int contentH, List<ArchivAsset> visibleAssets, int mouseX, int mouseY) {
        int innerPadding = 18;
        int scrollbarReserve = 16;
        int usableContentW = contentW - scrollbarReserve;

        int viewportX = contentX + 1;
        int viewportY = contentY + 1;
        int viewportW = contentW - 2 - scrollbarReserve;
        int viewportH = contentH - 2;

        int titleX = contentX + innerPadding;

        // ===== posições BASE (sem scroll) =====
        int titleBaseY = contentY + 16;
        int statsBaseY = titleBaseY + 40;

        int statGap = 12;
        int statW = (usableContentW - (innerPadding * 2) - (statGap * 3)) / 4;
        int statH = 68;

        int quickBaseY = statsBaseY + statH + 18;
        int quickButtonBaseY = quickBaseY + 14;
        int quickButtonH = 30;
        int quickGap = 12;
        int quickW = (usableContentW - (innerPadding * 2) - (quickGap * 2)) / 3;

        int sectionBaseY = quickButtonBaseY + quickButtonH + 20;

        List<ArchivAsset> recentAssets = getRecentAssets(4);

        boolean collectionsSection = "Collections".equals(selectedMyAssetsSection);
        boolean allAssetsSection = "All Assets".equals(selectedMyAssetsSection);
        boolean showImportedGrid = myAssetsShowsImportedGrid();

        int importedTitleBaseY = sectionBaseY;

        if (allAssetsSection) {
            importedTitleBaseY = sectionBaseY;

            if (!collectionEntries.isEmpty()) {
                int collectionBaseY = sectionBaseY + 18;
                int collectionH = 88;
                importedTitleBaseY = collectionBaseY + collectionH + 20;
            }

            if (!recentAssets.isEmpty()) {
                int recentTitleBaseY = importedTitleBaseY;
                int recentCardBaseY = recentTitleBaseY + 18;
                int recentH = 62;

                importedTitleBaseY = recentCardBaseY + recentH + 20;
            }
        }

        if (collectionsSection) {
            int collectionBaseY = sectionBaseY + 34;
            int collectionH = 94;
            int collectionInfoGap = 16;
            int collectionInfoH = 54;

            importedTitleBaseY = collectionBaseY + collectionH + collectionInfoGap + collectionInfoH + 20;
        }

        int contentBottomBaseY;

        if (!showImportedGrid) {
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

        ScrollbarLayout scrollbar = buildMyAssetsScrollbarLayout(contentX, contentY, contentW, contentH);

        guiGraphics.enableScissor(viewportX, viewportY, viewportX + viewportW, viewportY + viewportH);

        guiGraphics.drawString(this.font, "My Assets", titleX, titleY, COLOR_TEXT);
        guiGraphics.drawString(this.font, "Your personal build library and saved collections.", titleX, titleY + 16, COLOR_TEXT_DIM);

        drawStatCard(guiGraphics, titleX, statsY, statW, statH, String.valueOf(getSavedFavoritesCount()), "Favorites", "Starred assets");
        drawStatCard(guiGraphics, titleX + statW + statGap, statsY, statW, statH, String.valueOf(savedAssets.size()), "Imported", "Imported assets");
        drawStatCard(guiGraphics, titleX + (statW + statGap) * 2, statsY, statW, statH, String.valueOf(getRecentAssetsCount()), "Recent", "Used recently");
        drawStatCard(guiGraphics, titleX + (statW + statGap) * 3, statsY, statW, statH, String.valueOf(getCollectionCount()), "Collections", "Saved collections");

        guiGraphics.drawString(this.font, "Quick Actions", titleX, quickY, COLOR_TEXT);

        drawButtonBox(guiGraphics, "Import Asset", titleX, quickButtonY, quickW, quickButtonH, true);
        drawButtonBox(guiGraphics, "Create Collection", titleX + quickW + quickGap, quickButtonY, quickW, quickButtonH, false);
        drawButtonBox(guiGraphics, "Open Local Folder", titleX + (quickW + quickGap) * 2, quickButtonY, quickW, quickButtonH, false);

        int currentY = sectionY;

        if (collectionsSection) {
            guiGraphics.drawString(this.font, "Collections", titleX, currentY, COLOR_TEXT);
            guiGraphics.drawString(this.font, "Saved themed asset groups and folders.", titleX, currentY + 16, COLOR_TEXT_DIM);

            int collectionY = currentY + 34;
            int collectionGap = 14;
            int collectionW = (usableContentW - (innerPadding * 2) - (collectionGap * 2)) / 3;
            int collectionH = 94;

            int collectionViewportX = titleX;
            int collectionViewportY = collectionY;
            int collectionViewportW = usableContentW - (innerPadding * 2);
            int collectionViewportH = collectionH;

            int collectionContentW = collectionEntries.isEmpty()
                    ? collectionViewportW
                    : (collectionEntries.size() * collectionW) + ((collectionEntries.size() - 1) * collectionGap);

            myAssetsCollectionsMaxScrollX = Math.max(0, collectionContentW - collectionViewportW);
            myAssetsCollectionsScrollX = clampInt(myAssetsCollectionsScrollX, 0, myAssetsCollectionsMaxScrollX);

            guiGraphics.enableScissor(
                    collectionViewportX,
                    collectionViewportY,
                    collectionViewportX + collectionViewportW,
                    collectionViewportY + collectionViewportH
            );

            for (int i = 0; i < collectionEntries.size(); i++) {
                CollectionEntry entry = collectionEntries.get(i);
                int cardX = titleX + i * (collectionW + collectionGap) - myAssetsCollectionsScrollX;

                drawCollectionCard(
                        guiGraphics,
                        cardX,
                        collectionY,
                        collectionW,
                        collectionH,
                        entry,
                        selectedCollectionName != null && selectedCollectionName.equals(entry.getName())
                );
            }

            guiGraphics.disableScissor();
            guiGraphics.enableScissor(viewportX, viewportY, viewportX + viewportW, viewportY + viewportH);

            int infoY = collectionY + collectionH + 16;
            int infoH = 54;

            drawPanel(guiGraphics, titleX, infoY, usableContentW - (innerPadding * 2), infoH, COLOR_PANEL, COLOR_BORDER);

            CollectionEntry selectedEntry = getSelectedCollectionEntry();
            if (selectedEntry == null) {
                guiGraphics.drawString(this.font, "No collection selected", titleX + 12, infoY + 12, COLOR_TEXT);
                guiGraphics.drawString(this.font, "Choose a collection card above to view its assets.", titleX + 12, infoY + 28, COLOR_TEXT_DIM);
            } else {
                guiGraphics.drawString(this.font, selectedEntry.getName(), titleX + 12, infoY + 12, COLOR_TEXT);

                String metaLine = selectedEntry.getTag() == null || selectedEntry.getTag().isBlank()
                        ? selectedEntry.getAssetCount() + " assets"
                        : "#" + selectedEntry.getTag() + "  •  " + selectedEntry.getAssetCount() + " assets";

                guiGraphics.drawString(this.font, metaLine, titleX + 12, infoY + 28, COLOR_TEXT_DIM);

                String description = selectedEntry.getDescription() == null || selectedEntry.getDescription().isBlank()
                        ? "No description yet."
                        : fitTextToWidth(selectedEntry.getDescription(), usableContentW - (innerPadding * 2) - 220);

                guiGraphics.drawString(this.font, description, titleX + 180, infoY + 28, COLOR_TEXT_DIM);
            }

            currentY = infoY + infoH + 20;
        } else if (allAssetsSection) {
            if (!collectionEntries.isEmpty()) {
                guiGraphics.drawString(this.font, "Collections", titleX, sectionY, COLOR_TEXT);

                String viewAllCollections = "View all collections ->";
                guiGraphics.drawString(this.font, viewAllCollections, contentX + usableContentW - innerPadding - this.font.width(viewAllCollections), sectionY, COLOR_BORDER_ACTIVE);

                int collectionY = sectionY + 18;
                int collectionGap = 14;
                int collectionW = (usableContentW - (innerPadding * 2) - (collectionGap * 2)) / 3;
                int collectionH = 88;

                for (int i = 0; i < collectionEntries.size(); i++) {
                    CollectionEntry entry = collectionEntries.get(i);
                    int cardX = titleX + i * (collectionW + collectionGap);
                    drawCollectionCard(
                            guiGraphics,
                            cardX,
                            collectionY,
                            collectionW,
                            collectionH,
                            entry,
                            selectedCollectionName != null && selectedCollectionName.equals(entry.getName())
                    );
                }

                if (!recentAssets.isEmpty()) {
                    int recentTitleY = collectionY + collectionH + 20;
                    guiGraphics.drawString(this.font, "Recently Used", titleX, recentTitleY, COLOR_TEXT);

                    String viewAllRecent = "View all recent ->";
                    guiGraphics.drawString(this.font, viewAllRecent, contentX + usableContentW - innerPadding - this.font.width(viewAllRecent), recentTitleY, COLOR_BORDER_ACTIVE);

                    int recentCardY = recentTitleY + 18;
                    int recentGap = 10;
                    int recentW = (usableContentW - (innerPadding * 2) - (recentGap * 3)) / 4;
                    int recentH = 62;

                    for (int i = 0; i < recentAssets.size(); i++) {
                        int recentX = titleX + i * (recentW + recentGap);
                        drawRecentMiniCard(guiGraphics, recentX, recentCardY, recentW, recentH, recentAssets.get(i));
                    }
                }
            } else if (!recentAssets.isEmpty()) {
                guiGraphics.drawString(this.font, "Recently Used", titleX, sectionY, COLOR_TEXT);

                String viewAllRecent = "View all recent ->";
                guiGraphics.drawString(this.font, viewAllRecent, contentX + usableContentW - innerPadding - this.font.width(viewAllRecent), sectionY, COLOR_BORDER_ACTIVE);

                int recentCardY = sectionY + 18;
                int recentGap = 10;
                int recentW = (usableContentW - (innerPadding * 2) - (recentGap * 3)) / 4;
                int recentH = 62;

                for (int i = 0; i < recentAssets.size(); i++) {
                    int recentX = titleX + i * (recentW + recentGap);
                    drawRecentMiniCard(guiGraphics, recentX, recentCardY, recentW, recentH, recentAssets.get(i));
                }
            }

            currentY = importedTitleY;
        } else {
            myAssetsCollectionsMaxScrollX = 0;
            myAssetsCollectionsScrollX = 0;
        }

        if (!showImportedGrid) {
            drawEmptyState(
                    guiGraphics,
                    contentX + innerPadding,
                    currentY,
                    contentW - (innerPadding * 2),
                    160,
                    selectedMyAssetsSection,
                    "This management section is not implemented yet."
            );
            guiGraphics.disableScissor();
            drawScrollbar(guiGraphics, scrollbar, myAssetsScrollbarDragging);
            return;
        }

        String importedSectionTitle = switch (selectedMyAssetsSection) {
            case "Favorites" -> "Favorite Assets";
            case "Recent" -> "Recent Assets";
            case "Collections" -> selectedCollectionName != null
                    ? "Assets in " + selectedCollectionName
                    : "Collection Assets";
            default -> "Imported Assets";
        };

        guiGraphics.drawString(this.font, importedSectionTitle, titleX, currentY, COLOR_TEXT);

        CardGridLayout layout = buildMyAssetsImportedGridLayout(contentX, contentW, currentY, visibleAssets.size());

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
            } else if ("Collections".equals(selectedMyAssetsSection)) {
                drawEmptyState(
                        guiGraphics,
                        layout.cardsAreaX,
                        layout.cardsAreaY,
                        layout.cardsAreaW,
                        160,
                        selectedCollectionName == null ? "No collection selected" : "This collection is empty",
                        selectedCollectionName == null
                                ? "Choose a collection card above."
                                : "Use Add to Collection to organize assets here."
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

                boolean hovered = isInside(mouseX, mouseY, cardX, cardY, layout.cardW, layout.cardH);
                drawAssetCard(guiGraphics, cardX, cardY, layout.cardW, layout.cardH, asset, hovered, mouseX, mouseY);
            }
        }

        guiGraphics.disableScissor();
        drawScrollbar(guiGraphics, scrollbar, myAssetsScrollbarDragging);
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
        String previewType = mockDetailsFilled ? getCurrentImportType() : "Unknown Type";
        String previewVersion = mockDetailsFilled ? getCurrentImportVersion() : "Unknown";
        String previewAuthor = mockDetailsFilled ? preset.author : "Unknown";
        String previewCategory = mockDetailsFilled ? getCurrentImportCategory() : "Uncategorized";
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

        ImportDetailsFormLayout form = buildImportDetailsFormLayout(layout);

        drawFieldBox(guiGraphics, mockDetailsFilled ? preset.name : "Asset Name...", form.nameX, form.nameY, form.fieldW1, form.fieldH, mockDetailsFilled);

        if (mockDetailsFilled) {
            drawSelectorField(guiGraphics, getCurrentImportCategory(), form.categoryX, form.categoryY, form.fieldW2, form.fieldH);
        } else {
            drawFieldBox(guiGraphics, "Macro Category...", form.categoryX, form.categoryY, form.fieldW2, form.fieldH, false);
        }

        drawFieldBox(guiGraphics, mockDetailsFilled ? preset.author : "Author...", form.authorX, form.authorY, form.fieldW3, form.fieldH, mockDetailsFilled);

        if (mockDetailsFilled) {
            drawSelectorField(guiGraphics, getCurrentImportType(), form.typeX, form.typeY, form.fieldW1, form.fieldH);
            drawSelectorField(guiGraphics, getCurrentImportVersion(), form.versionX, form.versionY, form.fieldW2, form.fieldH);

            drawPanel(guiGraphics, form.variantsX, form.variantsY, form.fieldW3, form.fieldH, COLOR_PANEL, COLOR_BORDER);
            String variantsLabel = "-  " + getCurrentImportVariantCount() + " variants  +";
            guiGraphics.drawString(this.font, fitTextToWidth(variantsLabel, form.fieldW3 - 18), form.variantsX + 9, form.variantsY + 7, COLOR_TEXT);
        } else {
            drawFieldBox(guiGraphics, "Type...", form.typeX, form.typeY, form.fieldW1, form.fieldH, false);
            drawFieldBox(guiGraphics, "Minecraft Version...", form.versionX, form.versionY, form.fieldW2, form.fieldH, false);
            drawFieldBox(guiGraphics, "Variants...", form.variantsX, form.variantsY, form.fieldW3, form.fieldH, false);
        }

        drawFieldBox(guiGraphics, mockDetailsFilled ? preset.tags : "Tags...", form.tagsX, form.tagsY, form.wideFieldW, form.fieldH, mockDetailsFilled);
        drawFieldBox(guiGraphics, mockDetailsFilled ? preset.fileInfo : "File Info...", form.fileX, form.fileY, form.wideFieldW, form.fieldH, mockDetailsFilled);

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

    private void drawAssetCard(GuiGraphics guiGraphics, int x, int y, int width, int height, ArchivAsset asset, boolean hovered, int mouseX, int mouseY) {
        boolean loaded = isAssetLoaded(asset);

        int border = hovered
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

        if (hovered) {
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
                drawAssetContextMenu(guiGraphics, layout.menuX, layout.menuY, layout.menuW, asset, mouseX, mouseY);
            }
        }
    }

    private void drawBrowseListRow(GuiGraphics guiGraphics, int x, int y, int width, int height, ArchivAsset asset, boolean hovered, int mouseX, int mouseY) {
        boolean loaded = isAssetLoaded(asset);

        int borderColor = hovered
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
        if (hovered) {
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
                drawAssetContextMenu(guiGraphics, layout.menuX, layout.menuY, layout.menuW, asset, mouseX, mouseY);
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

    private void drawDragHandle(GuiGraphics guiGraphics, int x, int y, int color) {
        // Six-dot grip, shared by Settings and Edit Asset so reorder handles
        // keep the same visual language across Archiv.
        int dot = 2;
        int gap = 5;
        for (int row = 0; row < 3; row++) {
            guiGraphics.fill(x, y + row * gap, x + dot, y + row * gap + dot, color);
            guiGraphics.fill(x + gap, y + row * gap, x + gap + dot, y + row * gap + dot, color);
        }
    }

    private void drawDot(GuiGraphics guiGraphics, int x, int y, int color) {
        guiGraphics.fill(x, y, x + 8, y + 8, color);
    }
}