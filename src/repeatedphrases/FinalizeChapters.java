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
		int limit = validateArgs(args);
		String trail = args[0];
		
		String[] noargs = new String[0];
		String[] trailArgs = new String[]{ trail };
		
		System.out.printf("Converting html files in %s to text.%n", HtmlToText.READ_FROM.folderName());
		HtmlToText.main(noargs);
		
		System.out.println("Finding phrases that repeat in the corpus.");
		FindRepeatedPhrases.main(noargs);
		
		System.out.println("Finding and ignoring phrase-instances that are subsumed by an instance of a larger repeated phrase");
		RemoveDependentPhrases.main(noargs);
		
		System.out.println("Finding and ignoring phrase-instances that are not repeated among the phrases that passed the previous test.");
		RemoveUniqueIndependents.main(noargs);
		
		System.out.println("Turning repeated-independent-repeats data into anchor-data");
		DetermineAnchors.main( trailArgs );
		
		System.out.println("Adding anchors to html chapters");
		LinkChapters.main( limit==NO_LIMIT ? noargs : new String[]{ Integer.toString(limit) } );
		
		System.out.println("Adding previous-chapter and next-chapter links to html chapters.");
		SetTrail.main( trailArgs );
	}
	
	/**
	 * <p>Checks that the command-line arguments passed to main() include 
	 * an existing file to be passed to SetTrail, and returns the int 
	 * value of the second command-line argument, if it is present and 
	 * if it parses as an int, for use as the phrase-size threshold 
	 * passed to LinkChapters.main().</p>
	 * @param args command-line arguments passed from main()
	 * @return the int value of the second command-line argument, if 
	 * it is present and parses as an int, {@value #NO_LIMIT} otherwise.
	 */
	private static int validateArgs(String[] args){
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
			return NO_LIMIT;
		}
		
		try{
			return Integer.parseInt(args[1]);
		} catch(NumberFormatException e){
			return NO_LIMIT;
		}
	}
	
	/**
	 * <p>The value returned by {@link #validateArgs(String[]) validateArgs()} 
	 * when the phrase-size-limit argument is absent from the 
	 * command-line args or doesn't parse as an int.</p>
	 */
	public static final int NO_LIMIT = -1;
}
