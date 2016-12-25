package text;

/**
 * <p>Represents a phrase found in the text of the novels and novellas of A Song of Ice and 
 * Fire.</p>
 * @author fiveham
 *
 */
public class Phrase implements Comparable<Phrase>{
    
    /**
     * <p>A space ({@value}) used to separate words in multi-word phrases. Is used as a regex sent
     * to {@link java.lang.String#split(String) String.split()} in some parts of the
     * application.</p>
     */
    public static final String WORD_SEPARATOR = " ";
    
    public static final char WORD_SEPARATOR_CHAR = ' ';
    
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
    
    public Phrase(String text){
        this.text = text;
        String[] split = text.split(WORD_SEPARATOR);
        this.wordCount = split.length;
        this.lastSpace = text.length() - split[split.length - 1].length() - 1;
    }
    
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
    
    @Override
    public int compareTo(Phrase other){
        return text.compareTo(other.text);
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
    
    public static final char E_ACUTE = '\u00E9';
    public static final char E_CIRCUMFLEX = '\u00EA';
    
    /**
     * <p>Returns true if {@code c} is a legal word-character: alphanumeric, apostrophe, hyphen,
     * e-acute, or e-circumflex.</p>
     * @param c a character to be tested for legality as a word-character
     * @return true if {@code c} is a legal word-character: alphanumeric, apostrophe, hyphen,
     * e-acute, or e-circumflex
     */
    public static boolean isPhraseChar(Character c){
        return c != null 
                && (('a' <= c && c <= 'z') 
                        || ('A' <= c && c <= 'Z') 
                        || c == '\'' 
                        || c == '-' 
                        || ('0' <= c && c <= '9') 
                        || c == E_ACUTE 
                        || c == E_CIRCUMFLEX);
    }
    
    @Override
    public int hashCode(){
        return text.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
        if(o == this){
            return true;
        }
        if(o instanceof Phrase){
            Phrase p = (Phrase) o;
            return p.text == null 
                    ? text == null 
                    : p.text.equals(text);
        }
        return false;
    }
}
