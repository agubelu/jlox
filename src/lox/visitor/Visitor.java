package lox.visitor;

import lox.expr.BinaryExpression;
import lox.expr.Grouping;
import lox.expr.LiteralExpression;
import lox.expr.UnaryExpression;

public interface Visitor<R> {
    public R visitBinaryExpr(BinaryExpression binaryExpr);
    public R visitLiteralExpr(LiteralExpression literalExpr);
    public R visitUnaryExpr(UnaryExpression unaryExpr);
    public R visitGrouping(Grouping grouping);
}
