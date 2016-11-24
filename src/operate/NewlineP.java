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
 * <p>Adds system-dependent newlines/linebreaks before opening html paragraph tags in html book
 * files from {@code READ_FROM} to increase the human-readability of those files.</p>
 */
public class NewlineP{
	
    public static final Operation OPERATION = Operation.NEWLINE_P;
    
    /**
     * <p>The first characters of an opening paragraph tag.</p>
     */
    public static final String BEGIN_P = "<p ";
    
    /**
     * <p>Detects the html files in {@code READ_FROM}, reads them using {@link #BEGIN_P BEGIN_P} as
     * a Scanner's {@linkplain java.util.Scanner#useDelimiter(String) delimiter}, and accumulates
     * the content returned by that Scanner, including a {@linkplain IO#NEW_LINE newline} and the
     * value of {@code BEGIN_P} before each element after the first one produced by the Scanner.</p>
     * @param msg
     */
    public static void newlineP(String[] args, Consumer<String> msg){
        
        String[] readUs = OPERATION.readFrom().folder().list(IO::isHtml);
        
        for(String filename : readUs){
            
            String inName = getInOutName(filename, OPERATION.readFrom());
            String outName = getInOutName(filename, OPERATION.writeTo());
            
            try(
                    Scanner scan = new Scanner(new File(inName), IO.ENCODING);
            		OutputStreamWriter out = IO.newOutputStreamWriter(outName)){
                
                scan.useDelimiter(BEGIN_P);
                String content = getContent(scan);
                scan.close();
                
                out.write(content);
                out.close();
            } catch(FileNotFoundException e){
                msg.accept("FileNotFoundException occured for file "+filename);
            } catch(UnsupportedEncodingException e){
                msg.accept("UnsupportedEncodingException occured for file "+filename);
            } catch(IOException e){
                msg.accept("IOException occured for file "+filename);
            }
        }
        
        msg.accept("Done");
    }
    
    /**
     * <p>Returns the name of the file to or from which this class should write or read content,
     * depending on the value of {@code folder}. If READ_FROM is specified, then the value returned
     * is the name of the file from which content should be read. If WRITE_TO is specified, then the
     * value returned is the name of the file to which content should be written.</p>
     * @param filename the name of the file to which a folder reference should be prepended
     * @param folder the folder to which a reference is prepended to {@code filename}
     * @return the name of the file to or from which this class should write or read content
     */
    private static String getInOutName(String filename, Folder folder){
        return folder.folderName() + File.separator + IO.stripFolder(filename);
    }
    
    /**
     * <p>Returns a {@code String} containing all the contents of the body that {@code s} reads,
     * with newlines inserted before every opening paragraph tag.</p>
     * @param s a Scanner reading a file from {@code READ_FROM}
     * @return the text content produced by {@code s} with newlines inserted before every opening
     * paragraph tag.
     */
    private static String getContent(Scanner s){
        StringBuilder result = new StringBuilder();
        
        if(s.hasNext()){
            result.append(s.next());
        }
        while(s.hasNext()){
            result.append(IO.NEW_LINE).append(BEGIN_P).append(s.next());
        }
        return result.toString();
    } 
}
