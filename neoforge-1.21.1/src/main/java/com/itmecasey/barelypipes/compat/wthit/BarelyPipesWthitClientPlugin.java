package com.itmecasey.barelypipes.compat.wthit;

import com.itmecasey.barelypipes.compat.look.PipeLookAdapter;
import com.itmecasey.barelypipes.conduit.ConduitBundleBlock;
import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import com.itmecasey.barelypipes.core.conduit.PipeLookInfo;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import net.minecraft.world.phys.BlockHitResult;

/**
 * WTHIT support: looking at a bundle names the exact pipe under the crosshair and lists the same detail lines as the Show Me
 * What This Is overlay and the Jade plugin, all worked out on the client through {@link PipeLookAdapter}. WTHIT finds this
 * class through {@code waila_plugins.json}, and only when WTHIT is installed.
 */
public class BarelyPipesWthitClientPlugin implements IWailaClientPlugin {
    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(Lines.INSTANCE, ConduitBundleBlock.class);
    }

    private enum Lines implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            if (!(accessor.getBlockEntity() instanceof ConduitBundleBlockEntity bundle)
                    || !(net.minecraft.client.Minecraft.getInstance().hitResult instanceof BlockHitResult hit)
                    || !hit.getBlockPos().equals(accessor.getPosition())) {
                return;
            }
            PipeLookInfo.Look look = PipeLookAdapter.look(accessor.getWorld(), accessor.getPosition(), bundle, hit.getLocation());
            if (look == null) {
                return;
            }
            if (look.part() != PipeLookInfo.Part.JUNCTION) {
                tooltip.addLine(PipeLookAdapter.stack(look).getHoverName());
            }
            PipeLookAdapter.lines(look).forEach(tooltip::addLine);
        }
    }
}
