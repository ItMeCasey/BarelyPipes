package com.itmecasey.barelypipes.client;

import com.itmecasey.barelypipes.BarelyPipes;
import com.itmecasey.barelypipes.client.model.BundleGeometryLoader;
import com.itmecasey.barelypipes.client.screen.PipeConfigScreen;
import com.itmecasey.barelypipes.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only setup: the bundle's model type and the pipe screen. */
@Mod.EventBusSubscriber(modid = BarelyPipes.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {
    }

    /** Teaches the model system the {@code barelypipes:conduit_bundle} model type. */
    @SubscribeEvent
    public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        // Forge prefixes the name with the active mod id, giving barelypipes:conduit_bundle.
        event.register("conduit_bundle", new BundleGeometryLoader());
    }

    /** Pairs the pipe menu with its screen; Forge has no separate event for that. */
    @SubscribeEvent
    public static void registerScreens(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModMenus.PIPE_CONFIG.get(), PipeConfigScreen::new));
    }
}
