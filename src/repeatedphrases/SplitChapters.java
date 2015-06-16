package repeatedphrases;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * <p>Splits an ASOIAF main series novel HTML file into 
 * files for its individual chapters.</p>
 */
public class SplitChapters {
	
	/**
	 * <p>The folder from which this class reads html files of the ASOIAF novels 
	 * which have been prepared to be split into individual chapters.</p>
	 * @see Folder#HTML_BOOKS_CORRECT_APOSTROPHES
	 */
	public static final Folder READ_FROM = Folder.HTML_BOOKS_CORRECT_APOSTROPHES;
	
	/**
	 * <p>The folder to which this class writes individual chapter html files 
	 * from the ASOIAF novels.</p>
	 * @see Folder#HTML_CHAPTERS_UNCHECKED
	 */
	public static final Folder WRITE_TO = Folder.HTML_CHAPTERS_UNCHECKED;
	
	/**
	 * <p>Detects all html novel files in <code>READ_FROM</code>, reads them, and 
	 * saves individual files for each chapter to <code>WRITE_TO</code>.</p>
	 * @param args command-line arguments (unused)
	 */
	public static void main(String[] args) {
		
		handleNovels();
		
		handleNovellas();
		
		handlePQ();
	}
	
	private static void handleNovels(){
		//String[] readUs = READ_FROM.folder().list ( IO.IS_NOVEL );
		File[] readUs = READ_FROM.folder().listFiles( IO.IS_NOVEL );
		
		//for(String filename : readUs){
		for(File f : readUs){
			try{
				HTMLFile file = new HTMLFile(f.getName(), new Scanner(f, IO.ENCODING));
				
				HTMLFile.ParagraphIterator piter = file.paragraphIterator();
				List<HTMLEntity> buffer = new ArrayList<>();
				OutputStreamWriter out = null;
				int writeCount = 0;
				String chapterName = null;
				
				while(piter.hasNext()){
					int[] paragraphBounds = piter.next();
					
					List<HTMLEntity> paragraph = file.section(paragraphBounds);
					
					if( isTitleParagraph(paragraph) ){
						writeBuffer(buffer, out, chapterName);
						
						chapterName = extractChapterTitle(paragraph);
						buffer = new ArrayList<>();
						out = IO.newOutputStreamWriter( WRITE_TO.folderName() 
								+ IO.DIR_SEP 
								+ chapterFileName(f.getName(), writeCount, chapterName) );
						writeCount++;
					} else{
						buffer.addAll(paragraph);
						buffer.add(new Ch('\n'));
					}
				}
				
				//reached end of file
				//dump the buffer to a file
				writeBuffer(buffer, out, chapterName);
				
			} catch(FileNotFoundException e){
				System.out.println("FileNotFoundException occured for file "+f.getName()+": "+e.getMessage());
			} catch(UnsupportedEncodingException e){
				System.out.println("UnsupportedEncodingException occured for file "+f.getName()+": "+e.getMessage());
			} catch(IOException e){
				System.out.println("IOException occured for file "+f.getName()+": "+e.getMessage());
			}
		}
	}
	
	private static void handleNovellas(){
		String[] easyNovellas = { "DE_0.html", "DE_1.html", "DE_2.html", "RP.html" };
		
		for(String novella : easyNovellas){
			try{
				OutputStreamWriter out = IO.newOutputStreamWriter( WRITE_TO.folderName() + IO.DIR_SEP + novellaOut(novella) );
				
				HTMLFile file = new HTMLFile(new File(READ_FROM.folderName() + IO.DIR_SEP + novella));
				List<HTMLEntity> pseudoBuffer = file.section(0);
				
				String title = novellaTitle(novella);
				writeBuffer(pseudoBuffer,out,title);
				
			} catch(FileNotFoundException e){
				System.out.println("FileNotFoundException occured for file "+novella+": "+e.getMessage());
			} catch(UnsupportedEncodingException e){
				System.out.println("UnsupportedEncodingException occured for file "+novella+": "+e.getMessage());
			} catch(IOException e){
				System.out.println("IOException occured for file "+novella+": "+e.getMessage());
			}
		}
	}
	
