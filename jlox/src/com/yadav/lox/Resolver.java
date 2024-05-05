package com.yadav.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.yadav.lox.Expr.Assign;
import com.yadav.lox.Expr.Binary;
import com.yadav.lox.Expr.Call;
import com.yadav.lox.Expr.Grouping;
import com.yadav.lox.Expr.Literal;
import com.yadav.lox.Expr.Logical;
import com.yadav.lox.Expr.Ternary;
import com.yadav.lox.Expr.Unary;
import com.yadav.lox.Expr.Variable;
import com.yadav.lox.Stmt.Block;
import com.yadav.lox.Stmt.Expression;
import com.yadav.lox.Stmt.Function;
import com.yadav.lox.Stmt.If;
import com.yadav.lox.Stmt.Print;
import com.yadav.lox.Stmt.Return;
import com.yadav.lox.Stmt.Var;
import com.yadav.lox.Stmt.While;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void>{

  private final Interpreter interpreter;
  private final Stack<Map<String, Boolean>> scopes = new Stack<>();
  private FunctionType currentFunction = FunctionType.NONE;

  Resolver(Interpreter interpreter) {
    this.interpreter = interpreter;
  }

  private enum FunctionType {
    NONE,
    FUNCTION
  }

	@Override
	public Void visitBlockStmt(Block stmt) {
    beginScope();
    resolve(stmt.statements);
    endScope();
    return null;
	}

  void resolve(List<Stmt> statements) {
    for (Stmt statement : statements) {
      resolve(statement);
    }
  }

  private void resolve(Stmt statement) {
    statement.accept(this);
  }

  private void resolve(Expr expression) {
    expression.accept(this);
  }

  private void beginScope() {
    scopes.push(new HashMap<String, Boolean>());
  }

  private void endScope() {
    scopes.pop();
  }

  @Override
  public Void visitClassStmt(Stmt.Class stmt) {
    declare(stmt.name);
    define(stmt.name);
    return null;
  }

	@Override
	public Void visitFunctionStmt(Function stmt) {
    declare(stmt.name);
    define(stmt.name);

    resolveFunction(stmt, FunctionType.FUNCTION);
    return null;
	}
  
  private void resolveFunction(Stmt.Function stmt, FunctionType type) {
    beginScope();
    FunctionType enclosingType = currentFunction;
    currentFunction = type;
    for (Token param : stmt.params) {
      declare(param);
      define(param);
    }
    resolve(stmt.body);
    endScope();
    currentFunction = enclosingType;
  }

	@Override
	public Void visitExpressionStmt(Expression stmt) {
    resolve(stmt.expression);
    return null;
	}

	@Override
	public Void visitIfStmt(If stmt) {
    resolve(stmt.condition);
    resolve(stmt.thenBranch);
    if (stmt.elseBranch != null) resolve(stmt.elseBranch);
    return null;
	}

	@Override
	public Void visitPrintStmt(Print stmt) {
    resolve(stmt.expression);
    return null;
	}

	@Override
	public Void visitReturnStmt(Return stmt) {
    if (currentFunction == FunctionType.NONE) {
      Lox.error(stmt.keyword, "Can't return from a top-level code.");
    }
    if (stmt.value != null) {
      resolve(stmt.value);
    }

    return null;
	}

	@Override
	public Void visitVarStmt(Var stmt) {
    declare(stmt.name);
    if (stmt.initializer != null) {
      resolve(stmt.initializer);
    }
    define(stmt.name);
    return null;
  }

  private void declare(Token name) {
    if (scopes.isEmpty()) return;

    Map<String, Boolean> scope = scopes.peek();

    if (scope.containsKey(name.lexeme)) {
      Lox.error(name, "Already a variable with the name in this scope.");
    }

    scope.put(name.lexeme, false);
  }

  private void define(Token name) {
    if (scopes.isEmpty()) return;
    scopes.peek().put(name.lexeme, true);
  }

	@Override
	public Void visitWhileStmt(While stmt) {
    resolve(stmt.condition);
    resolve(stmt.body);
    return null;
	}

	@Override
	public Void visitTernaryExpr(Ternary expr) {
    resolve(expr.left);
    resolve(expr.mid);
    resolve(expr.right);
    return null;
	}

	@Override
	public Void visitBinaryExpr(Binary expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
	}

	@Override
	public Void visitCallExpr(Call expr) {
    resolve(expr.callee);

    for (Expr arg : expr.arguments) {
      resolve(arg);
    }

    return null;
	}

  @Override
  public Void visitGetExpr(Expr.Get expr) {
    resolve(expr.object);
    return null;
  }

	@Override
	public Void visitGroupingExpr(Grouping expr) {
    resolve(expr.expression);
    return null;
	}

	@Override
	public Void visitLiteralExpr(Literal expr) {
    return null;
	}

	@Override
	public Void visitLogicalExpr(Logical expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
	}

  @Override
  public Void visitSetExpr(Expr.Set expr) {
    resolve(expr.value);
    resolve(expr.object);
    return null;
  }

	@Override
	public Void visitUnaryExpr(Unary expr) {
    resolve(expr.right);
    return null;
	}

	@Override
	public Void visitVariableExpr(Variable expr) {
    if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
      Lox.error(expr.name, "Can't read local variable in it's own initializer");
    }
    resolveLocal(expr, expr.name);
    return null;
	}

	@Override
	public Void visitAssignExpr(Assign expr) {
    resolve(expr.value);
    resolveLocal(expr, expr.name);
    return null;
	}

  private void resolveLocal(Expr expr, Token name) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      if (scopes.get(i).containsKey(name.lexeme)) {
        interpreter.resolve(expr, scopes.size() - 1 - i);
        return;
      }
    }
  }

}
