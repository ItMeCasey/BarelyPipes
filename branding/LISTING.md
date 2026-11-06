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
| Game versions | Minecraft 1.20.1, 1.21.1, 26.1.2, 26.2 and 26.3, one jar per Minecraft version (two jars on 1.20.1, see below); set the loader and game version on each uploaded file |
| Environment | Client and Server (the upload script sends both) |
| License | MIT |
| Source | https://github.com/ItMeCasey/BarelyPipes |
| Issues | https://github.com/ItMeCasey/BarelyPipes/issues |
| Wiki | https://itmecasey.com/wiki/barelypipes/ (not deployed yet; the description already links it, deploy before the project goes live) |

### Files per release

| Minecraft | Jars | Loaders |
|---|---|---|
| 26.3, 26.2, 26.1.2, 1.21.1 | `barelypipes-mc<mc>-<version>.jar`, one combined jar | Forge and NeoForge (upload once, tick both loaders) |
| 1.20.1 | `barelypipes-forge-mc1.20.1-<version>.jar` and `barelypipes-neoforge-mc1.20.1-<version>.jar` | one jar per loader (the two loaders share one metadata file name on 1.20.1, so they cannot be combined) |

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

The CurseForge description is in [`curseforge/DESCRIPTION.md`](../curseforge/DESCRIPTION.md). Paste it with the description type set to **Markdown**. Its images load from GitHub, so `branding/` has to be pushed first. Its Tested line says only what was run.

## Checklist

1. Push `branding/` to GitHub so the description images load.
2. Create the CurseForge project with `icon_512.png` as the logo, record the project id above and in the upload script, and add the `CURSEFORGE_TOKEN`, `IMC_MAVEN_USER` and `IMC_MAVEN_PASSWORD` repository secrets.
3. Set `banners/social.png` as the GitHub social preview.
4. Deploy the wiki (`wiki/barelypipes`) so the description's wiki link works.
5. **After the client checks, update the Tested line** in `curseforge/DESCRIPTION.md`, the "What was checked" section in `README.md` and the table on the wiki page `known-gaps`. These three are the only places that state what was verified.
6. `icon/barelypipes_logo.png` is the mod list logo for the jar.
7. No Modrinth upload (house rule): the listing is CurseForge only.
