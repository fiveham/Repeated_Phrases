package operate;

import common.BookData;
import common.IO;
import html.AnchorInfo;
import html.HTMLFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import text.Chapter;
import text.Location;
import text.PhraseBox;
import text.Quote;

//TODO use separate managers for each major runtime step rather than one for all
//That way, encapsulation, hiding whether a member is generated or simply retrieved, is preserved
class DataManager {
    
    private Collection<HTMLFile>   htmlChapters   = null;
    private Collection<Chapter>    chapters       = null;
    private Collection<AnchorInfo> anchorData     = null;
    private Collection<HTMLFile>   linkedChapters = null;
    
    //TODO use an input-comparing cache to check whether to generate linkedChapters etc.
    
    DataManager(){
    }
    
    private <T> Collection<T> softGet(Collection<T> member, Runnable generateMember){
        if(member == null){
            generateMember.run();
        }
        return member;
    }
    
    //getters
    
    public Collection<HTMLFile> getHtmlChapters(){
        return softGet(htmlChapters, this::generateHtmlChapters);
    }
    
    public Collection<Chapter> getChapters(){
        return softGet(chapters, this::generateChapters);
    }
    
    public Collection<AnchorInfo> getAnchors(Trail trail){
        return softGet(anchorData, () -> generateAnchorData(trail));
    }
    
    public Collection<HTMLFile> linkChapters(int minSize, Trail trail){
        return softGet(linkedChapters, () -> generateLinkedChapters(minSize, trail));
    }
    
    public void setTrail(int minSize, Trail trail){
        linkChapters(minSize, trail).parallelStream()
                .forEach(Folder.READABLE::save);
    }
    
    //generators
    
    private void generateHtmlChapters(){
        Stream<File> fs = Stream.of(Folder.HTML_BOOKS.folder().listFiles(BookData::isBook));
        Stream<HTMLFile> novels = fs
                .map(DataManager::newHTMLFile);
        this.htmlChapters = novels
                .map(HTMLFile::cleanAndSplit)
                .reduce((c1, c2) -> {
                    c1.addAll(c2);
                    return c1;
                })
                .get();
    }
    
    private static HTMLFile newHTMLFile(File f){
        try{
            return new HTMLFile(f);
        } catch(FileNotFoundException e){
            throw new RuntimeException(f + " not found", e);
        }
    }
    
    private void generateChapters(){
        this.chapters = getHtmlChapters().stream()
                .map(Chapter::new)
                .collect(Collectors.toList());
    }
    
    private void generateAnchorData(Trail trail){
        Collection<Chapter> chapters = getChapters();
        
        Map<Chapter, Collection<Quote>> allQuotes = Collections.synchronizedMap(new HashMap<>());
        chapters.parallelStream().forEach(
                (c) -> allQuotes.put(
                        c, 
                        c.getAllQuotes(IO.PHRASE_SIZE_THRESHOLD_FOR_ANCHOR, IO.MAX_PHRASE_SIZE)));
        
        Set<String> repeatedPhrases = repeatedPhrases(allQuotes);
        
        Map<Chapter, Collection<Quote>> repeatedQuotes = 
                Collections.synchronizedMap(new HashMap<>());
        chapters.parallelStream().forEach((c) -> {
            List<Quote> rep = allQuotes.get(c).stream()
                    .filter(repeatedPhrases::contains)
                    .collect(Collectors.toList());
            repeatedQuotes.put(c, rep);
        });
        
        //remove dependent phrases
        
        repeatedQuotes.entrySet().parallelStream()
                .forEach((e) -> e.getKey().setRepeatedQuotes(e.getValue()));
        
        Map<Chapter, Collection<Quote>> independent = Collections.synchronizedMap(new HashMap<>());
        chapters.parallelStream().forEach((c) -> {
            Collection<Quote> indep = repeatedQuotes.get(c).parallelStream()
                    .filter(Quote::isIndependent)
                    .collect(Collectors.toSet());
            independent.put(c, indep);
        });
        
        //remove unique independent phrases
        
        Set<String> dupIndep = repeatedPhrases(independent);
        
        Map<Chapter, List<Quote>> diQuotes = Collections.synchronizedMap(new HashMap<>());
        chapters.parallelStream().forEach((c) -> {
            List<Quote> di = independent.get(c).stream()
                    .filter(dupIndep::contains)
                    .collect(Collectors.toList());
            diQuotes.put(c, di);
        });
        
        //create anchor data
        
        this.anchorData = generateAnchorInfo(diQuotes, trail);
    }
    
    private static Set<String> repeatedPhrases(Map<Chapter, ? extends Collection<Quote>> map){
        Map<String, Boolean> data = Collections.synchronizedMap(new HashMap<>());
        
        map.keySet().parallelStream().forEach(
                (c) -> map.get(c).parallelStream()
                        .map(Quote::text)
                        .forEach((t) -> data.put(t, data.containsKey(t))));
        
        return data.keySet().stream()
                .filter(data::get)
                .collect(Collectors.toSet());
    }

    private static List<AnchorInfo> generateAnchorInfo(
            Map<Chapter, List<Quote>> diQuotes, 
            Trail trail){
        
        List<AnchorInfo> result = new ArrayList<>();
        
        PhraseBox phrasebox = phrasesToLocations(diQuotes, trail);
        for(Chapter chapter : diQuotes.keySet()){
            List<Quote> quotes = diQuotes.get(chapter);
            quotes.sort(null);
            
            for(Quote quote : quotes){
                String phrase = quote.text();
                
                //XXX get all anchors for locs at once and add to result in bulk
                List<Location> locs = phrasebox.get(phrase);
                
                Location linkTo = quote.location().after(locs);//locAfter(locs, quote.location());
                
                AnchorInfo ai = new AnchorInfo(phrase, quote.location(), linkTo);
                result.add(ai);
            }
        }
        
        return result;
    }
    
    private static PhraseBox phrasesToLocations(Map<Chapter, List<Quote>> diQuotes, Trail trail){
        PhraseBox result = new PhraseBox();
        diQuotes.keySet().parallelStream().forEach(
                (c) -> diQuotes.get(c).parallelStream().forEach(
                        (q) -> result.add(q.text(), q.location())));
        
        for(String phrase : result.phrases()){
            result.get(phrase).sort(trail);
        }
        
        return result;
    }
    
    private void generateLinkedChapters(int minSize, Trail trail){
        Map<Chapter, List<AnchorInfo>> toAnchors = getAnchors(trail).stream()
                .collect(Collectors.groupingBy((ai) -> ai.position().getChapter()));
        
        //add phrase-anchors
        List<HTMLFile> linked = toAnchors.keySet().stream()
                .map((c) -> c.getSource().clone().link(toAnchors.get(c)))
                .collect(Collectors.toList());
        
        //add links to chapter headers and footers
        trail.trailElements().forEach(
                (te) -> te.chapter().getSource().setTrail(
                        te.prev().chapter().getName(), 
                        te.next().chapter().getName()));
        
        this.linkedChapters = linked;
    }
}
