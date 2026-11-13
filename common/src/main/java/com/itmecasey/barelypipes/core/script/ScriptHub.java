package com.itmecasey.barelypipes.core.script;

import com.itmecasey.barelypipes.core.Tier;
import com.itmecasey.barelypipes.core.conduit.PipeTiers;
import com.itmecasey.barelypipes.core.conduit.PipeType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Where the transfer code reports to and where scripts read from. Nothing here names a Minecraft class: the loaders' transfer
 * code hands over plain numbers and strings, and the {@link PipeScripts} API gives scripts snapshots and events built from them.
 *
 * <p>Nothing is recorded until {@link #enable()} is called (which the ModJS bridge does when ModJS is installed, and a script
 * does by listening), so a game without scripts pays for one boolean check per network rebuild and per tick. All calls come
 * from the server thread.
 */
public final class ScriptHub {
    private static final Logger LOG = Logger.getLogger("barelypipes");
    /** A snapshot that nothing has refreshed for this long (in nanoseconds) belongs to a network that is no longer ticking. */
    private static final long STALE_NANOS = 60_000_000_000L;

    private static volatile boolean tracking;

    private static final Map<String, Entry> NETWORKS = new ConcurrentHashMap<>();
    private static final Map<String, Totals> TOTALS = new ConcurrentHashMap<>();
    private static final List<Listener<TransferEvent>> TRANSFER = new CopyOnWriteArrayList<>();
    private static final List<Listener<NetworkEvent>> CHANGED = new CopyOnWriteArrayList<>();

    private ScriptHub() {
    }

    /** A block position as scripts see it. */
    public static final class Pos {
        private final int x;
        private final int y;
        private final int z;

        public Pos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public int z() {
            return z;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Pos p && p.x == x && p.y == y && p.z == z;
        }

        @Override
        public int hashCode() {
            return (y + z * 31) * 31 + x;
        }

        @Override
        public String toString() {
            return x + " " + y + " " + z;
        }
    }

    /** One place where a network touches a machine. */
    public static final class Endpoint {
        private final Pos bundle;
        private final String face;
        private final Pos machine;
        private final String mode;
        private final boolean pulls;
        private final boolean pushes;
        private final int priority;

        public Endpoint(Pos bundle, String face, Pos machine, String mode, boolean pulls, boolean pushes, int priority) {
            this.bundle = bundle;
            this.face = face;
            this.machine = machine;
            this.mode = mode;
            this.pulls = pulls;
            this.pushes = pushes;
            this.priority = priority;
        }

        /** The bundle that owns this face. */
        public Pos bundle() {
            return bundle;
        }

        /** The face of that bundle: down, up, north, south, west or east. */
        public String face() {
            return face;
        }

        /** The machine block the face points at. */
        public Pos machine() {
            return machine;
        }

        /** import, export or open. */
        public String mode() {
            return mode;
        }

        /** Whether the network takes from this machine. */
        public boolean pulls() {
            return pulls;
        }

        /** Whether the network gives to this machine. */
        public boolean pushes() {
            return pushes;
        }

        public int priority() {
            return priority;
        }

        boolean sameAs(Endpoint other) {
            return bundle.equals(other.bundle) && face.equals(other.face) && machine.equals(other.machine) && mode.equals(other.mode)
                    && pulls == other.pulls && pushes == other.pushes && priority == other.priority;
        }
    }

    /** What the network has moved since it was first seen; shared by every snapshot of the same network. */
    static final class Totals {
        volatile long moved;
        volatile String lastResource = "";
    }

    private record Entry(NetworkInfo info, long seenNanos) {
    }

    /** Wraps a script's listener so that its failures are logged once, not on every tick. */
    private static final class Listener<T> {
        private final Consumer<T> target;
        private boolean reported;

        Listener(Consumer<T> target) {
            this.target = target;
        }

        void accept(String what, T event) {
            try {
                target.accept(event);
            } catch (RuntimeException | LinkageError e) {
                if (!reported) {
                    reported = true;
                    LOG.warning("Barely Pipes: a script listener for " + what + " failed (further failures of it are not logged): " + e);
                }
            }
        }
    }

    // ---- switching on ----

    /** Starts recording networks and transfers. Called by the ModJS bridge and by script listeners. */
    public static void enable() {
        tracking = true;
    }

    /** Whether anything is recorded; the transfer code checks this before it builds anything. */
    public static boolean tracking() {
        return tracking;
    }

    // ---- fed by the transfer code ----

