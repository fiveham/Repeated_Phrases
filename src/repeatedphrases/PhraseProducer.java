package repeatedphrases;

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
	 * in some parts of the project.</p>
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
	
	/**
	 * <p>All the words from the file whose name is passed to 
	 * the constructor are stored in this String, with one 
	 * instance of <code>WORD_SEPARATOR</code> between each 
	 * pair of adjacent words.</p>
	 */
	private final String content;
	
	/**
	 * <p>The filename of the chapter whose 
	 * {@link Chapter#getBody() body} this 
	 * PhraseProducer traverses.</p>
	 */
	private final String filename;
	
	/**
	 * <p>Index in <code>content</code> of the first character of the next 
	 * phrase that this PhraseProducer will produce, or, if there are not 
	 * enough words left ahead of this point in <code>content</code> for 
	 * this PhraseProducer to produce any more phrases with <code>size</code> 
	 * words, this is the index of the first letter of the second word of 
	 * the last phrase this object outputs.</p>
	 */
	private int phraseStart;
	
	/**
	 * <p>Index in <code>content</code> of the first character after the last 
	 * word of the next phrase this object will output, or content.length() 
	 * if there are no more phrases for this object to output.</p>
	 */
	private int phraseEnd;
	
	/**
	 * <p>Constructs a PhraseProducer that extracts phrases with <code>size</code> 
	 * words from the {@link Chapter#getBody() body} of <code>chapter</code> as 
	 * {@link java.lang.String#substring(int,int) substrings}.</p>
	 * @param size the number of words in the phrases produced.
	 * @param chapter the Chapter whose {@link Chapter#getBody() body} 
	 * backs all the phrases this PhraseProducer produces
	 */
	public PhraseProducer(int size, Chapter chapter) {
		if(size < MIN_SIZE){
			throw new IllegalArgumentException("size ("+size+") < MIN_SIZE ("+MIN_SIZE+")");
		}
		this.size = size;
		outputCount = 0;
		
		this.filename = chapter.getName();
		this.content = chapter.getBody();
		
		try{
			phraseStart = initPhraseStart();
		} catch(IllegalStateException e){
			throw new IllegalArgumentException("File "+filename+" doesn't contain a word");
		}
		
		try{
			phraseEnd = initPhraseEnd();
		} catch(IllegalStateException e){
			throw new IllegalArgumentException("File "+filename+" contains "+Integer.parseInt(e.getMessage())+" words, but needs "+size+".");
		}
	}
	
	/**
	 * <p>Initializes the value of {@link #phraseEnd phraseEnd}.</p>
	 * @return the correct initial value of {@link #phraseEnd phraseEnd}
	 */
	private int initPhraseEnd(){
		int wordEndCount = 0;
		for(int i=phraseStart+1; i<content.length(); i++){
			if( !hasPhraseCharAt(i) && hasPhraseCharAt(i-1) && ++wordEndCount==size){
				return i;
			}
		}
		throw new IllegalStateException(""+wordEndCount);
	}
	
	/**
	 * <p>Initializes {@link #phraseStart phraseStart}.</p>
	 * @return the correct initial value of 
	 * {@link #phraseStart phraseStart}
	 */
	private int initPhraseStart(){
		for(int i=0; i<content.length(); i++){
			if( hasPhraseCharAt(i) ){
				return i;
			}
		}
		throw new IllegalStateException();
	}
	
	/**
	 * <p>Returns true if the character at index <code>i</code> 
	 * in <code>content<code> is a legal word-character, false 
	 * otherwise. Legal word-characters are: alphanumerics, 
	 * apostrophe, hyphen, e-acute, or e-circumflex.</p>
	 * @param i an index in <code>content</code>
	 * @return true if the character at index <code>i</code> 
	 * in <code>content<code> is a legal word-character, false 
	 * otherwise
	 */
	private boolean hasPhraseCharAt(int i){
		return (0<=i && i<content.length()) && isPhraseChar(content.charAt(i));
	}
	
	/**
	 * <p>Returns true if <code>c</code> is a legal word-character: 
	 * alphanumeric, apostrophe, hyphen, e-acute, or e-circumflex.</p>
	 * @param c a character to be tested for legality as a word-character
	 * @return true if <code>c</code> is a legal word-character: 
	 * alphanumeric, apostrophe, hyphen, e-acute, or e-circumflex
	 */
	public static boolean isPhraseChar(Character c){
		return c!=null 
				&& (('a'<=c && c<='z') 
				|| ('A'<=c && c<='Z') 
				|| c=='\'' 
				|| c=='-' 
				|| ('0'<=c && c<='9') 
				|| c=='é' 
				|| c=='ê');
	}

	@Override
	/**
	 * <p>Returns true if there is at least one more phrase to extract
	 * from <code>content</code>, false otherwise.</p>
	 * @return true if there is at least one more phrase to extract
	 * from <code>content</code>, false otherwise
	 */
	public boolean hasNext(){
		return phraseEnd <= content.length();
	}

	@Override
	/**
	 * <p>Returns the next phrase with <code>size</code> words from 
	 * </code>content</code>.</p>
	 * @return the next phrase with <code>size</code> words from 
	 * </code>content</code>
	 */
	public String next(){
		outputCount++;
		String out = content.substring(phraseStart, phraseEnd);
		phraseStart = nextPhraseStart();
		phraseEnd = nextPhraseEnd();
		return out;
	}
	
	/**
	 * <p>In <code>content</code>, finds the next character after 
	 * <code>phraseEnd</code> that's the first character after a 
	 * word.</p>
	 * @return the index in <code>content</code> of the next 
	 * word-end after <code>phraseEnd</code>
	 */
	private int nextPhraseEnd(){
		for(int i=phraseEnd+1; i<=content.length(); i++){
			if( hasPhraseCharAt(i-1) && !hasPhraseCharAt(i) ){
				return i;
			}
		}
		return content.length()+1;
	}
	
	/**
	 * <p>Determines the index at which the next phrase starts after a phrase 
	 * is returned by {@link #next() next()}.</p>
	 * @return the next index in <code>content</code> at which the next word starts, 
	 * or </code>content.length()</code> if there is no next word
	 */
	private int nextPhraseStart(){
		for(int i=phraseStart+1; i<content.length(); i++){
			if( !hasPhraseCharAt(i-1) && hasPhraseCharAt(i) ){
				return i;
			}
		}
		return content.length();
	}
	
	/**
	 * <p>Returns <code>(value + 1) % modValue</code>.</p>
	 * <p>This implementation increments <code>value</code> then 
	 * subtracts <code>modValue</code> from it if it exceeds 
	 * <code>modValue</code> after incrementation. This is a valid 
	 * implementation assuming that <code>0 &lte; value &lt; modValue</code>, 
	 * which is always the case in this project.</p>
	 * @param value the value whose modular successor 
	 * is to be returned
	 * @param modValue the value to be used for modulo
	 * @return the modular successor of <code>value</code>, 
	 * using <code>modValue</code> for the modulo operation.
	 */
	public int modSuccessor(int value, int modValue){
		return (++value >= modValue)
				? value-modValue
				: value;
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
	}/**/

	/*private Function<Integer,Boolean> charAtCache = new Function<Integer,Boolean>(){
	private Integer oldInput  = null;
	private Boolean oldOutput = null;
	private Integer newInput  = null;
	private Boolean newOutput = null;
	@Override
	public Boolean apply(Integer input){
		if(input.equals(oldInput)){
			return oldOutput;
		} else if(input.equals(newInput)){
			return newOutput;
		} else{
			oldInput = newInput;
			oldOutput = newOutput;
			newInput = input;
			char c = content.charAt(input);
			newOutput = ('a'<=c && c<='z') || ('A'<=c && c<='Z') || c=='-' || c=='\'';
			return newOutput;
		}
	}
	};/**/
	
	/*private int nextPhraseExtremum(boolean isStart){
		for(int i=1+(isStart?phraseStart:phraseEnd); i<content.length(); i++){
			//if( (isStart^hasPhraseCharAt(i-1)) && !(isStart^hasPhraseCharAt(i)) ){
			//if( (isStart && !hasPhraseCharAt(i-1) && hasPhraseCharAt(i)) || (!isStart && hasPhraseCharAt(i-1) && !hasPhraseCharAt(i)) ){
			if( isStart
					? (!hasPhraseCharAt(i-1) &&  hasPhraseCharAt(i))
					: ( hasPhraseCharAt(i-1) && !hasPhraseCharAt(i)) ){
				return i;
			}
		}
		return content.length();
	}/**/
	
	/* *
	 * Closes the Scanner that this object wraps.
	 * /
	public void close(){
		scanner.close();
	}/**/
	
	/* *
	 * Wraps a StringBuilder, automatically including a space between strings 
	 * added, and automatically removing the previous word and its associated 
	 * space when a new word is added.
	 * /
	private class StringPipe{
		
		/**
		 * The wrapped StringBuilder
		 * /
		private StringBuilder stringStorage;
		
		/**
		 * Constructs a new StringPipe that produces phrases 
		 * with `size` words in them.
		 * Leaves the last token in the Scanner until called 
		 * by next() so that hasNext() can simply call the 
		 * wrapped Scanner's hasNext().
		 * @param size	Number of words in the phrases produced.
		 * @throws NoSuchElementException if the wrapped 
		 * Scanner doesn't have (size-1) elements available.
		 * /
		private StringPipe(int size){
			stringStorage = new StringBuilder();
			
			//populate the buffer, without including the final token, 
			//leaving it to be set by the first call to next()
			//An extra WORD_SEPARATOR remains at the beginning of stringStorage; 
			//it will be removed the first time next() is called.
			while(--size > 0){
				store(scanner.next());
			}
		}
		
		/**
		 * Appends the specified word to the wrapped StringBuilder, 
		 * preceded by WORD_SEPARATOR.
		 * @param word A word to be added to the end of 
		 * the wrapped StringBuilder, preceded by a 
		 * WORD_SEPARATOR.
		 * /
		private void store(String word){
			stringStorage.append(WORD_SEPARATOR).append(word);
		}
		
		/**
		 * Adds the specified word to the end of the wrapped string buffer, 
		 * removes the first word in the buffer and the first 
		 * PhraseProducer.WORD_SEPARATOR after that word.
		 * Returns this object.
		 * @param word	The token to be appended to the end of the 
		 * StringBuilder, preceded by a space.
		 * @return this object
		 * /
		private StringPipe push(String word){
			store(word);
			drop();
			return this;
		}
		
		/**
		 * Removes the first word from the beginning of the buffer and 
		 * removes the first PhraseProducer.WORD_SEPARATOR that immediately 
		 * follows the removed word.
		 * The first word is identified by locating the first instance of 
		 * PhraseProducer.WORD_SEPARATOR in the buffer.
		 * /
		private void drop(){
			//remove the first "word" whether it's of length 0 or greater, 
			//and the first WORD_SEPARATOR, which follows it.
			stringStorage.delete(0, stringStorage.indexOf(WORD_SEPARATOR)+WORD_SEPARATOR.length());
		}
		
		/**
		 * Returns the phrase currently stored in this StringPipe
		 * /
		@Override
		public String toString(){
			return stringStorage.toString();
		}
	}/**/
	
	/* *
	 * The Scanner this object wraps
	 */
	//private final Scanner scanner;
	/* *
	 * A wrapped StringBuilder responsible for producing each phrase.
	 */
	//private final StringPipe buffer;
}
