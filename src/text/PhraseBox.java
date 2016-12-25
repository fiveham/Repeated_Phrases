package text;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;

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
	private final Map<Phrase, List<Location>> map;
	
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
	public Set<Phrase> getPhrases(){
		return map.keySet();
	}
	
    /**
     * <p>Adds {@code location} to the {@literal List<Location>} mapped to {@code phrase} in the
     * underlying hashmap.</p>
     * @param phrase the phrase being given another Location
     * @param location a location at which {@code phrase} occurs
     */
	public synchronized void add(Phrase phrase, Location location){
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
	public List<Location> get(Phrase phrase){
		return map.get(phrase);
	}
}
