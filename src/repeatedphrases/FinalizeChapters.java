package repeatedphrases;

import java.io.File;

/**
 * <p>Unites the operations of HtmlToText, 
 * FindRepeatedPhrases, RemoveDependentPhrases,
 * RemoveUniqueIndependents, DetermineAnchors, 
 * LinkChapters, and SetTrail for convenience.</p>
 */
public class FinalizeChapters {
	
	/**
	 * <p>Calls the main methods of HtmlToText, 
	 * FindRepeatedPhrases, RemoveDependentPhrases,
	 * RemoveUniqueIndependents, DetermineAnchors, 
	 * LinkChapters, and SetTrail.
	 * 
	 * Passes the first command line argument to 
	 * DetermineAnchors and SetTrail, and passes the 
	 * second command-line argument to LinkChapters 
	 * if it is present and if it parses as an int.</p>
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		
		validateArgs(args);
		String[] noargs = {};

		EnsureFolders.ensureFolders();
		
		System.out.println("Adding newlines before paragraphs to enhance HTML files' human-readability.");
		NewlineP.main(noargs);
		
		System.out.println("Removing inconsistent, unnecessary structure.");
		ClearExcessStructure.main(noargs);
		
		System.out.println("Removing non-chapter material.");
		ClearFrontAndBackMatter.main(noargs);
		
		System.out.println("Placing apostrophes (') in place of certain right single quotes.");
		SwapApostrophes.main(noargs);
		
		System.out.println("Splitting books into individual chapter files.");
		SplitChapters.main(noargs);
		
		System.out.printf("Converting html files in %s to text.%n", HtmlToText.READ_FROM.folderName());
		HtmlToText.main(noargs);
		
		System.out.println("Finding phrases that repeat in the corpus.");
		FindRepeatedPhrases.main(noargs);
		
		System.out.println("Finding and ignoring phrase-instances that are subsumed by an instance of a larger repeated phrase");
		RemoveDependentPhrases.main(noargs);
		
		System.out.println("Finding and ignoring phrase-instances that are not repeated among the phrases that passed the previous test.");
		RemoveUniqueIndependents.main(noargs);
		
		LinksAndTrail.main( args );
	}
	
	/**
	 * <p>Checks that the command-line arguments passed to main() include 
	 * an existing file to be passed to SetTrail, and returns the int 
	 * value of the second command-line argument, if it is present and 
	 * parses as an int, for use as the phrase-size threshold passed to 
	 * {@link LinkChapters#main(String[]) LinkChapters}.</p>
	 * @param args command-line arguments passed from main()
	 * @return the int value of the second command-line argument, if 
	 * it is present and parses as an int, {@value #IO.PHRASE_SIZE_FOR_ANCHOR} otherwise.
	 */
	static int validateArgs(String[] args){
		if( args.length < 1 ){
			System.out.println("Need a trail file.");
			System.out.println("Usage: java FinalizeChapters trail-file-name [phrase-size-min-for-linking]");
			System.exit(0);
		}
		
		String trail = args[0];
		
		if( !(new File(trail)).exists() ){
			System.out.println("Cannot find file "+trail);
			System.exit(0);
		}
		
		if( args.length < 2 ){
			return IO.PHRASE_SIZE_THRESHOLD_FOR_ANCHOR;
		}
		
		try{
			return Integer.parseInt(args[1]);
		} catch(NumberFormatException e){
			return IO.PHRASE_SIZE_THRESHOLD_FOR_ANCHOR;
		}
	}
}
