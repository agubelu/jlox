package lox;

import lox.tokens.Token;

public class RuntimeError extends RuntimeException {
    final Token token;

    public RuntimeError(String message, Token token) {
        super(message);
        this.token = token;
    }
}
