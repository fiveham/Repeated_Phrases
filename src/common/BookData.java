package common;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum BookData{
    AGOT(
            true, 
            "AGOT.html", 
            "We should start", 
            "music of dragons."), 
    ACOK(
            true, 
            "ACOK.html", 
            "The comet" + IO.RIGHT_SINGLE_QUOTE + "s tail", 
            "not dead either."), 
    ASOS(
            true, 
            "ASOS.html", 
            "The day was", 
            "up and up."), 
    AFFC(
            true, 
            "AFFC.html", 
            "Dragons," + IO.RIGHT_SINGLE_QUOTE + " said Mollander", 
            "the pig boy." + IO.RIGHT_DOUBLE_QUOTE), 
    ADWD(
            true, 
            "ADWD.html", 
            "The night was", 
            "hands, the daggers."), 
    DE_0(
            false, 
            "DE_0.html", 
            "The spring rains", 
            "shows," + IO.RIGHT_DOUBLE_QUOTE + " he said."), 
    DE_1(
            false, 
            "DE_1.html", 
            "In an iron", 
            "hear it" + IO.RIGHT_SINGLE_QUOTE + "s tall." + IO.RIGHT_DOUBLE_QUOTE), 
    DE_2(
            false, 
            "DE_2.html", 
            "A light summer", 
            "of comic dwarfs?"), 
    PQ(
            false, 
            "PQ.html", 
            "The Dance of", 
            "Ser Gwayne Hightower."), 
    RP(
            false, 
            "RP.html", 
            "He was the grandson", 
            "danced and died.");
    
    private final boolean isNovel;
    private final String filename;
    private final String firstWords;
    private final String lastWords;
    
    private BookData(boolean isNovel, String filename, String firstWords, String lastWords){
        this.isNovel = isNovel;
        this.filename = filename;
        this.firstWords = firstWords;
        this.lastWords = lastWords;
    }
    
    public boolean isNovel(){
        return isNovel;
    }
    
    public boolean isNovella(){
        return !isNovel;
    }
    
    public String filename(){
        return filename;
    }
    
    public String firstWords(){
        return firstWords;
    }
    
    public String lastWords(){
        return lastWords;
    }
    
    public static Map<String, String> words(Predicate<BookData> test, Function<BookData,String> words){
        return Stream.of(BookData.values())
                .filter(test)
                .collect(Collectors.toMap(BookData::filename, words));
    }
}
