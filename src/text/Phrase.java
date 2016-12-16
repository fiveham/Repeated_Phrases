package text;

/**
 * <p>Represents a phrase found in the text of the novels and novellas of A Song of Ice and 
 * Fire.</p>
 * @author fiveham
 *
 */
public class Phrase {
    
    /**
     * <p>An empty string. This is the {@link #reduced() reduced} form of a phrase with only one 
     * word.</p>
     */
    public static final String ZERO_WORD_PHRASE = "";
    
    /**
     * <p>The value that should be specified as {@code lastSpace} for a phrase with one word.</p>
     */
    public static final int LAST_SPACE_INDEX_FOR_ONE_WORD_PHRASE = -1;
    
    private final String text;
    private final int lastSpace;
    private final int wordCount;
    
    /**
     * <p>Constructs a Phrase for the specified piece of {@code text}
     * @param text the actual phrase from the text of a book
     * @param lastSpace the {@link String#lastIndexOf(String) last index of} a space in {@code text}
     */
    Phrase(String text, int lastSpace, int wordCount){
        this.text = text;
        this.lastSpace = lastSpace;
        this.wordCount = wordCount;
    }
    
    public int getWordCount(){
        return wordCount;
    }
    
    /**
     * <p>Returns the index of the last space ({@code " "}) in the {@link #getText() actual phrase} 
     * of this Phrase.</p>
     * @return the index of the last space ({@code " "}) in the {@link #getText() actual phrase} of 
     * this Phrase
     */
    public int getLastSpace(){
        return lastSpace;
    }
    
    /**
     * <p>Returns the actual phrase this object wraps.</p>
     * @return this phrase without the last word
     */
    public String getText(){
        return text;
    }
    
    /**
     * <p>Returns this phrase without the last word.</p>
     * @return this phrase without the last word
     */
    public String reduced(){
        return lastSpace == LAST_SPACE_INDEX_FOR_ONE_WORD_PHRASE 
                ? ZERO_WORD_PHRASE 
                : text.substring(0, lastSpace);
    }
}
