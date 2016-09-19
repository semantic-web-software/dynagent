package dynagent.ejb.old;
/*package dynagent.ejb;
import java.beans.*;
import java.util.Iterator;
import java.io.StringReader;
import javax.ejb.*;

public class ProcessTransitionEvent {
	String texto=null;
	int processId=0;
	int object=0;
	String user=null;
	int transition=0;
	String rol=null;
	int task=0;
	int current_task=0;

	public ProcessTransitionEvent() {

	}

	public String toString(){
		String res= "<EVENT"+
			" CURR_TASK=\""+current_task+ "\""+
			" TASK_TRANSITION=\""+transition+ "\""+
			" USER=\""+user+	"\""+
			" TYPE=\"TRANSITION\""+
			" CURR_PROCESS=\""+processId+ "\"";

		if(object!=0)
			res+= " OBJECT=\"" + object +"\"";
		if(current_task!=0)
			res+= " CURR_TASK=\"" + current_task +"\"";

		res+=	"/>";
		return res;
	}

	public void setTask(int s){
		task= s;
	}
	public void setProcessId(int proc){
		processId= proc;
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
	public void setObject(int obj){
		object= obj;
	}
	public void setCurrent_task(int st){
		current_task= st;
	}
	public int getCurrent_task(){
		return current_task;
	}
	public int getObject(){
		return object;
	}
	public int getTransition(){
		return transition;
	}

	public int getProcessId(){
		return processId;
	}
	public String getUser(){
		return user;
	}
	public int getTask(){
		return task;
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