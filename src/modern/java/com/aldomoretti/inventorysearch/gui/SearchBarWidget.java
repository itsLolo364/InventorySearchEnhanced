package com.aldomoretti.inventorysearch.gui;

import com.aldomoretti.inventorysearch.config.SearchConfig;
import com.aldomoretti.inventorysearch.util.SearchQueryParser;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class SearchBarWidget extends EditBox {
    private Consumer<String> onSearch;
    private SuggestionMenu suggestionMenu;
    private boolean showSuggestions = false;
    private final Font menuFont;
    private static final SearchConfig CFG = SearchConfig.getInstance();

    public SearchBarWidget(Font font, int x, int y, int width, int height) {
        super(font, x, y, width, height, Component.literal(""));
        this.menuFont = font;
        this.setHint(Component.translatable("inventorysearch.search.placeholder"));
        this.setMaxLength(CFG.maxQueryLength);
        this.setVisible(true);
        this.setBordered(true);
        this.setCanLoseFocus(true);
        this.setResponder(text -> {
            this.updateSuggestions(text);
            if (this.onSearch != null) {
                this.onSearch.accept(text);
            }
        });
    }

    public void setSearchCallback(Consumer<String> callback) {
        this.onSearch = callback;
    }

    public void clear() {
        this.setValue("");
        this.showSuggestions = false;
    }

    private void updateSuggestions(String text) {
        // Always show suggestions when text ends with ? or contains :
        if (CFG.showSuggestions && (text.endsWith("?") || text.contains(":"))) {
            List<String> suggestions = SearchQueryParser.getParameterSuggestions();
            this.suggestionMenu = new SuggestionMenu(this.menuFont, this.getX(), this.getY() - 45, 250, suggestions);
            this.showSuggestions = true;
        } else {
            this.showSuggestions = false;
        }
    }

    public void forceShowSuggestions() {
        if (!CFG.showSuggestions) {
            return;
        }
        List<String> suggestions = SearchQueryParser.getParameterSuggestions();
        this.suggestionMenu = new SuggestionMenu(this.menuFont, this.getX(), this.getY() - 45, 250, suggestions);
        this.showSuggestions = true;
    }

    public void renderSuggestions(GuiGraphicsExtractor graphics) {
        if (this.showSuggestions && this.suggestionMenu != null) {
            this.suggestionMenu.render(graphics);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!this.isFocused()) {
            return false;
        }

        // Handle escape - remove focus instead of clearing content
        if (event.key() == 256) { // Escape
            if (this.showSuggestions) {
                this.showSuggestions = false;
            }
            this.setFocused(false);
            return true;
        }

        // Handle backspace - only allow if there's content to delete
        if (event.key() == 259) { // Backspace
            if (this.getValue().isEmpty()) {
                return true; // Block backspace when empty
            }
        }

        if (this.showSuggestions && this.suggestionMenu != null) {
            if (event.key() == 265) { // Up arrow
                this.suggestionMenu.scrollUp();
                return true;
            } else if (event.key() == 264) { // Down arrow
                this.suggestionMenu.scrollDown();
                return true;
            } else if (event.key() == 257) { // Enter
                String selected = this.suggestionMenu.getSelectedSuggestion();
                if (selected != null) {
                    this.insertSuggestion(selected);
                    return true;
                }
            }
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (!this.isFocused()) {
            return false;
        }

        // Let the parent handle character input, but check for ? after
        boolean result = super.charTyped(event);

        // Check if we just added a question mark
        String currentText = this.getValue();
        if (currentText.endsWith("?")) {
            this.forceShowSuggestions();
        }

        return result;
    }

    private void insertSuggestion(String suggestion) {
        String currentText = this.getValue();
        // Remove the "?" or ":" and append the suggestion
        String newText = currentText.replaceAll("[\\?:]$", "") + " " + suggestion;
        this.setValue(newText);
        this.showSuggestions = false;
    }

    public boolean isSuggestionMenuVisible() {
        return this.showSuggestions && this.suggestionMenu != null && this.suggestionMenu.isVisible();
    }
}