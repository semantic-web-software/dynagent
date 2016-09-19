package dynagent.gui.actions.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import dynagent.common.sessions.Session;
import dynagent.common.utils.IndividualValues;

public class QuestionTaskCommandPath extends commandPath{

	private int idto;
	private String rdn;
	private ArrayList<IndividualValues> data;
	
	public QuestionTaskCommandPath(int idto, String rdn, ArrayList<IndividualValues> data, HashMap<String,String> alias, Integer userRol, Integer idtoUserTask, Session session) {
		super(userRol, idtoUserTask, session);
		this.idto=idto;
		this.rdn=rdn;
		this.data=data;
		this.setAlias(alias);
	}

	public int getIdto() {
		return idto;
	}

	public void setIdto(int idto) {
		this.idto = idto;
	}

	public String getRdn() {
		return rdn;
	}

	public void setRdn(String rdn) {
		this.rdn = rdn;
	}

	public ArrayList<IndividualValues> getData() {
		return data;
	}

	public void setData(ArrayList<IndividualValues> data) {
		this.data = data;
	}

}
