package repeatedphrases;

import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * <p>Stores phrase-instance data chapter-first.</p>
 * 
 * <p>Wraps a {@link repeatedphrases.FileBox FileBox}.</p>
 */
public class Database {
	
	/**
	 * <p>The wrapped FileBox.</p>
	 */
	private FileBox textCorpus;
	
	/**
	 * <p>Constructs a Database with an empty <code>textCorpus</code>.</p>
	 */
	public Database(){
		textCorpus = new FileBox();
	}
	
	/**
	 * <p>Constructs a Database with a FileBox containing all the 
	 * phrase-instance data from the specified File.</p>
	 * @param f the file containing phrase-instance data to be read
	 * @throws FileNotFoundException if the specified File 
	 * does not exist or cannot be read
	 */
	public Database(File f) throws FileNotFoundException{
		textCorpus = new FileBox(f);
		
		//List<Record> allRecords = new ArrayList<>();
		
		/*try(Scanner scan = new Scanner( f )){
			while(scan.hasNextLine() && scan.hasNext()){
				
				//Separate the phrase from the locations
				String[] phraseAndLocations = scan.nextLine().split(IO.LOCATION_DELIM);
				String phrase = phraseAndLocations[0];
				
				//For each Location represented on the line, 
				//create a Record and an entry in the abused PhraseBox
				for(int i=1; i<phraseAndLocations.length; i++){
					String[] fileAndIndex = phraseAndLocations[i].split(Location.ELEMENT_DELIM);
					int index = Integer.parseInt(fileAndIndex[1]);
					String filename = fileAndIndex[0];
					allRecords.add( new Database.Record(phrase, filename, index) );
					textCorpus.add(filename, new Location(index, phrase) );
				}
			}
			scan.close();
		} catch(ArrayIndexOutOfBoundsException e){
			throw new IllegalArgumentException("The specified file ("+f.getName()+") is not structured like a record of phrases and locations.");
		}/**/
		
		//data = new SortedList<>(allRecords);
	}
	
	/**
	 * <p>Returns a HashMap mapping the int parts of the 
	 * <code>IntString</code>s from <code>list</code> to the 
	 * Strings paired with them.</p>
	 * @param list a list of int-string pairs to be converted 
	 * into a HashMap
	 * @return a HashMap mapping the int parts of the 
	 * <code>IntString</code>s from <code>list</code> to the 
	 * Strings paired with them.
	 */
	private HashMap<Integer, String> mapFromIntString(List<IntString> list){
		HashMap<Integer,String> result = new HashMap<>();
		for(IntString is : list){
			result.put(is.index, is.phrase);
		}
		return result;
	}
	
