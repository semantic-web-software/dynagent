package dynagent.gui.actions.commands;

import dynagent.common.sessions.Session;

public class NewRelCommandPath extends commandPath {
	
	private int ido;
	private int idto;
	private int value;
	private int idProp;
	private int level;
	
	public NewRelCommandPath(int ido, int idto, int idProp, int value, int level, Integer idtoUserTask, Integer userRol, Session session){
		super(userRol,idtoUserTask,session);
		this.ido=ido;
		this.idto=idto;
		this.idProp=idProp;
		this.value=value;
		this.level=level;
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

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	public String toString(){
		return "(NewRelCommand ido:"+ido+" idProp:"+idProp+" value:"+value+" idtoUserTask:"+getIdtoUserTask()+")";
	}

	public int getIdto() {
		return idto;
	}

	public void setIdto(int idto) {
		this.idto = idto;
	}
}
