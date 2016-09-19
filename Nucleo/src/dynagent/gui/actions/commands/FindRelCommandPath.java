package dynagent.gui.actions.commands;

import dynagent.common.knowledge.instance;
import dynagent.common.sessions.Session;

public class FindRelCommandPath extends commandPath {
	
	private int ido;
	private int idProp;
	private int valueCls;
	private instance instance;
	
	// Si es facil meterle el filtro(value) desde el motor quizas deberia darmelo y asi no tener que preguntar yo x el
	public FindRelCommandPath(int ido,int idProp, int valueCls,Integer idtoUserTask, Integer userRol, Session session, instance filter){
		super(userRol,idtoUserTask,session);
		this.ido=ido;
    	this.idProp=idProp;
    	this.valueCls=valueCls;
    	this.instance=filter;
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

	public instance getInstance() {
		return instance;
	}

	public void setInstance(instance instance) {
		this.instance = instance;
	}
	
	public String toString(){
		return "(FindRelCommand ido:"+ido+" idProp:"+idProp+" valueCls:"+valueCls+" idtoUserTask:"+getIdtoUserTask()+")";
	}
}
