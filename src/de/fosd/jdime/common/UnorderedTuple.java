package de.fosd.jdime.common;

import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * A <code>Tuple</code> whose {@link #equals(Object)} and {@link #hashCode()} methods are implemented for unordered
 * value equality.
 *
 * @param <X>
 * 		the type of the first object
 * @param <Y>
 * 		the type of the second object
 * @author Georg Seibt
 */
public class UnorderedTuple<X, Y> {

	private X x;
	private Y y;

	/**
	 * Constructs an <code>UnorderedTuple</code> of the two given objects.
	 *
	 * @param x
	 * 		the first object
	 * @param y
	 * 		the second object
	 * @param <X>
	 * 		the type of the first object
	 * @param <Y>
	 * 		the type of the second object
	 * @return an <code>UnorderedTuple</code> containing <code>x</code> and <code>y</code>
	 */
	public static <X, Y> UnorderedTuple<X, Y> of(X x, Y y) {
		return new UnorderedTuple<>(x, y);
	}

	/**
	 * Constructs a new <code>UnorderedTuple</code> containing the given objects.
	 *
	 * @param x
	 * 		the first object
	 * @param y
	 * 		the second object
	 */
	private UnorderedTuple(X x, Y y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Returns the first object contained in the <code>UnorderedTuple</code>.
	 *
	 * @return the first obejct
	 */
	public X getX() {
		return x;
	}

	/**
	 * Returns the second object contained in the <code>UnorderedTuple</code>.
	 *
	 * @return the second object
	 */
	public Y getY() {
		return y;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Tuple<?, ?> tuple = (Tuple<?, ?>) o;

		EqualsBuilder eqBuilder = new EqualsBuilder();
		eqBuilder.append(x, tuple.x);
		eqBuilder.append(y, tuple.y);

		if (eqBuilder.isEquals()) {
			return true;
		}

		eqBuilder.reset();
		eqBuilder.append(x, tuple.y);
		eqBuilder.append(y, tuple.x);

		return eqBuilder.isEquals();
	}

	@Override
	public int hashCode() {
		int hashX = x == null ? 0 : x.hashCode();
		int hashY = y == null ? 0 : y.hashCode();

		return hashX ^ hashY;
	}
}
