package com.itmecasey.barelypipes.dev;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Client-only development hooks, kept apart so a dedicated server never loads client classes. */
final class ClientHooks {
    private ClientHooks() {
    }

    static void install() {
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> { ShotTour.tick(); PlaygroundClient.tick(); });
    }
}
