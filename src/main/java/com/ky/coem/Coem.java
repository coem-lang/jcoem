/**
 * Runs Coem code as passed into the program.
 */

package com.ky.coem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Coem {

  static boolean hadError = false;
  static boolean hadRuntimeError = false;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jcoem [script]");
      System.exit(64); 
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));

    // Indicate an error in the exit code.
    if (hadError) System.exit(65);
    if (hadRuntimeError) System.exit(70);
  }

  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for (;;) { 
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null) break;
      run(line);
      hadError = false;
    }
  }

  private static void run(String source) {
    // turn source string into list of tokens
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    // (print the tokens)
    // for (Token token : tokens) {
    //   System.out.print(token);
    //   System.out.print(", ");
    // }
    // System.out.println();

    // turn list of tokens into list of statements
    Parser parser = new Parser(tokens);
    List<Stmt> statements = parser.parse();
    // for (Stmt stmt : statements) {
    //   System.out.println(stmt);
    // }

    // Stop if there was a syntax error.
    if (hadError) return;

    // (print the AST)
    AstPrinter printer = new AstPrinter();
    for (Stmt statement : statements) {
      System.out.println(printer.print(statement));
    }
    System.out.println();

    // interpret the statements
    Interpreter interpreter = new Interpreter(source);
    interpreter.interpret(statements);
    String echo = interpreter.getEcho();
    System.out.println(echo);
  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  private static void report(int line, String where,
                             String message) {
    System.err.println(
        "[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }

  static void error(Token token, String message) {
    if (token.type == TokenType.EOF) {
      report(token.line, " at end", message);
    } else {
      report(token.line, " at '" + token.lexeme + "'", message);
    }
  }

  static void runtimeError(RuntimeError error) {
    System.err.println(error.getMessage() +
        "\n[line " + error.token.line + "]");
    hadRuntimeError = true;
  }

}
