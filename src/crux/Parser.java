package crux;

import java.io.IOException;

import crux.Token.Kind;

public class Parser {
	private Scanner scanner;
	private Token current;

	private ErrorReport error;
	private ParseTree parseTree;

	public Parser(Scanner scanner) {
		this.scanner = scanner;

		this.parseTree = new ParseTree();
		this.error = new ErrorReport();
	}

	public void parse() {
		try {
			program();
		} catch (QuitParseException e) {
			error.reportSyntaxError();
		}
	}

	// program := declaration-list EOF
	private void program() {
		parseTree.enterRule(NonTerminal.PROGRAM);
		nextToken();
		declarationList();
		expect(Token.Kind.EOF);
		parseTree.exitRule(NonTerminal.PROGRAM);
	}

	// declaration-list := { declaration }
	private void declarationList() {
		parseTree.enterRule(NonTerminal.DECLARATION_LIST);
		while (have(NonTerminal.DECLARATION_LIST)) {
			declaration();
		}
		parseTree.exitRule(NonTerminal.DECLARATION_LIST);
	}

	// declaration := variable-declaration | array-declaration |
	// function-definition
	private void declaration() {
		parseTree.enterRule(NonTerminal.DECLARATION);
		if (have(NonTerminal.VARIABLE_DECLARATION)) {
			variableDeclaration();
		} else if (have(NonTerminal.FUNCTION_DEFINITION)) {
			functionDefinition();
		} else if (have(NonTerminal.ARRAY_DECLARATION)) {
			arrayDeclaration();
		}
		parseTree.exitRule(NonTerminal.DECLARATION);
	}

	// variable-declaration := "var" IDENTIFIER ":" type ";"
	private void variableDeclaration() {
		if (accept(NonTerminal.VARIABLE_DECLARATION)) {
			parseTree.enterRule(NonTerminal.VARIABLE_DECLARATION);
			expect(Token.Kind.IDENTIFIER);
			expect(Token.Kind.COLON);
			type();
			expect(Token.Kind.SEMICOLON);
			parseTree.exitRule(NonTerminal.VARIABLE_DECLARATION);
		}
	}

	// function-definition := "func" IDENTIFIER "(" parameter-list ")" ":" type
	// statement-block
	private void functionDefinition() {
		if (accept(NonTerminal.FUNCTION_DEFINITION)) {
			parseTree.enterRule(NonTerminal.FUNCTION_DEFINITION);
			expect(Token.Kind.IDENTIFIER);
			expect(Token.Kind.OPEN_PAREN);
			parameterList();
			expect(Token.Kind.CLOSE_PAREN);
			expect(Token.Kind.COLON);
			type();
			statementBlock();
			parseTree.exitRule(NonTerminal.FUNCTION_DEFINITION);
		}
	}

	// array-declaration := "array" IDENTIFIER ":" type "[" INTEGER "]" { "["
	// INTEGER "]" } ";"
	private void arrayDeclaration() {
		if (accept(NonTerminal.ARRAY_DECLARATION)) {
			parseTree.enterRule(NonTerminal.ARRAY_DECLARATION);
			expect(Token.Kind.IDENTIFIER);
			expect(Token.Kind.COLON);
			type();
			expect(Token.Kind.OPEN_BRACKET);
			expect(Token.Kind.INTEGER);
			expect(Token.Kind.CLOSE_BRACKET);
			while (accept(Token.Kind.OPEN_BRACKET)) {
				expect(Token.Kind.INTEGER);
				expect(Token.Kind.CLOSE_BRACKET);
			}
			expect(Token.Kind.SEMICOLON);
			parseTree.exitRule(NonTerminal.ARRAY_DECLARATION);
		}
	}

	// statement-block := "{" statement-list "}"
	private void statementBlock() {
		parseTree.enterRule(NonTerminal.STATEMENT_BLOCK);
		expect(Token.Kind.OPEN_BRACE);
		statementList();
		expect(Token.Kind.CLOSE_BRACE);
		parseTree.exitRule(NonTerminal.STATEMENT_BLOCK);
	}

	// statement-list := { statement }
	private void statementList() {
		parseTree.enterRule(NonTerminal.STATEMENT_LIST);
		while (have(NonTerminal.STATEMENT)) {
			statement();
		}
		parseTree.exitRule(NonTerminal.STATEMENT_LIST);
	}

	// statement := variable-declaration | call-statement | assignment-statement
	// | if-statement | while-statement | return-statement
	private void statement() {
		parseTree.enterRule(NonTerminal.STATEMENT);
		if (have(NonTerminal.VARIABLE_DECLARATION)) {
			variableDeclaration();
		} else if (have(NonTerminal.CALL_STATEMENT)) {
			callStatement();
		} else if (have(NonTerminal.ASSIGNMENT_STATEMENT)) {
			assignmentStatement();
		} else if (have(NonTerminal.IF_STATEMENT)) {
			ifStatement();
		} else if (have(NonTerminal.WHILE_STATEMENT)) {
			whileStatement();
		} else if (have(NonTerminal.RETURN_STATEMENT)) {
			returnStatement();
		}
		parseTree.exitRule(NonTerminal.STATEMENT);

	}

