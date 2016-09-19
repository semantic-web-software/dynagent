package dynagent.gui.actions.commands;

import java.util.ArrayList;

import dynagent.common.sessions.Session;

public class SetCommandPath extends commandPath {
	
	private int value;
	private int valueCls;
	private int ido;
	private int idto;
	
	public SetCommandPath(int ido,int idto,int value, int valueCls,Integer idtoUserTask, Integer userRol, Session session){
		super(userRol,idtoUserTask,session);
		this.value=value;
		this.valueCls=valueCls;
		this.ido=ido;
		this.idto=idto;
    }
	
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getIdo() {
		return ido;
	}

	public void setIdo(int ido) {
		this.ido = ido;
	}
	
	public String toString(){
		return "(SetCommand ido:"+ido+" idto:"+idto+" value:"+value+" valueCls:"+valueCls+" idtoUserTask:"+getIdtoUserTask()+")";
	}

	public int getIdto() {
		return idto;
	}

	public void setIdto(int idto) {
		this.idto = idto;
	}

	public int getValueCls() {
		return valueCls;
	}

	public void setValueCls(int valueCls) {
		this.valueCls = valueCls;
	}
}
