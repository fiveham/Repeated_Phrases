package operate;

import java.io.File;
import java.util.function.Consumer;

import common.Folder;
import common.IO;

//TODO use references to elements of Operations once they are properly defined
public class RepeatedPhrasesApp {
	
    /**
     * <p>Ensures that the working directory has the folders specified in
     * {@link Folders Folders}.</p>
     */
	public static void ensureFolders(Consumer<String> msg){
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
    public static void isolateChaptersAndLink(String[] args, Consumer<String> msg) {
    	validateArgs(args, msg);
    	
        ensureFolders(msg);
        
        msg.accept("Newlining parargraphs");
        NewlineP.newlineP(null, msg);
        
        msg.accept("Removing inconsistent divs etc");
        ClearExcessStructure.clearXSStruct(null, msg);
        
        msg.accept("Removing non-chapter matter");
        ClearFrontAndBackMatter.clearFrontBack(null, msg);
        
        msg.accept("Normalizing apostrophes");
        SwapApostrophes.swapApostrophes(null, msg);
        
        msg.accept("Splitting books into chapters");
        SplitChapters.splitChapters(null, msg);
        
        msg.accept("Creating plaintext corpus");
        HtmlToText.htmlToText(null, msg);
        
        msg.accept("Finding repeat phrases in corpus");
        FindRepeatedPhrases.findRepPhrases(null, msg);
        
        msg.accept("Ignoring dependent quotes");
        RemoveDependentPhrases.rmDepPhrases(null, msg);
        
        msg.accept("Ignoring unique independent instances");
        RemoveUniqueIndependents.rmUniqIndeps(null, msg);
        
        linksAndTrail(args, msg);
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
        if( args.length < 1 ){
        	throw new IllegalArgumentException("I need a trail file.");
        }
        
        String trail = args[0];
        
        if( !(new File(trail)).exists() ){
            throw new IllegalArgumentException("I can't find that trail-file: \""+trail+"\".");
        }
        
        if( args.length < 2 ){
            return IO.PHRASE_SIZE_THRESHOLD_FOR_ANCHOR;
        }
        
        try{
            return Integer.parseInt(args[1]);
        } catch(NumberFormatException e){
            return IO.PHRASE_SIZE_THRESHOLD_FOR_ANCHOR;
        }
    }
    
    public static void linksAndTrail(String[] args, Consumer<String> msg) {
    	
        int limit = RepeatedPhrasesApp.validateArgs(args, msg);
        String[] trailArgs = new String[]{ args[0] };
        
        msg.accept("Determining links to add to phrases");
        DetermineAnchors.determineAnchors(trailArgs, msg);
        
        msg.accept("Adding links to html chapters");
        LinkChapters.linkChapters( new String[]{ Integer.toString(limit) }, msg );
        
        msg.accept("Adding prev- and next-chapter links");
        SetTrail.setTrail( trailArgs, msg );
    }
}
