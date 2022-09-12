package j8_util.math.expr;

import net.orbyfied.j8.tests.Benchmarks;
import net.orbyfied.j8.util.math.expr.*;
import org.junit.jupiter.api.Test;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleExpressionTests {

    /* ------------------ 1 --------------------- */

    ExpressionValue<?> eval(ExpressionParser parser,
              Context ctx,
              String str) {
        try {
            parser.forString(str)
                    .lex()
                    .parse();

            // evaluate
            return parser.getAstNode().evaluate(ctx);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Test
    void test_simpleExpr() {
        // input string
        final String str1 = "f = func(x) x";
        final String str2 = "f(5)";

        // prepare global context
        Context global = Context.newDefaultGlobal()
                .setValue("PI", Math.PI);
        ExpressionParser parser = new ExpressionParser();

        // evaluate
        eval(parser, global, str1);
        eval(parser, global, str2);

    }

    public static void main(String[] args) {
        new SimpleExpressionTests().test_shell();
    }

    @Test
    void test_shell() {
        // flags
        AtomicBoolean debug = new AtomicBoolean(false);
        AtomicBoolean exit  = new AtomicBoolean(false);

        // create context
        Context global = Context.newDefaultGlobal();
        ExpressionParser parser = new ExpressionParser();

        global.setValue("exit", ExpressionFunction.make((ctx, values) -> {
            exit.set(true);
            return ExpressionValue.NIL;
        }));

        global.setValue("debug", ExpressionFunction.make((ctx, values) -> {
            debug.set(!debug.get());
            return ExpressionValue.NIL;
        }));

        // input loop
        Scanner scanner = new Scanner(System.in);
        while (!exit.get()) {
            // read input and parse
            System.out.print("+-> ");
            String input = scanner.nextLine();

            // evaluate
            long t1 = System.nanoTime();
            Context c = global;
            ExpressionValue<?> val = eval(parser, c, input);
            long t2 = System.nanoTime();

            // print debug info
            if (debug.get()) {
                System.out.println("| \uD83D\uDEE0 SOURCE: | " + parser.getStrReader().getString());
                System.out.println("| \uD83D\uDEE0 TOKENS: | " + parser.getTokens());
                if (parser.getAstNode() != null) {
                    System.out.println("| \uD83D\uDEE0 AST:    | " + parser.getAstNode());
                    System.out.println("| \uD83D\uDEE0 CTX:    | " + c.getValues());
                }
            }

            // print evaluated
            System.out.println("|   " + (t2 - t1) + " ns (" + (t2 - t1) / 1_000_000 + " ms)");
            System.out.println("| = " + val);
        }
    }

    /* ------------ 2 ------------- */

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
