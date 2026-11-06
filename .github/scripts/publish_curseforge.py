#!/usr/bin/env python3
"""Uploads the Barely Pipes jars to CurseForge: one combined jar per Minecraft version (1.21.1, 26.1.2, 26.2, 26.3) and, for 1.20.1, a separate Forge jar and NeoForge jar.

Usage:
  publish_curseforge.py --jars DIR --version 2026.11.1 --changelog-file notes.md [--release-type beta] [--dry-run]
  publish_curseforge.py --list-versions

DIR holds barelypipes-mc<minecraft>-<version>.jar files, as made by scripts/combine_jars.py, and for 1.20.1
barelypipes-forge-mc1.20.1-<version>.jar and barelypipes-neoforge-mc1.20.1-<version>.jar (Forge and NeoForge share one
metadata file name there, so they cannot be merged). A combined jar carries the Forge and NeoForge layers and is uploaded
once per Minecraft version with both loaders ticked. The upload token
is read from the CURSEFORGE_TOKEN environment variable and is never printed. Only the Python standard library is used.
"""
import argparse
import json
import os
import sys
import urllib.error
import re
import urllib.request
import uuid
import zipfile
from pathlib import Path

# CurseForge project id (set by the Project Manager).
PROJECT_ID = 1787208
API = "https://minecraft.curseforge.com/api"

# Minecraft version -> Java version name on CurseForge
MINECRAFT = {
    "1.20.1": "Java 17",
    "1.21.1": "Java 21",
    "26.1.2": "Java 25",
    "26.2": "Java 25",
    "26.3": "Java 25",
}
LOADERS = ("Forge", "NeoForge")
# Minecraft versions where each loader has its own jar instead of one combined jar.
SEPARATE = ("1.20.1",)
RELEASE_TYPES = ("alpha", "beta", "release")
ROOT = Path(__file__).resolve().parents[2]


def module_versions():
    """Every mod_version found in the modules' gradle.properties files."""
    found = set()
    for props in ROOT.glob("*/gradle.properties"):
        for line in props.read_text(encoding="utf-8").splitlines():
            if line.startswith("mod_version="):
                found.add(line.split("=", 1)[1].strip())
    return found


def builds_at(version):
    """The Minecraft versions whose Forge and NeoForge layers both carry this mod_version.

    A release only uploads those: a layer that was not bumped to the tag's version is left out and named in the log, so a tag can
    never publish an older build under a newer version number.
    """
    ready = []
    for minecraft in MINECRAFT:
        carried = set()
        for loader in ("forge", "neoforge"):
            props = ROOT / f"{loader}-{minecraft}" / "gradle.properties"
            if props.is_file():
                for line in props.read_text(encoding="utf-8").splitlines():
                    if line.startswith("mod_version="):
                        carried.add(line.split("=", 1)[1].strip())
        if carried == {version}:
            ready.append(minecraft)
        else:
            print(f"skipping Minecraft {minecraft}: its layers carry {sorted(carried) or 'no version'}, not {version}")
    return ready


def jar_versions(jar):
    """The version written into each loader metadata file inside a jar (mods.toml and neoforge.mods.toml)."""
    found = {}
    with zipfile.ZipFile(jar) as z:
        for name in ("META-INF/mods.toml", "META-INF/neoforge.mods.toml"):
            if name in z.namelist():
                text = z.read(name).decode("utf-8")
                match = re.search(r'^version\s*=\s*"([^"]+)"', text, re.MULTILINE)
                found[name] = match.group(1) if match else None
    return found


def token():
    value = os.environ.get("CURSEFORGE_TOKEN", "")
    if not value:
        sys.exit("CURSEFORGE_TOKEN is not set")
    return value


def request(url, data=None, headers=None):
    req = urllib.request.Request(url, data=data, headers={"X-Api-Token": token(), **(headers or {})})
    try:
        with urllib.request.urlopen(req, timeout=120) as response:
            return json.loads(response.read().decode("utf-8"))
    except urllib.error.HTTPError as error:
        body = error.read().decode("utf-8", "replace")[:500]
        sys.exit(f"CurseForge answered {error.code} for {url}: {body}")


def resolve(versions, name):
    matches = [v for v in versions if v["name"] == name]
    if not matches:
        close = sorted({v["name"] for v in versions if name.split(".")[0] in v["name"]})[:15]
        sys.exit(f"CurseForge has no game version named '{name}'. Similar names: {close}")
    return matches[0]["id"]


