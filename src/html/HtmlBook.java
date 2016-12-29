package html;

import common.BookData;
import common.IO;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import text.Phrase;

/**
 * <p>Represents an HTML file and provides some convenience methods for working with an HTML
 * file.</p>
 */
public class HtmlBook extends HtmlFile{
    
    /**
     * <p>The literal filename of the file to which the content of this HtmlBook belongs. Contains
     * an extension and possibly a folder reference.</p>
     */
    private final File source;
    
    /**
     * <p>Constructs an HtmlBook representing the contents of the File {@code f}. Calls
     * {@link #HtmlBook(String,Scanner) this(String,Scanner)} using
     * {@link java.io.File.getName() the file's name} and a new Scanner of {@code f}.</p>
     * @param source the File whose contents go into this HtmlBook
     * @throws FileNotFoundException if {@code f} does not exist or cannot be read
     */
    public HtmlBook(File source) throws FileNotFoundException{
        this(
                source, 
                new Scanner(
                        readFile(new Scanner(source, IO.ENCODING))
                        .toString()));
    }
    
    /**
     * <p>Constructs an HtmlBook that believes its filename is {@code name} and which gets the
     * literal text it turns into HTMLEntitys via {@code scan}.</p>
     * @param name the filename that this HtmlBook uses to determine information about itself
     * assuming that the filename is structured the way that the chapter files are split by
     * {@link operate.SplitChapters SplitChapters}
     * @param scan the Scanner used to obtain literal text to parse into HTMLEntitys
     */
    private HtmlBook(File source, Scanner scan) {
        super(getHtmlBookContent(scan));
        this.source = source;
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
    private boolean hasLiteralAt(String literal, int index){
        for(int i = 0; i < literal.length(); i++){
            char c = literal.charAt(i);
            if(!content.get(index).match(c)){
                return false;
            }
            index = adjacentElement(index, HtmlBook::isCharacter, Direction.NEXT);
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
    private boolean hasLiteralBetween(String literal, int start, int end){
        OptionalInt oi = IntStream.range(start + 1, end - literal.length())
                .filter((i) -> hasLiteralAt(literal, i))
                .findFirst();
        return oi.isPresent();
    }
    
    private int adjacentElement(
            Predicate<Integer> condition, 
            Direction direction, 
            int startPosition){
        
        for(int i = direction.apply(startPosition); 
                direction.crawlTest(i, content); 
                i = direction.apply(i)){
            if(condition.test(i)){
                return i;
            }
        }
        return BEFORE_BEGINNING;
    }
    
    /**
     * <p>Removes from the underlying list all elements in the region bounded by {@code start} and
     * {@code end}.</p>
     * @param start the inclusive lower bound of the region to be removed from the underlying list
     * @param end the exclusive upper bound of the region to be removed from the underlying list
     */
    private void removeAll(int start, int end){
        List<HtmlEntity> front = content.subList(0,start);
        List<HtmlEntity> back = content.subList(end, content.size());
        front.addAll(back);
        content = new ArrayList<>(front);
        modCount++;
    }
    
    /**
     * <p>Removes all content from the underlying list at or after the position {@code start}. This
     * works like {@link java.lang.String#substring(int) substring}, but in reverse.</p>
     * @param start the inclusive lower bound of the region of the underlying list to be removed
     */
    private void removeAll(int start){
        content = new ArrayList<>(content.subList(0, start));
        modCount++;
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
    private int closingMatch(int startPoint){
        HtmlEntity a = content.get(startPoint);
        if(!Tag.isOpen(a)){
            throw new IllegalArgumentException(
                    "The element at index " + startPoint 
                    + " (\"" + a 
                    + "\") is not an opening tag.");
        }
        Tag t = (Tag) a;
        
        final String type = t.getType();
        final Predicate<HtmlEntity> isTagOfType = 
                (h) -> Tag.class.isInstance(h) && ((Tag)h).getType().equals(type);
        int tagIndex = startPoint;
        
        for(int depth = 1; depth > 0 && 0 <= tagIndex && tagIndex < content.size() - 1;){
            tagIndex = adjacentElement(tagIndex, isTagOfType, Direction.NEXT);
            Tag someTag = (Tag) content.get(tagIndex);
            depth += someTag.isOpening() 
                    ? INCREASE_DEPTH 
                    : DECREASE_DEPTH;
        }
        return tagIndex;
    }
    
    private static final int INCREASE_DEPTH = 1;
    private static final int DECREASE_DEPTH = -1;
    
    /**
     * <p>Returns a {@literal List<HTMLEntity>} representing the contents of the body scanned by
     * {@code s}. Each HTML tag, whether opening or closing, gets its own element in this list. Each
     * HTML character code (&...;) does, too, as does each literal character not part of a tag or a
     * code.<p> <p>The type of the List returned is
     * {@link repeatedphrases.ArrayList2 ArrayList2}.</p>
     * @param s a Scanner that produces the literal text of an HTML file to be rendered as an
     * {@code HtmlBook} in memory
     * @return a {@literal List<HTMLEntity>} representing the contents of the body scanned by
     * {@code s}
     */
    private static ArrayList<HtmlEntity> getHtmlBookContent(Scanner s){
        ArrayList<HtmlEntity> result = new ArrayList<>();
        
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
                HtmlEntity newEntry = (mate == Tag.END_CHAR) 
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
    private static Character risingCounterpart(Character c){
        switch(c){
            case Tag.START_CHAR : return Tag.END_CHAR;
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
    private List<HtmlEntity> section(int[] bounds){
        return section(bounds[0], bounds[1]);
    }
    
    private List<HtmlEntity> section(int lo, int hi){
        return content.subList(lo, hi);
    }
    
    private List<HtmlEntity> section(int lo){
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
    
    private static boolean isParagraphishOpen(HtmlEntity h){
        return Tag.isPOpen(h) || Tag.isHeaderOpen(h);
    }
    
    /**
     * <p>A utility class that crawls the list of HTMLEntity that underlies this HtmlBook and
     * locates entire paragraph blocks.</p>
     */
    private class ParagraphIterator implements Iterator<int[]>{
        
        private final int modCount;
        
        /**
         * <p>This ParagraphIterator's position in the list underlying this HtmlBook.</p>
         */
        private int position;
        
        /**
         * <p>Constructs a ParagraphIterator that works on {@code HtmlBook.this.content}.</p>
         */
        private ParagraphIterator(){
            position = BEFORE_BEGINNING;
            modCount = HtmlBook.this.modCount;
        }
        
        @Override
        /**
         * <p>Returns true if this ParagraphIterator has another paragraph block available in the
         * list underlying this HtmlBook, false otherwise.</p>
         * @return true if this ParagraphIterator has another paragraph block available in the list
         * underlying this HtmlBook, false otherwise.
         */
        public boolean hasNext(){
            concurrentModificationCheck();
            return BEFORE_BEGINNING != adjacentElement(
                    position, 
                    HtmlBook::isParagraphishOpen, 
                    Direction.NEXT);
        }
        
        @Override
        /**
         * <p>Returns an int array of length 2, containng the inclusive lower bound and exclusive
         * upper bound in this HtmlBook's underlying list of the opening and closing tags of the
         * next paragraph.</p>
         * @return an int array of length 2, containng the inclusive lower bound and exclusive upper
         * bound in this HtmlBook's underlying list of the opening and closing tags of the next
         * paragraph.
         */
        public int[] next(){
            concurrentModificationCheck();
            int start = adjacentElement(position, HtmlBook::isParagraphishOpen, Direction.NEXT);
            int end = closingMatch(start);
            int[] result = {start, end+1};
            position = end;
            return result;
        }
        
        /**
         * <p>Throws a ConcurrentModificationException if the underlying HtmlBook was modified after
         * this ParagraphIterator was constructed.</p>
         * @throws ConcurrentModificationException if the underlying HtmlBook was modified after
         * this ParagraphIterator was constructed
         */
        private void concurrentModificationCheck(){
            if(this.modCount != HtmlBook.this.modCount){
                throw new ConcurrentModificationException(
                        "Mismatch between " 
                        + "ParagraphIterator.this.modCount and HtmlBook.this.modCount: (" 
                        + this.modCount + " != " + HtmlBook.this.modCount + ")");
            }
        }
    }
    
    public Collection<HtmlChapter> cleanAndSplit(){
        for(Operation op : Operation.values()){
            if(!op.isDone(this)){
                op.operate(this);
            }
        }
        return splitChapters();
    }
    
    private boolean newlinePDone = false;
    private boolean clearExcessStructureDone = false;
    private boolean clearFrontAndBackMatterDone = false;
    private boolean swapApostrophesDone = false;
    
    private static enum Operation{
        NEWLINE_P(
                (h) -> h.newlinePDone, 
                HtmlBook::newlineP, 
                (h) -> h.newlinePDone = true), 
        CLEAR_XS_STRUCT(
                (h) -> h.clearExcessStructureDone, 
                HtmlBook::clearExcessStructure, 
                (h) -> h.clearExcessStructureDone = true), 
        CLEAR_FRONT_BACK_MATTER(
                (h) -> h.clearFrontAndBackMatterDone, 
                HtmlBook::clearFrontAndBackMatter, 
                (h) -> h.clearFrontAndBackMatterDone = true),
        SWAP_APOSTROPHES(
                (h) -> h.swapApostrophesDone, 
                HtmlBook::swapApostrophes, 
                (h) -> h.swapApostrophesDone = true);
        
        private final Predicate<HtmlBook> test;
        private final Consumer<HtmlBook> operation;
        private final Consumer<HtmlBook> setter;
        
        private Operation(
                Predicate<HtmlBook> test, 
                Consumer<HtmlBook> operation, 
                Consumer<HtmlBook> setter){
            
            this.test = test;
            this.operation = operation;
            this.setter = setter;
        }
        
        boolean isDone(HtmlBook h){
            return test.test(h);
        }
        
        void operate(HtmlBook h){
            operation.accept(h);
            setter.accept(h);
        }
    }
    
    private void newlineP(){
        List<HtmlEntity> newContent = new ArrayList<>(content.size());
        for(HtmlEntity h : content){
            if(Tag.isPOpen(h)){
                newContent.addAll(CharLiteral.NEW_LINE_LITERAL);
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
                .filter(Tag::notDiv)
                .filter(Tag::notBlockquote)
                .filter(Tag::notImg)
                .filter(CharCode::notNbsp)
                .collect(Collectors.toList());
    }
    
    /**
     * returns a list of the indices in this HtmlBook of the opening paragraph tag of an empty 
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
        String firstWords = NOVEL_FIRST_WORDS.get(source.getName());
        Predicate<Integer> hasFirstWordsAt = (i) -> hasLiteralAt(firstWords, i);
        
        int chapterStartIndex = adjacentElement(hasFirstWordsAt, Direction.NEXT, BEFORE_BEGINNING);
        
        Predicate<Integer> isPrologueBlock = 
                (i) -> isParagraphishOpen(content.get(i)) 
                        && hasLiteralBetween("PROLOGUE", i, closingMatch(i));
        int pLocation = adjacentElement(isPrologueBlock, Direction.PREV, chapterStartIndex);
        
        return pLocation - 1; //MAGIC
    }
    
    private int backMatterStart(){
        String lastWords = NOVEL_LAST_WORDS.get(source.getName());
        
        Predicate<Integer> hasLastWordsAt = (i) -> hasLiteralAt(lastWords, i);
        
        int textIndex = adjacentElement(hasLastWordsAt, Direction.PREV, content.size());
        int pIndex = adjacentElement(textIndex, HtmlBook::isParagraphishOpen, Direction.NEXT);
        
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
        String firstWords = NOVELLA_FIRST_WORDS.get(source.getName());
        Predicate<Integer> predicate = (i) -> hasLiteralAt(firstWords, i);
        
        int literalIndex = adjacentElement(predicate, Direction.NEXT, BEFORE_BEGINNING);
        
        return adjacentElement(literalIndex, Tag::isPOpen, Direction.PREV);
    }
    
    /**
     * <p>Returns the index in {@code file} of the closing "p" tag of the last paragraph that ends
     * with the {@link #lastWords(String) last words} of the specified ASOIAF novella.</p>
     * @param file
     * @param novella
     * @return
     */
    private int lastWordsP(){
        String lastWords = NOVELLA_LAST_WORDS.get(source.getName());
        Predicate<Integer> predicate = (i) -> hasLiteralAt(lastWords, i);
        
        int literalIndex = adjacentElement(predicate, Direction.PREV, content.size());
        
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
        private static final char WORD_CHAR = '*';
        
        /**
         * <p>Used in a string sent to ApoPattern's constructor, this represents any
         * {@link #isWordChar(Character) non-word character}. It is an ampersand: {@value}</p>
         */
        private static final char NON_WORD_CHAR = '&';
        
        /**
         * <p>Used in a string sent to ApoPattern's constructor, this represents any
         * {@link #isAlphabetical(Character) alphabetic character}. It is an at sign: {@value}</p>
         */
        private static final char ALPHA_CHAR = '@';
        
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
        
        private boolean match(HtmlBook h, int index){
            return CharLiteral.RIGHT_SINGLE_QUOTE.equals(h.content.get(index)) 
                    && Stream.of(Side.values())
                            .allMatch((side) -> side.match(ApostrophePattern.this, h, index));
        }
        
        private static enum Side{
            BEFORE((ap) -> ap.before, (i) -> i - 1), 
            AFTER ((ap) -> ap.after,  (i) -> i + 1);
            
            private final Function<ApostrophePattern, List<Character>> listFunc;
            private final IntUnaryOperator nextInt;
            
            private Side(
                    Function<ApostrophePattern, List<Character>> listFunc, 
                    IntUnaryOperator nextInt){
                
                this.listFunc = listFunc;
                this.nextInt = nextInt;
            }
            
            boolean match(ApostrophePattern pattern, HtmlBook h, int a){
                for(char c : listFunc.apply(pattern)){
                    a = nextInt(a, h);
                    if(!ApostrophePattern.match(c, characterAt(a, h))){
                        return false;
                    }
                }
                return true;
            }
            
            private int nextInt(int a, HtmlBook h){
                do{
                    a = nextInt.applyAsInt(a);
                    
                    //the while test will throw an exception if a is not in range, but a is tested 
                    //for range in Side.match() before being sent to this method; so, that problem 
                    //should never occur
                } while(!(h.content.get(a) instanceof CharLiteral));
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
        private static Character characterAt(int index, HtmlBook h){
            if(0 <= index && index < h.content.size()){
                HtmlEntity ent = h.content.get(index);
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
    
    private static final Map<String, String> NOVEL_FIRST_WORDS = 
            BookData.words(BookData::isNovel, BookData::getFirstWords);
    
    private static final Map<String, String> NOVEL_LAST_WORDS = 
            BookData.words(BookData::isNovel, BookData::getLastWords);
    
    private static final Map<String, String> NOVELLA_FIRST_WORDS = 
            BookData.words(BookData::isNovella, BookData::getFirstWords);
    
    private static final Map<String, String> NOVELLA_LAST_WORDS = 
            BookData.words(BookData::isNovella, BookData::getLastWords);
    
    private Collection<HtmlChapter> splitChapters(){
        return BookData
                .valueOf(
                        source
                        .getName()
                        .substring(0, source.getName().length() - IO.HTML_EXT.length()))
                .getChapterizer()
                .apply(this);
    }
    
    public static Collection<HtmlChapter> chapterizeNovel(HtmlBook novel){
        List<HtmlChapter> result = new ArrayList<>();
        
        List<HtmlEntity> buffer = new ArrayList<>();
        String chapterName = null;
        
        int writeCount = 0;
        
        for(
                Iterator<int[]> piter = novel.new ParagraphIterator(); 
                piter.hasNext();){
            
            int[] paragraphBounds = piter.next();
            
            List<HtmlEntity> paragraph = novel.section(paragraphBounds);
            
            if(isTitleParagraph(paragraph)){
                if(!buffer.isEmpty()){
                    //dump the buffer
                    result.add(HtmlChapter.fromBuffer(
                            novel.chapterFileName(writeCount, chapterName), 
                            buffer));
                    writeCount++;
                }
                
                //new buffer
                chapterName = extractChapterTitle(paragraph);
                buffer = new ArrayList<>();
            } else{
                buffer.addAll(paragraph);
                buffer.addAll(CharLiteral.NEW_LINE_LITERAL);
            }
        }
        
        //reached end of file
        //dump the buffer to a file
        result.add(HtmlChapter.fromBuffer(
                novel.chapterFileName(writeCount, chapterName), 
                buffer));
        
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
    private String chapterFileName(int chapterIndex, String chapterName){
        String bookName = IO.stripExtension(source.getName());
        return bookName 
                + IO.FILENAME_COMPONENT_SEPARATOR_CHAR 
                + chapterIndex 
                + IO.FILENAME_COMPONENT_SEPARATOR_CHAR 
                + chapterName.replace(' ', IO.FILENAME_COMPONENT_SEPARATOR_CHAR) 
                + IO.HTML_EXT;
    }
    
    /**
     * <p>Returns true if the {@code paragraph}'s only character-type contents are characters that
     * can appear in chapter titles, false otherwise.</p>
     * @param paragraph a list of HTMLEntity, a piece of an HtmlBook
     * @return true if the {@code paragraph}'s only character-type contents are characters that can
     * appear in chapter titles, false otherwise
     */
    private static boolean isTitleParagraph(List<HtmlEntity> paragraph){
        boolean titleCharFound = false;
        
        for(HtmlEntity h : paragraph){
            if(CharCode.class.isInstance(h)){
                return false;
            } else if(CharLiteral.class.isInstance(h)){
                if(isLegalChapterTitleCharacter(((CharLiteral) h).c)){
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
    private static boolean isLegalChapterTitleCharacter(char c){
        return ('A' <= c && c <= 'Z') || c == ' ' || c == '\'';
    }
    
    /**
     * <p>Extracts a chapter's title from a {@code paragraph}
     * {@link #isTitleParagraph(List<HTMLEntity>) containing a chapter title}.</p>
     * @param paragraph the paragraph whose contained chapter title is extracted and returned
     * @return the chapter title that's the sole visible content of the specified {@code paragraph}
     */
    private static String extractChapterTitle(List<HtmlEntity> paragraph){
        StringBuilder result = new StringBuilder(paragraph.size());

        for(HtmlEntity h : paragraph){
            if(CharLiteral.class.isInstance(h)){
                result.append(((CharLiteral)h).c);
            }
        }

        return result.toString();
    }
    
    public static Collection<HtmlChapter> chapterizeNovella(HtmlBook novella){
        return new ArrayList<>(
                Arrays.asList(
                        HtmlChapter.fromBuffer(
                                novella.source.getName(), 
                                novella.content)));
    }
    
    public static Collection<HtmlChapter> chapterizePQ(HtmlBook pq){
        HtmlChapter[] files;
        {
            HtmlChapter body;
            {
                int footnoteIndex = pq.adjacentElement(
                        (i) -> pq.hasLiteralAt("Footnote",i), Direction.PREV, pq.content.size());
                int bodyEndIndex = pq.adjacentElement(footnoteIndex, Tag::isPOpen, Direction.PREV);
                List<HtmlEntity> bodySection = pq.section(0,bodyEndIndex);
                body = HtmlChapter.fromBuffer("PQ_0_THE_PRINCESS_AND_THE_QUEEN.html", bodySection);
            }
            
            HtmlChapter footnote;
            {
                int footnoteStart = pq.adjacentElement(
                        pq.content.size(), 
                        Tag::isPOpen, 
                        Direction.PREV);
                List<HtmlEntity> footnoteSection = pq.section(footnoteStart);
                footnote = HtmlChapter.fromBuffer("PQ_1_FOOTNOTE.html", footnoteSection);
            }
            
            files = new HtmlChapter[]{
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
                    HtmlChapter file = files[i];
                    String href = hrefs[i];
                    
                    //replace superscript 1 with asterisk
                    int noteIndex = file.adjacentElement(
                            BEFORE_BEGINNING, 
                            Tag::isSup, 
                            Direction.NEXT);
                    noteIndex = file.adjacentElement(noteIndex, CharLiteral::is1, Direction.NEXT);
                    file.set(noteIndex, new CharLiteral('*'));
                    
                    //replace internal link with external link
                    int noteAnchorIndex = file.adjacentElement(
                            noteIndex, 
                            Tag::isAnchorOpen, 
                            Direction.PREV);
                    file.set(
                            noteAnchorIndex, 
                            new Tag("a id=\"FOOTNOTE\" href=\"" + href + "\""));
                });
        
        return new ArrayList<>(Arrays.asList(files));
    }
    
    private static final int PQ_FINAL_FILE_COUNT = 2;
}
