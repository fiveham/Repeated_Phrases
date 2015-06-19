package repeatedphrases;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Scans each specified file looking for single-quote characters, 
 * and replaces them with proper apostrophes ("\'") if the 
 * characters around the single-quote match any of several PATTERNS.
 */
public class SwapApostrophes{

    /**
     * <p>The folder from which this class reads html novels files 
     * whose apostrophes it will correct.</p>
     * @see Folder#HTML_BOOKS_CHAPTER_CORE
     */
    public static final Folder READ_FROM = Folder.HTML_BOOKS_CHAPTER_CORE;

    /**
     * <p>The folder where this class writes the modified html novel 
     * files it creates.</p>
     * @see Folder#HTML_BOOKS_CORRECT_APOSTROPHES
     */
    public static final Folder WRITE_TO = Folder.HTML_BOOKS_CORRECT_APOSTROPHES;

    /**
     * <p>The apostrophe character ({@value}), used in 
     * {@link #ApoPattern apostrophe-replacement context codes} 
     * and as the literal apostrophe to install in place of right 
     * single quote apostrophes that match a replacement pattern.</p>
     */
    public static final char APOSTROPHE = '\'';

    /**
     * <p>The right single quote conventionally used as an apostrophe as 
     * well as as a closing quote in nested quotes. To be replaced in the 
     * corpus at certain places with an {@link #APOSTROPHE apostrophe}.</p>
     */
    public static final char RIGHT_SINGLE_QUOTE = '\u2019';

    public static void main(String[] args){
        swapApostrophes(IO.DEFAULT_MSG);
    }

    /**
     * <p>Detects all the ASOIAF novel files in {@link #READ_FROM READ_FROM}, 
     * reads them line by line, finds all the right single quotes, and replaces 
     * those that fit any {@link #PATTERNS replacement pattern} with an 
     * {@link #APOSTROPHE apostrophe}.</p>
     * @param args command-line arguments (unused)
     */
    public static void swapApostrophes(Consumer<String> msg){
        File[] readUs = READ_FROM.folder().listFiles( IO.IS_HTML );

        for(File srcFile : readUs){
            msg.accept("Normalizing apostrophes: "+srcFile.getName());

            try(OutputStreamWriter out = IO.newOutputStreamWriter( WRITE_TO.folderName() + File.separator + srcFile.getName() );){
                List<String> lines = IO.fileContentsAsList(srcFile, IO.NEXT_LINE, IO.SCANNER_HAS_NEXT_LINE);

                for( String l : lines ){
                    StringBuilder line = new StringBuilder( l );
                    List<Integer> singleQuoteIndices = singleQuoteIndices(line);
                    for(Integer index : singleQuoteIndices){

                        if( shouldChangeCharacter(line, index)){
                            line.setCharAt(index, APOSTROPHE);
                        }
                    }
                    out.write(line.toString() + IO.NEW_LINE);
                }
                
                out.close();
            } catch(IOException e){}
        }
    }

    /**
     * <p>Returns true if the text at and around <code>index</code> in 
     * <code>line</code> matches any of the {@link #ApoPattern patterns} 
     * in {@link #PATTERNS PATTERNS}, false otherwise.</p>
     * @param line a line that has a right single quote at <code>index</code>
     * @param index the location in <code>line</code> of a right single 
     * quote
     * @return true if the text at and around <code>index</code> in 
     * <code>line</code> matches any of the {@link #ApoPattern patterns} 
     * in {@link #PATTERNS PATTERNS}, false otherwise
     */
    private static boolean shouldChangeCharacter(StringBuilder line, int index){
        for( ApoPattern pattern : PATTERNS){
            if(pattern.match(line, index)){
                return true;
            }
        }
        return false;
    }

