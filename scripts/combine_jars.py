"""Merge the per-loader jars of one Minecraft version into a single combined jar.

Usage: python scripts/combine_jars.py <out.jar> <loader.jar> [<loader.jar> ...]

Each loader jar carries the shared code (common/) plus its own loader layer and metadata file. The shared classes are
byte-identical in every input, so the first copy wins. Loader-specific classes live in their own packages, and each
loader's metadata file (mods.toml for Forge, neoforge.mods.toml for NeoForge) has its own name, so nothing collides.
"""

import sys
import zipfile


def combine(output, inputs):
    seen = set()
    with zipfile.ZipFile(output, "w", compression=zipfile.ZIP_DEFLATED) as out:
        for path in inputs:
            with zipfile.ZipFile(path) as src:
                for info in src.infolist():
                    name = info.filename
                    if info.is_dir() or name.startswith("META-INF/MANIFEST.MF"):
                        continue
                    if name in seen:
                        continue
                    seen.add(name)
                    out.writestr(name, src.read(name))
        out.writestr("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\n\n")
    return len(seen)


def main(argv):
    if len(argv) < 3:
        print(__doc__)
        return 2
    count = combine(argv[1], argv[2:])
    print(f"wrote {argv[1]} with {count} entries")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
