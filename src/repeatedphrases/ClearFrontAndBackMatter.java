package repeatedphrases;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * <p>This class reads html files of the ASOIAF novels and 
 * saves copies of them from which the content before the 
 * Prologue and the content after the last chapter have been 
 * removed.</p>
 */
public class ClearFrontAndBackMatter {
	
	/**
	 * <p>The directory from which this class reads html files of the 
	 * ASOIAF books from which it will remove front and back matter.</p>
	 */
	public static final Folder READ_FROM = Folder.HTML_BOOKS_UNSTRUCTURED;
	
	/**
	 * <p>The directory to which this class writes HTML files of the 
	 * ASOIAF books from which it has removed front and back matter.</p>
	 */
	public static final Folder WRITE_TO = Folder.HTML_BOOKS_CHAPTER_CORE;
	
	/**
	 * <p>Detects all the html files for the ASOIAF novels in 
	 * <code>READ_FROM</code>, reads them, detects the paragraph 
	 * containing the title of the Prologue and removes all 
	 * content prior to that paragraph, and detects the paragraph 
	 * containing the title of the first chapter-like feature after 
	 * the last actual chapter and removes everything at or after 
	 * that.<p/>
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		
		handleNovels();
		
		handleNovellas();
		
		System.out.println("Finished.");
	}
	
	private static void handleNovellas(){
		File[] novellaFiles = READ_FROM.folder().listFiles( IO.IS_NOVELLA );
		
		for(File f : novellaFiles){

			System.out.println("Removing front and back matter from the novella "+f.getName());
			
			try{
				HTMLFile file = new HTMLFile(f.getName(), new Scanner(f, IO.ENCODING));
				
				int pWhereFirstWords = firstWordsP(file, f);
				file.removeAll(0,pWhereFirstWords);
				
				int pWhereLastWords = lastWordsP(file, f);
				file.removeAll( pWhereLastWords + 1 );
				
				OutputStreamWriter out = IO.newOutputStreamWriter( WRITE_TO.folderName()+IO.DIR_SEP+f.getName() );
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
	}
	
	private static void handleNovels(){
		File[] novelFiles = READ_FROM.folder().listFiles( IO.IS_NOVEL );
		for(File f : novelFiles){
			
			System.out.println("Removing front and back matter from the novel "+f.getName());
			
			try{
				HTMLFile file = new HTMLFile(f.getName(), new Scanner(f, IO.ENCODING));
				
				int pWherePrologueTitle = prologueTitleP(file);
				file.removeAll(0,pWherePrologueTitle);
				
				int pWhereBackMatterStart = backMatterStart(file);
				file.removeAll(pWhereBackMatterStart);
				
				OutputStreamWriter out = IO.newOutputStreamWriter( WRITE_TO.folderName()+IO.DIR_SEP+f.getName() );
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
	}
	
	/**
	 * <p>Returns <code>i-1</code> where <code>i</code> is 
	 * the index in <code>file</code> of the second 
	 * paragraph containing the word "PROLOGUE", or the first if 
	 * there isn't a second such paragraph.</p>
	 * 
	 * <p>Returning a reference to the second instance accounts 
	 * for the table of contents.</p>
	 * @param file the <code>HTMLFile</code> in which the Prologue 
	 * title is to be found
	 * @return the index in <code>file</code> of the second 
	 * paragraph containing the word "PROLOGUE", or the first if 
	 * there isn't a second such paragraph.
	 */
	private static int prologueTitleP(HTMLFile file){
		Predicate<Integer> predicate = (i) -> file.hasLiteralAt("PROLOGUE", i);
		
		int literalIndex1 = file.adjacentElement(predicate, Direction.NEXT, -1);
		int literalIndex2 = file.adjacentElement(predicate, Direction.NEXT, literalIndex1);
		
		int seekStart = Math.max(literalIndex1, literalIndex2);
		int pLocation = file.adjacentElement(seekStart, Tag.IS_P_OPEN, Direction.PREV);
		
		return pLocation-1;
	}
	