	private static void handlePQ(){
		String novella = "PQ.html";
		
		try{
			HTMLFile pq = new HTMLFile( new File(READ_FROM.folderName() + IO.DIR_SEP + novella));
			
			int footnoteIndex = pq.adjacentElement( (i) -> pq.hasLiteralAt("Footnote",i), Direction.PREV, pq.elementCount());
			int bodyEndIndex = pq.adjacentElement( footnoteIndex, Tag.IS_P_OPEN, Direction.PREV);
			List<HTMLEntity> bodySection = pq.section(0,bodyEndIndex);
			
			int footnoteStart = pq.adjacentElement( pq.elementCount(), Tag.IS_P_OPEN, Direction.PREV);
			List<HTMLEntity> footnoteSection = pq.section(footnoteStart);
			
			HTMLFile body = new HTMLFile("PQ_0_THE_PRINCESS_AND_THE_QUEEN.html", bodySection);
			HTMLFile footnote = new HTMLFile("PQ_1_FOOTNOTE.html", footnoteSection);
			
			Predicate<HTMLEntity> isSuperscript1 = (h) -> HTMLFile.IS_CH.test(h) && ((Ch)h).c == '1';
			
			//replace superscript 1 with asterisk
			//in body
			int bodyNote = body.adjacentElement(-1, Tag.IS_SUP, Direction.NEXT);
			bodyNote = body.adjacentElement(bodyNote, isSuperscript1, Direction.NEXT);
			body.set(bodyNote, new Ch('*'));
			//in footnote
			int footnoteNote = footnote.adjacentElement(-1, Tag.IS_SUP, Direction.NEXT);
			footnoteNote = footnote.adjacentElement(footnoteNote, isSuperscript1, Direction.NEXT);
			footnote.set(footnoteNote, new Ch('*'));
			
			//alter the footnote-anchors
			int bodyNoteAnchor = body.adjacentElement(bodyNote, Tag.IS_A_OPEN, Direction.PREV);
			body.set(bodyNoteAnchor, new Tag( "a id=\"FOOTNOTE\" href=\"PQ_1_FOOTNOTE.html#FOOTNOTE\"" ) );
			int footnoteNoteAnchor = footnote.adjacentElement(footnoteNote, Tag.IS_A_OPEN, Direction.PREV);
			footnote.set(footnoteNoteAnchor, new Tag( "a id=\"FOOTNOTE\" href=\"PQ_0_THE_PRINCESS_AND_THE_QUEEN.html#FOOTNOTE\"" ) );
			
			//write the files
			OutputStreamWriter bodyOut = IO.newOutputStreamWriter(WRITE_TO.folderName() + IO.DIR_SEP + body.getName());
			writeBuffer(body.section(0),bodyOut, body.chapterName());
			
			OutputStreamWriter footnoteOut = IO.newOutputStreamWriter(WRITE_TO.folderName() + IO.DIR_SEP + footnote.getName());
			writeBuffer(footnote.section(0),footnoteOut, footnote.chapterName());
			
		} catch(FileNotFoundException e){
			System.out.println("FileNotFoundException occured for file "+novella+": "+e.getMessage());
		} catch(UnsupportedEncodingException e){
			System.out.println("UnsupportedEncodingException occured for file "+novella+": "+e.getMessage());
		} catch(IOException e){
			System.out.println("IOException occured for file "+novella+": "+e.getMessage());
		}
	}
	
	/**
	 * <p>Returns the name of the chapter-file for novellas 
	 * after they've had head/foot tables added.</p>
	 * @param novellaName
	 * @return
	 */
	private static String novellaOut(String novellaName){
		StringBuilder result = new StringBuilder(IO.stripExtension(novellaName));
		
		if( "RP.html".equals(novellaName) ){
			result.append("_0");
		}
		result.append('_').append(novellaTitle(novellaName).replace(' ','_')).append(IO.HTML_EXT);
		
		return result.toString();
	}
	
