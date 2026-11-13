package com.itmecasey.barelypipes.conduit.transfer;

import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import com.itmecasey.barelypipes.core.conduit.PipeType;
import com.itmecasey.barelypipes.core.script.ScriptHub;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Entry point of the transfer system. Every bundle calls {@link #tick} once per server tick; for
 * each pipe it holds, the bundle first asks who leads that lane's network. Only the leader (the
 * member with the lowest position, see {@link NetworkScanner#LEADER_ORDER}) does the work, so a
 * network moves things once per tick however many bundles it has.
 */
public final class TransferEngine {
    private static final ResourceMover FLUID = new FluidMover();
    private static final ResourceMover ITEM = new ItemMover();
    private static final ResourceMover STORAGE = new StorageMover();
    private static final ResourceMover REDSTONE = new RedstoneMover();
    private static final ResourceMover ENERGY = new EnergyMover();
    private static final ResourceMover GAS = new GasMover();

    private TransferEngine() {
    }

    /** Runs this bundle's share of the transfer work for one tick. Server side only. */
    public static void tick(ServerLevel level, ConduitBundleBlockEntity entity) {
        BlockPos pos = entity.getBlockPos();
        for (PipeType type : entity.bundle().set().types()) {
            ResourceMover mover = moverFor(type);
            if (mover == null) {
                continue;
            }
            try {
                LaneNetwork network = TransferNetworks.networkFor(level, pos, type);
                if (network != null && network.leader().equals(pos)) {
                    LaneState state = TransferNetworks.stateFor(level, pos, type.lane());
                    mover.run(level, network, state);
                    state.drainMoved((id, amount) -> ScriptHub.transferred(TransferNetworks.dimensionOf(level), type,
                            new ScriptHub.Pos(pos.getX(), pos.getY(), pos.getZ()), id, amount));
                }
            } catch (RuntimeException e) {
                // One broken neighbor must not stop the server; the next tick tries again.
                TransferFaults.report(pos, e);
            }
        }
    }

    /** Whether a redstone lane is being sampled right now; used by the block to hide its own output. */
    public static boolean isSamplingRedstone() {
        return RedstoneMover.isSampling();
    }

    /** The mover for a pipe type; ME pipes have none because AE2 runs its own network. */
    private static ResourceMover moverFor(PipeType type) {
        return switch (type) {
            case FLUID -> FLUID;
            case ITEM -> ITEM;
            case STORAGE -> STORAGE;
            case REDSTONE -> REDSTONE;
            case ENERGY -> ENERGY;
            case GAS -> GAS;
            case ME, DENSE_ME -> null;
        };
    }
}