	/**
	 * <p>Returns a PhraseBox containing exactly those phrase-instances 
	 * represented in <code>this.textCorpus</code> that are independent 
	 * of all the phrase-instances represented in 
	 * <code>otherDatabase.textCorpus</code>.</p>
	 * @param otherDatabase another Database against whose phrase-
	 * instances the phrase-instances of this Database are to be 
	 * tested for independence.
	 * @return a PhraseBox containing exactly those phrase-instances 
	 * represented in <code>this.textCorpus</code> that are independent 
	 * of all the phrase-instances represented in 
	 * <code>otherDatabase.textCorpus</code>.
	 */
	public PhraseBox phrasesIndependentOf(Database otherDatabase){
		PhraseBox result = new PhraseBox();
		
		//Look at each group of phrase-instances that share a filename
		for( String filename : textCorpus.filenames() ){
			if( otherDatabase.textCorpus.contains(filename) ){
				
				//System.out.println("About to expand a list of IntStrings");
				//get a random-access representation of the current corpus file
				//List<String> fileForLargePhrases = expand( otherDatabase.textCorpus.get(filename) );
				HashMap<Integer, String> fileForLargePhrases = mapFromIntString(otherDatabase.textCorpus.get(filename));
				
				//iterate over the phrase-instances for the current filename in this Database.
				for(IntString smallerPhraseInFile : textCorpus.get(filename)){
					
					//Phrases from the other Database that could possibly contain 
					//the phrase component of the current phrase-index pair.
					
					//Only two samples of the representation of the other file 
					//are taken because the overall program only ever calls this 
					//method with an argument Database containing phrases one word 
					//longer than those in the Database on which the method is 
					//executed.
					
					//the larger-sized phrase starting one word earlier in the file 
					//than smallerPhraseInFile does
					String largerPhraseWithIndexOneLess = smallerPhraseInFile.index == 0 
							? null 
							: fileForLargePhrases.get(smallerPhraseInFile.index-1);
					
					//the larger-sized phrase starting at the same location in the file 
					//as smallerPhraseInFile
					String largerPhraseWithSameIndex = fileForLargePhrases.size() == smallerPhraseInFile.index 
							? null
							: fileForLargePhrases.get(smallerPhraseInFile.index);
					
					//If either of the possible subsuming phrases contains the current phrase 
					//at the proper location in itself, then 
					Boolean lowerIndexLargerPhraseExistsAndEndsWithSmallPhrase = largerPhraseWithIndexOneLess == null
							? null
							: largerPhraseWithIndexOneLess.endsWith(smallerPhraseInFile.phrase);
					
					Boolean sameIndexLargerPhraseExistsAndStartsWithSmallPhrase = largerPhraseWithSameIndex == null
							? null
							: largerPhraseWithSameIndex.startsWith(smallerPhraseInFile.phrase);
					
					if( lowerIndexLargerPhraseExistsAndEndsWithSmallPhrase == null 
							&& sameIndexLargerPhraseExistsAndStartsWithSmallPhrase == null ){
						//There's no phrase-instance in otherDatabase at this phrase's index or 
						//1 less than that
						
						//There's no phrase in otherDatabase that subsumes this phrase
						//the phrase in focus from this Database is independent of otherDatabase; 
						//so, the current small phrase should be added to the output.
						result.add(smallerPhraseInFile.phrase, new Location(smallerPhraseInFile.index, filename));
					} else if( !((lowerIndexLargerPhraseExistsAndEndsWithSmallPhrase  == null 
							||    lowerIndexLargerPhraseExistsAndEndsWithSmallPhrase  == true) 
							||   (sameIndexLargerPhraseExistsAndStartsWithSmallPhrase == null 
							||    sameIndexLargerPhraseExistsAndStartsWithSmallPhrase == true)) ){
						
						//It is not the case that LILPEAEWSP and SILPEASWSP have a value pair of 
						//(null, true), (true, true), or (true, null), any of which would 
						//indicate perfectly that the smaller phrase is contained in an instance 
						//of a larger phrase from otherDatabase.
						//Since (null, null) is also excluded (by this test and by the fact 
						//that execution has moved to this block of code while the previous if-test 
						//tests exactly for (null, null)), at least one of the Boolean 
						//values is false, which means that an instance of a larger phrase 
						//exists in a position that should overlap the smaller phrase, 
						//yet that larger phrase doesn't startWith or endWith the smaller phrase
						//the way its location demands, which is impossible.
						
						throw new IllegalStateException(
								"The smaller phrase \""+IO.shortForm(smallerPhraseInFile.phrase)+
								"\" is contained at the proper location in zero or one of the two larger phrases that could contain it: " +  
								"Phrase at some index in file "+filename+": \""+
								IO.shortForm(largerPhraseWithIndexOneLess)+"\" --- " + 
								"Phrase at some index in file "+filename+": \""+
								IO.shortForm(largerPhraseWithSameIndex)+"\".");
					} //Otherwise, the Boolean assignments did equal (null, true), (true, true), or (true, null), 
					//which indicate that the small phrase is dependent on a larger phrase 
					//with no error.
					//In that case, no action should be taken.
				}
			} else{
				//otherDatabase contains no entries from this file; so, 
				//all entries in this Database from that corpus file 
				//are independent of otherDatabase.
				//Add all phrase-instances for this filename to the output
				for(IntString locatedPhrase : textCorpus.get(filename) ){
					result.add(locatedPhrase.phrase, new Location(locatedPhrase.index, filename) );
				}
			}
		}
		
		return result;
	}
	
