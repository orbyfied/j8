package j8_util.math.expr;

import net.orbyfied.j8.util.math.expr.*;
import net.orbyfied.j8.util.math.expr.error.ExprException;
import org.junit.jupiter.api.Test;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class MathShellTest {

    ExpressionValue<?> eval(ExpressionParser parser,
                            Context ctx,
                            String str,
                            boolean debug) {
        try {
            parser.forString(str)
                    .lex()
                    .parse();

            // evaluate
            return parser.getAstNode().evaluate(ctx);
        } catch (Exception e) {
            System.out.println("\u001B[31m" + e.getClass().getSimpleName() + ": " + e.getMessage() + "\u001B[0m");
            StringLocation loc;
            if (e instanceof ExprException le && (loc = le.getLocation()) != null) {
                System.out.println("\u001B[31m  " + loc.toStringFancy(10, true) + "\u001B[0m");
            }

            if (debug) {
                for (StackTraceElement elem : e.getStackTrace()) {
                    System.out.println("\u001B[31m   at " + elem + "\u001B[0m");
                }
            }
        }

        return null;
    }

    public static void main(String[] args) {
        new MathShellTest().test_shell();
    }

    @Test
    void test_shell() {
        // flags
        AtomicBoolean debug = new AtomicBoolean(false);
        AtomicBoolean exit  = new AtomicBoolean(false);

        // create context
        Context global = Context.newDefaultGlobal();
        ExpressionParser parser = new ExpressionParser()
                .inFile("<stdin>");

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
            ExpressionValue<?> val = eval(parser, c, input, debug.get());
            global.setValue("ans", val);
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
            System.out.println("| = \u001B[33m" + val + "\u001B[0m");
        }
    }

}
