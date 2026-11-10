package com.itmecasey.barelypipes.dev;

import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import com.itmecasey.barelypipes.conduit.config.PipeConfigMenu;
import com.itmecasey.barelypipes.core.Tier;
import com.itmecasey.barelypipes.core.conduit.Box;
import com.itmecasey.barelypipes.core.conduit.ConduitLayout;
import com.itmecasey.barelypipes.core.conduit.ConnectionMode;
import com.itmecasey.barelypipes.core.conduit.Face;
import com.itmecasey.barelypipes.core.conduit.PipeType;
import com.itmecasey.barelypipes.registry.ModBlocks;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A development-only camera tour for the client check: it opens a fresh superflat world, builds a few pipe scenes, saves
 * screenshots into {@code run/screenshots} as {@code barelypipes_<name>.png}, opens the pipe screen once, then quits. It
 * does nothing unless the environment variable {@code BARELYPIPES_SHOTS} is set. Progress goes to the log as
 * {@code BARELYPIPES-SHOTS} lines. It never touches the OS cursor or keyboard: the camera is moved through the player, the
 * screen through the server, and the mouse is released every tick.
 */
public final class ShotTour {
    private static final Logger LOGGER = LoggerFactory.getLogger("BarelyPipes");
    private static final boolean ENABLED = System.getenv("BARELYPIPES_SHOTS") != null;
    private static final String WORLD = "barelypipes_shots";

    private enum Stage { WAITING_FOR_MENU, LOADING_WORLD, TOURING, DONE }

    private record Step(int delay, Runnable action) {
    }

    private static final Deque<Step> STEPS = new ArrayDeque<>();
    private static Stage stage = Stage.WAITING_FOR_MENU;
    private static int idle;
    private static int wait;
    private static int ground;

    private ShotTour() {
    }