	private static String novellaTitle(String novellaName){
		switch(novellaName){
		case "DE_0.html" : return "THE HEDGE KNIGHT";
		case "DE_1.html" : return "THE SWORN SWORD";
		case "DE_2.html" : return "THE MYSTERY KNIGHT";
		case "RP.html"   : return "THE ROGUE PRINCE";
		default : throw new IllegalArgumentException(novellaName+" is not a recognized ASOIAF novella with a single chapter.");
		}
	}
	
	/**
	 * <p>Returns true if the <code>paragraph</code>'s only character-type 
	 * contents are characters that can appear in chapter titles, 
	 * false otherwise.</p>
	 * @param paragraph a list of HTMLEntity, a piece of an HTMLFile
	 * @return true if the <code>paragraph</code>'s only character-type 
	 * contents are characters that can appear in chapter titles, 
	 * false otherwise
	 */
	private static boolean isTitleParagraph(List<HTMLEntity> paragraph){
		int titleCharCount = 0;
		
		for(HTMLEntity h : paragraph){
			if( HTMLFile.IS_CODE.test(h)){
				return false;
			} else if( HTMLFile.IS_CH.test(h) ){
				if(IO.isLegalChapterTitleCharacter( ((Ch)h).c ) ){
					titleCharCount++;
				} else{
					return false;
				}
			}
		}
		
		return titleCharCount > 0;
	}
	
	/**
	 * <p>Writes the contents of <code>buffer</code> to a file via <code>out</code>, 
	 * prepended with {@link #writeHeader(String,OutputStreamWriter) a header} and 
	 * appended with {@link #writerFooter(String,OutputStreamWriter) a footer}. If 
	 * <code>out == null</code>, does nothing.</p>
	 * @param buffer a list of HTMLEntitys to be written as html text to a 
	 * file via <code>out</code>
	 * @param out writes to the file to which the content of <code>buffer</code> 
	 * should be written
	 * @param chapterName the name of the chapter being written, to be added to the 
	 * header/footer tables
	 * @throws IOException if an I/O error occurs writing to the file through 
	 * <code>out</code>
	 */
	private static void writeBuffer(List<HTMLEntity> buffer, OutputStreamWriter out, String chapterName) throws IOException{
		if(out != null){
			System.out.println("Saving a chapter titled " + chapterName);
			writeHeader(chapterName, out);
			for(HTMLEntity h : buffer){
				out.write(h.toString());
			}
			writeFooter(chapterName, out);
			out.close();
		}
	}
	
	/**
	 * <p>Returns the name of the file to which a chapter's content will be written.</p>
	 * @param bookFile the source file from <code>READ_FROM</code> from which the 
	 * chapter's content was extracted
	 * @param chapterIndex the chapter's number in its book (zero-based)
	 * @param chapterName the name of the chapter as extracted from the text of its 
	 * source html novel file, including spaces
	 * @return the name of the file to which a chapter's content will be written
	 */
	private static String chapterFileName(String bookFile, int chapterIndex, String chapterName){
		String bookName = IO.stripFolderExtension(bookFile);
		return bookName + '_' + chapterIndex + '_' + chapterName.replace(' ','_') + IO.HTML_EXT;
	}
	
	/**
	 * <p>Extracts a chapter's title from a <code>paragraph</code> 
	 * {@link #isTitleParagraph(List<HTMLEntity>) containing a chapter title}.</p>
	 * @param paragraph the paragraph whose contained chapter title 
	 * is extracted and returned
	 * @return the chapter title that's the sole visible content of 
	 * the specified <code>paragraph</code>
	 */
	private static String extractChapterTitle(List<HTMLEntity> paragraph){
		StringBuilder result = new StringBuilder( paragraph.size() );
		
		for( HTMLEntity h : paragraph ){
			if( HTMLFile.IS_CH.test(h) ){
				result.append( ((Ch)h).c );
			}
		}
		
		return result.toString();
	}
	
