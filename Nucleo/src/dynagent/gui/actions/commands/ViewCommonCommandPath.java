package dynagent.gui.actions.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import dynagent.common.sessions.Session;

public class ViewCommonCommandPath extends commandPath {
	
	private HashMap<Integer,Integer> values;
	private int ido;
	private int valueCls;
	
	public ViewCommonCommandPath(int ido, HashMap<Integer,Integer> values, Integer idtoUserTask, Integer userRol, Session session){
		super(userRol,idtoUserTask,session);
		this.values=values;
		this.ido=ido;
    }
	
	public HashMap<Integer,Integer> getValues() {
		return values;
	}

	public void setValues(HashMap<Integer,Integer> values) {
		this.values = values;
	}

	public int getIdo() {
		return ido;
	}

	public void setIdo(int ido) {
		this.ido = ido;
	}
	
	public String toString(){
		return "(ViewCommonCommand ido:"+ido+" values:"+values+" idtoUserTask:"+getIdtoUserTask()+")";
	}
}