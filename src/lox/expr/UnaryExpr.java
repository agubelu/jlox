package lox.expr;

import lox.tokens.Token;
import lox.visitors.ExpressionVisitor;

public class UnaryExpr extends Expression {

    public final Token operator;
    public final Expression rightSide;

    public UnaryExpr(Token operator, Expression rightSide) {
        this.operator = operator;
        this.rightSide = rightSide;
    }

    @Override
    public<R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitUnaryExpr(this);
    }
}
