package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

// import com.craftinginterpreters.lox.Token;
// import com.craftinginterpreters.lox.Scanner;

public class Lox {

  private static final Interpreter interpreter = new Interpreter();
  static boolean hadError = false;
  static boolean hadRuntimeError = false;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
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
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    // PRINT THE TOKENS
    // System.out.println("TOKENS");
    // for (Token token : tokens) {
    //   System.out.print(token);
    //   System.out.print(", ");
    // }
    // System.out.println();
    // System.out.println();

    Parser parser = new Parser(tokens);
    // Expr expression = parser.parse();
    List<Stmt> statements = parser.parse();

    // Stop if there was a syntax error.
    if (hadError) return;

    // PRINT THE AST
    // System.out.println("ASTS");
    // AstPrinter printer = new AstPrinter();
    // for (Stmt statement : statements) {
    //   if (statement instanceof Stmt.Expression) {
    //     Stmt.Expression expr = (Stmt.Expression) statement;
    //     System.out.println(printer.print(expr.expression));
    //   } else if (statement instanceof Stmt.Print) {
    //     Stmt.Print print = (Stmt.Print) statement;
    //     System.out.println(printer.print(print.expression));
    //   } else if (statement instanceof Stmt.Var) {
    //     Stmt.Var var = (Stmt.Var) statement;
    //     System.out.println(printer.print(var.initializer));
    //   }
    // }
    // System.out.println();
    // // System.out.println(new AstPrinter().print(expression));
    // // System.out.println();

    // INTERPRET THE EXPRESSION
    // interpreter.interpret(expression);
    System.out.println("OUTPUT");
    interpreter.interpret(statements);
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
