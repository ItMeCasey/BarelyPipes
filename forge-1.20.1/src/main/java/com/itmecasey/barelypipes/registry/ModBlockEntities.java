package com.itmecasey.barelypipes.registry;

import com.itmecasey.barelypipes.BarelyPipes;
import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** The block entity types Barely Pipes adds. */
public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BarelyPipes.MODID);

    public static final RegistryObject<BlockEntityType<ConduitBundleBlockEntity>> CONDUIT_BUNDLE =
            BLOCK_ENTITIES.register("conduit_bundle", () -> BlockEntityType.Builder.of(
                    ConduitBundleBlockEntity::new, ModBlocks.CONDUIT_BUNDLE.get()).build(null));

    private ModBlockEntities() {
    }

    /** Hooks this registry into the mod event bus; call once from the mod constructor. */
    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
