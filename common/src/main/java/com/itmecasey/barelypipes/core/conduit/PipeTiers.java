package com.itmecasey.barelypipes.core.conduit;

import com.itmecasey.barelypipes.core.Tier;

/**
 * How much each kind of pipe can move at each tier. These are the built-in numbers from the
 * plan, meant to be rebalanced later, so nothing else should hard-code them.
 *
 * <p>Fluid, energy and gas pipes move an amount per tick. Item pipes move a batch of items every
 * so many ticks. Redstone pipes carry a number of independent channels. ME pipes have no tiers.
 */
public final class PipeTiers {
    /** Fluid per tick in mB, iron, gold, diamond. */
    private static final int[] FLUID = {250, 650, 1_800};
    /** Energy per tick in FE, iron, gold, diamond. */
    private static final int[] ENERGY = {4_000, 16_000, 64_000};
    /** Gas per tick in mB, iron, gold, diamond. */
    private static final int[] GAS = {800, 2_400, 7_000};
    /** Items moved in one operation, iron, gold, diamond. */
    private static final int[] ITEMS_PER_OPERATION = {4, 8, 16};
    /** Ticks between two item operations, iron, gold, diamond. */
    private static final int[] TICKS_PER_OPERATION = {16, 12, 8};
    /** Redstone channels a pipe carries, iron, gold, diamond. */
    /** Redstone pipes have no tiers and carry one channel (the other channels were never wired up). */
    private static final int REDSTONE_CHANNELS = 1;
    /** The storage pipe has one fixed speed: it moves items at the gold item pipe's rate. */
    public static final Tier STORAGE_TIER = Tier.GOLD;

    private PipeTiers() {
    }

    /** Fluid a fluid pipe moves per tick, in mB. */
    public static int fluidPerTick(Tier tier) {
        return FLUID[tier.ordinal()];
    }

    /** Energy an energy pipe moves per tick, in FE. */
    public static int energyPerTick(Tier tier) {
        return ENERGY[tier.ordinal()];
    }

    /** Gas a gas pipe moves per tick, in mB. */
    public static int gasPerTick(Tier tier) {
        return GAS[tier.ordinal()];
    }

    /** Items an item pipe moves in one operation. */
    public static int itemsPerOperation(Tier tier) {
        return ITEMS_PER_OPERATION[tier.ordinal()];
    }

    /** Ticks an item pipe waits between operations. */
    public static int ticksPerOperation(Tier tier) {
        return TICKS_PER_OPERATION[tier.ordinal()];
    }

    /** Redstone channels a redstone pipe carries. */
    public static int redstoneChannels(Tier tier) {
        return REDSTONE_CHANNELS;
    }

    /** Whether pipes of this type come in three tiers: only fluid, item, energy and gas pipes do. */
    public static boolean isTiered(PipeType type) {
        return type == PipeType.FLUID || type == PipeType.ITEM || type == PipeType.ENERGY || type == PipeType.GAS;
    }

    /**
     * The amount a network of this type may move per tick, for the types that move an amount
     * (fluid, energy and gas). A network moves at the speed of its slowest pipe.
     *
     * @return the limit per tick, or 0 for types that do not move a plain amount
     */
    public static long amountPerTick(PipeType type, Tier tier) {
        return switch (type) {
            case FLUID -> fluidPerTick(tier);
            case ENERGY -> energyPerTick(tier);
            case GAS -> gasPerTick(tier);
            default -> 0L;
        };
    }
}
