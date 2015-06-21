package repeatedphrases;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

public class Launch {
	
	public static void main(String[] args) {
		try{
			String jarName = getJarName();
			
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
	 * <p>Tries to determine the name of the jar file from which this class 
	 * was launched. If there's only one jar in the working directory, that 
	 * is assumed to be the correct jar; otherwise, "repeatedphrases.jar" is 
	 * returned.</p>
	 * @return the name of the jar file from which this class was launched
	 */
	private static String getJarName(){
		String[] files = new File(".").list( (dir,name) -> name.endsWith(".jar") );
		return (files.length == 1) ? files[0] : "repeatedphrases.jar";
	}
	
	/**
	 * <p>Continually consumes the out or err of a process 
	 * via an InputStreamReader of its 
	 * {@link Process#getInputStream() input stream} or 
	 * {@link Process#getErrorStream() error stream} respectively.</p>
	 * <p>Based on the StreamGobbler from "When Runtime.exec() won't" 
	 * by Michael C. Daconta, at 
	 * http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2</p>
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
