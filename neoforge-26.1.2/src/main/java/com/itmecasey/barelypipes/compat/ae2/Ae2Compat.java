package com.itmecasey.barelypipes.compat.ae2;

import appeng.api.AECapabilities;
import appeng.api.networking.GridHelper;
import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import com.itmecasey.barelypipes.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jspecify.annotations.Nullable;

/**
 * Everything the rest of BarelyPipes asks of AE2, behind a check that AE2 is installed. Callers only
 * ever hold the link as an {@code Object}, so no AE2 class is loaded when AE2 is absent.
 */
public final class Ae2Compat {
    private static final boolean LOADED = ModList.get().isLoaded("ae2");

    private Ae2Compat() {
    }

    /** Whether AE2 is installed. */
    public static boolean loaded() {
        return LOADED;
    }

    /** Lets AE2 find the grid node of bundles; called from capability registration. */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        if (LOADED) {
            Hooks.registerCapabilities(event);
        }
    }

    /**
     * Brings a bundle's ME link up to date with its pipes: creates it when an ME pipe arrives,
     * drops it when the pipe goes, and otherwise refreshes the faces it is exposed on.
     *
     * @param link the bundle's current link, or {@code null}
     * @return the link to keep, or {@code null}
     */
    public static @Nullable Object sync(ConduitBundleBlockEntity bundle, @Nullable Object link) {
        return LOADED ? Hooks.sync(bundle, link) : null;
    }

    /** Takes a bundle's link out of AE2's grid, for when the block is broken or unloaded. */
    public static void stop(@Nullable Object link) {
        if (LOADED && link != null) {
            Hooks.stop(link);
        }
    }

    /** Whether an ME pipe attaches to the block at {@code pos} on its given side: anything with an AE2 node there. */
    public static boolean attaches(Level level, BlockPos pos, Direction side) {
        return LOADED && Hooks.attaches(level, pos, side);
    }

    /** The part that names AE2 classes, only ever loaded when AE2 is installed. */
    private static final class Hooks {
        static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, ModBlockEntities.CONDUIT_BUNDLE.get(),
                    (bundle, context) -> bundle.meLink() instanceof MeLink link ? link : null);
        }

        static @Nullable Object sync(ConduitBundleBlockEntity bundle, @Nullable Object current) {
            Level level = bundle.getLevel();
            if (level == null || level.isClientSide()) {
                return current;
            }
            MeLink link = (MeLink) current;
            if (link != null && !link.matches(bundle.bundle())) {
                link.stop();
                link = null;
            }
            if (link == null) {
                link = MeLink.forBundle(bundle);
                if (link != null) {
                    link.start(level, bundle.getBlockPos());
                }
                return link;
            }
            link.updateSides();
            return link;
        }

        static void stop(Object link) {
            switch (link) {
                case MeLink pipe -> pipe.stop();
                default -> {
                }
            }
        }

        static boolean attaches(Level level, BlockPos pos, Direction side) {
            return GridHelper.getExposedNode(level, pos, side) != null;
        }
    }
}
