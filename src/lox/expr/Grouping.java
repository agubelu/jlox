package lox.expr;

import lox.visitors.ExpressionVisitor;

public class Grouping extends Expression {

    public final Expression expr;

    public Grouping(Expression expr) {
        this.expr = expr;
    }

    @Override
    public<R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitGrouping(this);
    }
}
