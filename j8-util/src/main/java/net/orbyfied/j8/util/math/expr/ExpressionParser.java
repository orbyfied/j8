package net.orbyfied.j8.util.math.expr;

import net.orbyfied.j8.util.Reader;
import net.orbyfied.j8.util.Sequence;
import net.orbyfied.j8.util.StringReader;
import net.orbyfied.j8.util.math.expr.error.ExprInterpreterException;
import net.orbyfied.j8.util.math.expr.error.ExprParserException;
import net.orbyfied.j8.util.math.expr.error.SyntaxError;
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

    // the file name
    String fn = "<in>";

    void tokenize() {
        // basic state checks
        if (strReader == null)
            return;

        // tokenize
        char c;
        int si;
        while ((c = strReader.current()) != StringReader.DONE) {
            // skip whitespace
            if (isWhitespace(c)) {
                strReader.next();
                continue;
            }

            // collect number literal
            if (isDigit(c, 10)) {
                si = strReader.index();

                // append token
                tokens.add(collectNumberLiteral().located(fn, strReader, si, strReader.index()));

                // continue, dont advance to next character
                // because the collection of the literal
                // already did that
                continue;
            }

            // parse operator
            Token<?> tk = null;
            si = strReader.index();
            Operator op = null;
            switch (c) {
                case '+' -> op = Operator.PLUS;
                case '-' -> {
                    if (strReader.peek(1) == '>') {
                        tk = new Token<>(Token.Type.ARROW);
                        strReader.next();
                    } else {
                        op = Operator.MINUS;
                    }
                }

                case '/' -> op = Operator.DIVIDE;
                case '*' -> op = Operator.MULTIPLY;
                case '^' -> op = Operator.POW;
            }

            if (op != null) {
                tokens.add(new Token<>(Token.Type.OPERATOR, op)
                        .located(new StringLocation(fn, strReader, si, strReader.index())));
                // continue
                strReader.next();
                continue;
            }

            // parse other symbols
            si = strReader.index();
            if (tk == null) {
                switch (c) {
                    case '(' -> tk = new Token<>(Token.Type.LEFT_PARENTHESIS);
                    case ')' -> tk = new Token<>(Token.Type.RIGHT_PARENTHESIS);
                    case ',' -> tk = new Token<>(Token.Type.COMMA);
                    case '.' -> tk = new Token<>(Token.Type.DOT);
                    case '=' -> tk = new Token<>(Token.Type.ASSIGN);
                    case ':' -> tk = new Token<>(Token.Type.COLON);
                }
            }

            if (tk != null) {
                tokens.add(tk
                        .located(new StringLocation(fn, strReader, si, strReader.index())));
                // continue
                strReader.next();
                continue;
            }

            // parse identifiers and keywords
            if (isFirstIdChar(c)) {
                si = strReader.index();

                // collect identifier token
                Token<?> itk = collectIdentifier();
                // switch for keywords
                tokens.add((switch (itk.getValueAs(String.class)) {
                    // check keywords
                    case "func" -> new Token<>(Token.Type.KW_FUNC);
                    // add identifier
                    default -> itk;
                }).located(new StringLocation(fn, strReader, si, strReader.index() - 1)));
                // continue, no need to advance
                // because collection already did
                continue;
            }

            // throw error
            throw new ExprParserException("unknown symbol while tokenizing")
                    .located(new StringLocation(fn, strReader, strReader.index(), strReader.index()));
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
                c1 -> c1 == '_',
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
                    .located(new StringLocation(fn, strReader.getString(), si, strReader.index() - 1));
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
        // get first expression
        if (tokenReader.current() == null)
            throw new SyntaxError("expected expression");
        ExpressionNode left = supplier.get();

        // for every operator
        Token<?> tok;
        while ((tok = tokenReader.current()) != null &&
                tok.getType() == Token.Type.OPERATOR &&
                ops.contains(tok.getValueAs(Operator.class))) {
            // get operator
            Operator op = tok.getValueAs();
            // get second operand
            tokenReader.next();
            if (tokenReader.current() == null)
                throw new SyntaxError("expected expression as second operand")
                .located(new StringLocation(tok.loc, tok.loc.startIndex + 1, tok.loc.endIndex + 1));
            ExpressionNode right = supplier.get();
            left = new BinOpNode(op, left, right)
                    .located(tok.loc);
        }

        // return
        return left;
    }

    private ConstantNode val$FuncDef() {
        // collect parameters
        final List<String> paramNames = new ArrayList<>();
        final List<String> paramTypes = new ArrayList<>();
        Token<?> t1;
        while ((t1 = tokenReader.current()) != null &&
                t1.type != Token.Type.RIGHT_PARENTHESIS) {
            tokenReader.next();
            if (tokenReader.current() == null)
                throw new SyntaxError("expected ')' to close function call");
            if (tokenReader.current().type == Token.Type.RIGHT_PARENTHESIS)
                break;
            if (tokenReader.current().type == Token.Type.COMMA)
                continue;

            // collect parameter
            paramNames.add(tokenReader.current().getValueAs(String.class));
            // collect type
            Token<?> tk;
            if ((tk = tokenReader.peek(1)) != null && tk.type == Token.Type.COLON) {
                tokenReader.next(2);
                if (tokenReader.current() == null || tokenReader.current().type != Token.Type.IDENTIFIER)
                    throw new SyntaxError("expected identifier to denote type after ':'")
                    .located(new StringLocation(tk.loc, tk.loc.getStartIndex() + 1, tk.loc.getEndIndex() + 1));

                String id = tokenReader.current().getValueAs();
                paramTypes.add(id);
            } else {
                paramTypes.add(null);
            }
        }

        tokenReader.next();

        // parse body expression
        final ExpressionNode body = node$Expr();
        // create node
        return new ConstantNode(ExpressionFunction.make((ctx, args) -> {
            // create local child context
            Context c = ctx.child(true);
            // check args
            if (args.length < paramNames.size() || args.length > paramNames.size())
                throw new ExprInterpreterException("Expected " + paramNames.size() + " parameters, got " + args.length);
            // set args in context
            int l = args.length;
            for (int i = 0; i < l; i++) {
                // set value
                c.setValueStrict(
                        new ExpressionValue<>(ExpressionValue.Type.STRING, paramNames.get(i)),
                        args[i]
                );
            }

            // invoke expression
            return body.evaluate(c);
        }, paramTypes));
    }

    private ExpressionNode node$Expr() {
        // parse function
        if (tokenReader.current() != null &&
                tokenReader.current().type == Token.Type.KW_FUNC) {
            StringLocation s = tokenReader.current().loc;
            // advance to parameters
            tokenReader.next();
            // make and return func
            return val$FuncDef().located(StringLocation.cover(s, tokenReader.peek(-1).loc));
        }

        return node$BinOp(this::node$Term, Set.of(Operator.PLUS, Operator.MINUS));
    }

    private ExpressionNode node$Term() {
        return node$BinOp(this::node$Factor, Set.of(Operator.MULTIPLY, Operator.DIVIDE, Operator.POW));
    }

    private ExpressionNode node$Factor() {
        // temporary node
        ExpressionNode tn;

        // return node
        ExpressionNode node = null;

        // get token
        Token<?> tok = tokenReader.current();
        if (tok != null) {
            // create identifier node
            ExpressionNode indexNode;

            // check for unary operator
            if (tok.getType() == Token.Type.OPERATOR && Set.of(
                    Operator.MINUS).contains(tok.getValueAs(Operator.class))) {
                StringLocation loc1 = tokenReader.current().loc;
                tokenReader.next();
                if (tokenReader.current() == null)
                    throw new SyntaxError("expected expression after unary operator")
                            .located(new StringLocation(loc1, loc1.startIndex + 1, loc1.endIndex + 1));
                ExpressionNode fac = node$Factor();
                node = new UnaryOpNode(tok.getValueAs(Operator.class), fac).located(loc1);
            }

            // check for more
            if (tok.getType() == Token.Type.IDENTIFIER) {
                indexNode = new IndexNode(new ReturnContextNode(),
                        new ConstantNode(new ExpressionValue<>(ExpressionValue.Type.STRING, tok.getValueAs())));

                Token<?> tko = tokenReader.current();
                Token<?> tk1;
                while ((tk1 = tokenReader.next()) != null &&
                        tk1.getType() == Token.Type.DOT) {
                    // advance and try collect identifier
                    StringLocation loc1 = tokenReader.current().loc;
                    tokenReader.next();
                    if (tokenReader.current() == null)
                        throw new SyntaxError("expected identifier as index")
                                .located(new StringLocation(loc1, loc1.endIndex + 1, loc1.endIndex + 1));
                    if (tokenReader.current().type != Token.Type.IDENTIFIER)
                        throw new SyntaxError("expected identifier as index")
                                .located(tokenReader.current().loc);
                    Token<?> idTok = tokenReader.current();
                    String id = idTok.getValueAs();

                    // update index node
                    indexNode = (IndexNode) new IndexNode(indexNode,
                            new ConstantNode(new ExpressionValue<>(ExpressionValue.Type.STRING, id))
                                    .located(idTok.loc))
                            .located(tk1.loc);
                }

                node = indexNode;
            }

            // check for number
            if ((tn = node$Number()) != null)
                node = tn;

            // check for expression
            if (tok.getType() == Token.Type.LEFT_PARENTHESIS) {
                tokenReader.next();
                if ((tn = node$Expr()) != null) {
                    if (tokenReader.current().getType() == Token.Type.RIGHT_PARENTHESIS) {
                        // register node
                        node = tn;
                        // advance past r paren
                        tokenReader.next();
                    } else {
                        throw new SyntaxError("expected ')' to end expression");
                    }
                }
            }

            // check function call
            if (tokenReader.current() != null &&
                    tokenReader.current().type == Token.Type.LEFT_PARENTHESIS) {
                StringLocation sl = tokenReader.current().loc;

                // collect parameters
                List<ExpressionNode> parameters = new ArrayList<>();
                Token<?> t1;
                while ((t1 = tokenReader.current()) != null &&
                        t1.type != Token.Type.RIGHT_PARENTHESIS) {
                    StringLocation loc1 = t1.loc;
                    tokenReader.next();
                    if (tokenReader.current() == null)
                        throw new SyntaxError("expected ')' to close function call")
                        .located(new StringLocation(loc1, loc1.endIndex + 1, loc1.endIndex + 1));
                    if (tokenReader.current().type == Token.Type.RIGHT_PARENTHESIS)
                        break;

                    if (tokenReader.current().type == Token.Type.COMMA)
                        tokenReader.next();

                    // collect parameter
                    parameters.add(node$Expr());
                }

                // return call node
                node = new CallNode(node, parameters).located(StringLocation
                        .cover(sl, tokenReader.current().loc));

                // advance past right parenthesis
                tokenReader.next();
            }

            if (tokenReader.current() != null &&
                    tokenReader.current().type == Token.Type.ASSIGN) {
                if (!(node instanceof IndexNode))
                    throw new SyntaxError("invalid indexing for assignment")
                    .located(tokenReader.current().loc);

                // skip to value token
                tokenReader.next();

                // get value node
                ExpressionNode value = node$Expr();

                // return
                node = new AssignNode(((IndexNode) node).src, ((IndexNode) node).index, value);
            }

            // return node
            return node;
        }

        // invalid value
        throw new SyntaxError("expected NUMBER_LITERAL, " +
                "OPERATOR or IDENTIFIER, got " + tokenReader.current()).located(tokenReader.current().loc);
    }

    private ExpressionNode node$Number() {
        Token<?> tok = tokenReader.current();
        if (tok != null && tok.getType() == Token.Type.NUMBER_LITERAL) {
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

    public ExpressionParser resetParsed() {
        this.tokens    = new ArrayList<>();
        this.astNode   = null;
        this.strReader = null;
        return this;
    }

    public ExpressionParser forString(String name) {
        resetParsed();
        this.strReader = new StringReader(name, 0);
        return this;
    }

    public ExpressionParser inFile(String fn) {
        this.fn = fn;
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
