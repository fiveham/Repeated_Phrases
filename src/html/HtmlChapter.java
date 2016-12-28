package html;

import common.IO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import text.Location;
import text.Phrase;

/**
 * <p>Represents an HTML file and provides some convenience methods for working with an HTML
 * file.</p>
 */
public class HtmlChapter implements Iterable<HtmlEntity>, Cloneable{
    
    /**
     * <p>The index of the chapter's title in the array resulting from calling
     * String.split(UNDERSCORE, FILENAME_ELEMENT_COUNT) on the extensionless name of this chapter's
     * file. Chapter file names are structured as follows:
     * "BOOKNAME_CHAPTERINDEX_MULTI_WORD_CHAPTER_TITLE.html" Splitting that String at underscores
     * leaves the chapter's title at index 2 in the resulting array, but only if
     * {@link java.lang.String#split() split()} is limited in how many times it tries to split the
     * string, requiring {@link #FILENAME_ELEMENT_COUNT a limit}.</p>
     */
    private static final int FILENAME_CHAPTERNAME_INDEX = 2;
    
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
    private List<HtmlEntity> content;
    
    /**
     * <p>The literal filename of the file to which the content of this HtmlChapter belongs. Contains
     * an extension and possibly a folder reference.</p>
     */
    private final String filename;
    
    private int modCount = 0;
    
    /**
     * <p>Constructs an HtmlChapter based on the elements of {@code content}, with the filename
     * {@code name}.</p>
     * @param name the file address/name of this HtmlChapter
     * @param content a list whose elements will be the elements of this HtmlChapter
     */
    private HtmlChapter(String name, List<HtmlEntity> content){
        this.content = new ArrayList<>(content);
        filename = IO.stripFolder(name);
    }
    
    static HtmlChapter fromBuffer(String name, List<HtmlEntity> buffer){
        HtmlChapter result = new HtmlChapter(name, buffer);
        result.addHeaderFooter();
        return result;
    }
    
    /**
     * <p>Returns {@link #filename filename}.</p>
     * @return {@link #filename filename}
     */
    public String getName(){
        return filename;
    }
    
    /**
     * <p>Adds a pair of anchor tags to this HtmlChapter around based on the AnchorInfo {@code a}. The
     * added Tags are placed around the {@link Location#index word-index}-th word in this HtmlChapter,
     * specified by {@code a}'s {@link AnchorInfo#position position} after the word at that position
     * is verified as the first word of {@code a}'s {@link AnchorInfo#text phrase}. The link added
     * via these tags links to {@code a}'s {@link AnchorInfo#linkTo destination}.</p>
     * @param a an AnchorInfo specifying everything needed to create a link from one repeated phrase
     * in this HtmlChapter to the same repeated phrase in another HtmlChapter chapter.
     */
    private void addAnchor(AnchorInfo a){
        int wordIndex = a.position().getIndex();
        
        validateWordWithIndex(firstWord(a.phrase()), wordIndex);
        
        List<Integer> insertPoints = anchorInsertionPoints(wordIndex);
        insertPoints.sort((i1,i2) -> i2.compareTo(i1)); //sort into reverse order
        
        Tag open = new Tag(a.openingTagText());
        Tag close = new Tag(a.closingTagText());
        //add close tag on even indices (e.g. rightmost insert position) 
        //and add open tag on odd indices.
        Function<Integer,Tag> nextTag = 
                (i) -> i % 2 == 0 
                        ? close 
                        : open; 
        
        for(int i = 0; i < insertPoints.size(); i++){
            content.add(insertPoints.get(i), nextTag.apply(i));
            modCount++;
        }
    }
    
    /**
     * <p>Throws an exception if the {@code wordIndex}-th word in this HtmlChapter is not
     * {@code word}.</p>
     * @param word the word whose status as the {@code wordIndex}-th word in this HtmlChapter is to be
     * verified.
     * @param wordIndex the number of words prior to the word in this HtmlChapter compared against
     * {@code word}, starting counting after the chapter title.
     * @throws IllegalStateException if the {@code wordIndex}-th word in this HtmlChapter is not
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
      * <p>Replaces the element in the underlying list at index {@code position} with
      * {@code elem}.</p>
      * @param position the index in the underlying list at which {@code elem} is placed
      * @param elem the HTMLEntity put into the list at index {@code position}
      * @return the element originally at index {@code position}
      */
    HtmlEntity set(int position, HtmlEntity elem){
        modCount++;
        return content.set(position, elem);
    }
    