	/**
	 * <p>Of the header content to be added to individual chapters' files, 
	 * this is the portion prior to the name of the chapter.</p>
	 * @see #writeHeader(String,OutputStreamWriter)
	 */
	public static final String HEADER_FRONT = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" />"
			+ "<link href=\"style.css\" rel=\"stylesheet\" type=\"text/css\" /></head><body><div class=\"chapter\">"
			+ "<div class=\"head\"><table class=\"head\"><tr class=\"head\"><td class=\"prev_chapter\"><p class=\"prev_chapter\">"
			+ "<a id=\"prev_chapter\" href=\"nowhere\" title=\"nothing\" style=\"change_chapter\">&lt;&lt;</a></p></td>"
			+ "<td class=\"chapter_title\"><p class=\"chapter_title\">";
	
	/**
	 * <p>Of the header content to be added to individual chapters' files, 
	 * this is the portion after the name of the chapter.</p>
	 * @see #writeHeader(String,OutputStreamWriter)
	 */
	public static final String HEADER_BACK = "</p></td><td class=\"next_chapter\"><p class=\"next_chapter\">"
			+ "<a id=\"next_chapter\" href=\"nowhere\" title=\"nothing\" style=\"change_chapter\">&gt;&gt;</a></p></td></tr>"
			+ "</table></div><div class=\"chapter_body\">";
	
	/**
	 * <p>Adds a header to the specified file consisting of an 
	 * opening html tag, opening head tag, charset and stylesheet 
	 * specifications, closing head tag, opening body tag, 
	 * opening div tag for the overall chapter, opening div 
	 * tag for the chapter-navigation table before the chapter 
	 * body, chapter-navigation table before the chapter body 
	 * with the chapter's name included, closing div for the header 
	 * table, and an opening div for the body of the chapter.</p>
	 * @param chapterName the name of the chapter to which this 
	 * header will be applied by being added to the central cell 
	 * of the header chapter-navigation table
	 * @param out specifies the file to which the header is written
	 * @throws IOException if an I/O error occurs while writing 
	 * to the file
	 */
	public static void writeHeader(String chapterName, OutputStreamWriter out) throws IOException{
		String header = HEADER_FRONT + chapterName + HEADER_BACK + IO.NEW_LINE;
		out.write(header);
	}
	
	/**
	 * <p>Of the footer content to be added to individual chapters' files, 
	 * this is the portion prior to the name of the chapter.</p>
	 * @see #writeFooter(String,OutputStreamWriter)
	 */
	public static final String FOOTER_FRONT = "</div><div class=\"foot\"><table class=\"foot\"><tr class=\"foot\">"
			+ "<td class=\"prev_chapter\"><p class=\"prev_chapter\">"
			+ "<a id=\"prev_chapter\" href=\"nowhere\" title=\"nothing\" style=\"change_chapter\">&lt;&lt;</a></p></td>"
			+ "<td class=\"chapter_title\"><p class=\"chapter_title\">";
	
	/**
	 * <p>Of the footer content to be added to individual chapters' files, 
	 * this is the portion after the name of the chapter.</p>
	 * @see #writeFooter(String,OutputStreamWriter)
	 */
	public static final String FOOTER_BACK = "</p></td><td class=\"next_chapter\"><p class=\"next_chapter\">"
			+ "<a id=\"next_chapter\" href=\"nowhere\" title=\"nothing\" style=\"change_chapter\">&gt;&gt;</a></p></td></tr>"
			+ "</table></div></div></body></html>";
	
	/**
	 * <p>Adds a footer to the specified file consisting of 
	 * a closing div for the chapter body, a chapter-navigation 
	 * table containing the chapter's title, in its own div, a 
	 * closing div tag for the overall chapter, and closing body 
	 * and html tags.</p>
	 * @param chapterName the name of the chapter to which this 
	 * footer is applied
	 * @param out specifies the file to which the footer is 
	 * written
	 * @throws IOException if an I/O error occurs while writing 
	 * the file
	 */
	public static void writeFooter(String chapterName, OutputStreamWriter out) throws IOException{
		String footer = FOOTER_FRONT + chapterName + FOOTER_BACK;
		out.write(footer);
	}
	
