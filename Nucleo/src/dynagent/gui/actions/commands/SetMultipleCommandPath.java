package dynagent.gui.actions.commands;

import java.util.HashMap;
import java.util.HashSet;

import dynagent.common.properties.ObjectProperty;
import dynagent.common.sessions.Session;

public class SetMultipleCommandPath extends commandPath {
	
	private HashMap<Integer,Integer> values;
	private ObjectProperty property;
	
	public SetMultipleCommandPath(ObjectProperty property, HashMap<Integer,Integer> values, Integer idtoUserTask, Integer userRol, Session session){
		super(userRol,idtoUserTask,session);
		this.values=values;
		this.property=property;
    }
	
	public HashMap<Integer,Integer> getValues() {
		return values;
	}

	public void setValues(HashMap<Integer,Integer> values) {
		this.values = values;
	}

	public ObjectProperty getProperty() {
		return property;
	}

	public void setProperty(ObjectProperty property) {
		this.property = property;
	}	
	
	public String toString(){
		return "(SetMultipleCommand property:"+property+" values:"+values+" idtoUserTask:"+getIdtoUserTask()+")";
	}
}