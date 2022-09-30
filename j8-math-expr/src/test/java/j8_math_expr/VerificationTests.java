package j8_math_expr;

import net.orbyfied.j8.math.expr.Context;
import net.orbyfied.j8.math.expr.ExpressionNode;
import net.orbyfied.j8.math.expr.ExpressionParser;
import net.orbyfied.j8.math.expr.node.ConstantNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VerificationTests {

    // the expression parser to use
    final ExpressionParser parser = new ExpressionParser();

    /* ------------ */

    @Test
    void testConstantExpression() {

        final String input = "3 * 6 + 5 * -9";
        final Context context = Context.newDefaultGlobal();
        ExpressionNode node = parser.parseString(input);

        final int cVal = ((3 * 6) + (5 * -9));
        assertTrue(node instanceof ConstantNode, "1: Constant Expression Optimization");
        assertEquals(cVal, node.evaluate(context).asDouble(), "1: Operator Priority");

        parser.withSetting("ConstantOptimization", false);
        node = parser.parseString(input);

        assertFalse(node instanceof ConstantNode, "2: Constant Expression Optimization Disabled");
        assertEquals(cVal, node.evaluate(context).asDouble(), "2: Result Stability Check");
        node = parser.parseString(input + " + PI");
        assertFalse(node instanceof ConstantNode, "3: Constant Expression Optimization Disabled");
        assertEquals(cVal + 3.141592653589793, node.evaluate(context).asDouble(), "3: Result Check");
        node = parser.parseString("PI");
        assertTrue(node instanceof ConstantNode, "3: Index Inlining Optimization");
        assertEquals(3.141592653589793, node.evaluate(context).asDouble(), "3: Result Check");

        parser.withSetting("IndexInline", false);
        node = parser.parseString("PI");
        assertFalse(node instanceof ConstantNode, "4: Index Inlining Optimization Disabled");
        assertEquals(3.141592653589793, node.evaluate(context).asDouble(), "4: Result Check");

    }

}
