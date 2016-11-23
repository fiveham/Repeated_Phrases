package html;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Scanner;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;

import common.IO;
import text.Location;
import text.PhraseProducer;

/**
 * <p>Represents an HTML file and provides some convenience methods for working with an HTML
 * file.</p>
 */
public class HTMLFile {
	
    /**
     * <p>The index of the chapter's book's name in the array resulting from calling
     * String.split(UNDERSCORE, FILENAME_ELEMENT_COUNT) on the extensionless name of this chapter's
     * file. Chapter file names are structured as follows:
     * "BOOKNAME_CHAPTERINDEX_MULTI_WORD_CHAPTER_TITLE.html" Splitting that String at underscores
     * leaves the name of the chapter's book at index 0 in the resulting array.</p>
     */
	public static final int FILENAME_BOOKNAME_INDEX = 0;
	
    /**
     * <p>The index of the chapter's index in its source book in the array resulting from calling
     * String.split(UNDERSCORE, FILENAME_ELEMENT_COUNT) on the extensionless name of this chapter's
     * file. Chapter file names are structured as follows:
     * "BOOKNAME_CHAPTERINDEX_MULTI_WORD_CHAPTER_TITLE.html" Splitting that String at underscores
     * leaves the chapter's index in its book at index 1 in the resulting array.</p>
     */
	public static final int FILENAME_CHAPTERNUMBER_INDEX = 1;
	
    /**
     * <p>The index of the chapter's title in the array resulting from calling
     * String.split(UNDERSCORE, FILENAME_ELEMENT_COUNT) on the extensionless name of this chapter's
     * file. Chapter file names are structured as follows:
     * "BOOKNAME_CHAPTERINDEX_MULTI_WORD_CHAPTER_TITLE.html" Splitting that String at underscores
     * leaves the chapter's title at index 2 in the resulting array, but only if
     * {@link java.lang.String#split() split()} is limited in how many times it tries to split the
     * string, requiring {@link #FILENAME_ELEMENT_COUNT a limit}.</p>
     */
	public static final int FILENAME_CHAPTERNAME_INDEX = 2;
	
    /**
     * <p>The number of meaningful components in a chapter's filename. These are the source book,
     * the chapter number, and the chapter name. Chapter file names are structured as follows:
     * "BOOKNAME_CHAPTERINDEX_MULTI_WORD_CHAPTER_TITLE.html" Splitting that String at all
     * underscores will split a multi-word chapter name (for example "THE WATCHER") into multiple
     * entries in the resulting array. Sending this limit to String.split() ensures that the chapter
     * name remains in one piece.</p>
     */
	public static final int FILENAME_ELEMENT_COUNT = 3;
	
    /**
     * <p>The underlying list.</p>
     */
	private ArrayList<HTMLEntity> content;
	
    /**
     * <p>The literal filename of the file to which the content of this HTMLFile belongs. Contains
     * an extension and possibly a folder reference.</p>
     */
	private final String filename;
	
    /**
     * <p>The name of the file from which this HTMLFile was derived, without any file extension at
     * the end.</p>
     */
	private final String extensionlessName;
	
    /**
     * <p>The name of the chapter that the HTML file from which this HTMLFile was derived. The
     * underscores present in the portion of the filename that contained this information have been
     * replaced with spaces.</p>
     */
	private final String chapterName;
	
    /**
     * <p>The effective word-index of the first word of the chapter's title, given that the first
     * word of the real text of the chapter, which comes after the title, has word-index 0. None of
     * the processes of this project add words to in-memory representations of HTML chapters; so, no
     * matter what changes are made to the structure of the file in memory, this value remains
     * invariant and can thus be relied on as a consistent and simple starting point for finding
     * words based on their word-index.</p>
     */
	private final int baseWordIndex;
	
	private int modCount = 0;
	
    /**
     * <p>Constructs an HTMLFile representing the contents of the File {@code f}. Calls
     * {@link #HTMLFile(String,Scanner) this(String,Scanner)} using
     * {@link java.io.File.getName() the file's name} and a new Scanner of {@code f}.</p>
     * @param f the File whose contents go into this HTMLFile
     * @throws FileNotFoundException if {@code f} does not exist or cannot be read
     */
	public HTMLFile(File f) throws FileNotFoundException{
		this(f.getName(), 
				new Scanner(
						readFile( new Scanner(f, IO.ENCODING) )
						.toString()));
	}
	
    /**
     * <p>Constructs an HTMLFile that believes its filename is {@code name} and which gets the
     * literal text it turns into HTMLEntitys via {@code scan}.</p>
     * @param name the filename that this HTMLFile uses to determine information about itself
     * assuming that the filename is structured the way that the chapter files are split by
     * {@link operate.SplitChapters SplitChapters}
     * @param scan the Scanner used to obtain literal text to parse into HTMLEntitys
     */
	public HTMLFile(String name, Scanner scan) {
		
		content = getHTMLFileContent(scan);
		
		filename = name;
		
		int p = filename.indexOf( IO.FILENAME_ELEMENT_DELIM );
		extensionlessName = p>=0 ? filename.substring(0,p) : filename;
		
		String[] split = extensionlessName
				.split(IO.FILENAME_COMPONENT_SEPARATOR, FILENAME_ELEMENT_COUNT);
		chapterName = FILENAME_CHAPTERNAME_INDEX < split.length 
				? split[FILENAME_CHAPTERNAME_INDEX]
						.replace(IO.FILENAME_COMPONENT_SEPARATOR_CHAR, ' ') 
				: null;
		
		baseWordIndex = chapterName!=null 
				? -chapterName.split(" ").length 
				: 0;
	}
	
