package dynagent.ejb.old;
/*package dynagent.ejb;
import java.beans.*;
import java.util.Iterator;
import java.io.StringReader;
import javax.ejb.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

public class ProcessOwningEvent {
	String texto=null;
	int currentProcess=0;
	int processType=0;
	String user=null;
	int transition=0;
	String rol=null;
	int taskType=0;
	int currentTask=0;
	String tranOID=null;
	String taskOID=null;
	long exeDate=0;



	public ProcessOwningEvent() {

	}

	public String toString() {
		String res= "<EVENT"+
			" TASK_TYPE=\""+taskType+ "\""+
			" TASK_TRANSITION=\""+transition+ "\""+
			" USER=\""+user+	"\""+
			" TYPE=\"OWNING\""+
			" CURR_PROCESS=\""+currentProcess+ "\""+
			" PROCESS_TYPE=\""+processType+ "\""+
			" TASK_NAME=\""+taskOID+"\""+
			" TRAN_NAME=\""+tranOID+"\"";

		if(exeDate!=0){
			SimpleDateFormat sdf= new SimpleDateFormat("dd/MM/yy hh:mm");
			java.util.Date dia = new Date(exeDate); 
			res+= " EXE_DATE=\"" + sdf.format( dia ) + "\"";
		}

		if(currentTask!=0)
			res+= " CURR_TASK=\"" + currentTask +"\"";

		res+=	"/>";
		return res;

	}
	public void setExeDate( long fecha ){
		exeDate= fecha;
	}

	public void setTaskType(int s){
		taskType= s;
	}
	public void setProcessType(int s){
		processType= s;
	}
	public void setTaskOID(String OID){
		taskOID= OID;
	}
	public void setTranOID(String OID){
		tranOID= OID;
	}

	public void setCurrentProcess(int proc){
		currentProcess= proc;
	}
	public void setUser(String us){
		user= us;
	}
	public void setTransition(int t){
		transition= t;
	}
	public void setRol(String r){
		rol= r;
	}
	public void setCurrentTask(int st){
		currentTask= st;
	}
	public int getCurrentTask(){
		return currentTask;
	}
	public int getProcessType(){
		return processType;
	}
	public int getTransition(){
		return transition;
	}

	public int getCurrentProcess(){
		return currentProcess;
	}
	public String getUser(){
		return user;
	}
	public int getTaskType(){
		return taskType;
	}
	public String getTaskOID(){
		return taskOID;
	}
	public String getTranOID(){
		return tranOID;
	}

	public String getRol(){
		return rol;
	}
	public String getType(){
		return "TRANSITION";
	}

  private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  public void addPropertyChangeListener(PropertyChangeListener pcl)
  {
    pcs.addPropertyChangeListener(pcl);
  }
  public void removePropertyChangeListener(PropertyChangeListener pcl)
  {
    pcs.removePropertyChangeListener(pcl);
  }


}
*/