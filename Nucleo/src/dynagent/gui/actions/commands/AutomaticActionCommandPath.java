package dynagent.gui.actions.commands;

import java.util.ArrayList;
import java.util.HashMap;

import dynagent.common.knowledge.IExecuteActionListener;
import dynagent.common.properties.Domain;
import dynagent.common.sessions.Session;

public class AutomaticActionCommandPath extends commandPath {
	private int idoUserTask;
	private ArrayList<Domain> sources;
	private HashMap<Integer, Object> mapParamValue;
	private IExecuteActionListener executeActionListener;
	private String rdn;

	public AutomaticActionCommandPath(int idoUserTask, String rdn, ArrayList<Domain> sources, HashMap<Integer,Object> mapParamValue, IExecuteActionListener executeActionListener, Integer idtoUserTask, Integer userRol, Session session) {
		super(userRol, idtoUserTask, session);
		this.idoUserTask=idoUserTask;
		this.sources=sources;
		this.mapParamValue=mapParamValue;
		this.executeActionListener=executeActionListener;
		this.rdn=rdn;
	}
	
	public int getIdoUserTask() {
		return idoUserTask;
	}

	public void setIdoUserTask(int idoUserTask) {
		this.idoUserTask = idoUserTask;
	}

	public ArrayList<Domain> getSources() {
		return sources;
	}

	public void setSources(ArrayList<Domain> sources) {
		this.sources = sources;
	}

	public HashMap<Integer, Object> getMapParamValue() {
		return mapParamValue;
	}

	public void setMapParamValue(HashMap<Integer, Object> mapParamValue) {
		this.mapParamValue = mapParamValue;
	}

	public IExecuteActionListener getExecuteActionListener() {
		return executeActionListener;
	}

	public void setExecuteActionListener(IExecuteActionListener executeActionListener) {
		this.executeActionListener = executeActionListener;
	}
	
	public String getRdn() {
		return rdn;
	}

	public void setRdn(String rdn) {
		this.rdn = rdn;
	}
}
