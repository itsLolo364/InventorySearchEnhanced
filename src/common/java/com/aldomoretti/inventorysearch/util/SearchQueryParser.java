package com.aldomoretti.inventorysearch.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SearchQueryParser {
    private SearchQueryParser() {}

    public static class ParsedQuery {
        public String searchTerm = "";
        public List<EnchantFilter> enchantFilters = new ArrayList<>();
        public String loreFilter = null;
        public Integer durabilityFilter = null;
        public boolean hasExactMatch = false;
    }

    public static class EnchantFilter {
        public final String enchantName;
        public final Integer level;

        public EnchantFilter(String enchantName, Integer level) {
            this.enchantName = enchantName;
            this.level = level;
        }
    }

    private static final Pattern EXACT_MATCH_PATTERN = Pattern.compile("\"([^\"]+)\"");
    private static final Pattern ENCHANT_PATTERN = Pattern.compile("enchant:([\\w\\s,]+)(?:\\s+(\\d+))?");
    private static final Pattern ENCHANT_WITH_LEVEL_PATTERN = Pattern.compile("([\\w\\s]+)\\s+(\\d+)");
    private static final Pattern LORE_PATTERN = Pattern.compile("lore:(.+)");
    private static final Pattern DURABILITY_PATTERN = Pattern.compile("durability:(\\d+)");

    public static ParsedQuery parse(String query) {
        ParsedQuery parsed = new ParsedQuery();
        if (query == null || query.isBlank()) {
            return parsed;
        }

        String remainingQuery = query;

        // Parse exact match
        Matcher exactMatcher = EXACT_MATCH_PATTERN.matcher(remainingQuery);
        if (exactMatcher.find()) {
            parsed.searchTerm = exactMatcher.group(1).toLowerCase();
            parsed.hasExactMatch = true;
            remainingQuery = remainingQuery.replace(exactMatcher.group(0), "").trim();
        }

        // Parse enchant filters - support multiple enchant filters with comma or space separation
        Matcher enchantMatcher = ENCHANT_PATTERN.matcher(remainingQuery);
        while (enchantMatcher.find()) {
            String enchantPart = enchantMatcher.group(1).trim();
            Integer level = enchantMatcher.group(2) != null ? Integer.parseInt(enchantMatcher.group(2)) : null;

            // Split by comma for multiple enchants (space is used for level)
            String[] enchantNames = enchantPart.split(",");
            for (String enchantName : enchantNames) {
                if (!enchantName.isBlank()) {
                    parsed.enchantFilters.add(new EnchantFilter(enchantName.trim(), level));
                }
            }

            remainingQuery = remainingQuery.replace(enchantMatcher.group(0), "").trim();
        }

        // Parse lore filter
        Matcher loreMatcher = LORE_PATTERN.matcher(remainingQuery);
        if (loreMatcher.find()) {
            parsed.loreFilter = loreMatcher.group(1).toLowerCase();
            remainingQuery = remainingQuery.replace(loreMatcher.group(0), "").trim();
        }

        // Parse durability filter
        Matcher durabilityMatcher = DURABILITY_PATTERN.matcher(remainingQuery);
        if (durabilityMatcher.find()) {
            parsed.durabilityFilter = Integer.parseInt(durabilityMatcher.group(1));
            remainingQuery = remainingQuery.replace(durabilityMatcher.group(0), "").trim();
        }

        // Remaining text is search term
        if (!parsed.hasExactMatch && !remainingQuery.isBlank()) {
            parsed.searchTerm = remainingQuery.toLowerCase();
        }

        return parsed;
    }

    // Method to parse auto-enchant detection (words that look like enchant names)
    public static ParsedQuery parseAutoEnchant(String query) {
        ParsedQuery parsed = parse(query);

        // If no explicit enchant filters were found, try to auto-detect enchant names
        if (parsed.enchantFilters.isEmpty() && !parsed.searchTerm.isBlank()) {
            String[] words = parsed.searchTerm.split("\\s+");
            StringBuilder remainingSearch = new StringBuilder();
            boolean[] used = new boolean[words.length];

            // First pass: check for multi-word enchant names
            for (int i = 0; i < words.length; i++) {
                if (used[i] || words[i].isBlank()) continue;

                // Try combinations of 2, 3, 4 words
                for (int len = Math.min(4, words.length - i); len >= 2; len--) {
                    StringBuilder combined = new StringBuilder();
                    for (int j = 0; j < len; j++) {
                        if (j > 0) combined.append(" ");
                        combined.append(words[i + j]);
                    }

                    String combinedStr = combined.toString();
                    if (EnchantMatcher.looksLikeEnchant(combinedStr)) {
                        parsed.enchantFilters.add(new EnchantFilter(combinedStr, null));
                        // Mark these words as used
                        for (int j = 0; j < len; j++) {
                            used[i + j] = true;
                        }
                        break; // Move to next position
                    }
                }
            }

            // Second pass: check for single-word enchant names and item names
            for (int i = 0; i < words.length; i++) {
                if (used[i] || words[i].isBlank()) continue;

                String word = words[i];

                // Check if this is a number (level) - apply to last enchant
                try {
                    int level = Integer.parseInt(word);
                    if (!parsed.enchantFilters.isEmpty()) {
                        // Apply this level to the last enchant filter
                        int lastIndex = parsed.enchantFilters.size() - 1;
                        EnchantFilter lastFilter = parsed.enchantFilters.get(lastIndex);
                        parsed.enchantFilters.set(lastIndex, new EnchantFilter(lastFilter.enchantName, level));
                    }
                } catch (NumberFormatException e) {
                    // Not a number, process as normal word
                    if (EnchantMatcher.looksLikeEnchant(word)) {
                        // Add enchant without level (will be set if number follows)
                        parsed.enchantFilters.add(new EnchantFilter(word, null));
                    } else {
                        if (remainingSearch.length() > 0) {
                            remainingSearch.append(" ");
                        }
                        remainingSearch.append(word);
                    }
                }
            }

            parsed.searchTerm = remainingSearch.toString().trim();
        }

        return parsed;
    }

    public static List<String> getParameterSuggestions() {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("enchant:<name> <level> - es: enchant:mend, enchant:blast prot 4");
        suggestions.add("Auto-enchant: mend aqua aff (senza enchant:)");
        suggestions.add("lore:<text> - Cerca nel lore");
        suggestions.add("durability:<value> - Durabilità minima");
        suggestions.add("\"text\" - Ricerca esatta");
        suggestions.add("Combinabili: enchant:mend lore:testo durability:50");
        return suggestions;
    }
}