package com.itmecasey.barelypipes.registry;

import com.itmecasey.barelypipes.BarelyPipes;
import com.itmecasey.barelypipes.conduit.EndpointUpgradeItem;
import com.itmecasey.barelypipes.conduit.FluxWrenchItem;
import com.itmecasey.barelypipes.conduit.PipeItem;
import com.itmecasey.barelypipes.conduit.UpgradeItem;
import com.itmecasey.barelypipes.conduit.tool.ConduitToolItem;
import com.itmecasey.barelypipes.core.Tier;
import com.itmecasey.barelypipes.core.conduit.EndpointSettings;
import com.itmecasey.barelypipes.core.conduit.PipeTiers;
import com.itmecasey.barelypipes.core.conduit.PipeType;
import com.itmecasey.barelypipes.core.conduit.PipeUpgrades;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Every item Barely Pipes adds. The bundle block has no item: pipes are placed into it. */
public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BarelyPipes.MODID);

    /** One item per pipe type and tier. The untiered ME pipes have a single entry, under iron. */
    private static final Map<PipeType, Map<Tier, DeferredItem<PipeItem>>> PIPES = new EnumMap<>(PipeType.class);

    /** Installs into the pipe nearest the crosshair; makes it move faster. */
    public static final DeferredItem<UpgradeItem> SPEED_UPGRADE = ITEMS.registerItem("speed_upgrade",
            properties -> new UpgradeItem(PipeUpgrades.Kind.SPEED, properties));

    /** Installs into the pipe nearest the crosshair; makes it move more at a time. */
    public static final DeferredItem<UpgradeItem> CAPACITY_UPGRADE = ITEMS.registerItem("capacity_upgrade",
            properties -> new UpgradeItem(PipeUpgrades.Kind.CAPACITY, properties));

    /** Installs into one pipe at one face; only lets chosen items or fluids through that face. */
    public static final DeferredItem<EndpointUpgradeItem> FILTER_UPGRADE = ITEMS.registerItem("filter_upgrade",
            properties -> new EndpointUpgradeItem(EndpointSettings.Upgrade.FILTER, properties));

    /** Installs into one pipe at one face; faces with a higher priority are filled first. */
    public static final DeferredItem<EndpointUpgradeItem> PRIORITY_UPGRADE = ITEMS.registerItem("priority_upgrade",
            properties -> new EndpointUpgradeItem(EndpointSettings.Upgrade.PRIORITY, properties));

    /** Cycles the mode of the pipe nearest the crosshair at the clicked face of a bundle. */
    public static final DeferredItem<FluxWrenchItem> FLUX_WRENCH = ITEMS.registerItem("pipe_wrench",
            FluxWrenchItem::new, new Item.Properties().stacksTo(1));

    /** Edits, removes, copies and inspects pipes; its mode is kept on the stack. */
    public static final DeferredItem<ConduitToolItem> CONDUIT_TOOL = ITEMS.registerItem("conduit_tool",
            ConduitToolItem::new, new Item.Properties().stacksTo(1));

    static {
        for (PipeType type : PipeType.values()) {
            Map<Tier, DeferredItem<PipeItem>> byTier = new EnumMap<>(Tier.class);
            if (PipeTiers.isTiered(type)) {
                for (Tier tier : Tier.values()) {
                    byTier.put(tier, registerPipe(tier.id() + "_" + type.id() + "_pipe", type, tier));
                }
            } else {
                byTier.put(Tier.IRON, registerPipe(type.id() + "_pipe", type, Tier.IRON));
            }
            PIPES.put(type, byTier);
        }
    }

    private ModItems() {
    }

    private static DeferredItem<PipeItem> registerPipe(String id, PipeType type, Tier tier) {
        return ITEMS.registerItem(id, properties -> new PipeItem(type, tier, properties));
    }

    /**
     * The item that places one kind of pipe at one tier. The tier is ignored for ME and dense ME
     * pipes, which do not come in tiers.
     */
    public static DeferredItem<PipeItem> pipe(PipeType type, Tier tier) {
        Map<Tier, DeferredItem<PipeItem>> byTier = PIPES.get(type);
        return PipeTiers.isTiered(type) ? byTier.get(tier) : byTier.get(Tier.IRON);
    }

    /** Hooks this registry into the mod event bus; call once from the mod constructor. */
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
