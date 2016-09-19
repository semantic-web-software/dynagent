package dynagent.common.knowledge;

public class action extends Object{
    public final static int GET=1;//VIEW
    public final static int NEW=2;
    public final static int SET=4;
//    public final static int SET_CLASS=8;
    public final static int DEL_OBJECT=16;
    public final static int DEL=32;
    public final static int EVOL=64;
    public final static int createINE=128;
    //public final static int NULL=33;
    //Todos los de arriba son de acuerdo a tabla S_OPERATIONS que no debe colisionar con appControl
    //public final static int LOAD=100;
    //public final static int SAVE=101;

    /*session ses;
    int action;
    Object value;
    public action( session ses, int action, Object value ){
        this.ses=ses;
        this.action=action;
        this.value=value;
    }

    public int getAction(){
        return action;
    }
    public session getSession(){
        return ses;
    }
    public Object getValue(){
        return value;
    }
    public Object clone(){
        return new action(ses,action,value);
    }
    public String toString(){
        return "ACTION action "+instance.buildAtChanged(action);
    }*/
}