    /**
     * <p>All the conditions according to which a right single 
     * quote may be changed to an apostrophe.</p>
     * </p>The vast majority of these are plural possessives 
     * quoted from the text of ASOIAF.</p>
     */
    private static final List<ApoPattern> PATTERNS = new ArrayList<>();
    static{
        PATTERNS.add(new ApoPattern("@'@"));	//everything from I'm to shouldn't've
        PATTERNS.add(new ApoPattern("&o'&"));	//of
        PATTERNS.add(new ApoPattern("&t'&"));	//to
        PATTERNS.add(new ApoPattern("&'*"));	//'Yaya, the 'bite, 'bout, 'cat (shadowcat), 'tis, 'twas, 'twixt, 'prentice, 'em, 'ud
        PATTERNS.add(new ApoPattern("&ha'&"));	//have, usually "gods have mercy"
        PATTERNS.add(new ApoPattern("&f'&"));	//for
        PATTERNS.add(new ApoPattern("&a'&"));	//at, as pronounced by some wildlings
        PATTERNS.add(new ApoPattern("traitors' graves"));
        PATTERNS.add(new ApoPattern("traitors' collars"));
        PATTERNS.add(new ApoPattern("traitors' heads"));	//"@@@ traitors' @@@@@"
        PATTERNS.add(new ApoPattern("wolves' work"));
        PATTERNS.add(new ApoPattern("wolves' heads"));	//"@@@ wolves' @@@@"
        PATTERNS.add(new ApoPattern("rams' heads"));
        PATTERNS.add(new ApoPattern("lions' heads"));
        PATTERNS.add(new ApoPattern("lions' paws"));
        PATTERNS.add(new ApoPattern("lions' tails"));
        PATTERNS.add(new ApoPattern("&the alchemists' guild&"));
        PATTERNS.add(new ApoPattern("&the alchemists' vile&"));	//"&the alchemists' @@@@"
        PATTERNS.add(new ApoPattern("pyromancers' piss"));
        PATTERNS.add(new ApoPattern("@@@s' own&"));
        PATTERNS.add(new ApoPattern("&the @o@s' @@@@"));
        PATTERNS.add(new ApoPattern("&the boys' grandfather&"));
        PATTERNS.add(new ApoPattern("&the boys' heads&"));	//"@@@ the boys' @@@@@"
        PATTERNS.add(new ApoPattern("&merchants' sons&"));
        PATTERNS.add(new ApoPattern("&merchants' stalls&"));	//"&merchants' s@@@"
        PATTERNS.add(new ApoPattern("merchants' carts"));
        PATTERNS.add(new ApoPattern("the merchants' row"));
        PATTERNS.add(new ApoPattern("the merchants' wagons"));
        PATTERNS.add(new ApoPattern("&their mothers' @@@"));	//"&their mothers' @@@@@"
        PATTERNS.add(new ApoPattern("whores' skirts"));
        PATTERNS.add(new ApoPattern("&your brothers' @@@@"));
        PATTERNS.add(new ApoPattern("&my brothers' ghosts&"));	//"@@ brothers' @@@@"
        PATTERNS.add(new ApoPattern("&his brothers' @@@@"));
        PATTERNS.add(new ApoPattern("@@@@s' nest"));
        PATTERNS.add(new ApoPattern("horses' hooves"));
        PATTERNS.add(new ApoPattern("be keepin'&"));
        PATTERNS.add(new ApoPattern("is carryin'&"));	//"@@@@in' @@@"
        PATTERNS.add(new ApoPattern("@@@@ts' respite"));
        PATTERNS.add(new ApoPattern("years' remission"));
        PATTERNS.add(new ApoPattern("pigs' feet"));
        PATTERNS.add(new ApoPattern("calves' brains"));
        PATTERNS.add(new ApoPattern("servants' steps"));
        PATTERNS.add(new ApoPattern("servants' time"));
        PATTERNS.add(new ApoPattern("servants' corridor"));
        PATTERNS.add(new ApoPattern("lords' bannermen"));
        PATTERNS.add(new ApoPattern("lords' entrance"));
        PATTERNS.add(new ApoPattern("were lords' sons"));
        PATTERNS.add(new ApoPattern("&goats' milk&"));
        PATTERNS.add(new ApoPattern("&slavers' filth&"));
        PATTERNS.add(new ApoPattern("&slavers' pyramid&"));
        PATTERNS.add(new ApoPattern("&sailors' stor@"));
        PATTERNS.add(new ApoPattern("&sailors' temple&"));
        PATTERNS.add(new ApoPattern("&smugglers' cove&"));
        PATTERNS.add(new ApoPattern("&smugglers' stars&"));
        PATTERNS.add(new ApoPattern("&days' ride&"));
        PATTERNS.add(new ApoPattern("&days' food&"));
        PATTERNS.add(new ApoPattern("&days' sail&"));
        PATTERNS.add(new ApoPattern("&hours' ride&"));
        PATTERNS.add(new ApoPattern("&hours' sail&"));
        PATTERNS.add(new ApoPattern("bakers'&"));
        PATTERNS.add(new ApoPattern("@ mummers' @@@@"));
        PATTERNS.add(new ApoPattern("with strangers' eyes"));
        PATTERNS.add(new ApoPattern("their masters' business"));
        PATTERNS.add(new ApoPattern("the challengers' paddock"));
        PATTERNS.add(new ApoPattern("stoops' wife"));
        PATTERNS.add(new ApoPattern("&or one of the lannisters'&"));
        PATTERNS.add(new ApoPattern("ladies' cats"));
        PATTERNS.add(new ApoPattern("bastards' names"));
        PATTERNS.add(new ApoPattern("the rangers' search"));
        PATTERNS.add(new ApoPattern("their fathers' rusted swords"));
        PATTERNS.add(new ApoPattern("his cousins' eyes"));
        PATTERNS.add(new ApoPattern("maidens' judgments"));
        PATTERNS.add(new ApoPattern("a singers' tourney"));
        PATTERNS.add(new ApoPattern("a fools' joust"));
        PATTERNS.add(new ApoPattern("an outlaws' lair"));
        PATTERNS.add(new ApoPattern("rats' eyes"));
        PATTERNS.add(new ApoPattern("the wildlings' herds"));
        PATTERNS.add(new ApoPattern("dead friends' father"));
        PATTERNS.add(new ApoPattern("archers' stakes"));
        PATTERNS.add(new ApoPattern("heralds' trumpets"));
        PATTERNS.add(new ApoPattern("the climbers' rope"));
        PATTERNS.add(new ApoPattern("griffins' men"));
        PATTERNS.add(new ApoPattern("their masters' possessions"));
        PATTERNS.add(new ApoPattern("their neighbors' daughters"));
        PATTERNS.add(new ApoPattern("the musicians' gallery"));
        PATTERNS.add(new ApoPattern("kings' blood"));
        PATTERNS.add(new ApoPattern("the besiegers' cheers"));
        PATTERNS.add(new ApoPattern("gulls' eggs"));
        PATTERNS.add(new ApoPattern("the defenders' shouts"));
        PATTERNS.add(new ApoPattern("priests' song"));
        PATTERNS.add(new ApoPattern("heroes' tombs"));
        PATTERNS.add(new ApoPattern("some robbers' den"));
        PATTERNS.add(new ApoPattern("babies' bottoms"));
        PATTERNS.add(new ApoPattern("sound of lovers' footsteps"));
        PATTERNS.add(new ApoPattern("the murderers' secret"));
        PATTERNS.add(new ApoPattern("abandoned crofters' village"));
        PATTERNS.add(new ApoPattern("the diggers' eyes were"));
        PATTERNS.add(new ApoPattern("my sons' things"));
        PATTERNS.add(new ApoPattern("&lil'&"));
    }

