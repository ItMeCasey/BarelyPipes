package com.itmecasey.barelypipes.core.script;

/** What a {@code transfer} listener receives: one network moved something this tick. */
public final class TransferEvent {
    private final String dimension;
    private final String type;
    private final ScriptHub.Pos leader;
    private final String resource;
    private final long amount;

    TransferEvent(String dimension, String type, ScriptHub.Pos leader, String resource, long amount) {
        this.dimension = dimension;
        this.type = type;
        this.leader = leader;
        this.resource = resource;
        this.amount = amount;
    }

    public String dimension() {
        return dimension;
    }

    /** The pipe type that moved it: item, fluid, energy, gas or storage. */
    public String type() {
        return type;
    }

    /** The bundle that leads the network (the one that did the work). */
    public ScriptHub.Pos leader() {
        return leader;
    }

    /** The registry id of the item, fluid or gas; {@code energy} for energy. */
    public String resource() {
        return resource;
    }

    /** How many items, mB or FE moved in this tick. */
    public long amount() {
        return amount;
    }

    /** The network that moved it, or null if it was rebuilt meanwhile. */
    public NetworkInfo network() {
        for (NetworkInfo info : ScriptHub.networks()) {
            if (info.dimension().equals(dimension) && info.type().equals(type) && info.leader().equals(leader)) {
                return info;
            }
        }
        return null;
    }
}
