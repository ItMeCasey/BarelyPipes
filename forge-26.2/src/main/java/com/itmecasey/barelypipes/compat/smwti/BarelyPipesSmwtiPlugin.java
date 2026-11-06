package com.itmecasey.barelypipes.compat.smwti;

import com.itmecasey.smwti.api.ClientRegistration;
import com.itmecasey.smwti.api.ISmwtiPlugin;
import com.itmecasey.smwti.api.SmwtiPlugin;

/**
 * The Barely Pipes lines in Show Me What This Is, our own look-at overlay: the exact pipe under the crosshair (its item name
 * and icon as the title) with its side, mode, neighbor, throughput, upgrades, filter and priority. The text is built by
 * {@code PipeLookInfo} and {@code PipeLookAdapter}; this class only hands it to SMWTI. SMWTI finds it by its annotation;
 * nothing calls into it, and it is never loaded unless SMWTI is installed. The client part lives in its own class so a
 * dedicated server never touches client code.
 */
@SmwtiPlugin
public class BarelyPipesSmwtiPlugin implements ISmwtiPlugin {
    @Override
    public void registerClient(ClientRegistration registration) {
        BarelyPipesSmwtiClient.register(registration);
    }
}
