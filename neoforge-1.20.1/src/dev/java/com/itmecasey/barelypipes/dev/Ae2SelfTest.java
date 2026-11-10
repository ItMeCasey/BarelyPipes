package com.itmecasey.barelypipes.dev;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import com.itmecasey.barelypipes.core.conduit.Face;
import com.itmecasey.barelypipes.core.conduit.PipeType;
import com.itmecasey.barelypipes.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * The ME pipe against real AE2 blocks (only run when AE2 is installed): two ME Interfaces with three bundles holding an ME pipe
 * between them must end up in ONE AE2 grid, and a control pair with a gap must not. Checks the grid through AE2's own API.
 */
final class Ae2SelfTest {
    private static ServerLevel level;
    private static BlockPos a;
    private static BlockPos b;
    private static BlockPos controlA;
    private static BlockPos controlB;
    private static int tick;
    private static boolean finished = true;

    private Ae2SelfTest() {
    }

    static boolean finished() {
        return finished;
    }

    static void setUp(ServerLevel serverLevel) {
        level = serverLevel;
        Block iface = BuiltInRegistries.BLOCK.get(new ResourceLocation("ae2:interface"));
        if (iface == Blocks.AIR) {
            SelfTest.check("ae2: the ME Interface block exists", false);
            return;
        }
        finished = false;
        tick = 0;
        a = new BlockPos(10, 290, 3);
        b = a.east(4);
        controlA = new BlockPos(10, 290, 8);
        controlB = controlA.east(4);
        for (BlockPos p : BlockPos.betweenClosed(a.offset(-1, -1, -1), controlB.offset(1, 1, 1))) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        level.setBlock(a, iface.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(b, iface.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(controlA, iface.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(controlB, iface.defaultBlockState(), Block.UPDATE_ALL);
        for (int i = 1; i <= 3; i++) {
            bundle(a.east(i));
        }
        // Control: the middle bundle is missing, so the two interfaces are not joined.
        bundle(controlA.east(1));
        bundle(controlA.east(3));
    }

    private static void bundle(BlockPos pos) {
        level.setBlock(pos, ModBlocks.CONDUIT_BUNDLE.get().defaultBlockState(), Block.UPDATE_ALL);
        ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) level.getBlockEntity(pos);
        entity.addPipe(PipeType.ME);
        entity.refreshConnections();
    }

    static void tick() {
        if (finished) {
            return;
        }
        ++tick;
        if (tick == 30) {
            // AE2 blocks create their grid node on their first ticks; a pipe placed beside a block that has run for a while
            // (the normal case) sees it at once, so refresh the pipes now as a neighbor update would.
            for (BlockPos p : new BlockPos[] {a.east(1), a.east(2), a.east(3), controlA.east(1), controlA.east(3)}) {
                if (level.getBlockEntity(p) instanceof ConduitBundleBlockEntity entity) {
                    entity.refreshConnections();
                }
            }
        }
        if (tick != 60) {
            return;
        }
        IGridNode nodeA = GridHelper.getExposedNode(level, a, Direction.EAST);
        IGridNode nodeB = GridHelper.getExposedNode(level, b, Direction.WEST);
        SelfTest.check("ae2: an ME pipe attaches to an ME Interface (face connected)",
                level.getBlockEntity(a.east()) instanceof ConduitBundleBlockEntity e && e.bundle().isConnected(PipeType.ME.lane(), Face.WEST)
                        && e.bundle().isExternal(PipeType.ME.lane(), Face.WEST));
        SelfTest.check("ae2: both interfaces expose a grid node", nodeA != null && nodeB != null);
        SelfTest.check("ae2: two interfaces joined by three ME pipe bundles are in one grid",
                nodeA != null && nodeB != null && nodeA.getGrid() == nodeB.getGrid());
        IGridNode cA = GridHelper.getExposedNode(level, controlA, Direction.EAST);
        IGridNode cB = GridHelper.getExposedNode(level, controlB, Direction.WEST);
        SelfTest.check("ae2: with a gap in the pipe line the interfaces are in different grids",
                cA != null && cB != null && cA.getGrid() != cB.getGrid());
        finished = true;
    }
}
