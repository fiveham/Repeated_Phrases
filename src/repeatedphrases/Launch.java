package repeatedphrases;

import java.io.*;

public class Launch {
	
	public static void main(String[] args) {
		try{
			String jarName = "repeatedphrases.jar";
			
            Process p = Runtime.getRuntime().exec("java -classpath "+jarName+" -Xmx512m repeatedphrases.RepeatedPhrasesUI");
            
            StreamGobbler errEat = new StreamGobbler(p.getErrorStream());
            StreamGobbler outEat = new StreamGobbler(p.getInputStream());
            
            errEat.start();
            outEat.start();
            
            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch( InterruptedException e){
        	e.printStackTrace();
        }
	}
	
	/**
	 * <p></p>
	 * @author JavaWorld
	 * http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
	 */
	private static class StreamGobbler extends Thread{
	    private InputStream src;
	    
	    private StreamGobbler(InputStream src){
	        this.src = src;
	    }
	    
	    @Override
	    public void run(){
	        try{
	            BufferedReader b = new BufferedReader(new InputStreamReader(src));
	            while ( b.readLine() != null);
            } catch (IOException e){}
	    }
	}
}
