package com.itmecasey.barelypipes.client;

import com.itmecasey.barelypipes.BarelyPipes;
import com.itmecasey.barelypipes.Ids;
import com.itmecasey.barelypipes.client.model.BundleUnbakedModel;
import com.itmecasey.barelypipes.client.screen.PipeConfigScreen;
import com.itmecasey.barelypipes.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only setup: the bundle's model type and the pipe screen. */
@EventBusSubscriber(modid = BarelyPipes.MODID, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {
    }

    /** Teaches the block state loader the {@code barelypipes:conduit_bundle} model type. */
    @SubscribeEvent
    public static void registerBlockStateModels(RegisterBlockStateModels event) {
        event.registerModel(Ids.mod("conduit_bundle"), BundleUnbakedModel.CODEC);
    }

    /** Pairs the pipe menu with its screen. */
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.PIPE_CONFIG.get(), PipeConfigScreen::new);
    }
}
