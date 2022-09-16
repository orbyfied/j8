package j8_util.math;

import net.orbyfied.j8.util.math.Matrix;
import org.junit.jupiter.api.Test;

public class MatrixTests {

    @Test
    void test_BasicMatrixPrint() {
        Matrix matrix = new Matrix(2, 2);
        matrix.set(0, 0, 3);
        matrix.set(0, 1, Math.PI);
        System.out.println(matrix.toString());
    }

}
