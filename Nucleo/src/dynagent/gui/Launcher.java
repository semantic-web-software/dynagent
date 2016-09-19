package dynagent.gui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;


public class Launcher {
	public static void main(String[] args) throws Exception {
		System.err.println("Launcher...");
	    startSecondJVM(dynaApplet.class, false, args);
	    System.out.println("Main");
	  }
	
	  public static void startSecondJVM(Class<? extends Object> clazz, boolean redirectStream, String[] args) throws Exception {
	    System.out.println(clazz.getCanonicalName());
	    String separator = System.getProperty("file.separator");
	    String classpath = System.getProperty("java.class.path");
	    String path = System.getProperty("java.home")
	            + separator + "bin" + separator + "javaws";
	    
	    ArrayList<String> memory=new ArrayList<String>();
	    memory.add("2950");
	    memory.add("750");
	    memory.add("450");
	    boolean memoryError;
	    int i=0;
	    do{
	    	memoryError=false;
		    ArrayList<String> list=new ArrayList<String>();
//		    list.add(path);
//		    if(memory.size()>i){//Ponemos memoria, y si no no la ponemos para que java le asigne la que considere oportuna (ocurre en la ultima iteracion)
//		    	list.add("-Xmx"+memory.get(i)+"m");
//		    }
//		    list.add("-cp");
//		    list.add(classpath);
//		    list.add(clazz.getCanonicalName());
//		    list.addAll(Arrays.asList(args));
		    
		    list.add(path);
		    list.add("http://dynagent.dnsalias.com:8088/dyna/webstart-"+memory.get(0)+".jnlp");
		    
		    ProcessBuilder processBuilder = new ProcessBuilder(list);
		    processBuilder.redirectErrorStream(redirectStream);
		    Process process = processBuilder.start();
		    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String line;
			
			while ((line = errorReader.readLine()) != null){
				System.err.println(line);
				/*if(error==null){
					error=line;
				}else{
					error+="\n"+line;
				}*/
				if(line.equalsIgnoreCase("Could not create the Java virtual machine.")){
					memoryError=true;
				}
			}
			errorReader.close();

		    process.waitFor();
		    System.out.println("Fin memoryError:"+memoryError+" con codigo"+process.exitValue()+" para memoria "+(memory.size()>i?memory.get(i):"Default"));
		    i++;
	    }while(memoryError && memory.size()>=i);
	  }
}