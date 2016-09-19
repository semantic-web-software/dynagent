package dynagent.common.communication;

import org.jdom.Element;

/**
 * Esta clase se encarga del flujo de mensajes.
 * <br>Existen métodos para acceder o modificar el proceso acutal, tipo de tarea, tarea actual, 
 * estado de la tarea, tarea padre y el tiempo de ejecución.
 */

public class flowAction extends message{

   //int actionType=0;

    private int processType = 0;
    private int taskType = 0;
    private int currProcess = 0;
    private int currTask = 0;
    private int currTaskTrans = 0;
    private int currTaskState = 0;
    private long exeDate = 0;
    private int currParentTask = 0;

    public Object clone(){
        flowAction fl= new flowAction(getType());
        cloneData(fl);
        cloneFlowData(fl);
        return fl;
    }

    public flowAction(){
        super(message.MSG_FLOW);
    }

    public flowAction( int type ){
        super(type);
    }

    public boolean equals(Object obB){
        if( !(obB instanceof flowAction) || !super.equals(obB))
            return false;
        flowAction flB=(flowAction)obB;

        return  taskType==flB.getTaskType() &&
                currTask==flB.getCurrTask() &&
                currTaskState==flB.getCurrTaskState();
    }

    void cloneFlowData(flowAction act){
        act.setProcessType(processType);
        act.setTaskType(taskType);
        act.setCurrProcess(currProcess);
        act.setCurrTask(currTask);
        act.setCurrTaskTrans(currTaskTrans);
        act.setCurrTaskState(currTaskState);
        act.setExeDate(exeDate);
    }

    public int getProcessType() {
        return processType;
    }

    public int getTaskType() {
        return taskType;
    }

    public int getCurrProcess() {
        return currProcess;
    }

    public int getCurrTask() {
        return currTask;
    }

    public int getCurrTaskTrans() {
        return currTaskTrans;
    }

    public int getCurrTaskState() {
        return currTaskState;
    }

    public long getExeDate() {
        return exeDate;
    }

    public int getCurrParentTask() {
        return currParentTask;
    }

    public void setProcessType(int processType) {
        this.processType = processType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public void setCurrProcess(int currProcess) {
        this.currProcess = currProcess;
    }

    public void setCurrTask(int currTask) {
        this.currTask = currTask;
    }

    public void setCurrTaskTrans(int currTaskTrans) {
        this.currTaskTrans = currTaskTrans;
    }

    public void setCurrTaskState(int currTaskState) {
        this.currTaskState = currTaskState;
    }

    public void setExeDate(long exeDate) {
        this.exeDate = exeDate;
    }

    public void setCurrParentTask(int currParentTask) {
        this.currParentTask = currParentTask;
    }

    void toElementHeader(Element root){
        super.toElementHeader(root);
        if( currParentTask!=0 )
            root.setAttribute("PARENT_TASK",String.valueOf(currParentTask));
        if( currProcess!=0 )
            root.setAttribute("CURR_PROCESS",String.valueOf(currProcess));
        if( currTask!=0 )
            root.setAttribute("CURR_TASK",String.valueOf(currTask));
        if(processType!=0)
            root.setAttribute("PROCESS_TYPE",String.valueOf(processType));
        if(taskType!=0)
            root.setAttribute("TASK_TYPE",String.valueOf(taskType));
        root.setAttribute("BNS",String.valueOf(getBusiness()));
        if(exeDate!=0)
            root.setAttribute("EXE_DATE",String.valueOf(exeDate));
        if(currTaskTrans!=0)
            root.setAttribute("TASK_TRANSITION",String.valueOf(currTaskTrans));
        if(currTaskState!=0)
            root.setAttribute("TASK_STATE",String.valueOf(currTaskState));
    }

    void toElementContent( Element root ){
        super.toElementContent(root);
        if( getType()==message.MSG_EXE_TRAN_ACTION && content!=null ){
            Element eContent = root.getChild("CONTENT");
            if (eContent == null) {
                eContent = new Element("CONTENT");
                root.addContent(eContent);
            }
        }
    }
}
