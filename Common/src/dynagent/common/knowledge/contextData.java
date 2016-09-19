package dynagent.common.knowledge;

import org.jdom.Element;

public class contextData extends instance {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int currTask;
    int idCtx=0;
    int idFilter = 0;
    private int currProcess = 0;
    private int idDom=0;
    private int taskType = 0;
    private int transition = 0;
    private int taskState = 0;


    public contextData(  int type, int id, int idCtx, int idFilter, int currTask){
        super(type,id);
        this.currTask=currTask;
        this.idCtx = idCtx;
        this.idFilter = idFilter;
    }
    public int getCurrentTask(){
        return currTask;
    }
    /*public Object clone(int operation){
        contextData res= new contextData(getType(),getIDO(),idCtx,idFilter,currTask,operation);
        subClone(res,false);
        return res;
    }*/

    public instance extractChanges(){
        contextData res= new contextData(getIdTo(),getIDO(),idCtx,idFilter,currTask);
        subClone(res, true);
        return res;
    }

    void subClone(contextData res, boolean checkDiferentialExistence ){
        super.clone();
        //super.subClone(res,checkDiferentialExistence);
        res.setCurrProcess(getCurrProcess());
    }

    public int getContextID() {
        return idCtx;
    }
    public int getIdFilter() {
        return idFilter;
    }

    public int getCurrProcess() {
        return currProcess;
    }

    public void setCurrProcess(int currPro) {
        currProcess=currPro;
    }

    public int getIdDom() {
        return idDom;
    }

    public int getTaskType() {
        return taskType;
    }

    public int getTransition() {
        return transition;
    }

    public int getTaskState() {
        return taskState;
    }

    public void setIdFilter(int id) {
        idFilter = id;
    }


    public void setIdDom(int idDom) {
        this.idDom = idDom;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public void setTransition(int transition) {
        this.transition = transition;
    }

    public void setTaskState(int taskState) {
        this.taskState = taskState;
    }

    public void setIdContext(int id) {
        idCtx = id;
    }

    public Element toElement(){
        Element res= super.toElement();
        res.setAttribute("CURR_TASK",String.valueOf(currTask));
        res.setAttribute("ID_CONTEXT",String.valueOf(idCtx));
        res.setAttribute("ID_FILTER",String.valueOf(idFilter));
        res.setAttribute("CURR_PROCESS",String.valueOf(currProcess));
        res.setAttribute("ID_DOM",String.valueOf(idDom));
        res.setAttribute("TASK_TYPE",String.valueOf(taskType));
        res.setAttribute("TASK_TRANSITION",String.valueOf(transition));
        res.setAttribute("TASK_STATE",String.valueOf(taskState));
        return res;
    }
}
