package operate;

import html.Direction;
import html.HTMLEntity;
import html.HTMLFile;
import html.Tag;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import common.Folder;
import common.IO;

/**
 * <p>Opens the file specified as a command-line argument 
 * and assigns the files named in the second tab-delimited 
 * column therein links to the other files on the same 
 * line. The file named in the first column is linked 
 * as the previous chapter, and the file named in the 
 * third column is linked as the next chapter.</p>
 * 
 * <p>This allows you to set the addresses to which the 
 * "previous chapter" and "next chapter" arrows at the 
 * top and bottom of the ASOIAF chapter html files link.</p>
 * 
 * <p>The file should be formatted as three columns of tab-
 * delimited values. Each line gives specifications for a 
 * single chapter, named in the middle column. The left-hand 
 * column specifies the file to be linked as "previous chapter", 
 * and the right-hand column indicates the file to be linked 
 * as "next chapter". Leaving the left or right column empty 
 * for a given middle column value causes the corresponding 
 * link(s) for that file to be set targeting the empty string 
 * as their address so they go nowhere.</p>
 * 
 * <p>These trail files are also used to determine the sorting 
 * order of chapters; so, they must have an entry for each 
 * existing chapter.</p>
 */
public class SetTrail {

    /**
     * <p>The folder from which this class reads HTML chapters to 
     * which to add links for previous and next chapters.</p>
     * @see Folder#LINKED_CHAPTERS
     */
    public static final Folder READ_FROM = Folder.LINKED_CHAPTERS;

    /**
     * <p>The folder where this class writes the html chapter files 
     * to which it has added links for previous and next chapters.</p>
     * @see Folder#READABLE
     */
    public static final Folder WRITE_TO = Folder.READABLE;

    /**
     * <p>The string that names the "id" attribute of an html tag.</p>
     */
    public static final String ID_ATTRIB = "id";

    /**
     * <p>The value of the id attribute of the anchors in the 
     * head and foot tables for html chapters which link to 
     * the previous chapter.</p>
     */
    public static final String PREV_CHAPTER = "prev_chapter";

    /**
     * <p>The value of the id attribute for the anchors in the 
     * head and foot tables for html chapters which link to 
     * the next chapter.</p>
     */
    public static final String NEXT_CHAPTER = "next_chapter";

    /**
     * <p>The text of the html "href" attribute followed by an 
     * equals sign and a quote.</p>
     */
    public static final String HREF_START = "href=\"";

    /**
     * <p>Calls {@link #setTrail(String[],Consumer<String>) setTrail()}.</p>
     * @param args 
     */
    public static void main(String[] args){
        setTrail(args, IO.DEFAULT_MSG);
    }

    /**
     * <p>Reads the html chapter files from <code>READ_FROM</code> and writes 
     * modified versions of them with links to previous and next chapters 
     * added according to the data in the trail file named by the first 
     * command-line argument to the folder <code>WRITE_TO</code>.</p>
     * @param args command-line arguments
     * @param msg
     */
    public static void setTrail(String[] args, Consumer<String> msg) {
        if(args.length < 1){
            throw new IllegalArgumentException("SetTrail: I need a trial file.");
        }
        String trailSource = args[0];
        msg.accept("Getting trail data from " + trailSource );
        List<TrailElement> elements = getTrailElements( trailSource );

        for(int i=0; i<elements.size(); i++){
        	TrailElement node = elements.get(i);
            msg.accept("Trail-linking "+node.focus());
            
            File fileToModify = new File( READ_FROM.folderName() + File.separator + node.focus());
            
            if(fileToModify.exists()){
                HTMLFile file = null;
                try{
                    file = new HTMLFile(fileToModify);
                } catch( FileNotFoundException e){
                	throw new RuntimeException(IO.ERROR_EXIT_MSG + READ_FROM.folderName() + File.separator + node.focus() + " for reading");
                }

                setAdjacentChapterLinks(file, PREV_CHAPTER, ID_ATTRIB, availableConnected(elements, i, TrailElement.PREV));
                setAdjacentChapterLinks(file, NEXT_CHAPTER, ID_ATTRIB, availableConnected(elements, i, TrailElement.NEXT));
                
                file.print( WRITE_TO.folderName() + File.separator + node.focus());
            }
        }
    }
    
