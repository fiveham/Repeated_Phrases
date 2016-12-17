package operate;

import common.IO;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import text.Location;

//TODO describe what the inputs and outputs are for each Operation
//in preparation to integrate the operations
public enum Operation{
    
    //TODO let CLEAN_AND_SPLIT acknowledge that it writes to both CORPUS and HTML_CHAPTERS
    CLEAN_AND_SPLIT(
            null, 
            Folder.HTML_BOOKS, 
            Folder.CORPUS, 
            null), //XXX put a meaningful expression in place of this null
    
    //TODO combine FIND_REPEATED_PHRASES, REMOVE_DEPENDENT_PHRASES, and REMOVE_UNIQUE_INDEPENDENTS 
    //into one operation.  Other operations might be able to be rolled in, as well.
    
    /**
     * <p>Reads the text chapters and determines which phrases at each size are repeated.</p>
     * 
     * Input: Text chapters with each paragraph preceded by a newline, without any divs, 
     * blockquotes, imgs, nbsps, or empty paragraphs, and with all right single quotes that 
     * function as apostrophes replaced by actual apostrophe characters
     * 
     * Output: An index of phrases that occur multiple times in the overall corpus
     */
    FIND_REPEATED_PHRASES(
            null, 
            Folder.CORPUS, 
            Folder.REPEATS, 
            null), 
    
    /**
     * <p>Isolates those repeated Phrases that are not subsumed by a larger repeated Phrase.</p>
     * 
     * Input: An index of phrases that occur multiple times in the overall corpus
     * 
     * Output: An index of phrases that occur multiple times in the overall corpus and which are 
     * not subsumed by some larger phrase that also occurs multiple times in the corpus
     */
    REMOVE_DEPENDENT_PHRASES(
            null, 
            Folder.REPEATS, 
            Folder.INDEPENDENT_INSTANCES, 
            RemoveDependentPhrases::rmDepPhrases), 
    
    /**
     * <p>Filters out the repeated Phrases which have only one instance that isn't subsumed by a 
     * larger phrase.</p>
     * 
     * Input: An index of phrases that occur multiple times in the overall corpus and which are 
     * not subsumed by some larger phrase that also occurs multiple times in the corpus
     * 
     * Output: An index of phrases from the overall corpus which occur at multiple non-subsumed 
     * locations
     */
    REMOVE_UNIQUE_INDEPENDENTS(
            null, 
            Folder.INDEPENDENT_INSTANCES, 
            Folder.DUPLICATE_INDEPENDENTS, 
            Operation::rmUniqIndeps), 
    
    /**
     * <p>Creates anchor data for each phrase that has passed all the phrase filters.</p>
     * 
     * Input: An index of phrases from the overall corpus which occur at multiple non-subsumed 
     * locations
     * 
     * Output: An index of anchors connecting each of the phrases from the overall corpus which 
     * occur at multiple non-subsumed locations to the corresponding phrase that comes after it
     */
    DETERMINE_ANCHORS(
            null, 
            Folder.DUPLICATE_INDEPENDENTS, 
            Folder.ANCHORS, 
            null), //XXX replace with something meaningful
    
    /**
     * <p>Creates html chapters from existing html chapters and anchor data.</p>
     * 
     * Input: An index of anchors connecting each of the phrases from the overall corpus which 
     * occur at multiple non-subsumed locations to the corresponding phrase that comes after it and 
     * html chapters of the ASOIAF books
     * 
     * Output: Html chapters where each of the phrases from the overall corpus which occur at 
     * multiple non-subsumed locations is linked to its successor
     */
    LINK_CHAPTERS(
            Folder.ANCHORS, 
            Folder.HTML_CHAPTERS, 
            Folder.LINKED_CHAPTERS, //FIXME linkChapters actually outputs into HTML_CHAPTERS
            LinkChapters::linkChapters), 
    
    /**
     * <p>Adds trail info to anchored html chapters.</p>
     * 
     * Input: Html chapters where each of the phrases from the overall corpus which occur at 
     * multiple non-subsumed locations is linked to its successor
     * 
     * Output: Html chapters where each of the phrases from the overall corpus which occur at 
     * multiple non-subsumed locations is linked to its successor and which is each linked to its 
     * preceding and succeeding html chapter
     */
    SET_TRAIL(
            null, 
            Folder.LINKED_CHAPTERS, 
            Folder.READABLE, 
            RepeatedPhrasesApp::setTrail);
    
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
     * <p>Reads each file from {@code READ_FROM} and prints only the lines of each file that have
     * more than one Location to a corresponding file in {@code WRITE_TO}.</p>
     * @param op the Operation whose folders will be used
     * @param args command-line args (not used)
     * @param msg receives and handles messages output by arbitrary parts of this operation
     */
    private static void rmUniqIndeps(Operation op, String[] args, Consumer<String> msg) {
        IntStream.range(IO.MIN_PHRASE_SIZE, IO.MAX_PHRASE_SIZE)
                .parallel()
                .forEach((i) -> {
                    try(
                            Scanner scan = new Scanner(new File(op.readFrom().filename(i)), IO.ENCODING); 
                            OutputStreamWriter out  = IO.newOutputStreamWriter(
                                        op.writeTo().filename(i), 
                                        scan)){
                        
                        while(IO.scannerHasNonEmptyNextLine(scan)){
                            String line = scan.nextLine();
                            if(line.indexOf(Location.ELEMENT_DELIM) 
                                    != line.lastIndexOf(Location.ELEMENT_DELIM)){
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
