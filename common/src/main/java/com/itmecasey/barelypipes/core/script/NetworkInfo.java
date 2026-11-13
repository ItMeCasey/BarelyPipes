package com.itmecasey.barelypipes.core.script;

import java.util.ArrayList;
import java.util.List;

/** A read-only snapshot of one pipe network (one lane of connected bundles), as scripts see it. Never edited; rebuilt by the game. */
public final class NetworkInfo {
    private final String dimension;
    private final String type;
    private final String tier;
    private final ScriptHub.Pos leader;
    private final List<ScriptHub.Pos> bundles;
    private final List<ScriptHub.Endpoint> endpoints;
    private final long amountPerTick;
    private final int ticksPerOperation;
    private final int itemsPerOperation;
    private final boolean truncated;
    private final ScriptHub.Totals totals;

    NetworkInfo(String dimension, String type, String tier, ScriptHub.Pos leader, List<ScriptHub.Pos> bundles,
                List<ScriptHub.Endpoint> endpoints, long amountPerTick, int ticksPerOperation, int itemsPerOperation,
                boolean truncated, ScriptHub.Totals totals) {
        this.dimension = dimension;
        this.type = type;
        this.tier = tier;
        this.leader = leader;
        this.bundles = bundles;
        this.endpoints = endpoints;
        this.amountPerTick = amountPerTick;
        this.ticksPerOperation = ticksPerOperation;
        this.itemsPerOperation = itemsPerOperation;
        this.truncated = truncated;
        this.totals = totals;
    }

    /** The dimension id, such as {@code minecraft:overworld}. */
    public String dimension() {
        return dimension;
    }

    /** The pipe type: item, fluid, energy, gas, redstone, me, dense_me or storage. */
    public String type() {
        return type;
    }

    /** The tier the network moves at, the slowest pipe in it: iron, gold or diamond; empty for pipes without tiers. */
    public String tier() {
        return tier;
    }

    /** The bundle that does the network's work. */
    public ScriptHub.Pos leader() {
        return leader;
    }

    public int bundleCount() {
        return bundles.size();
    }

    /** Every bundle in the network. */
    public List<ScriptHub.Pos> bundles() {
        return bundles;
    }

    public boolean contains(int x, int y, int z) {
        for (ScriptHub.Pos pos : bundles) {
            if (pos.x() == x && pos.y() == y && pos.z() == z) {
                return true;
            }
        }
        return false;
    }

    /** Every machine face the network touches. */
    public List<ScriptHub.Endpoint> endpoints() {
        return endpoints;
    }

    /** The machine faces the network takes from. */
    public List<ScriptHub.Endpoint> sources() {
        List<ScriptHub.Endpoint> out = new ArrayList<>();
        for (ScriptHub.Endpoint endpoint : endpoints) {
            if (endpoint.pulls()) {
                out.add(endpoint);
            }
        }
        return out;
    }

    /** The machine faces the network gives to. */
    public List<ScriptHub.Endpoint> destinations() {
        List<ScriptHub.Endpoint> out = new ArrayList<>();
        for (ScriptHub.Endpoint endpoint : endpoints) {
            if (endpoint.pushes()) {
                out.add(endpoint);
            }
        }
        return out;
    }

    /** Fluid and gas (mB) or energy (FE) the network may move per tick; 0 for the other types. */
    public long amountPerTick() {
        return amountPerTick;
    }

    /** Ticks between two item operations. */
    public int ticksPerOperation() {
        return ticksPerOperation;
    }

    /** Items one source gives in one operation. */
    public int itemsPerOperation() {
        return itemsPerOperation;
    }

    /** True if the search hit the size limit, so the network is only partly known. */
    public boolean truncated() {
        return truncated;
    }

    /** Items, mB or FE the network has moved since it was first seen (counted while scripts are enabled). */
    public long moved() {
        return totals.moved;
    }

    /** The registry id of the last item, fluid or gas it moved; {@code energy} for energy; empty if nothing yet. */
    public String lastResource() {
        return totals.lastResource;
    }

    boolean sameShape(NetworkInfo other) {
        if (!type.equals(other.type) || !tier.equals(other.tier) || amountPerTick != other.amountPerTick
                || ticksPerOperation != other.ticksPerOperation || itemsPerOperation != other.itemsPerOperation
                || truncated != other.truncated || !bundles.equals(other.bundles) || endpoints.size() != other.endpoints.size()) {
            return false;
        }
        for (int i = 0; i < endpoints.size(); i++) {
            if (!endpoints.get(i).sameAs(other.endpoints.get(i))) {
                return false;
            }
        }
        return true;
    }
}