	/*private static List<String> expand(List<IntString> list){
		ArrayList<String> result = new ArrayList<>();
		
		for(IntString is : list){
			result.ensureCapacity(is.index);
			while(result.size() < is.index){
				result.add(null);
			}
			result.add(is.index, is.phrase);
			System.out.printf("Added %s to an expanded list at index %d\n", IO.shortForm(is.phrase), is.index);
		}
		
		return result;
	}/**/
	
	/* *
	 * Returns an int indicating what should be done in phrasesIndependentOf(Database).
	 * 
	 * If neither the lower nor the higher test could be conducted, 
	 * the lower and higher larger phrases must not have existed, 
	 * in which case the smaller phrase under examination is independent.
	 * In that case, 9 is returned.
	 * 
	 * If one of the tests could not be conducted but the other could, then 
	 * that conductable test distinguishes whether the smaller phrase under 
	 * examination is independent.  If the test comes back true, then the 
	 * smaller phrase is dependent, otherwise the smaller phrase is 
	 * independent.
	 * 
	 * If both the tests could be conducted and they produced the same result, 
	 * then a result of true indicates the smaller phrase is dependent, and 
	 * a result of false indicates it is independent.
	 * 
	 * If both tests could be conducted, but they produced different results, 
	 * then something has gone wrong and an IllegalStateException is thrown.
	 * @param b1
	 * @param b2
	 * @return
	 * /
	private static int score(Boolean b1, Boolean b2){
		return Math.min(score(b1),score(b2));
	}
	
	private static int score(Boolean b){
		return (b==null) ? NULL_SCORE : ( b ? TRUE_SCORE : FALSE_SCORE );
	}
	
	private static final int NULL_SCORE = 2;
	private static final int TRUE_SCORE = 1;
	private static final int FALSE_SCORE = 0;
	
	private static Boolean result(String longPhrase, Predicate<String> p){
		return longPhrase==null ? null : p.test(longPhrase);
	}/**/
	
	/* *
	 * Returns the value of the int included in the specified filename 
	 * preceded by the last underscore in the name and followed by the 
	 * last period in the name.
	 * @param filename
	 * @return
	 * /
	private static int determineSize(String filename){
		return Integer.parseInt(filename.substring(filename.lastIndexOf("_")+1, filename.lastIndexOf(".")));
	}/**/
	
	/* *
	 * Returns a Table representing all those phrase-instances 
	 * represented in this Database which are independent of all 
	 * phrase-instances represented in otherDatabase.
	 * 
	 * All phrases might be independent; until you've proven a phrase 
	 * is dependent, you cannot justify excluding it from the returned 
	 * Table.
	 * 
	 * If a phrase-instance is dependent on a larger one from 
	 * otherDatabase, then the phrase-instance from here and the 
	 * one from there must share filenames.
	 * 
	 * Thus, if we identify what filenames are present for this 
	 * Database and which ones are present for both this one and 
	 * otherDatabase, we can quickly assess that those phrase-instances 
	 * from here whose filenames are not in the list of shared 
	 * filenames are independent of otherDatabase.
	 * 
	 * @param otherDatabase
	 * @return a Table representing all those phrase-instances 
	 * represented in this Database which are independent of all 
	 * phrase-instances represented in otherDatabase.
	 * /
	public PhraseBox phrasesIndependentOf_old(Database otherDatabase){
		
		//System.out.println("Assessing phrase-independence.");
		
		PhraseBox result = new PhraseBox();
		
		//List<String> sharedFilenames = new SortedList<>(intersection(filenames(), otherDatabase.filenames()));
		
		//System.out.println("Got list of "+sharedFilenames.size()+" shared filenames.");
		//for(String s : sharedFilenames){
		//	System.out.println("\t"+s);
		//}
		
		//System.out.println("Looping over Records");
		
		for(Record r : data){
			
			//System.out.println("Current Record: "+r.toString());
			
			//boolean includes = otherDatabase.includes(r);
			//if(includes){
				//System.out.println("The other Database includes (w/o accounting for filename) this Record.");
			//} else{
				//System.out.println("The other Database does not include this Record.");
			//}
			
			if( !otherDatabase.includes(r) ){
				//System.out.println("This Record does not meet the criteria for dependence and thus is independent and is added to output.");
				result.add(r.phrase, new Location(r.index, r.filename) );
			}
		}
		
		return result;
	}/**/
	
