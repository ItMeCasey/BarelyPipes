"""Draws the six arrow overlays laid over the side faces of a plug (see PipeTextures.plugOverlay).

  python scripts/gen_plug_overlays.py            (from the repo root)

Each is a 16x16 PNG with bright chevrons on a transparent field, the same for every pipe: pipe_overlay_plug_up, _down, _left and
_right point that way in the texture, _both_v and _both_h point both ways along the height or the width. The plug side face they
go on is only 3 by 4 pixels of the block, so the chevrons are bold and fill the whole width. Running it again changes nothing.
"""
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BLOCK = os.path.join(ROOT, 'common', 'src', 'main', 'resources', 'assets', 'barelypipes', 'textures', 'block')
COLOR = (245, 245, 245, 255)
EDGE = (20, 20, 24, 255)


def chevron_up(img, top):
    """A chevron pointing up whose tip is at row `top`, 14 wide, 3 thick, with a dark edge."""
    pix = img.load()
    for k in range(7):
        for dx in (-1, 0):
            for x in (7 + dx - k, 8 - dx + k):
                for dy in range(3):
                    if 0 <= x < 16 and 0 <= top + k + dy < 16:
                        pix[x, top + k + dy] = EDGE
    for k in range(7):
        for x in (7 - k, 8 + k):
            for dy in range(2):
                if 0 <= x < 16 and 0 <= top + k + dy < 16:
                    pix[x, top + k + dy] = COLOR


def up(rows):
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for top in rows:
        chevron_up(img, top)
    return img


def main():
    one = up((0, 8))
    out = {
        'up': one,
        'down': one.transpose(Image.FLIP_TOP_BOTTOM),
        'left': one.transpose(Image.ROTATE_90),
        'right': one.transpose(Image.ROTATE_270),
    }
    both_v = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    both_v.alpha_composite(up((0,)).crop((0, 0, 16, 8)).resize((16, 8)), (0, 0))
    both_v.alpha_composite(up((0,)).transpose(Image.FLIP_TOP_BOTTOM).crop((0, 8, 16, 16)), (0, 8))
    out['both_v'] = both_v
    out['both_h'] = both_v.transpose(Image.ROTATE_90)
    for name, img in out.items():
        img.save(os.path.join(BLOCK, f'pipe_overlay_plug_{name}.png'))
        print('wrote', name)


if __name__ == '__main__':
    main()
