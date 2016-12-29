package operate;

import common.Files;
import html.AnchorInfo;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import text.Chapter;
import text.Location;

/**
 * <p>A list of references to chapters, used to specify the order in which chapters are to be 
 * read.</p>
 * @author fiveham
 *
 */
public class Trail implements Comparator<Location>{
    
    private final List<TrailElement> list;
    private final Map<Chapter, Integer> map;
    
    public Trail(List<Chapter> chapterSequence){
        this.list = chapterSequence.stream()
                .map(TrailElement::new)
                .collect(Collectors.toList());
        this.map = new HashMap<>();
        for(int i = 0; i < list.size(); i++){
            TrailElement prev = list.get(
                    i == 0 
                            ? list.size() - 1 
                            : i - 1);
            TrailElement here = list.get(i);
            TrailElement next = list.get((i + 1) % list.size());
            
            here.setPrev(prev);
            here.setNext(next);
            
            map.put(here.chapter, i);
        }
    }
    
    @Override
    public int compare(Location loc1, Location loc2){
        return Integer.compare(
                map.get(loc1.getChapter()), 
                map.get(loc2.getChapter()));
    }
    
    public int size(){
        return list.size();
    }
    
    public class TrailElement{
        
        private final Chapter chapter;
        private TrailElement prev;
        private TrailElement next;
        
        private TrailElement(Chapter ch){
            this.chapter = ch;
        }
        
        private void setPrev(TrailElement prev){
            this.prev = prev;
        }
        
        private void setNext(TrailElement next){
            this.next = next;
        }
        
        public TrailElement prev(){
            return prev;
        }
        
        public TrailElement next(){
            return next;
        }
        
        public Chapter chapter(){
            return chapter;
        }
    }
    
    public Stream<TrailElement> trailElements(){
        return list.stream();
    }
    
    /**
     * <p>Returns a list of {@code TrailElement}s describing each chapter's predecessor and
     * successor.</p>
     * @param trailFilename the name of the trail-file from which trail data is extracted
     * @return a list of {@code TrailElement}s describing each chapter's predecessor and successor
     */
    public static Trail fromFile(File trailFile, Map<String, Chapter> chapterNames){
        Stream<String[]> data = Files.fileContentStream(
                trailFile, 
                Scanner::nextLine, 
                Files::scannerHasNextAndNextLine)
                .map((line) -> line.split("\t", AnchorInfo.COLUMN_COUNT));
        return new Trail(data, chapterNames);
    }
    
    private Trail(Stream<String[]> anchorsAsText, Map<String, Chapter> chaptersByName){
        Map<Chapter, TrailElement> trailElementsByChapter = chaptersByName.values().stream()
                .collect(Collectors.toMap(
                        Function.identity(), 
                        TrailElement::new));
        
        this.list = anchorsAsText.map((anchorData) -> asChapters(anchorData, chaptersByName))
                .peek((chapters) -> {
                    TrailElement te = trailElementsByChapter.get(chapters[HERE_INDEX]);
                    te.setPrev(trailElementsByChapter.get(chapters[PREV_INDEX]));
                    te.setNext(trailElementsByChapter.get(chapters[NEXT_INDEX]));
                })
                .map(trailElementsByChapter::get)
                .collect(Collectors.toList());
        this.map = IntStream.range(0, list.size())
                .mapToObj(Integer::valueOf)
                .collect(Collectors.toMap(
                        (i) -> list.get(i).chapter, 
                        Function.identity()));
    }
    
    private static final int PREV_INDEX = 0;
    private static final int HERE_INDEX = 1;
    private static final int NEXT_INDEX = 2;
    
    private static Chapter[] asChapters(
            String[] filenamesForAnchor, 
            Map<String, Chapter> chapterNames){
        
        Chapter[] result = new Chapter[filenamesForAnchor.length];
        for(int i = 0; i < result.length; i++){
            result[i] = chapterNames.get(filenamesForAnchor[i]);
        }
        return result;
    }
}
