package repeatedphrases;

import java.io.*;

public class Launch {
	
	/* *
	 * <p>The command-line input equivalent of running this 
	 * class's main method. Launches RepeatedPhrasesUI with 
	 * java heap space specifications.</p>
	 */
	//public static final String COMMAND_LINE_INPUT = "java -Xms200m -Xmx200m RepeatedPhrasesUI";

	public static void main(String[] args) {
		try{
			//String parentDir = new File("..").getAbsolutePath();
			//String workDir = new File(".").getAbsolutePath();
			String jarName = "repeatedphrases.jar";
			
			/*if(workDir.endsWith(".")){
				workDir = workDir.substring(0,workDir.length()-1);
			}
			if(workDir.endsWith(File.separator)){
				workDir = workDir.substring(0,workDir.length()-1);
			}
			int i = workDir.lastIndexOf(File.separator);
			if( i>=0 ){
				workDir = workDir.substring(i+1);
			}*/
			
            Process p = Runtime.getRuntime().exec("java -classpath "+jarName+" -Xmx512m repeatedphrases.RepeatedPhrasesUI");
            //Process p = Runtime.getRuntime().exec("java -classpath "+parentDir+" -Xmx512m "+pwd+".RepeatedPhrasesUI");
            //Process p = Runtime.getRuntime().exec("java RepeatedPhrasesUI");
            
            InputStream stderr = p.getErrorStream();
            //OutputStream stdout = p.getOutputStream();
            InputStream stdout = p.getInputStream();
            
            //BufferedReader buffStderr = new BufferedReader( new InputStreamReader( stderr));
            //BufferedReader buffStdout = new BufferedReader( new InputStreamReader( stdout));
            //BufferedReader buffStdin  = new BufferedReader( new InputStreamReader( stdin));
            
            StreamGobbler errEat = new StreamGobbler(stderr/*, "ERROR"*/);
            StreamGobbler outEat = new StreamGobbler(stdout/*, "OUTPUT"*/);
            
            errEat.start();
            outEat.start();
            
            /*int exitValue = */
            p.waitFor();
            ///System.out.println("Exitvalue: "+exitValue);
            
            
            /*String line = null;
            while( (line = buffStderr.readLine()) != null ){
            	System.out.println(line);
            }*/
            
            
            
            /*BufferedReader in = new BufferedReader(
                                new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }*/
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
	    //private String type;
	    
	    private StreamGobbler(InputStream src/*, String type*/){
	        this.src = src;
	        //this.type = type;
	    }
	    
	    @Override
	    public void run(){
	        try{
	            BufferedReader br = new BufferedReader(new InputStreamReader(src));
	            //String line=null;
	            while ( br.readLine() != null);
	            /*while ( (line = br.readLine()) != null){
	                System.out.println(type + ">" + line);
	            }*/
            } catch (IOException e){
            	//e.printStackTrace();  
	        }
	    }
	}
}
