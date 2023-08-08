package lox.visitors;

import lox.expr.*;

public interface ExpressionVisitor<T> {
    T visitBinaryExpr(BinaryExpr binaryExpr);
    T visitLiteralExpr(LiteralExpr literalExpr);
    T visitLogicalExpr(LogicalExpr logicalExpr);
    T visitUnaryExpr(UnaryExpr unaryExpr);
    T visitGrouping(GroupExpr groupExpr);
    T visitVariableExpr(VariableExpr varExpr);
    T visitAssignmentExpr(AssignmentExpr assignExpr);
    T visitCallExpr(CallExpr callExpr);
}
