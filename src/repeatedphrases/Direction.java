package repeatedphrases;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.IntUnaryOperator;

/**
 * <p>Represents a direction of motion on a Collection.
 * NEXT is motion to the right (increasing index).
 * PREV is motion to the left (decreasing index).</p>
 * 
 * <p>{@link #crawlTest crawlTest} tests 
 * that the specified position is not outside the bounds 
 * of the specified collection.</p>
 * 
 * <p>For rightward motion, <code>crawlTest</code> tests 
 * that the specified position is less than the size of 
 * the specified collection. For leftward motion, 
 * <code>crawlTest</code> tests that the specified 
 * position is non-negative and therefore not less than 
 * the minimum position in the collection.</p>
 * 
 * <p>{@link #apply apply} returns one more than the specified 
 * value for NEXT and one less than the specified value 
 * in the case of PREV.</p>
 */
public enum Direction{
	
	NEXT( (i) -> i+1 , (i, c) -> i<c.size() ), 
	PREV( (i) -> i-1 , (i, c) -> i>=0       );
	
	private final IntUnaryOperator op;
	
	private final BiFunction<Integer, Collection<?>, Boolean> crawlTest;
	
	private Direction(IntUnaryOperator op, BiFunction<Integer, Collection<?>, Boolean> crawlTest){
		this.op = op;
		this.crawlTest = crawlTest;
	}
	
	/**
	 * <p>Returns the operation that this Direction 
	 * applies to an int position in a collection 
	 * to generate the next position in the collection.</p>
	 * @return
	 */
	public IntUnaryOperator op(){
		return op;
	}
	
	/**
	 * <p>Applies this Direction's operation to the specified 
	 * position in a collection and returns the result.
	 * For {@link #NEXT NEXT}, returns <code>i+1</code>.
	 * For {@link #PREV PREV}, returns <code>i-1</code>.</p>
	 * @param i position in a collection, for which the 
	 * next position is generated and returned.
	 * @return the next position in a collection after 
	 * the specified position <code>i</code>.
	 */
	public int apply(int i){
		return op.applyAsInt(i);
	}
	
	/**
	 * <p>Returns true if the position in the specified collection 
	 * is within the bounds of the collection. 
	 * For NEXT, returns true if the specified position is 
	 * less than the size of the specified collection, 
	 * false otherwise.
	 * For PREV, returns true if the specified position is 
	 * greater than or equal to 0, the lower bound 
	 * of any collection, false otherwise.</p>
	 * @param i a position in the collection.
	 * @param c the collection against whose bounds <code>i</code> 
	 * is compared
	 * @return true if <code>i</code> is within the bounds 
	 * of <code>c</code>, false otherwise.
	 */
	public boolean crawlTest(int i, Collection<?> c){
		return crawlTest.apply(i,c);
	}
}