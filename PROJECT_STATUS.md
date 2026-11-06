# Project Status
<!-- status-format: 1 -->
Updated: 2026-11-06
Summary: Barely Pipes (AeroFlux's pipe bundles as their own mod, seven pipes with a new BarelyStorage pipe, three tiers) builds and passes its unit tests and headless server self test on all ten Forge and NeoForge builds, a scripted real-client tour (frames read) on all ten, and ten packaged jars; the Gas Pipe does not work yet because Mekanism support was not carried over.
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
- [ ] Gas pipe: Mekanism support from AeroFlux (1.21.1 NeoForge, 1.20.1 both) was not carried over, so the Gas Pipe connects to nothing (w:3)

## Release and docs
- [x] Ten packaged jars in dist/ (scripts/build_all.sh run, no dev classes inside; Forge and NeoForge builds share class names so they are never combined) (w:2)
- [~] CI (build.yml) and release workflow (build.yml ran on GitHub: fixes pushed for gradlew line endings and a script syntax error; neoforge-26.2/26.3 failed on stale dev code, fixed locally; release workflow never run) (w:3)
- [x] Branding, CurseForge description, README (icon, banners, LISTING, DESCRIPTION, README and CHANGELOG written; Tested lines say what is verified only) (w:3)
- [~] Wiki pages (18-page draft, Tested and known-gaps updated; built, not deployed, no screenshots yet) (w:3)
- [~] GitHub repository (created and pushed by the PM), CurseForge project (id set), first release (not tagged) (w:2)
