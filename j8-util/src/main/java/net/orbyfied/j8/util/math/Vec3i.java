package net.orbyfied.j8.util.math;

public class Vec3i {

    int x;
    int y;
    int z;

    public Vec3i(int x,
                 int y,
                 int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3i(int[] c) {
        this.x = c[0];
        this.y = c[1];
        this.z = c[2];
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

}
