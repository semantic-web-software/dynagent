package dynagent.ruleengine.test.test20.src;
/**
 * Esta clase extiende a DataProperty, donde añadiremos funcionalidad necesaria <br>
 * para la adaptación.
 * @author Dynagent - David
 */
import java.util.LinkedList;

import dynagent.ruleengine.meta.api.DataProperty;
import dynagent.ruleengine.meta.api.ObjectValue;

public class DataPropertyVirtual extends DataProperty{
	/**
	 * Booleano que indica si la property es opcional o no.
	 */
	private boolean required=true;
	/**
	 * Entero por el cual se va a agrupar las properties. El valor 0 <br>
	 * indica el grupo principal, no implica la creación de un cuadro en el <br>
	 * intefaz gráfico. 
	 */
	private Integer idGroup=0;
	/**
	 * Cadena que le pone nombre al grupo para poder ponerselo la label posteriormente <br>
	 * en la interfaz gráfica, por defecto es "PRINCIPAL".
	 */
	private String nameGroup="PRINCIPAL";
	/**
	 * Devuelve el nombre del grupo al que esta asociado la property.
	 * @return Nombre del grupo
	 */
	public String getNameGroup() {
		return nameGroup;
	}
	/**
	 * Asigna el nombre del grupo a la property.
	 * @param nameGroup Nombre del grupo.
	 */
	public void setNameGroup(String nameGroup) {
		this.nameGroup = nameGroup;
	}
	/**
	 * Metodo toString()
	 */
	public String toString(){
		String result="<DataPropertyVirtual";
		result=result+"  ido="+super.getIdo();
		result=result+"  idto="+super.getIdto();
		result=result+"  idProp="+super.getIdProp();
		result=result+"  name="+super.getName();
		result=result+"  cardMin="+super.getCardMin();
		result=result+"  cardMax="+super.getCardMax();
	
		result=result+"  typeAcces="+super.getTypeAccess();
		result=result+"  length="+this.getLength();
		result=result+"  datatype="+this.getDataType();
		result=result+"  required="+this.isRequired();
		result=result+"  idGroup="+this.getIdGroup();
		result=result+"  nameGroup="+this.getNameGroup();
		result=result+">";
		LinkedList<ObjectValue> valueList = new LinkedList<ObjectValue> ();
		
		result=result+"\n</DataPropertyVirtual>";
		
		return result;
				
	}
	/**
	 * Crea una nueva DataPropertyVirtual a partir de una DataProperty, asignandole<br>
	 * los valores de la DataProperty
	 * @param p DataProperty con los datos.
	 */
	public DataPropertyVirtual(DataProperty p){
		this.setCardMax(p.getCardMax());
		this.setCardMin(p.getCardMin());
	
		this.setDataType(p.getDataType());
		this.setEnumList(p.getEnumList());
		this.setExcluList(p.getExcluList());
		this.setIdo(p.getIdo());
	
		this.setIdProp(p.getIdProp());
		this.setIdto(p.getIdto());
		this.setLength(p.getLength());
		this.setName(p.getName());
		this.setTypeAccess(p.getTypeAccess());
		this.setValues(p.getValues());
	}
	/**
	 * Metodo que devuelve si es requerido o no.
	 * @return booleano que indica si es requerido o no.
	 */
	public boolean isRequired() {
		return required;
	}
	/**
	 * Metodo que asigna a la property el atributo requerido.
	 * @param required booleano (true o false).
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}
	/**
	 * Devuleve el identificador del grupo.
	 * @return identificador de grupo.
	 */
	public Integer getIdGroup() {
		return idGroup;
	}
	/**
	 * Asigna un identificador de grupo a la property.
	 * @param idGroup Identificador de grupo.
	 */
	public void setIdGroup(Integer idGroup) {
		this.idGroup = idGroup;
	}
}
