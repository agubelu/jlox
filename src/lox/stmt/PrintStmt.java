package lox.stmt;

import lox.expr.Expression;
import lox.visitors.StatementVisitor;

public class PrintStmt extends Statement {

    public final Expression expr;

    public PrintStmt(Expression expr) {
        this.expr = expr;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitPrintStmt(this);
    }
}
