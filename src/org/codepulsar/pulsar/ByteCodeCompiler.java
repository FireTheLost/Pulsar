package org.codepulsar.pulsar;

import org.codepulsar.analysis.TypeChecker;
import org.codepulsar.analysis.Validator;
import org.codepulsar.ast.Expression;
import org.codepulsar.ast.Statement;
import org.codepulsar.ast.expressions.*;
import org.codepulsar.ast.statements.*;
import org.codepulsar.lang.*;
import org.codepulsar.lang.variables.GlobalVariable;
import org.codepulsar.lang.variables.LocalVariable;
import org.codepulsar.primitives.Primitive;
import org.codepulsar.primitives.PrimitiveType;
import org.codepulsar.primitives.types.*;
import org.codepulsar.util.ASTPrinter;

import java.util.ArrayList;

import static org.codepulsar.lang.ByteCode.*;

public class ByteCodeCompiler implements Expression.Visitor<Instruction>, Statement.Visitor<Void> {
    // Input Data
    private final String sourceCode;
    private Statement program;

    // Data To Help In Compiling To ByteCode
    private GlobalVariable globals;
    private LocalVariable locals;
    private final ArrayList<Primitive> values; // Constant Values To Be Stored

    // Output Data
    private final ArrayList<Instruction> instructions;
    private CompilerError errors;
    private CompilerError staticErrors;

    public ByteCodeCompiler(String sourceCode) {
        this.sourceCode = sourceCode;

        this.values = new ArrayList<>();

        this.instructions = new ArrayList<>();
    }

    public ArrayList<Instruction> compileByteCode() {
        Parser ast = new Parser(this.sourceCode);
        this.program = ast.parse();

        this.globals = ast.getGlobals();
        this.locals = ast.getLocals();

        this.errors = ast.getErrors();
        if (this.errors.hasError()) return instructions;

        ASTPrinter astPrinter = new ASTPrinter();
        astPrinter.print(this.program);

        TypeChecker analyzer = new TypeChecker(this.program, this.globals, this.locals);
        Validator validator = new Validator(this.program, this.globals, this.locals);

        analyzer.check();
        validator.validate();

        this.staticErrors = analyzer.getErrors();
        if (this.staticErrors.hasError()) return instructions;
        this.staticErrors = validator.getErrors();
        if (this.staticErrors.hasError()) return instructions;

        compile();
        
        return this.instructions;
    }

    private void compile() {
        if (this.program == null) {
            return;
        }

        this.program.accept(this);
    }

    public Void visitVariableStatement(Variable statement) {
        statement.getInitializer().accept(this);
        ByteCode type;

        if (statement.isGlobal()) {
            type = OP_NEW_GLOBAL;
        } else {
            type = OP_NEW_LOCAL;
        }

        makeOpCode(type, statement.getName(), statement.getLine());
        return null;
    }

    public Void visitBlockStatement(Block statement) {
        for (Statement stmt: statement.getStatements()) {
            stmt.accept(this);
        }

        return null;
    }

    public Void visitEndScopeStatement(EndScope statement) {
        for (int i = 0; i < statement.getLocalsToDelete(); i++) {
            makeOpCode(OP_POP, statement.getLine());
        }

        return null;
    }

    public Void visitWhileStatement(While statement) {
        int start = this.instructions.size();
        statement.getCondition().accept(this);

        int offset = makeJump(OP_JUMP_IF_FALSE, statement.getLine());
        makeOpCode(OP_POP, statement.getLine());

        statement.getStatements().accept(this);
        makeOpCode(OP_JUMP, start, statement.getStatements().getLine());

        fixJump(offset, OP_JUMP_IF_FALSE);
        makeOpCode(OP_POP, statement.getStatements().getLine());

        return null;
    }

    public Void visitIfStatement(If statement) {
        statement.getCondition().accept(this);
        int ifOffset = makeJump(OP_JUMP_IF_FALSE, statement.getLine());
        makeOpCode(OP_POP, statement.getLine());
        statement.getThenBranch().accept(this);

        int elseOffset = makeJump(OP_JUMP, statement.getThenBranch().getLine());
        fixJump(ifOffset, OP_JUMP_IF_FALSE);
        makeOpCode(OP_POP, statement.getThenBranch().getLine());

        if (statement.hasElse()) {
            statement.getElseBranch().accept(this);
        }

        fixJump(elseOffset, OP_JUMP);
        return null;
    }

    public Void visitNoneStatement(NoneStatement statement) {
        return null;
    }

    public Void visitPrintExpression(Print statement) {
        statement.getExpression().accept(this);
        makeOpCode(OP_PRINT, statement.getLine());

        return null;
    }

    public Void visitExpressionStatement(ExpressionStmt statement) {
        statement.getExpression().accept(this);
        makeOpCode(OP_POP, statement.getLine());

        return null;
    }

    // TODO Generate ByteCode For Functions
    public Void visitFunctionStatement(Function statement) {
        return null;
    }

    public Instruction visitAssignmentExpression(Assignment expression) {
        expression.getValue().accept(this);
        ByteCode type;

        if (expression.isGlobalAssignment()) {
            type = OP_STORE_GLOBAL;
            return makeOpCode(type, expression.getIdentifier(), expression.getLine());
        } else {
            type = OP_SET_LOCAL;
            return makeOpCode(type, expression.getNumber(), expression.getLine());
        }
    }

