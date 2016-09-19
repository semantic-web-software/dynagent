package dynagent.server.services.reports;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;

public class EditReports {

	public static void edit(Map<String,String> jrxmls, String path) throws JRException, IOException, InterruptedException{
		editWidthiReport(jrxmls, path);
	}
	
	private static void editWidthiReport(Map<String, String> jrxmls, String command) throws IOException, InterruptedException {
		Collection<String> jrxmlsFiles = jrxmls.values();
		Runtime runtime= Runtime.getRuntime();
		
		String wd = command.replaceFirst("iReport.exe", "");
		File workD=new File(wd);
		workD.deleteOnExit();
		
		
		Iterator<String> it=jrxmlsFiles.iterator();
		while(it.hasNext()){
			command=command.concat(" \""+it.next()+"\"");
		}
		System.out.println("command:"+command);
				
		Process proc = runtime.exec(command,null,workD);
		proc.waitFor();
		
	}
}
