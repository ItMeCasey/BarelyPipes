package com.itmecasey.barelypipes.dev;

import com.itmecasey.barelypipes.compat.mekanism.MekanismGas;
import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import com.itmecasey.barelypipes.core.Tier;
import com.itmecasey.barelypipes.core.conduit.ConnectionMode;
import com.itmecasey.barelypipes.core.conduit.Face;
import com.itmecasey.barelypipes.core.conduit.PipeType;
import com.itmecasey.barelypipes.registry.ModBlocks;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * The gas pipe checks. They run only when Mekanism is installed (this class names Mekanism classes). Two Mekanism
 * chemical tanks joined by three iron gas pipes: the first is pulled from (IMPORT) and the last is pushed into (EXPORT).
 * The checks look at how much arrived, that nothing was lost and that no single tick moved more than the tier's rate.
 */
final class GasSelfTest {
    private static final int START = 40_000;
    private static final int RATE = 800;
    private static final int CHECK_EARLY = 10;
    private static final int CHECK_LATE = 120;
    private static final int Y = 300;
    private static final int Z = 2;
    private static final ResourceLocation TANK = new ResourceLocation("mekanism", "basic_chemical_tank");
    private static final ResourceLocation HYDROGEN = new ResourceLocation("mekanism", "hydrogen");

    private static ServerLevel level;
    private static boolean built;
    private static boolean finished = true;
    private static int tick;
    private static long previous;
    private static long largestStep;
    private static long earlyArrived;

    private static BlockPos source;
    private static BlockPos sink;

    private GasSelfTest() {
    }

    /** Whether the gas checks have run (true at once when Mekanism is absent). */
    static boolean finished() {
        return finished;
    }

    static void setUp(ServerLevel serverLevel) {
        level = serverLevel;
        built = false;
        tick = 0;
        finished = false;
    }

    static void tick() {
        if (level == null || finished) {
            return;
        }
        if (!built) {
            if (SelfTest.blockEntitiesTick(level, new BlockPos(2, Y, Z))) {
                build(level, Y, Z);
                built = true;
                previous = amountOf(level, source, HYDROGEN);
            }
            return;
        }
        tick++;
        long now = amountOf(level, source, HYDROGEN);
        largestStep = Math.max(largestStep, previous - now);
        previous = now;
        if (tick == CHECK_EARLY) {
            earlyArrived = amountOf(level, sink, HYDROGEN);
        }
        if (tick == CHECK_LATE) {
            check(level);
            finished = true;
        }
    }

    /** Builds the line at {@code y}: a full tank, three iron gas pipes, an empty tank. */
    static void build(ServerLevel level, int y, int z) {
        source = new BlockPos(1, y, z);
        sink = new BlockPos(5, y, z);
        // A tank gives out through its front and takes in on every other side, so both face east:
        // the full one's front meets the pipe line and the empty one takes it in through its back.
        placeTank(level, source, Direction.EAST);
        placeTank(level, sink, Direction.EAST);
        SelfTest.check("gas: a Mekanism chemical tank offers a gas handler", MekanismGas.handler(level, source, null) != null);
        fillTank(level, source, HYDROGEN, START);
        for (int x = 2; x <= 4; x++) {
            BlockPos pos = new BlockPos(x, y, z);
            level.setBlock(pos, ModBlocks.CONDUIT_BUNDLE.get().defaultBlockState(), Block.UPDATE_ALL);
            bundle(level, pos).addPipe(PipeType.GAS, Tier.IRON);
        }
        setMode(level, new BlockPos(2, y, z), Face.WEST, ConnectionMode.IMPORT);
        setMode(level, new BlockPos(4, y, z), Face.EAST, ConnectionMode.EXPORT);
    }

    static void check(ServerLevel level) {
        long left = amountOf(level, source, HYDROGEN);
        long arrived = amountOf(level, sink, HYDROGEN);
        SelfTest.check("gas: a gas pipe attaches to a Mekanism chemical tank",
                bundle(level, new BlockPos(2, source.getY(), source.getZ())).bundle().isConnected(PipeType.GAS.lane(), Face.WEST));
        SelfTest.check("gas: hydrogen arrives in the far tank through three gas pipes", arrived > 0);
        SelfTest.check("gas: nothing is lost or made on the way", left + arrived == START);
        SelfTest.check("gas: the whole amount arrives in the far tank (" + arrived + " of " + START + " mB)", arrived == START);
        SelfTest.check("gas: some gas had already arrived after " + CHECK_EARLY + " ticks, but no more than the rate allows ("
                + earlyArrived + " mB)", earlyArrived > 0 && earlyArrived <= (long) RATE * CHECK_EARLY);
        SelfTest.check("gas: an iron gas pipe moves exactly " + RATE + " mB per tick at most (largest step " + largestStep + " mB)",
                largestStep == RATE);
    }

    static void placeTank(ServerLevel level, BlockPos pos, Direction facing) {
        BlockState state = BuiltInRegistries.BLOCK.get(TANK).defaultBlockState();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        }
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }

    /** Fills a tank through whichever side takes gas in. */
    static void fillTank(ServerLevel level, BlockPos pos, ResourceLocation id, long amount) {
        Gas gas = MekanismAPI.gasRegistry().getValue(id);
        for (Direction side : Direction.values()) {
            IGasHandler handler = MekanismGas.handler(level, pos, side);
            if (handler != null && handler.insertChemical(new GasStack(gas, amount), Action.EXECUTE).isEmpty()) {
                return;
            }
        }
    }

    /** How much of a gas a block holds, in mB. */
    static long amountOf(net.minecraft.world.level.Level level, BlockPos pos, ResourceLocation id) {
        IGasHandler handler = MekanismGas.handler(level, pos, null);
        if (handler == null) {
            return 0;
        }
        Gas gas = MekanismAPI.gasRegistry().getValue(id);
        long total = 0;
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            GasStack held = handler.getChemicalInTank(tank);
            if (held.isTypeEqual(gas)) {
                total += held.getAmount();
            }
        }
        return total;
    }

    private static ConduitBundleBlockEntity bundle(ServerLevel level, BlockPos pos) {
        return (ConduitBundleBlockEntity) level.getBlockEntity(pos);
    }

    private static void setMode(ServerLevel level, BlockPos pos, Face face, ConnectionMode mode) {
        ConduitBundleBlockEntity entity = bundle(level, pos);
        entity.bundle().setMode(PipeType.GAS.lane(), face, mode);
        entity.refreshConnections();
        entity.changed();
    }
}
