package j8_util.math;

import net.orbyfied.j8.util.math.Matrix;
import net.orbyfied.j8.util.math.Vector;
import org.junit.jupiter.api.Test;

public class MatrixTests {

    @Test
    void test_BasicMatrixPrint() {
        Matrix matrix = new Matrix(2, 2);
        matrix.set(0, 0, 3).set(0, 1, Math.PI);
        Vector vector = new Vector(3);
        vector.set(0, 5).set(1, 4387).set(2, 14.4);

        System.out.println(matrix);
        System.out.println();
        System.out.println(vector);
        System.out.println();
        System.out.println(matrix.dot(vector));
    }

}
