package progettoFinale.parser;

public enum TokenType {
	// symbols
	ASSIGN, MINUS, PLUS, TIMES, NOT, AND, EQ, STMT_SEP, PAIR_OP, OPEN_PAR, CLOSE_PAR, OPEN_BLOCK, CLOSE_BLOCK,
	// Simboli aggiunti: ';' '[' ']'
	SEPARATOR, OPEN_VECT, CLOSE_VECT,
	// keywords
	PRINT, VAR, BOOL, IF, ELSE, FST, SND,
	// keywords aggiunte: 'foreach' e 'in'
	FOREACH, IN,
	// non singleton categories
	SKIP, IDENT, NUM,
	// end-of-file
	EOF,
}
