package text;

/**
 * <p>Represents a phrase located within a known file. 
 * Pairs a phrase with its word-index in the chapter 
 * from which it comes.</p>
 */
public class Phrase implements Comparable<Phrase>{
	
	/**
	 * <p>A phrase from the body of text being analysed.</p>
	 */
	public final String phrase;
	
	/**
	 * <p><code>phrase</code>'s word-index in its source chapter.</p>
	 */
	public final int index;
	
	/**
	 * <p>Constructs an <code>IntString</code> with the specified 
	 * word-index and phrase.</p>
	 * @param index <code>phrase</code>'s word-index in its source 
	 * chapter
	 * @param phrase a phrase from the body of text being analysed
	 */
	public Phrase(int index, String phrase) {
		this.index = index;
		this.phrase = phrase;
	}
	
	public int index(){
		return index;
	}
	
	public String phrase(){
		return phrase;
	}
	
	@Override
	/**
	 * <p>Returns a string representation of this IntString, 
	 * made of the concatenation of <code>index</code>, 
	 * <code>Location.ELEMENT_DELIM</code> 
	 * ({@value Location#ELEMENT_DELIM}), and 
	 * <code>phrase</code>.</p>
	 * @return a string representation of this IntString, 
	 * made of the concatenation of <code>index</code>, 
	 * <code>Location.ELEMENT_DELIM</code> 
	 * ({@value Location#ELEMENT_DELIM}), and 
	 * <code>phrase</code>.
	 */
	public String toString(){
		return index+Location.ELEMENT_DELIM+phrase;
	}
	
	/**
	 * <p>Compares the specified IntString to this one.</p>
	 * @param otherIntString the IntString to be compared to this one.
	 * @return an int whose sign indicates the natural 
	 * ordering between this IntString and the specified one.
	 */
	@Override
	public int compareTo(Phrase otherIntString){
		if(index != otherIntString.index){
			return Integer.compare(index, otherIntString.index);
		}
		return phrase.compareTo(otherIntString.phrase);
	}
}
