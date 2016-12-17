package text;

import common.IO;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * <p>Wraps a Map linking chapter filenames with data structures that store pairs of String and int.
 * This is used to store quote data in a way that enables all the quotes from a given chapter to be
 * accessed easily.</p>
 */
public class FileBox extends HashMap<Chapter, List<Quote>>{
	
    /**
     * <p>Constructs a FileBox with no contents.</p>
     */
	public FileBox() {
		super();
	}
	
    /**
     * <p>Constructs a FileBox with all the quote data contained in the specified File
     * {@code f}.</p>
     * @param f a File from which to read quote data
     * @throws FileNotFoundException when {@code f} does not exist or cannot be read
     */
	public FileBox(File f) throws FileNotFoundException{
		this(new Scanner(f, IO.ENCODING));
	}
	
    /**
     * <p>Constructs a FileBox containing the quote data represented in the body that {@code scan}
     * reads.</p>
     * @param scan a Scanner that produces quote data as though reading files that go in
     * {@link #Folder.REPEATS REPEATS}, {@link #Folder.INDEPENDENT_INSTANCES INDEPENDENT_INSTANCES},
     * or {@link #Folder.DUPLICATE_INDEPENDENTS DUPLICATE_INDEPENDENTS}.
     */
	public FileBox(Scanner scan){
	    super();
		try{
			while(IO.scannerHasNonEmptyNextLine(scan)){
				String[] phraseAndLocations = scan.nextLine().split(IO.LOCATION_DELIM);
				
				String phrase = phraseAndLocations[0];
				
				for(int i=1; i<phraseAndLocations.length; i++){
					String[] fileAndIndex = phraseAndLocations[i].split(Location.ELEMENT_DELIM);
					String filename = fileAndIndex[Location.FILENAME_POSITION];
					int index = Integer.parseInt(fileAndIndex[Location.INDEX_POSITION]);
					Chapter chapter = null; //FIXME get pertinent chapter for filename
					
					Quote quote = new Quote(
							new Location(index, chapter), 
							phrase);
					
					if(containsKey(filename)){
						get(filename).add(quote);
					} else{
						List<Quote> l = new ArrayList<>();
						l.add(quote);
						put(chapter, l);
					}
				}
			}
			
			scan.close();
		} catch(ArrayIndexOutOfBoundsException e){
			throw new IllegalArgumentException(
					"The content scanned by the specified Scanner is not structured like a record " 
					+ "of phrases and locations.");
		}
	}
	
    /**
     * <p>Returns a {@link java.util.Set Set} of the names of the files of the chapters for which
     * this FileBox has phrase- instance data.</p>
     * @return a {@link java.util.Set Set} of the names of the files of the chapters for which this
     * FileBox has phrase- instance data
     */
	public Set<Chapter> chapters(){
		return keySet();
	}
	
    /**
     * <p>Returns true if there is an entry for {@code filename} in this FileBox, false
     * otherwise.</p>
     * @param filename the filename whose status as a key in the underlying HashMap is returned
     * @return true if there is an entry for {@code filename} in this FileBox, false otherwise.
     */
	public boolean contains(String filename){
		return containsKey(filename);
	}
}
