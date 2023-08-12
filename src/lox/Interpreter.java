package lox;

import lox.builtins.PrintFunc;
import lox.builtins.RandomFunc;
import lox.builtins.StrFunc;
import lox.builtins.TimeFunc;
import lox.callables.LoxCallable;
import lox.callables.LoxFunction;
import lox.decl.Declaration;
import lox.decl.FunctionDecl;
import lox.decl.StatementDecl;
import lox.decl.VariableDecl;
import lox.exceptions.BreakExc;
import lox.exceptions.ReturnExc;
import lox.expr.*;
import lox.stmt.*;
import lox.tokens.Token;
import lox.visitors.DeclarationVisitor;
import lox.visitors.ExpressionVisitor;
import lox.visitors.StatementVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lox.tokens.TokenType.*;

/**
 * The Interpreter class is used to visit an Expression and compute its resulting
 * value, recursively interpreting any subexpressions and combining them
 * according to the operator in the parent expression.
 */
public class Interpreter implements ExpressionVisitor<Object>, StatementVisitor<Void>, DeclarationVisitor<Void> {

    final Environment globals = new Environment();
    final Map<Expression, Integer> locals = new HashMap<>();
    private Environment environment = globals;

    public Interpreter() {
        globals.declare("print", new PrintFunc());
        globals.declare("time", new TimeFunc());
        globals.declare("str", new StrFunc());
        globals.declare("random", new RandomFunc());
    }

