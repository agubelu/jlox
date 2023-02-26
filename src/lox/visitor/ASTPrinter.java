package lox.visitor;

import lox.expr.*;

public class ASTPrinter implements Visitor<String> {

    public String printExpression(Expression expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(BinaryExpression binaryExpr) {
        return "(" + binaryExpr.leftSide.accept(this) + " " +
                binaryExpr.operator.toString() + " " +
                binaryExpr.rightSide.accept(this) + ")";
    }

    @Override
    public String visitLiteralExpr(LiteralExpression literalExpr) {
        return literalExpr.literal.toString();
    }

    @Override
    public String visitUnaryExpr(UnaryExpression unaryExpr) {
        return "(" + unaryExpr.operator.toString() + " " + unaryExpr.rightSide.accept(this) + ")";
    }

    @Override
    public String visitGrouping(Grouping grouping) {
        return "(" + grouping.expr.accept(this) + ")";
    }
}
