package lox.expr;

import lox.visitors.ExpressionVisitor;

public class LiteralExpr extends Expression {
    public final Object literal;

    public LiteralExpr(Object literal) {
        this.literal = literal;
    }

    @Override
    public<R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitLiteralExpr(this);
    }
}
