/*package dynagent.knowledge.instance;

import dynagent.application.*;

import java.util.ArrayList;
import java.io.Serializable;
import java.util.Iterator;

class sessionableNotSerializable{
    //Esta clase mantiene la parte que no quiero sea serializable (datos de sesiones)
    private ArrayList sessAction = new ArrayList();
    sessionableNotSerializable() {;}
    void addAction( action act ){
        sessAction.add(act);
    }
    action getLastAction(){
        if(sessAction.size()==0) return null;
        return (action)sessAction.get(sessAction.size() - 1);
    }
    action getFirstAction(){
        if(sessAction.size()==0) return null;
        return (action)sessAction.get(0);
    }
    public Iterator getActionIterator(){
        return sessAction.iterator();
    }
    boolean hasActions(){
        return sessAction.size()>0;
    }
    boolean moreThanOneAction(){
        return sessAction.size()>1;
    }
    public void rollback(session ses) {
        //System.out.println("SESROLLBACK "+sessAction.size() + ", currValue "+getValue());
        for (int i = sessAction.size() - 1; i >= 0; i--) {
            System.out.println("SESSION "+i);
            action act = (action) sessAction.get(i);
            if (act.getSession().getID() == ses.getID()) {
                for (int j = sessAction.size() - 1; j >= i; j--) {
                    System.out.println("SESSION DEL "+j);
                    sessAction.remove(j);
                }
                return;
            }
        }
    }

    public void clone( sessionableNotSerializable out ){
        for (int i = 0; i<sessAction.size() ; i++) {
            action act = (action) sessAction.get(i);
            out.addAction( (action)act.clone() );
        }
    }

    void delete(session ses) {
        sessAction.add(new action(ses, dynagent.application.action.DEL, null));
    }
 }


public abstract class defaultSessionable extends sessionableNotSerializable implements Serializable, sessionable  {
     El objeto de currValue es almacenar el valor asigando por la última session, es decir el valor al momento actual (un rolback
     podria modificarlo). Y su finalidad es permitir la serialización de este unico valor, omitiendo todos los valores de otras sesiones.
      Para ello defaultSessionable no implementa el interfaz Serializable, eso hace que el gestor de la serialización no serialize los
      valores de sessiones, pero como attribute si es serializable si se copia su field currValue. Para que funcione es necesario que
     getValue() devuelva currValue y que messageParser cree una primera session

    Object currValue = null;
    int currOperation = 0;

    public void saveActionPerformed(session ses) {
        addAction(new action(ses, dynagent.application.action.SAVE, null));
        updateValue();
    }

    defaultSessionable() {
        ;
    }

    abstract void notifyChange();

    public defaultSessionable(session ses, int operation, Object val) {
        addAction(ses, operation, val);
    }

    void addAction(session ses, int operation, Object val) {
        super.addAction(new action(ses, operation, val));
        if (ses != null)
            ses.addSessionable(this);
        updateValue();
        notifyChange();
    }

    void addAction(action act) {
        super.addAction(act);
        System.out.println("DEFSESS addAction. Op " + getOperation()+"("+instance.buildAtChanged(getOperation()) +
                           ") Class "+ getClass().toString() + ","+ act);
        updateValue();
        System.out.println("DEFSESS NEW OP "+getOperation()+"("+instance.buildAtChanged(getOperation()) +")");
    }

    public Object getValue() {
        return currValue;
    }

    public boolean isNull(){
        return currOperation==dynagent.application.action.NULL;
    }

    void updateValue() {
        action act = getLastAction();
        if( act!=null )
            currValue= act.getValue();
        updateOperation();
    }

    public int getOperation(){
        return currOperation;
    }

    void updateOperation(){
        //currOperation SOLO AFECTA A LA SERIALIZAción (NO AFECTA A LA FUNCION EXISTS()
        int op= currOperation!=0 ? currOperation:dynagent.application.action.GET;
        Iterator itr= getActionIterator();
        while(itr.hasNext()){
            action act=(action)itr.next();
            switch(act.getAction()){
            case dynagent.application.action.LOAD:
                op= dynagent.application.action.GET;
                break;
            case dynagent.application.action.NEW:
                op= dynagent.application.action.NEW;
                break;
            case action.SET:
                if( op==dynagent.application.action.NEW )
                    continue;
                else
                    op=dynagent.application.action.SET;
                break;
            case dynagent.application.action.DEL:
                if( op==dynagent.application.action.NEW )
                    op=dynagent.application.action.NULL;
                else op=dynagent.application.action.DEL;
                break;
            case dynagent.application.action.SAVE:
                if( op!=dynagent.application.action.NEW )
                    op=dynagent.application.action.SET;
                break;
            }
        }
        currOperation= op;
    }

    public boolean exists() {
        if( !hasActions() ) return true;
        action act = getLastAction();
        if( act.getAction()==dynagent.application.action.NULL ) return false;
        return act.getAction() != dynagent.application.action.DEL;
    }

    public boolean exists(boolean checkAllSession) {
        if(!checkAllSession) return exists();
        return currOperation!=dynagent.application.action.DEL;
    }

    public void rollback( session ses ){
        super.rollback(ses);
        updateValue();
        notifyChange();
    }
    public boolean justCreated(){
        if(getOperation()!=dynagent.application.action.NEW) return false;
        Iterator itr= getActionIterator();
        while(itr.hasNext()){
            action act=(action)itr.next();
            if(act.getAction()==dynagent.application.action.SAVE)
                return true;
        }
        return false;
    }
    public boolean hasChanged(){
        if( getOperation()==dynagent.application.action.GET ||
            getOperation()==dynagent.application.action.NULL)
            return false;
        if( getOperation()==dynagent.application.action.SET && moreThanOneAction()){
            Object old= getFirstAction().getValue();
            Object newv=getLastAction().getValue();
            if ( old!=null && newv!=null && old.equals(newv))
                return false;
        }
        return true;
    }

    public boolean databaseReflection() {
        action act = (action) sessAction.get(0);
        if(act.getAction()!= action.LOAD){
            for( int i=0;i< sessAction.size();i++){
                act=(action)sessAction.get(i);
                if(act.getAction()==action.SAVE)
                    return true;
            }
            return false;
        }else
            return true;
    }

    public void delete(session ses) {
        super.delete(ses);
        updateValue();
    }

}
*/