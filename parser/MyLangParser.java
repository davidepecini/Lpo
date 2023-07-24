package progettoFinale.parser;

import static java.lang.System.*;
import static java.util.Objects.*;
import static progettoFinale.parser.TokenType.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import progettoFinale.parser.ast.Add;
import progettoFinale.parser.ast.And;
import progettoFinale.parser.ast.AssignStmt;
import progettoFinale.parser.ast.Block;
import progettoFinale.parser.ast.BoolLiteral;
import progettoFinale.parser.ast.EmptyStmtSeq;
import progettoFinale.parser.ast.Eq;
import progettoFinale.parser.ast.Exp;
import progettoFinale.parser.ast.ForeachStmt;
import progettoFinale.parser.ast.Fst;
import progettoFinale.parser.ast.IfStmt;
import progettoFinale.parser.ast.IntLiteral;
import progettoFinale.parser.ast.Mul;
import progettoFinale.parser.ast.MyLangProg;
import progettoFinale.parser.ast.NonEmptyStmtSeq;
import progettoFinale.parser.ast.Not;
import progettoFinale.parser.ast.PairLit;
import progettoFinale.parser.ast.PrintStmt;
import progettoFinale.parser.ast.Prog;
import progettoFinale.parser.ast.Sign;
import progettoFinale.parser.ast.Snd;
import progettoFinale.parser.ast.Stmt;
import progettoFinale.parser.ast.StmtSeq;
import progettoFinale.parser.ast.VarStmt;
import progettoFinale.parser.ast.Variable;
import progettoFinale.parser.ast.VectorLiteral;

/*
Prog ::= StmtSeq EOF
StmtSeq ::= Stmt (';' StmtSeq)?
Stmt ::= 'var'? IDENT '=' Exp | 'print' Exp |  'if' '(' Exp ')' Block ('else' Block)? | 'foreach' IDENT 'in' Exp Block
Block ::= '{' StmtSeq '}'
Exp ::= And (',' And)*
And ::= Eq ('&&' Eq)*
Eq ::= Add ('==' Add)*
Add ::= Mul ('+' Mul)*
Mul::= Atom ('*' Atom)*
Atom ::= 'fst' Atom | 'snd' Atom | '-' Atom | '!' Atom | BOOL | NUM | IDENT | '(' Exp ')' | '[' Exp ';' Exp ']'
*/

public class MyLangParser implements Parser {

	private final MyLangTokenizer tokenizer; // the tokenizer used by the parser

	// reads the next token through the tokenizer associated with the
	// parser; TokenizerExceptions are chained into corresponding ParserExceptions
	private void nextToken() throws ParserException {
		try {
			tokenizer.next();
		} catch (TokenizerException e) {
			throw new ParserException(e);
		}
	}

	// decorates error message with the corresponding line number
	private String line_err_msg(String msg) {
		return "on line " + tokenizer.getLineNumber() + ": " + msg;
	}

	// checks whether the token type of the currently recognized token matches
	// 'expected'; if not, it throws a corresponding ParserException
	private void match(TokenType expected) throws ParserException {
		final var found = tokenizer.tokenType();
		if (found != expected)
			throw new ParserException(line_err_msg(
					"Expecting " + expected + ", found " + found + "('" + tokenizer.tokenString() + "')"));
	}

	// checks whether the token type of the currently recognized token matches
	// 'expected'; if so, it reads the next token, otherwise it throws a
	// corresponding ParserException
	private void consume(TokenType expected) throws ParserException {
		match(expected);
		nextToken();
	}

	// throws a ParserException because the current token was not expected
	private <T> T unexpectedTokenError() throws ParserException {
		throw new ParserException(line_err_msg(
				"Unexpected token " + tokenizer.tokenType() + "('" + tokenizer.tokenString() + "')"));
	}

	// associates the parser with a corresponding non-null tokenizer
	public MyLangParser(MyLangTokenizer tokenizer) {
		this.tokenizer = requireNonNull(tokenizer);
	}

	// parses a program Prog ::= StmtSeq EOF
	@Override
	public Prog parseProg() throws ParserException {
		nextToken(); // one look-ahead symbol
		final var prog = new MyLangProg(parseStmtSeq());
		match(EOF); // last token must have type EOF
		return prog;
	}

	@Override
	public void close() throws IOException {
		if (tokenizer != null)
			tokenizer.close();
	}

	// parses a non empty sequence of statements, binary operator STMT_SEP is right
	// associative StmtSeq ::= Stmt (';' StmtSeq)?
	private StmtSeq parseStmtSeq() throws ParserException {
		final var stmt = parseStmt();
		StmtSeq stmtSeq;
		if (tokenizer.tokenType() == STMT_SEP) {
			nextToken();
			stmtSeq = parseStmtSeq();
		} else
			stmtSeq = new EmptyStmtSeq();
		return new NonEmptyStmtSeq(stmt, stmtSeq);
	}

