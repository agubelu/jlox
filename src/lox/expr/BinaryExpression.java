package lox.expr;

import lox.tokens.Token;
import lox.visitor.Visitor;

public class BinaryExpression extends Expression {

    final Expression leftSide;
    final Token operator;
    final Expression rightSide;

    public BinaryExpression(Expression leftSide, Token operator, Expression rightSide) {
        this.leftSide = leftSide;
        this.operator = operator;
        this.rightSide = rightSide;
    }

    @Override
    public<R> R accept(Visitor<R> visitor) {
        return visitor.visitBinaryExpr(this);
    }
}
