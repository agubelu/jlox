package lox.stmt;

import lox.visitors.StatementVisitor;

public class BreakStmt extends Statement {

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitBreakStmt(this);
    }
}
