package repeatedphrases;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.function.Consumer;

/**
 * <p>This class reads every phrase-instance from ASOIAF 
 * represented in the files in 
 * {@link #READ_FROM the folder for non-singular independent repeated phrases}, 
 * reorganizes that phrase-instance data by chapter, and 
 * saves it to {@link #WRITE_TO the folder for that}, 
 * where each chapter represented in the read-in data 
 * has its own output file. Each chapter-file produced 
 * contains on each line a phrase that needs anchor tags 
 * applied to it in the chapter's HTML file, the 
 * FindRepeatedPhrases-style word-index of that phrase-instance 
 * in its original file, and the Location to which this 
 * phrase-instance, once its anchor tags are applied, needs to 
 * link.</p>
 */
public class DetermineAnchors {
	
	/**
	 * <p>The folder from which this class reads phrase-instance data.</p>
	 */
	public static final Folder READ_FROM = Folder.DUPLICATE_INDEPENDENTS;
	
	/**
	 * <p>The folder to which this class writes the anchor-definitions 
	 * that it generates.</p>
	 */
	public static final Folder WRITE_TO = Folder.ANCHORS;
	
    public static void main(String[] args){
        determineAnchors(args, IO.DEFAULT_MSG);
    }
        
	/**
	 * <p>Detects all the .txt files in <code>READ_FROM</code>, reads them 
	 * all, and organizes the extracted data by chapter name and by phrase. 
	 * Saves files to <code>WRITE_TO</code> for each chapter. Each written 
	 * file contains on each line the information for one link, specifying 
	 * the phrase being linked, the location in the chapter for which the 
	 * file is named of the instance of the specified phrase on which the 
	 * link is applied, and the 
	 * {@link repeatedphrases.Location#toString() string representation} of 
	 * the Location of the instance of the phrase to which the link leads.</p>
	 * @param args command-line arguments. args[0], if present, names a file 
	 * to be used in sequencing the 
	 * @param msg a messenger that {@link System#out prints} to the console 
	 * when this class is run from the command-line or 
	 * {@link javax.swing.JLabel#setText(String) sets the text of a label} in 
	 * the GUI if this class is run from the GUI.
	 */
	public static void determineAnchors(String[] args, Consumer<String> msg) {
		Comparator<Location> phraseSorter = getPhraseSorter( args.length > 0 ? args[0] : "");
		
		msg.accept("Rendering phrase data as filebox and phrasebox.");
		
		String allAnchorablePhraseInstances = getDupPhraseData(msg);
		msg.accept("Got anchorable phrase data.");
		
		msg.accept("Generating phrase-first data structure.");
		PhraseBox phrasebox = new PhraseBox( new Scanner( allAnchorablePhraseInstances ) );
		for(String phrase : phrasebox.phrases()){
			phrasebox.get(phrase).sort(phraseSorter);
		}
		
		msg.accept("Generating chaptername-first data structure.");
		FileBox filebox = new FileBox( new Scanner( allAnchorablePhraseInstances ) );
		
		//create a file for each chapter and fill it with phrases 
		//that need to be tagged in that chapter, the locations in 
		//that chapter at which those phrase-instances appear, and 
		//references to the phrase-instances to which those phrase-
		//instances need to link.
		for(String chapter : filebox.filenames()){
			msg.accept("Creating anchor data for "+chapter);
			
			String name = anchorOutName(chapter);
			
			try(OutputStreamWriter out = IO.newOutputStreamWriter( name );){
				
				List<IntString> phrases = filebox.get(chapter);
				phrases.sort(null);
				
				for(IntString positionedPhrase : phrases){
					String phrase = positionedPhrase.phrase;
					
					List<Location> locs = phrasebox.get(phrase);
					
					Location linkTo = locAfter(locs, chapter, positionedPhrase.index);
					
					out.write( phrase + IO.LOCATION_DELIM + positionedPhrase.index + IO.LOCATION_DELIM + linkTo.toString() + IO.NEW_LINE);
				}
				
				out.close();
			} catch( IOException e){
				throw new RuntimeException(IO.ERROR_EXIT_MSG + name + " for writing.");
			}
		}
	}
	
