package com.itmecasey.barelypipes.client;

import com.itmecasey.barelypipes.BarelyPipes;
import com.itmecasey.barelypipes.client.model.BundleGeometryLoader;
import com.itmecasey.barelypipes.client.screen.PipeConfigScreen;
import com.itmecasey.barelypipes.registry.ModMenus;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only setup: the bundle's model type and the pipe screen. */
@EventBusSubscriber(modid = BarelyPipes.MODID, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {
    }

    /** Pairs the pipe menu with its screen. */
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.PIPE_CONFIG.get(), PipeConfigScreen::new);
    }

    /** Teaches the model system the {@code barelypipes:conduit_bundle} model type. */
    @SubscribeEvent
    public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(ResourceLocation.fromNamespaceAndPath(BarelyPipes.MODID, "conduit_bundle"),
                new BundleGeometryLoader());
    }
}
