package com.aldomoretti.inventorysearch.util;

import com.aldomoretti.inventorysearch.config.SearchConfig;
import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import com.misterpemodder.shulkerboxtooltip.api.ShulkerBoxTooltipApi;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProvider;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;

public final class ContainerScanner {
    private static final SearchConfig CFG = SearchConfig.getInstance();

    private ContainerScanner() {}

    private static final class ScanAccumulator {
        private final List<String> foundNames = new ArrayList<>();
        private final Set<Integer> matchedSlots = new LinkedHashSet<>();
        private int matchCount;

        private void addMatch(ItemStack stack, int slotIndex) {
            this.matchCount += stack.getCount();
            this.foundNames.add(stack.getHoverName().getString() + " x" + stack.getCount());
            if (slotIndex >= 0) {
                this.matchedSlots.add(slotIndex);
            }
        }
    }

    public static List<SearchResult> scan(AbstractContainerMenu menu, String query, Player player) {
        List<SearchResult> results = new ArrayList<>();
        if (query == null || query.isBlank()) {
            return results;
        }

        HolderLookup.Provider registryLookup = player == null ? null : player.registryAccess();

        for (int slotIndex = 0; slotIndex < menu.slots.size(); slotIndex++) {
            Slot slot = menu.slots.get(slotIndex);
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                continue;
            }

            boolean directMatch = ItemMatcher.matches(stack, query);
            ScanAccumulator accumulator = new ScanAccumulator();
            ContainerType containerType = scanInside(stack, query, player, registryLookup, accumulator, 0);

            if (directMatch && accumulator.matchCount > 0) {
                results.add(SearchResult.directAndContainerMatch(
                        slotIndex,
                        containerType,
                        List.copyOf(accumulator.foundNames),
                        List.copyOf(accumulator.matchedSlots),
                        accumulator.matchCount
                ));
            } else if (directMatch) {
                results.add(SearchResult.directMatch(slotIndex));
            } else if (accumulator.matchCount > 0) {
                results.add(SearchResult.containerMatch(
                        slotIndex,
                        containerType,
                        List.copyOf(accumulator.foundNames),
                        List.copyOf(accumulator.matchedSlots),
                        accumulator.matchCount
                ));
            }
        }

        return results;
    }

    private static ContainerType scanInside(
            ItemStack stack,
            String query,
            Player player,
            HolderLookup.Provider registryLookup,
            ScanAccumulator accumulator,
            int depth
    ) {
        if (depth > CFG.maxNestDepth) {
            return ContainerType.NONE;
        }

        PreviewProvider provider = ShulkerBoxTooltipApi.getPreviewProviderForStackWithOverrides(stack);
        if (provider != null) {
            PreviewContext.Builder contextBuilder = PreviewContext.builder(stack);
            if (player != null) {
                contextBuilder.withOwner(player);
            }
            if (registryLookup != null) {
                contextBuilder.withRegistryLookup(registryLookup);
            }

            PreviewContext context = contextBuilder.build();
            List<ItemStack> inventory = provider.getInventory(context);
            ContainerType containerType = resolveContainerType(stack, provider);

            if (inventory != null) {
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack inner = inventory.get(i);
                    if (inner == null || inner.isEmpty()) {
                        continue;
                    }

                    if (ItemMatcher.matches(inner, query)) {
                        accumulator.addMatch(inner, i);
                    }

                    ContainerType nestedType = scanInside(inner, query, player, registryLookup, accumulator, depth + 1);
                    if (containerType == ContainerType.NONE && nestedType != ContainerType.NONE) {
                        containerType = nestedType;
                    }
                }
            }

            return containerType;
        }

        return scanBundleContents(stack, query, player, registryLookup, accumulator, depth);
    }

    private static ContainerType scanBundleContents(
            ItemStack stack,
            String query,
            Player player,
            HolderLookup.Provider registryLookup,
            ScanAccumulator accumulator,
            int depth
    ) {
        BundleContents bundleContents = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents == null) {
            return ContainerType.NONE;
        }

        ContainerType nestedType = ContainerType.NONE;
        for (ItemStack inner : bundleContents.itemCopyStream().toList()) {
            if (inner.isEmpty()) {
                continue;
            }

            if (ItemMatcher.matches(inner, query)) {
                accumulator.addMatch(inner, -1);
            }

            ContainerType childType = scanInside(inner, query, player, registryLookup, accumulator, depth + 1);
            if (childType != ContainerType.NONE) {
                nestedType = childType;
            }
        }

        return nestedType == ContainerType.NONE && accumulator.matchCount > 0 ? ContainerType.BUNDLE : nestedType;
    }

    private static ContainerType resolveContainerType(ItemStack stack, PreviewProvider provider) {
        String providerName = provider.getClass().getSimpleName().toLowerCase();
        if (providerName.contains("enderchest")) {
            return ContainerType.ENDER_CHEST;
        }
        if (stack.is(Items.ENDER_CHEST)) {
            return ContainerType.ENDER_CHEST;
        }

        String itemId = stack.getItem().toString().toLowerCase();
        if (itemId.contains("shulker")) {
            return ContainerType.SHULKER;
        }
        if (itemId.contains("chest") || itemId.contains("barrel")) {
            return ContainerType.CHEST;
        }

        return ContainerType.OTHER;
    }
}
