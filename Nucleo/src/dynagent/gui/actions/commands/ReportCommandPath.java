package dynagent.gui.actions.commands;

import java.util.HashMap;
import java.util.HashSet;

import dynagent.common.properties.Property;
import dynagent.common.sessions.Session;

public class ReportCommandPath extends commandPath {

	private Property property;
	private HashMap<Integer,Integer> values;
	
	public ReportCommandPath(Property property, HashMap<Integer,Integer> values, Integer idtoUserTask, Integer userRol, Session session){
		super(userRol,idtoUserTask,session);
		this.property=property;
		this.values=values;
	}

	public Property getProperty() {
		return property;
	}

	public void setProperty(Property property) {
		this.property = property;
	}

	public HashMap<Integer,Integer> getValues() {
		return values;
	}

	public void setValues(HashMap<Integer,Integer> values) {
		this.values = values;
	}
	
	public String toString(){
		return "(ReportCommand property:"+property+" values:"+values+" idtoUserTask:"+getIdtoUserTask()+")";
	}

}
