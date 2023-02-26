package lox;

import lox.tokens.Token;
import lox.tokens.TokenScanner;
import lox.tokens.TokenType;
import lox.visitor.ASTPrinter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Lox {
    static boolean hadError = false;

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

        if(hadError) {
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

    private static void runInterpreter(String input) {
        var scanner = new TokenScanner(input);
        var tokens = scanner.scanTokens();
        var parser = new ASTParser(tokens);
        var ast = parser.parseTokens();

        if(hadError) return;

        var printer = new ASTPrinter();
        System.out.println(printer.printExpression(ast));
    }

    ///////////////////////// Error handling /////////////////////////
    public static void error(Token token, String errorMessage) {
        var where = token.getType() == TokenType.EOF ?
                        " at end" :
                        " at \"" + token.getLexeme() + "\"";
        reportError(token.getLine(), where, errorMessage);
    }

    public static void error(int line, String errorMessage) {
        reportError(line, "", errorMessage);
    }

    private static void reportError(int line, String where, String errorMessage) {
        System.err.println("[Line " + line + "] Error" + where + ": " + errorMessage);
        hadError = true;
    }
}
