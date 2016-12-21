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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import text.Chapter;
import text.Location;
import text.PhraseBox;
import text.Quote;

class DataManager {
    
    private Collection<HTMLFile>   htmlChapters   = null;
    private Collection<Chapter>    chapters       = null;
    private Collection<AnchorInfo> anchorData     = null;
    private Collection<HTMLFile>   linkedChapters = null;
    
    DataManager(){
        
    }
    
    private <T> Collection<T> softGet(Collection<T> ref, Runnable genRef){
        if(ref == null){
            genRef.run();
        }
        return ref;
    }
    
    //getters
    
    public Collection<HTMLFile> getHtmlChapters(){
        return softGet(htmlChapters, this::generateHtmlChapters);
    }
    
    public Collection<Chapter> getChapters(){
        return softGet(chapters, this::generateChapters);
    }
    
    public Collection<AnchorInfo> getAnchors(List<TrailElement> trail){
        return softGet(anchorData, () -> generateAnchorData(trail));
    }
    
    public Collection<HTMLFile> linkChapters(){
        return softGet(linkedChapters, this::generateLinkedChapters);
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
    
    private void generateAnchorData(List<TrailElement> trail){
        Collection<Chapter> chapters = getChapters();
        
        Map<Chapter, Collection<Quote>> allQuotes = Collections.synchronizedMap(new HashMap<>());
        chapters.parallelStream().forEach(
                (c) -> allQuotes.put(c, c.getAllQuotes(IO.PHRASE_SIZE_THRESHOLD_FOR_ANCHOR, 218))); //MAGIC
        
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
            List<TrailElement> trail){
        
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
    
    private static PhraseBox phrasesToLocations(Map<Chapter, List<Quote>> diQuotes, List<TrailElement> trail){
        PhraseBox result = new PhraseBox();
        diQuotes.keySet().parallelStream().forEach(
                (c) -> diQuotes.get(c).parallelStream().forEach(
                        (q) -> result.add(q.text(), q.location())));
        
        //sort the anchors according to the trail file
        /*Comparator<Location> phraseSorter = trailFile.exists() && trailFile.canRead() 
                ? new AdHocComparator(trailFile) 
                : Location::compareTo;*/
        for(String phrase : result.phrases()){
            result.get(phrase).sort(new AdHocComparator(trail));
        }
        
        return result;
    }
    
    private static class AdHocComparator implements Comparator<Location>{
        private final Map<String, Integer> chapterIndices;
        
        private AdHocComparator(List<TrailElement> elems){
            chapterIndices = IntStream.range(0, elems.size())
                    .mapToObj(Integer::valueOf)
                    .collect(Collectors.toMap(
                            (i) -> IO.stripFolderExtension(elems.get(i).focus()), 
                            (i) -> i));
        }
        
        @Override
        public int compare(Location loc1, Location loc2){
            String chapter1 = IO.stripFolderExtension(loc1.getFilename());
            String chapter2 = IO.stripFolderExtension(loc2.getFilename());
            
            int indexInChapter1 = chapterIndices.get(chapter1);
            int indexInChapter2 = chapterIndices.get(chapter2);
            
            return indexInChapter1 != indexInChapter2 
                    ? indexInChapter1 - indexInChapter2 
                    : loc1.getIndex() - loc2.getIndex();
        }
    }
    
    /**
     * <p>Number of columns to anticipate in input file: {@value}</p> <p>The fourth column is unused
     * at this time, but expecting it allows the third column to be cleanly isolated if a fourth
     * column exists.</p>
     */
    private static final int COLUMN_COUNT = 4;
    
    /**
     * <p>Returns a list of {@code TrailElement}s describing each chapter's predecessor and
     * successor.</p>
     * @param trailFilename the name of the trail-file from which trail data is extracted
     * @return a list of {@code TrailElement}s describing each chapter's predecessor and successor
     */
    static List<TrailElement> getTrailElements(File trailFile){
        return IO.fileContentsAsList(
                trailFile, 
                Scanner::nextLine, 
                IO::scannerHasNonEmptyNextLine)
                .stream()
                .map((line) -> line.split("\t", COLUMN_COUNT))
                .map(TrailElement::new)
                .collect(Collectors.toList());
    }
    
    //TODO can uses of List<TrailElement> be replaced with uses of a LinkedList?
    //...using the nodes of the LinkedList to implicitly contain info about prev and next
    /**
     * <p>Represents an element of a chapter trail, a sequence of backward and forward links between
     * chapters.</p>
     */
    static class TrailElement implements Comparable<TrailElement>{
        
        /**
         * <p>The chapter to be linked as the preceding chapter in the trail.</p>
         */
        private final String prev;
        
        /**
         * <p>The chapter for which links to the specified preceding and succeeding chapters are to
         * be installed.</p>
         */
        private final String focus;
        
        /**
         * <p>The chapter to be linked as the succeeding chapter in the trail.</p>
         */
        private final String next;
        
        TrailElement(String[] strings){
            this(strings[INDEX_PREV], strings[INDEX_FOCUS], strings[INDEX_NEXT]);
        }
        
        private static final int INDEX_PREV = 0;
        private static final int INDEX_FOCUS = 1;
        private static final int INDEX_NEXT = 2;
        
        /**
         * <p>Constructs a TrailElement indicating that the chapter named by {@code focus} has the
         * chapter named by {@code next} as its successor.</p>
         * @param prev the chapter before {@code focus} in sequence
         * @param focus the chapter in which links to {@code prev} and {@code next} are to be
         * installed
         * @param next the chapter after {@code focus} in sequence
         */
        TrailElement(String prev, String focus, String next){
            this.prev = prev;
            this.focus = focus;
            this.next = next;
        }
        
        /**
         * <p>Compares two TrailElements, first by their {@code focus}, then by their {@code prev},
         * and last by their {@code next}.</p>
         * @return an int whose sign reflects the natural ordering between this TrailElement and
         * {@code t}
         */
        @Override
        public int compareTo(TrailElement t){
            int comp = focus.compareTo(t.focus);
            if(comp != 0){
                return comp;
            } else if(0 != (comp = prev.compareTo(t.prev))){
                return comp;
            } else{
                return next.compareTo(t.next);
            }
        }
        
        /**
         * <p>Returns {@link #prev prev}.</p>
         * @return {@link #prev prev}
         */
        public String prev(){
            return prev;
        }
        
        /**
         * <p>Returns {@link #focus focus}.</p>
         * @return {@link #focus focus}
         */
        public String focus(){
            return focus;
        }
        
        /**
         * <p>Returns {@link #next next}.</p>
         * @return {@link #next next}
         */
        public String next(){
            return next;
        }
    }
    
    private void generateLinkedChapters(){
        
    }
}
