package com.itmecasey.barelypipes.compat.smwti;

import com.itmecasey.barelypipes.compat.look.PipeLookAdapter;
import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import com.itmecasey.barelypipes.core.conduit.PipeLookInfo;
import com.itmecasey.smwti.api.BlockView;
import com.itmecasey.smwti.api.ClientRegistration;
import com.itmecasey.smwti.api.TargetOverride;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import javax.annotation.Nullable;

/**
 * The client side of the SMWTI plugin. The overlay's name, icon and lines all come from the same question: which pipe, and
 * which part of it, is under the crosshair right now. Everything the bundle needs (pipes, tiers, modes, upgrades, filters
 * and priorities) is already on the client, so no server round trip is needed.
 */
final class BarelyPipesSmwtiClient {
    private BarelyPipesSmwtiClient() {
    }

    static void register(ClientRegistration registration) {
        registration.overrideBlock(new TargetOverride<BlockView>() {
            @Override
            public @Nullable Component name(BlockView view) {
                PipeLookInfo.Look look = look(view);
                return look == null || look.part() == PipeLookInfo.Part.JUNCTION ? null : PipeLookAdapter.stack(look).getHoverName();
            }

            @Override
            public @Nullable ItemStack icon(BlockView view) {
                PipeLookInfo.Look look = look(view);
                return look == null || look.part() == PipeLookInfo.Part.JUNCTION ? null : PipeLookAdapter.stack(look);
            }
        });
        registration.block((tooltip, view) -> {
            PipeLookInfo.Look look = look(view);
            if (look != null) {
                PipeLookAdapter.lines(look).forEach(tooltip::add);
            }
        });
    }

    /** What the player is aiming at on this bundle, or {@code null} when the view is not a bundle the crosshair is on. */
    private static @Nullable PipeLookInfo.Look look(BlockView view) {
        if (!(view.entity() instanceof ConduitBundleBlockEntity bundle)) {
            return null;
        }
        HitResult hit = Minecraft.getInstance().hitResult;
        if (!(hit instanceof BlockHitResult block) || !block.getBlockPos().equals(view.pos())) {
            return null;
        }
        return PipeLookAdapter.look(view.level(), view.pos(), bundle, block.getLocation());
    }
}
