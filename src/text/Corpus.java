package text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

//TODO remove references in comments to 'chapters' and 'chapterpointer'
/**
 * <p>Iterates over a list of chapters, and extracts as many phrases of the specified
 * {@link #size size} as possible from each chapter.</p>
 */
public class Corpus implements Iterator<Quote>{
	
    /**
     * <p>The number of words in the phrases this object produces.</p>
     */
	private final int size;
	
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
	
    /**
     * <p>Crawls along the {@link Chapter#getBody() body} of a Chapter and produces phrases with a
     * certain {@link #size number} of words as substrings of the Chapter's body.</p>
     */
    private static class PhraseProducer implements Iterator<Quote>{
        
        /**
         * <p>The least number of words allowable in phrases produced by a PhraseProducer.</p>
         */
        public static final int MIN_SIZE = 1;
        
        /**
         * <p>The number of words in the phrases this PhraseProducer produces.</p>
         */
        private final int size;
        
        /**
         * <p>The number of times this object has returned a phrase from {@link #next() next()}, which
         * is equal to the word-index in its source chapter of that instance of that phrase.</p>
         */
        private int outputCount;
        
        private final Chapter chapter;
        
        /**
         * <p>Index in {@code content} of the first character of the next phrase that this
         * PhraseProducer will produce, or, if there are not enough words left ahead of this point in
         * {@code content} for this PhraseProducer to produce any more phrases with {@code size} words,
         * this is the index of the first letter of the second word of the last phrase this object
         * outputs.</p>
         */
        private int phraseStart;
        
        /**
         * <p>Index in {@code content} of the first character after the last word of the next phrase
         * this object will output, or content.length() if there are no more phrases for this object to
         * output.</p>
         */
        private int phraseEnd;
        
        /**
         * <p>Constructs a PhraseProducer that extracts phrases with {@code size} words from the
         * {@link Chapter#getBody() body} of {@code chapter} as
         * {@link java.lang.String#substring(int,int) substrings}.</p>
         * @param size the number of words in the phrases produced.
         * @param chapter the Chapter whose {@link Chapter#getBody() body} backs all the phrases this
         * PhraseProducer produces
         * @throws IllegalArgumentException if {@code chapter}'s body has no words or fewer than
         * {@code size} words
         */
        public PhraseProducer(int size, Chapter chapter) {
            if(size < MIN_SIZE){
                throw new IllegalArgumentException("size ("+size+") < MIN_SIZE ("+MIN_SIZE+")");
            }
            this.size = size;
            outputCount = 0;
            
            this.chapter = chapter;
            
            phraseStart = initPhraseStart();
            phraseEnd = initPhraseEnd();
        }
        
        /**
         * <p>Initializes the value of {@link #phraseEnd phraseEnd}.</p>
         * @return the correct initial value of {@link #phraseEnd phraseEnd}
         * @throws IllegalStateException if {@link #filename filename} contains fewer than
         * {@link #size size} words
         */
        private int initPhraseEnd(){
            int wordEndCount = 0;
            for(int i=phraseStart+1; i<chapter.getBody().length(); i++){
                wordEndCount++;
                if(!hasPhraseCharAt(i) 
                        && hasPhraseCharAt(i-1) 
                        && wordEndCount==size){
                    return i;
                }
            }
            throw new IllegalStateException(
                    "File " + chapter.getName() 
                    + " contains " + wordEndCount 
                    + " words, but needs " + size 
                    + ".");
        }
        
        /**
         * <p>Initializes {@link #phraseStart phraseStart}.</p>
         * @return the correct initial value of {@link #phraseStart phraseStart}
         * @throws IllegalStateException if {@link #content} doesn't contain any words
         */
        private int initPhraseStart(){
            for(int i = 0; i < chapter.getBody().length(); i++){
                if(hasPhraseCharAt(i)){
                    return i;
                }
            }
            throw new IllegalStateException("File "+chapter.getName()+" doesn't contain a word");
        }
        
        /**
         * <p>Returns true if the character at index {@code i} in {@code content}
         * @return true if the character at index {@code i} in
         */
        private boolean hasPhraseCharAt(int i){
            return 0 <= i 
                    && i < chapter.getBody().length() 
                    && Phrase.isPhraseChar(chapter.getBody().charAt(i));
        }
        
        @Override
        /**
         * <p>Returns true if there is at least one more phrase to extract from {@code content}, false
         * otherwise.</p>
         * @return true if there is at least one more phrase to extract from {@code content}, false
         * otherwise
         */
        public boolean hasNext(){
            return phraseEnd <= chapter.getBody().length();
        }
        
        @Override
        /**
         * <p>Returns the next phrase with {@code size} words from  }content}.</p>
         * @return the next phrase with {@code size} words from  }content}
         */
        public Quote next(){
            outputCount++;
            Quote out = new Quote(
                    new Location(outputCount, chapter), //XXX may be off by 1
                    chapter.getBody().substring(phraseStart, phraseEnd));
            phraseStart = nextPhraseStart();
            phraseEnd = nextPhraseEnd();
            return out;
        }
        
        /**
         * <p>In {@code content}, finds the next character after {@code phraseEnd} that's the first
         * character after a word.</p>
         * @return the index in {@code content} of the next word-end after {@code phraseEnd}
         */
        private int nextPhraseEnd(){
            for(int i = phraseEnd + 1; i <= chapter.getBody().length(); i++){
                if(hasPhraseCharAt(i - 1) && !hasPhraseCharAt(i)){
                    return i;
                }
            }
            return chapter.getBody().length()+1;
        }
        
        /**
         * <p>Determines the index at which the next phrase starts after a phrase is returned by
         * {@link #next() next()}.</p>
         * @return the next index in {@code content} at which the next word starts, or
         *  }content.length()} if there is no next word
         */
        private int nextPhraseStart(){
            for(int i = phraseStart + 1; i < chapter.getBody().length(); i++){
                if(!hasPhraseCharAt(i - 1) && hasPhraseCharAt(i)){
                    return i;
                }
            }
            return chapter.getBody().length();
        }
    }
}
