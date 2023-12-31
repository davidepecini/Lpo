package progettoFinale.parser.ast;

import static java.util.Objects.*;

import progettoFinale.visitors.Visitor;

public class VectorLiteral implements Exp {
	private final Exp exp1;
	private final Exp exp2;

	public VectorLiteral(Exp exp1, Exp exp2) {
		this.exp1 = requireNonNull(exp1);
		this.exp2 = requireNonNull(exp2);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName + "[" + exp1 + " ; " + exp2 + "]";
	}

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visitVectorLiteral(exp1, exp2);
	}

}
// aggiunta