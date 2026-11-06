package com.itmecasey.barelypipes.registry;

import com.itmecasey.barelypipes.BarelyPipes;
import com.itmecasey.barelypipes.conduit.ConduitBundleBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Every block Barely Pipes adds: the one bundle block that carries every kind of pipe. */
public final class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BarelyPipes.MODID);

    /** The block that carries every kind of pipe; see {@link ConduitBundleBlock}. */
    public static final DeferredBlock<ConduitBundleBlock> CONDUIT_BUNDLE = BLOCKS.registerBlock("conduit_bundle",
            ConduitBundleBlock::new, ModBlocks::bundleProperties);

    private ModBlocks() {
    }

    private static BlockBehaviour.Properties bundleProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.0F, 3.0F)
                .sound(SoundType.METAL)
                .noOcclusion()
                .dynamicShape()
                .isViewBlocking((state, level, pos, box) -> false)
                .isSuffocating((state, level, pos) -> false);
    }

    /** Hooks this registry into the mod event bus; call once from the mod constructor. */
    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
