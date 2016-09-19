package dynagent.common.basicobjects;

public class GlobalClases {
	
	private int userTask;
	private Integer idtoRoot;
	private boolean centralized;
	
	public GlobalClases() {}
	
	public GlobalClases(int userTask, Integer idtoRoot, boolean centralized) {
		this.userTask = userTask;
		this.idtoRoot = idtoRoot;
		this.centralized = centralized;
	}

	public int getUserTask() {
		return userTask;
	}
	public void setUserTask(int userTask) {
		this.userTask = userTask;
	}

	public boolean isCentralized() {
		return centralized;
	}
	public void setCentralized(boolean centralized) {
		this.centralized = centralized;
	}

	public Integer getIdtoRoot() {
		return idtoRoot;
	}
	public void setIdtoRoot(Integer idtoRoot) {
		this.idtoRoot = idtoRoot;
	}
	
	public String toString() {
		return "userTask " + userTask + ", idtoRoot " + idtoRoot + ", centralized " + centralized;
	}
}
