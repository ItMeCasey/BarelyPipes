package com.itmecasey.barelypipes.registry;

import com.itmecasey.barelypipes.BarelyPipes;
import com.itmecasey.barelypipes.compat.ae2.Ae2Compat;
import com.itmecasey.barelypipes.conduit.transfer.PipeFluidInlet;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Exposes bundles to other mods' pipes and pumps through NeoForge capabilities. */
@EventBusSubscriber(modid = BarelyPipes.MODID)
public final class ModCapabilities {
    private ModCapabilities() {
    }

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        // A fluid pipe's Open and Import faces take fluid from other mods' pipes and pumps.
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.CONDUIT_BUNDLE.get(),
                PipeFluidInlet::forSide);

        // With AE2 installed, a bundle's ME pipe is a grid node AE2 can join.
        Ae2Compat.registerCapabilities(event);
    }
}