	/**
	 * <p>Returns a {@literal Comparator<Location>} that sequences  
	 * a list of <code>Location</code>s in the order of the chapter filenames 
	 * indicated in the second tab-delimited column of the 
	 * {@link SetTrail#getTrailElements() trail file} named 
	 * <code>trailFile</code>, or the {@link #PHRASE_SORTER default} 
	 * phrase-sorter if <code>trailFile</code> does not exist or cannot 
	 * be read.</p>
	 * 
	 * <p><code>Location</code>s sorted by this Comparator such that 
	 * there first comes a block of <code>Location</code>s whose 
	 * {@link Location#getFilename() filenames} pertain to the chapter 
	 * whose name is represented in the first filename in the second 
	 * tab-delimited column of <code>trailFile</code>, followed by a 
	 * block of <code>Location</code>s whose 
	 * {@link Location#getFilename() filenames} pertain to the chapter 
	 * whose name is represented in the second filename in the second 
	 * tab-delimited column of <code>trailFile</code>, and so on. 
	 * Within each such block, <code>Location</code>s are sequenced 
	 * by {@link Location#getIndex() index}, with lower values first.</p>
	 * @param trailFile the name of the file from which a sequence of 
	 * chapter names is obtained
	 * @return a {@literal Comparator<Location>} that sequences  
	 * a list of <code>Location</code>s in the order of the chapter filenames 
	 * indicated in the second tab-delimited column of the 
	 * {@link SetTrail#getTrailElements() trail file} named 
	 * <code>trailFile</code>, or the {@link #PHRASE_SORTER default} 
	 * phrase-sorter if <code>trailFile</code> does not exist or cannot 
	 * be read.
	 */
	public static Comparator<Location> getPhraseSorter(String trailFile){
		Comparator<Location> phraseSorter = PHRASE_SORTER;
		
		File f = new File(trailFile);
		if(f.exists() && f.canRead()){
			
			List<SetTrail.TrailElement> elems = SetTrail.getTrailElements(trailFile);
			
			HashMap<String,Integer> chaptersAndIndices = new HashMap<>();
			
			for(int i=0; i<elems.size(); i++){
				chaptersAndIndices.put( IO.stripFolderExtension(elems.get(i).focus()), i );
			}
			
			phraseSorter = new Comparator<Location>(){
				
				private HashMap<String,Integer> chapterIndices = chaptersAndIndices;
				
				@Override
				public int compare(Location loc1, Location loc2){
					String chapter1 = IO.stripFolderExtension(loc1.getFilename());
					String chapter2 = IO.stripFolderExtension(loc2.getFilename());
					
					//System.out.println("NULL?: "+(chapterIndices==null?"YES":"NO") );
					
					int indexInChapter1 = chapterIndices.get(chapter1);
					int indexInChapter2 = chapterIndices.get(chapter2);
					
					return indexInChapter1 != indexInChapter2 
							? indexInChapter1 - indexInChapter2 
							: loc1.getIndex() - loc2.getIndex();
				}
			};
		}
		
		return phraseSorter;
	}
	
	/**
	 * <p>Returns a String containing all the lines of all the 
	 * files containing non-unique independent repeated 
	 * phrase information from <code>READ_FROM</code>.</p>
	 * @return  a String containing all the lines of all the 
	 * files containing non-unique independent repeated 
	 * phrase information from <code>READ_FROM</code>.
	 */
	private static String getDupPhraseData(Consumer<String> msg){
		StringBuilder sb = new StringBuilder();
		
		for(int size=FindRepeatedPhrases.MIN_PHRASE_SIZE; size<FindRepeatedPhrases.MAX_PHRASE_SIZE; size++){
			
			String name = READ_FROM.filename(size);
			msg.accept("Reading anchorable phrase data from "+IO.stripFolder(name));
			try{
				List<String> lines = IO.fileContentsAsList( 
						new Scanner(new File( name ), IO.ENCODING), 
						IO.NEXT_LINE, IO.SCANNER_HAS_NEXT_LINE );
				for(String line : lines){
					line = removeFolders(line);
					sb.append(line).append("\n");
				}
			} catch(FileNotFoundException e){
				throw new RuntimeException(IO.ERROR_EXIT_MSG + name + " for reading.");
			}
		}
		
		return sb.delete(sb.length()-1, sb.length()).toString();
	}
	
	/**
	 * <p>Returns a version of the specified line from which 
	 * all instances of the name of <code>Folder.CORPUS</code> 
	 * have been removed. The chapter-filenames included in the 
	 * phrase-instance data files written to <code>Folder.REPEATS</code>, 
	 * <code>Folder.INDEPENDENT_INSTANCES</code>, and 
	 * <code>Folder.DUPLICATE_INDEPENDENTS</code> are the addresses 
	 * of the text files in <code>Folder.CORPUS</code>.</p>
	 * @param multiLocatedPhraseLine a line from a text file from 
	 * <code>Folder.DUPLICATE_INDEPENDENTS</code> specifying a 
	 * phrase from the corpus and an arbitrary number of {@link repeatedphrases.Location#toString() Locations} 
	 * @return  a version of the specified line from which 
	 * all instances of the name of <code>Folder.CORPUS</code> 
	 * have been removed
	 */
	private static String removeFolders(String multiLocatedPhraseLine){
		return multiLocatedPhraseLine.replace( Folder.CORPUS.folderName() + File.separator, "" );
	}
	
