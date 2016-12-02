package text;

import common.IO;

/**
 * <p>Represents a location in a multi-file body of text at which a phrase begins. The index is the
 * number of words between the first word of the body of the file and the first word of the phrase
 * at the indicated word-index in the file.</p>
 */
public class Location implements Comparable<Location>{
	
    /**
     * <p>The term used to mark the boundary between the filename part of a Location and the index
     * part when the Location is represented {@linkplain #toString() as a string}.</p>
     */
	public static final String ELEMENT_DELIM = ";";
	
	private Chapter chapter;
	
    /**
     * <p>The word-index of first word of this Location's pertinent phrase in the file.</p> <p>A
     * word-index 0 indicates the first word in the body of the chapter.</p>
     */
	private int index;
	
	//Constructors
    /**
     * <p>Constructs a Location with the specified index and filename.</p>
     * @param index the index for this Location
     * @param file the filename for this Location
     */
	public Location(int index, Chapter chapter) {
		this.index = index;
		this.chapter = chapter;
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
		return chapter.getName();
	}
	
	//Information
	@Override
    /**
     * <p>Assesses whether the specified object is equal to this Location.</p>
     * @param o object to be tested for equality against this Location.
     * @return true if {@code o} is a Location and its components are equal to those of this
     * Location, false otherwise.
     */
	public boolean equals(Object o){
		if(o instanceof Location ){
			Location loc = (Location) o;
			return index == loc.index && loc.getFilename().equals(getFilename());
		} else{
			return false;
		}
	}

	@Override
    /**
     * <p>Compares the specified Location to this one.</p>
     * @param loc a Location to be compared to this one.
     * @return an int whose sign indicates the natural ordering between this Location and
     * {@code loc}.
     */
	public int compareTo(Location loc){
		if(equals(loc)){
			return 0;
		}
		
		int fileComp = getFilename().compareTo(loc.getFilename());
		return fileComp != 0 
    			? fileComp 
    			: Integer.compare(index, loc.index);
	}
	
	//Tools
	@Override
    /**
     * <p>Returns a String representation of this Location. Intended for printing Locations to a
     * file.</p>
     * @return the concatenation of {@link #filename filename},
     * {@link #ELEMENT_DELIM ELEMENT_DELIM}, and {@link #index index}
     */
	public String toString(){
		return getFilename() + ELEMENT_DELIM + index;
	}
	
    /**
     * <p>Returns a string representation of this Location lacking any folders or file-extensions
     * mentioned in {@code filename}.</p>
     */
	public String shortString(){
		return IO.stripFolderExtension(getFilename()) + ELEMENT_DELIM + index;
	}
	
    /**
     * <p>Subtracts {@code thatLocation.index} from {@code index} and returns the result. Throws an
     * exception if these are Locations in different files.</p>
     * @param thatLocation a Location whose index is extracted
     * @return this.index - thatLocation.index
     * @throws IllegalArgumentException if the two Locations have different filenames
     */
	public int minus(Location thatLocation){
		if(getFilename().equals(thatLocation.getFilename())){
			return this.index - thatLocation.index;
		} else{
			throw new IllegalArgumentException(
					"mismatching filenames: " + getFilename() + " and " + thatLocation.getFilename());
		}
	}
	
    /**
     * <p>Adds {@code indx} to {@code this.index} and creates a new Location with {@code filename}
     * and {@code this.index + indx} as its {@link #index index}.</p>
     * @param indx a value to be added to {@code index}
     * @return a new Location with the same filename as this one and an {@link #index index} equal
     * to the sum of {@code index} and {@code indx}
     */
	public Location add(int indx){
		return new Location(index+indx, chapter);
	}
}
