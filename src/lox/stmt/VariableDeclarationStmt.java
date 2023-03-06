package lox.stmt;

import lox.expr.Expression;
import lox.tokens.Token;
import lox.visitors.StatementVisitor;

public class VariableDeclarationStmt extends Statement {

    public final Token identifier;
    public final Expression value;

    public VariableDeclarationStmt(Token identifier, Expression value) {
        this.identifier = identifier;
        this.value = value;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitVariableDeclarationStmt(this);
    }
}
