package test.orbyfied.j8.expr;

import net.orbyfied.j8.expr.ExpressionParser;
import net.orbyfied.j8.expr.ast.BinOpNode;
import net.orbyfied.j8.expr.ast.ConstantNode;
import net.orbyfied.j8.expr.ast.exec.ASTEvaluationContext;
import net.orbyfied.j8.expr.ast.exec.EvalValue;
import net.orbyfied.j8.expr.error.ExprException;
import net.orbyfied.j8.expr.parser.Operator;
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
                .withSetting("ConstantOptimization", false);

        // specify string and parse
        try {
            final String str = "8 * (-2 + --4 * 9) + 0b1101 / 0x69 * 9 + (6 * 7 * (4 + 5)) eeee";
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

}
