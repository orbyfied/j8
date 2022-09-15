package net.orbyfied.j8.util.logging.formatting.attr;

import net.orbyfied.j8.util.logging.formatting.AnsiAttr;

/**
 * A literal ANSI attribute, which just returns
 * the code passed into the constructor, without
 * any processing or anything like that.
 */
public class LiteralAttr extends AnsiAttr {
    private String code;
    public LiteralAttr(String code) { this.code = code; }

    @Override
    public String code(Object... args) { return code; }
}
