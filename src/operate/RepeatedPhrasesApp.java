package operate;

import common.IO;
import html.Direction;
import html.HTMLEntity;
import html.HTMLFile;
import html.Tag;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import text.Chapter;

//TODO use the following structure:
//stage 1: ensure folders exist
//stage 2: ensure chapters are available
//stage 3: construct a graph linking chapters with quotes with locations and phrases ... etc.
//stage 4: reduce the graph, leaving each Location with a single Quote
//stage 5: link quotes and add links to html files
public class RepeatedPhrasesApp {
    
    private Collection<Chapter> chapters = null;
    private final Consumer<String> msg;
    private final boolean recordHtmlChapters;
    private final boolean recordTextChapters;
    private final boolean generateChapters;
    
    public RepeatedPhrasesApp(
            Consumer<String> msg, 
            boolean recordHtmlChapters, 
            boolean recordTextChapters, 
            boolean generateChapters){
        
        this.msg = msg;
        this.recordHtmlChapters = recordHtmlChapters;
        this.recordTextChapters = recordTextChapters;
        this.generateChapters = generateChapters;
    }
    
    public Collection<Chapter> getChapters(){
        if(chapters == null){
            chapters = initChapters();
        }
        return chapters;
    }
    
    private Collection<Chapter> initChapters(){
        return generateChapters
                ? novelsToChapters() 
                : readChapters();
    }
	
    private static Collection<Chapter> readChapters(){
        return Stream.of(Folder.CORPUS.folder().listFiles(Chapter::isChapter))
                .map(Chapter::new)
                .collect(Collectors.toList());
    }
    
    /**
     * <p>Ensures that the working directory has the folders specified in
     * {@link Folders Folders}.</p>
     */
	public void ensureFolders(Consumer<String> msg){
		for(Folder f : Folder.values()){
			File name = f.folder();
			if(!name.exists()){
				msg.accept("Creating "+name.getName());
				name.mkdir();
			}
		}
	}
	
    /**
     * <p>Calls the main methods of HtmlToText, FindRepeatedPhrases, RemoveDependentPhrases,
     * RemoveUniqueIndependents, DetermineAnchors, LinkChapters, and SetTrail. Passes the first
     * command line argument to DetermineAnchors and SetTrail, and passes the second command-line
     * argument to LinkChapters if it is present and if it parses as an int.</p>
     * @param args command-line arguments
     * @param msg
     */
    public void isolateChaptersAndLink(String[] args, Consumer<String> msg) {
    	int limit = validateArgs(args);
    	
        ensureFolders(msg);
        
        msg.accept("Newlining parargraphs");
        Operation.NEWLINE_P.operate(null, msg);
        
        msg.accept("Removing inconsistent divs etc");
        Operation.CLEAR_EXCESS_STRUCTURE.operate(null, msg);
        
        msg.accept("Removing non-chapter matter");
        Operation.CLEAR_FRONT_AND_BACK_MATTER.operate(null, msg);
        
        msg.accept("Normalizing apostrophes");
        Operation.SWAP_APOSTROPHES.operate(null, msg);
        
        msg.accept("Splitting books into chapters");
        Operation.SPLIT_CHAPTERS.operate(null, msg);
        
        msg.accept("Creating plaintext corpus");
        Operation.HTML_TO_TEXT.operate(null, msg);
        
        msg.accept("Finding repeat phrases in corpus");
        Operation.FIND_REPEATED_PHRASES.operate(null, msg);
        
        msg.accept("Ignoring dependent quotes");
        Operation.REMOVE_DEPENDENT_PHRASES.operate(null, msg);
        
        msg.accept("Ignoring unique independent instances");
        Operation.REMOVE_UNIQUE_INDEPENDENTS.operate(null, msg);
        
        linksAndTrail(limit, trailArgs(args));
    }
    
    private static final int TRAIL_FILE_ARG_INDEX = 0;
    private static final int PHRASE_SIZE_THRESHOLD_ARG_INDEX = 1;
    
