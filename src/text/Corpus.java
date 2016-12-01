package text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * <p>Iterates over a list of chapters, and extracts as many phrases of the specified
 * {@link #size size} as possible from each chapter.</p>
 */
public class Corpus implements Iterator<Quote>{
	
    /**
     * <p>The number of words in the phrases this object produces.</p>
     */
	private final int size;
	
    /**
     * <p>The current position in the list of {@link #chapters chapters}.</p>
     */
	//private int chapterPointer;
	
	private final Iterator<Chapter> chIter;
	
    /**
     * <p>A {@link text.PhraseProducer PhraseProducer} that extract phrases from the
     * {@link text.Chapter#body body} of the {@link #chapterPointer current} Chapter.</p>
     */
	private PhraseProducer currentBuffer = null;
	
    /**
     * <p>Constructs a Corpus that produces phrases with {@code size} words, extracted from the
     * {@link Chapter#body bodies} of {@link #chapters chapters}.<p>
     * @param size the number of words in each phrase
     * @param chapters a list of {@code Chapter}s to whose bodies are examined to find phrases
     */
	public Corpus(int size, Collection<Chapter> chapters) {
		this.size = size;
		chIter = new ArrayList<>(chapters).iterator();
		updateBuffer();	//Sets the first buffer
	}
	
    /**
     * <p>The initial value to which the chapterPointer is set. This is {@value} so that
     * {@link #updateBuffer() updateBuffer}, which increments {@code chapterPointer}, can be used to
     * initially set the buffer during construction<p>
     */
	public static final int INIT_POINTER_VALUE = -1;
	
    /**
     * <p>Makes sure that {@link #currentBuffer the current buffer} has a
     * {@link text.PhraseProducer#next() next} element available, unless there are no more
     * {@link #chapters chapters} available.<p>
     */
	private void updateBuffer(){
		//while currentBuffer doesn't have a next element 
		//(or is null) update the buffer to a new buffer 
		//of the next file 
		while(!bufferHasNext() && chIter.hasNext()){
			try{
				currentBuffer = new PhraseProducer(size, chIter.next());
			} catch(IllegalArgumentException f){
				//specified chapter didn't have enough tokens in it
				//cycle around to the next chapter
			}
		}
	}
	
	private boolean bufferHasNext(){
		return currentBuffer != null 
				&& currentBuffer.hasNext();
	}
	
	@Override
    /**
     * <p>Returns true if the current chapter has a phrase of size {@code size} available,
     * {@code false} otherwise. If the {@link #chapterPointer current} Chapter does not have a
     * phrase of size {@code size} available, focus is moved to the next chapter repeatedly until a
     * chapter with an appropriately-sized phrase is found or there are no more chapters left, at
     * which point, {@code false} is returned.<p>
     * @return true if the current chapter has a phrase of size {@code size} available, false
     * otherwise.
     */
	public boolean hasNext(){
		updateBuffer();
		return currentBuffer.hasNext();
	}
	
	@Override
    /**
     * <p>Returns the next {@code size}-word phrase and stores that phrase's Location in
     * {@code previousLocation}.<p>
     * @return the next {@code size}-word phrase
     * @throws IllegalStateException when there is no next {@code size}-word phrase
     */
	public Quote next(){
		try{
			updateBuffer();
			return currentBuffer.next();
		} catch(IndexOutOfBoundsException e){
			throw new IllegalStateException("No next phrase available.");
		}
	}
}
