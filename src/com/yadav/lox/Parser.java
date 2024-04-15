package com.yadav.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.yadav.lox.TokenType.*;

class Parser {
  private static class ParseError extends RuntimeException {};
  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  // Expr parse() {
  //   try {
  //     return expression();
  //   } catch (ParseError error) {
  //     return null;
  //   }
  // }
  
  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while(!isAtEnd()) {
      statements.add(declaration());
    }

    return statements;
  }

  private Stmt declaration() {
    try {
      if (match(VAR)) return variableDeclaration();

      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  } 

  private Stmt variableDeclaration() {
    Token name = consume(IDENTIFIER, "Expect variable name.");

    Expr init = null;
    if (match(EQUAL)) {
      init = expression();
    }

    consume(SEMICOLON, "Expect ';' after variable declaration");
    return new Stmt.Var(name, init);
  }

  private Stmt statement() {
    if (match(FOR)) return forStatement();
    if (match(IF)) return ifStatement();
    if (match(PRINT)) return printStatement();
    if (match(WHILE)) return whileStatement();
    if (match(LEFT_BRACE)) return new Stmt.Block(block());

    return expressionStatement();
  }

  private Stmt forStatement() {
    consume(LEFT_PAREN, "Expected '(' after 'for'.");

    Stmt initializer;
    if (match(SEMICOLON)) {
      initializer = null;
    } else if (match(VAR)) {
      initializer = variableDeclaration();
    } else {
      initializer = expressionStatement();
    }

    Expr condition = null;
    if (!check(SEMICOLON)) {
      condition = expression();
    }
    consume(SEMICOLON, "Expected ';' after for loop condition");

    Expr increment = null;
    if (!check(SEMICOLON)) {
      increment = expression();
    }
    consume(RIGHT_PAREN, "Expcted ')' after for clauses.");
    Stmt body = statement();

    if (increment != null) {
      body = new Stmt.Block(Arrays.asList(
        body,
        new Stmt.Expression(increment)
      ));
    }

    if (condition == null) condition = new Expr.Literal(true);
    body = new Stmt.While(condition, body);

    if (initializer != null) {
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }
    return body;
  }

  private Stmt whileStatement() {
    consume(LEFT_PAREN, "Expected '(' after 'while'.");
    Expr condition = expression();
    consume(RIGHT_PAREN, "Expected ')' after while condition.");
    Stmt body = statement();

    return new Stmt.While(condition, body);
  }

  private Stmt ifStatement() {
    consume(LEFT_PAREN, "Expected '(' after an 'if' statement.");
    Expr condition = expression();
    consume(RIGHT_PAREN, "Expected ')' after if condition");

    Stmt thenBranch = statement();
    Stmt elseBranch = null;

    if (match(ELSE)) {
      elseBranch = statement();
    }

    return new Stmt.If(condition, thenBranch, elseBranch);
  }

  private List<Stmt> block() {
    List<Stmt> statements = new ArrayList<>();

    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(RIGHT_BRACE, "Expected '}' after block.");

    return statements;
  }

  private Stmt printStatement() {
    Expr value = expression();
    consume(SEMICOLON, "Expected ';' after value");
    return new Stmt.Print(value);
  }

  private Stmt expressionStatement() {
    Expr expr = expression();
    consume(SEMICOLON, "Expected ';' after value");
    return new Stmt.Expression(expr);
  }

  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }
    return false;
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) return false;
    return peek().type == type;
  }

  private Token advance() {
    if (!isAtEnd()) current++;
    return previous();
  }

  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private Expr expression() {
    return assignment();
  }
  
  private Expr assignment() {
    Expr expr = or();

    if (match(EQUAL)) {
      Token equals = previous();
      Expr value = assignment();
      
      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable)expr).name;
        return new Expr.Assign(name, value);
      }

      error(equals, "Invalid assignment target.");
    }

    return expr;
  }

  private Expr or() {
    Expr expr = and();

    while (match(OR)) {
      Token operator = previous();
      Expr right = and();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr and() {
    Expr expr = equality();

    while (match(AND)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr equality() {
    Expr expr = comparison();

    while(match(BANG_EQUAL, EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr comparison() {
    Expr expr = term();

    while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr term() {
    Expr expr = factor();

    while(match(PLUS, MINUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr factor() {
    Expr expr = ternary();

    while(match(STAR, SLASH)) {
      Token operator = previous();
      Expr right = ternary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr ternary() {
    Expr expr = unary();

    while(match(QUESTION)) {
      Token question = previous();
      Expr mid = ternary();
      consume(COLON, "Expected a ':' in ternary expression");
      Token colon = previous();
      Expr right = ternary();
      expr = new Expr.Ternary(expr, question, mid, colon, right);
    }

    return expr;
  }

  private Expr unary() {
    while(match(BANG, MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

    return primary();
  }

  private Expr primary() {
    if(match(FALSE)) return new Expr.Literal(false);
    if(match(TRUE)) return new Expr.Literal(true);
    if(match(NIL)) return new Expr.Literal(null);

    if(match(NUMBER, STRING)) {
      return new Expr.Literal(previous().literal);
    }

    if(match(IDENTIFIER)) {
      return new Expr.Variable(previous());
    }

    if(match(LEFT_PAREN)) {
      Expr expr = expression();
      consume(RIGHT_PAREN, "Expect ')' after expression");
      return new Expr.Grouping(expr);
    }

    throw error(peek(), "Expect expressions");
  }

  private Token consume(TokenType type, String message){
    if (check(type)) return advance();

    throw error(peek(), message);
  }

  private ParseError error(Token token, String message) {
    Lox.error(token, message);
    return new ParseError();
  }

  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == SEMICOLON) return;

      switch (peek().type) {
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }

      advance();
    }
  }

}
