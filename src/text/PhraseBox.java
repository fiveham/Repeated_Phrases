package text;

import common.IO;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Consumer;

//TODO Use Phrase objects as keys
//TODO create Phrase class which stores lastIndexOf space (" ") data to expedite reducedPhrase time 
//to calculate in FindRepeatedPhrases. Get the data from the Chapter.body indexOf data for spaces 
//while creating the Chapter's phrases in the first place
/**
 * <p>Wraps a Map linking string phrases with data structures that store multiple Locations.</p>
 * <p>{@link #printPhrasesWithLocations()} and {@link #removeUniques()} are the non-wrapper methods
 * that justify this being its own class.</p>
 */
public class PhraseBox{
	
    /**
     * <p>The wrapped HashMap.</p>
     */
	private final Map<String, List<Location>> map;
	
    /**
     * <p>Constructs a PhraseBox with no contents.</p>
     */
	public PhraseBox() {
		map = new HashMap<>();
	}
	
    /**
     * <p>Returns a {@link java.util.Set Set} of the phrases that have mapped Locations in this
     * PhraseBox.</p>
     * @return a {@link java.util.Set Set} of the phrases that have mapped Locations in this
     * PhraseBox.
     */
	public Set<String> phrases(){
		return map.keySet();
	}
	
    /**
     * <p>Adds {@code location} to the {@literal List<Location>} mapped to {@code phrase} in the
     * underlying hashmap.</p>
     * @param phrase the phrase being given another Location
     * @param location a location at which {@code phrase} occurs
     */
	public synchronized void add(String phrase, Location location){
		if(map.containsKey(phrase)){
			map.get(phrase).add(location);
		} else{
			List<Location> l = new ArrayList<>();
			l.add(location);
			map.put(phrase, l);
		}
	}
	
    /**
     * <p>Returns a list of the locations at which {@code phrase} occurs.</p>
     * @param phrase any object, to maintain compatibility with
     * {@link java.util.HashMap#get(Object) HashMap.get(Object)}, but should be a String phrase to
     * get a real result
     * @return a list of the locations at which {@code phrase} occurs if {@code phrase} is a String
     * and has been mapped in the underlying HashMap
     */
	public List<Location> get(Object phrase){
		return map.get(phrase);
	}
	
    /**
     * <p>Returns the number of phrases that have associate Locations in this PhraseBox.</p>
     * @return the number of phrases that have associate Locations in this PhraseBox
     */
	public int size(){
		return map.size();
	}
	
    /**
     * <p>Returns true if this PhraseBox has no quote data, false otherwise.</p>
     * @return true if this PhraseBox has no quote data, false otherwise
     */
	public boolean isEmpty(){
		return map.isEmpty();
	}
	
    /**
     * <p>Returns true if this PhraseBox has quote data for {@code phrase}, false otherwise.</p>
     * @param phrase the phrase whose inclusion in this PhraseBox is determined
     * @return true if this PhraseBox has quote data for {@code phrase}, false otherwise
     */
	public boolean contains(String phrase){
		return map.containsKey(phrase);
	}
	
    /**
     * <p>Defines the number of locations in the corpus at which a unique phrase occurs.</p>
     */
    public static final int UNIQUE_PHRASE_LOCATION_COUNT = 1;
	
    /**
     * <p>Removes from this PhraseBox all the quote data for phrases that have only one associated
     * Location.</p>
     */
	public PhraseBox removeUniques(Consumer<String> msg){
		final int initSize = size();
		map.entrySet().removeIf(
				(e) -> map.get(e.getKey()).size() <= UNIQUE_PHRASE_LOCATION_COUNT);
		msg.accept("Removed "+ (initSize - size()) +" non-repeated terms");
		return this;
	}
	
    /**
     * <p>Writes the quote data from this PhraseBox to a file via the OutputStreamWriter
     * {@code phraseInstanceFile}.</p>
     * @param phraseInstanceFile an OutputStreamWriter by way of which the quote data in this
     * PhraseBox is written to a file
     */
	public PhraseBox printPhrasesWithLocations(String phraseInstFileName){
		try(OutputStreamWriter phraseInstanceFile = IO.newOutputStreamWriter(phraseInstFileName)){
			for(String phrase : map.keySet()){
				phraseInstanceFile.write(phrase);
				List<Location> list = map.get(phrase);
				list.sort(null);
				for(int i=0; i<list.size(); i++){
					phraseInstanceFile.write(IO.LOCATION_DELIM + list.get(i).shortString());
				}
				phraseInstanceFile.write(IO.NEW_LINE);
			}
			
			phraseInstanceFile.close();
			return this;
		} catch(IOException e){
			throw new RuntimeException(IO.ERROR_EXIT_MSG + phraseInstFileName+" for writing");
		}
	}
}
