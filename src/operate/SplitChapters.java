package operate;

import common.IO;
import html.CharCode;
import html.CharLiteral;
import html.Direction;
import html.HTMLEntity;
import html.HTMLFile;
import html.HTMLFile.BookData;
import html.Tag;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Splits an ASOIAF main series novel HTML file into files for its individual chapters.</p>
 */
class SplitChapters {
	
    /**
     * <p>Detects all html novel files in {@code READ_FROM}, reads them, and saves individual files
     * for each chapter to {@code WRITE_TO}.</p>
     * @param op the Operation whose folders will be used
     * @param args command-line args (not used)
     * @param msg receives and handles messages output by arbitrary parts of this operation
     */
    static void splitChapters(Operation op, String[] args, Consumer<String> msg) {
        handleNovels(op, msg);
        handleNovellas(op, msg);
        handlePQ(op, msg);
    }
    
    private static void handleNovels(Operation op, Consumer<String> msg){
        File[] readUs = op.readFrom().folder().listFiles(IO::isNovel);
        
        Stream.of(readUs)
                .parallel()
                .forEach((f) -> {
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
                            
                            if(isTitleParagraph(paragraph)){
                                writeBuffer(buffer, out, chapterName, msg);
                                
                                chapterName = extractChapterTitle(paragraph);
                                buffer = new ArrayList<>();
                                out = IO.newOutputStreamWriter(op.writeTo().folderName() 
                                        + File.separator 
                                        + chapterFileName(f.getName(), writeCount, chapterName));
                                writeCount++;
                            } else{
                                buffer.addAll(paragraph);
                                
                                //TODO add a constant somewhere to use here. 
                                //Only need 1 instance in memory
                                buffer.add(new CharLiteral('\n'));
                            }
                        }
                        
                        //reached end of file
                        //dump the buffer to a file
                        writeBuffer(buffer, out, chapterName, msg);
                        
                    } catch(FileNotFoundException e){
                        msg.accept("FileNotFoundException occured for file "+f.getName());
                    } catch(UnsupportedEncodingException e){
                        msg.accept("UnsupportedEncodingException occured for file "+f.getName());
                    } catch(IOException e){
                        msg.accept("IOException occured for file "+f.getName());
                    }
                });
    }
    
    private static final List<String> EASY_NOVELLA_NAMES = Stream.of(BookData.values())
            .filter(BookData::isNovella)
            .filter((b) -> b != BookData.PQ)
            .map(BookData::filename)
            .collect(Collectors.toList());
    
    private static void handleNovellas(Operation op, Consumer<String> msg){
        String[] extantEasyNovellas = op.readFrom().folder()
        		.list((dir, name) -> EASY_NOVELLA_NAMES.contains(name));
        
        Stream.of(extantEasyNovellas)
                .parallel()
                .forEach((novella) -> {
                    try(OutputStreamWriter out = IO.newOutputStreamWriter(
                            op.writeTo().folderName() 
                            + File.separator 
                            + novellaOut(novella))){
                        
                        HTMLFile file = new HTMLFile(
                                new File(op.readFrom().folderName() + File.separator + novella));
                        
                        String title = novellaTitle(novella);
                        writeBuffer(file, out, title, msg);
                    } catch(FileNotFoundException e){
                        msg.accept(novella + " not found.");
                    } catch(UnsupportedEncodingException e){
                        msg.accept("UnsupportedEncodingException occured for file " + novella);
                    } catch(IOException e){
                        msg.accept("IOException occured for file " + novella);
                    }
                });
    }
    
    private static void handlePQ(Operation op, Consumer<String> msg){
        String novella = "PQ.html";
        
        try{
            HTMLFile pq = new HTMLFile(new File(op.readFrom().folderName() + File.separator + novella));
            
            int footnoteIndex = pq.adjacentElement(
            		(i) -> pq.hasLiteralAt("Footnote",i), Direction.PREV, pq.elementCount());
            
            int bodyEndIndex = pq.adjacentElement(footnoteIndex, Tag::isPOpen, Direction.PREV);
            List<HTMLEntity> bodySection = pq.section(0, bodyEndIndex);
            
            int footnoteStart = pq.adjacentElement(
            		pq.elementCount(), 
            		Tag::isPOpen, 
            		Direction.PREV);
            List<HTMLEntity> footnoteSection = pq.section(footnoteStart);
            
            HTMLFile body = new HTMLFile("PQ_0_THE_PRINCESS_AND_THE_QUEEN.html", bodySection);
            HTMLFile footnote = new HTMLFile("PQ_1_FOOTNOTE.html", footnoteSection);
            
            String[] hrefs = {
            		"PQ_1_FOOTNOTE.html#FOOTNOTE", 
            		"PQ_0_THE_PRINCESS_AND_THE_QUEEN.html#FOOTNOTE"
            };
            HTMLFile[] files = {body, footnote};
            for(int i = 0; i < hrefs.length; i++){
                HTMLFile file = files[i];
                
                //replace superscript 1 with asterisk
                int noteIndex = file.adjacentElement(-1, Tag::isSup, Direction.NEXT);
                noteIndex = file.adjacentElement(noteIndex, CharLiteral::is1, Direction.NEXT);
                file.set(noteIndex, new CharLiteral('*'));
                
                //replace internal link with external link
                int noteAnchorIndex = file.adjacentElement(
                		noteIndex, 
                		Tag::isAnchorOpen, 
                		Direction.PREV);
                file.set(noteAnchorIndex, new Tag("a id=\"FOOTNOTE\" href=\"" + hrefs[i] + "\"" ));
                
                //save the file
                OutputStreamWriter out = IO.newOutputStreamWriter(
                		op.writeTo().folderName() 
                		+ File.separator 
                		+ file.getName());
                writeBuffer(file, out, chapterName(file), msg);
            }
        } catch(FileNotFoundException e){
            msg.accept(novella + " not found by SplitChapters");
        } catch(UnsupportedEncodingException e){
            msg.accept("UnsupportedEncodingException occured for file "+novella);
        } catch(IOException e){
            msg.accept("IOException occured for file "+novella);
        }
    }
    
    private static String chapterName(HTMLFile file){
        String filename = file.getName();
        filename = IO.stripFolderExtension(filename);
        
        int a = filename.indexOf(IO.FILENAME_COMPONENT_SEPARATOR);
        int b = filename.indexOf(IO.FILENAME_COMPONENT_SEPARATOR, a + 1);
        
        String result = filename.substring(b + 1);
        return result;
    }
    
    /**
     * <p>Returns the name of the chapter-file for novellas after they've had head/foot tables
     * added.</p>
     * @param novellaName
     * @return
     */
    private static String novellaOut(String novellaName){
        StringBuilder result = new StringBuilder(IO.stripExtension(novellaName));
        
        if("RP.html".equals(novellaName)){
            result.append("_0");
        }
        result.append(IO.FILENAME_COMPONENT_SEPARATOR_CHAR)
        		.append(novellaTitle(novellaName)
        				.replace(' ', IO.FILENAME_COMPONENT_SEPARATOR_CHAR))
        		.append(IO.HTML_EXT);
        
        return result.toString();
    }
    
    private static String novellaTitle(String novellaName){
        switch(novellaName){
        case "DE_0.html" : return "THE HEDGE KNIGHT";
        case "DE_1.html" : return "THE SWORN SWORD";
        case "DE_2.html" : return "THE MYSTERY KNIGHT";
        case "RP.html"   : return "THE ROGUE PRINCE";
        default : throw new IllegalArgumentException(
        		novellaName+" is not a recognized ASOIAF novella with a single chapter.");
        }
    }
    
    /**
     * <p>Returns true if the {@code paragraph}'s only character-type contents are characters that
     * can appear in chapter titles, false otherwise.</p>
     * @param paragraph a list of HTMLEntity, a piece of an HTMLFile
     * @return true if the {@code paragraph}'s only character-type contents are characters that can
     * appear in chapter titles, false otherwise
     */
    private static boolean isTitleParagraph(List<HTMLEntity> paragraph){
        int titleCharCount = 0;
        
        for(HTMLEntity h : paragraph){
            if(CharCode.class.isInstance(h)){
                return false;
            } else if(CharLiteral.class.isInstance(h)){
                if(isLegalChapterTitleCharacter(((CharLiteral)h).c)){
                    titleCharCount++;
                } else{
                    return false;
                }
            }
        }

        return titleCharCount > 0;
    }
    
    /**
     * <p>Returns true if {@code c} occurs in chapters' titles, false otherwise.</p>
     * @param c a char to be tested for status as a character that occurs in chapters' titles
     * @return true if {@code c} is an uppercase letter, space, or apostrophe
     */
	public static boolean isLegalChapterTitleCharacter(char c){
		return ('A'<=c && c<='Z') || c==' ' || c=='\'';
	}
	
	private static void writeBuffer(
	        HTMLFile file, 
	        OutputStreamWriter out, 
	        String chaptername, 
	        Consumer<String> msg) throws IOException{

        writeBuffer(file.section(0), out, chaptername, msg);
	}
	
    /**
     * <p>Writes the contents of {@code buffer} to a file via {@code out}, prepended with
     * {@link #writeHeader(String,OutputStreamWriter) a header} and appended with
     * {@link #writerFooter(String,OutputStreamWriter) a footer}. If {@code out == null}, does
     * nothing.</p>
     * @param buffer a list of HTMLEntitys to be written as html text to a file via {@code out}
     * @param out writes to the file to which the content of {@code buffer} should be written; is
     * {@link OutputStreamWriter#close() closed}
     * @param chapterName the name of the chapter being written, to be added to the header/footer
     * tables
     * @throws IOException if an I/O error occurs writing to the file through {@code out}
     */
    private static void writeBuffer(
    		List<HTMLEntity> buffer, 
    		OutputStreamWriter out, 
    		String chapterName, 
    		Consumer<String> msg) throws IOException{
    	
        if(out != null){
            msg.accept("Saving a chapter titled " + chapterName);
            writeHeader(chapterName, out);
            for(HTMLEntity e : buffer){
                out.write(e.toString());
            }
            writeFooter(chapterName, out);
            out.close();
        }
    }
    
    /**
     * <p>Returns the name of the file to which a chapter's content will be written.</p>
     * @param bookFile the source file from {@code READ_FROM} from which the chapter's content was
     * extracted
     * @param chapterIndex the chapter's number in its book (zero-based)
     * @param chapterName the name of the chapter as extracted from the text of its source html
     * novel file, including spaces
     * @return the name of the file to which a chapter's content will be written
     */
    private static String chapterFileName(String bookFile, int chapterIndex, String chapterName){
        String bookName = IO.stripFolderExtension(bookFile);
        return bookName 
        		+ IO.FILENAME_COMPONENT_SEPARATOR_CHAR 
        		+ chapterIndex 
        		+ IO.FILENAME_COMPONENT_SEPARATOR_CHAR 
        		+ chapterName.replace(' ', IO.FILENAME_COMPONENT_SEPARATOR_CHAR) 
        		+ IO.HTML_EXT;
    }
    
    /**
     * <p>Extracts a chapter's title from a {@code paragraph}
     * {@link #isTitleParagraph(List<HTMLEntity>) containing a chapter title}.</p>
     * @param paragraph the paragraph whose contained chapter title is extracted and returned
     * @return the chapter title that's the sole visible content of the specified {@code paragraph}
     */
    private static String extractChapterTitle(List<HTMLEntity> paragraph){
        StringBuilder result = new StringBuilder( paragraph.size() );

        for(HTMLEntity h : paragraph){
            if(CharLiteral.class.isInstance(h)){
                result.append(((CharLiteral)h).c);
            }
        }

        return result.toString();
    }
    
    /**
     * <p>Of the header content to be added to individual chapters' files, this is the portion prior
     * to the name of the chapter.</p>
     * @see #writeHeader(String,OutputStreamWriter)
     */
    public static final String HEADER_FRONT = 
    		"<html><head><meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" />"
            + "<link href=\"style.css\" rel=\"stylesheet\" type=\"text/css\" /></head><body>"
            + "<div class=\"chapter\"><div class=\"head\"><table class=\"head\"><tr class=\"head\">"
            + "<td class=\"prev_chapter\"><p class=\"prev_chapter\">"
            + "<a id=\"prev_chapter\" href=\"nowhere\" title=\"nothing\" style=\"change_chapter\">"
            + "&lt;&lt;</a></p></td><td class=\"chapter_title\"><p class=\"chapter_title\">";
    
    /**
     * <p>Of the header content to be added to individual chapters' files, this is the portion after
     * the name of the chapter.</p>
     * @see #writeHeader(String,OutputStreamWriter)
     */
    public static final String HEADER_BACK = 
    		"</p></td><td class=\"next_chapter\"><p class=\"next_chapter\">"
            + "<a id=\"next_chapter\" href=\"nowhere\" title=\"nothing\" style=\"change_chapter\">"
            + "&gt;&gt;</a></p></td></tr></table></div><div class=\"chapter_body\">";
    
    public static String header(String chapterName){
        return HEADER_FRONT + chapterName + HEADER_BACK + IO.NEW_LINE;
    }
    
    /**
     * <p>Of the footer content to be added to individual chapters' files, this is the portion prior
     * to the name of the chapter.</p>
     * @see #writeFooter(String,OutputStreamWriter)
     */
    public static final String FOOTER_FRONT = 
    		"</div><div class=\"foot\"><table class=\"foot\"><tr class=\"foot\">"
            + "<td class=\"prev_chapter\"><p class=\"prev_chapter\">"
            + "<a id=\"prev_chapter\" href=\"nowhere\" title=\"nothing\" style=\"change_chapter\">"
            + "&lt;&lt;</a></p></td><td class=\"chapter_title\"><p class=\"chapter_title\">";
    
    /**
     * <p>Of the footer content to be added to individual chapters' files, this is the portion after
     * the name of the chapter.</p>
     * @see #writeFooter(String,OutputStreamWriter)
     */
    public static final String FOOTER_BACK = 
    		"</p></td><td class=\"next_chapter\"><p class=\"next_chapter\">"
            + "<a id=\"next_chapter\" href=\"nowhere\" title=\"nothing\" style=\"change_chapter\">"
            + "&gt;&gt;</a></p></td></tr></table></div></div></body></html>";
    
    public static String footer(String chapterName){
        return FOOTER_FRONT + chapterName + FOOTER_BACK;
    }
    
    /**
     * <p>Adds a header to the specified file consisting of an opening html tag, opening head tag,
     * charset and stylesheet specifications, closing head tag, opening body tag, opening div tag
     * for the overall chapter, opening div tag for the chapter-navigation table before the chapter
     * body, chapter-navigation table before the chapter body with the chapter's name included,
     * closing div for the header table, and an opening div for the body of the chapter.</p>
     * @param chapterName the name of the chapter to which this header will be applied by being
     * added to the central cell of the header chapter-navigation table
     * @param out specifies the file to which the header is written
     * @throws IOException if an I/O error occurs while writing to the file
     */
    public static void writeHeader(String chapterName, OutputStreamWriter out) throws IOException{
        out.write(header(chapterName));
    }
    
    /**
     * <p>Adds a footer to the specified file consisting of a closing div for the chapter body, a
     * chapter-navigation table containing the chapter's title, in its own div, a closing div tag
     * for the overall chapter, and closing body and html tags.</p>
     * @param chapterName the name of the chapter to which this footer is applied
     * @param out specifies the file to which the footer is written
     * @throws IOException if an I/O error occurs while writing the file
     */
    public static void writeFooter(String chapterName, OutputStreamWriter out) throws IOException{
        out.write(footer(chapterName));
    }
    
    /**
     * <p>Returns true if the specified HTMLEntity is a literal character that is legal for a
     * chapter title, false otherwise.</p>
     * @param h an HTMLEntity to be tested for status as a legal chapter title character
     * @return true if the specified HTMLEntity is a literal character that is legal for a chapter
     * title, false otherwise
     */
    public static boolean isTitleChar(HTMLEntity h){
        return CharLiteral.class.isInstance(h) && isTitle(((CharLiteral)h).c);
    }
    
    /**
     * <p>Returns true if the specified char is legal for a chapter title, false otherwise. A char
     * is legal for a chapter title if it is a capital letter, a space, or an apostrophe</p>
     * @param c a {@code char} to test for legality in chapter titles
     * @return true if the specified char is legal for a chapter title, false otherwise
     */
    public static boolean isTitle(char c){
        return ('A' <= c && c <= 'Z') 
                || c == ' ' 
                || c == '\'';
    }
}
