# InventorySearchEnhanced - Project Primer

## Current state

This workspace is a **single, multi-version Fabric/Loom Gradle mod project** that builds for five Minecraft targets from one source tree: **1.21.8, 1.21.11, 26.1, 26.1.1, 26.1.2**. The target Minecraft version is selected at build time with the `-PmcVersion` Gradle property; each target uses its own compatibility layer (GUI/mixin/integration variants) over a shared core (`src/common`). The mod integrates with ShulkerBoxTooltip to highlight matching items inside container previews.

The original artifact was a binary-only extraction of a 26.1.2 mod build; it has been fully reconstructed as editable source and verified to build for all five versions.

## Mod metadata

- Mod id: `inventorysearch`
- Mod name: `Inventory Search`
- Mod version: `1.0.0` (fixed, `mod_version` in `gradle.properties`)
- Environment: Fabric client mod
- Minecraft target: per-profile (`mcVersion`, default `26.1.2`)
- Java target: `21` for 1.21.x, `25` for 26.1.x (via `options.release`)
- Entry point: `com.aldomoretti.inventorysearch.InventorySearchMod`
- Mixins config: `inventorysearch.mixins.json` (one per codebase; `compatibilityLevel: JAVA_21`, **no refmap** — Loom 1.12+ remaps mixin targets in-place)
- Entry points: `client` (mod initializer) + ShulkerBoxTooltip plugin (`com.aldomoretti.inventorysearch.integration` via SBT's entry point, wired at runtime by SBT)
- **Required dependency**: ShulkerBoxTooltip (version per profile, see matrix below)

## Version matrix

| Profile  | Minecraft | Fabric Loader | Fabric API       | ShulkerBoxTooltip | Java | Loom plugin          |
|----------|-----------|---------------|------------------|-------------------|------|----------------------|
| 1.21.8   | 1.21.8    | 0.16.14       | 0.136.1+1.21.8   | 5.2.12+1.21.8     | 21   | fabric-loom-remap    |
| 1.21.11  | 1.21.11   | 0.17.3        | 0.141.6+1.21.11  | 5.2.16+1.21.11    | 21   | fabric-loom-remap    |
| 26.1     | 26.1      | 0.19.5        | 0.140.3+26.1     | 5.2.18+26.1       | 25   | fabric-loom (puro)   |
| 26.1.1   | 26.1.1    | 0.19.5        | 0.145.4+26.1.1   | 5.4.0+26.1.1      | 25   | fabric-loom (puro)   |
| 26.1.2   | 26.1.2    | 0.19.5        | 0.155.3+26.1.2   | 5.4.0+26.1.1      | 25   | fabric-loom (puro)   |

Notes:
- **1.21.x uses `net.fabricmc.fabric-loom-remap` 1.17.21** (Mojang official mappings, obfuscated MC). Dependencies are remapped dev→official: use `modImplementation`/`modCompileOnly`.
- **26.1+ uses the plain `net.fabricmc.fabric-loom` 1.17.21** (non-obfuscated MC). Use `implementation`/`compileOnly`.
- Both plugins declared with `apply false` and applied conditionally after profile selection.
- ShulkerBoxTooltip has two API generations: **5.2.x** (old API: `draw(x,y,GuiGraphics|GuiGraphicsExtractor,Font,mx,my)`, `getDefaultRendererInstance()` no-arg, no `Theme`) and **5.4.0** (new API: `draw(RenderContext)`, `Theme`, `getActiveSlotCount()`, `MergedItemStack`, `getOutsideXOffset/YOffset`). The integration layer is duplicated per combo.

## Architecture

The mod uses a centralized search state, a lazy per-screen result cache, and ShulkerBoxTooltip's PreviewProvider API for nested-container detection and preview highlighting.

### Core components (`src/common`)

- `InventorySearchMod`: client mod initializer (version-agnostic logging).
- `SearchState`: singleton client-side state holding the current query; read by the SBT renderer variants to drive highlight overlays.
- `config/SearchConfig`: config from `config/inventorysearch.json` — search bar dimensions/position, clear button, highlight colors (fill/border/count/status), preview slot offsets, `showSuggestions`/`showMatchCount`/`showStatusText` toggles. Auto-created on first run.
- `util/SearchQueryParser`: parses the query and exposes parameter suggestions (used by `SuggestionMenu`).
- `util/ItemMatcher`: matching of items/queries; **lore-agnostic** via `DataComponents.LORE` (`lore.lines()`, no named `ItemLore`/`LoreContents` classes) so it compiles on 5.2.x and 5.4.x API families.
- `util/EnchantMatcher`: enchantment matching (^ench query operations).
- `util/ContainerType`: enum (SHULKER, CHEST, ENDER_CHEST, BUNDLE, OTHER).
- `util/SearchResult`: slot index, container type, matched slot indices, match count.
- `util/ContainerScanner`: scans menu slots via SBT PreviewProvider API with bundle DataComponents fallback.
- `resources/assets/inventorysearch`: icon + `en_us.json`/`it_it.json` lang.

### GUI + mixins (per MC codebase)

- `gui/SearchBarWidget` — extends `EditBox`, hosts `SuggestionMenu`.
- `gui/SuggestionMenu` — parameter suggestion popup.
- `gui/ClearSearchButton` — resets the query.
- `mixin/AbstractContainerScreenMixin` — injects the search bar/clear button, `@Redirect` on `KeyMapping.matches` (so typing in the bar doesn't trigger keybinds), slot highlight rendering + match-count labels + status text, and key/mouse routing when the bar is focused.
- `mixin/KeyboardHandlerMixin` — forwards `charTyped` to the focused search bar.
- `mixin/ScreenAccessor` — `@Invoker(addRenderableWidget)`.

Codebase per MC family:
- `src/legacy` — MC 1.21.8: `ResourceLocation`, `GuiGraphics`, classic int-key input (`keyPressed(int,int,int)` etc.).
- `src/legacy11` — MC 1.21.11: `Identifier`, `GuiGraphics`, record events (`KeyEvent`/`CharacterEvent`/`MouseButtonEvent`), `AbstractButton.renderContents`, `EditBox.keyPressed(KeyEvent)`.
- `src/modern` — MC 26.1+: `Identifier`, `GuiGraphicsExtractor`, record events, `EditBox.keyPressed(KeyEvent)`.

### ShulkerBoxTooltip integration (per SBT variant)

- `IntegrationSearchTooltipPlugin` — SBT plugin; wraps every registered `PreviewProvider` in a `SearchHighlightPreviewProvider` (priority +500) under an `inventorysearch:*` id.
- `SearchHighlightPreviewProvider` — decorator delegating all provider methods (and `getTheme()`/active-slot methods where the 5.4.0 API has them).
- `SearchHighlightPreviewRenderer` — draws purple fill/border overlays on slots whose stack matches the active query.

Variants:
- `src/sbt-legacy` — 5.2.x, MC 1.21.8 (`ResourceLocation`, `GuiGraphics`).
- `src/sbt-legacy11` — 5.2.x, MC 1.21.11 (`Identifier`, `GuiGraphics`).
- `src/modern-sbt-old` — 5.2.x, MC 26.1 (`Identifier`, `GuiGraphicsExtractor`, no `Theme`).
- `src/sbt-modern` — 5.4.0, MC 26.1.1/26.1.2 (`RenderContext`, `Theme`, `MergedItemStack` compact mode).

## Search flow

1. User types in the search bar (bottom-right corner of the screen).
2. `SearchState` updates with the query.
3. `ContainerScanner` scans menu slots via SBT PreviewProvider API.
4. Results cached in `AbstractContainerScreenMixin`'s `HashMap` keyed by slot index (O(1) lookup).
5. Purple highlights drawn on matching slots; container matches show a match-count label; status text shows found/not-found summary.
6. With an item hovered, SBT renders a preview where matching inner items are highlighted.

## Project structure

```
build.gradle          multi-profile definition + conditional loom plugin + composed sourceSets
settings.gradle       plugin/env management, rootProject name
gradle.properties     mcVersion (default 26.1.2), mod_version 1.0.0, maven_group
gradlew / gradlew.bat Gradle 9.5.1 wrapper
src/
  common/java|resources               shared logic + lang/icon
  legacy / legacy11 / modern          GUI + mixins for 1.21.8 / 1.21.11 / 26.1+
  sbt-legacy / sbt-legacy11           SBT 5.2.x integration (1.21.8 / 1.21.11)
  modern-sbt-old                      SBT 5.2.x integration (26.1)
  sbt-modern                          SBT 5.4.0 integration (26.1.1 / 26.1.2)
```

Each codebase also holds its own `resources/` → `fabric.mod.json` (per-version ranges, `processResources` expands `${version}`/`${minecraftRange}`/`${loaderRange}`/`${javaRange}`) and `inventorysearch.mixins.json`.

## Dependencies (per version)

- **Fabric Loader**: see matrix (`modImplementation` on 1.21.x, `implementation` on 26.1+).
- **Fabric API**: see matrix.
- **ShulkerBoxTooltip**: see matrix (`modCompileOnly` on 1.21.x so it's remapped to official mojmap; `compileOnly` on 26.1+). Fabric API/Loader/SBT are `compileOnly`-level for the mod's own builds; runtime require the real mods installed (fabric.mod.json `depends`).

## Ender chest support

Ender chest previews require ShulkerBoxTooltip's server integration. The mod uses whatever inventory data SBT exposes via its PreviewProvider API; no server-side logic is implemented.

## Installation (per Minecraft version)

1. Install Fabric Loader for the target MC version.
2. Install Fabric API.
3. Install the matching ShulkerBoxTooltip version.
4. Install the built Inventory Search jar.

## How to build

Requires JDK 25 (also builds the Java-21 legacy profiles via `options.release`).

```powershell
$env:JAVA_HOME = 'C:\Users\LoloCass\.jdks\jdk-25\jdk-25.0.4.1+1'
.\gradlew.bat build "-PmcVersion=1.21.8"   # oppure 1.21.11, 26.1, 26.1.1, 26.1.2
```

- **Windows**: always quote `-PmcVersion=...` — cmd/PowerShell split unquoted arguments on `.` and `=`, truncating e.g. `26.1.2` to `26`.
- Override the default in `gradle.properties` (`mcVersion`) if preferred; the `MC_VERSION` env var approach was dropped (config-cache/daemon env snapshots made it unreliable).
- For legacy (1.21.x) the final artifact is produced by `remapJar` (run `build`), not the bare `jar` task.
- Output: `build/libs/inventory-search-enhanced-1.0.0.jar` (+ sources jar). One build overwrites the previous profile's jar.

## Features

- Compact search bar bottom-right; configurable position/size.
- Real-time search, instant feedback; matches re-scanned on query change.
- Purple highlight fill `0x90D8B4FE`, border `0xFFD8B4FE`; count labels and status text (found/none).
- Container previews with matching inner items highlighted (SBT).
- Nested containers up to 5 levels deep.
- English/Italian localization.
- Clear search button; search-bar focused once registered, `?` triggers a parameter suggestion menu (`showSuggestions`).