    /**
     * <p>Constructs an HTMLFile based on the elements of {@code content}, with the filename
     * {@code name}.</p>
     * @param name the file address/name of this HTMLFile
     * @param content a list whose elements will be the elements of this HTMLFile
     */
	public HTMLFile(String name, List<HTMLEntity> content){
		this.content = new ArrayList<>(content);
		
		filename = name;
		
		int p = filename.indexOf( IO.FILENAME_ELEMENT_DELIM );
		extensionlessName = p>=0 ? filename.substring(0,p) : filename;
		
		String[] split = extensionlessName
				.split(IO.FILENAME_COMPONENT_SEPARATOR, FILENAME_ELEMENT_COUNT);
		chapterName = FILENAME_CHAPTERNAME_INDEX < split.length 
				? split[FILENAME_CHAPTERNAME_INDEX]
						.replace(IO.FILENAME_COMPONENT_SEPARATOR_CHAR,' ') 
				: null;
		
		baseWordIndex = chapterName!=null 
				? -chapterName.split(" ").length 
				: 0;
	}
	
    /**
     * <p>Returns {@link #chapterName chapterName}.</p>
     * @return {@link #chapterName chapterName}
     */
	public String chapterName(){
		return chapterName;
	}
	
    /**
     * <p>Returns {@link #filename filename}.</p>
     * @return {@link #filename filename}
     */
	public String getName(){
		return filename;
	}
	
    /**
     * <p>Returns {@link #extensionlessName extensionlessName}.</p>
     * @return {@link #extensionlessName extensionlessName}
     */
	public String getExtensionlessName(){
		return extensionlessName;
	}
	
    /**
     * <p>Adds a pair of anchor tags to this HTMLFile around based on the AnchorInfo {@code a}. The
     * added Tags are placed around the {@link Location#index word-index}-th word in this HTMLFile,
     * specified by {@code a}'s {@link AnchorInfo#position position} after the word at that position
     * is verified as the first word of {@code a}'s {@link AnchorInfo#text phrase}. The link added
     * via these tags links to {@code a}'s {@link AnchorInfo#linkTo destination}.</p>
     * @param a an AnchorInfo specifying everything needed to create a link from one repeated phrase
     * in this HTMLFile to the same repeated phrase in another HTMLFile chapter.
     */
	public void addAnchor(AnchorInfo a){
		int wordIndex = a.rawIndex();
		
		validateWordWithIndex(firstWord(a.phrase()), wordIndex);
		
		List<Integer> insertPoints = anchorInsertionPoints(wordIndex);
		insertPoints.sort( (i1,i2) -> i2.compareTo(i1) ); //sort into reverse order
		
		Tag open = new Tag( a.openingTagText() );
		Tag close = new Tag( a.closingTagText() );
		//add close tag on even indices (e.g. rightmost insert position) 
		//and add open tag on odd indices.
		Function<Integer,Tag> nextTag = (i) -> i%2==0 ? close : open; 
		
		for(int i=0; i<insertPoints.size(); i++){
			content.add( insertPoints.get(i), nextTag.apply(i) );
			modCount++;
		}
	}
	
    /**
     * <p>Throws an exception if the {@code wordIndex}-th word in this HTMLFile is not
     * {@code word}.</p>
     * @param word the word whose status as the {@code wordIndex}-th word in this HTMLFile is to be
     * verified.
     * @param wordIndex the number of words prior to the word in this HTMLFile compared against
     * {@code word}, starting counting after the chapter title.
     * @throws IllegalStateException if the {@code wordIndex}-th word in this HTMLFile is not
     * {@code word}
     */
 	private void validateWordWithIndex(String word, int wordIndex){
		String wordThere = wordAt( wordIndex);
		if( !word.equals( wordThere )){
			throw new IllegalStateException(
					"Sought word (" + word 
					+ ") is not equal to the word (" + wordThere 
					+ ") with the specified word-index (" + wordIndex 
					+ ") in this file");
		}
	}
 	
     /**
      * <p>Adds the HTMLEntity {@code item} to the underlying list at index {@code position} in the
      * list.</p>
      * @param position the index in the underlying list at which {@code item} is inserted
      * @param item the HTMLEntity to be added to the underlying list
      */
 	public void add(int position, HTMLEntity item){
 		content.add(position, item);
 		modCount++;
 	}
 	
     /**
      * <p>Adds all elements of {@code list} to the underlying list, starting at index
      *  }position}.</p>
      * @param position the index in the underlying list at which elements of {@code list} are added
      * @param list HTMLEntitys to be added to the underlying list
      * @return true if the underlying list was changed, false otherwise
      */
 	public boolean addAll(int position, List<HTMLEntity> list){
 		modCount++;
 		return content.addAll(position, list);
 	}
 	
