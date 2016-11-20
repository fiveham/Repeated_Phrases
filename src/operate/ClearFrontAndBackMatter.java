package operate;

import html.Direction;
import html.HTMLFile;
import html.Tag;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.function.Predicate;

import common.Folder;
import common.IO;

import java.util.function.Consumer;
import java.util.HashMap;

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
	 * @see Folder#HTML_BOOKS_UNSTRUCTURED
	 */
	public static final Folder READ_FROM = Folder.HTML_BOOKS_UNSTRUCTURED;
	
	/**
	 * <p>The directory to which this class writes HTML files of the 
	 * ASOIAF books from which it has removed front and back matter.</p>
	 * @see Folder#HTML_BOOKS_CHAPTER_CORE
	 */
	public static final Folder WRITE_TO = Folder.HTML_BOOKS_CHAPTER_CORE;
	
    public static void main(String[] args){
        clearFrontBack(IO.DEFAULT_MSG);
    }
    
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
	public static void clearFrontBack(Consumer<String> msg) {

        handleNovels(msg);

        handleNovellas(msg);
	}
	
	private static void handleNovellas(Consumer<String> msg){
            File[] novellaFiles = READ_FROM.folder().listFiles(IO::isNovella);

            for(File f : novellaFiles){

                msg.accept("Removing front/back matter: "+f.getName());

                try(OutputStreamWriter out = IO.newOutputStreamWriter( WRITE_TO.folderName()+File.separator+f.getName() );){
                    HTMLFile file = new HTMLFile(f.getName(), new Scanner(f, IO.ENCODING));

                    int pWhereFirstWords = firstWordsP(file, f);
                    file.removeAll(0,pWhereFirstWords);

                    int pWhereLastWords = lastWordsP(file, f);
                    file.removeAll( pWhereLastWords + 1 );
                    
                    file.print(out);
                    out.close();

                } catch(FileNotFoundException e){
                    msg.accept("FileNotFoundException occured for file "+f.getName()+": "+e.getMessage());
                } catch(UnsupportedEncodingException e){
                    msg.accept("UnsupportedEncodingException occured for file "+f.getName()+": "+e.getMessage());
                } catch(IOException e){
                    msg.accept("IOException occured for file "+f.getName()+": "+e.getMessage());
                }
            }
	}
	
	private static void handleNovels(Consumer<String> msg){
        File[] novelFiles = READ_FROM.folder().listFiles(IO::isNovel);
        for(File f : novelFiles){

            msg.accept("Removing front/back matter: "+f.getName());

            try(OutputStreamWriter out = IO.newOutputStreamWriter( WRITE_TO.folderName()+File.separator+f.getName() );){
                HTMLFile file = new HTMLFile(f.getName(), new Scanner(f, IO.ENCODING));

                int pWherePrologueTitle = prologueTitleBlock(file, f.getName());
                file.removeAll(0,pWherePrologueTitle);

                int pWhereBackMatterStart = backMatterStart(file);
                file.removeAll(pWhereBackMatterStart);
                
                file.print(out);
                out.close();

            } catch(FileNotFoundException e){
                msg.accept("FileNotFoundException for file "+f.getName());
            } catch(UnsupportedEncodingException e){
                msg.accept("UnsupportedEncodingException  for file "+f.getName());
            } catch(IOException e){
                msg.accept("IOException for file "+f.getName());
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
	private static int prologueTitleBlock(HTMLFile file, String bookname){
        String firstWords = ClearFrontAndBackMatter.FIRST_WORDS.get(bookname);
        Predicate<Integer> hasFirstWordsAt = (i) -> file.hasLiteralAt(firstWords, i);
        
        int chapterStartIndex = file.adjacentElement(hasFirstWordsAt, Direction.NEXT, -1);
        
        Predicate<Integer> isPrologueBlock = (i) -> HTMLFile.IS_PARAGRAPHISH_OPEN.test(file.get(i)) 
        		&& file.hasLiteralBetween("PROLOGUE",i,file.closingMatch(i));
        int pLocation = file.adjacentElement(isPrologueBlock, Direction.PREV, chapterStartIndex);
        
        return pLocation-1;
	}

	public static final char RIGHT_DOUBLE_QUOTE = '\u201D';
	public static final char RIGHT_SINGLE_QUOTE = '\u2019';
	
	private static HashMap<String,String> FIRST_WORDS;
	static{
		FIRST_WORDS = new HashMap<>();
		FIRST_WORDS.put("AGOT.html", "We should start");
		FIRST_WORDS.put("ACOK.html", "The comet"+RIGHT_SINGLE_QUOTE+"s tail");
		FIRST_WORDS.put("ASOS.html", "The day was");
		FIRST_WORDS.put("AFFC.html", "Dragons,â€� said Mollander");
		FIRST_WORDS.put("ADWD.html", "The night was");
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
        String lastWords = LAST_WORDS.get(bookName);
        
        Predicate<Integer> hasLastWordsAt = (i) -> file.hasLiteralAt(lastWords, i);
        
        int textIndex = file.adjacentElement(hasLastWordsAt, Direction.PREV, file.elementCount());
        int pIndex = file.adjacentElement(textIndex, HTMLFile.IS_PARAGRAPHISH_OPEN, Direction.NEXT);
        
        return pIndex;
	}
	
	private static final HashMap<String,String> LAST_WORDS;
	static{
		LAST_WORDS = new HashMap<>();
		LAST_WORDS.put("AGOT","music of dragons.");
		LAST_WORDS.put("ACOK","not dead either.");
		LAST_WORDS.put("ASOS","up and up.");
		LAST_WORDS.put("AFFC","the pig boy."+RIGHT_DOUBLE_QUOTE);
		LAST_WORDS.put("ADWD","hands, the daggers.");
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
        case "DE_0.html" : return "shows,"+RIGHT_DOUBLE_QUOTE+" he said.";
        case "DE_1.html" : return "hear it"+RIGHT_SINGLE_QUOTE+"s tall."+RIGHT_DOUBLE_QUOTE;
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
	 * {@link #FIRST_WORDS(String) first words} of the specified 
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