    public void interpret(List<Declaration> declarations) {
        try {
            for(Declaration decl : declarations) {
                execute(decl);
            }
        } catch(RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    public void addLocalResolution(Expression expr, int depth) {
        this.locals.put(expr, depth);
    }

    private void execute(Declaration decl) {
        decl.accept(this);
    }

    private void execute(Statement stmt) {
        stmt.accept(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Declaration visitors
    @Override
    public Void visitVariableDecl(VariableDecl decl) {
        Object value = null;
        if(decl.value != null) {
            value = evaluate(decl.value);
        }

        environment.declare(decl.identifier.getLexeme(), value);
        return null;
    }

    @Override
    public Void visitFunctionDecl(FunctionDecl decl) {
        var currentEnv = this.environment;
        var fnCallable = new LoxFunction(decl, currentEnv);
        this.environment.declare(decl.identifier.getLexeme(), fnCallable);
        return null;
    }

    @Override
    public Void visitStatementDecl(StatementDecl decl) {
        return decl.stmt.accept(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Statement visitors

    @Override
    public Void visitBlock(Block block) {
        var environ = new Environment(this.environment);
        runBlock(block, environ);
        return null;
    }

    @Override
    public Void visitBreakStmt(BreakStmt stmt) {
        throw new BreakExc();
    }

    @Override
    public Void visitReturnStmt(ReturnStmt stmt) {
        Object value = null;
        if(stmt.value != null) {
            value = evaluate(stmt.value);
        }

        throw new ReturnExc(value);
    }

    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        var condition = evaluate(ifStmt.condition);
        if(isTruthy(condition)) {
            execute(ifStmt.trueBranch);
        } else if(ifStmt.falseBranch != null) {
            execute(ifStmt.falseBranch);
        }

        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt stmt) {
        while(isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.body);
            } catch(BreakExc stop) {
                break;
            }
        }

        return null;
    }

    @Override
    public Void visitExpressionStmt(ExpressionStmt stmt) {
        evaluate(stmt.expr);
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Expression visitors

    /**
     * Evaluates an expression and returns its result.
     * This method does not catch RuntimeErrors and thus propagates them upwards.
     * This is the method that should be used internally when evaluating subexpressions,
     * since the whole expression must be discarded if a RuntimeError occurs anywhere inside it.
     */
    private Object evaluate(Expression expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitAssignmentExpr(AssignmentExpr assignExpr) {
        // This can be a direct assignment (=) or syntactic sugar for operation and assignment
        // i.e., += -= *= /=
        // In the latter case, we construct a binary expression with the corresponding
        // operator and evaluate it to get the result that will be assigned.

        var operatorType = assignExpr.operator.getType();
        Object value;

        if(operatorType == EQUAL) {
            value = evaluate(assignExpr.rightSide);
        } else {
            var binaryOpType = switch(operatorType) {
                case PLUS_EQUAL ->  PLUS;
                case MINUS_EQUAL -> MINUS;
                case ASTERISK_EQUAL -> ASTERISK;
                case SLASH_EQUAL -> SLASH;
                default -> throw new IllegalStateException("Unsupported assignment operator");
            };

            var leftSide = new VariableExpr(assignExpr.target);
            var binaryOp = new Token(binaryOpType);
            var expr = new BinaryExpr(leftSide, binaryOp, assignExpr.rightSide);
            value = evaluate(expr);
        }

        var depth = this.locals.get(assignExpr);
        if(depth != null) {
            environment.assignAt(assignExpr.target, value, depth);
        } else {
            globals.assign(assignExpr.target, value);
        }

        return value;
    }

    @Override
    public Object visitLiteralExpr(LiteralExpr literalExpr) {
        return literalExpr.literal;
    }

    @Override
    public Object visitGrouping(GroupExpr groupExpr) {
        return evaluate(groupExpr.expr);
    }

    @Override
    public Object visitVariableExpr(VariableExpr varExpr) {
        var depth = this.locals.get(varExpr);
        if(depth != null) {
            return environment.getAt(varExpr.identifier, depth);
        } else {
            return globals.get(varExpr.identifier);
        }
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr) {
        var rightResult = evaluate(unaryExpr.rightSide);

        return switch(unaryExpr.operator.getType()) {
            case MINUS:
                ensureValueIsNumber(rightResult, unaryExpr.operator);
                yield -(Double)rightResult;
            case NOT:
                yield !isTruthy(rightResult);
            default:
                // Unreachable, all possible unary operators should have been covered
                throw new IllegalStateException("Unsupported unary operator: " + unaryExpr.operator);
        };
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr) {
        var leftResult = evaluate(binaryExpr.leftSide);
        var rightResult = evaluate(binaryExpr.rightSide);

        return switch(binaryExpr.operator.getType()) {
            case EQUAL_EQUAL:
                yield valuesAreEqual(leftResult, rightResult);
            case NOT_EQUAL:
                yield !valuesAreEqual(leftResult, rightResult);
            case PLUS:
                // The plus operator is overloaded, compute it in its own function
                yield plus(leftResult, rightResult, binaryExpr.operator);
            case MINUS:
                ensureValuesAreNumbers(leftResult, rightResult, binaryExpr.operator);
                yield (Double) leftResult - (Double) rightResult;
            case SLASH:
                ensureValuesAreNumbers(leftResult, rightResult, binaryExpr.operator);
                yield (Double) leftResult / (Double) rightResult;
            case ASTERISK:
                ensureValuesAreNumbers(leftResult, rightResult, binaryExpr.operator);
                yield (Double) leftResult * (Double) rightResult;
            case PERCENT:
                ensureValuesAreNumbers(leftResult, rightResult, binaryExpr.operator);
                yield (Double) leftResult % (Double) rightResult;
            case LESS:
                ensureValuesAreNumbers(leftResult, rightResult, binaryExpr.operator);
                yield (Double) leftResult < (Double) rightResult;
            case LESS_EQUAL:
                ensureValuesAreNumbers(leftResult, rightResult, binaryExpr.operator);
                yield (Double) leftResult <= (Double) rightResult;
            case GREATER:
                ensureValuesAreNumbers(leftResult, rightResult, binaryExpr.operator);
                yield (Double) leftResult > (Double) rightResult;
            case GREATER_EQUAL:
                ensureValuesAreNumbers(leftResult, rightResult, binaryExpr.operator);
                yield (Double) leftResult >= (Double) rightResult;
            default:
                // Unreachable, all possible unary operators should have been covered
                throw new IllegalStateException("Unsupported binary operator: " + binaryExpr.operator);
        };
    }

    public Object visitLogicalExpr(LogicalExpr logicalExpr) {
        var leftResult = evaluate(logicalExpr.leftSide);
        var leftIsTruthy = isTruthy(leftResult);
        var opType = logicalExpr.operator.getType();

        // Short-circuit if left is true and it's an OR, or if it's false and it's an AND
        if((leftIsTruthy && opType == OR) || (!leftIsTruthy && opType == AND)) {
            return leftResult;
        }

        // In all other cases, the result is the truthiness of the right-hand operand
        // NOTE: this is only the case if we have only AND and OR operations. If more
        // logical operations are included, returning the right operand at this point
        // may not be the correct output.
        return evaluate(logicalExpr.rightSide);
    }

    public Object visitCallExpr(CallExpr callExpr) {
        var callee = evaluate(callExpr.callee);

        // Make sure that the callee evaluates in runtime to something that is indeed callable
        if(!(callee instanceof LoxCallable callable)) {
            throw new RuntimeError("The object is not callable", callExpr.closingParens);
        }

        // Make sure that the argument count matches the arity of the callable
        var arity = callable.getArity();
        var nArgs = callExpr.args.size();
        if(arity != nArgs) {
            throw new RuntimeError("Expected " + arity + " arguments, got " + nArgs + ".", callExpr.closingParens);
        }

        // Runtime checks passed, evaluate the arguments in order and store their results
        List<Object> argValues = new ArrayList<>();
        for(Expression argExpr : callExpr.args) {
            argValues.add(evaluate(argExpr));
        }

        return callable.call(this, argValues);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Auxiliary private methods

    /**
     * Runs a block containing statements using the provided environment, which
     * must be adequately updated by the caller beforehand.
     */
    public void runBlock(Block block, Environment newEnviron) {
        var oldEnviron = this.environment;
        this.environment = newEnviron;

        try {
            for(Declaration decl : block.decls) {
                execute(decl);
            }
        } finally {
            this.environment = oldEnviron;
        }
    }

    /**
     * Determines the truthiness of a value when implicitly converted to a boolean
     * via the ! operator. In Lox, everything is truthy except false and null.
     */
    private boolean isTruthy(Object o) {
        if(o == null) return false;
        if(o instanceof Boolean) return (boolean) o;
        return true;
    }

    /**
     * Ensures that the provided value is a number. All numbers in Lox are Doubles.
     * The token that contains the operator acting on the value is provided
     * to track down the error location when displaying it to the user.
     */
    private void ensureValueIsNumber(Object value, Token operator) {
        if(!(value instanceof Double)) {
            var name = value == null ? "null" : value.toString();
            throw new RuntimeError("Value is not a number: " + name, operator);
        }
    }

    /**
     * Similar to ensureValueIsNumber() but checks two values at the same time for convenience
     */
    private void ensureValuesAreNumbers(Object v1, Object v2, Token operator) {
        ensureValueIsNumber(v1, operator);
        ensureValueIsNumber(v2, operator);
    }

    /**
     * Auxiliary method for determining equality while avoiding NullPointerExceptions.
     * Two nulls are always the same, otherwise we rely on Java's equals() if the left
     * hand operand isn't null
     */
    private boolean valuesAreEqual(Object right, Object left) {
        if(right == null && left == null) return true;
        if(right == null) return false; // left is not null
        return right.equals(left);
    }

    /**
     * Aux method to execute the plus operator, which can either sum two numbers or
     * concatenate two strings. If the values have any other types, or an incompatible
     * type combination, this will raise a RuntimeError.
     */
    private Object plus(Object right, Object left, Token operator) {
        if(right instanceof Double && left instanceof Double) {
            return (Double) right + (Double) left;
        } else if(right instanceof String && left instanceof String) {
            return right + (String) left;
        }

        // Incompatible or non-supported types
        throw new RuntimeError("Operators for sum must be two numbers or two strings", operator);
    }
}
