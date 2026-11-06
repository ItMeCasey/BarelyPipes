package com.itmecasey.barelypipes.dev;

import com.itmecasey.barelypipes.BarelyPipes;
import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import com.itmecasey.barelypipes.conduit.transfer.StorageLink;
import com.itmecasey.barelypipes.core.Tier;
import com.itmecasey.barelypipes.core.conduit.ConnectionMode;
import com.itmecasey.barelypipes.core.conduit.Face;
import com.itmecasey.barelypipes.core.conduit.PipeType;
import com.itmecasey.barelypipes.registry.ModBlocks;
import com.itmecasey.barelypipes.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * A hand-play showcase for a person to try the mod: when {@code BARELYPIPES_PLAYGROUND} is set, a persistent flat world gets
 * item, fluid, energy and storage pipe lines, a mixed bundle, signs, and a kit in the player's inventory. It builds once (a
 * marker block records that), so the world can be kept and reopened. Development only; never in a release jar.
 */
public final class Playground {
    public static final String WORLD = "BarelyPipesTest";

    private Playground() {
    }

    public static boolean requested() {
        return System.getenv("BARELYPIPES_PLAYGROUND") != null;
    }

    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().overworld();
        int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0);
        BlockPos marker = new BlockPos(3, ground - 1, 5);
        if (level.getBlockState(marker).is(Blocks.GOLD_BLOCK)) {
            BarelyPipes.LOGGER.info("PLAYGROUND already built");
            return;
        }
        level.getChunk(0, 0);
        level.getChunk(1, 0);
        level.getChunk(0, 1);
        level.getChunk(1, 1);
        build(level, ground);
        level.setBlock(marker, Blocks.GOLD_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
        BarelyPipes.LOGGER.info("PLAYGROUND built at ground y={}", ground);
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.entityTags().contains("barelypipes_kit")) {
            return;
        }
        player.addTag("barelypipes_kit");
        player.getInventory().add(new ItemStack(ModItems.FLUX_WRENCH.get()));
        player.getInventory().add(new ItemStack(ModItems.CONDUIT_TOOL.get()));
        player.getInventory().add(new ItemStack(ModItems.SPEED_UPGRADE.get(), 8));
        player.getInventory().add(new ItemStack(ModItems.CAPACITY_UPGRADE.get(), 8));
        player.getInventory().add(new ItemStack(ModItems.FILTER_UPGRADE.get(), 8));
        player.getInventory().add(new ItemStack(ModItems.PRIORITY_UPGRADE.get(), 8));
        player.getInventory().add(new ItemStack(ModItems.pipe(PipeType.ITEM, Tier.GOLD).get(), 32));
        player.getInventory().add(new ItemStack(ModItems.pipe(PipeType.STORAGE, Tier.IRON).get(), 32));
        player.getInventory().add(new ItemStack(Items.CHEST, 16));
    }

    private static void build(ServerLevel level, int g) {
        int x0 = 4;
        for (BlockPos p : BlockPos.betweenClosed(new BlockPos(-2, g, -2), new BlockPos(30, g + 5, 36))) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        sign(level, new BlockPos(1, g, 4), "Barely Pipes", "test world", "Rows run east", "Wrench: faces");

        Tier[] tiers = Tier.values();
        String[] rates = {"4 per 16 ticks", "8 per 12 ticks", "16 per 8 ticks"};
        for (int i = 0; i < 3; i++) {
            int z = 6 + i * 2;
            lineOfItems(level, g, x0, z, tiers[i]);
            sign(level, new BlockPos(x0 - 2, g, z), tiers[i].id() + " item pipe", rates[i], "", "");
        }
        int fz = 14;
        level.setBlock(new BlockPos(x0, g, fz), DevHooks.TEST_TANK.get().defaultBlockState(), Block.UPDATE_ALL);
        fillTank(level, new BlockPos(x0, g, fz), 80_000);
        for (int i = 1; i <= 5; i++) {
            bundle(level, new BlockPos(x0 + i, g, fz)).addPipe(PipeType.FLUID, Tier.GOLD);
        }
        level.setBlock(new BlockPos(x0 + 6, g, fz), DevHooks.TEST_TANK.get().defaultBlockState(), Block.UPDATE_ALL);
        modes(level, new BlockPos(x0 + 1, g, fz), PipeType.FLUID, Face.WEST, ConnectionMode.IMPORT);
        modes(level, new BlockPos(x0 + 5, g, fz), PipeType.FLUID, Face.EAST, ConnectionMode.EXPORT);
        sign(level, new BlockPos(x0 - 2, g, fz), "gold fluid pipe", "water tank to tank", "(dev blocks, no", "texture)");

        int ez = 18;
        level.setBlock(new BlockPos(x0, g, ez), DevHooks.TEST_TANK.get().defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(new BlockPos(x0, g, ez)) instanceof TestTankBlockEntity tank) {
            tank.energy().set(5_000_000);
        }
        for (int i = 1; i <= 5; i++) {
            bundle(level, new BlockPos(x0 + i, g, ez)).addPipe(PipeType.ENERGY, Tier.DIAMOND);
        }
        level.setBlock(new BlockPos(x0 + 6, g, ez), DevHooks.TEST_TANK.get().defaultBlockState(), Block.UPDATE_ALL);
        modes(level, new BlockPos(x0 + 1, g, ez), PipeType.ENERGY, Face.WEST, ConnectionMode.IMPORT);
        modes(level, new BlockPos(x0 + 5, g, ez), PipeType.ENERGY, Face.EAST, ConnectionMode.EXPORT);
        sign(level, new BlockPos(x0 - 2, g, ez), "diamond energy pipe", "full FE tank to", "an empty one", "");

        int mz = 22;
        level.setBlock(new BlockPos(x0, g, mz), Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        ConduitBundleBlockEntity all = bundle(level, new BlockPos(x0 + 1, g, mz));
        for (PipeType type : PipeType.values()) {
            if (type != PipeType.DENSE_ME) {
                all.addPipe(type, Tier.GOLD);
            }
        }
        sign(level, new BlockPos(x0 - 2, g, mz), "all seven pipes", "in one block", "Wrench a face,", "right-click to open");

        if (StorageLink.loaded()) {
            storage(level, g, x0, 26);
        }
    }

    private static void storage(ServerLevel level, int g, int x0, int z) {
        BlockPos rackPos = new BlockPos(x0, g, z);
        level.setBlock(rackPos, com.itmecasey.barelystorage.ModContent.blocks().get("server_rack").defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(rackPos) instanceof com.itmecasey.barelystorage.block.RackBlockEntity rack) {
            rack.drives.setItem(0, new ItemStack(com.itmecasey.barelystorage.ModContent.item("cartridge_iron")));
            rack.setEnergy(rack.maxEnergy());
        }
        level.setBlock(new BlockPos(x0 + 1, g, z), com.itmecasey.barelystorage.ModContent.blocks().get("rack_bus_cable").defaultBlockState(), Block.UPDATE_ALL);
        ConduitBundleBlockEntity bundle = bundle(level, new BlockPos(x0 + 2, g, z));
        bundle.addPipe(PipeType.STORAGE);
        BlockPos chest = new BlockPos(x0 + 3, g, z);
        level.setBlock(chest, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        bundle.refreshConnections();
        modes(level, bundle.getBlockPos(), PipeType.STORAGE, Face.EAST, ConnectionMode.IMPORT);
        modes(level, bundle.getBlockPos(), PipeType.STORAGE, Face.WEST, ConnectionMode.EXPORT);
        if (level.getBlockEntity(chest) instanceof Container c) {
            c.setItem(0, new ItemStack(Items.COBBLESTONE, 64));
            c.setItem(1, new ItemStack(Items.OAK_LOG, 64));
            c.setItem(2, new ItemStack(Items.IRON_INGOT, 32));
            c.setItem(3, new ItemStack(Items.DIAMOND, 8));
        }
        sign(level, new BlockPos(x0 - 2, g, z), "storage pipe", "chest to rack", "Wrench swaps the", "faces: pull back out");
    }

    private static void lineOfItems(ServerLevel level, int g, int x0, int z, Tier tier) {
        BlockPos source = new BlockPos(x0, g, z);
        level.setBlock(source, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(source) instanceof Container c) {
            for (int i = 0; i < c.getContainerSize(); i++) {
                c.setItem(i, new ItemStack(i % 3 == 0 ? Items.COBBLESTONE : i % 3 == 1 ? Items.OAK_PLANKS : Items.COAL, 64));
            }
        }
        for (int i = 1; i <= 4; i++) {
            bundle(level, new BlockPos(x0 + i, g, z)).addPipe(PipeType.ITEM, tier);
        }
        level.setBlock(new BlockPos(x0 + 5, g, z), Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        modes(level, new BlockPos(x0 + 1, g, z), PipeType.ITEM, Face.WEST, ConnectionMode.IMPORT);
        modes(level, new BlockPos(x0 + 4, g, z), PipeType.ITEM, Face.EAST, ConnectionMode.EXPORT);
    }

    private static ConduitBundleBlockEntity bundle(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, ModBlocks.CONDUIT_BUNDLE.get().defaultBlockState(), Block.UPDATE_ALL);
        return (ConduitBundleBlockEntity) level.getBlockEntity(pos);
    }

    private static void modes(ServerLevel level, BlockPos pos, PipeType type, Face face, ConnectionMode mode) {
        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity entity) {
            entity.refreshConnections();
            entity.bundle().setMode(type.lane(), face, mode);
            entity.changed();
        }
    }

    private static void fillTank(ServerLevel level, BlockPos pos, int amount) {
        if (level.getBlockEntity(pos) instanceof TestTankBlockEntity tank) {
            try (Transaction transaction = Transaction.openRoot()) {
                tank.tank().insert(FluidResource.of(net.minecraft.world.level.material.Fluids.WATER), amount, transaction);
                transaction.commit();
            }
        }
    }

    private static void sign(ServerLevel level, BlockPos pos, String a, String b, String c, String d) {
        BlockState state = Blocks.OAK_SIGN.defaultBlockState().setValue(BlockStateProperties.ROTATION_16, 4);
        level.setBlock(pos, state, Block.UPDATE_ALL);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof SignBlockEntity sign) {
            SignText text = new SignText()
                    .setMessage(0, Component.literal(a)).setMessage(1, Component.literal(b))
                    .setMessage(2, Component.literal(c)).setMessage(3, Component.literal(d));
            sign.setText(text, true);
            sign.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }
}
