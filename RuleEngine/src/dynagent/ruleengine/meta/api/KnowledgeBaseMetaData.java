/***
 * KnowledgeBaseMetaData.java
 * @author Ildefonso Montero rez - monteroperez@us.es
 * @description It represents an extension of KnowledgeBase for represent metaData information, 
 * 				because this is independient of OWL file and we must store all the information.
 */

package dynagent.ruleengine.meta.api;

import java.util.LinkedList;

import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.Properties;
import dynagent.common.basicobjects.T_Herencias;

public class KnowledgeBaseMetaData {

	LinkedList<Instance> instancias = new LinkedList<Instance>();
	LinkedList<Properties> propiedades = new LinkedList<Properties>();
	LinkedList<T_Herencias> padres = new LinkedList<T_Herencias>();
	
	
	public LinkedList<Instance> getInstancias() {
		return instancias;
	}
	public void setInstancias(LinkedList<Instance> instancias) {
		this.instancias = instancias;
	}
	public LinkedList<T_Herencias> getPadres() {
		return padres;
	}
	public void setPadres(LinkedList<T_Herencias> padres) {
		this.padres = padres;
	}
	public LinkedList<Properties> getPropiedades() {
		return propiedades;
	}
	public void setPropiedades(LinkedList<Properties> propiedades) {
		this.propiedades = propiedades;
	}
	
}
