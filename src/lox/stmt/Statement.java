package lox.stmt;

import lox.visitors.StatementVisitor;

public abstract class Statement {
    public abstract<T> T accept(StatementVisitor<T> visitor);
}
