package com.itmecasey.barelypipes.registry;

import com.itmecasey.barelypipes.BarelyPipes;
import com.itmecasey.barelypipes.conduit.ConduitBundleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Every block Barely Pipes adds: the one bundle block that carries every kind of pipe. */
public final class ModBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BarelyPipes.MODID);

    /** The block that carries every kind of pipe; see {@link ConduitBundleBlock}. */
    public static final RegistryObject<ConduitBundleBlock> CONDUIT_BUNDLE = BLOCKS.register("conduit_bundle",
            () -> new ConduitBundleBlock(bundleProperties()));

    private ModBlocks() {
    }

    private static BlockBehaviour.Properties bundleProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.0F, 3.0F)
                .sound(SoundType.METAL)
                .noOcclusion()
                .dynamicShape()
                .isViewBlocking((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false);
    }

    /** Hooks this registry into the mod event bus; call once from the mod constructor. */
    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
