package lox.tokens;

public enum TokenType {
    // Block delimiters
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,

    // General 1-character syntax elements
    COMMA, DOT, SEMICOLON,

    // Math operations
    PLUS, PLUS_EQUAL, MINUS, MINUS_EQUAL, ASTERISK, ASTERISK_EQUAL, SLASH, SLASH_EQUAL, PERCENT,

    // Comparators
    NOT, NOT_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,

    // Keywords
    IF, ELSE, OR, AND, FOR, WHILE, NULL, CLASS, FN, LET,
    TRUE, FALSE, RETURN, THIS, SUPER, BREAK,

    // Assorted literals
    IDENTIFIER, STRING, NUMBER,

    // EOF
    EOF,
}
