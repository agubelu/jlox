package lox.expr;

import lox.tokens.Token;
import lox.visitors.ExpressionVisitor;

public class AssignmentExpression extends Expression {
    public final Token target;
    public final Token operator;
    public final Expression rightSide;

    public AssignmentExpression(Token target, Token operator, Expression rightSide) {
        this.target = target;
        this.operator = operator;
        this.rightSide = rightSide;
    }


    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitAssignmentExpr(this);
    }
}
