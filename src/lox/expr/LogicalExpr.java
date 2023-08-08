package lox.expr;

import lox.tokens.Token;
import lox.visitors.ExpressionVisitor;

/**
    LogicalExpression stores boolean expressions, like "and" & "or". It is defined
    as a subclass of BinaryExpression because it should be evaluated a bit differently.
    The interpreter evaluates both sides of a BinaryExpression eagerly before producing
    a result, however, operands of a LogicalExpression can short-circuit, meaning that
    the right-hand one may not get evaluated if, for instance, it's an OR and the left
    operand is truthy. Giving them their own class warrants a separate method to
    evaluate them in the interpreter and keeps code a bit tidier.
 */
public class LogicalExpr extends BinaryExpr {


    public LogicalExpr(Expression leftSide, Token operator, Expression rightSide) {
        super(leftSide, operator, rightSide);
    }

    @Override
    public<R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitLogicalExpr(this);
    }
}
