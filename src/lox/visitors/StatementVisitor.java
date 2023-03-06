package lox.visitors;

import lox.stmt.ExpressionStmt;
import lox.stmt.PrintStmt;
import lox.stmt.VariableDeclarationStmt;

public interface StatementVisitor<T> {

    T visitVariableDeclarationStmt(VariableDeclarationStmt stmt);
    T visitPrintStmt(PrintStmt stmt);
    T visitExpressionStmt(ExpressionStmt stmt);
}