	/**
	 * <p>Returns the index of the opening paragraph tag of the 
	 * last paragraph containing the title of the first non-
	 * chapter chapter-like element of the book whose html file 
	 * <code>file</code> represents.</p>
	 * @param file the HTMLFile whose first paragraph after the 
	 * end of the last real chapter is returned
	 * @return  the index of the opening paragraph tag of the 
	 * last paragraph containing the title of the first non-
	 * chapter chapter-like element of the book whose html file 
	 * <code>file</code> represents.
	 */
	private static int backMatterStart(HTMLFile file){
		String bookName = file.getExtensionlessName().substring(0,4);
		String backMatterStart = backMatterStart(bookName);
		
		Predicate<Integer> predicate = (i) -> file.hasLiteralAt(backMatterStart, i);
		
		int textIndex = file.adjacentElement(predicate, Direction.PREV, file.elementCount());
		int pIndex = file.adjacentElement(textIndex, Tag.IS_P_OPEN, Direction.PREV);
		
		return pIndex;
	}
	
	/**
	 * <p>Returns the title of the first non-chapter chapter-like 
	 * section of the ASOIAF novel named <code>bookName</code>.</p>
	 * @param bookName the name of the ASOIAF novel for which a 
	 * result is to be determined
	 * @return the title of the first non-chapter chapter-like 
	 * section of the ASOIAF novel named <code>bookName</code>.
	 */
	private static String backMatterStart(String bookName){
		switch(bookName){
		case "AFFC" : return "MEANWHILE";
		case "ADWD" : return "WESTEROS";
		default     : return "APPENDIX";
		}
	}
	
	private static String firstWords(String novellaName){
		switch(novellaName){
		case "DE_0.html" : return "The spring rains";
		case "DE_1.html" : return "In an iron";
		case "DE_2.html" : return "A light summer";
		case "PQ.html"   : return "The Dance of";
		case "RP.html"   : return "He was the grandson";
		default : throw new IllegalArgumentException(novellaName+" is not a recognized name of an ASOIAF novella html file.");
		}
	}
	
	private static String lastWords(String novellaName){
		switch(novellaName){
		case "DE_0.html" : return "shows,” he said.";
		case "DE_1.html" : return "hear it’s tall.”";
		case "DE_2.html" : return "of comic dwarfs?";
		case "PQ.html"   : return "Ser Gwayne Hightower.";
		case "RP.html"   : return "danced and died.";
		default : throw new IllegalArgumentException(novellaName+" is not a recognized name of an ASOIAF novella html file.");
		}
	}
	
	/**
	 * <p>Returns the index in <code>file</code> of the closing "p" tag 
	 * of the last paragraph that ends with the 
	 * {@link #lastWords(String) last words} of the specified 
	 * ASOIAF novella.</p>
	 * @param file
	 * @param novella
	 * @return
	 */
	private static int lastWordsP(HTMLFile file, File novella){
		String lastWords = lastWords(novella.getName());
		Predicate<Integer> predicate = (i) -> file.hasLiteralAt(lastWords, i);
		
		int literalIndex = file.adjacentElement(predicate, Direction.PREV, file.elementCount());
		
		return file.adjacentElement(literalIndex, Tag.IS_P_CLOSE, Direction.NEXT);
	}
	
	/**
	 * <p>Returns the index in <code>file</code> of the opening "p" tag 
	 * of the first paragraph that starts with the 
	 * {@link #firstWords(String) first words} of the specified 
	 * ASOIAF novella.</p>
	 * @param file
	 * @param novella
	 * @return
	 */
	private static int firstWordsP(HTMLFile file, File novella){
		String firstWords = firstWords(novella.getName());
		Predicate<Integer> predicate = (i) -> file.hasLiteralAt(firstWords, i);
		
		int literalIndex = file.adjacentElement(predicate, Direction.NEXT, -1);
		
		return file.adjacentElement(literalIndex, Tag.IS_P_OPEN, Direction.PREV);
	}
}
