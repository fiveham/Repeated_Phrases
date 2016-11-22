package operate;

import common.Folder;
import common.IO;
import html.CharCode;
import html.HTMLFile;
import html.Tag;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * <p>Removes certain sequences from the HTML 
 * files specified as command-line arguments.</p>
 * 
 * <p>Removes:
 * <ul>
 *  <li>div tags</li>
 *  <li>blockquote tags</li>
 *  <li>img tags</li>
 *  <li>&nbsp;</li>
 *  <li>paragraphs with no visible content</li>
 * </ul></p>
 */
public class ClearExcessStructure{
	
	/**
	 * <p>The directory from which this class reads files it modifies.</p>
	 */
	public static final Folder READ_FROM = Folder.HTML_BOOKS_NEWLINE;
	
	/**
	 * <p>The director to which this class writes files it has modified.</p>
	 */
	public static final Folder WRITE_TO = Folder.HTML_BOOKS_UNSTRUCTURED;
	
	public static void main(String[] args){
		clearXSStruct(IO.DEFAULT_MSG);
	}
	
	/**
	 * <p>Detects the html files in the directory {@code READ_FROM}, 
	 * reads each of them, removes divs, blockquotes, imgs, non-breaking 
	 * spaces, and empty paragraphs from them, and saves them to 
	 * {@code WRITE_TO}.</p>
	 * @param args command-line arguments
	 */
	public static void clearXSStruct(Consumer<String> msg){
		File[] readUs = READ_FROM.folder().listFiles(IO::isHtml);
		for(File f : readUs){
			
			msg.accept("Removing structure from "+f.getName());
			
			try(OutputStreamWriter out = IO.newOutputStreamWriter( WRITE_TO.folderName() + File.separator + f.getName() );){
				HTMLFile file = new HTMLFile(f.getName(), new Scanner(f, IO.ENCODING));
				
				file.removeAll( Tag.IS_DIV );
				file.removeAll( Tag.IS_BLOCKQUOTE );
				file.removeAll( Tag.IS_IMG );
				file.removeAll( CharCode.IS_NBSP );
				removeEmptyP(file);
				
				file.print(out);
				out.close();
				
			} catch(FileNotFoundException e){
				msg.accept("FileNotFoundException occured for file "+f.getName());
			} catch(UnsupportedEncodingException e){
				msg.accept("UnsupportedEncodingException occured for file "+f.getName());
			} catch(IOException e){
				msg.accept("IOException occured for file "+f.getName());
			}
		}
	}
	
	/**
	 * <p>Removes empty paragraph blocks from the specified 
	 * {@code HTMLFile}.</p>
	 */
	private static void removeEmptyP(HTMLFile file){
		HTMLFile.ParagraphIterator piter = file.paragraphIterator();
		List<int[]> paragraphs = new ArrayList<>();
		while(piter.hasNext()){
			paragraphs.add(piter.next());
		}
		
		for(int i = paragraphs.size()-1; i >= 0; i--){
			int[] bounds = paragraphs.get(i);
			
			if( !isThereLiteralContent( file, bounds[0], bounds[1] ) ){
				file.removeAll(bounds[0], bounds[1]);
			}
		}
	}
	
	/**
	 * <p>Returns true if there is visible content between the 
	 * tags whose positions are indicated by low and top, 
	 * false otherwise.</p>
	 * @param low exclusive lower bound
	 * @param top exclusive upper bound
	 * @return true if there is visible content between the 
	 * tags whose positions are indicated by low and top, 
	 * false otherwise.
	 */
	private static boolean isThereLiteralContent(HTMLFile file, int low, int top){
		for(int i=low+1; i<top; i++){
			if(file.get(i).isVisible()){
				return true;
			}
		}
		return false;
	}
}
