package lox.expr;

import lox.tokens.Token;
import lox.visitors.Visitor;

public class UnaryExpression extends Expression {

    public final Token operator;
    public final Expression rightSide;

    public UnaryExpression(Token operator, Expression rightSide) {
        this.operator = operator;
        this.rightSide = rightSide;
    }

    @Override
    public<R> R accept(Visitor<R> visitor) {
        return visitor.visitUnaryExpr(this);
    }
}
