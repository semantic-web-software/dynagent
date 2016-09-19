package dynagent.server.services.old;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.Rdn;
import dynagent.common.utils.jdomParser;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.QueryService;
import dynagent.server.services.reports.ParQuery;

public class ReportService {

	private FactoryConnectionDB factConnDB = null;
	
	private IKnowledgeBaseInfo ik;
	
	private boolean generateQuery;
	
	private boolean parser;

	public ReportService(FactoryConnectionDB factConnDB, IKnowledgeBaseInfo ik, boolean generateQuery, boolean parser) 
			throws JDOMException, SQLException {
		//System.out.println("Inicio de la funcion ReportService");
		this.factConnDB = factConnDB;
		this.ik = ik;
		this.generateQuery = generateQuery;
		this.parser = parser;
		//System.out.println("Fin de la funcion ReportService");
	}
	
	public HashMap<String,ParQuery> Report(Element root, String idMaster) 
			throws JDOMException, SQLException, NamingException, NotFoundException, IncoherenceInMotorException, ParseException {
		//System.out.println("Inicio de la funcion Report");
		if (parser) {
			QueryReportParser qrp = new QueryReportParser(factConnDB, true, false, true);
			qrp.parserIDs(root);
		}
		
		HashMap<String,ParQuery> hrq = new HashMap<String,ParQuery>(); 

		int numStructure = 0;
		//que sean structure o union
		Iterator it = root.getChildren().iterator();
		while (it.hasNext()) {
			Element nodoStructure = (Element)it.next();
			if (nodoStructure.getName().equals(QueryConstants.UNION) || nodoStructure.getName().equals(QueryConstants.STRUCTURE))
				numStructure++;
		}
		Iterator it2 = root.getChildren().iterator();
		while (it2.hasNext()) {
			Element nodoStructure = (Element)it2.next();
			if (nodoStructure.getName().equals(QueryConstants.UNION) || nodoStructure.getName().equals(QueryConstants.STRUCTURE)) {
				Element view = null;
				if (!nodoStructure.getName().equals(QueryConstants.UNION))
					view = nodoStructure.getChild(QueryConstants.PRESENTATION).getChild(QueryConstants.VIEW);
				dbFindReport(nodoStructure, view, hrq, new HashMap<String,Integer>());
			}
		}
		if (numStructure>1)
			anadeMasterBlanco(root, hrq, idMaster);
		//RESULTADOS:
		//empresa, hrq y factConnDB
		/*System.out.println("*******************************************************************************************");
		System.out.println("RESULTADO HRQ");
		System.out.println("*******************************************************************************************");
		
		if (numStructure>1) {
			System.out.println("ELEMENTO " + idMaster + " :");
			System.out.println(hrq.get(idMaster).toString());
		}
			
		it = root.getChildren().iterator();
		while (it.hasNext()) {
			Element nodoStructure = (Element)it.next();
			if (nodoStructure.getName().equals(QueryConstants.UNION)) {
//				Element nodoOrder = nodoStructure.getChild(QueryConstants.ORDER);
//				if (nodoOrder!=null) {
//					if (StringUtils.equals(nodoStructure.getAttributeValue(QueryConstants.INDEX), nodoOrder.getAttributeValue(QueryConstants.INDEX))) {
						int elemento = Integer.parseInt(nodoStructure.getAttributeValue(QueryConstants.INDEX));
						System.out.println("ELEMENTO " + elemento + " :");
						System.out.println(hrq.get(elemento).toString());
//					}
//				}
				Iterator iterador = nodoStructure.getChildren(QueryConstants.STRUCTURE).iterator();
				while(iterador.hasNext()) {
					Element childStructure = (Element)iterador.next();
					Iterator iterador3 = childStructure.getChildren(QueryConstants.PRESENTATION).iterator();
					while (iterador3.hasNext()) {
						Element childPresentation = (Element)iterador3.next();
						Iterator iterador4 = childPresentation.getChildren(QueryConstants.VIEW).iterator();
						while (iterador4.hasNext()) {
							Element view = (Element)iterador4.next();
							Iterator iterador5 = view.getChildren(QueryConstants.VIEW).iterator();
							while (iterador5.hasNext()) {
								Element view2 = (Element)iterador5.next();
								ArrayList<Integer> viewIdsChild = new ArrayList<Integer>();
								funcionTemporalIDs(view2, viewIdsChild);
								for (int i=0;i<viewIdsChild.size();i++) {
									int elem = viewIdsChild.get(i);
									System.out.println("ELEMENTO " + elem + " :");
									System.out.println(hrq.get(elem).toString());
								}
							}
						}
					}
				}
			} else {
				Element view = nodoStructure.getChild(QueryConstants.PRESENTATION).getChild(QueryConstants.VIEW); 
				ArrayList<Integer> viewIdsChild = new ArrayList<Integer>();
				funcionTemporalIDs(view, viewIdsChild);
				for (int i=0;i<viewIdsChild.size();i++) {
					int elem = viewIdsChild.get(i);
					System.out.println("ELEMENTO " + elem + " :");
					System.out.println(hrq.get(elem).toString());
				}
			}
		}
		System.out.println("*******************************************************************************************");
		*/
		System.out.println("*******************************************************************************************");
		System.out.println("RESULTADO HRQ");
		System.out.println("*******************************************************************************************");
		
		it = hrq.keySet().iterator();
		while (it.hasNext()) {
			String key = (String)it.next();
			System.out.println("ELEMENTO " + key + " :");
			System.out.println(hrq.get(key).toString());
		}
		System.out.println("*******************************************************************************************");

		//System.out.println("Fin de la funcion Report");
		return hrq;
	}
	
