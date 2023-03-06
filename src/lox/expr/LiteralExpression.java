package lox.expr;

import lox.visitors.ExpressionVisitor;

public class LiteralExpression extends Expression {
    public final Object literal;

    public LiteralExpression(Object literal) {
        this.literal = literal;
    }

    @Override
    public<R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitLiteralExpr(this);
    }
}
