package com.itmecasey.barelypipes.conduit.transfer;

import net.minecraft.server.level.ServerLevel;

/**
 * Moves items for the storage pipe. BarelyStorage does not exist for this Minecraft version, so the
 * storage pipe works between ordinary inventories exactly like the item pipe; the lane's rate (the
 * storage tier) is already set by the network, so this only hands the work to an {@link ItemMover}.
 */
final class StorageMover implements ResourceMover {
    private final ItemMover items = new ItemMover();

    @Override
    public void run(ServerLevel level, LaneNetwork network, LaneState state) {
        items.run(level, network, state);
    }
}
