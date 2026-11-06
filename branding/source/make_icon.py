"""Draws the Barely Pipes icon as 32x32 pixel art and writes it at several sizes.

Run from the repository root: python branding/source/make_icon.py
Writes branding/icon/icon_<size>.png, branding/icon/icon.svg and barelypipes_logo.png (for the jar).

The picture: an end-on view of the seven pipes of a bundle (one center pipe and six around it) in their type colors,
inside a gold clamp, on the studio's night tile. Pipe base colors are the averages of the core_v textures in
AeroFlux-Pipe-Template/gen (tier 1); the storage pipe is emerald.
"""
import math
from pathlib import Path

from PIL import Image

N = 32
OUT = Path(__file__).resolve().parent.parent / "icon"

BG = (24, 22, 38)
BG_EDGE = (46, 42, 70)
BG_TOP = (58, 54, 88)
GOLD = (242, 179, 61)
GOLD_HI = (255, 226, 140)
GOLD_LO = (178, 116, 32)

# Averages of the gen/pipe_<type>_core_v textures (tier 1); storage is emerald.
FLUID = (122, 114, 112)
ITEM = (83, 72, 71)
REDSTONE = (173, 53, 42)
ENERGY = (184, 95, 31)
GAS = (186, 139, 37)
ME = (122, 84, 173)
STORAGE = (62, 160, 96)

px = [[None] * N for _ in range(N)]


def put(x, y, c):
    if 0 <= x < N and 0 <= y < N:
        px[y][x] = c


def rect(x0, y0, x1, y1, c):
    for yy in range(y0, y1 + 1):
        for xx in range(x0, x1 + 1):
            put(xx, yy, c)


def shade(c, f):
    return tuple(max(0, min(255, round(v * f))) for v in c)


def lift(c, f):
    return tuple(max(0, min(255, round(v + (255 - v) * f))) for v in c)


R = 4
for y in range(N):
    for x in range(N):
        cx = min(max(x, R - 0.5), N - R - 0.5)
        cy = min(max(y, R - 0.5), N - R - 0.5)
        if math.hypot(x - cx, y - cy) <= R:
            px[y][x] = BG
for y in range(N):
    for x in range(N):
        if px[y][x] is None:
            continue
        edge = any(not (0 <= x + dx < N and 0 <= y + dy < N) or px[y + dy][x + dx] is None for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        if edge:
            px[y][x] = BG_TOP if y < N // 2 else BG_EDGE

# Gold clamp: a flat hexagon-ish plate behind the pipes, drawn as a ring of gold just outside them.
CENTER = (15.5, 15.0)
STEP_X, STEP_Y = 8.0, 7.0
POS = {
    "storage": (0, 0),
    "redstone": (-0.5, -1), "energy": (0.5, -1),
    "item": (1, 0), "me": (0.5, 1), "gas": (-0.5, 1), "fluid": (-1, 0),
}
COLORS = {"storage": STORAGE, "redstone": REDSTONE, "energy": ENERGY, "gas": GAS, "me": ME, "fluid": FLUID, "item": ITEM}
# Item is dark and fluid is gray: lighten the outline of every pipe a little so they read on the night tile.
RAD = 4.0

# Clamp: a plate behind the pipes (union of discs a little bigger than the pipes) with a gold edge.
centers = {k: (CENTER[0] + v[0] * STEP_X, CENTER[1] + v[1] * STEP_Y) for k, v in POS.items()}
PLATE = (52, 40, 34)


def plate_dist(x, y):
    return min(math.hypot(x - cx, y - cy) for cx, cy in centers.values())


for y in range(N):
    for x in range(N):
        if px[y][x] is None or x in (0, N - 1) or y in (0, N - 1):
            continue
        d = plate_dist(x, y)
        if d <= RAD + 1.6:
            px[y][x] = GOLD if d > RAD + 0.8 else PLATE
            if d > RAD + 0.8 and (x + y) % 2 == 0 and (x < 16) == (y < 16):
                px[y][x] = GOLD_LO

for name in ("fluid", "item", "redstone", "energy", "gas", "me", "storage"):
    base = COLORS[name]
    cx, cy = centers[name]
    rad = RAD + (0.4 if name == "storage" else 0)
    for y in range(N):
        for x in range(N):
            d = math.hypot(x - cx, y - cy)
            if d > rad:
                continue
            # light from the top left: lit rim, body, dark rim at the bottom right
            nx, ny = (x - cx) / rad, (y - cy) / rad
            lit = -(nx + ny) / 1.41
            if d > rad - 1.0:
                c = lift(base, 0.45) if lit > 0.15 else shade(base, 0.45) if lit < -0.15 else shade(base, 0.7)
            else:
                c = lift(base, 0.18) if lit > 0.35 else shade(base, 0.82) if lit < -0.45 else base
            put(x, y, c)
    # a bright core dot, the pipe's channel
    ix, iy = int(round(cx - 0.5)), int(round(cy - 0.5))
    put(ix, iy, lift(base, 0.85))
    put(ix + 1, iy, lift(base, 0.6))
    put(ix, iy + 1, lift(base, 0.6))

# Gold dots mark the clamp at the two ends of the middle row, and a gold foot plate underneath.
for x in range(8, 24):
    put(x, 29, GOLD_LO)
    put(x, 28, GOLD)
put(8, 28, GOLD_HI)
put(9, 28, GOLD_HI)

img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
for y in range(N):
    for x in range(N):
        if px[y][x] is not None:
            img.putpixel((x, y), px[y][x] + (255,))

OUT.mkdir(parents=True, exist_ok=True)
for size in (32, 64, 128, 256, 512, 1024):
    img.resize((size, size), Image.NEAREST).save(OUT / f"icon_{size}.png", optimize=True)
img.resize((512, 512), Image.NEAREST).save(OUT / "barelypipes_logo.png", optimize=True)

rects = []
for y in range(N):
    for x in range(N):
        c = px[y][x]
        if c is not None:
            rects.append(f'<rect x="{x}" y="{y}" width="1.02" height="1.02" fill="#{c[0]:02x}{c[1]:02x}{c[2]:02x}"/>')
(OUT / "icon.svg").write_text(
    f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {N} {N}" shape-rendering="crispEdges">' + "".join(rects) + "</svg>\n",
    encoding="utf-8",
)
print("wrote", OUT)
