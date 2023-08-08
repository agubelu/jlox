package lox.expr;

import lox.tokens.Token;
import lox.visitors.ExpressionVisitor;

import java.util.List;

public class CallExpression extends Expression {

    public final Expression callee;
    public final List<Expression> args;
    // Stored to report errors in function calls
    public final Token closingParens;

    public CallExpression(Expression callee, List<Expression> args, Token closingParens) {
        this.callee = callee;
        this.args = args;
        this.closingParens = closingParens;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitCallExpr(this);
    }
}
