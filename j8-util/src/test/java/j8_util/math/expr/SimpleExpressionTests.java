package j8_util.math.expr;

import net.orbyfied.j8.tests.Benchmarks;
import net.orbyfied.j8.util.math.expr.*;
import org.junit.jupiter.api.Test;

public class SimpleExpressionTests {

    /* ------------------ 1 --------------------- */

    @Test
    void test_benchmark_Parsing() {
        // input string and parser
        final String input = "test(x * 9 + 1) ^ (7 - 5)";
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
        final String input = "test(x * 9 + 1) ^ (7 - 5)";
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
        final String input = "test(x * 9 + 1) ^ (7 - 5)";
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
