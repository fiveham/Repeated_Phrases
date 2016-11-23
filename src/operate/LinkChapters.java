package operate;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.Iterator;
import java.util.Scanner;
import java.util.function.Consumer;

import common.Folder;
import common.IO;
import html.AnchorInfo;
import html.HTMLFile;
import text.Location;

/**
 * <p>For each html chapter file in {@link #READ_SUBSTANCE READ_SUBSTANCE}, anchor data from the
 * corresponding file in {@link #READ_DECORATION READ_DECORATION} is added to the html file and the
 * result is saved to {@link #WRITE_TO WRITE_TO}.</p>
 */
public class LinkChapters {
	
    /**
     * <p>The directory in which this operation saves its output files.</p>
     * @see Folder#LINKED_CHAPTERS
     */
    public static final Folder WRITE_TO = Folder.LINKED_CHAPTERS;

    /**
     * <p>The directory from which this operation reads anchor-definition files.</p>
     * @see Folder#ANCHORS
     */
    public static final Folder READ_DECORATION = Folder.ANCHORS;

    /**
     * <p>The directory from which this operation reads html chapter files.</p>
     * @see Folder#HTML_CHAPTERS
     */
    public static final Folder READ_SUBSTANCE = Folder.HTML_CHAPTERS;

    public static void main(String[] args){
        linkChapters(args, IO.DEFAULT_MSG);
    }

    /**
     * <p>For each corresponding pair of files from {@code READ_SUBSTANCE} and
     * {@code READ_DECORATION}, adds anchors based on the latter to the former and saves the result
     * to {@code WRITE_TO}.</p> <p>Only anchors of phrases with at least a certain number of words
     * specified in the command-line arguments will be applied.</p>
     * @param args command-line arguments
     */
    public static void linkChapters(String[] args, Consumer<String> msg) {

        int threshold = getPhraseSizeThreshold(args);

        msg.accept("Starting LinkChapters");

        msg.accept("Getting HTML-anchor-data pairs.");

        List<FileDataPair> fileDataPairs = 
                getFileDataPairs( 
                        READ_SUBSTANCE.folder().list(IO::isHtml), 
                        READ_DECORATION.folder().list( (dir,name) -> name.endsWith( DetermineAnchors.ANCHOR_EXT ) ) );

        msg.accept("Got "+fileDataPairs.size()+" FileDataPairs");

        for(FileDataPair pair : fileDataPairs){
            msg.accept("Adding links to "+pair.toString());
            combineFiles( pair.htmlFile, pair.anchFile, threshold );
        }
    }

    /**
     * <p>Returns the first command-line argument, if there are any, parsed as an int, if that's
     * possible, {@value #PHRASE_SIZE_THRESHOLD_FOR_ANCHOR} otherwise.</p>
     * @param args command-line arguments from {@link #main(String[]) main()}
     * @return the first command-line argument, if there are any, parsed as an int, if that's
     * possible, {@value #PHRASE_SIZE_THRESHOLD_FOR_ANCHOR} otherwise
     */
    private static int getPhraseSizeThreshold(String[] args){
        if(args.length>0){
            try{
                return Integer.parseInt(args[0]);
            } catch(NumberFormatException e){}
        }
        return IO.PHRASE_SIZE_THRESHOLD_FOR_ANCHOR;
    }

    /**
     * <p>Reads the files specified by {@code htmlFileName} and {@code anchorFile}, adds anchor tags
     * indicated by the content of {@code anchorFile} to the in-memory representation of
     * {@code htmlFileName}, and saves the modified html file to the location returned by
     * {@code IO.linkedChapterName(htmlFileName)}, which is a location in {@code WRITE_TO}
     * (13_linked_chapters).</p>
     * @param htmlFileName the name of the html chapter file to which anchors will be added
     * @param anchorFile the name of the anchor-data file from which to load anchor-definition data
     * @param threshold the minimum number of words a phrase indicated in the anchor data must have
     * to have its anchor applied.
     */
    private static void combineFiles(String htmlFileName, String anchorFile, int threshold){
        HTMLFile htmlFile = null;
        try{
            htmlFile = new HTMLFile(new File(htmlFileName));
        } catch(FileNotFoundException e){
            throw new RuntimeException(IO.ERROR_EXIT_MSG + htmlFileName + " for reading.");
        }

        List<AnchorInfo> anchorInfo = anchorInfo(new File(anchorFile));
        anchorInfo.sort(null);

        for(AnchorInfo a : anchorInfo){
            if(a.phraseSize() >= threshold){
                htmlFile.addAnchor(a);
            }
        }

        htmlFile.print(linkedChapterName(htmlFileName));
    }

