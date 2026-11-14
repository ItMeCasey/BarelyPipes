# Testing notes

What was run for each change, newest first. The CurseForge description never carries these.

## 2026.11.6: no hints, ModJS scripting

- Pipe screen without hints, real client, NeoForge 26.2 (dev tour steps `pipebuild` and `pipescreen`, then hover): the screen opens with glyph buttons only
  (`docs/frames/viewers/neoforge-26.2/pipe_screen_no_hints.png`), and a hover shows only the control's name: `Open` on a face mode button
  (`hints4_hover_mode.png`), `First to last` on a distribution button (`hints4_hover_distribution.png`), `Filter Upgrade` on the filter place
  (`hints4_hover_filter.png`). The old tooltips explained what each control does.
- Why the tour needed two fixes: a pipe screen closes itself when the player is more than a few blocks from the pipe, so the dev pipe is placed
  3 blocks from the player, and the client needs a moment to get the new bundle before the screen opens.
- `NoHintsTest` (unit test) keeps hint keys and instruction phrases out of `lang/en_us.json`; `ScriptBridgeTest` runs real JavaScript (Rhino) against the
  ModJS bridge; the NeoForge 1.21.1 headless self test passes with 242 checks, 7 of them on the script API, with no ModJS installed.
- All ten builds compile.
