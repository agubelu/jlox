package lox.expr;

import lox.tokens.Token;
import lox.visitor.Visitor;

public class LiteralExpression extends Expression {
    public final Object literal;

    public LiteralExpression(Object literal) {
        this.literal = literal;
    }

    @Override
    public<R> R accept(Visitor<R> visitor) {
        return visitor.visitLiteralExpr(this);
    }
}
