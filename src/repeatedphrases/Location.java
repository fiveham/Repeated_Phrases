package repeatedphrases;

/**
 * <p>Represents a location in a multi-file body of text 
 * at which a phrase begins. The index is the number of words 
 * between the first word of the body of the file and 
 * the first word of the phrase at the indicated word-index in 
 * the file.</p>
 */
public class Location implements Comparable<Location>{
	
	/**
	 * <p>The term used to mark the boundary between the filename 
	 * part of a Location and the index part when the Location 
	 * is represented {@linkplain #toString() as a string}.</p>
	 */
	public static final String ELEMENT_DELIM = ";";
	
	/**
	 * <p>The file in which is located the phrase-instance 
	 * to which this Location pertains.</p>
	 */
	private String filename;
	
	/**
	 * <p>The word-index of first word of this Location's 
	 * pertinent phrase in the file.</p>
	 * <p>A word-index 0 indicates the first word in the 
	 * body of the chapter.</p>
	 */
	private int index;
	
	
	//Constructors
	/**
	 * <p>Constructs a Location with the specified index and filename.</p>
	 * @param index the index for this Location
	 * @param file the filename for this Location
	 */
	public Location(int index, String file) {
		this.index = index;
		this.filename = file;
	}
	
	
	//Getters
	/**
	 * <p>Returns the index portion of this Location</p>
	 * @return the index portion of this Location
	 */
	public Integer getIndex(){
		return index;
	}
	
	/**
	 * <p>Returns the filename portion of this Location</p>
	 * @return the filename portion of this Location
	 */
	public String getFilename(){
		return filename;
	}
	
	
	//Information
	/**
	 * <p>Assesses whether the specified object is equal to 
	 * this Location.</p>
	 * @param o object to be tested for equality against 
	 * this Location.
	 * @return true if <code>o</code> is a Location and its 
	 * components are equal to those of this Location, 
	 * false otherwise.
	 */
	@Override	//overrides Object.equals
	public boolean equals(Object o){
		if(o instanceof Location ){
			Location loc = (Location) o;
			return index==loc.index && loc.filename.equals(filename);
		}
		else{
			return false;
		}
	}
	
	/**
	 * <p>Compares the specified Location to this one.</p>
	 * @param loc a Location to be compared to this one.
	 * @return an int whose sign indicates the natural 
	 * ordering between this Location and <code>loc</code>.
	 */
	@Override	//implements Comparable<Location>
	public int compareTo(Location loc){
		if(equals(loc)){
			return 0;
		}
		
		int fileComp = filename.compareTo(loc.filename);
		if( fileComp!=0 ){
			return fileComp;
		}
		
		return index - loc.index;
	}
	
	
	//Tools
	/**
	 * <p>Returns a String representation of this Location. 
	 * Intended for printing Locations to a file.</p>
	 * @return the concatenation of {@link #filename filename}, 
	 * {@link #ELEMENT_DELIM ELEMENT_DELIM}, and 
	 * {@link #index index}
	 */
	@Override	//overrides Object.toString
	public String toString(){
		return filename + ELEMENT_DELIM + index;
	}
	
	/**
	 * <p>Returns a string representation of this Location 
	 * lacking any folders or file-extensions mentioned in 
	 * <code>filename</code>.</p>
	 */
	public String shortString(){
		return IO.stripFolderExtension(filename) + ELEMENT_DELIM + index;
	}
	
	/**
	 * <p>Subtracts <code>thatLocation.index</code> 
	 * from <code>index</code> and returns the result. 
	 * Throws an exception if these are Locations 
	 * in different files.</p>
	 * @param thatLocation a Location whose index 
	 * is extracted
	 * @return this.index - thatLocation.index
	 * @throws IllegalArgumentException if the two 
	 * Locations have different filenames
	 */
	public int minus(Location thatLocation){
		if( !filename.equals(thatLocation.filename) )
			throw new IllegalArgumentException("mismatching filenames: "+filename+" and "+thatLocation.filename);
		return this.index - thatLocation.index;
	}
	
	/**
	 * <p>Adds <code>indx</code> to <code>this.index</code> and 
	 * creates a new Location with <code>filename</code> and 
	 * <code>this.index + indx</code> as its {@link #index index}.</p>
	 * @param indx a value to be added to <code>index</code>
	 * @return a new Location with the same filename as this one 
	 * and an {@link #index index} equal to the sum of 
	 * <code>index</code> and <code>indx</code>
	 */
	public Location add(int indx){
		return new Location(index+indx, filename);
	}
}
