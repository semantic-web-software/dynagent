package dynagent.ejbengine.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;

import dynagent.common.Constants;
import dynagent.ejbengine.engine.EngineObject;
import dynagent.server.dbmap.ClassInfo;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.PropertyInfo;
import dynagent.server.services.XMLConstants;

/**
 * Clase para procesar XML de datos.
 */
public abstract class XMLProcessor {

	/**
	 * Rellena los nodos de referencia con los atributos del nodo de datos al
	 * que se está referenciando.
	 * 
	 * @param document
	 *            Documento sin procesar, tal y como se obtiene al hacer el GET
	 * @throws DataConversionException
	 *             Si se intenta convertir el valor de un atributo a un tipo de
	 *             datos no compatible.
	 */
	@SuppressWarnings("unchecked")
	public static void fillRefNodes(Document document) throws DataConversionException{
		Map<Integer, Element> elementsWithData = new Hashtable<Integer, Element>();
		preProcessXML(document, elementsWithData);
		Element rootElement = document.getRootElement();
		Element objectsElement = rootElement.getChild(XMLConstants.TAG_OBJECTS);
		List<Element> elements = /*new ArrayList<Element>(*/objectsElement.getChildren()/*)*/;
		for (Element element : elements) {
			fillRefNodesRec(element, elementsWithData);
		}
	}