	/*private static final Comparator<Location> INDEX_FILENAME = new Comparator<Location>(){
		@Override
		public int compare(Location loc1, Location loc2){
			int indexComp = loc1.getIndex() - loc2.getIndex();
			if(indexComp != 0){
				return indexComp;
			}
			return loc1.getFilename().compareTo(loc2.getFilename());
		}
	};/**/
	
	//private boolean listIncludesLocatedPhrase(List<Location> list, Location locatedPhrase){
	//	
	//}
	
	/* *
	 * Assesses whether the specified Record is included in this Database.
	 * @param otherRecord
	 * @return
	 * /
	private boolean includes(Record otherRecord){
		
		//int[] bounds = filenameMatchBounds(otherRecord.filename);
		//int lowerBound = bounds[0];
		//int upperBound = bounds[1];
		
		boolean retBool = false;
		//if( 0 <= lowerBound && lowerBound <= upperBound ){
			//System.out.println("Bounds for filename-matching region are positive; need further analysis. Iterating over bounds-region.");
			for(int i=0; !retBool && i<data.size(); i++){
				//System.out.println("i="+i+"; Checking if this Database's i^th Record includes otherRecord.");
				if(data.get(i).includesAssumeFilename(otherRecord)){
					//System.out.println("It matched!");
					retBool = true;
				}
			}
		//}// else{
			//System.out.println("A bound for filename-matching region in this Database was negative; the Record is not included.");
		//}
		return retBool;
		
		
		//System.out.println("Determining inclusion/dependence for Record: "+otherRecord.toString());
		//
		//int[] bounds = filenameMatchBounds(otherRecord.filename);
		//int lowerBound = bounds[0];
		//int upperBound = bounds[1];
		//
		//boolean retBool = false;
		//if( 0 <= lowerBound && lowerBound <= upperBound ){
		//	//System.out.println("Bounds for filename-matching region are positive; need further analysis. Iterating over bounds-region.");
		//	for(int i=lowerBound; retBool == false && i<=upperBound; i++){
		//		//System.out.println("i="+i+"; Checking if this Database's i^th Record includes otherRecord.");
		//		if(data.get(i).includesAssumeFilename(otherRecord)){
		//			//System.out.println("It matched!");
		//			retBool = true;
		//		}
		//	}
		//}// else{
		//	//System.out.println("A bound for filename-matching region in this Database was negative; the Record is not included.");
		////}
		//return retBool;
		
	}/**/
	
	/* *
	 * Determines what region in <code>data</code> has Records 
	 * that have <code>name</code> as their <code>filename</code>.
	 * @param name
	 * @return a long whose most significant 32 bits are equivalent 
	 * to an int equal to the highest index in <code>data</code> at 
	 * which a Record has <code>name</code> as its <code>filename</code> 
	 * and whose least significant 32 bits are equivalent to an int 
	 * equal to the lowest index in <code>data</code> at which a 
	 * Record has <code>name</code> as its <code>filename</code>.
	 * /
	private int[] filenameMatchBounds(String filename){
		return filenameBoundsCache.apply(filename);
	}/**/
	
	/*private static class IntPair{
		private final int lo;
		private final int hi;
		private IntPair(int i, int j){
			lo = i;
			hi = j;
		}
		private boolean areNonNegative(){
			return lo>-1 && hi>-1;
		}
		//@Override
		//public String toString(){
		//	return "["+lo+","+hi+"]";
		//}
	}/**/
	
