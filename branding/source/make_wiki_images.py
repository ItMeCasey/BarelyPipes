"""Writes the images the Barely Pipes wiki uses: pipe item icons made from the generated pipe textures, the tool and
upgrade icons, the logo and the branding banners.

Run from the repository root after make_icon.py and render_banners.py:
    python branding/source/make_wiki_images.py [<wiki folder>]
The default wiki folder is the barelypipes folder of the site repo. Vanilla item pictures are copied from the AeroFlux wiki.
"""
import shutil
import sys
from pathlib import Path

from PIL import Image

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent.parent
ART = HERE / "art"
SITE = Path(r"G:\Other computers\My Computer\Projects\ItMeCasey\website\d-minimal\wiki")
OUT = Path(sys.argv[1]) if len(sys.argv) > 1 else SITE / "barelypipes"
AF_VANILLA = SITE / "aeroflux" / "images" / "vanilla"

TIERS = ["iron", "gold", "diamond"]
TIERED = ["fluid", "item", "energy", "gas"]
UNTIERED = ["redstone", "me", "dense_me", "storage"]


def pipe_icon(kind, tier):
    suffix = f"_t{tier}" if tier else ""
    arm = Image.open(ART / f"pipe_{kind}_arm_h{suffix}.png").convert("RGBA").crop((0, 0, 64, 16))
    core = Image.open(ART / f"pipe_{kind}_core_h{suffix}.png").convert("RGBA").resize((24, 24), Image.NEAREST)
    img = Image.new("RGBA", (64, 24), (0, 0, 0, 0))
    img.paste(arm, (0, 4))
    img.paste(core, (20, 0))
    return img


(OUT / "images" / "pipe").mkdir(parents=True, exist_ok=True)
for kind in TIERED:
    for i, tier in enumerate(TIERS, 1):
        pipe_icon(kind, i).save(OUT / "images" / "pipe" / f"{tier}_{kind}_pipe.png")
for kind in UNTIERED:
    pipe_icon(kind, 0).save(OUT / "images" / "pipe" / f"{kind}_pipe.png")

(OUT / "images" / "item").mkdir(exist_ok=True)
for name in ("pipe_wrench", "conduit_tool", "speed_upgrade", "capacity_upgrade", "filter_upgrade", "priority_upgrade"):
    shutil.copyfile(ART / f"{name}.png", OUT / "images" / "item" / f"{name}.png")

(OUT / "images" / "vanilla").mkdir(exist_ok=True)
for name in ("iron_ingot", "gold_ingot", "diamond", "redstone", "glass", "copper_ingot", "amethyst_shard", "paper", "comparator",
             "quartz", "glowstone_dust", "hopper"):
    shutil.copyfile(AF_VANILLA / f"{name}.png", OUT / "images" / "vanilla" / f"{name}.png")

shutil.copyfile(ROOT / "branding" / "icon" / "icon_256.png", OUT / "images" / "icon.png")
shutil.copyfile(ROOT / "branding" / "banners" / "banner.png", OUT / "images" / "header.png")
shutil.copyfile(ROOT / "branding" / "banners" / "features.png", OUT / "images" / "features.png")
shutil.copyfile(ROOT / "branding" / "banners" / "tiers.png", OUT / "images" / "tiers.png")
print("wrote", OUT)
