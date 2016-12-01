package text;

import java.io.File;

import common.IO;

/**
 * <p>Represents a chapter of a book in the body of text being analysed. Pairs the name of the file
 * from which content is read with the contents of the file as supplied to the constructor.</p>
 */
public class Chapter {
	
    /**
     * <p>The name of the file for this chapter.</p>
     */
	private final String filename;
	
    /**
     * <p>The content of the file for this chapter.</p>
     */
	private final String body;
	
    /**
     * <p>Constructs a Chapter based on the specified file.</p>
     * @param file the file for this Chapter
     */
	public Chapter(File file){
	    this.filename = file.getName();
	    this.body = IO.fileAsString(file);
	}
	
    /**
     * <p>Returns {@code filename}.</p>
     * @return {@code filename}
     */
	public String getName(){
		return filename;
	}
	
    /**
     * <p>Returns {@code body}.</p>
     * @return {@code body}
     */
	public String getBody(){
		return body;
	}
}
