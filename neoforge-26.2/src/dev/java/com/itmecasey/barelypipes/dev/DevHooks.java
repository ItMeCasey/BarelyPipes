package com.itmecasey.barelypipes.dev;

import com.itmecasey.barelypipes.BarelyPipes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Development hooks: the test tank and the headless self test. Only built with -PdevTools; the main class finds this class by name. */
public final class DevHooks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BarelyPipes.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BarelyPipes.MODID);
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BarelyPipes.MODID);

    public static final DeferredBlock<TestTankBlock> TEST_TANK = BLOCKS.registerBlock("test_tank", TestTankBlock::new,
            () -> net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(1.0F));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TestTankBlockEntity>> TEST_TANK_ENTITY =
            ENTITIES.register("test_tank", () -> new BlockEntityType<>(TestTankBlockEntity::new, TEST_TANK.get()));

    private DevHooks() {
    }

    /** Called by the main class through reflection when the dev system property is set. */
    public static void install(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.registerSimpleBlockItem(TEST_TANK);
        ITEMS.register(modBus);
        ENTITIES.register(modBus);
        modBus.addListener(DevHooks::capabilities);
        if (SelfTest.requested()) {
            NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> SelfTest.start(event.getServer()));
            NeoForge.EVENT_BUS.addListener(SelfTest::onServerTick);
        }
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, TEST_TANK_ENTITY.get(), (be, side) -> be.tank());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, TEST_TANK_ENTITY.get(), (be, side) -> be.energy());
    }
}
