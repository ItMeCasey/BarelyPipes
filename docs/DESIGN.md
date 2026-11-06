# Barely Pipes design notes

Barely Pipes is the pipe bundle of AeroFlux as a standalone mod. Seven pipes share one block (the bundle): fluid, item,
redstone, energy, gas, ME (AE2) and storage (BarelyStorage). Dense ME replaces ME in the ME lane.

## Bundle lanes

A bundle has one lane per network type: fluid 0, item 1, redstone 2, energy 3, gas 4, ME or dense ME 5, storage 6.
AeroFlux had six lanes; the storage pipe is the seventh. `ConduitLayout` already spreads pipes over a face in a
grid of up to nine spots, so seven pipes fit without a new layout. Packing changed to fit seven lanes: the pipe set key
uses 4 bits per lane, tiers 2 bits per lane.

## Tiers

Three tiers, matching the generated textures (`_t1`, `_t2`, `_t3`): iron (light gray, `#C8C8C8`), gold (`#CFA000`),
diamond (`#2A8FB5`). They use AeroFlux's iron, gold and diamond numbers (its copper, netherite and fluxium tiers are gone):

| | iron | gold | diamond |
|---|---|---|---|
| fluid, mB per tick | 250 | 650 | 1,800 |
| energy, FE per tick | 4,000 | 16,000 | 64,000 |
| gas, mB per tick | 800 | 2,400 | 7,000 |
| items per operation | 4 | 8 | 16 |
| ticks between operations | 16 | 12 | 8 |

Only fluid, item, energy and gas pipes have tiers. Redstone, ME, dense ME and storage pipes have one set of textures.
Every tiered pipe has three upgrade slots (speed and capacity). Redstone carries one channel (AeroFlux only ever wired up
channel 0). The storage pipe runs at the gold item pipe's rate (`PipeTiers.STORAGE_TIER`) and takes no speed or capacity
upgrades, but it takes Filter and Priority upgrades per face like every pipe.

## Storage pipe

BarelyStorage exposes no item capability: its racks, cables and nodes are linked through its own `NetNode` interface and
`StorageNetwork`. The storage pipe therefore attaches to any block that is a BarelyStorage `NetNode` on that side, and
also to ordinary inventories, so items can move network to inventory, inventory to network and network to network. A face
on a network block gives any item the network holds (a Filter upgrade picks which) and takes whatever the racks have room
for. `StorageMover` does the moving; `StorageLink` is the only class that names BarelyStorage, loaded only when
BarelyStorage is installed.

BarelyStorage only has 26.1.2, 26.2 and 26.3 builds, so on 1.20.1 and 1.21.1 the storage pipe item exists but has no recipe
and joins inventories only (the same way the gas pipe is inert without Mekanism).

## Gas pipe and Mekanism

The gas pipe carries Mekanism chemicals through a soft dependency. `MekanismCompat` checks `ModList` and is the only class the rest of the mod calls; `MekanismChemicals` (1.21.1, one chemical handler capability) or `MekanismGas` (1.20.1, Mekanism 10.4's gas handler) and `ChemicalMover` are the classes that name Mekanism, reached only after `loaded()` is true. `Connections.attaches` asks `MekanismCompat.offersChemicals` or `offersGas`; `GasMover` runs `ChemicalMover`, which picks one chemical per step and shares it out with `AmountMover` at the PipeTiers gas rate, with Priority upgrades. The Filter Upgrade is not offered on gas pipes, so AeroFlux's chemical-from-held-item filter code was not carried over. Mekanism exists only for 1.20.1 (Forge and NeoForge) and 1.21.1 (NeoForge): on the other seven builds there is no Mekanism code and the gas pipe is inert. In the build, Mekanism is compile-only; `-PdevTools` adds it to the dev run and `-PnoMekanism` takes it out again.

## Items

Pipes (one item per pipe type and tier), Pipe Wrench (was Flux Wrench), Conduit Tool, Speed, Capacity, Filter and Priority
Upgrades. Recipes use vanilla ingredients only (ME pipes are conditional on AE2).

## Layout

`common/` holds Minecraft-free code (`core/conduit`), the unit tests, and the resources every build shares (pipe textures,
GUI glyphs, item models, lang). `<loader>-<mc>/` holds each build: the Minecraft code, the blockstate and the recipes.
The loaders differ too much for a shared version layer (AeroFlux keeps all its modules separate for the same reason).
`scripts/make_assets.py` writes the resources, `scripts/port_aeroflux.py` was the one-time seed from AeroFlux.

## Look-at overlays (SMWTI)

Show Me What This Is (SMWTI, our own overlay) must say exactly which pipe is under the crosshair, not just "Pipe Bundle". The words come from `core/conduit/PipeLookInfo` in `common/`: it takes the bundle, its endpoint settings and the hit point in block pixels, asks `ConduitLayout.pick` for the piece under the point (the same resolver the wrench and tools use: a plug before an arm before a core, the junction box resolving to its nearest pipe) and returns the pipe, its tier, the part (arm, core, plug, junction), the side, and detail lines as `Text` (a lang key plus arguments). The loader supplies only an `Environment`: item names, the neighbor block's name and the throughput line from `PipeConfigActions.rate`, so the overlay never promises more than the pipe delivers. All keys are `smwti.barelypipes.*` in `make_assets.py lang()`.

Per build the module-specific files are `compat/look/PipeLookAdapter` (names, rate, `Text` to `Component`) and `compat/smwti/BarelyPipesSmwtiPlugin` plus `BarelyPipesSmwtiClient` (the SMWTI API: a block override for the title and icon and a client block provider for the lines). The client has the whole bundle, so no server provider is needed. The plugin class is found by SMWTI through its annotation and is never loaded without SMWTI; the API jar (`libs/smwti-api-<mc>.jar`) is compile-only. Done on NeoForge 26.3 first; the other builds follow the same two files. Jade and WTHIT plugins do not exist yet: AeroFlux had them, but they listed every pipe of the bundle instead of the one aimed at, so a port should reuse `PipeLookAdapter` for the lines.
