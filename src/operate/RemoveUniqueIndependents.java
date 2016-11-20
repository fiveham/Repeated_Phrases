package operate;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import java.util.function.Consumer;

import common.Folder;
import common.IO;
import text.Location;

/**
 * <p>Reads the phrase-instance files from <code>READ_FROM</code> 
 * line by line and writes their contents, to corresponding 
 * files in <code>WRITE_TO</code>, except for lines containing 
 * only one Location.</code> The number of Locations represented 
 * on a line is determined by accounting for the semicolon 
 * (<code>Location.ELEMENT_DELIM</code>) used to separate the 
 * two components of a Location 
 * {@link java.lang.String#toString() in string form}.</p>
 */
public class RemoveUniqueIndependents {

    /**
     * <p>The folder from which this class reads phrase-instance data for 
     * independent phrase-instances including those with only one independent 
     * instance.</p>
     * @see Folder#INDEPENDENT_INSTANCES
     */
    public static final Folder READ_FROM = Folder.INDEPENDENT_INSTANCES;

    /**
     * <p>The folder where the read-in phrase-instance data with the 
     * unique independent instances removed are saved.</p>
     */
    public static final Folder WRITE_TO = Folder.DUPLICATE_INDEPENDENTS;

    public static void main(String[] args){
        rmUniqIndeps(IO.DEFAULT_MSG);
    }

    /**
     * <p>Reads each file from <code>READ_FROM</code> and prints only the 
     * lines of each file that have more than one Location to a corresponding 
     * file in <code>WRITE_TO</code>.</p>
     * @param args command-line arguments (unused)
     */
    public static void rmUniqIndeps(Consumer<String> msg) {

        for(int i=FindRepeatedPhrases.MIN_PHRASE_SIZE; i<FindRepeatedPhrases.MAX_PHRASE_SIZE; i++){
            try(Scanner scan = new Scanner(new File( READ_FROM.filename(i) ), IO.ENCODING ); 
                        OutputStreamWriter out  = IO.newOutputStreamWriter( WRITE_TO.filename(i), scan )){
                while( scan.hasNextLine() && scan.hasNext() ){
                    String line = scan.nextLine();
                    if( line.indexOf(Location.ELEMENT_DELIM) != line.lastIndexOf(Location.ELEMENT_DELIM)){ //then there's multiple Locations on that line
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
}
