package dynagent.server.services;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.naming.NamingException;

import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.communication.IndividualData;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.action;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.jdomParser;
import dynagent.server.dbmap.ClassInfo;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.IQueryInfo;
import dynagent.server.dbmap.PropertyInfo;
import dynagent.server.dbmap.Table;
import dynagent.server.dbmap.TableColumn;

/**
 * Esta clase se encarga de traducir un XML de Facts a un XML basado en el nuevo modelo de XML
 */
public class FactsAdapter {
	
	/** Mapa de todos los nodos creados indexados por su ido, tanto real como temporal */
	private static Map<Integer, List<Element>> elementsByIdo;
	/** Documento XML que se está generando y que va a ser devuelto como resultado */
	private static Document document;
	/**
	 * número de nodos creados debajo de datos en el nuevo documento. Servira para identificar univocamente a cada nodo
	 * que represente a un objeto.
	 */
	private static int nodeIndex;

	private static DataBaseMap dataBaseMap;

	/**
	 * Recopila toda la informacion del XML de facts para generar otro XML
	 * totalmente distinto basado en el nuevo modelo de XML.
	 * 
	 * @param dataBaseMap
	 *            Mapa de la base de datos.
	 * @param factsXML
	 *            XML que contiene el listado de facts a traducir.
	 * @return XML basado en el nuevo modelo de XML.
	 * @throws DataErrorException
	 * @throws DataConversionException
	 *             Si el formato de los datos del XML es incorrecto.
	 */
	@SuppressWarnings("unchecked")
	public static synchronized Document factsXMLToDataXML(DataBaseMap dataBaseMap, Document factsXML) throws DataErrorException, DataConversionException {
		return factsXMLToDataXML(dataBaseMap,factsXML,false,false); 
	}
	public static synchronized Document factsXMLToDataXML(DataBaseMap dataBaseMap, Document factsXML,boolean createINE, boolean hasReplica) throws DataErrorException, DataConversionException {
		reset();
		//TODO: de momento deshabilito opcion de regenerar RDN cuando es new y no viene
		hasReplica=false;
		FactsAdapter.dataBaseMap = dataBaseMap;
		Element factsRootElement = factsXML.getRootElement();
		List<Element> factList = (List<Element>) factsRootElement.getChildren(XMLConstants.TAG_FACT);
		
		HashSet<Integer> incrementales=new HashSet<Integer>();
		HashMap<Integer,String> rdnMap=new HashMap<Integer,String>();
		
		//primero rescato rdns, por si viniendo de replica, llega primero un obj prop sin rdn value, pero ese mismo individuo esta como dominio con rdn
		for (Element factElement : factList) {
			Element newFactElement = factElement.getChild(XMLConstants.TAG_NEW_FACT);
			String tmprdn=newFactElement.getAttributeValue("RDN");
			
			if(tmprdn!=null){
				tmprdn=tmprdn.trim();
				newFactElement.setAttribute("RDN", tmprdn);				
			}
			
			Integer idProperty = Integer.parseInt(newFactElement.getAttributeValue(XMLConstants.ATTRIBUTE_PROPERTY));
			if(tmprdn==null && idProperty==Constants.IdPROP_RDN){
				tmprdn=newFactElement.getText();
				if(tmprdn!=null){
					tmprdn=tmprdn.trim();
					newFactElement.setText(tmprdn);
				}
			}
				
			Integer ido = Integer.parseInt(newFactElement.getAttributeValue(XMLConstants.ATTRIBUTE_IDO));		
						
			if(tmprdn!=null && ido!=null ){
				/*PARCHE FALLO REPLICAS Integer idto = Integer.parseInt(newFactElement.getAttributeValue(XMLConstants.ATTRIBUTE_IDTO));
				if(idto.intValue()==3 && tmprdn.equals("23003")){
					tmprdn="007";
					newFactElement.setAttribute("RDN","007");
					System.err.println("PARCHEO RDN ALMACEN");
				}*/
				rdnMap.put(ido, tmprdn);
			}
			
			String rdnval=newFactElement.getAttributeValue(XMLConstants.ATTRIBUTE_RDNVALUE);
			if(rdnval!=null){
				rdnval=rdnval.trim();
				newFactElement.setAttribute(XMLConstants.ATTRIBUTE_RDNVALUE, rdnval);				
			}
			
			/*PARCHE FALLO REPLICAS Integer targetIdto = Integer.valueOf(newFactElement.getAttributeValue(XMLConstants.ATTRIBUTE_VALUECLS));		
			if(targetIdto.intValue()==3){
				String rdnval=newFactElement.getAttributeValue(XMLConstants.ATTRIBUTE_RDNVALUE);
				if(rdnval.equals("23003")){
					newFactElement.setAttribute(XMLConstants.ATTRIBUTE_RDNVALUE,"007");
					System.err.println("PARCHEO RDNVALUE ALMACEN");
				}
			}*/
		}
				
		for (Element factElement : factList) {
			/*try {
				System.err.println(jdomParser.returnXML(factElement));
			} catch (JDOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
			Element newFactElement = factElement.getChild(XMLConstants.TAG_NEW_FACT);
			Integer ido = Integer.parseInt(newFactElement.getAttributeValue(XMLConstants.ATTRIBUTE_IDO));
			
			Integer idto = Integer.parseInt(newFactElement.getAttributeValue(XMLConstants.ATTRIBUTE_IDTO));
			Integer order = Integer.parseInt(newFactElement.getAttributeValue(XMLConstants.ATTRIBUTE_ORDER));				
			
			Integer idProperty = Integer.parseInt(newFactElement.getAttributeValue(XMLConstants.ATTRIBUTE_PROPERTY));
			
			//Solo cogemos el destination si se trata del fact rdn
			String destinationIndividual = idProperty==Constants.IdPROP_RDN?newFactElement.getAttributeValue(XMLConstants.ATTRIBUTE_DESTINATION):null;
			String destinationFact = newFactElement.getAttributeValue(XMLConstants.ATTRIBUTE_DESTINATION);
			ClassInfo classInfo = dataBaseMap.getClass(idto);		
			
			if (classInfo == null) {
				continue;
			}
			
			PropertyInfo propertyInfo = classInfo.getProperty(idProperty);
			String rdn=rdnMap.get(ido);	
			
			// Miramos si existe el nodo de clase del mismo ido y compatible con la orden que se indica.
			List<Element> classElements = elementsByIdo.get(ido);
			Element classElement = null;
			if (classElements == null) {				
				classElements = new LinkedList<Element>();
				classElement = decodeCreateClassElement(ido, idto, order, destinationFact,true);
				classElement.setAttribute(XMLConstants.ATTRIBUTE_IDO_ORDER, String.valueOf(ido));

				classElements.add(classElement);
				elementsByIdo.put(ido, classElements);
				if(rdn!=null){
					newFactElement.setAttribute("RDN",rdn);
					classElement.setAttribute(Constants.PROP_RDN,rdn);
				}	
				
			} else {				
				for (Element element : classElements){
					if (decodeHasCompatibleOrder(element, order,destinationFact,ido<0)){						
						classElement = element;
					}
				}
				if (classElement == null){					
					classElement = decodeCreateClassElement(ido, idto, order, destinationFact,true);
					classElements.add(classElement);
					//se ha creado otro nodo mismo dominio por incompatibilidad, el rdn está mapeado pero no asignado
					if(rdn!=null){
						newFactElement.setAttribute("RDN",rdn);
						classElement.setAttribute(Constants.PROP_RDN,rdn);
					}					
				}
			}
			
			//opcion ser capaz crear objetos que no existen, por ejemplo tienda crea mensaje y lo elimina, y entre medio central lo marca leido
			//tiene el problema que fuerza un set a rdn cuando puede no haber cambiado, si bien cuando hay replicas siempre se trabaja con rdn
			if(rdn==null){	
				if(idProperty==Constants.IdPROP_RDN){
					rdn=newFactElement.getText();
				}else{
					rdn = newFactElement.getAttributeValue("RDN");//Ojo, es atributo RDN de Fact que es en mayuscula
				}
				if(ido.intValue()>0 && rdn==null){//rdn si puede venir en fac con ido negativo, cuando viene de replicas
					//Ojo databasemap si viene de replica puede no ser la del destino, esto solo debe ser necesario cuando viene datos de applet, que falta rdn 
					rdn=InstanceService.getRdn(dataBaseMap.getFactoryConnectionDB(), dataBaseMap,QueryConstants.getTableId(ido), idto);
				}
				//System.out.println("FactsAdapter get rdn ido:"+ido+" idto:"+idto+" rdn:"+rdn);
				if(rdn!=null){
					newFactElement.setAttribute("RDN",rdn);
					classElement.setAttribute(Constants.PROP_RDN,rdn);
					rdnMap.put(ido,rdn);
				}
				
			}						
			
			if(idProperty==Constants.IdPROP_RDN){
				//es posible que exista otro nodos mismo ido, que por ser incompatible con este, ya fue creado sin rdn
				for (Element element : classElements){
					element.setAttribute(Constants.PROP_RDN,rdn);				
				}
				//
				Element initFactElement = factElement.getChild(XMLConstants.TAG_INITIAL_FACT);
				if(initFactElement!=null){
					String rdnOld = initFactElement.getText();
					String rdnNew=newFactElement.getText();
					if(!Auxiliar.equals(rdnOld,rdnNew)) classElement.setAttribute(XMLConstants.ATTRIBUTE_RDN_PREV,rdnOld);
					rdnMap.put(ido,rdnOld);
				}
			}
			
			if (propertyInfo == null) {
				if (idProperty != Constants.IdPROP_OBJECTDELETED){
					System.err.println("Inconsistencia en el modelo: La propiedad " + idProperty + " no existe para la clase " + idto + " en el modelo.");
				} else {
					setAttributeOrder(action.DEL_OBJECT, classElement,null);
				}
				continue;
			}
			// Los nodos de clases muchas veces son creados en base a un
			// vinculo, y en el vinculo viene que es un nuevo vinculo, pero si
			// se empiezan a editar propiedades de objeto vinculado, hay que
			// cambiar a SET la acción
			if (classElement.getAttribute(XMLConstants.ATTRIBUTE_ACTION).equals(XMLConstants.ACTION_NEW) && order.equals(action.SET)){
				classElement.setAttribute(XMLConstants.ATTRIBUTE_ACTION, XMLConstants.ACTION_SET);
			}
			// Buscamos el valor que se le ha de asignar a la propiedad.
			String value = newFactElement.getAttributeValue(XMLConstants.ATTRIBUTE_QMAX);
			if (value == null) {
				// Si no es un valor numero
				value = newFactElement.getTextTrim();
				System.err.println("VALUE NULO, tomo texto "+value);
			}
			if (value == null){
				throw new DataErrorException("No se ha encontrado ningun valor para añadir a la propiedad " + propertyInfo.getName() + " de la clase " + classInfo.getName());
			}
			if (Constants.isDataType((Integer) propertyInfo.getPropertyTypes().toArray()[0])) {
				// Es DataProperty
				Integer newTableId=decodeAddDataProperty(factElement, classElement,  idto, ido,order, value, propertyInfo,incrementales,rdnMap);
				rdn=rdnMap.get(ido);
								
				//if(order.equals(action.NEW)&&ido.intValue()<0&&incrementales.contains(ido)&&rdn!=null && createINE){
				
				//opcion ser capaz crear objetos que no existen, por ejemplo tienda crea mensaje y lo elimina, y entre medio central lo marca leido
								
			} else {
				// Es object Property	
				if(!value.matches("\\d+(\\.\\d+)?")){
					System.err.println("VALUE NO ES NUMERICO "+value);
					try {
						System.err.println(jdomParser.returnXML(factElement));
					} catch (JDOMException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				Integer targetIdo = Integer.valueOf(value);
				Integer targetIdto = Integer.valueOf(newFactElement.getAttributeValue(XMLConstants.ATTRIBUTE_VALUECLS));
					
				setDB_Rdnvalue(rdnMap,newFactElement,XMLConstants.ATTRIBUTE_RDNVALUE,targetIdo,targetIdto);
				decodeAddObjectProperty(rdnMap,ido,factElement, newFactElement, order, destinationFact, classElement, value, propertyInfo);
			}
		}
		constructDocument();
		return document;
	}
	

	private static String setDB_Rdnvalue(HashMap<Integer,String> rdnMap, Element fact,String atLabel, int ido,int idto) throws DataErrorException{
		
		String rdn = fact.getAttributeValue(atLabel);
		if(rdn==null){
			rdn=rdnMap.get(ido);
		}
				
		if(rdn==null && ido>0 ){
			//Ojo databasemap si viene de replica puede no ser la del destino, esto solo debe ser necesario cuando viene datos de applet, que falta rdn 
			rdn=InstanceService.getRdn(dataBaseMap.getFactoryConnectionDB(), dataBaseMap,QueryConstants.getTableId(ido), idto);
		}

		if(rdn==null){
			rdn="ERROR#"+ido;
		}
		
		if(rdn!=null){
			fact.setAttribute(atLabel,rdn);
			rdnMap.put(ido,rdn);
		}	

		return rdn;
	}

	/**
	 * Indica si la orden es compatible con la acción del nodo.
	 * 
	 * @param element
	 *            Nodo a analizar.
	 * @param order
	 *            Orden que llega en el XML de facts.
	 * @return <code>true</code> solo si son compatibles.
	 */
	//en algunos casos, es necesario un nodo adicional, siendo mismo dominio
	//por ejemplo si se elimina un estructural a la vez que se modifican data properties, el estructural va en otro nodo, y no se referencian uno a otro
	private static boolean decodeHasCompatibleOrder(Element element, Integer order,String destinationFact,boolean generalizeDestination) {
		String destcurr=element.getAttributeValue(XMLConstants.ATTRIBUTE_DESTINATIONm);
		
		if(!Auxiliar.equals(destcurr,destinationFact)){
			//de momento se sabe que no coincide literalmente, pero hay que ordenarlo
			boolean compatible=true;
			if(destcurr==null) destcurr="";
			if(destinationFact==null) destinationFact="";					
			
			String[] destCurrArr=destcurr.trim().split(",");
			String[] destFactArr=destinationFact.trim().split(",");
			Arrays.sort(destCurrArr);
			Arrays.sort(destFactArr);
			if(!Arrays.equals(destCurrArr,destFactArr)){
				//debe crearse otro nodo para este destino, de lo contrario se perdería informacion al convertir a fact
				if(!generalizeDestination) return false;
						
				//ATENCION: en negativos no debo separar por destinos ya que de lo contrario se crean dos nodos que incluyen rdn y se intenta crear mismo individuo dos veces
				//otra cosa es que se cree un link node pero para ello no se pregunta compatibilidad
				
				//debo generalizar destinos como la union, de lo contrario una sola propertie que no se replica hace que todo el individuo no se replique												
				
				String newDest=null;
				HashSet<String> iH=new HashSet<String>();
				iH.addAll(Arrays.asList(destCurrArr));
				iH.addAll(Arrays.asList(destFactArr));
				//generalizar destino
				Iterator<String> ith=iH.iterator();
				boolean existsGlobalDest=false;//nulo, solo baja a todas redes
				boolean existsNetwDest=false;
				boolean existsFullDest=false;//* sube y baja a todas redes
				boolean existsSpecificDest=false;//* sube y baja a todas redes
				while(ith.hasNext()){
					String d=ith.next();
					if(d.length()==0) existsGlobalDest=true;
					else
						if(d.contains("*")){
								existsFullDest=true;
								newDest="*";
								break;
						}else	if(d.contains("#")) existsNetwDest=true;
								else existsSpecificDest=true;
				}		
				if(!existsFullDest){
					if(existsGlobalDest){
						if(existsNetwDest){
							//realmente si uno es nulo y otro # hay ambiguedad, porque # es mas generico en subida a niveles superiores pero mas especifico en bajada
							newDest="*";
						}else 	newDest=null;//se supone existe especifico. Normalmente si coexiste especifico con golbal es para anular replica
					}else{
						if(existsNetwDest) newDest="#";
						else{
							//se supone aqui debe existir especifico
							newDest=Auxiliar.hashSetStringToString(iH, ",");
							if(newDest.length()>98) newDest="*";
						}
					}
				}
										
				if(newDest==null){
					element.removeAttribute(XMLConstants.ATTRIBUTE_DESTINATIONm);
				}else{
					element.setAttribute(XMLConstants.ATTRIBUTE_DESTINATIONm,newDest);
				}
				return true; //hemos llegado aqui por ido negativo, asi que debo devolver compatible porque nunca podrian ser operaciones diferentes a nivel de individuo
			}
		}
					
		Attribute actionAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_ACTION);
		String actionValue = actionAttribute.getValue();
		boolean result;
		switch (order) {
		case action.DEL:
			result = actionValue.equals(XMLConstants.ACTION_DEL) || actionValue.equals(XMLConstants.ACTION_DELOBJECT);			
			break;
		case action.DEL_OBJECT:
			actionAttribute.setValue(XMLConstants.ACTION_DELOBJECT);
			result = true;
			break;
		case action.NEW:
			//data property vienen de applet como set cuando son nuevas, new cuando tiene initial. es indiferente
			//si es obj prop se crear
			result = actionValue.equals(XMLConstants.ACTION_NEW)|| actionValue.equals(XMLConstants.ACTION_CREATE_IF_NOT_EXIST) || actionValue.equals(XMLConstants.ACTION_SET);			
			break;
		case action.SET:
			result = actionValue.equals(XMLConstants.ACTION_SET)||actionValue.equals(XMLConstants.ACTION_NEW)||actionValue.equals(XMLConstants.ACTION_CREATE_IF_NOT_EXIST) ;			
			break;
		case action.createINE:
			result = actionValue.equals(XMLConstants.ACTION_NEW) || actionValue.equals(XMLConstants.ACTION_SET);			
			break;
		default:
			result = false;
			break;
	}
		return result;
	}
	private static Element buildLinkElement(int order,Element childRangeElement,PropertyInfo propertyInfo,String destination,Integer rangeIdo){
		Element linkElement = new Element(childRangeElement.getName());
		linkElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_REFNODE, childRangeElement.getAttributeValue(XMLConstants.ATTRIBUTE_IDNODE)));
		String strTableId=childRangeElement.getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID);
		if(strTableId!=null&&strTableId.matches("-?\\d+")){
			linkElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_TABLEID, strTableId));
		}
		linkElement.removeAttribute(XMLConstants.ATTRIBUTE_IDNODE);
		linkElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_PROPERTYm, propertyInfo.getName()));
		if (destination != null && !destination.isEmpty()) {
			linkElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_DESTINATIONm, destination));
		}
		setAttributeOrder(order, linkElement,rangeIdo);
		return linkElement;
	}

	/**
	 * añade la ObjectProperty indicada al objeto<br>
	 * Puede que el objeto referenciado no está todavia representado por un
	 * elemento, con lo que hay que crearlo con la informacion mínima y mapearlo
	 * para que cuando se le empiece a dar informacion se rellene el mismo
	 * elemento y no se duplique informacion
	 * @param factElement TODO
	 * @param newFactElement
	 *            Elemento que contiene la informacion de la ObjectProperty que
	 *            se quiere añadir.
	 * @param order
	 *            Orden que tenia el objeto padre
	 * @param destination
	 *            Informacion para la réplica que dice que destino tiene que
	 *            tener el objeto
	 * @param parentElement
	 *            Elemento que representa al objeto al que le tenemos que añadir
	 *            la objectProperty.
	 * @param value
	 *            Valor que contenia la ObjectProperty
	 * @param propertyInfo
	 *            Informacion de la ObjectProperty que queremos añadir.
	 * 
	 * @throws DataErrorException
	 */
	private static void decodeAddObjectProperty(HashMap<Integer,String> rdnMap,Integer domainIdo,Element factElement, Element objPropNewFact, Integer order, String objProDestination, Element domainElement, String value, PropertyInfo propertyInfo) throws DataErrorException {
		Integer rangeIdo = Integer.valueOf(value);
		Integer rangeIdto = Integer.valueOf(objPropNewFact.getAttributeValue(XMLConstants.ATTRIBUTE_VALUECLS));
		Integer rangeOrder = Integer.valueOf(objPropNewFact.getAttributeValue(XMLConstants.ATTRIBUTE_ORDER));		
		
		List<Element> candidateElements = elementsByIdo.get(rangeIdo);
		Element childRangeElement = null;
		if (candidateElements == null) {
			// No existen nodos asociados con el ido dado. Hay que inicializar la lista
			candidateElements = new LinkedList<Element>();
			//si se divide este nodo representara la objec prop. El destino por tanto debe ser el del fact ObjProp
			childRangeElement = decodeCreateClassElement(rangeIdo, rangeIdto, rangeOrder, objProDestination,false);
			childRangeElement.setAttribute(XMLConstants.ATTRIBUTE_IDO_ORDER, String.valueOf(rangeIdo));
			if (childRangeElement == null) {
				throw new DataErrorException("Fallo al intentar crear el elemento para representar al objeto: [IDO=" + rangeIdo + ", IDTO=" + rangeIdto + ", ORDER=" + rangeOrder + "objProDestination=" + objProDestination + "]");
			}
			childRangeElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_PROPERTYm, propertyInfo.getName()));
			if (childRangeElement.getAttributeValue(XMLConstants.ATTRIBUTE_ACTION).equals(XMLConstants.ACTION_NEW)) {

			}
			
			candidateElements.add(childRangeElement);
			domainElement.addContent(childRangeElement);
			elementsByIdo.put(rangeIdo, candidateElements);
		} else {
			// Se entra aque si el nodo correspondiente al objeto al que se apunta ya está creado en el mapa.
			// Tenemos que comprobar si ya se lo hemos añadido como hijo a nuestro nodo de clase actual para
			// añadirlo en caso contrario.
			Element equalIndividualFound=null;
			Element sourceElement=null;
			boolean rangeAdded=false;
			for (Element candidateElement : candidateElements){		
				equalIndividualFound=candidateElement;
				//No debo pasar como ido el domain a "has compatible", ya que en cualquier caso se va a partir a un nuevo nodo, el hijo, de cara a decidir si al ser negativo no se divide doc,
				//ya que si se divide este nodo representara la objec prop. El destino por tanto debe ser el del fact ObjProp
				//En objet property la condicion de nuevo de dominio o rango es irrelevante para la campatibilidad
				if (decodeHasCompatibleOrder(candidateElement, rangeOrder,objProDestination,false)){
					//System.out.println("Nodo compatible");
					childRangeElement = candidateElement;
				}else{
					if(candidateElement.getAttributeValue(XMLConstants.ATTRIBUTE_IDNODE)!=null){
						//System.out.println("Child Range es source node");
						sourceElement=candidateElement;
					}
				}
				
			}
			if (childRangeElement == null){
				if(candidateElements.size()==0){		
					//System.out.println("Creando child range element "+rangeIdo);
					childRangeElement = decodeCreateClassElement(rangeIdo, rangeIdto, rangeOrder, objProDestination,false);
					candidateElements.add(childRangeElement);					
				}else{
					if(sourceElement!=null){
						childRangeElement=buildLinkElement(order,sourceElement,propertyInfo,objProDestination,rangeIdo);
						domainElement.addContent(childRangeElement);
						//ademas de crear un nodo link , se añade ya como hijo
						rangeAdded=true;
					}else{
						System.out.println("childRangeElement nulo");
					}
				}				
			}
			if(!rangeAdded){
				if (!rangeAdded && !domainElement.getChildren().contains(childRangeElement)) {
					if (childRangeElement.getParent() != null) {
						// El objeto al que apuntamos ya ha sido añadido como hijo de otro nodo, con lo que creamos
						// un elemento de vinculo.		
						childRangeElement=buildLinkElement(order,childRangeElement,propertyInfo,objProDestination,rangeIdo);
						domainElement.addContent(childRangeElement);					
					} else {					
						boolean hayRelacion=false;
						
						if (childRangeElement.getChildren().contains(domainElement)||domainElement.isAncestor(childRangeElement)||childRangeElement.isAncestor(domainElement)) {
							// El nodo objetivo ya tiene como hijo a nuestro nodo actual, seguramente se trate de
							// una relación mediante una propiedad inversa. Creamos un nodo de vinculo.
							hayRelacion=true;							
							childRangeElement=buildLinkElement(order,childRangeElement,propertyInfo,objProDestination,rangeIdo);
							domainElement.addContent(childRangeElement);	
						} 
						
						if(!hayRelacion){
							// El nodo objetivo no tiene ninguna relación todavia con nuestro nodo actual, con lo
							// cual le ponemos la propiedad por la que se relaciona con nuestro nodo actual y
							// colgamos el nodo objetivo debajo del actual.
							childRangeElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_PROPERTYm, propertyInfo.getName()));
	
							if (objProDestination != null && !objProDestination.isEmpty()) {
								childRangeElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_DESTINATIONm, objProDestination));
							}
							/*try {
								System.out.println("ELEMNT HIJO: "+jdomParser.returnXML(childRangeElement));
								System.out.println("ELEMNT PADRE: "+jdomParser.returnXML(domainElement));
							} catch (JDOMException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}*/
							domainElement.addContent(childRangeElement);
						}
					}
				}else if (!propertyInfo.getName().equals(childRangeElement.getAttributeValue(XMLConstants.ATTRIBUTE_PROPERTYm))) {
					// El objeto al que apuntamos ya ha sido añadido pero con property distinta, con lo que creamos
					// un elemento de vinculo.
					childRangeElement=buildLinkElement(order,childRangeElement,propertyInfo,objProDestination,rangeIdo);
					domainElement.addContent(childRangeElement);				
				}
			}
		}
		
		String valTxt = objPropNewFact.getAttributeValue(XMLConstants.ATTRIBUTE_RDNVALUE);
		if(valTxt==null) 
			valTxt=rdnMap.get(rangeIdo);
		else rdnMap.put(rangeIdo, valTxt);
		
		if (valTxt!=null)	childRangeElement.setAttribute(Constants.PROP_RDN, valTxt);
		
		
		// Si venia Initial_Fact, significa que estamos sustituyendo un valor
		// por otro, y lo que realmente tenemos que hacer es poner un Del del
		// antiguo valor.
		Element initialFactElement = factElement.getChild(XMLConstants.TAG_INITIAL_FACT);
		if(initialFactElement != null){
			childRangeElement = null;
			Integer oldRangeIdo = Integer.parseInt(initialFactElement.getTextTrim());
			if (oldRangeIdo.equals(rangeIdo)){
				return;
			}
			Integer oldRangeIdto = Integer.parseInt(initialFactElement.getAttributeValue(XMLConstants.ATTRIBUTE_VALUECLS));
			List<Element> elementsWithSameIdo = elementsByIdo.get(oldRangeIdo);
			if (elementsWithSameIdo == null){
				elementsWithSameIdo = new LinkedList<Element>();
				childRangeElement = decodeCreateClassElement(oldRangeIdo, oldRangeIdto, action.DEL, objProDestination,false);
				childRangeElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_PROPERTYm, propertyInfo.getName()));
				elementsWithSameIdo.add(childRangeElement);
				domainElement.addContent(childRangeElement);
				elementsByIdo.put(oldRangeIdo, elementsWithSameIdo);
				String rdnvalue=setDB_Rdnvalue(rdnMap,initialFactElement,XMLConstants.ATTRIBUTE_RDNVALUE,oldRangeIdo,oldRangeIdto);				
				if (rdnvalue!=null)	childRangeElement.setAttribute(Constants.PROP_RDN, rdnvalue);
			}else{
				for (Element elementWithSameIdo : elementsWithSameIdo){
					if (decodeHasCompatibleOrder(elementWithSameIdo, rangeOrder,objProDestination,false)){
						childRangeElement = elementWithSameIdo;
					}
				}
				if (childRangeElement == null){
					childRangeElement = decodeCreateClassElement(oldRangeIdo, oldRangeIdto, action.DEL, objProDestination,false);
					elementsWithSameIdo.add(childRangeElement);
				}
				if (!domainElement.getChildren().contains(childRangeElement)) {
					
					/*FRAN: Añadimos siempre un nodo referencia ya que aunque exista el nodo estara con otro order por lo que comento el resto de logica*/
					
										
//					if (childRangeElement.getParent() != null) {
						// El objeto al que apuntamos ya ha sido añadido como hijo de otro nodo, con lo que creamos
						// un elemento de vinculo.
						//En este caso como ponemos del por venir de initial, el destino es del fact obj prop
						domainElement.addContent(buildLinkElement(action.DEL,childRangeElement,propertyInfo,objProDestination,rangeIdo));
						
//					} else {
//						// El nodo no ha sido añadido como hijo de ningun otro nodo.
//						if (childRangeElement.getChildren().contains(domainElement)) {
//							// El nodo objetivo ya tiene como hijo a nuestro nodo actual, seguramente se trate de
//							// una relación mediante una propiedad inversa. Creamos un nodo de vinculo.
//							Element linkElement = new Element(childRangeElement.getName());
//							linkElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_REFNODE, childRangeElement.getAttributeValue(XMLConstants.ATTRIBUTE_IDNODE)));
//							linkElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_PROPERTYm, propertyInfo.getName()));
//							setAttributeOrder(action.DEL, linkElement);
//							domainElement.addContent(linkElement);
//						} else {
//							// El nodo objetivo no tiene ninguna relación todavia con nuestro nodo actual, con lo
//							// cual le ponemos la propiedad por la que se relaciona con nuestro nodo actual y
//							// colgamos el nodo objetivo debajo del actual.
//							childRangeElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_PROPERTYm, propertyInfo.getName()));
//							domainElement.addContent(childRangeElement);
//						}
//					}
				}
			}
		}  
	}

	/**
	 * añade una DataProperty al nodo de la clase con el que se está trabajando.
	 * 
	 * @param factElement
	 *            Elemento que contiene la informacion del FACT
	 * @param classElement
	 *            Nodo donde se está guardando la informacion del objecto.
	 * @param order
	 *            Identificador de la orden que se está ejecutando.
	 * @param value
	 *            Valor que se le va a asignar a la DataProperty.
	 * @param propertyInfo
	 *            Informacion de la DataProperty.
	 * @return
	 * @throws DataErrorException
	 * @throws DataConversionException 
	 */
	private static int getTableId(String rdn,Integer idto) throws DataErrorException{
		Table table = dataBaseMap.getTable(idto);
		TableColumn rdnColumn = table.getDataPropertyColumn(Constants.IdPROP_RDN);
		String i="\"";
		String e="\"";
		String sqlGetTableId = 	"SELECT " + i + IQueryInfo.COLUMN_NAME_TABLEID + e + " FROM " + i + table.getName() + e + 
								" WHERE "  + i + rdnColumn.getColumnName() + e + "='" + rdn + "';";
		Integer tableId = null;
		List<List<String>> queryResult;
		try {
			queryResult = DBQueries.executeQuery(dataBaseMap.getFactoryConnectionDB(), sqlGetTableId);
			if (!queryResult.isEmpty()){
				//System.out.println("CREATEINE INCREMENTAL "+rdn);
				tableId = Integer.parseInt(queryResult.get(0).get(0));
				return tableId.intValue();	
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new DataErrorException("Error SQL "+e1.getMessage());
		} catch (NamingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new DataErrorException("Error SQL "+e1.getMessage());
		}
		return -1;
	}
	
	private static Integer decodeAddDataProperty(Element factElement, Element classElement, Integer idto,Integer ido,Integer order, String value, PropertyInfo propertyInfo,
												 HashSet<Integer> incrementales,HashMap<Integer,String> rdnMap) throws DataErrorException, DataConversionException {
		Integer newTableId=null;
		Element newFactElement = factElement.getChild(XMLConstants.TAG_NEW_FACT);
		Integer dataPropertyType = (Integer) propertyInfo.getPropertyTypes().toArray()[0];
		if (dataPropertyType == Constants.IDTO_MEMO) {
			// Los memo tienen un tratamiento especial, se construye un nodo que se añade como hijo del nodo
			// actual.
			Element memoElement = decodeCreateMemoElement(propertyInfo, order, value, null);
			classElement.addContent(memoElement);
		} else {
			String rdn=null;
			if(propertyInfo.getIdProp()==Constants.IdPROP_RDN){
				rdn=value;
				rdnMap.put(ido,rdn);
			}
			Boolean incremental = Boolean.valueOf(newFactElement.getAttributeValue(XMLConstants.ATTRIBUTE_INCREMENTAL));
			if (incremental.booleanValue()) {
				incrementales.add(ido);
				// Si es incremental hay que calcular la diferencia con el valor antiguo.
				Element initialFactElement = factElement.getChild(XMLConstants.TAG_INITIAL_FACT);
				Double newValue = Double.parseDouble(value);
				Double oldValue;
				if (initialFactElement != null){	
					Attribute qmaxAttribute = initialFactElement.getAttribute(XMLConstants.ATTRIBUTE_QMAX);
					oldValue = qmaxAttribute.getDoubleValue();
				} else {
					// Si no existe antiguo valor, consideramos que era 0.
					oldValue = 0.0D;
				}
//				String format = getFormat(newValue, oldValue);
//				NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
//			    DecimalFormat dformater = (DecimalFormat)nf;
//			    dformater.applyPattern(format); 
			    
				double difference = newValue.doubleValue() - oldValue.doubleValue();
				//System.out.println("difference " + difference);
//				value = "+" + dformater.format(difference);
				value = "+" + difference;
				//System.out.println("value " + value);
			}
			if (propertyInfo.getMaxCardinality() == 1){
				classElement.setAttribute(new Attribute(propertyInfo.getName(), decodeFormatValue(dataPropertyType, value)));
			}else{
				// DataProperty con cardinalidad mayor que 1
				Element dataPropertyElement = new Element(XMLConstants.TAG_DATA_PROPERTY);
				dataPropertyElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_PROPERTYm, propertyInfo.getName()));
				dataPropertyElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_VALUE, value));
				setAttributeOrder(order, dataPropertyElement,null);
				classElement.addContent(dataPropertyElement);
			}
		}
		return newTableId;
	}

	private static String getFormat(Double newValue, Double oldValue) {
		//System.out.println("Inicio de getFormat");
		
		BigDecimal newBig = BigDecimal.valueOf(newValue);
		int newDecs = newBig.scale();
		//System.out.println("newValue " + newValue + ", newDecs " + newDecs);
		BigDecimal oldBig = BigDecimal.valueOf(oldValue);
		int oldDecs = oldBig.scale();
		//System.out.println("oldValue " + oldValue + ", oldDecs " + oldDecs);
		
		int decimals = Math.max(newDecs, oldDecs);
		//System.out.println("decimals " + decimals);
		String format = "0";
		if (decimals>0) {
			format += ".";
			for(int i=0;i<decimals;i++)
				format += "0";
		}
		
		//System.out.println("Fin de getFormat devuelve " + format);
		return format;
	}

	/**
	 * Dado el valor que venia en el xml de Facts, y el tipo de dato que tendria
	 * que representar, se construye una cadena para tratarlo de manera correcta
	 * en el nuevo XML.
	 * 
	 * @param dataPropertyType
	 *            Entero que representa el tipo de datos que contiene la
	 *            propiedad.
	 * @param value
	 *            Valor tal y como viene en el xml de Facts
	 * @return Cadena para representar el dato en el formato que reconoce el
	 *         nuevo XML
	 */
	private static String decodeFormatValue(Integer dataPropertyType, String value) {
		String result;
		Double number;
		switch (dataPropertyType) {
		case Constants.IDTO_DOUBLE:
			result = value;
			break;
		case Constants.IDTO_DATE:
		case Constants.IDTO_DATETIME:
		case Constants.IDTO_TIME:
			number = Double.parseDouble(value);
			result = String.valueOf(number.longValue());
			break;
		case Constants.IDTO_STRING:
		case Constants.IDTO_MEMO:
		case Constants.IDTO_IMAGE:
		case Constants.IDTO_FILE:
			result = value;
			break;
		case Constants.IDTO_INT:
			number = Double.parseDouble(value);
			result = String.valueOf(number.intValue());
			break;
		case Constants.IDTO_BOOLEAN:
			number = Double.parseDouble(value);
			result = number.intValue() == 0 ? "false" : "true";
			break;
		default:
			result = value;
			break;
		}
		return result;
	}

	/**
	 * Decodifica el XML de datos al antiguo XML de facts
	 * 
	 * @param dataBaseMap
	 *            Mapa del modelo y de las tablas de base de datos
	 * @param dataDocument
	 *            Documento con el nuevo formato que se quiere traducir a un
	 *            array de Facts.
	 * @return Objeto con los FACTS
	 * @throws DataErrorException
	 *             Si la estructura del XML es incorrecta.
	 * @throws DataConversionException
	 *             Si algun atributo del XML tiene un formato no esperado.
	 */
	@SuppressWarnings("unchecked")
	public static IndividualData DataXMLToFactsXML(DataBaseMap dataBaseMap, Document dataDocument, HashSet<String> destinationList,boolean buildChangesOnly) throws DataErrorException, DataConversionException{
		IndividualData individualData = new IndividualData();
		Element rootElement = dataDocument.getRootElement();
		Element objectsElement = rootElement.getChild(XMLConstants.TAG_OBJECTS);
		List<Element> children = new ArrayList<Element>(objectsElement.getChildren()); 
		Map<Integer, Element> elementsByIdNode = new Hashtable<Integer, Element>();
		addIdtosToDataNodes(dataBaseMap, dataDocument);
		encodePreprocessObjects(objectsElement, elementsByIdNode);
		try {
			for (Element element : children) {
				encodeAddFirstLevelElement(element, individualData, dataBaseMap, elementsByIdNode, destinationList,buildChangesOnly);
			}
		} catch (DataConversionException e) {
			System.err.println("Error en la conversion de tipos de un atributo del XML original");
			e.printStackTrace();
		}
		return individualData;
	}

	/**
	 * Preprocesa un documento XML con el nuevo formato para construir un mapa
	 * que asocie cada idNode con su elemento real.
	 * 
	 * @param parentElement
	 *            Nodo &ltobjects&gt de donde cuelgan todos los objetos que
	 *            representan datos.
	 * @return Mapa con los elementos que contienen datos indexados por su
	 *         id_node
	 */
	@SuppressWarnings("unchecked")
	private static void encodePreprocessObjects(Element parentElement, Map<Integer, Element> map){
		List<Element> children = new ArrayList<Element>(parentElement.getChildren());
		for (Element element : children) {
			Attribute idtoAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);
			if (idtoAttribute != null){
				// Si tiene el atributo idto, siginifica que es un nodo con
				// datos, con lo cual es un elemento que debemos añadir al mapa.
				Attribute idNodeAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDNODE);
				if(idNodeAttribute!=null){//No tendra idNode si tiene refNode
					Integer idNode;
					try {
						idNode = idNodeAttribute.getIntValue();
					} catch (DataConversionException e) {
						e.printStackTrace();
						continue;
					}
					map.put(idNode, element);
					encodePreprocessObjects(element, map);
				}
			}
		}
	}

	/**
	 * Metodo para procear los elementos del XML que no tienen un padre que sea
	 * otro objeto de base de datos.
	 * 
	 * @param element
	 *            Elemento que no está representado como apuntado por otro
	 *            objeto.
	 * @param individualData
	 *            Objeto donde vamos acumulando los facts que vamos creando en
	 *            base a la informacion de los elementos.
	 * @param dataBaseMap
	 *            Mapa de la base de datos y el modelo que se está usando.
	 * @param elementsByIdNode
	 *            Mapa de los nodos representados por su idNode
	 * @throws DataConversionException
	 *             Si hay algun atributo con un formato no esperado.
	 */
	private static void encodeAddFirstLevelElement (Element element, IndividualData individualData, DataBaseMap dataBaseMap, Map<Integer, Element> elementsByIdNode, HashSet<String> destinationList,boolean buildChangesOnly) throws DataConversionException{
		// Conseguimos el IDTO y el tableId del elemento para construir el ido.
		Attribute idtoAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);		
		
		Integer idto = idtoAttribute.getIntValue();
		Integer tableId = DatabaseManager.getTableId(idto,null,null,null,elementsByIdNode,element);//tableIdAttribute.getIntValue();

		int ido = tableId;
		if (ido>0)
			ido = QueryConstants.getIdo(tableId, idto);
		encodeAddDataProperties(idto, ido, element, individualData, dataBaseMap, destinationList,buildChangesOnly);
		
		encodeAddChildrenElements(element, ido, individualData, dataBaseMap, elementsByIdNode, destinationList,buildChangesOnly);
		
	}

	/**
	 * Este metodo se encarga de analizar los nodos de segundo nivel en
	 * adelante, es decir, aquellos que tienen padre. Nos podemos encontrar con
	 * varios tipos de hijos:
	 * <ul>
	 * <li><b>Nodos de vinculo:</b> Solo se tiene que añadir el vinculo por la
	 * property al objeto referenciado. No se analizan hijos, porque se
	 * analizaran en el nodo real.</li>
	 * <li><b>Nodos reales:</b> Se añade el vinculo con el elemento padre y se
	 * tienen que analizar sus hijos si los tuviera.</li>
	 * <li><b>Otros nodos:</b> Nodos MEMO.
	 * 
	 * @param element
	 *            Elemento padre del que tenemos que analizar sus hijos.
	 * @param ido
	 *            Identificador para el aplet del objeto.
	 * @param individualData
	 *            Objeto donde se está montando el array de FACTS
	 * @param dataBaseMap
	 *            Objeto que nos proporciona el mapa de las clases y las tablas.
	 * @param elementsByIdNode
	 *            Mapa de los elementos indexados por su IdNode.
	 * @throws DataConversionException
	 *             Si se produce algun error en la conversion de tipos de los
	 *             datos del XML
	 */
	@SuppressWarnings("unchecked")
	private static void encodeAddChildrenElements(Element domainElement, int ido, IndividualData individualData, DataBaseMap dataBaseMap, Map<Integer, Element> elementsByIdNode,HashSet<String> destinationList,boolean buildChangesOnly) throws DataConversionException{
		Attribute idtoParentAttribute = domainElement.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);
		Integer idtoParent = idtoParentAttribute.getIntValue();
		ClassInfo parentClass = dataBaseMap.getClass(idtoParent);
