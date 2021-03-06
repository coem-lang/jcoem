/**
 * Describes individual tokens, keeping track of TokenType,
 * lexeme String, literal Object, and line number.
 */

package com.ky.coem;

class Token {
  final TokenType type;
  final String lexeme;
  final Object literal;
  final int line; 

  Token(TokenType type, String lexeme, Object literal, int line) {
    this.type = type;
    this.lexeme = lexeme;
    this.literal = literal;
    this.line = line;
  }

  public String toString() {
    switch (type) {
      case COLON: return "COLON";
      case COMMA: return "COMMA";
      case DOT: return "DOT";
      case EMDASH: return "EMDASH";
      case NEWLINE: return "NEWLINE";
      case AMPERSAND: return "AMPERSAND";
      case POUND: return "POUND";
      case DAGGER: return "DAGGER";
      case IDENTIFIER: return "IDENTIFIER";
      case STRING: return "STRING";
      case COMMENT: return "COMMENT";
      case AND: return "AND";
      case OR: return "OR";
      case IS:
      case AM:
      case ARE: return "EQUALS";
      case IF: return "IF";
      case ELSE: return "ELSE";
      case WHILE: return "WHILE";
      case LET: return "LET";
      case BE: return "BE";
      case TO: return "TO";
      case TRUE: return "TRUE";
      case FALSE: return "FALSE";
      case NOTHING: return "NOTHING";
      case NOT: return "NOT";
      case EOF: return "EOF";
    }
    return "";
  }
}
