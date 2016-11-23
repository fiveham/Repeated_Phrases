package text;

import java.util.Map;
import java.util.stream.Collectors;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * <p>Stores quote data chapter-first.</p> <p>Wraps a {@link text.FileBox FileBox}.</p>
 */
public class Database {
	
    /**
     * <p>The wrapped FileBox.</p>
     */
	private FileBox textCorpus;
	
    /**
     * <p>Constructs a Database with an empty {@code textCorpus}.</p>
     */
	public Database(){
		textCorpus = new FileBox();
	}
	
    /**
     * <p>Constructs a Database with a FileBox containing all the quote data from the specified
     * File.</p>
     * @param f the file containing quote data to be read
     * @throws FileNotFoundException if the specified File does not exist or cannot be read
     */
	public Database(File f) throws FileNotFoundException{
		textCorpus = new FileBox(f);
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
	
    /**
     * <p>Returns a PhraseBox containing exactly those quotes represented in {@code this.textCorpus}
     * that are independent of all the quotes represented in {@code otherDatabase.textCorpus}.</p>
     * @param otherDatabase another Database against whose quotes the quotes of this Database are to
     * be tested for independence.
     * @return a PhraseBox containing exactly those quotes represented in {@code this.textCorpus}
     * that are independent of all the quotes represented in {@code otherDatabase.textCorpus}.
     */
	public PhraseBox phrasesIndependentOf(Database otherDatabase){
		PhraseBox result = new PhraseBox();
		
		//Look at each group of quotes that share a filename
		for(String filename : textCorpus.filenames()){
			if(otherDatabase.textCorpus.contains(filename)){
				
				//get a random-access representation of the current corpus file
				Map<Integer, String> fileForLargePhrases = 
						otherDatabase.textCorpus.get(filename).stream()
								.collect(Collectors.toMap(Quote::index, Quote::text));
				
				//iterate over the quotes for the current filename in this Database.
				for(Quote phraseHere : textCorpus.get(filename)){
					
					//Phrases from the other Database that could possibly contain the phrase 
					//component of the current phrase-index pair.
					
					//Only two samples of the representation of the other file are taken because 
					//the overall program only ever calls this method with an argument Database 
					//containing phrases one word longer than those in the Database on which the 
					//method is executed.
					
					//the larger-sized phrase starting one word earlier in the file than 
					//smallerPhraseInFile does
					String largerPhraseWithIndexOneLess = phraseHere.index() == 0 
							? null 
							: fileForLargePhrases.get(phraseHere.index()-1);
					
					//the larger-sized phrase starting at the same location in the file as 
					//smallerPhraseInFile
					String largerPhraseWithSameIndex = 
							fileForLargePhrases.size() == phraseHere.index() 
									? null
									: fileForLargePhrases.get(phraseHere.index());
					
					//If either of the possible subsuming phrases contains the current phrase at 
					//the proper location in itself, then 
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
						//There's no quote in otherDatabase at this phrase's index or 1 less than 
						//that
						
						//There's no phrase in otherDatabase that subsumes this phrase the phrase 
						//in focus from this Database is independent of otherDatabase; so, the 
						//current small phrase should be added to the output.
						result.add(phraseHere.text(), new Location(phraseHere.index(), filename));
					} else if(!((lowerIndexLargerPhraseExistsAndEndsWithSmallPhrase  == null 
							||    lowerIndexLargerPhraseExistsAndEndsWithSmallPhrase  == true) 
							||   (sameIndexLargerPhraseExistsAndStartsWithSmallPhrase == null 
							||    sameIndexLargerPhraseExistsAndStartsWithSmallPhrase == true))){
						
						//It is not the case that LILPEAEWSP and SILPEASWSP have a value pair of 
						//(null, true), (true, true), or (true, null), any of which would 
						//indicate perfectly that the smaller phrase is contained in an instance 
						//of a larger phrase from otherDatabase.
						//Since (null, null) is also excluded (by this test and by the fact 
						//that execution has moved to this block of code while the previous if-test 
						//tests exactly for (null, null)), at least one of the Boolean 
						//values is false, which means that an instance of a larger phrase 
						//exists in a position that should overlap the smaller phrase, 
						//yet that larger phrase doesn't startWith or endWith the smaller phrase
						//the way its location demands, which is impossible.
						
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
					} //Otherwise, the Boolean assignments did equal (null, true), (true, true), 
					//or (true, null), which indicate that the small phrase is dependent on a 
					//larger phrase with no error.
					//In that case, no action should be taken.
				}
			} else{
				//otherDatabase contains no entries from this file; so, 
				//all entries in this Database from that corpus file 
				//are independent of otherDatabase.
				//Add all quotes for this filename to the output
				textCorpus.get(filename).forEach(
						(lp) -> result.add(lp.text(), new Location(lp.index(), filename)));
			}
		}
		
		return result;
	}
}
