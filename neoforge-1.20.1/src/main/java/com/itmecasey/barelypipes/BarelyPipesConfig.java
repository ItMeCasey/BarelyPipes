package com.itmecasey.barelypipes;

import net.minecraftforge.common.ForgeConfigSpec;

/** The common configuration, stored in {@code barelypipes-common.toml}. */
public final class BarelyPipesConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue DEBUG_LOGGING = BUILDER
            .comment("Log extra detail about pipe networks.")
            .define("debugLogging", false);

    private static final ForgeConfigSpec.BooleanValue PROTECTION_ENABLED = BUILDER
            .comment("Before the Conduit Tool changes or removes pipes, ask the standard permission events that claim mods veto.")
            .define("protectionEnabled", true);

    /** The spec to register with the mod container. */
    public static final ForgeConfigSpec SPEC = BUILDER.build();

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