    /**
     * <p>Represents a pattern of characters around an 
     * apostrophe, meant for use in determining which 
     * instances of a right single quote in the text 
     * of ASOIAF should be ordinary apostrophes instead.</p>
     */
    public static class ApoPattern{

        /**
         * <p>Used in a string sent to ApoPattern's constructor, 
         * this represents any {@link #isWordChar(Character) word character}. 
         * It is an asterisk: {@value}</p>
         */
        public static final char WORD_CHAR = '*';

        /**
         * <p>Used in a string sent to ApoPattern's constructor, 
         * this represents any {@link #isWordChar(Character) non-word character}. 
         * It is an ampersand: {@value}</p>
         */
        public static final char NON_WORD_CHAR = '&';

        /**
         * <p>Used in a string sent to ApoPattern's constructor, 
         * this represents any {@link #isAlphabetical(Character) alphabetic character}.
         * It is an at sign: {@value}</p>
         */
        public static final char ALPHA_CHAR = '@';

        /**
         * <p>A list of the characters from the string used to 
         * construct this ApoPatern prior to the apostrophe, 
         * in reverse order. For example, sending "ab'cd" to 
         * the constructor makes <code>before</code> equivalent 
         * to <code>before = new ArrayList<>(); before.add(new Character('b')); before.add(new Character('a'));</code></p>
         */
        private List<Character> before;

