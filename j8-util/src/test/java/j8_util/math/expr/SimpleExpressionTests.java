package j8_util.math.expr;

import net.orbyfied.j8.util.math.expr.Context;
import net.orbyfied.j8.util.math.expr.ExpressionFunction;
import net.orbyfied.j8.util.math.expr.ExpressionParser;
import net.orbyfied.j8.util.math.expr.ExpressionValue;
import org.junit.jupiter.api.Test;

public class SimpleExpressionTests {

    /* ------------------ 1 --------------------- */

    @Test
    void test_simpleLiteralTokens() {
        // input string
        final String str = "sqrt(9, 16)";

        // create parser and lex
        ExpressionParser parser = new ExpressionParser()
                .forString(str);

        try {
            parser
                    .lex()
                    .parse();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // print results
        System.out.println("SOURCE: | " + parser.getStrReader().getString());
        System.out.println("TOKENS: | " + parser.getTokens());
        if (parser.getAstNode() != null) {
            System.out.println("AST:    | " + parser.getAstNode());
            System.out.println("EVAL:   | " + parser.getAstNode().evaluate(
                    Context.newGlobal()
                            .setValue(ExpressionValue.of("sqrt"), ExpressionFunction.make(args ->
                                    ExpressionValue.ofDouble(Math.sqrt(args[0].getValueAs()) *
                                            args[1].getValueAs(Double.class))
                            ))
            ));
        }
    }

}
