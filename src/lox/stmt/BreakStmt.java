package lox.stmt;

import lox.tokens.Token;
import lox.visitors.StatementVisitor;

public class BreakStmt extends Statement {

    public final Token keyword;

    public BreakStmt(Token keyword) {
        this.keyword = keyword;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitBreakStmt(this);
    }
}
