package com.craftinginterpreters.lox;

// import java.beans.beancontext.BeanContext;

enum TokenType {
  // Single-character tokens.
  // LEFT_PAREN, RIGHT_PAREN,
  // LEFT_BRACE, RIGHT_BRACE,
  COLON,
  COMMA, DOT, MINUS, PLUS, SLASH, STAR,
  EM_DASH,
  SEMICOLON,
  // NEWLINE,

  // One or two character tokens.
  BANG, BANG_EQUAL,
  EQUAL, EQUAL_EQUAL,
  GREATER, GREATER_EQUAL,
  LESS, LESS_EQUAL,

  // Literals.
  IDENTIFIER, STRING, NUMBER,

  // Keywords.
  AND, OR, // logic
  IS, AM, ARE, // comparison
  IF, ELSE, FOR, WHILE, // control flow
  LET, BE, // variables
  TO, RETURN, PRINT, // functions
  CLASS, SUPER, THIS, // classes
  TRUE, FALSE, NOTHING, // values
  // MAYBE,

  EOF
}
