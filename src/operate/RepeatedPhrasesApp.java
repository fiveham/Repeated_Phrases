package operate;

import common.IO;
import html.AnchorInfo;
import html.HTMLFile;
import java.io.File;
import java.util.Collection;
import java.util.function.Consumer;
import text.Chapter;

public class RepeatedPhrasesApp {
    
    private final DataManager dataManager;
    private final Consumer<String> msg;
    
    public RepeatedPhrasesApp(Consumer<String> msg){
        this.dataManager = new DataManager();
        this.msg = msg;
    }
    
    /**
     * <p>Ensures that the working directory has the folders specified in
     * {@link Folders Folders}.</p>
     */
    public void ensureFolders(Consumer<String> msg){
        for(Folder f : Folder.values()){
            File name = f.folder();
            if(!name.exists()){
                msg.accept("Creating "+name.getName());
                name.mkdir();
            }
        }
    }
    
    public Collection<HTMLFile> getHtmlChapters(){
        return dataManager.getHtmlChapters();
    }
    
    public Collection<Chapter> getChapters(){
        return dataManager.getChapters();
    }
    
    public Collection<AnchorInfo> getAnchors(Trail trail){
        return dataManager.getAnchors(trail);
    }
    
    public Collection<HTMLFile> getLinkedChapters(int minSize, Trail trail){
        return dataManager.linkChapters(minSize, trail);
    }
	
    /**
     * <p>Does everything: Reads HTML novels from the hard drive, extracts Chapters from them, 
     * generates anchors, links repeated phrases in html chapters, and links html chapters 
     * together.</p>
     * <p>Calls the main methods of HtmlToText, FindRepeatedPhrases, RemoveDependentPhrases,
     * RemoveUniqueIndependents, DetermineAnchors, LinkChapters, and SetTrail. Passes the first
     * command line argument to DetermineAnchors and SetTrail, and passes the second command-line
     * argument to LinkChapters if it is present and if it parses as an int.</p>
     * @param args command-line arguments
     * @param msg
     */
    public void isolateChaptersAndLink(Trail trail, int limit, Consumer<String> msg) {
        ensureFolders(msg);
        dataManager.setTrail(limit, trail);
    }
    
    public void linksAndTrail(int limit, Trail trail){
        dataManager.setTrail(limit, trail);
    }
    
    public void setTrail(Trail trail, Consumer<String> msg) {
        int limit = 1 + IO.MAX_PHRASE_SIZE; //XXX placeholder
        dataManager.setTrail(limit, trail);
    }
    
    public Consumer<String> getMsg(){
        return this.msg;
    }
}