	// parses a statement Stmt ::= 'var'? IDENT '=' Exp | 'print' Exp | 'if' '(' Exp
	// ')' Block ('else' Block)?
	private Stmt parseStmt() throws ParserException {
		return switch (tokenizer.tokenType()) {
			case PRINT -> parsePrintStmt();
			case VAR -> parseVarStmt();
			case IDENT -> parseAssignStmt();
			case IF -> parseIfStmt();
			case FOREACH -> parseForeachStmt(); // Aggiunto
			default -> unexpectedTokenError();
		};
	}

	// parses the 'print' statement Stmt ::= 'print' Exp
	private PrintStmt parsePrintStmt() throws ParserException {
		consume(PRINT); // or nextToken() since PRINT has already been recognized
		return new PrintStmt(parseExp());
	}

	// parses the 'var' statement Stmt ::= 'var' IDENT '=' Exp
	private VarStmt parseVarStmt() throws ParserException {
		consume(VAR); // or nextToken() since VAR has already been recognized
		final var var = parseVariable();
		consume(ASSIGN);
		return new VarStmt(var, parseExp());
	}

	// parses tparses the assignment statement Stmt ::= IDENT '=' Exp
	private AssignStmt parseAssignStmt() throws ParserException {
		final var var = parseVariable();
		consume(ASSIGN);
		return new AssignStmt(var, parseExp());
	}

	// parses the 'if' statement Stmt ::= 'if' '(' Exp ')' Block ('else' Block)?
	private IfStmt parseIfStmt() throws ParserException {
		consume(IF); // or nextToken() since IF has already been recognized
		final var exp = parseRoundPar();
		final var thenBlock = parseBlock();
		if (tokenizer.tokenType() != ELSE)
			return new IfStmt(exp, thenBlock);
		nextToken();
		return new IfStmt(exp, thenBlock, parseBlock());
	}

	// Aggiunto: parses the 'foreach' statement
	// old ver
	/*
	 * private ForeachStmt parseForeachStmt() throws ParserException {
	 * consume(FOREACH);
	 * final Variable var = parseVariable();
	 * consume(IN);
	 * final Exp exp = parseRoundPar();
	 * final Block foreachBlock = parseBlock();
	 * return new ForeachStmt(var, exp, foreachBlock);
	 * }
	 */

	// new :
	// Stmt::= 'foreach' IDENT 'in' Exp Block
	private ForeachStmt parseForeachStmt() throws ParserException {
		consume(FOREACH);
		final var ident = parseVariable();
		consume(IN);
		final var exp = parseExp();
		final var block = parseBlock();
		return new ForeachStmt(ident, exp, block);
	}

	// parses a block of statements Block ::= '{' StmtSeq '}'
	private Block parseBlock() throws ParserException {
		consume(OPEN_BLOCK);
		final var stmts = parseStmtSeq();
		consume(CLOSE_BLOCK);
		return new Block(stmts);
	}

	// parses expressions, starting from the lowest precedence operator PAIR_OP
	// which is left-associative Exp ::= And (',' And)*
	private Exp parseExp() throws ParserException {
		var exp = parseAnd();
		while (tokenizer.tokenType() == PAIR_OP) {
			nextToken();
			exp = new PairLit(exp, parseAnd());
		}
		return exp;
	}

	// parses expressions, starting from the lowest precedence operator AND which is
	// left-associative And ::= Eq ('&&' Eq)*
	private Exp parseAnd() throws ParserException {
		var exp = parseEq();
		while (tokenizer.tokenType() == AND) {
			nextToken();
			exp = new And(exp, parseEq());
		}
		return exp;
	}

	// parses expressions, starting from the lowest precedence operator EQ which is
	// left-associative Eq ::= Add ('==' Add)*
	private Exp parseEq() throws ParserException {
		var exp = parseAdd();
		while (tokenizer.tokenType() == EQ) {
			nextToken();
			exp = new Eq(exp, parseAdd());
		}
		return exp;
	}

	// parses expressions, starting from the lowest precedence operator PLUS which
	// is left-associative Add ::= Mul ('+' Mul)*
	private Exp parseAdd() throws ParserException {
		var exp = parseMul();
		while (tokenizer.tokenType() == PLUS) {
			nextToken();
			exp = new Add(exp, parseMul());
		}
		return exp;
	}

