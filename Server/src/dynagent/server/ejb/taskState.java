package dynagent.server.ejb;

class taskState extends Object{
    int value=0;
    boolean lock=false;
    int task=0;
    taskState( int task, int value, boolean lock ){
        this.value=value;
        this.lock=lock;
        this.task=task;
    }
    public Object clone(){
        return new taskState(task,value,lock);
    }
}
