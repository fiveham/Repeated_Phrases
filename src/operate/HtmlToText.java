package operate;

import common.IO;
import html.HTMLFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.function.Consumer;

/**
 * <p>Converts the HTML chapters in {@code READ_FROM} to .txt files containing only the contents of
 * the chapters' bodies in {@code WRITE_TO}.</p>
 */
public class HtmlToText {
    
    public static final Operation OPERATION = Operation.HTML_TO_TEXT;
    
    /**
     * <p>Detects all the .html files in {@code READ_FROM}, reads them as HTMLFiles, and prints them
     * as .txt files in {@code WRITE_TO}.</p>
     * @param args command-line arguments (unused)
     */
    public static void htmlToText(String[] args, Consumer<String> msg) {
        File[] readUs = OPERATION.readFrom().folder().listFiles(IO::isHtml);
        for(File f : readUs){
            try{
                msg.accept("Saving as text " + f.getName());
                new HTMLFile(f).printAsText(
                        OPERATION.writeTo().folderName() 
                		+ File.separator 
                		+ IO.stripExtension(f.getName()) 
                		+ IO.TXT_EXT);
            } catch(FileNotFoundException e){
            	throw new RuntimeException(IO.ERROR_EXIT_MSG + f.getName() + " for reading");
            }
        }
    }
}
