package dynagent.gui.actions.commands;

import dynagent.common.sessions.Session;
import dynagent.gui.actions.commands.commandPath;

public class RelCommandPath extends commandPath {

	private int ido;
	private int idProp;
	private int value;
	private int valueCls;
	
	public RelCommandPath(int ido,int idProp,int value,int valueCls,Integer idtoUserTask, Integer userRol,Session session){
		super(userRol,idtoUserTask,session);
		this.ido=ido;
    	this.idProp=idProp;
    	this.value=value;
    	this.valueCls=valueCls;
    }
	
	public int getIdo() {
		return ido;
	}

	public void setIdo(int ido) {
		this.ido = ido;
	}

	public int getIdProp() {
		return idProp;
	}

	public void setIdProp(int idProp) {
		this.idProp = idProp;
	}

	public int getValueCls() {
		return valueCls;
	}

	public void setValueCls(int valueCls) {
		this.valueCls = valueCls;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public String toString(){
		return "(RelCommand ido:"+ido+" idProp:"+idProp+" value:"+value+" valueCls:"+valueCls+" idtoUserTask:"+getIdtoUserTask()+")";
	}
}
