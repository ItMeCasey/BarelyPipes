package com.itmecasey.barelypipes.dev;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;

/** The dev test tank: one fluid slot of 100 buckets and an energy buffer of 10 million FE. */
public class TestTankBlockEntity extends BlockEntity {
    public static final int FLUID_CAPACITY = 100_000;
    public static final int ENERGY_CAPACITY = 10_000_000;

    private final FluidStacksResourceHandler tank = new FluidStacksResourceHandler(1, FLUID_CAPACITY) {
        @Override
        protected void onContentsChanged(int index, net.neoforged.neoforge.fluids.FluidStack previous) {
            setChanged();
        }
    };
    private final SimpleEnergyHandler energy = new SimpleEnergyHandler(ENERGY_CAPACITY, Integer.MAX_VALUE, Integer.MAX_VALUE) {
        @Override
        protected void onEnergyChanged(int previous) {
            setChanged();
        }
    };

    public TestTankBlockEntity(BlockPos pos, BlockState state) {
        super(DevHooks.TEST_TANK_ENTITY.get(), pos, state);
    }

    public ResourceHandler<FluidResource> tank() {
        return tank;
    }

    public SimpleEnergyHandler energy() {
        return energy;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        tank.serialize(output.child("tank"));
        energy.serialize(output.child("energy"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("tank").ifPresent(tank::deserialize);
        input.child("energy").ifPresent(energy::deserialize);
    }
}