	/*private void funcionTemporalIDs(Element view, ArrayList<Integer> ids) {
		ids.add(Integer.parseInt(view.getAttributeValue(QueryConstants.INDEX)));
		Iterator iterador = view.getChildren(QueryConstants.VIEW).iterator();
		while(iterador.hasNext()) {
			Element viewChild = (Element)iterador.next();
			funcionTemporalIDs(viewChild, ids);
		}
	}*/

	private void anadeMasterBlanco(Element root, HashMap<String,ParQuery> hrq, String idMaster) {
		String idsSubQuery = "";
		Iterator it = root.getChildren().iterator();
		while (it.hasNext()) {
			Element nodoStructure = (Element)it.next();
			if (nodoStructure.getName().equals(QueryConstants.UNION) || nodoStructure.getName().equals(QueryConstants.STRUCTURE)) {
				if (!nodoStructure.getName().equals(QueryConstants.UNION)) {
					Element view = nodoStructure.getChild(QueryConstants.PRESENTATION).getChild(QueryConstants.VIEW);
					if (idsSubQuery.length()>0)
						idsSubQuery += ",";
					idsSubQuery += view.getAttributeValue(QueryConstants.ID);
				} else {
					if (idsSubQuery.length()>0)
						idsSubQuery += ",";
					idsSubQuery += nodoStructure.getAttributeValue(QueryConstants.ID);
				}
			}
		}
		ParQuery rq = new ParQuery("",idsSubQuery,"","");
		hrq.put(idMaster, rq);
	}
	
