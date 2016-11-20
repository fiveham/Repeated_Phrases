package operate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.function.Consumer;

import common.Folder;
import common.IO;

/**
 * <p>Adds system-dependent newlines/linebreaks before 
 * opening html paragraph tags in html book files from 
 * <cod>READ_FROM</code> to increase the human-readability 
 * of those files.</p>
 */
public class NewlineP{
	
    /**
     * <p>The folder from which this class reads the html 
     * ASOIAF book files.</p>
     * @see Folder#HTML_BOOKS
     */
    public static final Folder READ_FROM = Folder.HTML_BOOKS;
    
    /**
     * <p>The folder to which this class writes copies of 
     * the html ASOIAF book files with 
     * {@linkplain IO#NEW_LINE newlines} before each 
     * paragraph.</p>
     * @see Folder#HTML_BOOKS
     */
    public static final Folder WRITE_TO = Folder.HTML_BOOKS_NEWLINE;
    
    /**
     * <p>The first characters of an opening paragraph tag.</p>
     */
    public static final String BEGIN_P = "<p ";
    
    /**
     * 
     * @param args command-line arguments
     */
    public static void main(String[] args){
        newlineP(IO.DEFAULT_MSG);
    }

    /**
     * <p>Detects the html files in <code>READ_FROM</code>, reads them 
     * using {@link #BEGIN_P BEGIN_P} as a Scanner's 
     * {@linkplain java.util.Scanner#useDelimiter(String) delimiter}, 
     * and accumulates the content returned by that Scanner, including 
     * a {@linkplain IO#NEW_LINE newline} and the value of 
     * <code>BEGIN_P</code> before each element after the first one 
     * produced by the Scanner.</p>
     * @param msg 
     */
    public static void newlineP(Consumer<String> msg){

        String[] readUs = READ_FROM.folder().list(IO::isHtml);

        for(String filename : readUs){

            String inName = getInOutName(filename, READ_FROM);
            String outName = getInOutName(filename, WRITE_TO);

            try(Scanner scan = new Scanner(new File(inName), IO.ENCODING);
            		OutputStreamWriter out = IO.newOutputStreamWriter( outName );){
                
                scan.useDelimiter(BEGIN_P);
                String content = getContent(scan);
                scan.close();

                out.write(content);
                out.close();
            } catch(FileNotFoundException e){
                msg.accept("FileNotFoundException occured for file "+filename);//+": "+e.getMessage());
            } catch(UnsupportedEncodingException e){
                msg.accept("UnsupportedEncodingException occured for file "+filename);//+": "+e.getMessage());
            } catch(IOException e){
                msg.accept("IOException occured for file "+filename);//+": "+e.getMessage());
            }
        }

        msg.accept("Done");
    }

    /**
     * <p>Returns the name of the file to or from which this 
     * class should write or read content, depending on the 
     * value of <code>folder</code>. If READ_FROM is specified, 
     * then the value returned is the name of the file from 
     * which content should be read. If WRITE_TO is specified, 
     * then the value returned is the name of the file to which 
     * content should be written.</p>
     * @param filename the name of the file to which a 
     * folder reference should be prepended
     * @param folder the folder to which a reference is 
     * prepended to <code>filename</code>
     * @return the name of the file to or from which this 
     * class should write or read content
     */
    private static String getInOutName(String filename, Folder folder){
        return folder.folderName() + File.separator + IO.stripFolder(filename);
    }

    /**
     * <p>Returns a <code>String</code> containing all the contents 
     * of the body that <code>s</code> reads, with newlines 
     * inserted before every opening paragraph tag.</p>
     * @param s a Scanner reading a file from <code>READ_FROM</code>
     * @return the text content produced by <code>s</code> 
     * with newlines inserted before every opening 
     * paragraph tag.
     */
    private static String getContent(Scanner s){
        StringBuilder result = new StringBuilder();

        if(s.hasNext()){
            result.append(s.next());
        }
        while(s.hasNext()){
            result.append( IO.NEW_LINE ).append(BEGIN_P).append(s.next());
        }
        return result.toString();
    } 
}