    /**
     * Records a network after it was (re)built. Fires {@code networkChanged} when the network is new or its shape changed;
     * the periodic rebuild of an unchanged network is silent.
     */
    public static void publish(String dimension, PipeType type, Pos leader, List<Pos> bundles, List<Endpoint> endpoints,
                               long amountPerTick, int ticksPerOperation, int itemsPerOperation, boolean truncated) {
        if (!tracking) {
            return;
        }
        String key = key(dimension, leader, type);
        Totals totals = TOTALS.computeIfAbsent(key, k -> new Totals());
        NetworkInfo info = new NetworkInfo(dimension, type.id(), tierName(type, amountPerTick, ticksPerOperation), leader,
                List.copyOf(bundles), List.copyOf(endpoints), amountPerTick, ticksPerOperation, itemsPerOperation, truncated, totals);
        Entry old = NETWORKS.put(key, new Entry(info, System.nanoTime()));
        if (old == null) {
            fireChanged("created", info);
        } else if (!old.info().sameShape(info)) {
            fireChanged("changed", info);
        }
    }

    /** A bundle was removed: every network that contained it is gone (the rest of it is published again when it is rebuilt). */
    public static void forgetBundle(String dimension, Pos bundle) {
        if (!tracking) {
            return;
        }
        for (Map.Entry<String, Entry> entry : new ArrayList<>(NETWORKS.entrySet())) {
            NetworkInfo info = entry.getValue().info();
            if (info.dimension().equals(dimension) && info.contains(bundle.x(), bundle.y(), bundle.z())) {
                NETWORKS.remove(entry.getKey());
                TOTALS.remove(entry.getKey());
                fireChanged("removed", info);
            }
        }
    }

    /** A network's leader moved something; called once per resource and tick. */
    public static void transferred(String dimension, PipeType type, Pos leader, String resource, long amount) {
        if (!tracking || amount <= 0) {
            return;
        }
        Totals totals = TOTALS.computeIfAbsent(key(dimension, leader, type), k -> new Totals());
        totals.moved += amount;
        totals.lastResource = resource;
        if (!TRANSFER.isEmpty()) {
            TransferEvent event = new TransferEvent(dimension, type.id(), leader, resource, amount);
            for (Listener<TransferEvent> listener : TRANSFER) {
                listener.accept("transfer", event);
            }
        }
    }

    private static void fireChanged(String kind, NetworkInfo info) {
        if (CHANGED.isEmpty()) {
            return;
        }
        NetworkEvent event = new NetworkEvent(kind, info);
        for (Listener<NetworkEvent> listener : CHANGED) {
            listener.accept("networkChanged", event);
        }
    }

    // ---- read by the API ----

    /** The snapshots of the networks that are being ticked now, in no particular order. */
    public static List<NetworkInfo> networks() {
        long now = System.nanoTime();
        List<NetworkInfo> out = new ArrayList<>();
        for (Map.Entry<String, Entry> entry : NETWORKS.entrySet()) {
            if (now - entry.getValue().seenNanos() > STALE_NANOS) {
                NETWORKS.remove(entry.getKey());
                continue;
            }
            out.add(entry.getValue().info());
        }
        return out;
    }

    static void addTransferListener(Consumer<TransferEvent> listener) {
        enable();
        TRANSFER.add(new Listener<>(listener));
    }

    static void addNetworkListener(Consumer<NetworkEvent> listener) {
        enable();
        CHANGED.add(new Listener<>(listener));
    }

    /** Drops every listener, for a script reload. */
    public static void clearListeners() {
        TRANSFER.clear();
        CHANGED.clear();
    }

    /** Forgets everything recorded, for tests. */
    public static void reset() {
        NETWORKS.clear();
        TOTALS.clear();
        clearListeners();
        tracking = false;
    }

    private static String key(String dimension, Pos leader, PipeType type) {
        return dimension + "|" + leader + "|" + type.lane();
    }

    /** The tier a network moves at (its slowest pipe), found from the numbers it was built with; empty for pipes without tiers. */
    static String tierName(PipeType type, long amountPerTick, int ticksPerOperation) {
        if (!PipeTiers.isTiered(type) && type != PipeType.STORAGE) {
            return "";
        }
        for (Tier tier : Tier.values()) {
            boolean match = switch (type) {
                case ITEM, STORAGE -> PipeTiers.ticksPerOperation(tier) == ticksPerOperation;
                default -> PipeTiers.amountPerTick(type, tier) == amountPerTick;
            };
            if (match) {
                return tier.name().toLowerCase(Locale.ROOT);
            }
        }
        return "";
    }
}
