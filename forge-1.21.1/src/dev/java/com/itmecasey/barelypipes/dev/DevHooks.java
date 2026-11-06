package com.itmecasey.barelypipes.dev;

import com.itmecasey.barelypipes.BarelyPipes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Development hooks: the test tank and the headless self test. Only built with -PdevTools; the main class finds this class by name. */
public final class DevHooks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BarelyPipes.MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BarelyPipes.MODID);
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BarelyPipes.MODID);

    public static final RegistryObject<TestTankBlock> TEST_TANK = BLOCKS.register("test_tank",
            () -> new TestTankBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(1.0F)));
    public static final RegistryObject<BlockEntityType<TestTankBlockEntity>> TEST_TANK_ENTITY =
            ENTITIES.register("test_tank", () -> BlockEntityType.Builder.of(TestTankBlockEntity::new, TEST_TANK.get()).build(null));

    private DevHooks() {
    }

    /** Called by the main class through reflection when the dev system property is set. */
    public static void install(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register("test_tank", () -> new BlockItem(TEST_TANK.get(), new Item.Properties()));
        ITEMS.register(modBus);
        ENTITIES.register(modBus);
        if (SelfTest.requested()) {
            MinecraftForge.EVENT_BUS.addListener((ServerStartedEvent event) -> SelfTest.start(event.getServer()));
            MinecraftForge.EVENT_BUS.addListener(SelfTest::onServerTick);
        }
    }
}
