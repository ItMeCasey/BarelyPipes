package com.itmecasey.barelypipes.core.script;

import com.itmecasey.barelypipes.core.Tier;
import com.itmecasey.barelypipes.core.conduit.PipeTiers;
import com.itmecasey.barelypipes.core.conduit.PipeType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * The one scripting surface of Barely Pipes: ModJS exposes an instance of this class to server scripts as {@code BarelyPipes}. All
 * methods take and return plain strings, numbers, lists and the small read-only objects of this package, so no script can change
 * a pipe through it; it only watches.
 *
 * <p>In scripts: {@code BarelyPipes.onTransfer(e => console.log(e.type() + ' ' + e.amount() + ' ' + e.resource()))}.
 */
public final class PipeScripts {
    /** The single API object shared by every bridge. */
    public static final PipeScripts API = new PipeScripts();

    private PipeScripts() {
    }

    // ---- events ----

    /** Calls {@code listener} when a pipe network moves items, fluid, energy or gas: once per network, resource and tick. */
    public void onTransfer(Consumer<TransferEvent> listener) {
        ScriptHub.addTransferListener(listener);
    }

    /** Calls {@code listener} when a network is created, changed or removed (see {@link NetworkEvent#kind()}). */
    public void onNetworkChanged(Consumer<NetworkEvent> listener) {
        ScriptHub.addNetworkListener(listener);
    }

    /** Drops every listener a script registered; call it at the top of a script that is reloaded. */
    public void clearListeners() {
        ScriptHub.clearListeners();
    }

    // ---- reading ----

    /** The networks that are being ticked now, in every dimension. */
    public List<NetworkInfo> networks() {
        ScriptHub.enable();
        return ScriptHub.networks();
    }

    /** The networks of one pipe type: item, fluid, energy, gas, redstone, me, dense_me or storage. */
    public List<NetworkInfo> networks(String type) {
        List<NetworkInfo> out = new ArrayList<>();
        for (NetworkInfo info : networks()) {
            if (info.type().equals(type)) {
                out.add(info);
            }
        }
        return out;
    }

    /** The network of the given type that contains the bundle at this position, or null. */
    public NetworkInfo networkAt(String dimension, int x, int y, int z, String type) {
        for (NetworkInfo info : networks()) {
            if (info.dimension().equals(dimension) && info.type().equals(type) && info.contains(x, y, z)) {
                return info;
            }
        }
        return null;
    }

    /** The ids of the pipe types. */
    public List<String> types() {
        List<String> out = new ArrayList<>();
        for (PipeType type : PipeType.values()) {
            out.add(type.id());
        }
        return out;
    }

    /** The speed table: what each pipe type moves at each tier (item, fluid, energy and gas pipes have three tiers). */
    public List<TierInfo> tiers() {
        List<TierInfo> out = new ArrayList<>();
        for (PipeType type : new PipeType[] {PipeType.ITEM, PipeType.FLUID, PipeType.ENERGY, PipeType.GAS}) {
            for (Tier tier : Tier.values()) {
                boolean items = type == PipeType.ITEM;
                out.add(new TierInfo(type.id(), tier.name().toLowerCase(Locale.ROOT), items ? 0 : PipeTiers.amountPerTick(type, tier),
                        items ? PipeTiers.itemsPerOperation(tier) : 0, items ? PipeTiers.ticksPerOperation(tier) : 0));
            }
        }
        return out;
    }

    /** The speed of one pipe type at one tier, or null for a type or tier that does not exist. */
    public TierInfo tier(String type, String tier) {
        for (TierInfo info : tiers()) {
            if (info.type().equals(type) && info.tier().equals(tier)) {
                return info;
            }
        }
        return null;
    }
}
