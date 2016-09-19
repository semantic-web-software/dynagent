package dynagent.ruleengine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RuleEngineLogger {

	static private RuleEngineLogger logger = null;
	
	public static RuleEngineLogger getLogger(){
        if(logger == null)
            return new RuleEngineLogger();
        else
            return logger;
    }
    
	private RuleEngineLogger(){}
	
	
	public void write(String msg){
        RandomAccessFile miRAFile;
		try {
			miRAFile = new RandomAccessFile( "ruleengine.log","rw" );
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
}
