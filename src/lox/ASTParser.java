package lox;

import lox.expr.*;
import lox.stmt.*;
import lox.decl.*;
import lox.tokens.Token;
import lox.tokens.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static lox.tokens.TokenType.*;
public class ASTParser {

    public static class ParseError extends RuntimeException { }
    final List<Token> tokens;
    int current;

    public ASTParser(List<Token> tokens) {
        this.tokens = tokens;
        this.current = 0;
    }

    public List<Declaration> parseTokens() {
        var declarations = new ArrayList<Declaration>();

        while(!isAtEnd()) {
            var decl = parseDeclaration();
            declarations.add(decl);
        }

        return declarations;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Recursive parsing methods for different levels of precedence
    /// First, declaration parsers

    private Declaration parseDeclaration() {
        try {
            if(match(LET)) {
                return parseVariableDecl();
            } else if(match(FN)) {
                return parseFunctionDecl("function");
            }

            return parseStatementDecl();
        } catch(ParseError err) {
            synchronize();
            return null;
        }
    }

    private Declaration parseVariableDecl() {
        // The LET token has been consumed by the call to parseVariableDecl()
        var identifier = consumeExpectedOrError(IDENTIFIER, "Expected identifier after 'let'");
        Expression value = null;
        if(match(EQUAL)) {
            value = parseExpression();
        }
        consumeExpectedOrError(SEMICOLON, "Expected semicolon after variable declaration");
        return new VariableDecl(identifier, value);
    }

    private Declaration parseFunctionDecl(String kind) {
        // FN has already been consumed
        var identifier = consumeExpectedOrError(IDENTIFIER, "Expected " + kind + " name");
        consumeExpectedOrError(LEFT_PAREN, "Expected '(' after " + kind + " name");

        List<Token> parameters = new ArrayList<>();
        if(!match(RIGHT_PAREN)) {
            do {
                var param = consumeExpectedOrError(IDENTIFIER, "Expected parameter name");
                parameters.add(param);
            } while(match(COMMA));
            consumeExpectedOrError(RIGHT_PAREN, "Expected ')' after parameter list");
        }

        // parseBlock expects the opening brace to be consumed
        consumeExpectedOrError(LEFT_BRACE, "Expected '{' before " + kind + " body");
        var body = new Block(parseBlock());

        return new FunctionDecl(identifier, parameters, body);
    }

    private Declaration parseStatementDecl() {
        var stmt = parseStatement();
        return new StatementDecl(stmt);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Next, statement parsers

    private Statement parseStatement() {
        if(match(BREAK)) {
            return parseBreakStatement();
        } else if (match(RETURN)) {
            return parseReturnStatement();
        } else if(match(LEFT_BRACE)) {
            return new Block(parseBlock());
        } else if(match(IF)) {
            return parseIfStatement();
        } else if(match(WHILE)) {
            return parseWhileStatement();
        } else if(match(FOR)) {
            return parseForStatement();
        } else {
            return parseExpressionStatement();
        }
    }

    private Statement parseBreakStatement() {
        var keyword = previousToken();
        consumeExpectedOrError(SEMICOLON, "Expected semicolon after 'break'");
        return new BreakStmt(keyword);
    }

    private Statement parseReturnStatement() {
        var keyword = previousToken();
        Expression value = null;
        if(!match(SEMICOLON)) {
            value = parseExpression();
            consumeExpectedOrError(SEMICOLON, "Expected semicolon after return");
        }

        return new ReturnStmt(keyword, value);
    }

    private Statement parseIfStatement() {
        consumeExpectedOrError(LEFT_PAREN, "Expected '(' after if");
        var condition = parseExpression();
        consumeExpectedOrError(RIGHT_PAREN, "Expected ')' after if condition");
        var trueBranch = parseStatement();

        // If there is an else branch, consume and store it
        Statement falseBranch = null;
        if(match(ELSE)) {
            falseBranch = parseStatement();
        }

        return new IfStmt(condition, trueBranch, falseBranch);
    }

    private Statement parseWhileStatement() {
        consumeExpectedOrError(LEFT_PAREN, "Expected '(' after while");
        var condition = parseExpression();
        consumeExpectedOrError(RIGHT_PAREN, "Expected ')' after while condition");
        var body = parseStatement();
        return new WhileStmt(condition, body);
    }

    /** De-sugars a for into a while statement. Returns a block which contains
     * the initializer and the WhileStmt. The While has its body modified to a
     * block that contains the original for body, and then the update.
     */
    private Statement parseForStatement() {
        consumeExpectedOrError(LEFT_PAREN, "Expected '(' after for");

        // The initializer can be an expression, a variable declaration, or empty
        // The two first ones will consume the expected semicolon after it.
        Declaration initializer;
        if(match(SEMICOLON)) {
            initializer = null;
        } else if(match(LET)) {
            initializer = parseVariableDecl();
        } else {
            initializer = new StatementDecl(parseExpressionStatement());
        }

        Expression condition = null;
        if(!match(SEMICOLON)) { // If there is a semicolon, it's consumed
            condition = parseExpression();
            consumeExpectedOrError(SEMICOLON, "Expected ';' after for condition");
        }

        Expression update = null;
        if(peekNextToken().getType() != RIGHT_PAREN) {
            update = parseExpression();
        }

        consumeExpectedOrError(RIGHT_PAREN, "Expected ')' after for");

        Statement body = parseStatement();

        // De-sugar the elements into a while. First, put the update at the end of the body
        var updateDecl = new StatementDecl(new ExpressionStmt(update));
        var bodyDecl = new StatementDecl(body);
        body = new Block(Arrays.asList(bodyDecl, updateDecl));

        // Then, construct the while with the given condition. If there is no condition,
        // provide "true" for an infinite loop.
        if(condition == null) {
            condition = new LiteralExpr(true);
        }
        body = new WhileStmt(condition, body);

        // Finally, put the initializer in front of the while, if it exists
        if(initializer != null) {
            var newBody = new StatementDecl(body);
            body = new Block(Arrays.asList(initializer, newBody));
        }

        return body;
    }

    private Statement parseExpressionStatement() {
        var expr = parseExpression();
        consumeExpectedOrError(SEMICOLON, "Expected semicolon after expression");
        return new ExpressionStmt(expr);
    }

    private ArrayList<Declaration> parseBlock() {
        var declarations = new ArrayList<Declaration>();

        while(peekNextToken().getType() != RIGHT_BRACE && !isAtEnd()) {
            declarations.add(parseDeclaration());
        }

        consumeExpectedOrError(RIGHT_BRACE, "Expected '}' after block");
        return declarations;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Finally, expression parsers

    private Expression parseExpression() {
        return parseAssignment();
    }

    private Expression parseAssignment() {
        var leftSide = parseOr();

        if(match(EQUAL, PLUS_EQUAL, MINUS_EQUAL, ASTERISK_EQUAL, SLASH_EQUAL)) {
            // We can only generate an Assignment expression if whatever is on the
            // left side is an expression that we can assign to. Also, we consume
            // the entire right side before checking, to avoid leaving the parser
            // in an invalid state if the following check fails.
            var operator = previousToken();
            var value = parseAssignment();

            if(leftSide instanceof VariableExpr) {
                var target = ((VariableExpr) leftSide).identifier;
                return new AssignmentExpr(target, operator, value);
            } else {
                Lox.error(previousToken(), "Invalid target for assignment");
            }
        }

        return leftSide;
    }

    private Expression parseOr() {
        var expr = parseAnd();

        while(match(OR)) {
            var operator = previousToken();
            var rightSide = parseAnd();
            expr = new LogicalExpr(expr, operator, rightSide);
        }

        return expr;
    }

    private Expression parseAnd() {
        var expr = parseEquality();

        while(match(AND)) {
            var operator = previousToken();
            var rightSide = parseEquality();
            expr = new LogicalExpr(expr, operator, rightSide);
        }

        return expr;
    }

    private Expression parseEquality() {
        var expr = parseComparison();

        while(match(EQUAL_EQUAL, NOT_EQUAL)) {
            var operator = previousToken();
            var rightSide = parseComparison();
            expr = new BinaryExpr(expr, operator, rightSide);
        }

        return expr;
    }

    private Expression parseComparison() {
        var expr = parseTerm();

        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            var operator = previousToken();
            var rightSide = parseTerm();
            expr = new BinaryExpr(expr, operator, rightSide);
        }

        return expr;
    }

    private Expression parseTerm() {
        var expr = parseFactor();

        while(match(MINUS, PLUS)) {
            var operator = previousToken();
            var rightSide = parseFactor();
            expr = new BinaryExpr(expr, operator, rightSide);
        }

        return expr;
    }

    private Expression parseFactor() {
        var expr = parseUnary();

        while(match(SLASH, ASTERISK, PERCENT)) {
            var operator = previousToken();
            var rightSide = parseUnary();
            expr = new BinaryExpr(expr, operator, rightSide);
        }

        return expr;
    }

    private Expression parseUnary() {
        if(match(NOT, MINUS)) {
            var operator = previousToken();
            var rightSide = parseUnary();
            return new UnaryExpr(operator, rightSide);
        } else {
            return parseCall();
        }
    }

    private Expression parseCall() {
        var call = parsePrimary();

        // Match series of subsequent calls after the identifier
        while(true) {
            if(match(LEFT_PAREN)) {
                call = finalizeCall(call);
            } else {
                break;
            }
        }

        return call;
    }

    private Expression parsePrimary() {
        if(match(TRUE)) return new LiteralExpr(true);
        if(match(FALSE)) return new LiteralExpr(false);
        if(match(NULL)) return new LiteralExpr(null);

        if(match(STRING, NUMBER)) {
            var literal = previousToken().getLiteral();
            return new LiteralExpr(literal);
        }

        if(match(LEFT_PAREN)) {
            var expr = parseExpression();
            consumeExpectedOrError(RIGHT_PAREN, "Missing closing parenthesis");
            return new GroupExpr(expr);
        }

        if(match(IDENTIFIER)) {
            var ident = previousToken();
            return new VariableExpr(ident);
        }

        // Unexpected token
        throw createError(peekNextToken(), "Unexpected token");
    }

    // Synchronizes the state of the parser after a syntax error, discarding tokens
    // until we hit a semicolon or a token that is likely to start a new statement
    private void synchronize() {
        consumeNextToken();

        while(!isAtEnd()) {
            if(previousToken().getType() == SEMICOLON) break;

            switch(peekNextToken().getType()) {
                case CLASS:
                case FN:
                case LET:
                case FOR:
                case IF:
                case WHILE:
                case RETURN:
                    break;
            }

            consumeNextToken();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Aux general methods

    // Parses the arguments of a call after the left parens has been consumed.
    // Receives the callee expression and returns a Call to said expression.
    private Expression finalizeCall(Expression callee) {
        List<Expression> args = new ArrayList<>();

        // Parse arguments only if there are any, i.e., the next token is not ')'
        if(!match(RIGHT_PAREN)) {
            do {
                var argument = parseExpression();
                args.add(argument);
            } while(match(COMMA));  // The condition check consumes the comma

            // Consume the closing parens, since it wasn't matched by the previous if
            consumeExpectedOrError(RIGHT_PAREN, "Expected closing parenthesis after function arguments");
        }

        var closingParens = previousToken();
        return new CallExpr(callee, args, closingParens);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Aux navigation methods
    private boolean isAtEnd() {
        return peekNextToken().getType() == EOF;
    }

    private Token peekNextToken() {
        return tokens.get(current);
    }

    private Token previousToken() {
        return tokens.get(current - 1);
    }

    private Token consumeNextToken() {
        if(isAtEnd()) return tokens.get(current);
        return tokens.get(current++);
    }

    private Token consumeExpectedOrError(TokenType expected, String errorMsg) {
        var nextToken = peekNextToken();
        if(nextToken.getType() == expected) {
            return consumeNextToken();
        } else {
            throw createError(nextToken, errorMsg);
        }
    }

    private boolean match(TokenType... types) {
        var nextToken = peekNextToken();
        var typeMatched = Arrays.stream(types).anyMatch(t -> t == nextToken.getType());

        if(typeMatched) {
            consumeNextToken();
        }
        return typeMatched;
    }

    private static ParseError createError(Token token, String errorMsg) {
        Lox.error(token, errorMsg);
        return new ParseError();
    }
}
