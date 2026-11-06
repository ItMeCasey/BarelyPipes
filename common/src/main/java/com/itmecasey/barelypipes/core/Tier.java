package com.itmecasey.barelypipes.core;

import java.util.Locale;

/**
 * The three pipe tiers: iron, gold and diamond. Their textures are numbered 1 to 3 (the {@code _t1}
 * to {@code _t3} suffix) and their colors are the ones the pipe textures are drawn with.
 */
public enum Tier {
    IRON(0xC8C8C8),
    GOLD(0xCFA000),
    DIAMOND(0x2A8FB5);

    private final int color;

    Tier(int color) {
        this.color = color;
    }

    /** RGB color (no alpha) that identifies this tier in the UI. */
    public int color() {
        return color;
    }

    /** One-based tier number, 1 for iron through 3 for diamond; also the texture suffix. */
    public int level() {
        return ordinal() + 1;
    }

    /** Lowercase name used in registry ids and translation keys, such as {@code "iron"}. */
    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Texture suffix such as {@code "t2"}. */
    public String suffix() {
        return "t" + level();
    }
}
