package lox.stmt;

import lox.expr.Expression;
import lox.visitors.StatementVisitor;

public class ExpressionStmt extends Statement {

    public final Expression expr;

    public ExpressionStmt(Expression expr) {
        this.expr = expr;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitExpressionStmt(this);
    }
}
