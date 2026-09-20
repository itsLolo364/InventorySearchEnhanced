package com.aldomoretti.inventorysearch.integration;

import com.aldomoretti.inventorysearch.SearchState;
import com.aldomoretti.inventorysearch.config.SearchConfig;
import com.aldomoretti.inventorysearch.util.ItemMatcher;
import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import com.misterpemodder.shulkerboxtooltip.api.PreviewType;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProvider;
import com.misterpemodder.shulkerboxtooltip.api.renderer.PreviewRenderer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public final class SearchHighlightPreviewRenderer implements PreviewRenderer {
    private static final SearchConfig CFG = SearchConfig.getInstance();

    private final PreviewRenderer delegate;
    private PreviewProvider provider;
    private PreviewContext previewContext;
    private PreviewType previewType = PreviewType.FULL;
    private List<ItemStack> fullItems = List.of();
    private int maxRowSize = 9;

    public SearchHighlightPreviewRenderer() {
        this.delegate = PreviewRenderer.getDefaultRendererInstance();
    }

    @Override
    public int getHeight() {
        return this.delegate.getHeight();
    }

    @Override
    public int getWidth() {
        return this.delegate.getWidth();
    }

    @Override
    public void setPreview(PreviewContext context, PreviewProvider provider) {
        this.previewContext = context;
        this.provider = provider;
        this.fullItems = provider.getInventory(context);

        int rowSize = provider.getMaxRowSize(context);
        int compactRowSize = provider.getCompactMaxRowSize(context);
        if (compactRowSize <= 0) {
            compactRowSize = context.config().defaultMaxRowSize();
        }
        if (compactRowSize <= 0) {
            compactRowSize = 9;
        }
        if (rowSize <= 0) {
            rowSize = compactRowSize;
        }
        this.maxRowSize = rowSize;

        this.delegate.setPreview(context, provider);
    }

    @Override
    public void setPreviewType(PreviewType type) {
        this.previewType = type;
        this.delegate.setPreviewType(type);
    }

    @Override
    public void draw(int x, int y, GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        this.delegate.draw(x, y, graphics, font, mouseX, mouseY);
        this.drawSearchHighlights(graphics, x, y);
    }

    private void drawSearchHighlights(GuiGraphicsExtractor graphics, int x, int y) {
        SearchState searchState = SearchState.getInstance();
        if (!searchState.isActive() || this.previewType == PreviewType.NO_PREVIEW) {
            return;
        }

        String query = searchState.getQuery();
        for (int slot = 0; slot < this.fullItems.size(); slot++) {
            ItemStack stack = this.fullItems.get(slot);
            if (ItemMatcher.matches(stack, query)) {
                this.drawSlotOverlay(graphics, x, y, slot, this.maxRowSize);
            }
        }
    }

    private void drawSlotOverlay(GuiGraphicsExtractor graphics, int x, int y, int slot, int maxRowSize) {
        int slotX = CFG.previewSlotXOffset + x + CFG.previewSlotWidth * (slot % maxRowSize);
        int slotY = CFG.previewSlotYOffset + y + CFG.previewSlotHeight * (slot / maxRowSize);

        graphics.fill(slotX, slotY, slotX + 16, slotY + 16, CFG.getHighlightFill());
        graphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY, CFG.getHighlightBorder());
        graphics.fill(slotX - 1, slotY + 16, slotX + 17, slotY + 17, CFG.getHighlightBorder());
        graphics.fill(slotX - 1, slotY, slotX, slotY + 16, CFG.getHighlightBorder());
        graphics.fill(slotX + 16, slotY, slotX + 17, slotY + 16, CFG.getHighlightBorder());
    }
}