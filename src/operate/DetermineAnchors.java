package operate;

import common.Book;
import common.IO;
import html.HTMLFile;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import operate.RepeatedPhrasesApp.TrailElement;
import text.Chapter;
import text.FileBox;
import text.Location;
import text.Quote;
import text.PhraseBox;

/**
 * <p>This class reads every quote from ASOIAF represented in the files in
 * {@link #READ_FROM the folder for non-singular independent repeated phrases}, reorganizes that
 * quote data by chapter, and saves it to {@link #WRITE_TO the folder for that}, where each chapter
 * represented in the read-in data has its own output file. Each chapter-file produced contains on
 * each line a phrase that needs anchor tags applied to it in the chapter's HTML file, the
 * FindRepeatedPhrases-style word-index of that quote in its original file, and the Location to
 * which this quote, once its anchor tags are applied, needs to link.</p>
 */
class DetermineAnchors {
    
    private static final int TRAIL_FILE_ARG_INDEX = 0;
	
    /**
     * <p>Detects all the .txt files in {@code READ_FROM}, reads them all, and organizes the
     * extracted data by chapter name and by phrase. Saves files to {@code WRITE_TO} for each
     * chapter. Each written file contains on each line the information for one link, specifying the
     * phrase being linked, the location in the chapter for which the file is named of the instance
     * of the specified phrase on which the link is applied, and the
     * {@link text.Location#toString() string representation} of the Location of the instance of the
     * phrase to which the link leads.</p>
     * @param op the Operation whose folders will be used
     * @param args command-line arguments. args[0], if present, names a file to be used in
     * sequencing the chapters
     * @param msg receives and handles messages output by arbitrary parts of this operation
     */
	static void determineAnchors(Operation op, String[] args, Consumer<String> msg) {
		msg.accept("Rendering phrase data as filebox and phrasebox.");
		
		String allAnchorablePhraseInstances = getDupPhraseData(op, msg);
		msg.accept("Got anchorable phrase data.");
		
		msg.accept("Generating phrase-first data structure.");
		PhraseBox phrasebox = new PhraseBox(new Scanner(allAnchorablePhraseInstances));
        Comparator<Location> phraseSorter = getPhraseSorter(args.length > TRAIL_FILE_ARG_INDEX 
                ? args[TRAIL_FILE_ARG_INDEX] 
                : "");
		for(String phrase : phrasebox.phrases()){
			phrasebox.get(phrase).sort(phraseSorter);
		}
		
		msg.accept("Generating chaptername-first data structure.");
		FileBox filebox = new FileBox(new Scanner(allAnchorablePhraseInstances));
		
		//create a file for each chapter and fill it with phrases 
		//that need to be tagged in that chapter, the locations in 
		//that chapter at which those quotes appear, and 
		//references to the quotes to which those phrase-
		//instances need to link.
		for(Chapter chapter : filebox.chapters()){
			msg.accept("Creating anchor data for "+chapter);
			
			String name = anchorOutName(chapter);
			
			try(OutputStreamWriter out = IO.newOutputStreamWriter(name)){
				
				List<Quote> quotes = filebox.get(chapter);
				quotes.sort(null);
				
				for(Quote positionedPhrase : quotes){
					String phrase = positionedPhrase.text();
					
					List<Location> locs = phrasebox.get(phrase);
					
					Location linkTo = locAfter(locs, chapter, positionedPhrase.index());
					
					out.write(
							phrase 
							+ IO.LOCATION_DELIM 
							+ positionedPhrase.index() 
							+ IO.LOCATION_DELIM 
							+ linkTo.toString() 
							+ IO.NEW_LINE);
				}
				
				out.close();
			} catch( IOException e){
				throw new RuntimeException(IO.ERROR_EXIT_MSG + name + " for writing.");
			}
		}
	}
	
