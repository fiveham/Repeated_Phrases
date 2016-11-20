package text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import repeatedphrases.Location;


/**
 * <p>Iterates over a list of chapters, and extracts as many phrases 
 * of the specified {@link #size size} as possible from each 
 * chapter.</p>
 */
public class Corpus {
	
	/**
	 * <p>The number of words in the phrases this object produces.</p>
	 */
	private final int size;
	
	/**
	 * <p>The chapters to be processed.</p>
	 */
	private final List<Chapter> chapters;
	
	/**
	 * <p>The current position in the list of 
	 * {@link #chapters chapters}.</p>
	 */
	private int chapterPointer;
	
	/**
	 * <p>A {@link text.PhraseProducer PhraseProducer} that 
	 * extract phrases from the {@link text.Chapter#body body}
	 * of the {@link #chapterPointer current} Chapter.</p>
	 */
	private PhraseProducer currentBuffer = null;
	
	/**
	 * <p>Constructs a Corpus that produces phrases with <code>size</code> 
	 * words, extracted from the {@link Chapter#body bodies} of 
	 * {@link #chapters chapters}.<p>
	 * @param size the number of words in each phrase
	 * @param chapters a list of <code>Chapter</code>s to whose bodies 
	 * are examined to find phrases
	 */
	public Corpus(int size, Collection<Chapter> chapters) {
		this.size = size;
		this.chapters = new ArrayList<>(chapters);
		chapterPointer = INIT_POINTER_VALUE;
		updateBuffer();	//Sets the first buffer
	}
	
	/**
	 * <p>The initial value to which the chapterPointer 
	 * is set. This is {@value} so that {@link #updateBuffer() updateBuffer}, 
	 * which increments <code>chapterPointer</code>, can be used 
	 * to initially set the buffer during construction<p>
	 */
	public static final int INIT_POINTER_VALUE = -1;
	
	/**
	 * <p>Makes sure that {@link #currentBuffer the current buffer} 
	 * has a {@link text.PhraseProducer#next() next} element 
	 * available, unless there are no more {@link #chapters chapters} 
	 * available.<p>
	 */
	private void updateBuffer(){
		
		//while currentBuffer doesn't have a next element 
		//(or is null) update the buffer to a new buffer 
		//of the next file 
		while( !(currentBuffer != null && currentBuffer.hasNext()) 
				&& chapterPointer < chapters.size()-1){
			
			try{
				currentBuffer = new PhraseProducer(size, chapters.get(++chapterPointer));
			} catch(IllegalArgumentException e){
				//specified chapter didn't have enough tokens in it
				//cycle around to the next chapter
			}/* catch(IndexOutOfBoundsException e){
				//The final buffer is already exhausted
				return;
			}*/
		}
	}
	
	/**
	 * <p>Returns true if the current chapter has a phrase of size 
	 * <code>size</code> available, <code>false</code> otherwise.
	 * If the {@link #chapterPointer current} Chapter does not have a phrase of size 
	 * <code>size</code> available, focus is moved to the next chapter  
	 * repeatedly until a chapter with an appropriately-sized phrase is found 
	 * or there are no more chapters left, at which point, <code>false</code> 
	 * is returned.<p>
	 * @return true if the current chapter has a phrase of size 
	 * <code>size</code> available, false otherwise.
	 */
	public boolean hasNext(){
		updateBuffer();
		return currentBuffer.hasNext();
	}
	
	/**
	 * <p>Returns the next <code>size</code>-word phrase and stores 
	 * that phrase's Location in <code>previousLocation</code>.<p>
	 * @return the next <code>size</code>-word phrase
	 * @throws IllegalStateException when there is no next 
	 * <code>size</code>-word phrase
	 */
	public String next(){
		try{
			updateBuffer();
			previousLocation = new Location(currentBuffer.outputCount(), chapters.get(chapterPointer).getName() );
			return currentBuffer.next();
		} catch(IndexOutOfBoundsException e){
			throw new IllegalStateException("No next phrase available.");
		}
	}
	
	/**
	 * <p>Returns the Location of the last phrase returned from 
	 * {@link #next() next}.</p>
	 * 
	 * <p>The information accessed is only updated before a result is 
	 * returned from {@link #next() next()}.</p>
	 * @return the {@link repeatedphrases.Location Location} of the 
	 * last phrase returned from {@link #next() next()}.
	 */
	public Location prevLocation(){
		if(previousLocation != null){
			return previousLocation;
		}
		else{
			throw new IllegalStateException("No previous Location. No phrases have been output.");
		}
	}
	
	/**
	 * <p>Stores the Location in the corpus of the previous 
	 * phrase output by the underlying PhraseProducer.</p>
	 * 
	 * <p>A value is stored here each time {@link #next() next()} 
	 * is called, just before it returns a result.</p>
	 * 
	 * <p>The stored value is output by 
	 * {@link #prevLocation() prevLocation()}.</p>
	 */
	private Location previousLocation = null;
}
