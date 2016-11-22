package text;

/**
 * <p>Represents a chapter of a book in the body of text 
 * being analysed. Pairs the name of the file from 
 * which content is read with the contents of the file 
 * as supplied to the constructor.</p>
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
	 * <p>Constructs a Chapter with the specified filename and 
	 * file contents.</p>
	 * @param filename the name of the file for this chapter
	 * @param body the content of this chapter's file
	 */
	public Chapter(String filename, String body) {
		this.filename = filename;
		this.body = body;
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
