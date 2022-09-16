package j8_util.math;

import net.orbyfied.j8.util.math.Vector;
import org.junit.jupiter.api.Test;

public class VectorTests {

    @Test
    void test_BasicVectorPrint() {
        Vector vector = new Vector(5, 6, 7, 8);
        System.out.println(vector.toString());
    }

}
