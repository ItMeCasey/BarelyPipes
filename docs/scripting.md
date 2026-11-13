# Scripting with ModJS

With ModJS installed, server scripts get a global `BarelyPipes`. ModJS is not required: without it nothing is loaded and nothing is
recorded. Client scripts get no `BarelyPipes` global, because pipe networks exist on the server only.

The API watches. It never changes a pipe, a network or a machine. Pipe recipes are ordinary data pack recipes (ModJS's recipe
events change them); the speeds are fixed in the mod and can be read with `tiers()`.

## Events

| Call | Listener gets |
|---|---|
| `onTransfer(fn)` | `type()` (`item`, `fluid`, `energy`, `gas`, `storage`), `dimension()`, `leader()` (the bundle that did the work), `resource()` (item, fluid or gas id; `energy` for energy), `amount()` (items, mB or FE moved in that tick), `network()`. Fired once per network, resource and tick. |
| `onNetworkChanged(fn)` | `kind()` (`created`, `changed`, `removed`), `network()`. A network is identified by its leader bundle: when the leader changes, the old one is `removed` and a new one `created`. A rebuild that changes nothing is silent. |
| `clearListeners()` | Drops every listener; call it at the top of a script that is reloaded. |

A listener that throws is logged once and skipped from then on; it never stops the game or the other listeners.

## Reading

| Call | Returns |
|---|---|
| `networks()`, `networks(type)` | The networks being ticked now (loaded chunks), as snapshots. |
| `networkAt(dimension, x, y, z, type)` | The network of that type containing the bundle at that position, or `null`. |
| `types()` | The pipe type ids. |
| `tiers()`, `tier(type, tier)` | The speed table: `amountPerTick()` (mB or FE per tick), `itemsPerOperation()`, `ticksPerOperation()`. |

A network snapshot has `dimension()`, `type()`, `tier()` (the slowest pipe in it: `iron`, `gold`, `diamond`; empty for pipes without
tiers), `leader()`, `bundleCount()`, `bundles()`, `contains(x, y, z)`, `endpoints()`, `sources()` (faces the network takes from),
`destinations()` (faces it gives to), `amountPerTick()`, `ticksPerOperation()`, `itemsPerOperation()`, `truncated()`, `moved()` (items,
mB or FE moved since it was first seen) and `lastResource()`. A position has `x()`, `y()`, `z()`. An endpoint has `bundle()`, `face()`,
`machine()`, `mode()` (`import`, `export`, `open`), `pulls()`, `pushes()` and `priority()`.

Nothing is recorded until a script uses the API (or ModJS is installed), so a game without scripts pays for one boolean check per
network rebuild and per tick. Snapshots are rebuilt every 10 ticks for loaded networks; a network that is no longer ticking drops
out of `networks()` after a minute.

## Example

```js
// server_scripts/pipes.js
BarelyPipes.clearListeners()

// log what the big item networks carry
BarelyPipes.onTransfer(e => {
  if (e.type() == 'item' && e.amount() >= 16) {
    console.log(e.resource() + ' x' + e.amount() + ' through ' + e.leader())
  }
})

// watch networks appear and break
BarelyPipes.onNetworkChanged(e => {
  const n = e.network()
  console.log(e.kind() + ': ' + n.tier() + ' ' + n.type() + ' network, ' + n.bundleCount() + ' bundles, '
    + n.sources().length + ' sources and ' + n.destinations().length + ' destinations')
})

// the speed table
BarelyPipes.tiers().forEach(t => console.log(t.type() + ' ' + t.tier() + ': ' + t.amountPerTick() + ' per tick'))
```

## For mod authors

The API is `com.itmecasey.barelypipes.core.script.PipeScripts.API`; the bridge that exposes it to ModJS is `ModJsBridge` and goes
through reflection. The transfer code reports to `ScriptHub` with plain strings and numbers (`ScriptHub.publish`, `transferred`,
`forgetBundle`), so the shared code has no Minecraft classes.
