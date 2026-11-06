# Porting a Barely Pipes module

`neoforge-26.1.2` is the finished reference build: it compiles, passes the 98 unit tests and passes the headless self test
(`python scripts/server_check.py neoforge-26.1.2` prints `SERVER PASS`, 219 checks including the storage pipe against a real
BarelyStorage network). Every other module is the same mod on another loader or Minecraft version. Do the same steps for each.

## Where the code comes from

AeroFlux (`C:\Users\casey\Desktop\AeroFlux\versions\mc<mc>\<loader>`) has the pipe code for 1.20.1, 1.21.1 and 26.1.2 on both
loaders. It has nothing for 26.2 and 26.3: derive those from the 26.1.2 module of the same loader and apply the API changes
(BarelyPowered and BarelyStorage already run on 26.2 and 26.3 on both loaders: diff their `version-26.1.2`, `version-26.2` and
`version-26.3` folders and their loader layers to find what changed, for example 26.3 has no `codec()` MapCodec on blocks,
`pose.rotateDegrees`, a different loot table format and `Containers.dropItemStack`).

`scripts/port_aeroflux.py <mc> <loader>` seeds `<loader>-<mc>/src/main/java/com/itmecasey/barelypipes` from the AeroFlux
module (renames the package and ids). It is a seed only. Then replay what was done to the reference module. To see exactly
what that was, diff a reference file against its AeroFlux original after renaming, for example:

    diff <(sed 's/aeroflux/barelypipes/g;s/AeroFlux/BarelyPipes/g' AeroFlux/versions/mc26.1.2/neoforge/src/main/java/com/itmecasey/aeroflux/conduit/Connections.java) \
         neoforge-26.1.2/src/main/java/com/itmecasey/barelypipes/conduit/Connections.java

## What was changed from AeroFlux (replay all of it)

1. Machines, collectors, tanks, converters, lamps, chunk loaders, guide, configurator, fluids, profiler and every compat
   except AE2 were removed. Registries (`registry/*`), the main class, `BarelyPipesConfig` and `client/ClientSetup` were rewritten
   to hold only the bundle block, its block entity, the pipe menu, the pipe and tool items and one creative tab. The
   protection layer stays (`protect/Protection`, `ToolGuard`, `EventProtection`, with only the standard permission-event
   provider; the FTB Chunks and Open Parties providers were dropped).
2. `Tier` has three values, IRON, GOLD, DIAMOND (`common/.../core/Tier.java`); every `Tier.COPPER` became `Tier.IRON`.
   `PipeTiers`, `PipeUpgrades`, `PipeTextures`, `PipeType` (new `STORAGE`, 7 lanes), `PipeSet` (4 bits per lane) and
   `BundleState` (2 tier bits; `tier(lane)` of the storage pipe is `PipeTiers.STORAGE_TIER`) are already changed in `common/`:
   do not change `common/` without telling the manager, all ten builds share it.
3. Item ids: `<tier>_<type>_pipe` for fluid, item, energy and gas (tiers iron, gold, diamond), `redstone_pipe`, `me_pipe`,
   `dense_me_pipe`, `storage_pipe`, and `pipe_wrench` (was `flux_wrench`). The Flux Configurator, upgrade cards and machine
   upgrades are gone.
4. Storage pipe: `conduit/transfer/StorageMover` (a mover that works over a `Port` interface), `conduit/transfer/StorageLink`
   (only class that names BarelyStorage; guarded by `ModList...isLoaded("barelystorage")`), `TransferEngine` maps STORAGE to
   the mover, `Connections.attaches` has a STORAGE case (BarelyStorage `NetNode` block, or any item handler), the rate switches in
   `PipeConfigActions` and `ConduitToolActions` take `ITEM, STORAGE`. BarelyStorage exists only for 26.1.2, 26.2 and 26.3:
   on 1.20.1 and 1.21.1 keep the STORAGE case in `Connections` (item handlers only), leave `StorageLink` out and no recipe (the asset
   script already skips the recipe for those versions); BarelyStorage's jar for compile is `libs/barelystorage-mc<mc>.jar`
   (copy from `C:\Users\casey\Desktop\BarelyStorage\dist\barelystorage-mc<mc>-2026.11.1.jar`; 26.1.2 is already there).
5. Dev tools live in `src/dev/java/com/itmecasey/barelypipes/dev` and only build with `-PdevTools`; the main class finds
   `dev.DevHooks` by reflection when the system property `barelypipes.dev` is set (the `run` blocks of `build.gradle` set it).
   `DevHooks` registers a `test_tank` block (fluid and energy on every side) and runs `SelfTest`, which sets up
   `TransferSelfTest`, `ToolSelfTest`, `ScreenSelfTest` and, with BarelyStorage installed, `StorageSelfTest`. These are
   AeroFlux's own self tests with the tank swapped for the test tank and the numbers changed for the new tiers; they print
   `SELFTEST PASS <name>` and end with `SELFTEST DONE failures=<n>`.
6. Resources: `python scripts/make_assets.py <mc> <loader>` writes the shared resources into `common/src/main/resources`
   (textures, glyphs, item models, lang: identical for every module) and the per-module blockstate and recipes into
   `<module>/src/main/resources` (it copies the blockstate from the matching AeroFlux module, so it also shows which loader
   needs which model loader). Check it handles your Minecraft version and loader; extend it if a format differs.

## Build setup per module

Copy `build.gradle`, `gradle.properties`, `settings.gradle`, `gradlew*`, `gradle/` and the metadata template from the matching
BarelyPowered layer (`C:\Users\casey\Desktop\BarelyPowered\<loader>-<mc>`; 1.20.1 has no BarelyPowered layer, take AeroFlux's
module for that). Make the same edits the reference module's `build.gradle` has: shared `../common` sources, resources and tests,
the `-PdevTools` block, AE2 compile-only (Modrinth Maven, `ae2_version` property: look up the id used in AeroFlux's
`gradle.properties` for that version) and BarelyStorage compile-only. Forge jars need a `pack.mcmeta` in
`src/main/templates` (Forge 1.21.1 and 26.x: the playbook warns about this) and the jar name must be
`barelypipes-<loader>-mc<mc>-<version>.jar`. `mod_version=2026.11.1` in every module.

## Done for one module means

- `./gradlew build --no-daemon` passes (98 common unit tests run in every module).
- `python scripts/server_check.py <loader>-<mc>` prints `SERVER PASS` with zero self-test failures (26.x modules run the
  storage checks too; 1.20.1 and 1.21.1 show no storage checks).
- The built jar contains `pack.mcmeta` (Forge) or the right mods metadata, the textures and the lang file.
- Update the self test expectations only when the behavior really differs between versions, and say why.
