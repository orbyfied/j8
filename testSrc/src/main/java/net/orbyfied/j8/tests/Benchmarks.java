package net.orbyfied.j8.tests;

import java.io.PrintStream;
import java.util.function.Consumer;

public class Benchmarks {

    public static record BenchmarkResult(String name, int maxPasses, long maxTime,
                                         int passes, long totalTime, double averagePassTime) {
        public BenchmarkResult print(PrintStream stream) {
            long tms = totalTime / 1_000_000;
            stream.println("+- " + name + ": " + passes + " Passes " +
                    (passes >= maxPasses ? "(MAX) " : "") + " | Total Time: " + totalTime + "ns (" + tms + "ms)" +
                    " | Avg. time/pass: " + averagePassTime + "ns (" + averagePassTime / 1_000_000 + "ms)");
            return this;
        }

        public BenchmarkResult print() {
            return print(System.out);
        }
    }

    ///////////////////////////////

    public static BenchmarkResult performBenchmark(
            String name,
            Consumer<Integer> func,
            int maxPasses,
            long maxTime
    ) {
        maxPasses--; // account for i++ returning last value

        // timing
        long start = System.nanoTime();
        // do passes
        int i = 0;
        while (
                (System.nanoTime() - start) < maxTime
                        && (i++) < maxPasses
        )
            func.accept(i);

        // timings
        long end = System.nanoTime();
        long tns = end - start;
        double tpp = tns / (i * 1D);

        // return result
        return new BenchmarkResult(
                name, maxPasses, maxTime,
                i, tns, tpp
        );
    }

}
