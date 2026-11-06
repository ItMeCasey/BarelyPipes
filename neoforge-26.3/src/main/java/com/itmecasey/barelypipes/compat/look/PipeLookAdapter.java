package com.itmecasey.barelypipes.compat.look;

import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import com.itmecasey.barelypipes.conduit.config.PipeConfigActions;
import com.itmecasey.barelypipes.core.Tier;
import com.itmecasey.barelypipes.core.conduit.Face;
import com.itmecasey.barelypipes.core.conduit.PipeLookInfo;
import com.itmecasey.barelypipes.core.conduit.PipeLookInfo.Text;
import com.itmecasey.barelypipes.core.conduit.PipeType;
import com.itmecasey.barelypipes.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Connects {@link PipeLookInfo} (the loader-free text builder) to the game: the names of items and blocks, the pipe screen's
 * throughput helper, and turning its {@link Text} lines into components. Look-at plugins (Show Me What This Is now, Jade
 * or WTHIT later) call {@link #look} and show what comes back. This class names no other mod, so it is safe to load anywhere.
 */
public final class PipeLookAdapter {
    private PipeLookAdapter() {
    }

    /**
     * The pipe and part under a point of a bundle.
     *
     * @param hit where the crosshair hit, in world coordinates
     * @return the look, or {@code null} for an empty bundle
     */
    public static PipeLookInfo.Look look(Level level, BlockPos pos, ConduitBundleBlockEntity entity, Vec3 hit) {
        Vec3 local = hit.subtract(pos.getX(), pos.getY(), pos.getZ()).scale(16.0);
        return PipeLookInfo.of(entity.bundle(), entity.endpoints(), local.x, local.y, local.z, environment(level, pos));
    }

    /** The item of the pipe a look is about: its name and icon are the overlay's title. */
    public static ItemStack stack(PipeLookInfo.Look look) {
        return new ItemStack(ModItems.pipe(look.type(), look.tier()).get());
    }

    /** The detail lines of a look, as components. */
    public static List<Component> lines(PipeLookInfo.Look look) {
        List<Component> lines = new ArrayList<>();
        for (Text text : look.lines()) {
            lines.add(component(text));
        }
        return lines;
    }

    /** Turns a key with arguments into a translatable component; a list argument is joined with commas. */
    public static Component component(Text text) {
        Object[] args = new Object[text.args().size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = argument(text.args().get(i));
        }
        return Component.translatable(text.key(), args);
    }

    private static Object argument(Object arg) {
        if (arg instanceof Text nested) {
            return component(nested);
        }
        if (arg instanceof List<?> list) {
            MutableComponent joined = Component.empty();
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    joined.append(Component.literal(", "));
                }
                joined.append(argument(list.get(i)) instanceof Component c ? c : Component.literal(String.valueOf(list.get(i))));
            }
            return joined;
        }
        return arg;
    }

    private static PipeLookInfo.Environment environment(Level level, BlockPos pos) {
        return new PipeLookInfo.Environment() {
            @Override
            public Text pipeName(PipeType type, Tier tier) {
                Item item = ModItems.pipe(type, tier).get();
                return Text.of(item.getDescriptionId());
            }

            @Override
            public Text neighbor(Face face) {
                BlockState state = level.getBlockState(pos.relative(Direction.from3DDataValue(face.index())));
                return state.isAir() ? null : Text.of(state.getBlock().getDescriptionId());
            }

            @Override
            public Text rate(PipeType type, Tier tier, int speedUpgrades, int capacityUpgrades) {
                PipeConfigActions.Rate rate = PipeConfigActions.rate(type, tier, speedUpgrades, capacityUpgrades);
                String key = "screen.barelypipes.pipe_config.rate_" + rate.kind().name().toLowerCase(Locale.ROOT);
                return rate.kind() == PipeConfigActions.RateKind.ITEMS
                        ? Text.of(key, format(rate.first()), format(rate.second()))
                        : Text.of(key, format(rate.first()));
            }
        };
    }

    /** Thousands separators, the way the pipe screen writes numbers. */
    private static String format(long value) {
        return String.format(Locale.US, "%,d", value);
    }
}
