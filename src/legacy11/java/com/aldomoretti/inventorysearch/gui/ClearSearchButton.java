package com.aldomoretti.inventorysearch.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ClearSearchButton extends Button {
    public ClearSearchButton(int x, int y, int width, int height, Runnable onClear) {
        super(x, y, width, height, Component.translatable("inventorysearch.search.clear"), button -> onClear.run(), DEFAULT_NARRATION);
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Default implementation for Minecraft 1.21.11
    }
}