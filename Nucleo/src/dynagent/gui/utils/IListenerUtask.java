package dynagent.gui.utils;

import java.util.Iterator;

import dynagent.common.basicobjects.ListenerUtask;

public interface IListenerUtask {

	public boolean isListener(int idtoUserTask);
	
	public Iterator<Integer> getListenerUtasks();
	
	public ListenerUtask getListenerUtask(int idtoUserTask);
}
