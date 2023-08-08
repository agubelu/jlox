package lox.stmt;

import lox.expr.Expression;
import lox.tokens.Token;
import lox.visitors.StatementVisitor;

public class ReturnStmt extends Statement {

    public final Token keyword;
    public final Expression value;

    public ReturnStmt(Token keyword, Expression value) {
        this.keyword = keyword;
        this.value = value;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitReturnStmt(this);
    }
}
