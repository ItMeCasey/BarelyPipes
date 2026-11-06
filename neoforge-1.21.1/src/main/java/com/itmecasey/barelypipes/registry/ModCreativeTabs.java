package com.itmecasey.barelypipes.registry;

import com.itmecasey.barelypipes.BarelyPipes;
import com.itmecasey.barelypipes.core.Tier;
import com.itmecasey.barelypipes.core.conduit.PipeTiers;
import com.itmecasey.barelypipes.core.conduit.PipeType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The creative tab Barely Pipes adds. */
public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BarelyPipes.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + BarelyPipes.MODID))
                    .icon(() -> ModItems.pipe(PipeType.ITEM, Tier.IRON).get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        for (PipeType type : PipeType.values()) {
                            if (PipeTiers.isTiered(type)) {
                                for (Tier tier : Tier.values()) {
                                    output.accept(ModItems.pipe(type, tier).get());
                                }
                            } else {
                                output.accept(ModItems.pipe(type, Tier.IRON).get());
                            }
                        }
                        output.accept(ModItems.FLUX_WRENCH.get());
                        output.accept(ModItems.CONDUIT_TOOL.get());
                        output.accept(ModItems.SPEED_UPGRADE.get());
                        output.accept(ModItems.CAPACITY_UPGRADE.get());
                        output.accept(ModItems.FILTER_UPGRADE.get());
                        output.accept(ModItems.PRIORITY_UPGRADE.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }

    /** Hooks this registry into the mod event bus; call once from the mod constructor. */
    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