//		String action = domainElement.getAttributeValue(XMLConstants.ATTRIBUTE_ACTION);
		String destinationDomain = domainElement.getAttributeValue(XMLConstants.ATTRIBUTE_DESTINATIONm);
		String strDomainAction = domainElement.getAttributeValue(XMLConstants.ATTRIBUTE_ACTION);
		
		List<Element> children = new ArrayList<Element>(domainElement.getChildren());
		for (Element child : children) {
			String childElementName = child.getName();
			String propertyName = child.getAttributeValue(XMLConstants.ATTRIBUTE_PROPERTYm);
			if(propertyName==null){
				try {
					System.out.println("PROP NULA "+jdomParser.returnXML(child));
				} catch (JDOMException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Integer idProperty = dataBaseMap.getPropertyId(propertyName);
			PropertyInfo property = parentClass.getProperty(idProperty);
			if (childElementName.equals(XMLConstants.TAG_MEMO)){
				// Si es un nodo memo, cogemos el texto, añadimos el vinculo y continuamos con el siguiente hijo.
				Element valueElement = child.getChild(XMLConstants.TAG_VALUE);
				//CDATA cData = (CDATA) valueElement.getContent().get(0);
				String memoText = valueElement.getText();
				
				FactInstance fact = new FactInstance(idtoParent, ido, idProperty, memoText, Constants.IDTO_MEMO, Constants.DATA_MEMO, null, null, null, domainElement.getName());
				fact.setRdn(domainElement.getAttributeValue(XMLConstants.ATTRIBUTE_RDN));	
				generalizeMessageDestination(fact,destinationList,destinationDomain,null);

				String action = child.getAttributeValue(XMLConstants.ATTRIBUTE_ACTION);
				setOrder(fact, action,null);
				fact.setExistia_BD(true);
				individualData.addIPropertyDef(fact);				
				continue;
			}else if (childElementName.equals(XMLConstants.TAG_DATA_PROPERTY)){
				String value = child.getAttributeValue(XMLConstants.ATTRIBUTE_VALUE);
				int dataType = property.getPropertyTypes().iterator().next();
				
				FactInstance fact = new FactInstance(idtoParent, ido, idProperty, value, dataType, getRangeName(dataType), null, null, null, domainElement.getName());
				fact.setRdn(domainElement.getAttributeValue(XMLConstants.ATTRIBUTE_RDN));	
				generalizeMessageDestination(fact,destinationList,destinationDomain,null);
				String action = child.getAttributeValue(XMLConstants.ATTRIBUTE_ACTION);
				setOrder(fact, action,null);
				fact.setExistia_BD(true);
				individualData.addIPropertyDef(fact);
				continue;
			}
			// Si llegamos aque, tenemos que estar tratando una ObjectProperty.
			Attribute childNodeIdentifierAttribute = child.getAttribute(XMLConstants.ATTRIBUTE_IDNODE);
			Integer idtoChild;
			Integer tableIdChild;
			int idoChild;
			boolean analyzeChildren = false;
			String destinationRange = null;
			String rdnValue=child.getAttributeValue(XMLConstants.ATTRIBUTE_RDN);
			destinationRange = child.getAttributeValue(XMLConstants.ATTRIBUTE_DESTINATIONm);
//			String action = null;
			if (childNodeIdentifierAttribute == null){
				// Se trata de un nodo vinculo y tenemos que obtener el nodo real para poder averiguar el ido.
				//el destino sigue siendo el de este nodo, y no el del nodo fuente de la referencia, ya que cada nodo se segmenta para cada destino
				Attribute childIdtoAttribute = child.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);
				idtoChild = childIdtoAttribute.getIntValue();
				
				tableIdChild=DatabaseManager.getTableId(idtoChild,null,null,null,elementsByIdNode,child);
				rdnValue=child.getAttributeValue(XMLConstants.ATTRIBUTE_RDN);								

				idoChild = tableIdChild;
				if (idoChild>0)
					idoChild = QueryConstants.getIdo(tableIdChild, idtoChild);
			}else{				
//				action = child.getAttributeValue(XMLConstants.ATTRIBUTE_ACTION);
				// Es un nodo real, del que hay que añadir sus DataProperties y al que hay que explorarle sus hijos si los tuviera.
				Attribute childIdtoAttribute = child.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);
				Attribute childTableIdAttribute = child.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);
				idtoChild = childIdtoAttribute.getIntValue();
				tableIdChild = childTableIdAttribute.getIntValue();
				idoChild = tableIdChild;
				if (idoChild>0)
					idoChild = QueryConstants.getIdo(tableIdChild, idtoChild);
				analyzeChildren = true;
			}

			FactInstance fact = new FactInstance(idtoParent, ido, idProperty, String.valueOf(idoChild), idtoChild, child.getName(), null, null, null, domainElement.getName());
			fact.setRdn(domainElement.getAttributeValue(XMLConstants.ATTRIBUTE_RDN));
			fact.setRdnValue(rdnValue);
			
			//la object property hereda el destination del individuo del que es propiedad, pero no de las data property del nodo inferior (por ejemplo en createIne) que pueden tener otro destino
			//por ejmplo un cliente tiene un nodo inferior localidad, la obj prop a localidad es mismo detino que cliente (en este caso domainElement), pero la localidad en si y sus data properties tiene el destino dicho en el child
			generalizeMessageDestination(fact,destinationList,destinationDomain,destinationRange);
			
			String strRangeAction = child.getAttributeValue(XMLConstants.ATTRIBUTE_ACTION);
			
			setOrder(fact, strDomainAction,strRangeAction);
			fact.setExistia_BD(true);
			individualData.addIPropertyDef(fact);
			if(analyzeChildren){
				encodeAddDataProperties(idtoChild, idoChild, child, individualData, dataBaseMap,destinationList,buildChangesOnly);
				encodeAddChildrenElements(child, idoChild, individualData, dataBaseMap, elementsByIdNode,destinationList,buildChangesOnly);
			}
		}
	}
	
	private static void setOrder(IPropertyDef fact, String strDomainAction,String strRangeAction) {		
		if (strDomainAction!=null) {
			if (strRangeAction!=null && strRangeAction.equals(XMLConstants.ACTION_DEL) || strDomainAction.equals(XMLConstants.ACTION_DEL)) {
				((FactInstance)fact).setOrder(action.DEL);
			} else if (strDomainAction.equals(XMLConstants.ACTION_DELOBJECT)) {
				((FactInstance)fact).setOrder(action.DEL_OBJECT);
			} else if (strDomainAction.equals(XMLConstants.ACTION_NEW)) {
				((FactInstance)fact).setOrder(action.NEW);
			} else if (strDomainAction.equals(XMLConstants.ACTION_SET)) {
				((FactInstance)fact).setOrder(action.SET);
			} else if (strDomainAction.equals(XMLConstants.ACTION_CREATE_IF_NOT_EXIST)) {
				((FactInstance)fact).setOrder(action.createINE);
			}
		}
	}

	private static void generalizeMessageDestination(FactInstance fact,HashSet<String> destinationList,String destinationDomain,String destinationRange){
		String destinationFact=destinationDomain;
		if(destinationRange!=null){
			//Esta seccion solo tendria sentido si el dominio tiene un rango mas restrictivo que el nodo hijo que representa el rango, lo cual no debería pasar puesto
			//que ya los nodos hijos se segmentan contemplando ese caso
			
			//A nivel de fact debo quedarme con la interseccion de destinos. Si el dominio es global y el rango especifico, no debo replicar un enlace a algo que no existe en otra maquina
			//y si es al contrario, debo quedarme con el destino del dominio 
			if(destinationDomain==null||destinationDomain.length()==0){
				destinationFact=destinationRange.contains("*") ? destinationDomain:destinationRange;
			}else{				
				if(destinationDomain.contains("*")){
					if(destinationRange.contains("*")) destinationFact="*";
					else{
						destinationFact=destinationRange;
					}
				}else if(destinationDomain.contains("#")){
					//entre global y red, red es mas especifico (aun a riesgo que suba con post cosas que no deberian con global)
					if(destinationRange.contains("#") || destinationRange==null || destinationRange.length()==0 || destinationRange.contains("*")) destinationFact="#";
					else{
						destinationFact=destinationRange;
					}
				}else{//dominio tiene destino especifico, no es global ni * ni #
					if(destinationRange==null || destinationRange.length()==0 || destinationRange.contains("*") || destinationRange.contains("#") ) destinationFact=destinationDomain;
					else{
						String[] r=destinationRange.split(",");
						String[] d=destinationDomain.split(",");
						HashSet<String> rh=new HashSet<String>();
						rh.addAll(Arrays.asList(r));
						
						HashSet<String> rd=new HashSet<String>();
						rd.addAll(Arrays.asList(d));
						
						destinationFact=null;
						Iterator<String> itr=rd.iterator();
						while(itr.hasNext()){
							String dest=itr.next();
							if(rh.contains(dest)){
								if(destinationFact==null) destinationFact=dest;
								else destinationFact+=","+dest;
							}
						}						
					}
				}
			}			
		}
		//resolver destino general del mensaje, no a nivel de fact 
		//si sucede en una central, esta bien que global se convierta en *. Si este dato lo lee un replicador con destino de subida hacia entidad jerarquia superior, no deberia convertirse global a * salvo que haya varios
		//pero igualmente al segmentar por destinos resolvera si realmente debe enviarse algo hacía arriba, es decir el destino general de mensaje mejora rendimiento pero no es definitivo
		if(destinationList!=null){			
			if(destinationFact!=null && destinationFact.length()>0 && !destinationFact.equals("*") && !destinationList.contains("*")){					
				String[] split=destinationFact.split(",");
				for(int i=0;i<split.length;i++){
					destinationList.add(split[i]);
				}
			}else{
				destinationList.clear();
				destinationList.add("*");
			}
		}
		if(destinationFact!=null) fact.setDestinationSystem(destinationFact);
	}
	/**
	 * añade todas las data properties de un elemento.
	 * 
	 * @param idto
	 *            Identificador de la clase del objeto del que queremos añadir
	 *            los datos.
	 * @param ido
	 *            Identificador del objeto para el aplet.
	 * @param element
	 *            Elemento que contiene los datos.
	 * @param individualData
	 *            Objeto donde se está construyendo el nuevo array de FACTS
	 * @param dataBaseMap
	 *            Objeto que nos da el mapa de clases del modelo.
	 * @throws DataConversionException
	 *             Si algun atributo del XML tiene un formato de datos no
	 *             esperado
	 */

	
	@SuppressWarnings("unchecked")
	private static void encodeAddDataProperties(int idto, int ido, Element element, IndividualData individualData, DataBaseMap dataBaseMap, HashSet<String> destinationList,boolean buildChangesOnly) throws DataConversionException{
		String destinationSystem = element.getAttributeValue(XMLConstants.ATTRIBUTE_DESTINATIONm);
		String action = element.getAttributeValue(XMLConstants.ATTRIBUTE_ACTION);
		if (action!=null && action.equals(XMLConstants.ACTION_DELOBJECT)) {
			FactInstance fact = new FactInstance(idto, ido, Constants.IdPROP_OBJECTDELETED, null, null, null, null, null, null, element.getName());
			generalizeMessageDestination(fact,destinationList,destinationSystem,null);
			
			setOrder(fact, action,null);
			fact.setExistia_BD(true);
			individualData.addIPropertyDef(fact);
			fact.setRdn(element.getAttributeValue(XMLConstants.ATTRIBUTE_RDN));
			//Element parent=element.getParent();
			//if(parent!=null) fact.setRdn(parent.getAttributeValue(XMLConstants.ATTRIBUTE_RDN));
		} else {
			List<Attribute> attributes = new ArrayList<Attribute>(element.getAttributes());
			ClassInfo classInfo = dataBaseMap.getClass(idto);
			for (Attribute attribute : attributes) {
				String propertyName = attribute.getName();
				if (! isControlAttribute(propertyName)){
					Integer idProperty = dataBaseMap.getPropertyId(propertyName);
					if (idProperty == null){
						System.err.println("No se sabe el idProperty para la propiedad de nombre: " + propertyName);
						continue;
					}
					String rdnprev= element.getAttributeValue(XMLConstants.ATTRIBUTE_RDN_PREV);
					
					if(idProperty.equals(Constants.IdPROP_RDN) && 
						(action!=null && action.equals(XMLConstants.ACTION_DEL) ||
						buildChangesOnly && ido>0 && rdnprev==null)){//si ido >0 y no hay rdn prev, se trata de un nodo creado para otras properties
						continue;
						//Si es un nodo "del", todos los atributos se eliminan, tanto data property como obj prop, excepto el rdn que debe estar presente para identificar en replicas
						//por tanto no puede eliminarse un rdn, debe reemplazarse por otro valor, pero eso ya sería en otro nodo
					}
					PropertyInfo property = classInfo.getProperty(idProperty);
					// Si es dataProperty, solo tiene un tipo, con lo cual lo podemos coger de la siguiente manera.
					if(property==null){
						System.err.println("la propiedad no es de este nodo: " + propertyName+","+classInfo.getName());
					}
					Integer propertyType = property.getPropertyTypes().iterator().next();
					String value = null;
					Double qmin = null;
					Double qmax = null;
					boolean incremental = false;
					if (propertyType.equals(Constants.IDTO_STRING) || propertyType.equals(Constants.IDTO_MEMO) || 
							propertyType.equals(Constants.IDTO_IMAGE) || propertyType.equals(Constants.IDTO_FILE)){
						value = attribute.getValue();
					}else{
						if (propertyType.equals(Constants.IDTO_DOUBLE) && attribute.getValue().startsWith("+")) {
							incremental = true;
						}
						Double numeric = getNumericData(propertyType, attribute);
						qmin = numeric;
						qmax = numeric;
					}
					FactInstance fact = new FactInstance(idto, ido, idProperty, value, propertyType, getRangeName(propertyType), qmin, qmax, null, element.getName());
					generalizeMessageDestination(fact,destinationList,destinationSystem,null);
					setOrder(fact, action,null);
					if (incremental) fact.setIncremental(true);
					fact.setExistia_BD(true);
					individualData.addIPropertyDef(fact);
										
					fact.setRdn(element.getAttributeValue(XMLConstants.ATTRIBUTE_RDN));
										
					if(idProperty.equals(Constants.IdPROP_RDN) && rdnprev!=null){
						FactInstance initF=fact.clone();
						initF.setRdn(rdnprev);
						initF.setVALUE(rdnprev);
						fact.setInitialValues(initF);
					}							
				}
			}
		}
	}

	/**
	 * Dado el tipo de la propiedad, devuelve la cadena que representa esa
	 * propiedad. Solo funciona con las DataProperty.
	 * 
	 * @param propertyType
	 *            Tipo de la DataProperty
	 * @return Nombre correspondiente a la DataProperty que hay que poner en
	 *         rangeName.
	 */
	private static String getRangeName(Integer propertyType) {
		String rangeName = "";
		switch (propertyType) {
		case Constants.IDTO_INT:
			rangeName = Constants.DATA_INT;
			break;
		case Constants.IDTO_DOUBLE:
			rangeName = Constants.DATA_DOUBLE;
			break;
		case Constants.IDTO_DATE:
			rangeName = Constants.DATA_DATE;
			break;
		case Constants.IDTO_DATETIME:
			rangeName = Constants.DATA_DATETIME;
			break;
		case Constants.IDTO_STRING:
			rangeName = Constants.DATA_STRING;
			break;
		case Constants.IDTO_MEMO:
			rangeName = Constants.DATA_MEMO;
			break;
		case Constants.IDTO_IMAGE:
			rangeName = Constants.DATA_IMAGE;
			break;
		case Constants.IDTO_FILE:
			rangeName = Constants.DATA_FILE;
			break;
		case Constants.IDTO_BOOLEAN:
			rangeName = Constants.DATA_BOOLEAN;
			break;
		case Constants.IDTO_TIME:
			rangeName = Constants.DATA_TIME;
			break;
		default:
			break;
		}
		return rangeName;
	}

	/**
	 * Dado el tipo y el atributo, convierte el valor del atributo a Double para
	 * que pueda ser insertado en qmin y qmax
	 * 
	 * @param propertyType
	 *            Tipo del dato de la propiedad
	 * @param attribute
	 *            Atributo que contiene el valor de la propiedad.
	 * @return Doble con el valor que hay que darle a QMIN y QMAX
	 */
	private static Double getNumericData(Integer propertyType, Attribute attribute) {
		Double result = null;
		try {
			switch (propertyType) {
			case Constants.IDTO_DOUBLE:
				if (attribute.getValue().startsWith("+"))
					result = Double.parseDouble(attribute.getValue().substring(1));
				else
					result = attribute.getDoubleValue();
				break;
			case Constants.IDTO_INT:
				result = new Double(attribute.getIntValue());
				break;
			case Constants.IDTO_DATETIME:
			case Constants.IDTO_DATE:
			case Constants.IDTO_TIME:
				result = new Double(attribute.getLongValue()); 
				break;
			case Constants.IDTO_BOOLEAN:
				boolean bool = attribute.getBooleanValue();
				if (bool){
					result = new Double(1);
				}else{
					result = new Double(0);
				}
				break;
			default:
				System.err.println("Tipo de dato desconocido: " + propertyType);
				break;
			}
		} catch (DataConversionException e) {
			System.err.println("Error de conversion");
			e.printStackTrace();
		}
		return result;
	}

	private static boolean isControlAttribute(String name) {
		return name.equals(XMLConstants.ATTRIBUTE_ACTION)
				|| name.equals(XMLConstants.ATTRIBUTE_DESTINATIONm)
				|| name.equals(XMLConstants.ATTRIBUTE_IDTOm)
				|| name.equals(XMLConstants.ATTRIBUTE_TABLEID)
				|| name.equals(XMLConstants.ATTRIBUTE_IDNODE)
				|| name.equals(XMLConstants.ATTRIBUTE_IDO_ORDER)
				|| name.equals(XMLConstants.ATTRIBUTE_PROPERTYm);
	}

	/**
	 * Una vez finalizado el parseo del XML de facts, se va a crear el documento resultante.
	 */
	private static void constructDocument() {
		Element rootElement = document.getRootElement();
		Element objectsElment = new Element(XMLConstants.TAG_OBJECTS);
		// Añadimos la informacion de los objetos.
		for (List<Element> elements : elementsByIdo.values()) {
			for (Element element : elements){
				if (element.getParent() == null) {
					objectsElment.addContent(element);
				}
			}
		}
		
		rootElement.addContent(objectsElment);
	}

	/**
	 * metodo que se encarga de crear un nodo a partir de un fact con etiqueta: NEW_FACT
	 * 
	 * @param destination
	 *            
	 * @param factElement
	 * 
	 * @return
	 */
	private static Element decodeCreateClassElement(Integer ido, Integer idto, Integer order, String destination, boolean isDomain) {
		ClassInfo classInfo = dataBaseMap.getClass(idto);
		if (classInfo == null) {
			System.err.println("No se tiene informacion sobre la clase con idto=" + idto);
			return null;
		}
		nodeIndex++;
		Element resultElement = new Element(classInfo.getName());
		//resultElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_IDTOm, idto.toString()));
		if (destination != null && ! destination.isEmpty()){
			resultElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_DESTINATIONm, destination));
		}
		resultElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_IDNODE, String.valueOf(nodeIndex)));
		
		int tableId = QueryConstants.getTableId(ido);
		
		resultElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_TABLEID, String.valueOf(tableId)));
		//si la order es delete y ido >0, debe respetarse
		/*if (ido > 0 && order.equals(action.NEW)){//debo respetar orden new porque puede venir positivo de una replica, y despues set local idos lo reconvertira a negativo
			order = action.SET;
		}*/
		if(ido<0 && order.equals(action.SET)){//Añadido por el registro online o por la importacion de individuos mediante excel. En el applet nunca debe ocurrir este caso
			order = action.NEW;
		}
		/*if(isDomain && ido>0 && order==action.DEL){
			order=action.SET;//eliminar una object property no es un del del objeto
		}*/
		setAttributeOrder(order, resultElement,null);
		return resultElement;
	}

	
	/**
	 * Crea un nodo de tipo memo para representar el valor de la propiedad.
	 * 
	 * @param property
	 *            Informacion de la propiedad sobre la que se está actuando.
	 * @param order
	 *            acción que se está realizando sobre la propiedad.
	 * @param newValue
	 *            Nuevo valor que se le quiere asignar a la propiedad.
	 * @param oldValue
	 *            Valor antiguo que tenia la propiedad.<br>
	 *            Si es <code>null</code> se considera que no habia valor
	 *            antiguo.
	 * @return Elemento memo con los datos indicados.
	 * @throws DataErrorException
	 *             Si los datos dados son incorrectos.
	 */
	private static Element decodeCreateMemoElement(PropertyInfo property, int order, String newValue, String oldValue) throws DataErrorException{
		if (newValue == null){
			throw new DataErrorException("Cuando se está creando un nodo memo, debe tener al menos un valor nuevo a asignar.");
		}
		Element memoElement = new Element(XMLConstants.TAG_MEMO);
		setAttributeOrder(order, memoElement,null);
		memoElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_PROPERTYm, property.getName()));
		Element valueElement = new Element(XMLConstants.TAG_VALUE);
		valueElement.addContent(new CDATA(newValue));
		memoElement.addContent(valueElement);
		if (oldValue != null){
			Element oldValueElement = new Element(XMLConstants.TAG_VALUE + XMLConstants.OLD_PROPERTY);
			oldValueElement.addContent(new CDATA(oldValue));
			memoElement.addContent(oldValueElement);
		}
		
		return memoElement;
	}

	/**
	 * añade al elemento dado el atributo con la orden identificada por el número pasado.
	 * @param order número identificando el tipo de orden
	 * @param element Elemento al que se le añadira el número.
	 */
	private static void setAttributeOrder(Integer order, Element element, Integer idoRange) {
		if(idoRange!=null && idoRange>0 && order.equals(action.NEW)){
			element.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_ACTION, XMLConstants.ACTION_SET));
			return;
		}
		switch (order) {
			case action.DEL:
				element.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_ACTION, XMLConstants.ACTION_DEL));
				break;
			case action.DEL_OBJECT:
				element.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_ACTION, XMLConstants.ACTION_DELOBJECT));
				break;
			case action.NEW:
				element.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_ACTION, XMLConstants.ACTION_NEW));
				break;
			case action.SET:
				element.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_ACTION, XMLConstants.ACTION_SET));
				break;
			case action.createINE:
				element.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_ACTION, XMLConstants.ACTION_CREATE_IF_NOT_EXIST));
				break;
			default:
				break;
		}
	}

	/**
	 * Da el valor inicial a todos los elementos de la clase.
	 */
	private static void reset() {
		elementsByIdo = new Hashtable<Integer, List<Element>>();
		document = new Document(new Element("datos"));
		nodeIndex = 0;
	}

	/**
	 * añade a los nodos de datos el atributo idto con el identificador de la
	 * clase segun el mapa que se da.
	 * 
	 * @param dataBaseMap
	 *            Mapa de la base de datos y el modelo.
	 * @param dataDocument
	 *            XML con el nuevo formato al que se le han de añadir los
	 *            atributos 'idto'.
	 * @throws DataErrorException
	 *             Si hay algun error en la estructura del XML.
	 */
	@SuppressWarnings("unchecked")
	public static void addIdtosToDataNodes(DataBaseMap dataBaseMap, Document dataDocument) throws DataErrorException {
		if (dataDocument == null){
			throw new DataErrorException("Se ha pasado un XML nulo");
		}
		Element rootElement = dataDocument.getRootElement();
		Element objectsElement = rootElement.getChild(XMLConstants.TAG_OBJECTS);
		if (objectsElement == null){
			throw new DataErrorException("Se esperaba el nodo <" + XMLConstants.TAG_OBJECTS + "> pero no se encontro");
		}
		
		List<Element> objectsList = new LinkedList<Element>(objectsElement.getChildren());
		addIdtosProcessChildren(objectsList, dataBaseMap);
	}

	/**
	 * Procesa los nodos recursivamente en busca de nodos de datos para
	 * añadirles el idto.<br>
	 * Se considera que un nodo es de datos si tiene el atributo 'id_node'
	 * 
	 * @param children
	 *            Lista de los elementos a procesar.
	 * @param dataBaseMap
	 *            Mapa de la base de datos.
	 * @throws DataErrorException
	 *             Si hay algun error en los datos del XML
	 */
	@SuppressWarnings("unchecked")
	private static void addIdtosProcessChildren(List<Element> children, DataBaseMap dataBaseMap) throws DataErrorException {
		if (children == null){
			return;
		}
		for (Element childElement : children) {
			Attribute idNodeAttribute = childElement.getAttribute(XMLConstants.ATTRIBUTE_IDNODE);
			if (idNodeAttribute == null && childElement.getAttribute(XMLConstants.ATTRIBUTE_REFNODE)==null){
				// No es un nodo de datos.
				continue;
			}
			
			String className = childElement.getName();
			ClassInfo classInfo = dataBaseMap.getClass(className);
			if (classInfo == null){
				throw new DataErrorException("No se ha encontrado la clase representada por el nombre " + className);
			}
			// Añadimos el atributo idto.
			int idto=classInfo.getIdto();
			childElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_IDTOm, String.valueOf(idto)));
			// Al ser un nodo de datos, exploramos los hijos.
			List<Element> myChildren = new LinkedList<Element>(childElement.getChildren());
			addIdtosProcessChildren(myChildren, dataBaseMap);
		}
	}

}
