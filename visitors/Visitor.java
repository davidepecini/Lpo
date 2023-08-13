package progettoFinale.visitors;

import progettoFinale.parser.ast.Block;
import progettoFinale.parser.ast.Exp;
import progettoFinale.parser.ast.Stmt;
import progettoFinale.parser.ast.StmtSeq;
import progettoFinale.parser.ast.Variable;

public interface Visitor<T> {
	T visitAdd(Exp left, Exp right);

	T visitAssignStmt(Variable var, Exp exp);

	T visitIntLiteral(int value);

	T visitEq(Exp left, Exp right);

	T visitNonEmptyStmtSeq(Stmt first, StmtSeq rest);

	T visitMul(Exp left, Exp right);

	T visitPrintStmt(Exp exp);

	T visitMyLangProg(StmtSeq stmtSeq);

	T visitSign(Exp exp);

	T visitVariable(Variable var); // only in this case more efficient then T visitVariable(String name)

	T visitEmptyStmtSeq();

	T visitVarStmt(Variable var, Exp exp);

	T visitNot(Exp exp);

	T visitAnd(Exp left, Exp right);

	T visitBoolLiteral(boolean value);

	T visitIfStmt(Exp exp, Block thenBlock, Block elseBlock);

	T visitBlock(StmtSeq stmtSeq);

	T visitPairLit(Exp left, Exp right);

	T visitFst(Exp exp);

	T visitSnd(Exp exp);

	// aggiunte
	T visitForeachStmt(Variable ident, Exp exp, Block foreachBlock);

	T visitVectorLiteral(Exp exp1, Exp exp2);

	T visitGenericAdd(Exp exp1, Exp exp2);

	T visitGenericMul(Exp exp1, Exp exp2);
}
