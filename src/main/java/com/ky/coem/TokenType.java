/**
 * Defines all valid tokens of the grammar.
 */

package com.ky.coem;

// import java.beans.beancontext.BeanContext;

enum TokenType {
  // Single-character tokens.
  // LEFT_PAREN, RIGHT_PAREN,
  // LEFT_BRACE, RIGHT_BRACE,
  COLON,
  COMMA, DOT,
  EMDASH,
  // SEMICOLON,
  // NEWLINE,
  AMPERSAND,

  // Literals.
  IDENTIFIER, STRING,

  // Keywords.
  AND, OR, // logic
  IS, AM, ARE, // comparison
  IF, ELSE, FOR, WHILE, // control flow
  LET, BE, // variables
  TO, 
  // PRINT, KNOW, SAY,
  TRUE, FALSE, NOTHING, // values
  // MAYBE,
  NOT,

  NEWLINE,

  EOF
}
