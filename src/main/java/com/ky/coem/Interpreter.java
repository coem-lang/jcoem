/**
 * Takes a List of Stmts and executes it.
 */

package com.ky.coem;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.BooleanSupplier;

class Interpreter implements Expr.Visitor<Object>,
                             Stmt.Visitor<Void> {

  final Environment globals = new Environment();
  private Environment environment = globals;
  
  String source;
  String[][] lines;
  String echo;

  Interpreter(String source) {
    this.source = source;
    this.echo = source;
    String[] linesWhole = source.split("\n");
    lines = new String[linesWhole.length][2];
    for (int i = 0; i < linesWhole.length; i++) {
      String line = linesWhole[i];
      if (line.strip().indexOf(" †") > -1) {
        lines[i] = line.split(" †");
      } else {
        lines[i][0] = line;
      }
    }

    // clock
    globals.define("clock", new CoemCallable() {
      @Override
      public int arity() { return 0; }
      
      @Override
      public Object call(Interpreter interpreter, List<Object> arguments, Expr callee) {
        return (double)System.currentTimeMillis() / 1000.0;
      }

      @Override
      public String toString() { return "<native clock fn>"; }
    });

    // maybe
    BooleanSupplier maybe = () -> (new Random().nextDouble()) > 0.5;
    globals.define("maybe", maybe);

    // print
    CoemCallable print = new CoemCallable() {
      @Override
      public int arity() { return 1; }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments, Expr callee) {
        String print = " ";
        int line = ((Expr.Variable)callee).line - 1;
        if (arguments.size() >= 1) {
          print += arguments.get(0);
          if (arguments.size() > 1) {
            for (int i = 1; i < arguments.size(); i++) {
              print += " " + arguments.get(i);
            }
          }
        }

        if (lines[line][1] != null) {
          lines[line][1] += print;
        } else {
          lines[line][1] = print;
        }

        return null;
      }

      @Override
      public String toString() { return "<native print fn>"; }
    };
    globals.define("print", print);
    globals.define("know", print);
    globals.define("say", print);
  }

  void interpret(List<Stmt> statements) {
    try {
      for (Stmt statement : statements) {
        execute(statement);
      }
    } catch (RuntimeError error) {
      Coem.runtimeError(error);
    }
  }

  @Override
  public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value;
  }

  @Override
  public Object visitLogicalExpr(Expr.Logical expr) {
    Object left = evaluate(expr.left);

    if (expr.operator.type == TokenType.OR) {
      if (isTruthy(left)) return left;
    } else {
      if (!isTruthy(left)) return left;
    }

    return evaluate(expr.right);
  }

  @Override
  public Object visitUnaryExpr(Expr.Unary expr) {
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case NOT:
        return !isTruthy(right);
      default:
    }

    // Unreachable.
    return null;
  }

  @Override
  public Object visitVariableExpr(Expr.Variable expr) {
    return environment.get(expr.name);
  }

  private boolean isTruthy(Object object) {
    if (object == null) return false;
    if (object instanceof Boolean) return (boolean)object;
    return true;
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;

    return a.equals(b);
  }

  private String stringify(Object object) {
    if (object == null) return "nothing";

    if (object instanceof Double) {
      String text = object.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
      }
      return text;
    }

    return object.toString();
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  private void execute(Stmt stmt) {
    stmt.accept(this);
  }

  void executeBlock(List<Stmt> statements,
                    Environment environment) {
    Environment previous = this.environment;
    try {
      this.environment = environment;

      for (Stmt statement : statements) {
        execute(statement);
      }
    } finally {
      this.environment = previous;
    }
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    executeBlock(stmt.statements, new Environment(environment));
    return null;
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    evaluate(stmt.expression);
    return null;
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    CoemFunction function = new CoemFunction(stmt, environment);
    environment.define(stmt.name.lexeme, function);
    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    if (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.thenBranch);
    } else if (stmt.elseBranch != null) {
      execute(stmt.elseBranch);
    }
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    Object value = null;
    if (stmt.value != null) value = evaluate(stmt.value);

    throw new Return(value);
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    Object value = null;
    if (stmt.value != null) {
      value = evaluate(stmt.value);
    }

    environment.define(stmt.name.lexeme, value);
    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    while (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.body);
    }
    return null;
  }

  @Override
  public Void visitDirectiveStmt(Stmt.Directive stmt) {
    globals.define(stmt.name.lexeme, stmt.value.lexeme);
    return null;
  }

  @Override
  public Void visitCommentStmt(Stmt.Comment stmt) {
    return null;
  }

  @Override
  public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right); 

    switch (expr.operator.type) {
      // case PLUS:
      //   if (left instanceof Double && right instanceof Double) {
      //     return (double)left + (double)right;
      //   } 

      //   if (left instanceof String && right instanceof String) {
      //     return (String)left + (String)right;
      //   }

      //   throw new RuntimeError(expr.operator,
      //       "Operands must be two numbers or two strings.");
      // case BANG_EQUAL: return !isEqual(left, right);
      case IS:
      case AM:
      case ARE:
        return isEqual(left, right);
      default:
    }

    // Unreachable.
    return null;
  }

  @Override
  public Object visitCallExpr(Expr.Call expr) {
    Object callee = evaluate(expr.callee);

    List<Object> arguments = new ArrayList<>();
    for (Expr argument : expr.arguments) {
      arguments.add(evaluate(argument));
    }

    if (!(callee instanceof CoemCallable)) {
      throw new RuntimeError(expr.dash, "Can only call functions.");
    }

    CoemCallable function = (CoemCallable)callee;
    if (arguments.size() != function.arity()) {
      if (!function.toString().equals("<native print fn>")) {
        throw new RuntimeError(expr.dash, "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
      }
    }

    return function.call(this, arguments, expr.callee);
  }

  public String getEcho() {
    String echo = "";
    for (int i = 0; i < lines.length; i++) {
      if (lines[i][1] != null) {
        String line = String.join(" †", lines[i]);
        echo += line;
      } else {
        echo += lines[i][0];
      }
      echo += "\n";
    }
    return echo;
  }
}
