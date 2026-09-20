package com.aldomoretti.inventorysearch.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EnchantMatcher {
    private EnchantMatcher() {}

    // Common enchant ID mappings for matching (support both underscore and space versions)
    private static final Map<String, String> ENCHANT_IDS = new HashMap<>();
    static {
        ENCHANT_IDS.put("protection", "minecraft:protection");
        ENCHANT_IDS.put("sharpness", "minecraft:sharpness");
        ENCHANT_IDS.put("efficiency", "minecraft:efficiency");
        ENCHANT_IDS.put("unbreaking", "minecraft:unbreaking");
        ENCHANT_IDS.put("fortune", "minecraft:fortune");
        ENCHANT_IDS.put("mending", "minecraft:mending");
        ENCHANT_IDS.put("thorns", "minecraft:thorns");
        ENCHANT_IDS.put("blast protection", "minecraft:blast_protection");
        ENCHANT_IDS.put("blast_protection", "minecraft:blast_protection");
        ENCHANT_IDS.put("fire protection", "minecraft:fire_protection");
        ENCHANT_IDS.put("fire_protection", "minecraft:fire_protection");
        ENCHANT_IDS.put("projectile protection", "minecraft:projectile_protection");
        ENCHANT_IDS.put("projectile_protection", "minecraft:projectile_protection");
        ENCHANT_IDS.put("feather falling", "minecraft:feather_falling");
        ENCHANT_IDS.put("feather_falling", "minecraft:feather_falling");
        ENCHANT_IDS.put("depth strider", "minecraft:depth_strider");
        ENCHANT_IDS.put("depth_strider", "minecraft:depth_strider");
        ENCHANT_IDS.put("respiration", "minecraft:respiration");
        ENCHANT_IDS.put("aqua affinity", "minecraft:aqua_affinity");
        ENCHANT_IDS.put("aqua_affinity", "minecraft:aqua_affinity");
        ENCHANT_IDS.put("punch", "minecraft:punch");
        ENCHANT_IDS.put("flame", "minecraft:flame");
        ENCHANT_IDS.put("infinity", "minecraft:infinity");
        ENCHANT_IDS.put("power", "minecraft:power");
        ENCHANT_IDS.put("luck of the sea", "minecraft:luck_of_the_sea");
        ENCHANT_IDS.put("luck_of_the_sea", "minecraft:luck_of_the_sea");
        ENCHANT_IDS.put("lure", "minecraft:lure");
        ENCHANT_IDS.put("silk touch", "minecraft:silk_touch");
        ENCHANT_IDS.put("silk_touch", "minecraft:silk_touch");
        ENCHANT_IDS.put("sweeping", "minecraft:sweeping");
        ENCHANT_IDS.put("loyalty", "minecraft:loyalty");
        ENCHANT_IDS.put("impaling", "minecraft:impaling");
        ENCHANT_IDS.put("riptide", "minecraft:riptide");
        ENCHANT_IDS.put("channeling", "minecraft:channeling");
        ENCHANT_IDS.put("multishot", "minecraft:multishot");
        ENCHANT_IDS.put("piercing", "minecraft:piercing");
        ENCHANT_IDS.put("quick charge", "minecraft:quick_charge");
        ENCHANT_IDS.put("quick_charge", "minecraft:quick_charge");
        ENCHANT_IDS.put("frost walker", "minecraft:frost_walker");
        ENCHANT_IDS.put("frost_walker", "minecraft:frost_walker");
        ENCHANT_IDS.put("soul speed", "minecraft:soul_speed");
        ENCHANT_IDS.put("soul_speed", "minecraft:soul_speed");
        ENCHANT_IDS.put("swift sneak", "minecraft:swift_sneak");
        ENCHANT_IDS.put("swift_sneak", "minecraft:swift_sneak");
    }

    public static boolean matchesEnchants(ItemStack stack, List<SearchQueryParser.EnchantFilter> filters) {
        if (filters == null || filters.isEmpty()) {
            return true;
        }

        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null) {
            return false;
        }

        // Check each filter
        for (SearchQueryParser.EnchantFilter filter : filters) {
            boolean found = false;

            // Try to find matching enchant by checking all enchantments
            for (var entry : enchantments.entrySet()) {
                String enchantId = entry.getKey().toString();
                int level = entry.getIntValue();

                if (matchesEnchantFilter(enchantId, filter.enchantName, filter.level, level)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }

    public static boolean looksLikeEnchant(String word) {
        // Check if the word matches known enchant names
        String normalizedWord = word.toLowerCase().replace("_", " ");

        // Direct match
        if (ENCHANT_IDS.containsKey(normalizedWord) ||
            ENCHANT_IDS.containsKey(normalizedWord.replace(" ", "_"))) {
            return true;
        }

        // Partial match - check if any enchant ID contains this word or word contains enchant name
        for (String enchantId : ENCHANT_IDS.values()) {
            String normalizedId = enchantId.replace("minecraft:", "").replace("_", " ");
            if (normalizedId.contains(normalizedWord) || normalizedWord.contains(normalizedId)) {
                return true;
            }
        }

        return false;
    }

    private static boolean matchesEnchantFilter(String enchantId, String filterName, Integer filterLevel, int actualLevel) {
        // Normalize the enchant ID for comparison
        String normalizedId = enchantId.replace("minecraft:", "").replace("_", " ").toLowerCase();
        String normalizedFilter = filterName.toLowerCase().replace("_", " ").trim();

        // Check if the enchant matches the filter
        boolean nameMatches = normalizedId.equalsIgnoreCase(normalizedFilter) ||
                normalizedId.contains(normalizedFilter) ||
                enchantId.toLowerCase().contains(normalizedFilter.replace(" ", "_"));

        if (!nameMatches) {
            // Check known mappings
            String mappedId = ENCHANT_IDS.get(filterName.toLowerCase().replace(" ", "_"));
            if (mappedId != null) {
                nameMatches = enchantId.equals(mappedId) || enchantId.contains(mappedId);
            }
        }

        if (!nameMatches) {
            return false;
        }

        // Check level if specified - MUST BE EXACT MATCH
        if (filterLevel != null && actualLevel != filterLevel) {
            return false;
        }

        return true;
    }
}