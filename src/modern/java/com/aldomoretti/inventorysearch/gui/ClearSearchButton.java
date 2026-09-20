package com.aldomoretti.inventorysearch.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ClearSearchButton extends Button {
    public ClearSearchButton(int x, int y, int width, int height, Runnable onClear) {
        super(x, y, width, height, Component.translatable("inventorysearch.search.clear"), button -> onClear.run(), DEFAULT_NARRATION);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphicsExtractor, int i, int j, float f) {
        // Default implementation for Minecraft 26.1.2
    }
}
