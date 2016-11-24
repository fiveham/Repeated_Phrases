package text;

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
	
	public FileBox textCorpus(){
	    return textCorpus;
	}
}
