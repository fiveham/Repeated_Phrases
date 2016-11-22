package text;

import java.util.Iterator;

/**
 * <p>Crawls along the {@link Chapter#getBody() body} 
 * of a Chapter and 
 * produces phrases with a certain {@link #size number} 
 * of words as substrings of the Chapter's body.</p>
 */
public class PhraseProducer implements Iterator<String>{
	
	/**
	 * <p>A space ({@value}) used to separate words in 
	 * multi-word phrases. 
	 * Is used as a regex sent to 
	 * {@link java.lang.String#split(String) String.split()} 
	 * in some parts of the application.</p>
	 */
	public static final String WORD_SEPARATOR = " ";
	
	/**
	 * <p>The least number of words allowable in phrases 
	 * produced by a PhraseProducer.</p>
	 */
	public static final int MIN_SIZE = 1;
	
	/**
	 * <p>The number of words in the phrases 
	 * this PhraseProducer produces.</p>
	 */
	private final int size;
	
	/**
	 * <p>The number of times this object has returned a 
	 * phrase from {@link #next() next()}, which is equal 
	 * to the word-index in its source chapter of that 
	 * instance of that phrase.</p>
	 */
	private int outputCount;
	
	private final Chapter chapter;
	
	/**
	 * <p>Index in {@code content} of the first character of the next 
	 * phrase that this PhraseProducer will produce, or, if there are not 
	 * enough words left ahead of this point in {@code content} for 
	 * this PhraseProducer to produce any more phrases with {@code size} 
	 * words, this is the index of the first letter of the second word of 
	 * the last phrase this object outputs.</p>
	 */
	private int phraseStart;
	
	/**
	 * <p>Index in {@code content} of the first character after the last 
	 * word of the next phrase this object will output, or content.length() 
	 * if there are no more phrases for this object to output.</p>
	 */
	private int phraseEnd;
	
	/**
	 * <p>Constructs a PhraseProducer that extracts phrases with {@code size} 
	 * words from the {@link Chapter#getBody() body} of {@code chapter} as 
	 * {@link java.lang.String#substring(int,int) substrings}.</p>
	 * @param size the number of words in the phrases produced.
	 * @param chapter the Chapter whose {@link Chapter#getBody() body} 
	 * backs all the phrases this PhraseProducer produces
	 * @throws IllegalArgumentException if {@code chapter}'s body 
	 * has no words or fewer than {@code size} words
	 */
	public PhraseProducer(int size, Chapter chapter) {
		if(size < MIN_SIZE){
			throw new IllegalArgumentException("size ("+size+") < MIN_SIZE ("+MIN_SIZE+")");
		}
		this.size = size;
		outputCount = 0;
		
		this.chapter = chapter;
		
		phraseStart = initPhraseStart();
		phraseEnd = initPhraseEnd();
	}
	
	/**
	 * <p>Initializes the value of {@link #phraseEnd phraseEnd}.</p>
	 * @return the correct initial value of {@link #phraseEnd phraseEnd}
	 * @throws IllegalStateException if {@link #filename filename} 
	 * contains fewer than {@link #size size} words
	 */
	private int initPhraseEnd(){
		int wordEndCount = 0;
		for(int i=phraseStart+1; i<chapter.getBody().length(); i++){
			wordEndCount++;
			if( !hasPhraseCharAt(i) 
					&& hasPhraseCharAt(i-1) 
					&& wordEndCount==size){
				return i;
			}
		}
		throw new IllegalStateException("File "+chapter.getName()+" contains "+wordEndCount+" words, but needs "+size+".");
	}
	
	/**
	 * <p>Initializes {@link #phraseStart phraseStart}.</p>
	 * @return the correct initial value of 
	 * {@link #phraseStart phraseStart}
	 * @throws IllegalStateException if {@link #content} 
	 * doesn't contain any words
	 */
	private int initPhraseStart(){
		for(int i=0; i<chapter.getBody().length(); i++){
			if(hasPhraseCharAt(i)){
				return i;
			}
		}
		throw new IllegalStateException("File "+chapter.getName()+" doesn't contain a word");
	}
	
