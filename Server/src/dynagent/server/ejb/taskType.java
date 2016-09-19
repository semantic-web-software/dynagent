
package dynagent.server.ejb;

import java.util.HashMap;
import java.util.Iterator;

public class taskType extends Object{

	public String label;
	public int taskID;
	public int stateStart;
	public int taposAtState;
        HashMap states= new HashMap();

	public taskType( String label, int id, int st, int taposAtState ){
		this.label=label;
		taskID= id;
		stateStart= st;
		this.taposAtState=taposAtState;
	}

        public void addState( int id, boolean lock){
            states.put( new Integer(id), new taskState( taskID, id, lock ));
        }

        public boolean isStateLock(int state){
            Integer st=new Integer(state);
            if(!states.containsKey(st)) return false;
            else return ((taskState)states.get(st)).lock;
        }

        public Iterator getStates(){
            return states.values().iterator();
        }
        public int getStateStart(){
            return stateStart;
        }
        public Object clone(){
            taskType tt= new taskType( label, taskID, stateStart, taposAtState );
            Iterator itr=states.keySet().iterator();
            while(itr.hasNext()){
                Integer id=(Integer)itr.next();
                taskState ts=(taskState)states.get(id);
                tt.addState(id.intValue(),ts.lock);
            }
            return tt;
        }
}
