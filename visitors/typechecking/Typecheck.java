package progettoFinale.visitors.typechecking;

import static progettoFinale.visitors.typechecking.AtomicType.*;

import progettoFinale.environments.EnvironmentException;
import progettoFinale.environments.GenEnvironment;
import progettoFinale.parser.ast.Block;
import progettoFinale.parser.ast.Exp;
import progettoFinale.parser.ast.Stmt;
import progettoFinale.parser.ast.StmtSeq;
import progettoFinale.parser.ast.Variable;
import progettoFinale.visitors.Visitor;

public class Typecheck implements Visitor<Type> {

	private final GenEnvironment<Type> env = new GenEnvironment<>();

	// useful to typecheck binary operations where operands must have the same type
	private void checkBinOp(Exp left, Exp right, Type type) {
		type.checkEqual(left.accept(this));
		type.checkEqual(right.accept(this));
	}

	// static semantics for programs; no value returned by the visitor

	@Override
	public Type visitMyLangProg(StmtSeq stmtSeq) {
		try {
			stmtSeq.accept(this);
		} catch (EnvironmentException e) { // undeclared variable
			throw new TypecheckerException(e);
		}
		return null;
	}

	// static semantics for statements; no value returned by the visitor

	@Override
	public Type visitAssignStmt(Variable var, Exp exp) {
		var found = env.lookup(var);
		found.checkEqual(exp.accept(this));
		return null;
	}

	@Override
	public Type visitPrintStmt(Exp exp) {
		exp.accept(this);
		return null;
	}

	@Override
	public Type visitVarStmt(Variable var, Exp exp) {
		env.dec(var, exp.accept(this));
		return null;
	}

	@Override
	public Type visitIfStmt(Exp exp, Block thenBlock, Block elseBlock) {
		BOOL.checkEqual(exp.accept(this));
		thenBlock.accept(this);
		if (elseBlock != null)
			elseBlock.accept(this);
		return null;
	}

	@Override
	public Type visitBlock(StmtSeq stmtSeq) {
		env.enterScope();
		stmtSeq.accept(this);
		env.exitScope();
		return null;
	}

	// static semantics for sequences of statements
	// no value returned by the visitor

	@Override
	public Type visitEmptyStmtSeq() {
		return null;
	}

	@Override
	public Type visitNonEmptyStmtSeq(Stmt first, StmtSeq rest) {
		first.accept(this);
		rest.accept(this);
		return null;
	}

	// static semantics of expressions; a type is returned by the visitor

	@Override
	public AtomicType visitAdd(Exp left, Exp right) {
		checkBinOp(left, right, INT);
		return INT;
	}

	@Override
	public AtomicType visitIntLiteral(int value) {
		return INT;
	}

	@Override
	public AtomicType visitMul(Exp left, Exp right) {
		checkBinOp(left, right, INT);
		return INT;
	}

	@Override
	public AtomicType visitSign(Exp exp) {
		INT.checkEqual(exp.accept(this));
		return INT;
	}

	@Override
	public Type visitVariable(Variable var) {
		return env.lookup(var);
	}

	@Override
	public AtomicType visitNot(Exp exp) {
		BOOL.checkEqual(exp.accept(this));
		return BOOL;
	}

	@Override
	public AtomicType visitAnd(Exp left, Exp right) {
		checkBinOp(left, right, BOOL);
		return BOOL;
	}

	@Override
	public AtomicType visitBoolLiteral(boolean value) {
		return BOOL;
	}

	@Override
	public AtomicType visitEq(Exp left, Exp right) {
		left.accept(this).checkEqual(right.accept(this));
		return BOOL;
	}

	@Override
	public PairType visitPairLit(Exp left, Exp right) {
		return new PairType(left.accept(this), right.accept(this));
	}

	@Override
	public Type visitFst(Exp exp) {
		return exp.accept(this).getFstPairType();
	}

	@Override
	public Type visitSnd(Exp exp) {
		return exp.accept(this).getSndPairType();
	}

	// aggiunte
	@Override
	public Type visitForeachStmt(Variable ident, Exp exp, Block foreachBlock) {
		exp.accept(this).checkIsRangeType();
		env.enterScope();
		env.dec(ident, INT);
		foreachBlock.accept(this);
		env.exitScope();
		return null;
	}

	@Override
	public Type visitVectorLiteral(Exp exp1, Exp exp2) {
		checkBinOp(exp1, exp2, INT);
		return VECTOR;
	}

	@Override
	public Type visitGenericAdd(Exp exp1, Exp exp2) {
		Type type1 = exp1.accept(this);
		Type type2 = exp2.accept(this);

		if (type1.equals(INT) && type2.equals(INT)) {
			return INT;
		} else if (type1.equals(VECTOR) && type2.equals(VECTOR)) {
			if (exp1.size() != exp2.size()) {
				throw new TypecheckerException("Vectors with different dimensions");
			}
			return VECTOR;
		} else {
			throw new TypecheckerException("Operands must be integers or vectors");
		}
	}

	@Override
	public Type visitGenericMul(Exp exp1, Exp exp2) {
		Type type1 = exp1.accept(this);
		Type type2 = exp2.accept(this);

		if (type1.equals(INT) && type2.equals(INT)) {
			return INT;
		} else if (type1.equals(VECTOR) && type2.equals(INT) || type1.equals(INT) && type2.equals(VECTOR)) {
			return VECTOR;
		} else {
			throw new TypecheckerException("Operands must be integers or vectors");
		}
	}
}
