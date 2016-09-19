package dynagent.gui.actions.commands;

import dynagent.common.sessions.Session;

public class NewCommandPath extends commandPath {
	
	private int ido;
	private int idto;
	private int value;
	private int idProp;
	
	public NewCommandPath(int ido, int idto, int idProp, int value, Integer idtoUserTask, Integer userRol, Session session){
		super(userRol,idtoUserTask,session);
		this.ido=ido;
		this.idto=idto;
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
		return "(NewCommand ido:"+ido+" idto:"+idto+" idProp:"+idProp+" value:"+value+" idtoUserTask:"+getIdtoUserTask()+")";
	}

	public int getIdto() {
		return idto;
	}

	public void setIdto(int idto) {
		this.idto = idto;
	}

}
