package lox.expr;

import lox.tokens.Token;
import lox.visitor.Visitor;

public class UnaryExpression extends Expression {

    final Token operator;
    final Expression rightSide;

    public UnaryExpression(Token operator, Expression rightSide) {
        this.operator = operator;
        this.rightSide = rightSide;
    }

    @Override
    public<R> R accept(Visitor<R> visitor) {
        return visitor.visitUnaryExpr(this);
    }
}
