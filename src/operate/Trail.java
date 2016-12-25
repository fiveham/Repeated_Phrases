package operate;

import common.IO;
import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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
            TrailElement prev = list.get((i - 1) % list.size());
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
        
        TrailElement(Chapter ch){
            this.chapter = ch;
        }
        
        void setPrev(TrailElement prev){
            this.prev = prev;
        }
        
        void setNext(TrailElement next){
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
    public static Trail fromFile(File trailFile, Map<String, Chapter> chapterNames){
        
        Stream<String[]> data = IO.fileContentStream(
                trailFile, 
                Scanner::nextLine, 
                IO::scannerHasNonEmptyNextLine)
                .map((line) -> line.split("\t", COLUMN_COUNT));
        return new Trail(data, chapterNames);
    }
    
    private Trail(Stream<String[]> data, Map<String, Chapter> chapterNames){
        Map<Chapter, TrailElement> innerMap = chapterNames.values().stream()
                .collect(Collectors.toMap((c) -> c, TrailElement::new));
        
        this.list = data.map((d) -> asChapters(d, chapterNames))
                .peek((cs) -> {
                    TrailElement te = innerMap.get(cs[HERE_INDEX]);
                    te.setPrev(innerMap.get(cs[PREV_INDEX]));
                    te.setNext(innerMap.get(cs[NEXT_INDEX]));
                })
                .map(innerMap::get)
                .collect(Collectors.toList());
        this.map = IntStream.range(0, list.size())
                .mapToObj(Integer::valueOf)
                .collect(Collectors.toMap((i) -> list.get(i).chapter, (i) -> i));
    }
    
    private static final int 
            PREV_INDEX = 0, 
            HERE_INDEX = 1, 
            NEXT_INDEX = 2;
    
    private static Chapter[] asChapters(String[] s, Map<String, Chapter> chapterNames){
        Chapter[] result = new Chapter[s.length];
        for(int i = 0; i < result.length; i++){
            result[i] = chapterNames.get(s[i]);
        }
        return result;
    }
}
