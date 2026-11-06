![Barely Pipes](https://raw.githubusercontent.com/ItMeCasey/BarelyPipes/main/branding/banners/banner.png)

**Seven pipes in one block for Forge and NeoForge: items, fluids, energy, gas, redstone, AE2 ME and BarelyStorage.**

Barely Pipes is the pipe system of AeroFlux as a mod of its own. A pipe is two pixels thin, and up to seven different pipes share one block, the pipe bundle, so one run can carry items, fluid, power, a redstone signal and an ME network side by side. Each pipe has its own mode on every face, three tiers, upgrades, and two tools to edit whole runs. It has no machines and no ores: it moves what other blocks already hold.

Docs and recipes: [itmecasey.com/wiki/barelypipes](https://itmecasey.com/wiki/barelypipes/)

![Features](https://raw.githubusercontent.com/ItMeCasey/BarelyPipes/main/branding/banners/h_features.png)

- **One block, seven pipes:** a bundle holds at most one pipe of each kind. Pipes join the same kind next to them and attach to any block that has what they carry.
- **Per-face modes:** every pipe has Open, Import, Export, Off or Blocked on each of the six faces of its bundle.
- **Distribution modes:** round robin, first to last, last to first or random decide who is served first.
- **Three tiers:** iron, gold and diamond for item, fluid, energy and gas pipes. A higher-tier pipe used on a bundle upgrades the pipe in place and keeps its settings.
- **Upgrades:** Speed and Capacity raise a whole pipe (three slots). Filter and Priority work on one face: Filter whitelists or blacklists items and fluids, Priority ranks destinations from -5 to +5.
- **A pipe screen:** right-click a pipe where it meets a face for a screen made of glyphs with modes, distribution, throughput, upgrade slots, filter and priority.
- **Two tools:** the Pipe Wrench cycles a face's mode (sneak: the distribution mode), and the Conduit Tool edits, removes, removes a whole run, copies, pastes (also along a run) and inspects.
- **Claim friendly:** the Conduit Tool asks the standard permission events before it changes or removes pipes, so claim mods can veto it.

![The pipes](https://raw.githubusercontent.com/ItMeCasey/BarelyPipes/main/branding/banners/h_pipes.png)

- **Item Pipe:** 4, 8 or 16 items per operation, every 16, 12 or 8 ticks.
- **Fluid Pipe:** 250, 650 or 1,800 mB per tick, any fluid.
- **Energy Pipe:** 4,000, 16,000 or 64,000 FE per tick between any blocks that take Forge Energy.
- **Gas Pipe:** 800, 2,400 or 7,000 mB per tick of Mekanism gas. Needs Mekanism, which exists for only some of our builds (see Compatibility).
- **Redstone Pipe:** carries one redstone signal along the run.
- **ME Pipe and Dense ME Pipe:** join Applied Energistics 2 grids with 8 channels (32 for Dense). The recipes only load with AE2.
- **Storage Pipe:** joins BarelyStorage networks and ordinary inventories at a fixed speed. The recipe only exists on Minecraft 26.1.2, 26.2 and 26.3, where BarelyStorage exists; on 1.20.1 and 1.21.1 the pipe can only join inventories.

![How the tiers compare](https://raw.githubusercontent.com/ItMeCasey/BarelyPipes/main/branding/banners/tiers.png)

![Tools](https://raw.githubusercontent.com/ItMeCasey/BarelyPipes/main/branding/banners/h_tools.png)

- **Pipe Wrench:** use it on a pipe to cycle that face through Open, Import, Export, Off and Blocked. Sneak and use it to cycle the distribution mode instead. It picks the pipe nearest your crosshair, so you do not have to hit a two-pixel pipe exactly.
- **Conduit Tool:** sneak and use it in the air to change mode. Edit (like the wrench), Remove (sneak: the whole bundle), Remove run (two clicks, a straight line of up to 64 blocks), Copy, Paste (it also adds missing pipes from your inventory, and sneak-use marks the two ends of a run to paste along) and Inspect (describes the network in chat and outlines it).

![Compatibility](https://raw.githubusercontent.com/ItMeCasey/BarelyPipes/main/branding/banners/h_compat.png)

- **BarelyStorage** (our storage mod, Minecraft 26.1.2, 26.2 and 26.3): the Storage Pipe moves items between its racks, nodes and cables, ordinary inventories, and other networks.
- **Applied Energistics 2:** ME pipes carry a real ME grid. Recipes load only when AE2 is installed.
- **Mekanism:** gas pipes work with Mekanism on NeoForge 1.21.1 and on Forge and NeoForge 1.20.1. On the other builds the Gas Pipe item exists but has nothing to carry.
- **Forge Energy and the standard fluid and item handlers:** work with any mod that speaks them.
- Item viewers (BEI, REI, JEI, EMI) and the in-world info mods Show Me What This Is and Jade have no plugin yet. This is on the plan, not done.

Install: put the jar for your Minecraft version in `mods` on the server and on every client. One jar per version holds both loaders on 26.3, 26.2, 26.1.2 and 1.21.1; on 1.20.1 take the Forge jar or the NeoForge jar to match your loader.

The pictures on this page are illustrations drawn for the listing, not game screenshots.

**Tested:** the shared pipe logic passes 98 unit tests, and the NeoForge 26.1.2 build passes a headless server self test of 219 checks (item, fluid, energy, gas and redstone transfer, the tools, the pipe screen's server side, and the Storage Pipe moving items in both directions against a real BarelyStorage rack). The other builds are still being ported and have not been run. Nothing has been seen in a real game client yet: how the pipes, screens and textures look in game, multiplayer, AE2 grids with real devices, Mekanism gas in a real game and long play sessions are all unchecked.

![FAQ](https://raw.githubusercontent.com/ItMeCasey/BarelyPipes/main/branding/banners/h_faq.png)

**Which jar do I use?** One jar per Minecraft version holds both loaders for 26.3, 26.2, 26.1.2 and 1.21.1 (`barelypipes-mc<version>-<release>.jar`). On 1.20.1 there is one jar for Forge and one for NeoForge.

**Does it need to be on the server?** Yes, on the server and on every client.

**Does it need another mod?** No. BarelyStorage, Applied Energistics 2 and Mekanism are optional and only unlock their own pipes.

**Why can I not craft the Storage Pipe on 1.21.1?** BarelyStorage has no build for 1.20.1 or 1.21.1, so the pipe has no recipe there and can only join inventories.

**Can I change the numbers?** Not yet. The tier rates are built in. The config file has two switches: extra logging and the Conduit Tool claim check.

**Fabric?** Not at this time.

**Can I use it in my modpack?** Yes. It is MIT licensed.

Made by ItMeCasey Studios. Source and issues on [GitHub](https://github.com/ItMeCasey/BarelyPipes).
