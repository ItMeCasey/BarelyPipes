package com.itmecasey.barelypipes.core.conduit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

/** Hit boxes against what is drawn, clicks and rays on plugs, plug arrows and glyph centering. */
class BundleShapeTest {
    private static final Path GLYPHS = Path.of("..", "common", "src", "main", "resources", "assets", "barelypipes",
            "textures", "gui", "glyph");

    /** Fluid and item pipes that both end at a machine on the south face. */
    private static BundleState twoAtMachine() {
        BundleState state = new BundleState();
        state.addPipe(PipeType.FLUID);
        state.addPipe(PipeType.ITEM);
        state.setConnection(PipeType.FLUID.lane(), Face.SOUTH, true, true);
        state.setConnection(PipeType.ITEM.lane(), Face.SOUTH, true, true);
        return state;
    }

    private static BundleState lone(PipeType type, Face face) {
        BundleState state = new BundleState();
        state.addPipe(type);
        state.setConnection(type.lane(), face, true, true);
        return state;
    }

    private static ConduitLayout.Piece piece(ConduitLayout layout, ConduitLayout.Kind kind, PipeType type) {
        for (ConduitLayout.Piece piece : layout.pieces()) {
            if (piece.kind() == kind && piece.type() == type) {
                return piece;
            }
        }
        throw new AssertionError("No " + kind + " for " + type);
    }

    private static double[] center(Box box) {
        return new double[] {(box.minX() + box.maxX()) / 2, (box.minY() + box.maxY()) / 2, (box.minZ() + box.maxZ()) / 2};
    }

    @Test
    void hitBoxesAreExactlyTheDrawnBoxes() {
        BundleState state = twoAtMachine();
        state.addPipe(PipeType.ENERGY);
        state.setConnection(PipeType.ENERGY.lane(), Face.UP, true, false);
        state.setConnection(PipeType.ENERGY.lane(), Face.EAST, true, false);
        ConduitLayout layout = ConduitLayout.of(state);
        List<Box> drawn = new ArrayList<>();
        for (ConduitLayout.Piece piece : layout.pieces()) {
            drawn.add(piece.box());
        }
        assertEquals(drawn, layout.hitBoxes(), "the outline and collision hug what is drawn, arms included");
    }

    @Test
    void armsAreTwoPixelsWideInTheOutlineAsDrawn() {
        BundleState state = lone(PipeType.FLUID, Face.NORTH);
        state.setConnection(PipeType.FLUID.lane(), Face.NORTH, true, false);
        ConduitLayout layout = ConduitLayout.of(state);
        Box arm = layout.armOf(PipeType.FLUID, Face.NORTH);
        assertNotNull(arm);
        assertEquals(2.0, arm.maxX() - arm.minX(), 1e-9);
        assertEquals(2.0, arm.maxY() - arm.minY(), 1e-9);
        // A point half a pixel beside the arm, where the old widened box used to catch clicks, is not on it.
        assertNull(layout.pick(arm.maxX() + 0.5, (arm.minY() + arm.maxY()) / 2, 2.0));
    }

    @Test
    void plugsAndPlatesAreTheirOwnBoxes() {
        ConduitLayout layout = ConduitLayout.of(twoAtMachine());
        Box plug = piece(layout, ConduitLayout.Kind.PLUG, PipeType.FLUID).box();
        assertEquals(3.0, plug.maxX() - plug.minX(), 1e-9);
        assertEquals(3.0, plug.maxY() - plug.minY(), 1e-9);
        assertEquals(ConduitLayout.PLUG_DEPTH, plug.maxZ() - plug.minZ(), 1e-9);
        assertEquals(16.0, plug.maxZ(), 1e-9);
        Box plate = layout.plateOf(Face.SOUTH);
        assertEquals(ConduitLayout.PLATE_SIZE, plate.maxX() - plate.minX(), 1e-9);
        assertTrue(layout.hitBoxes().contains(plate));
    }

    @Test
    void aRayAlongTheAxisIntoEachPlugPicksThatPipe() {
        ConduitLayout layout = ConduitLayout.of(twoAtMachine());
        for (PipeType type : List.of(PipeType.FLUID, PipeType.ITEM)) {
            Box plug = piece(layout, ConduitLayout.Kind.PLUG, type).box();
            double[] c = center(plug);
            // From inside the bundle toward the machine, onto the rim of the glyph face (past the 2 pixel arm).
            ConduitLayout.Pick fromInside = layout.pick(c[0] + 1.2, c[1], 10.5, 0, 0, 1);
            assertEquals(new ConduitLayout.Pick(type, Face.SOUTH, ConduitLayout.Kind.PLUG), fromInside);
            // From outside, through the machine side: the plate is hit at the same depth as the plug.
            ConduitLayout.Pick fromOutside = layout.pick(c[0], c[1], 30, 0, 0, -1);
            assertEquals(type, fromOutside.pipe());
            assertEquals(Face.SOUTH, fromOutside.face());
        }
    }

