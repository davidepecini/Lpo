package progettoFinale.parser.ast;

import progettoFinale.visitors.Visitor;

public interface AST {
	<T> T accept(Visitor<T> visitor);
}
