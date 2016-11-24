package operate;

import common.Folder;

//TODO add a member that accepts a Consumer<String> and let it point to these classes' work methods
public enum Operation{
    CLEAR_EXCESS_STRUCTURE(
            null,           Folder.HTML_BOOKS_UNSTRUCTURED, Folder.HTML_BOOKS_NEWLINE), 
    CLEAR_FRONT_AND_BACK_MATTER(
            null,           Folder.HTML_BOOKS_CHAPTER_CORE, Folder.HTML_BOOKS_UNSTRUCTURED),
    DETERMINE_ANCHORS(
            null,           Folder.ANCHORS,                 Folder.DUPLICATE_INDEPENDENTS), 
    FIND_REPEATED_PHRASES(
            null,           Folder.REPEATS,                 Folder.CORPUS), 
    HTML_TO_TEXT(
            null,           Folder.CORPUS,                  Folder.HTML_CHAPTERS), 
    LINK_CHAPTERS(
            Folder.ANCHORS, Folder.LINKED_CHAPTERS,         Folder.HTML_CHAPTERS), 
    NEWLINE_P(
            null,           Folder.HTML_BOOKS_NEWLINE,      Folder.HTML_BOOKS), 
    REMOVE_DEPENDENT_PHRASES(
            null,           Folder.INDEPENDENT_INSTANCES,   Folder.REPEATS), 
    REMOVE_UNIQUE_INDEPENDENTS(
            null,           Folder.DUPLICATE_INDEPENDENTS,  Folder.INDEPENDENT_INSTANCES), 
    SET_TRAIL(
            null,           Folder.READABLE,                Folder.LINKED_CHAPTERS), 
    SPLIT_CHAPTERS(
            null,           Folder.HTML_CHAPTERS,           Folder.HTML_BOOKS_CORRECT_APOSTROPHES), 
    SWAP_APOSTROPHES(
            null,           Folder.HTML_BOOKS_CORRECT_APOSTROPHES, Folder.HTML_BOOKS_CHAPTER_CORE);
    
    private final Folder writeTo;
    private final Folder readFrom;
    private final Folder readDecoration;
    
    private Operation(Folder readDecoration, Folder writeTo, Folder readFrom){
        this.writeTo = writeTo;
        this.readFrom = readFrom;
        this.readDecoration = readDecoration;
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
}
