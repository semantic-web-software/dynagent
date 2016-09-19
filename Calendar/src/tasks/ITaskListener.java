package tasks;


public interface ITaskListener {

	//public void updateTasks(selectData tasks);
	public void updateTasks(int idoUserTask,String labelUserTask,String status,String asignDate,String ejecutionDate);
}
