package dynagent.ruleengine;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConceptLogger {
		static private ConceptLogger logger = null;
		String nameLogFile=null;

	public static ConceptLogger getLogger(){
        if(logger == null)
        { 	   	  			
    								
            return new ConceptLogger();
        }
        else
            return logger;
    }
	
	public static ConceptLogger getLogger(String namelogFile){
        if(logger == null)
            return new ConceptLogger(namelogFile);
        else
            return logger;
    }
    
	private ConceptLogger(){	
	
	}
	public void cleanFile()
	{
		if(this.nameLogFile == null)
			try {
				FileWriter f = new FileWriter("Concepts.log");
			} catch (IOException e) {
				e.printStackTrace();
			}
			else
			try {
				FileWriter f = new FileWriter(this.nameLogFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
					
	}
	
	private ConceptLogger(String namelogFile){	
		this.nameLogFile=namelogFile;
	}
	
	
	
	public String getNameLogFile() {
		return nameLogFile;
	}

	public static void setLogger(ConceptLogger logger) {
		ConceptLogger.logger = logger;
	}

	public void setNameLogFile(String nameLogFile) {
		this.nameLogFile = nameLogFile;
	}

	public void write(String msg){
        RandomAccessFile miRAFile;
		try {
			if(nameLogFile==null)
				miRAFile = new RandomAccessFile( "concepts.log","rw" );
			else
				miRAFile = new RandomAccessFile( this.getNameLogFile(),"rw" );
			miRAFile.seek( miRAFile.length() );
			Date d = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHmmss");
	        miRAFile.writeBytes("["+format.format(d)+"]:"+msg+"\n");
	        miRAFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	public void writeln(String msg){
        RandomAccessFile miRAFile;
		try {
			if(nameLogFile==null)
				miRAFile = new RandomAccessFile( "concepts.log","rw" );
			else
				miRAFile = new RandomAccessFile( this.getNameLogFile(),"rw" );
			miRAFile.seek( miRAFile.length() );
			Date d = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHmmss");
	       // miRAFile.writeBytes("["+format.format(d)+"]:"+msg+"\n");
			miRAFile.writeBytes("\n"+msg);
			
	        miRAFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
