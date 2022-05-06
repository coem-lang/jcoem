/**
 * Takes a List of Tokens and returns a List of Stmts.
 */

package com.ky.coem;

import java.util.List;

import java.util.ArrayList;

import static com.ky.coem.TokenType.*;

class Parser {
  private static class ParseError extends RuntimeException {}

  private final List<Token> tokens;
  private int current = 0;
  private boolean isParamListStarted = false;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      while (check(NEWLINE)) {
        consume(NEWLINE, "Expect newline between statements.");
      }
      if (!isAtEnd()) {
        statements.add(declaration());
      }
    }

    return statements;
  }

  private Stmt declaration() {
    try {
      if (match(DAGGER)) return comment();
      if (match(POUND)) return directive();
      if (match(TO)) return function();
      if (match(LET)) return varDeclaration();
      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt.Directive directive() {
    if (match(IDENTIFIER, BE)) {
      Token name = previous();
      Token value = consume(IDENTIFIER, "Expect value after directive name.");
      return new Stmt.Directive(name, value);
    }

    throw error(peek(), "Expect directive name after pound.");
  }

  private Stmt.Comment comment() {
    Token text = advance();
    return new Stmt.Comment(text);
  }

  private Stmt.Function function() {
    Token name = consume(IDENTIFIER, "Expect function name.");
    consume(EMDASH, "Expect '—' after function name.");
    List<Token> parameters = new ArrayList<>();
    if (!check(EMDASH)) {
      do {
        if (parameters.size() >= 255) {
          error(peek(), "Can't have more than 255 parameters.");
        }

        parameters.add(consume(IDENTIFIER, "Expect identifier name."));
      } while (match(COMMA));
    }
    consume(EMDASH, "Expect '—' after parameters.");

    consume(COLON, "Expect ':' before function body.");
    List<Stmt> body = block();
    return new Stmt.Function(name, parameters, body);
  }

  private List<Stmt> block() {
    List<Stmt> statements = new ArrayList<>();

    while (check(NEWLINE)) {
      consume(NEWLINE, "Expect newline between statements.");
    }

    while (!check(DOT) && !isAtEnd()) {
      while (check(NEWLINE)) {
        consume(NEWLINE, "Expect newline between statements.");
      }
      if (!check(DOT) && !isAtEnd()) {
        statements.add(declaration());
      }
    }

    consume(DOT, "Expect '.' after block.");
    return statements;
  }

  private Stmt varDeclaration() {
    Token name = consume(IDENTIFIER, "Expect variable name.");

    Expr value = null;
    if (match(BE)) {
      value = expression();
    }

    return new Stmt.Var(name, value);
  }

  private Expr expression() {
    return or();
  }

  private Expr or() {
    Expr expr = and();

    while (match(OR)) {
      Token operator = previous();
      Expr right = and();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr and() {
    Expr expr = equality();

    while (match(AND)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr equality() {
    Expr expr = unary();

    // while (match(BANG_EQUAL, EQUAL_EQUAL, IS, AM, ARE)) {
    while (match(IS, AM, ARE)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr unary() {
    // if (match(BANG, MINUS, NOT)) {
    if (match(NOT)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

    return call();
  }

  private Expr call() {
    Expr expr = primary();

    if (!isParamListStarted) {
      while (true) {
        if (match(EMDASH)) {
          expr = finishCall(expr);
        } else {
          break;
        }
      }
    }

    return expr;
  }

  private Expr primary() {
    if (match(FALSE)) return new Expr.Literal(false);
    if (match(TRUE)) return new Expr.Literal(true);
    if (match(NOTHING)) return new Expr.Literal(null);

    if (match(STRING)) {
      return new Expr.Literal(previous().literal);
    }

    if (match(IDENTIFIER)) {
      return new Expr.Variable(previous(), previous().line);
    }

    throw error(peek(), "Expect expression.");
  }

  private Expr finishCall(Expr callee) {
    List<Expr> arguments = new ArrayList<>();

    if (!check(EMDASH)) {
      do {
        if (arguments.size() >= 255) {
          error(peek(), "Can't have more than 255 arguments.");
        }
        isParamListStarted = true;
        arguments.add(expression());
        isParamListStarted = false;
      } while (match(COMMA));
    }

    Token dash = consume(EMDASH, "Expect '—' after arguments.");

    return new Expr.Call(callee, dash, arguments);
  }

  private Stmt statement() {
    if (match(IF)) return ifStatement();
    if (match(AMPERSAND)) return returnStatement();
    if (match(WHILE)) return whileStatement();
    if (match(COLON)) return new Stmt.Block(block());

    return expressionStatement();
  }

  private Stmt ifStatement() {
    consume(EMDASH, "Expect '—' after 'if'.");
    isParamListStarted = true;
    Expr condition = expression();
    consume(EMDASH, "Expect '—' after if condition.");
    isParamListStarted = false;

    Stmt thenBranch = statement();
    Stmt elseBranch = null;
    if (match(ELSE)) {
      elseBranch = statement();
    }

    return new Stmt.If(condition, thenBranch, elseBranch);
  }

  private Stmt returnStatement() {
    Token ampersand = previous();
    Expr value = null;
    if (!check(NEWLINE)) {
      value = expression();
    }
    
    return new Stmt.Return(ampersand, value);
  }

  private Stmt whileStatement() {
    consume(EMDASH, "Expect '—' after 'while'.");
    isParamListStarted = true;
    Expr condition = expression();
    consume(EMDASH, "Expect '—' after condition.");
    isParamListStarted = false;
    Stmt body = statement();

    return new Stmt.While(condition, body);
  }

  private Stmt expressionStatement() {
    Expr expr = expression();

    // if it's a bare expression, print the expression
    // but don't print a print statement
    Expr wrapped = expr;
    if (expr instanceof Expr.Call) {
      // Expr.Call call = (Expr.Call)expr;
      // Expr.Variable callee = (Expr.Variable)call.callee;
      // String calleeName = callee.name.lexeme;
      // if (!(calleeName.equals("print") || calleeName.equals("know") || calleeName.equals("say"))) {
      //   wrapped = printExpression(expr);
      // }
    } else {
      wrapped = printExpression(expr);
    }

    return new Stmt.Expression(wrapped);
  }

  private Expr printExpression(Expr expr) {
    Token printToken = new Token(IDENTIFIER, "print", null, peek().line);
    Expr printExpr = new Expr.Variable(printToken, printToken.line);
    Token dash = new Token(EMDASH, "—", null, peek().line);
    List<Expr> arguments = new ArrayList<>();
    arguments.add(expr);
    Expr call = new Expr.Call(printExpr, dash, arguments);
    return call;
  }

  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }

    return false;
  }

  private Token consume(TokenType type, String message) {
    if (check(type)) return advance();

    throw error(peek(), message);
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) return false;
    return peek().type == type;
  }

  private Token advance() {
    if (!isAtEnd()) current++;
    return previous();
  }

  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private ParseError error(Token token, String message) {
    Coem.error(token, message);
    return new ParseError();
  }

  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == NEWLINE) return;

      switch (peek().type) {
        case TO:
        case LET:
        case IF:
        case WHILE:
        case AMPERSAND:
          return;
        default:
      }

      advance();
    }
  }
}
