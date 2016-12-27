package text;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;

/**
 * <p>Wraps a Map linking string phrases with data structures that store multiple Locations.</p>
 * <p>{@link #printPhrasesWithLocations()} and {@link #removeUniques()} are the non-wrapper methods
 * that justify this being its own class.</p>
 */
public class PhraseBox extends HashMap<Phrase, List<Location>>{
	
    /**
     * <p>Constructs a PhraseBox with no contents.</p>
     */
	public PhraseBox() {
		super();
	}
	
    /**
     * <p>Adds {@code location} to the {@literal List<Location>} mapped to {@code phrase} in the
     * underlying hashmap.</p>
     * @param phrase the phrase being given another Location
     * @param location a location at which {@code phrase} occurs
     */
	public synchronized void add(Phrase phrase, Location location){
	    compute(
	            phrase, 
	            (p, list) -> {
	                List<Location> result = list == null 
	                        ? new ArrayList<>() 
	                        : get(p);
                    result.add(location);
	                return result;
	            });
	}
}
