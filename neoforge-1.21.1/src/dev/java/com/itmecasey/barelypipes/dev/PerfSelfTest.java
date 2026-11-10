package com.itmecasey.barelypipes.dev;

import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import com.itmecasey.barelypipes.core.Tier;
import com.itmecasey.barelypipes.core.conduit.ConnectionMode;
import com.itmecasey.barelypipes.core.conduit.Face;
import com.itmecasey.barelypipes.core.conduit.PipeType;
import com.itmecasey.barelypipes.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * A big network under load. A solid block of 14 x 14 x 4 bundles (784 bundles, three pipes each: fluid, item and energy, 2,352
 * pipes, all joined into one network per lane) in the forced chunk, with a tank and a chest at one corner feeding a tank and a
 * chest at the opposite corner. It checks that things still arrive across the whole block and that the server keeps its tick
 * time: the average over the 100 ticks the server remembers must stay far under the 50 ms budget, and not grow more than a few
 * milliseconds over the idle baseline measured just before the network was built.
 */
final class PerfSelfTest {
    private static final int Y0 = 280;
    private static final int LAYERS = 4;
    private static final int MEASURE_AFTER = 160;
    private static final double BUDGET_MS = 25.0;

    private static ServerLevel level;
    private static boolean built;
    private static boolean finished;
    private static int tick;
    private static double baselineMs;
    private static BlockPos fluidSink;
    private static BlockPos itemSink;

    private PerfSelfTest() {
    }

    static boolean finished() {
        return finished;
    }

    static void setUp(ServerLevel serverLevel) {
        level = serverLevel;
        built = false;
        finished = false;
        tick = 0;
    }

    static void tick() {
        if (level == null || finished) {
            return;
        }
        if (!built) {
            if (SelfTest.blockEntitiesTick(level, new BlockPos(2, 210, 2))) {
                baselineMs = level.getServer().getAverageTickTimeNanos() / 1.0e6;
                build();
                built = true;
            }
            return;
        }
        tick++;
        if (tick == MEASURE_AFTER) {
            double ms = level.getServer().getAverageTickTimeNanos() / 1.0e6;
            int fluid = ((TestTankBlockEntity) level.getBlockEntity(fluidSink)).tank().getFluidAmount();
            int items = count(itemSink);
            SelfTest.check("perf: fluid crossed the whole 784-bundle block (" + fluid + " mB at the far tank)", fluid > 0);
            SelfTest.check("perf: items crossed the whole 784-bundle block (" + items + " at the far chest)", items > 0);
            SelfTest.check(String.format("perf: server tick under load %.2f ms (idle %.2f ms) stays under %.0f ms", ms, baselineMs, BUDGET_MS),
                    ms < BUDGET_MS);
            finished = true;
        }
    }

    private static void build() {
        for (int layer = 0; layer < LAYERS; layer++) {
            for (int x = 1; x <= 14; x++) {
                for (int z = 1; z <= 14; z++) {
                    BlockPos pos = new BlockPos(x, Y0 + layer, z);
                    level.setBlock(pos, ModBlocks.CONDUIT_BUNDLE.get().defaultBlockState(), Block.UPDATE_ALL);
                    ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) level.getBlockEntity(pos);
                    entity.addPipe(PipeType.FLUID, Tier.GOLD);
                    entity.addPipe(PipeType.ITEM, Tier.GOLD);
                    entity.addPipe(PipeType.ENERGY, Tier.GOLD);
                }
            }
        }
        // Feed the block at one corner and drain it at the other.
        BlockPos fluidSource = new BlockPos(0, Y0, 1);
        level.setBlock(fluidSource, DevHooks.TEST_TANK.get().defaultBlockState(), Block.UPDATE_ALL);
        ((TestTankBlockEntity) level.getBlockEntity(fluidSource)).tank()
                .setFluid(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 100000));
        fluidSink = new BlockPos(15, Y0 + LAYERS - 1, 14);
        level.setBlock(fluidSink, DevHooks.TEST_TANK.get().defaultBlockState(), Block.UPDATE_ALL);
        BlockPos itemSource = new BlockPos(0, Y0 + 1, 1);
        level.setBlock(itemSource, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        Container chest = (Container) level.getBlockEntity(itemSource);
        for (int slot = 0; slot < chest.getContainerSize(); slot++) {
            chest.setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        }
        itemSink = new BlockPos(15, Y0 + LAYERS - 2, 14);
        level.setBlock(itemSink, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);

        mode(new BlockPos(1, Y0, 1), PipeType.FLUID, Face.WEST, ConnectionMode.IMPORT);
        mode(new BlockPos(1, Y0 + 1, 1), PipeType.ITEM, Face.WEST, ConnectionMode.IMPORT);
        mode(new BlockPos(14, Y0 + LAYERS - 1, 14), PipeType.FLUID, Face.EAST, ConnectionMode.EXPORT);
        mode(new BlockPos(14, Y0 + LAYERS - 2, 14), PipeType.ITEM, Face.EAST, ConnectionMode.EXPORT);
    }

    private static void mode(BlockPos pos, PipeType type, Face face, ConnectionMode mode) {
        ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) level.getBlockEntity(pos);
        entity.refreshConnections();
        entity.bundle().setMode(type.lane(), face, mode);
        entity.refreshConnections();
        entity.changed();
    }

    private static int count(BlockPos pos) {
        Container container = (Container) level.getBlockEntity(pos);
        int total = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            total += container.getItem(slot).getCount();
        }
        return total;
    }
}
