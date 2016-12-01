package operate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import common.IO;
import html.HTMLFile;
import text.Location;

public enum Operation{
    NEWLINE_P(
            null, 
            Folder.HTML_BOOKS, 
            Folder.HTML_BOOKS_NEWLINE, 
            NewlineP::newlineP), 
    CLEAR_EXCESS_STRUCTURE(
            null, 
            Folder.HTML_BOOKS_NEWLINE, 
            Folder.HTML_BOOKS_UNSTRUCTURED, 
            ClearExcessStructure::clearXSStruct), 
    CLEAR_FRONT_AND_BACK_MATTER(
            null, 
            Folder.HTML_BOOKS_UNSTRUCTURED, 
            Folder.HTML_BOOKS_CHAPTER_CORE, 
            ClearFrontAndBackMatter::clearFrontBack), 
    SWAP_APOSTROPHES(
            null, 
            Folder.HTML_BOOKS_CHAPTER_CORE, 
            Folder.HTML_BOOKS_CORRECT_APOSTROPHES, 
            SwapApostrophes::swapApostrophes), 
    SPLIT_CHAPTERS(
            null, 
            Folder.HTML_BOOKS_CORRECT_APOSTROPHES, 
            Folder.HTML_CHAPTERS, 
            SplitChapters::splitChapters), 
    HTML_TO_TEXT(
            null, 
            Folder.HTML_CHAPTERS, 
            Folder.CORPUS, 
            Operation::htmlToText), 
    FIND_REPEATED_PHRASES(
            null, 
            Folder.CORPUS, 
            Folder.REPEATS, 
            FindRepeatedPhrases::findRepPhrases),
    REMOVE_DEPENDENT_PHRASES(
            null, 
            Folder.REPEATS, 
            Folder.INDEPENDENT_INSTANCES, 
            RemoveDependentPhrases::rmDepPhrases), 
    REMOVE_UNIQUE_INDEPENDENTS(
            null, 
            Folder.INDEPENDENT_INSTANCES, 
            Folder.DUPLICATE_INDEPENDENTS, 
            Operation::rmUniqIndeps),
    DETERMINE_ANCHORS(
            null, 
            Folder.DUPLICATE_INDEPENDENTS, 
            Folder.ANCHORS, 
            DetermineAnchors::determineAnchors), 
    LINK_CHAPTERS(
            Folder.ANCHORS, 
            Folder.HTML_CHAPTERS, 
            Folder.LINKED_CHAPTERS, 
            LinkChapters::linkChapters), 
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
    
    public BiConsumer<String[], ? super Consumer<String>> operation(){
        return this::operate;
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
    
    /**
     * <p>Reads each file from {@code READ_FROM} and prints only the lines of each file that have
     * more than one Location to a corresponding file in {@code WRITE_TO}.</p>
     * @param op the Operation whose folders will be used
     * @param args command-line args (not used)
     * @param msg receives and handles messages output by arbitrary parts of this operation
     */
    public static void rmUniqIndeps(Operation op, String[] args, Consumer<String> msg) {

        for(int i=FindRepeatedPhrases.MIN_PHRASE_SIZE; i<FindRepeatedPhrases.MAX_PHRASE_SIZE; i++){
            try(Scanner scan = new Scanner(new File(op.readFrom().filename(i)), IO.ENCODING); 
                        OutputStreamWriter out  = IO.newOutputStreamWriter(
                                op.writeTo().filename(i), 
                                scan)){
                while(scan.hasNextLine() && scan.hasNext()){
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
            } catch(IOException e){}
        }
    }
    
    @FunctionalInterface
    private interface TriConsumer<A,B,C>{
        public void accept(A a, B b, C c);
    }
}
