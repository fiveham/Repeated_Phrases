package repeatedphrases;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * <p>After the novels have been split into individual chapters 
 * and the novellas have been moved into the folder with them, 
 * and after PQ has been split into body and footnote, run 
 * this class to automagically add the header and footer 
 * content to the novella files.</p>
 */
public class NovellaHeadFoot {
	
	/**
	 * <p>The folder from which this class reads html files of 
	 * ASOIAF novellas.</p>
	 * @see Folder#HTML_CHAPTERS_UNCHECKED
	 */
	public static final Folder READ_FROM = Folder.HTML_CHAPTERS_UNCHECKED;
	
	/**
	 * <p>The folder to which this class writes html files of 
	 * ASOIAF novellas with header and footer content added.</p>
	 * @see Folder#HTML_CHAPTERS_UNCHECKED
	 */
	public static final Folder WRITE_TO = Folder.HTML_CHAPTERS_UNCHECKED;
	
	/**
	 * <p>Detects all the novella files in 
	 * <code>Folder.HTML_CHAPTERS_UNCHECKED</code>, reads them, 
	 * adds html front and back matter and adds chapter-navigation 
	 * tables to the beginning and end of each chapter.</p>
	 * @param args command-line arguments (unused)
	 */
	public static void main(String[] args) {
		String[] novellaFiles = READ_FROM.folder()
				.list( (dir,name) -> IO.IS_HTML.accept(dir,name) 
						&& !IO.IS_NOVEL_CHAPTER.accept(dir,name) );
		
		for(String f : novellaFiles){
			
			System.out.println("Working with file: "+f);
			
			String filename = READ_FROM.folderName() + IO.DIR_SEP + f;
			System.out.println("full name: "+filename);
			
			String content = fileAsString(filename);
			System.out.println("Got content of that file.  It's this long: "+content.length());
			System.out.println("Here's a sample of it: "+IO.shortForm(content));

			String chapterName = getChapterName(f);
			
			try{
				OutputStreamWriter out = IO.newOutputStreamWriter(filename);
				//java.io.PrintStream out = new java.io.PrintStream(filename);
				System.out.println("Created the outputstreamwriter for the file");
				
				SplitChapters.writeHeader(chapterName, out);
				//out.print(SplitChapters.HEADER_FRONT);
				//out.print(chapterName);
				//out.println(SplitChapters.HEADER_BACK);
				System.out.println("added the header to the file");
				
				Scanner read = new Scanner(content);
				while(read.hasNextLine()){
					out.write(read.nextLine()+IO.NEW_LINE);
					//out.println(read.nextLine());
				}
				read.close();
				System.out.println("wrote the original content back into the file");
				
				SplitChapters.writeFooter(chapterName, out);
				//out.print(SplitChapters.FOOTER_FRONT);
				//out.print(chapterName);
				//out.println(SplitChapters.FOOTER_BACK);
				System.out.println("added the footer to the file");
				
				out.close();
				
			} catch(IOException e){
				IO.errorExit(filename + " for writing.");
			}
		}
	}
	
	/**
	 * <p>Returns a string containing all the text contents of the 
	 * file named <code>name</code>.</p>
	 * @param name the name of the file to be returned as a content-string
	 * @return a string containing all the text contents of the 
	 * file named <code>name</code>
	 */
	public static String fileAsString(String name){
		StringBuilder sb = new StringBuilder();
		
		Scanner s = null;
		try{
			s = new Scanner( new File(name), IO.ENCODING);
		} catch(FileNotFoundException e){
			IO.errorExit(name + " for reading.");
		}
		
		if(s.hasNextLine()){
			sb.append(s.nextLine());
		}
		while(s.hasNextLine()){
			sb.append(IO.NEW_LINE).append(s.nextLine());
		}
		s.close();
		
		return sb.toString();
	}
	
	/**
	 * <p>Extracts the name of the chapter that <code>nativeFileName</code> 
	 * contains.</p>
	 * @param nativeFileName the name of the chapter file whose 
	 * chapter title is returned
	 * @return the title of the chapter in the file named 
	 * <code>nativeFileName</code>
	 */
	private static String getChapterName(String nativeFileName){
		String name = IO.stripFolderExtension(nativeFileName);
		
		int u1 = name.indexOf('_');
		int u2 = name.indexOf('_', u1+1);
		
		return name.substring(u2 + 1).replace('_',' ');
	}
}
