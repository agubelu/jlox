package lox.visitors;

import lox.stmt.ExpressionStmt;
import lox.stmt.PrintStmt;
import lox.stmt.VariableDeclarationStmt;
import lox.stmt.Block;

public interface StatementVisitor<T> {

    T visitVariableDeclarationStmt(VariableDeclarationStmt stmt);
    T visitPrintStmt(PrintStmt stmt);
    T visitExpressionStmt(ExpressionStmt stmt);
    T visitBlock(Block block);
}
