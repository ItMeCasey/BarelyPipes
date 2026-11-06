package com.itmecasey.barelypipes.client;

import com.itmecasey.barelypipes.BarelyPipes;
import com.itmecasey.barelypipes.Ids;
import com.itmecasey.barelypipes.client.model.BundleBlockStateModel;
import com.itmecasey.barelypipes.client.model.BundleGeometryLoader;
import com.itmecasey.barelypipes.client.screen.PipeConfigScreen;
import com.itmecasey.barelypipes.registry.ModBlocks;
import com.itmecasey.barelypipes.registry.ModMenus;
import java.util.Map;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only setup: the pipe screen and the bundle's model, which is built in code. */
@Mod.EventBusSubscriber(modid = BarelyPipes.MODID, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {
    }

    /** Pairs the pipe menu with its screen. */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModMenus.PIPE_CONFIG.get(), PipeConfigScreen::new));
    }

    /** Teaches the model system the {@code barelypipes:conduit_bundle} model type. */
    @SubscribeEvent
    public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(Ids.mod("conduit_bundle"), new BundleGeometryLoader());
    }

    /**
     * Replaces the baked bundle model with one that draws from the block entity's data. The
     * model file can only describe a fixed set of quads, so it serves as the source of the
     * textures and the real model is swapped in once everything is baked.
     */
    @SubscribeEvent
    public static void swapInBundleModel(ModelEvent.ModifyBakingResult event) {
        Map<BlockState, BlockStateModel> models = event.getResults().blockStateModels();
        for (BlockState state : ModBlocks.CONDUIT_BUNDLE.get().getStateDefinition().getPossibleStates()) {
            BlockStateModel baked = models.get(state);
            if (baked == null) {
                continue;
            }
            BundleBlockStateModel dynamic = BundleBlockStateModel.from(baked);
            if (dynamic == null) {
                BarelyPipes.LOGGER.error("The conduit bundle model for {} was not built by the bundle loader; "
                        + "check models/block/conduit_bundle.json", state);
            } else {
                models.put(state, dynamic);
            }
        }
    }
}
