package operate;

import common.IO;
import html.HTMLEntity;
import html.HTMLFile;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.function.Function;

/**
 * <p>Represents the folders that play significant roles in this project: Five folders for entire 
 * books of the ASOIAF series in HTML format; four folders for chapters or chapter-like bodies of 
 * text in html format, and five folders for text files used in identifying repeated phrases in 
 * the corpus and adding links for those phrases to the html chapters.</p>
 */
public enum Folder {
    
    HTML_BOOKS      (null),
    HTML_CHAPTERS   (Object::toString),
    CORPUS          (HTMLEntity::txtString),
    ANCHORS         (null),
    LINKED_CHAPTERS (null),
    READABLE        (Object::toString);
    
	/**
	 * <p>The actual directory</p>
	 */
	private final File folder;
	
	private final Function<HTMLEntity, String> func;
	
	private Folder(Function<HTMLEntity, String> func){
	    this.folder = new File(folderName());
		this.func = func;
	}
	
	/**
	 * <p>Returns the name for a file in this directory pertaining to phrases of the specified 
	 * {@code size}. @param size the number of words in the phrases in the file with this name 
	 * which is in this directory or which is to be written in this directory.</p>
	 * @return the name of the file pertaining to phrases of {@code size} words in this directory.
	 */
	public String filename(int size){
		return new StringBuilder(folderName())
				.append(File.separator)
				.append(IO.FILENAME_COMPONENT_SEPARATOR_CHAR)
				.append(size)
				.append(IO.TXT_EXT)
				.toString();
	}
	
	/**
	 * <p>Returns a {@code File} representation of this directory.</p>
	 * @return a {@code File} representation of this directory.
	 */
	public File folder(){
		return folder;
	}
	
	/**
	 * <p>Returns the name of this directory.</p>
	 * @return the name of this directory.
	 */
	public String folderName(){
		return new StringBuilder()
		        .append(ordinal())
                .append(IO.FILENAME_COMPONENT_SEPARATOR)
                .append(toString().toLowerCase())
                .toString();
	}
	
	public void save(HTMLFile h){
	    try(OutputStreamWriter out = 
	            IO.newOutputStreamWriter(folder + File.separator + h.getName())){
	        for(HTMLEntity e : h){
	            out.write(func.apply(e));
	        }
	        out.close();
	    } catch(IOException e){
	        //TODO implement the content of this block
	        throw new RuntimeException("Cannot save the HTMLFile to the Folder");
	    }
	}
}
