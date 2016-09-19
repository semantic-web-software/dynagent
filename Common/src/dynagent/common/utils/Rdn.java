package dynagent.common.utils;


import java.util.Iterator;

import org.jdom.Element;

import dynagent.common.Constants;


public class Rdn {
	
	/* Inserta en el primer nodo CLASS el nodo RDN, es decir, nodo ATTRIBUTE con PROPERTY IdPROP_RDN, se utiliza un booleano
	 * que si está a true comprueba si existe y en tal caso no se inserta. 
	 * Si existe el atributo SELECT lo inserta para que lo muestre el primero.*/
	public static void insertRDNRoot(String label, String id, Element nodeStructure, String value, boolean exists) {
		//System.out.println("Inicio de la funcion insertRDN");
		int cont = 1;
		Iterator it = nodeStructure.getChildren(QueryConstants.CLASS).iterator();
		while (it.hasNext()) {
			Element nodeClass = (Element)it.next();
			Element attrExistente = null;
			if (exists && nodeClass!=null) {
//				Element eRdnExistente = jdomParser.findElementByAt(nodeClass, QueryConstants.ATTRIBUTE, QueryConstants.PROP, 
//						String.valueOf(Constants.IdPROP_RDN), false);
				attrExistente = jdomParser.findFirstOf(nodeClass, QueryConstants.ATTRIBUTE, false);
			}
			String tmp = null;
			boolean create = false;
			if (attrExistente == null) {
				create = true;
				tmp = QueryConstants.RDN_TEMP_NO_SQ;
			} else {
				Element eRdnExistente = jdomParser.findElementByAt(nodeClass, QueryConstants.ATTRIBUTE, QueryConstants.PROP, 
						String.valueOf(Constants.IdPROP_RDN), false);
				if (eRdnExistente == null) {
					create = true;
					tmp = QueryConstants.RDN_TEMP_NO_SQ;
				}
			}
			if (create) {
				Element nodePresentation = nodeStructure.getChild(QueryConstants.PRESENTATION);
				Element nodePresentationRoot = null;
				if (nodePresentation!=null)
					nodePresentationRoot = nodePresentation.getChild(QueryConstants.VIEW);
		
				if (nodeClass!=null) {
					Element eRdn = createRdn(label, value, id + "_h" + cont, tmp);
					cont++;
					insertRdnStructure(nodeClass, eRdn);
					String idParent = nodeClass.getAttributeValue(QueryConstants.ID);
					insertRdnPresentation(nodePresentationRoot, eRdn, idParent);
				}
			}
		}
		//System.out.println("Fin de la funcion insertRDN");
	}

	/* Inserta en un nodo CLASS el nodo RDN, es decir, nodo ATTRIBUTE con PROPERTY IdPROP_RDN, se utiliza un booleano
	 * que si está a true comprueba si existe y en tal caso no se inserta.*/
	public static void insertRDNClass(String label, Element nodeStructure, Element nodeClass, String value, boolean exists, String id) {
		//System.out.println("Inicio de la funcion insertRDNClass");
		if (exists) {
			String[] names = new String[2];
			names[0] = QueryConstants.ATTRIBUTE;
			names[1] = QueryConstants.WHERE;
//			Element eRdnExistente = jdomParser.findElementByAt(nodeClass, names, QueryConstants.PROP, 
//					String.valueOf(Constants.IdPROP_RDN), false);
			Element eRdnExistente = jdomParser.findFirstOf(nodeClass, QueryConstants.ATTRIBUTE, false);
			if (eRdnExistente != null) {
				//System.out.println("Fin de la funcion insertRDN");
				return;
			}
		}

		if (nodeClass!=null) {
			Element nodePresentation = nodeStructure.getChild(QueryConstants.PRESENTATION);
			Element nodePresentationRoot = null;
			if (nodePresentation!=null)
				nodePresentationRoot = nodePresentation.getChild(QueryConstants.VIEW);
	
			Element eRdn = createRdn(label, value, id, QueryConstants.RDN_TEMP_NO_SQ);
			insertRdnStructure(nodeClass, eRdn);
			String idParent = nodeClass.getAttributeValue(QueryConstants.ID);
			insertRdnPresentation(nodePresentationRoot, eRdn, idParent);
		}
		//System.out.println("Fin de la funcion insertRDNClass");
	}
	
	private static Element createRdn(String label, String value, String id, String tmp) {
		//System.out.println("Inicio de la funcion createRdn");
		Element eRdn = new Element(label);
		eRdn.setAttribute(QueryConstants.PROP, String.valueOf(Constants.IdPROP_RDN));
		eRdn.setAttribute(QueryConstants.ID_TM_RULEENGINE, String.valueOf(Constants.IDTO_STRING));
		eRdn.setAttribute(QueryConstants.NAME, "RDN");
		eRdn.setAttribute(tmp, "TRUE");
		eRdn.setAttribute(QueryConstants.ID, id);
		if (value != null)
			eRdn.setText(value);
		//System.out.println("Fin de la funcion createRdn");
		return eRdn;
	}

	private static void insertRdnPresentation(Element nodePresentationRoot, Element eRdn, String idParent) {
		//System.out.println("Inicio de la funcion insertRdnPresentation");
		if (nodePresentationRoot!=null) {
			String ordenacion = nodePresentationRoot.getAttributeValue(QueryConstants.SELECT);
			StringBuffer orden = new StringBuffer(eRdn.getAttributeValue(QueryConstants.ID));
			if (ordenacion!=null)
				orden.append("," + ordenacion);
			nodePresentationRoot.setAttribute(QueryConstants.SELECT,orden.toString());
			
			String ordenacionIdo = nodePresentationRoot.getAttributeValue(QueryConstants.SELECT_IDO);
			//insertar el rdn tras idParent
			boolean insertado = false;
			StringBuffer ordenIdo = new StringBuffer("");
			String[] ordenacionIdoSpl = ordenacionIdo.split(",");
			for (int i=0;i<ordenacionIdoSpl.length;i++) {
				String ord = ordenacionIdoSpl[i];
				if (ordenIdo.length()>0)
					ordenIdo.append(",");
				ordenIdo.append(ord);
				if (ord.equals(idParent)) {
					ordenIdo.append("," + eRdn.getAttributeValue(QueryConstants.ID));
					insertado = true;
				}
			}
			if (!insertado)
				ordenIdo.append("," + eRdn.getAttributeValue(QueryConstants.ID));
			nodePresentationRoot.setAttribute(QueryConstants.SELECT_IDO,ordenIdo.toString());
		}
		//System.out.println("Fin de la funcion insertRdnPresentation");
	}
	private static void insertRdnStructure(Element nodeHijo, Element eRdn) {
		/*List lista = nodeHijo.getChildren();
		int size = lista.size();
		Element[] tmp = new Element[size];
		for (int i = 0; i < size; i++)
			tmp[i] = (Element) lista.get(i);
		for (int i = 0; i < size; i++)
			((Element) tmp[i]).detach();*/
		nodeHijo.addContent(eRdn);
		//for (int i = 0; i < size; i++)
			//nodeHijo.addContent((Element) tmp[i]);
	}

}
