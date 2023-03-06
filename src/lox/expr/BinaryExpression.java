package lox.expr;

import lox.tokens.Token;
import lox.visitors.ExpressionVisitor;

public class BinaryExpression extends Expression {

    public final Expression leftSide;
    public final Token operator;
    public final Expression rightSide;

    public BinaryExpression(Expression leftSide, Token operator, Expression rightSide) {
        this.leftSide = leftSide;
        this.operator = operator;
        this.rightSide = rightSide;
    }

    @Override
    public<R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitBinaryExpr(this);
    }
}
