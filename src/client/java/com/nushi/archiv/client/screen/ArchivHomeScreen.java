package com.nushi.archiv.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ArchivHomeScreen extends Screen {
    private final Screen parent;

    public ArchivHomeScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = 120;
        int buttonHeight = 20;
        int buttonX = (this.width - buttonWidth) / 2;
        int buttonY = this.height - 40;

        Button closeButton = Button.builder(Component.literal("Fechar"), button -> this.onClose())
                .bounds(buttonX, buttonY, buttonWidth, buttonHeight)
                .build();

        this.addRenderableWidget(closeButton);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xC0101010);

        guiGraphics.drawString(this.font, this.title, 40, 40, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Primeira tela do Archiv.", 40, 60, 0xE0E0E0);
        guiGraphics.drawString(this.font, "Ainda simples de proposito.", 40, 74, 0xE0E0E0);
        guiGraphics.drawString(this.font, "Depois a gente troca isso pela UI real.", 40, 88, 0xE0E0E0);

        super.render(guiGraphics, mouseX, mouseY, delta);
    }
}