    @Test
    void aRayAtTheSideOfAPlugPicksThatPlugNotItsNeighbor() {
        ConduitLayout layout = ConduitLayout.of(twoAtMachine());
        for (PipeType type : List.of(PipeType.FLUID, PipeType.ITEM)) {
            double[] c = center(piece(layout, ConduitLayout.Kind.PLUG, type).box());
            ConduitLayout.Pick pick = layout.pick(-5, c[1], c[2], 1, 0, 0);
            assertEquals(new ConduitLayout.Pick(type, Face.SOUTH, ConduitLayout.Kind.PLUG), pick);
        }
    }

    @Test
    void aClickOnAnArmPicksItsPipeAndFace() {
        ConduitLayout layout = ConduitLayout.of(twoAtMachine());
        for (PipeType type : List.of(PipeType.FLUID, PipeType.ITEM)) {
            Box arm = layout.armOf(type, Face.SOUTH);
            // The arm's side surface between the core and the plug, where no plug covers it.
            double[] c = center(arm);
            ConduitLayout.Pick pick = layout.pick(arm.minX(), c[1], 10.2);
            assertNotNull(pick);
            assertEquals(type, pick.pipe());
            assertEquals(Face.SOUTH, pick.face());
        }
    }

    @Test
    void aClickOnACoreHasNoFace() {
        ConduitLayout layout = ConduitLayout.of(lone(PipeType.GAS, Face.NORTH));
        double[] c = center(piece(layout, ConduitLayout.Kind.CORE, PipeType.GAS).box());
        ConduitLayout.Pick pick = layout.pick(c[0], c[1], c[2] + 1.5);
        assertEquals(PipeType.GAS, pick.pipe());
        assertNull(pick.face());
    }

    @Test
    void aClickOnThePlateBesideAPlugPicksTheNearestPlug() {
        ConduitLayout layout = ConduitLayout.of(twoAtMachine());
        Box fluid = piece(layout, ConduitLayout.Kind.PLUG, PipeType.FLUID).box();
        Box item = piece(layout, ConduitLayout.Kind.PLUG, PipeType.ITEM).box();
        Box plate = layout.plateOf(Face.SOUTH);
        // The plate's edge nearest the fluid plug, outside every plug.
        double y = fluid.minY() < item.minY() ? plate.minY() + 0.2 : plate.maxY() - 0.2;
        ConduitLayout.Pick pick = layout.pick(8, y, 15.99);
        assertEquals(PipeType.FLUID, pick.pipe());
        assertEquals(Face.SOUTH, pick.face());
    }

    @Test
    void nearestPipeStillFallsBackToTheClosestPiece() {
        assertEquals(PipeType.FLUID, lone(PipeType.FLUID, Face.WEST).nearestPipe(14, 8, 8));
        assertNull(new BundleState().nearestPipe(8, 8, 8));
    }

    @Test
    void aRayThatMissesPicksNothing() {
        ConduitLayout layout = ConduitLayout.of(lone(PipeType.FLUID, Face.SOUTH));
        assertNull(layout.pick(1, 1, -5, 0, 0, 1));
    }

    // --- Plug arrows --------------------------------------------------------------------------

    @Test
    void plugPiecesCarryTheirPipesMode() {
        BundleState state = twoAtMachine();
        state.setMode(PipeType.FLUID.lane(), Face.SOUTH, ConnectionMode.IMPORT);
        state.setMode(PipeType.ITEM.lane(), Face.SOUTH, ConnectionMode.EXPORT);
        ConduitLayout layout = ConduitLayout.of(state);
        assertEquals(ConnectionMode.IMPORT, piece(layout, ConduitLayout.Kind.PLUG, PipeType.FLUID).mode());
        assertEquals(ConnectionMode.EXPORT, piece(layout, ConduitLayout.Kind.PLUG, PipeType.ITEM).mode());
        assertNull(piece(layout, ConduitLayout.Kind.ARM, PipeType.ITEM).mode());
    }

