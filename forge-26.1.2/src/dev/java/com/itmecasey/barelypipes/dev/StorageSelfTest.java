package com.itmecasey.barelypipes.dev;

import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import com.itmecasey.barelypipes.conduit.transfer.StorageLink;
import com.itmecasey.barelypipes.core.conduit.ConnectionMode;
import com.itmecasey.barelypipes.core.conduit.Face;
import com.itmecasey.barelypipes.core.conduit.PipeType;
import com.itmecasey.barelypipes.registry.ModBlocks;
import com.itmecasey.barelystorage.ItemKey;
import com.itmecasey.barelystorage.Keys;
import com.itmecasey.barelystorage.ModContent;
import com.itmecasey.barelystorage.StorageNetwork;
import com.itmecasey.barelystorage.block.RackBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * The storage pipe against a real BarelyStorage network: a powered rack with a cartridge, a bus cable next to it, a
 * bundle with a storage pipe next to the cable, and a chest on the pipe's far side. Items must go chest to network, and
 * then network to chest when the faces are swapped. Runs only when BarelyStorage is installed.
 */
final class StorageSelfTest {
    private static final int PHASE_TICKS = 80;
    private static ServerLevel level;
    private static BlockPos rack;
    private static BlockPos bundle;
    private static BlockPos chest;
    private static int tick;
    private static boolean finished = true;

    private StorageSelfTest() {
    }

    static boolean finished() {
        return finished;
    }

    static void setUp(ServerLevel serverLevel) {
        if (!StorageLink.loaded()) {
            SelfTest.check("storage: BarelyStorage is present in the dev run", false);
            return;
        }
        level = serverLevel;
        finished = false;
        tick = 0;
        rack = new BlockPos(8, 300, 12);
        BlockPos cable = rack.east();
        bundle = rack.east(2);
        chest = rack.east(3);
        for (BlockPos p : BlockPos.betweenClosed(rack.offset(-1, -1, -1), rack.offset(4, 1, 1))) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        level.setBlock(rack, ModContent.blocks().get("server_rack").defaultBlockState(), Block.UPDATE_ALL);
        RackBlockEntity rackEntity = (RackBlockEntity) level.getBlockEntity(rack);
        rackEntity.drives.setItem(0, new ItemStack(ModContent.item("cartridge_iron")));
        rackEntity.setEnergy(rackEntity.maxEnergy());
        level.setBlock(cable, ModContent.blocks().get("rack_bus_cable").defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(bundle, ModBlocks.CONDUIT_BUNDLE.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(chest, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) level.getBlockEntity(bundle);
        entity.addPipe(PipeType.STORAGE);
        entity.refreshConnections();
        int lane = PipeType.STORAGE.lane();
        SelfTest.check("storage: the storage pipe attaches to a bus cable", entity.bundle().isConnected(lane, Face.WEST)
                && entity.bundle().isExternal(lane, Face.WEST));
        SelfTest.check("storage: the storage pipe attaches to a chest", entity.bundle().isConnected(lane, Face.EAST));
        // Phase 1: the chest gives, the network takes.
        entity.bundle().setMode(lane, Face.EAST, ConnectionMode.IMPORT);
        entity.bundle().setMode(lane, Face.WEST, ConnectionMode.EXPORT);
        entity.changed();
        ((Container) level.getBlockEntity(chest)).setItem(0, new ItemStack(Items.COBBLESTONE, 64));
    }

    static void tick() {
        if (finished) {
            return;
        }
        tick++;
        if (tick == PHASE_TICKS) {
            StorageNetwork network = StorageNetwork.find(level, rack);
            ItemKey cobble = Keys.of(new ItemStack(Items.COBBLESTONE), level.registryAccess());
            long stored = network.pool().count(cobble);
            SelfTest.check("storage: the network is online", network.online());
            SelfTest.check("storage: items from a chest go into the network through the pipe", stored > 0);
            SelfTest.check("storage: the chest shrank by what the network took",
                    ((Container) level.getBlockEntity(chest)).getItem(0).getCount() == 64 - stored);
            // Phase 2: swap the faces, the network gives and the chest takes.
            ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) level.getBlockEntity(bundle);
            int lane = PipeType.STORAGE.lane();
            entity.bundle().setMode(lane, Face.EAST, ConnectionMode.EXPORT);
            entity.bundle().setMode(lane, Face.WEST, ConnectionMode.IMPORT);
            entity.changed();
            ((Container) level.getBlockEntity(chest)).setItem(0, ItemStack.EMPTY);
        }
        if (tick == 2 * PHASE_TICKS) {
            Container inventory = (Container) level.getBlockEntity(chest);
            int in = 0;
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                in += inventory.getItem(slot).getCount();
            }
            SelfTest.check("storage: items from the network come out into a chest through the pipe", in > 0);
            finished = true;
        }
    }
}
