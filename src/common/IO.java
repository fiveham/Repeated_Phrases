package common;

import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;

import text.Location;

import java.util.function.Consumer;

/**
 * <p>Provides convenience methods for reading and writing files. 
 * Stores project-wide constants for consistent access to 
 * standards.</p>
 */
public class IO {
	
	/**
	 * <p>The term used to separate a file's name from 
	 * its extension: {@value}</p>
	 */
	public static final String FILENAME_ELEMENT_DELIM = ".";
	
	/**
	 * <p>System-dependent newline returned by 
	 * {@literal System.getProperty("line.separator")}</p>
	 */
	public static final String NEW_LINE = System.getProperty("line.separator");
	
	/**
	 * <p>The html file extension: {@value}</p>
	 */
	public static final String HTML_EXT = ".html";
	
	/**
	 * <p>The txt file extension: {@value}</p>
	 */
	public static final String TXT_EXT = ".txt";
	
	/**
	 * <p>The charset used in the original html files of 
	 * the books produced by Calibre is UTF-8, and the 
	 * headers of those files indicate that fact. 
	 * For Scanners reading those files to read 
	 * correctly, they must be constructed 
	 * {@link java.util.Scanner#Scanner(File,String) with this encoding parameter}.</p>
	 * <p>The phrase-instance data files must also be 
	 * written with this encoding, because "melee" is 
	 * (after ACOK) written with characters not supported 
	 * by the (default and unchangeable) encoding of a 
	 * PrintStream.</p>
	 */
	public static final String ENCODING = "UTF-8";
	
	/**
	 * <p>The default value of the minimum number of words a 
	 * phrase needs to have for its related anchors to be 
	 * added to output files. Used when such a value is not 
	 * specified as a command-line argument.</p>
	 */
	public static final int PHRASE_SIZE_THRESHOLD_FOR_ANCHOR = 3;

	/**
	 * <p>Delimiter for {@link Location Locations} recorded in 
	 * phrase-instance files. Separates a multi-word phrase 
	 * that contains spaces from the 
	 * {@link Location#toString() string representations}  of 
	 * the locations in the corpus at which that phrase occurs.</p>
	 */
	public static final String LOCATION_DELIM = "\t";
	
	/**
	 * <p>Lists the initialisms of the published novels to make 
	 * it easier to distinguish which html chapters are from 
	 * novels and which are from novellas.</p>
	 */
	public static final List<String> NOVELS = Arrays.asList(
		"AGOT", "ACOK", "ASOS", "AFFC", "ADWD");
	
	/**
	 * <p>Evaluates to true if a file's name ends with the HTML 
	 * extension ({@value IO#HTML_EXT}).</p>
	 */
	public static final FilenameFilter IS_HTML = (dir,name) -> name.endsWith(HTML_EXT);
	
	/**
	 * <p>Evaluates to true if a file {@link #IS_HTML is html} and 
	 * has {@link #NOVELS an initialism of a novel} as the 
	 * part of its name prior to its file extension.</p>
	 */
	public static final FilenameFilter IS_NOVEL = 
			(dir,name) -> IS_HTML.accept(dir,name) 
			&& NOVELS.contains( stripExtension(name) );
	
	/**
	 * <p>Evaluates to true if a file {@link #IS_HTML is html} and 
	 * is not {@link #IS_NOVEL a novel}.</p>
	 */
	public static final FilenameFilter IS_NOVELLA = 
			(dir,name) -> IS_HTML.accept(dir,name) && !IS_NOVEL.accept(dir,name);
	
	/**
	 * <p>An underscore, used to separate the bookname, chapter-number, 
	 * and chapter name in chapter files' names.</p>
	 */
	public static final char FILENAME_COMPONENT_SEPARATOR_CHAR = '_';
	
	/**
	 * <p>An underscore, used to separate the bookname, chapter-number, 
	 * and chapter name in chapter files' names.</p>
	 */
	public static final String FILENAME_COMPONENT_SEPARATOR = "_";
	
	/**
	 * <p>Evaluates to true if a file {@link #IS_HTML is html}, 
	 * starts with a novels {@link #NOVELS initialism}, and 
	 * is not an {@link #IS_NOVEL entire novel}.</p>
	 */
	public static final FilenameFilter IS_NOVEL_CHAPTER = 
			(dir,name) -> IS_HTML.accept(dir,name) 
			&& NOVELS.contains( name.substring(0, ( name.indexOf(FILENAME_COMPONENT_SEPARATOR_CHAR)>=0 ? name.indexOf(FILENAME_COMPONENT_SEPARATOR_CHAR) : 0 ) ) )
			&& !IS_NOVEL.accept(dir,name);
	
	/**
	 * <p>Evaluates to true if a file's name ends with the 
	 * {@link #TXT_EXT txt extension}.</p>
	 */
	public static final FilenameFilter IS_TXT = (dir,name) -> name.endsWith(TXT_EXT);
	
    /**
     * <p>The default way to display a message: printing to the console.</p>
     */
    public static final Consumer<String> DEFAULT_MSG = (s) -> System.out.println(s);
    
	/**
	 * <p>Returns a new OutputStreamWriter writing to the file named <code>filename</code> 
	 * using {@link #ENCODING UTF-8 encoding}. If the result cannot be created, then 
	 * the program {@link java.lang.System#exit(int) exits} after closing all 
	 * specified <code>Closeable</code>s.</p>
	 * @param filename the name of the file to which to write
	 * @param closeUs <code>Closeable</code>s to close if the OutputStreamWriter cannot be 
	 * created
	 * @return a new OutputStreamWriter writing to the file named <code>filename</code> 
	 * using {@link #ENCODING UTF-8 encoding}
	 */
	public static OutputStreamWriter newOutputStreamWriter(String filename, Closeable... closeUs){
		OutputStreamWriter retVal = null;
		try{
			retVal = new OutputStreamWriter( new FileOutputStream( filename ), ENCODING );
		} catch(FileNotFoundException | UnsupportedEncodingException e){
			closeAll(closeUs);
			throw new RuntimeException(ERROR_EXIT_MSG + filename + " for writing.");
		}
		return retVal;
	}
	
