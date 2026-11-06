package com.itmecasey.barelypipes;

import net.minecraft.resources.Identifier;

/** Builds resource identifiers. Minecraft 26.1 renamed {@code ResourceLocation} to {@code Identifier}. */
public final class Ids {
    private Ids() {
    }

    /** An identifier in this mod's namespace, such as {@code barelypipes:block/flux_still}. */
    public static Identifier mod(String path) {
        return Identifier.fromNamespaceAndPath(BarelyPipes.MODID, path);
    }

    /** An identifier in any namespace. */
    public static Identifier of(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }
}
