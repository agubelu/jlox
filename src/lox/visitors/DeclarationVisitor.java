package lox.visitors;

import lox.decl.StatementDecl;
import lox.decl.VariableDecl;

public interface DeclarationVisitor<T> {
    T visitVariableDecl(VariableDecl decl);
    T visitStatementDecl(StatementDecl decl);
}
