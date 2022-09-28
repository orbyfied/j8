package j8_util.math.expr;

import net.orbyfied.j8.tests.Benchmarks;
import net.orbyfied.j8.util.math.expr.*;
import org.junit.jupiter.api.Test;

public class SimpleExpressionTests {

    /* ------------------ 1 --------------------- */

    @Test
    void test_10000_eval() {
        final String input = "3*x";
        ExpressionParser parser = new ExpressionParser()
                .forString(input)
                .lex()
                .parse();
        ExpressionNode node = parser.getAstNode();
        Context context = Context.newDefaultGlobal()
                .setValue("x", 4);
        long t1 = System.nanoTime();
        for (int i = 0; i < 10_000; i++)
            node.evaluate(context);
        long t2 = System.nanoTime();
        System.out.println("Time: " + (t2 - t1) + "ns");
    }

    void setupEnv(Context context) {
        context.setValue("x", 4);
    }

    final String input = "3*4";

    @Test
    void test_benchmark_Parsing() {
        // input string and parser
        ExpressionParser parser = new ExpressionParser()
                .forString(input);

        // perform
        Benchmarks.performBenchmark(
                "ExprParsing", i -> {
                    parser
                            .lex()
                            .parse();
                }, 1_000_000, 1_000_000_000L * 15
        ).print();
    }

    @Test
    void test_benchmark_ParsingAndExec() {
        // input string, parser and context
        ExpressionParser parser = new ExpressionParser()
                .forString(input);
        Context global = (Context) Context.newDefaultGlobal()
                .tableSet("test", ExpressionFunction.make((ctx, args) -> args[0]))
                .tableSet("x", Math.random() * 10000);

        // perform
        Benchmarks.performBenchmark(
                "ExprParsingAndExec", i -> {
                    parser
                            .lex()
                            .parse()
                            .getAstNode()
                            .evaluate(global);
                }, 1_000_000, 1_000_000_000L * 15
        ).print();
    }

    @Test
    void test_benchmark_Exec() {
        // input string, parser and context
        ExpressionParser parser = new ExpressionParser()
                .forString(input);
        Context global = (Context) Context.newDefaultGlobal()
                .tableSet("test", ExpressionFunction.make((ctx, args) -> args[0]))
                .tableSet("x", Math.random() * 10000);

        // parse into node
        ExpressionNode node = parser
                .lex()
                .parse()
                .getAstNode();

        // perform
        Benchmarks.performBenchmark(
                "ExprExec", i -> {
                    node.evaluate(global);
                }, 1_000_000, 1_000_000_000L * 15
        ).print();
    }

    @Test
    void test_benchmark_all() {
        test_benchmark_Parsing();
        test_benchmark_ParsingAndExec();
        test_benchmark_Exec();
    }

}
