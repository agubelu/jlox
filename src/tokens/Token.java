package tokens;

public class Token {
    private TokenType type;
    private String lexeme;
    private Object literal;
    private int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return String.format("[%s] %s %s (line %s)", this.type, this.lexeme, this.literal, this.line);
    }
}
