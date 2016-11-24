package operate;

import common.Folder;

public enum Operation {
    CLEAR_EXCESS_STRUCTURE(
            Folder.HTML_BOOKS_UNSTRUCTURED, Folder.HTML_BOOKS_NEWLINE,             null), 
    CLEAR_FRONT_AND_BACK_MATTER(
            Folder.HTML_BOOKS_CHAPTER_CORE, Folder.HTML_BOOKS_UNSTRUCTURED,        null),
    DETERMINE_ANCHORS(
            Folder.ANCHORS,                 Folder.DUPLICATE_INDEPENDENTS,         null), 
    FIND_REPEATED_PHRASES(
            Folder.REPEATS,                 Folder.CORPUS,                         null), 
    HTML_TO_TEXT(
            Folder.CORPUS,                  Folder.HTML_CHAPTERS,                  null), 
    LINK_CHAPTERS(
            Folder.LINKED_CHAPTERS,         Folder.HTML_CHAPTERS,                  Folder.ANCHORS), 
    NEWLINE_P(
            Folder.HTML_BOOKS_NEWLINE,      Folder.HTML_BOOKS,                     null), 
    REMOVE_DEPENDENT_PHRASES(
            Folder.INDEPENDENT_INSTANCES,   Folder.REPEATS,                        null), 
    REMOVE_UNIQUE_INDEPENDENTS(
            Folder.DUPLICATE_INDEPENDENTS,  Folder.INDEPENDENT_INSTANCES,          null), 
    SET_TRAIL(
            Folder.READABLE,                Folder.LINKED_CHAPTERS,                null), 
    SPLIT_CHAPTERS(
            Folder.HTML_CHAPTERS,           Folder.HTML_BOOKS_CORRECT_APOSTROPHES, null), 
    SWAP_APOSTROPHES(
            Folder.HTML_BOOKS_CORRECT_APOSTROPHES, Folder.HTML_BOOKS_CHAPTER_CORE, null);
    
    private final Folder writeTo;
    private final Folder readFrom;
    private final Folder readDecoration;
    
    private Operation(Folder writeTo, Folder readFrom, Folder readDecoration){
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
