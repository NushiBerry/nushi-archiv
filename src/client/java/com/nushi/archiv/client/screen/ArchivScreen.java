package com.nushi.archiv.client.screen;
import com.nushi.archiv.client.model.ArchivAsset;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.nushi.archiv.client.repository.MockAssetRepository;
import net.minecraft.client.input.MouseButtonEvent;
import java.util.ArrayList;

// Primeira versão real da tela principal do Archiv.
// Nesta etapa, ela é um shell visual: layout grande, fullscreen e inspirado no design aprovado.
public class ArchivScreen extends Screen {

    // ===== Cores principais da interface =====
    private static final int COLOR_BACKGROUND = 0xFF08111D;
    private static final int COLOR_ROOT = 0xFF0C1624;
    private static final int COLOR_PANEL = 0xFF0F1B2D;
    private static final int COLOR_PANEL_ALT = 0xFF13253B;
    private static final int COLOR_BORDER = 0xFF223854;
    private static final int COLOR_BORDER_ACTIVE = 0xFF2DAEFF;
    private static final int COLOR_TEXT = 0xFFF2F7FF;
    private static final int COLOR_TEXT_DIM = 0xFF93A8C1;
    private static final int COLOR_SUCCESS = 0xFF36C275;

    // Guarda a tela anterior para voltar ao fechar.
    private final Screen parent;

    private final List<ArchivAsset> mockAssets;

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

    private String selectedCategory = "All";
    private String selectedTopTab = "Browse";
    private int selectedImportStep = 1;

