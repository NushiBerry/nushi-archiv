package com.nushi.archiv.client.screen;
import com.nushi.archiv.client.model.ArchivAsset;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.nushi.archiv.client.repository.MockAssetRepository;

// Primeira versão real da tela principal do Archiv.
// Nesta etapa, ela é um shell visual: layout grande, fullscreen e inspirado no design aprovado.
public class ArchivBrowseScreen extends Screen {

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

    // Construtor da tela.
    public ArchivBrowseScreen(Component title, Screen parent) {
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

        drawTopTab(guiGraphics, "Browse", tabX, tabY, tabW, tabH, true);
        drawTopTab(guiGraphics, "My Assets", tabX + (tabW + tabGap), tabY, tabW + 10, tabH, false);
        drawTopTab(guiGraphics, "Import", tabX + (tabW + tabGap) * 2 + 10, tabY, tabW, tabH, false);
        drawTopTab(guiGraphics, "Settings", tabX + (tabW + tabGap) * 3 + 10, tabY, tabW, tabH, false);

        // ===== Sidebar content =====
        guiGraphics.drawString(this.font, "CATEGORIES", rootX + 16, bodyY + 14, COLOR_TEXT_DIM);

        String[] categories = {
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

        int categoryY = bodyY + 34;
        for (int i = 0; i < categories.length; i++) {
            boolean active = i == 0;
            drawSidebarItem(guiGraphics, categories[i], rootX + 12, categoryY, sidebarW - 24, 34, active);
            categoryY += 40;
        }

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

        for (int i = 0; i < mockAssets.size(); i++) {
            ArchivAsset asset = mockAssets.get(i);

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

        // ===== Footer =====
        drawPanel(guiGraphics, rootX, rootY + rootH - footerH, rootW, footerH, COLOR_PANEL, COLOR_BORDER);

        int footerY = rootY + rootH - footerH + 8;
        guiGraphics.drawString(this.font, "WorldEdit: pending", rootX + 12, footerY, COLOR_SUCCESS);
        guiGraphics.drawString(this.font, "Preview pipeline: planned", rootX + 140, footerY, COLOR_TEXT_DIM);
        guiGraphics.drawString(this.font, "v0.1.0", rootX + rootW - 50, footerY, COLOR_TEXT_DIM);

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
        guiGraphics.drawString(this.font, label, x + 12, y + 12, COLOR_TEXT_DIM);
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