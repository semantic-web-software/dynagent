package dynagent.gui.actions.commands;

import dynagent.common.sessions.Session;

public class DelCommandPath extends commandPath {

	private int ido;
	private int idProp;
	private int value;
	
	public DelCommandPath(int ido, int idProp, int value, Integer idtoUserTask, Integer userRol, Session session){
		super(userRol,idtoUserTask,session);
		this.ido=ido;
    	this.idProp=idProp;
    	this.value=value;
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

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public String toString(){
		return "(DelCommand ido:"+ido+" idProp:"+idProp+" value:"+value+" idtoUserTask:"+getIdtoUserTask()+")";
	}
}