	private void dbFindReport(Element nodoStructure, Element viewPresentationActual, HashMap<String,ParQuery> hrq, 
			HashMap<String,Integer> mLastIDO) throws SQLException, JDOMException, NotFoundException, IncoherenceInMotorException, ParseException {
		//System.out.println("Inicio de la funcion dbFindReport");
		//iterar aqui con group
		ArrayList<String> viewIdsChild = new ArrayList<String>();
		ArrayList<Element> aViewChild = new ArrayList<Element>();
		boolean isUnion = nodoStructure.getName().equals(QueryConstants.UNION);
		String id = null;
		Element nodoStructureActual = null;
		if (isUnion) {
			id = nodoStructure.getAttributeValue(QueryConstants.ID);
			Iterator iterador = nodoStructure.getChildren(QueryConstants.STRUCTURE).iterator();
			while(iterador.hasNext()) {
				Element childStructure = (Element)iterador.next();
				Iterator iterador3 = childStructure.getChildren(QueryConstants.PRESENTATION).iterator();
				while (iterador3.hasNext()) {
					Element childPresentation = (Element)iterador3.next();
					Iterator iterador4 = childPresentation.getChildren(QueryConstants.VIEW).iterator();
					while (iterador4.hasNext()) {
						Element childView = (Element)iterador4.next();
						Iterator iterador5 = childView.getChildren(QueryConstants.VIEW).iterator();
						while (iterador5.hasNext()) {
							Element childView2 = (Element)iterador5.next();
							viewIdsChild.add(childView2.getAttributeValue(QueryConstants.ID));
							//aViewChild.add(childView2.detach());
							aViewChild.add(jdomParser.cloneTree(childView2));
						}
					}
				}
			}
		} else {
			boolean isStructure = nodoStructure.getName().equals(QueryConstants.STRUCTURE);
			id = viewPresentationActual.getAttributeValue(QueryConstants.ID);
			if (id==null && isStructure) {
				id = nodoStructure.getAttributeValue(QueryConstants.ID);
				nodoStructureActual = nodoStructure;
			} else {
				String[] names = new String[2];
				names[0] = QueryConstants.CLASS;
				names[1] = QueryConstants.STRUCTURE;
				nodoStructureActual = jdomParser.findElementByAt(nodoStructure, names, QueryConstants.ID, id, true, true);
			}
			Iterator iterador2 = viewPresentationActual.getChildren(QueryConstants.VIEW).iterator();
			while(iterador2.hasNext()) {
				Element childView = (Element)iterador2.next();
				viewIdsChild.add(childView.getAttributeValue(QueryConstants.ID));
			}
		}
		//troceo el xml y creo un array de Element
		Element nuevoXML = new Element(QueryConstants.QUERY);
		
		Element childStructure = null;
		//copio los nodos que me hagan falta del root hasta que llegue a un id que este contenido en viewHijos
		if (isUnion) {
			childStructure = jdomParser.cloneNode(nodoStructure);
			int cont = 1;
			Iterator it = nodoStructure.getChildren(QueryConstants.STRUCTURE).iterator();
			while (it.hasNext()) {
				Element childActual = (Element)it.next();
				Element childStructure2 = jdomParser.cloneNode(childActual);
				//Añado nodo rdn
				copiaParteSubStructure(childActual, childStructure2, viewIdsChild);
				Rdn.insertRDNRoot(QueryConstants.ATTRIBUTE, "0_u"+cont, childStructure2, null, true, true);
				cont++;
				childStructure.addContent(childStructure2);
				childStructure2.setAttribute(QueryConstants.REPORT,"TRUE");
				
				Iterator it2 = childActual.getChildren(QueryConstants.PRESENTATION).iterator();
				while (it2.hasNext()) {
					Element childPresentation = (Element)it2.next();
					Element childPresentationClone  = new Element(QueryConstants.PRESENTATION);
					Iterator it3 = childPresentation.getChildren(QueryConstants.VIEW).iterator();
					while (it3.hasNext()) {
						Element childView = (Element)it3.next();
						copiaPartePresentation(childView, childPresentationClone);
					}
					childStructure2.addContent(childPresentationClone); //xa añadir los hijos
				}
			}
			Element childOrder = nodoStructure.getChild(QueryConstants.ORDER);
			if (childOrder!=null) {
				//if (StringUtils.equals(childOrder.getAttributeValue(QueryConstants.INDEX), idStr)) {
					Element order = jdomParser.cloneNode(childOrder);
					childStructure.addContent(order);
				//}
			}
		} else {
			if (!nodoStructureActual.getName().equals(QueryConstants.STRUCTURE)) {
				childStructure = new Element(QueryConstants.STRUCTURE);
				//Añado nodo rdn
				copiaParteStructure(nodoStructureActual, childStructure, viewIdsChild);
				posCopiaXML(childStructure, nodoStructureActual);
			} else {
				childStructure = jdomParser.cloneNode(nodoStructureActual);
				copiaParteSubStructure(nodoStructureActual, childStructure, viewIdsChild);
			}
			childStructure.setAttribute(QueryConstants.REPORT,"TRUE");
			Rdn.insertRDNRoot(QueryConstants.ATTRIBUTE, "0", childStructure, null, true, true);
			Element childPresentation = new Element(QueryConstants.PRESENTATION);
			copiaPartePresentation(viewPresentationActual, childPresentation);
			childStructure.addContent(childPresentation); //xa añadir los hijos
		}
		nuevoXML.addContent(childStructure); //xa añadir los hijos
		
		//itero sobre ellos
		//llamando a estas funciones pudiendoles pasar consulta
		QueryService qs = new QueryService(factConnDB, ik);
				
		String whereEnlace = "";
		if (generateQuery /*&& !isUnion*/) {
			Integer lastIDOid = mLastIDO.get(id);
			//si ido e idoRel son nulas -> es el 1er nodo
			if (lastIDOid!=null) {
				int idoValNum = lastIDOid;
				//modificar el whereInt con las condiciones que sean necesarias
				String name = nodoStructureActual.getName();
				Element parent = nodoStructureActual.getParent();
	
				//es un xml sin modificar
				//ahora siempre tendremos el mismo caso:
				if (name.equals(QueryConstants.CLASS) && parent.getName().equals(QueryConstants.CLASS))
					whereEnlace += "V1.ID_O=__" + idoValNum + "__";
			}
		}
		
		String sqlCompleta = qs.dbFindQuery(nuevoXML, whereEnlace, generateQuery);
		guardaParQuery(Auxiliar.arrayToString(viewIdsChild, ","), id, sqlCompleta, qs.getShowRows(), qs.getHideRows(), hrq);
		
		if (isUnion) {
			for (int i=0;i<aViewChild.size();i++) {
				String idActual = aViewChild.get(i).getAttributeValue(QueryConstants.ID);
				String[] names = new String[2];
				names[0] = QueryConstants.CLASS;
				names[1] = QueryConstants.STRUCTURE;
				nodoStructureActual = jdomParser.findElementByAt(nodoStructure, names, QueryConstants.ID, idActual, true, true);
				//buscar nodoStructureActual de aViewChild.get(i)
				dbFindReport(nodoStructureActual, aViewChild.get(i), hrq, qs.getMLastIDO());
			}
		} else {
			Iterator iterador = viewPresentationActual.getChildren(QueryConstants.VIEW).iterator();
			while(iterador.hasNext()) {
				Element childView = (Element)iterador.next();
				//actualizar ido, idoRel y pasarlo como parametro
				//pasar tambien nodoStructureActual para ir acortando la busqueda, los siguientes ids estaran bajo este nodo
				dbFindReport(nodoStructureActual, childView, hrq, qs.getMLastIDO());
			}
		}
		//System.out.println("Fin de la funcion dbFindReport");
	}
	