	/**
	 * <p>Returns true if the specified HTMLEntity is a 
	 * literal character that is legal for a chapter 
	 * title, false otherwise.</p>
	 * @param h an HTMLEntity to be tested for status as a
	 * legal chapter title character
	 * @return  true if the specified HTMLEntity is a 
	 * literal character that is legal for a chapter 
	 * title, false otherwise
	 */
	public static boolean isTitleChar(HTMLEntity h){
		return h instanceof Ch && isTitle(((Ch)h).c);
	}
	
	/**
	 * <p>Returns true if the specified char is legal for 
	 * a chapter title, false otherwise. A char is legal 
	 * for a chapter title if it is a capital letter, a 
	 * space, or an apostrophe</p>
	 * @param c a <code>char</code> to test for legality 
	 * in chapter titles
	 * @return true if the specified char is legal for 
	 * a chapter title, false otherwise
	 */
	public static boolean isTitle(char c){
		return ('A' <= c && c <= 'Z') || c == ' ' || c == '\'';
	}
	
	/* *
	 * Returns a String made of exactly the literal characters 
	 * represented between the first opening and closing p tags 
	 * after the specified point in the specified HTMLFile, in 
	 * the same order in which they occur in the HTMLFile.
	 * @param file HTMLFile from which literal characters are 
	 * extracted to form a chapter title.
	 * @param start position in the HTMLFile to start looking for 
	 * a paragraph from which to extract a chapter title.
	 * @return a String representation of the chapter title 
	 * contained in the first paragraph following the specified 
	 * <code>start</code> point in the file.
	 * /
	private static String chapterName(HTMLFile file, int start){
		int nextP = file.adjacentElement(start-1, Tag.IS_P_OPEN, Direction.NEXT);
		int nextPEnd = file.adjacentElement(nextP, Tag.IS_P_CLOSE, Direction.NEXT);
		
		StringBuilder result = new StringBuilder();
		
		for(int i=nextP+1; i<nextPEnd; i++){
			if(HTMLFile.IS_CH.test(file.get(i))){
				result.append( ((Ch)file.get(i)).c );
			}
		}
		
		return result.toString();
	}/**/
	
	/* *
	 * Returns true if the element at position <code>position</code> 
	 * in the specified HTMLFile is the opening p tag of a paragraph 
	 * containing exactly the title of a chapter, false otherwise.
	 * @param position
	 * @param file
	 * @return
	 * /
	private static boolean meetsDumpConditions(int position, HTMLFile file){
		if(position >= file.elementCount()){
			return true;
		} else{
			HTMLEntity h = file.get(position);
			if( Tag.IS_P_OPEN.test(h) ){
				int close = file.adjacentElement(position, Tag.IS_P_CLOSE, Direction.NEXT);
				return hasOnlyTitleChars(file, position+1, close);
			} else{
				return false;
			}
		}
	}/**/
	
	/* *
	 * Returns true if the literal characters in the region from 
	 * start inclusive to end exclusive are all legal title 
	 * characters, false otherwise.
	 * 
	 * Legal title characters are capital letters, apostrophe, 
	 * and space.
	 * @param file
	 * @param start inclusive lower bound of test region
	 * @param end exclusive upper bound of test region
	 * @return
	 * /
	private static boolean hasOnlyTitleChars(HTMLFile file, int start, int end){
		//System.out.print("\nTesting titlehood: ");
		for(int i=start; i<end; i++){
			//System.out.print(file.get(i).toString());
			if( !isTitleChar(file.get(i)) ){
				return false;
			}
		}
		//System.out.println("\nAll good");
		return true;
	}/**/
	
