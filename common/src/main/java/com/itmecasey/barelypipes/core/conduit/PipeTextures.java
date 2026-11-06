package com.itmecasey.barelypipes.core.conduit;

import com.itmecasey.barelypipes.core.Tier;
import java.util.ArrayList;
import java.util.List;

/**
 * Which pipe texture goes on which face of a conduit bundle. The textures (made by
 * {@code assets-src/tools/gen_pipe_textures.py}) are colored already and sized to their part, so
 * the model never tints, rotates or flips them: it only picks the file and, for arms, how much of
 * it to show.
 *
 * <p>Every pipe part has a V file, whose length runs down the texture, and an H file, whose length
 * runs across it. Which one a side face takes depends only on the pipe's axis and the face:
 *
 * <pre>
 *   pipe axis | up, down | north, south | east, west
 *   x         | H        | H            | (end)
 *   y         | (end)    | V            | V
 *   z         | V        | (end)        | H
 * </pre>
 *
 * <p>Names are {@code pipe_<type>_<part>[_t1..t3]}: only fluid, item, energy and gas pipes have a tier suffix. The plug's glyph face is the same for every tier.
 */
public final class PipeTextures {
    /** The longest arm, in pixels; an arm texture covers this length. */
    public static final double ARM_LENGTH = 10.0;

    /** The tiered parts of every pipe: arm, core and plug sides in both directions, and arm and core ends. */
    public static final List<String> TIERED_PARTS = List.of("arm_v", "arm_h", "arm_end", "core_v", "core_h", "core_end", "plug_v", "plug_h");
    /** The plug's glyph face, which shows what the pipe carries and has no tier. */
    public static final String PLUG_FACE = "plug";

    /**
     * The plug's glyph faces that carry arrows: the glyph with arrows in its side margins that point
     * toward it (import, into the pipe), away from it (export, out of the pipe) or both (open).
     */
    public static final List<String> PLUG_ARROW_FACES = List.of("plug_import", "plug_export", "plug_open");

    private PipeTextures() {
    }

    /**
     * The glyph face a plug takes for the pipe's mode at that face: arrows for import, export and
     * open, the plain glyph for off (and for anything else, which has no plug).
     */
    public static String plugFace(ConnectionMode mode) {
        if (mode == null) {
            return PLUG_FACE;
        }
        return switch (mode) {
            case IMPORT -> PLUG_ARROW_FACES.get(0);
            case EXPORT -> PLUG_ARROW_FACES.get(1);
            case OPEN -> PLUG_ARROW_FACES.get(2);
            default -> PLUG_FACE;
        };
    }

    /** Whether a pipe type comes in tiers; redstone, ME, dense ME and storage pipes have a single set. */
    public static boolean tiered(PipeType type) {
        return PipeTiers.isTiered(type);
    }

    /** The texture name (without folder) of a part of a pipe of the given type and tier, such as {@code pipe_item_arm_v_t2}. */
    public static String name(PipeType type, Tier tier, String part) {
        String base = "pipe_" + type.id() + "_" + part;
        return tiered(type) ? base + "_" + tier.suffix() : base;
    }

    /** Every pipe texture name, for loading them all when the model is baked. */
    public static List<String> allNames() {
        List<String> names = new ArrayList<>();
        for (PipeType type : PipeType.values()) {
            for (Tier tier : tiered(type) ? List.of(Tier.values()) : List.of(Tier.IRON)) {
                for (String part : TIERED_PARTS) {
                    names.add(name(type, tier, part));
                }
                names.add(name(type, tier, PLUG_FACE));
                for (String arrows : PLUG_ARROW_FACES) {
                    names.add(name(type, tier, arrows));
                }
            }
        }
        return names;
    }

    /** Whether a face lies across the pipe (an end) rather than along it. */
    public static boolean isEnd(int axis, Face face) {
        return ConduitLayout.axisOf(face) == axis;
    }

    /**
     * Whether a side face of a pipe running along {@code axis} (0 x, 1 y, 2 z) takes the V file;
     * otherwise it takes the H file. Not meant for end faces.
     */
    public static boolean vertical(int axis, Face face) {
        return switch (axis) {
            case 1 -> true;
            case 2 -> face == Face.UP || face == Face.DOWN;
            default -> false;
        };
    }

    /**
     * The UV rectangle (u1, v1, u2, v2, in the usual 0 to 16 range) for a side of an arm
     * {@code lengthPx} pixels long: the part of the texture that matches the arm, so its pixels
     * keep their size whatever the arm's length.
     */
    public static float[] armUv(boolean vertical, double lengthPx) {
        float span = (float) (16.0 * Math.max(0.0, Math.min(lengthPx, ARM_LENGTH)) / ARM_LENGTH);
        return vertical ? new float[] {0, 0, 16, span} : new float[] {0, 0, span, 16};
    }
}
