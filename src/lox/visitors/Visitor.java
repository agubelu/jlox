package lox.visitors;

import lox.expr.BinaryExpression;
import lox.expr.Grouping;
import lox.expr.LiteralExpression;
import lox.expr.UnaryExpression;

public interface Visitor<T> {
    public T visitBinaryExpr(BinaryExpression binaryExpr);
    public T visitLiteralExpr(LiteralExpression literalExpr);
    public T visitUnaryExpr(UnaryExpression unaryExpr);
    public T visitGrouping(Grouping grouping);
}
