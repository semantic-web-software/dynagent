package dynagent.server.services.reports;

import java.io.File;
import java.io.FilenameFilter;

public class ExtensionFilter implements FilenameFilter{

	private String ext;
	private boolean without;
	public ExtensionFilter(String ext,boolean without){
		this.ext=ext;
		this.without=without;
	}
	public boolean accept(File f, String filter) {
		if(without){
			return !filter.endsWith(ext);
		}else{
			return filter.endsWith(ext);
		}
		 
	}

}
