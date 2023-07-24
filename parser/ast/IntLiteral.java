package progettoFinale.parser.ast;

import progettoFinale.visitors.Visitor;

public class IntLiteral extends AtomicLiteral<Integer> {

	public IntLiteral(int n) {
		super(n);
	}

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visitIntLiteral(value);
	}
}