    private static String availableConnected(List<TrailElement> elements, int index, Function<TrailElement,String> connection){
    	TrailElement node = elements.get(index);
    	
    	List<String> visited = new ArrayList<>();
    	
    	String name = null;
    	while( !new File(READ_FROM.folderName() + File.separator + (name=connection.apply(node)) ).exists() && !visited.contains(name) ){
    		visited.add(name);
    		
    		node = getTrailElementWithFocus(elements, name);
    		
    	}
    	
    	return visited.contains(name) ? "" : connection.apply(node);
    }
    
    private static TrailElement getTrailElementWithFocus(List<TrailElement> elements, String focus){
    	for(int i=0; i<elements.size(); i++){
    		if( elements.get(i).focus().equals(focus) ){
    			return elements.get(i);
    		}
    	}
    	throw new NoSuchElementException("No entry for file \""+focus+"\" in the trail file.");
    }

    /**
     * <p>Finds all anchor tags with the specified value of the 
     * specified attribute in the specified html file and changes 
     * the values of the href attributes of those anchors to 
     * <code>address</code>.</p>
     * @param file the html file whose anchor tags with the specified 
     * attribute and value are being modified
     * @param idValue the value to be used in determining which 
     * anchors to modify
     * @param idAttrib the attribute to use in determining which 
     * anchors to modify
     * @param address the relative address of an html chapter 
     * file to which to link as an adjacent chapter
     */
    private static void setAdjacentChapterLinks(HTMLFile file, String idValue, String idAttrib, String address){
        Predicate<HTMLEntity> isAnchorWithMatchID = (h) -> isAnchorWithMatchID(h, idValue, idAttrib);
        int pointer = -1;
        while( -1 != (pointer=file.adjacentElement(pointer, isAnchorWithMatchID, Direction.NEXT))){

            String tag = file.get(pointer).toString();
            tag = tag.substring(1,tag.length()-1);

            file.set(pointer, new Tag( anchor(tag, address)));
        }
    }
    
    private static boolean isAnchorWithMatchID(HTMLEntity h, String idValue, String idAttrib){
    	if(Tag.class.isInstance(h)){
    		Tag t = (Tag) h;
    		return t.isType(Tag.A) && idValue.equals(t.valueOfAttribute(idAttrib)); 
    	}
    	return false;
    }

    /**
     * <p>The closing quote for an html tag attribute's value.</p>
     */
    public static final String QUOTE = "\"";

    /**
     * <p>The title attribute of an html tag and the quote that 
     * begins the attribute's value.</p>
     */
    public static final String TITLE_START = "title=\"";

    /**
     * <p>Returns a String based on <code>tag</code>, with the value 
     * of the pre-existing href attribute replaced by the parameter 
     * <code>address</code> and with the value of the pre-existing 
     * title attribute replaced by a chapter title extracted from 
     * <code>address</code> by calling {@link #title(String) title(address)}.</p>
     * 
     * <p>For example, 
     * <code>anchor("<a href=\"no.html\" title=\"no\">", "book_0_yes_yes.html")</code> 
     * would return "<a href=\"book_0_yes_yes.html\" title=\"yes yes\">".</p>
     * @param tag
     * @param address
     * @return
     */
    private static String anchor(String tag, String address){
        return replaceValueOfAttribute( replaceValueOfAttribute(tag, HREF_START, address), TITLE_START, title(address));
    }

    /**
     * <p>Returns the value for the title attribute of an anchor tag 
     * based on the specified address to which the anchor links.</p>
     * 
     * <p>Returns <code>address</code> with its book name, chapter index, 
     * and file extension stripped away and underscores replaced 
     * with spaces.</p>
     * @param address the address of an html file for a chapter being 
     * linked.
     * @return <code>address</code> with its book name, chapter index, 
     * and file extension stripped away.
     */
    private static String title(String address){
        if(address.isEmpty()){
            return address;
        }
        String name = IO.stripFolderExtension(address);
        String withoutBook = name.substring(name.indexOf(IO.FILENAME_COMPONENT_SEPARATOR_CHAR)+1);
        String withoutIndx = withoutBook.substring(withoutBook.indexOf(IO.FILENAME_COMPONENT_SEPARATOR_CHAR)+1);
        return withoutIndx.replace(IO.FILENAME_COMPONENT_SEPARATOR_CHAR,' ');
    }

