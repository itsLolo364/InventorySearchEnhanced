package com.aldomoretti.inventorysearch.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class ItemMatcher {
    private ItemMatcher() {}

    public static String clean(String value) {
        return value == null ? "" : value.replaceAll("§.", "").toLowerCase().trim();
    }

    public static boolean matches(ItemStack stack, String query) {
        if (stack == null || stack.isEmpty() || query == null || query.isBlank()) {
            return false;
        }

        SearchQueryParser.ParsedQuery parsed = SearchQueryParser.parse(query);

        // Try auto-enchant detection for simple text search
        SearchQueryParser.ParsedQuery autoParsed = SearchQueryParser.parseAutoEnchant(query);
        if (!autoParsed.enchantFilters.isEmpty()) {
            return matchesWithParsed(stack, autoParsed);
        }

        // If we have explicit parameters, use parsed query
        if (!parsed.enchantFilters.isEmpty() || parsed.loreFilter != null || parsed.durabilityFilter != null) {
            return matchesWithParsed(stack, parsed);
        }

        // For simple text search (no parameters), treat as partial word match
        return matchesPartial(stack, parsed.searchTerm);
    }

    private static boolean matchesWithParsed(ItemStack stack, SearchQueryParser.ParsedQuery parsed) {
        // Check search term - use partial matching for non-exact searches
        if (!parsed.searchTerm.isBlank()) {
            if (!matchesPartial(stack, parsed.searchTerm)) {
                return false;
            }
        }

        // Check enchant filters
        if (!parsed.enchantFilters.isEmpty()) {
            if (!EnchantMatcher.matchesEnchants(stack, parsed.enchantFilters)) {
                return false;
            }
        }

        // Check lore filter
        if (parsed.loreFilter != null) {
            if (!matchesLore(stack, parsed.loreFilter)) {
                return false;
            }
        }

        // Check durability filter
        if (parsed.durabilityFilter != null) {
            if (!matchesDurability(stack, parsed.durabilityFilter)) {
                return false;
            }
        }

        return true;
    }

    private static boolean matchesPartial(ItemStack stack, String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return true;
        }

        String displayName = clean(stack.getHoverName().getString());
        String registryId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        // Split search term into words and check if all words match
        String[] words = searchTerm.split("\\s+");
        for (String word : words) {
            if (word.isBlank()) continue;

            if (!displayName.contains(word) && !registryId.contains(word)) {
                return false;
            }
        }

        return true;
    }

    private static boolean matchesLore(ItemStack stack, String loreQuery) {
        // The lore component type changed name across Minecraft versions
        // (LoreContents <-> ItemLore), so we never reference it directly:
        // we only rely on DataComponents.LORE and iterate the returned lines.
        if (!stack.has(DataComponents.LORE)) {
            return false;
        }

        var lore = stack.get(DataComponents.LORE);
        if (lore == null || lore.lines().isEmpty()) {
            return false;
        }

        StringBuilder loreText = new StringBuilder();
        for (Object line : lore.lines()) {
            if (line instanceof Component component) {
                loreText.append(clean(component.getString())).append(" ");
            }
        }

        return loreText.toString().contains(loreQuery);
    }

    private static boolean matchesDurability(ItemStack stack, int targetDurability) {
        if (!stack.isDamageableItem()) {
            return false;
        }

        int maxDurability = stack.getMaxDamage();
        int currentDurability = maxDurability - stack.getDamageValue();
        return currentDurability >= targetDurability;
    }
}