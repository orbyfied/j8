package net.orbyfied.j8.util.logging.formatting;

import net.orbyfied.j8.util.logging.formatting.arg.Space;
import net.orbyfied.j8.util.logging.formatting.attr.ColorAttr;

import java.awt.*;

public class TextFormat
        extends ColorAttr
        implements Cloneable {

    /* Background and foreground sequence prefixes. */
    private static final String BG_P = Space.BACKGROUND.getPrefix();
    private static final String FG_P = Space.FOREGROUND.getPrefix();

    /* True and 8-bit color sequence prefixes. */
    private static final String TRUE_COLOR_P = "2;";
    private static final String EBIT_COLOR_P = "5;";

    private boolean isBright;

    private boolean   isLiteral;
    private int       literal;

    /** Cache code. */
    private String code;

    /**
     * In what space should this color be used by default?
     */
    private Space space = Space.FOREGROUND;

    /*               */
    /* Constructors. */
    /*               */

    public static TextFormat of(Color color) {
        return new TextFormat(color);
    }

    TextFormat() { super(0, 0, 0); }

    public TextFormat(int c) {
        super(0, 0, 0);
        this.isLiteral = true;
        this.literal = c;

        if (literal >= 90 && 107 >= literal)
            isBright = true;

        createAndCacheCode();
    }

    public TextFormat(char c, Space space) {
        super(c);
        this.space = space;

        createAndCacheCode();
    }

    public TextFormat(int r, int g, int b, Space space) {
        super(r, g, b);
        this.space = space;

        createAndCacheCode();
    }

    public TextFormat(int r, int g, int b) {
        super(r, g, b);
        this.space = Space.FOREGROUND;

        createAndCacheCode();
    }

    public TextFormat(Color color, Space space) {
        super(color);
        this.space = space;

        createAndCacheCode();
    }

    public TextFormat(Color color) {
        super(color);
        this.space = Space.FOREGROUND;

        createAndCacheCode();
    }


    /**
     * Generates the sequence prefix.
     * Returns nothing if literal, otherwise it returns
     * The background/foreground prefix with if it is
     * a true color or not.
     */
    @Override
    public String seqPrefix(Object... args) {
        if (isLiteral) return "";

        // get space
        Space space = Space.getFromArray(args);
        if (space == null) space = this.space;

        // get color depth and return
        return space.getPrefix() + (isRGB() ? TRUE_COLOR_P : EBIT_COLOR_P);
    }

    public String createAndCacheCode(Object... args) {
        return this.code = createCode(args);
    }

    /**
     * Generate the code. If it is literal, it modifies the
     * literal based on if it is a background color, and
     * returns it, otherwise it returns the super method.
     */
    public String createCode(Object... args) {
        // retrieve space
        Space space = Space.getFromArray(args);
        if (space == null) space = this.space;

        // check if it is literal
        if (isLiteral) {
            int    l = literal;
            if (space == Space.BACKGROUND) l += 10;
            return Integer.toString(l);
        }

        // return super code
        return super.code(args);
    }

    /**
     * Returns the ANSI code for this format.
     * @see TextFormat#createCode(Object...)
     */
    @Override public String code(Object... args) {
        if (args.length == 0)
            return code;
        return createCode(args);
    }

    public TextFormat clone() {
        // create new color
        TextFormat color = new TextFormat();

        // copy fields
        color.space = this.space;

        color.col   = this.col;
        color.color = this.color;

        color.isLiteral = this.isLiteral;
        color.literal   = this.literal;

        color.createAndCacheCode();

        // return
        return color;
    }

    /**
     * Clones the object with the specified space
     * applied.
     * @param space The specified space.
     * @return The cloned object.
     */
    public TextFormat space(Space space) {
        TextFormat color = this.clone();
        color.space = space;
        return color;
    }

    /** @see TextFormat#space(Space) */
    public TextFormat bg() {
        return space(Space.BACKGROUND);
    }

    /** @see TextFormat#space(Space) */
    public TextFormat fg() {
        return space(Space.FOREGROUND);
    }

    /////////////////////////////////////////////////////////////////////////////////

    public static final TextFormat RESET = new TextFormat(0);

                public static final TextFormat BOLD      = new TextFormat(1);
    @Deprecated public static final TextFormat FAINT     = new TextFormat(2);
    @Deprecated public static final TextFormat ITALIC    = new TextFormat(3);
                public static final TextFormat UNDERLINE = new TextFormat(4);
                public static final TextFormat FRAMED    = new TextFormat(51);
                public static final TextFormat ENCIRCLED = new TextFormat(52);
                public static final TextFormat OVERLINED = new TextFormat(53);

    public static final TextFormat BLACK_FG = new TextFormat(30);
    public static final TextFormat BLACK_BG = BLACK_FG.bg();
    public static final TextFormat DARK_RED_FG = new TextFormat(31);
    public static final TextFormat DARK_RED_BG = DARK_RED_FG.bg();
    public static final TextFormat DARK_GREEN_FG = new TextFormat(32);
    public static final TextFormat DARK_GREEN_BG = DARK_GREEN_FG.bg();
    public static final TextFormat DARK_YELLOW_FG = new TextFormat(33);
    public static final TextFormat DARK_YELLOW_BG = DARK_YELLOW_FG.bg();
    public static final TextFormat DARK_BLUE_FG = new TextFormat(34);
    public static final TextFormat DARK_BLUE_BG = DARK_BLUE_FG.bg();
    public static final TextFormat DARK_PINK_FG = new TextFormat(35);
    public static final TextFormat DARK_PINK_BG = DARK_PINK_FG.bg();
    public static final TextFormat DARK_AQUA_FG = new TextFormat(36);
    public static final TextFormat DARK_AQUA_BG = DARK_AQUA_FG.bg();
    public static final TextFormat DARK_GRAY_FG = new TextFormat(37);
    public static final TextFormat DARK_GRAY_BG = DARK_GRAY_FG.bg();
    public static final TextFormat WHITE_FG = new TextFormat(30);
    public static final TextFormat WHITE_BG = WHITE_FG.bg();
    public static final TextFormat RED_FG = new TextFormat(81);
    public static final TextFormat RED_BG = RED_FG.bg();
    public static final TextFormat GREEN_FG = new TextFormat(82);
    public static final TextFormat GREEN_BG = GREEN_FG.bg();
    public static final TextFormat YELLOW_FG = new TextFormat(83);
    public static final TextFormat YELLOW_BG = YELLOW_FG.bg();
    public static final TextFormat BLUE_FG = new TextFormat(84);
    public static final TextFormat BLUE_BG = BLUE_FG.bg();
    public static final TextFormat PINK_FG = new TextFormat(85);
    public static final TextFormat PINK_BG = PINK_FG.bg();
    public static final TextFormat AQUA_FG = new TextFormat(86);
    public static final TextFormat AQUA_BG = AQUA_FG.bg();
    public static final TextFormat GRAY_FG = new TextFormat(87);
    public static final TextFormat GRAY_BG = DARK_GRAY_FG.bg();

}
