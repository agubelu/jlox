package lox.visitors;

import lox.stmt.*;

public interface StatementVisitor<T> {

    T visitPrintStmt(PrintStmt stmt);
    T visitBreakStmt(BreakStmt stmt);
    T visitExpressionStmt(ExpressionStmt stmt);
    T visitIfStmt(IfStmt stmt);
    T visitWhileStmt(WhileStmt stmt);
    T visitBlock(Block block);
}
