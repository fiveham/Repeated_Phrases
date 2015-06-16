package repeatedphrases;

public class LinksAndTrail {

	public static void main(String[] args) {
		
		int limit = FinalizeChapters.validateArgs(args);
		String[] trailArgs = new String[]{ args[0] };
		String[] noargs = {};
		
		System.out.println("Determining what links to add to what phrases and where those links lead.");
		DetermineAnchors.main( trailArgs );
		
		System.out.println("Adding links to html chapters");
		LinkChapters.main( limit==FinalizeChapters.NO_LIMIT ? noargs : new String[]{ Integer.toString(limit) } );
		
		System.out.println("Adding previous-chapter and next-chapter links to html chapters.");
		SetTrail.main( trailArgs );
	}
}
