package html;

import java.util.function.Predicate;
import java.util.List;
import java.util.ArrayList;

/**
 * <p>Represents an HTML tag.</p>
 */
public class Tag extends HTMLEntity {
	
    /**
     * <p>The literal text of the "p" type.</p>
     */
	public static final String P = "p";
	
    /**
     * <p>The literal text of the "div" type.</p>
     */
	public static final String DIV = "div";
	
    /**
     * <p>The literal text of the "table" type.</p>
     */
	public static final String TABLE = "table";
	
    /**
     * <p>The literal text of the "blockquote" type.</p>
     */
	public static final String BLOCKQUOTE = "blockquote";
	
    /**
     * <p>The literal text of the "img" type.</p>
     */
	public static final String IMG = "img";
	
    /**
     * <p>The literal text of the "a" type.</p>
     */
	public static final String A = "a";
	
    /**
     * <p>The literal text of the "sup" type.</p>
     */
	public static final String SUP = "sup";
	
    /**
     * Evaluates to true if the HTMLEntity tested is an opening Tag.
     */
	public static final Predicate<HTMLEntity> IS_OPEN = 
			(h) -> Tag.class.isInstance(h) && ((Tag)h).isOpening();
		
    /**
     * Evaluates to true if the HTMLEntity tested is a closing Tag.
     */
	public static final Predicate<HTMLEntity> IS_CLOSE = 
			(h) -> Tag.class.isInstance(h) && ((Tag)h).isClosing();
	
    /**
     * <p>Evaluates to true if the HTMLEntity tested is a Tag and has type "p".</p>
     */
	public static final Predicate<HTMLEntity> IS_P = 
			(h) -> Tag.class.isInstance(h) 
			&& P.equals(((Tag)h).getType());
			
    /**
     * <p>Evaluates to true if the HTMLEntity tested is a Tag, has type "p", and is opening.</p>
     */
	public static final Predicate<HTMLEntity> IS_P_OPEN = 
			(h) -> IS_P.test(h)  
			&& ((Tag)h).isOpening();
			
    /**
     * <p>Evaluates to true if the HTMLEntity tested is a Tag, has type "p", and is closing.</p>
     */
	public static final Predicate<HTMLEntity> IS_P_CLOSE = 
			(h) -> IS_P.test(h)  
			&& ((Tag)h).isClosing();
	
    /**
     * <p>Evaluates to true if the HTMLEntity tested is a Tag and has type "div".</p>
     */
	public static final Predicate<HTMLEntity> IS_DIV = 
			(h) -> Tag.class.isInstance(h) 
			&& DIV.equals(((Tag)h).getType());
	
    /**
     * <p>Evaluates to true if the HTMLEntity tested is a Tag, has type "div", and is opening.</p>
     */
	public static final Predicate<HTMLEntity> IS_DIV_OPEN = 
			(h) -> IS_DIV.test(h)  
			&& ((Tag)h).isOpening();
	
    /**
     * <p>Evaluates to true if the HTMLEntity tested is a Tag, has type "div", and is closing.</p>
     */
	public static final Predicate<HTMLEntity> IS_DIV_CLOSE = 
			(h) -> IS_DIV.test(h)  
			&& ((Tag)h).isClosing();
	
    /**
     * <p>Evaluates to true if the HTMLEntity tested is a Tag and has type "blockquote".</p>
     */
	public static final Predicate<HTMLEntity> IS_BLOCKQUOTE = 
			(h) -> Tag.class.isInstance(h) 
			&& BLOCKQUOTE.equals(((Tag)h).getType());
	
    /**
     * <p>Evaluates to true if the HTMLEntity tested is a Tag and has type "img".</p>
     */
	public static final Predicate<HTMLEntity> IS_IMG = 
			(h) -> Tag.class.isInstance(h) 
			&& IMG.equals(((Tag)h).getType());
	
    /**
     * <p>Evaluates to true if the HTMLEntity tested is a Tag, and has type "table".</p>
     */
	public static final Predicate<HTMLEntity> IS_TABLE = 
			(h) -> Tag.class.isInstance(h) 
			&& TABLE.equals(((Tag)h).getType());
	
    /**
     * <p>Evaluates to true if the HTMLEntity tested is a Tag, has type "table", and is opening.</p>
     */
	public static final Predicate<HTMLEntity> IS_TABLE_OPEN = 
			(h) -> IS_TABLE.test(h) && ((Tag)h).isOpening();
	
    /**
     * <p>Evaluates to true if the HTMLEntity tested is a Tag, has type "table" and is closing.</p>
     */
	public static final Predicate<HTMLEntity> IS_TABLE_CLOSE = 
			(h) -> IS_TABLE.test(h) && ((Tag)h).isClosing();
	
    /**
     * <p>Evaluates to true if the HTMLEntity tested is a Tag and has type "a".</p>
     */
	public static final Predicate<HTMLEntity> IS_A = 
			(h) -> Tag.class.isInstance(h) && A.equals(((Tag)h).getType());
	
    /**
     * <p>Evaluates to true if the HTMLEntity tested is a Tag, has type "a", and is opening.</p>
     */
	public static final Predicate<HTMLEntity> IS_A_OPEN = 
			(h) -> IS_A.test(h) && ((Tag)h).isOpening();
	
    /**
     * <p>Evaluates to true if the HTMLEntity tested is a Tag, has type "a", and is closing.</p>
     */
	public static final Predicate<HTMLEntity> IS_A_CLOSE = 
			(h) -> IS_A.test(h) && ((Tag)h).isClosing();
	