    /**
     * <p>Returns a {@literal Comparator<Location>} that sequences a list of {@code Location}s in
     * the order of the chapter filenames indicated in the second tab-delimited column of the
     * {@link SetTrail#getTrailElements() trail file} named {@code trailFile}, or the
     * {@link #PHRASE_SORTER default} phrase-sorter if {@code trailFile} does not exist or cannot be
     * read.</p> <p>{@code Location}s sorted by this Comparator such that there first comes a block
     * of {@code Location}s whose {@link Location#getFilename() filenames} pertain to the chapter
     * whose name is represented in the first filename in the second tab-delimited column of
     * {@code trailFile}, followed by a block of {@code Location}s whose
     * {@link Location#getFilename() filenames} pertain to the chapter whose name is represented in
     * the second filename in the second tab-delimited column of {@code trailFile}, and so on.
     * Within each such block, {@code Location}s are sequenced by {@link Location#getIndex() index},
     * with lower values first.</p>
     * @param trailFile the name of the file from which a sequence of chapter names is obtained
     * @return a {@literal Comparator<Location>} that sequences a list of {@code Location}s in the
     * order of the chapter filenames indicated in the second tab-delimited column of the
     * {@link SetTrail#getTrailElements() trail file} named {@code trailFile}, or the
     * {@link #PHRASE_SORTER default} phrase-sorter if {@code trailFile} does not exist or cannot be
     * read.
     */
	public static Comparator<Location> getPhraseSorter(String trailFile){
		File f = new File(trailFile);
		return f.exists() && f.canRead() 
				? new AdHocComparator(trailFile) 
				: PHRASE_SORTER;
	}
	
	private static class AdHocComparator implements Comparator<Location>{
	    private final Map<String,Integer> chapterIndices;
	    
        private AdHocComparator(String trailFile){
            List<TrailElement> elems = RepeatedPhrasesApp.getTrailElements(trailFile);
            chapterIndices = new HashMap<>();
            for(int i=0; i<elems.size(); i++){
                chapterIndices.put(IO.stripFolderExtension(elems.get(i).focus()), i);
            }
        }
        
        @Override
        public int compare(Location loc1, Location loc2){
            String chapter1 = IO.stripFolderExtension(loc1.getFilename());
            String chapter2 = IO.stripFolderExtension(loc2.getFilename());
            
            int indexInChapter1 = chapterIndices.get(chapter1);
            int indexInChapter2 = chapterIndices.get(chapter2);
            
            return indexInChapter1 != indexInChapter2 
                    ? indexInChapter1 - indexInChapter2 
                    : loc1.getIndex() - loc2.getIndex();
        }
	}
	
    /**
     * <p>Returns a string containing all the lines of all the files containing non-unique
     * independent repeated phrase information from {@value Folder#READ_FROM.foldername}.</p>
     * @param op the Operation whose folders will be used
     * @return a string containing all the lines of all the files containing non-unique independent
     * repeated phrase information from {@value Folder#READ_FROM.foldername}
     */
	private static String getDupPhraseData(Operation op, Consumer<String> msg){
		StringBuilder sb = new StringBuilder();
		
		IntStream.range(FindRepeatedPhrases.MIN_PHRASE_SIZE, FindRepeatedPhrases.MAX_PHRASE_SIZE)
		        .parallel()
		        .mapToObj(op.readFrom()::filename)
		        .forEach((name) -> {
		            msg.accept("Reading anchorable phrase data from "+IO.stripFolder(name));
		            try{
		                List<String> lines = IO.fileContentsAsList( 
		                        new Scanner(new File(name), IO.ENCODING), 
		                        Scanner::nextLine, 
		                        Scanner::hasNextLine);
		                lines.forEach((line) -> sb.append(line).append("\n"));
		            } catch(FileNotFoundException e){
		                throw new RuntimeException(IO.ERROR_EXIT_MSG + name + " for reading.");
		            }
		        });
		
		return sb.delete(sb.length() - 1, sb.length()).toString();
	}
	