	private void copiaParteStructure(Element nodoStructure, Element nodoStructureClone, ArrayList viewIdsChild) {
		//System.out.println("Inicio de la funcion copiaParteStructure");
		Element rootClone = jdomParser.cloneNode(nodoStructure);
		nodoStructureClone.addContent(rootClone);
		copiaParteSubStructure(nodoStructure, rootClone, viewIdsChild);
		//System.out.println("Fin de la funcion copiaParteStructure");
	}
	
	private void copiaParteSubStructure(Element nodoStructure, Element nodoStructureClone, ArrayList viewIdsChild) {
		//System.out.println("Inicio de la funcion copiaParteSubStructure");
		Iterator iterador = nodoStructure.getChildren().iterator();
		while (iterador.hasNext()) {
			Element child = (Element)iterador.next();
			if (!child.getName().equals(QueryConstants.PRESENTATION)) {
				if (!viewIdsChild.contains(child.getAttributeValue(QueryConstants.ID))) {
					Element childClone = jdomParser.cloneNode(child);
					nodoStructureClone.addContent(childClone); //xa añadir los hijos
					copiaParteSubStructure(child, childClone, viewIdsChild);
				} else {
					//SIG_IDS va en el ultimo class
					//child es el siguiente nodo, ya que me he encontrado un id
					//child.getParent o nodoStructureClone seria el ultimo nodo
					//nodoStructureClone.setAttribute(QueryConstants.LAST, "TRUE");
					String name = child.getName();
					if (name.equals(QueryConstants.CLASS) || name.equals(QueryConstants.XOR)) {
						Element childClone = jdomParser.cloneNode(child);
						nodoStructureClone.addContent(childClone);
						if (name.equals(QueryConstants.CLASS)) {
							String lastInf = nodoStructureClone.getAttributeValue(QueryConstants.SIG_IDS);
							String id = child.getAttributeValue(QueryConstants.ID); 
							if (lastInf!=null && lastInf.length()>0) {
								lastInf += ",";
								lastInf += id;
							} else
								lastInf = id;
							nodoStructureClone.setAttribute(QueryConstants.SIG_IDS, lastInf);
						}
					}
				}
			}
		}
		//System.out.println("Fin de la funcion copiaParteSubStructure");
	}
	
