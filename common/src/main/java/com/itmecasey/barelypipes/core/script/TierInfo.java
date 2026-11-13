package com.itmecasey.barelypipes.core.script;

/** One row of the speed table: what a pipe type moves at a tier. */
public final class TierInfo {
    private final String type;
    private final String tier;
    private final long amountPerTick;
    private final int itemsPerOperation;
    private final int ticksPerOperation;

    TierInfo(String type, String tier, long amountPerTick, int itemsPerOperation, int ticksPerOperation) {
        this.type = type;
        this.tier = tier;
        this.amountPerTick = amountPerTick;
        this.itemsPerOperation = itemsPerOperation;
        this.ticksPerOperation = ticksPerOperation;
    }

    public String type() {
        return type;
    }

    public String tier() {
        return tier;
    }

    /** mB or FE per tick for fluid, gas and energy pipes; 0 for item pipes. */
    public long amountPerTick() {
        return amountPerTick;
    }

    /** Items per operation for item pipes; 0 for the others. */
    public int itemsPerOperation() {
        return itemsPerOperation;
    }

    /** Ticks between two item operations for item pipes; 0 for the others. */
    public int ticksPerOperation() {
        return ticksPerOperation;
    }
}
