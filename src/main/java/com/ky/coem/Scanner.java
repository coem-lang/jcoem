package com.ky.coem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ky.coem.TokenType.*; 

class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();

  private int start = 0;
  private int current = 0;
  private int line = 1;

  private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("and",    AND);
    // keywords.put("class",  CLASS);
    keywords.put("else",   ELSE);
    keywords.put("for",    FOR);
    keywords.put("to",     TO);
    keywords.put("if",     IF);
    keywords.put("or",     OR);
    keywords.put("print",  PRINT);
    keywords.put("know",  KNOW);
    // keywords.put("return", RETURN);
    // keywords.put("super",  SUPER);
    // keywords.put("this",   THIS);
    keywords.put("true",   TRUE);
    keywords.put("false",  FALSE);
    keywords.put("nothing", NOTHING);
    keywords.put("let", LET);
    keywords.put("while",  WHILE);
    keywords.put("be", BE);
    keywords.put("is", IS);
    keywords.put("am", AM);
    keywords.put("are", ARE);
    // keywords.put("maybe", MAYBE);
    keywords.put("not", NOT);
  }

  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
      // We are at the beginning of the next lexeme.
      start = current;
      scanToken();
    }
    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
      // case '(': addToken(LEFT_PAREN); break;
      // case ')': addToken(RIGHT_PAREN); break;
      case '—': addToken(EM_DASH); break;
      // case '{': addToken(LEFT_BRACE); break;
      // case '}': addToken(RIGHT_BRACE); break;
      case ':': addToken(COLON); break;
      case ',': addToken(COMMA); break;
      case '.': addToken(DOT); break;
      // case '-': addToken(MINUS); break;
      case '+': addToken(PLUS); break;
      case ';': addToken(SEMICOLON); break;
      // case '*': addToken(STAR); break;
      case '&': addToken(AMPERSAND); break; 
      // case '!':
      //   addToken(match('=') ? BANG_EQUAL : BANG);
      //   break;
      // case '=':
      //   addToken(match('=') ? EQUAL_EQUAL : EQUAL);
      //   break;
      // case '<':
      //   addToken(match('=') ? LESS_EQUAL : LESS);
      //   break;
      // case '>':
      //   addToken(match('=') ? GREATER_EQUAL : GREATER);
      //   break;
      // case '/':
      //   if (match('/')) {
      //     // A comment goes until the end of the line.
      //     while (peek() != '\n' && !isAtEnd()) advance();
      //   } else {
      //     addToken(SLASH);
      //   }
      //   break;
      case '*':
      case '†':
        while (peek() != '\n' && !isAtEnd()) advance();
        break;
        
      case ' ':
      case '\r':
      case '\t':
        // Ignore whitespace.
        break;

      case '\n':
        // addToken(NEWLINE);
        line++;
        break;

      case '"': string(); break;

      default:
        if (isDigit(c)) {
          number();
        // // normal
        // } else if (isAlpha(c)) {
        //   identifier();
        // } else {
        //   coem.error(line, "Unexpected character.");
        // }
        // regex
        } else {
          identifier();
        }
        break;
    }
  }

  private void identifier() {
    // normal
    // while (isAlphaNumeric(peek())) advance();
    // regex
    while (isNotBoundary(peek())) advance();

    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    if (type == null) type = IDENTIFIER;
    addToken(type);
  }

  private void number() {
    while (isDigit(peek())) advance();

    // Look for a fractional part.
    if (peek() == '.' && isDigit(peekNext())) {
      // Consume the "."
      advance();

      while (isDigit(peek())) advance();
    }

    addToken(NUMBER,
        Double.parseDouble(source.substring(start, current)));
  }

  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') line++;
      advance();
    }

    if (isAtEnd()) {
      Coem.error(line, "Unterminated string.");
      return;
    }

    // The closing ".
    advance();

    // Trim the surrounding quotes.
    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }

  private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }

  private char peek() {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }

  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  }

  // private boolean isAlpha(char c) {
  //   return (c >= 'a' && c <= 'z') ||
  //          (c >= 'A' && c <= 'Z') ||
  //           c == '_';
  // }

  // private boolean isAlphaNumeric(char c) {
  //   return isAlpha(c) || isDigit(c);
  // }

  private boolean isNotBoundary(char c) {
    return (!isSpace(c) && !isSemicolon(c) && !isEmdash(c));
    // return (!isSpace(c) && !isNewline(c) && !isEmdash(c));
  }

  private boolean isSpace(char c) {
    return c == ' ';
  }

  private boolean isSemicolon(char c) {
    return c == ';';
  }

  // private boolean isNewline(char c) {
  //   System.out.print(c);
  //   System.out.print(' ');
  //   System.out.println(c == '\n');
  //   return c == '\n';
  // }

  private boolean isEmdash(char c) {
    return c == '—';
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private char advance() {
    return source.charAt(current++);
  }

  private void addToken(TokenType type) {
    // System.out.println(type);
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    // System.out.println(literal);
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
}
