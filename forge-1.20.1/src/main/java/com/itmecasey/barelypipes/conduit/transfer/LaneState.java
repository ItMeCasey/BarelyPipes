package com.itmecasey.barelypipes.conduit.transfer;

import com.itmecasey.barelypipes.core.script.ScriptHub;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * The small amount of memory a lane's leader keeps between ticks: a rotation counter for
 * round-robin sharing, the item cooldown, a source of randomness, and how much of this tick's
 * throughput is already spent. It lives apart from {@link LaneNetwork} so rebuilding the network
 * does not reset it.
 */
public final class LaneState {
    private final Random random;
    private int rotation;
    private int cooldown;
    /** The game tick {@link #spent} counts for. */
    private long budgetTick = Long.MIN_VALUE;
    private long spent;

    /** What was moved since the last {@link #drainMoved}, by resource id; only filled while scripts are enabled. */
    private Map<String, Long> movedPending;

    /** Notes that {@code amount} of the resource {@code id} moved in this step, for the script events. */
    void moved(String id, long amount) {
        if (amount > 0 && ScriptHub.tracking()) {
            if (movedPending == null) {
                movedPending = new LinkedHashMap<>();
            }
            movedPending.merge(id, amount, Long::sum);
        }
    }

    /** Hands over what was moved since the last call, one entry per resource, and forgets it. */
    void drainMoved(BiConsumer<String, Long> sink) {
        if (movedPending != null && !movedPending.isEmpty()) {
            Map<String, Long> now = movedPending;
            movedPending = null;
            now.forEach(sink);
        }
    }

    LaneState(long seed) {
        this.random = new Random(seed);
    }

    /** A counter that grows by one each time it is asked; feeds round-robin choices. */
    int nextRotation() {
        return rotation++;
    }

    /** The current rotation without advancing it. */
    int rotation() {
        return rotation;
    }

    Random random() {
        return random;
    }

    /**
     * How much of {@code limit} is still free in game tick {@code gameTime}. The network's own
     * step and other mods pushing into its faces (see {@link PipeFluidInlet}) share the one limit,
     * so a pipe never carries more per tick than its tier allows, whoever does the pushing.
     */
    long remaining(long gameTime, long limit) {
        startTick(gameTime);
        return Math.max(0, limit - spent);
    }

    /** Counts {@code amount} against the budget of tick {@code gameTime}. */
    void spend(long gameTime, long amount) {
        startTick(gameTime);
        spent += Math.max(0, amount);
    }

    /** A new tick starts with nothing spent. */
    private void startTick(long gameTime) {
        if (gameTime != budgetTick) {
            budgetTick = gameTime;
            spent = 0;
        }
    }

    /** Counts one tick of the item cooldown; true when an operation is due, and then restarts the wait. */
    boolean cooldownElapsed(int ticksBetweenOperations) {
        if (--cooldown > 0) {
            return false;
        }
        cooldown = ticksBetweenOperations;
        return true;
    }
}
