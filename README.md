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
- **Mekanism** (Minecraft 1.20.1 on Forge and NeoForge, and 1.21.1 on NeoForge only): the Gas Pipe carries Mekanism gases and chemicals between tanks and machines. Mekanism has no build for 1.21.1 Forge, 26.1.2, 26.2 or 26.3, so on those seven builds the Gas Pipe item exists and its recipe loads, but it is inert and connects to nothing.

## Features

- **Seven pipes, one block:** fluid, item, redstone, energy, gas, ME (Dense ME takes its place) and storage. A bundle holds at most one of each.
- **Three tiers** (iron, gold, diamond) for item, fluid, energy and gas pipes. Using a higher-tier pipe on a bundle upgrades that pipe in place.

  | | iron | gold | diamond |
  |---|---|---|---|
  | Fluid, mB per tick | 250 | 650 | 1,800 |
  | Energy, FE per tick | 4,000 | 16,000 | 64,000 |
  | Gas, mB per tick (needs Mekanism) | 800 | 2,400 | 7,000 |
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

Every build has these two options. Tier rates are built in and not configurable yet.

## What was checked

- Shared pipe logic (`common/`): 98 unit tests, passing in every module.
- All ten builds compile and pass the headless server self test (219 checks on 26.x: item, fluid, energy and redstone transfer, tool actions, the pipe screen's server side, and on 26.x the Storage Pipe against a real BarelyStorage rack, both directions; 213 checks on 1.20.1 and 1.21.1, 220 there with Mekanism installed, which adds the gas checks).
- All ten builds ran a scripted client tour in a real game (2026-11-06) and the screenshots were read (`docs/frames/<module>/`): pipe models, tier colors, plates and plugs, the pipe screen and, on 26.x, the Storage Pipe next to a BarelyStorage rack. A few frames of Forge 26.1.2 and Forge 26.2 were not read.
- The Gas Pipe was checked against real Mekanism tanks on the three builds that have Mekanism (Forge 1.20.1, NeoForge 1.20.1, NeoForge 1.21.1): in the headless self test 40,000 mB of hydrogen moves from one Mekanism chemical tank to another through three iron gas pipes, all of it arrives, and no tick moves more than 800 mB (7 more checks there, 220 in all; the same layers pass with Mekanism left out, 213 checks). The client tour on those three builds shows the yellow gas pipe joining two Mekanism tanks and the gas pipe screen. The tank models in those frames do not show their contents, so the transfer itself is proven by the self test numbers, not by a picture. Mekanism machines other than the chemical tank, the Priority Upgrade on a gas pipe against Mekanism, and Mekanism's other chemical kinds (infusion types, pigments, slurries on 1.21.1) were not tried.
- Not checked: multiplayer, long sessions, and ME pipes against real AE2 devices.
- Not integrated yet: item viewers (BEI, REI, JEI, EMI) and in-world info (Show Me What This Is, Jade).

## Build and test

One module per loader and Minecraft version (`neoforge-26.1.2`, `forge-1.20.1`, and so on) sharing `common/` (Minecraft-free logic, the unit tests and the resources every build uses). Run Gradle inside a module:

```
cd neoforge-26.1.2
./gradlew build                                  # jar in build/libs, runs the unit tests
cd ..
python scripts/server_check.py neoforge-26.1.2   # headless self test, prints SERVER PASS or SERVER FAIL
```

`python scripts/server_check.py all` runs every layer. `python scripts/make_assets.py <mc> <forge|neoforge>` rewrites the textures, models, lang and recipes of a module. Development tools (the self test) build only with `-PdevTools`. On `forge-1.20.1`, `neoforge-1.20.1` and `neoforge-1.21.1`, `-PdevTools` also puts Mekanism into the dev run (it is compile-only otherwise, so it is never in the jar or required) and the self test adds the gas checks; add `-PnoMekanism` to leave it out (with `server_check.py`: set `BARELYPIPES_NO_MEKANISM=1`) and check that the mod loads and passes without Mekanism. [`docs/DESIGN.md`](docs/DESIGN.md) has the design notes and [`docs/PORTING.md`](docs/PORTING.md) how a module is ported.

Branding: `python branding/source/make_icon.py`, then `python branding/source/render_banners.py` (Pillow and Google Chrome).

## Version

`YYYY.M.n`, the same in every module's `gradle.properties`. Releases are tags `v<version>`.

## Links

- Wiki: https://itmecasey.com/wiki/barelypipes/
- Issues: https://github.com/ItMeCasey/BarelyPipes/issues
- Source: https://github.com/ItMeCasey/BarelyPipes

License: MIT.
