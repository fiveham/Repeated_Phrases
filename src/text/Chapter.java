package text;

import java.io.File;

import common.IO;
import html.HTMLFile;

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
	
	public Chapter(HTMLFile h){
	    //TODO see whether that name has exactly the needed folder info.
	    this(new File(h.getName()));
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
	
	@Override
	public boolean equals(Object o){
	    if(o instanceof Chapter){
	        Chapter c = (Chapter) o;
	        return c.body.equals(this.body) && c.filename.equals(this.filename);
	    }
	    return false;
	}
	
	@Override
	public int hashCode(){
	    return filename.hashCode() * 31 + body.hashCode();
	}
	
	public static boolean isChapter(File dir, String name){
	    return IO.isTxt(dir,name); //TODO add other necessary stipulations
	}
}
