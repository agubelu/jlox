package lox;

import tokens.TokenScanner;

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

        for(var token : tokens) {
            System.out.println(token);
        }
    }

    ///////////////////////// Error handling /////////////////////////
    public static void error(int line, String errorMessage) {
        reportError(line, "", errorMessage);
    }

    private static void reportError(int line, String where, String errorMessage) {
        System.err.println("[Line " + line + "] Error" + where + ": " + errorMessage);
        hadError = true;
    }
}
