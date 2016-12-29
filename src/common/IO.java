package common;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * <p>Provides convenience methods for reading and writing files. Stores project-wide constants for 
 * consistent access to standards.</p>
 */
public class IO {
    
    /**
     * <p>Maximum size of repeated phrases to be found. The value {@value #MAX_PHRASE_SIZE} was
     * determined empirically, and pertains to an overlap of text between AFFC Samwell I and ADWD
     * Jon II.</p>
     */
    public static final int MAX_PHRASE_SIZE = 218;
    
    public static final char RIGHT_SINGLE_QUOTE = '\u2019';
    
    public static final char RIGHT_DOUBLE_QUOTE = '\u201D';
    
	private static final String ERROR_EXIT_MSG = "Can't open the file ";
	
	/**
	 * <p>System-dependent newline returned by {@literal System.getProperty("line.separator")}</p>
	 */
	public static final String NEW_LINE = System.getProperty("line.separator");
	
	/**
	 * <p>The html file extension: {@value}</p>
	 */
	public static final String HTML_EXT = ".html";
	
	/**
	 * <p>The charset used in the original html files of the books produced by Calibre is UTF-8, 
	 * and the headers of those files indicate that fact. For Scanners reading those files to read 
	 * correctly, they must be constructed {@link java.util.Scanner#Scanner(File,String) with this 
	 * encoding parameter}.</p>
	 * <p>The quote data files must also be written with this encoding, because "melee" is 
	 * (after ACOK) written with characters not supported by the default encoding of a 
	 * PrintStream.</p>
	 */
	public static final String ENCODING = "UTF-8";
	
	/**
	 * <p>An underscore, used to separate the bookname, chapter-number, and chapter name in chapter 
	 * files' names.</p>
	 */
	public static final char FILENAME_COMPONENT_SEPARATOR_CHAR = '_';
	
	/**
	 * <p>An underscore, used to separate the bookname, chapter-number, and chapter name in chapter 
	 * files' names.</p>
	 */
	public static final String FILENAME_COMPONENT_SEPARATOR = "_";
	
    /**
     * <p>Returns true if {@code s} {@link Scanner#hasNext() has a next token} and 
     * {@link Scanner#hasNextLine() a next line}, false otherwise.</p>
     * @param s the Scanner to be tested
     * @return true if {@code s} {@link Scanner#hasNext() has a next token} and 
     * {@link Scanner#hasNextLine() a next line}, false otherwise
     */
	public static boolean scannerHasNextAndNextLine(Scanner s){
		return s.hasNextLine() && s.hasNext();
	}
	
	/**
	 * <p>Returns a new OutputStreamWriter writing to the file named {@code filename} using 
	 * {@link #ENCODING UTF-8 encoding}. If the result cannot be created, then the program 
	 * {@link java.lang.System#exit(int) exits} after closing all specified {@code Closeable}s.</p>
	 * @param filename the name of the file to which to write
	 * @param closeUs {@code Closeable}s to close if the OutputStreamWriter cannot be created
	 * @return a new OutputStreamWriter writing to the file named {@code filename} using 
	 * {@link #ENCODING UTF-8 encoding}
	 */
	public static OutputStreamWriter newOutputStreamWriter(String filename, Closeable... closeUs){
		OutputStreamWriter retVal = null;
		try{
			retVal = new OutputStreamWriter(new FileOutputStream(filename), ENCODING);
		} catch(FileNotFoundException | UnsupportedEncodingException e){
			closeAll(closeUs);
			throw new RuntimeException(ERROR_EXIT_MSG + filename + " for writing.");
		}
		return retVal;
	}
	
	public static Stream<String> fileContentStream(
	        File source, 
	        Function<Scanner, String> operation, 
	        Predicate<Scanner> test){
	    
	    Scanner s = null;
	    try{
	        s = new Scanner(source, ENCODING);
	    } catch(FileNotFoundException e){
	        throw new RuntimeException(ERROR_EXIT_MSG + source.getName());
	    }
	    
	    Stream.Builder<String> builder = Stream.builder();
	    while(test.test(s)){
	        builder.accept(operation.apply(s));
	    }
	    s.close();
	    return builder.build();
	}
	
	/**
	 * <p>Returns the name of the specified file without any directory references or file 
	 * extensions.</p>
	 * @param fileAddress the name of the file whose extensionless, folder-free name is returned.
	 * @return the name of the specified file without any directory references or file extensions
	 */
	public static String stripFolderExtension(String fileAddress){
		return stripExtension(stripFolder(fileAddress));
	}

    /**
     * <p>The term used to separate a file's name from its extension: {@value}</p>
     */
    public static final String FILENAME_ELEMENT_DELIM = ".";
    
	/**
	 * <p>Returns the name of the specified file without any file extensions.</p>
	 * @param nameInFolder the name of the file whose extensionless name is returned.
	 * @return the name of the specified file without any file extension
	 */
	public static String stripExtension(String nameInFolder){
		int dot = nameInFolder.lastIndexOf(FILENAME_ELEMENT_DELIM);
		return dot >= 0 
				? nameInFolder.substring(0, dot) 
				: nameInFolder;
	}
	
	/**
	 * <p>Returns the name of the specified file without any directory references.</p>
	 * @param fileAddress the name of the file whose extensionless name is returned.
	 * @return the name of the specified file without any directory references
	 */
	public static String stripFolder(String fileAddress){
		int slash = fileAddress.lastIndexOf(File.separator);
		return fileAddress.substring(slash + 1);
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
				System.out.println(
						"By the way, an IOException happened in closing some Closeable.");
			}
		}
	}
}
