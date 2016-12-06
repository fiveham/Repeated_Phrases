package operate;

import common.IO;
import html.HTMLFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import text.Location;

//TODO describe what the inputs and outputs are for each Operation
//in preparation to integrate the operations
public enum Operation{
    
    /**
     * <p>Reads the html novel files and ensures that each opening paragraph tag is preceded by at 
     * least one {@link IO#NEW_LINE newline character}.</p>
     */
    NEWLINE_P(
            null, 
            Folder.HTML_BOOKS, 
            Folder.HTML_BOOKS_NEWLINE, 
            NewlineP::newlineP), 
    
    /**
     * <p>Removes useless elements from the html novels: 
     * <ul>
     * <li>{@code div}s</li>
     * <li>{@code blockquote}s</li>
     * <li>{@code img}s</li>
     * <li>non-breaking spaces ({@code &nbsp;}), and</li>
     * <li>empty paragraphs ({@code <p></p>}</li>
     * </ul>
     * </p>
     */
    CLEAR_EXCESS_STRUCTURE(
            null, 
            Folder.HTML_BOOKS_NEWLINE, 
            Folder.HTML_BOOKS_UNSTRUCTURED, 
            ClearExcessStructure::clearXSStruct), 
    
    /**
     * <p>Removes from the html novels 
     * <ul>
     * <li>the text prior to the paragraph containing the name of the first chapter, and</li>
     * <li>the text following the final paragraph of the last chapter.</li>
     * </ul>
     * </p>
     */
    CLEAR_FRONT_AND_BACK_MATTER(
            null, 
            Folder.HTML_BOOKS_UNSTRUCTURED, 
            Folder.HTML_BOOKS_CHAPTER_CORE, 
            ClearFrontAndBackMatter::clearFrontBack), 
    
    /**
     * <p>Replaces apostrophes represented by right single quotes in novel html files with ordinary 
     * apostrophe characters.</p>
     */
    SWAP_APOSTROPHES(
            null, 
            Folder.HTML_BOOKS_CHAPTER_CORE, 
            Folder.HTML_BOOKS_CORRECT_APOSTROPHES, 
            SwapApostrophes::swapApostrophes), 
    
    /**
     * <p>Identifies chapters by splitting html novel files.</p>
     */
    SPLIT_CHAPTERS(
            null, 
            Folder.HTML_BOOKS_CORRECT_APOSTROPHES, 
            Folder.HTML_CHAPTERS, 
            SplitChapters::splitChapters), 
    
    /**
     * <p>Reads the html chapter files and saves them to another folder after stripping out all 
     * html elements.</p>
     */
    HTML_TO_TEXT(
            null, 
            Folder.HTML_CHAPTERS, 
            Folder.CORPUS, 
            Operation::htmlToText), 
    
    /**
     * <p>Reads the text chapters and determines which phrases at each size are repeated.</p>
     */
    FIND_REPEATED_PHRASES(
            null, 
            Folder.CORPUS, 
            Folder.REPEATS, 
            FindRepeatedPhrases::findRepPhrases), 
    
    /**
     * <p>Isolates those repeated Phrases that are not subsumed by a larger repeated Phrase.</p>
     */
    REMOVE_DEPENDENT_PHRASES(
            null, 
            Folder.REPEATS, 
            Folder.INDEPENDENT_INSTANCES, 
            RemoveDependentPhrases::rmDepPhrases), 
    
    /**
     * <p>Filters out the repeated Phrases which have only one instance that isn't subsumed by a 
     * larger phrase.</p>
     */
    REMOVE_UNIQUE_INDEPENDENTS(
            null, 
            Folder.INDEPENDENT_INSTANCES, 
            Folder.DUPLICATE_INDEPENDENTS, 
            Operation::rmUniqIndeps), 
    
    /**
     * <p>Creates anchor data for each phrase that has passed all the phrase filters.</p>
     */
    DETERMINE_ANCHORS(
            null, 
            Folder.DUPLICATE_INDEPENDENTS, 
            Folder.ANCHORS, 
            DetermineAnchors::determineAnchors), 
    
