package com.itmecasey.barelypipes.dev;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** A dev-only block that holds fluid and energy and offers both on every side, so pipes have something to move. */
public class TestTankBlock extends BaseEntityBlock {
    public static final MapCodec<TestTankBlock> CODEC = simpleCodec(TestTankBlock::new);

    public TestTankBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TestTankBlockEntity(pos, state);
    }
}
