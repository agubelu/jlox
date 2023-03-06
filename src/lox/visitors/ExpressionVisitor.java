package lox.visitors;

import lox.expr.*;

public interface ExpressionVisitor<T> {
    T visitBinaryExpr(BinaryExpression binaryExpr);
    T visitLiteralExpr(LiteralExpression literalExpr);
    T visitUnaryExpr(UnaryExpression unaryExpr);
    T visitGrouping(Grouping grouping);
    T visitVariableExpr(VariableExpression varExpr);
    T visitAssignmentExpr(AssignmentExpression assignExpr);
}
