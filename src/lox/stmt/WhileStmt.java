package lox.stmt;

import lox.expr.Expression;
import lox.visitors.StatementVisitor;

public class WhileStmt extends Statement {
    public final Expression condition;
    public final Statement body;

    public WhileStmt(Expression condition, Statement body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitWhileStmt(this);
    }
}
