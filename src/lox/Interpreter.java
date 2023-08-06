package lox;

import lox.decl.Declaration;
import lox.decl.StatementDecl;
import lox.decl.VariableDecl;
import lox.expr.*;
import lox.stmt.*;
import lox.tokens.Token;
import lox.visitors.DeclarationVisitor;
import lox.visitors.ExpressionVisitor;
import lox.visitors.StatementVisitor;

import java.util.List;

import static lox.tokens.TokenType.*;

/**
 * The Interpreter class is used to visit an Expression and compute its resulting
 * value, recursively interpreting any subexpressions and combining them
 * according to the operator in the parent expression.
 */
public class Interpreter implements ExpressionVisitor<Object>, StatementVisitor<Void>, DeclarationVisitor<Void> {

    private Environment environment;

    public Interpreter() {
        environment = new Environment();
    }

    public void interpret(List<Declaration> declarations) {
        try {
            for(Declaration decl : declarations) {
                execute(decl);
            }
        } catch(RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private void execute(Declaration decl) {
        decl.accept(this);
    }

    private void execute(Statement stmt) {
        stmt.accept(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Declaration visitors
    @Override
    public Void visitVariableDecl(VariableDecl decl) {
        Object value = null;
        if(decl.value != null) {
            value = evaluate(decl.value);
        }

        environment.declare(decl.identifier.getLexeme(), value);
        return null;
    }

    @Override
    public Void visitStatementDecl(StatementDecl decl) {
        return decl.stmt.accept(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Statement visitors

    @Override
    public Void visitBlock(Block block) {
        runBlock(block);
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        var condition = evaluate(ifStmt.condition);
        if(isTruthy(condition)) {
            execute(ifStmt.trueBranch);
        } else if(ifStmt.falseBranch != null) {
            execute(ifStmt.falseBranch);
        }

        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt stmt) {
        while(isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }

        return null;
    }

    @Override
    public Void visitPrintStmt(PrintStmt stmt) {
        var value = evaluate(stmt.expr);
        System.out.println(value);
        return null;
    }

    @Override
    public Void visitExpressionStmt(ExpressionStmt stmt) {
        evaluate(stmt.expr);
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Expression visitors

    /**
     * Evaluates an expression and returns its result.
     * This method does not catch RuntimeErrors and thus propagates them upwards.
     * This is the method that should be used internally when evaluating subexpressions,
     * since the whole expression must be discarded if a RuntimeError occurs anywhere inside it.
     */
    private Object evaluate(Expression expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitAssignmentExpr(AssignmentExpression assignExpr) {
        // This can be a direct assignment (=) or syntactic sugar for operation and assignment
        // i.e., += -= *= /=
        // In the latter case, we construct a binary expression with the corresponding
        // operator and evaluate it to get the result that will be assigned.

        var operatorType = assignExpr.operator.getType();
        Object value;

        if(operatorType == EQUAL) {
            value = evaluate(assignExpr.rightSide);
        } else {
            var binaryOpType = switch(operatorType) {
                case PLUS_EQUAL ->  PLUS;
                case MINUS_EQUAL -> MINUS;
                case ASTERISK_EQUAL -> ASTERISK;
                case SLASH_EQUAL -> SLASH;
                default -> throw new IllegalStateException("Unsupported assignment operator");
            };

            var leftSide = new VariableExpression(assignExpr.target);
            var binaryOp = new Token(binaryOpType);
            var expr = new BinaryExpression(leftSide, binaryOp, assignExpr.rightSide);
            value = evaluate(expr);
        }

        environment.assign(assignExpr.target, value);
        return value;
    }

    @Override
    public Object visitLiteralExpr(LiteralExpression literalExpr) {
        return literalExpr.literal;
    }

    @Override
    public Object visitGrouping(Grouping grouping) {
        return evaluate(grouping.expr);
    }

    @Override
    public Object visitVariableExpr(VariableExpression varExpr) {
        return environment.get(varExpr.identifier);
    }

    @Override
    public Object visitUnaryExpr(UnaryExpression unaryExpr) {
        var rightResult = evaluate(unaryExpr.rightSide);

        return switch(unaryExpr.operator.getType()) {
            case MINUS:
                ensureValueIsNumber(rightResult, unaryExpr.operator);
                yield -(Double)rightResult;
            case NOT:
                yield !isTruthy(rightResult);
            default:
                // Unreachable, all possible unary operators should have been covered
                throw new IllegalStateException("Unsupported unary operator: " + unaryExpr.operator);
        };
    }

    @Override
    public Object visitBinaryExpr(BinaryExpression binaryExpr) {
        var leftResult = evaluate(binaryExpr.leftSide);
        var rightResult = evaluate(binaryExpr.rightSide);

        return switch(binaryExpr.operator.getType()) {
            case EQUAL_EQUAL:
                yield valuesAreEqual(leftResult, rightResult);
            case NOT_EQUAL:
                yield !valuesAreEqual(leftResult, rightResult);
            case PLUS:
                // The plus operator is overloaded, compute it in its own function
                yield plus(leftResult, rightResult, binaryExpr.operator);
            case MINUS:
                ensureValuesAreNumbers(leftResult, rightResult, binaryExpr.operator);
                yield (Double) leftResult - (Double) rightResult;
            case SLASH:
                ensureValuesAreNumbers(leftResult, rightResult, binaryExpr.operator);
                yield (Double) leftResult / (Double) rightResult;
            case ASTERISK:
                ensureValuesAreNumbers(leftResult, rightResult, binaryExpr.operator);
                yield (Double) leftResult * (Double) rightResult;
            case PERCENT:
                ensureValuesAreNumbers(leftResult, rightResult, binaryExpr.operator);
                yield (Double) leftResult % (Double) rightResult;
            case LESS:
                ensureValuesAreNumbers(leftResult, rightResult, binaryExpr.operator);
                yield (Double) leftResult < (Double) rightResult;
            case LESS_EQUAL:
                ensureValuesAreNumbers(leftResult, rightResult, binaryExpr.operator);
                yield (Double) leftResult <= (Double) rightResult;
            case GREATER:
                ensureValuesAreNumbers(leftResult, rightResult, binaryExpr.operator);
                yield (Double) leftResult > (Double) rightResult;
            case GREATER_EQUAL:
                ensureValuesAreNumbers(leftResult, rightResult, binaryExpr.operator);
                yield (Double) leftResult >= (Double) rightResult;
            default:
                // Unreachable, all possible unary operators should have been covered
                throw new IllegalStateException("Unsupported binary operator: " + binaryExpr.operator);
        };
    }

    public Object visitLogicalExpr(LogicalExpression logicalExpr) {
        var leftResult = evaluate(logicalExpr.leftSide);
        var leftIsTruthy = isTruthy(leftResult);
        var opType = logicalExpr.operator.getType();

        // Short-circuit if left is true and it's an OR, or if it's false and it's an AND
        if((leftIsTruthy && opType == OR) || (!leftIsTruthy && opType == AND)) {
            return leftResult;
        }

        // In all other cases, the result is the truthiness of the right-hand operand
        // NOTE: this is only the case if we have only AND and OR operations. If more
        // logical operations are included, returning the right operand at this point
        // may not be the correct output.
        return evaluate(logicalExpr.rightSide);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Auxiliary private methods

    /**
     * Runs a block containing statements, taking care of replacing
     * and updating the environment as needed
     */
    private void runBlock(Block block) {
        var oldEnv = this.environment;
        this.environment = new Environment(oldEnv);

        try {
            for(Declaration decl : block.decls) {
                execute(decl);
            }
        } finally {
            this.environment = oldEnv;
        }
    }

    /**
     * Determines the truthiness of a value when implicitly converted to a boolean
     * via the ! operator. In Lox, everything is truthy except false and null.
     */
    private boolean isTruthy(Object o) {
        if(o == null) return false;
        if(o instanceof Boolean) return (boolean) o;
        return true;
    }

    /**
     * Ensures that the provided value is a number. All numbers in Lox are Doubles.
     * The token that contains the operator acting on the value is provided
     * to track down the error location when displaying it to the user.
     */
    private void ensureValueIsNumber(Object value, Token operator) {
        if(!(value instanceof Double)) {
            var name = value == null ? "null" : value.toString();
            throw new RuntimeError("Value is not a number: " + name, operator);
        }
    }

    /**
     * Similar to ensureValueIsNumber() but checks two values at the same time for convenience
     */
    private void ensureValuesAreNumbers(Object v1, Object v2, Token operator) {
        ensureValueIsNumber(v1, operator);
        ensureValueIsNumber(v2, operator);
    }

    /**
     * Auxiliary method for determining equality while avoiding NullPointerExceptions.
     * Two nulls are always the same, otherwise we rely on Java's equals() if the left
     * hand operand isn't null
     */
    private boolean valuesAreEqual(Object right, Object left) {
        if(right == null && left == null) return true;
        if(right == null) return false; // left is not null
        return right.equals(left);
    }

    /**
     * Aux method to execute the plus operator, which can either sum two numbers or
     * concatenate two strings. If the values have any other types, or an incompatible
     * type combination, this will raise a RuntimeError.
     */
    private Object plus(Object right, Object left, Token operator) {
        if(right instanceof Double && left instanceof Double) {
            return (Double) right + (Double) left;
        } else if(right instanceof String && left instanceof String) {
            return right + (String) left;
        }

        // Incompatible or non-supported types
        throw new RuntimeError("Operators for sum must be two numbers or two strings", operator);
    }
}
