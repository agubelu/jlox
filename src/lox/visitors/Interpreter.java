package lox.visitors;

import lox.Lox;
import lox.RuntimeError;
import lox.expr.*;
import lox.tokens.Token;

/**
 * The Interpreter class is used to visit an Expression and compute its resulting
 * value, recursively interpreting any subexpressions and combining them
 * according to the operator in the parent expression.
 */
public class Interpreter implements Visitor<Object> {


    public String interpret(Expression expr) {
        try {
            var res = evaluate(expr);
            return res.toString();
        } catch(RuntimeError error) {
            Lox.runtimeError(error);
            return "";
        }
    }

    /**
     * Evaluates an expression and returns its result.
     * Contrary to interpret(), evaluate() does not catch RuntimeErrors and thus
     * propagates them upwards. This is the method that should be used internally
     * when evaluating subexpressions, since the whole expression must be discarded
     * if a RuntimeError occurs anywhere inside it.
     */
    private Object evaluate(Expression expr) {
        return expr.accept(this);
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
            throw new RuntimeError("Value is not a number: " + value.toString(), operator);
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
