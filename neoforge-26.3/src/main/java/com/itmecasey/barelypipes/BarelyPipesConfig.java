package com.itmecasey.barelypipes;

import net.neoforged.neoforge.common.ModConfigSpec;

/** The common configuration, stored in {@code barelypipes-common.toml}. */
public final class BarelyPipesConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue DEBUG_LOGGING = BUILDER
            .comment("Log extra detail about pipe networks.")
            .define("debugLogging", false);

    private static final ModConfigSpec.BooleanValue PROTECTION_ENABLED = BUILDER
            .comment("Before the Conduit Tool changes or removes pipes, ask the standard permission events that claim mods veto.")
            .define("protectionEnabled", true);

    /** The spec to register with the mod container. */
    public static final ModConfigSpec SPEC = BUILDER.build();

    private BarelyPipesConfig() {
    }

    /** Whether the Conduit Tool asks the claim checks before it acts. */
    public static boolean protectionEnabled() {
        return PROTECTION_ENABLED.get();
    }

    /** Whether to log extra detail. */
    public static boolean debugLogging() {
        return SPEC.isLoaded() && DEBUG_LOGGING.get();
    }
}