    public static void tick() {
        if (!ENABLED || stage == Stage.DONE) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.mouseHandler.isMouseGrabbed()) {
            minecraft.mouseHandler.releaseMouse();
        }
        switch (stage) {
            case WAITING_FOR_MENU -> waitForMenu(minecraft);
            case LOADING_WORLD -> waitForWorld(minecraft);
            case TOURING -> runSteps();
            default -> {
            }
        }
    }

    private static void waitForMenu(Minecraft minecraft) {
        if (minecraft.getOverlay() != null || minecraft.level != null || minecraft.screen == null || ++idle < 20) {
            return;
        }
        minecraft.options.pauseOnLostFocus = false;
        LevelStorageSource source = minecraft.getLevelSource();
        if (source.levelExists(WORLD)) {
            try (LevelStorageSource.LevelStorageAccess access = source.createAccess(WORLD)) {
                access.deleteLevel();
            } catch (IOException e) {
                LOGGER.warn("BARELYPIPES-SHOTS could not delete the old world", e);
            }
        }
        LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE,
                new LevelSettings.DifficultySettings(Difficulty.NORMAL, false, false), true, WorldDataConfiguration.DEFAULT);
        stage = Stage.LOADING_WORLD;
        idle = 0;
        LOGGER.info("BARELYPIPES-SHOTS creating world");
        minecraft.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(0L, false, false),
                WorldPresets::createFlatWorldDimensions, new TitleScreen());
    }

    private static void waitForWorld(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null || minecraft.screen != null
                || minecraft.getSingleplayerServer() == null || ++idle < 40) {
            return;
        }
        ground = minecraft.level.getHeight(Heightmap.Types.MOTION_BLOCKING, 0, 0);
        LOGGER.info("BARELYPIPES-SHOTS in the world, ground at y={}", ground);
        planTour();
        stage = Stage.TOURING;
    }

    private static void runSteps() {
        if (wait > 0) {
            wait--;
            return;
        }
        Step step = STEPS.poll();
        if (step == null) {
            return;
        }
        step.action().run();
        Step next = STEPS.peek();
        wait = next == null ? 0 : next.delay();
    }

    // --- The tour ----------------------------------------------------------------------------

    private static void planTour() {
        if ("rackplug".equals(System.getenv("BARELYPIPES_SHOTS"))) {
            planRackPlug();
            return;
        }
        then(0, () -> {
            server("gamerule advance_time false", "gamerule advance_weather false", "gamerule spawn_mobs false",
                    "time set noon", "weather clear", "kill @e[type=!player]", "clear @p");
            LocalPlayer player = player();
            player.getAbilities().flying = false;
            Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
            Minecraft.getInstance().options.hideGui = false;
            Minecraft.getInstance().options.guiScale().set(3);
        });
        // The whole family in the hotbar.
        then(5, () -> server("give @p barelypipes:iron_item_pipe", "give @p barelypipes:gold_fluid_pipe",
                "give @p barelypipes:diamond_energy_pipe", "give @p barelypipes:iron_gas_pipe", "give @p barelypipes:redstone_pipe",
                "give @p barelypipes:me_pipe", "give @p barelypipes:storage_pipe", "give @p barelypipes:pipe_wrench",
                "give @p barelypipes:conduit_tool"));
        then(10, () -> player().getInventory().setSelectedSlot(0));
        then(2, () -> moveTo(new Vec3(0.5, ground + 2.0, -2.0), new Vec3(0.5, ground + 0.5, 0.5)));
        shoot("hotbar_items", 20);
        then(2, () -> server("give @p barelypipes:speed_upgrade", "give @p barelypipes:capacity_upgrade",
                "give @p barelypipes:filter_upgrade", "give @p barelypipes:priority_upgrade", "give @p barelypipes:dense_me_pipe",
                "give @p barelypipes:gold_item_pipe", "give @p barelypipes:diamond_fluid_pipe", "give @p barelypipes:gold_energy_pipe"));
        then(10, () -> player().getInventory().setSelectedSlot(0));
        shoot("hotbar_items_more", 20);
        then(2, () -> server("clear @p"));

        // One bundle with all seven pipes and nothing attached.
        int ax = 4;
        then(4, () -> onServer(srv -> {
            ConduitBundleBlockEntity entity = place(new BlockPos(ax, ground, 0));
            for (PipeType type : PipeType.values()) {
                if (type != PipeType.DENSE_ME) {
                    entity.addPipe(type, Tier.GOLD);
                }
            }
        }));
        then(4, () -> moveTo(new Vec3(ax + 0.5, ground + 1.1, -1.4), new Vec3(ax + 0.5, ground + 0.5, 0.5)));
        shoot("bundle_all_front", 20);
        then(2, () -> moveTo(new Vec3(ax + 1.6, ground + 1.5, -1.1), new Vec3(ax + 0.5, ground + 0.5, 0.5)));
        shoot("bundle_all_side", 6);
        then(2, () -> moveTo(new Vec3(ax + 0.5, ground + 2.4, 0.5 - 0.3), new Vec3(ax + 0.5, ground + 0.5, 0.5)));
        shoot("bundle_all_top", 6);

        // Three tiers of the item, fluid and energy pipe in rows, four blocks long.
        int rowZ = 6;
        then(4, () -> onServer(srv -> {
            for (int tier = 0; tier < 3; tier++) {
                for (int i = 0; i < 4; i++) {
                    ConduitBundleBlockEntity entity = place(new BlockPos(i * 1, ground + tier * 2, rowZ));
                    entity.addPipe(PipeType.ITEM, Tier.values()[tier]);
                    entity.addPipe(PipeType.FLUID, Tier.values()[tier]);
                    entity.addPipe(PipeType.ENERGY, Tier.values()[tier]);
                    entity.addPipe(PipeType.GAS, Tier.values()[tier]);
                }
            }
            for (int i = 0; i < 4; i++) {
                srv.overworld().setBlock(new BlockPos(i, ground - 1 + 0, rowZ), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            }
        }));
        then(4, () -> moveTo(new Vec3(1.5, ground + 3.0, rowZ - 5.0), new Vec3(1.5, ground + 2.0, rowZ + 0.5)));
        shoot("tiers_rows_front", 20);
        then(2, () -> moveTo(new Vec3(5.5, ground + 3.0, rowZ - 2.0), new Vec3(1.5, ground + 2.0, rowZ + 0.5)));
        shoot("tiers_rows_side", 6);

        // A bundle attached to a chest and the dev test tank: plates, plugs, connections.
        int cx = 10;
        then(4, () -> onServer(srv -> {
            srv.overworld().setBlock(new BlockPos(cx - 1, ground, 0), Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
            srv.overworld().setBlock(new BlockPos(cx + 1, ground, 0), DevHooks.TEST_TANK.get().defaultBlockState(), Block.UPDATE_ALL);
            ConduitBundleBlockEntity entity = place(new BlockPos(cx, ground, 0));
            entity.addPipe(PipeType.ITEM, Tier.GOLD);
            entity.addPipe(PipeType.FLUID, Tier.IRON);
            entity.addPipe(PipeType.ENERGY, Tier.DIAMOND);
            entity.addPipe(PipeType.REDSTONE, Tier.IRON);
            entity.refreshConnections();
            entity.bundle().setMode(PipeType.ITEM.lane(), Face.WEST, ConnectionMode.IMPORT);
            entity.changed();
        }));
        then(4, () -> moveTo(new Vec3(cx + 0.5, ground + 1.3, -1.6), new Vec3(cx + 0.5, ground + 0.5, 0.5)));
        shoot("attached_front", 20);
        then(2, () -> moveTo(new Vec3(cx - 0.4, ground + 1.2, -0.9), new Vec3(cx - 0.4, ground + 0.5, 0.5)));
        shoot("attached_chest_side", 6);

        // The look-at overlay (Show Me What This Is, when installed): the item pipe's arm, its plug and the fluid pipe's core.
        then(4, () -> aimAt(new BlockPos(cx, ground, 0), "arm"));
        shoot("look_item_arm", 30);
        then(2, () -> aimAt(new BlockPos(cx, ground, 0), "plug"));
        shoot("look_item_plug", 30);
        then(2, () -> aimAt(new BlockPos(cx, ground, 0), "core"));
        shoot("look_core", 30);

        // The pipe screen on the item pipe's chest side.
        then(4, () -> onServer(srv -> {
            ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) srv.overworld().getBlockEntity(new BlockPos(cx, ground, 0));
            ServerPlayer player = srv.getPlayerList().getPlayers().get(0);
            PipeConfigMenu.open(player, entity, PipeType.ITEM, Face.WEST);
        }));
        shoot("pipe_screen_item", 40);
        then(2, () -> onServer(srv -> srv.getPlayerList().getPlayers().get(0).closeContainer()));
        then(10, () -> onServer(srv -> {
            ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) srv.overworld().getBlockEntity(new BlockPos(cx, ground, 0));
            PipeConfigMenu.open(srv.getPlayerList().getPlayers().get(0), entity, PipeType.ENERGY, Face.EAST);
        }));
        shoot("pipe_screen_energy", 40);
        then(2, () -> onServer(srv -> srv.getPlayerList().getPlayers().get(0).closeContainer()));

        // The item pipe screen again with a Filter and a Priority upgrade installed, so both side panels show.
        then(10, () -> onServer(srv -> {
            ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) srv.overworld().getBlockEntity(new BlockPos(cx, ground, 0));
            int lane = PipeType.ITEM.lane();
            entity.endpoints().install(lane, Face.WEST, com.itmecasey.barelypipes.core.conduit.EndpointSettings.Upgrade.FILTER);
            entity.endpoints().install(lane, Face.WEST, com.itmecasey.barelypipes.core.conduit.EndpointSettings.Upgrade.PRIORITY);
            entity.changed();
            PipeConfigMenu.open(srv.getPlayerList().getPlayers().get(0), entity, PipeType.ITEM, Face.WEST);
        }));
        shoot("pipe_screen_item_panels", 40);
        then(2, () -> onServer(srv -> srv.getPlayerList().getPlayers().get(0).closeContainer()));

        planOutlines();
        planPlugArrows();
        planCloseups();

        // BarelyStorage: a rack, a bus cable, a storage pipe and a chest.
        if (com.itmecasey.barelypipes.conduit.transfer.StorageLink.loaded()) {
            planStorage();
        }
        then(20, () -> {
            LOGGER.info("BARELYPIPES-SHOTS DONE");
            stage = Stage.DONE;
            Minecraft.getInstance().stop();
        });
    }

    /** One fluid pipe from a tank to another pipe: the vanilla outline with the crosshair on a plug, an arm and the core. */
    private static void planOutlines() {
        int ox = 22;
        then(4, () -> onServer(srv -> {
            srv.overworld().setBlock(new BlockPos(ox, ground, 1), DevHooks.TEST_TANK.get().defaultBlockState(), Block.UPDATE_ALL);
            ConduitBundleBlockEntity far = place(new BlockPos(ox, ground, -1));
            far.addPipe(PipeType.FLUID, Tier.IRON);
            ConduitBundleBlockEntity entity = place(new BlockPos(ox, ground, 0));
            entity.addPipe(PipeType.FLUID, Tier.IRON);
            entity.addPipe(PipeType.ITEM, Tier.GOLD);
            entity.refreshConnections();
            far.refreshConnections();
            entity.changed();
        }));
        then(10, () -> onServer(srv -> logShape(srv, new BlockPos(ox, ground, 0))));
        // The camera looks along -x at the middle of each piece of the fluid pipe's south arm, so the crosshair is on it.
        then(4, () -> aimSide(new BlockPos(ox, ground, 0), PipeType.FLUID, "plug"));
        shoot("outline_plug", 30);
        then(2, () -> aimSide(new BlockPos(ox, ground, 0), PipeType.FLUID, "arm"));
        shoot("outline_arm", 6);
        then(2, () -> aimSide(new BlockPos(ox, ground, 0), PipeType.FLUID, "core"));
        shoot("outline_core", 6);
        then(2, () -> moveTo(new Vec3(ox + 0.5, ground + 1.4, -0.9), new Vec3(ox + 0.5, ground + 0.5, 0.5)));
        shoot("outline_overview", 6);
    }

    /** Fluid and energy pipes on a tank in every plug arrow combination, seen from the side to show the glyph faces. */
    private static void planPlugArrows() {
        ConnectionMode[] fluid = {ConnectionMode.IMPORT, ConnectionMode.EXPORT, ConnectionMode.OPEN, ConnectionMode.OFF};
        ConnectionMode[] energy = {ConnectionMode.EXPORT, ConnectionMode.OPEN, ConnectionMode.OFF, ConnectionMode.IMPORT};
        then(4, () -> onServer(srv -> {
            for (int i = 0; i < 4; i++) {
                int bx = 26 + i * 3;
                srv.overworld().setBlock(new BlockPos(bx, ground, 1), DevHooks.TEST_TANK.get().defaultBlockState(), Block.UPDATE_ALL);
                ConduitBundleBlockEntity entity = place(new BlockPos(bx, ground, 0));
                entity.addPipe(PipeType.FLUID, Tier.values()[i % 3]);
                entity.addPipe(PipeType.ENERGY, Tier.values()[(i + 1) % 3]);
                entity.refreshConnections();
                entity.bundle().setMode(PipeType.FLUID.lane(), Face.SOUTH, fluid[i]);
                entity.bundle().setMode(PipeType.ENERGY.lane(), Face.SOUTH, energy[i]);
                entity.refreshConnections();
                entity.changed();
            }
        }));
        for (int i = 0; i < 4; i++) {
            int bx = 26 + i * 3;
            then(4, () -> moveTo(new Vec3(bx + 0.5 - 1.5, ground + 0.75, -0.15), new Vec3(bx + 0.5, ground + 0.5, 0.76)));
            shoot("plug_arrows_" + fluid[i].name().toLowerCase(Locale.ROOT), i == 0 ? 30 : 6);
            then(2, () -> moveTo(new Vec3(bx + 0.5 + 1.5, ground + 0.75, -0.15), new Vec3(bx + 0.5, ground + 0.5, 0.76)));
            shoot("plug_arrows_" + fluid[i].name().toLowerCase(Locale.ROOT) + "_right", 6);
        }
    }

    /** Logs what the client's real hit result is when a frame is taken, and which pipe piece it resolves to. */
    private static void logHit(Minecraft minecraft, String name) {
        if (!(minecraft.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit)
                || hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS) {
            LOGGER.info("BARELYPIPES-SHOTS {} crosshair target: {}", name, minecraft.hitResult == null ? "none" : minecraft.hitResult.getType());
            return;
        }
        BlockPos pos = hit.getBlockPos();
        String resolved = "";
        if (minecraft.level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity entity) {
            Vec3 local = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ()).scale(16.0);
            resolved = " -> " + com.itmecasey.barelypipes.conduit.config.PipeConfigActions.resolveTarget(entity.bundle(), local.x, local.y, local.z);
        }
        LOGGER.info("BARELYPIPES-SHOTS {} crosshair target: {} {} at {}{}", name, minecraft.level.getBlockState(pos).getBlock(), pos,
                hit.getLocation(), resolved);
    }

    private static void logShape(MinecraftServer srv, BlockPos pos) {
        var shape = srv.overworld().getBlockState(pos).getShape(srv.overworld(), pos);
        LOGGER.info("BARELYPIPES-SHOTS shape boxes at {} (pixels): {}", pos, shape.toAabbs().stream().map(b -> String.format(Locale.ROOT,
                "[%.2f %.2f %.2f %.2f %.2f %.2f]", b.minX * 16, b.minY * 16, b.minZ * 16, b.maxX * 16, b.maxY * 16, b.maxZ * 16)).toList());
    }

    /** The box (block pixels) of a piece of one pipe on the south face: its plug, its arm or its core (or the junction). */
    private static Box pieceBox(ConduitBundleBlockEntity entity, PipeType type, Face face, String what) {
        ConduitLayout layout = ConduitLayout.of(entity.bundle());
        return layout.pieces().stream().filter(p -> switch (what) {
            case "plug" -> p.kind() == ConduitLayout.Kind.PLUG && p.type() == type && p.face() == face;
            case "arm" -> p.kind() == ConduitLayout.Kind.ARM && p.type() == type && p.face() == face;
            default -> (p.kind() == ConduitLayout.Kind.CORE && p.type() == type) || p.kind() == ConduitLayout.Kind.JUNCTION;
        }).findFirst().orElseThrow().box();
    }

    private static Vec3 center(BlockPos pos, Box box) {
        return new Vec3(pos.getX() + (box.minX() + box.maxX()) / 32.0, pos.getY() + (box.minY() + box.maxY()) / 32.0,
                pos.getZ() + (box.minZ() + box.maxZ()) / 32.0);
    }

    /** Camera a block east of the piece, looking west along -x with the crosshair on the middle of the piece. */
    private static void aimSide(BlockPos pos, PipeType type, String what) {
        ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) Minecraft.getInstance().level.getBlockEntity(pos);
        Vec3 target = center(pos, pieceBox(entity, type, Face.SOUTH, what));
        moveTo(target.add(1.0, 0.0, 0.0), target);
    }

    /**
     * Camera close to a plug's glyph face (the face that looks into the bundle, toward -z for a south plug), 0.4 block in front
     * of it and about {@code side} blocks off to the side of the pipe axis (0.55 is about 54 degrees), crosshair on the glyph.
     */
    private static void aimPlugClose(BlockPos pos, PipeType type, double side) {
        ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) Minecraft.getInstance().level.getBlockEntity(pos);
        Box box = pieceBox(entity, type, Face.SOUTH, "plug");
        Vec3 target = new Vec3(pos.getX() + (box.minX() + box.maxX()) / 32.0, pos.getY() + (box.minY() + box.maxY()) / 32.0,
                pos.getZ() + box.minZ() / 16.0);
        moveTo(target.add(side, 0.0, -0.4), target);
    }

    private record Closeup(String name, PipeType type, Tier tier, ConnectionMode mode, Block neighbor) {
    }

    /**
     * Close-ups of each plug's glyph face with the crosshair on it (so the vanilla outline draws on that plug): the item pipe in
     * all four modes next to a chest, then the fluid, energy, storage and redstone plugs. The gas plug needs Mekanism and is
     * in the Mekanism builds' tour.
     */
    private static void planCloseups() {
        Closeup[] specs = {
                new Closeup("item_import", PipeType.ITEM, Tier.IRON, ConnectionMode.IMPORT, Blocks.CHEST),
                new Closeup("item_export", PipeType.ITEM, Tier.GOLD, ConnectionMode.EXPORT, Blocks.CHEST),
                new Closeup("item_open", PipeType.ITEM, Tier.DIAMOND, ConnectionMode.OPEN, Blocks.CHEST),
                new Closeup("item_off", PipeType.ITEM, Tier.IRON, ConnectionMode.OFF, Blocks.CHEST),
                new Closeup("fluid_import", PipeType.FLUID, Tier.GOLD, ConnectionMode.IMPORT, null),
                new Closeup("energy_export", PipeType.ENERGY, Tier.DIAMOND, ConnectionMode.EXPORT, null),
                new Closeup("storage_open", PipeType.STORAGE, Tier.IRON, ConnectionMode.OPEN, Blocks.CHEST),
                new Closeup("redstone", PipeType.REDSTONE, Tier.IRON, ConnectionMode.OPEN, Blocks.REDSTONE_BLOCK),
        };
        int base = 48;
        then(4, () -> onServer(srv -> {
            for (int i = 0; i < specs.length; i++) {
                int bx = base + i * 3;
                Closeup spec = specs[i];
                srv.overworld().setBlock(new BlockPos(bx, ground, 1), spec.neighbor() != null ? spec.neighbor().defaultBlockState()
                        : DevHooks.TEST_TANK.get().defaultBlockState(), Block.UPDATE_ALL);
                ConduitBundleBlockEntity entity = place(new BlockPos(bx, ground, 0));
                entity.addPipe(spec.type(), spec.tier());
                entity.refreshConnections();
                entity.bundle().setMode(spec.type().lane(), Face.SOUTH, spec.mode());
                entity.refreshConnections();
                entity.changed();
            }
        }));
        then(10, () -> onServer(srv -> logShape(srv, new BlockPos(base, ground, 0))));
        for (int i = 0; i < specs.length; i++) {
            BlockPos pos = new BlockPos(base + i * 3, ground, 0);
            Closeup spec = specs[i];
            then(4, () -> aimPlugClose(pos, spec.type(), 0.55));
            shoot("closeup_" + spec.name(), i == 0 ? 30 : 8);
            then(2, () -> aimPlugClose(pos, spec.type(), -0.55));
            shoot("closeup_" + spec.name() + "_left", 8);
        }
        // The same plug seen from farther off, and the arm and the core with the crosshair on them (the outline should hug each).
        BlockPos first = new BlockPos(base, ground, 0);
        then(4, () -> aimPlugClose(first, PipeType.ITEM, 1.0));
        shoot("closeup_item_import_far", 8);
        then(4, () -> {
            ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) Minecraft.getInstance().level.getBlockEntity(first);
            Vec3 target = center(first, pieceBox(entity, PipeType.ITEM, Face.SOUTH, "arm"));
            moveTo(target.add(0.45, 0.25, -0.1), target);
        });
        shoot("closeup_item_import_arm", 8);
        then(4, () -> {
            ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) Minecraft.getInstance().level.getBlockEntity(first);
            Vec3 target = center(first, pieceBox(entity, PipeType.ITEM, Face.SOUTH, "core"));
            moveTo(target.add(0.5, 0.3, -0.3), target);
        });
        shoot("closeup_item_import_core", 8);
    }

    /**
     * The two open checks of the 2026.11.3 status: BarelyStorage's rack, terminals and assembler seen from above (their top faces must
     * be drawn, not see-through), and a plug's glyph face seen straight on, from above its arm and from a small angle, close and far.
     * BARELYPIPES_SHOTS=rackplug selects it.
     */
    private static void planRackPlug() {
        then(0, () -> {
            server("gamerule advance_time false", "gamerule advance_weather false", "gamerule spawn_mobs false",
                    "time set noon", "weather clear", "kill @e[type=!player]", "clear @p");
            player().getAbilities().flying = false;
            Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
            Minecraft.getInstance().options.hideGui = false;
            Minecraft.getInstance().options.guiScale().set(3);
        });
        int bx = 30;
        then(4, () -> onServer(srv -> {
            var level = srv.overworld();
            String[] names = {"server_rack", "storage_terminal", "crafting_terminal", "assembler"};
            for (int i = 0; i < names.length; i++) {
                level.setBlock(new BlockPos(bx + i * 2, ground, 0), com.itmecasey.barelystorage.ModContent.blocks().get(names[i]).defaultBlockState(), Block.UPDATE_ALL);
            }
        }));
        then(10, () -> moveTo(new Vec3(bx + 3.0, ground + 3.2, -2.6), new Vec3(bx + 3.0, ground + 1.0, 0.5)));
        shoot("rack_tops_far", 30);
        then(2, () -> moveTo(new Vec3(bx + 1.0, ground + 2.4, -1.6), new Vec3(bx + 1.0, ground + 1.0, 0.5)));
        shoot("rack_tops_near", 6);

        // The plug glyph face, item pipe in import mode next to a chest, south plug (glyph faces -z).
        int px = 48;
        then(4, () -> onServer(srv -> {
            srv.overworld().setBlock(new BlockPos(px, ground, 1), Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
            ConduitBundleBlockEntity entity = place(new BlockPos(px, ground, 0));
            entity.addPipe(PipeType.ITEM, Tier.GOLD);
            entity.refreshConnections();
            entity.bundle().setMode(PipeType.ITEM.lane(), Face.SOUTH, ConnectionMode.IMPORT);
            entity.refreshConnections();
            entity.changed();
        }));
        BlockPos pos = new BlockPos(px, ground, 0);
        double[][] views = {
                // {camera dx from the glyph centre, dy, distance in front (-z), name index}
                {0.0, 0.0, 0.45}, {0.0, 0.14, 0.45}, {0.0, 0.30, 0.45}, {0.0, 0.0, 1.0}, {0.12, 0.12, 0.30},
        };
        String[] viewNames = {"plug_axis_close", "plug_over_arm_close", "plug_above_close", "plug_axis_far", "plug_offset_near"};
        for (int i = 0; i < views.length; i++) {
            double[] v = views[i];
            then(4, () -> {
                ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) Minecraft.getInstance().level.getBlockEntity(pos);
                Box box = pieceBox(entity, PipeType.ITEM, Face.SOUTH, "plug");
                Vec3 target = new Vec3(pos.getX() + (box.minX() + box.maxX()) / 32.0, pos.getY() + (box.minY() + box.maxY()) / 32.0,
                        pos.getZ() + box.minZ() / 16.0);
                moveTo(target.add(v[0], v[1], -v[2]), target);
            });
            shoot(viewNames[i], i == 0 ? 20 : 6);
        }
        // The same plug in export and open mode straight on.
        then(4, () -> onServer(srv -> {
            for (int k = 0; k < 2; k++) {
                BlockPos q = new BlockPos(px + 3 + k * 3, ground, 0);
                srv.overworld().setBlock(new BlockPos(q.getX(), ground, 1), Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
                ConduitBundleBlockEntity entity = place(q);
                entity.addPipe(PipeType.ITEM, Tier.GOLD);
                entity.refreshConnections();
                entity.bundle().setMode(PipeType.ITEM.lane(), Face.SOUTH, k == 0 ? ConnectionMode.EXPORT : ConnectionMode.OPEN);
                entity.refreshConnections();
                entity.changed();
            }
        }));
        String[] modeNames = {"plug_export_over_arm", "plug_open_over_arm"};
        for (int k = 0; k < 2; k++) {
            BlockPos q = new BlockPos(px + 3 + k * 3, ground, 0);
            String name = modeNames[k];
            then(8, () -> {
                ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) Minecraft.getInstance().level.getBlockEntity(q);
                Box box = pieceBox(entity, PipeType.ITEM, Face.SOUTH, "plug");
                Vec3 target = new Vec3(q.getX() + (box.minX() + box.maxX()) / 32.0, q.getY() + (box.minY() + box.maxY()) / 32.0,
                        q.getZ() + box.minZ() / 16.0);
                moveTo(target.add(0.0, 0.14, -0.45), target);
            });
            shoot(name, 6);
        }
        // The arrow overlays on the plug's side faces: import (px), export (px + 3) and open (px + 6), seen from the east side and from above.
        then(4, () -> onServer(srv -> {
            BlockPos q = new BlockPos(px + 6, ground, 0);
            if (srv.overworld().getBlockEntity(q) == null) {
                srv.overworld().setBlock(new BlockPos(q.getX(), ground, 1), Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
                ConduitBundleBlockEntity entity = place(q);
                entity.addPipe(PipeType.ITEM, Tier.GOLD);
                entity.refreshConnections();
                entity.bundle().setMode(PipeType.ITEM.lane(), Face.SOUTH, ConnectionMode.OPEN);
                entity.refreshConnections();
                entity.changed();
            }
        }));
        String[] sideModes = {"import", "export", "open"};
        for (int k = 0; k < 3; k++) {
            BlockPos q = new BlockPos(px + k * 3, ground, 0);
            String mode = sideModes[k];
            then(8, () -> {
                ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) Minecraft.getInstance().level.getBlockEntity(q);
                Vec3 c = center(q, pieceBox(entity, PipeType.ITEM, Face.SOUTH, "plug"));
                moveTo(c.add(0.55, 0.12, -0.12), c);
            });
            shoot("side_" + mode, 6);
            then(4, () -> {
                ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) Minecraft.getInstance().level.getBlockEntity(q);
                Vec3 c = center(q, pieceBox(entity, PipeType.ITEM, Face.SOUTH, "plug"));
                moveTo(c.add(0.0, 0.6, -0.15), c);
            });
            shoot("top_" + mode, 6);
            then(4, () -> {
                ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) Minecraft.getInstance().level.getBlockEntity(q);
                Vec3 c = center(q, pieceBox(entity, PipeType.ITEM, Face.SOUTH, "plug"));
                moveTo(c.add(0.0, 1.6, -1.8), c);
            });
            shoot("normal_" + mode, 6);
        }
        then(20, () -> {
            LOGGER.info("BARELYPIPES-SHOTS DONE");
            stage = Stage.DONE;
            Minecraft.getInstance().stop();
        });
    }

    private static void planStorage() {
        int sx = 16;
        then(4, () -> onServer(srv -> {
            var level = srv.overworld();
            level.setBlock(new BlockPos(sx, ground, 0), com.itmecasey.barelystorage.ModContent.blocks().get("server_rack").defaultBlockState(), Block.UPDATE_ALL);
            if (level.getBlockEntity(new BlockPos(sx, ground, 0)) instanceof com.itmecasey.barelystorage.block.RackBlockEntity rack) {
                rack.drives.setItem(0, new ItemStack(com.itmecasey.barelystorage.ModContent.item("cartridge_iron")));
                rack.setEnergy(rack.maxEnergy());
            }
            level.setBlock(new BlockPos(sx + 1, ground, 0), com.itmecasey.barelystorage.ModContent.blocks().get("rack_bus_cable").defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(new BlockPos(sx + 3, ground, 0), Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
            ConduitBundleBlockEntity entity = place(new BlockPos(sx + 2, ground, 0));
            entity.addPipe(PipeType.STORAGE);
            entity.addPipe(PipeType.ITEM, Tier.GOLD);
            entity.refreshConnections();
            entity.bundle().setMode(PipeType.STORAGE.lane(), Face.EAST, ConnectionMode.IMPORT);
            entity.bundle().setMode(PipeType.STORAGE.lane(), Face.WEST, ConnectionMode.EXPORT);
            entity.changed();
            if (level.getBlockEntity(new BlockPos(sx + 3, ground, 0)) instanceof net.minecraft.world.Container chest) {
                chest.setItem(0, new ItemStack(Items.COBBLESTONE, 64));
            }
        }));
        then(4, () -> moveTo(new Vec3(sx + 2.0, ground + 1.4, -2.2), new Vec3(sx + 2.0, ground + 0.5, 0.5)));
        shoot("storage_pipe_start", 6);
        shoot("storage_pipe_after", 120);
        then(2, () -> moveTo(new Vec3(sx + 2.5, ground + 1.0, -0.9), new Vec3(sx + 2.5, ground + 0.5, 0.5)));
        shoot("storage_pipe_close", 6);
        then(2, () -> onServer(srv -> {
            if (srv.overworld().getBlockEntity(new BlockPos(sx + 2, ground, 0)) instanceof ConduitBundleBlockEntity entity) {
                PipeConfigMenu.open(srv.getPlayerList().getPlayers().get(0), entity, PipeType.STORAGE, Face.WEST);
            }
        }));
        shoot("pipe_screen_storage", 40);
        then(2, () -> onServer(srv -> srv.getPlayerList().getPlayers().get(0).closeContainer()));
    }

    private static ConduitBundleBlockEntity place(BlockPos pos) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        server.overworld().setBlock(pos, ModBlocks.CONDUIT_BUNDLE.get().defaultBlockState(), Block.UPDATE_ALL);
        return (ConduitBundleBlockEntity) server.overworld().getBlockEntity(pos);
    }

    /** Puts the camera a block north of a part of the bundle at {@code pos}, looking at it: the item pipe's west arm or plug, or a core or the junction box. */
    private static void aimAt(BlockPos pos, String what) {
        ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) Minecraft.getInstance().level.getBlockEntity(pos);
        ConduitLayout layout = ConduitLayout.of(entity.bundle());
        Box box;
        switch (what) {
            case "arm" -> {
                Box arm = layout.armOf(PipeType.ITEM, Face.WEST);
                box = new Box(arm.maxX() - 1.0, arm.minY(), arm.minZ(), arm.maxX(), arm.maxY(), arm.maxZ());
            }
            case "plug" -> box = layout.pieces().stream().filter(p -> p.kind() == ConduitLayout.Kind.PLUG && p.type() == PipeType.ITEM)
                    .findFirst().orElseThrow().box();
            default -> box = layout.pieces().stream().filter(p -> p.kind() == ConduitLayout.Kind.CORE || p.kind() == ConduitLayout.Kind.JUNCTION)
                    .findFirst().orElseThrow().box();
        }
        Vec3 target = new Vec3(pos.getX() + (box.minX() + box.maxX()) / 32.0, pos.getY() + (box.minY() + box.maxY()) / 32.0,
                pos.getZ() + (box.minZ() + box.maxZ()) / 32.0);
        moveTo(target.add(0.0, 0.0, -1.2), target);
    }

    static void moveTo(Vec3 eye, Vec3 target) {
        Vec3 d = target.subtract(eye);
        float yaw = (float) (Mth.atan2(d.z, d.x) * Mth.RAD_TO_DEG) - 90.0F;
        float pitch = (float) -(Mth.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)) * Mth.RAD_TO_DEG);
        server(String.format(Locale.ROOT, "tp @a %.3f %.3f %.3f %.2f %.2f", eye.x, eye.y - 1.62, eye.z, yaw, pitch));
        LocalPlayer player = player();
        player.snapTo(eye.x, eye.y - 1.62, eye.z, yaw, pitch);
        player.setYHeadRot(yaw);
        player.setDeltaMovement(Vec3.ZERO);
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
    }

    static void shoot(String name, int delay) {
        then(delay, () -> {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.gui.getChat().clearMessages(false);
            minecraft.getToastManager().clear();
        });
        then(2, () -> {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer self = minecraft.player;
            LOGGER.info("BARELYPIPES-SHOTS {} eye={} pitch={} yaw={}", name, self.getEyePosition(), self.getXRot(), self.getYRot());
            logHit(minecraft, name);
            Screenshot.grab(minecraft.gameDirectory, "barelypipes_" + name + ".png", minecraft.getMainRenderTarget(), 1,
                    message -> LOGGER.info("BARELYPIPES-SHOTS saved {}", name));
        });
    }

    static void then(int delay, Runnable action) {
        STEPS.add(new Step(delay, action));
    }

    private static LocalPlayer player() {
        return Minecraft.getInstance().player;
    }

    static void onServer(java.util.function.Consumer<MinecraftServer> action) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) {
            server.execute(() -> action.accept(server));
        }
    }

    static void server(String... commands) {
        onServer(server -> {
            for (String command : commands) {
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
            }
        });
    }
}
