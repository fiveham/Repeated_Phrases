package repeatedphrases;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>An enhancement of {@link java.util.ArrayList ArrayList}, adding 
 * <code>String</code>-style {@link java.lang.String#indexOf(Object,int) indexOf()} and 
 * {@link java.lang.String#lastIndexOf(Object,int) lastIndexOf()} methods for use in 
 * {@link HTMLFile HTMLFile}.</p>
 * @param <T> the type of the elements of this list
 */
public class ArrayList2<T> extends ArrayList<T> {
	
	/**
	 * <p>automatically generated</p>
	 */
	private static final long serialVersionUID = -2567485613070128549L;

	/**
	 * <p>Constructs an ArrayList2 with an initial capacity of 10 
	 * and no elements.</p>
	 */
	public ArrayList2() {
		super();
	}
	
	/**
	 * <p>Constructs an ArrayList2 with an initial capacity of 
	 * <code>arg0</code> and no elements.</p>
	 * @param arg0 the initial capacity of this ArrayList2
	 */
	public ArrayList2(int arg0) {
		super(arg0);
	}
	
	/**
	 * <p>Constructs an ArrayList2 with an initial capacity of 
	 * <code>arg0.size()</code>, containing all the elements 
	 * of <code>arg0</code>.</p>
	 * @param arg0 the collection whose elements are added to 
	 * this ArrayList2
	 */
	public ArrayList2(Collection<T> arg0) {
		super(arg0);
	}
	
	/**
	 * <p>Returns the first index <code>i</code> greater than or equal to <code>fromIndex</code> 
	 * at which <code>o</code> occurs, as determined by <code>o.equals(get(i))</code>.</p>
	 * 
	 * @param o the object to be found
	 * 
	 * @param fromIndex the position in this list at which to start searching
	 * 
	 * @return the first index <code>i</code> greater than or equal to <code>fromIndex</code> 
	 * at which <code>o</code> occurs, as determined by <code>o.equals(get(i))</code>.
	 * 
	 * @see java.lang.String#indexOf(String,int)
	 */
	public int indexOf(Object o, int fromIndex){
		return fromIndex==0 ? super.indexOf(o) : subList(fromIndex, size()).indexOf(o) + fromIndex;
	}
	
	/**
	 * <p>Returns the highest index <code>i</code> less than or equal to 
	 * <code>fromIndex</code> at which <code>o</code> occurs, as determined 
	 * by <code>o.equals(get(i))</code>.</p>
	 * 
	 * <p>If <code>fromIndex</code> is greater than or equal to 
	 * <code>size()</code>, then a result is returned as if <code>fromIndex</code> 
	 * were equal to </code>size()-1</code>. This feature is included to mimic 
	 * the behavior of <code>String.lastIndexOf(char,int)</code>.</p>
	 * 
	 * @param o the object to be found
	 * 
	 * @param fromIndex the position from which to start the search
	 * 
	 * @return the highest index <code>i</code> less than or equal to 
	 * <code>fromIndex</code> at which <code>o</code> occurs, as determined 
	 * by <code>o.equals(get(i))</code>.
	 * 
	 * @see java.lang.String#lastIndexOf(String,int)
	 */
	public int lastIndexOf(Object o, int fromIndex){
		if(fromIndex >= size()){
			fromIndex = size()-1;
		}
		return fromIndex == size()-1 ? super.lastIndexOf(o) : subList(0, fromIndex+1).lastIndexOf(o);
	}

}
