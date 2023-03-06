package lox.expr;

import lox.tokens.Token;
import lox.visitors.ExpressionVisitor;

public class AssignmentExpression extends Expression {
    public final Token target;
    public final Expression value;

    public AssignmentExpression(Token target, Expression value) {
        this.target = target;
        this.value = value;
    }


    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitAssignmentExpr(this);
    }
}
