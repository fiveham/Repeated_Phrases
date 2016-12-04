package text;

public class Phrase {
    
    private final String text;
    private final int lastSpace;
    
    Phrase(String text, int lastSpace){
        this.text = text;
        this.lastSpace = lastSpace;
    }
    
    public int getLastSpace(){
        return lastSpace;
    }
    
    public String getText(){
        return text;
    }
    
    String reduced = null;
    
    public String reduced(){
        return text.substring(0, lastSpace);
    }
}