     /**
      * <p>Replaces the element in the underlying list at index {@code position} with
      * {@code elem}.</p>
      * @param position the index in the underlying list at which {@code elem} is placed
      * @param elem the HTMLEntity put into the list at index {@code position}
      * @return the element originally at index {@code position}
      */
 	public HTMLEntity set(int position, HTMLEntity elem){
 		modCount++;
 		return content.set(position, elem);
 	}
 	
     /**
      * <p>Returns the number of elements in the underlying list.</p>
      * @return the number of elements in the underlying list
      */
 	public int elementCount(){
 		return content.size();
 	}
	
     /**
      * <p>Returns the {@code wordIndex}-th (zero-based) word after the chapter's title in this
      * HTMLFile. The first word after the title has wordIndex 0.</p> <p>The wordIndex-th word is
      * determined by counting word-start points where a word character's most recent visible
      * predecessor (any {@link CharLiteral literal character} or {@link CharCode character code} is
      * not a word-legal character. HTML tags are not considered when finding the beginning of a
      * word.</p> <p>The characters after the first are determined via a look- around process
      * similar to that used to determine the location of the first character. The underlying list
      * is crawled with increasing index, and individual characters are added to a StringBuilder as
      * they are encountered. {@link Tag Tags} are ignored, and Codes and word-illegal literal
      * characters cause the crawl-accumulation process to end.</p>
      * @param wordIndex the number of words between the first word of the body of this file and the
      * word to be retrieved
      * @return the {@code wordIndex}-th (zero-based) word after the chapter's title in this
      * HTMLFile
      */
	private String wordAt(int wordIndex){
		StringBuilder result = new StringBuilder();

		int[] bounds = getWordBounds(wordIndex);
		
		for(int i=bounds[0]; i<bounds[1]; i++){
			HTMLEntity item = content.get(i);
			if( CharLiteral.class.isInstance(item) ){
				result.append( ((CharLiteral)item).c );
			}
		}
		
		return result.toString();
	}
	
    /**
     * <p>Returns a list of indices in the underlying list at which an anchor tag must be inserted
     * in order to link the {@code wordIndex}-th word to another instance of the repeated phrase of
     * which it is the first word.</p> <p>If there are any {@link Tag Tags} amongst the literal
     * characters of the {@code wordIndex}-th word, blindly adding opening and closing tags around
     * the outside of the word could result in unbalanced tags. So, this method crawls along the
     * underlying list finding any clusters of Tags inside the word and marking the points around
     * them where extra closing anchor tags and extra opening anchor tags are needed to ensure tags
     * are not unbalanced by the addition of these anchor tags.</p> <p>The returned list is
     * constructed keeping mind that the calling context will add anchor tags of the correct type
     * (closing or opening) and do so in reverse order, starting with the last index at which a tag
     * is to be added, allowing the indices less than that to retain their meaning without any
     * adjustments being needed.</p>
     * @param wordIndex the number of words between the sought word in this file and the first word
     * in the body of this chapter
     * @return a list of indices in the underlying list at which an anchor tag must be inserted in
     * order to link the {@code wordIndex}-th word to another instance of the repeated phrase of
     * which it is the first word
     */
	private List<Integer> anchorInsertionPoints(int wordIndex){
		List<Integer> result = new ArrayList<>();
		
		int[] bounds = getWordBounds(wordIndex);
		int lo = bounds[0];
		int hi = bounds[1];
		
		result.add(lo);
		for(int i=lo+1; i<hi; i++){
			if( is(i, 
					IS_CHARACTER, 
					Direction.PREV, 
					Tag.class::isInstance) //htmlFile.get(i) is a character preceded by a tag.
					|| is(i, 
							Tag.class::isInstance, 
							Direction.PREV, 
							IS_CHARACTER) ){ //htmlFile.get(i) is a Tag preceded by a character.
				result.add(i);
			}
		}
		result.add(hi);
		
		return result;
	}
	
    /**
     * <p>Returns true if the underlying list contains a contiguous (ignoring Tags) region of
     * literal characters starting at {@code index} which match the characters of {@code literal},
     * false otherwise.</p>
     * @param literal the literal text to be matched starting at {@code index} in the underlying
     * list
     * @param index the position in the underlying list starting at which {@code literal} is to be
     * sought
     * @return true if the underlying list contains a contiguous (ignoring Tags) region of literal
     * characters starting at {@code index} which match the characters of {@code literal}, false
     * otherwise.
     */
	public boolean hasLiteralAt(String literal, int index){
		for(int i=0; i<literal.length(); i++){
			char c = literal.charAt(i);
			if( !match(c, content.get(index)) ){
				return false;
			}
			index = adjacentElement(index, IS_CHARACTER, Direction.NEXT);
		}
		return true;
	}
	
    /**
     * <p>Returns true if this file contains the specified {@code literal} text starting at any
     * position after {@code start} and ending at any position before {@code end}, false
     * otherwise.</p>
     * @param literal
     * @param start exclusive lower bound
     * @param end exclusive upper bound
     * @return
     */
	public boolean hasLiteralBetween(String literal, int start, int end){
		for(int i=start+1; i<end-literal.length(); i++){
			if(hasLiteralAt(literal, i)){
				return true;
			}
		}
		return false;
	}
	
