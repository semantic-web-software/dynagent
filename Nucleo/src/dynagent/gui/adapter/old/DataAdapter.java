package dynagent.gui.adapter.old;
/**
 * Esta clase contiene toda la información que agrupa una clase virtual,<br>
 * es decir, todas las properties de las clases y relaciones que se han <br>
 * incluido a la hora de agrupar.<br><br>
 * 
 *  @author Dynagent - David
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import dynagent.common.properties.Property;

public class DataAdapter {
	/**
	 * Identificador del ido virtual de la clase virtual donde agrupamos.
	 */
	private Integer idoTemp;
	/**
	 * Identificador del idto virtual de la clase virtual donde agrupamos.
	 */
	private Integer idtoTemp;
	/**
	 * Mapa el cual contiene todas las properties con el fin de poder desadaptar<br>
	 * en cualquier momento.
	 */
	private Map<Property,InfoOriginal> propertyAdapter= new HashMap<Property,InfoOriginal>();
	/**
	 * Constructor de clase, al cual se le pasan los parametros ido e idto del individuo virtual.<br><br>
	 * @param idoTemp Identificador del individuo virtual.
	 * @param idtoTemp Idto del individuo virtual.
	 */
	
	public DataAdapter(Integer idoTemp, Integer idtoTemp){
		this.idoTemp=idoTemp;
		this.idtoTemp=idtoTemp;
	}
	/**
	 * Método para insertar una property virtual dentro del individuo virtual.<br>
	 * Para ello necesitamos el idProp, idoClass e idtoClass original, para poder<br>
	 * construir un InfoOriginal con estos datos, esto nos sirve para tener la referencia<br>
	 * de que property es en realidad la property virtual.<br><br>
	 * 
	 * @param idPropTemp Property Virtual que vamos a almacenar.
	 * @param idProp Entero que identifica a la property original.
	 * @param idoClass Identificador del individuo original al que pertenece la property original.
	 * @param idtoClass Identificador de la clase a la que pertenece dicho individuo original.
	 */
	public void setPropertyAdapter(Property idPropTemp,Integer idProp,Integer idoClass, Integer idtoClass){
		InfoOriginal info= new InfoOriginal(idProp,idoClass,idtoClass);
		propertyAdapter.put(idPropTemp, info);
	}
	/**
	 * Método con el que a través de una property virtual, obtenemos el identificador del <br>
	 * individuo original.<br><br>
	 * @param idPropTemp Property virtual.
	 * @return Entero identificando al individuo original.
	 */
	public Integer getOriginalIdoClass(Property idPropTemp){
		Integer result=null;
		InfoOriginal info=propertyAdapter.get(idPropTemp);
		result=info.getIdoClass();
		return result;
	}
	/**
	 * Método con el que a través de una property virtual, obtenemos el identificador de <br>
	 * la clase a la que pertenece el individuo original.<br><br>
	 * @param idPropTemp Property virtual.
	 * @return Entero identificando a la clase a la que pertenece el individuo original.
	 */
	public Integer getOriginalIdtoClass(Property idPropTemp){
		Integer result=null;
		InfoOriginal info=propertyAdapter.get(idPropTemp);
		result=info.getIdtoClass();
		return result;
	}
	/**
	 * Método con el que a través de una property virtual, obtenemos el identificador de <br>
	 * la property original.<br><br>
	 * @param idPropTemp Property virtual.
	 * @return Entero identificando a la property original.
	 */
	public Integer getOriginalIdProp(Property idPropTemp){
		Integer result=null;
		InfoOriginal info=propertyAdapter.get(idPropTemp);
		result=info.getIdProp();
		return result;
	}
	/**
	 * Método de consulta. <br>Sirve para consultar el ido del individuo virtual en el que
	 * estamos almacenando.<br><br>
	 * @return Identificador del individuo virtual.
	 */
	public Integer getIdoTemp() {
		return idoTemp;
	}
	/**
	 * Método que devuleve todas las properties que hay almacenada en un individuo virtual.<br><br>
	 * @return Iterador de proeperties con todas las properties.
	 */
	public Iterator<Property> getPropertyIterator(){
		Set<Property> properties= propertyAdapter.keySet();
		Iterator <Property> it=properties.iterator();
		return it;
	}
	/**
	 * Método que devuelve una property virtual en concreto.<br>
	 * Le pasamos el identificado de property virtual, llamamos a {@link #getPropertyIterator()}<br>
	 * y iteramos buscando dicha property
	 * @param idProp Identificador de la property virtual.
	 * @return Property virtual.
	 */
	public Property getProperty(Integer idProp){
		Iterator <Property> it=this.getPropertyIterator();
		Property p=null;
		while(it.hasNext()){
			p=it.next();
			if(p.getIdProp().equals(idProp)){
				return p;
			}
		}
		if(p==null){
	
			System.err.println("ERROR PROPERTY NULA");
		}
		if(p.getIdProp().equals(idProp)){
			return p;
		}else{
			return null;
		}
	}
	/**
	 * Método set sobre el ido virtual del individuo.
	 * @param idoTemp Ido Virtual
	 */
	public void setIdoTemp(Integer idoTemp) {
		this.idoTemp = idoTemp;
	}
	/**
	 * Método set sobre el idto virtual del individuo.
	 * @param idoTemp Idto Virtual
	 */
	public void setIdtoTemp(Integer idtoTemp) {
		this.idtoTemp = idtoTemp;
	}
	/**
	 * Método get sobre el idto virtual del individuo.
	 * @return Idto Virtual
	 */
	
	public Integer getIdtoTemp() {
		return idtoTemp;
	}
	/**
	 * Método que elimina una property virtual de una agrupación.
	 * @param p Property Virtual.
	 * @return Objeto InfoOriginal, si existia.
	 */
	public InfoOriginal deleteProperty(Property p){
		return propertyAdapter.remove(p);
	}
	public String toString(){
		String result;
		result="=====IDO VIRUTUAL: "+this.idoTemp+" =====IDTO VIRTUAL: "+this.idtoTemp+" ==\n\n";
		result+="PROPERTIES\n";
		Iterator<Property> it =this.getPropertyIterator();
		while(it.hasNext()){
			Property p =it.next();
			result+=p.toString()+"\n\n";
			
		}
		return result;
	}
	public void changeProperty(Integer idProp,Property pr){
		Set<Property> pset=propertyAdapter.keySet();
		Iterator<Property> itp=pset.iterator();
		while(itp.hasNext()){
			Property p=itp.next();
			if(p.getIdProp().equals(idProp)){
				InfoOriginal inf=propertyAdapter.get(p);
				deleteProperty(p);
				propertyAdapter.put(pr, inf);
			}
		}
	}
	public void setType(Property idPropTemp,String type){
		InfoOriginal inf=propertyAdapter.get(idPropTemp);
		inf.setType(type);
	}
	public String getType (Property p){
		InfoOriginal inf=propertyAdapter.get(p);
		return inf.getType();
	}
	public Iterator<DataPropertyVirtual> getAllDataPropertyVirtual() {
	
		ArrayList<DataPropertyVirtual> adp=new ArrayList<DataPropertyVirtual>();
		Iterator<Property> itp=getPropertyIterator();
		
		while (itp.hasNext()){
			Property p= itp.next();
			if (p instanceof DataPropertyVirtual){
				DataPropertyVirtual dp= (DataPropertyVirtual)p;
				adp.add(dp);
			}
		}
		return adp.iterator();
	}
	
	public Iterator<ObjectPropertyVirtual> getAllObjectPropertyVirtual() {
	
		ArrayList<ObjectPropertyVirtual> aop=new ArrayList<ObjectPropertyVirtual>();
		Iterator<Property> itp=getPropertyIterator();
		
		while (itp.hasNext()){
			Property p= itp.next();
			if (p instanceof ObjectPropertyVirtual){
				ObjectPropertyVirtual op= (ObjectPropertyVirtual)p;
				aop.add(op);
			}
		}
		return aop.iterator();
	}
}