        /**
         * <p>A list of the characters from the string used to 
         * construct this ApoPatern after to the apostrophe. 
         * For example, sending "ab'cd" to 
         * the constructor makes <code>before</code> equivalent 
         * to <code>after = new ArrayList<>(); after.add(new Character('c')); after.add(new Character('d'));</code></p>
         */
        private List<Character> after;

        /**
         * <p>Constructs an ApoPattern based on the specified string.</p>
         * @param s a string containing an apostrophe used to specify 
         * characters around an apostrophe
         */
        public ApoPattern(String s){
            s = s.toLowerCase();
            int index = s.indexOf(APOSTROPHE);
            before = new ArrayList<>();
            after = new ArrayList<>();
            for(int i=index-1; i>=0; i--){
                before.add( s.charAt(i) );
            }
            for(int i=index+1; i<s.length(); i++){
                after.add( s.charAt(i) );
            }
        }

        /**
         * <p>Returns true if the content of <code>line</code> around 
         * position <code>index</code> matches this ApoPattern, 
         * false otherwise.</p>
         * @param line string in which to try to match this ApoPattern
         * @param index position in <code>line</code> around which 
         * to try to match this ApoPattern
         * @return true if the content of <code>line</code> around 
         * position <code>index</code> matches this ApoPattern, 
         * false otherwise
         */
        public boolean match(StringBuilder line, int index){
        	
            if( !isPossibleApostrophe(line.charAt(index)) ){
                return false;
            }
            List<IntChar> cleanLine = cleanLine(line);
            int cleanPointer = cleanPointer(cleanLine, index);
            int a = cleanPointer-1;
            for(char charBefore : before){
                if( !match(charBefore, characterAt(a, cleanLine))){
                    return false;
                }
                a--;
            }

            a = cleanPointer+1;
            for(char charAfter : after){
                if( !match(charAfter, characterAt(a, cleanLine))){
                    return false;
                }
                a++;
            }
            return true;
        }

        /**
         * <p>Returns the character at the specified <code>index</code> in 
         * <code>list</code> if <code>index</code> is within the bounds of 
         * <code>list</code>, null otherwise.</p>
         * @param index
         * @param list
         * @return
         */
        private Character characterAt(int index, List<IntChar> list){
            return 0<=index && index < list.size()
                    ? list.get(index).c
                    : null;
        }

        /**
         * <p>Returns the index in <code>cleanLine</code> at which the 
         * {@link IntChar#i index} of the element there equals <code>soughtIndex</code>.</p>
         * @param cleanLine an HTML-free representation of a literal line from 
         * an HTML file being processed
         * @param soughtIndex an index in the original HTML file line such that 
         * if the line's character from that point ended up in the HTML-free 
         * <code>cleanLine</code>, the position in <code>cleanLine</code> of 
         * the IntChar derived from that character in the literal line is returned
         * @return the index in the HTML-free <code>cleanLine</code> of the IntChar 
         * derived from the character at <code>soughtIndex</code> in the original, 
         * literal text line from an HTML file, or -1 if the character at 
         * <code>soughtIndex</code> in the literal HTML-file line is not represented 
         * in the HTML-free <code>cleanLine</code>.
         */
        private int cleanPointer(List<IntChar> cleanLine, int soughtIndex){
            for(int i=0; i<cleanLine.size(); i++){
                if(cleanLine.get(i).i==soughtIndex){
                    return i;
                }
            }
            return -1;
        }