	// parses expressions, starting from the lowest precedence operator TIMES which
	// is left-associative Mul::= Atom ('*' Atom)*
	private Exp parseMul() throws ParserException {
		var exp = parseAtom();
		while (tokenizer.tokenType() == TIMES) {
			nextToken();
			exp = new Mul(exp, parseAtom());
		}
		return exp;
	}

	// parses expressions of type Atom Atom ::= 'fst' Atom | 'snd' Atom | '-' Atom |
	// '!' Atom | BOOL | NUM | IDENT | '(' Exp ')' |'[' Exp ']' // aggiunto
	private Exp parseAtom() throws ParserException {
		return switch (tokenizer.tokenType()) {
			case NUM -> parseNum();
			case IDENT -> parseVariable();
			case MINUS -> parseMinus();
			case OPEN_PAR -> parseRoundPar();
			case BOOL -> parseBoolean();
			case NOT -> parseNot();
			case FST -> parseFst();
			case SND -> parseSnd();
			// case OPEN_VECT -> parseVector();
			case OPEN_VECT -> parseVectorLit();
			default -> unexpectedTokenError();
		};
	}

	// parses number literals
	private IntLiteral parseNum() throws ParserException {
		final var val = tokenizer.intValue();
		consume(NUM); // or nextToken() since NUM has already been recognized
		return new IntLiteral(val);
	}

	// parses boolean literals
	private BoolLiteral parseBoolean() throws ParserException {
		final var val = tokenizer.boolValue();
		consume(BOOL); // or nextToken() since BOOL has already been recognized
		return new BoolLiteral(val);
	}

	// parses variable identifiers
	private Variable parseVariable() throws ParserException {
		final var name = tokenizer.tokenString();
		consume(IDENT); // this check is necessary for parsing correctly the 'var' statement
		return new Variable(name);
	}

	// parses expressions with unary operator MINUS Atom ::= '-' Atom
	private Sign parseMinus() throws ParserException {
		consume(MINUS); // or nextToken() since MINUS has already been recognized
		return new Sign(parseAtom());
	}

	// parses expressions with unary operator FST Atom ::= 'fst' Atom
	private Fst parseFst() throws ParserException {
		consume(FST); // or nextToken() since FST has already been recognized
		return new Fst(parseAtom());
	}

	// parses expressions with unary operator SND Atom ::= 'snd' Atom
	private Snd parseSnd() throws ParserException {
		consume(SND); // or nextToken() since SND has already been recognized
		return new Snd(parseAtom());
	}

	// parses expressions with unary operator NOT Atom ::= '!' Atom

	private Not parseNot() throws ParserException {
		consume(NOT); // or nextToken() since NOT has already been recognized
		return new Not(parseAtom());
	}

	// parses expressions delimited by parentheses Atom ::= '(' Exp ')'
	private Exp parseRoundPar() throws ParserException {
		consume(OPEN_PAR); // this check is necessary for parsing correctly the 'if' statement
		final var exp = parseExp();
		consume(CLOSE_PAR);
		return exp;
	}

	// Aggiunto parsing per '[' e ']'
	/*
	 * private Exp parseVector() throws ParserException {
	 * consume(OPEN_VECT);
	 * final var exp = parseSep();
	 * consume(CLOSE_VECT);
	 * return exp;
	 * }
	 */

	// VectorLiteral ::= '[' Exp ';' Exp ']'
	private Exp parseVectorLit() throws ParserException {
		consume(OPEN_VECT);
		Exp exp1 = parseExp();
		consume(SEPARATOR);
		Exp exp2 = parseExp();
		consume(CLOSE_VECT);
		return new VectorLiteral(exp1, exp2);
	}

	// parsing per ';'. nome provvisorio
	private Exp parseSep() throws ParserException {
		var exp = parseAnd();
		while (tokenizer.tokenType() == SEPARATOR) {
			nextToken();
			exp = new PairLit(exp, parseAnd());
		}
		return exp;
	}

	// opens the input stream, standard input if -i option is null
	private static BufferedReader tryOpenInput(String inputPath) throws FileNotFoundException {
		return new BufferedReader(inputPath == null ? new InputStreamReader(System.in) : new FileReader(inputPath));
	}

	public static void main(String[] args) {
		try (var lang_reader = tryOpenInput(args.length > 0 ? args[0] : null);
				var lang_tokenizer = new MyLangTokenizer(lang_reader);
				var lang_parser = new MyLangParser(lang_tokenizer);) {
			var prog = lang_parser.parseProg();
			System.out.println(prog);
		} catch (IOException e) {
			err.println("I/O error: " + e.getMessage());
		} catch (ParserException e) {
			err.println("Syntax error: " + e.getMessage());
		} catch (Throwable e) {
			err.println("Unexpected error.");
			e.printStackTrace();
		}
	}

}