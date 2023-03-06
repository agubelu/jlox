package lox.expr;

import lox.tokens.Token;
import lox.visitors.ExpressionVisitor;

public class VariableExpression extends Expression {

    public Token identifier;

    public VariableExpression(Token identifier) {
        this.identifier = identifier;
    }

    @Override
    public<T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitVariableExpr(this);
    }
}