    public Instruction visitBinaryExpression(Binary expression) {
        expression.getLeft().accept(this);
        expression.getRight().accept(this);

        ArrayList<ByteCode> operators = identifyBinaryOperator(expression.getOperator());
        for (ByteCode op: operators) {
            makeOpCode(op, expression.getLine());
        }

        return null;
    }

    // TODO Generating ByteCode For Function Calls
    public Instruction visitCallExpression(Call expression) {
        return null;
    }

    public Instruction visitGroupingExpression(Grouping expression) {
        return expression.getExpression().accept(this);
    }

    public Instruction visitLiteralExpression(Literal expression) {
        String value = expression.getValue();
        return makeConstant(value, expression.getLine(), expression.getType());
    }

    public Instruction visitLogicalExpression(Logical expression) {
        expression.getLeft().accept(this);
        int line = expression.getLine();

        ByteCode jumpType = switch (expression.getOperator()) {
            case "&&" -> OP_JUMP_IF_FALSE;
            case "||" -> OP_JUMP_IF_TRUE;
            default -> null; // Unreachable
        };

        int offset = makeJump(jumpType, line);
        makeOpCode(OP_POP, line);

        expression.getRight().accept(this);
        fixJump(offset, jumpType);

        return null;
    }

    public Instruction visitNoneExpression(NoneExpression expression) {
        return null;
    }

    public Instruction visitUnaryExpression(Unary expression) {
        expression.getRight().accept(this);
        ByteCode operator = identifyUnaryOperator(expression.getOperator());
        return makeOpCode(operator, expression.getLine());
    }

    public Instruction visitVariableExpression(VariableAccess expression) {
        ByteCode type;

        if (expression.isGlobalVariable()) {
            type = OP_LOAD_GLOBAL;
            return makeOpCode(type, expression.getName(), expression.getLine());
        } else {
            type = OP_GET_LOCAL;
            return makeOpCode(type, expression.getNumber(), expression.getLine());
        }
    }

    private Instruction makeConstant(String value, int line, PrimitiveType type) {
        Primitive primitiveLiteral = null;

        switch (type) {
            case PR_INTEGER -> primitiveLiteral = new PInteger(Integer.parseInt(value));
            case PR_DOUBLE -> primitiveLiteral = new PDouble(Double.parseDouble(value));
            case PR_CHARACTER -> primitiveLiteral = new PCharacter(value.charAt(1));
            case PR_BOOLEAN -> primitiveLiteral = new PBoolean(Boolean.parseBoolean(value));
            case PR_NULL -> primitiveLiteral = new PNull();
        }

        this.values.add(primitiveLiteral);

        Instruction instruction = new Instruction(OP_CONSTANT, this.values.size() - 1, line);
        this.instructions.add(instruction);
        return instruction;
    }

    private Instruction makeOpCode(ByteCode opcode, int line) {
        Instruction instruction = new Instruction(opcode, null, line);
        this.instructions.add(instruction);
        return instruction;
    }

    private Instruction makeOpCode(ByteCode opcode, Object operand, int line) {
        Instruction instruction = new Instruction(opcode, operand, line);
        this.instructions.add(instruction);
        return instruction;
    }

    private int makeJump(ByteCode opcode, int line) {
        int size = this.instructions.size();

        makeOpCode(opcode, line);

        return size;
    }

    private void fixJump(int offset, ByteCode opcode) {
        Instruction oldJump = this.instructions.get(offset);
        int line = oldJump.getLine();
        Instruction jumpOpCode = new Instruction(opcode, this.instructions.size(), line);
        this.instructions.set(offset, jumpOpCode);
    }

    private ByteCode identifyUnaryOperator(String operator) {
        switch (operator) {
            case "-" -> { return OP_NEGATE; }
            case "!" -> { return OP_NOT; }

            // Unreachable
            default -> { return null; }
        }
    }

    private ArrayList<ByteCode> identifyBinaryOperator(String operator) {
        ArrayList<ByteCode> codes = new ArrayList<>();
        switch (operator) {
            case "+" -> codes.add(OP_ADD);
            case "-" -> codes.add(OP_SUBTRACT);
            case "*" -> codes.add(OP_MULTIPLY);
            case "/" -> codes.add(OP_DIVIDE);
            case "%" -> codes.add(OP_MODULO);

            case ">" -> codes.add(OP_COMPARE_GREATER);
            case ">=" -> {
                codes.add(OP_COMPARE_LESSER);
                codes.add(OP_NOT);
            }

            case "<" -> codes.add(OP_COMPARE_LESSER);
            case "<=" -> {
                codes.add(OP_COMPARE_GREATER);
                codes.add(OP_NOT);
            }

            case "==" -> codes.add(OP_COMPARE_EQUAL);
            case "!=" -> {
                codes.add(OP_COMPARE_EQUAL);
                codes.add(OP_NOT);
            }
        }

        return codes;
    }

    public ArrayList<Primitive> getValues() {
        return this.values;
    }

    public GlobalVariable getGlobals() {
        return this.globals;
    }

    public LocalVariable getLocals() {
        return this.locals;
    }

    public CompilerError getErrors() {
        return this.errors;
    }

    public CompilerError getStaticErrors() {
        return this.staticErrors;
    }
}
