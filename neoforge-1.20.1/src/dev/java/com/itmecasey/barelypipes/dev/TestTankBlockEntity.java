package com.itmecasey.barelypipes.dev;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** The dev test tank: one fluid tank of 100 buckets and an energy buffer of 10 million FE, offered on every side. */
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
    private final LazyOptional<FluidTank> fluidHandler = LazyOptional.of(() -> tank);
    private final LazyOptional<EnergyStorage> energyHandler = LazyOptional.of(() -> energy);

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
    public <T> @Nonnull LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandler.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandler.invalidate();
        energyHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("tank", tank.writeToNBT(new CompoundTag()));
        tag.putInt("energy", energy.getEnergyStored());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag.getCompound("tank"));
        energy.deserializeNBT(IntTag.valueOf(tag.getInt("energy")));
    }
}