	// call-statement := call-expression ";"
	private void callStatement() {
		parseTree.enterRule(NonTerminal.CALL_STATEMENT);
		callExpression();
		expect(Token.Kind.SEMICOLON);
		parseTree.exitRule(NonTerminal.CALL_STATEMENT);
	}

	// call-expression := "::" IDENTIFIER "(" expression-list ")"
	private void callExpression() {
		if (accept(NonTerminal.CALL_EXPRESSION)) {
			parseTree.enterRule(NonTerminal.CALL_EXPRESSION);
			expect(Token.Kind.IDENTIFIER);
			expect(Token.Kind.OPEN_PAREN);
			expressionList();
			expect(Token.Kind.CLOSE_PAREN);
			parseTree.exitRule(NonTerminal.CALL_EXPRESSION);
		}
	}

	// assignment-statement := "let" designator "=" expression0 ";"
	private void assignmentStatement() {
		if (accept(NonTerminal.ASSIGNMENT_STATEMENT)) {
			parseTree.enterRule(NonTerminal.ASSIGNMENT_STATEMENT);
			designator();
			expect(Token.Kind.ASSIGN);
			expression0();
			expect(Token.Kind.SEMICOLON);
			parseTree.exitRule(NonTerminal.ASSIGNMENT_STATEMENT);
		}
	}

	// designator := IDENTIFIER { "[" expression0 "]" }
	private void designator() {
		if (accept(NonTerminal.DESIGNATOR)) {
			parseTree.enterRule(NonTerminal.DESIGNATOR);
			while (accept(Token.Kind.OPEN_BRACKET)) {
				expression0();
				expect(Token.Kind.CLOSE_BRACKET);
			}
			parseTree.exitRule(NonTerminal.DESIGNATOR);
		}
	}

	// if-statement := "if" expression0 statement-block [ "else" statement-block
	// ]
	private boolean ifStatement() {
		if (accept(NonTerminal.IF_STATEMENT)) {
			parseTree.enterRule(NonTerminal.IF_STATEMENT);
			expression0();
			statementBlock();
			if (accept(Token.Kind.ELSE)) {
				statementBlock();
			}
			parseTree.exitRule(NonTerminal.IF_STATEMENT);
		}
		return false;
	}

	// while-statement := "while" expression0 statement-block
	private void whileStatement() {
		if (accept(NonTerminal.WHILE_STATEMENT)) {
			parseTree.enterRule(NonTerminal.WHILE_STATEMENT);
			expression0();
			statementBlock();
			parseTree.exitRule(NonTerminal.RETURN_STATEMENT);
		}
	}

	// return-statement := "return" expression0 ";"
	private void returnStatement() {
		if (accept(NonTerminal.RETURN_STATEMENT)) {
			parseTree.enterRule(NonTerminal.RETURN_STATEMENT);
			expression0();
			expect(Token.Kind.SEMICOLON);
			parseTree.exitRule(NonTerminal.RETURN_STATEMENT);
		}
	}

	// expression-list := [ expression0 { "," expression0 } ]
	private void expressionList() {
		parseTree.enterRule(NonTerminal.EXPRESSION_LIST);
		if (have(NonTerminal.EXPRESSION0)) {
			do {
				expression0();
			} while (accept(Token.Kind.COMMA));
		}
		parseTree.exitRule(NonTerminal.EXPRESSION_LIST);
	}

	// expression0 := expression1 [ op0 expression1 ]
	private void expression0() {
		parseTree.enterRule(NonTerminal.EXPRESSION0);
		expression1();
		if (accept(NonTerminal.OP0)) {
			parseTree.enterRule(NonTerminal.OP0);
			parseTree.exitRule(NonTerminal.OP0);
			expression1();
		}
		parseTree.exitRule(NonTerminal.EXPRESSION0);
	}

	// expression1 := expression2 { op1 expression2 }
	private void expression1() {
		parseTree.enterRule(NonTerminal.EXPRESSION1);
		expression2();
		while (accept(NonTerminal.OP1)) {
			parseTree.enterRule(NonTerminal.OP1);
			parseTree.exitRule(NonTerminal.OP1);
			expression2();
		}
		parseTree.exitRule(NonTerminal.EXPRESSION1);
	}

	// expression2 := expression3 { op2 expression3 }
	private void expression2() {
		parseTree.enterRule(NonTerminal.EXPRESSION2);
		expression3();
		while (accept(NonTerminal.OP2)) {
			parseTree.enterRule(NonTerminal.OP2);
			parseTree.exitRule(NonTerminal.OP2);
			expression3();
		}
		parseTree.exitRule(NonTerminal.EXPRESSION2);
	}

