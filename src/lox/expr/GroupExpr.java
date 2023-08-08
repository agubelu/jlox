package lox.expr;

import lox.visitors.ExpressionVisitor;

public class GroupExpr extends Expression {

    public final Expression expr;

    public GroupExpr(Expression expr) {
        this.expr = expr;
    }

    @Override
    public<R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitGrouping(this);
    }
}
