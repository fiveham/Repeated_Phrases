package html;

import text.PhraseProducer;

/**
 * <p>Represents a literal character from an HTML file.</p> <p>This class was created to allow
 * literal characters to be represented easily in a collection of HTML {@link Tag tags} and
 * {@link CharCode character codes}.</p>
 */
public class CharLiteral extends HTMLEntity {
    
    public static final CharLiteral RIGHT_SINGLE_QUOTE = new CharLiteral(HTMLFile.RIGHT_SINGLE_QUOTE);
    public static final CharLiteral APOSTROPHE = new CharLiteral('\'');
	
    /**
     * <p>The literal character this object wraps.</p>
     */
	public final char c;
	
    /**
     * <p>Constructs a Ch wrapping the specified literal character {@code c}.</p>
     * @param c the literal character to wrap as an {@code HTMLEntity}.
     */
	public CharLiteral(char c) {
		this.c = c;
	}
	
	@Override
    /**
     * <p>Returns a string representation of this Ch. If the wrapped literal character is a newline
     * ('\n'), then a system-dependent newline is returned (via
     * {@code System.getProperty("line.separator")}), otherwise returns a string consisting of
     * exactly the wrapped literal character {@code c}.</p>
     * @return a string representation of this Ch: the literal wrapped character or a
     * system-dependent newline if the wrapped character is '\n'.
     */
	public String toString(){
		return (c==NEW_LINE) ? System.getProperty("line.separator") : new String( new char[]{c} );
	}
	
    /**
     * <p>The newline character '\n'.</p>
     */
	public static final char NEW_LINE = '\n';
	
	@Override
    /**
     * <p>Returns true if {@code o} is a {@code Ch} and wraps the name character as this Ch, false
     * otherwise.</p>
     * @return true if {@code o} is a {@code Ch} and wraps the name character as this Ch, false
     * otherwise.
     */
	public boolean equals(Object o){
		return o instanceof CharLiteral && c == ((CharLiteral)o).c;
	}
	
	@Override
    /**
     * <p>Returns a plaintext representation of this Ch: the result returned by
     * {@code toString()}.</p>
     */
	public String txtString(){
		return toString();
	}
	
	@Override
	public boolean isVisible(){
		return PhraseProducer.isPhraseChar(c);
	}
	
	@Override
	public boolean match(char c){
	    return c == this.c;
	}
}
