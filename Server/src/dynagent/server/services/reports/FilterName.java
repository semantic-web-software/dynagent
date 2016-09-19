package dynagent.server.services.reports;

import java.io.File;
import java.io.FilenameFilter;

public class FilterName implements FilenameFilter{

	private String name;
	public FilterName(String name){
		this.name=name.toLowerCase();
	}
	public boolean accept(File f, String filter) {
		filter=filter.toLowerCase();
		return filter.equals(name+".jrxml"); 
	}

}