	/**
	 * <p>Sequences <code>Location</code>s according to the name of the 
	 * book their filename starts with, in the order given by 
	 * {@link #bookList bookList}. The default 
	 * {@literal Comparator<Location>} returned by getPhraseSorter 
	 * when it's passed an invalid filename.</p>
	 */
	private static final Comparator<Location> PHRASE_SORTER = new Comparator<Location>(){
		@Override
		public int compare(Location loc1, Location loc2){
			int comp = compareFilenames(loc1.getFilename(), loc2.getFilename());
			return comp != 0 ? comp : loc1.getIndex() - loc2.getIndex();
		}
	};
	
	/**
	 * <p>Compares the filenames of two <code>Location</code>s according 
	 * to the order of the ASOIAF books given by {@link #bookList bookList}.</p>
	 * @param f1 the {@link Location#getFilename() filename} of a Location
	 * @param f2 the {@link Location#getFilename() filename} of a Location
	 * @return a negative value if <code>f1</code>'s book precedes that of 
	 * <code>f2</code>, a positive value if <code>f2's</code> precedes 
	 * <code>f1</code>'s, or zero if <code>f1</code> and <code>f2</code> 
	 * have the same book.
	 */
	private static int compareFilenames(String f1, String f2){
		
		
		
		String[] split1 = IO.stripExtension(f1).split(IO.FILENAME_COMPONENT_SEPARATOR, HTMLFile.FILENAME_ELEMENT_COUNT);
		String book1 = split1[0];
		String chapterNumber1 = split1[1];
		
		String[] split2 = IO.stripExtension(f2).split(IO.FILENAME_COMPONENT_SEPARATOR, HTMLFile.FILENAME_ELEMENT_COUNT);
		String book2 = split2[0];
		String chapterNumber2 = split2[1];
		
		int comp = bookList.indexOf(book1) - bookList.indexOf(book2);
		return comp != 0 
				? comp 
				: Integer.parseInt(chapterNumber1) - Integer.parseInt(chapterNumber2);
	}
	
	/**
	 * <p>The default order of the books of ASOIAF.</p>
	 */
	public static final List<String> bookList = new ArrayList<>();
	static{
		bookList.add("AGOT");
		bookList.add("ACOK");
		bookList.add("ASOS");
		bookList.add("AFFC");
		bookList.add("ADWD");
		bookList.add("DE");
		bookList.add("PQ");
		bookList.add("RP");
	}
	
	/**
	 * <p>Returns the Location in the list <code>locs</code> after the 
	 * Location whose {@link Location#getIndex() index} and 
	 * {@link Location#getFilename() filename} are specified by 
	 * <code>index</code> and <code>chapter</code> respectively, or 
	 * the first Location in the list if the indicated Location is the 
	 * last in the list.</p>
	 * @param locs a list of Location from which a Location is returned
	 * @param chapter the {@link Location#getFilename() filename} of the 
	 * chapter whose successor is to be returned
	 * @param index the {@link Location#getIndex() index} of the chapter 
	 * whose successor is to be returned
	 * @return  the Location in the list <code>locs</code> after the 
	 * Location whose {@link Location#getIndex() index} and 
	 * {@link Location#getFilename() filename} are specified by 
	 * <code>index</code> and <code>chapter</code> respectively, or 
	 * the first Location in the list if the indicated Location is the 
	 * last in the list
	 * @throws IllegalArgumentException if the Location specified by 
	 * <code>chapter</code> and <code>index</code> is not present in 
	 * the specified list.
	 */
	public static Location locAfter(List<Location> locs, String chapter, int index){
		Location here = new Location(index, chapter);
		int i = locs.indexOf(here);
		if(i<0){
			throw new IllegalArgumentException("The Location "+here.toString()+" is not present in the specified list.");
		} else{
			i++;
			int nextInCycle = i==locs.size() ? 0 : i;
			return locs.get(nextInCycle);
		}
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
		return Folder.ANCHORS.folderName() + File.separator 
				+ IO.stripFolderExtension(chapter) 
				+ ANCHOR_EXT;
	}
	
	/**
	 * <p>The file extension for anchor-data files: {@value}</p>
	 */
	public static final String ANCHOR_EXT = ".anchordata" + IO.TXT_EXT;
}