     /**
      * <p>Returns the {@code wordIndex}-th (zero-based) word after the chapter's title in this
      * HtmlChapter. The first word after the title has wordIndex 0.</p> <p>The wordIndex-th word is
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
      * HtmlChapter
      */
    private String wordAt(int wordIndex){
        StringBuilder result = new StringBuilder();

        int[] bounds = getWordBounds(wordIndex);
        
        for(int i = bounds[0]; i < bounds[1]; i++){
            HtmlEntity item = content.get(i);
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
                    HtmlChapter::isCharacter, 
                    Direction.PREV, 
                    Tag.class::isInstance) //htmlFile.get(i) is a character preceded by a tag.
                    
                    //htmlFile.get(i) is a Tag preceded by a character.
                    || is(i, 
                            Tag.class::isInstance, 
                            Direction.PREV, 
                            HtmlChapter::isCharacter)){
                result.add(i);
            }
        }
        result.add(hi);
        
        return result;
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
            Predicate<HtmlEntity> test1, 
            Direction dir, 
            Predicate<HtmlEntity> test2){
        
        HtmlEntity item = content.get(position);
        HtmlEntity prevOrNext = content.get(dir.apply(position));
        return test1.test(item) && test2.test(prevOrNext);
    }
    
    /**
     * <p>Returns the first space-delimited word of {@code phrase}.</p>
     * @param phrase a phrase of which the first word is returned
     * @return the first space-delimited word of {@code phrase}.
     */
    private static final String firstWord(String phrase){
        int index = phrase.indexOf(Phrase.WORD_SEPARATOR);
        return index < 0 
                ? phrase 
                : phrase.substring(0, index);
    }
    
    /**
     * <p>Returns the index in the underlying list of the {@code wordIndex}th word in the file after
     * the title.</p>
     * @param wordIndex the number of words between the sought word in this file and the first word
     * in the body of this chapter
     * @return the index in the underlying list of the {@code wordIndex}th word in the file after
     * the title
     */
    private int getWord(int wordIndex){
        return getWordCache.applyAsInt(wordIndex);
    }
    
    private class GetWordCache implements IntUnaryOperator{
        
        /**
         * <p>When this record of how many times this HtmlChapter has been modified is different from
         * the {@link HtmlChapter#modCount value} stored in this HtmlChapter itself, a result is
         * determined under worst-case scenario conditions, with the search beginning from the start
         * of the file.</p>
         */
        private int modCount = HtmlChapter.this.modCount;
        
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
         * @return the index in this HtmlChapter's underlying list of the first letter of the
         * {@code wordIndex}th word in the body of this chapter.
         * @throws IllegalArgumentException if {@code wordIndex} is less than
         * {@link HtmlChapter#baseWordIndex baseWordIndex}
         * @throws IllegalStateException if {@code wordIndex} is too high such that there aren't
         * enough words in this HtmlChapter to count that high
         */
        public int applyAsInt(int wordIndex){
            if(wordIndex < 0){
                throw new IllegalArgumentException(
                        "wordIndex " + wordIndex + " < 0");
            } else if(this.modCount == HtmlChapter.this.modCount && wordIndex == storedWordIndex){
                return storedWordPointer;
            } else{
                int previousWordIndex = -1;
                int init_i = 0;
                
                if(this.modCount == HtmlChapter.this.modCount){
                    if(wordIndex > storedWordIndex){
                        init_i = storedWordPointer;
                        previousWordIndex = storedWordIndex-1;
                    }
                } else{
                    this.modCount = HtmlChapter.this.modCount;
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
     * @param startPoint a position in (typically at the very start of) the {@code wordIndex}-th
     * word, from which to start looking for the end of the current word
     * @return the position in the underlying list of the last character of the {@code wordIndex}-th
     * word in the body of this chapter
     */
    private int getLastCharacter(int wordIndex, int startPoint){
        for(int i = startPoint; i < content.size(); i++){
            if(!isWord(adjacentElement(i, Direction.NEXT, HtmlChapter::isCharacter))){
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
    private int[] getWordBounds(int wordIndex){
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
    private boolean isWordStart(int index){
        return isWord(content.get(index)) 
                && !isWord(adjacentElement(index, Direction.PREV, HtmlChapter::isCharacter));
    }
    
    /**
     * <p>Returns true if {@code elem} is character-type and is a word character, false
     * otherwise.</p>
     * @param elem the HTMLEntity to be assessed for legality as a word character
     * @return true if {@code elem} is character-type and is a word character, false otherwise
     */
    private static boolean isWord(HtmlEntity elem){
        return CharLiteral.class.isInstance(elem) 
                && Phrase.isPhraseChar(((CharLiteral)elem).c);
    }
    
    /**
     * <p>Evaluates to true if the specified HTMLEntity {@code h} is a character-type HTMLEntity: a
     * {@link CharLiteral Ch} or a {@link CharCode Code}.</p>
     */
    private static boolean isCharacter(HtmlEntity h){
        return CharLiteral.class.isInstance(h) || CharCode.class.isInstance(h);
    }
    
    /**
     * <p>Returns the position in the underlying list of the element nearest to but not at
     * {@code position} in the direction (before or after) specified by {@code direction} for which
     * {@code condition} evaluates to true.</p>
     * @param startPosition the pre-starting position for this operation. One less than the starting
     * point if {@code direction} is {@code Direction.NEXT}, or one more if it's
     * {@code Direction.PREV}.
     * @param condition a Predicate whose evaluation to true causes this method to return its
     * current position in the underlying list
     * @param direction this method's direction of traversal of the underlying list
     * @return the position in the underlying list of the element nearest to but not at
     * {@code position} in the direction (before or after) specified by {@code direction} for which
     * {@code condition} evaluates to true
     */
    int adjacentElement(
            int startPosition, 
            Predicate<HtmlEntity> condition, 
            Direction direction){
        
        for(int i = direction.apply(startPosition); 
                direction.crawlTest(i, content);
                i = direction.apply(i)){
            if(condition.test(content.get(i))){
                return i;
            }
        }
        return BEFORE_BEGINNING;
    }
    
    private HtmlEntity adjacentElement(
            int position, 
            Direction direction, 
            Predicate<HtmlEntity> typeRestriction){
        
        int index = adjacentElement(position, typeRestriction, direction);
        return index >= 0 
                ? content.get(index) 
                : null;
    }
    
    private static final int BEFORE_BEGINNING = -1;
    
    @Override
    public HtmlChapter clone(){
        return new HtmlChapter(filename, content);
    }
    
    private void addHeaderFooter(){
        List<HtmlEntity> head = header();
        List<HtmlEntity> foot = footer();
        List<HtmlEntity> newContent = new ArrayList<>(head.size() + content.size() + foot.size());
        newContent.addAll(head);
        newContent.addAll(content);
        newContent.addAll(foot);
        
        content = newContent;
    }
    
    private static final List<HtmlEntity> HEADER_FRONT_HTML = new ArrayList<>();
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
    
    private static final List<HtmlEntity> HEADER_BACK_HTML = new ArrayList<>();
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
        
        HEADER_BACK_HTML.addAll(CharLiteral.NEW_LINE_LITERAL);
    }
    
    private List<HtmlEntity> header(){
        List<CharLiteral> name = CharLiteral.asList(chapterName());
        List<HtmlEntity> result = new ArrayList<>(
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
    
    private static final List<HtmlEntity> FOOTER_FRONT_HTML = new ArrayList<>();
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
    
    private static final List<HtmlEntity> FOOTER_BACK_HTML = new ArrayList<>();
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
    
    private List<HtmlEntity> footer(){
        List<CharLiteral> name = CharLiteral.asList(chapterName());
        List<HtmlEntity> result = new ArrayList<>(
                FOOTER_BACK_HTML.size() + name.size() + FOOTER_FRONT_HTML.size());
        result.addAll(FOOTER_FRONT_HTML);
        result.addAll(name);
        result.addAll(FOOTER_BACK_HTML);
        return result;
    }
    
    @Override
    public Iterator<HtmlEntity> iterator(){
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
                if(!Phrase.isPhraseChar(entityText.charAt(j))){
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
    
    public HtmlChapter link(List<AnchorInfo> anchors){
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
        Predicate<HtmlEntity> isAnchorWithMatchID = 
                (h) -> isAnchorWithMatchID(h, idValue, idAttrib);
        int pointer = INIT_POINTER;
        while(INIT_POINTER 
                != (pointer = adjacentElement(pointer, isAnchorWithMatchID, Direction.NEXT))){
            
            String tag = content.get(pointer).toString();
            tag = tag.substring(Tag.START.length(), tag.length() - Tag.END.length());
            
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
    
    private static boolean isAnchorWithMatchID(HtmlEntity h, String idValue, String idAttrib){
        if(Tag.class.isInstance(h)){
            Tag t = (Tag) h;
            return t.isType(Tag.A) && idValue.equals(t.valueOfAttribute(idAttrib)); 
        }
        return false;
    }
}
