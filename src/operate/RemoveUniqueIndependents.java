package operate;

import common.IO;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import java.util.function.Consumer;
import text.Location;

/**
 * <p>Reads the quote files from {@code READ_FROM} line by line and writes their contents, to
 * corresponding files in {@code WRITE_TO}, except for lines containing only one  Location.} The
 * number of Locations represented on a line is determined by accounting for the semicolon
 * ({@code Location.ELEMENT_DELIM}) used to separate the two components of a Location
 * {@link java.lang.String#toString() in string form}.</p>
 */
public class RemoveUniqueIndependents {
    
    public static final Operation OPERATION = Operation.REMOVE_UNIQUE_INDEPENDENTS;
    
    /**
     * <p>Reads each file from {@code READ_FROM} and prints only the lines of each file that have
     * more than one Location to a corresponding file in {@code WRITE_TO}.</p>
     * @param args command-line arguments (unused)
     */
    public static void rmUniqIndeps(String[] args, Consumer<String> msg) {

        for(int i=FindRepeatedPhrases.MIN_PHRASE_SIZE; i<FindRepeatedPhrases.MAX_PHRASE_SIZE; i++){
            try(Scanner scan = new Scanner(new File(OPERATION.readFrom().filename(i)), IO.ENCODING); 
                        OutputStreamWriter out  = IO.newOutputStreamWriter(
                        		OPERATION.writeTo().filename(i), 
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
}
