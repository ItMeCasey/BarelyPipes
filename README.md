# Barely Pipes

Seven pipes in one block for Minecraft on Forge and NeoForge. It is the pipe system of AeroFlux as a mod of its own: a pipe is two pixels thin and up to seven different pipes share one block, the pipe bundle, so one run carries items, fluid, energy, gas, a redstone signal and an ME network side by side. Every pipe has a connection mode on each face, three tiers, upgrades, and a screen to set it all up. The Pipe Wrench and the Conduit Tool edit single faces and whole runs.

Docs and recipes: https://itmecasey.com/wiki/barelypipes/ (once published). Listing text: [`curseforge/DESCRIPTION.md`](curseforge/DESCRIPTION.md).

## Versions and jars

Minecraft 1.20.1, 1.21.1, 26.1.2, 26.2 and 26.3, on Forge and NeoForge (ten builds). Release version `2026.11.1`.

| Minecraft | Jar | Loader |
|---|---|---|
| 26.3, 26.2, 26.1.2, 1.21.1, 1.20.1 | `barelypipes-forge-mc<mc>-2026.11.1.jar` | Forge |
| 26.3, 26.2, 26.1.2, 1.21.1, 1.20.1 | `barelypipes-neoforge-mc<mc>-2026.11.1.jar` | NeoForge |

There is no combined jar: Forge and NeoForge builds of the same Minecraft version use the same class names with different code, so each release file is built for one loader and one Minecraft version.

## Install

Put the jar for your Minecraft version and loader into `mods` on the server and on every client. No other mod is required.

Optional mods unlock their pipes:

- **BarelyStorage** (Minecraft 26.1.2, 26.2 and 26.3 only): the Storage Pipe joins its racks, cables and nodes. It has no recipe on 1.20.1 and 1.21.1, where it can only join ordinary inventories.
- **Applied Energistics 2:** the ME Pipe and Dense ME Pipe join AE2 grids (their recipes load only with AE2).
- **Mekanism:** the Gas Pipe carries Mekanism gases. It works on NeoForge 1.21.1 and on Forge and NeoForge 1.20.1 (the builds where AeroFlux had Mekanism support). On the other builds the Gas Pipe item exists but has nothing to carry.

## Features

- **Seven pipes, one block:** fluid, item, redstone, energy, gas, ME (Dense ME takes its place) and storage. A bundle holds at most one of each.
- **Three tiers** (iron, gold, diamond) for item, fluid, energy and gas pipes. Using a higher-tier pipe on a bundle upgrades that pipe in place.

  | | iron | gold | diamond |
  |---|---|---|---|
  | Fluid, mB per tick | 250 | 650 | 1,800 |
  | Energy, FE per tick | 4,000 | 16,000 | 64,000 |
  | Gas, mB per tick | 800 | 2,400 | 7,000 |
  | Items per operation | 4 | 8 | 16 |
  | Ticks between operations | 16 | 12 | 8 |

  Redstone (one channel), ME (8 channels), Dense ME (32 channels) and Storage (the gold item pipe's rate) pipes have a single set.
- **Per-face connection modes:** Open, Import, Export, Off and Blocked. **Distribution modes:** round robin, first to last, last to first, random.
- **Upgrades:** Speed and Capacity (three slots per tiered pipe, whole pipe), Filter (item and fluid pipes, one per face) and Priority (one per face, -5 to +5).
- **Pipe screen:** right-click a pipe where it meets a face.
- **Pipe Wrench:** cycles a face's mode; sneak cycles the distribution mode.
- **Conduit Tool:** Edit, Remove, Remove run (straight line, up to 64 blocks), Copy, Paste (also along a run) and Inspect.
- **Storage Pipe:** moves items between BarelyStorage networks and inventories in every direction.
- **ME Pipes:** carry a real AE2 grid.
- Protection: the Conduit Tool asks the standard permission events before it changes or removes pipes.

## Config

`config/barelypipes-common.toml`, created on first start:

| Key | Default | What it does |
|---|---|---|
| `debugLogging` | `false` | Logs extra detail about pipe networks |
| `protectionEnabled` | `true` | The Conduit Tool asks the standard permission events (claim mods) before it acts |

These are the options of the NeoForge and Forge 26.1.2 builds. The builds that are still being ported carry AeroFlux's larger config until they are done. Tier rates are built in and not configurable yet.

## What was checked

<!-- TESTED: update this section after the client checks. It is one of three places that state what was verified
     (the others are curseforge/DESCRIPTION.md and the wiki page known-gaps). -->

- Shared pipe logic (`common/`): 98 unit tests, passing.
- NeoForge 26.1.2: builds and passes the headless server self test (219 checks: item, fluid, energy, gas and redstone transfer, tool actions, the pipe screen's server side, and the Storage Pipe against a real BarelyStorage rack, both directions).
- The other nine builds (Forge 26.1.2, Forge and NeoForge 26.2, 26.3, 1.21.1 and 1.20.1) are being ported and have not been checked.
- Nothing has been seen in a real game client: how the pipes, the pipe screen and the textures look in game, multiplayer, AE2 grids with real devices, Mekanism gas and long sessions are all unchecked.
- Not integrated yet: item viewers (BEI, REI, JEI, EMI) and in-world info (Show Me What This Is, Jade).

## Build and test

One module per loader and Minecraft version (`neoforge-26.1.2`, `forge-1.20.1`, and so on) sharing `common/` (Minecraft-free logic, the unit tests and the resources every build uses). Run Gradle inside a module:

```
cd neoforge-26.1.2
./gradlew build                                  # jar in build/libs, runs the unit tests
cd ..
python scripts/server_check.py neoforge-26.1.2   # headless self test, prints SERVER PASS or SERVER FAIL
```

`python scripts/server_check.py all` runs every layer. `python scripts/make_assets.py <mc> <forge|neoforge>` rewrites the textures, models, lang and recipes of a module. Development tools (the self test) build only with `-PdevTools`. [`docs/DESIGN.md`](docs/DESIGN.md) has the design notes and [`docs/PORTING.md`](docs/PORTING.md) how a module is ported.

Branding: `python branding/source/make_icon.py`, then `python branding/source/render_banners.py` (Pillow and Google Chrome).

## Version

`YYYY.M.n`, the same in every module's `gradle.properties`. Releases are tags `v<version>`.

## Links

- Wiki: https://itmecasey.com/wiki/barelypipes/
- Issues: https://github.com/ItMeCasey/BarelyPipes/issues
- Source: https://github.com/ItMeCasey/BarelyPipes

License: MIT.
