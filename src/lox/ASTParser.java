package lox;

import lox.expr.*;
import lox.stmt.ExpressionStmt;
import lox.stmt.PrintStmt;
import lox.stmt.Statement;
import lox.stmt.VariableDeclarationStmt;
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

    public List<Statement> parseTokens() {
        var statements = new ArrayList<Statement>();

        while(!isAtEnd()) {
            var stmt = parseDeclaration();
            statements.add(stmt);
        }

        return statements;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Recursive parsing methods for different levels of precedence
    /// First, statement parsers

    private Statement parseDeclaration() {
        try {
            if(match(LET)) {
                return parseVariableStatement();
            }

            return parseStatement();
        } catch(ParseError err) {
            synchronize();
            return null;
        }
    }

    private Statement parseVariableStatement() {
        // The LET token has been consumed by the call to parseDeclaration()
        var identifier = consumeExpectedOrError(IDENTIFIER, "Expected identifier after 'let'");
        Expression value = null;
        if(match(EQUAL)) {
            value = parseExpression();
        }
        consumeExpectedOrError(SEMICOLON, "Expected semicolon after variable declaration");
        return new VariableDeclarationStmt(identifier, value);
    }

    private Statement parseStatement() {
        if(match(PRINT)) {
            return parsePrintStatement();
        } else {
            return parseExpressionStatement();
        }
    }

    private Statement parsePrintStatement() {
        var expr = parseExpression();
        consumeExpectedOrError(SEMICOLON, "Expected semicolon after expression to print");
        return new PrintStmt(expr);
    }

    private Statement parseExpressionStatement() {
        var expr = parseExpression();
        consumeExpectedOrError(SEMICOLON, "Expected semicolon after expression");
        return new ExpressionStmt(expr);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Expression parsers

    private Expression parseExpression() {
        return parseAssignment();
    }

    private Expression parseAssignment() {
        var leftSide = parseEquality();

        if(match(EQUAL)) {
            // We can only generate an Assignment expression if whatever is on the
            // left side is an expression that we can assign to. Also, we consume
            // the entire right side before checking, to avoid leaving the parser
            // in an invalid state if the following check fails.
            var value = parseAssignment();
            if(leftSide instanceof VariableExpression) {
                var target = ((VariableExpression) leftSide).identifier;
                return new AssignmentExpression(target, value);
            } else {
                Lox.error(previousToken(), "Invalid target for assignment");
            }
        }

        return leftSide;
    }

    private Expression parseEquality() {
        var expr = parseComparison();

        while(match(EQUAL_EQUAL, NOT_EQUAL)) {
            var operator = previousToken();
            var rightSide = parseComparison();
            expr = new BinaryExpression(expr, operator, rightSide);
        }

        return expr;
    }

    private Expression parseComparison() {
        var expr = parseTerm();

        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            var operator = previousToken();
            var rightSide = parseTerm();
            expr = new BinaryExpression(expr, operator, rightSide);
        }

        return expr;
    }

    private Expression parseTerm() {
        var expr = parseFactor();

        while(match(MINUS, PLUS)) {
            var operator = previousToken();
            var rightSide = parseFactor();
            expr = new BinaryExpression(expr, operator, rightSide);
        }

        return expr;
    }

    private Expression parseFactor() {
        var expr = parseUnary();

        while(match(SLASH, ASTERISK)) {
            var operator = previousToken();
            var rightSide = parseUnary();
            expr = new BinaryExpression(expr, operator, rightSide);
        }

        return expr;
    }

    private Expression parseUnary() {
        if(match(NOT, MINUS)) {
            var operator = previousToken();
            var rightSide = parseUnary();
            return new UnaryExpression(operator, rightSide);
        } else {
            return parsePrimary();
        }
    }

    private Expression parsePrimary() {
        if(match(TRUE)) return new LiteralExpression(true);
        if(match(FALSE)) return new LiteralExpression(false);
        if(match(NULL)) return new LiteralExpression(null);

        if(match(STRING, NUMBER)) {
            var literal = previousToken().getLiteral();
            return new LiteralExpression(literal);
        }

        if(match(LEFT_PAREN)) {
            var expr = parseExpression();
            consumeExpectedOrError(RIGHT_PAREN, "Missing closing parenthesis");
            return new Grouping(expr);
        }

        if(match(IDENTIFIER)) {
            var ident = previousToken();
            return new VariableExpression(ident);
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
                case PRINT:
                case RETURN:
                    break;
            }

            consumeNextToken();
        }
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
            var error = createError(nextToken, errorMsg);
            throw error;
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
