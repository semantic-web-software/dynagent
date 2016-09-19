package dynagent.common.basicobjects;

import java.io.File;

public class SubReport {
	private String name;
	private File file;
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	private boolean generateExcel;
	private String comments;
	
	public SubReport(){
		generateExcel=false;
	}
	
	public SubReport(String name, File file, boolean generateExcel, String comments) {
		super();
		this.name = name;
		this.file = file;
		this.generateExcel = generateExcel;
		this.comments = comments;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public boolean isGenerateExcel() {
		return generateExcel;
	}
	public void setGenerateExcel(boolean generateExcel) {
		this.generateExcel = generateExcel;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