	/* *
	 * Returns the name of the book in the ASOIAF series to which 
	 * the specified source file name pertains.
	 * 
	 * Any prepended folders are ignored and the first four characters 
	 * of the name of the file itself are returned.
	 * @param srcFileName
	 * @return
	 * /
	private static String getBookName(String srcFileName){
		String nativeName = srcFileName.substring( srcFileName.lastIndexOf(IO.DIR_SEP)+1 );
		return nativeName.substring(0,4);
	}/**/
	
	/* *
	 * Returns the name of the file to which the content for 
	 * the chapter with the specified name should be saved.
	 * @param chapterTitle
	 * @return
	 * /
	private static String outName(String bookName, int chapterIndex, String chapterTitle){
		return WRITE_TO.folderName() + IO.DIR_SEP 
				+ bookName + '_' 
				+ chapterIndex + '_' 
				+ chapterTitle.replace(' ','_') + IO.HTML_EXT;
	}/**/
	
	/* *
	 * Prints the String-equivalent content of the specified HTMLFile from 
	 * inclusive startPoint to inclusive endPoint to the file specified via 
	 * the OutputStreamWriter <code>out</code>.  That content is prepended 
	 * with necessary header content and appended with necessary footer 
	 * content, which includes opening and closing html, head, and body tags, 
	 * chapter-navigation tables above and below the chapter body, 
	 * divs aroud the tables, around the chapter body, and around those divs.
	 * @param startPoint inclusive start point for content to be extracted 
	 * from the specified HTMLFile for printing.
	 * @param endPoint inclusive end point for content to be extracted from 
	 * the specified HTMLFile for printing.
	 * @param file The HTMLFile from which to extract content for printing a 
	 * chapter.
	 * @param out Specifies the file to which the chapter being printed will 
	 * be printed.
	 * @param chapterName The literal name of the chapter being printed, as 
	 * extracted from the original HTML file.
	 * @throws IOException
	 * /
	private static void dump(int startPoint, int endPoint, HTMLFile file, OutputStreamWriter out, String chapterName) throws IOException{
		if(out!=null){
			writeHeader(chapterName, out);
			for(int i=startPoint; i<=endPoint; i++){
				out.write(file.get(i).toString());
			}
			writeFooter(chapterName, out);
		}
	}/**/
	
	/*public static void mane(String[] args) {
		String[] readUs = READ_FROM.folder().list ( IO.IS_NOVEL );
		
		for(String filename : readUs){
			try{
				//System.out.println("Extracting an html file from: " + READ_FROM.folderName() + IO.DIR_SEP + filename);
				HTMLFile file = new HTMLFile(filename, new Scanner(new File(READ_FROM.folderName()+IO.DIR_SEP+filename), IO.ENCODING));
				
				OutputStreamWriter out = null;
				String chapterName = null;
				int startPoint = 0;
				int endPoint = startPoint;
				
				int dumpCount = -1;
				String bookName = getBookName(filename);
				
				for(int position=0; position < file.elementCount(); position++){
					if( meetsDumpConditions(position, file )){
						dump(startPoint, endPoint, file, out, chapterName);
						
						dumpCount++;
						
						//determine the name of the new chapter.
						chapterName = chapterName(file, startPoint);
						
						//create new output file for the new chapter
						out = IO.newOutputStreamWriter( outName(bookName, dumpCount, chapterName) );
						
						//jump over the rest of the paragraph.
						//the increment term in the loop header moves us 
						//beyond the paragraph altogether.
						position = file.adjacentElement(position, Tag.IS_P_CLOSE, Direction.NEXT);
						
						//reset the start point for this new chapter, 
						//and reset the end point to match
						endPoint = startPoint = position+1;
					} else{
						endPoint++;
					}
				}
				
				out = IO.newOutputStreamWriter( filename );
				file.print(out);
				out.close();
				
			} catch(FileNotFoundException e){
				System.out.println("FileNotFoundException occured for file "+filename+": "+e.getMessage());
			} catch(UnsupportedEncodingException e){
				System.out.println("UnsupportedEncodingException occured for file "+filename+": "+e.getMessage());
			} catch(IOException e){
				System.out.println("IOException occured for file "+filename+": "+e.getMessage());
			}
		}
	}/**/
}