    /**
     * <p>Replaces the pre-existing value of the attribute specified by 
     * <code>attributeStart</code> in the specified <code>body</code> 
     * of an html tag with <code>installValue</code>.</p>
     * 
     * @param body the text of an html tag of which a modified version 
     * is returned
     * 
     * @param attributeStart identifies the attribute whose value is 
     * to be modified. Must  be the name of an attribute 
     * followed by an equals sign followed by a double quote, such as 
     * {@link #TITLE_START TITLE_START} or {@link #HREF_START HREF_START}.
     * 
     * @param installValue the value of the attribute named by 
     * <code>attributeStart</code> to install in place of the 
     * pre-existing value
     * 
     * @return  the pre-existing value of the attribute specified by 
     * <code>attributeStart</code> in the specified <code>body</code> 
     * of an html tag with <code>installValue</code>
     */
    private static String replaceValueOfAttribute(String body, String attributeStart, String installValue){
        int start = body.indexOf(attributeStart)+attributeStart.length();
        int end = body.indexOf(QUOTE, start);
        String front = body.substring(0,start);
        String back = body.substring(end);
        return front + installValue + back;
    }

    /**
     * <p>Number of columns to anticipate in input file: {@value}</p>
     * 
     * <p>The fourth column is unused at this time, 
     * but expecting it allows the third column to 
     * be cleanly isolated if a fourth column exists.</p>
     */
    public static final int COLUMN_COUNT = 4;

    /**
     * <p>Returns a list of <code>TrailElement</code>s describing each 
     * chapter's predecessor and successor.</p>
     * @param trailFilename the name of the trail-file from which 
     * trail data is extracted
     * @return a list of <code>TrailElement</code>s describing each 
     * chapter's predecessor and successor
     */
    public static List<TrailElement> getTrailElements(String trailFilename){
        List<String> lines = IO.fileContentsAsList(
        		new File(trailFilename), 
        		Scanner::nextLine, 
        		IO.SCANNER_HAS_NONEMPTY_NEXT_LINE);
        List<TrailElement> result = new ArrayList<>();
        for(String line : lines){
            String[] s = line.split("\t", COLUMN_COUNT);
            result.add( new TrailElement( s[0], s[1], s[2] ) );
        }
        return result;
    }

    /**
     * <p>Represents an element of a chapter trail, a sequence 
     * of backward and forward links between chapters.</p>
     */
    public static class TrailElement implements Comparable<TrailElement>{
    	
    	public static final Function<TrailElement,String> PREV = (te) -> te.prev();
    	
    	public static final Function<TrailElement,String> NEXT = (te) -> te.next();

        /**
         * <p>The chapter to be linked as the preceding chapter in the trail.</p>
         */
        private final String prev;

        /**
         * <p>The chapter for which links to the specified preceding and 
         * succeeding chapters are to be installed.</p>
         */
        private final String focus;

        /**
         * <p>The chapter to be linked as the succeeding chapter in the trail.</p>
         */
        private final String next;

        /**
         * <p>Constructs a TrailElement indicating that the chapter 
         * named by <code>focus</code> has the chapter named by 
         * <code>prev</code as its predecessor and the chapter 
         * named by <code>next</code> as its successor.</p>
         * @param prev the chapter before <code>focus</code> in sequence
         * @param focus the chapter in which links to <code>prev</code> 
         * and <code>next</code> are to be installed
         * @param next the chapter after <code>focus</code> in sequence
         */
        public TrailElement(String prev, String focus, String next){
            this.prev = prev;
            this.focus = focus;
            this.next = next;
        }

        /**
         * <p>Compares two TrailElements, first by their 
         * <code>focus</code>, then by their <code>prev</code>, 
         * and last by their <code>next</code>.</p>
         * @return an int whose sign reflects the natural ordering 
         * between this TrailElement and <code>t</code>
         */
        @Override
        public int compareTo(TrailElement t){
            int comp = focus.compareTo(t.focus);
            if(comp!=0){
                return comp;
            } else if(0 != (comp = prev.compareTo(t.prev))){
                return comp;
            } else{
                return next.compareTo(t.next);
            }
        }

        /**
         * <p>Returns {@link #prev prev}.</p>
         * @return {@link #prev prev}
         */
        public String prev(){
            return prev;
        }

        /**
         * <p>Returns {@link #focus focus}.</p>
         * @return {@link #focus focus}
         */
        public String focus(){
            return focus;
        }

        /**
         * <p>Returns {@link #next next}.</p>
         * @return {@link #next next}
         */
        public String next(){
            return next;
        }
    }
}
