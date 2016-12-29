package text;

import common.Files;
import html.HtmlChapter;
import java.util.List;

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
	public Chapter getChapter(){
	    return chapter;
	}
	
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
    /**
     * <p>Assesses whether the specified object is equal to this Location.</p>
     * @param o object to be tested for equality against this Location.
     * @return true if {@code o} is a Location and its components are equal to those of this
     * Location, false otherwise.
     */
    @Override
    public boolean equals(Object o){
        if(o == this){
            return true;
        }
        if(o instanceof Location){
            Location l = (Location) o;
            return l.index == index && l.chapter.equals(chapter);
        }
        return false;
    }
    
    /**
     * <p>Compares the specified Location to this one.</p>
     * @param loc a Location to be compared to this one.
     * @return an int whose sign indicates the natural ordering between this Location and
     * {@code loc}.
     */
    @Override
	public int compareTo(Location loc){
		if(equals(loc)){
			return 0;
		}
		
		int fileComp = compareFilenames(getFilename(), loc.getFilename());
		return fileComp != 0 
    			? fileComp 
    			: Integer.compare(index, loc.index);
	}
	
    /**
     * <p>Compares the filenames of two {@code Location}s according to the order of the ASOIAF books
     * given by {@link #bookList bookList}.</p>
     * @param f1 the {@link Location#getFilename() filename} of a Location
     * @param f2 the {@link Location#getFilename() filename} of a Location
     * @return a negative value if {@code f1}'s book precedes that of {@code f2}, a positive value
     * if {@code f2's} precedes {@code f1}'s, or zero if {@code f1} and {@code f2} have the same
     * book.
     */
    private static int compareFilenames(String f1, String f2){
        String[] split1 = Files.stripExtension(f1)
                .split(Files.FILENAME_COMPONENT_SEPARATOR, HtmlChapter.FILENAME_ELEMENT_COUNT);
        String book1 = split1[0];
        String chapterNumber1 = split1[1];
        
        String[] split2 = Files.stripExtension(f2)
                .split(Files.FILENAME_COMPONENT_SEPARATOR, HtmlChapter.FILENAME_ELEMENT_COUNT);
        String book2 = split2[0];
        String chapterNumber2 = split2[1];
        
        int comp = Book.valueOf(book1).ordinal() - Book.valueOf(book2).ordinal();
        return comp != 0 
                ? comp 
                : Integer.parseInt(chapterNumber1) - Integer.parseInt(chapterNumber2);
    }
    
    private static enum Book {
        AGOT, 
        ACOK, 
        ASOS, 
        AFFC, 
        ADWD, 
        DE, 
        PQ, 
        RP;
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
	
	public boolean hasPredecessor(){
	    return index != 0;
	}
	
	public Location getPredecessor(){
	    return chapter.getLocations().get(index - 1);
	}
	
	public Location after(List<Location> locs){
	    int i = locs.indexOf(this);
        if(i < 0){
            throw new IllegalArgumentException(
                    "This Location " + toString() + " is not present in the specified list.");
        } else{
            i++;
            return locs.get(i % locs.size());
        }
	}
	
	@Override
	public int hashCode(){
	    return chapter.hashCode() * 31 + index;
	}
}
