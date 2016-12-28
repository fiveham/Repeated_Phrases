package html;

import java.util.List;

public class HtmlFile {

    protected static final int BEFORE_BEGINNING = -1;
    
    /**
     * <p>The underlying list.</p>
     */
    protected List<HtmlEntity> content;
    
    protected HtmlFile(List<HtmlEntity> content){
        this.content = content;
    }
}
