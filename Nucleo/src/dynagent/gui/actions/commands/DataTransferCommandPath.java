package dynagent.gui.actions.commands;

import dynagent.common.sessions.Session;

public class DataTransferCommandPath extends commandPath{
	private int idto;
	private int idoProgram;
	private int idtoProgram;
	
	public DataTransferCommandPath(int idto, int idoProgram, int idtoProgram, Integer userRol, Session session){
		super(userRol,null,session);
		this.idto=idto;
		this.idoProgram=idoProgram;
		this.idtoProgram=idtoProgram;
    }

	public int getIdto() {
		return idto;
	}

	public void setIdto(int idto) {
		this.idto = idto;
	}
	
	public String toString(){
		return "(DataTransferCommand idto:"+idto+")";
	}

	public int getIdoProgram() {
		return idoProgram;
	}

	public void setIdoProgram(int idoProgram) {
		this.idoProgram = idoProgram;
	}

	public int getIdtoProgram() {
		return idtoProgram;
	}

	public void setIdtoProgram(int idtoProgram) {
		this.idtoProgram = idtoProgram;
	}
}