	/**
	 * <p>Returns true if the character at index {@code i} 
	 * in {@code content{@code  is a legal word-character, false 
	 * otherwise. Legal word-characters are: alphanumerics, 
	 * apostrophe, hyphen, e-acute, or e-circumflex.</p>
	 * @param i an index in {@code content}
	 * @return true if the character at index {@code i} 
	 * in {@code content{@code  is a legal word-character, false 
	 * otherwise
	 */
	private boolean hasPhraseCharAt(int i){
		return 0<=i 
				&& i<chapter.getBody().length() 
				&& isPhraseChar(chapter.getBody().charAt(i));
	}
	
	/**
	 * <p>Returns true if {@code c} is a legal word-character: 
	 * alphanumeric, apostrophe, hyphen, e-acute, or e-circumflex.</p>
	 * @param c a character to be tested for legality as a word-character
	 * @return true if {@code c} is a legal word-character: 
	 * alphanumeric, apostrophe, hyphen, e-acute, or e-circumflex
	 */
	public static boolean isPhraseChar(Character c){
		return c != null 
				&& (('a'<=c && c<='z') 
						|| ('A'<=c && c<='Z') 
						|| c=='\'' 
						|| c=='-' 
						|| ('0'<=c && c<='9') 
						|| c==E_ACUTE 
						|| c==E_CIRCUMFLEX);
	}
	
	public static final char E_ACUTE = '\u00E9';
	public static final char E_CIRCUMFLEX = '\u00EA';
	
	@Override
	/**
	 * <p>Returns true if there is at least one more phrase to extract
	 * from {@code content}, false otherwise.</p>
	 * @return true if there is at least one more phrase to extract
	 * from {@code content}, false otherwise
	 */
	public boolean hasNext(){
		return phraseEnd <= chapter.getBody().length();
	}
	
	@Override
	/**
	 * <p>Returns the next phrase with {@code size} words from 
	 * }content}.</p>
	 * @return the next phrase with {@code size} words from 
	 * }content}
	 */
	public String next(){
		outputCount++;
		String out = chapter.getBody().substring(phraseStart, phraseEnd);
		phraseStart = nextPhraseStart();
		phraseEnd = nextPhraseEnd();
		return out;
	}
	
	/**
	 * <p>In {@code content}, finds the next character after 
	 * {@code phraseEnd} that's the first character after a 
	 * word.</p>
	 * @return the index in {@code content} of the next 
	 * word-end after {@code phraseEnd}
	 */
	private int nextPhraseEnd(){
		for(int i=phraseEnd+1; i<=chapter.getBody().length(); i++){
			if(hasPhraseCharAt(i-1) && !hasPhraseCharAt(i)){
				return i;
			}
		}
		return chapter.getBody().length()+1;
	}
	
	/**
	 * <p>Determines the index at which the next phrase starts after a phrase 
	 * is returned by {@link #next() next()}.</p>
	 * @return the next index in {@code content} at which the next word starts, 
	 * or }content.length()} if there is no next word
	 */
	private int nextPhraseStart(){
		for(int i=phraseStart+1; i<chapter.getBody().length(); i++){
			if(!hasPhraseCharAt(i-1) && hasPhraseCharAt(i)){
				return i;
			}
		}
		return chapter.getBody().length();
	}
	
	/**
	 * <p>Returns the number of words in phrases this PhraseProducer produces.</p>
	 * @return the number of space-delimited tokens in any result 
	 * returned from {@link #next() next()}
	 */
	public int size(){
		return size;
	}
	
	/**
	 * <p>Returns the number of times this PhraseProducer has produced a 
	 * phrase via {@link #next() next()}.</p>
	 * @return the number of times this PhraseProducer has produced a 
	 * phrase via {@link #next() next()}
	 */
	public int outputCount(){
		return outputCount;
	}
}
