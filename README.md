# InventorySearchEnhanced

Mod Fabric client per cercare item nell'inventario e nei contenitori (shulker box, casse, ecc. via ShulkerBoxTooltip).

## Versioni supportate

| Profilo   | Minecraft   | Fabric Loader | Fabric API       | ShulkerBoxTooltip | Java |
|-----------|-------------|---------------|------------------|-------------------|------|
| 1.21.8    | 1.21.8      | 0.16.14       | 0.136.1+1.21.8   | 5.2.12+1.21.8     | 21   |
| 1.21.11   | 1.21.11     | 0.17.3        | 0.141.6+1.21.11  | 5.2.16+1.21.11    | 21   |
| 26.1      | 26.1        | 0.19.5        | 0.140.3+26.1     | 5.2.18+26.1       | 25   |
| 26.1.1    | 26.1.1      | 0.19.5        | 0.145.4+26.1.1   | 5.4.0+26.1.1      | 25   |
| 26.1.2    | 26.1.2      | 0.19.5        | 0.155.3+26.1.2   | 5.4.0+26.1.1      | 25   |

Il profilo di default è `26.1.2` (vedi `gradle.properties`, chiave `mcVersion`).

## Build

```bat
:: Windows: quotare SEMPRE l'argomento -PmcVersion (cmd/PS spezzano su . e =)
set JAVA_HOME=C:\Users\LoloCass\.jdks\jdk-25
gradlew.bat build "-PmcVersion=1.21.8"
```

```sh
# Linux/macOS
export JAVA_HOME=/path/to/jdk-25
./gradlew build "-PmcVersion=26.1.2"
```

L'artefatto è `build/libs/inventory-search-enhanced-1.0.0.jar`.

## Struttura delle sorgenti

- `src/common` — codice condiviso tra tutte le versioni (logica di ricerca, config, ItemMatcher lore-agnostico, lang, icona).
- `src/legacy`, `src/legacy11` — GUI e mixin per MC 1.21.8 / 1.21.11 (mapping official, plugin `fabric-loom-remap`).
- `src/modern` — GUI e mixin per MC 26.1+ (non-ofuscato, plugin `fabric-loom`).
- `src/sbt-*`, `src/modern-sbt-old` — integrazione ShulkerBoxTooltip per API vecchia (5.2.x) e nuova (5.4.x).

Vedi `project_primer.md` per dettagli storici della struttura.