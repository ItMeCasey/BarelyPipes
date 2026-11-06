package com.itmecasey.barelypipes.compat.mekanism;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

/**
 * The part of the Mekanism support that names Mekanism classes. Only code that has checked
 * {@link MekanismCompat#loaded()} reaches it.
 *
 * <p>Mekanism hands out its chemical handlers through capabilities named
 * {@code mekanism:chemical_handler}. NeoForge gives back the same capability for the same name,
 * so asking for it by name finds Mekanism's own without reaching into Mekanism's internals.
 */
public final class MekanismChemicals {
    private static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath("mekanism", "chemical_handler");

    /** Chemical handlers of blocks, by side. */
    public static final BlockCapability<IChemicalHandler, @Nullable Direction> BLOCK = BlockCapability.createSided(NAME, IChemicalHandler.class);

    private MekanismChemicals() {
    }

    /** The chemical handler of the block at {@code pos} on {@code side}, or {@code null}. */
    public static @Nullable IChemicalHandler handler(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(BLOCK, pos, side);
    }

    /** The registry id of a chemical, the form endpoint filters use. */
    public static String id(ChemicalStack stack) {
        return stack.getChemicalHolder().getRegisteredName();
    }
}
