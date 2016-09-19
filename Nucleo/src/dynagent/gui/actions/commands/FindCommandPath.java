package dynagent.gui.actions.commands;

import dynagent.common.properties.Property;
import dynagent.common.sessions.Session;

public class FindCommandPath extends commandPath{
	private int value;
	private Property property;
	
	public FindCommandPath(Property property,int value,Integer idtoUserTask, Integer userRol, Session session){
		super(userRol,idtoUserTask,session);
		this.property=property;
    	this.value=value;
    }

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public Property getProperty() {
		return property;
	}

	public void setProperty(Property property) {
		this.property = property;
	}
	
	public String toString(){
		return "(FindCommand property:"+property+" value:"+value+" idtoUserTask:"+getIdtoUserTask()+")";
	}
}
