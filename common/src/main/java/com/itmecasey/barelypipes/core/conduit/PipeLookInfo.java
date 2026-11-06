package com.itmecasey.barelypipes.core.conduit;

import com.itmecasey.barelypipes.core.Tier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * What a look-at overlay (Show Me What This Is today, Jade or WTHIT later) says about the pipe under the crosshair.
 *
 * <p>The pipe is the one the Pipe Wrench and the Conduit Tool pick ({@link ConduitLayout#nearestPipe}); this class also
 * says which part of it the point is on (arm, core, plug, or the shared junction box), so an arm reads with its side, its
 * mode and what it connects to, and a core reads with all its sides and the other pipes of the bundle. It knows nothing
 * about Minecraft: lines come out as {@link Text} (a translation key with arguments) and each loader turns them into
 * components, so every build shows exactly the same words and the unit tests can check them.
 */
public final class PipeLookInfo {
    /** The part of a pipe the point is on. */
    public enum Part {
        /** A pipe's arm toward one side. */
        ARM,
        /** The core of a pipe that has one spot to itself. */
        CORE,
        /** The shared box around the spots of pipes that change position. */
        JUNCTION,
        /** A pipe's plug on the plate where it meets a machine. */
        PLUG
    }

    /**
     * A translatable line: a key and its arguments. An argument is a {@link String}, a {@link Number}, a {@link Text} (a
     * nested translation) or a {@code List<Text>} (shown joined with commas).
     */
    public record Text(String key, List<Object> args) {
        public static Text of(String key, Object... args) {
            return new Text(key, List.of(args));
        }
    }

    /** What the loader supplies: things only the game knows. */
    public interface Environment {
        /** The name of a pipe as its item reads, tier included ("Gold Item Pipe"). */
        Text pipeName(PipeType type, Tier tier);

        /** The name of the block next to the bundle at a face, or {@code null} if there is none worth naming. */
        Text neighbor(Face face);

        /** The throughput line of a pipe, worked out by the same helper the pipe screen uses. */
        Text rate(PipeType type, Tier tier, int speedUpgrades, int capacityUpgrades);
    }

    /**
     * The answer.
     *
     * @param type  the pipe under the point (for a junction, the nearest one: the overlay should not name it)
     * @param tier  its tier ({@link PipeTiers#STORAGE_TIER} for the storage pipe, iron for untiered pipes)
     * @param part  which part of it
     * @param face  the side of an arm or plug, or {@code null} for a core or junction
     * @param lines the detail lines, in order
     */
    public record Look(PipeType type, Tier tier, Part part, Face face, List<Text> lines) {
    }

    private static final String P = "smwti.barelypipes.";

    private PipeLookInfo() {
    }

    /**
     * Works out the pipe and part at a point of a bundle and its lines.
     *
     * @param x x of the point in block pixels (16 to a block)
     * @param y y of the point in block pixels
     * @param z z of the point in block pixels
     * @return the look, or {@code null} for an empty bundle
     */
    public static Look of(BundleState bundle, EndpointSettings endpoints, double x, double y, double z, Environment env) {
        ConduitLayout layout = ConduitLayout.of(bundle);

        // The shared resolver the wrench and the tools use: the piece the point is on, a plug before an arm before a core.
        ConduitLayout.Pick pick = layout.pick(x, y, z);
        PipeType pipe;
        Part part;
        Face face = null;
        if (pick != null) {
            pipe = pick.pipe();
            part = switch (pick.kind()) {
                case PLUG -> Part.PLUG;
                case ARM -> Part.ARM;
                case JUNCTION -> Part.JUNCTION;
                default -> Part.CORE;
            };
            if (part == Part.ARM || part == Part.PLUG) {
                face = pick.face();
            }
        } else {
            // Not on any piece (a point a little off the surface): the nearest pipe, described as a whole.
            pipe = layout.nearestPipe(x, y, z);
            part = Part.CORE;
        }
        if (pipe == null) {
            return null;
        }

        if (part == Part.JUNCTION) {
            // The junction belongs to no single pipe: say what it is and list every pipe in the bundle.
            List<Text> all = new ArrayList<>();
            for (PipeType each : bundle.set().types()) {
                all.add(env.pipeName(each, bundle.tier(each.lane())));
            }
            return new Look(pipe, bundle.tier(pipe.lane()), part, null,
                    List.of(Text.of(P + "junction"), Text.of(P + "pipes", all)));
        }

        int lane = pipe.lane();
        Tier tier = bundle.tier(lane);
        List<Text> lines = new ArrayList<>();

        if (face != null) {
            lines.add(Text.of(P + (part == Part.ARM ? "arm" : "plug"), faceName(face)));
            ConnectionMode mode = bundle.mode(lane, face);
            lines.add(Text.of(P + "mode", Text.of("connection_mode.barelypipes." + mode.name().toLowerCase(Locale.ROOT))));
            lines.add(connection(bundle, lane, face, env));
        } else {
            lines.add(Text.of(P + (part == Part.CORE ? "core" : "junction")));
            lines.add(sides(bundle, lane));
        }

        int speed = bundle.speedUpgrades(lane);
        int capacity = bundle.capacityUpgrades(lane);
        lines.add(Text.of(P + "throughput", env.rate(pipe, tier, speed, capacity)));
        if (PipeTiers.isTiered(pipe)) {
            lines.add(Text.of("tooltip.barelypipes.look.upgrades", speed, capacity));
        }
        if (pipe.usesDistribution()) {
            lines.add(Text.of(P + "distribution", Text.of("distribution_mode.barelypipes."
                    + bundle.distribution(lane).name().toLowerCase(Locale.ROOT))));
        }

        if (face != null) {
            EndpointFilter filter = endpoints.filter(lane, face);
            if (filter != null) {
                lines.add(filterLine(filter));
            }
            if (endpoints.has(lane, face, EndpointSettings.Upgrade.PRIORITY)) {
                lines.add(Text.of(P + "priority", priorityText(endpoints.priority(lane, face))));
            }
        } else {
            List<Text> others = new ArrayList<>();
            for (PipeType other : bundle.set().types()) {
                if (other != pipe) {
                    others.add(env.pipeName(other, bundle.tier(other.lane())));
                }
            }
            if (!others.isEmpty()) {
                lines.add(Text.of(P + "others", others));
            }
        }
        return new Look(pipe, tier, part, face, List.copyOf(lines));
    }

    private static Text faceName(Face face) {
        return Text.of("face.barelypipes." + face.name().toLowerCase(Locale.ROOT));
    }

    /** What an arm joins: the machine's name, another bundle, or nothing. */
    private static Text connection(BundleState bundle, int lane, Face face, Environment env) {
        if (!bundle.isConnected(lane, face)) {
            return Text.of(P + "not_connected");
        }
        if (!bundle.isExternal(lane, face)) {
            return Text.of(P + "connected_to", Text.of(P + "another_bundle"));
        }
        Text name = env.neighbor(face);
        return Text.of(P + "connected_to", name != null ? name : Text.of(P + "a_machine"));
    }

    /** The connected sides of a pipe with the mode of each, for the core. */
    private static Text sides(BundleState bundle, int lane) {
        List<Text> sides = new ArrayList<>();
        for (Face face : Face.values()) {
            if (bundle.isConnected(lane, face)) {
                sides.add(Text.of(P + "side_mode", faceName(face), Text.of("connection_mode.barelypipes."
                        + bundle.mode(lane, face).name().toLowerCase(Locale.ROOT))));
            }
        }
        return sides.isEmpty() ? Text.of(P + "no_sides") : Text.of(P + "sides", sides);
    }

    private static Text filterLine(EndpointFilter filter) {
        int entries = 0;
        for (int slot = 0; slot < EndpointFilter.SLOTS; slot++) {
            if (filter.entry(slot) != null) {
                entries++;
            }
        }
        String kind = filter.isWhitelist() ? "whitelist" : "blacklist";
        return entries == 0 ? Text.of(P + "filter_" + kind + "_empty") : Text.of(P + "filter_" + kind, entries);
    }

    /** How a priority reads: with a plus sign above zero (the same as the pipe screen). */
    static String priorityText(int priority) {
        return priority > PriorityPlanner.DEFAULT ? "+" + priority : Integer.toString(priority);
    }
}
