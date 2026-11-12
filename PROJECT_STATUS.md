# Project Status
<!-- status-format: 1 -->
Updated: 2026-11-12
Summary: Barely Pipes 2026.11.4 is built and checked locally (plug arrows moved onto the plug sides on all ten builds, Jade and WTHIT and the rack top faces checked) but not yet pushed, tagged or released; 2026.11.3 is the released version; what is left is the 2026.11.4 release itself and the wiki redeploy.
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
- [x] Gas pipe against another machine and another chemical: chlorine moves 6,000 of 6,000 mB from a tank into a Mekanism Chemical Infuser through three iron gas pipes (NeoForge 1.21.1 self test, 239 checks with AE2; the other Mekanism builds run the same GasSelfTest lines only where ported) (w:1)
- [x] ME pipes join real AE2 grids on NeoForge 1.20.1 (AE2 15.4.12), 1.21.1 (19.2.18) and 26.1.2 (26.1.13): ME Interface face connects, two interfaces joined by three ME pipe bundles share one grid, with a gap they do not (self test with BARELYPIPES_AE2=1: 233, 239 and 232 checks pass). A pipe placed before the AE2 node is ready attaches after the next neighbor update. NeoForge 26.2 and 26.3 (AE2 alpha builds) not run, same code as 26.1.2; no AE2 for Forge 1.21.1 and Forge 26.x (w:3)
- [x] Pipe Wrench and Conduit Tool (self tested; click targets follow the drawn boxes since 2026.11.2) (w:3)

## 2026.11.2 fixes (from Casey's play test)
- [x] Outline, collision and clicks follow the drawn pipe boxes (ConduitLayout.pick, unit tests, all ten builds) (w:3)
- [x] Plug arrows for import, export and open on the plug's four sides, one small outlined arrow (into the pipe for import, out for export, a head at each end for open) laid over the plug past the plate layers, with a readable direction from normal angles: six shared overlay textures (scripts/gen_plug_overlays.py), PipeTextures.plugOverlay and ConduitLayout.plugOverlayBox (unit tested), drawn by the baked model of all ten builds (the four 1.20.1 and 1.21.1 builds draw the bundle in the cutout layer for the transparent pixels); seen in a client on NeoForge 26.1.2 (docs/frames/viewers/neoforge-26.1.2/overlay_{side,top,normal}_*.png) and NeoForge 1.21.1 (plugsides_*.png); the other eight builds compile the same logic (w:3)
- [x] Show Me What This Is names the exact pipe, tier and details on all ten builds (headless self test lines; overlay read in a client on NeoForge 26.1.2 and 26.3 only) (w:3)
- [x] See-through top face under BarelyStorage racks, terminals and assembler: cause was missing noOcclusion in BarelyStorage; with the released BarelyStorage 2026.11.3 jar and Barely Pipes in a NeoForge 26.1.2 client the top faces of the server rack, storage terminal, crafting terminal and assembler are all drawn solid when seen from above (frames docs/frames/viewers/neoforge-26.1.2/rackplug_rack_tops_near.png and rackplug_rack_tops_far.png) (w:2)

