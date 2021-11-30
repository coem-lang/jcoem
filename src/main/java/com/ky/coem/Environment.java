package com.ky.coem;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.BooleanSupplier;

class Environment {
  final Environment enclosing;

  Environment() {
    enclosing = null;
  }

  Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

  // private final Map<String, Object> values = new HashMap<>();
  private final Map<Pattern, Object> values = new HashMap<>();

  Object get(Token name) {
    // if (values.containsKey(name.lexeme)) {
    //   return values.get(name.lexeme);
    // }
    // for (Map.Entry<Pattern, Object> set : values.entrySet()) {
    //   Matcher matcher = set.getKey().matcher(name.lexeme);
    //   if (matcher.find()) {
    //     return set.getValue();
    //   }
    // }
    Map.Entry<Pattern, Object> set = getSet(name.lexeme);
    if (set != null) {
      Object value = set.getValue();
      if (value instanceof BooleanSupplier) {
        BooleanSupplier bs = (BooleanSupplier)value;
        return bs.getAsBoolean();
      }
      return set.getValue();
    }

    // if not found in this environment, try enclosing one
    if (enclosing != null) return enclosing.get(name);

    // if not found after recursively walking up the chain, throw error
    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }

  void assign(Token name, Object value) {
    Map.Entry<Pattern, Object> set = getSet(name.lexeme);
    if (set != null) {
      values.put(set.getKey(), value);
      return;
    }

    // try enclosing env
    if (enclosing != null) {
      enclosing.assign(name, value);
      return;
    }

    // throw after recursion
    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }

  Map.Entry<Pattern, Object> getSet(String name) {
    for (Map.Entry<Pattern, Object> set : values.entrySet()) {
      Matcher matcher = set.getKey().matcher(name);
      if (matcher.find()) {
        return set;
      }
    }
    return null;
  }

  void define(String name, Object value) {
    // values.put(name, value);
    Pattern pat = Pattern.compile(name);
    Map.Entry<Pattern, Object> set = getSet(name);
    if (set != null) {
      values.put(set.getKey(), value);
    } else {
      values.put(pat, value);
    }
  }
}