	/* *
	 * A cache for the results of <code>longWhereFilenameEquals()</code>.
	 * 
	 * One input-output pair is stored at a time.
	 * /
	private Function<String, int[]> filenameBoundsCache = new Function<String, int[]>(){
		private String input = null;
		private int[] output = null;
		@Override
		public int[] apply(String filename){
			//System.out.println("Determining bounds of region in this Database where the Records' filenames equal "+filename);
			if( !filename.equals(input) ){
				Test<Record> test = (r) -> r.filename.compareTo(filename);
				int low = data.indexOrInsertCode(test);
				int top = data.greatestIndexOf(test, low);
				input = filename;
				output = new int[]{low, top};
			}
			//System.out.println("Got inclusive bounds "+output.toString());
			return output;
		}
	};/**/
	
	
	/*private static class Record implements Comparable<Record>{
		
		private String phrase;
		private String filename;
		private int index;
		
		public Record(String phrase, String filename, Integer index){
			this.phrase = phrase;
			this.filename = filename;
			this.index = index;
		}
		
		@Override	//implements Comparable<Database.Record>
		public int compareTo(Record otherRecord){
			int c = filename.compareTo(otherRecord.filename);
			if(c!=0){
				return c;
			} else if(index != otherRecord.index){
				return index - otherRecord.index;
			} else{
				return phrase.compareTo(otherRecord.phrase);
			}
		}
		
		@Override
		public String toString(){
			return "\""+IO.shortForm(phrase)+"\""+PhraseProducer.WORD_SEPARATOR+filename+Location.ELEMENT_DELIM+index;
		}
		
		/**
		 * Returns true if the (non-negative) number of spaces 
		 * preceding otherRecord.phrase in this.phrase equals 
		 * the difference between index and otherRecord.index, 
		 * false otherwise.
		 * 
		 * If this method returns true, then the parameter is 
		 * included in this Record in the general sense if its 
		 * filename is the same as that of this Record.
		 * @param otherRecord
		 * @return
		 * /
		private boolean includesAssumeFilename(Record otherRecord){
			
			//System.out.printf("Question: Does %s include %s, assuming they share filenames?\n", toString(), otherRecord.toString());	//debug
			
			//Get the difference of the indices of the Records.
			//That's how many words should precede otherRecord.phrase 
			//in this.phrase if otherRecord is included in this 
			//Record.
			//If the phrase-instance represented by otherRecord is 
			//included in the phrase-instance represented by this Record, 
			//then otherRecord.index >= this.index; otherwise, 
			//otherRecord is not included in this Record.
			//Last but most important, the substring of this.phrase 
			//that immediately follows the nth space in this.phrase, 
			//where n = this.index - otherRecord.index, 
			//must start with otherRecord.phrase; otherwise this.phrase 
			//does not contain otherRecord.phrase at all or does 
			//not contain it at the correct position given the 
			//value of otherRecord.index
			int indexDiff = otherRecord.index - index;
			boolean retVal;
			if(indexDiff < 0){
				retVal = false;
			} else{
				String portion;
				if(indexDiff == 0){
					portion = phrase;
				} else if(indexDiff == 1){
					String space = PhraseProducer.WORD_SEPARATOR;
					portion = phrase.substring(phrase.indexOf(space)+space.length());
				} else{
					String[] split = phrase.split(PhraseProducer.WORD_SEPARATOR, indexDiff+1);	//"+1" because String.split is weird.
					portion = split[split.length-1];
				}
				retVal = portion.startsWith(otherRecord.phrase);
			}
			return retVal;
		}
		
		
		@Deprecated
		private Record(Table.Pair pair){
			this.phrase   = pair.phrase();
			this.filename = pair.location().getFilename();
			this.index    = pair.location().getIndex();
		}
		
		@Deprecated
		/**
		 * Returns a Table.Pair that represents the same 
		 * phrase-instance that this Record represents.
		 * @return a Table.Pair that represents the same 
		 * phrase-instance that this Record represents.
		 * /
		private Table.Pair toPair(){
			return new Table.Pair(phrase, new Location(index, filename));
		}
	}/**/
	
	
	/*public static void main(String[] args){
		String filename = "dork.txt";
		
		String bigPhrase = "the night's king";
		String lilPhrase1 = "the night's";
		String lilPhrase2 = "night's king";
		
		int firstIndex = 8;
		int secondIndex = 9;
		
		Record bigR = new Record(bigPhrase, filename, firstIndex);
		Record lilR1 = new Record(lilPhrase1, filename, firstIndex);
		Record lilR2 = new Record(lilPhrase2, filename, secondIndex);
		
		System.out.printf("Does \"%s\"@%d include (assuming filename) %s@%d?: %b\n", 
				bigPhrase, firstIndex, lilPhrase1, firstIndex, bigR.includesAssumeFilename(lilR1));
		System.out.printf("Does \"%s\"@%d include (assuming filename) %s@%d?: %b\n", 
				bigPhrase, firstIndex, lilPhrase2, secondIndex, bigR.includesAssumeFilename(lilR2));
		
	}/**/
	

	
	/*
	@Deprecated
	/* *
	 * Returns a Table representing all those phrase-instances 
	 * represented in this Database which are independent of all 
	 * phrase-instances represented in otherDatabase.
	 * 
	 * All phrases might be independent; until you've proven a phrase 
	 * is dependent, you cannot justify excluding it from the returned 
	 * Table.
	 * 
	 * If a phrase-instance is dependent on a larger one from 
	 * otherDatabase, then the phrase-instance from here and the 
	 * one from there must share filenames.
	 * 
	 * Thus, if we identify what filenames are present for this 
	 * Database and which ones are present for both this one and 
	 * otherDatabase, we can quickly assess that those phrase-instances 
	 * from here whose filenames are not in the list of shared 
	 * filenames are independent of otherDatabase.
	 * 
	 * @param otherDatabase
	 * @return a Table representing all those phrase-instances 
	 * represented in this Database which are independent of all 
	 * phrase-instances represented in otherDatabase.
	 * /
	public Table tableOfPhrasesIndependentOf(Database otherDatabase){
		
		//System.out.println("Determining independent phrase-instances at Database level.");	//debug
		
		//for each filename represented in both these databases
		//isolate the portion of each database that 
		//matches the current filename
		//then do old-fashioned brute-force comparisons to 
		//determine whether the phrase-instance represented 
		//by a Record from the sample of this Database is 
		//independent of the sample of the other Database.
		//if a phrase-instance is found independent, include 
		//it in the result-Table.
		//determine whether or not the data from a Record 
		//from the sample of this Database should be included 
		//in the results.
		//Database result = new Database();
		Table outTable = new Table();
		
		List<String> sharedFilenames = new SortedList<>(intersection(filenames(), otherDatabase.filenames()));
		for(Record r : data){
			if( !( sharedFilenames.contains(r.filename) 
					&& otherDatabase.includesAssumeFilename(r) ) ){
				outTable.add(r.toPair());
			}
		}
		return outTable;
		
		//for(String filename : intersection(filenames(), otherDatabase.filenames())){
		//	long longSample = longWhereFilenameEquals(filename);
		//	long longOtherSample = otherDatabase.longWhereFilenameEquals(filename);
		//	int     lowHere  = (int)  longSample, 
		//			topHere  = (int) (longSample >> 32), 
		//			lowThere = (int)  longOtherSample, 
		//			topThere = (int) (longOtherSample >> 32);
			
			//for each Record in this Database for which the filename matches the current filename, 
			//add that Record to result.data if and only if the filename-matching region of the 
			//other Database does not have a Record that includes the current Record
		//	for(int i=lowHere; i<=topHere; i++){
		//		if( !otherDatabase.includesAssumeFilename(data.get(i), lowThere, topThere) ){
		//			//result.data.add(data.get(i));
		//			outTable.add(data.get(i).toPair());
		//			outTable.trimRecursive();
		//		}
		//	}
		//}
	}/**/
	
