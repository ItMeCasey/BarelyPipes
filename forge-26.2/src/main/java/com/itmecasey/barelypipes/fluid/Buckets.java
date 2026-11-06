package com.itmecasey.barelypipes.fluid;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

/**
 * Forge 26.1 has item fluid handlers switched off for now (its {@code FluidUtil.getFluidHandler(ItemStack)} always
 * answers empty), so the fluid in a held item is read from plain buckets, which is what a player carries fluid in.
 */
public final class Buckets {
    private Buckets() {
    }

    /** The fluid in a filled bucket, a bucket's worth of it, or empty for anything else. */
    public static FluidStack contained(ItemStack stack) {
        if (stack.getItem() instanceof BucketItem bucket && bucket.getContent() != Fluids.EMPTY) {
            return new FluidStack(bucket.getContent(), FluidType.BUCKET_VOLUME);
        }
        return FluidStack.EMPTY;
    }
}
