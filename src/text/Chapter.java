package text;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import common.IO;
import html.HTMLFile;

/**
 * <p>Represents a chapter of a book in the body of text being analysed. Pairs the name of the file
 * from which content is read with the contents of the file as supplied to the constructor.</p>
 */
public class Chapter {
	
    /**
     * <p>The name of the file for this chapter.</p>
     */
	private final String filename;
	
    /**
     * <p>The content of the file for this chapter.</p>
     */
	private final String body;
	
    /**
     * <p>Constructs a Chapter based on the specified file.</p>
     * @param file the file for this Chapter
     */
	public Chapter(File file){
	    this.filename = file.getName();
	    this.body = IO.fileAsString(file);
	}
	
	//FIXME need to use Corpus's weird Scanner delimiter instead of blindly reading text
	public Chapter(HTMLFile h){
	    this.filename = h.getName();
	    this.body = h.body();
	}
	
	//XXX rename getFilename, since each chapter has an actual name other than the filename
    /**
     * <p>Returns {@code filename}.</p>
     * @return {@code filename}
     */
	public String getName(){
		return filename;
	}
	
    /**
     * <p>Returns {@code body}.</p>
     * @return {@code body}
     */
	public String getBody(){
		return body;
	}
	
	@Override
	public boolean equals(Object o){
	    if(o instanceof Chapter){
	        Chapter c = (Chapter) o;
	        return c.body.equals(this.body) && c.filename.equals(this.filename);
	    }
	    return false;
	}
	
	@Override
	public int hashCode(){
	    return filename.hashCode() * 31 + body.hashCode();
	}
	
	public static boolean isChapter(File dir, String name){
	    return IO.isTxt(dir, name); //TODO add other necessary stipulations
	}
	
	public Collection<Quote> getAllQuotes(int min, int max){
	    List<Integer> wordIndices = getWordIndices(min);
	    
	    List<Quote> result = new ArrayList<>();
	    for(int size = min; size <= max; size++){
	        
	        for(int indexInto = 0; indexInto - size < wordIndices.size(); indexInto++){
	            int endIndex = wordEndIndex(wordIndices, indexInto, size);
	            String phrase = body.substring(wordIndices.get(indexInto), endIndex);
	            result.add(new Quote(getLocations().get(indexInto), phrase));
	        }
	    }
	    
	    return result;
	}
	
	private static int wordEndIndex(List<Integer> wordIndices, int phraseStart, int phraseSize){
	    return phraseStart + phraseSize == wordIndices.size() 
	        ? wordIndices.size() 
	        : wordIndices.get(phraseStart + phraseSize) - IO.SPACE.length();
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
	    return hasChar(i) && isWordChar(body.charAt(i));
	}
	
	private boolean hasChar(int i){
	    return 0 <= i && i < body.length();
	}
	
	//XXX move to IO as a standard
	public static boolean isWordChar(char c){
	    return ('a' <= c && c <= 'z') 
                || ('A' <= c && c <= 'Z') 
                || c == '\'' 
                || c == '-' 
                || ('0' <= c && c <= '9') 
                || c == E_ACUTE 
                || c == E_CIRCUMFLEX;
	}
    
    private static final char E_ACUTE = '\u00E9';
    private static final char E_CIRCUMFLEX = '\u00EA';
	
	List<Location> getLocations(){
	    if(locations == null){
	        locations = IntStream.range(0, getWordIndices(0).size())
	                .mapToObj((i) -> new Location(i, this))
	                .collect(Collectors.toList());
	    }
	    return locations;
	}
	
	private List<Location> locations;
	
	public boolean hasLargerPhraseAt(Location location, String text){
	    int largerPhraseSize = wordCount(text) + 1;
	    int wordIndex = location.getIndex();
	    
	    if(repeatedQuotes == null){
	        throw new IllegalStateException("Repeated quotes not specified.");
	    }
	    
	    List<Integer> key = generateKey(largerPhraseSize, wordIndex);
	    return repeatedQuotes.containsKey(key);
	}
	
	private Map<List<Integer>, Quote> repeatedQuotes = null;
	
	public void setRepeatedQuotes(Collection<Quote> repeatedQuotes){
	    this.repeatedQuotes = repeatedQuotes.stream()
        	    .collect(Collectors.toMap(
        	            (q) -> generateKey(
        	                    wordCount(q.text()), 
        	                    q.location().getIndex()), 
        	            (q) -> q)); 
	}
	
	private static List<Integer> generateKey(int size, int index){
	    return Arrays.asList(size, index);
	}
	
	//TODO use Phrase with internally stored word-count instead of String
	private static int wordCount(String text){
	    return text.split(IO.SPACE).length;
	}
}
