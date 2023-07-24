package progettoFinale.visitors.execution;

import static java.util.Objects.*;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class VectorValue implements Value, Iterator<Integer>, Iterable<Integer> {

	private int start;
	private int end;

	public VectorValue(int start, int end) {
		this.start = requireNonNull(start);
		this.end = requireNonNull(end);
	}

	public VectorValue(VectorValue vect) {
		this.start = requireNonNull(vect.start);
		this.end = requireNonNull(vect.end);
	}

	public IntValue getStart() {
		return new IntValue(start);
	}

	public IntValue getEnd() {
		return new IntValue(end);
	}

	public boolean hasNext() {
		return start < end;
	}

	@Override
	public Integer next() {
		if (!hasNext())
			throw new NoSuchElementException();
		int res;
		res = start;
		if (start < end)
			start++;
		else
			start--;
		return res;
	}

	@Override
	public Iterator<Integer> iterator() {
		return this;
	}

	@Override
	public VectorValue toVect() {
		return this;
	}

	// @Override public String toString() { return "[" + start + ";" + end + "]";}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		boolean first = true;
		for (int i : this) {
			if (!first) {
				sb.append(";");
			}
			sb.append(i);
			first = false;
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return hash(start, end);
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof VectorValue))
			return false;
		var op = (VectorValue) obj;
		return (!this.hasNext() && !op.hasNext()) || (start == op.start && end == op.end);
	}

}
