package com.itmecasey.barelypipes;

import com.itmecasey.barelypipes.registry.ModBlockEntities;
import com.itmecasey.barelypipes.registry.ModBlocks;
import com.itmecasey.barelypipes.registry.ModCreativeTabs;
import com.itmecasey.barelypipes.registry.ModItems;
import com.itmecasey.barelypipes.registry.ModMenus;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/** Entry point for Barely Pipes (NeoForge 1.21.1): seven pipes that share one bundle block. */
@Mod(BarelyPipes.MODID)
public class BarelyPipes {
    /** Mod id; must match {@code mod_id} in gradle.properties and the mods metadata file. */
    public static final String MODID = "barelypipes";

    /** Shared logger for the mod. */
    public static final Logger LOGGER = LogUtils.getLogger();

    public BarelyPipes(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenus.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, BarelyPipesConfig.SPEC);
        if (System.getProperty("barelypipes.dev") != null) {
            // Development tools (test tank, self test) exist only in builds made with -PdevTools.
            try {
                Class.forName("com.itmecasey.barelypipes.dev.DevHooks").getMethod("install", IEventBus.class).invoke(null, modEventBus);
            } catch (ReflectiveOperationException e) {
                LOGGER.warn("barelypipes.dev is set but this build has no development tools");
            }
        }
        // Optional ModJS scripting: the global BarelyPipes, only when ModJS is installed (see docs/scripting.md).
        com.itmecasey.barelypipes.core.script.ModJsBridge.install(com.itmecasey.barelypipes.core.script.PipeScripts.API);
        LOGGER.info("Barely Pipes loaded (NeoForge 1.21.1)");
    }
}
