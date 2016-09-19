/*package dynagent.ejb;
import java.beans.*;
import java.util.Iterator;
import java.io.StringReader;
import javax.ejb.EJBException;



public class TransObjectEvent {
	int currentProcess=0;
	int currentTask=0;
	int object=0;
	int objectType=0;
	int context=0;
	int tranAction;
	int taskTransition=0;
	String operationType=null;
	String user=null;
	String texto=null;
	boolean isRootContext=true;
	String taskActionParam1=null;
	String tempProcessId;
	String exeDate=null;
	String rdn=null;

	public TransObjectEvent(int idto,int ido,String rdn,String exeDate,int process,int task,int tran,int tranAction,
					String op,int ctx,boolean isRootCtx,String user,String taskActionP1, String tpi) {
		setObjectType(idto);
		setObject(ido);
		setIsRootContext(isRootCtx);
		setOperationType(op);
		setContext(ctx);
		setCurrentProcess(process);
		setTaskTransition(tran);
		setCurrentTask(task);
		setUser(user);
		setTranAction(tranAction);
		setTaskActionParam1(taskActionP1);
		setExeDate( exeDate );
		setRdn(rdn);
		if(tpi!=null)
			setTempProcessId(tpi);
	}

	public void setObject(int obj){
		object= obj;
	}
	public void setIsRootContext(boolean isR){
		isRootContext= isR;
	}

	public void setTaskTransition(int tran){
		taskTransition=tran;
	}
	public void setContext(int ctx){
		context=ctx;
	}
	public void setOperationType(String op){
		operationType=op;
	}
	public void setObjectType(int to){
		objectType=to;
	}
	public void setTranAction(int act){
		tranAction= act;
	}
	public void setCurrentProcess(int proc){
		currentProcess= proc;
	}

	public void setCurrentTask(int st){
		currentTask= st;
	}
	public void setUser(String user){
		this.user= user;
	}
	public void setExeDate( String fecha ){
		exeDate= fecha;
	}
	public void setTaskActionParam1(String act){
		taskActionParam1= act;
	}
	public void setTempProcessId(String tpi){
		tempProcessId=tpi;
	}
	public void setRdn( String r ){
		rdn=r;
	}
	public int getObjectType(){
		return objectType;
	}
	public String getRdn( ){
		return rdn;
	}
	public String getTempProcessId(){
		return tempProcessId;
	}
	public boolean getIsRootContext(){
		return isRootContext;
	}

	public int getTranAction(){
		return tranAction;
	}
	public String getExeDate( ){
		return exeDate;
	}
	public int getObject(){
		return object;
	}
	public int getTaskTransition(){
		return taskTransition;
	}
	public String getUser(){
		return user;
	}

	public int getCurrentProcess(){
		return currentProcess;
	}
	public int getCurrentTask(){
		return currentTask;
	}

	public int getContext(){
		return context;
	}
	public String getOperationType(){
		return operationType;
	}
	public String getTaskActionParam1(){
		return taskActionParam1;
	}

	public String toString() {		
		String res= "<EVENT"+
			" USER=\""+user+	"\""+
			" TYPE=\"" +operationType+"\""+
			" CURR_PROCESS=\""+currentProcess+ "\""+
			" CURR_TASK=\""+currentTask+ "\""+
			" TASK_TRANSITION=\""+taskTransition+ "\""+
			" CURR_TRAN_ACTION=\""+tranAction+ "\""+
			" ID_O=\""+ object +"\""+
			" CONTEXT=\"" + context + "\"";
		if(!isRootContext)
			res+= " IS_ROOT_CONTEXT=\"FALSE\"";
		if(taskActionParam1!=null)
			res+= " TASK_ACTION_PARAM_1=\""+taskActionParam1+"\"";
		if(tempProcessId!=null)
			res+= " TEMP_PROCESS_ID=\""+tempProcessId+"\"";

		if( exeDate!=null )
			res+= " EXE_DATE=\"" + exeDate +"\"";

		if( rdn!=null )
			res+= " RDN=\"" + rdn +"\"";
		if( objectType!=0 )
			res+= " ID_TO=\"" + objectType +"\"";

		res+=	"/>";
		return res;
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