	/**
	 * Analiza recursivamente los elementos del XML en busca de nodos vínculo
	 * para añadirles los atributos que representan properties del nodo con
	 * datos al que apuntan.
	 * 
	 * @param element
	 *            Elemento a analizar.
	 * @param elementsWithData
	 *            Mapa de los nodos con datos indexados por su id_node
	 * @throws DataConversionException
	 *             Si se intenta convertir el valor de un atributo a un formato
	 *             no compatible.
	 */
	@SuppressWarnings("unchecked")
	private static void fillRefNodesRec(Element element, Map<Integer, Element> elementsWithData) throws DataConversionException {
		Attribute refNodeAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_REFNODE);
		if (refNodeAttribute != null){
			// Es un nodo vínculo al que le tenemos que añadir los atributos.
			Integer refNode = refNodeAttribute.getIntValue();
			Element elementWithData = elementsWithData.get(refNode);
			copyAttributes(elementWithData, element);
		}else{
			Attribute idNodeAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDNODE);
			if (idNodeAttribute == null){
				// No se trata de un nodo que represente a un objeto de una
				// clase.
				return;
			}
			// Conseguimos los nodos hijos del nodo que representa al objeto y
			// los analizamos en busca de nodos vínculo.
			List<Element> children = /*new ArrayList<Element>(*/element.getChildren()/*)*/;
			for (Element childElement : children) {
				fillRefNodesRec(childElement, elementsWithData);
			}
		}
	}

	/**
	 * Copia los atributos que no son de control desde el nodo con datos al nodo
	 * vínculo.
	 * 
	 * @param elementWithData
	 *            Elemento que contiene los datos que queremos copiar.
	 * @param element
	 *            Elemento al que queremos copiar los datos.
	 */
	@SuppressWarnings("unchecked")
	private static void copyAttributes(Element elementWithData, Element element) {
		List<Attribute> attributes = elementWithData.getAttributes();
		for (Attribute attribute : attributes) {
			String attributeName = attribute.getName();
			if (! attributeName.equals(XMLConstants.ATTRIBUTE_ACTION) &&
					! attributeName.equals(XMLConstants.ATTRIBUTE_DESTINATIONm) &&
					! attributeName.equals(XMLConstants.ATTRIBUTE_IDNODE) &&
					! attributeName.equals(XMLConstants.ATTRIBUTE_PROPERTYm)){
				element.setAttribute(new Attribute(attributeName, attribute.getValue()));
			}
		}
	}

	/**
	 * Método que se encarga preprocesar el XML de datos para para rellenar los
	 * mapas con la información necesaria para poder trabajar con el mismo.
	 * 
	 * @param document
	 *            Documento con los datos original.
	 * @param elementsWithData
	 *            Mapa donde insertan los elementos que contienen datos,
	 *            referenciados por su id_node.
	 * @throws DataConversionException
	 *             Si se intenta convertir el valor de un atributo a un tipo de
	 *             datos no compatible.
	 */
	@SuppressWarnings("unchecked")
	private static void preProcessXML(Document document, Map<Integer, Element> elementsWithData) throws DataConversionException {
		Element rootElement = document.getRootElement();
		Element objectsElement = rootElement.getChild(XMLConstants.TAG_OBJECTS);
		List<Element> children = objectsElement.getChildren();
		for (Element childElement : children) {
			preProcessChild(childElement, elementsWithData);
		}
	}

	/**
	 * Preprocesa un nodo del documento de datos.<br>
	 * Este método es recursivo, si el nodo tiene hijos, se analizarán también
	 * tal y como se analizará el nodo entrante.
	 * 
	 * @param element
	 *            Elemento a procesar.
	 * @param elementsWithData
	 *            Mapa donde insertan los elementos que contienen datos,
	 *            referenciados por su id_node.
	 * @throws DataConversionException
	 *             Si se intenta convertir el valor de un atributo a un tipo de
	 *             datos no compatible.
	 */
	@SuppressWarnings("unchecked")
	private static void preProcessChild(Element element, Map<Integer, Element> elementsWithData) throws DataConversionException {
		Attribute idNodeAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDNODE);
		if (idNodeAttribute == null){
			// No es un nodo de datos, por lo que no es necesario analizarlo.
			return;
		}
		// Añadimos el nodo al mapa de elementos con datos.
		Integer idNode = idNodeAttribute.getIntValue();
		elementsWithData.put(idNode, element);
		// Comprobamos si tiene hijos para analizarlos también.
		List<Element> children = element.getChildren();
		for (Element childElement : children) {
			preProcessChild(childElement, elementsWithData);
		}
	}

	/**
	 * Genera un documento XML a partir de los objetos dados.
	 * 
	 * @param ido
	 *            Identificador del objeto raíz.
	 * @param objectsByIdo
	 *            Mapa de todos los objetos que necesitamos para construir el
	 *            XML indexados por el ido de los objetos.
	 * @param dataBaseMap
	 *            Mapa del modelo y de la base de datos.
	 */
	public static Document getDocument(int ido, Map<Integer, EngineObject> objectsByIdo, DataBaseMap dataBaseMap) {
		EngineObject mainObject = objectsByIdo.get(ido);
		Element mainElement = convertToElement(mainObject, objectsByIdo, dataBaseMap, new HashSet<Integer>());
		Element rootElement = new Element(XMLConstants.TAG_DATOS);
		Document result = new Document(rootElement);
		Element objectsElement = new Element(XMLConstants.TAG_OBJECTS);
		rootElement.addContent(objectsElement);
		objectsElement.addContent(mainElement);
		return result;
	}

	/**
	 * Convierte el objeto en un elemento XML.
	 * 
	 * @param object
	 *            Objeto con toda la información que queremos reflejar en el XML
	 * @param objectsByIdo
	 *            Mapa de todos los objetos que podemos necesitar mientras
	 *            procesamos los vínculos.
	 * @param dataBaseMap
	 *            Mapa del modelo y de la base de datos.
	 * @param processedIdos
	 *            Conjunto de los objetos ya procesados.
	 * @return Devuelve el elemento generado.
	 */
	private static Element convertToElement(EngineObject object, Map<Integer, EngineObject> objectsByIdo, DataBaseMap dataBaseMap, Set<Integer> processedIdos) {
		// Añadimos el objeto que vamos a generar al conjunto de objetos ya procesados.
		processedIdos.add(object.getId());
		ClassInfo objectClass = dataBaseMap.getClass(object.getIdto());
		Element element = new Element(objectClass.getName());
		element.setAttribute(XMLConstants.ATTRIBUTE_IDNODE, String.valueOf(object.getId()));
		// Cogemos todas las propiedades para iterar sobre ellas, cogiendo sus valores e incluyéndolas en el nodo.
		for (Integer idProperty : object.getProperties()) {
			List<String> values = object.getPropertyValues(idProperty);
			PropertyInfo property = objectClass.getProperty(idProperty);
			for (String value : values) {
				if (object.isObjectProperty(idProperty)){
					// Si se trata de una ObjectProperty, tenemos que comprobar
					// si ya se ha procesado el objeto apuntado o no.
					Integer referencedIdo = Integer.parseInt(value);
					if (processedIdos.contains(referencedIdo)){
						// Construimos un nodo vínculo.
						Element refNodeElement = constructRefNode(referencedIdo, property.getName(), objectClass.getName());
						element.addContent(refNodeElement);
					}else{
						if(objectsByIdo.get(referencedIdo)!=null){
							// Llamamos recursivamente a este mismo método para
							// generar el elemento que representa al objeto y, una
							// vez obtenido, lo añadimos como contenido de este
							// mismo objeto.
							Element referencedElement = convertToElement(objectsByIdo.get(referencedIdo), objectsByIdo, dataBaseMap, processedIdos);
							element.addContent(referencedElement);
						}
					}
				}else{
					// Se trata de una DataProperty. Tenemos que comprobar si
					// tiene cardinalidad múltiple o no o si se trata de una
					// property de tipo MEMO, pues se comportan de una manera
					// especial (añadiendo nodos hijos)
					if (property.getPropertyTypes().iterator().next().intValue() == Constants.IDTO_MEMO){
						// DataProperty de tipo memo.
						Element memoNodeElement=constructMemoElement(value, property.getName());
						element.addContent(memoNodeElement);
					}else if (property.getMaxCardinality() > 1){
						// DataProperty de cardinalidad múltiple.
						Element dataPNodeElement=constructDataPropertyElement(value, property.getName());
						element.addContent(dataPNodeElement);
					}else{
						// DataProperty sin tratamiento especial.
						element.setAttribute(property.getName(), value);
					}
				}
			}
		}
		return element;
	}

	/**
	 * Construye un nodo que que representa una DataProperty de tipo MEMO.
	 * 
	 * @param value
	 *            Valor de la propiedad.
	 * @param propertyName
	 *            Nombre de la propiedad.
	 * @return Element para representar la DataProperty.
	 */
	private static Element constructMemoElement(String value, String propertyName) {
		Element memoElement = new Element(XMLConstants.TAG_MEMO);
		memoElement.setAttribute(XMLConstants.ATTRIBUTE_PROPERTYm, propertyName);
		Element valueElement = new Element(XMLConstants.TAG_VALUE);
		valueElement.addContent(new CDATA(value));
		memoElement.addContent(valueElement);
		return memoElement;
	}

	/**
	 * Construye un nodo que representa uno de los valores de una DataProperty
	 * de cardinalidad múltiple.
	 * 
	 * @param value
	 *            Valor de la propiedad.
	 * @param propertyName
	 *            Nombre de la propiedad.
	 * @return Elemento para representar el valor que toma la DataProperty.
	 */
	private static Element constructDataPropertyElement(String value, String propertyName) {
		Element dataPropertyElement = new Element(XMLConstants.TAG_DATA_PROPERTY);
		dataPropertyElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_PROPERTYm, propertyName));
		dataPropertyElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_VALUE, value));
		return dataPropertyElement;
	}

	/**
	 * Construye un nodo vínculo.
	 * 
	 * @param referencedIdo
	 *            Identificador del objeto referenciado.
	 * @param propertyName
	 *            Nombre de la propiedad por la que apuntamos al objeto.
	 * @param className
	 *            Nombre de la clase referenciada.
	 * @return Devuelve el elemento que representa el vínculo.
	 */
	private static Element constructRefNode(Integer referencedIdo, String propertyName, String className) {
		Element result = new Element(className);
		result.setAttribute(XMLConstants.ATTRIBUTE_REFNODE, referencedIdo.toString());
		result.setAttribute(XMLConstants.ATTRIBUTE_PROPERTYm, propertyName);
		return result;
	}
}
