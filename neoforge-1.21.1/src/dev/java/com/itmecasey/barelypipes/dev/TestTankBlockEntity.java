package com.itmecasey.barelypipes.dev;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/** The dev test tank: one fluid tank of 100 buckets and an energy buffer of 10 million FE. */
public class TestTankBlockEntity extends BlockEntity {
    public static final int FLUID_CAPACITY = 100_000;
    public static final int ENERGY_CAPACITY = 10_000_000;

    private final FluidTank tank = new FluidTank(FLUID_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private final EnergyStorage energy = new EnergyStorage(ENERGY_CAPACITY, Integer.MAX_VALUE, Integer.MAX_VALUE);

    public TestTankBlockEntity(BlockPos pos, BlockState state) {
        super(DevHooks.TEST_TANK_ENTITY.get(), pos, state);
    }

    public FluidTank tank() {
        return tank;
    }

    public EnergyStorage energy() {
        return energy;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag fluid = new CompoundTag();
        tank.writeToNBT(registries, fluid);
        tag.put("tank", fluid);
        tag.putInt("energy", energy.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tank.readFromNBT(registries, tag.getCompound("tank"));
        energy.deserializeNBT(registries, net.minecraft.nbt.IntTag.valueOf(tag.getInt("energy")));
    }
}
