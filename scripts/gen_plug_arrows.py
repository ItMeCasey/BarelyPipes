"""Draws the plug glyph faces with arrows, from the plug textures already in the repo.

  python scripts/gen_plug_arrows.py            (from the repo root)

For every pipe type and tier this reads common/.../textures/block/pipe_<type>_plug[_t<n>].png (the glyph in the
tier color on the pipe's field, made by AeroFlux-Pipe-Template/gen_pipes.py) and writes three more 16x16 faces
beside it, the same size and style, with 3-pixel chevrons in the glyph's own color in the side margins
(columns 0-1 and 14-15), so the glyph itself stays exactly where it was:

  pipe_<type>_plug_import[_t<n>].png   chevrons point toward the glyph (into the pipe)
  pipe_<type>_plug_export[_t<n>].png   chevrons point away from the glyph (out of the pipe)
  pipe_<type>_plug_open[_t<n>].png     both: one of each on each side

The gas plug glyph face is also redrawn once on a darker field (the item pipe's plugs do the same), because its
gold and iron tier colors vanish on the gas pipe's yellow field (tier 2 had a contrast of 1.04 to 1), which made the
gas glyph look missing. Running the script again changes nothing. Output is deterministic.
"""
import os
import sys

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BLOCK = os.path.join(ROOT, 'common', 'src', 'main', 'resources', 'assets', 'barelypipes', 'textures', 'block')
TIERS = {1: (0xC8, 0xC8, 0xC8), 2: (0xCF, 0xA0, 0x00), 3: (0x2A, 0x8F, 0xB5)}
TYPES = [('fluid', True), ('item', True), ('energy', True), ('gas', True),
         ('redstone', False), ('me', False), ('dense_me', False), ('storage', False)]
# The flat dark field these types get on their glyph face (the pipe's own field, about 42% as bright).
DARK_FIELD = {'gas': (90, 68, 18)}

def median(px):
    return tuple(sorted(p[k] for p in px)[len(px) // 2] for k in range(3))


def dist(a, b):
    return sum(abs(a[k] - b[k]) for k in range(3))


def chevron(side, direction, y):
    """The 3 pixels of a chevron in the left or right margin, centered on row y."""
    if side == 'left':
        tip_x, wing_x = (1, 0) if direction == 'in' else (0, 1)
    else:
        tip_x, wing_x = (14, 15) if direction == 'in' else (15, 14)
    return [(tip_x, y), (wing_x, y - 1), (wing_x, y + 1)]


def arrows(mode):
    if mode == 'import':
        spots = [('left', 'in', 7), ('right', 'in', 7)]
    elif mode == 'export':
        spots = [('left', 'out', 7), ('right', 'out', 7)]
    else:
        spots = [('left', 'in', 4), ('left', 'out', 10), ('right', 'in', 4), ('right', 'out', 10)]
    pixels = []
    for side, direction, y in spots:
        pixels += chevron(side, direction, y)
    return pixels


def load(path):
    return Image.open(path).convert('RGBA')


def build(t, tiered, tier):
    suffix = '_t%d' % tier if tiered else ''
    img = load(os.path.join(BLOCK, 'pipe_%s_plug%s.png' % (t, suffix)))
    px = [img.getpixel((x, y))[:3] for y in range(16) for x in range(16)]
    bg = median(px)
    if tiered:
        color = TIERS[tier]
        mask = {(x, y) for y in range(16) for x in range(16) if img.getpixel((x, y))[:3] == color}
    else:
        mask = {(x, y) for y in range(16) for x in range(16) if dist(img.getpixel((x, y))[:3], bg) > 70}
        counts = {}
        for pos in mask:
            c = img.getpixel(pos)[:3]
            counts[c] = counts.get(c, 0) + 1
        color = max(sorted(counts), key=lambda c: counts[c])
    if t in DARK_FIELD:
        for y in range(16):
            for x in range(16):
                if (x, y) not in mask:
                    img.putpixel((x, y), DARK_FIELD[t] + (255,))
    out = {'plug' + suffix: img}
    for mode in ('import', 'export', 'open'):
        face = img.copy()
        for pos in arrows(mode):
            face.putpixel(pos, color + (255,))
        out['plug_%s%s' % (mode, suffix)] = face
    return out


GUI_GLYPHS = os.path.join(ROOT, 'common', 'src', 'main', 'resources', 'assets', 'barelypipes', 'textures', 'gui', 'glyph')


def storage_gui_glyph():
    """The storage glyph of the pipe screen: a three drawer cabinet, pure white, exactly 13 by 13 starting one
    pixel into the 16 by 16 canvas like every other glyph (the template's storage glyph is 12 by 14, off-white and
    centered half a pixel off, so it sat crooked in its button)."""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    white = (255, 255, 255, 255)
    for x in range(1, 14):
        for y in (1, 5, 9, 13):
            img.putpixel((x, y), white)
    for y in range(1, 14):
        for x in (1, 13):
            img.putpixel((x, y), white)
    for y in (3, 7, 11):
        for x in range(5, 10):
            img.putpixel((x, y), white)
    img.save(os.path.join(GUI_GLYPHS, 'storage.png'))


def main():
    storage_gui_glyph()
    written = []
    for t, tiered in TYPES:
        for tier in ((1, 2, 3) if tiered else (1,)):
            for name, img in build(t, tiered, tier).items():
                file = 'pipe_%s_%s.png' % (t, name)
                img.save(os.path.join(BLOCK, file))
                written.append(file)
    print('wrote %d textures' % len(written))
    for file in written:
        if 'import' in file or 'export' in file or 'open' in file:
            print(' ', file)
    return 0


if __name__ == '__main__':
    sys.exit(main())
