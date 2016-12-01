package operate;

import common.IO;
import java.util.List;
import java.io.File;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import text.Chapter;
import text.Corpus;
import text.PhraseBox;
import text.PhraseProducer;
import text.Quote;

/**
 * <p>Finds phrases that are repeated in the corpus and prints them to files accompanied by a list
 * of the {@code Location}s in the corpus at which those phrases occur.</p>
 */
public class FindRepeatedPhrases {
	
    public static final Operation OPERATION = Operation.FIND_REPEATED_PHRASES;
	
    /**
     * <p>Number of locations in the corpus at which a unique phrase occurs, by definition.</p>
     */
	public static final int UNIQUE_PHRASE_LOCATION_COUNT = 1;
	
    /**
     * <p>Minimum size of repeated phrases to be found.</p>
     */
	public static final int MIN_PHRASE_SIZE = 1;
	
    /**
     * <p>Maximum size of repeated phrases to be found. The value {@value #MAX_PHRASE_SIZE} was
     * determined empirically, and pertains to an overlap of text between AFFC Samwell I and ADWD
     * Jon II.</p>
     */
	public static final int MAX_PHRASE_SIZE = 218;
    
    /**
     * <p>Gets a list of the files to be analysed via
     * {@code READ_FROM.folder().listFiles(IO.IS_TXT)}, loops from {@code MIN_PHRASE_SIZE} to
     * {@code MAX_PHRASE_SIZE}, finding all the phrases of each size repeated in the corpus, and
     * prints them to files named for the phrase size according to
     * {@code WRITE_TO.filename(size)}.</p> <p>"Phrase size" is the number of words in a given
     * phrase.</p>
     * @param args Command-line arguments (unused)
     */
	public static void findRepPhrases(String[] args, Consumer<String> msg) {
		
		File[] readUs = OPERATION.readFrom().folder().listFiles(IO::isTxt);
		final List<Chapter> chapters = getChapters(readUs);
		PhraseBox repeatedPhrasesFromPrevLoop = new PhraseBox();
		repeatedPhrasesFromPrevLoop.add(ZERO_WORD_PHRASE, null);
		
		//find phrases of ever greater size and record them in files
		for(int phraseSize = MIN_PHRASE_SIZE; 
				phraseSize <= MAX_PHRASE_SIZE; 
				phraseSize++){
			
			msg.accept("Begin process for phrase size "+phraseSize);
			
			//Create an index of phrases from the corpus and their locations
			PhraseBox words = scanCorpus(phraseSize, chapters, repeatedPhrasesFromPrevLoop, msg);
			
			//remove non-repeated phrases
			words.removeUniques(msg);
			
			//print repeated phrases and all their locations in the corpus to a file
			words.printPhrasesWithLocations(OPERATION.writeTo().filename(phraseSize));
			
			//Store the current list of repeated phrases for the next loop
			repeatedPhrasesFromPrevLoop = words;
		}
	}
	
    /**
     * <p>Returns a list of {@code Chapter}s pairing the full names of the files specified by
     * {@code filesToRead} with the words-only content of those files as produced by
     * {@link #fileAsString() fileAsString()}.</p>
     * @param filesToRead an array listing the plain filenames of the chapters to be processed and
     * turned into {@code Chapter}s.
     * @return a list of {@code Chapter}s pairing the full names of the files specified by
     * {@code filesToRead} with the words-only content of those files as produced by
     * {@link #fileAsString() fileAsString()}
     */
	public static List<Chapter> getChapters(File[] filesToRead){
	    return Stream.of(filesToRead)
        	    .map(Chapter::new ) 
        	    .collect(Collectors.toList());
	}
	
    /**
     * <p>Scans the files of the corpus, extracting phrases of the specified size. Adds phrases
     * whose predecessor was non-unique at the previous phrase size to the {@code PhraseBox} to be
     * returned.</p>
     * @param phraseSize number of words in the phrases being assessed
     * @param chapters a list of {@code Chapter}s representing the body of text being analysed.
     * @param repeatPhrasesForPrevSize list of phrases of the size {@code phraseSize-1} that are
     * repeated in the corpus.
     * @return a {@code PhraseBox} with phrases of the specified size, containing all such phrases
     * that are repeated in the corpus, as well as some number of unique phrases.
     */
	private static PhraseBox scanCorpus(
			int phraseSize, 
			List<Chapter> chapters, 
			PhraseBox repeatPhrasesForPrevSize, 
			Consumer<String> msg){
		
		msg.accept("Finding "+phraseSize+"-word phrases");
		
		Corpus corpus = new Corpus(phraseSize, chapters);
		PhraseBox corpusAsStructure = new PhraseBox();
		
		while(corpus.hasNext()){
			Quote phrase = corpus.next();
			String less = reducedPhrase(phrase.text());
			if(repeatPhrasesForPrevSize.contains(less)){
				corpusAsStructure.add(phrase.text(), phrase.location());
			}
		}
		
		msg.accept("Got "+corpusAsStructure.size()+" phrases");
		return corpusAsStructure;
	}
	
    /**
     * <p>Returns a string containing the first {@code n-1} words of the specified {@code n}-word
     * phrase.</p>
     * @param s the phrase whose last token is to be removed
     * @return a String containing all words but the last from the specified phrase.
     */
	public static String reducedPhrase(String s){
		int index = s.lastIndexOf(PhraseProducer.WORD_SEPARATOR);
		return index < 0 ? ZERO_WORD_PHRASE : s.substring(0,index);
	}
	
    /**
     * <p>An empty string. Returned by {@link #reducedPhrase(String) reducedPhrase()} when it is
     * sent a phrase with only one word. Added to the initial {@code PhraseBox} assigned to
     * {@code repeatedPhrasesFromPrevLoop} in {@code main()} so that every single-word phrase's
     * corresponding reduced phrase is contained by that object, ensuring that every phrase of size
     * {@code 1} passes the preliminary test for inclusion in the PhraseBox returned from
     * {@link #scanCorpus scanCorpus()}.</p>
     */
	public static final String ZERO_WORD_PHRASE = "";
}
