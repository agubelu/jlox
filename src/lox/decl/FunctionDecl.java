package lox.decl;

import lox.stmt.Block;
import lox.tokens.Token;
import lox.visitors.DeclarationVisitor;

import java.util.List;

public class FunctionDecl extends Declaration {
    public final Token identifier;
    public final List<Token> parameters;
    public final Block body;

    public FunctionDecl(Token identifier, List<Token> parameters, Block body) {
        this.identifier = identifier;
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public <T> T accept(DeclarationVisitor<T> visitor) {
        return visitor.visitFunctionDecl(this);
    }
}