    /**
     * <p>Evaluates to true if the HTMLEntity tested is a Tag and has type "sup".</p>
     */
	public static final Predicate<HTMLEntity> IS_SUP = 
			(h) -> Tag.class.isInstance(h) && SUP.equals(((Tag)h).getType());
	
	public static final List<String> HEADERS;
	static{
		HEADERS = new ArrayList<String>();
		HEADERS.add("h1");
		HEADERS.add("h2");
		HEADERS.add("h3");
		HEADERS.add("h4");
		HEADERS.add("h5");
		HEADERS.add("h6");
	}
	
	//TODO use methods + :: for these Predicates
	public static final Predicate<HTMLEntity> IS_HEADER = 
			(h) -> Tag.class.isInstance(h) && HEADERS.contains(((Tag)h).getType());
	
	public static final Predicate<HTMLEntity> IS_HEADER_OPEN = 
			(h) -> IS_HEADER.test(h) && IS_OPEN.test(h);
	
    /**
     * <p>The literal text of this tag inside its opening and closing angle brackets.</p>
     */
	public final String content;
	
    /**
     * <p>Constructs a Tag with the specified {@code content}.</p>
     * @param content the literal text of this tag inside the opening and closing angle brackets.
     */
	public Tag(String content) {
		this.content = content;
	}
	
    /**
     * <p>Returns the value of the specified attribute if this tag has that attribute, null 
     * otherwise.</p>
     * @param attribute the attribute whose value is to be found and returned
     * @return the value of the specified attribute if this tag has that attribute, null otherwise.
     */
	public String valueOfAttribute(String attribute){
		String seek = attribute+"=\"";
		int index = content.indexOf(seek);
		if( index >= 0 ){
			int end = content.indexOf("\"", index+seek.length());
			return content.substring(index+seek.length(), end);
		} else{
			return null;
		}
	}
	
    /**
     * <p>Stores the type of this Tag after it has been determined by
     * {@link #getType() getType()}.</p>
     */
	private String type = null;
	
    /**
     * <p>Returns the type of this Tag, which is the text at the beginning of the tag.</p>
     * @return the type of this Tag, which is the text at the beginning of the text of the tag.
     */
	public String getType(){
		if(type!=null){
			return type;
		} else{
			String meaningfulContent = isClosing() 
					? this.content.substring(1,this.content.length()) 
					: this.content;
			
			for(int i=0; i<meaningfulContent.length(); i++){
				if( !isTagNameChar(meaningfulContent.charAt(i)) ){
					return type = meaningfulContent.substring(0,i);
				}
			}
			return type = meaningfulContent;
		}
	}
	
    /**
     * <p>Returns true if this {@code Tag}'s {@link #getType() type} is equal to the specified
     * {@code type}, false otherwise.</p>
     * @param type the type of Tag to compare against this Tag's type
     * @return true if this {@code Tag}'s {@link #getType() type} is equal to the specified
     * {@code type}, false otherwise
     */
	public boolean isType(String type){
		return type.equals(getType());
	}
	
    /**
     * <p>Returns true if {@code c} is a valid char for the type of a Tag, false otherwise. A valid
     * char for a Tag type is a lowercase letter.</p>
     * @param c a char to be tested for whether it's valid in the type of a Tag
     * @return true if {@code c} is a valid char for the type of a Tag, false otherwise.
     */
	public boolean isTagNameChar(char c){
		return 'a'<=c && c<='z';
	}
	
	@Override
    /**
     * <p>Returns a String representation of this Tag, the plaintext equivalent of this Tag, an
     * opening angle bracket followed by {@code content} followed by a closing angle bracket.</p>
     * @return a String representation of this Tag, the plaintext equivalent of this Tag, an opening
     * angle bracket followed by {@code content} followed by a closing angle bracket.
     */
	public String toString(){
		return START+content+END;
	}
	
    /**
     * <p>The beginning of the literal plaintext version of a Tag, an opening angle bracket, the
     * less-than symbol.</p>
     */
	public static final char START = '<';
	
    /**
     * <p>The end of the literal plaintext version of a Tag, a closing angle bracket, the
     * greater-than symbol.</p>
     */
	public static final char END = '>';
	
    /**
     * <p>The slash used to begin a closing Tag or to end a clopen Tag.</p>
     */
	public static final char CLOSE = '/';
	
    /**
     * <p>Returns true if this Tag is a closing tag, false otherwise.</p>
     * @return true if this Tag is a closing tag, false otherwise.
     */
	public boolean isClosing(){
		return content.charAt(0) == CLOSE;
	}
	
    /**
     * <p>Returns true if this Tag is an opening tag, false otherwise.</p>
     * @return true if this Tag is an opening tag, false otherwise.
     */
	public boolean isOpening(){
		return content.charAt(0) != CLOSE;
	}
	
    /**
     * <p>ReturnsC A tag is clopen if it does not start with a {@code CLOSE} and ends with a
     * {@code CLOSE}.</p>
     * @return true if this Tag is an opening tag, false otherwise.
     */
	public boolean isClopen(){
		return content.charAt(content.length()-1) == CLOSE;
	}
	
	@Override
    /**
     * <p>Returns true if {@code o} is a Tag and has {@code content} equal to 
     * {@code this.content}.</p>
     * @param o an object to be tested for equality with this Tag
     */
	public boolean equals(Object o){
		return o instanceof Tag && content.equals(((Tag)o).content);
	}
	
	@Override
    /**
     * <p>Returns a .txt-format equivalent of this Tag, an empty String.</p>
     */
	public String txtString(){
		return "";
	}
	
	@Override
	public boolean isVisible(){
		return false;
	}
}