    /**
     * <p>Creates html chapters from existing html chapters and anchor data.</p>
     */
    LINK_CHAPTERS(
            Folder.ANCHORS, 
            Folder.HTML_CHAPTERS, 
            Folder.LINKED_CHAPTERS, //FIXME linkChapters actually outputs into HTML_CHAPTERS
            LinkChapters::linkChapters), 
    
    /**
     * <p>Adds trail info to anchored html chapters.</p>
     */
    SET_TRAIL(
            null, 
            Folder.LINKED_CHAPTERS, 
            Folder.READABLE, 
            SetTrail::setTrail);
    
    private final Folder readDecoration;
    private final Folder readFrom;
    private final Folder writeTo;
    private final TriConsumer<Operation, String[], ? super Consumer<String>> operation;
    
    private Operation(
            Folder readDecoration, 
            Folder readFrom, 
            Folder writeTo, 
            TriConsumer<Operation, String[], ? super Consumer<String>> operation){
        
        this.readDecoration = readDecoration;
        this.readFrom = readFrom;
        this.writeTo = writeTo;
        this.operation = operation;
    }
    
    public Folder writeTo(){
        return writeTo;
    }
    
    public Folder readFrom(){
        return readFrom;
    }
    
    public Folder readDecoration(){
        return readDecoration;
    }
    
    public void operate(String[] args, Consumer<String> msg){
        operation.accept(this, args, msg);
    }
    
    /**
     * <p>Detects all the .html files in {@code READ_FROM}, reads them as HTMLFiles, and prints them
     * as .txt files in {@code WRITE_TO}.</p>
     * @param op the Operation whose folders will be used
     * @param args command-line args (not used)
     * @param msg receives and handles messages output by arbitrary parts of this operation
     */
    private static void htmlToText(Operation op, String[] args, Consumer<String> msg) {
        File[] readUs = op.readFrom().folder().listFiles(IO::isHtml);
        Stream.of(readUs)
                .parallel()
                .forEach((f) -> {
                    try{
                        msg.accept("Saving as text " + f.getName());
                        new HTMLFile(f).printAsText(
                                op.writeTo().folderName() 
                                + File.separator 
                                + IO.stripExtension(f.getName()) 
                                + IO.TXT_EXT);
                    } catch(FileNotFoundException e){
                        throw new RuntimeException(
                                IO.ERROR_EXIT_MSG + f.getName() + " for reading");
                    }
                });
    }
    
    /**
     * <p>Reads each file from {@code READ_FROM} and prints only the lines of each file that have
     * more than one Location to a corresponding file in {@code WRITE_TO}.</p>
     * @param op the Operation whose folders will be used
     * @param args command-line args (not used)
     * @param msg receives and handles messages output by arbitrary parts of this operation
     */
    public static void rmUniqIndeps(Operation op, String[] args, Consumer<String> msg) {
        IntStream.range(FindRepeatedPhrases.MIN_PHRASE_SIZE, FindRepeatedPhrases.MAX_PHRASE_SIZE)
                .parallel()
                .forEach((i) -> {
                    try(
                            Scanner scan = new Scanner(new File(op.readFrom().filename(i)), IO.ENCODING); 
                            OutputStreamWriter out  = IO.newOutputStreamWriter(
                                        op.writeTo().filename(i), 
                                        scan)){
                        
                        while(IO.scannerHasNonEmptyNextLine(scan)){
                            String line = scan.nextLine();
                            if(line.indexOf(
                                    Location.ELEMENT_DELIM) != line.lastIndexOf(Location.ELEMENT_DELIM)){
                                //then there's multiple Locations on that line
                                
                                //the case of -1 == -1 can be ignored because a phrase 
                                //with no Locations will not have been printed to file.
                                out.write(line + IO.NEW_LINE);
                            }
                        }
                        scan.close();
                        out.close();
                    } catch(IOException e){
                    }
                });
    }
    
    @FunctionalInterface
    private interface TriConsumer<A,B,C>{
        public void accept(A a, B b, C c);
    }
}
