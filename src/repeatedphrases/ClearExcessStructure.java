package repeatedphrases;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

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
	
	/**
	 * <p>Detects the html files in the directory <code>READ_FROM</code>, 
	 * reads each of them, removes divs, blockquotes, imgs, non-breaking 
	 * spaces, and empty paragraphs from them, and saves them to 
	 * <code>WRITE_TO</code>.</p>
	 * @param args command-line arguments
	 */
	public static void main(String[] args){
		File[] readUs = READ_FROM.folder().listFiles( IO.IS_HTML );
		for(File f : readUs){
			
			System.out.println("Removing structure from "+f.getName());
			
			try{
				HTMLFile file = new HTMLFile(f.getName(), new Scanner( f, IO.ENCODING));
				
				file.removeAll( Tag.IS_DIV );
				file.removeAll( Tag.IS_BLOCKQUOTE );
				file.removeAll( Tag.IS_IMG );
				file.removeAll( Code.IS_NBSP );
				removeEmptyP(file);
				
				OutputStreamWriter out = IO.newOutputStreamWriter( WRITE_TO.folderName() + IO.DIR_SEP + f.getName() );
				file.print(out);
				out.close();
				
			} catch(FileNotFoundException e){
				System.out.println("FileNotFoundException occured for file "+f.getName()+": "+e.getMessage());
			} catch(UnsupportedEncodingException e){
				System.out.println("UnsupportedEncodingException occured for file "+f.getName()+": "+e.getMessage());
			} catch(IOException e){
				System.out.println("IOException occured for file "+f.getName()+": "+e.getMessage());
			}
		}
		System.out.println("Finished.");
	}
	
	/**
	 * <p>Removes empty paragraph blocks from the specified 
	 * <code>HTMLFile</code>.</p>
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
			if( isVisible(file.get(i) )){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * <p>Returns true if the specified HTMLEntity is visible, 
	 * false otherwise.</p>
	 * @param h the HTMLEntity to be tested for visibility
	 * @return true if the specified HTMLEntity is visible, 
	 * false otherwise.
	 */
	private static boolean isVisible(HTMLEntity h){
		if(h instanceof Tag){
			return false;
		} else{
			if(h instanceof Ch){
				char c = ((Ch)h).c;
				return PhraseProducer.isPhraseChar(c);
			} else{ //it's a Code
				return true; //IDK, but I can't think of any invisible characters that could be here after nbsp are removed.
			}
		}
	}
}
