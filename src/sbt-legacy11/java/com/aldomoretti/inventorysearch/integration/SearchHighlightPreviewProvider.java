package com.aldomoretti.inventorysearch.integration;

import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import com.misterpemodder.shulkerboxtooltip.api.color.ColorKey;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProvider;
import com.misterpemodder.shulkerboxtooltip.api.renderer.PreviewRenderer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class SearchHighlightPreviewProvider implements PreviewProvider {
    private static final int PRIORITY_BOOST = 500;

    private final PreviewProvider delegate;
    private final SearchHighlightPreviewRenderer renderer;

    public SearchHighlightPreviewProvider(PreviewProvider delegate) {
        this.delegate = delegate;
        this.renderer = new SearchHighlightPreviewRenderer();
    }

    public PreviewProvider getDelegate() {
        return this.delegate;
    }

    @Override
    public boolean shouldDisplay(PreviewContext context) {
        return this.delegate.shouldDisplay(context);
    }

    @Override
    public List<ItemStack> getInventory(PreviewContext context) {
        return this.delegate.getInventory(context);
    }

    @Override
    public int getInventoryMaxSize(PreviewContext context) {
        return this.delegate.getInventoryMaxSize(context);
    }

    @Override
    public int getMaxRowSize(PreviewContext context) {
        return this.delegate.getMaxRowSize(context);
    }

    @Override
    public int getCompactMaxRowSize(PreviewContext context) {
        return this.delegate.getCompactMaxRowSize(context);
    }

    @Override
    public boolean isFullPreviewAvailable(PreviewContext context) {
        return this.delegate.isFullPreviewAvailable(context);
    }

    @Override
    public boolean showTooltipHints(PreviewContext context) {
        return this.delegate.showTooltipHints(context);
    }

    @Override
    public String getTooltipHintLangKey(PreviewContext context) {
        return this.delegate.getTooltipHintLangKey(context);
    }

    @Override
    public String getFullTooltipHintLangKey(PreviewContext context) {
        return this.delegate.getFullTooltipHintLangKey(context);
    }

    @Override
    public String getLockKeyTooltipHintLangKey(PreviewContext context) {
        return this.delegate.getLockKeyTooltipHintLangKey(context);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ColorKey getWindowColorKey(PreviewContext context) {
        return this.delegate.getWindowColorKey(context);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public PreviewRenderer getRenderer() {
        return this.renderer;
    }

    @Override
    public List<Component> addTooltip(PreviewContext context) {
        return this.delegate.addTooltip(context);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onInventoryAccessStart(PreviewContext context) {
        this.delegate.onInventoryAccessStart(context);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier getTextureOverride(PreviewContext context) {
        return this.delegate.getTextureOverride(context);
    }

    @Override
    public int getPriority() {
        return this.delegate.getPriority() + PRIORITY_BOOST;
    }
}