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
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Every item Barely Pipes adds. The bundle block has no item: pipes are placed into it. */
public final class ModItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BarelyPipes.MODID);

    /** One item per pipe type and tier. The untiered ME pipes have a single entry, under iron. */
    private static final Map<PipeType, Map<Tier, RegistryObject<PipeItem>>> PIPES = new EnumMap<>(PipeType.class);

    /** Installs into the pipe nearest the crosshair; makes it move faster. */
    public static final RegistryObject<UpgradeItem> SPEED_UPGRADE = ITEMS.register("speed_upgrade", () -> new UpgradeItem(PipeUpgrades.Kind.SPEED, new Item.Properties().setId(ITEMS.key("speed_upgrade"))));

    /** Installs into the pipe nearest the crosshair; makes it move more at a time. */
    public static final RegistryObject<UpgradeItem> CAPACITY_UPGRADE = ITEMS.register("capacity_upgrade", () -> new UpgradeItem(PipeUpgrades.Kind.CAPACITY, new Item.Properties().setId(ITEMS.key("capacity_upgrade"))));

    /** Installs into one pipe at one face; only lets chosen items or fluids through that face. */
    public static final RegistryObject<EndpointUpgradeItem> FILTER_UPGRADE = ITEMS.register("filter_upgrade", () -> new EndpointUpgradeItem(EndpointSettings.Upgrade.FILTER, new Item.Properties().setId(ITEMS.key("filter_upgrade"))));

    /** Installs into one pipe at one face; faces with a higher priority are filled first. */
    public static final RegistryObject<EndpointUpgradeItem> PRIORITY_UPGRADE = ITEMS.register("priority_upgrade", () -> new EndpointUpgradeItem(EndpointSettings.Upgrade.PRIORITY, new Item.Properties().setId(ITEMS.key("priority_upgrade"))));

    /** Cycles the mode of the pipe nearest the crosshair at the clicked face of a bundle. */
    public static final RegistryObject<FluxWrenchItem> FLUX_WRENCH = ITEMS.register("pipe_wrench", () -> new FluxWrenchItem(new Item.Properties().setId(ITEMS.key("pipe_wrench")).stacksTo(1)));

    /** Edits, removes, copies and inspects pipes; its mode is kept on the stack. */
    public static final RegistryObject<ConduitToolItem> CONDUIT_TOOL = ITEMS.register("conduit_tool", () -> new ConduitToolItem(new Item.Properties().setId(ITEMS.key("conduit_tool")).stacksTo(1)));

    static {
        for (PipeType type : PipeType.values()) {
            Map<Tier, RegistryObject<PipeItem>> byTier = new EnumMap<>(Tier.class);
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

    private static RegistryObject<PipeItem> registerPipe(String id, PipeType type, Tier tier) {
        return ITEMS.register(id, () -> new PipeItem(type, tier, new Item.Properties().setId(ITEMS.key(id))));
    }

    /**
     * The item that places one kind of pipe at one tier. The tier is ignored for ME and dense ME
     * pipes, which do not come in tiers.
     */
    public static RegistryObject<PipeItem> pipe(PipeType type, Tier tier) {
        Map<Tier, RegistryObject<PipeItem>> byTier = PIPES.get(type);
        return PipeTiers.isTiered(type) ? byTier.get(tier) : byTier.get(Tier.IRON);
    }

    /** Hooks this registry into the mod event bus group; call once from the mod constructor. */
    public static void register(BusGroup modBusGroup) {
        ITEMS.register(modBusGroup);
    }
}
