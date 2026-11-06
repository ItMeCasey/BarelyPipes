package com.itmecasey.barelypipes.dev;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;

/**
 * Opens the persistent {@link Playground#WORLD} from the title screen, creating it (flat, creative, cheats on) the first time.
 * Unlike the screenshot tour it leaves the mouse and keyboard alone: a person plays this client.
 */
final class PlaygroundClient {
    private static boolean started;
    private static int idle;

    private PlaygroundClient() {
    }

    static void tick() {
        if (!Playground.requested() || started) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getOverlay() != null || minecraft.level != null || minecraft.screen == null || ++idle < 40) {
            return;
        }
        started = true;
        LevelStorageSource source = minecraft.getLevelSource();
        if (source.levelExists(Playground.WORLD)) {
            minecraft.createWorldOpenFlows().openWorld(Playground.WORLD, () -> { });
            return;
        }
        LevelSettings settings = new LevelSettings(Playground.WORLD, GameType.CREATIVE,
                new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false), true, WorldDataConfiguration.DEFAULT);
        minecraft.createWorldOpenFlows().createFreshLevel(Playground.WORLD, settings, new WorldOptions(0L, false, false),
                WorldPresets::createFlatWorldDimensions, new TitleScreen());
    }
}
