package com.itmecasey.barelypipes.dev;

import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import com.itmecasey.barelypipes.conduit.config.PipeConfigMenu;
import com.itmecasey.barelypipes.core.Tier;
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