    // Construtor da tela.
    public ArchivScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;
        this.mockAssets = MockAssetRepository.getAllAssets();
    }

    @Override
    protected void init() {
        // Botão simples de fechar no canto superior direito.
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
        // Se a categoria selecionada for "All", mostra tudo.
        if (selectedCategory.equals("All")) {
            return mockAssets;
        }

        // Caso contrário, criamos uma nova lista só com os assets
        // cuja macroCategory bate com a categoria selecionada.
        List<ArchivAsset> filteredAssets = new ArrayList<>();

        for (ArchivAsset asset : mockAssets) {
            if (asset.getMacroCategory().equals(selectedCategory)) {
                filteredAssets.add(asset);
            }
        }

        return filteredAssets;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        // botão esquerdo do mouse = 0
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
            selectedTopTab = "Import";
            selectedImportStep = 1;
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
            int stepGap = 40;

            for (int i = 0; i < 4; i++) {
                int currentY = stepY + (i * stepGap);

                boolean insideStepX = mouseX >= stepX && mouseX <= stepX + stepW;
                boolean insideStepY = mouseY >= currentY && mouseY <= currentY + stepH;

                if (insideStepX && insideStepY) {
                    selectedImportStep = i + 1;
                    return true;
                }
            }
        }

        if ("Browse".equals(selectedTopTab)) {
            for (int i = 0; i < categories.length; i++) {
                int currentY = itemY + (i * itemGap);

                boolean insideX = mouseX >= itemX && mouseX <= itemX + itemW;
                boolean insideY = mouseY >= currentY && mouseY <= currentY + itemH;

                if (insideX && insideY) {
                    selectedCategory = categories[i];
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        // Fundo totalmente opaco.
        guiGraphics.fill(0, 0, this.width, this.height, COLOR_BACKGROUND);

        // ===== Layout base =====
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
        List<ArchivAsset> visibleAssets = getVisibleAssets();

        // Container principal
        drawPanel(guiGraphics, rootX, rootY, rootW, rootH, COLOR_ROOT, COLOR_BORDER);

        // Header
        drawPanel(guiGraphics, rootX, rootY, rootW, headerH, COLOR_PANEL, COLOR_BORDER);

        // Sidebar
        drawPanel(guiGraphics, rootX, bodyY, sidebarW, bodyH, COLOR_PANEL, COLOR_BORDER);

        // Área de conteúdo
        drawPanel(guiGraphics, contentX, contentY, contentW, contentH, COLOR_ROOT, COLOR_BORDER);

        // ===== Header content =====
        guiGraphics.drawString(this.font, "ARCHIV", rootX + 18, rootY + 20, COLOR_TEXT);

        int tabY = rootY + 10;
        int tabX = rootX + 150;
        int tabW = 110;
        int tabH = 38;
        int tabGap = 8;

        drawTopTab(guiGraphics, "Browse", tabX, tabY, tabW, tabH, "Browse".equals(selectedTopTab));
        drawTopTab(guiGraphics, "My Assets", tabX + (tabW + tabGap), tabY, tabW + 10, tabH, "My Assets".equals(selectedTopTab));
        drawTopTab(guiGraphics, "Import", tabX + (tabW + tabGap) * 2 + 10, tabY, tabW, tabH, "Import".equals(selectedTopTab));
        drawTopTab(guiGraphics, "Settings", tabX + (tabW + tabGap) * 3 + 10, tabY, tabW, tabH, "Settings".equals(selectedTopTab));

        // ===== Sidebar content =====
        if (browseActive) {
            guiGraphics.drawString(this.font, "CATEGORIES", rootX + 16, bodyY + 14, COLOR_TEXT_DIM);

            int categoryY = bodyY + 34;
            for (int i = 0; i < categories.length; i++) {
                boolean active = categories[i].equals(selectedCategory);
                drawSidebarItem(guiGraphics, categories[i], rootX + 12, categoryY, sidebarW - 24, 34, active);
                categoryY += 40;
            }
        } else if (!"Import".equals(selectedTopTab)) {
            guiGraphics.drawString(this.font, "SECTION", rootX + 16, bodyY + 14, COLOR_TEXT_DIM);
            guiGraphics.drawString(this.font, selectedTopTab, rootX + 16, bodyY + 34, COLOR_TEXT);
        }

        if (browseActive) {
        // ===== Toolbar / controls =====
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
        drawControlBox(guiGraphics, "Filter", filterX, toolbarY, filterW, 34);
        guiGraphics.drawString(this.font, "Sort by:", sortLabelX, toolbarY + 12, COLOR_TEXT_DIM);
        drawControlBox(guiGraphics, "Newest", sortBoxX, toolbarY, sortBoxW, 34);
        guiGraphics.drawString(this.font, "View:", viewLabelX, toolbarY + 12, COLOR_TEXT_DIM);
        drawControlBox(guiGraphics, "Grid", gridX, toolbarY, gridW, 34);
        drawControlBox(guiGraphics, "List", listX, toolbarY, listW, 34);

        int cardsAreaX = contentX + innerPadding;
        int cardsAreaY = toolbarY + 50;
        int cardsAreaW = contentW - (innerPadding * 2);
        int cardsAreaH = (contentY + contentH - 16) - cardsAreaY;

        int cardsGap = 14;
        int columns = 3;
        int rows = 2;

        int cardW = (cardsAreaW - (cardsGap * (columns - 1))) / columns;
        int rowGap = 16;
        int cardH = (cardsAreaH - (rowGap * (rows - 1))) / rows;

        if (visibleAssets.isEmpty()) {
            drawEmptyState(guiGraphics, cardsAreaX, cardsAreaY, cardsAreaW, cardsAreaH);
        } else {
            for (int i = 0; i < visibleAssets.size(); i++) {
                ArchivAsset asset = visibleAssets.get(i);

                int column = i % columns;
                int row = i / columns;

                int cardX = cardsAreaX + column * (cardW + cardsGap);
                int cardY = cardsAreaY + row * (cardH + rowGap);

                drawAssetCard(
                        guiGraphics,
                        cardX,
                        cardY,
                        cardW,
                        cardH,
                        asset
                );
            }
        }

        } else if ("Import".equals(selectedTopTab)) {
            drawImportTab(guiGraphics, rootX, rootY, rootW, rootH, bodyY, bodyH, contentX, contentY, contentW, contentH);
        } else {
            drawTabPlaceholder(guiGraphics, contentX, contentY, contentW, contentH, selectedTopTab);
        }
        // ===== Footer =====
        drawPanel(guiGraphics, rootX, rootY + rootH - footerH, rootW, footerH, COLOR_PANEL, COLOR_BORDER);

        int footerY = rootY + rootH - footerH + 8;
        guiGraphics.drawString(this.font, "WorldEdit: pending", rootX + 12, footerY, COLOR_SUCCESS);
        guiGraphics.drawString(this.font, "Preview pipeline: planned", rootX + 140, footerY, COLOR_TEXT_DIM);
        if (browseActive) {
            String assetCountText = visibleAssets.size() + " / " + mockAssets.size() + " assets";
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

    private void drawEmptyState(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Painel interno da área vazia
        int panelWidth = 320;
        int panelHeight = 90;

        int panelX = x + (width / 2) - (panelWidth / 2);
        int panelY = y + (height / 2) - (panelHeight / 2);

        drawPanel(guiGraphics, panelX, panelY, panelWidth, panelHeight, COLOR_PANEL, COLOR_BORDER);

        String title = "No assets found";
        String subtitle = "Try another category or import new assets.";

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

    private void drawImportTab(GuiGraphics guiGraphics, int rootX, int rootY, int rootW, int rootH,
                               int bodyY, int bodyH, int contentX, int contentY, int contentW, int contentH) {

        // ===== Sidebar do Import =====
        guiGraphics.drawString(this.font, "IMPORT STEPS", rootX + 16, bodyY + 14, COLOR_TEXT_DIM);

        int stepX = rootX + 12;
        int stepW = 156;
        int stepH = 34;
        int stepGap = 40;

        drawSidebarItem(guiGraphics, "1. Select File", stepX, bodyY + 34, stepW, stepH, selectedImportStep == 1);
        drawSidebarItem(guiGraphics, "2. Preview Image", stepX, bodyY + 34 + stepGap, stepW, stepH, selectedImportStep == 2);
        drawSidebarItem(guiGraphics, "3. Details", stepX, bodyY + 34 + (stepGap * 2), stepW, stepH, selectedImportStep == 3);
        drawSidebarItem(guiGraphics, "4. Save Asset", stepX, bodyY + 34 + (stepGap * 3), stepW, stepH, selectedImportStep == 4);

        // ===== Área útil interna =====
        int pad = 18;
        int innerX = contentX + pad;
        int innerY = contentY + pad;
        int innerW = contentW - (pad * 2);
        int innerH = contentH - (pad * 2);

// Se a altura útil estiver pequena, usamos layout compacto
        boolean compact = true;

        int titleBlockH = compact ? 44 : 36;
        int gap = compact ? 12 : 16;

        guiGraphics.drawString(this.font, "Import Asset", innerX, innerY, COLOR_TEXT);
        guiGraphics.drawString(this.font, "Import a new build asset into Archiv", innerX, innerY + 16, COLOR_TEXT_DIM);

// ===== Geometria principal =====
        int sectionY = innerY + titleBlockH;

        int previewColumnW = compact ? 210 : 220;
        int leftAreaW = innerW - previewColumnW - gap;

        int topBoxH = compact ? 118 : 170;
        int actionsBarH = 28;
        int actionsBottomMargin = compact ? 8 : 18;
        int detailsGap = compact ? 10 : 16;
        int actionsGap = compact ? 8 : 12;

        int structureW = (leftAreaW * 58) / 100;
        int imageW = leftAreaW - structureW - gap;

        int structureX = innerX;
        int imageX = structureX + structureW + gap;
        int previewX = innerX + leftAreaW + gap;

        int detailsY = sectionY + topBoxH + detailsGap;

// Botões ficam ancorados no fundo da área útil do conteúdo
        int actionsY = innerY + innerH - actionsBottomMargin - actionsBarH;

// O painel de details ocupa só o espaço até um pouco antes dos botões
        int detailsH = actionsY - actionsGap - detailsY;

        // ===== Blocos superiores =====
        drawPanel(guiGraphics, structureX, sectionY, structureW, topBoxH, COLOR_PANEL, COLOR_BORDER);
        drawPanel(guiGraphics, imageX, sectionY, imageW, topBoxH, COLOR_PANEL, COLOR_BORDER);
        drawPanel(guiGraphics, previewX, sectionY, previewColumnW, topBoxH + detailsGap + detailsH, COLOR_PANEL, COLOR_BORDER);

        int boxMainTextY = compact ? sectionY + 42 : sectionY + 76;
        int boxSubTextY = compact ? sectionY + 60 : sectionY + 92;
        int boxButtonY = compact ? sectionY + 80 : sectionY + 118;

// ===== Structure file =====
        guiGraphics.drawString(this.font, "1. Structure File", structureX + 12, sectionY + 12, COLOR_TEXT);
        guiGraphics.drawString(this.font, "Drop .schem / .schematic here", structureX + 45, boxMainTextY, COLOR_TEXT);
        guiGraphics.drawString(this.font, "Supports .schem and .schematic files", structureX + 26, boxSubTextY, COLOR_TEXT_DIM);
        drawButtonBox(guiGraphics, "Browse File", structureX + (structureW / 2) - 60, boxButtonY, 120, 28, false);

// ===== Preview image =====
        guiGraphics.drawString(this.font, "2. Preview Image (Optional)", imageX + 12, sectionY + 12, COLOR_TEXT);
        guiGraphics.drawString(this.font, "Drop image here", imageX + 40, boxMainTextY, COLOR_TEXT);
        guiGraphics.drawString(this.font, "PNG or JPG", imageX + 70, boxSubTextY, COLOR_TEXT_DIM);
        drawButtonBox(guiGraphics, "Browse Image", imageX + (imageW / 2) - 60, boxButtonY, 120, 28, false);

        // ===== Preview lateral =====
        guiGraphics.drawString(this.font, "3. Asset Preview", previewX + 12, sectionY + 12, COLOR_TEXT);

        int previewImageX = previewX + 12;
        int previewImageY = sectionY + 30;
        int previewImageW = previewColumnW - 24;
        int previewImageH = compact ? 92 : 110;

        guiGraphics.fill(previewImageX, previewImageY, previewImageX + previewImageW, previewImageY + previewImageH, 0xFF4B6E9A);
        guiGraphics.drawString(this.font, "PREVIEW", previewImageX + (previewImageW / 2) - 24, previewImageY + (previewImageH / 2) - 4, 0xFFFFFFFF);

        int previewInfoY = compact ? sectionY + 132 : sectionY + 150;

        guiGraphics.drawString(this.font, "Stone Tower", previewX + 12, previewInfoY, COLOR_TEXT);
        guiGraphics.drawString(this.font, "Structure  •  1.20.1", previewX + 12, previewInfoY + 16, COLOR_TEXT_DIM);

        guiGraphics.drawString(this.font, "Author", previewX + 12, previewInfoY + 44, COLOR_TEXT_DIM);
        guiGraphics.drawString(this.font, "BuilderX", previewX + 90, previewInfoY + 44, COLOR_TEXT);

        guiGraphics.drawString(this.font, "Category", previewX + 12, previewInfoY + 60, COLOR_TEXT_DIM);
        guiGraphics.drawString(this.font, "Medieval", previewX + 90, previewInfoY + 60, COLOR_TEXT);

        guiGraphics.drawString(this.font, "Format", previewX + 12, previewInfoY + 76, COLOR_TEXT_DIM);
        guiGraphics.drawString(this.font, ".schem", previewX + 90, previewInfoY + 76, COLOR_TEXT);

        // ===== Details =====
        drawPanel(guiGraphics, innerX, detailsY, leftAreaW, detailsH, COLOR_PANEL, COLOR_BORDER);
        guiGraphics.drawString(this.font, "4. Asset Details", innerX + 12, detailsY + 12, COLOR_TEXT);

        int formX = innerX + 12;
        int fieldGap = 12;
        int fieldH = compact ? 22 : 28;
        int verticalGap = compact ? 10 : 12;

        int fieldW1 = (leftAreaW - 48) / 3;
        int fieldW2 = fieldW1;
        int fieldW3 = fieldW1;
        int wideFieldW = (leftAreaW - 36) / 2;

// Espaços internos do painel de details
        int topPadding = compact ? 34 : 38;
        int bottomPadding = compact ? 10 : 14;

// Calcula as 3 linhas para caberem de verdade dentro do painel
        int totalFieldsHeight = (fieldH * 3) + (verticalGap * 2);
        int availableFormHeight = detailsH - topPadding - bottomPadding;
        int extraSpace = Math.max(0, availableFormHeight - totalFieldsHeight);

// Centraliza verticalmente o bloco de campos dentro do painel
        int formY1 = detailsY + topPadding + (extraSpace / 2);
        int formY2 = formY1 + fieldH + verticalGap;
        int formY3 = formY2 + fieldH + verticalGap;

        drawControlBox(guiGraphics, "Asset Name...", formX, formY1, fieldW1, fieldH);
        drawControlBox(guiGraphics, "Macro Category...", formX + fieldW1 + fieldGap, formY1, fieldW2, fieldH);
        drawControlBox(guiGraphics, "Author...", formX + fieldW1 + fieldGap + fieldW2 + fieldGap, formY1, fieldW3, fieldH);

        drawControlBox(guiGraphics, "Type...", formX, formY2, fieldW1, fieldH);
        drawControlBox(guiGraphics, "Minecraft Version...", formX + fieldW1 + fieldGap, formY2, fieldW2, fieldH);
        drawControlBox(guiGraphics, "Variants...", formX + fieldW1 + fieldGap + fieldW2 + fieldGap, formY2, fieldW3, fieldH);

        drawControlBox(guiGraphics, "Tags...", formX, formY3, wideFieldW, fieldH);
        drawControlBox(guiGraphics, "File Info...", formX + wideFieldW + fieldGap, formY3, wideFieldW, fieldH);

        // ===== Ações inferiores =====
        int buttonH = 28;
        int saveW = 88;
        int cancelW = 90;
        int resetW = 90;

        int saveX = innerX + innerW - saveW;
        int cancelX = saveX - fieldGap - cancelW;
        int resetX = cancelX - fieldGap - resetW;

        drawButtonBox(guiGraphics, "Reset", resetX, actionsY, resetW, buttonH, false);
        drawButtonBox(guiGraphics, "Cancel", cancelX, actionsY, cancelW, buttonH, false);
        drawButtonBox(guiGraphics, "Save", saveX, actionsY, saveW, buttonH, true);
    }

    private void drawAssetCard(GuiGraphics guiGraphics, int x, int y, int width, int height, ArchivAsset asset) {
        int border = asset.isHighlighted() ? COLOR_BORDER_ACTIVE : COLOR_BORDER;
        drawPanel(guiGraphics, x, y, width, height, COLOR_PANEL, border);

        int previewX = x + 1;
        int previewY = y + 1;
        int previewW = width - 2;

        int previewH = Math.max(80, (int) (height * 0.55));

        guiGraphics.fill(previewX, previewY, previewX + previewW, previewY + previewH, asset.getPreviewColor());

        String previewText = "PREVIEW";
        int textWidth = this.font.width(previewText);
        guiGraphics.drawString(this.font, previewText,
                previewX + (previewW / 2) - (textWidth / 2),
                previewY + (previewH / 2) - 4,
                0xFFFFFFFF);

        guiGraphics.drawString(this.font, asset.isFavorite() ? "★" : "☆", x + width - 16, y + 10, 0xFFFFD45A);

        if (asset.isHighlighted()) {
            guiGraphics.fill(previewX, previewY, previewX + previewW, previewY + previewH, 0x55000000);

            int overlayButtonW = width - 60;
            int overlayX = x + 30;

            drawPanel(guiGraphics, overlayX, y + 30, overlayButtonW, 24, 0xFF2F9BE6, 0xFF73C8FF);
            drawPanel(guiGraphics, overlayX, y + 60, overlayButtonW, 22, 0xFF1A2638, COLOR_BORDER);

            guiGraphics.drawString(this.font, "Load", x + (width / 2) - 12, y + 38, 0xFFFFFFFF);
            guiGraphics.drawString(this.font, "Details", x + (width / 2) - 18, y + 67, 0xFFE5EEF8);
        }

        int infoY = y + previewH + 12;
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