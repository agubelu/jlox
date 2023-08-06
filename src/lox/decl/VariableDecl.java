package lox.decl;

import lox.expr.Expression;
import lox.tokens.Token;
import lox.visitors.DeclarationVisitor;

public class VariableDecl extends Declaration {

    public final Token identifier;
    public final Expression value;

    public VariableDecl(Token identifier, Expression value) {
        this.identifier = identifier;
        this.value = value;
    }

    @Override
    public<T> T accept(DeclarationVisitor<T> visitor) {
        return visitor.visitVariableDecl(this);
    }

}
