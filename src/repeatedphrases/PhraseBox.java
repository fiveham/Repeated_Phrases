package repeatedphrases;

import java.io.FileNotFoundException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Consumer;

import common.IO;
import text.Location;

/**
 * <p>Wraps a HashMap linking string phrases with data structures 
 * that store multiple Locations.</p>
 */
public class PhraseBox{
	
	/**
	 * <p>The wrapped HashMap.</p>
	 */
	private final Map<String, List<Location>> hashmap;
	
	/**
	 * <p>Constructs a PhraseBox with no contents.</p>
	 */
	public PhraseBox() {
		hashmap = new HashMap<>();
	}
	
	/**
	 * <p>Constructs a PhraseBox with the phrase-instance 
	 * data contents of <code>f</code>.</p>
	 * @param f the file whose phrase-instance data is to 
	 * be put into this PhraseBox
	 * @throws FileNotFoundException if <code>f</code> 
	 * does not exist or cannot be read
	 */
	public PhraseBox(File f) throws FileNotFoundException{
		this(new Scanner(f, IO.ENCODING));
	}
	
	/**
	 * <p>Constructs a PhraseBox with the phrase-instance 
	 * data produced by <code>scan</code>. <code>scan.nextLine()</code> 
	 * is called repeatedly, and each resulting line is 
	 * parsed as a line from a phrase-instance data file, such 
	 * as those written to Folder.REPEATS, 
	 * Folder.INDEPENDENT_INSTANCES, or 
	 * Folder.DUPLICATE_INDEPENDENTS. The line is 
	 * {@link java.lang.String#split(String) split} at every 
	 * Location-delimiter ({@value IO#LOCATION_DELIM}), 
	 * and the phrase at the start of the line is mapped 
	 * in the underlying HashMap to a List of all the Locations 
	 * represented in the rest of the line.</p>
	 * @param scan a Scanner used to obtain lines from which 
	 * phrase-instance data is read
	 */
	public PhraseBox(Scanner scan){
		try{
			hashmap = new HashMap<>();
			
			while(scan.hasNextLine() && scan.hasNext()){
				String[] phraseAndLocations = scan.nextLine().split(IO.LOCATION_DELIM);
				
				String phrase = phraseAndLocations[0];
				
				List<Location> locs = new ArrayList<>(phraseAndLocations.length-1);
				for(int i=1; i<phraseAndLocations.length; i++){
					String[] fileAndIndex = phraseAndLocations[i].split(Location.ELEMENT_DELIM);
					locs.add( new Location(Integer.parseInt(fileAndIndex[1]), fileAndIndex[0]) );
				}
				
				hashmap.put(phrase, locs);
			}
			
			scan.close();
		} catch(ArrayIndexOutOfBoundsException e){
			throw new IllegalArgumentException("The content scanned by the specified Scanner is not structured like a record of phrases and locations.");
		}
	}
	
	/**
	 * <p>Returns a {@link java.util.Set Set} of the  phrases 
	 * that have mapped Locations in this PhraseBox.</p>
	 * @return a {@link java.util.Set Set} of the  phrases 
	 * that have mapped Locations in this PhraseBox.
	 */
	public Set<String> phrases(){
		return hashmap.keySet();
	}
	
	/**
	 * <p>Adds <code>location</code> to the {@literal List<Location>} 
	 * mapped to <code>phrase</code> in the underlying hashmap.</p>
	 * @param phrase the phrase being given another Location 
	 * @param location a location at which <code>phrase</code> occurs
	 */
	public void add(String phrase, Location location){
		if(hashmap.containsKey(phrase)){
			hashmap.get(phrase).add(location);
		} else{
			List<Location> l = new ArrayList<>();
			l.add(location);
			hashmap.put(phrase, l);
		}
	}
	
	/**
	 * <p>Returns a list of the locations at which <code>phrase</code> 
	 * occurs.</p>
	 * @param phrase any object, to maintain compatibility with 
	 * {@link java.util.HashMap#get(Object) HashMap.get(Object)}, 
	 * but should be a String phrase to get a real result
	 * @return  a list of the locations at which <code>phrase</code> 
	 * occurs if <code>phrase</code> is a String and has been mapped 
	 * in the underlying HashMap
	 */
	public List<Location> get(Object phrase){
		return hashmap.get(phrase);
	}
	
	/**
	 * <p>Returns the number of phrases that have associate Locations 
	 * in this PhraseBox.</p>
	 * @return the number of phrases that have associate Locations 
	 * in this PhraseBox
	 */
	public int size(){
		return hashmap.size();
	}
	
	/**
	 * <p>Returns true if this PhraseBox has no 
	 * phrase-instance data, false otherwise.</p>
	 * @return true if this PhraseBox has no 
	 * phrase-instance data, false otherwise
	 */
	public boolean isEmpty(){
		return hashmap.isEmpty();
	}
	
	/**
	 * <p>Returns true if this PhraseBox has phrase-instance data 
	 * for <code>phrase</code>, false otherwise.</p>
	 * @param phrase the phrase whose inclusion in this PhraseBox 
	 * is determined
	 * @return  true if this PhraseBox has phrase-instance data 
	 * for <code>phrase</code>, false otherwise
	 */
	public boolean contains(String phrase){
		return hashmap.containsKey(phrase);
	}
	
	/**
	 * <p>Removes from this PhraseBox all the phrase-instance data 
	 * for phrases that have only one associated Location.</p>
	 */
	public void removeUniques(Consumer<String> msg){
		final int initSize = size();
		
		List<String> keys = new ArrayList<>(hashmap.keySet());
		for(String phrase : keys ){
			if(hashmap.get(phrase).size() <= FindRepeatedPhrases.UNIQUE_PHRASE_LOCATION_COUNT){
				hashmap.remove(phrase);
			}
		}
		
		msg.accept("Removed "+ (initSize - size()) +" non-repeated terms");
	}
	
	/**
	 * <p>Writes the phrase-instance data from this PhraseBox 
	 * to a file via the OutputStreamWriter 
	 * <code>phraseInstanceFile</code>.</p>
	 * @param phraseInstanceFile an OutputStreamWriter by way of which 
	 * the phrase-instance data in this PhraseBox is written to a file
	 */
	public void printPhrasesWithLocations(String phraseInstFileName){
		try(OutputStreamWriter phraseInstanceFile = IO.newOutputStreamWriter(phraseInstFileName);){
			for(String phrase : hashmap.keySet()){
				phraseInstanceFile.write( phrase );
				List<Location> list = hashmap.get(phrase);
				list.sort(null);
				for( int i=0; i<list.size(); i++ ){
					phraseInstanceFile.write( IO.LOCATION_DELIM + list.get(i).shortString() );
				}
				phraseInstanceFile.write(IO.NEW_LINE);
			}
			
			phraseInstanceFile.close();
		} catch(IOException e){
			throw new RuntimeException(IO.ERROR_EXIT_MSG + phraseInstFileName+" for writing" );
		}
	}
}
