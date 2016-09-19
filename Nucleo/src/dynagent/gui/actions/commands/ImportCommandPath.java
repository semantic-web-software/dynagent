package dynagent.gui.actions.commands;


import dynagent.common.sessions.Session;

public class ImportCommandPath extends commandPath {
	
	private int ido;
	private int idto;
	
	public ImportCommandPath(int ido, int idto, Integer idtoUserTask, Integer userRol, Session session){
		super(userRol,idtoUserTask,session);
		this.ido=ido;
		this.idto=idto;
    }	

	public int getIdo() {
		return ido;
	}

	public void setIdo(int ido) {
		this.ido = ido;
	}

	public String toString(){
		return "(ImportCommand ido:"+ido+" idto:"+idto+" idtoUserTask:"+getIdtoUserTask()+")";
	}

	public int getIdto() {
		return idto;
	}

	public void setIdto(int idto) {
		this.idto = idto;
	}

}
