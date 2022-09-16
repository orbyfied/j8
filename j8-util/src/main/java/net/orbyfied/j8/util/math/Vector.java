package net.orbyfied.j8.util.math;

public class Vector {

    // the size
    int s;

    // the data
    double[] data;

    public Vector(int s) {
        this.s    = s;
        this.data = new double[s];
    }

    public Vector(double... d) {
        this.s    = d.length;
        this.data = d;
    }


    public int getSize() {
        return s;
    }

    public double[] getData() {
        return data;
    }

    /* ---- Math ---- */

    public double dot(Vector vec) {
        if (vec.s != s)
            throw new IllegalArgumentException("provided vector is not of same length");
        double a = 0;
        for (int i = 0; i < s; i++)
            a += data[i] * vec.data[i];
        return a;
    }

    /* ---- Other ---- */

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("(");
        for (int i = 0; i < s; i++) {
            double d = data[i];
            if (i != 0)
                b.append(", ");
            b.append(d);
        }

        return b.append(")").toString();
    }

}
