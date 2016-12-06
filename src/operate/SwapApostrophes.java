package operate;

import common.IO;
import html.CharCode;
import html.HTMLFile;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import text.PhraseProducer;

/**
 * Scans each specified file looking for single-quote characters, and replaces them with proper
 * apostrophes ("\'") if the characters around the single-quote match any of several PATTERNS.
 */
class SwapApostrophes{
    
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
    static void swapApostrophes(Operation op, String[] args, Consumer<String> msg){
        File[] readUs = op.readFrom().folder().listFiles(IO::isHtml);
        
        Stream.of(readUs)
                .parallel()
                .forEach((srcFile) -> {
                    msg.accept("Normalizing apostrophes: " + srcFile.getName());
                    
                    try(OutputStreamWriter out = IO.newOutputStreamWriter(
                            op.writeTo().folderName() 
                            + File.separator 
                            + srcFile.getName())){
                        
                        List<String> lines = IO.fileContentsAsList(
                                srcFile, 
                                Scanner::nextLine, 
                                Scanner::hasNextLine);
                        
                        for(String l : lines){
                            StringBuilder line = new StringBuilder(l);
                            singleQuoteIndices(line)
                                    .filter((index) -> shouldChangeCharacter(line, index))
                                    .forEach((index) -> line.setCharAt(index, APOSTROPHE));
                            out.write(line.toString() + IO.NEW_LINE);
                        }
                        
                        out.close();
                    } catch(IOException e){
                    }
                });
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
        for(ApoPattern pattern : PATTERNS){
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
    private static final List<ApoPattern> PATTERNS = Stream.of(
        "@'@",	//everything from I'm to shouldn't've
        "&o'&",	//of
        "&t'&",	//to
        
        //'Yaya, the 'bite, 'bout, 'cat (shadowcat, 'tis, 'twas, 'twixt, 'prentice, 'em, 'ud
        "&'*",	
        "&ha'&",	//have, usually "gods have mercy"
        "&f'&",	//for
        "&a'&",	//at, as pronounced by some wildlings
        "traitors' graves",
        "traitors' collars",
        "traitors' heads",	//"@@@ traitors' @@@@@"
        "wolves' work",
        "wolves' heads",	//"@@@ wolves' @@@@"
        "rams' heads",
        "lions' heads",
        "lions' paws",
        "lions' tails",
        "&the alchemists' guild&",
        "&the alchemists' vile&",	//"&the alchemists' @@@@"
        "pyromancers' piss",
        "@@@s' own&",
        "&the @o@s' @@@@",
        "&the boys' grandfather&",
        "&the boys' heads&",	//"@@@ the boys' @@@@@"
        "&merchants' sons&",
        "&merchants' stalls&",	//"&merchants' s@@@"
        "merchants' carts",
        "the merchants' row",
        "the merchants' wagons",
        "&their mothers' @@@",	//"&their mothers' @@@@@"
        "whores' skirts",
        "&your brothers' @@@@",
        "&my brothers' ghosts&",	//"@@ brothers' @@@@"
        "&his brothers' @@@@",
        "@@@@s' nest",
        "horses' hooves",
        "be keepin'&",
        "is carryin'&",	//"@@@@in' @@@"
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
        "&lil'&").map(ApoPattern::new).collect(Collectors.toList());
    
    /**
     * <p>Represents a pattern of characters around an apostrophe, meant for use in determining
     * which instances of a right single quote in the text of ASOIAF should be ordinary apostrophes
     * instead.</p>
     */
    private static class ApoPattern{
        
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
        private ApoPattern(String s){
            s = s.toLowerCase();
            int index = s.indexOf(APOSTROPHE);
            before = new ArrayList<>();
            after = IntStream.range(index + 1, s.length())
                    .mapToObj(s::charAt)
                    .collect(Collectors.toList());
            for(int i = index - 1; i >= 0; i--){
                before.add(s.charAt(i));
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
        	
            if(!isPossibleApostrophe(line.charAt(index))){
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
            for(int i = 0; i < line.length(); i++){
                char c = line.charAt(i);
                
                if(mate == null){ //we're not looking for a closing angle bracket or a semicolon.
                    Character counterpart = HTMLFile.risingCounterpart(c);
                    
                    if(counterpart == null){
                    	//the current character c isn't an opening angle bracket or ampersand.
                        result.add(new IntChar(i,c));
                    } else{
                        //c is a special character and we need to take special action.
                        //store the counterpart of c so we know what to look for later to end this 
                    	//special condition.
                        mate = counterpart;
                        //do not add c to the list.
                    }
                } else if(mate.equals(c)){
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
                    && !(c == APOSTROPHE 
                            || c == '-' 
                            || ('0' <= c && c <= '9')) 
                    && PhraseProducer.isPhraseChar(c);
        }
    }
    
    /**
     * <p>Returns a list of indices in {@code line} at which right single quotes are located.</p>
     * @param line a string to be analysed to find the locations of all right single quotes in it
     * @return a list of indices in {@code line} at which right single quotes are located
     */
    private static IntStream singleQuoteIndices(StringBuilder line){
        return IntStream.range(0, line.length())
                .filter((i) -> isPossibleApostrophe(line.charAt(i)));
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
