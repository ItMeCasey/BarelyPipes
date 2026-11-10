# Project Status
<!-- status-format: 1 -->
Updated: 2026-11-09
Summary: Barely Pipes 2026.11.3 is built and checked locally on the reference module (NeoForge 1.21.1) but not yet pushed or released: it adds Jade and WTHIT plugins on all builds that have those mods, and 2026.11.2 is the released version (ten beta jars on CurseForge and GitHub); what is left is the release itself, a close look at the plug arrows, the BarelyStorage see-through fix in a client, the wiki redeploy and the GitHub social preview.
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
- [~] Plug arrows for import, export and open and a readable gas plug glyph, centered storage and pipe screen glyphs (all ten builds, unit tests and self test; close-up frames on NeoForge 26.1.2 at 0.4 block and 54 degrees show the chevrons at the plug's glyph face edge and the vanilla outline hugging the arm (docs/frames/neoforge-26.1.2/closeup_*), but the glyph face is only visible at grazing angles and the direction of the chevrons is not readable by eye, so the arrows stay unproven as a readable direction hint) (w:3)
- [x] Show Me What This Is names the exact pipe, tier and details on all ten builds (headless self test lines; overlay read in a client on NeoForge 26.1.2 and 26.3 only) (w:3)
- [~] See-through top face under BarelyStorage racks, terminals and assembler: cause was missing noOcclusion in BarelyStorage; the BarelyStorage source now has it on those blocks, not confirmed in a client and not checked against a BarelyStorage release (w:2)

## Compatibility
- [x] Item and recipe viewers on NeoForge 1.21.1: a search for 'pipe' lists every item in JEI, REI, EMI and BEI and the recipe view opens in all four (gold item pipe in JEI, redstone pipe in REI, iron fluid pipe in EMI and BEI; frames docs/frames/viewers/neoforge-1.21.1/*rec*), and REI's recipe view on NeoForge 26.2 (w:3)
- [~] Viewers on the remaining builds (JEI and BEI on NeoForge 26.2 and 26.3, REI on 26.3, JEI on Forge 1.21.1, BEI on Forge 26.x): not run; the BEI 26.2 launch did not start twice because the launcher kept waiting for the shared client slot. These viewers need no plugin from us (w:1)
- [x] Jade and WTHIT plugins show the exact pipe lines in a client: WTHIT on NeoForge 1.20.1 (WTHIT 8), 1.21.1 (12) and 26.1.2 (19); Jade on NeoForge 1.20.1 (Jade 11), 1.21.1 (15) and 26.1.2 (26) (frames docs/frames/viewers/<layer>/{jade,wthit}_outline_arm.png). Plugins and the Jade config name are in all builds that have the mods: WTHIT on all ten, Jade on NeoForge 1.20.1, 1.21.1, 26.1.2, 26.2, 26.3 and Forge 1.20.1 (Forge 1.21.1 and Forge 26.x have no Jade build) (w:3)
- [~] Jade and WTHIT on the builds not started in a client: the same code compiled on Forge 1.20.1, Forge 1.21.1 and Forge 26.3 and the jars are checked, but no client ran there (Jade's own mixin fails in the ForgeGradle 1.20.1 dev client, not ours); the other builds are compiled by CI (w:2)

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
- [~] Wiki: 18 pages deployed at itmecasey.com/wiki/barelypipes; the draft folder has newer wording (SMWTI on all ten builds, 126 tests, evidence on viewers, AE2, multiplayer and gas, jar names 2026.11.3) that needs a redeploy; no screenshots on the pages yet; the wiki folder is not committed in the site repo (w:3)
- [ ] GitHub social preview image (banners/social.png; only the GitHub web settings page can set it: Casey) (w:1)
- [ ] 2026.11.3 pushed, CI green on all ten builds, tagged and uploaded (PM; all ten modules are at 2026.11.3 in gradle.properties; every jar changed because the shared lang file gained the Jade key and nine builds gained plugin classes) (w:2)
