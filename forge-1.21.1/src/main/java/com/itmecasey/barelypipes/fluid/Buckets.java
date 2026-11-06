package com.itmecasey.barelypipes.fluid;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

/**
 * What a held bucket contains. Forge has item fluid handlers switched off here (its
 * {@code FluidUtil.getFluidHandler(ItemStack)} always answers empty, and buckets carry no fluid
 * capability), so a filter entry set from a bucket has to read the bucket itself.
 */
public final class Buckets {
    private Buckets() {
    }

    /** The fluid in a filled bucket, a bucket's worth of it, or empty for anything else. */
    public static FluidStack contained(ItemStack stack) {
        if (stack.getItem() instanceof BucketItem bucket && bucket.getFluid() != Fluids.EMPTY) {
            return new FluidStack(bucket.getFluid(), FluidType.BUCKET_VOLUME);
        }
        return FluidStack.EMPTY;
    }
}
