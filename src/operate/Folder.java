package operate;

import java.io.File;

import common.IO;

/**
 * <p>Represents the folders that play significant roles in this project: Five folders for entire 
 * books of the ASOIAF series in HTML format; four folders for chapters or chapter-like bodies of 
 * text in html format, and five folders for text files used in identifying repeated phrases in 
 * the corpus and adding links for those phrases to the html chapters.</p>
 */
public enum Folder {
    
    HTML_BOOKS					  ("00_html_books",	                    null),
    HTML_BOOKS_NEWLINE			  ("01_html_books_newline",	            null),
    HTML_BOOKS_UNSTRUCTURED		  ("02_html_books_unstructured",        null),
    HTML_BOOKS_CHAPTER_CORE		  ("03_html_books_chapter_core",        null),
    HTML_BOOKS_CORRECT_APOSTROPHES("04_html_books_correct_apostrophes", null),
    HTML_CHAPTERS				  ("05_html_chapters",                  null),
    CORPUS						  ("06_corpus",                         null),
    REPEATS						  ("07_repeats",                        "repeats" ),
    INDEPENDENT_INSTANCES		  ("08_independent_instances",          "independent_instances" ),
    DUPLICATE_INDEPENDENTS		  ("09_duplicate_independents",         "duplicate_independents" ),
    ANCHORS						  ("10_anchors",                        null),
    LINKED_CHAPTERS				  ("11_linked_chapters",                null),
    READABLE					  ("12_readable",                       null);
    
	/**
	 * <p>The actual directory</p>
	 */
	private File folder;
	
	/**
	 * <p>The name of the directory, used in creating {@code folder}.</p>
	 */
	private String folderName;
	
	/**
	 * <p>The base of the name of files to be saved in or read from this directory. This is 
	 * non-null only for those directories whose contents pertain to phrases of certain sizes 
	 * rather than chapters or entire books.</p>
	 */
	private String namebase;
	
	private Folder(String name, String base ){
		folder = new File(name);
		folderName = name;
		namebase = base;
	}
	
	/**
	 * <p>Returns the name for a file in this directory pertaining to phrases of the specified 
	 * {@code size}. @param size the number of words in the phrases in the file with this name 
	 * which is in this directory or which is to be written in this directory.</p>
	 * @return the name of the file pertaining to phrases of {@code size} words in this directory.
	 */
	public String filename(int size){
		return folderName 
				+ File.separator 
				+ namebase 
				+ IO.FILENAME_COMPONENT_SEPARATOR_CHAR 
				+ size 
				+ IO.TXT_EXT;
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
		return folderName;
	}
	
	/**
	 * <p>Returns the string used as the base of the names of files in this directory. Returns 
	 * {@code null} if this directory is not {@code REPEATS}, {@code INDEPENDENT_INSTANCES}, or 
	 * {@code DUPLICATE_INDEPENDENTS}.</p>
	 * @return the string used as the base of the names of files in this directory.
	 */
	public String namebase(){
		return namebase;
	}
}
