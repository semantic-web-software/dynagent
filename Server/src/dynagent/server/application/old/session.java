package dynagent.server.application.old;
import java.util.ArrayList;

public class session implements sessionable{
    ArrayList items= new ArrayList();
    private static int maxID=0;
    int id;
    public session(){
        this.id=maxID+1;
        maxID++;
    }
    public int getID(){
        return id;
    }
    public void addSessionable( sessionable item ){
        items.add(item);
    }
    public void rollback(){
        for( int i=items.size()-1;i>=0; i--){
            sessionable ss=(sessionable)items.get(i);
            ss.rollback(this);
            items.remove(i);
        }
    }

    public void rollback(session ses) {
        rollback();
    }

    public boolean exists() {
        return true;
    }

    public boolean hasChanged() {
        return items.size()>0;
    }

    public void delete(session ses) {
    }

    public boolean isNull() {
        return false;
    }
    
    public void commit(){
    	
    }
}