## Compatibility
- [x] Item and recipe viewers on NeoForge 1.21.1: a search for 'pipe' lists every item in JEI, REI, EMI and BEI and the recipe view opens in all four (gold item pipe in JEI, redstone pipe in REI, iron fluid pipe in EMI and BEI; frames docs/frames/viewers/neoforge-1.21.1/*rec*), and REI's recipe view on NeoForge 26.2 (w:3)
- [x] n/a: no EMI build for NeoForge 26.x, Forge 1.21.1 or Forge 26.x; no REI build for Forge 1.21.1 or Forge 26.x; no JEI build for Forge 26.x; no AE2 build for Forge 1.21.1 or Forge 26.x; no Jade build for Forge 1.21.1 or Forge 26.x (w:1)
- [x] Viewers on the remaining builds that have them, seen in a client: JEI recipe view on NeoForge 26.2, 26.3 and Forge 1.21.1; BEI recipe view on NeoForge 26.2, Forge 1.21.1, Forge 26.1.2 and Forge 26.2; REI recipe view on NeoForge 26.3 (frames docs/frames/viewers/<layer>/*rec*_recipe.png). BEI on NeoForge 26.3 and Forge 26.3 list all 23 pipe items and open BEI's detail page, but showed its 'Uses' page ('No recipes found') instead of the crafting recipe (w:1)
- [x] Jade and WTHIT plugins show the exact pipe lines in a client: WTHIT on NeoForge 1.20.1 (WTHIT 8), 1.21.1 (12) and 26.1.2 (19); Jade on NeoForge 1.20.1 (Jade 11), 1.21.1 (15) and 26.1.2 (26) (frames docs/frames/viewers/<layer>/{jade,wthit}_outline_arm.png). Plugins and the Jade config name are in all builds that have the mods: WTHIT on all ten, Jade on NeoForge 1.20.1, 1.21.1, 26.1.2, 26.2, 26.3 and Forge 1.20.1 (Forge 1.21.1 and Forge 26.x have no Jade build) (w:3)
- [x] Jade and WTHIT in a client on the other builds: Jade on NeoForge 26.2 and 26.3, WTHIT on NeoForge 26.2 and 26.3 and Forge 1.21.1, 26.1.2, 26.2 and 26.3 (frames docs/frames/viewers/<layer>/{jade,wthit}_outline_arm.png, pipe name, arm, mode, throughput lines shown) (w:2)
- [x] Jade and WTHIT on Forge 1.20.1 in a real install (not the ForgeGradle dev client, where both mods' own mixins fail): the released barelypipes-forge-mc1.20.1-2026.11.3.jar (527 SRG names, verify_jars ok) plus Jade 11 or WTHIT 8.21.2 with Bad Packets in a real Forge 47.4.28 client; looking at an item pipe shows Pipe Bundle, Iron Item Pipe, Core of the pipe, Sides, Throughput, Upgrades, Distribution and the other pipe in the bundle in both (frames docs/frames/viewers/forge-1.20.1/{jade,wthit}_real_install_pipe.png; no mixin errors in the log) (w:1)

## Testing
- [x] Headless self test on all ten builds, 0 failures (228 checks on 26.x; 222 on 1.20.1 and 1.21.1 builds, 229 where Mekanism is loaded; Forge 1.21.1 222) (w:4)
- [x] Scripted client tours in a real game with frames read, at least one frame set per build (docs/frames; 2026.11.2 changes looked at on NeoForge 26.1.2, 26.2, 26.3, Forge 26.1.2 and NeoForge 1.21.1; Forge 1.20.1 frames and a few Forge 26.x frames not read) (w:4)
- [x] Hand play by Casey in a persistent test world on NeoForge 26.1.2 (found the six issues fixed in 2026.11.2) (w:2)
- [x] Multiplayer on NeoForge 1.21.1: a client joined a dedicated server (port 8988); the bundle and its pipes arrived on the client, the pipe screen opened from a click on a pipe arm over the network (frame docs/frames/multiplayer/mp_pipe_screen.png), and a 5 minute session ended with no server exception (w:2)
- [x] Big network: a 784-bundle block (2,352 pipes: fluid, item, energy) feeding a far tank and chest still moves fluid and items across the whole block, and the server tick stays at 1.9 ms against 2.3 ms idle (PerfSelfTest in the NeoForge 1.21.1 self test; reference build only) (w:2)

## Release and docs
- [x] Ten jars per release, one per loader and Minecraft version (Forge and NeoForge builds share class names, so they are never combined); no dev classes inside (w:2)
- [x] CI: build.yml green on main, release workflow ran green for v2026.11.1 and v2026.11.2 (GitHub run list, 2026-11-09) (w:3)
- [x] GitHub repository ItMeCasey/BarelyPipes, tags v2026.11.1 and v2026.11.2, pre-releases with ten jars each (w:2)
- [x] CurseForge project 1787208 with ten beta files for 2026.11.2 and ten for 2026.11.1 (file list read through the API; review state not checked) (w:3)
- [x] Branding, CurseForge description, README, CHANGELOG and issue templates (w:3)
- [x] Wiki: 18 pages deployed at itmecasey.com/wiki/barelypipes, redeployed 2026-11-10 with the newer wording (SMWTI on all ten builds, evidence on viewers, AE2, multiplayer and gas, jar names 2026.11.3; deployed by the PM); no screenshots on the pages yet (w:3)
- [x] 2026.11.3 pushed, tagged v2026.11.3 and the release run succeeded (done by the PM, reported 2026-11-10; all ten modules are at 2026.11.3) (w:2)
- [x] 2026.11.4 pushed, CI green on all ten builds, tagged and uploaded, wiki redeployed (PM, 2026-11-10) (w:1)
- [x] 2026.11.5 (Forge and NeoForge 26.3 only): mouse button fix for the 26.3 renumbering; compiled, not yet tried in a client (w:1)