	/* *
	 * Returns a long whose least significant 32 bits are 
	 * equivalent to <code>i</code>.
	 * @param i the int whose bits are to be represented 
	 * in a long.
	 * @return a long whose least significant 32 bits are 
	 * equivalent to <code>i</code>.
	 * /
	private static long bitstringLong(int i){
		long result = (long) i;
		if(i<0){
			result = (result & UNBIT_63) | LONG_BIT_31;
		}
		return result;
	}/**/
	//private static final long LONG_BIT_31 = ((long) Integer.MAX_VALUE) + 1;
	//private static final long UNBIT_63 = Long.MAX_VALUE;
	
	/* *
	 * Returns a list containing exactly the elements contained in both 
	 * list and otherList.
	 * @param list
	 * @param otherList
	 * @return
	 * /
	private static List<String> intersection(Collection<String> list, Collection<String> otherList){
		
		//Combine both lists into a sorted list; so duplicates are adjacent
		List<String> cumul = new ArrayList<String>(list.size()+otherList.size());
		cumul.addAll(list);
		cumul.addAll(otherList);
		cumul.sort(null);
		
		//Crawl the combined list and add items that differ from their 
		//predecessor to an output list, ensuring no duplicates are output.
		
		//for each element, if that element is a duplicate
		//(if it equals an adjacent element), 
		//and that element is not already included in the output, 
		//then add that element to the output
		List<String> out = new SortedList<>(cumul.size());
		for(int i=1; i<cumul.size(); i++){
			if( cumul.get(i).equals(cumul.get(i-1)) && !out.contains(cumul.get(i)) ){
				out.add( cumul.get(i) );
			}
		}
		return out;
	}/**/
	
