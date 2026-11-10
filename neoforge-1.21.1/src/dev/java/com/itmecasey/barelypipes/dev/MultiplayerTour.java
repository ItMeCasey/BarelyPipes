package com.itmecasey.barelypipes.dev;

import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import com.itmecasey.barelypipes.conduit.config.PipeConfigActions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Development-only multiplayer check, run when {@code BARELYPIPES_MP=host:port} is set: the client connects to a dedicated
 * server that kept its self-test scene (see SelfTest), so everything it sees arrives over the network: the bundles and their
 * pipes (block entity sync), the model, and the pipe screen (menu with open data). It stays connected for a few minutes
 * (a long session on a ticking server), looks again, then quits. Progress goes to the log as {@code BARELYPIPES-MP}.
 */
final class MultiplayerTour {
    private static final Logger LOGGER = LoggerFactory.getLogger("BarelyPipes");
    private static final String TARGET = System.getenv("BARELYPIPES_MP");
    private static final int SOAK_TICKS = Integer.getInteger("barelypipes.mp.soak", Integer.parseInt(System.getenv().getOrDefault("BARELYPIPES_MP_SOAK", "6000")));
    private static int tick;
    private static int stage;

    private MultiplayerTour() {
    }

    static boolean enabled() {
        return TARGET != null;
    }

    static void tick() {
        if (TARGET == null || stage >= 99) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        tick++;
        if (stage == 0) {
            if (mc.getOverlay() == null && mc.screen instanceof TitleScreen && tick > 40) {
                ServerAddress address = ServerAddress.parseString(TARGET);
                ConnectScreen.startConnecting(mc.screen, mc, address, new ServerData("barelypipes dev", TARGET, ServerData.Type.OTHER), false, null);
                LOGGER.info("BARELYPIPES-MP connecting to {}", TARGET);
                stage = 1;
                tick = 0;
            }
            return;
        }
        if (mc.level == null || mc.player == null) {
            return;
        }
        if (mc.getSingleplayerServer() != null) {
            LOGGER.error("BARELYPIPES-MP FAIL the client is on an integrated server, not a dedicated one");
            stage = 99;
            mc.stop();
            return;
        }
        switch (stage) {
            case 1 -> {
                if (tick == 100) {
                    mc.player.connection.sendCommand("gamemode creative");
                    mc.player.connection.sendCommand("tp @s 2.5 201 -1.5 0 20");
                    stage = 2;
                    tick = 0;
                }
            }
            case 2 -> {
                if (tick == 2) {
                    // Hold still in the air (a creative player flies): without it the player falls out of reach of the scene.
                    mc.player.getAbilities().flying = true;
                    mc.player.onUpdateAbilities();
                    mc.player.connection.sendCommand("tp @s 2.5 201 -1.5 0 20");
                }
                if (tick == 60) {
                    BlockPos pos = new BlockPos(2, 200, 2);
                    boolean block = mc.level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity;
                    LOGGER.info("BARELYPIPES-MP over the network: bundle block entity at {} present on the client: {}", pos, block);
                    if (block) {
                        ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) mc.level.getBlockEntity(pos);
                        LOGGER.info("BARELYPIPES-MP bundle pipes on the client: {}", entity.bundle().set().types());
                    }
                    shot(mc, "mp_scene_joined");
                    stage = 3;
                    tick = 0;
                }
            }
            case 3 -> {
                if (tick == 5) {
                    // Find a point on a pipe connector the way the server will (the same lookup the block uses), then use the block
                    // there with an empty hand through the game's own handler: the server opens the pipe screen over the network.
                    BlockPos pos = new BlockPos(2, 200, 2);
                    ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) mc.level.getBlockEntity(pos);
                    Vec3 found = null;
                    for (int x = 0; x < 32 && found == null; x++) {
                        for (int y = 0; y < 32 && found == null; y++) {
                            for (int z = 0; z < 32 && found == null; z++) {
                                if (PipeConfigActions.resolveTarget(entity.bundle(), x / 2.0, y / 2.0, z / 2.0) != null) {
                                    found = new Vec3(x / 2.0, y / 2.0, z / 2.0);
                                }
                            }
                        }
                    }
                    LOGGER.info("BARELYPIPES-MP connector point in the bundle (pixels): {}, main hand: {}", found, mc.player.getMainHandItem());
                    if (found != null) {
                        Vec3 at = new Vec3(pos.getX() + found.x / 16.0, pos.getY() + found.y / 16.0, pos.getZ() + found.z / 16.0);
                        mc.player.getInventory().selected = 8;
                        mc.player.getInventory().setItem(8, net.minecraft.world.item.ItemStack.EMPTY);
                        // Look at the point first, so the position and view the server checks match the click.
                        mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, at);
                        net.minecraft.world.InteractionResult result = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                                new BlockHitResult(at, Direction.SOUTH, pos, false));
                        LOGGER.info("BARELYPIPES-MP use result {} at {} from {} mode {}", result, at, mc.player.position(), mc.gameMode.getPlayerMode());
                    }
                }
                if (tick == 60) {
                    LOGGER.info("BARELYPIPES-MP screen after use: {}", mc.screen == null ? "none" : mc.screen.getClass().getSimpleName());
                    shot(mc, "mp_pipe_screen");
                    mc.setScreen(null);
                    stage = 4;
                    tick = 0;
                }
            }
            case 4 -> {
                if (tick == SOAK_TICKS) {
                    LOGGER.info("BARELYPIPES-MP soak of {} ticks done, still connected: {}", SOAK_TICKS, mc.getConnection() != null);
                    shot(mc, "mp_after_soak");
                    stage = 5;
                    tick = 0;
                }
            }
            case 5 -> {
                if (tick == 20) {
                    LOGGER.info("BARELYPIPES-MP DONE");
                    stage = 99;
                    mc.stop();
                }
            }
            default -> {
            }
        }
    }

    private static void shot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, "barelypipes_" + name + ".png", mc.getMainRenderTarget(),
                message -> LOGGER.info("BARELYPIPES-MP saved {}", name));
    }
}
