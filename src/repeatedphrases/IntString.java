package repeatedphrases;

/**
 * <p>Represents a phrase located within a known file. 
 * Pairs a phrase with its word-index in the chapter 
 * from which it comes.</p>
 */
public class IntString implements Comparable<IntString>{
	
	/**
	 * <p>A phrase from the body of text being analysed.</p>
	 */
	public final String phrase;
	
	/**
	 * <p><code>phrase</code>'s word-index in its source chapter.</p>
	 */
	public final int index;
	
	//Constructors
	/**
	 * <p>Constructs an <code>IntString</code> with the specified 
	 * word-index and phrase.</p>
	 * @param index <code>phrase</code>'s word-index in its source 
	 * chapter
	 * @param phrase a phrase from the body of text being analysed
	 */
	public IntString(int index, String phrase) {
		this.index = index;
		this.phrase = phrase;
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
	@Override	//implements Comparable<Location>
	public int compareTo(IntString otherIntString){
		if(index != otherIntString.index){
			return index - otherIntString.index;
		}
		return phrase.compareTo(otherIntString.phrase);
	}
	
	//Getters
	/* *
	 * Returns the index portion of this Location
	 * @return	Returns the index portion of this Location
	 * /
	public Integer getIndex(){
		return index;
	}
	
	/ * *
	 * Returns the filename portion of this Location
	 * @return	Returns the filename portion of this Location
	 * /
	public String getPhrase(){
		return phrase;
	}/**/
	
	
	//Information
	/* *
	 * Assesses whether the specified object is equal to 
	 * this Location.
	 * @param o		Object to be tested for equality against 
	 * this Location.
	 * @return	True if o is a Location and has components 
	 * that are equal to the corresponding components of 
	 * this Location, false otherwise.
	 * /
	@Override	//overrides Object.equals
	public boolean equals(Object o){
		if(o instanceof IntString ){
			IntString is = (IntString) o;
			return index==is.index && is.phrase.equals(phrase);
		}
		else{
			return false;
		}
	}/**/
}