	/* *
	 * Returns a list of all the filenames used in this Database.
	 * @return
	 * /
	private SortedList<String> filenames(){
		if(filenames == null){
			HashMap<String,Integer> hm = new HashMap<>();
			for(Record r : data){
				if(!hm.containsKey(r.filename)){
					hm.put(r.filename, 0);
				}
			}
			filenames = new SortedList<>(hm.keySet());
		}
		return filenames;
	}
	
	/ * *
	 * A list of the names of the files which have phrase-instances 
	 * represented in this Database.  Initialized to null; 
	 * given a meaningful value by <code>filenames()</code>.
	 * /
	private SortedList<String> filenames = null;/**/
	
	
	

	
	/*private static final int BUFFER_SIZE = 100;
	@Deprecated
	public Database(Table t){
		data = new SortedList<>(t.pairCount());
		
		SortedList<Record> temp = new SortedList<>(BUFFER_SIZE);
		for(Iterator<Table.Pair> iter = t.iterableOfPair().iterator(); iter.hasNext();){
			for(int i=0; i<BUFFER_SIZE && iter.hasNext(); i++){
				temp.add(new Record(iter.next()));
			}
			data.addAll(temp);
			temp.clear();
		}
	}/**/
	
	
	/* *
	 * A comparator which first compares the filenames of two Records, 
	 * then compares the Records' indices, and finally compares their 
	 * phrases.
	 * /
	private static final Comparator<Record> FILE_INDEX_PHRASE = new Comparator<Record>(){
		@Override
		public int compare(Record r1, Record r2){
			int c = r1.filename.compareTo(r2.filename);
			if(c!=0){
				return c;
			}
			else if(0 != (c = r1.index - r2.index)){
				return c;
			}
			else{
				return r1.phrase.compareTo(r2.phrase);
			}
		}
	};/**/
	
	/* *
	 * The Comparator currently used by Record for its natural ordering
	 * 
	 * Is initialized as FILE_INDEX_PHRASE
	 */
	//private static Comparator<Record> comparator = FILE_INDEX_PHRASE;
	
