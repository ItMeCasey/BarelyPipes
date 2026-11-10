"""Runs a real game client with one item and recipe viewer and takes screenshots of it showing Barely Pipes' items and recipes.

Usage: python scripts/viewer_check.py <layer> <mods> [--script "inv;wait:40;shot:name"] [--tag jei]
       e.g. python scripts/viewer_check.py neoforge-26.2 rei,architectury,cloth --tag rei

<mods> is a comma list of the viewer files the layer's build.gradle knows (jei, rei, emi, bei, plus architectury and cloth for REI).
The client starts through the studio's isolated launcher (launch-isolated.ps1: a hidden Windows desktop, muted, no focus or cursor
reaching the real desktop) with BARELYPIPES_SHOTS=viewers, so the dev tour runs ViewerTour instead of the pipe tour: --script is
its list of steps (see ViewerTour: inv, give:<item>, wait:N, shot:<name>, move:x,y, click:x,y[,button], key:[ctrl+]name,
type:text, esc; x and y up to 1.5 are fractions of the window). Screenshots land in <layer>/run/screenshots and are copied to
docs/frames/viewers/<layer>/ with --tag (default: the first mod) in the file names.

Only one Minecraft client may run on this PC at a time: the script waits while any other client is running.
"""
import os
import re
import shutil
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ISOLATED = r"C:\Users\casey\.claude\tools\launch-isolated.ps1"
WHO = r"C:\Users\casey\.claude\tools\who-owns-minecraft.ps1"
# Own desktop and log (BPipes) so other projects' launches and kills on the shared MCTest desktop do not touch this run.
DESKTOP = "BPipes"
ISOLATED_LOG = Path(os.environ.get("LOCALAPPDATA", "")) / "launch-isolated" / f"{DESKTOP}.log"
TIMEOUT = 600
OPTIONS = {
    "pauseOnLostFocus": "false", "fullscreen": "false", "guiScale": "2", "lang": "en_us", "onboardAccessibility": "false",
    "skipMultiplayerWarning": "true", "joinedFirstServer": "true", "tutorialStep": "none",
}
WINDOW = "1600x900"


def other_clients():
    out = subprocess.run(["powershell", "-NoProfile", "-File", WHO], capture_output=True, text=True).stdout
    return [line for line in out.splitlines() if re.search(r"\b(game|client)\b", line)]


def write_options(run):
    path = run / "options.txt"
    lines = path.read_text(encoding="utf-8").splitlines() if path.exists() else []
    kept = [l for l in lines if l.split(":", 1)[0] not in OPTIONS]
    path.write_text("\n".join(kept + [f"{k}:{v}" for k, v in OPTIONS.items()]) + "\n", encoding="utf-8")


def check(layer, mods, script, tag):
    folder = ROOT / layer
    run = folder / "run"
    run.mkdir(exist_ok=True)
    waited = time.time()
    while other_clients():
        if time.time() - waited > 1800:
            return f"VIEWER SKIP {layer} [{mods}]: another client kept running for 30 minutes"
        time.sleep(30)
    write_options(run)
    shots = run / "screenshots"
    if shots.exists():
        shutil.rmtree(shots)
    start_size = ISOLATED_LOG.stat().st_size if ISOLATED_LOG.exists() else 0
    env = {"BARELYPIPES_SHOTS": os.environ.get("VIEWER_SHOTS", "viewers"), "BARELYPIPES_VIEWER_SCRIPT": script}
    sets = "".join(f'set "{k}={v}"&& ' for k, v in env.items())
    command = sets + f".\\gradlew.bat runClient --no-daemon -q -PdevTools {"-Pviewers=" + mods if mods != "-" else ""} {os.environ.get("VIEWER_EXTRA", "")} -PwindowSize={WINDOW}"
    subprocess.run(["powershell", "-NoProfile", "-File", ISOLATED, "-Run", "-WaitMinutes", "60", "-Desktop", DESKTOP, "-WorkDir", str(folder), "-Command", command],
                   check=True, capture_output=True)
    began = time.time()

    def lines():
        if not ISOLATED_LOG.exists():
            return []
        data = ISOLATED_LOG.read_bytes()
        return data[start_size if len(data) >= start_size else 0:].decode("utf-8", errors="replace").splitlines()

    done = False
    failed = False
    while time.time() - began < TIMEOUT:
        time.sleep(3)
        log = lines()
        if any("BARELYPIPES-SHOTS DONE" in l for l in log):
            done = True
            break
        if any(re.search(r"BUILD FAILED|FAILURE: Build failed|Could not resolve|Crash report saved", l) for l in log):
            failed = True
            break
    if done:
        time.sleep(10)
    log = lines()
    subprocess.run(["powershell", "-NoProfile", "-File", ISOLATED, "-Kill", "-WorkDir", str(ROOT), "-Desktop", DESKTOP], capture_output=True)
    tag = tag or mods.split(",")[0]
    (run / f"viewer_check_{tag}.log").write_text("\n".join(log), encoding="utf-8")
    names = sorted(p.name for p in shots.glob("*.png")) if shots.exists() else []
    out = ROOT / "docs" / "frames" / "viewers" / layer
    out.mkdir(parents=True, exist_ok=True)
    for n in names:
        shutil.copy2(shots / n, out / n.replace("barelypipes_", f"{tag}_", 1))
    errors = [l for l in log if re.search(r"(barelypipes|jei|emi|roughlyenoughitems|bei).*(Exception|ERROR)|ModLoadingException|Failed to load", l, re.I)
              and "[mixin/]" not in l and "Untranslated tag" not in l]
    status = "VIEWER PASS" if done and not failed and not errors else "VIEWER FAIL"
    detail = "" if status == "VIEWER PASS" else f": done={done} failed={failed} first_error={errors[:2]}"
    return f"{status} {layer} [{mods}] ({len(names)} screenshots, {int(time.time() - began)}s){detail}"


if __name__ == "__main__":
    args = sys.argv[1:]
    script = "inv;wait:40;shot:inventory"
    tag = ""
    if "--script" in args:
        script = args[args.index("--script") + 1]
    if "--tag" in args:
        tag = args[args.index("--tag") + 1]
    positional = [a for i, a in enumerate(args) if not a.startswith("--") and (i == 0 or args[i - 1] not in ("--script", "--tag"))]
    result = check(positional[0], positional[1], script, tag)
    print(result, flush=True)
    sys.exit(1 if result.startswith("VIEWER FAIL") else 0)
