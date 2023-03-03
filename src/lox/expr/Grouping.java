package lox.expr;

import lox.visitors.Visitor;

public class Grouping extends Expression {

    public final Expression expr;

    public Grouping(Expression expr) {
        this.expr = expr;
    }

    @Override
    public<R> R accept(Visitor<R> visitor) {
        return visitor.visitGrouping(this);
    }
}
