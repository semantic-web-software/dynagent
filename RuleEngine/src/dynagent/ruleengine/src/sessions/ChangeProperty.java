package dynagent.ruleengine.src.sessions;

import java.io.Serializable;

import dynagent.common.properties.values.Value;
import dynagent.common.sessions.IChangePropertyListener;

public class ChangeProperty implements IChangePropertyListener{

	private Integer ido;
	private int idto;
	private int idProp;
	private int valueCls;
	private Value value;
	private Value oldValue;
	private int operation;
	private int level;

	public void initChangeValue() {
		// TODO Auto-generated method stub
		
	}
	
	public void changeValue(Integer ido, int idto, int idProp, int valueCls, Value value, Value oldValue, int level, int operation) {
		this.ido=ido;
		this.idto=idto;
		this.idProp=idProp;
		this.valueCls=valueCls;
		this.value=value;
		this.oldValue=oldValue;
		this.operation=operation;
		this.level=level;
	}
	
	public void endChangeValue() {
		// TODO Auto-generated method stub
		
	}

	public Integer getIdo() {
		return ido;
	}

	public int getIdProp() {
		return idProp;
	}

	public int getIdto() {
		return idto;
	}

	public Value getOldValue() {
		return oldValue;
	}

	public int getOperation() {
		return operation;
	}

	public Value getValue() {
		return value;
	}

	public int getValueCls() {
		return valueCls;
	}

	public int getLevel() {
		return level;
	}

	@Override
	public String toString() {
		return "ido:"+ido+" idto:"+idto+" idProp:"+idProp+ "valueCls:"+valueCls+" value:"+value+" oldValue:"+oldValue+" level:"+level+" operation:"+operation;
	}
	
}
