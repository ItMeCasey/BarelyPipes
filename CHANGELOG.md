# Changelog

## 2026.11.1

First version. Barely Pipes is the pipe bundles of AeroFlux as a mod of its own.

- One block, the pipe bundle, holding up to seven pipes: fluid, item, redstone, energy, gas, ME (Dense ME takes its place) and storage.
- Three tiers (iron, gold, diamond) for item, fluid, energy and gas pipes. Using a higher-tier pipe on a bundle upgrades the pipe in place and keeps its settings.
  - Fluid 250, 650 and 1,800 mB per tick; energy 4,000, 16,000 and 64,000 FE per tick; gas 800, 2,400 and 7,000 mB per tick; items 4, 8 and 16 per operation every 16, 12 and 8 ticks.
- Redstone pipe (one channel), ME pipe (8 channels) and Dense ME pipe (32 channels) with a single set of textures.
- New Storage Pipe: joins BarelyStorage racks, cables and nodes and ordinary inventories, at the gold item pipe's rate. Recipe on Minecraft 26.1.2, 26.2 and 26.3 only (BarelyStorage has no other builds).
- Per-face connection modes (Open, Import, Export, Off, Blocked) and distribution modes (round robin, first to last, last to first, random).
- Speed and Capacity upgrades for a whole pipe (three slots) and Filter and Priority upgrades for one face.
- The pipe screen with glyphs: face mode, distribution, throughput, upgrade slots, filter and priority.
- Pipe Wrench: cycles a face's mode, or the distribution mode while sneaking.
- Conduit Tool: Edit, Remove, Remove run, Copy, Paste (also along a run) and Inspect, with a claim check through the standard permission events.
- ME pipes join Applied Energistics 2 grids; the Gas Pipe does not work yet on any build (Mekanism support was not carried over from AeroFlux, so it connects to nothing; known gap).
- Recipes with vanilla ingredients for every pipe, tool and upgrade (ME pipe recipes load only with AE2).
- Config file `config/barelypipes-common.toml` with `debugLogging` and `protectionEnabled`.
- Forge and NeoForge on Minecraft 1.20.1, 1.21.1, 26.1.2, 26.2 and 26.3. The release is ten jars, one per loader and Minecraft version, named `barelypipes-<loader>-mc<mc>-<version>.jar`.
- Not in this version: item viewer plugins (BEI, REI, JEI, EMI), Show Me What This Is and Jade plugins, configurable tier rates.
