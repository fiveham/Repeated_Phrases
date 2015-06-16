package repeatedphrases;

/**
 * <p>Turns HTML files of the ASOIAF books into HTML files of individual 
 * chapters (and pseudochapters for the novellas).</p>
 */
public class GenerateChapters {
	
	/**
	 * <p>Ensures that the folders specified in {@link Folders Folders} are 
	 * available and that the HTML books are present in 
	 * {@link Folder#HTML_BOOKS the first directory} and calls the main 
	 * methods of NewlineP, ClearExcessStructure, 
	 * ClearFrontAndBackMatter, SwapApostrophes, and SplitChapters, 
	 * sending them no command-line args.</p>
	 * 
	 * @param args command-line arguments (unused)
	 */
	public static void main(String[] args) {
		
		EnsureFolders.ensureFolders();
		
		String[] noargs = {};
		
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
	}
}
