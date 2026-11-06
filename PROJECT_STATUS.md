# Project Status
<!-- status-format: 1 -->
Updated: 2026-11-06
Summary: Barely Pipes (AeroFlux's pipe bundles as their own mod, seven pipes with a new BarelyStorage pipe, three tiers) builds and passes its headless self test on NeoForge 26.1.2, and the other nine Forge and NeoForge builds are being ported from it.
Spend: none

## Foundation
- [x] Repository, layout and Gradle build for NeoForge 26.1.2 (w:3)
- [x] Seven-lane pipe bundle core in common/ with three tiers and 98 unit tests (w:4)
- [x] Approved generated pipe textures wired in (150 files, three tiers, storage single set) (w:3)
- [x] Recipes, lang and item models written by scripts/make_assets.py (w:2)
- [~] Builds for the other nine loader and version targets (w:5)

## Storage pipe
- [x] Storage pipe joins BarelyStorage racks and nodes (NeoForge 26.1.2, seen in the headless self test against a real rack, both directions) (w:4)
- [ ] Storage pipe on Forge 26.1.2, 26.2 and 26.3 and NeoForge 26.2 and 26.3 (w:3)

## Testing
- [x] Headless self test, NeoForge 26.1.2 (219 checks) (w:3)
- [ ] Headless self test on the other nine builds (w:4)
- [ ] Client checks with frames, one build per Minecraft version (w:4)

## Release and docs
- [ ] Packaged jar per Minecraft version (w:2)
- [ ] CI (build.yml) and release workflow (w:3)
- [ ] Branding, CurseForge description, README (w:3)
- [ ] Wiki pages (w:3)
- [ ] GitHub repository, CurseForge project, first release (w:2)
