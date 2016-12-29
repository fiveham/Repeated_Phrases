package operate;

import common.BookData;
import common.IO;
import html.AnchorInfo;
import html.HtmlBook;
import html.HtmlChapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import text.Chapter;
import text.Location;
import text.Phrase;
import text.Quote;

public class RepeatedPhrasesApp {
    
    private final Consumer<String> msg;
    
    private final Manager<Collection<HtmlChapter>> htmlChaptersManager;
    private final Manager<Collection<Chapter>>     chaptersManager;
    private final Manager<Collection<AnchorInfo>>  anchorsManager;
    private final Manager<Collection<HtmlChapter>> linkedChaptersManager;
    
    public RepeatedPhrasesApp(Consumer<String> msg){
        this.msg = msg;
        
        this.htmlChaptersManager = new Manager<>(
                (limit, trail) -> generateHtmlChapters(), 
                Cache::equals);
        this.chaptersManager = new Manager<>(
                (limit, trail) -> generateChapters(), 
                Cache::equals);
        this.anchorsManager = new Manager<>(
                (limit, trail) -> generateAnchorData(trail), 
                Cache::equals);
        this.linkedChaptersManager = new Manager<>(
                (limit, trail) -> generateLinkedChapters(limit, trail), 
                Cache::equals);
    }
    
    //Getters
    
    public Consumer<String> getMsg(){
        return this.msg;
    }
    
    public Collection<HtmlChapter> getHtmlChapters(){
        return htmlChaptersManager.get(null, null);
    }
    
    public Collection<Chapter> getChapters(){
        return chaptersManager.get(null, null);
    }
    
    public Collection<AnchorInfo> getAnchors(Trail trail){
        return anchorsManager.get(null, trail);
    }
    
    public Collection<HtmlChapter> getLinkedChapters(Integer minSize, Trail trail){
        return linkedChaptersManager.get(minSize, trail);
    }
    
    //Button operation methods
    
    /**
     * <p>Ensures that the working directory has the folders specified in
     * {@link Folders Folders}.</p>
     */
    public void ensureFolders(Consumer<String> msg){
        for(Folder f : Folder.values()){
            File name = f.getFolder();
            if(!name.exists()){
                msg.accept("Creating " + name.getName());
                name.mkdir();
            }
        }
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
        setTrail(limit, trail);
    }
    
    public void linksAndTrail(int limit, Trail trail){
        setTrail(limit, trail);
    }
    
    public void setTrail(Trail trail, Consumer<String> msg) {
        //XXX maybe use limit = null, if the linkedChaptersManager's cache rule allows for "null = 'whatever, dude'"
        int limit = 1 + IO.MAX_PHRASE_SIZE; //XXX placeholder
        setTrail(limit, trail);
    }
    
    //htmlChapters generation methods
    
    private Collection<HtmlChapter> generateHtmlChapters(){
        Stream<File> fs = Stream.of(Folder.HTML_BOOKS.getFolder().listFiles(BookData::isBook));
        Stream<HtmlBook> novels = fs
                .map(RepeatedPhrasesApp::newHTMLFile);
        return novels
                .map(HtmlBook::cleanAndSplit)
                .reduce((c1, c2) -> {
                    c1.addAll(c2);
                    return c1;
                })
                .get();
    }

    private static HtmlBook newHTMLFile(File f){
        try{
            return new HtmlBook(f);
        } catch(FileNotFoundException e){
            throw new RuntimeException(f + " not found", e);
        }
    }
    
    //chaptersManager generation methods
    
    private Collection<Chapter> generateChapters(){
        return getHtmlChapters().stream()
                .map(Chapter::new)
                .collect(Collectors.toList());
    }
    
    //anchorsManager generation methods

    private final Map<String, Phrase> phraseTracker = Collections.synchronizedMap(new HashMap<>());

    /**
     * <p>The default value of the minimum number of words a phrase needs to have for its related 
     * anchors to be added to output files. Used when such a value is not specified as a command-
     * line argument.</p>
     */
    private static final int PHRASE_SIZE_THRESHOLD_FOR_ANCHOR = 3;
    
