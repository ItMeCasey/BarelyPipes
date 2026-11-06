package com.itmecasey.barelypipes.dev;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** A dev-only block that holds fluid and energy and offers both on every side, so pipes have something to move. */
public class TestTankBlock extends BaseEntityBlock {
    public TestTankBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TestTankBlockEntity(pos, state);
    }
}
