package com.itmecasey.barelypipes.core.conduit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.itmecasey.barelypipes.core.Tier;
import com.itmecasey.barelypipes.core.conduit.PipeLookInfo.Look;
import com.itmecasey.barelypipes.core.conduit.PipeLookInfo.Part;
import com.itmecasey.barelypipes.core.conduit.PipeLookInfo.Text;
import java.util.List;
import org.junit.jupiter.api.Test;

class PipeLookInfoTest {
    private static final PipeLookInfo.Environment ENV = new PipeLookInfo.Environment() {
        @Override
        public Text pipeName(PipeType type, Tier tier) {
            return Text.of("pipe", type.id(), tier.id());
        }

        @Override
        public Text neighbor(Face face) {
            return Text.of("block.minecraft.chest");
        }

        @Override
        public Text rate(PipeType type, Tier tier, int speed, int capacity) {
            return Text.of("rate", type.id(), tier.id(), speed, capacity);
        }
    };

    /** A gold item pipe and an iron fluid pipe, both open to a machine on top. */
    private static BundleState bundle() {
        BundleState bundle = new BundleState();
        bundle.addPipe(PipeType.ITEM, Tier.GOLD);
        bundle.addPipe(PipeType.FLUID, Tier.IRON);
        bundle.setConnection(PipeType.ITEM.lane(), Face.UP, true, true);
        bundle.setConnection(PipeType.FLUID.lane(), Face.UP, true, true);
        return bundle;
    }

    private static double[] center(Box box) {
        return new double[] {(box.minX() + box.maxX()) / 2, (box.minY() + box.maxY()) / 2, (box.minZ() + box.maxZ()) / 2};
    }

    /** The inner end of an arm that points up, below the plug that sits on its outer end. */
    private static Box armBody(BundleState bundle, PipeType type) {
        Box arm = ConduitLayout.of(bundle).armOf(type, Face.UP);
        return new Box(arm.minX(), arm.minY(), arm.minZ(), arm.maxX(), arm.minY() + 1.0, arm.maxZ());
    }

    private static Look at(BundleState bundle, EndpointSettings endpoints, Box box) {
        double[] c = center(box);
        return PipeLookInfo.of(bundle, endpoints, c[0], c[1], c[2], ENV);
    }

    @Test
    void anArmNamesItsPipeSideModeAndMachine() {
        BundleState bundle = bundle();
        Look look = at(bundle, new EndpointSettings(), armBody(bundle, PipeType.ITEM));
        assertEquals(PipeType.ITEM, look.type());
        assertEquals(Tier.GOLD, look.tier());
        assertEquals(Part.ARM, look.part());
        assertEquals(Face.UP, look.face());
        assertEquals(Text.of("smwti.barelypipes.arm", Text.of("face.barelypipes.up")), look.lines().get(0));
        assertEquals(Text.of("smwti.barelypipes.mode", Text.of("connection_mode.barelypipes.open")), look.lines().get(1));
        assertEquals(Text.of("smwti.barelypipes.connected_to", Text.of("block.minecraft.chest")), look.lines().get(2));
        assertEquals(Text.of("smwti.barelypipes.throughput", Text.of("rate", "item", "gold", 0, 0)), look.lines().get(3));
    }

    @Test
    void theOtherPipeOnTheSameFaceIsToldApart() {
        BundleState bundle = bundle();
        Look look = at(bundle, new EndpointSettings(), armBody(bundle, PipeType.FLUID));
        assertEquals(PipeType.FLUID, look.type());
        assertEquals(Tier.IRON, look.tier());
    }

    @Test
    void aPlugIsAPlugNotAnArm() {
        BundleState bundle = bundle();
        Box plug = ConduitLayout.of(bundle).pieces().stream()
                .filter(p -> p.kind() == ConduitLayout.Kind.PLUG && p.type() == PipeType.ITEM).findFirst().orElseThrow().box();
        Look look = at(bundle, new EndpointSettings(), plug);
        assertEquals(PipeType.ITEM, look.type());
        assertEquals(Part.PLUG, look.part());
        assertEquals(Face.UP, look.face());
        assertEquals("smwti.barelypipes.plug", look.lines().get(0).key());
    }

    @Test
    void theModeAndTheNeighborFollowTheBundle() {
        BundleState bundle = bundle();
        bundle.setMode(PipeType.ITEM.lane(), Face.UP, ConnectionMode.IMPORT);
        Look look = at(bundle, new EndpointSettings(), armBody(bundle, PipeType.ITEM));
        assertEquals(Text.of("smwti.barelypipes.mode", Text.of("connection_mode.barelypipes.import")), look.lines().get(1));

        bundle.setConnection(PipeType.ITEM.lane(), Face.NORTH, true, false);
        look = at(bundle, new EndpointSettings(), ConduitLayout.of(bundle).armOf(PipeType.ITEM, Face.NORTH));
        assertEquals(Text.of("smwti.barelypipes.connected_to", Text.of("smwti.barelypipes.another_bundle")), look.lines().get(2));
    }