    @Test
    void plugFaceNamesFollowTheMode() {
        assertEquals("plug_import", PipeTextures.plugFace(ConnectionMode.IMPORT));
        assertEquals("plug_export", PipeTextures.plugFace(ConnectionMode.EXPORT));
        assertEquals("plug_open", PipeTextures.plugFace(ConnectionMode.OPEN));
        assertEquals("plug", PipeTextures.plugFace(ConnectionMode.OFF), "off shows no arrows");
        assertEquals("plug", PipeTextures.plugFace(ConnectionMode.BLOCKED));
        assertTrue(PipeTextures.allNames().contains("pipe_fluid_plug_export_t2"));
        assertTrue(PipeTextures.allNames().contains("pipe_storage_plug_open"));
    }

    @Test
    void aModeChangeAtAPlugChangesTheModelKey() {
        BundleState state = twoAtMachine();
        BundleState.ModelKey open = state.modelKey();
        state.setMode(PipeType.FLUID.lane(), Face.SOUTH, ConnectionMode.IMPORT);
        BundleState.ModelKey imported = state.modelKey();
        assertNotEquals(open, imported);
        state.setMode(PipeType.FLUID.lane(), Face.SOUTH, ConnectionMode.EXPORT);
        assertNotEquals(imported, state.modelKey());
        state.setMode(PipeType.FLUID.lane(), Face.SOUTH, ConnectionMode.OPEN);
        assertEquals(open, state.modelKey(), "the key is a pure function of the state");
        // The last lane and last face still reach the key.
        BundleState storage = lone(PipeType.STORAGE, Face.EAST);
        BundleState.ModelKey before = storage.modelKey();
        storage.setMode(PipeType.STORAGE.lane(), Face.EAST, ConnectionMode.EXPORT);
        assertNotEquals(before, storage.modelKey());
    }

    // --- Glyph centering ----------------------------------------------------------------------

    @Test
    void buttonSizesCenterTheThirteenPixelPictureExactly() {
        for (int size : new int[] {GlyphCentering.BUTTON, GlyphCentering.SMALL_BUTTON}) {
            assertTrue(GlyphCentering.exact(size), size + " must be odd so the picture has equal margins");
            int boxStart = 40;
            int pictureLeft = GlyphCentering.textureStart(boxStart, size) + GlyphCentering.INSET;
            int leftMargin = pictureLeft - boxStart;
            int rightMargin = boxStart + size - (pictureLeft + GlyphCentering.PICTURE);
            assertEquals(leftMargin, rightMargin);
            assertEquals((size - GlyphCentering.PICTURE) / 2, leftMargin);
        }
        // The old sizes, 22 and 16, could not be centered.
        assertTrue(!GlyphCentering.exact(22) && !GlyphCentering.exact(16));
    }

    @Test
    void everyGlyphPictureSitsInTheMiddleOfItsCanvas() throws Exception {
        try (var files = Files.list(GLYPHS)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".png")).toList()) {
                BufferedImage image = ImageIO.read(file.toFile());
                assertEquals(16, image.getWidth(), file.toString());
                assertEquals(16, image.getHeight(), file.toString());
                int x0 = 99;
                int y0 = 99;
                int x1 = -1;
                int y1 = -1;
                for (int y = 0; y < 16; y++) {
                    for (int x = 0; x < 16; x++) {
                        if ((image.getRGB(x, y) >>> 24) != 0) {
                            x0 = Math.min(x0, x);
                            x1 = Math.max(x1, x);
                            y0 = Math.min(y0, y);
                            y1 = Math.max(y1, y);
                        }
                    }
                }
                assertTrue(x0 >= 1 && y0 >= 1 && x1 <= 13 && y1 <= 13, file + " stays in its 13 by 13 picture");
                // An even-sized picture cannot sit on the middle pixel; half a pixel off is the best it can do.
                assertTrue(Math.abs((x0 + x1) / 2.0 - GlyphCentering.MIDDLE) <= 0.5, file + " horizontally");
                assertTrue(Math.abs((y0 + y1) / 2.0 - GlyphCentering.MIDDLE) <= 0.5, file + " vertically");
            }
        }
    }

    @Test
    void theStorageGlyphIsExactlyCentered() throws Exception {
        BufferedImage image = ImageIO.read(GLYPHS.resolve("storage.png").toFile());
        assertEquals(0xFFFFFFFF, image.getRGB(1, 1), "pure white, so its tint is exact");
        assertEquals(0xFFFFFFFF, image.getRGB(13, 13));
        assertEquals(0, image.getRGB(14, 14) >>> 24);
        assertEquals(0, image.getRGB(0, 0) >>> 24);
    }
}
