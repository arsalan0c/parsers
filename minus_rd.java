/*
Code partially referenced from: https://craftinginterpreters.com/

This program is a recursive descent parser for the following grammar:

S ::= L L*
L ::= id = E ; 
E ::= ( E ) E2
  | - E
  | number E2
E2 ::= - E
	| ε 
*/

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

enum TokenType {
  LPAREN, RPAREN, SEMICOLON, MINUS, EQ, ID, NUMBER
}

class Token {
  final TokenType type;
  final String lexeme;

  public Token(TokenType type, String lexeme) {
    this.type = type;
    this.lexeme = lexeme;
  }
}

class Main {
  public static void main(String[] args) {
    Parser p = new Parser(getTokens());
    System.out.println(p.parse().print());
  }

  public static List<Token> getTokens() {
    List<Token> tokens = new ArrayList<>();
    tokens.add(new Token(TokenType.ID,"A"));
    tokens.add(new Token(TokenType.EQ,"="));
    tokens.add(new Token(TokenType.LPAREN,"("));
    tokens.add(new Token(TokenType.NUMBER,"1"));
    tokens.add(new Token(TokenType.MINUS,"-"));
    tokens.add(new Token(TokenType.NUMBER,"3"));
    tokens.add(new Token(TokenType.RPAREN,")"));
    tokens.add(new Token(TokenType.MINUS,"-"));
    tokens.add(new Token(TokenType.NUMBER,"3"));
    tokens.add(new Token(TokenType.SEMICOLON,";"));
    tokens.add(new Token(TokenType.ID,"B"));
    tokens.add(new Token(TokenType.EQ,"="));
    tokens.add(new Token(TokenType.MINUS,"-"));
    tokens.add(new Token(TokenType.NUMBER,"1"));
    tokens.add(new Token(TokenType.MINUS,"-"));
    tokens.add(new Token(TokenType.NUMBER,"3"));
    tokens.add(new Token(TokenType.MINUS,"-"));
    tokens.add(new Token(TokenType.NUMBER,"2"));
    tokens.add(new Token(TokenType.SEMICOLON,";"));

    return tokens;
  }
}

class Parser {
  private static class ParseError extends RuntimeException {}
  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  public Node parse() {
    Node s = this.S();
    current = 0;
    return s;
  }

  private Lst S() {
    Lst.StatementLst sl = new Lst.StatementLst(this.L());
    while (!isAtEnd()) {
      Statement s = this.L();
      if (s == null) continue;
      sl.add(s);
    }
      
    return sl;
  }

  private Statement L() {
    try {
      Token id = consume("Expected identifier of [a-Z]+ in assignment statement", TokenType.ID);
      Expr.Identifier idNode = new Expr.Identifier(id.lexeme);
      consume("Expected '=' after identifier in assignment statement", TokenType.EQ);

      Expr e = E();
      consume("Expected ';' in assignment statement", TokenType.SEMICOLON);

      Statement l = new Statement.Assignment(idNode, e);
      return l;
    } catch (ParseError e) {
      synchronize();
      return null;
    }
  }

  private Expr E() {
    if (match(TokenType.LPAREN)) {
      Expr e = E();
      consume("Expected ')' after expression in expression E", TokenType.RPAREN);
      Expr.Unary e2 = E2();
      if (e2 == null) return e;

      Expr bin = new Expr.Binary(e, e2.op, e2.e);
      return bin;
    } else if (match(TokenType.NUMBER)) {
      Token num = previous();
      Expr.Number numNode = new Expr.Number(num.lexeme);

      Expr.Unary e2 = E2();
      if (e2 == null) return numNode;

      Expr bin = new Expr.Binary(numNode, e2.op, e2.e);
      return bin;
    } else if (match(TokenType.MINUS)) {
      Token minus = previous();
      
      Expr e = E();

      Expr u = new Expr.Unary(minus, e);
      return u;
    } else {
      throw error(peek(), "Expected '(' | number | '-' in expression E");
    }
  }

  private Expr.Unary E2() {
    if (match(TokenType.MINUS)) {
      Token minus = previous();
      Expr e = E();
      Expr.Unary u = new Expr.Unary(minus, e);
      return u;
    } else {
      return null;
    }
  }

  private boolean isAtEnd() {
    return current == tokens.size() - 1;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private Token consume(String errMsg, TokenType type) {
    if (check(type)) return advance();
    throw error(peek(), errMsg);
  }

  private Token advance() {
    if (!isAtEnd()) current++;
    return previous();
  }

  private boolean match(TokenType... types) {
    for (TokenType t: types) {
      if (check(t)) {
        advance();
        return true;
      }
    }

    return false;
  }

  private boolean check(TokenType type) {
    if (current >= tokens.size()) return false;
    return peek().type == type;
  }

  private ParseError error(Token t, String errMsg) {
    System.err.println(t.lexeme + " " + errMsg);
    return new ParseError();
  }

  /* 
    discard tokens until the next statement
    so that parsing can continue
  */
  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == TokenType.SEMICOLON) return;

      switch (peek().type) {
        case ID:
          current--;
          return;
      }

      advance();
    }
  }
}

abstract class Node {
    abstract public String print();
}
abstract class Expr extends Node {
  static class Identifier extends Expr {
    final String name;
    public Identifier(String name) {
      this.name = name;
    }

    @Override 
    public String print() {
      return this.name;
    }
  }

  static class Number extends Expr {
    final String n;
    public Number(String n) {
      this.n = n;
    }

    @Override 
    public String print() {
      return this.n;
    }
  }

  static class Binary extends Expr {
    final Expr left;
    final Token op;
    final Expr right;
    public Binary(Expr left, Token op, Expr right) {
      this.left = left;
      this.op = op;
      this.right = right;
    }

    @Override 
    public String print() {
      return this.left.print() + " " + this.op.lexeme + " " + this.right.print();
    }
  }

  static class Unary extends Expr {
    final Token op;
    final Expr e;
    public Unary(Token op, Expr e) {
      this.op = op;
      this.e = e;
    }

    @Override 
    public String print() {
      return this.op.lexeme + this.e.print();
    }
  }
}

abstract class Statement extends Node {
  static class Assignment extends Statement {
    final Expr.Identifier id;
    final Expr e;
    public Assignment(Expr.Identifier id, Expr e) {
      this.id = id;
      this.e = e;
    }

    @Override 
    public String print() {
      return this.id.print() + " = " + this.e.print() + ";";
    }
  }
}

abstract class Lst extends Node {
  static class StatementLst extends Lst {
    List<Statement> statements;
    public StatementLst(Statement... s) {
      statements = new ArrayList<Statement>(Arrays.asList(s));
    }

    public void add(Statement s) {
      this.statements.add(s);
    }

    @Override 
    public String print() {
      StringBuilder sb = new StringBuilder();
      for (Statement s : statements) {
        sb.append(s.print()).append("\n");
      }

      return sb.toString();
    }
  }
}
