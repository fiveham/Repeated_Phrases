package repeatedphrases;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * <p>Wraps a HashMap linking String filenames with data 
 * structures that store pairs of String and int. 
 * This is used to store phrase-instance data in a way 
 * that enables all the phrase-instances for a given 
 * chapter to be accessed easily.</p>
 */
public class FileBox{
	
	/**
	 * <p>The wrapped HashMap.</p>
	 */
	private final HashMap<String, List<IntString>> hashmap;
	
	/**
	 * <p>Constructs a FileBox with no contents.</p>
	 */
	public FileBox() {
		hashmap = new HashMap<>();
	}
	
	/**
	 * <p>Constructs a FileBox with all the phrase-instance 
	 * data contained in the specified File <code>f</code>.</p>
	 * @param f a File from which to read phrase-instance data
	 * @throws FileNotFoundException when <code>f</code> does 
	 * not exist or cannot be read
	 */
	public FileBox(File f) throws FileNotFoundException{
		this(new Scanner(f));
	}
	
	/**
	 * <p>Constructs a FileBox containing the phrase-instance 
	 * data represented in the body that <code>scan</code> 
	 * reads.</p>
	 * @param scan a Scanner that produces phrase-instance 
	 * data as though reading files that go in {@link #Folder.REPEATS REPEATS}, 
	 * {@link #Folder.INDEPENDENT_INSTANCES INDEPENDENT_INSTANCES}, or 
	 * {@link #Folder.DUPLICATE_INDEPENDENTS DUPLICATE_INDEPENDENTS}.
	 */
	public FileBox(Scanner scan){
		try{
			hashmap = new HashMap<>();
			
			while(scan.hasNextLine() && scan.hasNext()){
				String[] phraseAndLocations = scan.nextLine().split(IO.LOCATION_DELIM);
				
				String phrase = phraseAndLocations[0];
				
				for(int i=1; i<phraseAndLocations.length; i++){
					String[] fileAndIndex = phraseAndLocations[i].split(Location.ELEMENT_DELIM);
					String filename = fileAndIndex[0];
					
					IntString is = new IntString(Integer.parseInt(fileAndIndex[1]), phrase);
					
					if( hashmap.containsKey(filename) ){
						hashmap.get(filename).add( is );
					} else{
						List<IntString> l = new ArrayList<>();
						l.add( is );
						hashmap.put(filename, l);
					}
				}
			}
			
			scan.close();
		} catch(ArrayIndexOutOfBoundsException e){
			throw new IllegalArgumentException("The content scanned by the specified Scanner is not structured like a record of phrases and locations.");
		}
	}
	
	/**
	 * <p>Returns a {@link java.util.Set Set} of the names of the 
	 * files of the chapters for which this FileBox has phrase-
	 * instance data.</p>
	 * @return  a {@link java.util.Set Set} of the names of the 
	 * files of the chapters for which this FileBox has phrase-
	 * instance data
	 */
	public Set<String> filenames(){
		return hashmap.keySet();
	}
	
	/**
	 * <p>Returns the number of mappings in the underlying HashMap, 
	 * equal to the size of the Set returned by {@link #filenames() filenames()}, 
	 * as the one-to-many mappings are achieved via a one-to-one 
	 * mapping from key filenames to value {@literal List<IntString>}s.</p>
	 * @return the number of mappings in the underlying HashMap
	 */
	public int size(){
		return hashmap.size();
	}
	
	/*public void add(String phrase, Location location){
		if(hashmap.containsKey(phrase)){
			hashmap.get(phrase).add(location);
		} else{
			List<Location> l = new ArrayList<>();
			l.add(location);
			hashmap.put(phrase, l);
		}
	}/**/
	
	/**
	 * <p>Returns the {@literal List<IntString>} mapped in the 
	 * underlying HashMap for the key <code>o</code>. Returns 
	 * <code>null</code> if there is no mapping for <code>o</code> 
	 * in the underlying HashMap.</p>
	 * @param o the key filename whose value {@literal List<IntString>} 
	 * is to be returned
	 * @return the {@literal List<IntString>} mapped in the 
	 * underlying HashMap for the key <code>o</code>
	 */
	public List<IntString> get(Object o){
		return hashmap.get(o);
	}/**/
	
	/**
	 * <p>Returns true if there is an entry for <code>filename</code> 
	 * in this FileBox, false otherwise.</p>
	 * @param filename the filename whose status as a key in the 
	 * underlying HashMap is returned
	 * @return true if there is an entry for <code>filename</code> 
	 * in this FileBox, false otherwise.
	 */
	public boolean contains(String filename){
		return hashmap.containsKey(filename);
	}
}
