package repeatedphrases;

import java.io.File;
import java.util.function.Consumer;

import common.Folder;
import common.IO;

/**
 * <p>Ensures that the folders needed for this project are present 
 * in the current working directory.</p>
 */
public class EnsureFolders {
	
	/**
	 * <p>Calls {@link #ensureFolders() ensureFolders()}.</p>
	 * @param args command-line arguments (unused)
	 */
	public static void main(String[] args) {
		ensureFolders(IO.DEFAULT_MSG);
	}
	
	/**
	 * <p>Ensures that the working directory has the folders specified 
	 * in {@link Folders Folders} and that the books are present as HTML 
	 * files in {@link Folder#HTML_BOOKS the first directory}. Prints an 
	 * error message to the console if either of those is true and 
	 * {@link System#exit(int) exits}.</p>
	 */
	public static void ensureFolders(Consumer<String> msg){
		for(Folder f : Folder.values()){
			File name = f.folder();
			if( !name.exists() ){
				msg.accept("Creating "+name.getName());
				name.mkdir();
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
