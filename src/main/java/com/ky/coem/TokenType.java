/**
 * Defines all valid tokens of the grammar.
 */

package com.ky.coem;

// import java.beans.beancontext.BeanContext;

enum TokenType {
  // Single-character tokens.
  COLON,
  COMMA, DOT,
  EMDASH,
  AMPERSAND,
  POUND,

  // Literals.
  IDENTIFIER, STRING,

  // Keywords.
  AND, OR, // logic
  IS, AM, ARE, // comparison
  IF, ELSE, WHILE, // control flow
  LET, BE, // variables
  TO, 
  TRUE, FALSE, NOTHING, // values
  NOT,

  NEWLINE,

  EOF
}
