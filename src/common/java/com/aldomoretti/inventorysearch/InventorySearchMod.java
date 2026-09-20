package com.aldomoretti.inventorysearch;

import com.aldomoretti.inventorysearch.config.SearchConfig;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventorySearchMod implements ClientModInitializer {
    public static final String MOD_ID = "inventorysearch";
    public static final Logger LOGGER = LoggerFactory.getLogger("inventorysearch");

    @Override
    public void onInitializeClient() {
        SearchConfig.getInstance(); // Crea/legge config/inventorysearch.json
        LOGGER.info("Inventory Search Mod caricata! Autore: AldoMoretti");
    }
}