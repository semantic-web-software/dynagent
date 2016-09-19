package calendar;

public class Task {
	
	private String name;
	private String status;
	private String asignDate;
	private String executionDate;
	private int idoUserTask;
		
	public Task(String name, String status, String executionDate, String asignDate, int idoUserTask) {
		super();
		this.name = name;
		this.status = status;
		this.idoUserTask = idoUserTask;
		this.asignDate = asignDate;
		this.executionDate = executionDate;
	}

	public String getAsignDate() {
		return asignDate;
	}

	public void setAsignDate(String asignDate) {
		this.asignDate = asignDate;
	}

	public String getExecutionDate() {
		return executionDate;
	}

	public void setExecutionDate(String executionDate) {
		this.executionDate = executionDate;
	}

	public int getIdoUserTask() {
		return idoUserTask;
	}

	public void setIdoUserTask(int idoUserTask) {
		this.idoUserTask = idoUserTask;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
