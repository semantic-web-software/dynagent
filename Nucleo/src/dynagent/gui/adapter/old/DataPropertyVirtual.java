package dynagent.gui.adapter.old;
/**
 * Esta clase extiende a DataProperty, donde añadiremos funcionalidad necesaria <br>
 * para la adaptación.
 * @author Dynagent - David
 */

import java.util.LinkedList;

import dynagent.common.knowledge.access;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.values.DataValue;
import dynagent.common.properties.values.Value;

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
	 * Entero que identifica al grupo padre del que está alojado la property en concreto<br>
	 * sirve para poder crear grupos dentro de otros grupos.
	 */
	private Integer idGroupFather=null;
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
		result=result+"  idGroupFather="+this.getIdGroupFather();
		result=result+"  nameGroup="+this.getNameGroup();
		result=result+">";
		LinkedList<Value> valueList = new LinkedList<Value> ();
		valueList=this.getValues();
		if(valueList.size()>0)
			result=result+"\n   <valueList "+this.ValueListToString(this.getValues())+"\n   </valueList>";
		LinkedList<DataValue> enumList = new LinkedList<DataValue> ();
		enumList=this.getEnumList();
		if(enumList.size()>0)
			result=result+"\n   <enumList "+this.ValueListToString(this.getEnumList())+"\n   </enumList>";
		LinkedList<DataValue> excluList = new LinkedList<DataValue> ();
		excluList=this.getExcluList();
		if(excluList.size()>0)
			result=result+"\n   <excluList "+this.ValueListToString(this.getExcluList())+"\n   </excluList>";
		result=result+"\n</DataPropertyVirtual>";
		/*if(super.getQuantityDetailList().size()>0)
			result=result+"  quantityDetailList="+	super.getQuantityDetailList();*/
		return result;
				
	}
	public DataPropertyVirtual(){
		
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
		
		//this.setQuantityDetailList(p.getQuantityDetailList());

		
		this.setTypeAccess(new access(true,true,true,true,true,true,true,true,true,true));
		this.setValues((LinkedList<Value>)p.getValues().clone());
		this.setValuesFixed(true);

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
	/**
	 * Metodo get sobre el id del grupo padre.
	 * @return Identificador del grupo padre.
	 */
	
	public Integer getIdGroupFather() {
		return idGroupFather;
	}
	/**
	 * Metodo set sobre el id del grupo padre.
	 * @param idGroupFather Identificador del grupo padre.
	 */
	public void setIdGroupFather(Integer idGroupFather) {
		this.idGroupFather = idGroupFather;
	}
	
	public DataPropertyVirtual clone(){
		DataPropertyVirtual result=(DataPropertyVirtual) super.clone();
		result.idGroup=this.idGroup;
		result.idGroupFather=this.idGroupFather;
		result.nameGroup=this.nameGroup;
		result.required=this.required;
		return result;
	}
	public DataProperty toDataProperty() {
		// TODO Auto-generated method stub
		DataProperty dp=new DataProperty();
		dp.setCardMax(this.getCardMax());
		dp.setCardMin(this.getCardMin());
		dp.setDataType(this.getDataType());
		dp.setEnumList(this.getEnumList());
		dp.setExcluList(this.getExcluList());
		dp.setIdo(this.getIdo());
		dp.setIdProp(this.getIdProp());
		dp.setIdto(this.getIdto());
		dp.setLength(this.getLength());
		dp.setMask(this.getMask());
		dp.setName(this.getName());
		dp.setTypeAccess(this.getTypeAccess());
		dp.setValues(this.getValues());
		dp.setValuesFixed(false);
		return dp;
	}
	
	
	
}
