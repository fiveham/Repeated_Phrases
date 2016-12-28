package html;

import java.util.ArrayList;
import java.util.List;

public class HtmlFile {
    
    /**
     * <p>The underlying list.</p>
     */
    protected List<HtmlEntity> content;
    
    protected HtmlFile(List<HtmlEntity> content){
        this.content = content;
    }
}