	// expression3 := "not" expression3 | "(" expression0 ")" | designator |
	// call-expression | literal
	private void expression3() {
		if (have(NonTerminal.EXPRESSION3)) {
			parseTree.enterRule(NonTerminal.EXPRESSION3);
			if (accept(Token.Kind.NOT)) {
				expression3();
			} else if (accept(Token.Kind.OPEN_PAREN)) {
				expression0();
				expect(Token.Kind.CLOSE_PAREN);
			} else if (have(NonTerminal.DESIGNATOR)) {
				designator();
			} else if (have(NonTerminal.CALL_EXPRESSION)) {
				callExpression();
			} else if (have(NonTerminal.LITERAL)) {
				literal();
			}
			parseTree.exitRule(NonTerminal.EXPRESSION3);
		}
	}

	// literal := INTEGER | FLOAT | TRUE | FALSE
	private void literal() {
		if (accept(NonTerminal.LITERAL)) {
			parseTree.enterRule(NonTerminal.LITERAL);
			parseTree.exitRule(NonTerminal.LITERAL);
		}
	}

	// parameter-list := [ parameter { "," parameter } ]
	private void parameterList() {
		parseTree.enterRule(NonTerminal.PARAMETER_LIST);
		do {
			if (have(NonTerminal.PARAMETER)) {
				parameter();
			}
		} while (accept(Token.Kind.COMMA));
		parseTree.exitRule(NonTerminal.PARAMETER_LIST);
	}

	// parameter := IDENTIFIER ":" type
	private void parameter() {
		if (accept(NonTerminal.PARAMETER)) {
			parseTree.enterRule(NonTerminal.PARAMETER);
			expect(Token.Kind.COLON);
			type();
			parseTree.exitRule(NonTerminal.PARAMETER);
		}
	}

	// type := IDENTIFIER
	private void type() {
		parseTree.enterRule(NonTerminal.TYPE);
		expect(Token.Kind.IDENTIFIER);
		parseTree.exitRule(NonTerminal.TYPE);
	}

	private boolean have(NonTerminal nonterminal) {
		return nonterminal.firstSet().contains(current.kind);
	}

	private boolean have(Kind kind) {
		return current.isToken(kind);
	}

	private boolean accept(NonTerminal nonterminal) {
		if (have(nonterminal)) {
			nextToken();
			return true;
		}
		return false;
	}

	private boolean accept(Kind kind) {
		if (have(kind)) {
			nextToken();
			return true;
		}
		return false;
	}

	private boolean expect(Kind kind) {
		if (accept(kind)) {
			return true;
		}
		String msg = error.reportSyntaxError(kind);
		throw new QuitParseException(msg);
	}

	private boolean expect(NonTerminal nonterminal) {
		if (accept(nonterminal)) {
			return true;
		}
		String msg = error.reportSyntaxError(nonterminal);
		throw new QuitParseException(msg);
	}

	private void nextToken() {
		try {
			current = scanner.next();
		} catch (IOException e) {

		}
	}

	public boolean hasError() {
		return error.hasError();
	}

	public String errorReport() {
		return error.toString();
	}

	public String parseTreeReport() {
		return parseTree.toString();
	}

	private class QuitParseException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public QuitParseException(String msg) {
			super(msg);
		}
	}

	private class ErrorReport {
		private StringBuffer buffer = new StringBuffer();
		private final String expectedResponse = "SyntaxError(%d,%d)[Expected %s but got %s.]";
		private final String tokenResponse = "SyntaxError(%d,%d)[Expected a token from %s but got %s.]";
		private final String syntaxResponse = "SyntaxError(%d,%d)[Could not complete parsing.]";

		public boolean hasError() {
			return buffer.length() != 0;
		}

		public String toString() {
			return buffer.toString();
		}

		private void reportSyntaxError() {
			String message = String.format(syntaxResponse, current.lineNumber,
					current.charPosition);
			buffer.append(message);
		}

		private String reportSyntaxError(NonTerminal nonTerminal) {
			String message = String.format(tokenResponse, current.lineNumber,
					current.charPosition, nonTerminal.name(), current.kind);
			buffer.append(message + "\n");
			return message;
		}

		private String reportSyntaxError(Token.Kind kind) {
			String message = String.format(expectedResponse,
					current.lineNumber, current.charPosition, kind,
					current.kind);
			buffer.append(message + "\n");
			return message;
		}
	}

	private class ParseTree {
		private StringBuffer buffer;
		private int depth;

		public ParseTree() {
			depth = 0;
			buffer = new StringBuffer();
		}

		public void enterRule(NonTerminal nonTerminal) {
			String lineData = new String();
			for (int i = 0; i < depth; i++) {
				lineData += "  ";
			}
			lineData += nonTerminal.name();
			buffer.append(lineData + "\n");
			depth++;
		}

		public String toString() {
			return buffer.toString();
		}

		private void exitRule(NonTerminal nonTerminal) {
			depth--;
		}
	}
}