    /**
     * <p>Returns true if the {@code c} matches {@code h}, false otherwise. Generally, this means
     * that {@code c} is the literal character wrapped by  }h} because  }h} is a
     * {@link CharLiteral Ch}. A {@code Tag} never matches. A {@code Code} matches {@code c} if it
     * {@link CharCode#isEquivalent(char) is equivalent} to that {@code char}.</p>
     * @param c a literal {@code char} to be compared against {@code h}
     * @param h an HTMLEntity to be compared against {@code c}.
     * @return true if the {@code c} matches {@code h}, false otherwise
     */
	private boolean match(char c, HTMLEntity h){ //TODO move into HTMLEntity classes
		if( CharLiteral.class.isInstance(h) ){
			return ((CharLiteral)h).c == c;
		} else if(CharCode.class.isInstance(h)){
			return ((CharCode)h).isEquivalent(c);
		} else{
			return false;
		}
	}
	
    /**
     * <p>Returns true if the element at index {@code position} in the underlying list makes
     * {@code test1} evaluate to true and the element in the underlying list before or after (if
     * {@code dir} is Direction.PREV or Direction.NEXT respectively) that element makes
     * {@code test2} evaluate to true, false otherwise.</p>
     * @param position the position in the underlying list from which to extract an HTMLEntity to
     * send to {@code test1}
     * @param test1 the test to perform on the element of the underlying list at {@code position}
     * @param dir the direction to go from {@code position} to get another HTMLEntity to send to
     * {@code test2} for evaluation
     * @param test2 the test to perform on the element before or after the element at
     * {@code postion}, depending on the value of {@code dir}
     * @return true if the element at index {@code position} in the underlying list makes
     * {@code test1} evaluate to true and the element in the underlying list before or after (if
     * {@code dir} is Direction.PREV or Direction.NEXT respectively) that element makes
     * {@code test2} evaluate to true, false otherwise
     */
	private boolean is(
			int position, 
			Predicate<HTMLEntity> test1, 
			Direction dir, 
			Predicate<HTMLEntity> test2){
		
		HTMLEntity item = content.get( position );
		HTMLEntity prevOrNext = content.get( dir.apply(position) );
		return test1.test(item) && test2.test(prevOrNext);
	}
	
    /**
     * <p>Returns the first space-delimited word of {@code phrase}.</p>
     * @param phrase a phrase of which the first word is returned
     * @return the first space-delimited word of {@code phrase}.
     */
	public static final String firstWord(String phrase){
		int index = phrase.indexOf(PhraseProducer.WORD_SEPARATOR);
		return index<0 ? phrase : phrase.substring(0,index);
	}
	
    /**
     * <p>Prints this HTMLFile to a file named {@code name}.</p>
     * @param name the name of the file this HTMLFile is being saved as.
     */
	public void print(String name){
		try(OutputStreamWriter out = IO.newOutputStreamWriter(name);){
			print(out);
			out.close();
		} catch( IOException e){
			throw new RuntimeException(IO.ERROR_EXIT_MSG + name + " for writing.");
		}
	}
	
    /**
     * <p>Writes this HTMLFile to a file via {@code out}.</p>
     * @param out an OutputStreamWriter for the file that this HTMLFile is being saved as
     * @throws IOException if an I/O error occurs
     */
	public void print(OutputStreamWriter out) throws IOException{
		for(HTMLEntity item : content){
			out.write(item.toString());
		}
	}
	
    /**
     * <p>Writes to the file specified by {@code name} the text equivalent of the contents of this
     * file, except for everything before the end of the first table and everything after the
     * beginning of the last table--the head and foot tables used for this ASOIAF project.</p>
     * @param name the name of the file to which to write the text equivalent of this HTMLFile
     */
	public void printAsText(String name){
		int afterHeader = adjacentElement(-1, Tag::isTableClose, Direction.NEXT);
		int beforeFooter = adjacentElement(content.size(), Tag::isTableOpen, Direction.PREV);
		
		try(OutputStreamWriter out = IO.newOutputStreamWriter(name);){
			for(int i=afterHeader; i<=beforeFooter; i++){
				out.write(content.get(i).txtString());
			}
			out.close();
		} catch(IOException e){
			throw new RuntimeException(IO.ERROR_EXIT_MSG + name + " for writing.");
		}
	}
	
    /**
     * <p>Returns the index in the underlying list of the {@code wordIndex}th word in the file after
     * the title.</p>
     * @param wordIndex the number of words between the sought word in this file and the first word
     * in the body of this chapter
     * @return the index in the underlying list of the {@code wordIndex}th word in the file after
     * the title
     */
	public int getWord(int wordIndex){
		return getWordCache.applyAsInt(wordIndex);
	}
	
