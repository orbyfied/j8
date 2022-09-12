package net.orbyfied.j8.util.math.expr;

import net.orbyfied.j8.util.Reader;
import net.orbyfied.j8.util.Sequence;
import net.orbyfied.j8.util.StringReader;
import net.orbyfied.j8.util.math.expr.error.ExprInterpreterException;
import net.orbyfied.j8.util.math.expr.error.ExprParserException;
import net.orbyfied.j8.util.math.expr.node.*;

import java.util.*;
import java.util.function.Supplier;

/**
 * An expression parser.
 */
public class ExpressionParser {

    /*
     * ------ Lexer ------
     */

    private static final Set<Character> DIGITS_2  = Set.of('0', '1');
    private static final Set<Character> DIGITS_8  = Set.of('0', '1', '2', '3', '4', '5', '6', '7');
    private static final Set<Character> DIGITS_10 = Set.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
    private static final Set<Character> DIGITS_16 = Set.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                                                           'A', 'B', 'C', 'D', 'E', 'F');

    // the list of tokens
    List<Token<?>> tokens = new ArrayList<>();

    // the string reader
    StringReader strReader;

    void tokenize() {
        // basic state checks
        if (strReader == null)
            return;

        // tokenize
        char c;
        while ((c = strReader.current()) != StringReader.DONE) {
            // skip whitespace
            if (isWhitespace(c)) {
                strReader.next();
                continue;
            }

            // collect number literal
            if (isDigit(c, 10)) {
                // append token
                tokens.add(collectNumberLiteral());

                // continue, dont advance to next character
                // because the collection of the literal
                // already did that
                continue;
            }

            // parse operator
            Operator op = null;
            switch (c) {
                case '+' -> op = Operator.PLUS;
                case '-' -> op = Operator.MINUS;
                case '/' -> op = Operator.DIVIDE;
                case '*' -> op = Operator.MULTIPLY;
                case '^' -> op = Operator.POW;
            }

            if (op != null) {
                tokens.add(new Token<>(Token.Type.OPERATOR, op));
                // continue
                strReader.next();
                continue;
            }

            // parse other symbols
            Token<?> tk = null;
            switch (c) {
                case '(' -> tk = new Token<>(Token.Type.LEFT_PARENTHESIS);
                case ')' -> tk = new Token<>(Token.Type.RIGHT_PARENTHESIS);
                case ',' -> tk = new Token<>(Token.Type.COMMA);
                case '.' -> tk = new Token<>(Token.Type.DOT);
                case '=' -> tk = new Token<>(Token.Type.ASSIGN);
            }

            if (tk != null) {
                tokens.add(tk);
                // continue
                strReader.next();
                continue;
            }

            // parse identifiers and keywords
            if (isFirstIdChar(c)) {
                // collect identifier token
                Token<?> itk = collectIdentifier();
                // switch for keywords
                tokens.add(switch (itk.getValueAs(String.class)) {
                    // check keywords
                    case "func" -> new Token<>(Token.Type.KW_FUNC);
                    // add identifier
                    default -> itk;
                });
                // continue, no need to advance
                // because collection already did
                continue;
            }

            // throw error
            throw new ExprParserException("unknown symbol while lexing: '" + c + "' (i: " + strReader.index() + ")");
        }
    }

    boolean isFirstIdChar(char c) {
        char c1 = Character.toUpperCase(c);
        return (c1 >= 'A' && c1 <= 'Z') || c == '_';
    }

    boolean isIdChar(char c) {
        return isFirstIdChar(c) || isDigit(c, 10);
    }

    Token<String> collectIdentifier() {
        // collect while valid char
        String id = strReader.collect(this::isIdChar);

        // return token
        return new Token<>(Token.Type.IDENTIFIER, id);
    }

    Token<Double> collectNumberLiteral() {
        // start index of literal
        int si = strReader.index();

        // try to collect other bases
        int radix = 10;
        if (strReader.current() == '0') {
            char n = strReader.next();
            strReader.next(); // skip to literal for later parsing
            if (n == 'x')
                radix = 16;
            else if (n == 'o')
                radix = 8;
            else if (n == 'b')
                radix = 2;
            else {
                // go back 2 digits to backtrace the two characters skipped
                // if no base specifier was found
                strReader.prev(2);
            }
        }

        // collect number
        boolean[] hasPoint = new boolean[1];
        int finalRadix = radix;
        String ns = strReader.collect(c1 -> isDigit(c1, finalRadix) || c1 == '.',
                c1 -> c1 == '_' || c1 == ',',
                (c1 -> {
                    if (c1 == '.')
                        hasPoint[0] = true;
                }));

        // try to parse number and add token
        double v;
        try {
            if (hasPoint[0])
                v = Double.parseDouble(ns);
            else
                v = Long.parseLong(ns, radix);
        } catch (NumberFormatException e) {
            throw new ExprParserException("NumberFormatError: " + e.getMessage())
                    .located(new StringLocation(si, strReader.index() - 1));
        }

        // return
        return new Token<>(Token.Type.NUMBER_LITERAL, v);
    }

    private boolean isWhitespace(char c) {
        return c == ' ' || c == '\n' || c == '\t';
    }

    private boolean isDigit(char c, /* TODO: radix */ int radix) {
        char c1 = Character.toUpperCase(c);
        return switch (radix) {
            case 2 -> DIGITS_2.contains(c1);
            case 8 -> DIGITS_8.contains(c1);
            case 10 -> DIGITS_10.contains(c1);
            case 16 -> DIGITS_16.contains(c1);
            default -> false;
        };
    }

    /*
     * ------ Parsing ------
     */

    // the head AST node
    ExpressionNode astNode;

    // the token reader
    Reader<Token<?>> tokenReader;

    private ExpressionNode node$BinOp(Supplier<ExpressionNode> supplier, Set<Operator> ops) {
        ExpressionNode left = supplier.get();

        Token<?> tok;
        while ((tok = tokenReader.current()) != null &&
                tok.getType() == Token.Type.OPERATOR &&
                ops.contains(tok.getValueAs(Operator.class))) {
            Operator op = tok.getValueAs();
            tokenReader.next();
            ExpressionNode right = supplier.get();
            left = new BinOpNode(op, left, right);
        }

        // return
        return left;
    }

    private ConstantNode val$FuncDef() {
        // collect parameters
        final List<String> paramNames = new ArrayList<>();
        Token<?> t1;
        while ((t1 = tokenReader.current()) != null &&
                t1.type != Token.Type.RIGHT_PARENTHESIS) {
            tokenReader.next();
            if (tokenReader.current() == null)
                throw new ExprParserException("expected ')' to close function call");
            if (tokenReader.current().type == Token.Type.RIGHT_PARENTHESIS)
                break;
            if (tokenReader.current().type == Token.Type.COMMA)
                continue;

            // collect parameter
            paramNames.add(tokenReader.current().getValueAs(String.class));
        }

        tokenReader.next();

        // parse body expression
        final ExpressionNode body = node$Expr();
        // create node
        return new ConstantNode(ExpressionFunction.make((ctx, args) -> {
            Context c = ctx.child();
            if (args.length < paramNames.size())
                throw new ExprInterpreterException("Expected " + paramNames.size() + " parameters, got " + args.length);
            int l = args.length;
            for (int i = 0; i < l; i++)
                c.setValueStrict(
                        new ExpressionValue<>(ExpressionValue.Type.STRING, paramNames.get(i)),
                        args[i]
                );

            // call expression
            return body.evaluate(c);
        }));
    }

    private ExpressionNode node$Expr() {
        // parse function
        if (tokenReader.current() != null &&
                tokenReader.current().type == Token.Type.KW_FUNC) {
            // advance to parameters
            tokenReader.next();
            // make and return func
            return val$FuncDef();
        }

        return node$BinOp(this::node$Term, Set.of(Operator.PLUS, Operator.MINUS));
    }

    private ExpressionNode node$Term() {
        return node$BinOp(this::node$Factor, Set.of(Operator.MULTIPLY, Operator.DIVIDE, Operator.POW));
    }

    private ExpressionNode node$Factor() {
        ExpressionNode node;
        // check number literal
        if ((node = node$Number()) != null)
            return node;

        Token<?> tok = tokenReader.current();
        if (tok != null) {
            // check for global identifier index
            if (tok.getType() == Token.Type.IDENTIFIER) {
                // create identifier node
                IndexNode indexNode = new IndexNode(null,
                        new ConstantNode(new ExpressionValue<>(ExpressionValue.Type.STRING, tok.getValueAs())));
                ExpressionNode rnode = null;

                // check for more
                Token<?> tk1;
                while ((tk1 = tokenReader.next()) != null &&
                        tk1.getType() == Token.Type.DOT) {
                    // advance and try collect identifier
                    tokenReader.next();
                    if (tokenReader.current().type != Token.Type.IDENTIFIER)
                        throw new ExprParserException("expected identifier");
                    String id = tokenReader.current().getValueAs();

                    // update index node
                    indexNode = new IndexNode(indexNode,
                            new ConstantNode(new ExpressionValue<>(ExpressionValue.Type.STRING, id)));
                }

                rnode = indexNode;

                // check function call
                if (tokenReader.current() != null &&
                        tokenReader.current().type == Token.Type.LEFT_PARENTHESIS) {
                    // collect parameters
                    List<ExpressionNode> parameters = new ArrayList<>();
                    Token<?> t1;
                    while ((t1 = tokenReader.current()) != null &&
                            t1.type != Token.Type.RIGHT_PARENTHESIS) {
                        tokenReader.next();
                        if (tokenReader.current() == null)
                            throw new ExprParserException("expected ')' to close function call");
                        if (tokenReader.current().type == Token.Type.RIGHT_PARENTHESIS)
                            break;

                        // collect parameter
                        parameters.add(node$Expr());
                        tokenReader.prev();
                    }

                    // advance past right parenthesis
                    tokenReader.next();

                    // return call node
                    rnode = new CallNode(indexNode, parameters);
                }

                if (tokenReader.current() != null &&
                        tokenReader.current().type == Token.Type.ASSIGN) {
                    // skip to value token
                    tokenReader.next();

                    // get value node
                    ExpressionNode value = node$Expr();

                    // return
                    rnode = new AssignNode(indexNode.src, indexNode.index, value);
                }

                // return node
                return rnode;
            }

            // check for unary operator
            if (tok.getType() == Token.Type.OPERATOR && Set.of(
                    Operator.MINUS).contains(tok.getValueAs(Operator.class))) {
                tokenReader.next();
                ExpressionNode fac = node$Factor();
                return new UnaryOpNode(tok.getValueAs(Operator.class), fac);
            }

            // check for expression
            if (tok.getType() == Token.Type.LEFT_PARENTHESIS) {
                tokenReader.next();
                if ((node = node$Expr()) != null) {
                    if (tokenReader.current().getType() == Token.Type.RIGHT_PARENTHESIS) {
                        tokenReader.next();
                        return node;
                    } else {
                        throw new ExprParserException("Syntax Error: Expected ')' to end expression");
                    }
                }
            }
        }

        // invalid value
        throw new ExprParserException("Invalid Token: " + tokenReader.current() + ", expected NUMBER_LITERAL");
    }

    private ExpressionNode node$Number() {
        Token<?> tok = tokenReader.current();
        if (tok.getType() == Token.Type.NUMBER_LITERAL) {
            tokenReader.next();
            return new ConstantNode(
                    new ExpressionValue<>(ExpressionValue.Type.NUMBER, tok.getValueAs(Double.class))
            );
        }

        return null;
    }

    void parseTokens(List<Token<?>> tokens) {
        // create token reader
        tokenReader = new Reader<>(Sequence.ofList(tokens));

        // collect expression node and set
        astNode = node$Expr();
    }

    void parseAll() {
        // basic state checks
        if (strReader == null || tokens == null)
            return;

        // parse tokens into node
        parseTokens(tokens);
    }

    /*
     * ------ API ------
     */

    public ExpressionParser reset() {
        this.tokens  = new ArrayList<>();
        this.astNode = null;
        this.strReader = null;
        return this;
    }

    public ExpressionParser forString(String name) {
        reset();
        this.strReader = new StringReader(name, 0);
        return this;
    }

    public ExpressionValue<?> doString(Context ctx, String str) {
        forString(str)
                .lex()
                .parse();
        return astNode.evaluate(ctx);
    }

    public ExpressionParser lex() {
        tokenize();
        return this;
    }

    public ExpressionParser parse() {
        parseAll();
        return this;
    }

    public ExpressionNode getAstNode() {
        return astNode;
    }

    public StringReader getStrReader() {
        return strReader;
    }

    public List<Token<?>> getTokens() {
        return tokens;
    }

}
