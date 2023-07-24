package progettoFinale.parser.ast;

import static java.util.Objects.*;

import progettoFinale.visitors.Visitor;

public class ForeachStmt implements Stmt {
	private final Variable ident;
	private final Exp exp;
	private final Block foreachBlock;

	public ForeachStmt(Variable ident, Exp exp, Block foreachBlock) {
		this.ident = requireNonNull(ident);
		this.exp = requireNonNull(exp);
		this.foreachBlock = requireNonNull(foreachBlock);
	}

	public Variable getIdent() {
		return ident;
	}

	public Exp getExp() {
		return exp;
	}

	public Block getForeachBlock() {
		return foreachBlock;
	}

	@Override
	public String toString() {
		return "foreach " + ident + " in " + exp + " " + foreachBlock;
	}

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visitForeachStmt(ident, exp, foreachBlock);

	}
}

// Aggiunta