	//TODO split into a new class
    /**
     * <p>Does the work for {@link #getWord(int) getWord()} and stores its most recent input and
     * output to more quickly return a result.</p>
     */
	private final IntUnaryOperator getWordCache = new IntUnaryOperator(){
		
        /**
         * <p>When this record of how many times this HTMLFile has been modified is different from
         * the {@link HTMLFile#modCount value} stored in this HTMLFile itself, a result is
         * determined under worst-case scenario conditions, with the search beginning from the start
         * of the file.</p>
         */
		private int modCount = HTMLFile.this.modCount;
		
        /**
         * <p>The value that was last returned by {@code applyAsInt(int)}. Initialized to
         * Integer.MAX_VALUE. If {@code applyAsInt(int)} throws an exception, the output is not
         * stored.</p>
         */
		private int storedWordIndex = Integer.MAX_VALUE;
		
        /**
         * <p>The value sent as input to {@code applyAsInt(int)} for which {@code applyAsInt(int)}
         * last returned a result. Initialized to Integer.MAX_VALUE. If {@code applyAsInt(int)}
         * throws an exception, the input is not stored.</p>
         */
		private int storedWordStart = Integer.MAX_VALUE;
		
		@Override
        /**
         * <p>If {@code wordIndex} is the same as {@link #storedWordIndex the stored input}, then
         * the stored output is returned. Otherwise, crawls the underlying list from a starting
         * point and increments a counter when a {@link #isWordStart(int) word start} is encountered
         * until the counter reaches the input {@code wordIndex}, at which point the index in the
         * list at which the method is operating is returned.</p> <p>If {@code wordIndex} is less
         * than the stored input, then the start point for traversing the list is set to 0, the very
         * beginning of the list. If {@code wordIndex} is greater than the stored input, then the
         * start point for traversing the list is set to {@link #storedWordStart the stored output}
         * to save the time that would otherwise be spent traversing the list from the beginning to
         * that point.</p>
         * @param wordIndex the number of words between the sought word in this file and the first
         * word in the body of this chapter
         * @return the index in this HTMLFile's underlying list of the first letter of the
         * {@code wordIndex}th word in the body of this chapter.
         * @throws IllegalArgumentException if {@code wordIndex} is less than
         * {@link HTMLFile#baseWordIndex baseWordIndex}
         * @throws IllegalStateException if {@code wordIndex} is too high such that there aren't
         * enough words in this HTMLFile to count that high
         */
		public int applyAsInt(int wordIndex){
			if(wordIndex < baseWordIndex){
				throw new IllegalArgumentException(
						"wordIndex " + wordIndex 
						+ " less than baseWordIndex (" + baseWordIndex 
						+ ") is not allowable.");
			} else if(this.modCount == HTMLFile.this.modCount && wordIndex==storedWordIndex){
				return storedWordStart;
			} else{
				int previousWordIndex = baseWordIndex-1;
				int init_i = 0;
				
				if(this.modCount == HTMLFile.this.modCount){
					if(wordIndex > storedWordIndex){
						init_i = storedWordStart;
						previousWordIndex = storedWordIndex-1;
					}
				} else{
					this.modCount = HTMLFile.this.modCount;
				}
				
				int i;
				for(i=init_i; i<content.size(); i++){
					if( isWordStart(i) ){
						previousWordIndex++;
					}
					if(previousWordIndex==wordIndex){
						storedWordIndex = wordIndex;
						return storedWordStart = i;
					}
				}
				
				String msg = "The specified wordIndex (" + wordIndex 
						+ ") is too high (max value of " + previousWordIndex 
						+ ").";
				
				throw new IllegalStateException(msg);
			}
		}
	};
	
    /**
     * <p>Returns the position in the underlying list of the last character of the
     * {@code wordIndex}-th word in the body of this chapter.</p>
     * @param wordIndex the number of words between the sought word in this file and the first word
     * in the body of this chapter
     * @return the position in the underlying list of the last character of the {@code wordIndex}-th
     * word in the body of this chapter
     */
	public int getLastCharacter(int wordIndex){
		return getLastCharacter(wordIndex, getWord(wordIndex));
	}
	
    /**
     * <p>Returns the position in the underlying list of the last character of the
     * {@code wordIndex}-th word in the body of this chapter.</p>
     * @param wordIndex the number of words between the sought word in this file and the first word
     * in the body of this chapter
     * @param startPoint a position in (typically at the very start of) the {@code wordIndex}-th
     * word, from which to start looking for the end of the current word
     * @return the position in the underlying list of the last character of the {@code wordIndex}-th
     * word in the body of this chapter
     */
	private int getLastCharacter(int wordIndex, int startPoint){
		for(int i=startPoint; i<content.size(); i++){
			
			if( !isWord( adjacentElement(i, Direction.NEXT, IS_CHARACTER) ) ){
				return i;
			}
		}
		throw new IllegalStateException("Couldn't find last character of word.");
	}
	
    /**
     * <p>Returns an int array containing lower (inclusive) and upper (exclusive) bounds for the
     * {@code wordIndex}th word in this file.</p>
     * @param wordIndex the number of words between the sought word in this file and the first word
     * in the body of this chapter
     * @return an int array containing lower (inclusive) and upper (exclusive) bounds for the
     * {@code wordIndex}th word in this file
     */
	public int[] getWordBounds(int wordIndex){
		int start = getWord(wordIndex);
		return new int[]{start, getLastCharacter(wordIndex, start)+1};
	}
	
