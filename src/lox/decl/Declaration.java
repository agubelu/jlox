package lox.decl;

import lox.visitors.DeclarationVisitor;

public abstract class Declaration {
    public abstract<T> T accept(DeclarationVisitor<T> visitor);
}
