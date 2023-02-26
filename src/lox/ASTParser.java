package lox;

import lox.expr.*;
import lox.tokens.Token;
import lox.tokens.TokenType;

import java.security.InvalidParameterException;
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

    public Expression parseTokens() {
        try {
            return parseExpression();
        } catch(ParseError err) {
            return null;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Recursive parsing methods for different levels of precedence

    private Expression parseExpression() {
        return parseEquality();
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
            consumeIfEquals(RIGHT_PAREN, "Missing closing parenthesis");
            return new Grouping(expr);
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
        if(!isAtEnd()) {
            current++;
        }
        return tokens.get(current);
    }

    private void consumeIfEquals(TokenType expected, String errorMsg) {
        var nextToken = peekNextToken();
        if(nextToken.getType() == expected) {
            consumeNextToken();
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
