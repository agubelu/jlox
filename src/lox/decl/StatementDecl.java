package lox.decl;

import lox.stmt.Statement;
import lox.visitors.DeclarationVisitor;

public class StatementDecl extends Declaration {

    public final Statement stmt;

    public StatementDecl(Statement stmt) {
        this.stmt = stmt;
    }

    @Override
    public<T> T accept(DeclarationVisitor<T> visitor) {
        return visitor.visitStatementDecl(this);
    }
}
