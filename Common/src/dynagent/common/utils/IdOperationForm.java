package dynagent.common.utils;

public class IdOperationForm {

	/*Integer idUserTask;*/
	/*Integer target;*/
	private IdObjectForm target;
	private Integer operationType;
	private Integer buttonType;
	
	private static final String SEPARATOR="@";
	
	public IdOperationForm() {
		this("null"+SEPARATOR+"null"+SEPARATOR+"null");
	}
	
	public IdOperationForm(String id) {
		String[] buf = id.split(SEPARATOR);
		
		target = !buf[0].equals("null") ? new IdObjectForm(buf[0]): null;
		/*idUserTask = !buf[0].equals("null") ? Integer.parseInt(buf[0]): null;*/
		/*target = !buf[1].equals("null") ? Integer.parseInt(buf[1]): null;*/
		operationType = !buf[1].equals("null") ? Integer.parseInt(buf[1]): null;
		buttonType = !buf[2].equals("null") ? Integer.parseInt(buf[2]): null;
	}

	public static boolean matchFormat(String id){
		return (id.split(SEPARATOR).length==3?true:false);
	}
	
	public Integer getButtonType() {
		return buttonType;
	}

	public void setButtonType(Integer buttonType) {
		this.buttonType = buttonType;
	}

	/*public Integer getIdUserTask() {
		return idUserTask;
	}

	public void setIdUserTask(Integer idUserTask) {
		this.idUserTask = idUserTask;
	}
*/
	public Integer getOperationType() {
		return operationType;
	}

	public void setOperationType(Integer operationType) {
		this.operationType = operationType;
	}

	/*public Integer getTarget() {
		return target;
	}

	public void setTarget(Integer target) {
		this.target = target;
	}*/
	public IdObjectForm getTarget() {
		return target;
	}

	public void setTarget(IdObjectForm target) {
		this.target = target;
	}
	
	public String getIdString(){
		return /*idUserTask+":"+*/(target!=null?target.getIdString():null)+SEPARATOR+operationType+SEPARATOR+buttonType;
	}
}
