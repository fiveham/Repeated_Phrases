package repeatedphrases;

import java.util.function.Consumer;

import common.IO;

public class LinksAndTrail {

    public static void main(String[] args){
        linksAndTrail(args, IO.DEFAULT_MSG);
    }

    public static void linksAndTrail(String[] args, Consumer<String> msg) {

        int limit = IsolateChaptersAndLink.validateArgs(args, msg);
        String[] trailArgs = new String[]{ args[0] };

        msg.accept("Determining links to add to phrases");
        DetermineAnchors.determineAnchors( trailArgs, msg );

        msg.accept("Adding links to html chapters");
        LinkChapters.linkChapters( new String[]{ Integer.toString(limit) }, msg );

        msg.accept("Adding prev- and next-chapter links");
        SetTrail.setTrail( trailArgs, msg );
    }
}
