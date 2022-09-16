package net.orbyfied.j8.util.math;

import net.orbyfied.j8.util.StringUtil;

import java.text.DecimalFormat;

public class Matrix {

    // dimensions
    int w, h;

    // the data
    double[][] data;

    public Matrix(int w, int h) {
        this.w    = w;
        this.h    = h;
        this.data = new double[w][h];
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }

    public double[][] getData() {
        return data;
    }

    public Matrix set(int x, int y, double d) {
        data[x][y] = d;
        return this;
    }

    public Matrix setColumn(int x, double[] d) {
        data[x] = d;
        return this;
    }

    public Matrix setRow(int y, double[] d) {
        for (int x = 0; x < w; x++)
            data[x][y] = d[x];
        return this;
    }

    public Matrix copyFrom(Matrix a) {
        if (a.w != this.w || a.h != this.h)
            return this;
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                data[x][y] = a.data[x][y];
        return this;
    }

    public double get(int x, int y) {
        return data[x][y];
    }

    /* ---- Math ---- */

    public Vector product(Vector vec) {
        return vec;
    }

    /* ---- Other ---- */

    private static final DecimalFormat format = new DecimalFormat();

    static {
        format.setMaximumFractionDigits(4);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        int[] ml = new int[w];
        String[][] s = new String[w][h];
        for (int y = h - 1; y >= 0; y--) {
            for (int x = 0; x < w; x++) {
                double d  = data[x][y];
                String ds = format.format(d);
                if (ds.length() > ml[x])
                    ml[x] = ds.length();
                s[x][y] = ds;
            }
        }

//        b.append("\u250C ").append(" ".repeat((ml + 2) * w - 2)).append(" \u2510\n");

        for (int y = h - 1; y >= 0; y--) {
            b.append("| ");
            for (int x = 0; x < w; x++) {
                if (x != 0)
                    b.append(", ");
                b.append(StringUtil.extendTail(s[x][y], ml[x], ' '));
            }
            b.append(" |").append('\n');
        }

//        b.append("\u2514 ").append(" ".repeat((ml + 2) * w - 2)).append(" \u2518");

        return b.toString();
    }
}
