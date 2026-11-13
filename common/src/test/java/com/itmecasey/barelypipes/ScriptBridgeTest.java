package com.itmecasey.barelypipes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.itmecasey.barelypipes.core.conduit.PipeType;
import com.itmecasey.barelypipes.core.script.ModJsBridge;
import com.itmecasey.barelypipes.core.script.PipeScripts;
import com.itmecasey.barelypipes.core.script.ScriptHub;
import com.itmecasey.modjs.script.ExtraBindings;
import com.itmecasey.modjs.script.ScriptType;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/** Runs real JavaScript (Rhino, the engine ModJS uses) against the PipeScripts API through the reflective ModJS bridge. */
class ScriptBridgeTest {
    private static final String DIM = "minecraft:overworld";
    private static final ScriptHub.Pos LEADER = new ScriptHub.Pos(0, 64, 0);

    @AfterEach
    void tearDown() {
        ScriptHub.reset();
        ExtraBindings.PROVIDERS.clear();
        System.clearProperty("bp.test");
    }

    Object run(String js, ScriptType type) {
        assertTrue(ModJsBridge.install(PipeScripts.API));
        Context cx = Context.enter();
        try {
            ScriptableObject scope = cx.initStandardObjects();
            ExtraBindings.PROVIDERS.get(0).install(type, cx, scope);
            Object result = cx.evaluateString(scope, js, "test.js", 1, null);
            if (result instanceof org.mozilla.javascript.Wrapper wrapper) {
                result = wrapper.unwrap();
            }
            return result instanceof CharSequence text ? text.toString() : result;
        } finally {
            Context.exit();
        }
    }

    Object run(String js) {
        return run(js, ScriptType.SERVER);
    }

    private static void publishItemNetwork(boolean extraMachine) {
        List<ScriptHub.Pos> bundles = List.of(LEADER, new ScriptHub.Pos(1, 64, 0), new ScriptHub.Pos(2, 64, 0));
        List<ScriptHub.Endpoint> endpoints = new java.util.ArrayList<>(List.of(
                new ScriptHub.Endpoint(LEADER, "west", new ScriptHub.Pos(-1, 64, 0), "import", true, false, 0),
                new ScriptHub.Endpoint(new ScriptHub.Pos(2, 64, 0), "east", new ScriptHub.Pos(3, 64, 0), "export", false, true, 2)));
        if (extraMachine) {
            endpoints.add(new ScriptHub.Endpoint(new ScriptHub.Pos(1, 64, 0), "up", new ScriptHub.Pos(1, 65, 0), "open", true, true, 0));
        }
        ScriptHub.publish(DIM, PipeType.ITEM, LEADER, bundles, endpoints, 0, 12, 8, false);
    }

    @Test
    void jsReadsNetworksAndTheSpeedTable() {
        assertTrue(ModJsBridge.install(PipeScripts.API));
        publishItemNetwork(false);
        Object count = run("BarelyPipes.networks().size()");
        assertEquals(1, ((Number) count).intValue());
        assertEquals("gold", run("BarelyPipes.networks('item').get(0).tier()"), "12 ticks per operation is the gold item pipe");
        assertEquals(3, ((Number) run("BarelyPipes.networks('item').get(0).bundleCount()")).intValue());
        assertEquals(1, ((Number) run("BarelyPipes.networks('item').get(0).sources().size()")).intValue());
        assertEquals(1, ((Number) run("BarelyPipes.networks('item').get(0).destinations().size()")).intValue());
        assertEquals("export", run("BarelyPipes.networks()[0].destinations().get(0).mode()"));
        assertEquals(0, ((Number) run("BarelyPipes.networks('fluid').size()")).intValue());
        assertEquals(DIM, run("BarelyPipes.networkAt('" + DIM + "', 1, 64, 0, 'item').dimension()"));
        assertNull(run("BarelyPipes.networkAt('" + DIM + "', 9, 64, 0, 'item')"));
        assertEquals(650, ((Number) run("BarelyPipes.tier('fluid', 'gold').amountPerTick()")).intValue());
        assertEquals(16, ((Number) run("BarelyPipes.tier('item', 'diamond').itemsPerOperation()")).intValue());
        assertEquals(12, ((Number) run("BarelyPipes.tiers().size()")).intValue());
        assertTrue(((Number) run("BarelyPipes.types().size()")).intValue() >= 8);
    }

