package com.itmecasey.barelypipes;

import com.itmecasey.barelypipes.registry.ModBlockEntities;
import com.itmecasey.barelypipes.registry.ModBlocks;
import com.itmecasey.barelypipes.registry.ModCreativeTabs;
import com.itmecasey.barelypipes.registry.ModItems;
import com.itmecasey.barelypipes.registry.ModMenus;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/** Entry point for Barely Pipes (Forge 26.1.2): seven pipes that share one bundle block. */
@Mod(BarelyPipes.MODID)
public final class BarelyPipes {
    /** Mod id; must match {@code mod_id} in gradle.properties and the mods metadata file. */
    public static final String MODID = "barelypipes";

    /** Shared logger for the mod. */
    public static final Logger LOGGER = LogUtils.getLogger();

    public BarelyPipes(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();

        ModBlocks.register(modBusGroup);
        ModBlockEntities.register(modBusGroup);
        ModItems.register(modBusGroup);
        ModMenus.register(modBusGroup);
        ModCreativeTabs.register(modBusGroup);
        context.registerConfig(ModConfig.Type.COMMON, BarelyPipesConfig.SPEC);
        if (System.getProperty("barelypipes.dev") != null) {
            // Development tools (test tank, self test) exist only in builds made with -PdevTools.
            try {
                Class.forName("com.itmecasey.barelypipes.dev.DevHooks").getMethod("install", FMLJavaModLoadingContext.class).invoke(null, context);
            } catch (ReflectiveOperationException e) {
                LOGGER.warn("barelypipes.dev is set but this build has no development tools");
            }
        }
        LOGGER.info("Barely Pipes loaded (Forge 26.1.2)");
    }
}
