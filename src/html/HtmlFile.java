package html;

import java.util.List;
import java.util.function.Predicate;

public class HtmlFile {
    
    protected static final int BEFORE_BEGINNING = -1;
    
    /**
     * <p>The underlying list.</p>
     */
    protected List<HtmlEntity> content;
    
    protected int modCount = 0;
    
    protected HtmlFile(List<HtmlEntity> content){
        this.content = content;
    }
    
    /**
     * <p>Evaluates to true if the specified HTMLEntity {@code h} is a character-type HTMLEntity: a
     * {@link CharLiteral Ch} or a {@link CharCode Code}.</p>
     */
    protected static boolean isCharacter(HtmlEntity h){
        return CharLiteral.class.isInstance(h) || CharCode.class.isInstance(h);
    }
    
    /**
     * <p>Returns the position in the underlying list of the element nearest to but not at
     * {@code position} in the direction (before or after) specified by {@code direction} for which
     * {@code condition} evaluates to true.</p>
     * @param startPosition the pre-starting position for this operation. One less than the starting
     * point if {@code direction} is {@code Direction.NEXT}, or one more if it's
     * {@code Direction.PREV}.
     * @param condition a Predicate whose evaluation to true causes this method to return its
     * current position in the underlying list
     * @param direction this method's direction of traversal of the underlying list
     * @return the position in the underlying list of the element nearest to but not at
     * {@code position} in the direction (before or after) specified by {@code direction} for which
     * {@code condition} evaluates to true
     */
    protected int adjacentElement(
            int startPosition, 
            Predicate<HtmlEntity> condition, 
            Direction direction){
        
        for(int i = direction.apply(startPosition); 
                direction.crawlTest(i, content);
                i = direction.apply(i)){
            if(condition.test(content.get(i))){
                return i;
            }
        }
        return BEFORE_BEGINNING;
    }
}
