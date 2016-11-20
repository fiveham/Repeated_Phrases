package operate;

import java.io.File;
import java.util.function.Consumer;

import common.Folder;

public class RepeatedPhrasesApp {
	
	/**
	 * <p>Ensures that the working directory has the folders specified 
	 * in {@link Folders Folders}.</p>
	 */
	public static void ensureFolders(Consumer<String> msg){
		for(Folder f : Folder.values()){
			File name = f.folder();
			if( !name.exists() ){
				msg.accept("Creating "+name.getName());
				name.mkdir();
			}
		}
	}
}