    @Test
    void upgradesFilterAndPriorityAreListedForTheFace() {
        BundleState bundle = bundle();
        int lane = PipeType.ITEM.lane();
        bundle.addUpgrade(lane, PipeUpgrades.Kind.SPEED);
        bundle.addUpgrade(lane, PipeUpgrades.Kind.SPEED);
        bundle.addUpgrade(lane, PipeUpgrades.Kind.CAPACITY);
        EndpointSettings endpoints = new EndpointSettings();
        endpoints.install(lane, Face.UP, EndpointSettings.Upgrade.FILTER);
        endpoints.filter(lane, Face.UP).setEntry(0, "minecraft:stone");
        endpoints.filter(lane, Face.UP).setEntry(3, "minecraft:dirt");
        endpoints.filter(lane, Face.UP).setWhitelist(false);
        endpoints.install(lane, Face.UP, EndpointSettings.Upgrade.PRIORITY);
        endpoints.setPriority(lane, Face.UP, 3);

        List<Text> lines = at(bundle, endpoints, armBody(bundle, PipeType.ITEM)).lines();
        assertTrue(lines.contains(Text.of("smwti.barelypipes.throughput", Text.of("rate", "item", "gold", 2, 1))));
        assertTrue(lines.contains(Text.of("tooltip.barelypipes.look.upgrades", 2, 1)));
        assertTrue(lines.contains(Text.of("smwti.barelypipes.filter_blacklist", 2)));
        assertTrue(lines.contains(Text.of("smwti.barelypipes.priority", "+3")));

        // Nothing is installed at the other pipe's face.
        List<Text> fluid = at(bundle, endpoints, armBody(bundle, PipeType.FLUID)).lines();
        assertTrue(fluid.stream().noneMatch(t -> t.key().contains("filter") || t.key().contains("priority")));
    }

    @Test
    void anEmptyWhitelistSaysSo() {
        BundleState bundle = bundle();
        EndpointSettings endpoints = new EndpointSettings();
        endpoints.install(PipeType.ITEM.lane(), Face.UP, EndpointSettings.Upgrade.FILTER);
        List<Text> lines = at(bundle, endpoints, armBody(bundle, PipeType.ITEM)).lines();
        assertTrue(lines.contains(Text.of("smwti.barelypipes.filter_whitelist_empty")));
    }

    @Test
    void aCoreListsItsSidesAndTheOtherPipes() {
        BundleState bundle = new BundleState();
        bundle.addPipe(PipeType.ENERGY, Tier.DIAMOND);
        bundle.addPipe(PipeType.REDSTONE);
        ConduitLayout layout = ConduitLayout.of(bundle);
        Box core = layout.pieces().stream()
                .filter(p -> p.kind() == ConduitLayout.Kind.CORE && p.type() == PipeType.ENERGY).findFirst().orElseThrow().box();
        Look look = at(bundle, new EndpointSettings(), core);
        assertEquals(PipeType.ENERGY, look.type());
        assertEquals(Tier.DIAMOND, look.tier());
        assertEquals(Part.CORE, look.part());
        assertNull(look.face());
        assertEquals(Text.of("smwti.barelypipes.core"), look.lines().get(0));
        assertEquals(Text.of("smwti.barelypipes.no_sides"), look.lines().get(1));
        assertTrue(look.lines().contains(Text.of("smwti.barelypipes.others", List.of(Text.of("pipe", "redstone", "iron")))));
    }

    @Test
    void aCoreWithArmsListsEachConnectedSide() {
        BundleState bundle = new BundleState();
        bundle.addPipe(PipeType.ENERGY, Tier.IRON);
        bundle.setConnection(PipeType.ENERGY.lane(), Face.WEST, true, true);
        bundle.setConnection(PipeType.ENERGY.lane(), Face.EAST, true, true);
        bundle.setMode(PipeType.ENERGY.lane(), Face.EAST, ConnectionMode.EXPORT);
        Box core = ConduitLayout.of(bundle).pieces().stream()
                .filter(p -> p.kind() == ConduitLayout.Kind.CORE).findFirst().orElseThrow().box();
        Look look = at(bundle, new EndpointSettings(), core);
        assertEquals(Part.CORE, look.part());
        Text sides = look.lines().get(1);
        assertEquals("smwti.barelypipes.sides", sides.key());
        assertEquals(List.of(
                Text.of("smwti.barelypipes.side_mode", Text.of("face.barelypipes.west"), Text.of("connection_mode.barelypipes.open")),
                Text.of("smwti.barelypipes.side_mode", Text.of("face.barelypipes.east"), Text.of("connection_mode.barelypipes.export"))),
                sides.args().get(0));
    }

    @Test
    void thePipeIsTheOneTheWrenchWouldPick() {
        BundleState bundle = bundle();
        bundle.addPipe(PipeType.REDSTONE);
        bundle.setConnection(PipeType.REDSTONE.lane(), Face.SOUTH, true, true);
        ConduitLayout layout = ConduitLayout.of(bundle);
        for (Box box : layout.hitBoxes()) {
            double[] c = center(box);
            Look look = PipeLookInfo.of(bundle, new EndpointSettings(), c[0], c[1] + 0.2, c[2], ENV);
            assertNotNull(look);
            assertEquals(layout.nearestPipe(c[0], c[1] + 0.2, c[2]), look.type());
        }
    }

    @Test
    void anEmptyBundleHasNoLook() {
        assertNull(PipeLookInfo.of(new BundleState(), new EndpointSettings(), 8, 8, 8, ENV));
    }

    @Test
    void aStoragePipeIsRatedAtItsFixedTier() {
        BundleState bundle = new BundleState();
        bundle.addPipe(PipeType.STORAGE);
        Look look = PipeLookInfo.of(bundle, new EndpointSettings(), 8, 8, 8, ENV);
        assertEquals(PipeTiers.STORAGE_TIER, look.tier());
        assertFalse(look.lines().stream().anyMatch(t -> t.key().equals("tooltip.barelypipes.look.upgrades")));
    }
}
