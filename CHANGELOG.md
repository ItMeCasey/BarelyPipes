# Changelog

## Unreleased (NeoForge 1.21.1 jar only so far)

- New: Jade and WTHIT plugins on NeoForge 1.21.1 show the exact pipe under the crosshair (type and tier, side and part, mode, connection, throughput, upgrades, distribution), the same lines as the Show Me What This Is overlay. Seen in a real client with Jade and with WTHIT. The other builds have them next.

## 2026.11.2

- Show Me What This Is (all ten builds): the overlay now names the exact pipe you look at, such as Gold Item Pipe, Diamond Energy Pipe or Storage Pipe, with that pipe's item as the icon, instead of "Pipe Bundle". Detail lines give the side and whether you are on an arm, the core or a plug, the mode, what the side connects to, the throughput, Speed and Capacity upgrades, Filter and Priority; the core also lists the other pipes of the bundle.
- Fixed: the side names (Down, Up, North, South, West, East) were missing from the language file, so the Pipe Wrench and Conduit Tool messages showed raw keys.
- Fixed: the outline and collision of pipes, plugs and the bundle now match what is drawn (arms were drawn 2 pixels wide but outlined 3), and a click on a plug, an arm or the plate beside a plug picks that pipe and face. The Pipe Wrench and Conduit Tool act on the face the arm or plug points to, not on the side of it you happened to click.
- Fixed: the Gas Pipe's plug glyph was nearly invisible (gold and iron on a yellow field); it now sits on a dark field like the Item Pipe's. The Storage glyph in the pipe screen was off-white and half a pixel off center; it is now a centered white 13 by 13 picture.
- Fixed: glyphs in the pipe screen are centered to the pixel in their buttons (mode and distribution buttons are 21 pixels, the Priority buttons 17, so the 13 pixel picture has equal margins).
- New: plugs show arrows beside their glyph: pointing toward the glyph for Import (into the pipe), away for Export (out of the pipe), both for Open. Off shows none. They update when the mode changes.

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
- ME pipes join Applied Energistics 2 grids; the Gas Pipe joins Mekanism tanks and machines and moves their gases and chemicals, on Minecraft 1.20.1 (Forge and NeoForge) and 1.21.1 (NeoForge). Mekanism has no build for 1.21.1 Forge, 26.1.2, 26.2 or 26.3, so on those seven builds the Gas Pipe item exists but is inert.
- Recipes with vanilla ingredients for every pipe, tool and upgrade (ME pipe recipes load only with AE2).
- Config file `config/barelypipes-common.toml` with `debugLogging` and `protectionEnabled`.
- Forge and NeoForge on Minecraft 1.20.1, 1.21.1, 26.1.2, 26.2 and 26.3. The release is ten jars, one per loader and Minecraft version, named `barelypipes-<loader>-mc<mc>-<version>.jar`.
- Not in this version: item viewer plugins (BEI, REI, JEI, EMI), Show Me What This Is and Jade plugins, configurable tier rates.
