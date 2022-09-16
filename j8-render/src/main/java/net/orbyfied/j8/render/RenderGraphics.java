package net.orbyfied.j8.render;

import java.awt.*;

public interface RenderGraphics {

    /* ---- Transform ---- */

    RenderGraphics translate(float x, float y);

    RenderGraphics rotate(float x, float y);

    RenderGraphics scale(float x, float y);

    /* ---- Appearance ---- */

    RenderGraphics color(float r, float g, float b, float a);

    RenderGraphics color(Color color);

    /* ---- Draw ---- */

    RenderGraphics primitive(int glPrimitive, float[] vertices);

    RenderGraphics quad(float x1, float y1,
                        float x2, float y2,
                        float x3, float y3,
                        float x4, float y4);

    RenderGraphics quad(float x1, float y1, float x2, float y2);

}
