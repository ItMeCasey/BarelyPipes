package com.itmecasey.barelypipes.core.conduit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.itmecasey.barelypipes.core.Tier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class PipeTexturesTest {
    /** The pipe textures as a build ships them; every build has the same set. */
    private static final Path TEXTURES = Path.of("..", "common", "src", "main", "resources",
            "assets", "barelypipes", "textures", "block");

    @Test
    void namesFollowTheArtsPattern() {
        assertEquals("pipe_fluid_arm_v_t1", PipeTextures.name(PipeType.FLUID, Tier.IRON, "arm_v"));
        assertEquals("pipe_fluid_arm_v_t2", PipeTextures.name(PipeType.FLUID, Tier.GOLD, "arm_v"));
        assertEquals("pipe_fluid_plug_t3", PipeTextures.name(PipeType.FLUID, Tier.DIAMOND, "plug"));
        assertEquals("pipe_storage_plug", PipeTextures.name(PipeType.STORAGE, Tier.DIAMOND, "plug"));
        assertEquals("pipe_redstone_arm_h", PipeTextures.name(PipeType.REDSTONE, Tier.GOLD, "arm_h"));
        assertEquals("pipe_dense_me_core_end", PipeTextures.name(PipeType.DENSE_ME, Tier.DIAMOND, "core_end"));
    }

    @Test
    void everyNameHasItsFileAndNothingIsLeftOver() throws Exception {
        List<String> names = PipeTextures.allNames();
        assertEquals(names.size(), new HashSet<>(names).size(), "duplicate names");
        List<String> files;
        try (Stream<Path> list = Files.list(TEXTURES)) {
            // Only the per-part textures: the folder also holds the item textures and pipe_node.
            files = list.map(path -> path.getFileName().toString().replace(".png", ""))
                    .filter(name -> name.matches("pipe_.+_(arm|core|plug).*")).sorted().toList();
        }
        assertEquals(names.stream().sorted().toList(), files);
    }

    @Test
    void sidesFollowTheFaceTable() {
        // x: every side is H.
        for (Face face : List.of(Face.UP, Face.DOWN, Face.NORTH, Face.SOUTH)) {
            assertFalse(PipeTextures.vertical(0, face));
        }
        // y: every side is V.
        for (Face face : List.of(Face.NORTH, Face.SOUTH, Face.EAST, Face.WEST)) {
            assertTrue(PipeTextures.vertical(1, face));
        }
        // z: up and down are V, east and west H.
        assertTrue(PipeTextures.vertical(2, Face.UP));
        assertTrue(PipeTextures.vertical(2, Face.DOWN));
        assertFalse(PipeTextures.vertical(2, Face.EAST));
        assertFalse(PipeTextures.vertical(2, Face.WEST));
        assertTrue(PipeTextures.isEnd(2, Face.NORTH));
        assertFalse(PipeTextures.isEnd(2, Face.UP));
    }

    @Test
    void armsShowTheirShareOfTheTexture() {
        assertArrayEquals(new float[] {0, 0, 16, 8}, PipeTextures.armUv(true, 5));
        assertArrayEquals(new float[] {0, 0, 16, 16}, PipeTextures.armUv(false, 10));
        assertArrayEquals(new float[] {0, 0, 16, 16}, PipeTextures.armUv(false, 12), "never more than the whole texture");
    }

    @Test
    void plugOverlaysPointTheWayThePipeGoes() {
        // A south plug (neighbor toward +z): on top the texture's top is toward -z, so an export (toward +z) points down in the texture.
        assertEquals("pipe_overlay_plug_down", PipeTextures.plugOverlay(Face.SOUTH, Face.UP, ConnectionMode.EXPORT));
        assertEquals("pipe_overlay_plug_up", PipeTextures.plugOverlay(Face.SOUTH, Face.UP, ConnectionMode.IMPORT));
        // Underneath the texture is turned the other way, so the same export points up.
        assertEquals("pipe_overlay_plug_up", PipeTextures.plugOverlay(Face.SOUTH, Face.DOWN, ConnectionMode.EXPORT));
        // On the west side the texture's right edge is toward +z, so an export points right; on the east side left.
        assertEquals("pipe_overlay_plug_right", PipeTextures.plugOverlay(Face.SOUTH, Face.WEST, ConnectionMode.EXPORT));
        assertEquals("pipe_overlay_plug_left", PipeTextures.plugOverlay(Face.SOUTH, Face.EAST, ConnectionMode.EXPORT));
        // An up plug: every side's texture top is toward +y, so an export (up) points up.
        for (Face side : List.of(Face.NORTH, Face.SOUTH, Face.EAST, Face.WEST)) {
            assertEquals("pipe_overlay_plug_up", PipeTextures.plugOverlay(Face.UP, side, ConnectionMode.EXPORT));
            assertEquals("pipe_overlay_plug_down", PipeTextures.plugOverlay(Face.UP, side, ConnectionMode.IMPORT));
        }
        // An east plug seen from the south: the right edge is toward +x, so an export points right; from the north it is turned.
        assertEquals("pipe_overlay_plug_right", PipeTextures.plugOverlay(Face.EAST, Face.SOUTH, ConnectionMode.EXPORT));
        assertEquals("pipe_overlay_plug_left", PipeTextures.plugOverlay(Face.EAST, Face.NORTH, ConnectionMode.EXPORT));
        // Open shows both ways, along the height or the width; off and the plug's own end show nothing.
        assertEquals("pipe_overlay_plug_both_v", PipeTextures.plugOverlay(Face.SOUTH, Face.UP, ConnectionMode.OPEN));
        assertEquals("pipe_overlay_plug_both_h", PipeTextures.plugOverlay(Face.SOUTH, Face.WEST, ConnectionMode.OPEN));
        assertEquals(null, PipeTextures.plugOverlay(Face.SOUTH, Face.UP, ConnectionMode.OFF));
        assertEquals(null, PipeTextures.plugOverlay(Face.SOUTH, Face.NORTH, ConnectionMode.EXPORT));
    }
}
