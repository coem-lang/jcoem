package com.ky.coem;

import java.util.List;

class CoemFunction implements CoemCallable {
  private final Stmt.Function declaration;
  private final Environment closure;

  CoemFunction(Stmt.Function declaration, Environment closure) {
    this.closure = closure;
    this.declaration = declaration;
  }

  @Override
  public String toString() {
    return "<fun " + declaration.name.lexeme + ">";
  }

  @Override
  public int arity() {
    return declaration.params.size();
  }

  @Override
  // public Object call(Interpreter interpreter, List<Object> arguments) {
  public Object call(Interpreter interpreter, List<Object> arguments, Expr callee) {
    Environment environment = new Environment(closure);
    for (int i = 0; i < declaration.params.size(); i++) {
      environment.define(declaration.params.get(i).lexeme, arguments.get(i));
    }

    try {
      interpreter.executeBlock(declaration.body, environment);
    } catch (Return returnValue) {
      return returnValue.value;
    }
    return null;
  }
}