    /**
     * <p>Returns a list of {@code AnchorInfo}, each element of which is based on a line from the
     * specified file.</p>
     * @param f the file from which to extract anchor-definition data
     * @return a list of {@code AnchorInfo}, each element of which is based on a line from the
     * specified file.
     */
    private static List<AnchorInfo> anchorInfo(File f){
        List<AnchorInfo> result = new ArrayList<>();

        Scanner s = null;
        try{
            s = new Scanner(f, IO.ENCODING);
        } catch(FileNotFoundException e){
            throw new RuntimeException(IO.ERROR_EXIT_MSG + f.getName() + " for reading.");
        }

        String chapter = f.getName();
        chapter = IO.stripExtension(chapter) + IO.TXT_EXT;

        while(s.hasNextLine() && s.hasNext()){
            String line = s.nextLine();	//This line is made of a phrase, tab, an int, tab, and the toString() of a Location
            String[] elements = line.split(IO.LOCATION_DELIM);

            String phrase = elements[0];
            int rawIndex = Integer.parseInt(elements[1]);

            String[] location = elements[2].split(Location.ELEMENT_DELIM);
            Location loc = new Location(Integer.parseInt(location[1]), location[0]);

            result.add( new AnchorInfo(phrase, new Location(rawIndex, chapter), loc) );
        }

        s.close();
        return result;
    }

    /**
     * <p>Returns a list of {@code FileDataPair}s pairing the html files from the folder
     * {@value Folder#READ_SUBSTANCE} with the anchor-definition files from the folder
     * {@value Folder#READ_DECORATION}.</p>
     * @param htmlFiles an array of the names of the html chapters in {@value Folder#READ_SUBSTANCE}
     * @param anchFiles an array of the names of the anchor-definition files in
     * {@code READ_DECORATION} (11_anchors)
     * @return a list of {@code FileDataPair}s pairing the html files from the folder
     * {@code READ_SUBSTANCE} with the anchor-definition files from the folder
     * {@value Folder#READ_DECORATION}.
     */
    private static List<FileDataPair> getFileDataPairs(String[] htmlFiles, String[] anchFiles){
        List<FileDataPair> result = new ArrayList<>();
        
        List<String> hList = new ArrayList<>(Arrays.asList(htmlFiles));
        List<String> aList = new ArrayList<>(Arrays.asList(anchFiles));
        
        for(String h : hList){
        	for(String a : aList){
        		if( matchNames(h,a) ){
        			result.add( new FileDataPair( 
        					READ_SUBSTANCE.folderName()  + File.separator + h, 
        					READ_DECORATION.folderName() + File.separator + a));
        			aList.remove(a);
        			break; //go to next value of h and dodge a ConcurrentModificationException
        		}
        	}
        }
        
        return result;
    }

    /**
     * <p>Returns true if {@code htmlFile}'s name prior to its file extension is the same as that of
     * {@code anchFile} prior to its file extension, false otherwise.</p>
     * @param htmlFile the name of an html chapter file from the folder
     * {@code READ_SUBSTANCE.folderName()}
     * @param anchFile the name of an anchor-definition file from the folder
     * {@code READ_DECORATION.folderName()}
     * @return true if {@code htmlFile}'s name prior to its file extension is the same as that of
     * {@code anchFile} prior to its file extension, false otherwise.
     */
    private static boolean matchNames(String htmlFile, String anchFile){
        return IO.stripFolderExtension(htmlFile).equals(IO.stripFolderExtension(anchFile));
    }
	
    /**
     * <p>Returns the filename/address of the specified html chapter file after the file has had
     * links to repeated phrases later in the corpus added.</p>
     * @param originalName the original name of the chapter whose linked html file is named by the
     * returned value
     * @return the filename/address of the specified html chapter file after the file has had links
     * to repeated phrases later in the corpus added
     * @see repeatedphrases.Folder.LINKED_CHAPTERS
     */
	public static String linkedChapterName(String originalName){
		int index = originalName.lastIndexOf(File.separator);
		originalName = originalName.substring( index+1 );
		index = originalName.indexOf('.');
		if(index>=0){
			originalName = originalName.substring( 0, index );
		}
		return Folder.LINKED_CHAPTERS.folderName() + File.separator + originalName + ".html";
	}

    /**
     * <p>A pair of strings naming an html file to which anchors will be added and an
     * anchor-definition file describing the anchors to be added.</p>
     */
    public static class FileDataPair{

        /**
         * <p>The HTML file to whose content anchor tags will be added.</p>
         */
        public final String htmlFile;

        /**
         * <p>The anchor file whose contents are added to those of {@code htmlFile} as anchor
         * tags.</p>
         */
        public final String anchFile;

        /**
         * <p>Constructs a FileDataPair with the specified {@code htmlFile} and
         * {@code anchFile}.</p>
         * @param htmlFile the name of the HTML file to which anchor tags will be added
         * @param anchFile the name of the file from which anchor tag data is extracted
         */
        public FileDataPair(String htmlFile, String anchFile){
            this.htmlFile = htmlFile;
            this.anchFile = anchFile;
        }

        @Override
        /**
         * <p>Returns a String made of this FileDataPair's {@code htmlFile} and {@code anchFile}
         * separated by a tab ({@code "\t"}).</p>
         */
        public String toString(){
            String html = IO.stripFolderExtension(htmlFile);
            String anch = IO.stripFolderExtension(anchFile);
            if(html.equals(anch)){
                return html;
            } else{
                throw new IllegalStateException("Chapter mismatch: different names: "+html+" "+anch);
            }
        }
    }
}