    /**
     * <p>Checks that the command-line arguments passed to main() include an existing file to be
     * passed to SetTrail, and returns the int value of the second command-line argument, if it is
     * present and parses as an int, for use as the phrase-size threshold passed to
     * {@link LinkChapters#main(String[]) LinkChapters}.</p>
     * @param args command-line arguments passed from main()
     * @return the int value of the second command-line argument, if it is present and parses as an
     * int, {@value #IO.PHRASE_SIZE_FOR_ANCHOR} otherwise.
     */
    private static int validateArgs(String[] args){
        if(args.length <= TRAIL_FILE_ARG_INDEX){
        	throw new IllegalArgumentException("I need a trail file.");
        } else{
            String trail = args[TRAIL_FILE_ARG_INDEX];
            if(!(new File(trail)).exists()){
                throw new IllegalArgumentException("I can't find that trail-file: \""+trail+"\".");
            }
        }
        
        try{
            return Integer.parseInt(args[PHRASE_SIZE_THRESHOLD_ARG_INDEX]);
        } catch(ArrayIndexOutOfBoundsException | NumberFormatException e){
            return IO.PHRASE_SIZE_THRESHOLD_FOR_ANCHOR;
        }
    }
    
    public void linksAndTrail(String[] args) {
    	
        int limit = validateArgs(args);
        String[] trailArgs = trailArgs(args);
        
        linksAndTrail(limit, trailArgs);
    }
    
    private String[] trailArgs(String[] args){
        return new String[]{ args[0] };
    }
    
    private void linksAndTrail(int limit, String[] trailArgs){
        msg.accept("Determining links to add to phrases");
        Operation.DETERMINE_ANCHORS.operate(trailArgs, msg);
        
        msg.accept("Adding links to html chapters");
        Operation.LINK_CHAPTERS.operate(new String[]{Integer.toString(limit)}, msg);
        
        msg.accept("Adding prev- and next-chapter links");
        Operation.SET_TRAIL.operate(trailArgs, msg);
    }
    
    public Consumer<String> getMsg(){
        return this.msg;
    }
    
    /**
     * <p></p>
     * @param save
     * @return
     */
    private Collection<Chapter> novelsToChapters(){
        //XXX do these String names include the pertinent folders or not?
        //TODO use an "is book" test against BookData elements
        String[] htmlBooks = Folder.HTML_BOOKS.folder().list(IO::isHtml);
        
        Collection<HTMLFile> htmlChapters = Stream.of(htmlBooks)
                .parallel()
                .map(File::new)
                .map(this::newHTMLFile)
                .filter(Objects::nonNull)
                .map(HTMLFile::cleanAndSplit)
                .reduce((c1,c2) -> {
                    c1.addAll(c2); 
                    return c1;
                })
                .get();
        
        Stream<HTMLFile> htmlChapterStream = htmlChapters.stream();
        
        if(recordHtmlChapters){
            htmlChapterStream.peek(Folder.HTML_CHAPTERS::save);
        }
        
        if(recordTextChapters){
            htmlChapterStream.peek(Folder.CORPUS::save);
        }
        
        chapters = htmlChapterStream
                .map(Chapter::new)
                .collect(Collectors.toList());
        
        return chapters;
    }
    
    private HTMLFile newHTMLFile(File f){
        try{
            return new HTMLFile(f);
        } catch(FileNotFoundException e){
            getMsg().accept(e.getMessage());
            return null;
        }
    }
    
    /**
     * <p>Reads the html chapter files from {@code READ_FROM} and writes modified versions of them
     * with links to previous and next chapters added according to the data in the trail file named
     * by the first command-line argument to the folder {@code WRITE_TO}.</p>
     * @param op the Operation whose folders will be used
     * @param args command-line args
     * @param msg receives and handles messages output by arbitrary parts of this operation
     */
    static void setTrail(Operation op, String[] args, Consumer<String> msg) {
        if(args.length < 1){
            throw new IllegalArgumentException("SetTrail: I need a trial file.");
        }
        String trailSource = args[0];
        msg.accept("Getting trail data from " + trailSource );
        List<TrailElement> elements = getTrailElements(trailSource);
        
        IntStream.range(0, elements.size())
                .parallel()
                .forEach((i) -> {
                    TrailElement node = elements.get(i);
                    msg.accept("Trail-linking " + node.focus());
                    
                    File fileToModify = 
                            new File(op.readFrom().folderName() + File.separator + node.focus());
                    
                    if(fileToModify.exists()){
                        HTMLFile file = null;
                        try{
                            file = new HTMLFile(fileToModify);
                        } catch(FileNotFoundException e){
                            throw new RuntimeException(
                                    IO.ERROR_EXIT_MSG 
                                    + op.readFrom().folderName() 
                                    + File.separator 
                                    + node.focus() 
                                    + " for reading");
                        }
                        
                        setAdjacentChapterLinks(
                                file, 
                                PREV_CHAPTER, 
                                ID_ATTRIB, 
                                availableConnected(op, elements, i, TrailElement::prev));
                        setAdjacentChapterLinks(
                                file, 
                                NEXT_CHAPTER, 
                                ID_ATTRIB, 
                                availableConnected(op, elements, i, TrailElement::next));
                        
                        file.print(op.writeTo().folderName() + File.separator + node.focus());
                    }
                });
    }
    
