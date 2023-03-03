package lox.expr;

import lox.visitors.Visitor;

public abstract class Expression {
    public abstract<R> R accept(Visitor<R> visitor);
}
