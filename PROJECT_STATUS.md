# Project Status
<!-- status-format: 1 -->
Updated: 2026-11-09
Summary: Barely Pipes 2026.11.2 is released as ten beta jars on CurseForge and GitHub (all ten builds pass unit tests, the headless self test and CI); on NeoForge 1.21.1 the code now also has Jade/WTHIT plugins (unreleased) and was checked against real AE2, JEI/REI/EMI/BEI, Mekanism tank screens and a dedicated server; the other builds' viewer, Jade and AE2 checks, the pipe screen over the network and the social preview are open.
Spend: none

## Foundation
- [x] Repository, layout and Gradle builds for all ten loader and version targets (Forge and NeoForge on 1.20.1, 1.21.1, 26.1.2, 26.2, 26.3; `./gradlew build` exits 0 in each) (w:5)
- [x] Seven-lane pipe bundle core in common/ with three tiers and 126 unit tests (passing in every module) (w:4)
- [x] Approved generated pipe textures wired in, plus 48 plug arrow textures from scripts/gen_plug_arrows.py (w:3)
- [x] Recipes, lang and item models written by scripts/make_assets.py (w:2)

## Pipes
- [x] Item, fluid, energy and redstone pipes (headless self test: transfer, modes, distribution, upgrades, filter, priority, tools, pipe screen server side) (w:5)
- [x] Storage pipe joins BarelyStorage racks and nodes on 26.1.2, 26.2 and 26.3 (headless self test with a real rack, both directions; seen next to a rack in client frames); on 1.20.1 and 1.21.1 there is no BarelyStorage and the pipe only joins inventories (w:4)
- [x] Gas pipe with Mekanism on Forge 1.20.1, NeoForge 1.20.1 and NeoForge 1.21.1 (soft dependency; self test moves 40,000 mB between two Mekanism tanks at 800 mB per tick, passes with and without Mekanism); inert on the other seven builds that have no Mekanism (w:3)
- [x] Gas pipe tank contents visible in a client: after the transfer the receiving Basic Chemical Tank's own screen shows Chemical: Chlorine 40,000/64,000 (NeoForge 1.21.1, frame docs/frames/viewers/neoforge-1.21.1/wthit_gas_receiving_tank_gui.png; the tank model itself draws no contents in the tour frames, Mekanism's renderer) (w:1)
- [ ] Gas pipe against other Mekanism machines and chemical kinds (not tried) (w:1)
- [x] ME pipes join real AE2 grids on NeoForge 1.21.1 (AE2 19.2.18, self test with BARELYPIPES_AE2=1: ME Interface face connects, two interfaces joined by three ME pipe bundles are in one grid, with a gap they are in two; a pipe placed before the AE2 node is ready attaches after the next neighbor update) (w:3)
- [ ] ME pipes on the other AE2 builds (NeoForge 1.20.1 and 26.x have AE2; not run, reference build only; AE2 has no Forge 26.x or Forge 1.21.1 build, so ME pipes are inert there) (w:2)
- [x] Pipe Wrench and Conduit Tool (self tested; click targets follow the drawn boxes since 2026.11.2) (w:3)

