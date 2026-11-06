package com.itmecasey.barelypipes.registry;

import com.itmecasey.barelypipes.BarelyPipes;
import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The block entity types Barely Pipes adds. */
public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BarelyPipes.MODID);

    public static final RegistryObject<BlockEntityType<ConduitBundleBlockEntity>> CONDUIT_BUNDLE =
            BLOCK_ENTITIES.register("conduit_bundle", () -> new BlockEntityType<>(ConduitBundleBlockEntity::new,
                    Set.<Block>of(ModBlocks.CONDUIT_BUNDLE.get())));

    private ModBlockEntities() {
    }

    /** Hooks this registry into the mod event bus group; call once from the mod constructor. */
    public static void register(BusGroup modBusGroup) {
        BLOCK_ENTITIES.register(modBusGroup);
    }
}
