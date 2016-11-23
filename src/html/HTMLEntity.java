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
}
