package operate;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

//TODO update the javadoc for the methods that have had an args param added
public enum Operation{
    NEWLINE_P(
            null, 
            Folder.HTML_BOOKS_NEWLINE, 
            Folder.HTML_BOOKS, 
            NewlineP::newlineP), 
    CLEAR_EXCESS_STRUCTURE(
            null, 
            Folder.HTML_BOOKS_UNSTRUCTURED, 
            Folder.HTML_BOOKS_NEWLINE, 
            ClearExcessStructure::clearXSStruct), 
    CLEAR_FRONT_AND_BACK_MATTER(
            null, 
            Folder.HTML_BOOKS_CHAPTER_CORE, 
            Folder.HTML_BOOKS_UNSTRUCTURED, 
            ClearFrontAndBackMatter::clearFrontBack), 
    SWAP_APOSTROPHES(
            null, 
            Folder.HTML_BOOKS_CORRECT_APOSTROPHES, 
            Folder.HTML_BOOKS_CHAPTER_CORE, 
            SwapApostrophes::swapApostrophes), 
    SPLIT_CHAPTERS(
            null, 
            Folder.HTML_CHAPTERS, 
            Folder.HTML_BOOKS_CORRECT_APOSTROPHES, 
            SplitChapters::splitChapters), 
    HTML_TO_TEXT(
            null, 
            Folder.CORPUS, 
            Folder.HTML_CHAPTERS, 
            HtmlToText::htmlToText), 
    FIND_REPEATED_PHRASES(
            null, 
            Folder.REPEATS, 
            Folder.CORPUS, 
            FindRepeatedPhrases::findRepPhrases),
    REMOVE_DEPENDENT_PHRASES(
            null, 
            Folder.INDEPENDENT_INSTANCES, 
            Folder.REPEATS, 
            RemoveDependentPhrases::rmDepPhrases), 
    REMOVE_UNIQUE_INDEPENDENTS(
            null, 
            Folder.DUPLICATE_INDEPENDENTS, 
            Folder.INDEPENDENT_INSTANCES, 
            RemoveUniqueIndependents::rmUniqIndeps),
    DETERMINE_ANCHORS(
            null, 
            Folder.ANCHORS, 
            Folder.DUPLICATE_INDEPENDENTS, 
            DetermineAnchors::determineAnchors), 
    LINK_CHAPTERS(
            Folder.ANCHORS, 
            Folder.LINKED_CHAPTERS, 
            Folder.HTML_CHAPTERS, 
            LinkChapters::linkChapters), 
    SET_TRAIL(
            null, 
            Folder.READABLE, 
            Folder.LINKED_CHAPTERS, 
            SetTrail::setTrail);

    private final Folder readDecoration;
    private final Folder writeTo;
    private final Folder readFrom;
    private final BiConsumer<String[], ? super Consumer<String>> operation;
    
    private Operation(
            Folder readDecoration, 
            Folder writeTo, 
            Folder readFrom, 
            BiConsumer<String[], ? super Consumer<String>> operation){
        
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
        operation.accept(args, msg);
    }
    
    public BiConsumer<String[], ? super Consumer<String>> operation(){
        return this.operation;
    }
}
