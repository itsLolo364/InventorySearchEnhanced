package com.aldomoretti.inventorysearch.integration;

import com.aldomoretti.inventorysearch.InventorySearchMod;
import com.misterpemodder.shulkerboxtooltip.api.ShulkerBoxTooltipApi;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProvider;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProviderRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public final class InventorySearchTooltipPlugin implements ShulkerBoxTooltipApi {
    @Override
    public void registerProviders(PreviewProviderRegistry registry) {
        List<PreviewProvider> existingProviders = new ArrayList<>(registry.getProviders());

        for (PreviewProvider provider : existingProviders) {
            if (provider instanceof SearchHighlightPreviewProvider) {
                continue;
            }

            ResourceLocation providerId = registry.getId(provider);
            if (providerId == null) {
                continue;
            }

            registry.register(
                    ResourceLocation.fromNamespaceAndPath(
                            InventorySearchMod.MOD_ID,
                            "search_" + providerId.getNamespace() + "_" + providerId.getPath()
                    ),
                    new SearchHighlightPreviewProvider(provider),
                    registry.getItems(provider)
            );
        }
    }
}