        /**
         * <p>Creates a representation of a literal <code>line</code> from an 
         * HTML file such that {@link Tag Tags} and {@link Code Codes} are 
         * excluded.</p>
         * @param line a literal text line from an HTML file
         * @return  a representation of a literal <code>line</code> from an 
         * HTML file such that {@link Tag Tags} and {@link Code Codes} are 
         * excluded
         */
        private List<IntChar> cleanLine(StringBuilder line){
            List<IntChar> result = new ArrayList<>();

            Character mate = null; //stores '>' or ';' while iterating through an HTML tag or character code so we know when to stop skipping characters.
            for(int i=0; i<line.length(); i++){
                char c = line.charAt(i);

                if(mate==null){ //we're not looking for a closing angle bracket or a semicolon.
                    Character counterpart = HTMLFile.risingCounterpart(c);

                    if( counterpart==null ){ //the current character c isn't an opening angle bracket or ampersand.
                        result.add( new IntChar(i,c) );
                    } else{
                        //c is a special character and we need to take special action.
                        //store the counterpart of c so we know what to look for later to end this special condition.
                        mate = counterpart;
                        //do not add c to the list.
                    }
                } else if( mate.equals(c) ){
                    //then we can stop looking for that mate
                    mate = null;
                } //else, we need to keep looking for that mate
            }

            return result;
        }

        /**
         * <p>Pairs an int index in the string of a literal text line from an 
         * HTML file with the char at that index in the line.</p>
         */
        public static class IntChar{
            public final int i;
            public final char c;
            public IntChar(int i, char c){
                this.i = i;
                this.c = c;
            }
        }

        /**
         * <p>Returns true if the character from a line of an HTML file 
         * matches the corresponding character from the string used to 
         * construct this ApoPattern, false otherwise.</p>
         * 
         * <p>The special characters {@link #WORD_CHAR WORD_CHAR}, 
         * {@link #NON_WORD_CHAR NON_WORD_CHAR}, and {@link #ALPHA_CHAR ALPHA_CHAR} 
         * in this instance's code initiate specific tests; all other characters 
         * are tested literally against the <code>fromLine</code> character.</p>
         * @param fromInstanceCode a character from the string used to construct 
         * this ApoPattern
         * @param fromLine a character from a line from an HTML file
         * @return true if the character from a line of an HTML file 
         * matches the corresponding character from the string used to 
         * construct this ApoPattern, false otherwise
         */
        private static boolean match(Character fromInstanceCode, Character fromLine){
            switch(fromInstanceCode){
            case WORD_CHAR     : return PhraseProducer.isPhraseChar(fromLine);
            case NON_WORD_CHAR : return !PhraseProducer.isPhraseChar(fromLine);
            case ALPHA_CHAR    : return isAlphabetical(fromLine);
            default            : return fromLine!=null && fromInstanceCode.equals(Character.toLowerCase(fromLine)); 
            }
        }

        /**
         * <p>Returns true if <code>c</code> is an alphabetical character, 
         * false otherwise.</p>
         * @param c a character to be evaluated as alphabetical or not
         * @return true if <code>c</code> is an alphabetical character, 
         * false otherwise
         */
        private static boolean isAlphabetical(Character c){
            return c != null 
                    && !( c==APOSTROPHE 
                            || c=='-' 
                            || ('0'<=c && c<='9')) 
                    && PhraseProducer.isPhraseChar(c);
        }
    }

    /**
     * <p>Returns a list of indices in <code>line</code> at which 
     * right single quotes are located.</p>
     * @param line a string to be analysed to find the locations of 
     * all right single quotes in it
     * @return a list of indices in <code>line</code> at which 
     * right single quotes are located
     */
    private static List<Integer> singleQuoteIndices(StringBuilder line){
        List<Integer> result = new ArrayList<>();
        for(int i=0; i<line.length(); i++){
            if( isPossibleApostrophe(line.charAt(i)) ){
                result.add(i);
            }
        }
        return result;
    }

    /**
     * <p>Returns true if <code>c<code> is a character that might 
     * need to be changed to an apostrophe, false if <code>c</code> 
     * cannot need to be changed to an apostrophe.</p>
     * @param c a character whose candidacy for needing to be replaced 
     * by an apostrophe is determined
     * @return true if <code>c<code> is a character that might 
     * need to be changed to an apostrophe, false if <code>c</code> 
     * cannot need to be changed to an apostrophe
     */
    private static boolean isPossibleApostrophe(char c){
        return c == RIGHT_SINGLE_QUOTE;
    }
}