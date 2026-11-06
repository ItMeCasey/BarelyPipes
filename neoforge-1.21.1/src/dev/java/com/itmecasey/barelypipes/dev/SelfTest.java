package com.itmecasey.barelypipes.dev;

import com.itmecasey.barelypipes.BarelyPipes;
import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import com.itmecasey.barelypipes.core.conduit.ConduitLayout;
import com.itmecasey.barelypipes.core.conduit.ConnectionMode;
import com.itmecasey.barelypipes.core.conduit.Face;
import com.itmecasey.barelypipes.core.conduit.PipeType;
import com.itmecasey.barelypipes.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The headless self test. It runs inside a real server when the environment variable {@code BARELYPIPES_SELFTEST} is
 * set: it places bundles, joins them, saves and loads them, then lets the transfer, tool and screen scenarios run.
 * Results go to the log as {@code SELFTEST PASS/FAIL <name>} and a last line {@code SELFTEST DONE failures=<n>}; the
 * server then stops.
 */
public final class SelfTest {
    private static final String ENV = "BARELYPIPES_SELFTEST";

    private static MinecraftServer server;
    private static ServerLevel level;
    private static BlockPos origin;
    private static boolean running;
    private static int passed;
    private static int failed;

    private SelfTest() {
    }

    public static boolean requested() {
        return System.getenv(ENV) != null;
    }

    public static void start(MinecraftServer started) {
        server = started;
        level = server.overworld();
        // High above any terrain, inside the always-loaded spawn chunks.
        origin = new BlockPos(2, 200, 2);
        command("gamerule doDaylightCycle false");
        command("gamerule doWeatherCycle false");
        command("weather clear");
        // Only forced chunks tick without a player nearby, and block entities tick only in those.
        command("forceload add 0 0");
        runBundleChecks();
        TransferSelfTest.setUp(level);
        ToolSelfTest.setUp(level);
        ScreenSelfTest.setUp(level);
        running = true;
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (!running) {
            return;
        }
        TransferSelfTest.tick();
        // The tool checks place and remove many bundles, which must not disturb the timed transfer checks.
        if (TransferSelfTest.finished()) {
            ToolSelfTest.tick();
        }
        if (ToolSelfTest.finished()) {
            ScreenSelfTest.tick();
        }
        if (TransferSelfTest.finished() && ToolSelfTest.finished() && ScreenSelfTest.finished()) {
            finish();
        }
    }

    // --- Conduit bundles ---------------------------------------------------------------------

    private static void runBundleChecks() {
        BlockPos a = origin;
        BlockPos b = origin.east();
        level.setBlock(a, ModBlocks.CONDUIT_BUNDLE.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(b, ModBlocks.CONDUIT_BUNDLE.get().defaultBlockState(), Block.UPDATE_ALL);
        ConduitBundleBlockEntity bundleA = (ConduitBundleBlockEntity) level.getBlockEntity(a);
        ConduitBundleBlockEntity bundleB = (ConduitBundleBlockEntity) level.getBlockEntity(b);
        check("bundle blocks have block entities", bundleA != null && bundleB != null);
        if (bundleA == null || bundleB == null) {
            return;
        }

        check("a free lane accepts a pipe", bundleA.addPipe(PipeType.FLUID));
        check("a taken lane refuses a second pipe", !bundleA.addPipe(PipeType.FLUID));
        bundleB.addPipe(PipeType.FLUID);
        int fluid = PipeType.FLUID.lane();
        check("matching pipes join (A east)", bundleA.bundle().isConnected(fluid, Face.EAST)
                && !bundleA.bundle().isExternal(fluid, Face.EAST));
        check("matching pipes join (B west)", bundleB.bundle().isConnected(fluid, Face.WEST));

        bundleB.addPipe(PipeType.ITEM);
        check("a pipe still joins its kind next to a bundle with more pipes",
                bundleA.bundle().isConnected(fluid, Face.EAST) && bundleB.bundle().isConnected(fluid, Face.WEST));
        check("a pipe with no match next door does not join",
                !bundleB.bundle().isConnected(PipeType.ITEM.lane(), Face.WEST));

        bundleA.bundle().setMode(fluid, Face.EAST, ConnectionMode.BLOCKED);
        bundleA.changed();
        check("a blocked face never connects", !bundleA.bundle().isConnected(fluid, Face.EAST));
        check("blocking one side drops the other side too", !bundleB.bundle().isConnected(fluid, Face.WEST));

        // A fluid pipe should attach to a tank, which draws a machine connector.
        BlockPos tankPos = a.south();
        level.setBlock(tankPos, DevHooks.TEST_TANK.get().defaultBlockState(), Block.UPDATE_ALL);
        bundleA.refreshConnections();
        check("a fluid pipe attaches to a tank", bundleA.bundle().isConnected(fluid, Face.SOUTH)
                && bundleA.bundle().isExternal(fluid, Face.SOUTH));
        check("an attachment adds a plate", ConduitLayout.of(bundleA.bundle()).plateOf(Face.SOUTH) != null);

        BlockState state = level.getBlockState(a);
        VoxelShape shape = state.getShape(level, a);
        check("the hit shape is not empty", !shape.isEmpty());
        check("the hit shape grows with connections", shape.max(Direction.Axis.Z) > 0.625);

        // Save, then load into a fresh block entity: the contents must survive.
        CompoundTag saved = bundleA.saveWithFullMetadata(level.registryAccess());
        ConduitBundleBlockEntity restored = new ConduitBundleBlockEntity(a, state);
        restored.loadWithComponents(saved, level.registryAccess());
        check("a bundle survives saving and loading", restored.bundle().equals(bundleA.bundle()));

        bundleA.addPipe(PipeType.ENERGY);
        List<ItemStack> drops = Block.getDrops(state, level, a, bundleA);
        check("breaking a bundle drops one item per pipe", drops.size() == bundleA.bundle().set().size());
    }

    // --- Plumbing ----------------------------------------------------------------------------

    private static void command(String command) {
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
    }

    /** Whether block entities at {@code pos} tick yet: the chunk must tick and its entities must have loaded. */
    static boolean blockEntitiesTick(ServerLevel level, BlockPos pos) {
        return level.shouldTickBlocksAt(pos) && level.isPositionEntityTicking(pos);
    }

    static void check(String name, boolean condition) {
        if (condition) {
            passed++;
        } else {
            failed++;
        }
        BarelyPipes.LOGGER.info("SELFTEST {} {}", condition ? "PASS" : "FAIL", name);
    }

    private static void finish() {
        BarelyPipes.LOGGER.info("SELFTEST DONE failures={} passed={}", failed, passed);
        running = false;
        server.halt(false);
    }
}
