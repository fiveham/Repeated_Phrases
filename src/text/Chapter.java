package text;

import html.HtmlChapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <p>Represents a chapter of a book in the body of text being analysed. Pairs the name of the file
 * from which content is read with the contents of the file as supplied to the constructor.</p>
 */
public class Chapter {
	
	private final HtmlChapter source;
	
    /**
     * <p>The content of the file for this chapter.</p>
     */
	private final String body;
	
	public Chapter(HtmlChapter h){
	    this.source = h;
	    this.body = h.body();
	}
	
    /**
     * <p>Returns {@code filename}.</p>
     * @return {@code filename}
     */
	public String getName(){
		return source.getName();
	}
	
    /**
     * <p>Returns {@code body}.</p>
     * @return {@code body}
     */
	public String getBody(){
		return body;
	}
	
	public HtmlChapter getSource(){
	    return source;
	}
	
	@Override
	public boolean equals(Object o){
	    if(o == this){
	        return true;
	    }
	    if(o instanceof Chapter){
	        Chapter c = (Chapter) o;
	        return c.body.equals(this.body) && c.getName().equals(getName());
	    }
	    return false;
	}
	
	@Override
	public int hashCode(){
	    return getName().hashCode() * 31 + body.hashCode();
	}
	
	public Collection<Quote> getAllQuotes(int min, int max, Map<String, Phrase> textToPhrase){
	    List<Integer> wordIndices = getWordIndices(min);
	    
	    List<Quote> result = new ArrayList<>();
	    for(int size = min; size <= max; size++){
	        
	        for(int indexInto = 0; indexInto - size < wordIndices.size(); indexInto++){
	            int endIndex = wordEndIndex(wordIndices, indexInto, size);
	            String phrase = body.substring(wordIndices.get(indexInto), endIndex);
	            result.add(new Quote(
	                    getLocations().get(indexInto), 
	                    textToPhrase.computeIfAbsent(phrase, Phrase::new)));
	        }
	    }
	    
	    return result;
	}
	
	private static int wordEndIndex(List<Integer> wordIndices, int phraseStart, int phraseSize){
	    return phraseStart + phraseSize == wordIndices.size() 
	        ? wordIndices.size() 
	        : wordIndices.get(phraseStart + phraseSize) - Phrase.WORD_SEPARATOR.length();
	}
	
	private List<Integer> getWordIndices(int min){
	    if(wordIndices == null){
	        wordIndices = IntStream.range(0, body.length())
	                .filter(this::isWordStart)
	                .mapToObj(Integer::valueOf)
	                .collect(Collectors.toList());
	    }
	    
	    //XXX bugfix: if min is large enough, an exception is thrown
	    return wordIndices.subList(0, wordIndices.size() - (min - 1));
	}
	
	private List<Integer> wordIndices = null;
	
	private boolean isWordStart(int i){
	    return hasWordChar(i) && !hasWordChar(i);
	}
	
	private boolean hasWordChar(int i){
	    return hasChar(i) && Phrase.isPhraseChar(body.charAt(i));
	}
	
	private boolean hasChar(int i){
	    return 0 <= i && i < body.length();
	}
    
	List<Location> getLocations(){
	    if(locations == null){
	        locations = IntStream.range(0, getWordIndices(0).size())
	                .mapToObj((i) -> new Location(i, this))
	                .collect(Collectors.toList());
	    }
	    return locations;
	}
	
	private List<Location> locations;
	
	public boolean hasLargerPhraseAt(Location location, Phrase phrase){
	    int largerPhraseSize = phrase.getWordCount() + 1;
	    int wordIndex = location.getIndex();
	    
	    if(repeatedQuotes == null){
	        throw new IllegalStateException("Repeated quotes not specified.");
	    }
	    
	    List<Integer> key = listSizeIndex(largerPhraseSize, wordIndex);
	    return repeatedQuotes.containsKey(key);
	}
	
	private Map<List<Integer>, Quote> repeatedQuotes = null;
	
	public void setRepeatedQuotes(Collection<Quote> repeatedQuotes){
	    this.repeatedQuotes = repeatedQuotes.stream()
        	    .collect(Collectors.toMap(
        	            (q) -> listSizeIndex(
        	                    q.phrase().getWordCount(),
        	                    q.location().getIndex()), 
        	            Function.identity())); 
	}
	
	private static List<Integer> listSizeIndex(int size, int index){
	    return Arrays.asList(size, index);
	}
}
