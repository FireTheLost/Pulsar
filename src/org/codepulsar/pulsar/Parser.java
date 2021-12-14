package org.codepulsar.pulsar;

import java.util.ArrayList;
import static org.codepulsar.pulsar.TokenType.*;
import static org.codepulsar.pulsar.ByteCode.*;

public class Parser {
    private final String sourceCode;
    private ArrayList<Token> tokens;
    private ArrayList<Instruction> instructions;
    public static ArrayList<LiteralRepresentation> values;
    private int current;
    ArrayList<Error> errors;
    boolean hasError;

    public Parser(String sourceCode) {
        this.sourceCode = sourceCode;
        this.tokens = new ArrayList<>();
        this.instructions = new ArrayList<>();
        this.values = new ArrayList<>();
        this.current = 0;
        this.errors = new ArrayList<>();
        this.hasError = false;
    }

    public ArrayList<Instruction> parse() {
        Lexer lexer = new Lexer(this.sourceCode + "\n");
        this.tokens = lexer.tokenize();

        if (SetUpKt.getDebug()) {
            Disassembler.tokens(this.tokens);
        }

        statement(TK_EOF);
        return this.instructions;
    }

    private void statement(TokenType notMatch) {
        while (!match(notMatch)) {
            baseStatement();
        }
    }

    private void baseStatement() {
        if (match(TK_IF)) {
            advance();
            ifStatement();
        } else {
            expressionStatement();
        }
    }

    private void ifStatement() {
        expression();
        int offset = makeJump(OP_JUMP_IF_FALSE);
        this.instructions.add(makeOpCode(OP_POP, peek().getLine()));
        block();

        int elseOffset = makeJump(OP_JUMP);
        fixJump(offset, OP_JUMP_IF_FALSE);

        this.instructions.add(makeOpCode(OP_POP, peek().getLine()));
        if (match(TK_ELSE)) {
            advance();
            if (match(TK_IF)) {
                advance();
                ifStatement();
            } else {
                block();
            }
        }
        fixJump(elseOffset, OP_JUMP);
    }

    private int makeJump(ByteCode opcode) {
        int size = this.instructions.size();

        this.instructions.add(makeOpCode(opcode, peek().getLine()));

        return size;
    }

    private void fixJump(int offset, ByteCode opcode) {
        Instruction oldJump = this.instructions.get(offset);
        int line = oldJump.getLine();
        Instruction jumpOpCode = new Instruction(opcode, this.instructions.size(), line);
        this.instructions.set(offset, jumpOpCode);
    }

    private void block() {
        if (!match(TK_LBRACE)) {
            setErrors("Missing Character", "Left Curly Brace Expected", peek());
            synchronize();
            return;
        }
        look(TK_LBRACE, "Left Curly Brace Expected", "Missing Character");
        statement(TK_RBRACE);
        look(TK_RBRACE, "Right Curly Brace Expected", "Missing Character");
    }

    private void expressionStatement() {
        expression();
        this.instructions.add(makeOpCode(OP_POP, peek().getLine()));
        look(TK_SEMICOLON, "A Semicolon Was Expected After The Expression", "Missing Character");
    }

    private void expression() {
        logicalOr();
    }

    private void logicalOr() {
        logicalAnd();
        int endOffset = -1;

        while (match(TK_LOGICALOR)) {
            if (peek().getTtype() == TK_LOGICALOR) {
                int line = peek().getLine();
                advance();
                endOffset = makeJump(OP_JUMP_IF_TRUE);
                this.instructions.add(makeOpCode(OP_POP, line));
                logicalAnd();
                fixJump(endOffset, OP_JUMP_IF_TRUE);
            }
        }
    }

    private void logicalAnd() {
        equality();
        int endOffset = -1;

        while (match(TK_LOGICALAND)) {
            if (peek().getTtype() == TK_LOGICALAND) {
                int line = peek().getLine();
                advance();
                endOffset = makeJump(OP_JUMP_IF_FALSE);
                this.instructions.add(makeOpCode(OP_POP, line));
                equality();
                fixJump(endOffset, OP_JUMP_IF_FALSE);
            }
        }

    }

    private void equality() {
        comparison();

        while (match(TK_EQUALEQUAL) || match(TK_NOTEQUAL)) {
            if (peek().getTtype() == TK_EQUALEQUAL) {
                int line = peek().getLine();
                advance();
                comparison();
                this.instructions.add(makeOpCode(OP_COMPARE_EQUAL, line));
            }
            if (peek().getTtype() == TK_NOTEQUAL) {
                int line = peek().getLine();
                advance();
                comparison();
                this.instructions.add(makeOpCode(OP_COMPARE_EQUAL, line));
                this.instructions.add(makeOpCode(OP_NOT, line));
            }
        }
    }