	private void posCopiaXML(Element root, Element otroRoot) {
		Element otroRootParent = otroRoot.getParent();
		if (!otroRootParent.getName().equals(QueryConstants.STRUCTURE)/* && !otroRootParent.getName().equals(QueryConstants.UNION)*/) {
			Element classRoot = root.getChild(QueryConstants.CLASS);
			if (classRoot!=null) {
				classRoot.setAttribute(QueryConstants.REQUIRED,"TRUE");
				Element parentClone = jdomParser.cloneNode(otroRoot.getParent());
				//parentClone.setAttribute(QueryConstants.MULTIVALUE,"TRUE");
				root.addContent(parentClone);
				classRoot.detach();
				parentClone.addContent(classRoot);
			}
		}
	}
		
	private void copiaPartePresentation(Element nodoView, Element nodoPresentationClone) {
		//System.out.println("Inicio de la funcion copiaPartePresentation");
		//itera sobre los hijos no view y los copia en presentationClone
		Element nodoViewClone = jdomParser.cloneNode(nodoView);
		nodoPresentationClone.addContent(nodoViewClone); //xa añadir los hijos
		//enviar childClone que es el view y nodoPresentation que es el view con sus hijos
		
		copiaRestoPresentation(nodoView, nodoViewClone); //copia los hijos q no sean view
		//System.out.println("Fin de la funcion copiaPartePresentation");
	}

	private void copiaRestoPresentation(Element view, Element viewClone) {
		//System.out.println("Inicio de la funcion copiaRestoPresentation");
		Iterator iterador2 = view.getChildren().iterator();
		while (iterador2.hasNext()) {
			Element view2 = (Element)iterador2.next();
			if (!view2.getName().equals(QueryConstants.VIEW)) {
				Element childClone = jdomParser.cloneNode(view2);
				viewClone.addContent(childClone);
				copiaRestoPresentation(view2, childClone);
			}
		}
		//System.out.println("Fin de la funcion copiaRestoPresentation");
	}

	private void guardaParQuery(String idsSubQuery, String id/*, Element root*/, String sqlCompleta, String colMostradas, String colNoMostradas,  
			HashMap<String,ParQuery> hrq) {
		//System.out.println("Inicio de la funcion guardaParQuery");
//		int id = Integer.parseInt(root.getAttributeValue(QueryConstants.INDEX));
/*		String idsSubQuery = "";
		Iterator iterador = root.getChildren(QueryConstants.VIEW).iterator();
		while (iterador.hasNext()) {
			Element nodoAgreg = (Element)iterador.next();
			if (idsSubQuery.length()>0)
				idsSubQuery += ",";
			idsSubQuery += nodoAgreg.getAttributeValue(QueryConstants.INDEX);
		}*/
		ParQuery rq = new ParQuery(sqlCompleta,idsSubQuery,colMostradas,colNoMostradas);
		hrq.put(id, rq);
		//System.out.println("Fin de la funcion guardaParQuery");
	}
}