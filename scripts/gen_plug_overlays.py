"""Draws the six arrow overlays laid over the side faces of a plug (see PipeTextures.plugOverlay).

  python scripts/gen_plug_overlays.py            (from the repo root)

Each is a 16x16 PNG with one small arrow on a transparent field, the same for every pipe: pipe_overlay_plug_up, _down, _left and
_right point that way in the texture, _both_v and _both_h have a head at each end. The arrow is a plain shaft with a triangular
head, light with a dark edge, and leaves a margin round it because the plug side face it goes on is only 3 by 4 pixels of the
block. Running it again changes nothing.
"""
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BLOCK = os.path.join(ROOT, 'common', 'src', 'main', 'resources', 'assets', 'barelypipes', 'textures', 'block')
COLOR = (245, 245, 245, 255)
EDGE = (20, 20, 24, 255)


def arrow_cells(double):
    """The filled cells of an arrow pointing up (head at the top), or of a two-headed one: heads 2, 4 and 6 wide, a 2 wide shaft."""
    cells = set()
    top, bottom = 3, 12
    for k in range(3):
        for x in range(7 - k, 9 + k):
            cells.add((x, top + k))
    for y in range(top + 3, bottom + 1):
        for x in (7, 8):
            cells.add((x, y))
    if double:
        for k in range(3):
            for x in range(7 - k, 9 + k):
                cells.add((x, bottom - k))
    return cells


def draw(cells):
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    pix = img.load()
    for x, y in cells:
        for dx in (-1, 0, 1):
            for dy in (-1, 0, 1):
                nx, ny = x + dx, y + dy
                if 0 <= nx < 16 and 0 <= ny < 16 and (nx, ny) not in cells:
                    pix[nx, ny] = EDGE
    for x, y in cells:
        pix[x, y] = COLOR
    return img


def main():
    one = draw(arrow_cells(False))
    both_v = draw(arrow_cells(True))
    out = {
        'up': one,
        'down': one.transpose(Image.FLIP_TOP_BOTTOM),
        'left': one.transpose(Image.ROTATE_90),
        'right': one.transpose(Image.ROTATE_270),
        'both_v': both_v,
        'both_h': both_v.transpose(Image.ROTATE_90),
    }
    for name, img in out.items():
        img.save(os.path.join(BLOCK, f'pipe_overlay_plug_{name}.png'))
        print('wrote', name)


if __name__ == '__main__':
    main()