def multipart(fields, file_field, path, name):
    boundary = uuid.uuid4().hex
    parts = []
    for field, value in fields.items():
        parts.append((f"--{boundary}\r\nContent-Disposition: form-data; name=\"{field}\"\r\n"
                      f"Content-Type: application/json\r\n\r\n{value}\r\n").encode("utf-8"))
    parts.append((f"--{boundary}\r\nContent-Disposition: form-data; name=\"{file_field}\"; filename=\"{name}\"\r\n"
                  "Content-Type: application/java-archive\r\n\r\n").encode("utf-8"))
    parts.append(path.read_bytes())
    parts.append(f"\r\n--{boundary}--\r\n".encode("utf-8"))
    return b"".join(parts), f"multipart/form-data; boundary={boundary}"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--jars", type=Path)
    parser.add_argument("--version")
    parser.add_argument("--changelog-file", type=Path)
    parser.add_argument("--release-type", default="beta", choices=RELEASE_TYPES)
    parser.add_argument("--dry-run", action="store_true", help="resolve everything and show the plan without uploading")
    parser.add_argument("--list-versions", action="store_true", help="print the game versions CurseForge knows (read only)")
    args = parser.parse_args()

    if args.list_versions:
        for v in request(f"{API}/game/versions"):
            print(v["id"], v["name"])
        return
    if not (args.jars and args.version and args.changelog_file):
        parser.error("--jars, --version and --changelog-file are required")
    builds = builds_at(args.version)
    if not builds:
        sys.exit(f"No Minecraft version has both layers at mod_version {args.version}")

    versions = request(f"{API}/game/versions")
    changelog = args.changelog_file.read_text(encoding="utf-8")
    plan = []
    for minecraft, java in MINECRAFT.items():
        if minecraft not in builds:
            continue
        if minecraft in SEPARATE:
            jars = [(args.jars / f"barelypipes-{loader.lower()}-mc{minecraft}-{args.version}.jar", [loader]) for loader in LOADERS]
        else:
            jars = [(args.jars / f"barelypipes-mc{minecraft}-{args.version}.jar", list(LOADERS))]
        for jar, loaders in jars:
            if not jar.is_file():
                sys.exit(f"Missing jar {jar}")
            inside = jar_versions(jar)
            wanted = {"META-INF/mods.toml"} if len(loaders) == 1 and loaders[0] == "Forge" else (
                {"META-INF/neoforge.mods.toml"} if len(loaders) == 1 else {"META-INF/mods.toml", "META-INF/neoforge.mods.toml"})
            if minecraft in SEPARATE:
                wanted = {"META-INF/mods.toml"}  # NeoForge 1.20.1 still reads mods.toml
            if set(inside) != wanted or set(inside.values()) != {args.version}:
                sys.exit(f"{jar.name} does not carry version {args.version} in its loader metadata: {inside}")
            ids = [resolve(versions, minecraft), *(resolve(versions, loader) for loader in loaders),
                   resolve(versions, "Client"), resolve(versions, "Server")]
            try:
                ids.append(resolve(versions, java))
            except SystemExit:
                print(f"note: CurseForge has no '{java}', uploading {minecraft} without a Java version")
            label = f" {loaders[0]}" if len(loaders) == 1 else ""
            plan.append((jar, ids, f"Barely Pipes {args.version} (Minecraft {minecraft}{label})"))

    for jar, ids, title in plan:
        print(f"{title}: {jar.name} ({jar.stat().st_size // 1024} KiB) game version ids {ids}, {args.release_type}")
    if args.dry_run:
        print("dry run: nothing uploaded")
        return

    if not PROJECT_ID:
        sys.exit("PROJECT_ID is 0: create the CurseForge project and put its id in this script first")
    for jar, ids, title in plan:
        metadata = json.dumps({
            "changelog": changelog,
            "changelogType": "markdown",
            "displayName": title,
            "gameVersions": ids,
            "releaseType": args.release_type,
        })
        body, content_type = multipart({"metadata": metadata}, "file", jar, jar.name)
        result = request(f"{API}/projects/{PROJECT_ID}/upload-file", data=body, headers={"Content-Type": content_type})
        print(f"uploaded {jar.name} as file id {result.get('id')}")


if __name__ == "__main__":
    main()
