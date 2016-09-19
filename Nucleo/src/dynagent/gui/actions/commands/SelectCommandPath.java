package dynagent.gui.actions.commands;

import java.util.HashMap;
import java.util.HashSet;

import dynagent.common.properties.ObjectProperty;
import dynagent.common.sessions.Session;

public class SelectCommandPath extends commandPath{

	private ObjectProperty property;
	private HashMap<Integer,Integer> rows;
	
	public SelectCommandPath(ObjectProperty property,HashMap<Integer,Integer> rows,Integer idtoUserTask, Integer userRol, Session session){
		super(userRol,idtoUserTask,session);
		this.property=property;
		this.rows=rows;
	}

	public ObjectProperty getProperty() {
		return property;
	}

	public void setProperty(ObjectProperty property) {
		this.property = property;
	}

	public HashMap<Integer,Integer> getRows() {
		return rows;
	}

	public void setRows(HashMap<Integer,Integer> rows) {
		this.rows = rows;
	}
	
	public String toString(){
		return "(SelectCommand property:"+property+" rows:"+rows+" idtoUserTask:"+getIdtoUserTask()+")";
	}

}
