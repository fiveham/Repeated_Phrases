package text;

/**
 * <p>Represents a phrase located within a known file. Pairs a phrase with its word-index in the
 * chapter from which it comes.</p>
 */
public class Quote implements Comparable<Quote>{
	
	private final Phrase phrase;
	
    /**
     * <p>{@code phrase}'s word-index in its source chapter.</p>
     */
	private final Location location;
	
    /**
     * <p>Constructs an {@code IntString} with the specified word-index and phrase.</p>
     * @param index {@code phrase}'s word-index in its source chapter
     * @param phrase a phrase from the body of text being analysed
     */
	public Quote(Location location, Phrase phrase) {
		this.location = location;
		this.phrase = phrase;
	}
	
	public int getIndex(){
		return location.getIndex();
	}
	
	public Location getLocation(){
		return location;
	}
	
	public String getText(){
		return phrase.getText();
	}
	
	public Phrase getPhrase(){
	    return phrase;
	}
	
	@Override
    /**
     * <p>Returns a string representation of this IntString, made of the concatenation of
     * {@code index}, {@code Location.ELEMENT_DELIM} ({@value Location#ELEMENT_DELIM}), and
     * {@code phrase}.</p>
     * @return a string representation of this IntString, made of the concatenation of
     * {@code index}, {@code Location.ELEMENT_DELIM} ({@value Location#ELEMENT_DELIM}), and
     * {@code phrase}.
     */
	public String toString(){
		return getIndex() + Location.ELEMENT_DELIM + getText();
	}
	
    /**
     * <p>Compares the specified Quote to this one.</p>
     * @param otherQuote the Quote to be compared to this one.
     * @return an int whose sign indicates the natural ordering between this Quote and the specified
     * one.
     */
	@Override
	public int compareTo(Quote otherQuote){
		int comp = phrase.compareTo(otherQuote.phrase);
		return comp != 0 
		        ? comp 
		        : location.compareTo(otherQuote.location);
	}
	
	public boolean isIndependent(){
	    return !isDependent();
	}
	
	private boolean isDependent(){
        Chapter c = location.getChapter();
        return c.hasLargerPhraseAt(location, phrase) 
                || (location.hasPredecessor() 
                        && c.hasLargerPhraseAt(location.getPredecessor(), phrase));
	}
	
	@Override
	public boolean equals(Object o){
	    if(o == this){
	        return true;
	    }
	    if(o instanceof Quote){
	        Quote q = (Quote) o;
	        return q.location.equals(location) && q.phrase.equals(phrase);
	    }
	    return false;
	}
	
	@Override
	public int hashCode(){
	    return phrase.hashCode() * location.hashCode();
	}
}
