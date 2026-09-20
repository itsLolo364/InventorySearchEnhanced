package com.aldomoretti.inventorysearch.mixin;

import com.aldomoretti.inventorysearch.SearchState;
import com.aldomoretti.inventorysearch.config.SearchConfig;
import com.aldomoretti.inventorysearch.gui.ClearSearchButton;
import com.aldomoretti.inventorysearch.gui.SearchBarWidget;
import com.aldomoretti.inventorysearch.util.ContainerScanner;
import com.aldomoretti.inventorysearch.util.SearchResult;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> {
    @Shadow
    protected int leftPos;
    @Shadow
    protected int topPos;
    @Shadow
    protected int imageWidth;
    @Shadow
    protected int imageHeight;
    @Shadow
    @Final
    protected T menu;

    @Unique
    private SearchConfig is_config = SearchConfig.getInstance();
    @Unique
    private SearchBarWidget is_bar = null;
    @Unique
    private ClearSearchButton is_clearButton = null;
    @Unique
    private Map<Integer, SearchResult> is_results = new HashMap<>();
    @Unique
    private String is_query = "";

    @Inject(method = "init", at = @At("TAIL"))
    private void is_onInit(CallbackInfo ci) {
        AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
        Font font = self.getFont();

        int barWidth = this.is_config.searchBarWidth;
        int barHeight = this.is_config.searchBarHeight;
        int barX = self.width - this.is_config.screenPadding - barWidth;
        int barY = self.height - this.is_config.screenPadding - barHeight;

        this.is_bar = new SearchBarWidget(font, barX, barY, barWidth, barHeight);
        this.is_bar.setSearchCallback(this::is_onSearchChanged);

        this.is_clearButton = new ClearSearchButton(
                barX,
                barY - this.is_config.clearButtonHeight - this.is_config.widgetGap,
                barWidth,
                this.is_config.clearButtonHeight,
                () -> this.is_clearSearch()
        );

        ScreenAccessor accessor = (ScreenAccessor) self;
        if (this.is_config.showClearButton) {
            accessor.invokeAddRenderableWidget(this.is_clearButton);
        }
        accessor.invokeAddRenderableWidget(this.is_bar);

        this.is_query = "";
        this.is_results = new HashMap<>();
        SearchState.getInstance().clear();
    }

    @Unique
    private void is_onSearchChanged(String query) {
        this.is_query = query;
        SearchState.getInstance().setQuery(query);
        this.is_doSearch();
    }

    @Unique
    private void is_clearSearch() {
        if (this.is_bar != null) {
            this.is_bar.clear();
        }
    }

    @Unique
    private void is_doSearch() {
        this.is_results.clear();
        if (this.is_query == null || this.is_query.isBlank()) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        for (SearchResult result : ContainerScanner.scan(this.menu, this.is_query, player)) {
            this.is_results.put(result.slotIndex, result);
        }
    }

    @Inject(method = "extractSlot", at = @At("HEAD"))
    private void is_onExtractSlot(GuiGraphicsExtractor extractor, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.is_bar == null || this.is_query.isBlank()) {
            return;
        }

        int slotIndex = this.menu.slots.indexOf(slot);
        if (slotIndex < 0 || !this.is_results.containsKey(slotIndex)) {
            return;
        }

        SearchResult match = this.is_results.get(slotIndex);
        int slotX = slot.x;
        int slotY = slot.y;
        this.is_drawSlotHighlight(extractor, slotX, slotY);

        if (match.isContainer && match.matchCount > 0 && this.is_config.showMatchCount) {
            AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
            Font font = self.getFont();
            String countLabel = String.valueOf(match.matchCount);
            extractor.text(
                    font,
                    countLabel,
                    slotX + 17 - font.width(countLabel),
                    slotY + 9,
                    this.is_config.getHighlightCountText(),
                    true
            );
        }
    }

    @Inject(method = "extractSlot", at = @At("RETURN"))
    private void is_onExtractSlotEnd(GuiGraphicsExtractor extractor, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        // Render status text and suggestions after the last slot
        if (this.is_bar == null) {
            return;
        }

        // Only render once per frame (check if this is the last slot)
        if (this.menu.slots.indexOf(slot) != this.menu.slots.size() - 1) {
            return;
        }

        // Render suggestion menu if visible
        if (this.is_bar.isSuggestionMenuVisible()) {
            this.is_bar.renderSuggestions(extractor);
        }

        // Render status text
        if (this.is_query.isBlank() || !this.is_config.showStatusText) {
            return;
        }

        AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
        Font font = self.getFont();

        Component statusText;
        int statusColor;
        if (this.is_results.isEmpty()) {
            statusText = Component.translatable("inventorysearch.search.none");
            statusColor = this.is_config.getStatusNotFoundText();
        } else {
            long containerMatches = this.is_results.values().stream().filter(result -> result.isContainer).count();
            statusText = Component.translatable(
                    "inventorysearch.search.found",
                    this.is_results.size(),
                    containerMatches
            );
            statusColor = this.is_config.getStatusFoundText();
        }

        extractor.text(font, statusText, this.is_bar.getX(), this.is_bar.getY() - this.is_config.statusTextOffset, statusColor, true);
    }

    @Unique
    private void is_drawSlotHighlight(GuiGraphicsExtractor ctx, int slotX, int slotY) {
        ctx.fill(slotX, slotY, slotX + 16, slotY + 16, this.is_config.getHighlightFill());
        ctx.fill(slotX - 1, slotY - 1, slotX + 17, slotY, this.is_config.getHighlightBorder());
        ctx.fill(slotX - 1, slotY + 16, slotX + 17, slotY + 17, this.is_config.getHighlightBorder());
        ctx.fill(slotX - 1, slotY, slotX, slotY + 16, this.is_config.getHighlightBorder());
        ctx.fill(slotX + 16, slotY, slotX + 17, slotY + 16, this.is_config.getHighlightBorder());
    }

    @Redirect(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;matches(Lnet/minecraft/client/input/KeyEvent;)Z"))
    private boolean is_redirectMatches(KeyMapping keyMapping, KeyEvent event) {
        return this.is_bar != null && this.is_bar.isFocused() ? false : keyMapping.matches(event);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void is_onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (this.is_bar != null && this.is_bar.isFocused()) {
            // Let the search bar handle its own key events
            boolean handled = this.is_bar.keyPressed(event);
            cir.setReturnValue(handled);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void is_onMouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (this.is_bar != null) {
            this.is_bar.mouseClicked(event, doubleClick);
        }
    }
}