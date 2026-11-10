package com.itmecasey.barelypipes.dev;

import com.itmecasey.barelypipes.compat.mekanism.MekanismChemicals;
import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import com.itmecasey.barelypipes.core.Tier;
import com.itmecasey.barelypipes.core.conduit.ConnectionMode;
import com.itmecasey.barelypipes.core.conduit.Face;
import com.itmecasey.barelypipes.core.conduit.PipeType;
import com.itmecasey.barelypipes.registry.ModBlocks;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
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
    private static final ResourceLocation TANK = ResourceLocation.fromNamespaceAndPath("mekanism", "basic_chemical_tank");
    private static final ResourceLocation HYDROGEN = ResourceLocation.fromNamespaceAndPath("mekanism", "hydrogen");

    private static ServerLevel level;
    private static boolean built;
    private static boolean finished = true;
    private static int tick;
    private static long previous;
    private static long largestStep;
    private static long earlyArrived;

    private static BlockPos source;
    private static BlockPos sink;
    // The second scenario: another chemical (chlorine) into another kind of machine (a chemical infuser) through the same pipes.
    private static final ResourceLocation INFUSER = ResourceLocation.fromNamespaceAndPath("mekanism", "chemical_infuser");
    private static final ResourceLocation CHLORINE = ResourceLocation.fromNamespaceAndPath("mekanism", "chlorine");
    private static final int START2 = 6_000;
    private static BlockPos source2;
    private static BlockPos sink2;

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
                build2(level, Y + 4, Z);
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
            check2(level);
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
        SelfTest.check("gas: a Mekanism chemical tank offers a chemical handler", MekanismChemicals.handler(level, source, null) != null);
        fillTank(level, source, HYDROGEN, START);
        for (int x = 2; x <= 4; x++) {
            BlockPos pos = new BlockPos(x, y, z);
            level.setBlock(pos, ModBlocks.CONDUIT_BUNDLE.get().defaultBlockState(), Block.UPDATE_ALL);
            bundle(level, pos).addPipe(PipeType.GAS, Tier.IRON);
        }
        setMode(level, new BlockPos(2, y, z), Face.WEST, ConnectionMode.IMPORT);
        setMode(level, new BlockPos(4, y, z), Face.EAST, ConnectionMode.EXPORT);
    }

    /** A tank of chlorine, three iron gas pipes and a Mekanism Chemical Infuser (its left side takes a chemical into an input tank). */
    private static void build2(ServerLevel level, int y, int z) {
        source2 = new BlockPos(1, y, z);
        sink2 = new BlockPos(5, y, z);
        placeTank(level, source2, Direction.EAST);
        placeMachine(level, sink2, INFUSER, Direction.NORTH);
        fillTank(level, source2, CHLORINE, START2);
        for (int x = 2; x <= 4; x++) {
            BlockPos pos = new BlockPos(x, y, z);
            level.setBlock(pos, ModBlocks.CONDUIT_BUNDLE.get().defaultBlockState(), Block.UPDATE_ALL);
            bundle(level, pos).addPipe(PipeType.GAS, Tier.IRON);
        }
        setMode(level, new BlockPos(2, y, z), Face.WEST, ConnectionMode.IMPORT);
        setMode(level, new BlockPos(4, y, z), Face.EAST, ConnectionMode.EXPORT);
    }

    private static void check2(ServerLevel level) {
        long left = amountOf(level, source2, CHLORINE);
        long arrived = amountOf(level, sink2, CHLORINE);
        SelfTest.check("gas: a Mekanism Chemical Infuser offers a chemical handler", MekanismChemicals.handler(level, sink2, null) != null);
        SelfTest.check("gas: chlorine (another chemical) arrives in a Chemical Infuser (another machine), " + arrived + " of " + START2 + " mB",
                arrived > 0);
        SelfTest.check("gas: chlorine is neither lost nor made on the way", left + arrived == START2);
    }

    private static void placeMachine(ServerLevel level, BlockPos pos, ResourceLocation id, Direction facing) {
        Block block = BuiltInRegistries.BLOCK.get(id);
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        }
        level.setBlock(pos, state, Block.UPDATE_ALL);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity != null) {
            entity.applyComponentsFromItemStack(new ItemStack(block));
            entity.setChanged();
        }
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

    /**
     * Places a tank the way a player would. Mekanism keeps a machine's default side setup on its
     * item, so a bare {@code setBlock} leaves every side switched off; applying the item's data
     * gives the tank the same sides it has when placed by hand.
     */
    static void placeTank(ServerLevel level, BlockPos pos, Direction facing) {
        Block tank = BuiltInRegistries.BLOCK.get(TANK);
        BlockState state = tank.defaultBlockState();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        }
        level.setBlock(pos, state, Block.UPDATE_ALL);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity != null) {
            entity.applyComponentsFromItemStack(new ItemStack(tank));
            entity.setChanged();
        }
    }

    /** Fills a tank through whichever side takes chemicals in. */
    static void fillTank(ServerLevel level, BlockPos pos, ResourceLocation id, long amount) {
        Holder<Chemical> chemical = level.registryAccess().registryOrThrow(MekanismAPI.CHEMICAL_REGISTRY_NAME)
                .getHolderOrThrow(ResourceKey.create(MekanismAPI.CHEMICAL_REGISTRY_NAME, id));
        for (Direction side : Direction.values()) {
            IChemicalHandler handler = MekanismChemicals.handler(level, pos, side);
            if (handler != null && handler.insertChemical(new ChemicalStack(chemical, amount), Action.EXECUTE).isEmpty()) {
                return;
            }
        }
    }

    /** How much of a chemical a block holds, in mB. */
    static long amountOf(net.minecraft.world.level.Level level, BlockPos pos, ResourceLocation id) {
        IChemicalHandler handler = MekanismChemicals.handler(level, pos, null);
        if (handler == null) {
            return 0;
        }
        long total = 0;
        for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
            ChemicalStack held = handler.getChemicalInTank(tank);
            if (!held.isEmpty() && id.toString().equals(MekanismChemicals.id(held))) {
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
