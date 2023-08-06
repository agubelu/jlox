package lox.tokens;

public class Token {
    private TokenType type;
    private String lexeme;
    private Object literal;
    private int line;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public Token(TokenType type) {
        this.type = type;
        this.lexeme = null;
        this.literal = null;
        this.line = 0;
    }

    public TokenType getType() {
        return this.type;
    }

    public Object getLiteral() {
        return this.literal;
    }

    public int getLine() {
        return this.line;
    }

    public String getLexeme() {
        return this.lexeme;
    }

    public String toString() {
        return String.format("[%s] %s %s (line %s)", this.type, this.lexeme, this.literal, this.line);
    }
}
