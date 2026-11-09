"""Wires the item and recipe viewer check into one or more layers: the viewer file ids in build.gradle, src/dev/.../ViewerTour.java
(from scripts/templates/ViewerTour-<mc>.java.tmpl) and the "viewers" mode of ShotTour. Safe to run again.

Usage: python scripts/add_viewers.py <layer> [<layer> ...]      e.g. neoforge-26.2 forge-1.21.1
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
# curse.maven ids (CurseForge, looked up 2026-11-09 with the curseforge-lookup skill). Only builds that exist are listed.
IDS = {
    "neoforge-26.2": {"jade": "jade-324717:8755115", "jei": "jei-238222:9352384", "rei": "roughly-enough-items-310111:8271756", "bei": "bei-1783910:9469397",
                      "architectury": "architectury-api-419699:9111233", "cloth": "cloth-config-348521:8269700"},
    "neoforge-26.3": {"jade": "jade-324717:9042207", "jei": "jei-238222:9453343", "rei": "roughly-enough-items-310111:9090699", "bei": "bei-1783910:9469393",
                      "architectury": "architectury-api-419699:9111315", "cloth": "cloth-config-348521:8952995"},
    "forge-26.1.2": {"bei": "bei-1783910:9469399"},
    "forge-26.2": {"bei": "bei-1783910:9469395"},
    "forge-26.3": {"bei": "bei-1783910:9469392"},
    "forge-1.21.1": {"jei": "jei-238222:9393325", "bei": "bei-1783910:9469405"},
}
# Added to a Forge 26.x build.gradle that has no viewer block yet (a copy of what the NeoForge layers carry).
BLOCK = """// --- Optional item and recipe viewers (dev only) ---
// -Pviewers=jei,rei,architectury,cloth,bei adds those mods to the dev client (runtimeOnly: nothing ships and nothing is compiled against).
// The ItMeCasey Maven mirror is used when its credentials are set; Cursemaven is the fallback.
repositories {
    def imcUser = System.getenv('IMC_MAVEN_USER') ?: project.findProperty('imcMavenUser')
    def imcPass = System.getenv('IMC_MAVEN_PASSWORD') ?: project.findProperty('imcMavenPassword')
    if (imcUser && imcPass) {
        maven {
            name = 'ItMeCasey mirror'
            url = 'https://itmecasey.com/maven'
            credentials { username = imcUser; password = imcPass }
            content { includeGroup 'curse.maven' }
        }
    }
    maven {
        name = 'Cursemaven (fallback)'
        url = 'https://cursemaven.com'
        content { includeGroup 'curse.maven' }
    }
}

"""


def patch(layer):
    folder = ROOT / layer
    mc = layer.split("-", 1)[1]
    ids = IDS[layer]
    gradle = folder / "build.gradle"
    text = gradle.read_text(encoding="utf-8")
    line = "def viewerFiles = [" + ", ".join(f"{k}: '{v}'" for k, v in ids.items()) + "]"
    text, n = re.subn(r"def viewerFiles = \[.*\]", line, text)
    if n == 0:
        text = text.rstrip(chr(10)) + chr(10) * 2 + BLOCK + line + """
if (project.hasProperty('viewers')) {
    dependencies {
        project.property('viewers').split(',').each { name -> runtimeOnly "curse.maven:${viewerFiles[name.trim()]}" }
    }
}
"""
    gradle.write_text(text, encoding="utf-8")

    dev = folder / "src/dev/java/com/itmecasey/barelypipes/dev"
    (dev / "ViewerTour.java").write_text((ROOT / "scripts/templates" / f"ViewerTour-{mc}.java.tmpl").read_text(encoding="utf-8"), encoding="utf-8")

    shot = dev / "ShotTour.java"
    s = shot.read_text(encoding="utf-8")
    if "planViewers" not in s:
        method = '''    /** The viewer check: a script of steps in the inventory screen (see ViewerTour), then quit. BARELYPIPES_SHOTS=viewers selects it. */
    private static void planViewers() {
        then(0, () -> {
            player().getAbilities().flying = false;
        });
        ViewerTour.plan();
        then(20, () -> {
            LOGGER.info("BARELYPIPES-SHOTS DONE");
            stage = Stage.DONE;
            Minecraft.getInstance().stop();
        });
    }

    private static void planTour() {
        if ("viewers".equals(System.getenv("BARELYPIPES_SHOTS"))) {
            planViewers();
            return;
        }
'''
        assert "    private static void planTour() {\n" in s
        s = s.replace("    private static void planTour() {\n", method, 1)
        shot.write_text(s, encoding="utf-8")
    print("patched", layer)


if __name__ == "__main__":
    for arg in sys.argv[1:]:
        patch(arg)
