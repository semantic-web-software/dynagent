package dynagent.ruleengine.test.test20.src;
/**
 * Esta clase extiende a ObjectProperty, donde añadiremos funcionalidad necesaria <br>
 * para la adaptación.
 * @author Dynagent - David
 */
import java.util.LinkedList;

import dynagent.ruleengine.meta.api.ObjectProperty;
import dynagent.ruleengine.meta.api.ObjectValue;
import dynagent.ruleengine.meta.api.QuantityDetail;
import dynagent.ruleengine.meta.api.Value;

public class ObjectPropertyVirtual extends ObjectProperty{
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
	 * Crea una nueva ObjectPropertyVirtual a partir de una ObjectProperty, asignandole<br>
	 * los valores de la ObjectProperty
	 * @param p ObjectProperty con los datos.
	 */
	public ObjectPropertyVirtual(ObjectProperty p){
		this.setName(p.getName());
		this.setIdProp(p.getIdProp());
		this.setIdo(p.getIdo());
		this.setIdto(p.getIdto());
		this.setCardMin(p.getCardMin());
		this.setCardMax(p.getCardMax());
		this.setTypeAccess(p.getTypeAccess());
		LinkedList<QuantityDetail> QuantityDetailListClone = new LinkedList<QuantityDetail>();
		QuantityDetailListClone=p.getQuantityDetailList();
		this.setQuantityDetailList(QuantityDetailListClone);
		LinkedList<Integer> rangoListClone = new LinkedList<Integer>(); 
		rangoListClone=p.getRangoList();
		this.setRangoList(rangoListClone);
		LinkedList<ObjectValue> filterListClone = new LinkedList<ObjectValue>(); 
		
		LinkedList<Value> valueListClone = new LinkedList<Value> ();
		valueListClone=p.getValues();
		this.setValues(valueListClone);
		LinkedList<ObjectValue> enumListClone = new LinkedList<ObjectValue> ();
		enumListClone=p.getEnumList();
		this.setEnumList(enumListClone);
		LinkedList<ObjectValue> excluListClone = new LinkedList<ObjectValue> ();
		excluListClone=p.getExcluList();
		this.setExcluList(excluListClone);
	}
	/**
	 * Constructor vacio.
	 *
	 */
	public ObjectPropertyVirtual() {
		
	}
	
	/**
	 * Metodo que clona la ObjectPropertyVirtual
	 * @return ObjectPropertyVirtual
	 */
	public ObjectPropertyVirtual clone() {
		ObjectPropertyVirtual p = new ObjectPropertyVirtual();
		p.setName(getName());
		p.setIdProp(getIdProp());
		p.setIdo(getIdo());
		p.setIdto(getIdto());
		p.setCardMin(getCardMin());
		p.setCardMax(getCardMax());
		p.setTypeAccess(getTypeAccess());

		LinkedList<QuantityDetail> QuantityDetailListClone = new LinkedList<QuantityDetail>();
		QuantityDetailListClone=this.getQuantityDetailList();
		p.setQuantityDetailList(QuantityDetailListClone);
		LinkedList<Integer> rangoListClone = new LinkedList<Integer>(); 
		rangoListClone=this.getRangoList();
		p.setRangoList(rangoListClone);
		LinkedList<ObjectValue> filterListClone = new LinkedList<ObjectValue>(); 
		
		LinkedList<Value> valueListClone = new LinkedList<Value> ();
		valueListClone=this.getValues();
		p.setValues(valueListClone);
		LinkedList<ObjectValue> enumListClone = new LinkedList<ObjectValue> ();
		enumListClone=this.getEnumList();
		p.setEnumList(enumListClone);
		LinkedList<ObjectValue> excluListClone = new LinkedList<ObjectValue> ();
		excluListClone=this.getExcluList();
		p.setExcluList(excluListClone);
		p.setRequired(required);
		return p;
	}
	/**
	 * Metodo toString()
	 */
	public String toString(){
		String result="<ObjectPropertyVirtual";
		result=result+"  name="+super.getName();
		result=result+"  ido="+super.getIdo();
		result=result+"  idto="+super.getIdto();
		result=result+"  idProp="+super.getIdProp();
		result=result+"  cardMin="+super.getCardMin();
		result=result+"  cardMax="+super.getCardMax();
		result=result+"  typeAccess="+super.getTypeAccess();
		result=result+"  rangoList="+this.getRangoList().toString();
		result=result+"  required="+this.isRequired();
		result=result+"  idGroup="+this.getIdGroup();
		result=result+"  nameGroup="+this.getNameGroup();
		result=result+">";
		LinkedList<ObjectValue> valueList = new LinkedList<ObjectValue> ();
		
		if(super.getQuantityDetailList().size()>0)
			result=result+"\n   <quantityDetailList>\n    "+this.quantityDetailListToString(this.getQuantityDetailList())+"\n   </quantityDetailList>";
		LinkedList<ObjectValue> filterList = new LinkedList<ObjectValue> ();
	
		
		result=result+"\n</ObjectPropertyVirtual>";
		
		return result;
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
}
