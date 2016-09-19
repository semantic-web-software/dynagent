package dynagent.common.sessions;

import java.util.ArrayList;
import java.util.HashMap;

import dynagent.common.properties.Domain;

public class ExecuteActionRequest {
	
	private int idtoUserTask;
	private HashMap<String, Object> mapParamValue;
	private ArrayList<Domain> sources;
	private String rdn;

	public ExecuteActionRequest(int idtoUserTask, String rdn, ArrayList<Domain> sources, HashMap<String, Object> mapParamValue) {
		super();
		this.setIdtoUserTask(idtoUserTask);
		this.setMapParamValue(mapParamValue);	
		this.setSources(sources);
		this.setRdn(rdn);
	}

	public void setIdtoUserTask(int idtoUserTask) {
		this.idtoUserTask = idtoUserTask;
	}

	public int getIdtoUserTask() {
		return idtoUserTask;
	}

	public void setMapParamValue(HashMap<String, Object> mapParamValue) {
		this.mapParamValue = mapParamValue;
	}

	public HashMap<String, Object> getMapParamValue() {
		return mapParamValue;
	}

	public void setSources(ArrayList<Domain> sources) {
		this.sources = sources;
	}

	public ArrayList<Domain> getSources() {
		return sources;
	}

	public String getRdn() {
		return rdn;
	}

	public void setRdn(String rdn) {
		this.rdn = rdn;
	}	
	
}
