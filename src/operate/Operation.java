package operate;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import common.IO;
import html.HTMLFile;

//TODO update the javadoc for the methods that have had an args param added
public enum Operation{
    NEWLINE_P(
            null, 
            Folder.HTML_BOOKS_NEWLINE, 
            Folder.HTML_BOOKS, 
            (o,args,msg) -> NewlineP.newlineP(args, msg)), 
    CLEAR_EXCESS_STRUCTURE(
            null, 
            Folder.HTML_BOOKS_UNSTRUCTURED, 
            Folder.HTML_BOOKS_NEWLINE, 
            (o,args,msg) -> ClearExcessStructure.clearXSStruct(args, msg)), 
    CLEAR_FRONT_AND_BACK_MATTER(
            null, 
            Folder.HTML_BOOKS_CHAPTER_CORE, 
            Folder.HTML_BOOKS_UNSTRUCTURED, 
            (o,args,msg) -> ClearFrontAndBackMatter.clearFrontBack(args, msg)), 
    SWAP_APOSTROPHES(
            null, 
            Folder.HTML_BOOKS_CORRECT_APOSTROPHES, 
            Folder.HTML_BOOKS_CHAPTER_CORE, 
            (o,args,msg) -> SwapApostrophes.swapApostrophes(args, msg)), 
    SPLIT_CHAPTERS(
            null, 
            Folder.HTML_CHAPTERS, 
            Folder.HTML_BOOKS_CORRECT_APOSTROPHES, 
            (o,args,msg) -> SplitChapters.splitChapters(args, msg)), 
    HTML_TO_TEXT(
            null, 
            Folder.CORPUS, 
            Folder.HTML_CHAPTERS, 
            Operation::htmlToText), 
    FIND_REPEATED_PHRASES(
            null, 
            Folder.REPEATS, 
            Folder.CORPUS, 
            (o,args,msg) -> FindRepeatedPhrases.findRepPhrases(args, msg)),
    REMOVE_DEPENDENT_PHRASES(
            null, 
            Folder.INDEPENDENT_INSTANCES, 
            Folder.REPEATS, 
            (o,args,msg) -> RemoveDependentPhrases.rmDepPhrases(args, msg)), 
    REMOVE_UNIQUE_INDEPENDENTS(
            null, 
            Folder.DUPLICATE_INDEPENDENTS, 
            Folder.INDEPENDENT_INSTANCES, 
            (o,args,msg) -> RemoveUniqueIndependents.rmUniqIndeps(args, msg)),
    DETERMINE_ANCHORS(
            null, 
            Folder.ANCHORS, 
            Folder.DUPLICATE_INDEPENDENTS, 
            (o,args,msg) -> DetermineAnchors.determineAnchors(args, msg)), 
    LINK_CHAPTERS(
            Folder.ANCHORS, 
            Folder.LINKED_CHAPTERS, 
            Folder.HTML_CHAPTERS, 
            (o,args,msg) -> LinkChapters.linkChapters(args, msg)), 
    SET_TRAIL(
            null, 
            Folder.READABLE, 
            Folder.LINKED_CHAPTERS, 
            (o,args,msg) -> SetTrail.setTrail(args, msg));
    
    private final Folder readDecoration;
    private final Folder writeTo;
    private final Folder readFrom;
    private final TriConsumer<Operation, String[], ? super Consumer<String>> operation;
    
    private Operation(
            Folder readDecoration, 
            Folder writeTo, 
            Folder readFrom, 
            TriConsumer<Operation, String[], ? super Consumer<String>> operation){
        
        this.readDecoration = readDecoration;
        this.writeTo = writeTo;
        this.readFrom = readFrom;
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
    
    public BiConsumer<String[], ? super Consumer<String>> operation(){
        return this::operate;
    }
    
    /**
     * <p>Detects all the .html files in {@code READ_FROM}, reads them as HTMLFiles, and prints them
     * as .txt files in {@code WRITE_TO}.</p>
     * @param args command-line arguments (unused)
     */
    private static void htmlToText(Operation op, String[] args, Consumer<String> msg) {
        File[] readUs = op.readFrom().folder().listFiles(IO::isHtml);
        for(File f : readUs){
            try{
                msg.accept("Saving as text " + f.getName());
                new HTMLFile(f).printAsText(
                        op.writeTo().folderName() 
                        + File.separator 
                        + IO.stripExtension(f.getName()) 
                        + IO.TXT_EXT);
            } catch(FileNotFoundException e){
                throw new RuntimeException(IO.ERROR_EXIT_MSG + f.getName() + " for reading");
            }
        }
    }
    
    @FunctionalInterface
    private interface TriConsumer<A,B,C>{
        public void accept(A a, B b, C c);
    }
}
