package dynagent.gui.actions.commands;

import java.util.HashMap;

import dynagent.common.sessions.Session;

public class ExportCommandPath extends commandPath {
	
	private int ido;
	private int idto;
	private int valueCls;
	private HashMap<Integer,Integer> values;
	
	public ExportCommandPath(int ido, int idto, HashMap<Integer,Integer> values, Integer idtoUserTask, Integer userRol, Session session){
		super(userRol,idtoUserTask,session);
		this.ido=ido;
		this.idto=idto;
		this.values=values;
    }	

	public int getIdo() {
		return ido;
	}

	public void setIdo(int ido) {
		this.ido = ido;
	}

	public HashMap<Integer,Integer> getValues() {
		return values;
	}

	public void setValues(HashMap<Integer,Integer> values) {
		this.values = values;
	}

	public int getValueCls() {
		return valueCls;
	}

	public void setValueCls(int valueCls) {
		this.valueCls = valueCls;
	}
	
	public String toString(){
		return "(ExportCommand ido:"+ido+" idto:"+idto+" values:"+values+" valueCls:"+valueCls+" idtoUserTask:"+getIdtoUserTask()+")";
	}

	public int getIdto() {
		return idto;
	}

	public void setIdto(int idto) {
		this.idto = idto;
	}

}