## 2026.11.2 fixes (from Casey's play test)
- [x] Outline, collision and clicks follow the drawn pipe boxes (ConduitLayout.pick, unit tests, all ten builds) (w:3)
- [~] Plug arrows for import, export and open and a readable gas plug glyph, centered storage and pipe screen glyphs (all ten builds, unit tests and self test; close-up frames on NeoForge 26.1.2 at 0.4 block and 54 degrees show the chevrons at the plug's glyph face edge and the vanilla outline hugging the arm (docs/frames/neoforge-26.1.2/closeup_*), but the glyph face is only visible at grazing angles and the direction of the chevrons is not readable by eye, so the arrows stay unproven as a readable direction hint) (w:3)
- [x] Show Me What This Is names the exact pipe, tier and details on all ten builds (headless self test lines; overlay read in a client on NeoForge 26.1.2 and 26.3 only) (w:3)
- [~] See-through top face under BarelyStorage racks, terminals and assembler: cause was missing noOcclusion in BarelyStorage; the BarelyStorage source now has it on those blocks, not confirmed in a client and not checked against a BarelyStorage release (w:2)

## Compatibility
- [x] Item and recipe viewers show Barely Pipes items on NeoForge 1.21.1 (search 'pipe' lists every item with its icon in JEI, REI, EMI and BEI; JEI opens the real gold item pipe recipe; frames docs/frames/viewers/neoforge-1.21.1) (w:3)
- [ ] Viewer recipe views in REI, EMI and BEI, and viewers on the other builds (not run; JEI/REI exist for NeoForge 26.x, EMI only for 1.20.1 Forge and 1.21.1 NeoForge, BEI for all) (w:2)
- [x] Jade and WTHIT plugins on NeoForge 1.21.1 (same lines as Show Me What This Is; seen in client frames with WTHIT, docs/frames/viewers/neoforge-1.21.1/wthit_*; Jade tooltip with the same lines read in docs/frames/viewers/neoforge-1.21.1/jade_outline_arm.png) (w:2)
- [ ] Jade and WTHIT plugins on the other nine builds (Jade builds exist for 1.20.1 both loaders and NeoForge 26.x, WTHIT for all ten; Forge 1.21.1 and Forge 26.x have no Jade build; not ported) (w:2)

## Testing
- [x] Headless self test on all ten builds, 0 failures (228 checks on 26.x; 222 on 1.20.1 and 1.21.1 builds, 229 where Mekanism is loaded; Forge 1.21.1 222) (w:4)
- [x] Scripted client tours in a real game with frames read, at least one frame set per build (docs/frames; 2026.11.2 changes looked at on NeoForge 26.1.2, 26.2, 26.3, Forge 26.1.2 and NeoForge 1.21.1; Forge 1.20.1 frames and a few Forge 26.x frames not read) (w:4)
- [x] Hand play by Casey in a persistent test world on NeoForge 26.1.2 (found the six issues fixed in 2026.11.2) (w:2)
- [x] Multiplayer and a long session on NeoForge 1.21.1: a client joined a dedicated server (port 8988) that kept the self test scene; the bundle block entity with its FLUID and ENERGY pipes arrived on the client, and a 6,000 tick (5 minute) connection ended with no server exception (frame docs/frames/multiplayer, run with BARELYPIPES_KEEP and BARELYPIPES_MP) (w:2)
- [ ] The pipe screen opened over the network (two scripted attempts with the game's own use handler did not open it; the screen works in single player; not a verdict on the code) and big pipe networks (w:2)

## Release and docs
- [x] Ten jars per release, one per loader and Minecraft version (Forge and NeoForge builds share class names, so they are never combined); no dev classes inside (w:2)
- [x] CI: build.yml green on main, release workflow ran green for v2026.11.1 and v2026.11.2 (GitHub run list, 2026-11-09) (w:3)
- [x] GitHub repository ItMeCasey/BarelyPipes, tags v2026.11.1 and v2026.11.2, pre-releases with ten jars each (w:2)
- [x] CurseForge project 1787208 with ten beta files for 2026.11.2 and ten for 2026.11.1 (file list read through the API; review state not checked) (w:3)
- [x] Branding, CurseForge description, README, CHANGELOG and issue templates (w:3)
- [~] Wiki: 18 pages deployed at itmecasey.com/wiki/barelypipes and identical to the folder before this audit; the wording fixes of 2026-11-09 (SMWTI on all ten builds, 126 tests, release state) are edited but need a redeploy; no screenshots on the pages yet; the wiki folder is not committed in the site repo (w:3)
- [ ] GitHub social preview image (banners/social.png; only the GitHub web settings page can set it, no API: left for Casey) (w:1)