    /**
     * <p>Number of columns to anticipate in input file: {@value}</p> <p>The fourth column is unused
     * at this time, but expecting it allows the third column to be cleanly isolated if a fourth
     * column exists.</p>
     */
    private static final int COLUMN_COUNT = 4;
    
    /**
     * <p>Returns a list of {@code TrailElement}s describing each chapter's predecessor and
     * successor.</p>
     * @param trailFilename the name of the trail-file from which trail data is extracted
     * @return a list of {@code TrailElement}s describing each chapter's predecessor and successor
     */
    static List<TrailElement> getTrailElements(String trailFilename){
        return IO.fileContentsAsList(
                new File(trailFilename), 
                Scanner::nextLine, 
                IO::scannerHasNonEmptyNextLine)
                .stream()
                .map((line) -> line.split("\t", COLUMN_COUNT))
                .map(TrailElement::new)
                .collect(Collectors.toList());
    }
    
    /**
     * <p>The value of the id attribute of the anchors in the head and foot tables for html chapters
     * which link to the previous chapter.</p>
     */
    private static final String PREV_CHAPTER = "prev_chapter";
    
    /**
     * <p>The value of the id attribute for the anchors in the head and foot tables for html
     * chapters which link to the next chapter.</p>
     */
    private static final String NEXT_CHAPTER = "next_chapter";
    
    /**
     * <p>The string that names the "id" attribute of an html tag.</p>
     */
    private static final String ID_ATTRIB = "id";
    
    private static String availableConnected(
            Operation op, 
            List<TrailElement> elements, 
            int index, 
            Function<TrailElement,String> connection){
        
        TrailElement node = elements.get(index);
        
        List<String> visited = new ArrayList<>();
        
        String name = null;
        while(!new File(
                op.readFrom().folderName() 
                + File.separator 
                + (name = connection.apply(node)))
                .exists() 
                && !visited.contains(name)){
            visited.add(name);
            
            node = getTrailElementWithFocus(elements, name);
        }
        
        return visited.contains(name) 
                ? "" 
                : connection.apply(node);
    }
    
    private static TrailElement getTrailElementWithFocus(List<TrailElement> elements, String focus){
        Optional<TrailElement> result = IntStream.range(0, elements.size())
                .mapToObj(elements::get)
                .filter((e) -> e.focus().equals(focus))
                .findFirst();
        if(result.isPresent()){
            return result.get();
        } else{
            throw new NoSuchElementException("No entry for file \""+focus+"\" in the trail file.");
        }
    }
    
    /**
     * <p>Finds all anchor tags with the specified value of the specified attribute in the specified
     * html file and changes the values of the href attributes of those anchors to
     * {@code address}.</p>
     * @param file the html file whose anchor tags with the specified attribute and value are being
     * modified
     * @param idValue the value to be used in determining which anchors to modify
     * @param idAttrib the attribute to use in determining which anchors to modify
     * @param address the relative address of an html chapter file to which to link as an adjacent
     * chapter
     */
    private static void setAdjacentChapterLinks(
            HTMLFile file, 
            String idValue, 
            String idAttrib,
            String address){
        
        Predicate<HTMLEntity> isAnchorWithMatchID = 
                (h) -> isAnchorWithMatchID(h, idValue, idAttrib);
        int pointer = INIT_POINTER;
        while(INIT_POINTER 
                != (pointer = file.adjacentElement(pointer, isAnchorWithMatchID, Direction.NEXT))){
            
            String tag = file.get(pointer).toString();
            tag = tag.substring(1,tag.length()-1);
            
            file.set(pointer, new Tag(anchor(tag, address)));
        }
    }
    
    private static final int INIT_POINTER = -1;
    
    private static boolean isAnchorWithMatchID(HTMLEntity h, String idValue, String idAttrib){
        if(Tag.class.isInstance(h)){
            Tag t = (Tag) h;
            return t.isType(Tag.A) && idValue.equals(t.valueOfAttribute(idAttrib)); 
        }
        return false;
    }
    