    private void comparison() {
        term();

        while (match(TK_GT) || match(TK_GTEQUAL) || match(TK_LT) || match(TK_LTEQUAL)) {
            if (peek().getTtype() == TK_GT) {
                int line = peek().getLine();
                advance();
                term();
                this.instructions.add(makeOpCode(OP_GREATER, line));
            }
            if (peek().getTtype() == TK_LT) {
                int line = peek().getLine();
                advance();
                term();
                this.instructions.add(makeOpCode(OP_LESSER, line));
            }
            if (peek().getTtype() == TK_LTEQUAL) {
                int line = peek().getLine();
                advance();
                term();
                this.instructions.add(makeOpCode(OP_GREATER, line));
                this.instructions.add(makeOpCode(OP_NOT, line));
            }
            if (peek().getTtype() == TK_GTEQUAL) {
                int line = peek().getLine();
                advance();
                term();
                this.instructions.add(makeOpCode(OP_LESSER, line));
                this.instructions.add(makeOpCode(OP_NOT, line));
            }
        }
    }

    private void term() {
        factor();

        while (match(TK_PLUS) || match(TK_MINUS)) {
            if (peek().getTtype() == TK_PLUS) {
                int line = peek().getLine();
                advance();
                factor();
                this.instructions.add(makeOpCode(OP_ADD, line));
            } else if (peek().getTtype() == TK_MINUS) {
                int line = peek().getLine();
                advance();
                factor();
                this.instructions.add(makeOpCode(OP_SUBTRACT, line));
            }
        }
    }

    private void factor() {
        unary();

        while (match(TK_MULTIPLICATION) || match(TK_DIVISION) || match(TK_MODULUS)) {
            if (peek().getTtype() == TK_MULTIPLICATION) {
                int line = peek().getLine();
                advance();
                unary();
                this.instructions.add(makeOpCode(OP_MULTIPLY, line));
            } else if (peek().getTtype() == TK_DIVISION) {
                int line = peek().getLine();
                advance();
                unary();
                this.instructions.add(makeOpCode(OP_DIVIDE, line));
            } else if (peek().getTtype() == TK_MODULUS) {
                int line = peek().getLine();
                advance();
                unary();
                this.instructions.add(makeOpCode(OP_MODULO, line));
            }
        }
    }

    private void unary() {
        if (match(TK_MINUS)) {
            int line = peek().getLine();
            advance();
            unary();
            this.instructions.add(makeOpCode(OP_NEGATE, line));
        } else if (match(TK_NOT)) {
            int line = peek().getLine();
            advance();
            unary();
            this.instructions.add(makeOpCode(OP_NOT, line));
        } else {
            primary();
        }
    }

    private void primary() {
        if (match(TK_INTEGER) || match(TK_DOUBLE)) {
            int line = peek().getLine();
            this.instructions.add(makeOpCode(OP_CONSTANT, line));
            advance();
        } else if (match(TK_LPAR)) {
            advance();
            expression();
            look(TK_RPAR, "Expected Character: ')'", "Missing Character");
        } else {
            setErrors("Missing Expression", "Expression Expected", peek());
        }
    }

    private Instruction makeOpCode(ByteCode opcode, int line) {
        if (opcode == OP_CONSTANT) {
            if (peek().getTtype() == TK_INTEGER) {
                LiteralRepresentation lr = new LiteralRepresentation(Integer.parseInt(peek().getLiteral()));
                this.values.add(lr);
            } else if (peek().getTtype() == TK_DOUBLE) {
                LiteralRepresentation lr = new LiteralRepresentation(Double.parseDouble(peek().getLiteral()));
                this.values.add(lr);
            }
            return new Instruction(OP_CONSTANT, this.values.size() - 1, line);
        } else {
            return new Instruction(opcode, null, line);
        }
    }

    private void setErrors(String etype, String message, Token token) {
        this.hasError = true;
        Error error = new Error(etype, message, token);
        this.errors.add(error);
    }

    private void look(TokenType token, String message, String errorType) {
        if (peek().getTtype() != token) {
            setErrors(errorType, message, peek());
            synchronize();
        } else {
            advance();
        }
    }

    private void synchronize() {
        while (peek().getTtype() != TK_EOF) {
            if (peek().getTtype() == TK_SEMICOLON) {
                advance();
                return;
            }
            switch (peek().getTtype()) {
                case TK_IF -> { return; }
            }

            advance();
        }
    }

    private boolean match(TokenType type) {
        return peek().getTtype() == type;
    }

    private Token peek() {
        return this.tokens.get(this.current);
    }

    private Token advance() {
        this.current++;
        return this.tokens.get(this.current - 1);
    }
}
