package operate;

import common.IO;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import text.FileBox;
import text.Location;
import text.PhraseBox;
import text.Quote;

/**
 * <p>This class searches the population of known phrases that occur more than once in the text of A
 * Song of Ice and Fire and removes from an in-memory copy of that population all instances of such
 * phrases that are part of an instance of some larger phrase. These quotes to be removed are
 * "dependent"; those that remain are "independent".</p> <p>Instead of actually removing these
 * dependent phrases from the source from which they were obtained, new files are created containing
 * only the independent quotes.</p> <p>The files for independent quotes are organized according to
 * the number of words in their phrases.</p>
 */
public class RemoveDependentPhrases {
    
    public static final Operation OPERATION = Operation.REMOVE_DEPENDENT_PHRASES;
    
    /**
     * <p>The largest number of words in any repeated phrase in the corpus: {@value}</p>
     */
    public static final int INIT_LOW_SIZE = FindRepeatedPhrases.MAX_PHRASE_SIZE-1;
    
    /**
     * <p>An exclusive lower bound for how small the smaller of two phrase-sizes being used at once
     * for dependence-testing can be. No phrase can be of this size ({@value}).</p>
     */
    public static final int LOW_SIZE_EXCLUSIVE_LOWER_BOUND = 0;
    
    /**
     * <p>Iterates over a set of pre-existing files generated by FindRepeatedPhrases, starting at
     * the largest phrase-size for which the file output by FindRepeatedPhrases is not empty. For
     * each phrase-size considered, the repeated-phrase data for that size and for that size plus 1
     * are loaded into memory from the pertinent pre- existing files created by FindRepeatedPhrases,
     * except that the data for the previous size is carried over from one loop to the next and
     * reused as the data for the plus-one size in the next loop. Prints to file all those quotes of
     * the smaller phrase size specified in the current loop that are independent of the larger
     * quotes in the current group of larger phrases. If a problem occurs while reading a file,
     * System.exit is called, ending the program.</p>
     * @param msg receives and handles messages output by arbitrary parts of this operation
     */
    public static void rmDepPhrases(Consumer<String> msg) {
        
        FileBox smallerPhrases = null; //inter-loop storage
        
        for(int lowSize=INIT_LOW_SIZE; lowSize>LOW_SIZE_EXCLUSIVE_LOWER_BOUND; lowSize--){
            
            try{
                FileBox largerPhrases = (smallerPhrases != null)
                        ? smallerPhrases
                        : new FileBox(new File(OPERATION.readFrom().filename(lowSize+1)));
                smallerPhrases = new FileBox(new File(OPERATION.readFrom().filename(lowSize)));
                
                phrasesIndependentOf(smallerPhrases, largerPhrases)
                        .printPhrasesWithLocations(OPERATION.writeTo().filename(lowSize));
            } catch(FileNotFoundException e){
            	throw new RuntimeException(
            			IO.ERROR_EXIT_MSG 
            			+ OPERATION.readFrom().filename(lowSize) 
            			+ " or " 
            			+ OPERATION.readFrom().filename(lowSize+1));
            }
        }
    }
    
    /**
     * <p>Returns a PhraseBox containing exactly those quotes represented in {@code this.textCorpus}
     * that are independent of all the quotes represented in {@code otherDatabase.textCorpus}.</p>
     * @param small a FileBox of smaller phrases
     * @param large a FileBox of larger phrases
     * @return a PhraseBox containing exactly those quotes represented in {@code this.textCorpus}
     * that are independent of all the quotes represented in {@code otherDatabase.textCorpus}.
     */
    private static PhraseBox phrasesIndependentOf(FileBox small, FileBox large){
        PhraseBox result = new PhraseBox();
        
        for(String filename : small.filenames()){
            if(large.contains(filename)){
                
                Map<Integer, String> fileForLargePhrases = 
                        large.get(filename).stream()
                                .collect(Collectors.toMap(Quote::index, Quote::text));
                
                for(Quote phraseHere : small.get(filename)){
                    
                    String largerPhraseWithIndexOneLess = phraseHere.index() == 0 
                            ? null 
                            : fileForLargePhrases.get(phraseHere.index()-1);
                    
                    String largerPhraseWithSameIndex = 
                            fileForLargePhrases.size() == phraseHere.index() 
                                    ? null
                                    : fileForLargePhrases.get(phraseHere.index());
                    
                    Boolean lowerIndexLargerPhraseExistsAndEndsWithSmallPhrase = 
                            largerPhraseWithIndexOneLess == null
                                    ? null
                                    : largerPhraseWithIndexOneLess.endsWith(phraseHere.text());
                    
                    Boolean sameIndexLargerPhraseExistsAndStartsWithSmallPhrase = 
                            largerPhraseWithSameIndex == null
                                    ? null
                                    : largerPhraseWithSameIndex.startsWith(phraseHere.text());
                    
                    if(lowerIndexLargerPhraseExistsAndEndsWithSmallPhrase == null 
                            && sameIndexLargerPhraseExistsAndStartsWithSmallPhrase == null){
                        
                        result.add(phraseHere.text(), new Location(phraseHere.index(), filename));
                    } else if(!((lowerIndexLargerPhraseExistsAndEndsWithSmallPhrase  == null 
                            ||    lowerIndexLargerPhraseExistsAndEndsWithSmallPhrase  == true) 
                            ||   (sameIndexLargerPhraseExistsAndStartsWithSmallPhrase == null 
                            ||    sameIndexLargerPhraseExistsAndStartsWithSmallPhrase == true))){
                        
                        throw new IllegalStateException(
                                "The smaller phrase \"" 
                                + shortForm(phraseHere.text()) 
                                + "\" is contained at the proper location in zero or one of the " 
                                + "two larger phrases that could contain it: " 
                                + "Phrase at some index in file " + filename + ": \"" 
                                + shortForm(largerPhraseWithIndexOneLess) 
                                + "\" --- " 
                                + "Phrase at some index in file " + filename + ": \"" 
                                + shortForm(largerPhraseWithSameIndex) 
                                + "\".");
                    }
                }
            } else{
                small.get(filename).forEach(
                        (lp) -> result.add(lp.text(), new Location(lp.index(), filename)));
            }
        }
        
        return result;
    }
    
    /**
     * <p>Returns a shortened form of the specified String.</p>
     * @param phrase the phrase of which a shortened form will be returned
     * @return the first 25 characters of phrase + " ... " + the last 25 characters if the phrase
     * has 60 or more characters, otherwise the entire phrase.
     */
    private static String shortForm(String phrase){
        if(phrase.length() < 60){
            return phrase;
        }
        return phrase.substring(0, 25) + " ... " + phrase.substring(phrase.length()-26);
    }
}
