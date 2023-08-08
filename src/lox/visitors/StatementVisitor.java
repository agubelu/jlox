package lox.visitors;

import lox.stmt.*;

public interface StatementVisitor<T> {

    T visitBreakStmt(BreakStmt stmt);
    T visitReturnStmt(ReturnStmt stmt);
    T visitExpressionStmt(ExpressionStmt stmt);
    T visitIfStmt(IfStmt stmt);
    T visitWhileStmt(WhileStmt stmt);
    T visitBlock(Block block);
}