    /**
     * <p>Returns true if the element at index {@code index} in the underlying list is the first
     * character of a word, false otherwise.</p>
     * @param index the index in the underlying list to be tested for whether it's the first
     * character of a word
     * @return true if the element at index {@code index} in the underlying list is the first
     * character of a word, false otherwise
     */
	public boolean isWordStart(int index){
		return isWord(content.get(index)) 
				&& !isWord( adjacentElement(index, Direction.PREV, IS_CHARACTER) );
	}
	
    /**
     * <p>Returns true if the element at index {@code index} in the underlying list is the last
     * character of a word, false otherwise.</p>
     * @param index the index in the underlying list to be tested for whether it's the last
     * character of a word
     * @return true if the element at index {@code index} in the underlying list is the last
     * character of a word, false otherwise
     */
	public boolean isWordEnd(int index){
		return isWord(content.get(index))
				&& !isWord( adjacentElement(index, Direction.NEXT, IS_CHARACTER) );
	}
	
    /**
     * <p>Returns true if {@code elem} is character-type and is a word character, false
     * otherwise.</p>
     * @param elem the HTMLEntity to be assessed for legality as a word character
     * @return true if {@code elem} is character-type and is a word character, false otherwise
     */
	private static boolean isWord(HTMLEntity elem){
		return CharLiteral.class.isInstance(elem) 
				&& PhraseProducer.isPhraseChar( ((CharLiteral)elem).c );
	}
	
    /**
     * <p>Evaluates to true if the specified HTMLEntity {@code h} is a character-type HTMLEntity: a
     * {@link CharLiteral Ch} or a {@link CharCode Code}.</p>
     */
	public static final Predicate<HTMLEntity> IS_CHARACTER = 
			(h) -> CharLiteral.class.isInstance(h) || CharCode.class.isInstance(h);
	
    /**
     * <p>Evaluates to true if the specified HTMLEntity {@code h}
     * {@link #IS_CHARACTER is character-type} and is not a
     * {@link #isWord(HTMLEntity) legal word character}.</p>
     */
	public static final Predicate<HTMLEntity> IS_CHARACTER_NOT_WORD = 
			IS_CHARACTER.and((h) -> !isWord(h));

    /**
     * <p>Returns the position in the underlying list of the element nearest to but not at
     * {@code position} in the direction (before or after) specified by {@code direction} for which
     * {@code condition} evaluates to true.</p>
     * @param position the pre-starting position for this operation. One less than the starting
     * point if {@code direction} is {@code Direction.NEXT}, or one more if it's
     * {@code Direction.PREV}.
     * @param condition a Predicate whose evaluation to true causes this method to return its
     * current position in the underlying list
     * @param direction this method's direction of traversal of the underlying list
     * @return the position in the underlying list of the element nearest to but not at
     * {@code position} in the direction (before or after) specified by {@code direction} for which
     * {@code condition} evaluates to true
     */
	public int adjacentElement(int position, Predicate<HTMLEntity> condition, Direction direction){
		for(int i=direction.apply(position); 
				direction.crawlTest(i,content);
				i=direction.apply(i)){
			if(condition.test(content.get(i))){
				return i;
			}
		}
		return -1;
	}
	
	public HTMLEntity adjacentElement(
			int position, 
			Direction direction, 
			Predicate<HTMLEntity> typeRestriction){
		
		int index = adjacentElement(position, typeRestriction, direction);
		return index>=0 ? content.get(index) : null;
	}
	
	public int adjacentElement(Predicate<Integer> condition, Direction dir, int startPosition){
		for(int i=dir.apply(startPosition); dir.crawlTest(i,content); i=dir.apply(i)){
			if(condition.test(i)){
				return i;
			}
		}
		return -1;
	}
	
	public ParagraphIterator paragraphIterator(){
		return new ParagraphIterator();
	}
	
    /**
     * <p>Returns the {@code i}th element of the underlying list.</p>
     * @param i the position in the underlying list of the HTMLEntity returned
     * @return the {@code i}th element of the underlying list
     */
	public HTMLEntity get(int i){
		return content.get(i);
	}
	
    /**
     * <p>Removes the element at the specified position from the underlying list and returns it.</p>
     * @param position the position in the underlying list of the element that is removed
     * @return the element at {@code position} in the underlying list before it was removed
     */
	public HTMLEntity remove(int position){
		modCount++;
		return content.remove(position);
	}
	
    /**
     * <p>Removes from the underlying list all elements in the region bounded by {@code start} and
     * {@code end}.</p>
     * @param start the inclusive lower bound of the region to be removed from the underlying list
     * @param end the exclusive upper bound of the region to be removed from the underlying list
     */
	public void removeAll(int start, int end){
		List<HTMLEntity> front = content.subList(0,start);
		List<HTMLEntity> back = content.subList(end, content.size());
		front.addAll(back);
		content = new ArrayList<>(front);
		modCount++;
	}
	
    /**
     * <p>Removes all content from the underlying list at or after the position {@code start}. This
     * works like {@link java.lang.String#substring(int) substring}, but in reverse.</p>
     * @param start the inclusive lower bound of the region of the underlying list to be removed
     */
	public void removeAll(int start){
		content = new ArrayList<>( content.subList(0,start) );
		modCount++;
	}
	
