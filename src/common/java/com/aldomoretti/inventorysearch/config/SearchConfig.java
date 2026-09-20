package com.aldomoretti.inventorysearch.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configurazione centralizzata della mod, caricata da config/inventorysearch.json.
 * Se il file non esiste viene creato automaticamente con i valori predefiniti.
 * I colori accettano "0xRRGGBBAA", "#RRGGBBAA" o numeri decimali.
 */
public final class SearchConfig {
    private static SearchConfig instance;

    // I colori sono stringhe nel file per renderli facili da modificare.
    public String highlightFill = "0x90D8B4FE";
    public String highlightBorder = "0xFFD8B4FE";
    public String highlightCountText = "0xFFD8B4FE";
    public String statusFoundText = "0xFFD8B4FE";
    public String statusNotFoundText = "0xFFFF5555";

    // Dimensioni GUI
    public int searchBarWidth = 100;
    public int searchBarHeight = 14;
    public int clearButtonHeight = 14;
    public int screenPadding = 8;
    public int widgetGap = 2;
    public int statusTextOffset = 10;

    // Limiti
    public int maxNestDepth = 5;
    public int maxQueryLength = 50;

    // Layout slot del preview ShulkerBoxTooltip
    public int previewSlotWidth = 18;
    public int previewSlotHeight = 18;
    public int previewSlotXOffset = 8;
    public int previewSlotYOffset = 8;

    // Interruttori UI
    public boolean showStatusText = true;
    public boolean showSuggestions = true;
    public boolean showClearButton = true;
    public boolean showMatchCount = true;

    public final String _comment = "Inventory Search - configurazione. I colori accettano '0xRRGGBBAA' o '#RRGGBBAA'. Dopo aver modificato il file riavvia Minecraft per applicare le modifiche.";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private SearchConfig() {}

    public static SearchConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static void reload() {
        instance = load();
    }

    public static SearchConfig load() {
        Path configFile = getConfigPath();
        SearchConfig fresh = new SearchConfig();

        if (!Files.exists(configFile)) {
            save(fresh);
            return fresh;
        }

        try {
            String json = Files.readString(configFile);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            merge(fresh, root);
        } catch (IOException | IllegalStateException e) {
            // File illeggibile: si usano i valori predefiniti
        }
        return fresh;
    }

    public static void save(SearchConfig config) {
        Path configFile = getConfigPath();
        try {
            Path parent = configFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(configFile, GSON.toJson(config));
        } catch (IOException e) {
            // Impossibile scrivere la config: non bloccante
        }
    }

    private static void merge(SearchConfig target, JsonObject json) {
        for (String key : json.keySet()) {
            try {
                Field field = SearchConfig.class.getField(key);
                JsonElement value = json.get(key);
                if (value.isJsonPrimitive()) {
                    JsonPrimitive primitive = value.getAsJsonPrimitive();
                    Class<?> type = field.getType();
                    if (type == int.class && primitive.isNumber()) {
                        field.setInt(target, primitive.getAsInt());
                    } else if (type == boolean.class && primitive.isBoolean()) {
                        field.setBoolean(target, primitive.getAsBoolean());
                    } else if (type == String.class && primitive.isString()) {
                        field.set(target, primitive.getAsString());
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
                // Chiavi sconosciute (es. "_comment") o malformate: ignorate
            }
        }
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("inventorysearch.json");
    }

    // Getter colori (parsano la stringa in un int ARGB)
    public int getHighlightFill() {
        return parseColor(this.highlightFill, 0x90D8B4FE);
    }
    public int getHighlightBorder() {
        return parseColor(this.highlightBorder, 0xFFD8B4FE);
    }
    public int getHighlightCountText() {
        return parseColor(this.highlightCountText, 0xFFD8B4FE);
    }
    public int getStatusFoundText() {
        return parseColor(this.statusFoundText, 0xFFD8B4FE);
    }
    public int getStatusNotFoundText() {
        return parseColor(this.statusNotFoundText, 0xFFFF5555);
    }

    private static int parseColor(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        String s = value.trim();
        try {
            if (s.startsWith("#")) {
                s = s.substring(1);
            } else if (s.startsWith("0x") || s.startsWith("0X")) {
                s = s.substring(2);
            }
            if (s.matches("\\d+")) {
                long decimal = Long.parseLong(s);
                return decimal <= 0xFFFFFFFFL ? (int) decimal : fallback;
            }
            return (int) Long.parseLong(s, 16);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}