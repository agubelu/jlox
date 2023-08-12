package lox;

import lox.decl.Declaration;
import lox.decl.FunctionDecl;
import lox.decl.StatementDecl;
import lox.decl.VariableDecl;
import lox.expr.*;
import lox.stmt.*;
import lox.tokens.Token;
import lox.visitors.DeclarationVisitor;
import lox.visitors.ExpressionVisitor;
import lox.visitors.StatementVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

enum FuncType { NONE, FUNCTION }

/** Implements different static analyses, such as variable resolution and keyword validity checks */
public class Analyzer implements ExpressionVisitor<Void>, StatementVisitor<Void>, DeclarationVisitor<Void> {

    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    // State variables to keep track of the current branch,
    // to check for the validity of returns and breaks
    private boolean isInLoop;
    private FuncType funcType;

    public Analyzer(Interpreter interpreter) {
        this.interpreter = interpreter;
        this.isInLoop = false;
        this.funcType = FuncType.NONE;
    }

    public void resolve(Declaration decl) {
        decl.accept(this);
    }

    public void resolve(Statement stmt) {
        stmt.accept(this);
    }

    public void resolve(Expression expr) {
        expr.accept(this);
    }

    public void resolve(List<Declaration> decls) {
        for(Declaration decl : decls) {
            resolve(decl);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Aux methods
    private void startScope() {
        this.scopes.push(new HashMap<>());
    }

    private void endScope() {
        this.scopes.pop();
    }

    // Declares a variable in the current scope, but marks it as uninitialized
    // to wait for its associated value to be resolved
    private void declare(Token var) {
        if(!scopes.empty()) {
            scopes.peek().put(var.getLexeme(), false);
        }
    }

    // Fully defines a previously declared variable, making it available for later use
    private void define(Token var) {
        if(!scopes.empty()) {
            scopes.peek().put(var.getLexeme(), true);
        }
    }

    // Tries to look up the occurrence of a local variable in the current scopes, starting with
    // the innermost one. If the check succeeds, it informs the interpreter of the nested depth
    // of said variable. If it fails, it is assumed to be a global variable and the check is
    // performed at runtime instead.
    private void resolveLocalVar(Expression expr, Token varToken) {
        String name = varToken.getLexeme();
        int i = this.scopes.size() - 1;

        while(i >= 0) {
            if(this.scopes.get(i).containsKey(name)) {
                var depth = this.scopes.size() - 1 - i;
                this.interpreter.addLocalResolution(expr, depth);
                break;
            }
            i--;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Visitor implementations

    @Override
    public Void visitVariableDecl(VariableDecl decl) {
        declare(decl.identifier);
        if(decl.value != null) {
            resolve(decl.value);
        }
        define(decl.identifier);
        return null;
    }

    @Override
    public Void visitStatementDecl(StatementDecl decl) {
        resolve(decl.stmt);
        return null;
    }

    @Override
    public Void visitFunctionDecl(FunctionDecl decl) {
        declare(decl.identifier);
        define(decl.identifier);

        startScope();
        for(Token param : decl.parameters) {
            declare(param);
            define(param);
        }

        // Function declarations reset the flag for being inside a loop,
        // since a function could be declared inside a loop but a break
        // wouldn't be allowed in the bare function body.
        var previousInLoop = this.isInLoop;
        var previousFuncType = this.funcType;
        this.funcType = FuncType.FUNCTION;
        this.isInLoop = false;

        // Important: we resolve the List<Declaration> inside the block, instead of the
        // block itself, to bypass the scope creation of visitBlock(), since the
        // parameters must be resolved in the same scope as the function body.
        resolve(decl.body.decls);

        this.isInLoop = previousInLoop;
        this.funcType = previousFuncType;
        endScope();

        return null;
    }

    @Override
    public Void visitBinaryExpr(BinaryExpr binaryExpr) {
        resolve(binaryExpr.leftSide);
        resolve(binaryExpr.rightSide);
        return null;
    }

    @Override
    public Void visitLiteralExpr(LiteralExpr literalExpr) {
        // Nothing to do.
        return null;
    }

    @Override
    public Void visitLogicalExpr(LogicalExpr logicalExpr) {
        resolve(logicalExpr.leftSide);
        resolve(logicalExpr.rightSide);
        return null;
    }

    @Override
    public Void visitUnaryExpr(UnaryExpr unaryExpr) {
        resolve(unaryExpr.rightSide);
        return null;
    }

    @Override
    public Void visitGrouping(GroupExpr groupExpr) {
        resolve(groupExpr.expr);
        return null;
    }

    @Override
    public Void visitVariableExpr(VariableExpr varExpr) {
        // First, check that the same variable in this scope isn't in an
        // uninitialized state, which would mean that it's being used
        // in its own initialization
        var varName = varExpr.identifier.getLexeme();

        // We check explicitly against "false" to guard against .get() returning null
        // if it's not yet defined
        if(!scopes.isEmpty() && scopes.peek().get(varName) == Boolean.FALSE) {
            Lox.error(varExpr.identifier, "Can't use a variable in its own initializer.");
        }

        resolveLocalVar(varExpr, varExpr.identifier);
        return null;
    }

    @Override
    public Void visitAssignmentExpr(AssignmentExpr assignExpr) {
        resolve(assignExpr.rightSide);
        resolveLocalVar(assignExpr, assignExpr.target);
        return null;
    }

    @Override
    public Void visitCallExpr(CallExpr callExpr) {
        resolve(callExpr.callee);
        for(Expression arg : callExpr.args) {
            resolve(arg);
        }
        return null;
    }

    @Override
    public Void visitBreakStmt(BreakStmt stmt) {
        // Check that "break" is inside a loop
        if(!this.isInLoop) {
            Lox.error(stmt.keyword, "\"break\" not allowed unless in loop body.");
        }
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt stmt) {
        // Check that the return is inside the body of a function
        if(this.funcType == FuncType.NONE) {
            Lox.error(stmt.keyword, "\"return\" not allowed outside function body.");
        }

        if(stmt.value != null) {
            resolve(stmt.value);
        }
        return null;
    }

    @Override
    public Void visitExpressionStmt(ExpressionStmt stmt) {
        resolve(stmt.expr);
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt stmt) {
        resolve(stmt.condition);
        resolve(stmt.trueBranch);
        if(stmt.falseBranch != null) {
            resolve(stmt.falseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt stmt) {
        resolve(stmt.condition);
        var previousInLoop = this.isInLoop;
        this.isInLoop = true;
        resolve(stmt.body);
        this.isInLoop = previousInLoop;
        return null;
    }

    @Override
    public Void visitBlock(Block block) {
        startScope();

        for(Declaration decl : block.decls) {
            resolve(decl);
        }

        endScope();
        return null;
    }
}
