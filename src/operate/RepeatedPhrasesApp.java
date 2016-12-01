package operate;

import common.IO;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import text.Chapter;

//TODO use the following structure:
//stage 1: ensure folders exist
//stage 2: ensure chapters are available
//stage 3: construct a graph linking chapters with quotes with locations and phrases ... etc.
//stage 4: reduce the graph, leaving each Location with a single Quote
//stage 5: link quotes and add links to html files
public class RepeatedPhrasesApp {
    
    private Collection<Chapter> chapters = null;
    
    public RepeatedPhrasesApp(){
        
    }
    
    public Collection<Chapter> getChapters(boolean generate){
        if(chapters == null){
            chapters = readChapters(generate);
        }
        return chapters;
    }
	
    private static Collection<Chapter> readChapters(boolean generate){
        if(generate){
            genChapters(System.out::println); //TODO allow user to specify msg
        }
        
        return FindRepeatedPhrases.getChapters(
                //TODO create a FilenameFilter: Chapter.isChapter(File,String)
                Operation.FIND_REPEATED_PHRASES.readFrom().folder().listFiles(IO::isTxt));
    }
    
    private static void genChapters(Consumer<String> msg){
        List<Operation> ops = new ArrayList<>(
                EnumSet.range(Operation.NEWLINE_P, Operation.HTML_TO_TEXT));
        ops.sort(null);
        ops.forEach((op) -> op.operate(null, msg));
    }
    
    /**
     * <p>Ensures that the working directory has the folders specified in
     * {@link Folders Folders}.</p>
     */
	public void ensureFolders(Consumer<String> msg){
		for(Folder f : Folder.values()){
			File name = f.folder();
			if( !name.exists() ){
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
    	int limit = validateArgs(args, msg);
    	
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
        
        linksAndTrail(limit, trailArgs(args), msg);
    }
    
    /**
     * <p>Checks that the command-line arguments passed to main() include an existing file to be
     * passed to SetTrail, and returns the int value of the second command-line argument, if it is
     * present and parses as an int, for use as the phrase-size threshold passed to
     * {@link LinkChapters#main(String[]) LinkChapters}.</p>
     * @param args command-line arguments passed from main()
     * @return the int value of the second command-line argument, if it is present and parses as an
     * int, {@value #IO.PHRASE_SIZE_FOR_ANCHOR} otherwise.
     */
    static int validateArgs(String[] args, Consumer<String> msg){
        if(args.length < 1){ //MAGIC
        	throw new IllegalArgumentException("I need a trail file.");
        }
        
        String trail = args[0]; //MAGIC
        
        if(!(new File(trail)).exists()){
            throw new IllegalArgumentException("I can't find that trail-file: \""+trail+"\".");
        }
        
        if(args.length < 2){ //MAGIC
            return IO.PHRASE_SIZE_THRESHOLD_FOR_ANCHOR;
        }
        
        try{
            return Integer.parseInt(args[1]); //MAGIC
        } catch(NumberFormatException e){
            return IO.PHRASE_SIZE_THRESHOLD_FOR_ANCHOR;
        }
    }
    
    public void linksAndTrail(String[] args, Consumer<String> msg) {
    	
        int limit = validateArgs(args, msg);
        String[] trailArgs = trailArgs(args);//new String[]{ args[0] };
        
        linksAndTrail(limit, trailArgs, msg);
    }
    
    private String[] trailArgs(String[] args){
        return new String[]{ args[0] };
    }
    
    private void linksAndTrail(int limit, String[] trailArgs, Consumer<String> msg){
        msg.accept("Determining links to add to phrases");
        Operation.DETERMINE_ANCHORS.operate(trailArgs, msg);
        
        msg.accept("Adding links to html chapters");
        Operation.LINK_CHAPTERS.operate(new String[]{Integer.toString(limit)}, msg);
        
        msg.accept("Adding prev- and next-chapter links");
        Operation.SET_TRAIL.operate(trailArgs, msg);
    }
}
