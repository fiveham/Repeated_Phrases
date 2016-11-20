package operate;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.function.Consumer;

import common.Folder;
import common.IO;
import html.HTMLFile;

/**
 * <p>Converts the HTML chapters in <code>READ_FROM</code> to 
 * .txt files containing only the contents of the chapters' 
 * bodies in <code>WRITE_TO</code>.</p>
 */
public class HtmlToText {
    
    /**
     * <p>The <code>Folder</code> from which this class 
     * reads files to modify.</p>
     */
    public static final Folder READ_FROM = Folder.HTML_CHAPTERS;

    /**
     * <p>The <code>Folder</code> to which this class 
     * writes files it creates.</p>
     */
    public static final Folder WRITE_TO = Folder.CORPUS;

    public static void main(String[] args){
        htmlToText(IO.DEFAULT_MSG);
    }

    /**
     * <p>Detects all the .html files in <code>READ_FROM</code>, reads 
     * them as HTMLFiles, and prints them as .txt files in 
     * <code>WRITE_TO</code>.</p>
     * @param args command-line arguments (unused)
     */
    public static void htmlToText(Consumer<String> msg) {
        File[] readUs = READ_FROM.folder().listFiles(IO::isHtml);
        for(File f : readUs){
            try{
                msg.accept("Saving as text "+f.getName());
                HTMLFile file = new HTMLFile(f);
                file.printAsText( WRITE_TO.folderName() + File.separator + IO.stripExtension(f.getName()) + IO.TXT_EXT );
            } catch(FileNotFoundException e){
            	throw new RuntimeException(IO.ERROR_EXIT_MSG + f.getName() + " for reading");
            }
        }
    }
}
