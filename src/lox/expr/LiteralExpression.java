package lox.expr;

import lox.tokens.Token;
import lox.visitor.Visitor;

public class LiteralExpression extends Expression {
    final Token literal;

    public LiteralExpression(Token literal) {
        this.literal = literal;
    }

    @Override
    public<R> R accept(Visitor<R> visitor) {
        return visitor.visitLiteralExpr(this);
    }
}
