"""Seed one BarelyPipes loader module from the matching AeroFlux module (conduit code only).

Usage: python scripts/port_aeroflux.py <mc> <forge|neoforge>
Copies the pipe packages, renames aeroflux -> barelypipes, and leaves registry/main-class trimming to hand edits.
Only used while bootstrapping; the copied sources are owned by this repo afterwards.
"""
import os, re, shutil, sys

AF = r"C:\Users\casey\Desktop\AeroFlux\versions"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
KEEP = [
    "conduit", "save", "protect", "world/OutlineParticles.java", "Ids.java",
    "client/model", "client/ClientSetup.java", "client/screen/PipeConfigScreen.java",
    "client/screen/widget", "compat/ae2", "compat/mekanism", "registry", "item/UpgradeCardItem.java",
    "AeroFlux.java", "AeroFluxConfig.java", "AeroFluxClientConfig.java",
]

def rename(text):
    text = text.replace("com.itmecasey.aeroflux", "com.itmecasey.barelypipes")
    text = text.replace("AeroFluxClientConfig", "BarelyPipesClientConfig").replace("AeroFluxConfig", "BarelyPipesConfig")
    text = text.replace("AeroFlux", "BarelyPipes").replace("aeroflux", "barelypipes")
    return text

def main(mc, loader):
    src = os.path.join(AF, f"mc{mc}", loader, "src", "main", "java", "com", "itmecasey", "aeroflux")
    dst = os.path.join(ROOT, f"{loader}-{mc}", "src", "main", "java", "com", "itmecasey", "barelypipes")
    for k in KEEP:
        p = os.path.join(src, k)
        if not os.path.exists(p):
            print("missing", k); continue
        files = [p] if os.path.isfile(p) else [os.path.join(d, f) for d, _, fs in os.walk(p) for f in fs]
        for f in files:
            rel = os.path.relpath(f, src)
            out = os.path.join(dst, rename(rel.replace("AeroFlux", "BarelyPipes")))
            os.makedirs(os.path.dirname(out), exist_ok=True)
            with open(f, encoding="utf-8") as fh:
                data = rename(fh.read())
            with open(out, "w", encoding="utf-8", newline="\n") as fh:
                fh.write(data)
    print("done", dst)

if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
