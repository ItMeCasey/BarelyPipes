package com.itmecasey.barelypipes.dev;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** The dev test tank: one fluid slot of 100 buckets and an energy buffer of 10 million FE, offered on every side. */
public class TestTankBlockEntity extends BlockEntity {
    public static final int FLUID_CAPACITY = 100_000;
    public static final int ENERGY_CAPACITY = 10_000_000;

    private final FluidTank tank = new FluidTank(FLUID_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private final EnergyStorage energy = new EnergyStorage(ENERGY_CAPACITY, Integer.MAX_VALUE, Integer.MAX_VALUE) {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            int accepted = super.receiveEnergy(toReceive, simulate);
            if (accepted > 0 && !simulate) {
                setChanged();
            }
            return accepted;
        }
    };
    private final LazyOptional<FluidTank> tankView = LazyOptional.of(() -> tank);
    private final LazyOptional<EnergyStorage> energyView = LazyOptional.of(() -> energy);

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
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            return tankView.cast();
        }
        if (capability == ForgeCapabilities.ENERGY) {
            return energyView.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        tankView.invalidate();
        energyView.invalidate();
    }
}
