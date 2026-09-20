package com.aldomoretti.inventorysearch.integration;

import com.aldomoretti.inventorysearch.SearchState;
import com.aldomoretti.inventorysearch.config.SearchConfig;
import com.aldomoretti.inventorysearch.util.ItemMatcher;
import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import com.misterpemodder.shulkerboxtooltip.api.PreviewType;
import com.misterpemodder.shulkerboxtooltip.api.config.Theme;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProvider;
import com.misterpemodder.shulkerboxtooltip.api.renderer.PreviewRenderer;
import com.misterpemodder.shulkerboxtooltip.api.renderer.RenderContext;
import com.misterpemodder.shulkerboxtooltip.impl.util.MergedItemStack;
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
    private List<MergedItemStack> compactItems = List.of();
    private int maxRowSize = 9;
    private int compactMaxRowSize = 9;

    public SearchHighlightPreviewRenderer(Theme theme) {
        this.delegate = PreviewRenderer.getDefaultRendererInstance(theme);
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
        this.compactItems = MergedItemStack.mergeInventory(
                this.fullItems,
                provider.getInventoryMaxSize(context),
                context.config().itemStackMergingStrategy(),
                context.config().compactPreviewOrder().toComparator()
        );

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
        this.compactMaxRowSize = compactRowSize;

        this.delegate.setPreview(context, provider);
    }

    @Override
    public void setPreviewType(PreviewType type) {
        this.previewType = type;
        this.delegate.setPreviewType(type);
    }

    @Override
    public void draw(RenderContext context) {
        this.delegate.draw(context);
        this.drawSearchHighlights(context);
    }

    @Override
    public int getOutsideXOffset() {
        return this.delegate.getOutsideXOffset();
    }

    @Override
    public int getOutsideYOffset() {
        return this.delegate.getOutsideYOffset();
    }

    private void drawSearchHighlights(RenderContext context) {
        SearchState searchState = SearchState.getInstance();
        if (!searchState.isActive() || this.previewType == PreviewType.NO_PREVIEW) {
            return;
        }

        String query = searchState.getQuery();
        GuiGraphicsExtractor graphics = context.graphics();
        int x = context.x();
        int y = context.y();

        if (this.previewType == PreviewType.COMPACT) {
            for (int slot = 0; slot < this.compactItems.size(); slot++) {
                MergedItemStack merged = this.compactItems.get(slot);
                if (mergedContainsMatch(merged, query)) {
                    this.drawSlotOverlay(graphics, x, y, slot, this.compactMaxRowSize);
                }
            }
            return;
        }

        for (int slot = 0; slot < this.fullItems.size(); slot++) {
            ItemStack stack = this.fullItems.get(slot);
            if (ItemMatcher.matches(stack, query)) {
                this.drawSlotOverlay(graphics, x, y, slot, this.maxRowSize);
            }
        }
    }

    private static boolean mergedContainsMatch(MergedItemStack merged, String query) {
        if (merged == null) {
            return false;
        }

        // Check the representative stack
        return ItemMatcher.matches(merged.get(), query);
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
