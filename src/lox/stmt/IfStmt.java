package lox.stmt;

import lox.expr.Expression;
import lox.visitors.StatementVisitor;

public class IfStmt extends Statement {

    public final Expression condition;
    public final Statement trueBranch;
    public final Statement falseBranch;

    public IfStmt(Expression condition, Statement trueBranch, Statement falseBranch) {
        this.condition = condition;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitIfStmt(this);
    }
}