    /**
     * <p>Sequences {@code Location}s according to the name of the book their filename starts with,
     * in the order given by {@link #bookList bookList}. The default {@literal Comparator<Location>}
     * returned by getPhraseSorter when it's passed an invalid filename.</p>
     */
	private static final Comparator<Location> PHRASE_SORTER = 
	        Comparator.comparing(Location::getFilename, DetermineAnchors::compareFilenames)
	                .thenComparing(Location::getIndex, Integer::compare);
	
    /**
     * <p>Compares the filenames of two {@code Location}s according to the order of the ASOIAF books
     * given by {@link #bookList bookList}.</p>
     * @param f1 the {@link Location#getFilename() filename} of a Location
     * @param f2 the {@link Location#getFilename() filename} of a Location
     * @return a negative value if {@code f1}'s book precedes that of {@code f2}, a positive value
     * if {@code f2's} precedes {@code f1}'s, or zero if {@code f1} and {@code f2} have the same
     * book.
     */
	private static int compareFilenames(String f1, String f2){
		
		String[] split1 = IO.stripExtension(f1)
				.split(IO.FILENAME_COMPONENT_SEPARATOR, HTMLFile.FILENAME_ELEMENT_COUNT);
		String book1 = split1[0];
		String chapterNumber1 = split1[1];
		
		String[] split2 = IO.stripExtension(f2)
				.split(IO.FILENAME_COMPONENT_SEPARATOR, HTMLFile.FILENAME_ELEMENT_COUNT);
		String book2 = split2[0];
		String chapterNumber2 = split2[1];
		
		int comp = Book.valueOf(book1).ordinal() - Book.valueOf(book2).ordinal();
		return comp != 0 
				? comp 
				: Integer.parseInt(chapterNumber1) - Integer.parseInt(chapterNumber2);
	}
	
    /**
     * <p>Returns the Location in the list {@code locs} after the Location whose
     * {@link Location#getIndex() index} and {@link Location#getFilename() filename} are specified
     * by {@code index} and {@code chapter} respectively, or the first Location in the list if the
     * indicated Location is the last in the list.</p>
     * @param locs a list of Location from which a Location is returned
     * @param chapter the chapter whose successor is to be returned
     * @param index the {@link Location#getIndex() index} of the chapter whose successor is to be
     * returned
     * @return the Location in the list {@code locs} after the Location whose
     * {@link Location#getIndex() index} and {@link Location#getFilename() filename} are specified
     * by {@code index} and {@code chapter} respectively, or the first Location in the list if the
     * indicated Location is the last in the list
     * @throws IllegalArgumentException if the Location specified by {@code chapter} and
     * {@code index} is not present in the specified list.
     */
	public static Location locAfter(List<Location> locs, Chapter chapter, int index){
		Location here = new Location(index, chapter);
		int i = locs.indexOf(here);
		if(i < 0){
			throw new IllegalArgumentException(
					"The Location "+here.toString() + " is not present in the specified list.");
		} else{
			i++;
			int nextInCycle = (i == locs.size()) 
			        ? 0 
			        : i;
			return locs.get(nextInCycle);
		}
	}
	
    /**
     * <p>Returns the name of the file to which data for AnchorInfo objects should be written to add
     * anchor tags to the html source file pertaining to the chapter to which the specified filename
     * pertains.</p>
     * @param chapter a filename of the chapter that the returned String is the name of the
     * anchordata file for
     * @return the name of the file to which data for AnchorInfo objects should be written to add
     * anchor tags to the html source file pertaining to the chapter to which the specified filename
     * pertains.
     * @see operate.Folder#ANCHORS
     */
	public static String anchorOutName(Chapter chapter){
		return Folder.ANCHORS.folderName() 
		        + File.separator 
				+ IO.stripFolderExtension(chapter.getName()) 
				+ ANCHOR_EXT;
	}
	
    /**
     * <p>The file extension for anchor-data files: {@value}</p>
     * @see IO#TXT_EXT
     */
	public static final String ANCHOR_EXT = ".anchordata" + IO.TXT_EXT;
}