    @Test
    void jsListensToTransfersAndNetworkChanges() {
        run("BarelyPipes.onTransfer(e => { java.lang.System.setProperty('bp.test', e.type() + ':' + e.resource() + ':' + e.amount()); });"
                + "var kinds = [];"
                + "BarelyPipes.onNetworkChanged(e => { java.lang.System.setProperty('bp.kinds', "
                + "(java.lang.System.getProperty('bp.kinds') || '') + e.kind() + ','); });");
        publishItemNetwork(false);
        ScriptHub.transferred(DIM, PipeType.ITEM, LEADER, "minecraft:cobblestone", 8);
        assertEquals("item:minecraft:cobblestone:8", System.getProperty("bp.test"));
        assertEquals(8, ((Number) run("BarelyPipes.networks('item').get(0).moved()")).intValue());
        assertEquals("minecraft:cobblestone", run("BarelyPipes.networks('item').get(0).lastResource()"));
        // The same network again is silent; a new machine face is a change; a broken bundle removes it.
        publishItemNetwork(false);
        publishItemNetwork(true);
        ScriptHub.forgetBundle(DIM, new ScriptHub.Pos(1, 64, 0));
        assertEquals("created,changed,removed,", System.getProperty("bp.kinds"));
        assertEquals(0, ((Number) run("BarelyPipes.networks().size()")).intValue());
        System.clearProperty("bp.kinds");
    }

    @Test
    void aFailingListenerNeverReachesTheGame() {
        run("BarelyPipes.onTransfer(e => { throw new Error('boom'); });"
                + "BarelyPipes.onTransfer(e => { java.lang.System.setProperty('bp.test', 'second ran'); });");
        publishItemNetwork(false);
        ScriptHub.transferred(DIM, PipeType.ITEM, LEADER, "minecraft:stone", 4);
        ScriptHub.transferred(DIM, PipeType.ITEM, LEADER, "minecraft:stone", 4);
        assertEquals("second ran", System.getProperty("bp.test"), "the broken listener is skipped, the next one still runs");
    }

    @Test
    void clientScriptsGetNoGlobal() {
        assertEquals("undefined", run("typeof BarelyPipes", ScriptType.CLIENT));
        ExtraBindings.PROVIDERS.clear();
        assertEquals("object", run("typeof BarelyPipes", ScriptType.SERVER));
        ExtraBindings.PROVIDERS.clear();
        assertEquals("object", run("typeof BarelyPipes", ScriptType.STARTUP));
    }

    @Test
    void nothingIsRecordedUntilScriptsAreEnabled() {
        ScriptHub.reset();
        assertFalse(ScriptHub.tracking());
        publishItemNetwork(false);
        ScriptHub.transferred(DIM, PipeType.ITEM, LEADER, "minecraft:stone", 4);
        assertEquals(0, ScriptHub.networks().size());
        PipeScripts.API.networks();
        assertTrue(ScriptHub.tracking(), "reading from a script switches recording on");
    }

    @Test
    void clearListenersStopsOldListenersAfterAScriptReload() {
        run("BarelyPipes.onTransfer(e => { java.lang.System.setProperty('bp.test', 'old'); });");
        run("BarelyPipes.clearListeners();");
        ExtraBindings.PROVIDERS.clear();
        publishItemNetwork(false);
        ScriptHub.transferred(DIM, PipeType.ITEM, LEADER, "minecraft:stone", 4);
        assertNull(System.getProperty("bp.test"));
    }
}
