package lox.expr;

import lox.visitor.Visitor;

public class Grouping extends Expression {

    final Expression expr;

    public Grouping(Expression expr) {
        this.expr = expr;
    }

    @Override
    public<R> R accept(Visitor<R> visitor) {
        return visitor.visitGrouping(this);
    }
}
