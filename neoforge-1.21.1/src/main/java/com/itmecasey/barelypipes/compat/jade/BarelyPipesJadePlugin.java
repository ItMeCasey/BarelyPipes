package com.itmecasey.barelypipes.compat.jade;

import com.itmecasey.barelypipes.BarelyPipes;
import com.itmecasey.barelypipes.compat.look.PipeLookAdapter;
import com.itmecasey.barelypipes.conduit.ConduitBundleBlock;
import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import com.itmecasey.barelypipes.core.conduit.PipeLookInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade support: looking at a bundle names the exact pipe under the crosshair (type and tier) and lists the same detail lines
 * as the Show Me What This Is overlay. All of it is worked out on the client from the bundle the client already has, through
 * {@link PipeLookAdapter}. Jade finds this class by its annotation and only when Jade is installed.
 */
@WailaPlugin
public class BarelyPipesJadePlugin implements IWailaPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(BarelyPipes.MODID, "pipe_look");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(Tooltip.INSTANCE, ConduitBundleBlock.class);
    }

    private enum Tooltip implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!(accessor.getBlockEntity() instanceof ConduitBundleBlockEntity bundle)
                    || !(accessor.getHitResult() instanceof BlockHitResult hit)) {
                return;
            }
            PipeLookInfo.Look look = PipeLookAdapter.look(accessor.getLevel(), accessor.getPosition(), bundle, hit.getLocation());
            if (look == null) {
                return;
            }
            if (look.part() != PipeLookInfo.Part.JUNCTION) {
                tooltip.add(Component.empty().append(PipeLookAdapter.stack(look).getHoverName()));
            }
            PipeLookAdapter.lines(look).forEach(tooltip::add);
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }
}
