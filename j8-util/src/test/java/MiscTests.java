import it.unimi.dsi.fastutil.ints.Int2FloatArrayMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.orbyfied.j8.tests.Benchmarks;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class MiscTests {

    private static final int MAP_SIZE = 1_000_000;

    @Test
    void tBenchmarkGet() {
        final Int2ObjectOpenHashMap<Object> i2oMap = new Int2ObjectOpenHashMap<>();
        final Int2FloatOpenHashMap          i2fMap = new Int2FloatOpenHashMap();

        // fill hash map
        System.out.println("Started filling.");
        for (int i = 0; i < MAP_SIZE; i++) {
            final float f = 0;
            i2oMap.put(i, (Float)f);
            i2fMap.put(i, f);
        }

        System.out.println("Finished filling " + MAP_SIZE + " elements.");

        // start benchmarks
        Benchmarks.performBenchmark("I2OGet", integer -> {
            float f = (float) i2oMap.get(integer);
        }, MAP_SIZE, 1_000_000_000).print();
        Benchmarks.performBenchmark("I2FGet", integer -> {
            float f = i2fMap.get(integer);
        }, MAP_SIZE, 1_000_000_000).print();
    }

    @Test
    void tBenchmarkPut() {
        final Int2ObjectOpenHashMap<Object> i2oMap = new Int2ObjectOpenHashMap<>();
        final Int2FloatOpenHashMap          i2fMap = new Int2FloatOpenHashMap();

        // start benchmarks
        Benchmarks.performBenchmark("I2OGet", integer -> {
            i2oMap.put(integer, (Float)1f);
        }, MAP_SIZE, 1_000_000_000).print();
        Benchmarks.performBenchmark("I2FGet", integer -> {
            i2fMap.put(integer, 1f);
        }, MAP_SIZE, 1_000_000_000).print();
    }

}
