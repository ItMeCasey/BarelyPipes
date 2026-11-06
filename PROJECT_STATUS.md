# Project Status
<!-- status-format: 1 -->
Updated: 2026-11-06
Summary: Barely Pipes (AeroFlux's pipe bundles as their own mod, seven pipes with a new BarelyStorage pipe, three tiers) builds and passes its unit tests and headless server self test on all ten Forge and NeoForge builds, with a real client check done on NeoForge 26.1.2 only so far.
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
- [~] Client checks with frames, one build per Minecraft version (NeoForge 26.1.2 done and read; the other versions in progress) (w:4)
- [ ] ME pipes against real AE2 grids and gas pipes against Mekanism (not tried; no AE2 Forge build for 26.x or Forge 1.21.1) (w:3)

## Release and docs
- [ ] Ten packaged jars, one per loader and Minecraft version (scripts/build_all.sh written, not run; Forge and NeoForge builds share class names so they are never combined) (w:2)
- [~] CI (build.yml) and release workflow (written, never run on GitHub; CurseForge project id set by the PM) (w:3)
- [x] Branding, CurseForge description, README (icon, banners, LISTING, DESCRIPTION, README and CHANGELOG written; Tested lines say what is verified only) (w:3)
- [~] Wiki pages (18-page draft written and previewed locally with no missing links; not deployed, Tested and known-gaps pages to update after client checks) (w:3)
- [~] GitHub repository (created and pushed by the PM), CurseForge project (id set), first release (not tagged) (w:2)
