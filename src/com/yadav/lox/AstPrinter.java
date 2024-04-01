package com.yadav.lox;

import com.yadav.lox.Expr.Binary;
import com.yadav.lox.Expr.Grouping;
import com.yadav.lox.Expr.Literal;
import com.yadav.lox.Expr.Unary;
import com.yadav.lox.Expr.Ternary;

class AstPrinter implements Expr.Visitor<String> {
  String print(Expr expr) {
    return expr.accept(this);
  }

  @Override
  public String visitTernaryExpr(Ternary expr) {
    return parenthesize_ternary(expr.left, expr.mid, expr.right);
  }

  @Override
  public String visitBinaryExpr(Binary expr) {
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }

  @Override
  public String visitGroupingExpr(Grouping expr) {
    return parenthesize("group", expr.expression);
  }

  @Override
  public String visitLiteralExpr(Literal expr) {
    if (expr.value == null) return "nil";
    return expr.value.toString();
  }

  @Override
  public String visitUnaryExpr(Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.right);
  }

  private String parenthesize_ternary(Expr left, Expr mid, Expr right) {
    StringBuilder builder = new StringBuilder();

    builder.append("(");
    builder.append("? ");
    builder.append("( ");
    builder.append(left.accept(this));
    builder.append(" )");
    builder.append("(");
    builder.append(": ");
    builder.append(mid.accept(this));
    builder.append(" ");
    builder.append(right.accept(this));
    builder.append(" )");
    builder.append(")");
    return builder.toString();
  }

  private String parenthesize(String name, Expr... exprs) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    for (Expr expr : exprs) {
      builder.append(" ");
      builder.append(expr.accept(this));
    }
    builder.append(")");

    return builder.toString();
  }

  public static void main(String[] args) {
    Expr expression = new Expr.Binary(
        new Expr.Unary(
          new Token(TokenType.MINUS, "-", null, 1),
          new Expr.Literal(123)
        ),
        new Token(TokenType.STAR, "*", null, 1),
        new Expr.Grouping(new Expr.Literal(45.55))
      );

    System.out.println(new AstPrinter().print(expression));
  }
}
