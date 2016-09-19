package dynagent.common.communication;

import org.jdom.Element;


/**
 * Esta clase se encarga de acceder o modificar el contexto de las tareas, la tarea padre, el
 * estado anterior, ...
 */

public class threadData extends flowAction{

    private int threadContextTO = 0;
    private String threadContextRDN = null;
    private int currParentTask = 0;
    private int oldState = 0;

    public threadData(){;}

    public threadData( flowAction fa ){
        fa.cloneData(this);
        fa.cloneFlowData(this);
    }

    public int getThreadContextTO() {
        return threadContextTO;
    }

    public String getThreadContextRDN() {
        return threadContextRDN;
    }

    public int getCurrParentTask() {
        return currParentTask;
    }

    public int getOldState() {
        return oldState;
    }

    public void setThreadContextTO(int threadContextTO) {
        this.threadContextTO = threadContextTO;
    }

    public void setThreadContextRDN(String threadContextRDN) {
        this.threadContextRDN = threadContextRDN;
    }

    public void setCurrParentTask(int currParentTask) {
        this.currParentTask = currParentTask;
    }

    public void setOldState(int oldState) {
        this.oldState = oldState;
    }
    void toElementHeader(Element root){
        super.toElementHeader(root);
        if(oldState!=0)
            root.setAttribute("OLD_TASK_STATE",String.valueOf(oldState));
    }

    void toElementContent( Element root ){
        super.toElementContent(root);
    }
}
