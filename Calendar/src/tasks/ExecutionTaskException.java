package tasks;

public class ExecutionTaskException extends Exception{

	private static final long serialVersionUID = 1L;

	public ExecutionTaskException(String subject,Exception ex){
		super(subject);
		this.setStackTrace(ex.getStackTrace());
	}
}
