package dynagent.gui.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import dynagent.common.basicobjects.ListenerUtask;

public class ListenerMenu implements IListenerUtask{

	private HashMap<Integer, ListenerUtask> hmIDTOxLU= new HashMap<Integer, ListenerUtask>();
	
	public ListenerMenu(ArrayList<ListenerUtask> lu) {
		Iterator<ListenerUtask> itr=lu.iterator();
		while(itr.hasNext()){
			ListenerUtask l=itr.next();
			//System.err.println("listenerMenu:"+l.getUtask());
			hmIDTOxLU.put(l.getUtask(),l);
		}
	}

	public boolean isListener(int idtoUserTask) {
		return hmIDTOxLU.get(idtoUserTask)!=null;
	}

	public Iterator<Integer> getListenerUtasks() {
		//System.err.println(hmIDTOxLU.keySet());
		return hmIDTOxLU.keySet().iterator();
	}
	
	public ListenerUtask getListenerUtask(int idtoUserTask){
		return hmIDTOxLU.get(idtoUserTask);
	}

}
