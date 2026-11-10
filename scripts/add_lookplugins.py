"""Adds the Jade and WTHIT look-at plugins to a layer: the plugin classes, waila_plugins.json, the compile-only Modrinth Maven
dependencies and the dev-only -Plookmod=jade|wthit runtime switch in build.gradle. Safe to run again.

Usage: python scripts/add_lookplugins.py <layer> [<layer> ...]
Modrinth version ids looked up 2026-11-09 (Jade has no Forge build after 1.20.1; WTHIT has all ten).
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
# layer -> (jade version id or None, wthit version id, badpackets version id or None)
IDS = {
    "forge-1.20.1": ("xJQHCmWJ", "t9rIojT8", "aXZqpNgH"),
    "neoforge-1.20.1": ("xJQHCmWJ", "t9rIojT8", "dcVcnZ7u"),
    "forge-1.21.1": (None, "cCQhpuXM", "PXf9r02i"),
    "forge-26.1.2": (None, "XIAFKMBC", "DAQaU7UY"),
    "forge-26.2": (None, "xnO4lALy", "XGbJvoTc"),
    "forge-26.3": (None, "Ur29rkeB", "XGbJvoTc"),
    "neoforge-26.1.2": ("Pr1h4azo", "86roxNra", "8PgyQyuN"),
    "neoforge-26.2": ("GBES6etT", "6TB1L4QG", "oPDewtRE"),
    "neoforge-26.3": ("dVB8U7Td", "Id8LEy2N", "oPDewtRE"),
}

JADE = """package com.itmecasey.barelypipes.compat.jade;

import com.itmecasey.barelypipes.Ids;
import com.itmecasey.barelypipes.compat.look.PipeLookAdapter;
import com.itmecasey.barelypipes.conduit.ConduitBundleBlock;
import com.itmecasey.barelypipes.conduit.ConduitBundleBlockEntity;
import com.itmecasey.barelypipes.core.conduit.PipeLookInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
%IMPORT%

/**
 * Jade support: looking at a bundle names the exact pipe under the crosshair (type and tier) and lists the same detail lines
 * as the Show Me What This Is overlay. All of it is worked out on the client from the bundle the client already has, through
 * {@link PipeLookAdapter}. Jade finds this class by its annotation and only when Jade is installed.
 */
@WailaPlugin
public class BarelyPipesJadePlugin implements IWailaPlugin {
    private static final %ID% UID = Ids.mod("pipe_look");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(Tooltip.INSTANCE, ConduitBundleBlock.class);
    }

    private enum Tooltip implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            BlockHitResult hit = accessor.getHitResult();
            if (hit == null || !(accessor.getBlockEntity() instanceof ConduitBundleBlockEntity bundle)) {
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
        public %ID% getUid() {
            return UID;
        }
    }
}
"""

WTHIT = """package com.itmecasey.barelypipes.compat.wthit;

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
            BlockHitResult hit = accessor.getBlockHitResult();
            if (hit == null || !(accessor.getBlockEntity() instanceof ConduitBundleBlockEntity bundle)) {
                return;
            }
            PipeLookInfo.Look look = PipeLookAdapter.look(accessor.getLevel(), accessor.getPosition(), bundle, hit.getLocation());
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
"""

PLUGINS_JSON = """{
  "barelypipes:pipe_look": {
    "entrypoints": {
      "client": "com.itmecasey.barelypipes.compat.wthit.BarelyPipesWthitClientPlugin"
    },
    "side": "client",
    "required": []
  }
}
"""

MODRINTH_REPO = """
// Modrinth Maven (compile-only look-at mods). Not exclusive: ForgeGradle serves remapped copies under the same group.
repositories {
    maven {
        name = 'Modrinth (look-at mods)'
        url = 'https://api.modrinth.com/maven'
        content { includeGroup 'maven.modrinth' }
    }
}
"""


def patch(layer):
    folder = ROOT / layer
    loader, mc = layer.split("-", 1)
    jade, wthit, badpackets = IDS[layer]
    java = folder / "src/main/java/com/itmecasey/barelypipes/compat"
    new26 = mc.startswith("26")
    if jade:
        (java / "jade").mkdir(parents=True, exist_ok=True)
        text = JADE.replace("%ID%", "Identifier" if new26 else "ResourceLocation")
        text = text.replace("%IMPORT%", "import net.minecraft.resources." + ("Identifier;" if new26 else "ResourceLocation;"))
        (java / "jade/BarelyPipesJadePlugin.java").write_text(text, encoding="utf-8", newline="\n")
    (java / "wthit").mkdir(parents=True, exist_ok=True)
    (java / "wthit/BarelyPipesWthitClientPlugin.java").write_text(WTHIT, encoding="utf-8", newline="\n")
    res = folder / "src/main/resources"
    res.mkdir(parents=True, exist_ok=True)
    (res / "waila_plugins.json").write_text(PLUGINS_JSON, encoding="utf-8", newline="\n")

    gradle = folder / "build.gradle"
    text = gradle.read_text(encoding="utf-8")
    if "look-at mods" in text or "lookmod" in text:
        return
    text = re.sub(r"dependencies \{\n    compileOnly 'curse\.maven:jade-324717:\d+'\n\}\n", "", text)
    forge_old = loader == "forge" and mc in ("1.20.1", "1.21.1")
    mod_old = layer == "neoforge-1.20.1"
    compile_only = "modCompileOnly" if mod_old else "compileOnly"
    run_only = "modRuntimeOnly" if mod_old else "runtimeOnly"

    def dep(kind, name, vid):
        coord = f'"maven.modrinth:{name}:{vid}"'
        return f"{kind} fg.deobf({coord})" if forge_old else f"{kind} {coord}"

    lines = []
    if jade:
        lines.append("    " + dep(compile_only, "jade", jade))
    lines.append("    " + dep(compile_only, "wthit", wthit))
    run = []
    if jade:
        run.append(f"""    if (project.property('lookmod') == 'jade') {{
        {dep(run_only, 'jade', jade)}
    }}""")
    run.append(f"""    if (project.property('lookmod') == 'wthit') {{
        {dep(run_only, 'wthit', wthit)}
        {dep(run_only, 'badpackets', badpackets)}
    }}""")
    block = (MODRINTH_REPO if (loader == "forge" and mc != "1.20.1") else "") + f"""
// Jade and WTHIT (optional look-at overlays): compile-only; -PdevTools -Plookmod=jade|wthit runs one of them in the dev client.
dependencies {{
{chr(10).join(lines)}
}}
if (project.hasProperty('devTools') && project.hasProperty('lookmod')) {{
    dependencies {{
{chr(10).join(run)}
    }}
}}
"""
    gradle.write_text(text.rstrip("\n") + "\n" + block, encoding="utf-8", newline="\n")
    print("patched", layer)


if __name__ == "__main__":
    for layer in sys.argv[1:]:
        patch(layer)
