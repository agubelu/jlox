package lox;

import lox.tokens.Token;
import lox.tokens.TokenScanner;
import lox.tokens.TokenType;
import lox.visitors.ASTPrinter;
import lox.visitors.Interpreter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Lox {
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script] OR jlox for live interpreter");
            System.exit(64);
        } else if(args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        var fileBytes = Files.readAllBytes(Paths.get(path));
        var fileContent = new String(fileBytes, Charset.defaultCharset());
        runInterpreter(fileContent);

        if(hadError || hadRuntimeError) {
            System.exit(1);
        }
    }

    private static void runPrompt() throws IOException {
        var in = new InputStreamReader(System.in);
        var reader = new BufferedReader(in);

        while(true) {
            System.out.print("> ");
            System.out.flush();

            String line = reader.readLine();
            if(line == null) {
                break;
            }

            runInterpreter(line);
        }
    }

    /**
     * Interprets a piece of Lox code, either from the REPL or from a file
     */
    private static void runInterpreter(String input) {
        var scanner = new TokenScanner(input);
        var tokens = scanner.scanTokens();
        var parser = new ASTParser(tokens);
        var expr = parser.parseTokens();

        if(hadError) return;

        var interpreter = new Interpreter();
        System.out.println(interpreter.interpret(expr));
    }

    ///////////////////////// Error handling /////////////////////////
    public static void error(Token token, String errorMessage) {
        var where = token.getType() == TokenType.EOF ?
                        " at the end of file" :
                        " at \"" + token.getLexeme() + "\"";
        reportError(token.getLine(), where, errorMessage);
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println("[Line " + error.token.getLine() + "] Runtime error: " + error.getMessage());
        hadRuntimeError = true;
    }

    public static void error(int line, String errorMessage) {
        reportError(line, "", errorMessage);
    }

    private static void reportError(int line, String where, String errorMessage) {
        System.err.println("[Line " + line + "] Error" + where + ": " + errorMessage);
        hadError = true;
    }
}
