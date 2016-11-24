package text;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import common.IO;

/**
 * <p>Wraps a Map linking chapter filenames with data structures that store pairs of String and int.
 * This is used to store quote data in a way that enables all the quotes from a given chapter to be
 * accessed easily.</p>
 */
public class FileBox{
	
    /**
     * <p>The wrapped HashMap.</p>
     */
	private final Map<String, List<Quote>> hashmap;
	
    /**
     * <p>Constructs a FileBox with no contents.</p>
     */
	public FileBox() {
		hashmap = new HashMap<>();
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
		try{
			hashmap = new HashMap<>();
			
			while(IO.scannerHasNonEmptyNextLine(scan)){
				String[] phraseAndLocations = scan.nextLine().split(IO.LOCATION_DELIM);
				
				String phrase = phraseAndLocations[0];
				
				for(int i=1; i<phraseAndLocations.length; i++){
					String[] fileAndIndex = phraseAndLocations[i].split(Location.ELEMENT_DELIM);
					String filename = fileAndIndex[0];
					
					Quote quote = new Quote(
							new Location(Integer.parseInt(fileAndIndex[1]), filename), 
							phrase);
					
					if(hashmap.containsKey(filename)){
						hashmap.get(filename).add(quote);
					} else{
						List<Quote> l = new ArrayList<>();
						l.add(quote);
						hashmap.put(filename, l);
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
	public Set<String> filenames(){
		return hashmap.keySet();
	}
	
    /**
     * <p>Returns the {@literal List<IntString>} mapped in the underlying HashMap for the key
     * {@code o}. Returns {@code null} if there is no mapping for {@code o} in the underlying
     * HashMap.</p>
     * @param o the key filename whose value {@literal List<IntString>} is to be returned
     * @return the {@literal List<IntString>} mapped in the underlying HashMap for the key {@code o}
     */
	public List<Quote> get(Object o){
		return hashmap.get(o);
	}
	
    /**
     * <p>Returns true if there is an entry for {@code filename} in this FileBox, false
     * otherwise.</p>
     * @param filename the filename whose status as a key in the underlying HashMap is returned
     * @return true if there is an entry for {@code filename} in this FileBox, false otherwise.
     */
	public boolean contains(String filename){
		return hashmap.containsKey(filename);
	}
}
