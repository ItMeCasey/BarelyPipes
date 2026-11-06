# Project Status
<!-- status-format: 1 -->
Updated: 2026-11-06
Summary: Barely Pipes (AeroFlux's pipe bundles as their own mod, seven pipes with a new BarelyStorage pipe, three tiers) builds and passes its unit tests and headless server self test on all ten Forge and NeoForge builds, a scripted real-client tour (frames read) on all ten, and ten packaged jars; the Gas Pipe carries Mekanism gases on the three builds that have Mekanism (Forge and NeoForge 1.20.1, NeoForge 1.21.1) and is inert on the other seven.
Spend: none

## Foundation
- [x] Repository, layout and Gradle build for NeoForge 26.1.2 (w:3)
- [x] Seven-lane pipe bundle core in common/ with three tiers and 98 unit tests (w:4)
- [x] Approved generated pipe textures wired in (150 files, three tiers, storage single set) (w:3)
- [x] Recipes, lang and item models written by scripts/make_assets.py (w:2)
- [x] Builds for all ten loader and version targets (compile, 98 unit tests each) (w:5)

## Storage pipe
- [x] Storage pipe joins BarelyStorage racks and nodes (NeoForge 26.1.2, seen in the headless self test against a real rack, both directions) (w:4)
- [x] Storage pipe on Forge 26.1.2, 26.2 and 26.3 and NeoForge 26.2 and 26.3 (headless self test with a real BarelyStorage rack, 219 checks each; 1.20.1 and 1.21.1 have no BarelyStorage build, the pipe joins inventories only) (w:3)

## Testing
- [x] Headless self test, NeoForge 26.1.2 (219 checks) (w:3)
- [x] Headless self test on the other nine builds (219 checks on 26.x, 213 on 1.20.1 and 1.21.1, 0 failures) (w:4)
- [x] Client checks with frames on all ten builds (scripted tour, frames in docs/frames, a few Forge 26.1.2 and 26.2 frames unread) (w:4)
- [ ] ME pipes against real AE2 grids (not tried; no AE2 Forge build for 26.x or Forge 1.21.1) (w:3)
- [x] Gas pipe with Mekanism on Forge 1.20.1, NeoForge 1.20.1 and NeoForge 1.21.1 (soft dependency; self test moves 40,000 mB between two Mekanism tanks at 800 mB per tick, passes with and without Mekanism; client tour frames read; the other seven builds have no Mekanism and keep the pipe inert) (w:3)
- [ ] Gas pipe against more Mekanism machines and with tank contents visible in a client frame (the tour's tank models showed no contents; only chemical tanks were tried) (w:1)

## 2026.11.2 fixes (from Casey's play test)
- [x] Outline, collision and clicks follow the drawn pipe boxes (common ConduitLayout.pick, 125 unit tests, all ten builds) (w:3)
- [x] Plug arrows for import, export and open; gas plug glyph readable; storage glyph centered; pipe screen glyphs centered (all ten builds; arrows only seen small in tour frames) (w:3)
- [x] Show Me What This Is names the exact pipe, tier and details (all ten builds; seen on NeoForge 26.1.2 and 26.3 and 1.21.1 clients only) (w:3)
- [~] See-through top face under BarelyStorage racks, terminals and assembler: cause found (missing noOcclusion in BarelyStorage), patch handed to the BarelyStorage session, not confirmed in a client (w:2)

## Release and docs
- [x] Ten packaged jars in dist/ (scripts/build_all.sh run, no dev classes inside; Forge and NeoForge builds share class names so they are never combined) (w:2)
- [~] CI (build.yml) and release workflow (build.yml ran on GitHub: fixes pushed for gradlew line endings and a script syntax error; neoforge-26.2/26.3 failed on stale dev code, fixed locally; release workflow never run) (w:3)
- [x] Branding, CurseForge description, README (icon, banners, LISTING, DESCRIPTION, README and CHANGELOG written; Tested lines say what is verified only) (w:3)
- [~] Wiki pages (18-page draft, Tested and known-gaps updated; built, not deployed, no screenshots yet) (w:3)
- [~] GitHub repository (created and pushed by the PM), CurseForge project (id set), first release (not tagged) (w:2)
