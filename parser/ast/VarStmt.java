package progettoFinale.parser.ast;

import progettoFinale.visitors.Visitor;

public class VarStmt extends AbstractAssignStmt {

	public VarStmt(Variable var, Exp exp) {
		super(var, exp);
	}

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visitVarStmt(var, exp);
	}
}
