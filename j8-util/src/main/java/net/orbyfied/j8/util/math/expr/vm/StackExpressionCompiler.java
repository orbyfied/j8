package net.orbyfied.j8.util.math.expr.vm;

import net.orbyfied.j8.util.math.expr.node.BinOpNode;

import java.util.ArrayList;
import java.util.List;

import static net.orbyfied.j8.util.math.expr.vm.StackExprInst.*;

public class StackExpressionCompiler extends ExpressionCompiler<byte[], List<StackExprInst>> {

    @Override
    public List<StackExprInst> create() {
        return new ArrayList<>();
    }

    @Override
    public byte[] finish(List<StackExprInst> buffer) {
        return new byte[0];
    }

    public void compile$BinOp(BinOpNode node, List<StackExprInst> buffer) {
        compile(node.getLeft(),  buffer);
        compile(node.getRight(), buffer);
        short opcode = switch (node.getOp()) {
            case PLUS     -> OP_ADD;
            case MINUS    -> OP_SUB;
            case MULTIPLY -> OP_MUL;
            case DIVIDE   -> OP_DIV;
            case POW      -> OP_POW;
        };

        buffer.add(new StackExprInst(opcode));
    }

}
