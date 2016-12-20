package operate;

import java.util.function.Consumer;

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
    
    PHRASE_ANALYSIS(
            null, 
            Folder.CORPUS, 
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
    
    @FunctionalInterface
    private interface TriConsumer<A,B,C>{
        public void accept(A a, B b, C c);
    }
}
