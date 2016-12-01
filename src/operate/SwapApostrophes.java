package operate;

import common.IO;
import html.CharCode;
import html.HTMLFile;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import text.PhraseProducer;

/**
 * Scans each specified file looking for single-quote characters, and replaces them with proper
 * apostrophes ("\'") if the characters around the single-quote match any of several PATTERNS.
 */
public class SwapApostrophes{
    
    /**
     * <p>The apostrophe character ({@value}), used in
     * {@link #ApoPattern apostrophe-replacement context codes} and as the literal apostrophe to
     * install in place of right single quote apostrophes that match a replacement pattern.</p>
     */
    public static final char APOSTROPHE = '\'';

    /**
     * <p>The right single quote conventionally used as an apostrophe as well as as a closing quote
     * in nested quotes. To be replaced in the corpus at certain places with an
     * {@link #APOSTROPHE apostrophe}.</p>
     */
    public static final char RIGHT_SINGLE_QUOTE = '\u2019';
    
    /**
     * <p>Detects all the ASOIAF novel files in {@link #READ_FROM READ_FROM}, reads them line by
     * line, finds all the right single quotes, and replaces those that fit any
     * {@link #PATTERNS replacement pattern} with an {@link #APOSTROPHE apostrophe}.</p>
     * @param op the Operation whose folders will be used
     * @param args command-line args (not used)
     * @param msg receives and handles messages output by arbitrary parts of this operation
     */
    public static void swapApostrophes(Operation op, String[] args, Consumer<String> msg){
        File[] readUs = op.readFrom().folder().listFiles(IO::isHtml);

        for(File srcFile : readUs){
            msg.accept("Normalizing apostrophes: "+srcFile.getName());

            try(OutputStreamWriter out = IO.newOutputStreamWriter(
            		op.writeTo().folderName() 
            		+ File.separator 
            		+ srcFile.getName())){
            	
                List<String> lines = IO.fileContentsAsList(
                		srcFile, 
                		Scanner::nextLine, 
                		Scanner::hasNextLine);

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
     * <p>Returns true if the text at and around {@code index} in {@code line} matches any of the
     * {@link #ApoPattern patterns} in {@link #PATTERNS PATTERNS}, false otherwise.</p>
     * @param line a line that has a right single quote at {@code index}
     * @param index the location in {@code line} of a right single quote
     * @return true if the text at and around {@code index} in {@code line} matches any of the
     * {@link #ApoPattern patterns} in {@link #PATTERNS PATTERNS}, false otherwise
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
     * <p>All the conditions according to which a right single quote may be changed to an
     * apostrophe.</p> </p>The vast majority of these are plural possessives quoted from the text of
     * ASOIAF.</p>
     */
    private static final List<ApoPattern> PATTERNS = Arrays.asList(
        new ApoPattern("@'@"),	//everything from I'm to shouldn't've
        new ApoPattern("&o'&"),	//of
        new ApoPattern("&t'&"),	//to
        
        //'Yaya, the 'bite, 'bout, 'cat (shadowcat), 'tis, 'twas, 'twixt, 'prentice, 'em, 'ud
        new ApoPattern("&'*"),	
        new ApoPattern("&ha'&"),	//have, usually "gods have mercy"
        new ApoPattern("&f'&"),	//for
        new ApoPattern("&a'&"),	//at, as pronounced by some wildlings
        new ApoPattern("traitors' graves"),
        new ApoPattern("traitors' collars"),
        new ApoPattern("traitors' heads"),	//"@@@ traitors' @@@@@"
        new ApoPattern("wolves' work"),
        new ApoPattern("wolves' heads"),	//"@@@ wolves' @@@@"
        new ApoPattern("rams' heads"),
        new ApoPattern("lions' heads"),
        new ApoPattern("lions' paws"),
        new ApoPattern("lions' tails"),
        new ApoPattern("&the alchemists' guild&"),
        new ApoPattern("&the alchemists' vile&"),	//"&the alchemists' @@@@"
        new ApoPattern("pyromancers' piss"),
        new ApoPattern("@@@s' own&"),
        new ApoPattern("&the @o@s' @@@@"),
        new ApoPattern("&the boys' grandfather&"),
        new ApoPattern("&the boys' heads&"),	//"@@@ the boys' @@@@@"
        new ApoPattern("&merchants' sons&"),
        new ApoPattern("&merchants' stalls&"),	//"&merchants' s@@@"
        new ApoPattern("merchants' carts"),
        new ApoPattern("the merchants' row"),
        new ApoPattern("the merchants' wagons"),
        new ApoPattern("&their mothers' @@@"),	//"&their mothers' @@@@@"
        new ApoPattern("whores' skirts"),
        new ApoPattern("&your brothers' @@@@"),
        new ApoPattern("&my brothers' ghosts&"),	//"@@ brothers' @@@@"
        new ApoPattern("&his brothers' @@@@"),
        new ApoPattern("@@@@s' nest"),
        new ApoPattern("horses' hooves"),
        new ApoPattern("be keepin'&"),
        new ApoPattern("is carryin'&"),	//"@@@@in' @@@"
        new ApoPattern("@@@@ts' respite"),
        new ApoPattern("years' remission"),
        new ApoPattern("pigs' feet"),
        new ApoPattern("calves' brains"),
        new ApoPattern("servants' steps"),
        new ApoPattern("servants' time"),
        new ApoPattern("servants' corridor"),
        new ApoPattern("lords' bannermen"),
        new ApoPattern("lords' entrance"),
        new ApoPattern("were lords' sons"),
        new ApoPattern("&goats' milk&"),
        new ApoPattern("&slavers' filth&"),
        new ApoPattern("&slavers' pyramid&"),
        new ApoPattern("&sailors' stor@"),
        new ApoPattern("&sailors' temple&"),
        new ApoPattern("&smugglers' cove&"),
        new ApoPattern("&smugglers' stars&"),
        new ApoPattern("&days' ride&"),
        new ApoPattern("&days' food&"),
        new ApoPattern("&days' sail&"),
        new ApoPattern("&hours' ride&"),
        new ApoPattern("&hours' sail&"),
        new ApoPattern("bakers'&"),
        new ApoPattern("@ mummers' @@@@"),
        new ApoPattern("with strangers' eyes"),
        new ApoPattern("their masters' business"),
        new ApoPattern("the challengers' paddock"),
        new ApoPattern("stoops' wife"),
        new ApoPattern("&or one of the lannisters'&"),
        new ApoPattern("ladies' cats"),
        new ApoPattern("bastards' names"),
        new ApoPattern("the rangers' search"),
        new ApoPattern("their fathers' rusted swords"),
        new ApoPattern("his cousins' eyes"),
        new ApoPattern("maidens' judgments"),
        new ApoPattern("a singers' tourney"),
        new ApoPattern("a fools' joust"),
        new ApoPattern("an outlaws' lair"),
        new ApoPattern("rats' eyes"),
        new ApoPattern("the wildlings' herds"),
        new ApoPattern("dead friends' father"),
        new ApoPattern("archers' stakes"),
        new ApoPattern("heralds' trumpets"),
        new ApoPattern("the climbers' rope"),
        new ApoPattern("griffins' men"),
        new ApoPattern("their masters' possessions"),
        new ApoPattern("their neighbors' daughters"),
        new ApoPattern("the musicians' gallery"),
        new ApoPattern("kings' blood"),
        new ApoPattern("the besiegers' cheers"),
        new ApoPattern("gulls' eggs"),
        new ApoPattern("the defenders' shouts"),
        new ApoPattern("priests' song"),
        new ApoPattern("heroes' tombs"),
        new ApoPattern("some robbers' den"),
        new ApoPattern("babies' bottoms"),
        new ApoPattern("sound of lovers' footsteps"),
        new ApoPattern("the murderers' secret"),
        new ApoPattern("abandoned crofters' village"),
        new ApoPattern("the diggers' eyes were"),
        new ApoPattern("my sons' things"),
        new ApoPattern("&lil'&"));

    /**
     * <p>Represents a pattern of characters around an apostrophe, meant for use in determining
     * which instances of a right single quote in the text of ASOIAF should be ordinary apostrophes
     * instead.</p>
     */
    public static class ApoPattern{

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
         * <p>Returns true if the content of {@code line} around position {@code index} matches this
         * ApoPattern, false otherwise.</p>
         * @param line string in which to try to match this ApoPattern
         * @param index position in {@code line} around which to try to match this ApoPattern
         * @return true if the content of {@code line} around position {@code index} matches this
         * ApoPattern, false otherwise
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
         * <p>Returns the character at the specified {@code index} in {@code list} if {@code index}
         * is within the bounds of {@code list}, null otherwise.</p>
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
         * <p>Returns the index in {@code cleanLine} at which the {@link IntChar#i index} of the
         * element there equals {@code soughtIndex}.</p>
         * @param cleanLine an HTML-free representation of a literal line from an HTML file being
         * processed
         * @param soughtIndex an index in the original HTML file line such that if the line's
         * character from that point ended up in the HTML-free {@code cleanLine}, the position in
         * {@code cleanLine} of the IntChar derived from that character in the literal line is
         * returned
         * @return the index in the HTML-free {@code cleanLine} of the IntChar derived from the
         * character at {@code soughtIndex} in the original, literal text line from an HTML file, or
         * -1 if the character at {@code soughtIndex} in the literal HTML-file line is not
         * represented in the HTML-free {@code cleanLine}.
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
         * <p>Creates a representation of a literal {@code line} from an HTML file such that
         * {@link Tag Tags} and {@link CharCode Codes} are excluded.</p>
         * @param line a literal text line from an HTML file
         * @return a representation of a literal {@code line} from an HTML file such that
         * {@link Tag Tags} and {@link CharCode Codes} are excluded
         */
        private List<IntChar> cleanLine(StringBuilder line){
            List<IntChar> result = new ArrayList<>();
            
            //stores '>' or ';' while iterating through an HTML tag or character code so we know 
            //when to stop skipping characters.
            Character mate = null;
            for(int i=0; i<line.length(); i++){
                char c = line.charAt(i);
                
                if(mate==null){ //we're not looking for a closing angle bracket or a semicolon.
                    Character counterpart = HTMLFile.risingCounterpart(c);
                    
                    if(counterpart==null){
                    	//the current character c isn't an opening angle bracket or ampersand.
                        result.add( new IntChar(i,c) );
                    } else{
                        //c is a special character and we need to take special action.
                        //store the counterpart of c so we know what to look for later to end this 
                    	//special condition.
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
         * <p>Pairs an int index in the string of a literal text line from an HTML file with the
         * char at that index in the line.</p>
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
         * <p>Returns true if the character from a line of an HTML file matches the corresponding
         * character from the string used to construct this ApoPattern, false otherwise.</p> <p>The
         * special characters {@link #WORD_CHAR WORD_CHAR}, {@link #NON_WORD_CHAR NON_WORD_CHAR},
         * and {@link #ALPHA_CHAR ALPHA_CHAR} in this instance's code initiate specific tests; all
         * other characters are tested literally against the {@code fromLine} character.</p>
         * @param fromInstanceCode a character from the string used to construct this ApoPattern
         * @param fromLine a character from a line from an HTML file
         * @return true if the character from a line of an HTML file matches the corresponding
         * character from the string used to construct this ApoPattern, false otherwise
         */
        private static boolean match(Character fromInstanceCode, Character fromLine){
            switch(fromInstanceCode){
            case WORD_CHAR     : return PhraseProducer.isPhraseChar(fromLine);
            case NON_WORD_CHAR : return !PhraseProducer.isPhraseChar(fromLine);
            case ALPHA_CHAR    : return isAlphabetical(fromLine);
            default            : return fromLine != null 
            		&& fromInstanceCode.equals(Character.toLowerCase(fromLine)); 
            }
        }
        
        /**
         * <p>Returns true if {@code c} is an alphabetical character, false otherwise.</p>
         * @param c a character to be evaluated as alphabetical or not
         * @return true if {@code c} is an alphabetical character, false otherwise
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
     * <p>Returns a list of indices in {@code line} at which right single quotes are located.</p>
     * @param line a string to be analysed to find the locations of all right single quotes in it
     * @return a list of indices in {@code line} at which right single quotes are located
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
     * <p>Returns true if {@code c} cannot need to be changed to an apostrophe.</p>
     * @param c a character whose candidacy for needing to be replaced by an apostrophe is
     * determined
     * @return true if {@code c} cannot need to be changed to an apostrophe
     */
    private static boolean isPossibleApostrophe(char c){
        return c == RIGHT_SINGLE_QUOTE;
    }
}