	/* *
	 * Produces a Table representing all the phrase-instances 
	 * represented in this Database.
	 * @return
	 * /
	private Table toTable(){
		Table result = new Table();
		
		for(Record r : data){
			result.add(r.toPair());
		}
		
		result.trimRecursive();
		return result;
	}/**/
	
	/* *
	 * Returns false if the phrase-instance represented by otherRecord 
	 * is independent of all the phrase-instances represented in this 
	 * Database, true otherwise.
	 * @param otherRecord
	 * @return false if the phrase-instance represented by otherRecord 
	 * is independent of all the phrase-instances represented in this 
	 * Database, true otherwise.
	 * /
	private boolean includes(Record otherRecord){
		boolean retBool = false;
		for(int i=0; retBool == false && i<data.size(); i++){
			if(data.get(i).includesAssumeFilename(otherRecord)){
				retBool = true;
			}
		}
		//System.out.printf("Inclusion-test for %s in this Database returned %b\n", otherRecord.toString(), retBool);	//debug
		return retBool;
	}/**/
	
	/* *
	 * Returns a subportion of this object containing exactly 
	 * those records whose filename equals the specified name.
	 * Returns a new Database to account for situations in which 
	 * this Database is sorted in a manner other than that indicated 
	 * by FILE_INDEX_PHRASE.  This method stores the original 
	 * comparator used for sorting, sorts the list by FILE_INDEX_PHRASE, 
	 * populates a new Database with the Records whose filenames 
	 * match the specified filename, and resets the original 
	 * Comparator and resorts the list before returning.
	 * @param name
	 * @return
	 * /
	private Database whereFilenameEquals(String name){
		//store the original comparator to replace it after you're done.
		//if comparator is already the way we need it; don't bother.
		//set the comparator to FILE_INDEX_PHRASE, sorting the database
		Comparator<Record> originalComparator = null;
		if(comparator!=FILE_INDEX_PHRASE){
			originalComparator = comparator;
			useComparator(FILE_INDEX_PHRASE);
		}
		
		//get the earliest index at which a record has the specified filename
		//get the latest index at which a record has the specified filename
		Test<Record> test = new Test<Record>(){
			@Override
			public int test(Record r){
				return r.filename.compareTo(name);
			}
		};
		int low = data.indexOrInsertCode(test);
		
		SortedList<Record> subList;
		try{
			//int top = data.greatestIndexOf(test, low);
			//produce a subportion of the list based on these endpoints.
			subList = data.subList(low, 1+data.greatestIndexOf(test, low));
		}
		catch(IndexOutOfBoundsException e){	//low < 0 || top > data.size() 
			//if low < 0, then there's no Records in this Database 
			//whose filename is the specified name
			subList = new SortedList<>(0);
			//if top > data.size(), then something impossible has 
			//gone wrong inside SortedList.greatestIndexOf
		}
		//catch(IllegalArgumentException e){	//top < low
			//If top < low, then 
			//1. there are no Records in this Database with 
			//the specified filename, and 
			//2. low should already have been < 0, triggering 
			//the previous catch(IndexOutOfBoundsException) clause.
		//	subList = new SortedList<>(0);
		//}
		finally{	//If an exception I haven't accounted for is thrown, I must ensure the comparator is set to its original state.
			//restore the original comparator
			if(originalComparator!=null){
				comparator = originalComparator;
			}
		}
		
		//return the produced database
		//Database ret = new Database(subList);	//debug
		//System.out.println("Portion where filename equals "+name+": "+ret.toTable().toString());	//debug
		//return ret;
		Database ret = new Database();
		ret.data = subList;
		return ret;
	}/**/
	
	/* *
	 * Sets the comparator used by this class for its natural ordering 
	 * to the specified Comparator, if it is not null, 
	 * and sorts the Database's data according to the new 
	 * comparator.
	 * @param c
	 * /
	private void useComparator(Comparator<Record> c){
		comparator = c;		//change the "natural ordering"
		data.sort(null);	//use the new "natural ordering" 
	}/**/
}
