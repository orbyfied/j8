package net.orbyfied.j8.expr;

import net.orbyfied.j8.expr.ast.ASTNode;
import net.orbyfied.j8.expr.ast.BinOpNode;
import net.orbyfied.j8.expr.ast.ConstantNode;
import net.orbyfied.j8.expr.ast.UnaryOpNode;
import net.orbyfied.j8.expr.ast.exec.ASTEvaluationContext;
import net.orbyfied.j8.expr.ast.exec.EvalValue;
import net.orbyfied.j8.expr.error.ExprParserException;
import net.orbyfied.j8.expr.error.SyntaxError;
import net.orbyfied.j8.expr.parser.Operator;
import net.orbyfied.j8.expr.parser.Token;
import net.orbyfied.j8.expr.parser.TokenType;
import net.orbyfied.j8.util.Reader;
import net.orbyfied.j8.util.Sequence;
import net.orbyfied.j8.util.StringReader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ExpressionParser {

    ///////////////////////////////////////////////////////////////
    /////// GENERAL STATE
    ///////////////////////////////////////////////////////////////

    // the current file name
    protected String fn;
    // the reader of the current string
    protected StringReader strReader;

    // ----- Settings

    public ExpressionParser withSetting(String str, boolean b) {
        try {
            Field f = getClass().getDeclaredField("setting" + str);
            f.setAccessible(true);
            f.set(this, b);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown setting for expression parser: " + str);
        }

        return this;
    }

    // the context to use for constant optimization
    private ASTEvaluationContext constOptCtx = new ASTEvaluationContext();

    private boolean settingConstantOptimization = true;

    ///////////////////////////////////////////////////////////////
    /////// TOKENIZER
    ///////////////////////////////////////////////////////////////

    // the result list of tokens
    protected List<Token<?>> tokens;

    public static boolean isValidIdentifierStart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    public static boolean isValidIdentifierCharacter(char c) {
        return isValidIdentifierStart(c) || (c >= '0' && c <= '9');
    }

    public ExpressionParser tokenize() {
        // check state
        if (strReader == null)
            throw new IllegalStateException("No string reader set");

        // reset token list
        tokens = new ArrayList<>();

        // tokenize
        char c;
        while ((c = strReader.current()) != StringReader.DONE) {
            // skip whitespace
            char wc;
            while ((wc = strReader.current()) == ' ' || wc == '\n' || wc == '\t')
                strReader.next();
            c = strReader.current();

            // check for number
            if (StringReader.isDigit(c, 10)) {
                int radix = 10;

                // check if it is a radix spec
                if (c == '0') {
                    int rc = switch (strReader.next()) {
                        case 'x' -> 16;
                        case 'b' -> 2;
                        case 'o' -> 8;
                        default  -> -1;
                    };

                    if (rc == -1) {
                        strReader.prev();
                    } else {
                        radix = rc;
                        strReader.next();
                    }
                }

                // collect number and add token
                double num;
                if (radix == 10) num = strReader.collectDouble();
                else             num = strReader.collectLong(radix);
                tokens.add(new Token<>(TokenType.NUMBER_LITERAL, num));

                continue;
            }

            // check for string literal
            if (c == '"' || c == '\'') {
                int si = strReader.index();

                strReader.next();
                StringBuilder b = new StringBuilder();
                char c1;
                while ((c1 = strReader.current()) != StringReader.DONE &&
                        c1 != c) {
                    b.append(c1);
                    strReader.next();
                }

                strReader.next();

                // add token
                tokens.add(new Token<>(TokenType.STRING_LITERAL, b.toString())
                        .located(fn, strReader, si, strReader.index() - 1));

                continue;
            }

            // check for identifier
            // or keywords (keywords
            // are parsed as reserved
            // identifiers)
            if (isValidIdentifierStart(c)) {
                // collect identifier
                int si = strReader.index();
                String id = strReader.collect(ExpressionParser::isValidIdentifierCharacter);
                StringLocation loc = new StringLocation(fn, strReader, si, strReader.index() - 1);
                Token<?> tk = switch (id) {
                    // ---- keywords
                    case "return" -> new Token<>(TokenType.K_RETURN).located(loc);

                    // ---- unreserved identifier
                    default -> new Token<>(TokenType.IDENTIFIER, id).located(loc);
                };

                // add token
                tokens.add(tk);
                continue;
            }

            // check for special characters
            {
                TokenType tt = switch (c) {
                    case '(' -> TokenType.LEFT_PAREN;
                    case ')' -> TokenType.RIGHT_PAREN;
                    default  -> null;
                };
                if (tt != null) {
                    tokens.add(new Token<>(tt).located(fn, strReader, strReader.index(), strReader.index()));
                    strReader.next();
                    continue;
                }
            }

            // check for operator
            {
                Operator operator = switch (c) {
                    case '+' -> Operator.ADD;
                    case '-' -> Operator.SUB;
                    case '/' -> Operator.DIV;
                    case '*' -> Operator.MUL;
                    default  -> null;
                };
                if (operator != null) {
                    tokens.add(new Token<>(TokenType.OPERATOR, operator).located(fn, strReader, strReader.index(), strReader.index()));
                    strReader.next();
                    continue;
                }
            }

            // throw error
            throw new ExprParserException("unknown symbol while tokenizing")
                    .located(new StringLocation(fn, strReader, strReader.index(), strReader.index()));
        }

        // return
        return this;
    }

    public List<Token<?>> getTokens() {
        return tokens;
    }

    ///////////////////////////////////////////////////////////////
    /////// AST PARSING
    ///////////////////////////////////////////////////////////////

    // operator sets
    private final Set<Operator> OP_SET_MUL = Set.of(Operator.MUL, Operator.DIV);
    private final Set<Operator> OP_SET_ADD = Set.of(Operator.ADD, Operator.SUB);

    // the token reader
    Reader<Token<?>> tokenReader;

    // the head ast node
    ASTNode head;

    private ASTNode node$BinOp(Supplier<ASTNode> supplier, Set<Operator> ops) {
        // get first expression
        if (tokenReader.current() == null)
            throw new SyntaxError("expected expression");
        ASTNode left = supplier.get();

        // for every operator
        Token<?> tok;
        while ((tok = tokenReader.current()) != null) {
            // check for operator
            Operator op;
            if (tok.getType() == TokenType.OPERATOR &&
                    ops.contains((op = tok.getValue(Operator.class)))) {
                // advance to second operand
                tokenReader.next();
            } else break;

            // get second operand
            if (tokenReader.current() == null)
                throw new SyntaxError("expected expression as second operand")
                        .located(new StringLocation(tok.loc, tok.loc.startIndex + 1, tok.loc.endIndex + 1));
            ASTNode right = supplier.get();
            left = new BinOpNode(op, left, right)
                    .located(tok.loc);

            // check if both nodes are constant
            // if so optimize it to a constant
            if (settingConstantOptimization) {
                BinOpNode lb = (BinOpNode) left;
                if (lb.getLeft() instanceof ConstantNode &&
                        lb.getRight() instanceof ConstantNode) {
                    // evaluate node and make constant
                    lb.evaluate(constOptCtx);
                    left = new ConstantNode(constOptCtx.popValue());
                }
            }
        }

        // return
        return left;
    }

    private ASTNode node$expr() {
        // collect bin op with terms
        return node$BinOp(this::node$term, OP_SET_ADD);
    }

    private ASTNode node$term() {
        // collect bin op with factors
        return node$BinOp(this::node$factor, OP_SET_MUL);
    }

    private ASTNode node$factor() {
        // get current
        final Token<?>  tok = tokenReader.current();
        if (tok != null) {
            final TokenType tt = tok.getType();

            // current node
            ASTNode node = null;

            // check for unary op
            if (tt == TokenType.OPERATOR) {
                // get operator
                Operator op = switch (tok.getValue(Operator.class)) {
                    case SUB -> Operator.NEGATE;
                    default  -> null;
                };

                // get operand
                tokenReader.next();
                ASTNode operand = node$factor();

                // create node
                ASTNode opNode = new UnaryOpNode(op, operand);

                // if constant optimize
                if (settingConstantOptimization && operand instanceof ConstantNode cn) {
                    opNode.evaluate(constOptCtx);
                    opNode = new ConstantNode(constOptCtx.popValue());
                }

                // return node
                return opNode;
            }

            // check and parse constant
            ASTNode numberNode;
            if ((numberNode = node$Number()) != null)
                node = numberNode;

            // check for expression
            if (tt == TokenType.LEFT_PAREN) {
                tokenReader.next();
                ASTNode tn;
                if ((tn = node$expr()) != null) {
                    if (tokenReader.current().getType() == TokenType.RIGHT_PAREN) {
                        // register node
                        node = tn;
                        // advance past r paren
                        tokenReader.next();
                    } else {
                        throw new SyntaxError("expected ')' to end expression");
                    }
                }
            }

            if (node == null) {
                // throw exception
                throw new SyntaxError("Expected NUMBER_LITERAL, OPERATOR, IDENTIFIER or LEFT_PAREN.")
                        .located(tok.loc);
            } else {
                return node;
            }
        }

        // throw exception
        throw new SyntaxError("Expected NUMBER_LITERAL, OPERATOR, IDENTIFIER or LEFT_PAREN. Got EOF");
    }

    private ASTNode node$Number() {
        Token<?> tok = tokenReader.current();
        if (tok != null && tok.getType() == TokenType.NUMBER_LITERAL) {
            tokenReader.next();
            return new ConstantNode(
                    new EvalValue<>(EvalValue.TYPE_NUMBER, tok.getValue(Double.class))
            );
        }

        return null;
    }

    public ExpressionParser parse() {
        // create token reader
        tokenReader = new Reader<>(Sequence.ofList(tokens));

        // parse head node
        head = node$expr();

        // return
        return this;
    }

    public ASTNode getHeadNode() {
        return head;
    }

    ///////////////////////////////////////////////////////////////
    /////// INTERFACE
    ///////////////////////////////////////////////////////////////

    public ExpressionParser execute() {
        return tokenize().parse();
    }

    public ExpressionParser file(StringReader reader) {
        return file("<unknown>", reader);
    }

    public ExpressionParser file(String string) {
        return file("<unknown>", string);
    }

    public ExpressionParser file(String fn, StringReader reader) {
        this.fn = fn;
        this.strReader = reader;
        return this;
    }

    public ExpressionParser file(String fn, String string) {
        return file(fn, new StringReader(string));
    }

}
