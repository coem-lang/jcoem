package com.ky.coem;

import java.util.List;

import java.util.ArrayList;

import static com.ky.coem.TokenType.*;

class Parser {
  private static class ParseError extends RuntimeException {}

  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  // // before implementing Stmt
  // Expr parse() {
  //   try {
  //     return expression();
  //   } catch (ParseError erro) {
  //     return null;
  //   }
  // }
  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      // statements.add(statement());
      statements.add(declaration());
    }

    return statements;
  }

  private Expr expression() {
    // return equality();
    return assignment();
  }

  private Stmt declaration() {
    try {
      if (match(TO)) return function("function");
      if (match(LET)) return varDeclaration();
      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt statement() {
    if (match(IF)) return ifStatement();
    if (match(PRINT, KNOW)) return printStatement();
    if (match(WHILE)) return whileStatement();
    // if (match(RETURN)) return returnStatement();
    if (match(AMPERSAND)) return returnStatement();
    // if (match(LEFT_BRACE)) return new Stmt.Block(block());
    if (match(COLON)) return new Stmt.Block(block());

    return expressionStatement();
  }

  private Stmt ifStatement() {
    // consume(LEFT_PAREN, "Expect '(' after 'if'.");
    consume(EM_DASH, "Expect '—' after 'if'.");
    Expr condition = expression();
    // consume(RIGHT_PAREN, "Expect ')' after if condition.");
    consume(EM_DASH, "Expect '—' after if condition.");

    Stmt thenBranch = statement();
    Stmt elseBranch = null;
    if (match(ELSE)) {
      elseBranch = statement();
    }

    return new Stmt.If(condition, thenBranch, elseBranch);
  }

  private Stmt printStatement() {
    Expr value = expression();
    consume(SEMICOLON, "Expect ';' after value.");
    // consume(NEWLINE, "Expect newline after value.");
    return new Stmt.Print(value);
  }

  private Stmt returnStatement() {
    Token keyword = previous();
    Expr value = null;
    if (!check(SEMICOLON)) {
    // if (!check(NEWLINE)) {
      value = expression();
    }
    
    consume(SEMICOLON, "Expect ';' after return value.");
    // consume(NEWLINE, "Expect newline after return value.");
    return new Stmt.Return(keyword, value);
  }

  private Stmt varDeclaration() {
    Token name = consume(IDENTIFIER, "Expect variable name.");

    Expr initializer = null;
    if (match(BE)) {
      initializer = expression();
    }

    consume(SEMICOLON, "Expect ';' after variable declaration.");
    // consume(NEWLINE, "Expect newline after variable declaration.");
    return new Stmt.Var(name, initializer);
  }

  private Stmt whileStatement() {
    consume(EM_DASH, "Expect '—' after 'while'.");
    Expr condition = expression();
    consume(EM_DASH, "Expect '—' after condition.");
    Stmt body = statement();

    return new Stmt.While(condition, body);
  }

  private Stmt expressionStatement() {
    Expr expr = expression();
    consume(SEMICOLON, "Expect ';' after value.");
    // consume(NEWLINE, "Expect newline after value.");
    return new Stmt.Expression(expr);
  }

  private Stmt.Function function(String kind) {
    Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
    consume(EM_DASH, "Expect '—' after " + kind + " name.");
    List<Token> parameters = new ArrayList<>();
    if (!check(EM_DASH)) {
      do {
        if (parameters.size() >= 255) {
          error(peek(), "Can't have more than 255 parameters.");
        }

        parameters.add(consume(IDENTIFIER, "Expect identifier name."));
      } while (match(COMMA));
    }
    consume(EM_DASH, "Expect '—' after parameters.");

    // consume(LEFT_BRACE, "Expect '{' before " + kind + " name.");
    consume(COLON, "Expect ':' before " + kind + " name.");
    List<Stmt> body = block();
    return new Stmt.Function(name, parameters, body);
  }

  private List<Stmt> block() {
    List<Stmt> statements = new ArrayList<>();

    // while (!check(RIGHT_BRACE) && !isAtEnd()) {
    while (!check(DOT) && !isAtEnd()) {
      statements.add(declaration());
    }

    // consume(RIGHT_BRACE, "Expect '}' after block.");
    consume(DOT, "Expect '.' after block.");
    return statements;
  }

  private Expr assignment() {
    // Expr expr = equality();
    Expr expr = or();

    if (match(BE)) {
    // if (match(BE) || match(IS) || match(AM) || match(ARE)) {
      Token be = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable)expr).name;
        return new Expr.Assign(name, value);
      }

      error(be, "Invalid assignment target.");
    }

    return expr;
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
    Expr expr = comparison();

    while (match(BANG_EQUAL, EQUAL_EQUAL, IS, AM, ARE)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr comparison() {
    Expr expr = term();

    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr term() {
    Expr expr = factor();

    while (match(MINUS, PLUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr factor() {
    Expr expr = unary();

    while (match(SLASH, STAR)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr unary() {
    if (match(BANG, MINUS, NOT)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

    return call();
  }

  private Expr finishCall(Expr callee) {
    List<Expr> arguments = new ArrayList<>();

    if (!check(EM_DASH)) {
      do {
        if (arguments.size() >= 255) {
          error(peek(), "Can't have more than 255 arguments.");
        }
        arguments.add(expression());
      } while (match(COMMA));
    }

    Token dash = consume(EM_DASH, "Expect ')' after arguments.");

    return new Expr.Call(callee, dash, arguments);
  }

  private Expr call() {
    Expr expr = primary();

    while (true) {
      if (match(EM_DASH)) {
        expr = finishCall(expr);
      } else {
        break;
      }
    }

    return expr;
  }

  private Expr primary() {
    if (match(FALSE)) return new Expr.Literal(false);
    if (match(TRUE)) return new Expr.Literal(true);
    if (match (NOTHING)) return new Expr.Literal(null);

    if (match(NUMBER, STRING)) {
      return new Expr.Literal(previous().literal);
    }

    if (match(IDENTIFIER)) {
      return new Expr.Variable(previous());
    }

    // if (match(LEFT_PAREN)) {
    //   Expr expr = expression();
    //   consume(RIGHT_PAREN, "Expect ')' after expression.");
    //   return new Expr.Grouping(expr);
    // }

    throw error(peek(), "Expect expression.");
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
      if (previous().type == SEMICOLON) return;
      // if (previous().type == NEWLINE) return;

      switch (peek().type) {
        case CLASS:
        case TO:
        case LET:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case KNOW:
        // case RETURN:
        case AMPERSAND:
          return;
        default:
      }

      advance();
    }
  }
}
