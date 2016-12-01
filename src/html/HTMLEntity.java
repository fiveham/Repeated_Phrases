package html;

/**
 * <p>A text element of an HTML file.<p>
 */
public abstract class HTMLEntity {
	
    /**
     * <p>Returns a plaintext interpretation of this HTMLEntity.</p>
     * @return a plaintext interpretation of this HTMLEntity.
     */
	public abstract String txtString();
	
    /**
     * <p>Returns true if this HTMLEntity is visible, false otherwise.</p>
     * @return true if this HTMLEntity is visible, false otherwise.
     */
	public abstract boolean isVisible();
	
    /**
     * <p>Returns true if the {@code c} matches {@code h}, false otherwise. Generally, this means
     * that {@code c} is the literal character wrapped by  }h} because  }h} is a
     * {@link CharLiteral Ch}. A {@code Tag} never matches. A {@code Code} matches {@code c} if it
     * {@link CharCode#isEquivalent(char) is equivalent} to that {@code char}.</p>
     * @param c a literal {@code char} to be compared against {@code h}
     * @return true if the {@code c} matches {@code h}, false otherwise
     */
    public abstract boolean match(char c);//{
    //    if(CharLiteral.class.isInstance(h)){
    //        return ((CharLiteral)h).c == c;
    //    } else if(CharCode.class.isInstance(h)){
    //        return ((CharCode)h).isEquivalent(c);
    //    } else{
    //        return false;
    //    }
    //}
}
