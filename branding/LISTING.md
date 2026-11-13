# Barely Pipes listing kit: CurseForge

Everything needed to fill in the CurseForge project form. Files are in `branding/icon/` and `branding/banners/`.

## Project fields

| Field | CurseForge |
|---|---|
| Project ID | not created yet (Casey creates the project; record the id here and in `.github/scripts/publish_curseforge.py`) |
| Name | Barely Pipes |
| Slug / URL | `barelypipes` |
| Mod id | `barelypipes` |
| Icon / logo | `icon/icon_512.png` (square, at least 400x400) |
| Class | Mods |
| Main category | Technology, or the closest item, fluid and energy transport category the form offers |
| Loaders | Forge and NeoForge |
| Game versions | Minecraft 1.20.1, 1.21.1, 26.1.2, 26.2 and 26.3, ten jars, one per loader and Minecraft version (see below); set the loader and game version on each uploaded file |
| Environment | Client and Server (the upload script sends both) |
| License | MIT |
| Source | https://github.com/ItMeCasey/BarelyPipes |
| Issues | https://github.com/ItMeCasey/BarelyPipes/issues |
| Wiki | https://itmecasey.com/wiki/barelypipes/ (deployed) |

### Files per release

| Minecraft | Jars | Loaders |
|---|---|---|
| 26.3, 26.2, 26.1.2, 1.21.1, 1.20.1 | `barelypipes-forge-mc<mc>-<version>.jar` | Forge (upload as its own file, tick only Forge) |
| 26.3, 26.2, 26.1.2, 1.21.1, 1.20.1 | `barelypipes-neoforge-mc<mc>-<version>.jar` | NeoForge (upload as its own file, tick only NeoForge) |

Upload ten files per release, one per loader and Minecraft version, each with its own loader and game version ticked. There is no combined jar: Forge and NeoForge builds of the same Minecraft version use the same class names with different code, so they cannot be merged.

### One-line description

> Seven pipes in one block for Forge and NeoForge: items, fluids, energy, gas, redstone, AE2 ME and BarelyStorage, in three tiers with per-face modes, filters and priorities.

Use it for the CurseForge summary (under 250 characters), social posts and the top of the description.

### Gallery

| File | Use |
|---|---|
| `banners/features.png` (1920x1080) | First gallery image: the seven pipes |
| `banners/tiers.png` (1920x1080) | Iron, gold and diamond rates, second gallery image |
| `banners/social.png` (1280x640) | GitHub social preview (repo Settings > Social preview) |
| `banners/banner.png` (800x150, saved at 2x) | Top of the description |
| `banners/h_*.png` (800x46, saved at 2x) | Section headers inside the description (features, pipes, tools, compat, faq) |

The panels in these images are illustrations drawn for the branding; the pipe strips and glyphs are the mod's own generated textures (`AeroFlux-Pipe-Template/gen`, copied into `branding/source/art/`). There are no in-game screenshots yet: add real ones from the tour frames once a client check has been run and they are picked.

Rebuild everything with `python branding/source/make_icon.py`, then `python branding/source/render_banners.py` (needs Pillow and Google Chrome), then `python branding/source/make_wiki_images.py` for the wiki copies.

### Description

The CurseForge description is in [`curseforge/DESCRIPTION.md`](../curseforge/DESCRIPTION.md). Paste it with the description type set to **Markdown**. Its images load from GitHub, so `branding/` has to be pushed first. It has no install steps and no test notes: it describes the mod only.

## Checklist

Done (verified 2026-11-09): `branding/` is on GitHub; the CurseForge project exists (id 1787208) with ten beta files for 2026.11.2 (and ten for 2026.11.1); the repository has the `v2026.11.1` and `v2026.11.2` tags and GitHub prereleases with the ten jars; the wiki is deployed.

Still to check by hand: the GitHub social preview (`banners/social.png`, web UI only) and that the CurseForge files have passed review.

Rules: test results live in `README.md` ("What was checked"), `PROJECT_STATUS.md`, `CHANGELOG.md` and the wiki page `known-gaps`, never in the CurseForge description. `icon/barelypipes_logo.png` is the mod list logo for the jar. No Modrinth upload (house rule): the listing is CurseForge only.
