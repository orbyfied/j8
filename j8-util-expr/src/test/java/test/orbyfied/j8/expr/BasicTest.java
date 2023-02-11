package test.orbyfied.j8.expr;

import net.orbyfied.j8.expr.ExpressionParser;
import net.orbyfied.j8.expr.ast.ASTNode;
import net.orbyfied.j8.expr.ast.BinOpNode;
import net.orbyfied.j8.expr.ast.ConstantNode;
import net.orbyfied.j8.expr.ast.exec.ASTEvaluationContext;
import net.orbyfied.j8.expr.ast.exec.EvalValue;
import net.orbyfied.j8.expr.error.ExprException;
import net.orbyfied.j8.expr.parser.Operator;
import net.orbyfied.j8.tests.Benchmarks;
import org.junit.jupiter.api.Test;

public class BasicTest {

    @Test
    void test_BasicTokenize() {
        // create parser
        final ExpressionParser parser = new ExpressionParser();

        // specify string and tokenize
        try {
            final String str = "return 69 + (65563 - 420 * quandale)";
            parser.file("test", str).tokenize();
        } catch (Exception e) {
            ExprException.printFancy(System.out, e, true);
            return;
        }

        // check results
        System.out.println(parser.getTokens());
    }

    @Test
    void test_BasicParse() {
        // create parser
        final ExpressionParser parser = new ExpressionParser()
                .withSetting("ConstantOptimization", false);

        // set string
        parser.file("test", "f.a().b");

        try {
            System.out.println();
            System.out.println(parser.execute().getHeadNode());
            System.out.println();
        } catch (Exception e) {
            System.out.println();
            ExprException.printFancy(System.out, e, false);
            System.out.println();
        }
    }

    @Test
    void test_BasicExec() {
        // create parser
        final ExpressionParser parser = new ExpressionParser()
                .withSetting("ConstantOptimization", true);

        // specify string and parse
        try {
            final String str = "8 * (-2 + --4 * 9.9) + 0b1101 / 0x69 * 9 + (6 * 7 * (4 + 5.25))";
            parser.file("test", str).execute();

            // print node tree
            System.out.println("");
            System.out.println(parser.getHeadNode());

            // evaluate
            ASTEvaluationContext ctx = new ASTEvaluationContext();
            parser.getHeadNode().evaluate(ctx);
            System.out.println("-> " + ctx.popValue());
            System.out.println("");
        } catch (Exception e) {
            System.out.println();
            ExprException.printFancy(System.out, e, false);
            System.out.println();
            return;
        }
    }

    @Test
    void test_BasicEval() {
        ASTEvaluationContext ctx = new ASTEvaluationContext();
        new BinOpNode(Operator.ADD,
                new ConstantNode(new EvalValue<>(EvalValue.TYPE_NUMBER, 1.0)),
                new ConstantNode(new EvalValue<>(EvalValue.TYPE_NUMBER, 2.0))
        ).evaluate(ctx);
        System.out.println(ctx.popValue());
    }

    @Test
    void bench_EvalConstVSTree() {
        // the expression
        final String expr = "8 * (-2 + --4 * 9.9) + 0b1101 / 0x69 * 9 + (6 * 7 * (4 + 5.25))";

        // create parsers
        final ExpressionParser unoptimizedParser = new ExpressionParser()
                .withSetting("ConstantOptimization", false);
        final ExpressionParser optimizedParser = new ExpressionParser()
                .withSetting("ConstantOptimization", true);

        // parse values
        ASTNode unOptNode = unoptimizedParser.file(expr).tokenize().parse().getHeadNode();
        ASTNode optNode   = optimizedParser.file(expr).tokenize().parse().getHeadNode();

        // warm up
        ASTEvaluationContext wuContext = new ASTEvaluationContext();
        for (int i = 0; i < 100_000; i++) {
            optNode.evaluate(wuContext);
            wuContext.popValue();
        }

        {
            final ASTEvaluationContext context = new ASTEvaluationContext();
            Benchmarks.performBenchmark("UnoptimizedParserExec", i -> {
                unOptNode.evaluate(context);
                context.popValue();
            }, 100_000_000, 1_000_000_000).print();
        }

        {
            final ASTEvaluationContext context = new ASTEvaluationContext();
            Benchmarks.performBenchmark("OptimizedParserExec", i -> {
                optNode.evaluate(context);
                context.popValue();
            }, 100_000_000, 1_000_000_000).print();
        }
    }

}
