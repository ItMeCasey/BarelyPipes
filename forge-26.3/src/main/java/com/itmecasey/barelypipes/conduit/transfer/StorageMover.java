package com.itmecasey.barelypipes.conduit.transfer;

import com.itmecasey.barelypipes.core.conduit.PriorityPlanner;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * Moves items for the storage pipe. It works like {@link ItemMover}, with one difference: a face
 * can end at a BarelyStorage network block as well as at an ordinary inventory. A network face
 * gives any item the network holds (the filter picks which) and takes whatever the racks have room
 * for; an inventory face behaves as it does for the item pipe.
 *
 * <p>Both kinds of face are handled through {@link Port}, so one loop serves every combination:
 * inventory to network, network to inventory, and network to network through two different faces.
 */
final class StorageMover implements ResourceMover {
    /** One face of the lane as a place items can leave from or arrive at. */
    interface Port {
        NetworkEndpoint endpoint();

        /** The stack this port would hand out next (at most {@code batch} items, filter applied), or empty. Changes nothing. */
        ItemStack peek(int batch);

        /** Takes {@code wanted} out for real; returns what actually came out. */
        ItemStack take(ItemStack wanted);

        /** How many of the stack this port accepts; with {@code simulate} nothing changes. */
        int accept(ItemStack stack, boolean simulate);
    }

    @Override
    public void run(ServerLevel level, LaneNetwork network, LaneState state) {
        if (!state.cooldownElapsed(network.ticksPerOperation())) {
            return;
        }
        List<Port> sources = new ArrayList<>();
        List<Port> sinks = new ArrayList<>();
        for (NetworkEndpoint endpoint : network.endpoints()) {
            if (!level.isLoaded(endpoint.target())) {
                continue;
            }
            Port port = portFor(level, endpoint);
            if (port == null) {
                continue;
            }
            if (endpoint.pulls()) {
                sources.add(port);
            }
            if (endpoint.pushes()) {
                sinks.add(port);
            }
        }
        if (sources.isEmpty() || sinks.isEmpty()) {
            return;
        }
        int start = Math.floorMod(state.nextRotation(), sources.size());
        for (int step = 0; step < sources.size(); step++) {
            Port source = sources.get((start + step) % sources.size());
            try {
                moveBatch(level, source, sinks, network.itemsPerOperation(), state);
            } catch (RuntimeException e) {
                TransferFaults.report(source.endpoint().target(), e);
            }
        }
    }

    private static Port portFor(ServerLevel level, NetworkEndpoint endpoint) {
        if (StorageLink.isNode(level, endpoint.target(), endpoint.machineSide())) {
            return StorageLink.port(level, endpoint);
        }
        IItemHandler handler = Neighbors.find(level, endpoint.target(), endpoint.machineSide(), ForgeCapabilities.ITEM_HANDLER);
        return handler == null ? null : new HandlerPort(endpoint, handler);
    }

    private static void moveBatch(ServerLevel level, Port source, List<Port> sinks, int batchSize, LaneState state) {
        ItemStack offer = source.peek(batchSize);
        if (offer.isEmpty()) {
            return;
        }
        String id = BuiltInRegistries.ITEM.getKey(offer.getItem()).toString();
        int[] priorities = new int[sinks.size()];
        for (int j = 0; j < priorities.length; j++) {
            priorities[j] = sinks.get(j).endpoint().priority();
        }
        int[] order = PriorityPlanner.byPriority(
                source.endpoint().distribution().order(sinks.size(), state.rotation(), state.random()), priorities);
        boolean anyone = false;
        for (int index : order) {
            Port sink = sinks.get(index);
            if (!same(source, sink) && sink.endpoint().allows(id) && sink.accept(offer, true) > 0) {
                anyone = true;
                break;
            }
        }
        if (!anyone) {
            return;
        }
        ItemStack taken = source.take(offer);
        int remaining = taken.getCount();
        for (int index : order) {
            if (remaining <= 0) {
                break;
            }
            Port sink = sinks.get(index);
            if (same(source, sink) || !sink.endpoint().allows(id)) {
                continue;
            }
            try {
                ItemStack part = taken.copyWithCount(remaining);
                if (sink.accept(part, true) > 0) {
                    remaining -= sink.accept(part, false);
                }
            } catch (RuntimeException e) {
                TransferFaults.report(sink.endpoint().target(), e);
            }
        }
        if (remaining > 0) {
            // Nobody took the rest after all: give it back, and drop it as a last resort so nothing vanishes.
            ItemStack rest = taken.copyWithCount(remaining);
            remaining -= source.accept(rest, false);
            if (remaining > 0) {
                Block.popResource(level, source.endpoint().target(), taken.copyWithCount(remaining));
            }
        }
    }

    private static boolean same(Port a, Port b) {
        return a.endpoint().target().equals(b.endpoint().target());
    }

    /** An ordinary inventory face. */
    private record HandlerPort(NetworkEndpoint endpoint, IItemHandler handler) implements Port {
        @Override
        public ItemStack peek(int batch) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack offered = handler.extractItem(slot, batch, true);
                if (!offered.isEmpty() && endpoint.allows(BuiltInRegistries.ITEM.getKey(offered.getItem()).toString())) {
                    return offered;
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack take(ItemStack wanted) {
            ItemStack taken = ItemStack.EMPTY;
            for (int slot = 0; slot < handler.getSlots() && taken.getCount() < wanted.getCount(); slot++) {
                ItemStack inSlot = handler.getStackInSlot(slot);
                if (inSlot.isEmpty() || !ItemStack.isSameItemSameComponents(inSlot, wanted)) {
                    continue;
                }
                ItemStack part = handler.extractItem(slot, wanted.getCount() - taken.getCount(), false);
                if (part.isEmpty()) {
                    continue;
                }
                if (taken.isEmpty()) {
                    taken = part;
                } else {
                    taken.grow(part.getCount());
                }
            }
            return taken;
        }

        @Override
        public int accept(ItemStack stack, boolean simulate) {
            return stack.getCount() - ItemHandlerHelper.insertItem(handler, stack, simulate).getCount();
        }
    }
}
