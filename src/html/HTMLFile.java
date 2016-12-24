package html;

import common.BookData;
import common.IO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Scanner;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import text.Chapter;
import text.Location;
import text.Phrase;

/**
 * <p>Represents an HTML file and provides some convenience methods for working with an HTML
 * file.</p>
 */
public class HTMLFile implements Iterable<HTMLEntity>{
	
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
	private List<HTMLEntity> content;
	
    /**
     * <p>The literal filename of the file to which the content of this HTMLFile belongs. Contains
     * an extension and possibly a folder reference.</p>
     */
	private final String filename;
	
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
						readFile(new Scanner(f, IO.ENCODING))
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
		filename = IO.stripFolder(name);
	}
	
    /**
     * <p>Constructs an HTMLFile based on the elements of {@code content}, with the filename
     * {@code name}.</p>
     * @param name the file address/name of this HTMLFile
     * @param content a list whose elements will be the elements of this HTMLFile
     */
	public HTMLFile(String name, List<HTMLEntity> content){
		this.content = new ArrayList<>(content);
		filename = IO.stripFolder(name);
	}
	
    /**
     * <p>Returns {@link #filename filename}.</p>
     * @return {@link #filename filename}
     */
	public String getName(){
		return filename;
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
		int wordIndex = a.position().getIndex();
		
		validateWordWithIndex(firstWord(a.phrase()), wordIndex);
		
		List<Integer> insertPoints = anchorInsertionPoints(wordIndex);
		insertPoints.sort((i1,i2) -> i2.compareTo(i1)); //sort into reverse order
		
		Tag open = new Tag(a.openingTagText());
		Tag close = new Tag(a.closingTagText());
		//add close tag on even indices (e.g. rightmost insert position) 
		//and add open tag on odd indices.
		Function<Integer,Tag> nextTag = (i) -> i%2==0 ? close : open; 
		
		for(int i = 0; i < insertPoints.size(); i++){
			content.add(insertPoints.get(i), nextTag.apply(i));
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
		String wordThere = wordAt(wordIndex);
		if(!word.equals(wordThere)){
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
		
		for(int i = bounds[0]; i < bounds[1]; i++){
			HTMLEntity item = content.get(i);
			if(CharLiteral.class.isInstance(item)){
				result.append(((CharLiteral)item).c);
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
		for(int i = lo + 1; i < hi; i++){
			if(is(i, 
					HTMLFile::isCharacter, 
					Direction.PREV, 
					Tag.class::isInstance) //htmlFile.get(i) is a character preceded by a tag.
					
					//htmlFile.get(i) is a Tag preceded by a character.
					|| is(i, 
							Tag.class::isInstance, 
							Direction.PREV, 
							HTMLFile::isCharacter)){
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
		for(int i = 0; i < literal.length(); i++){
			char c = literal.charAt(i);
			if(!content.get(index).match(c)){
				return false;
			}
			index = adjacentElement(index, HTMLFile::isCharacter, Direction.NEXT);
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
	    OptionalInt oi = IntStream.range(start + 1, end - literal.length())
	            .filter((i) -> hasLiteralAt(literal, i))
	            .findFirst();
	    return oi.isPresent();
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
		
		HTMLEntity item = content.get(position);
		HTMLEntity prevOrNext = content.get(dir.apply(position));
		return test1.test(item) && test2.test(prevOrNext);
	}
	
    /**
     * <p>Returns the first space-delimited word of {@code phrase}.</p>
     * @param phrase a phrase of which the first word is returned
     * @return the first space-delimited word of {@code phrase}.
     */
	public static final String firstWord(String phrase){
		int index = phrase.indexOf(Phrase.WORD_SEPARATOR);
		return index < 0 
		        ? phrase 
		        : phrase.substring(0, index);
	}
	
    /**
     * <p>Prints this HTMLFile to a file named {@code name}.</p>
     * @param name the name of the file this HTMLFile is being saved as.
     */
	public void print(String name){
		try(OutputStreamWriter out = IO.newOutputStreamWriter(name);){
			print(out);
			out.close();
		} catch(IOException e){
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
		int afterHeader = adjacentElement(-1, Tag::isTableClose, Direction.NEXT); //MAGIC
		int beforeFooter = adjacentElement(content.size(), Tag::isTableOpen, Direction.PREV);
		
		try(OutputStreamWriter out = IO.newOutputStreamWriter(name);){
			for(int i = afterHeader; i <= beforeFooter; i++){
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
	
	private class GetWordCache implements IntUnaryOperator{
        
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
        private int storedWordPointer = Integer.MAX_VALUE;
        
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
            if(wordIndex < 0){
                throw new IllegalArgumentException(
                        "wordIndex " + wordIndex + " < 0");
            } else if(this.modCount == HTMLFile.this.modCount && wordIndex == storedWordIndex){
                return storedWordPointer;
            } else{
                int previousWordIndex = -1;
                int init_i = 0;
                
                if(this.modCount == HTMLFile.this.modCount){
                    if(wordIndex > storedWordIndex){
                        init_i = storedWordPointer;
                        previousWordIndex = storedWordIndex-1;
                    }
                } else{
                    this.modCount = HTMLFile.this.modCount;
                }
                
                int i;
                for(i = init_i; i < content.size(); i++){
                    if(isWordStart(i)){
                        previousWordIndex++;
                    }
                    if(previousWordIndex==wordIndex){
                        storedWordIndex = wordIndex;
                        return storedWordPointer = i;
                    }
                }
                
                throw new IllegalStateException(
                        "The specified wordIndex (" + wordIndex 
                        + ") is too high (max value of " + previousWordIndex 
                        + ").");
            }
        }
    }
	
    /**
     * <p>Does the work for {@link #getWord(int) getWord()} and stores its most recent input and
     * output to more quickly return a result.</p>
     */
	private final IntUnaryOperator getWordCache = new GetWordCache();
	
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
		for(int i = startPoint; i < content.size(); i++){
			if(!isWord(adjacentElement(i, Direction.NEXT, HTMLFile::isCharacter))){
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
				&& !isWord(adjacentElement(index, Direction.PREV, HTMLFile::isCharacter));
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
				&& !isWord(adjacentElement(index, Direction.NEXT, HTMLFile::isCharacter));
	}
	
    /**
     * <p>Returns true if {@code elem} is character-type and is a word character, false
     * otherwise.</p>
     * @param elem the HTMLEntity to be assessed for legality as a word character
     * @return true if {@code elem} is character-type and is a word character, false otherwise
     */
	private static boolean isWord(HTMLEntity elem){
		return CharLiteral.class.isInstance(elem) 
				&& Phrase.isPhraseChar(((CharLiteral)elem).c);
	}
	
    /**
     * <p>Evaluates to true if the specified HTMLEntity {@code h} is a character-type HTMLEntity: a
     * {@link CharLiteral Ch} or a {@link CharCode Code}.</p>
     */
	public static boolean isCharacter(HTMLEntity h){
		return CharLiteral.class.isInstance(h) || CharCode.class.isInstance(h);
	}
	
    /**
     * <p>Evaluates to true if the specified HTMLEntity {@code h}
     * {@link #IS_CHARACTER is character-type} and is not a
     * {@link #isWord(HTMLEntity) legal word character}.</p>
     */
	public static boolean isCharacterNotWord(HTMLEntity h){
		return isCharacter(h) && !isWord(h);
	}

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
		for(int i = direction.apply(position); 
				direction.crawlTest(i, content);
				i = direction.apply(i)){
			if(condition.test(content.get(i))){
				return i;
			}
		}
		return -1; //MAGIC
	}
	
	public HTMLEntity adjacentElement(
			int position, 
			Direction direction, 
			Predicate<HTMLEntity> typeRestriction){
		
		int index = adjacentElement(position, typeRestriction, direction);
		return index >= 0 
		        ? content.get(index) 
		        : null;
	}
	
	public int adjacentElement(Predicate<Integer> condition, Direction dir, int startPosition){
		for(int i = dir.apply(startPosition); 
		        dir.crawlTest(i, content); 
		        i = dir.apply(i)){
			if(condition.test(i)){
				return i;
			}
		}
		return -1; //MAGIC
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
		content = new ArrayList<>(content.subList(0, start));
		modCount++;
	}
	
    /**
     * <p>Removes from the underlying list all elements for which {@code test} evaluates to
     * true.</p>
     * @param test the Predicate that determines which elements are removed from the underlying
     * list.
     */
 	public void removeAll(Predicate<HTMLEntity> test){
 		List<HTMLEntity> newContent = content.stream()
 		        .filter(test.negate())
 		        .collect(Collectors.toList());
 		
 		if(newContent.size() != content.size()){
 		    modCount++;
 		}
 		
 		content = newContent;
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
			throw new IllegalArgumentException(
			        "The element at index " + startPoint 
			        + " (\"" + a 
			        + "\") is not an opening tag.");
		}
		Tag t = (Tag) a;
		
		final String type = t.getType();
		final Predicate<HTMLEntity> isTagOfType = 
				(h) -> Tag.class.isInstance(h) && ((Tag)h).getType().equals(type);
		int tagIndex = startPoint;
		
		for(int depth = 1; depth > 0 && 0 <= tagIndex && tagIndex < content.size() - 1;){
			tagIndex = adjacentElement(tagIndex, isTagOfType, Direction.NEXT);
			Tag someTag = (Tag) content.get(tagIndex);
			depth += (someTag.isOpening() 
			        ? 1 //MAGIC
			        : -1); //MAGIC
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
		for(int i = 0; i < fileBody.length(); i++){
        	char c = fileBody.charAt(i);
        	
        	if(mate == null){ //we're not looking for a closing angle bracket or a semicolon.
        		Character counterpart = risingCounterpart(c);
        		
        		//if the current character c isn't an opening angle bracket or ampersand.
        		if(counterpart == null){
        			result.add(new CharLiteral(c));
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
    			HTMLEntity newEntry = (mate == Tag.END) 
    					? new Tag(tagCode.toString()) 
    					: new CharCode(tagCode.toString());
    			result.add(newEntry);
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
    		case Tag.START      : return Tag.END;
    		case CharCode.START : return CharCode.END;
    		default             : return null;
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
	
	public static boolean isParagraphishOpen(HTMLEntity h){
		return Tag.isPOpen(h) || Tag.isHeaderOpen(h);
	}
	
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
			position = -1; //MAGIC
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
			return -1 != adjacentElement(position, HTMLFile::isParagraphishOpen, Direction.NEXT);
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
			int start = adjacentElement(position, HTMLFile::isParagraphishOpen, Direction.NEXT);
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
			if(this.modCount != HTMLFile.this.modCount){
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
	
	public Collection<HTMLFile> cleanAndSplit(){
	    newlineP();
	    clearExcessStructure();
	    clearFrontAndBackMatter();
	    swapApostrophes();
	    return splitChapters();
	}
    
    private void newlineP(){
        List<HTMLEntity> newContent = new ArrayList<>(content.size());
        for(HTMLEntity h : content){
            if(Tag.isPOpen(h)){
                newContent.addAll(NEW_LINE);
            }
            newContent.add(h);
        }
        content = newContent;
    }
    
    private void clearExcessStructure(){
        emptyPs().stream().forEach((i) -> {
            content.set(i, null);
            content.set(i + 1, null);
        });
        content = content.stream()
                .filter(Objects::nonNull)
                .filter(HTMLEntity::notDiv)
                .filter(HTMLEntity::notBlockquote)
                .filter(HTMLEntity::notImg)
                .filter(HTMLEntity::notNbsp)
                .collect(Collectors.toList());
    }
    
    /**
     * returns a list of the indices in this HTMLFile of the opening paragraph tag of an empty 
     * paragraph
     * @return
     */
    private List<Integer> emptyPs(){
        return IntStream.range(0, content.size() - 1)
                .filter((i) -> Tag.isPOpen(content.get(i)))
                .filter((i) -> Tag.isPClose(content.get(i + 1)))
                .mapToObj(Integer::new)
                .collect(Collectors.toList());
    }
	
	private void clearFrontAndBackMatter(){
	    handleNovels();
	    handleNovellas();
	}
	
	private void handleNovels(){
	    int pWherePrologueTitle = prologueTitleBlock();
        removeAll(0, pWherePrologueTitle);
        
        int pWhereBackMatterStart = backMatterStart();
        removeAll(pWhereBackMatterStart);
	}
	
	private int prologueTitleBlock(){
	    String firstWords = NOVEL_FIRST_WORDS.get(filename);
        Predicate<Integer> hasFirstWordsAt = (i) -> hasLiteralAt(firstWords, i);
        
        int chapterStartIndex = adjacentElement(hasFirstWordsAt, Direction.NEXT, -1); //MAGIC
        
        Predicate<Integer> isPrologueBlock = 
                (i) -> isParagraphishOpen(get(i)) 
                        && hasLiteralBetween("PROLOGUE", i, closingMatch(i));
        int pLocation = adjacentElement(isPrologueBlock, Direction.PREV, chapterStartIndex);
        
        return pLocation - 1;
	}
	
	private int backMatterStart(){
        String lastWords = NOVEL_LAST_WORDS.get(filename);
        
        Predicate<Integer> hasLastWordsAt = (i) -> hasLiteralAt(lastWords, i);
        
        int textIndex = adjacentElement(hasLastWordsAt, Direction.PREV, elementCount());
        int pIndex = adjacentElement(textIndex, HTMLFile::isParagraphishOpen, Direction.NEXT);
        
        return pIndex;
	}
	
	private void handleNovellas(){
	    int pWhereFirstWords = firstWordsP();
        removeAll(0, pWhereFirstWords);

        int pWhereLastWords = lastWordsP();
        removeAll(pWhereLastWords + 1);
	}
    
    /**
     * <p>Returns the index in {@code file} of the opening "p" tag of the first paragraph that
     * starts with the {@link #FIRST_WORDS(String) first words} of the specified ASOIAF novella.</p>
     * @param file
     * @param novella
     * @return
     */
    private int firstWordsP(){
        String firstWords = NOVELLA_FIRST_WORDS.get(filename);
        Predicate<Integer> predicate = (i) -> hasLiteralAt(firstWords, i);
        
        int literalIndex = adjacentElement(predicate, Direction.NEXT, BEFORE_BEGINNING);
        
        return adjacentElement(literalIndex, Tag::isPOpen, Direction.PREV);
    }
    
    private static final int BEFORE_BEGINNING = -1;
    
    /**
     * <p>Returns the index in {@code file} of the closing "p" tag of the last paragraph that ends
     * with the {@link #lastWords(String) last words} of the specified ASOIAF novella.</p>
     * @param file
     * @param novella
     * @return
     */
    private int lastWordsP(){
        String lastWords = NOVELLA_LAST_WORDS.get(filename);
        Predicate<Integer> predicate = (i) -> hasLiteralAt(lastWords, i);
        
        int literalIndex = adjacentElement(predicate, Direction.PREV, elementCount());
        
        return adjacentElement(literalIndex, Tag::isPClose, Direction.NEXT);
    }
    
    private void swapApostrophes(){
        IntStream.range(0, content.size())
                .filter((i) -> CharLiteral.RIGHT_SINGLE_QUOTE.equals(content.get(i)))
                .filter((i) -> shouldChangeCharacter(i))
                .forEach((i) -> content.set(i, CharLiteral.APOSTROPHE));
    }
    
    private boolean shouldChangeCharacter(int i){
        return PATTERNS.parallelStream()
                .anyMatch((pattern) -> pattern.match(this, i));
    }
    
    private static final List<ApostrophePattern> PATTERNS = Stream.of(
            "@'@",  //everything from I'm to shouldn't've
            "&o'&", //of
            "&t'&", //to
            
            //'Yaya, the 'bite, 'bout, 'cat (shadowcat, 'tis, 'twas, 'twixt, 'prentice, 'em, 'ud
            "&'*",  
            "&ha'&",    //have, usually "gods have mercy"
            "&f'&", //for
            "&a'&", //at, as pronounced by some wildlings
            "traitors' graves",
            "traitors' collars",
            "traitors' heads",  //"@@@ traitors' @@@@@"
            "wolves' work",
            "wolves' heads",    //"@@@ wolves' @@@@"
            "rams' heads",
            "lions' heads",
            "lions' paws",
            "lions' tails",
            "&the alchemists' guild&",
            "&the alchemists' vile&",   //"&the alchemists' @@@@"
            "pyromancers' piss",
            "@@@s' own&",
            "&the @o@s' @@@@",
            "&the boys' grandfather&",
            "&the boys' heads&",    //"@@@ the boys' @@@@@"
            "&merchants' sons&",
            "&merchants' stalls&",  //"&merchants' s@@@"
            "merchants' carts",
            "the merchants' row",
            "the merchants' wagons",
            "&their mothers' @@@",  //"&their mothers' @@@@@"
            "whores' skirts",
            "&your brothers' @@@@",
            "&my brothers' ghosts&",    //"@@ brothers' @@@@"
            "&his brothers' @@@@",
            "@@@@s' nest",
            "horses' hooves",
            "be keepin'&",
            "is carryin'&", //"@@@@in' @@@"
            "@@@@ts' respite",
            "years' remission",
            "pigs' feet",
            "calves' brains",
            "servants' steps",
            "servants' time",
            "servants' corridor",
            "lords' bannermen",
            "lords' entrance",
            "were lords' sons",
            "&goats' milk&",
            "&slavers' filth&",
            "&slavers' pyramid&",
            "&sailors' stor@",
            "&sailors' temple&",
            "&smugglers' cove&",
            "&smugglers' stars&",
            "&days' ride&",
            "&days' food&",
            "&days' sail&",
            "&hours' ride&",
            "&hours' sail&",
            "bakers'&",
            "@ mummers' @@@@",
            "with strangers' eyes",
            "their masters' business",
            "the challengers' paddock",
            "stoops' wife",
            "&or one of the lannisters'&",
            "ladies' cats",
            "bastards' names",
            "the rangers' search",
            "their fathers' rusted swords",
            "his cousins' eyes",
            "maidens' judgments",
            "a singers' tourney",
            "a fools' joust",
            "an outlaws' lair",
            "rats' eyes",
            "the wildlings' herds",
            "dead friends' father",
            "archers' stakes",
            "heralds' trumpets",
            "the climbers' rope",
            "griffins' men",
            "their masters' possessions",
            "their neighbors' daughters",
            "the musicians' gallery",
            "kings' blood",
            "the besiegers' cheers",
            "gulls' eggs",
            "the defenders' shouts",
            "priests' song",
            "heroes' tombs",
            "some robbers' den",
            "babies' bottoms",
            "sound of lovers' footsteps",
            "the murderers' secret",
            "abandoned crofters' village",
            "the diggers' eyes were",
            "my sons' things",
            "&lil'&")
            .map(ApostrophePattern::new)
            .collect(Collectors.toList());
    
    /**
     * <p>Represents a pattern of characters around an apostrophe, meant for use in determining
     * which instances of a right single quote in the text of ASOIAF should be ordinary apostrophes
     * instead.</p>
     */
    private static class ApostrophePattern{
        
        /**
         * <p>Used in a string sent to ApoPattern's constructor, this represents any
         * {@link #isWordChar(Character) word character}. It is an asterisk: {@value}</p>
         */
        public static final char WORD_CHAR = '*';
        
        /**
         * <p>Used in a string sent to ApoPattern's constructor, this represents any
         * {@link #isWordChar(Character) non-word character}. It is an ampersand: {@value}</p>
         */
        public static final char NON_WORD_CHAR = '&';
        
        /**
         * <p>Used in a string sent to ApoPattern's constructor, this represents any
         * {@link #isAlphabetical(Character) alphabetic character}. It is an at sign: {@value}</p>
         */
        public static final char ALPHA_CHAR = '@';
        
        /**
         * <p>A list of the characters from the string used to construct this ApoPatern prior to the
         * apostrophe, in reverse order. For example, sending "ab'cd" to the constructor makes
         * {@code before} equivalent to
         * {@code before = new ArrayList<>(); before.add(new Character('b')); before.add(new Character('a'));}</p>
         */
        private List<Character> before;
        
        /**
         * <p>A list of the characters from the string used to construct this ApoPatern after to the
         * apostrophe. For example, sending "ab'cd" to the constructor makes {@code before}
         * equivalent to
         * {@code after = new ArrayList<>(); after.add(new Character('c')); after.add(new Character('d'));}</p>
         */
        private List<Character> after;
        
        /**
         * <p>Constructs an ApoPattern based on the specified string.</p>
         * @param s a string containing an apostrophe used to specify characters around an
         * apostrophe
         */
        private ApostrophePattern(String s){
            s = s.toLowerCase();
            int index = s.indexOf(CharLiteral.APOSTROPHE.c);
            before = new ArrayList<>();
            after = IntStream.range(index + 1, s.length())
                    .mapToObj(s::charAt)
                    .collect(Collectors.toList());
            for(int i = index - 1; i >= 0; i--){
                before.add(s.charAt(i));
            }
        }
        
        public boolean match(HTMLFile h, int index){
            return CharLiteral.RIGHT_SINGLE_QUOTE.equals(h.get(index)) 
                    && Stream.of(Side.values())
                            .allMatch((side) -> side.match(ApostrophePattern.this, h, index));
        }
        
        private static enum Side{
            BEFORE((ap) -> ap.before, (i) -> i - 1), 
            AFTER ((ap) -> ap.after,  (i) -> i + 1);
            
            private final Function<ApostrophePattern, List<Character>> listFunc;
            private final IntUnaryOperator nextInt;
            
            private Side(
                    Function<ApostrophePattern, 
                    List<Character>> listFunc, 
                    IntUnaryOperator nextInt){
                
                this.listFunc = listFunc;
                this.nextInt = nextInt;
            }
            
            boolean match(ApostrophePattern pattern, HTMLFile h, int a){
                for(char c : listFunc.apply(pattern)){
                    a = nextInt(a, h);
                    if(!ApostrophePattern.match(c, characterAt(a, h))){
                        return false;
                    }
                }
                return true;
            }
            
            private int nextInt(int a, HTMLFile h){
                do{
                    a = nextInt.applyAsInt(a);
                    
                    //the while test will throw an exception if a is not in range, but a is tested 
                    //for range in Side.match() before being sent to this method; so, that problem 
                    //should never occur
                } while(!(h.get(a) instanceof CharLiteral));
                return a;
            }
        }
        
        /**
         * If the thing in {@code h} at the specified {@code index} is an actual character, then 
         * return that character, otherwise return null.
         * @param index
         * @param h
         * @return the literal character in {@code h} at the specified {@code index} if one exists 
         * there, otherwise {@code null}
         */
        private static Character characterAt(int index, HTMLFile h){
            if(0 <= index && index < h.elementCount()){
                HTMLEntity ent = h.get(index);
                if(ent instanceof CharLiteral){
                    CharLiteral cl = (CharLiteral) ent;
                    return cl.c;
                }
            }
            return null;
        }
        
        /**
         * <p>Returns true if the character from a line of an HTML file matches the corresponding
         * character from the string used to construct this ApoPattern, false otherwise.</p> <p>The
         * special characters {@link #WORD_CHAR WORD_CHAR}, {@link #NON_WORD_CHAR NON_WORD_CHAR},
         * and {@link #ALPHA_CHAR ALPHA_CHAR} in this instance's code initiate specific tests; all
         * other characters are tested literally against the {@code fromFile} character.</p>
         * @param fromInstanceCode a character from the string used to construct this ApoPattern
         * @param fromFile a character from a line from an HTML file
         * @return true if the character from a line of an HTML file matches the corresponding
         * character from the string used to construct this ApoPattern, false otherwise
         */
        private static boolean match(Character fromInstanceCode, Character fromFile){
            switch(fromInstanceCode){
            case WORD_CHAR     : return Phrase.isPhraseChar(fromFile);
            case NON_WORD_CHAR : return !Phrase.isPhraseChar(fromFile);
            case ALPHA_CHAR    : return isAlphabetical(fromFile);
            default            : return fromFile != null 
                    && fromInstanceCode.equals(Character.toLowerCase(fromFile)); 
            }
        }
        
        /**
         * <p>Returns true if {@code c} is an alphabetical character, false otherwise.</p>
         * @param c a character to be evaluated as alphabetical or not
         * @return true if {@code c} is an alphabetical character, false otherwise
         */
        private static boolean isAlphabetical(Character c){
            return c != null 
                    && !(c == CharLiteral.APOSTROPHE.c 
                            || c == '-' 
                            || ('0' <= c && c <= '9')) 
                    && Phrase.isPhraseChar(c);
        }
    }
    
    private static final Map<String,String> NOVEL_FIRST_WORDS = 
            BookData.words(BookData::isNovel, BookData::firstWords);
    
    private static final Map<String,String> NOVEL_LAST_WORDS = 
            BookData.words(BookData::isNovel, BookData::lastWords);
    
    private static final Map<String,String> NOVELLA_FIRST_WORDS = 
            BookData.words(BookData::isNovella, BookData::firstWords);
    
    private static final Map<String,String> NOVELLA_LAST_WORDS = 
            BookData.words(BookData::isNovella, BookData::lastWords);
	
	private Collection<HTMLFile> splitChapters(){
	    if(isNovel()){
	        return handleNovel();
	    } else if(isPQ()){
            return handlePQ();
        } else{
            return handleNovella();
        }
	}
	
	private boolean isNovel(){
	    return BookData
	            .valueOf(
	                    BookData.class, 
	                    filename.substring(0, filename.length() - IO.HTML_EXT.length()))
	            .isNovel();
	}
	
	private boolean isPQ(){
	    return BookData.PQ.filename().equals(this.filename);
	}
	
	private Collection<HTMLFile> handleNovel(){
	    List<HTMLFile> result = new ArrayList<>();
	    
        HTMLFile.ParagraphIterator piter = paragraphIterator();
        List<HTMLEntity> buffer = new ArrayList<>();
        int writeCount = 0;
        String chapterName = null;
        
        while(piter.hasNext()){
            int[] paragraphBounds = piter.next();
            
            List<HTMLEntity> paragraph = section(paragraphBounds);
            
            if(isTitleParagraph(paragraph)){
                if(chapterName != null){
                    //dump the buffer
                    result.add(saveChapterFile(buffer, chapterName, writeCount));
                    writeCount++;
                }
                
                //new buffer
                chapterName = extractChapterTitle(paragraph);
                buffer = new ArrayList<>();
            } else{
                buffer.addAll(paragraph);
                
                //TODO add a constant somewhere to use here. 
                //Only need 1 instance in memory
                buffer.add(new CharLiteral('\n'));
            }
        }
        
        //reached end of file
        //dump the buffer to a file
        result.add(saveChapterFile(buffer, chapterName, writeCount));
	    
	    return result;
	}
    
    /**
     * <p>Returns the name of the file to which a chapter's content will be written.</p>
     * @param bookFile the source file from {@code READ_FROM} from which the chapter's content was
     * extracted
     * @param chapterIndex the chapter's number in its book (zero-based)
     * @param chapterName the name of the chapter as extracted from the text of its source html
     * novel file, including spaces
     * @return the name of the file to which a chapter's content will be written
     */
    private static String chapterFileName(HTMLFile file, int chapterIndex, String chapterName){
        String bookName = IO.stripExtension(file.filename);
        return bookName 
                + IO.FILENAME_COMPONENT_SEPARATOR_CHAR 
                + chapterIndex 
                + IO.FILENAME_COMPONENT_SEPARATOR_CHAR 
                + chapterName.replace(' ', IO.FILENAME_COMPONENT_SEPARATOR_CHAR) 
                + IO.HTML_EXT;
    }
	
	private HTMLFile saveChapterFile(
	        List<HTMLEntity> buffer, 
	        String chapterName, 
	        int saveCount){
	    
	    HTMLFile result = new HTMLFile(
	            buffer, 
	            chapterFileName(this, saveCount, chapterName));
	    result.addHeaderFooter();
	    return result;
	}
	
	private HTMLFile(List<HTMLEntity> buffer, String fileName){
	    this.content = new ArrayList<>(buffer);
	    this.filename = IO.stripFolder(fileName);
	}
    
    /**
     * <p>Returns true if the {@code paragraph}'s only character-type contents are characters that
     * can appear in chapter titles, false otherwise.</p>
     * @param paragraph a list of HTMLEntity, a piece of an HTMLFile
     * @return true if the {@code paragraph}'s only character-type contents are characters that can
     * appear in chapter titles, false otherwise
     */
    private static boolean isTitleParagraph(List<HTMLEntity> paragraph){
        boolean titleCharFound = false;
        
        for(HTMLEntity h : paragraph){
            if(CharCode.class.isInstance(h)){
                return false;
            } else if(CharLiteral.class.isInstance(h)){
                if(isLegalChapterTitleCharacter(((CharLiteral)h).c)){
                    titleCharFound = true;
                } else{
                    return false;
                }
            }
        }

        return titleCharFound;
    }
    
    /**
     * <p>Returns true if {@code c} occurs in chapters' titles, false otherwise.</p>
     * @param c a char to be tested for status as a character that occurs in chapters' titles
     * @return true if {@code c} is an uppercase letter, space, or apostrophe
     */
    public static boolean isLegalChapterTitleCharacter(char c){
        return ('A'<=c && c<='Z') || c==' ' || c=='\'';
    }
    
    /**
     * <p>Extracts a chapter's title from a {@code paragraph}
     * {@link #isTitleParagraph(List<HTMLEntity>) containing a chapter title}.</p>
     * @param paragraph the paragraph whose contained chapter title is extracted and returned
     * @return the chapter title that's the sole visible content of the specified {@code paragraph}
     */
    private static String extractChapterTitle(List<HTMLEntity> paragraph){
        StringBuilder result = new StringBuilder(paragraph.size());

        for(HTMLEntity h : paragraph){
            if(CharLiteral.class.isInstance(h)){
                result.append(((CharLiteral)h).c);
            }
        }

        return result.toString();
    }
	
	//TODO make sure that splitChapter implementation methods can't bastardize the HTMLFile 
	//instance to which they belong if they're called more than once.
	//Make sure that headers, footers, newlineP changes, and other alterations can't be made 
	//multiple times to the same HTMLFile object
	private Collection<HTMLFile> handleNovella(){
	    HTMLFile copy = clone();
	    copy.addHeaderFooter();
	    return new ArrayList<>(Arrays.asList(copy));
	}
	
	@Override
	public HTMLFile clone(){
	    return new HTMLFile(this);
	}
	
	private HTMLFile(HTMLFile file){
        this.filename = file.filename;
	    this.content = new ArrayList<>(file.content);
	}
	
	public void forEach(Consumer<? super HTMLEntity> action){
	    content.forEach(action);
	}
	
	private Collection<HTMLFile> handlePQ(){
	    HTMLFile[] files;
	    {
	        HTMLFile body;
	        {
	            int footnoteIndex = adjacentElement(
	                    (i) -> hasLiteralAt("Footnote",i), Direction.PREV, elementCount());
	            int bodyEndIndex = adjacentElement(footnoteIndex, Tag::isPOpen, Direction.PREV);
	            List<HTMLEntity> bodySection = section(0,bodyEndIndex);
	            body = new HTMLFile("PQ_0_THE_PRINCESS_AND_THE_QUEEN.html", bodySection);
	        }
	        
	        HTMLFile footnote;
	        {
	            int footnoteStart = adjacentElement(
	                    elementCount(), 
	                    Tag::isPOpen, 
	                    Direction.PREV);
	            List<HTMLEntity> footnoteSection = section(footnoteStart);
	            footnote = new HTMLFile("PQ_1_FOOTNOTE.html", footnoteSection);
	        }
	        
	        files = new HTMLFile[]{
	                body, 
	                footnote
            };
	    }
	    
        String[] hrefs = {
                "PQ_1_FOOTNOTE.html#FOOTNOTE", 
                "PQ_0_THE_PRINCESS_AND_THE_QUEEN.html#FOOTNOTE"
        };
        
        IntStream.range(0, PQ_FINAL_FILE_COUNT)
                .parallel()
                .forEach((i) -> {
                    HTMLFile file = files[i];
                    String href = hrefs[i];
                    
                    //replace superscript 1 with asterisk
                    int noteIndex = file.adjacentElement(-1, Tag::isSup, Direction.NEXT);
                    noteIndex = file.adjacentElement(noteIndex, CharLiteral::is1, Direction.NEXT);
                    file.set(noteIndex, new CharLiteral('*'));
                    
                    //replace internal link with external link
                    int noteAnchorIndex = file.adjacentElement(
                            noteIndex, 
                            Tag::isAnchorOpen, 
                            Direction.PREV);
                    file.set(
                            noteAnchorIndex, 
                            new Tag("a id=\"FOOTNOTE\" href=\"" + href + "\"" ));
                    
                    file.addHeaderFooter();
                });
        
        return new ArrayList<>(Arrays.asList(files));
	}
	
	private void addHeaderFooter(){
	    List<HTMLEntity> head = header();
	    List<HTMLEntity> foot = footer();
	    List<HTMLEntity> newContent = new ArrayList<>(head.size() + content.size() + foot.size());
	    newContent.addAll(head);
	    newContent.addAll(content);
	    newContent.addAll(foot);
	    
	    content = newContent;
	}
    
    private static final List<HTMLEntity> HEADER_FRONT_HTML = new ArrayList<>();
    static{
        Stream.of(
                "html", 
                "head", 
                "meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" /", 
                "link href=\"style.css\" rel=\"stylesheet\" type=\"text/css\" /", 
                "/head", 
                "body", 
                "div class=\"chapter\"", 
                "div class=\"head\"", 
                "table class=\"head\"", 
                "tr class=\"head\"", 
                "td class=\"prev_chapter\"", 
                "p class=\"prev_chapter\"", 
                "a id=\"prev_chapter\" href=\"nowhere\" title=\"nothing\" style=\"change_chapter\"")
                .map(Tag::new)
                .forEach(HEADER_FRONT_HTML::add);
        
        Collections.addAll(
                HEADER_FRONT_HTML, 
                CharCode.LT, 
                CharCode.LT);
        
        Stream.of(
                "/a", 
                "/p", 
                "/td", 
                "td class=\"chapter_title\"", 
                "p class=\"chapter_title\"")
                .map(Tag::new)
                .forEach(HEADER_FRONT_HTML::add);
    }
    
    public static final List<HTMLEntity> HEADER_BACK_HTML = new ArrayList<>();
    static{
        Stream.of(
                "/p", 
                "/td", 
                "td class=\"next_chapter\"", 
                "p class=\"next_chapter\"", 
                "a id=\"next_chapter\" href=\"nowhere\" title=\"nothing\" style=\"change_chapter\"")
                .map(Tag::new)
                .forEach(HEADER_BACK_HTML::add);
        
        Collections.addAll(
                HEADER_BACK_HTML, 
                CharCode.GT, 
                CharCode.GT);
        
        Stream.of(
                "/a", 
                "/p", 
                "/td", 
                "/tr", 
                "/table", 
                "/div", 
                "div class=\"chapter_body\"")
                .map(Tag::new)
                .forEach(HEADER_BACK_HTML::add);
        
        //TODO unify this use with the other list-of-characters representation in this project
        HEADER_BACK_HTML.addAll(CharLiteral.NEW_LINE_LITERAL);
    }
    
    public List<HTMLEntity> header(){
        List<CharLiteral> name = CharLiteral.asList(chapterName());
        List<HTMLEntity> result = new ArrayList<>(
                HEADER_FRONT_HTML.size() + name.size() + HEADER_BACK_HTML.size());
        result.addAll(HEADER_FRONT_HTML);
        result.addAll(name);
        result.addAll(HEADER_BACK_HTML);
        return result;
    }
    
    private String chapterName(){
        String[] splitFilename = IO.stripFolderExtension(this.filename)
                .split(IO.FILENAME_COMPONENT_SEPARATOR, FILENAME_ELEMENT_COUNT);
        String chapterPart = splitFilename[FILENAME_CHAPTERNAME_INDEX];
        return chapterPart
                .replace(IO.FILENAME_COMPONENT_SEPARATOR, Phrase.WORD_SEPARATOR)
                .toUpperCase();
    }
    
    public static final List<HTMLEntity> FOOTER_FRONT_HTML = new ArrayList<>();
    static{
        Stream.of(
                "/div", 
                "div class=\"foot\"", 
                "table class=\"foot\"", 
                "tr class=\"foot\"", 
                "td class=\"prev_chapter\"", 
                "p class=\"prev_chapter\"", 
                "a id=\"prev_chapter\" href=\"nowhere\" title=\"nothing\" style=\"change_chapter\"")
                .map(Tag::new)
                .forEach(FOOTER_FRONT_HTML::add);
        
        Collections.addAll(FOOTER_FRONT_HTML, CharCode.LT, CharCode.LT);
        
        Stream.of(
                "/a", 
                "/p", 
                "/td", 
                "td class=\"chapter_title\"", 
                "p class=\"chapter_title\"")
                .map(Tag::new)
                .forEach(FOOTER_FRONT_HTML::add);
    }
    
    public static final List<HTMLEntity> FOOTER_BACK_HTML = new ArrayList<>();
    static{
        Stream.of(
                "/p", 
                "/td", 
                "td class=\"next_chapter\"", 
                "p class=\"next_chapter\"", 
                "a id=\"next_chapter\" href=\"nowhere\" title=\"nothing\" style=\"change_chapter\"")
                .map(Tag::new)
                .forEach(FOOTER_BACK_HTML::add);
        
        Collections.addAll(FOOTER_BACK_HTML, CharCode.GT, CharCode.GT);
        
        Stream.of(
                "/a", 
                "/p", 
                "/td", 
                "/tr", 
                "/table", 
                "/div", 
                "/div", 
                "/body", 
                "/html")
                .map(Tag::new)
                .forEach(FOOTER_BACK_HTML::add);
    }
    
    public List<HTMLEntity> footer(){
        List<CharLiteral> name = CharLiteral.asList(chapterName());
        List<HTMLEntity> result = new ArrayList<>(
                FOOTER_BACK_HTML.size() + name.size() + FOOTER_FRONT_HTML.size());
        result.addAll(FOOTER_FRONT_HTML);
        result.addAll(name);
        result.addAll(FOOTER_BACK_HTML);
        return result;
    }
	
	public static final int PQ_FINAL_FILE_COUNT = 2;
    
    /**
     * <p>The first characters of an opening paragraph tag.</p>
     */
    public static final String BEGIN_P = "<p ";
	
	public static final List<CharLiteral> NEW_LINE;
	static{
	    NEW_LINE = new ArrayList<>(IO.NEW_LINE.length());
	    for(char c : IO.NEW_LINE.toCharArray()){
	        NEW_LINE.add(new CharLiteral(c));
	    }
	}
	
	@Override
	public Iterator<HTMLEntity> iterator(){
	    return content.iterator();
	}
	
	/**
	 * <p>Generates a plaintext representation of the content of this html file other than the 
	 * chapter title if this is an html chapter.</p>
	 * @return
	 */
	public String body(){
	    StringBuilder sb = new StringBuilder();
	    
	    int startPoint = firstPClose();
	    
	    for(int i = startPoint; i < content.size(); i++){
	        StringBuilder entityText = new StringBuilder(content.get(i).txtString());
	        for(int j = 0; j < entityText.length(); j++){
	            if(!Chapter.isWordChar(entityText.charAt(j))){
	                entityText.setCharAt(j, Phrase.WORD_SEPARATOR_CHAR);
	            }
	        }
	        sb.append(entityText);
	    }
	    
	    return sb.toString();
	}
	
	private int firstPClose(){
	    return IntStream.range(0, content.size())
	            .filter((i) -> Tag.isPClose(content.get(i)))
	            .findFirst()
	            .getAsInt();
	}
	
	public HTMLFile link(List<AnchorInfo> anchors){
	    anchors.stream()
        	    .sorted()
        	    .forEach(this::addAnchor);
	    return this;
	}

    /**
     * <p>The value of the id attribute of the anchors in the head and foot tables for html chapters
     * which link to the previous chapter.</p>
     */
    private static final String PREV_CHAPTER = "prev_chapter";
    
    /**
     * <p>The value of the id attribute for the anchors in the head and foot tables for html
     * chapters which link to the next chapter.</p>
     */
    private static final String NEXT_CHAPTER = "next_chapter";
    
    /**
     * <p>The string that names the "id" attribute of an html tag.</p>
     */
    private static final String ID_ATTRIB = "id";
	
	public void setTrail(String prev, String next){
	    setAdjacency(PREV_CHAPTER, ID_ATTRIB, prev);
        setAdjacency(NEXT_CHAPTER, ID_ATTRIB, next);
	}
	
	private void setAdjacency(String idValue, String idAttrib, String address){
	    Predicate<HTMLEntity> isAnchorWithMatchID = 
                (h) -> isAnchorWithMatchID(h, idValue, idAttrib);
        int pointer = INIT_POINTER;
        while(INIT_POINTER 
                != (pointer = adjacentElement(pointer, isAnchorWithMatchID, Direction.NEXT))){
            
            String tag = get(pointer).toString();
            tag = tag.substring(1, tag.length() - 1); //MAGIC both ones are the length of the < and > for a tag
            
            set(pointer, new Tag(anchor(tag, address)));
        }
	}
    
    /**
     * <p>Returns a String based on {@code tag}, with the value of the pre-existing href attribute
     * replaced by the parameter {@code address} and with the value of the pre-existing title
     * attribute replaced by a chapter title extracted from {@code address} by calling
     * {@link #title(String) title(address)}.</p> <p>For example,
     * {@code anchor("<a href=\"no.html\" title=\"no\">", "book_0_yes_yes.html")} would return "<a
     * href=\"book_0_yes_yes.html\" title=\"yes yes\">".</p>
     * @param tag
     * @param address
     * @return
     */
    private static String anchor(String tag, String address){
        return replaceValueOfAttribute(
                replaceValueOfAttribute(tag, HREF_START, address), 
                TITLE_START, 
                title(address));
    }
    
    /**
     * <p>Returns the value for the title attribute of an anchor tag based on the specified address
     * to which the anchor links.</p> <p>Returns {@code address} with its book name, chapter index,
     * and file extension stripped away and underscores replaced with spaces.</p>
     * @param address the address of an html file for a chapter being linked.
     * @return {@code address} with its book name, chapter index, and file extension stripped away.
     */
    private static String title(String address){
        if(address.isEmpty()){
            return address;
        }
        String name = IO.stripFolderExtension(address);
        String withoutBook = name.substring(1 + name.indexOf(IO.FILENAME_COMPONENT_SEPARATOR_CHAR));
        String withoutIndx = withoutBook.substring(
                1 + withoutBook.indexOf(IO.FILENAME_COMPONENT_SEPARATOR_CHAR));
        return withoutIndx.replace(IO.FILENAME_COMPONENT_SEPARATOR_CHAR, ' ');
    }
    
    /**
     * <p>Replaces the pre-existing value of the attribute specified by {@code attributeStart} in
     * the specified {@code body} of an html tag with {@code installValue}.</p>
     * @param body the text of an html tag of which a modified version is returned
     * @param attributeStart identifies the attribute whose value is to be modified. Must be the
     * name of an attribute followed by an equals sign followed by a double quote, such as
     * {@link #TITLE_START TITLE_START} or {@link #HREF_START HREF_START}.
     * @param installValue the value of the attribute named by {@code attributeStart} to install in
     * place of the pre-existing value
     * @return the pre-existing value of the attribute specified by {@code attributeStart} in the
     * specified {@code body} of an html tag with {@code installValue}
     */
    private static String replaceValueOfAttribute(
            String body, 
            String attributeStart, 
            String installValue){
        
        int start = body.indexOf(attributeStart)+attributeStart.length();
        int end = body.indexOf(QUOTE, start);
        String front = body.substring(0,start);
        String back = body.substring(end);
        return front + installValue + back;
    }
    
    /**
     * <p>The closing quote for an html tag attribute's value.</p>
     */
    private static final String QUOTE = "\"";
    
    /**
     * <p>The text of the html "href" attribute followed by an equals sign and a quote.</p>
     */
    private static final String HREF_START = "href=\"";
    
    /**
     * <p>The title attribute of an html tag and the quote that begins the attribute's value.</p>
     */
    private static final String TITLE_START = "title=\"";
    
    private static final int INIT_POINTER = -1;
    
    private static boolean isAnchorWithMatchID(HTMLEntity h, String idValue, String idAttrib){
        if(Tag.class.isInstance(h)){
            Tag t = (Tag) h;
            return t.isType(Tag.A) && idValue.equals(t.valueOfAttribute(idAttrib)); 
        }
        return false;
    }
}
