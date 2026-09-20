package com.aldomoretti.inventorysearch.gui;

import com.aldomoretti.inventorysearch.util.SearchQueryParser;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SuggestionMenu {
    private final Font font;
    private final int x;
    private final int y;
    private final int width;
    private final List<String> suggestions;
    private int selectedIndex = 0;
    private int scrollOffset = 0;
    private static final int SUGGESTION_HEIGHT = 12;
    private static final int VISIBLE_SUGGESTIONS = 3;

    public SuggestionMenu(Font font, int x, int y, int width, List<String> suggestions) {
        this.font = font;
        this.x = x;
        this.y = y;
        this.width = width;
        this.suggestions = suggestions;
    }

    public void render(GuiGraphics graphics) {
        if (suggestions.isEmpty()) {
            return;
        }

        int totalHeight = Math.min(suggestions.size(), VISIBLE_SUGGESTIONS) * SUGGESTION_HEIGHT;

        // Draw background
        graphics.fill(x, y, x + width, y + totalHeight, 0xFF000000);
        graphics.fill(x, y, x + width, y + totalHeight, 0xFF800080); // Purple border

        // Draw suggestions
        for (int i = 0; i < VISIBLE_SUGGESTIONS && (scrollOffset + i) < suggestions.size(); i++) {
            int suggestionIndex = scrollOffset + i;
            String suggestion = suggestions.get(suggestionIndex);
            int suggestionY = y + i * SUGGESTION_HEIGHT;

            // Highlight selected
            if (suggestionIndex == selectedIndex) {
                graphics.fill(x + 1, suggestionY + 1, x + width - 1, suggestionY + SUGGESTION_HEIGHT - 1, 0xFFD8B4FE);
            }

            // Draw text (truncated if too long)
            String displayText = suggestion;
            if (font.width(displayText) > width - 4) {
                // Simple truncation
                while (font.width(displayText + "...") > width - 10 && displayText.length() > 0) {
                    displayText = displayText.substring(0, displayText.length() - 1);
                }
                displayText += "...";
            }

            graphics.drawString(font, Component.literal(displayText), x + 2, suggestionY + 2, 0xFFFFFF);
        }
    }

    public void scrollUp() {
        if (selectedIndex > 0) {
            selectedIndex--;
            if (selectedIndex < scrollOffset) {
                scrollOffset = selectedIndex;
            }
        }
    }

    public void scrollDown() {
        if (selectedIndex < suggestions.size() - 1) {
            selectedIndex++;
            if (selectedIndex >= scrollOffset + VISIBLE_SUGGESTIONS) {
                scrollOffset = selectedIndex - VISIBLE_SUGGESTIONS + 1;
            }
        }
    }

    public String getSelectedSuggestion() {
        if (selectedIndex >= 0 && selectedIndex < suggestions.size()) {
            return suggestions.get(selectedIndex);
        }
        return null;
    }

    public int getHeight() {
        return Math.min(suggestions.size(), VISIBLE_SUGGESTIONS) * SUGGESTION_HEIGHT;
    }

    public boolean isVisible() {
        return !suggestions.isEmpty();
    }
}