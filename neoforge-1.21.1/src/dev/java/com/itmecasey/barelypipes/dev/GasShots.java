package com.itmecasey.barelypipes.dev;

import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import com.itmecasey.barelypipes.conduit.config.PipeConfigMenu;
import com.itmecasey.barelypipes.core.Tier;
import com.itmecasey.barelypipes.core.conduit.ConnectionMode;
import com.itmecasey.barelypipes.core.conduit.Face;
import com.itmecasey.barelypipes.core.conduit.PipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The gas scene of the client tour, planned by {@link ShotTour} only when Mekanism is installed: a Mekanism chemical tank
 * holding chlorine, three gas pipe bundles and an empty tank, then the gas pipe screen, then the receiving tank after the
 * transfer ran. Names Mekanism classes through {@link GasSelfTest}, so it is only loaded with Mekanism present.
 */
final class GasShots {
    private static final Logger LOGGER = LoggerFactory.getLogger("BarelyPipes");
    private static final ResourceLocation CHLORINE = ResourceLocation.fromNamespaceAndPath("mekanism", "chlorine");
    private static final long AMOUNT = 40_000;
    private static final int X0 = 16;

    private GasShots() {
    }

    static void plan(int ground) {
        BlockPos source = new BlockPos(X0, ground, 0);
        BlockPos sink = new BlockPos(X0 + 4, ground, 0);
        BlockPos firstPipe = new BlockPos(X0 + 1, ground, 0);
        BlockPos lastPipe = new BlockPos(X0 + 3, ground, 0);

        ShotTour.then(4, () -> ShotTour.onServer(srv -> {
            GasSelfTest.placeTank(srv.overworld(), source, Direction.EAST);
            GasSelfTest.placeTank(srv.overworld(), sink, Direction.EAST);
            GasSelfTest.fillTank(srv.overworld(), source, CHLORINE, AMOUNT);
            for (int x = X0 + 1; x <= X0 + 3; x++) {
                ShotTour.place(new BlockPos(x, ground, 0)).addPipe(PipeType.GAS, Tier.IRON);
            }
            // Joined, but both ends off: nothing moves until the shot has been taken.
            setMode(srv, firstPipe, Face.WEST, ConnectionMode.OFF);
            setMode(srv, lastPipe, Face.EAST, ConnectionMode.OFF);
        }));
        then2(() -> ShotTour.moveTo(new Vec3(X0 + 2.5, ground + 1.7, -4.6), new Vec3(X0 + 2.5, ground + 0.5, 0.5)));
        ShotTour.shoot("gas_scene_before", 30);
        then2(() -> ShotTour.moveTo(new Vec3(X0 + 1.2, ground + 1.4, -1.6), new Vec3(X0 + 1.0, ground + 0.5, 0.5)));
        ShotTour.shoot("gas_source_tank_before", 6);
        then2(() -> ShotTour.moveTo(new Vec3(X0 + 2.5, ground + 5.0, 0.4), new Vec3(X0 + 2.5, ground + 0.5, 0.5)));
        ShotTour.shoot("gas_tanks_top_before", 6);

        // The gas pipe screen on the source side, with the face set to IMPORT.
        ShotTour.then(4, () -> ShotTour.onServer(srv -> {
            setMode(srv, firstPipe, Face.WEST, ConnectionMode.IMPORT);
            ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) srv.overworld().getBlockEntity(firstPipe);
            ServerPlayer player = srv.getPlayerList().getPlayers().get(0);
            PipeConfigMenu.open(player, entity, PipeType.GAS, Face.WEST);
        }));
        ShotTour.shoot("gas_pipe_screen", 40);
        ShotTour.then(2, () -> ShotTour.onServer(srv -> srv.getPlayerList().getPlayers().get(0).closeContainer()));

        // Switch the sink end on and let it run: 40,000 mB at 800 mB per tick takes about 50 ticks.
        ShotTour.then(4, () -> ShotTour.onServer(srv -> setMode(srv, lastPipe, Face.EAST, ConnectionMode.EXPORT)));
        then2(() -> ShotTour.moveTo(new Vec3(X0 + 2.5, ground + 1.7, -4.6), new Vec3(X0 + 2.5, ground + 0.5, 0.5)));
        ShotTour.then(120, () -> ShotTour.onServer(srv -> LOGGER.info("BARELYPIPES-SHOTS gas source holds {} mB, sink holds {} mB",
                GasSelfTest.amountOf(srv.overworld(), source, CHLORINE), GasSelfTest.amountOf(srv.overworld(), sink, CHLORINE))));
        ShotTour.shoot("gas_scene_after", 10);
        // What the client itself sees in the tanks (the tanks draw from this, not from the server's numbers).
        then2(() -> LOGGER.info("BARELYPIPES-SHOTS gas as the client sees it: source {} mB, sink {} mB",
                GasSelfTest.amountOf(net.minecraft.client.Minecraft.getInstance().level, source, CHLORINE),
                GasSelfTest.amountOf(net.minecraft.client.Minecraft.getInstance().level, sink, CHLORINE)));
        then2(() -> ShotTour.moveTo(new Vec3(X0 + 4.9, ground + 1.4, -1.6), new Vec3(X0 + 4.5, ground + 0.5, 0.5)));
        ShotTour.shoot("gas_receiving_tank_after", 6);
        then2(() -> ShotTour.moveTo(new Vec3(X0 + 4.5, ground + 3.2, -1.2), new Vec3(X0 + 4.5, ground + 0.8, 0.5)));
        ShotTour.shoot("gas_receiving_tank_top", 6);
        then2(() -> ShotTour.moveTo(new Vec3(X0 + 2.5, ground + 5.0, 0.4), new Vec3(X0 + 2.5, ground + 0.5, 0.5)));
        ShotTour.shoot("gas_tanks_top_after", 6);
    }

    private static void then2(Runnable action) {
        ShotTour.then(2, action);
    }

    private static void setMode(net.minecraft.server.MinecraftServer srv, BlockPos pos, Face face, ConnectionMode mode) {
        ConduitBundleBlockEntity entity = (ConduitBundleBlockEntity) srv.overworld().getBlockEntity(pos);
        entity.bundle().setMode(PipeType.GAS.lane(), face, mode);
        entity.refreshConnections();
        entity.changed();
    }
}
