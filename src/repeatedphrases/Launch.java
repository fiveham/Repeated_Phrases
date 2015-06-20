package repeatedphrases;

import java.io.*;

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
	
	private static String getJarName(){
		return "repeatedphrases.jar";
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
