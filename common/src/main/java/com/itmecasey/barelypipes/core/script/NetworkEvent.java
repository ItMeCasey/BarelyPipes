package com.itmecasey.barelypipes.core.script;

/** What a {@code networkChanged} listener receives. */
public final class NetworkEvent {
    private final String kind;
    private final NetworkInfo network;

    NetworkEvent(String kind, NetworkInfo network) {
        this.kind = kind;
        this.network = network;
    }

    /**
     * {@code created} when a network is seen for the first time, {@code changed} when its bundles, machine faces or speed changed,
     * {@code removed} when a bundle in it was broken. A network is identified by its leader bundle, so when the leader changes it
     * is removed and created again.
     */
    public String kind() {
        return kind;
    }

    /** The network after the change (before it, for {@code removed}). */
    public NetworkInfo network() {
        return network;
    }
}
