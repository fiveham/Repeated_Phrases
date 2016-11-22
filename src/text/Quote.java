package text;

/**
 * <p>Represents a phrase located within a known file. 
 * Pairs a phrase with its word-index in the chapter 
 * from which it comes.</p>
 */
public class Quote implements Comparable<Quote>{
	
	/**
	 * <p>A phrase from the body of text being analysed.</p>
	 */
	private final String text;
	
	/**
	 * <p>{@code phrase}'s word-index in its source chapter.</p>
	 */
	private final Location location;
	
	/**
	 * <p>Constructs an {@code IntString} with the specified 
	 * word-index and phrase.</p>
	 * @param index {@code phrase}'s word-index in its source 
	 * chapter
	 * @param phrase a phrase from the body of text being analysed
	 */
	public Quote(Location location, String phrase) {
		this.location = location;
		this.text = phrase;
	}
	
	public int index(){
		return location.getIndex();
	}
	
	public Location location(){
		return location;
	}
	
	public String text(){
		return text;
	}
	
	@Override
	/**
	 * <p>Returns a string representation of this IntString, 
	 * made of the concatenation of {@code index}, 
	 * {@code Location.ELEMENT_DELIM} 
	 * ({@value Location#ELEMENT_DELIM}), and 
	 * {@code phrase}.</p>
	 * @return a string representation of this IntString, 
	 * made of the concatenation of {@code index}, 
	 * {@code Location.ELEMENT_DELIM} 
	 * ({@value Location#ELEMENT_DELIM}), and 
	 * {@code phrase}.
	 */
	public String toString(){
		return index()+Location.ELEMENT_DELIM+text;
	}
	
	/**
	 * <p>Compares the specified Quote to this one.</p>
	 * @param otherQuote the Quote to be compared to this one.
	 * @return an int whose sign indicates the natural 
	 * ordering between this Quote and the specified one.
	 */
	@Override
	public int compareTo(Quote otherQuote){
		if(index() != otherQuote.index()){
			return Integer.compare(index(), otherQuote.index());
		}
		return text.compareTo(otherQuote.text);
	}
}
