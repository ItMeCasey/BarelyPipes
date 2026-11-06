package com.itmecasey.barelypipes.dev;

import com.itmecasey.barelypipes.BarelyPipes;
import com.itmecasey.barelypipes.compat.look.PipeLookAdapter;
import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import com.itmecasey.barelypipes.core.Tier;
import com.itmecasey.barelypipes.core.conduit.Box;
import com.itmecasey.barelypipes.core.conduit.ConduitLayout;
import com.itmecasey.barelypipes.core.conduit.ConnectionMode;
import com.itmecasey.barelypipes.core.conduit.Face;
import com.itmecasey.barelypipes.core.conduit.PipeLookInfo;
import com.itmecasey.barelypipes.core.conduit.PipeType;
import com.itmecasey.barelypipes.registry.ModItems;
import com.itmecasey.barelypipes.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
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
        command("gamerule advance_time false");
        command("gamerule advance_weather false");
        command("weather clear");
        // Only forced chunks tick without a player nearby, and block entities tick only in those.
        command("forceload add 0 0");
        runBundleChecks();
        TransferSelfTest.setUp(level);
        ToolSelfTest.setUp(level);
        ScreenSelfTest.setUp(level);
        if (com.itmecasey.barelypipes.conduit.transfer.StorageLink.loaded()) {
            StorageSelfTest.setUp(level);
        }
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
        if (com.itmecasey.barelypipes.conduit.transfer.StorageLink.loaded()) {
            StorageSelfTest.tick();
        }
        if (TransferSelfTest.finished() && ToolSelfTest.finished() && ScreenSelfTest.finished() && StorageSelfTest.finished()) {
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
        restored.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
        check("a bundle survives saving and loading", restored.bundle().equals(bundleA.bundle()));

        bundleA.addPipe(PipeType.ENERGY);
        List<ItemStack> drops = Block.getDrops(state, level, a, bundleA);
        check("breaking a bundle drops one item per pipe", drops.size() == bundleA.bundle().set().size());

        checkLookInfo();
    }

    /** What the look-at overlays say about a gold item pipe on a chest: the exact pipe, side, mode, neighbor and rate. */
    private static void checkLookInfo() {
        BlockPos pos = new BlockPos(12, 205, 12);
        level.setBlock(pos.south(), Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(pos, ModBlocks.CONDUIT_BUNDLE.get().defaultBlockState(), Block.UPDATE_ALL);
        ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) level.getBlockEntity(pos);
        if (entity == null) {
            check("look: the test bundle exists", false);
            return;
        }
        entity.addPipe(PipeType.ITEM, Tier.GOLD);
        entity.addPipe(PipeType.FLUID, Tier.IRON);
        entity.refreshConnections();
        int lane = PipeType.ITEM.lane();
        check("look: the gold item pipe attaches to the chest", entity.bundle().isExternal(lane, Face.SOUTH));

        // Aim at the inner end of the item pipe's arm toward the chest (the plug sits on its outer end).
        Box arm = ConduitLayout.of(entity.bundle()).armOf(PipeType.ITEM, Face.SOUTH);
        Vec3 hit = new Vec3(pos.getX() + (arm.minX() + arm.maxX()) / 32.0, pos.getY() + (arm.minY() + arm.maxY()) / 32.0,
                pos.getZ() + (arm.minZ() + 1.0) / 16.0);
        PipeLookInfo.Look look = PipeLookAdapter.look(level, pos, entity, hit);
        check("look: names the exact pipe, its tier and the part", look != null && look.type() == PipeType.ITEM
                && look.tier() == Tier.GOLD && look.part() == PipeLookInfo.Part.ARM && look.face() == Face.SOUTH);
        if (look == null) {
            return;
        }
        check("look: the title is the pipe item", ModItems.pipe(look.type(), look.tier()).get().getDescriptionId()
                .equals("item.barelypipes.gold_item_pipe") && PipeLookAdapter.stack(look).is(ModItems.pipe(PipeType.ITEM, Tier.GOLD).get()));
        List<PipeLookInfo.Text> lines = look.lines();
        check("look: the first line says arm and the side",
                lines.get(0).key().equals("smwti.barelypipes.arm") && lines.get(0).args().get(0).equals(PipeLookInfo.Text.of("face.barelypipes.south")));
        check("look: the mode is Open", lines.get(1).args().get(0).equals(PipeLookInfo.Text.of("connection_mode.barelypipes.open")));
        check("look: it says what it connects to", lines.get(2).equals(PipeLookInfo.Text.of("smwti.barelypipes.connected_to",
                PipeLookInfo.Text.of("block.minecraft.chest"))));
        check("look: the throughput is the gold item rate (8 items every 12 ticks)", lines.get(3).args().get(0)
                .equals(PipeLookInfo.Text.of("screen.barelypipes.pipe_config.rate_items", "8", "12")));
        check("look: the lines turn into components", PipeLookAdapter.lines(look).size() == lines.size());

        // The other pipe is the other pipe: the fluid pipe's core.
        Box core = ConduitLayout.of(entity.bundle()).pieces().stream()
                .filter(p -> p.type() == PipeType.FLUID && p.kind() == ConduitLayout.Kind.CORE).findFirst().map(ConduitLayout.Piece::box).orElse(null);
        if (core != null) {
            Vec3 c = new Vec3(pos.getX() + (core.minX() + core.maxX()) / 32.0, pos.getY() + (core.minY() + core.maxY()) / 32.0,
                    pos.getZ() + (core.minZ() + core.maxZ()) / 32.0);
            PipeLookInfo.Look other = PipeLookAdapter.look(level, pos, entity, c);
            check("look: a core is told apart from the other pipe", other != null && other.type() == PipeType.FLUID
                    && other.tier() == Tier.IRON && other.face() == null);
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(pos.south(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
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
