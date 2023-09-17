package progettoFinale.visitors.execution;

import static java.util.Objects.*;

import java.util.Arrays;

public class VectorValue implements Value {

	private final Value index;
	private final Value dim;
	private final int[] elements;

	private String EXPETED_ERR = "Expeted IntValue";
	private String DIMENSION_ERR = "Vectors should have the same dimension";

	public VectorValue(Value index, Value dim) {
		try {
			this.index = requireNonNull(checkValue(index));
			this.dim = requireNonNull(checkValue(dim));
			this.elements = new int[dim.toInt()];

			for (int i = 0; i < dimension.toInt(); i++)
				elements[i] = 0;
			elements[idex.toInt()] = 1;

		} catch (Exception e) {
			throw new InterpreterException(e);
		}
	}

	public VectorValue(int[] elements) {
		this.elements = requireNonNull(elements);
		this.index = new IntValue(0);
		this.dim = IntValue(elements.length);
	}

	public Value getDim() {
		return dim;
	}

	public Value getElements(int e) {
		return new intValue(elements[e]);
	}

	public boolean checkDimension(VectorValue v) {
		return this.dim.toInt() == v.dim.toInt();
	}

	public Value checkValue(Value index) {
		if (index instanceof IntValue)
			return index;
		throw new InterpreterException(EXPETED_ERR);
	}

	public IntValue mul(VectorValue v) {
		if (!checkDimension(v))
			throw new InterpreterException(DIMENSION_ERR);

		var val = 0;
		int size = getDimension().toInt();

		for (var i = 0; i < size; ++i)
			val += this.getElement(i).toInt() * v.getElement(i).toInt();

		return new IntValue(val);
	}

	public VectorValue sum(VectorValue v) {
		if (!checkDimension(v))
			throw new InterpreterException(DIMENSION_ERR);

		int size = getDimension().toInt();
		int[] vec = new int[size];

		for (var i = 0; i < size; ++i)
			vec[i] = this.getElement(i).toInt() + v.getElement(i).toInt();

		return new VectorValue(newVec);
	}

	public int hashCode() {
		return hash(Arrays.hashCode(elements));
	}

	@Override
	public VectorValue toVect() {
		return this;
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof VectorValue vt)
			return Arrays.equals(elements, vt.elements);
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < elements.length; i++) {
			sb.append(elements[i]);
			if (i != elements.length - 1)
				sb.append(";");
		}
		sb.append("]");
		return sb.toString();
	}
}