    private Collection<AnchorInfo> generateAnchorData(Trail trail){
        Collection<Chapter> chapters = getChapters();
        
        Map<Chapter, Collection<Quote>> allQuotes = chapters.stream()
                .collect(Collectors.toMap(
                        Function.identity(), 
                        (c) -> c.getAllQuotes(
                                PHRASE_SIZE_THRESHOLD_FOR_ANCHOR, 
                                IO.MAX_PHRASE_SIZE, 
                                phraseTracker)));
        
        Set<String> repeatedPhrases = repeatedPhrases(allQuotes);
        Map<Chapter, Collection<Quote>> repeatedQuotes = chapters.stream()
                .collect(Collectors.toMap(
                        Function.identity(), 
                        (c) -> allQuotes.get(c).stream()
                                .filter(repeatedPhrases::contains)
                                .collect(Collectors.toList())));
        
        //remove dependent phrases
        repeatedQuotes.entrySet().parallelStream()
                .forEach((e) -> e.getKey().setRepeatedQuotes(e.getValue()));
        Map<Chapter, Collection<Quote>> independent = chapters.stream()
                .collect(Collectors.toMap(
                        Function.identity(), 
                        (c) -> repeatedQuotes.get(c).stream()
                                .filter(Quote::isIndependent)
                                .collect(Collectors.toList())));
        
        //remove unique independent phrases
        Set<String> dupIndep = repeatedPhrases(independent);
        Map<Chapter, List<Quote>> dupIndepQuotes = chapters.stream()
                .collect(Collectors.toMap(
                        Function.identity(), 
                        (c) -> independent.get(c).stream()
                                .filter(dupIndep::contains)
                                .collect(Collectors.toList())));
        
        //create anchor data
        return generateAnchorInfo(dupIndepQuotes, trail);
    }

    private static Set<String> repeatedPhrases(Map<Chapter, Collection<Quote>> map){
        Map<String, Boolean> data = Collections.synchronizedMap(new HashMap<>());
        
        map.keySet().parallelStream().forEach(
                (c) -> map.get(c).parallelStream()
                        .map(Quote::getText)
                        .forEach((t) -> data.put(t, data.containsKey(t))));
        
        return data.keySet().stream()
                .filter(data::get)
                .collect(Collectors.toSet());
    }

    private static List<AnchorInfo> generateAnchorInfo(
            Map<Chapter, List<Quote>> duplicateIndependentQuotes, 
            Trail trail){
        
        Map<Phrase, List<Location>> locationsOfPhrases = phrasesToLocations(
                duplicateIndependentQuotes, 
                trail);
        return duplicateIndependentQuotes.values().stream()
                .map(
                        (quotes) -> quotes.stream()
                                .sorted()
                                .map(
                                        (quote) -> {
                                            Phrase phrase = quote.getPhrase();
                                            Location location = quote.getLocation();
                                            
                                            List<Location> locations = locationsOfPhrases
                                                    .get(phrase);
                                            
                                            Location linkTo = location.after(locations);
                                            
                                            return new AnchorInfo(
                                                    phrase.getText(), 
                                                    location, 
                                                    linkTo);
                                        })
                                .collect(Collectors.toList()))
                .reduce(
                        (a, b) -> {
                            a.addAll(b);
                            return a;
                        })
                .get();
    }

    private static Map<Phrase, List<Location>> phrasesToLocations(
            Map<Chapter, 
            List<Quote>> diQuotes, 
            Trail trail){
        
        Map<Phrase, List<Location>> result = Collections.synchronizedMap(new HashMap<>());
        diQuotes.keySet().parallelStream().forEach(
                (c) -> diQuotes.get(c).parallelStream().forEach(
                        (q) -> result.compute(
                                q.getPhrase(), 
                                (p, locs) -> {
                                    List<Location> locations = locs == null 
                                            ? new ArrayList<>()
                                            : result.get(p);
                                    locations.add(q.getLocation());
                                    return locations;
                                })));
        
        result.keySet().parallelStream()
                .map(result::get)
                .forEach((l) -> l.sort(trail));
        
        return result;
    }
    
    //linkedChaptersManager generation methods
    
    private Collection<HtmlChapter> generateLinkedChapters(int minSize, Trail trail){
        Map<Chapter, List<AnchorInfo>> toAnchors = getAnchors(trail).stream()
                .collect(Collectors.groupingBy((ai) -> ai.position().getChapter()));
        
        //add phrase-anchors
        List<HtmlChapter> linked = toAnchors.keySet().stream()
                .map((c) -> c.getSource().clone().link(toAnchors.get(c)))
                .collect(Collectors.toList());
        
        //add links to chapter headers and footers
        trail.trailElements().forEach(
                (te) -> te.chapter().getSource().setTrail(
                        te.prev().chapter().getName(), 
                        te.next().chapter().getName()));
        
        return linked;
    }
    
    //methods for saving finished linked chapters
    
    private void setTrail(Integer limit, Trail trail){
        getLinkedChapters(limit, trail).parallelStream().forEach(Folder.READABLE::save);
    }
}
