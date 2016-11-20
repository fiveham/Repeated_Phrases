package repeatedphrases;

/**
 * <p>Data defining an anchor tag to be installed in an html 
 * chapter, linking one instance of a phrase to another 
 * instance of the same phrase.</p>
 */
public class AnchorInfo implements Comparable<AnchorInfo>{
	
	/**
	 * <p>The phrase whose first word will receive the anchor tag. 
	 * This string will also be the vale of the title attribute 
	 * of the anchor tag, showing the exact phrase to which the link 
	 * pertains as mouseover text.</p>
	 */
	private final String phrase;
	
	/**
	 * <p>The location in the corpus of the phrase-instance on 
	 * which this anchor is to be installed. This is used to create 
	 * the value of the id attribute for the anchor tag, which some 
	 * other anchor tag uses to locate and link to this anchor.</p>
	 */
	private final Location position;
	
	/**
	 * <p>The location of the next instance of this anchor's 
	 * phrase in the corpus. Its <code>toString()</code> value 
	 * and its filename component are used to create the value 
	 * of the href attribute of this anchor.</p>
	 */
	private final Location linkTo;
	
	/**
	 * <p>Constructs an AnchorInfo for the specified phrase and position, with 
	 * the specified location to which to link.</p>
	 * @param phrase the phrase of which two instances are linked by this 
	 * anchor
	 * @param position the <code>Location</code> of the phrase-instance on 
	 * which this anchor is installed
	 * @param linkTo the <code>Location</code> of the phrase-instance to which 
	 * this anchor links
	 */
	public AnchorInfo(String phrase, Location position, Location linkTo) {
		this.phrase = phrase;
		this.position = position;
		this.linkTo = linkTo;
	}
	
	/**
	 * <p>Returns the number of words in this AnchorInfo's <code>phrase</code>.</p>
	 * @return the number of words in this AnchorInfo's <code>phrase</code>.
	 */
	public int phraseSize(){
		return phrase.split(" ").length;
	}
	
	/**
	 * <p>Returns this object's <code>phrase</code>.</p>
	 * @return this object's <code>phrase</code>.
	 */
	public String phrase(){
		return phrase;
	}
	
	/**
	 * <p>Returns the word-index of this AnchorInfo's phrase-instance 
	 * in its chapter source file. This is the literal int value 
	 * from the second tab-delimited column on the line of an 
	 * anchordata file pertaining to this AnchorInfo.</p>
	 * @return the word-index of this AnchorInfo's phrase-instance 
	 * in its chapter source file. This is the literal int value 
	 * from the second tab-delimited column on the line of an 
	 * anchordata file pertaining to this AnchorInfo.
	 */
	public int rawIndex(){
		return position.getIndex();
	}
	
	/**
	 * <p>Returns the <code>Location</code> in the corpus of 
	 * the phrase that will have this anchor applied.</p>
	 * @return the <code>Location</code> in the corpus of 
	 * the phrase that will have this anchor applied.
	 */
	public Location position(){
		return position;
	}
	
	/**
	 * <p>Returns the <code>Location</code> to which this 
	 * anchor links.</p>
	 * @return the <code>Location</code> to which this 
	 * anchor links.
	 */
	public Location linkTo(){
		return linkTo;
	}
	
	/**
	 * <p>Returns the value of the href attribute for the 
	 * anchor tag that this object represents, which is 
	 * the name of the html chapter file to which this object's 
	 * represented link leads followed by a hash ("#") and the 
	 * value of the id of the anchor in the linked file of the 
	 * phrase-instance being linked.</p>
	 * @param loc the destination of the link whose href value 
	 * is returned
	 * @return the value of the href attribute for the 
	 * anchor tag that this object represents, which is 
	 * the name of the html chapter file (except the extension) 
	 * to which this object's 
	 * represented link leads, followed by a hash ("#") and the 
	 * value of the id of the anchor in the linked file of the 
	 * phrase-instance being linked.
	 */
	public static String href(Location loc){
		String f = loc.getFilename();
		return IO.stripFolderExtension(f) + IO.HTML_EXT + ADDRESS_ID_CONNECTOR + locationID(loc);
	}
	
	/**
	 * <p>The character that marks that the content that follows 
	 * it in an href attribute's value is an id in the document 
	 * identified by the content prior to this symbol.</p>
	 */
	public static final char ADDRESS_ID_CONNECTOR = '#';
	
	/**
	 * <p>Returns the value of the id attribute of an anchor tag 
	 * on a phrase at the specified Location. Provides the id for 
	 * this anchor, based on <code>position</code> and the id for 
	 * the destination of the link based on <code>linkTo</code>.</p>
	 * @param loc a Location whose equivalent id in an html file 
	 * of a chapter is to be returned.
	 * @return the value of the id attribute of an anchor tag 
	 * on a phrase at the specified Location.
	 */
	public static String locationID(Location loc){
		return Integer.toString(loc.getIndex());
	}
	
	/**
	 * <p>Returns the plaintext equivalent of the opening tag of 
	 * the anchor represented by this object.</p>
	 * @return  the plaintext equivalent of the opening tag of 
	 * the anchor represented by this object.
	 */
	public String openingTag(){
		return Tag.START + openingTagText() + Tag.END;
	}
	
	/**
	 * <p>Returns the plaintext contents of the opening tag of 
	 * this anchor. This is the same as what is returned by 
	 * <code>openingTag()</code> except for the angle brackets.</p>
	 * @return the plaintext contents of the opening tag of 
	 * this anchor. This is the same as what is returned by 
	 * <code>openingTag()</code> except for the angle brackets.
	 */
	public String openingTagText(){
		return "a id=\""+locationID(position)+"\" href=\""+href(linkTo)+"\" title=\""+phrase+"\"";
	}
	
	/**
	 * <p>Returns the plaintext equivalent of the closing tag of 
	 * the anchor represented by this object.</p>
	 * @return the plaintext equivalent of the closing tag of 
	 * the anchor represented by this object.
	 */
	public String closingTag(){
		return Tag.START + closingTagText() + Tag.END;
	}
	
	/**
	 * <p>Returns the plaintext contents of the closing tag of 
	 * this anchor. This is the same as what is returned by 
	 * <code>closingTag()</code> except for the angle brackets.</p>
	 * @return the plaintext contents of the closing tag of 
	 * this anchor. This is the same as what is returned by 
	 * <code>closingTag()</code> except for the angle brackets.
	 */
	public String closingTagText(){
		return Tag.CLOSE + Tag.A;
	}
	
	@Override
	/**
	 * <p>Returns an int whose sign expresses the natural ordering 
	 * between this AnchorInfo and another.</p>
	 * @param a another AnchorInfo
	 * @return an int whose sign expresses the natural ordering 
	 * between this AnchorInfo and another.
	 */
	public int compareTo(AnchorInfo a){
		int comp = position.compareTo(a.position);
		if(comp!=0){
			return comp;
		} else if( (comp=phrase.compareTo(a.phrase))!=0 ){
			return comp;
		} else{
			return linkTo.compareTo(a.linkTo);
		}
	}
}
