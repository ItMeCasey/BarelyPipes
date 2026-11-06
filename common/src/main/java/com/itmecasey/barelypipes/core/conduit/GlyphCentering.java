package com.itmecasey.barelypipes.core.conduit;

/**
 * The pixel math for putting a screen glyph in the middle of a button. A glyph texture is 16 by 16
 * pixels whose 13 by 13 picture starts one pixel in, so the picture covers pixels 1 to 13 and its
 * middle pixel is 7. A 13 wide picture only has a middle pixel in a box with an odd number of
 * pixels, so the buttons are odd sizes: then the margins left and right (and above and below) are
 * the same, to the pixel.
 */
public final class GlyphCentering {
    /** Width and height of the picture inside the glyph texture. */
    public static final int PICTURE = 13;
    /** Width and height of the glyph texture. */
    public static final int CANVAS = 16;
    /** Pixels between the texture's edge and its picture. */
    public static final int INSET = 1;
    /** The middle pixel of the picture, counted from the texture's corner. */
    public static final int MIDDLE = INSET + PICTURE / 2;
    /** Size of the mode and distribution buttons and of the pipe box in the header. */
    public static final int BUTTON = 21;
    /** Size of the small buttons (Priority up and down). */
    public static final int SMALL_BUTTON = 17;

    private GlyphCentering() {
    }

    /** Whether a box this many pixels across can hold the picture with equal margins on both sides. */
    public static boolean exact(int boxSize) {
        return boxSize >= PICTURE && (boxSize - PICTURE) % 2 == 0;
    }

    /** The middle pixel of a box that starts at {@code boxStart} and is {@code boxSize} pixels across. */
    public static int middle(int boxStart, int boxSize) {
        return boxStart + boxSize / 2;
    }

    /** Where the texture's corner goes so the picture sits in the middle of the box. */
    public static int textureStart(int boxStart, int boxSize) {
        return middle(boxStart, boxSize) - MIDDLE;
    }
}
