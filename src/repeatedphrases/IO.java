package repeatedphrases;

import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;

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
	 * <p>The term used to separate a enclosing directories 
	 * from a file's name: {@value}</p>
	 */
	public static final String DIR_SEP = "/";
	
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
	public static final List<String> NOVELS = new ArrayList<>();
	static{
		NOVELS.add("AGOT");
		NOVELS.add("ACOK");
		NOVELS.add("ASOS");
		NOVELS.add("AFFC");
		NOVELS.add("ADWD");
	}
	
	/**
	 * <p>The number of characters in the {@link #NOVELS initialisms} 
	 * of the names of the ASOIAF novels.</p>
	 */
	public static final int NOVEL_INITIALISM_LENGTH = 4;
	
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
	 * <p>Evaluates to true if a file {@link #IS_HTML is html}, 
	 * starts with a novels {@link #NOVELS initialism}, and 
	 * is not an {@link #IS_NOVEL entire novel}.</p>
	 */
	public static final FilenameFilter IS_NOVEL_CHAPTER = 
			(dir,name) -> IS_HTML.accept(dir,name) 
			&& NOVELS.contains( name.substring(0, NOVEL_INITIALISM_LENGTH) )
			&& !IS_NOVEL.accept(dir,name);
	
	/**
	 * <p>Evaluates to true if a file's name ends with the 
	 * {@link #TXT_EXT txt extension}.</p>
	 */
	public static final FilenameFilter IS_TXT = (dir,name) -> name.endsWith(TXT_EXT);
	
	/**
	 * <p>Returns true if <code>c</code> occurs in chapters' titles, 
	 * false otherwise.</p>
	 * @param c a char to be tested for status as a character that 
	 * occurs in chapters' titles
	 * @return true if <code>c</code> is an uppercase letter, space, 
	 * or apostrophe
	 */
	public static boolean isLegalChapterTitleCharacter(char c){
		return ('A'<=c && c<='Z') || c==' ' || c=='\'';
	}
	
	/* *
	 * <p>Returns a String array containing the full relative filenames 
	 * of the files in <code>folder</code> satisfying <code>filter</code>.</p>
	 * @param folder the directory whose files' full relative addresses 
	 * are returned
	 * @param filter discriminating criteria that limits which files 
	 * are included in the returned list
	 * @return a String array containing the full relative filenames 
	 * of the files in <code>folder</code> satisfying <code>filter</code>
	 * /
	public static String[] list(Folder folder, FilenameFilter filter){
		String[] list = folder.folder().list (filter);
		for(int i=0; i<list.length; i++){
			list[i] = folder.folderName() + DIR_SEP + list[i];
		}
		return list;
	}/**/
	
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
			errorExit(filename+" for writing");
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
		//List<String> retVal = new ArrayList<>();
		//
		//try{
		//	Scanner s = new Scanner(source, ENCODING);
		//	while( continueTest.test(s) ){
		//		retVal.add(scannerOperation.apply(s));
		//	}
		//	s.close();
		//} catch(FileNotFoundException e){
		//	errorExit(source.getName() + " for reading.");
		//}
		//
		//return retVal;
		List<String> retList = null;
		try{
			retList = fileContentsAsList(
					new Scanner(source),
					scannerOperation, 
					continueTest);
		} catch( FileNotFoundException e){
			errorExit(source.getName() + " for reading");
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
		List<String> retVal = new ArrayList<>();
		
		while( continueTest.test(src) ){
			retVal.add(scannerOperation.apply(src));
		}
		src.close();
		
		return retVal;
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
		int slash = fileAddress.lastIndexOf(DIR_SEP);
		String nameInFolder = fileAddress.substring(slash+1);
		
		int dot = nameInFolder.indexOf(FILENAME_ELEMENT_DELIM);
		return dot >= 0 ? nameInFolder.substring(0,dot) : nameInFolder;
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
		int slash = fileAddress.lastIndexOf(DIR_SEP);
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
	
	/**
	 * <p>Returns a shortened form of the specified String.</p>
	 * @param phrase the phrase of which a shortened form will be returned
	 * @return the first 25 characters of phrase + " ... " + the last 25 
	 * characters if the phrase has 60 or more characters, else returns 
	 * the entire phrase.
	 */
	public static String shortForm(String phrase){
		if(phrase.length() < 60){
			return phrase;
		}
		return phrase.substring(0, 25) + " ... " + phrase.substring(phrase.length()-26);
	}
	
	/**
	 * <p>Prints a standard error message specifying that the 
	 * specified file couldn't be opened and then 
	 * {@linkplain java.lang.System#exit(int) exits}.</p>
	 * @param filename The name of the file that couldn't be opened.
	 */
	public static void errorExit(String filename){	//package-private for use by RemoveDependentPhrases
		System.out.println("Couldn't open file "+filename);
		System.exit(1);
	}
	
	/**
	 * <p>Returns the name of the file to which data for AnchorInfo 
	 * objects should be written to add anchor tags to the html 
	 * source file pertaining to the chapter to which the 
	 * specified filename pertains.</p>
	 * @param chapter a filename of the chapter that the 
	 * returned String is the name of the anchordata file for
	 * @return the name of the file to which data for AnchorInfo 
	 * objects should be written to add anchor tags to the html 
	 * source file pertaining to the chapter to which the 
	 * specified filename pertains.
	 * @see repeatedphrases.Folder#ANCHORS
	 */
	public static String anchorOutName(String chapter){
		return Folder.ANCHORS.folderName() + DIR_SEP 
				+ stripFolderExtension(chapter) 
				+ ANCHOR_EXT;
	}
	
	/**
	 * <p>The file extension for anchor-data files: {@value}</p>
	 */
	public static final String ANCHOR_EXT = ".anchordata" + TXT_EXT;
	
	/**
	 * <p>Returns the filename/address of the specified html 
	 * chapter file after the file has had links to repeated
	 * phrases later in the corpus added.</p>
	 * @param originalName the original name of the chapter 
	 * whose linked html file is named by the returned value
	 * @return the filename/address of the specified html 
	 * chapter file after the file has had links to repeated
	 * phrases later in the corpus added
	 * @see repeatedphrases.Folder.LINKED_CHAPTERS
	 */
	public static String linkedChapterName(String originalName){
		int index = originalName.lastIndexOf(DIR_SEP);
		originalName = originalName.substring( index+1 );
		index = originalName.indexOf('.');
		if(index>=0){
			originalName = originalName.substring( 0, index );
		}
		return Folder.LINKED_CHAPTERS.folderName() + DIR_SEP + originalName + ".html";
	}
	
	/* *
	 * Creates a new Scanner for the specified file.
	 * If something goes wrong while creating the 
	 * Scanner, closes the specified Closeables, 
	 * prints a message to the console, and exits.
	 * @param f			File to be scanned
	 * @param closeUs	Closeable system resources 
	 * that need to be closed before exiting in the 
	 * event of an exception.
	 * @return		Returns a new Scanner that reads 
	 * the specified file.
	 * /
	public static Scanner newScanner(File f, Closeable... closeUs){
		Scanner retVal = null;
		try{
			retVal = new Scanner(f);
		} catch(FileNotFoundException e){
			closeAll(closeUs);
			errorExit(f.getName()+" for reading");
		}
		return retVal;
	}/**/
	
	/* *
	 * Creates a new PrintStream for the specified file. 
	 * If something goes wrong while creating the 
	 * PrintStream, closes the specified Closeables, 
	 * prints a message to the console, and exits.
	 * @param f			File to be scanned
	 * @param closeUs	Closeable system resources 
	 * that need to be closed before exiting in the 
	 * event of an exception.
	 * @return		Returns a new PrintStream that 
	 * writes to the specified file.
	 * /
	public static PrintStream newPrintStream(File f, Closeable... closeUs){
		PrintStream retVal = null;
		try{
			retVal = new PrintStream(f);
		} catch(FileNotFoundException e){
			closeAll(closeUs);
			errorExit(f.getName()+" for writing");
		}
		return retVal;
	}/**/
	
	
	/* *
	 * The name for the file for repeated phrases of the specified size 
	 * and for their locations.
	 * @param size	The phrase-size for which to return a 
	 * repeats-file name.
	 * @return		the name of the file in which repeated phrases 
	 * of the specified `size` are stored.
	 * /
	public static String repeatsName(int size){
		return F08_REPEATS + DIR_SEP + F08_REPEATS+"_"+size+".txt";
	}/**/
	
	/* *
	 * Returns the name of the file for storing phrases that 
	 * are repeated in the corpus and which are independent 
	 * of instances of larger phrases.
	 * @param size		number of words in the phrases in 
	 * the file
	 * @return		the name of the file for repeated phrases 
	 * with all locations belonging to larger repeated 
	 * phrases removed.
	 * /
	public static String independentInstancesName(int size){
		return Folder.INDEPENDENT_INSTANCES.folderName() + DIR_SEP + Folder.INDEPENDENT_INSTANCES.namebase() + "_" + size + ".txt";
	}/**/
	
	/* *
	 * Returns the name of the file for storing non-unique 
	 * independent repeated phrases with the specified 
	 * number of words.
	 * @param size the number of words in the phrases to be 
	 * printed to the file with the output name.
	 * @return
	 * /
	public static String duplicateIndependentsName(int size){
		return F10_DUP_INDEPS + DIR_SEP + DUP_INDEPS_NAMEBASE + "_" + size + ".txt";
	}/**/

	/*COMMENT GRAVEYARD*/
	/* *
	 * Returns the name of the file for storing information about 
	 * non-unique independent repeated phrases from the corpus 
	 * with data about how important the phrase may be, determined 
	 * by any of a number of techniques, such as combining rarity 
	 * scores for the phrases's constituent words.
	 * @param size the number of words in the phrases in the file 
	 * whose name is returned.
	 * @return
	 * /
	@Deprecated
	public static String weightedDupIndepPhraseInstancesName(int size){
		return WEIGHTED_DUP_INDEP_PHRASE_INST_TERM + DIR_SEP + WEIGHTED_DUP_INDEP_PHRASE_INST_TERM+"_"+size+".txt";
	}/**/
	/* *
	 * The name of the directory in which the files named by 
	 * <code>weightedDupIndepPhraseInstancesName()</code> are 
	 * saved, and the base of the names of those files.
	 * /
	@Deprecated
	public static final String WEIGHTED_DUP_INDEP_PHRASE_INST_TERM = "weighted_duplicate_independent_phrase_instances";/**/
	
	/* *
	 * Returns a Database of representing all the phrase-instances 
	 * represented in the specified file.
	 * 
	 * This method assumes correctly that files describing LocatedPhrases 
	 * are structured with each Location explicitly mentioning its filename.
	 * The files that will be read were generated before LocatedPhrase 
	 * was reorganized as a HeadList<String, HeadList<>>; so, if LocatedPhrases 
	 * need to be written to files in the future, their format for such may 
	 * be inconsistent with the format that this method assumes.
	 * 
	 * @param size The phrase size pertaining to the file to be read.
	 * @return a table containing all the phrase-instances  
	 * represented by the contents of the file whose name is returned 
	 * by calling repeatsName(size).
	 * /
	public static Database databaseFromFile(File f) throws FileNotFoundException{
		
		//If file doesn't exist, locally non-caught FNFException is thrown, yielding no result
		Scanner scan = new Scanner( f );
		
		//This will be sent to Database constructor at the end of the method.
		List<Database.Record> allRecords = new ArrayList<>();
		
		while(scan.hasNextLine() && scan.hasNext()){
			
			//Separate the phrase from the locations
			String[] phraseAndLocations = scan.nextLine().split(LOCATION_DELIM);
			String phrase = phraseAndLocations[0];
			
			//For each Location represented on the line, create a Record.
			for(int i=1; i<phraseAndLocations.length; i++){
				String[] fileAndIndex = phraseAndLocations[i].split(Location.ELEMENT_DELIM);
				allRecords.add( new Database.Record(phrase, fileAndIndex[0], Integer.parseInt(fileAndIndex[1])) );
			}
		}
		
		scan.close();
		return new Database(allRecords);
	}/**/
	
	/* *
	 * Returns a table of LocatedPhrases containing all the phrases 
	 * from a file and all the locations associated with each phrase.
	 * 
	 * This method assumes correctly that files describing LocatedPhrases 
	 * are structured with each Location explicitly mentioning its filename.
	 * The files that will be read were generated before LocatedPhrase 
	 * was reorganized as a HeadList<String, HeadList<>>; so, if LocatedPhrases 
	 * need to be written to files in the future, their format for such may 
	 * be inconsistent with the format that this method assumes.
	 * 
	 * @param size The phrase size pertaining to the file to be read.
	 * @return a table containing all the LocatedPhrases 
	 * represented by the contents of the file whose name is returned 
	 * by calling repeatsName(size).
	 * /
	private static Table locatedPhrasesFromFile(File f) throws FileNotFoundException{	//package-private for use by RemoveDependentPhrases
		
		//System.out.println("Getting table of previous repeated phrases from file "+repeatsName(size));
		
		//If file doesn't exist, locally non-caught FNFException is thrown, yielding no result
		Scanner scan = new Scanner( f );
		
		Table retVal = new Table();
		
		while(scan.hasNextLine()){
			String[] phraseAndLocations = scan.nextLine().split(LOCATION_DELIM);
			
			String phrase = phraseAndLocations[0];
			List<Location> locs = new ArrayList<>(phraseAndLocations.length-1);
			for(int i=1; i<phraseAndLocations.length; i++){
				String[] fileAndIndex = phraseAndLocations[i].split(Location.ELEMENT_DELIM);
				locs.add( new Location(Integer.parseInt(fileAndIndex[1]), fileAndIndex[0]) );
			}
			retVal.addAll(phrase, locs);
			//retVal.trimRecursive();
		}
		
		scan.close();
		//retVal.trimRecursive();	//trim all lists in the object to conserve space.
		//System.out.println("Phrases from file: "+retVal.toString());//debug
		return retVal;
	}/**/
	
	/* *
	 * Prints the contents of the specified table to the specified file, 
	 * each line containing a located phrase followed by its locations.
	 * Closes the PrintStream.
	 * @param tableOrList		The table to be printed to file
	 * @param repeatedPhrases	Reference to the file to which to print 
	 * the Table.
	 * /
	private static void printPhrasesWithLocations(Iterable<LocatedPhrase> tableOrList, PrintStream repeatedPhrases){
		
		for(LocatedPhrase lp : tableOrList){
			repeatedPhrases.print( lp.head() );
			for( Location location : lp.iterableOfLocation() )
				repeatedPhrases.printf( "%s%s", LOCATION_DELIM, location.toString() );
			repeatedPhrases.println();
		}
		
		repeatedPhrases.close();
	}/**/
}
