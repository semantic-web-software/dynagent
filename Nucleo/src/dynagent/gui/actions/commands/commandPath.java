package dynagent.gui.actions.commands;

import java.util.ArrayList;
import java.util.HashMap;

import dynagent.common.sessions.Session;

public class commandPath {
	
	private Integer idtoUserTask;
	private Integer userRol;
	private Session session;
	private HashMap<String,String> alias;
	
	protected commandPath(Integer userRol,Integer idtoUserTask,Session session){
		this.userRol=userRol;
		this.idtoUserTask=idtoUserTask;
		this.session=session;		
	}
	
	public Integer getIdtoUserTask() {
		return idtoUserTask;
	}
	
	public void setIdtoUserTask(Integer idtoUserTask) {
		this.idtoUserTask = idtoUserTask;
	}
	
	public Integer getUserRol() {
		return userRol;
	}
	
	public void setUserRol(Integer userRol) {
		this.userRol = userRol;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session sessionParent) {
		this.session = sessionParent;
	}   
	
	public void setAlias(HashMap<String,String> alias) {
		this.alias=alias;
	}
	
	public HashMap<String,String> getAlias(){
		return this.alias;
	}
}
