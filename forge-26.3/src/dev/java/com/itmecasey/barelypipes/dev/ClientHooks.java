package com.itmecasey.barelypipes.dev;

import net.minecraftforge.event.TickEvent;

/** Client-only development hooks, kept apart so a dedicated server never loads client classes. */
final class ClientHooks {
    private ClientHooks() {
    }

    static void install() {
        TickEvent.ClientTickEvent.Post.BUS.addListener((TickEvent.ClientTickEvent.Post event) -> ShotTour.tick());
    }
}
