package com.itmecasey.barelypipes.compat.ae2;

import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Where BarelyPipes would talk to Applied Energistics 2. AE2 has no Forge build for 26.x, so on
 * this loader every call does nothing: ME pipes join only each other. The rest of the mod calls
 * these the same way on every loader, which keeps the shared code alike.
 */
public final class Ae2Compat {
    private Ae2Compat() {
    }

    /** Never: there is no AE2 to find. */
    public static boolean loaded() {
        return false;
    }

    @Nullable
    public static Object sync(ConduitBundleBlockEntity bundle, @Nullable Object link) {
        return null;
    }

    /** Takes a bundle's link out of AE2's grid, for when the block is broken or unloaded. */
    public static void stop(@Nullable Object link) {
    }

    /** An ME pipe only joins AE2's blocks, and there are none. */
    public static boolean attaches(Level level, BlockPos pos, Direction side) {
        return false;
    }
}
