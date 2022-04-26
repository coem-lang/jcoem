/**
 * Holds Map of identifiers and their corresponding values.
 */

package com.ky.coem;

import java.util.HashMap;
import java.util.Map;
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

  private final Map<Pattern, Object> values = new HashMap<>();

  Object get(Token name) {
    // try to get variable in current environment
    Map.Entry<Pattern, Object> set = getSet(name.lexeme);
    if (set != null) {
      Object value = set.getValue();
      if (value instanceof BooleanSupplier) {
        BooleanSupplier bs = (BooleanSupplier)value;
        return bs.getAsBoolean();
      }
      return set.getValue();
    }

    // if not found in this environment, try the enclosing one
    // if (enclosing != null) return enclosing.get(name);
    if (enclosing != null) {
      // check if variable exists in enclosing environment
      if (enclosing.getSet(name.lexeme) != null) {
        return enclosing.get(name);
      }
    }

    // return the variable name as a string
    return name.lexeme;

    // if not found after recursively walking up the chain, throw error
    // throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
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
    Pattern pat = Pattern.compile(name);
    Map.Entry<Pattern, Object> set = getSet(name);

    // redefine in current environment
    if (set != null) {
      values.put(set.getKey(), value);
      return;
    }
    
    if (enclosing != null) {
      Map.Entry<Pattern, Object> enclosingSet = enclosing.getSet(name);
      // redefine in enclosing environment
      if (enclosingSet != null) {
        enclosing.define(name, value);
        return;
      }
    }

    // define new in current environment
    values.put(pat, value);
  }
}
