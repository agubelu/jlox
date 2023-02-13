package tokens;

import static tokens.TokenType.*;
import lox.Lox;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenScanner {
    private static final Map<String, TokenType> keywords;
    private final String source;
    private final ArrayList<Token> tokens;
    private int start;
    private int current;
    private int line;

    public TokenScanner(String source) {
        this.source = source;
        this.tokens = new ArrayList<>();
        start = 0;
        current = 0;
        line = 1;
    }

    public List<Token> scanTokens() {
        while(!isAtEnd()) {
            // The next token begins here
            start = current;
            scanNextToken();
        }

        var eofToken = new Token(EOF, null, null, line);
        tokens.add(eofToken);
        return tokens;
    }

    private void scanNextToken() {
        char ch = consumeNextChar();
        switch(ch) {
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '.': addToken(DOT); break;
            case ',': addToken(COMMA); break;
            case ';': addToken(SEMICOLON); break;
            case '+': addToken(tokenIfNextEquals(PLUS, PLUS_EQUAL)); break;
            case '-': addToken(tokenIfNextEquals(MINUS, MINUS_EQUAL)); break;
            case '*': addToken(tokenIfNextEquals(ASTERISK, ASTERISK_EQUAL)); break;
            case '!': addToken(tokenIfNextEquals(NOT, NOT_EQUAL)); break;
            case '=': addToken(tokenIfNextEquals(EQUAL, EQUAL_EQUAL)); break;
            case '>': addToken(tokenIfNextEquals(GREATER, GREATER_EQUAL)); break;
            case '<': addToken(tokenIfNextEquals(LESS, LESS_EQUAL)); break;

            case '/':
                if(match('/')) {
                    while(!isAtEnd() && peekNextChar() != '\n') {
                        consumeNextChar();
                    }
                } else {
                    addToken(tokenIfNextEquals(SLASH, SLASH_EQUAL));
                }

                break;

            case '"': parseString(); break;

            // Ignore whitespaces
            case ' ':
            case '\t':
            case '\r':
                break;

            // Handle newlines
            case '\n':
                line++;
                break;

            // More generic cases such as numbers, identifiers and reserved words
            default:
                if(isDigit(ch)) {
                    // Number literal
                    parseNumber();
                } else if(isAlpha(ch)) {
                    // Identifier or keyword
                    parseIdentifierOrKeyword();
                } else {
                    // Unknown character
                    Lox.error(line, "Unexpected character: '" + ch + "'");
                }
        }
    }

    // Parses an identifier or keyword that must start with an alphabetic character or underscore
    private void parseIdentifierOrKeyword() {
        while(isAlphanumeric(peekNextChar())) {
            consumeNextChar();
        }

        var identifier = source.substring(start, current);
        // Is the identifier a language keyword?
        var tokenType = keywords.getOrDefault(identifier, IDENTIFIER);
        addToken(tokenType, identifier);
    }

    // Parses a literal number matching the pattern \d+(\.\d+)?
    private void parseNumber() {
        // Keep chomping all digits we find going forward
        while(isDigit(peekNextChar())) {
            consumeNextChar();
        }

        // Is the next character a dot? If so, is it followed by at least one number?
        if(peekNextChar() == '.' && isDigit(peekTwoForward())) {
            // Consume the dot and keep chomping numbers til the end
            consumeNextChar();
            while(isDigit(peekNextChar())) consumeNextChar();
        }

        // Parse the resulting floating number
        var number = Double.parseDouble(source.substring(start, current));
        addToken(NUMBER, number);
    }

    // Parses a string by skipping forward until we find the closing double quotes
    private void parseString() {
        while(!isAtEnd() && peekNextChar() != '"') {
            if(peekNextChar() == '\n') {
                line += 1;
            }
            consumeNextChar();
        }

        // Did we reach the end of the file without closing the string?
        if(isAtEnd()) {
            Lox.error(line, "Unterminated string reaching until the end of the file.");
            return;
        }

        // The string is OK, consume the closing quotes and trim both quotes away
        consumeNextChar();
        var string = source.substring(start + 1, current - 1);
        addToken(STRING, string);
    }

    private boolean isAtEnd() {
        return this.current >= this.source.length();
    }

    private char consumeNextChar() {
        return this.source.charAt(this.current++);
    }

    private char peekNextChar() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekTwoForward() {
        return current > source.length() - 2 ? '\0' : source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if(isAtEnd() || source.charAt(current) != expected) {
            return false;
        }

        current++;
        return true;
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        var text = source.substring(start, current);
        var token = new Token(type, text, literal, line);
        tokens.add(token);
    }

    private TokenType tokenIfNextEquals(TokenType ifAlone, TokenType ifEquals) {
        return match('=') ? ifEquals : ifAlone;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private static boolean isAlpha(char ch) {
        return ch == '_' || ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
    }

    private static boolean isAlphanumeric(char ch) {
        return isDigit(ch) || isAlpha(ch);
    }

    static {
        keywords = new HashMap<>();
        keywords.put("if", IF);
        keywords.put("else", ELSE);
        keywords.put("or", OR);
        keywords.put("and", AND);
        keywords.put("for", FOR);
        keywords.put("while", WHILE);
        keywords.put("null", NULL);
        keywords.put("class", CLASS);
        keywords.put("fn", FN);
        keywords.put("let", LET);
        keywords.put("true", TRUE);
        keywords.put("false", FALSE);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("this", THIS);
        keywords.put("super", SUPER);
    }
}
