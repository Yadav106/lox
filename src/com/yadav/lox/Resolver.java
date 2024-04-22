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

  Resolver(Interpreter interpreter) {
    this.interpreter = interpreter;
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
	public Void visitFunctionStmt(Function stmt) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitFunctionStmt'");
	}

	@Override
	public Void visitExpressionStmt(Expression stmt) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitExpressionStmt'");
	}

	@Override
	public Void visitIfStmt(If stmt) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitIfStmt'");
	}

	@Override
	public Void visitPrintStmt(Print stmt) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitPrintStmt'");
	}

	@Override
	public Void visitReturnStmt(Return stmt) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitReturnStmt'");
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
    scope.put(name.lexeme, false);
  }

  private void define(Token name) {
    if (scopes.isEmpty()) return;
    scopes.peek().put(name.lexeme, true);
  }

	@Override
	public Void visitWhileStmt(While stmt) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitWhileStmt'");
	}

	@Override
	public Void visitTernaryExpr(Ternary expr) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitTernaryExpr'");
	}

	@Override
	public Void visitBinaryExpr(Binary expr) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitBinaryExpr'");
	}

	@Override
	public Void visitCallExpr(Call expr) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitCallExpr'");
	}

	@Override
	public Void visitGroupingExpr(Grouping expr) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitGroupingExpr'");
	}

	@Override
	public Void visitLiteralExpr(Literal expr) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitLiteralExpr'");
	}

	@Override
	public Void visitLogicalExpr(Logical expr) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitLogicalExpr'");
	}

	@Override
	public Void visitUnaryExpr(Unary expr) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitUnaryExpr'");
	}

	@Override
	public Void visitVariableExpr(Variable expr) {
    if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
      Lox.error(expr.name, "Can't read local variable in it's own initializer");
    }
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

	@Override
	public Void visitAssignExpr(Assign expr) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitAssignExpr'");
	}

}
