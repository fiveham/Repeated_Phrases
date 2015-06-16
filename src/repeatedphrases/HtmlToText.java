package repeatedphrases;

import java.io.File;
import java.io.FileNotFoundException;

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
	
	/**
	 * <p>Detects all the .html files in <code>READ_FROM</code>, reads 
	 * them as HTMLFiles, and prints them as .txt files in 
	 * <code>WRITE_TO</code>.</p>
	 * @param args command-line arguments (unused)
	 */
	public static void main(String[] args) {
		//String[] readUs = READ_FROM.folder().list ( IO.IS_HTML );
		File[] readUs = READ_FROM.folder().listFiles( IO.IS_HTML );
		//for(String filename : readUs){
		for(File f : readUs){
			try{
				HTMLFile file = new HTMLFile(f);
				file.printAsText( WRITE_TO.folderName() + IO.DIR_SEP + IO.stripExtension(f.getName()) + IO.TXT_EXT );
			} catch(FileNotFoundException e){
				IO.errorExit(f.getName() + " for reading.");
			}
		}
	}
	
	/*private static String renameAsTxt(String fullFilename){
		int lastSlash = fullFilename.lastIndexOf(IO.DIR_SEP);
		String name = fullFilename.substring(lastSlash+1);
		
		int firstDot = name.indexOf('.');
		String base = firstDot < 0 
				? name 
				: name.substring(0, firstDot);
		
		return WRITE_TO.folderName() + IO.DIR_SEP + base + IO.TXT_EXT;
	}/**/
	
	/*public static String renameAsTxt(String filename){
		int index = filename.lastIndexOf('.');
		String base = index < 0 
				? filename 
				: filename.substring(0,index);
		return base+".txt";
	}/**/
	
	/*private static List<String> validateArgs(String[] args){
		List<String> result = new ArrayList<>();
		for(int pointer=0; pointer < args.length; pointer++){
			switch(args[pointer++]){
			case "-read" : result.addAll(IO.fileContentsAsList(new File(args[pointer]), IO.NEXT, IO.SCANNER_HAS_NEXT));
					break;
			case "-file" : result.add(args[pointer]);
					break;
			default : throw new IllegalArgumentException("Bad command-line arg \""+args[pointer]+"\" at position "+pointer+"with filenamecount "+result.size());
			}
		}
		return result;
	}/**/
}
