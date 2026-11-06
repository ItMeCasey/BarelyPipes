package com.itmecasey.barelypipes.compat.mekanism;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

/**
 * Everything the rest of BarelyPipes asks of Mekanism, behind a check that Mekanism is installed.
 * Nothing here names a Mekanism class, so this class is safe to load without it; the parts that
 * do live in {@link MekanismGas}, which is only touched when Mekanism is there.
 */
public final class MekanismCompat {
    private static final boolean LOADED = ModList.get().isLoaded("mekanism");

    private MekanismCompat() {
    }

    /** Whether Mekanism is installed. */
    public static boolean loaded() {
        return LOADED;
    }

    /** Whether a gas pipe attaches to the block at {@code pos} on its given side: anything that handles gas there. */
    public static boolean offersGas(Level level, BlockPos pos, Direction side) {
        return LOADED && MekanismGas.handler(level, pos, side) != null;
    }
}
