package com.itmecasey.barelypipes.conduit.transfer;

import com.itmecasey.barelystorage.ItemKey;
import com.itmecasey.barelystorage.Keys;
import com.itmecasey.barelystorage.NetNode;
import com.itmecasey.barelystorage.StorageNetwork;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import org.jspecify.annotations.Nullable;

/**
 * The storage pipe's link to BarelyStorage. BarelyStorage is optional, so everything that names its
 * classes sits in {@link Hooks}, which the game loads only when the mod is installed.
 */
public final class StorageLink {
    private static final boolean LOADED = ModList.get() != null && ModList.get().isLoaded("barelystorage");

    private StorageLink() {
    }

    /** Whether BarelyStorage is installed. */
    public static boolean loaded() {
        return LOADED;
    }

    /** Whether the block at {@code pos} is a BarelyStorage network block that joins on its given side. */
    public static boolean isNode(Level level, BlockPos pos, Direction side) {
        return LOADED && Hooks.isNode(level.getBlockState(pos), side);
    }

    static StorageMover.Port port(Level level, NetworkEndpoint endpoint) {
        return LOADED ? new Hooks.NetworkPort(level, endpoint) : null;
    }

    /** Only loaded when BarelyStorage is present. */
    private static final class Hooks {
        static boolean isNode(BlockState state, Direction side) {
            return state.getBlock() instanceof NetNode node && node.connects(state, side);
        }

        /** A face that ends at a network block: the whole network behind it is one big inventory. */
        private record NetworkPort(Level level, NetworkEndpoint endpoint) implements StorageMover.Port {
            private StorageNetwork network() {
                return StorageNetwork.find(level, endpoint.target());
            }

            @Override
            public ItemStack peek(int batch) {
                StorageNetwork network = network();
                if (!network.online()) {
                    return ItemStack.EMPTY;
                }
                for (Map.Entry<ItemKey, Long> entry : network.pool().view().entrySet()) {
                    if (entry.getValue() <= 0 || !endpoint.allows(entry.getKey().id())) {
                        continue;
                    }
                    int want = (int) Math.min(Math.min(batch, entry.getValue()), Keys.maxStack(entry.getKey()));
                    long can = network.pool().extract(entry.getKey(), want, true);
                    if (can > 0) {
                        return Keys.stack(entry.getKey(), (int) can, network.registries());
                    }
                }
                return ItemStack.EMPTY;
            }

            @Override
            public ItemStack take(ItemStack wanted) {
                StorageNetwork network = network();
                return network.extract(Keys.of(wanted, network.registries()), wanted.getCount());
            }

            @Override
            public int accept(ItemStack stack, boolean simulate) {
                StorageNetwork network = network();
                if (!network.online() || stack.isEmpty()) {
                    return 0;
                }
                if (simulate) {
                    return (int) network.pool().insert(Keys.of(stack, network.registries()), stack.getCount(), true);
                }
                return stack.getCount() - network.insert(stack).getCount();
            }
        }
    }
}