    /**
     * <p>Returns a String based on {@code tag}, with the value of the pre-existing href attribute
     * replaced by the parameter {@code address} and with the value of the pre-existing title
     * attribute replaced by a chapter title extracted from {@code address} by calling
     * {@link #title(String) title(address)}.</p> <p>For example,
     * {@code anchor("<a href=\"no.html\" title=\"no\">", "book_0_yes_yes.html")} would return "<a
     * href=\"book_0_yes_yes.html\" title=\"yes yes\">".</p>
     * @param tag
     * @param address
     * @return
     */
    private static String anchor(String tag, String address){
        return replaceValueOfAttribute(
                replaceValueOfAttribute(tag, HREF_START, address), 
                TITLE_START, 
                title(address));
    }
    
    /**
     * <p>Replaces the pre-existing value of the attribute specified by {@code attributeStart} in
     * the specified {@code body} of an html tag with {@code installValue}.</p>
     * @param body the text of an html tag of which a modified version is returned
     * @param attributeStart identifies the attribute whose value is to be modified. Must be the
     * name of an attribute followed by an equals sign followed by a double quote, such as
     * {@link #TITLE_START TITLE_START} or {@link #HREF_START HREF_START}.
     * @param installValue the value of the attribute named by {@code attributeStart} to install in
     * place of the pre-existing value
     * @return the pre-existing value of the attribute specified by {@code attributeStart} in the
     * specified {@code body} of an html tag with {@code installValue}
     */
    private static String replaceValueOfAttribute(
            String body, 
            String attributeStart, 
            String installValue){
        
        int start = body.indexOf(attributeStart)+attributeStart.length();
        int end = body.indexOf(QUOTE, start);
        String front = body.substring(0,start);
        String back = body.substring(end);
        return front + installValue + back;
    }
    
    /**
     * <p>The closing quote for an html tag attribute's value.</p>
     */
    private static final String QUOTE = "\"";
    
    /**
     * <p>Returns the value for the title attribute of an anchor tag based on the specified address
     * to which the anchor links.</p> <p>Returns {@code address} with its book name, chapter index,
     * and file extension stripped away and underscores replaced with spaces.</p>
     * @param address the address of an html file for a chapter being linked.
     * @return {@code address} with its book name, chapter index, and file extension stripped away.
     */
    private static String title(String address){
        if(address.isEmpty()){
            return address;
        }
        String name = IO.stripFolderExtension(address);
        String withoutBook = name.substring(1 + name.indexOf(IO.FILENAME_COMPONENT_SEPARATOR_CHAR));
        String withoutIndx = withoutBook.substring(
                1 + withoutBook.indexOf(IO.FILENAME_COMPONENT_SEPARATOR_CHAR));
        return withoutIndx.replace(IO.FILENAME_COMPONENT_SEPARATOR_CHAR, ' ');
    }
    
    /**
     * <p>The text of the html "href" attribute followed by an equals sign and a quote.</p>
     */
    private static final String HREF_START = "href=\"";
    
    /**
     * <p>The title attribute of an html tag and the quote that begins the attribute's value.</p>
     */
    private static final String TITLE_START = "title=\"";
    
    /**
     * <p>Represents an element of a chapter trail, a sequence of backward and forward links between
     * chapters.</p>
     */
    static class TrailElement implements Comparable<TrailElement>{
        
        /**
         * <p>The chapter to be linked as the preceding chapter in the trail.</p>
         */
        private final String prev;
        
        /**
         * <p>The chapter for which links to the specified preceding and succeeding chapters are to
         * be installed.</p>
         */
        private final String focus;
        
        /**
         * <p>The chapter to be linked as the succeeding chapter in the trail.</p>
         */
        private final String next;
        
        TrailElement(String[] strings){
            this(strings[INDEX_PREV], strings[INDEX_FOCUS], strings[INDEX_NEXT]);
        }
        
        private static final int INDEX_PREV = 0;
        private static final int INDEX_FOCUS = 1;
        private static final int INDEX_NEXT = 2;
        
        /**
         * <p>Constructs a TrailElement indicating that the chapter named by {@code focus} has the
         * chapter named by {@code next} as its successor.</p>
         * @param prev the chapter before {@code focus} in sequence
         * @param focus the chapter in which links to {@code prev} and {@code next} are to be
         * installed
         * @param next the chapter after {@code focus} in sequence
         */
        TrailElement(String prev, String focus, String next){
            this.prev = prev;
            this.focus = focus;
            this.next = next;
        }
        
        /**
         * <p>Compares two TrailElements, first by their {@code focus}, then by their {@code prev},
         * and last by their {@code next}.</p>
         * @return an int whose sign reflects the natural ordering between this TrailElement and
         * {@code t}
         */
        @Override
        public int compareTo(TrailElement t){
            int comp = focus.compareTo(t.focus);
            if(comp != 0){
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
