package operate;

import common.Files;
import html.HtmlChapter;
import html.HtmlEntity;
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
  
  HTML_BOOKS   (null),
  HTML_CHAPTERS(Object::toString),
  CORPUS       (HtmlEntity::txtString),
  READABLE     (Object::toString);
  
	/**
	 * <p>The actual directory</p>
	 */
	private final File folder;
	
	private final Function<HtmlEntity, String> func;
	
	private Folder(Function<HtmlEntity, String> func){
	  this.folder = new File(getFolderName());
		this.func = func;
	}
	
	/**
	 * <p>Returns a {@code File} representation of this directory.</p>
	 * @return a {@code File} representation of this directory.
	 */
	public File getFolder(){
		return folder;
	}
	
	/**
	 * <p>Returns the name of this directory.</p>
	 * @return the name of this directory.
	 */
	public String getFolderName(){
		return new StringBuilder()
		    .append(ordinal())
            .append(Files.FILENAME_COMPONENT_SEPARATOR)
            .append(toString().toLowerCase())
            .toString();
	}
	
	public void save(HtmlChapter h){
    try(OutputStreamWriter out = 
        Files.newOutputStreamWriter(folder + File.separator + h.getName())){
      for(HtmlEntity e : h){
        out.write(func.apply(e));
      }
      out.close();
    } catch(IOException e){
      //TODO implement the content of this block
      throw new RuntimeException("Cannot save the HTMLFile to the Folder");
    }
	}
}