    /**
     * <p>Removes from the underlying list all elements for which {@code test} evaluates to
     * true.</p>
     * @param test the Predicate that determines which elements are removed from the underlying
     * list.
     */
 	public void removeAll(Predicate<HTMLEntity> test){
 		for(int i=content.size()-1; i>=0; i--){
 			if( test.test(content.get(i)) ){
 				content.remove(i);
 				modCount++;
 			}
 		}
 	}
 	
     /**
      * <p>Returns the position in the underlying list of the closing HTML tag corresponding to an
      * opening HTML tag located at {@code startPoint}.</p> <p>When another opening Tag of the same
      * {@link Tag#getType() type} as the opening tag at {@code startPoint} is encountered before a
      * closing tag of the same type, a counter is incremented. When a closing Tag of the same type
      * is encountered, the counter is decreased. Only if the counter is at the correct value after
      * a closing Tag is found will that closing Tag's index be returned.</p>
      * @param startPoint the index in the underlying list of an opening Tag and the index at which
      * the search for a corresponding closing Tag is started,
      * @return the position in the underlying list of the closing HTML tag corresponding to an
      * opening HTML tag located at {@code startPoint}
      * @throws IllegalArgumentException if the element at {@code startPoint} is not an
      * {@link Tag#isOpening() opening} Tag.
      */
	public int closingMatch(int startPoint){
		HTMLEntity a = content.get(startPoint);
		if(!Tag.isOpen(a)){
			throw new IllegalArgumentException("The element at index "+startPoint+
					" (\""+a.toString()+"\") is not an opening tag.");
		}
		Tag t = (Tag) a;
		
		final String type = t.getType();
		final Predicate<HTMLEntity> isTagOfType = 
				(h) -> Tag.class.isInstance(h) && ((Tag)h).getType().equals(type);
		int tagIndex = startPoint;
		
		for(int depth=1; depth > 0 && 0<= tagIndex && tagIndex < content.size()-1;){
			tagIndex = adjacentElement(tagIndex, isTagOfType, Direction.NEXT);
			Tag someTag = (Tag) content.get(tagIndex);
			depth += ( someTag.isOpening() ? 1 : -1 );
		}
		return tagIndex;
	}
	
    /**
     * <p>Returns a list of all the HTMLEntitys from this file for which the specified Predicate
     * evaluates to true.</p>
     * @param test a Predicate used to distinguish which elements of this file should be returned
     * @return a list of all the HTMLEntitys from this file for which the specified Predicate
     * evaluates to true
     */
	public List<HTMLEntity> getElementsWhere(Predicate<HTMLEntity> test){
		List<HTMLEntity> result = new ArrayList<>();
		
		for(HTMLEntity item : content){
			if(test.test(item)){
				result.add(item);
			}
		}
		
		return result;
	}
	
    /**
     * <p>Returns a {@literal List<HTMLEntity>} representing the contents of the body scanned by
     * {@code s}. Each HTML tag, whether opening or closing, gets its own element in this list. Each
     * HTML character code (&...;) does, too, as does each literal character not part of a tag or a
     * code.<p> <p>The type of the List returned is
     * {@link repeatedphrases.ArrayList2 ArrayList2}.</p>
     * @param s a Scanner that produces the literal text of an HTML file to be rendered as an
     * {@code HTMLFile} in memory
     * @return a {@literal List<HTMLEntity>} representing the contents of the body scanned by
     * {@code s}
     */
	private static ArrayList<HTMLEntity> getHTMLFileContent(Scanner s){
		ArrayList<HTMLEntity> result = new ArrayList<>();
		
		StringBuilder fileBody = readFile(s);
		
		//iterate over the individual characters of the html file
		
		//stores '>' or ';' while iterating through an HTML tag or character code so we know when 
		//to stop skipping characters.
		Character mate = null;
		
        StringBuilder tagCode = null;
		for(int i=0; i<fileBody.length(); i++){
        	char c = fileBody.charAt(i);
        	
        	if(mate==null){ //we're not looking for a closing angle bracket or a semicolon.
        		Character counterpart = risingCounterpart(c);
        		
        		//if the current character c isn't an opening angle bracket or ampersand.
        		if(counterpart==null){
        			result.add( new CharLiteral(c) );
        		} else{
        			//c is a special character and we need to take special action.
        			//store the counterpart of c so we know what to look for later to end this 
        			//special condition.
        			mate = counterpart;
        			
        			//prepare to add characters to the body of the tag or code
        			tagCode = new StringBuilder();
        			//do not add c to the list.
        		}
        	} else if(mate.equals(c)){ //we are looking for a '>' or a ';' //we've found that mate
    			//then we can stop looking for that mate
    			HTMLEntity newEntry = mate==Tag.END ? new Tag(tagCode.toString()) : new CharCode(tagCode.toString());
    			result.add( newEntry);
    			mate = null;
    			tagCode = null;
    		} else{ //we're still in the middle of the current special HTML structure
    			tagCode.append(c);
    		}
        }
		
		return result;
	}
	
