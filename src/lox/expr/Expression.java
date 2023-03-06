package lox.expr;

import lox.visitors.ExpressionVisitor;

public abstract class Expression {
    public abstract<R> R accept(ExpressionVisitor<R> visitor);
}