	/**
	 * <p>Produces the {@link java.util.Scanner#nextLine() next line} of 
	 * a Scanner.</p>
	 */
	public static final Function<Scanner,String> NEXT_LINE = (s) -> s.nextLine();
	
	/**
	 * <p>Produces the {@link java.util.Scanner#next() next element} of 
	 * a Scanner.</p>
	 */
	public static final Function<Scanner,String> NEXT = (s) -> s.next();
	
	/**
	 * <p>Evaluates to true if a Scanner 
	 * {@link java.util.Scanner#hasNext() has a next element} 
	 * available.</p>
	 */
	public static final Predicate<Scanner> SCANNER_HAS_NEXT = (s) -> s.hasNext();
	
	/**
	 * <p>Evaluates to true if a Scanner 
	 * {@link java.util.Scanner#hasNextLine() has another line} 
	 * available.</p>
	 */
	public static final Predicate<Scanner> SCANNER_HAS_NEXT_LINE = (s) -> s.hasNextLine();
	
	/**
	 * <p>Evaluates to true if a Scanner 
	 * {@link java.util.Scanner#hasNextLine() has another line} and 
	 * {@link java.util.Scanner#hasNext() has a next element} available.</p>
	 */
	public static final Predicate<Scanner> SCANNER_HAS_NONEMPTY_NEXT_LINE = (s) -> s.hasNextLine() && s.hasNext();
	
	/**
	 * <p>Reads into a List each element from <code>source</code> of 
	 * the type determined by <code>scannerOperation</code> as long 
	 * as <code>continueTest</code> evaluates to true.</p>
	 * 
	 * <p>For example, use <code>scannerOperation = NEXT_LINE</code> 
	 * with <code>continueTest = SCANNER_HAS_NEXT_LINE</code> 
	 * to extract the lines of <code>source</code> as a list.</p>
	 * @param file	File to be converted into a list of Strings
	 * @param scannerOperation a Function specifying the operation 
	 * that this method's internal Scanner of <code>source</code> 
	 * uses to generate new Strings for the returned list
	 * @param continueTest a Predicate testing the state of the method's 
	 * internal Scanner of <code>source</code>
	 * @return a List containing each element of <code>source</code> of 
	 * the type produced by <code>scannerOperation</code>
	 */
	public static List<String> fileContentsAsList(File source, Function<Scanner,String> scannerOperation, Predicate<Scanner> continueTest){
		List<String> retList = null;
		try{
			retList = fileContentsAsList(
					new Scanner(source, ENCODING),
					scannerOperation, 
					continueTest);
		} catch( FileNotFoundException e){
			throw new RuntimeException(ERROR_EXIT_MSG + source.getName());
		}
		return retList;
	}
	
	/**
	 * <p>Returns a List of Strings produced by <code>src</code> when 
	 * it's sent to <code>scannerOperation.apply(src)</code>.</p>
	 * @param src a Scanner whose output elements are the elements of 
	 * the returned list
	 * @param scannerOperation an externally-defined reference to the 
	 * method that <code>src</code> uses to produce elements for the 
	 * returned list
	 * @param continueTest a Predicate that tests the state of <code>src</code> 
	 * to determine whether to continue adding elements from it to the 
	 * returned list
	 * @return a List containing each item returned by <code>src</code> 
	 * when it performs the operation specified by <code>scannerOperation</code>
	 */
	public static List<String> fileContentsAsList(Scanner src, Function<Scanner,String> scannerOperation, Predicate<Scanner> continueTest){
		List<String> result = new ArrayList<>();
		
		while( continueTest.test(src) ){
			result.add(scannerOperation.apply(src));
		}
		src.close();
		
		return result;
	}
	
	/**
	 * <p>Returns the name of the specified file without 
	 * any directory references or file extensions.</p>
	 * @param fileAddress the name of the file whose 
	 * extensionless, folder-free name is returned.
	 * @return the name of the specified file without 
	 * any directory references or file extensions
	 */
	public static String stripFolderExtension(String fileAddress){
		return stripExtension( stripFolder(fileAddress) );
	}
	
	/**
	 * <p>Returns the name of the specified file without 
	 * any file extensions.</p>
	 * @param nameInFolder the name of the file whose 
	 * extensionless name is returned.
	 * @return the name of the specified file without 
	 * any file extension
	 */
	public static String stripExtension(String nameInFolder){
		int dot = nameInFolder.indexOf(FILENAME_ELEMENT_DELIM);
		return dot >= 0 ? nameInFolder.substring(0,dot) : nameInFolder;
	}
	
	/**
	 * <p>Returns the name of the specified file without 
	 * any directory references.</p>
	 * @param fileAddress the name of the file whose 
	 * extensionless name is returned.
	 * @return the name of the specified file without 
	 * any directory references
	 */
	public static String stripFolder(String fileAddress){
		int slash = fileAddress.lastIndexOf(File.separator);
		return fileAddress.substring(slash+1);
	}
	
	/**
	 * <p>Closes the specified Closeables.</p>
	 * @param closeUs	Closeables to be closed.
	 */
	private static void closeAll(Closeable[] closeUs){
		for(Closeable c : closeUs){
			try{
				c.close();
			} catch(IOException e){
				System.out.println("By the way, an IOException happened in closing some Closeable.");
			}
		}
	}
	
	public static final String ERROR_EXIT_MSG = "I can't open the file ";
}