    /**
     * <p>Returns the character that ends an HTML tag or an HTML character code if {@code c} is the
     * character that begins an HTML tag or an HTML character code respectively; returns null
     * otherwise. This is a particular case of associating &lt; with &gt; and &amp; with ;. By
     * returning a non-null result only in the case of the characters used to <em>start</em> special
     * regions in HTML files, we avoid the problem of forcing the reader to wait for an ampersand
     * every time it finds a semicolon, which is a literal character found throughout the texts to
     * be processed.</p>
     * @param c a Character whose closing counterpart for special text in HTML files is returned, if
     * such a counterpart exists
     * @return the character that ends an HTML tag or an HTML character code if {@code c} is the
     * character that begins an HTML tag or an HTML character code respectively; returns null
     * otherwise
     */
	public static Character risingCounterpart(Character c){
    	switch(c){
    		case Tag.START  : return Tag.END;
    		case CharCode.START : return CharCode.END;
    		default         : return null;
        }
    }
	
    /**
     * <p>Returns a sublist of the list underlying this object, whose bounds are indicated by the
     * first and second entries in {@code bounds}.</p>
     * @param bounds an int array whose first entry is the inclusive lower bound of the region to be
     * returned and whose second entry is the exclusive upper bound of the region to be returned.
     * @return a sublist of the list underlying this object, whose bounds are indicated by the first
     * and second entries in {@code bounds}
     */
	public List<HTMLEntity> section(int[] bounds){
		return section(bounds[0], bounds[1]);
	}
	
	public List<HTMLEntity> section(int lo, int hi){
		return content.subList(lo, hi);
	}
	
	public List<HTMLEntity> section(int lo){
		return section(lo, content.size());
	}
	    
    /**
     * <p>Returns a StringBuilder whose contents are equal to the content returned by the specified
     * Scanner.</p> <p>Reads content from {@code s} line by line and appends each line, with a
     * newline character between lines, to a StringBuilder.</p>
     * @param s the Scanner whose contents are read out, accumulated, and returned
     * @return a StringBuilder whose contents are equal to the content returned by {@code s}.
     */
	private static StringBuilder readFile(Scanner s){
				
		StringBuilder result = new StringBuilder();
		
		if(s.hasNextLine()){
			result.append(s.nextLine());
		}
		while(s.hasNextLine()){
			result.append(CharLiteral.NEW_LINE).append(s.nextLine());
		}
		
		s.close();
		
		return result;
	}
	
	public static final Predicate<HTMLEntity> IS_PARAGRAPHISH_OPEN = //TODO use methods and ::
			(h) -> Tag.isPOpen(h) || Tag.isHeaderOpen(h) ;
	
    /**
     * <p>A utility class that crawls the list of HTMLEntity that underlies this HTMLFile and
     * locates entire paragraph blocks.</p>
     */
	public class ParagraphIterator implements Iterator<int[]>{
		
		private final int modCount;
		
        /**
         * <p>This ParagraphIterator's position in the list underlying this HTMLFile.</p>
         */
		private int position;
		
        /**
         * <p>Constructs a ParagraphIterator that works on {@code HTMLFile.this.content}.</p>
         */
		private ParagraphIterator(){
			position = -1;
			modCount = HTMLFile.this.modCount;
		}
		
		@Override
        /**
         * <p>Returns true if this ParagraphIterator has another paragraph block available in the
         * list underlying this HTMLFile, false otherwise.</p>
         * @return true if this ParagraphIterator has another paragraph block available in the list
         * underlying this HTMLFile, false otherwise.
         */
		public boolean hasNext(){
			concurrentModificationCheck();
			return -1 != adjacentElement(position, IS_PARAGRAPHISH_OPEN, Direction.NEXT);
		}
		
		@Override
        /**
         * <p>Returns an int array of length 2, containng the inclusive lower bound and exclusive
         * upper bound in this HTMLFile's underlying list of the opening and closing tags of the
         * next paragraph.</p>
         * @return an int array of length 2, containng the inclusive lower bound and exclusive upper
         * bound in this HTMLFile's underlying list of the opening and closing tags of the next
         * paragraph.
         */
		public int[] next(){
			concurrentModificationCheck();
			int start = adjacentElement(position, IS_PARAGRAPHISH_OPEN, Direction.NEXT);
			int end = closingMatch(start);
			int[] result = {start, end+1};
			position = end;
			return result;
		}
		
        /**
         * <p>Throws a ConcurrentModificationException if the underlying HTMLFile was modified after
         * this ParagraphIterator was constructed.</p>
         * @throws ConcurrentModificationException if the underlying HTMLFile was modified after
         * this ParagraphIterator was constructed
         */
		private void concurrentModificationCheck(){
			if( this.modCount != HTMLFile.this.modCount ){
				throw new ConcurrentModificationException(
						"Mismatch between " 
						+ "ParagraphIterator.this.modCount and HTMLFile.this.modCount: (" 
						+ this.modCount + " != " + HTMLFile.this.modCount + ")");
			}
		}
	}
	
    /**
     * <p>Returns true if the specified char is legal for a chapter title, false otherwise. Returns
     * true if {@code c} is a capital letter, space, or apostrophe, false otherwise.</p>
     * @param c a character to be tested for whether it is a legal character in a chapter title.
     * @return true if the specified char is legal for a chapter title, false otherwise.
     */
	public static boolean isTitle(char c){
		return ('A' <= c && c <= 'Z') || c == ' ' || c == '\'';
	}
}
