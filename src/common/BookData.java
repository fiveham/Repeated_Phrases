package common;

import html.HtmlBook;
import html.HtmlChapter;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum BookData{
    AGOT(
            true, 
            "AGOT.html", 
            "We should start", 
            "music of dragons.", 
            HtmlBook::chapterizeNovel), 
    ACOK(
            true, 
            "ACOK.html", 
            "The comet" + IO.RIGHT_SINGLE_QUOTE + "s tail", 
            "not dead either.", 
            HtmlBook::chapterizeNovel), 
    ASOS(
            true, 
            "ASOS.html", 
            "The day was", 
            "up and up.", 
            HtmlBook::chapterizeNovel), 
    AFFC(
            true, 
            "AFFC.html", 
            "Dragons," + IO.RIGHT_SINGLE_QUOTE + " said Mollander", 
            "the pig boy." + IO.RIGHT_DOUBLE_QUOTE, 
            HtmlBook::chapterizeNovel), 
    ADWD(
            true, 
            "ADWD.html", 
            "The night was", 
            "hands, the daggers.", 
            HtmlBook::chapterizeNovel), 
    DE_0(
            false, 
            "DE_0.html", 
            "The spring rains", 
            "shows," + IO.RIGHT_DOUBLE_QUOTE + " he said.", 
            HtmlBook::chapterizeNovella), 
    DE_1(
            false, 
            "DE_1.html", 
            "In an iron", 
            "hear it" + IO.RIGHT_SINGLE_QUOTE + "s tall." + IO.RIGHT_DOUBLE_QUOTE, 
            HtmlBook::chapterizeNovella), 
    DE_2(
            false, 
            "DE_2.html", 
            "A light summer", 
            "of comic dwarfs?", 
            HtmlBook::chapterizeNovella), 
    PQ(
            false, 
            "PQ.html", 
            "The Dance of", 
            "Ser Gwayne Hightower.", 
            HtmlBook::chapterizePQ), 
    RP(
            false, 
            "RP.html", 
            "He was the grandson", 
            "danced and died.", 
            HtmlBook::chapterizeNovella);
    
    private final boolean isNovel;
    private final String filename;
    private final String firstWords;
    private final String lastWords;
    private final Function<HtmlBook, Collection<HtmlChapter>> chapterizer;
    
    private BookData(
            boolean isNovel, 
            String filename, 
            String firstWords, 
            String lastWords, 
            Function<HtmlBook, Collection<HtmlChapter>> chapterizer){
        
        this.isNovel = isNovel;
        this.filename = filename;
        this.firstWords = firstWords;
        this.lastWords = lastWords;
        this.chapterizer = chapterizer;
    }
    
    public boolean isNovel(){
        return isNovel;
    }
    
    public boolean isNovella(){
        return !isNovel;
    }
    
    public Function<HtmlBook, Collection<HtmlChapter>> getChapterizer(){
        return chapterizer;
    }
    
    public String getFilename(){
        return filename;
    }
    
    public String getFirstWords(){
        return firstWords;
    }
    
    public String getLastWords(){
        return lastWords;
    }
    
    public static Map<String, String> words(Predicate<BookData> test, Function<BookData,String> words){
        return Stream.of(BookData.values())
                .filter(test)
                .collect(Collectors.toMap(BookData::getFilename, words));
    }
    
    public static boolean isBook(File dir, String name){
        return NAMES.contains(name);
    }
    
    private static final Set<String> NAMES = Stream.of(values())
            .map((bd) -> bd.toString() + IO.HTML_EXT)
            .collect(Collectors.toSet());
}
