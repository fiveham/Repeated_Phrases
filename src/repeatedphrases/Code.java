package repeatedphrases;

import java.util.function.Predicate;

/**
 * <p>Represents an HTML character code starting with an ampersand 
 * and ending with a semicolon.</p>
 */
public class Code extends HTMLEntity {
	
	/**
	 * <p>The content of a <code>Code</code> representing the 
	 * HTML character code for a non-breaking space.</p>
	 */
	public static final String NBSP = "nbsp";
	
	/**
	 * <p>Evaluates to true if the specified HTMLEntity is a 
	 * <code>Code</code> and represents a non-breaking space 
	 * as determined by <code>NBSP</code>.</p>
	 */
	public static final Predicate<HTMLEntity> IS_NBSP = (h) -> HTMLFile.IS_CODE.test(h) && NBSP.equals(((Code)h).code);
	
	/**
	 * <p>The text of this HTML character code between the 
	 * ampersand that begins it and the semicolon that ends 
	 * it.</p>
	 */
	private final String code;
	
	/**
	 * <p>Constructs a Code whose text between the beginning 
	 * ampersand and the ending semicolon equals <code>code</code>.</p>
	 * @param code the text of this HTML character code between 
	 * the beginning ampersand and the ending semicolon
	 */
	public Code(String code) {
		this.code = code;
	}
	
	/**
	 * <p>Returns the literal text for this <code>Code</code> 
	 * between its ampersand and semicolon.</p>
	 * @return
	 */
	public String code(){
		return code;
	}
	
	@Override
	/**
	 * <p>Returns a string representation of this <code>Code</code>, 
	 * made of the starting ampersand, <code>code</code>, and 
	 * the ending semicolon.</p>
	 */
	public String toString(){
		return START+code+END;
	}
	
	/**
	 * <p>The character that begins an html character 
	 * code in an html document.</p>
	 */
	public static final char START = '&';
	
	/**
	 * <p>The character that tneds an html character 
	 * code in an html document.</p>
	 */
	public static final char END = ';';
	
	/**
	 * <p>Returns true if this Code is equivalent to 
	 * the specified literal char, false otherwise.</p>
	 * 
	 * <p>Always returns false. This is a placeholder 
	 * in case it becomes necessary to account for 
	 * some literal characters in html book and chapter 
	 * files by representing them with character codes 
	 * instead of literally.</p>
	 * @param c the literal character to be tested for 
	 * equivalency with this Code
	 * @return true if this Code is equivalent to 
	 * the specified literal char, false otherwise.
	 */
	public boolean isEquivalent(char c){
		/*
		 * It may be necessary in the future to include a detailed 
		 * test for whether a literal char is equivalent to a given 
		 * Code, but for now, it is sufficient to say that no literal 
		 * character is equivalent to any Code, because only 
		 * HTMLFile.match() calls this, and it only needs to know 
		 * so it can try to match characters.
		 */
		return false;
	}
	
	@Override
	/**
	 * <p>Returns true if <code>o</code> is an <code>Code</code> and 
	 * its <code>code</code> member is the same as that of this 
	 * <code>Code</code>.</p>
	 * @return s true if <code>o</code> is an <code>Code</code> and 
	 * its <code>code</code> member is the same as that of this 
	 * <code>Code</code>.
	 */
	public boolean equals(Object o){
		return o instanceof Code && code.equals( ((Code)o).code );
	}
	
	@Override
	/**
	 * <p>Returns a plaintext string equivalent to the character 
	 * that this Code renders as in a browser. This is a single 
	 * space, regardless of the value of this Code.  This is a 
	 * placeholder method in case it becomes necessary to render 
	 * certain characters as html character codes in a file being 
	 * read, in which case, this method would serve as a means of 
	 * translating those codes back to characters.</p>
	 * @return  a plaintext string equivalent to the character 
	 * that this Code renders as in a browser.
	 */
	public String txtString(){
		return " ";
	}
}
