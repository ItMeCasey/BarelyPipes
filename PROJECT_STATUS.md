# Project Status
<!-- status-format: 1 -->
Updated: 2026-11-09
Summary: Barely Pipes 2026.11.2 (AeroFlux's pipe bundles as their own mod: seven pipes including a BarelyStorage pipe, three tiers, Mekanism gas on the three builds that have Mekanism) is released as ten beta jars on CurseForge and GitHub, with all ten builds passing unit tests, the headless self test and CI; item viewer plugins, Jade, ME against real AE2 and a close client look at the plug arrows are still open.
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
- [ ] Gas pipe: more Mekanism machines, other chemical kinds and tank contents visible in a client frame (the tour's tank models showed no contents) (w:1)
- [~] ME pipes: AE2 compile-only soft dependency and recipes; never tried against real AE2 grids; AE2 has no Forge 26.x or Forge 1.21.1 build, so they are inert there (w:3)
- [x] Pipe Wrench and Conduit Tool (self tested; click targets follow the drawn boxes since 2026.11.2) (w:3)

## 2026.11.2 fixes (from Casey's play test)
- [x] Outline, collision and clicks follow the drawn pipe boxes (ConduitLayout.pick, unit tests, all ten builds) (w:3)
- [~] Plug arrows for import, export and open and a readable gas plug glyph, centered storage and pipe screen glyphs (all ten builds, unit tests and self test; client frames show them but the arrows are tiny from the tour camera and the outline frames were inconclusive, so a close look in normal play is still open) (w:3)
- [x] Show Me What This Is names the exact pipe, tier and details on all ten builds (headless self test lines; overlay read in a client on NeoForge 26.1.2 and 26.3 only) (w:3)
- [~] See-through top face under BarelyStorage racks, terminals and assembler: cause was missing noOcclusion in BarelyStorage; the BarelyStorage source now has it on those blocks, not confirmed in a client and not checked against a BarelyStorage release (w:2)

## Compatibility
- [ ] Item and recipe viewer plugins: BEI, REI, JEI, EMI (not started; the pipes add items and recipes, so the standard asks for them) (w:4)
- [ ] Jade and WTHIT in-world info (AeroFlux had them; not ported; Show Me What This Is is done) (w:2)

## Testing
- [x] Headless self test on all ten builds, 0 failures (228 checks on 26.x; 222 on 1.20.1 and 1.21.1 builds, 229 where Mekanism is loaded; Forge 1.21.1 222) (w:4)
- [x] Scripted client tours in a real game with frames read, at least one frame set per build (docs/frames; 2026.11.2 changes looked at on NeoForge 26.1.2, 26.2, 26.3, Forge 26.1.2 and NeoForge 1.21.1; Forge 1.20.1 frames and a few Forge 26.x frames not read) (w:4)
- [x] Hand play by Casey in a persistent test world on NeoForge 26.1.2 (found the six issues fixed in 2026.11.2) (w:2)
- [ ] Multiplayer, long sessions and big pipe networks (not tried) (w:2)

## Release and docs
- [x] Ten jars per release, one per loader and Minecraft version (Forge and NeoForge builds share class names, so they are never combined); no dev classes inside (w:2)
- [x] CI: build.yml green on main, release workflow ran green for v2026.11.1 and v2026.11.2 (GitHub run list, 2026-11-09) (w:3)
- [x] GitHub repository ItMeCasey/BarelyPipes, tags v2026.11.1 and v2026.11.2, pre-releases with ten jars each (w:2)
- [x] CurseForge project 1787208 with ten beta files for 2026.11.2 and ten for 2026.11.1 (file list read through the API; review state not checked) (w:3)
- [x] Branding, CurseForge description, README, CHANGELOG and issue templates (w:3)
- [~] Wiki: 18 pages deployed at itmecasey.com/wiki/barelypipes and identical to the folder before this audit; the wording fixes of 2026-11-09 (SMWTI on all ten builds, 126 tests, release state) are edited but need a redeploy; no screenshots on the pages yet; the wiki folder is not committed in the site repo (w:3)
- [ ] GitHub social preview image (web UI only) (w:1)
