package dynagent.gui.forms.utils;

public class SelectedTreeLeaf {

	private String path;
	private String namesPath;
	private Integer order;

	public SelectedTreeLeaf(String path, String namesPath){
		this.setPath(path);
		this.setNamesPath(namesPath);
	}

	public SelectedTreeLeaf(String path, String namesPath, int order) {
		this(path, namesPath);
		this.order=order;
	}

	public void setNamesPath(String namesPath) {
		this.namesPath = namesPath;
	}

	public String getNamesPath() {
		return namesPath;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}
	
	@Override
	public String toString(){
		return namesPath;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public Integer getOrder() {
		return order;
	}

}
