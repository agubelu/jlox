package lox.expr;

import lox.visitor.Visitor;

public abstract class Expression {
    public abstract<R> R accept(Visitor<R> visitor);
}
