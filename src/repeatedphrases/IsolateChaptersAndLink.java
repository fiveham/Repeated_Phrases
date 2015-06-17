package repeatedphrases;

import java.io.File;
import java.util.function.Consumer;

/**
 * <p>Unites the operations of HtmlToText, 
 * FindRepeatedPhrases, RemoveDependentPhrases,
 * RemoveUniqueIndependents, DetermineAnchors, 
 * LinkChapters, and SetTrail for convenience.</p>
 */
public class IsolateChaptersAndLink {

    /**
     * <p>Calls {@link #isolateChaptersAndLink(String[],Consumer<String>) isolateChaptersAndLink()}.</p>
     * @param args command-line arguments
     */
     public static void main(String[] args){
         isolateChaptersAndLink(args, IO.DEFAULT_MSG);
     }

    /**
     * <p>Calls the main methods of HtmlToText, 
     * FindRepeatedPhrases, RemoveDependentPhrases,
     * RemoveUniqueIndependents, DetermineAnchors, 
     * LinkChapters, and SetTrail.
     * 
     * Passes the first command line argument to 
     * DetermineAnchors and SetTrail, and passes the 
     * second command-line argument to LinkChapters 
     * if it is present and if it parses as an int.</p>
     * @param args command-line arguments
     * @param msg 
     */
    public static void isolateChaptersAndLink(String[] args, Consumer<String> msg) {

        validateArgs(args, msg);

        EnsureFolders.ensureFolders(msg);

        msg.accept("Newlining parargraphs");
        NewlineP.newlineP(msg);

        msg.accept("Removing inconsistent divs etc");
        ClearExcessStructure.clearXSStruct(msg);

        msg.accept("Removing non-chapter matter");
        ClearFrontAndBackMatter.clearFrontBack(msg);

        msg.accept("Normalizing apostrophes");
        SwapApostrophes.swapApostrophes(msg);

        msg.accept("Splitting books into chapters");
        SplitChapters.splitChapters(msg);

        msg.accept("Creating plaintext corpus");
        HtmlToText.htmlToText(msg);

        msg.accept("Finding repeat phrases in corpus");
        FindRepeatedPhrases.findRepPhrases(msg);

        msg.accept("Ignoring dependent phrase-instances");
        RemoveDependentPhrases.rmDepPhrases(msg);

        msg.accept("Ignoring unique independent instances");
        RemoveUniqueIndependents.rmUniqIndeps(msg);

        LinksAndTrail.linksAndTrail( args, msg );
    }

    /**
     * <p>Checks that the command-line arguments passed to main() include 
     * an existing file to be passed to SetTrail, and returns the int 
     * value of the second command-line argument, if it is present and 
     * parses as an int, for use as the phrase-size threshold passed to 
     * {@link LinkChapters#main(String[]) LinkChapters}.</p>
     * @param args command-line arguments passed from main()
     * @return the int value of the second command-line argument, if 
     * it is present and parses as an int, {@value #IO.PHRASE_SIZE_FOR_ANCHOR} otherwise.
     */
    static int validateArgs(String[] args, Consumer<String> msg){
        if( args.length < 1 ){
            msg.accept("Need a trail file.");
            //System.out.println("Usage: java IsolateChaptersAndLink trail-file-name [phrase-size-min-for-linking]");
            System.exit(0);
        }

        String trail = args[0];

        if( !(new File(trail)).exists() ){
            msg.accept("Cannot find file "+trail);
            System.exit(0);
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
}
