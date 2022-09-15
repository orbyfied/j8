package net.orbyfied.j8.util.logging.formatting.attr;

import net.orbyfied.j8.util.logging.formatting.Ansi;
import net.orbyfied.j8.util.logging.formatting.AnsiAttr;

import java.awt.*;

import static java.lang.String.valueOf;

public abstract class ColorAttr extends AnsiAttr {

    /**
     * 8-bit constructor.
     * @param c The color number/character (range 0-255)
     */
    public ColorAttr(char c) {
        // check range
        if (c > 255) throw new IllegalArgumentException("invalid color char/number, range: [0-255]");

        // set values
        col   = c;
        color = new String[] { valueOf(c) };
    }

    /**
     * 24-bit RGB constructor.
     * @param r Red.
     * @param g Green.
     * @param b Blue.
     */
    public ColorAttr(int r, int g, int b) {
        // check range
        uValidateColorComponents(r, g, b);

        // set values
        col   = new Color(r, g, b);
        color = new String[] { valueOf(r), valueOf(g), valueOf(b) };
    }

    /**
     * @see ColorAttr#ColorAttr(int, int, int)
     * @param color The color object.
     */
    public ColorAttr(Color color) {
        this(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Color string/array. 3 elements if RGB,
     * 1 if 8-bit.
     */
    protected String[] color;

    /**
     * The raw color object.
     * This is a 'char' if it is 8-bit and
     * a <code>java.awt.Color</code> object if RGB.
     */
    protected Object col;
    public    Object getColorAsObject() { return col; }

    /**
     * Checks if the attribute contains an RGB color.
     * @return The boolean.
     */
    public boolean isRGB() {
        return color.length == 3;
    }

    /** Generates a sequence prefix. */
    public abstract String seqPrefix(Object... args);

    public String sequence() {
        if (isRGB()) return color[0] + Ansi.SEPARATOR + color[1] + Ansi.SEPARATOR + color[2];
        else         return color[0];
    }

    @Override
    public String code(Object... args) {
        return seqPrefix(args) + sequence();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void uValidateColorComponents(int r, int g, int b) {
        if (
            r < 0 || r > 255 ||
            g < 0 || g > 255 ||
            b < 0 || b > 255
        ) throw new IllegalArgumentException("invalid color range; expected [0-255] for all 3 components, got: " +
                r + ", " + g + ", " + b);
    }

}
