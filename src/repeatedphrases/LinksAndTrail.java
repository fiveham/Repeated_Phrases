package repeatedphrases;

public class LinksAndTrail {

	public static void main(String[] args) {
		
		int limit = FinalizeChapters.validateArgs(args);
		String[] trailArgs = new String[]{ args[0] };
		
		System.out.println("Determining what links to add to what phrases.");
		DetermineAnchors.main( trailArgs );
		
		System.out.println("Adding links to html chapters.");
		LinkChapters.main( new String[]{ Integer.toString(limit) } );
		
		System.out.println("Adding previous-chapter and next-chapter links to html chapters.");
		SetTrail.main( trailArgs );
	}
}
