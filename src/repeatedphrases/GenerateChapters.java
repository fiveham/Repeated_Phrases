package repeatedphrases;

import java.io.File;

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
		ensureFolders();
		
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
	
	/**
	 * <p>Ensures that the working directory has the folders specified 
	 * in {@link Folders Folders} and that the books are present as HTML 
	 * files in {@link Folder#HTML_BOOKS the first directory}. Prints an 
	 * error message to the console if either of those is true and 
	 * {@link System#exit(int) exits}.</p>
	 */
	private static void ensureFolders(){
		System.out.println("Checking for needed folders.");
		boolean createdSource = false;
		for(Folder f : Folder.values()){
			File name = f.folder();
			if( !name.exists() ){
				System.out.println("Creating needed folder "+name.getName());
				name.mkdir();
				if( f==Folder.HTML_BOOKS ){
					createdSource = true;
				}
			}
		}
		
		if(createdSource){
			System.out.println("USER ACTION REQUIRED");
			System.out.println("I had to create the folder for HTML books; so, there can't already be books there.");
			System.out.println("Add the HTML books to "+Folder.HTML_BOOKS.folderName()+" and re-run.");
			System.exit(0);
		} else{
			String[] sources = Folder.HTML_BOOKS.folder().list(IO.IS_HTML);
			if( sources.length != HTML_BOOK_COUNT ){
				System.out.println("USER ACTION REQUIRED");
				System.out.println("I found "+sources.length+" HTML files in "+Folder.HTML_BOOKS.folderName()
						+ ", but there should be "+HTML_BOOK_COUNT);
				System.exit(0);
			}
		}
		
	}
	
	/**
	 * <p>The number of HTML book files needed to start this project: 
	 * Five novels, AGOT through ADWD, and five novellas: D&E 1 through 3 
	 * (0 through 2 in filenames), PQ, and RP.</p>
	 */
	public static final int HTML_BOOK_COUNT = 10;
}
