package dynagent.reports.app.old;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

import dynagent.ejb.Auxiliar;
import dynagent.ejb.ConnectionDB;
import dynagent.ejb.FactoryConnectionDB;
import dynagent.ejb.helperConstant;
import dynagent.ejb.jdomParser;
import dynagent.ruleengine.src.xml.QueryXML;

public class Adaptador {
	private static int id = 1;
	private static boolean casoUnionReports = false;
	
	private static void setCasoUnionReports(Element query) {
		int children = query.getChildren().size();
		if (children>1)
			casoUnionReports = true;
	}
	
	private static int getNextId() {
		int nextId = 0;
		//se reserva el id=1 para el master
		if (casoUnionReports && id==1)
			id++;
		nextId = id;
		id++;
		return nextId;
	}
	
	public static void adaptaIDs(Element query, FactoryConnectionDB fcdb) {
		setCasoUnionReports(query);
		Iterator it = query.getChildren().iterator();
		while (it.hasNext()) {
			Element structure = (Element)it.next();
			Element nodoClass = structure.getChild("CLASS");
			if (nodoClass!=null) {
				HashMap<String,Integer> ids = new HashMap<String,Integer>();
				adaptaIDsStructure(nodoClass, ids, fcdb);
				String id = nodoClass.getAttributeValue("ID");
				Element presentation = structure.getChild("PRESENTATION");
				if (presentation!=null) {
					Element view = presentation.getChild("VIEW");
					if (view!=null) {
						getIdNodo(view, "ID", ids);
						System.out.println("id " + id);
						System.out.println("idView " + view.getAttributeValue("ID"));
						if (StringUtils.equals(id, view.getAttributeValue("ID"))) {
							adaptaIDsPresentation(view, nodoClass, ids);
							
						}
					}
				}
			}
		}
	}
	
	//Nodo Presentation
	private static void adaptaIDsPresentation(Element view, Element nodoClass, HashMap<String,Integer> ids) {
		ArrayList<String> idsAgregation = new ArrayList<String>();
		Iterator it = view.getChildren("AGREGATION").iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			adaptaIDsAgregExpr(child, ids, idsAgregation);
		}
		it = view.getChildren("EXPRESION").iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			adaptaIDsAgregExpr(child, ids, new ArrayList<String>());
			adaptaIdTmExpr(child, nodoClass);
		}
		it = view.getChildren("WHERE").iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			adaptaIDsWhere(child, ids);
			adaptaIdTmWhere(child, nodoClass);
		}
		
		String idsSelect = view.getAttributeValue("SELECT");
		ArrayList<String> aIdsSelectImportado = new ArrayList<String>();
		if (idsSelect!=null)
			aIdsSelectImportado = adaptaSelectPresentation(idsSelect, ids, view);
		
		String idsGroup = view.getAttributeValue("GROUPBY");
		ArrayList<String> aIdsGroupImportado = new ArrayList<String>();
		if (idsGroup!=null)
			aIdsGroupImportado = adaptaGroupPresentation(idsGroup, ids, idsAgregation, nodoClass, view);
		
		String idsOrder = view.getAttributeValue("ORDERBY");
		if (idsOrder!=null)
			adaptaOrderPresentation(idsOrder, aIdsSelectImportado, ids, view);
		
		it = view.getChildren("OP").iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			adaptaIDsOP(child, ids);
		}

		ArrayList<String> viewIds = new ArrayList<String>();
		it = view.getChildren("VIEW").iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			getIdNodo(child, "ID", ids);
			viewIds.add(child.getAttributeValue("ID"));
		}
		//añade multivalue cuando sea necesario
		adaptaMultivalueStructure(aIdsGroupImportado, nodoClass, /*view.getAttributeValue("ID"), */viewIds);
			
		it = view.getChildren("VIEW").iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			Element childStructure = jdomParser.findElementByAt(nodoClass, "ID", child.getAttributeValue("ID"), 
					true, "CLASS");
			adaptaIDsPresentation(child, childStructure, ids);
		}
	}
	private static void adaptaIdTmWhere(Element where, Element nodoClass) {
		String idAttributeStr = nodoClass.getAttributeValue("ID_IZQ");
		Element attribute = jdomParser.findElementByAt(nodoClass, "ID", idAttributeStr, true);
		if (attribute!=null)
			where.setAttribute("ID_TM",attribute.getAttributeValue("ID_TM"));
		else {
			attribute = jdomParser.findElementByAt(where.getParent(), "ID", idAttributeStr, true);
			if (attribute!=null && (attribute.getName().equals("AGREGATION") || attribute.getName().equals("EXPRESION")))
				where.setAttribute("ID_TM",attribute.getAttributeValue("ID_TM"));
		}
	}
	
	private static void adaptaIDsWhere(Element where, HashMap<String,Integer> ids) {
		adaptaIdNodo(where, ids);
		getIdNodo(where,"ID_IZQ",ids);
		getIdNodo(where,"ID_DER",ids);
	}
	
	private static void adaptaIdTmExpr(Element elem, Element nodoClass) {
		//nota ir descartando parentesis que están a la izquierda
		String op = elem.getAttributeValue("OP_EXPRES");
		int inicio = 0;
		while (inicio<op.length()) {
			if (op.charAt(inicio)=='(')
				inicio++;
			else
				break;
		}
		//ver el 1er dato, casos posibles:
		//1.- que sea una funcion:
			//funciones que devuelven string -> id_tm=string
			//funciones que devuelven int -> id_tm=quantity
			//TODO cast and convert
		//2.- que sea un campo -> id_tm del campo, por ejemplo campoX + substring(...)
		
		if (op.charAt(inicio)=='{') {
			//se trata de un campo
			int fin = op.indexOf('}',inicio+1);
			String idAttributeStr = op.substring(inicio+1, fin);
			//buscar idtm de este campo
			Element attribute = jdomParser.findElementByAt(nodoClass, "ID", idAttributeStr, true);
			if (attribute!=null)
				elem.setAttribute("ID_TM",attribute.getAttributeValue("ID_TM"));
			else {
				attribute = jdomParser.findElementByAt(elem.getParent(), "ID", idAttributeStr, true);
				if (attribute!=null && attribute.getName().equals("AGREGATION"))
					elem.setAttribute("ID_TM",String.valueOf(helperConstant.TM_QUANTITY));
			}
		} else {
			int fin = op.indexOf('(',inicio+1);
			String function = op.substring(inicio, fin).toLowerCase(); 
			if (function.equals("substring") 
					|| function.equals("lower") || function.equals("upper")
					|| function.equals("rtrim") || function.equals("ltrim")
					|| function.equals("right") || function.equals("left")
					|| function.equals("reverse")
					|| function.equals("space")
					|| function.equals("replicate")
					|| function.equals("stuff"))
				elem.setAttribute("ID_TM",String.valueOf(helperConstant.TM_STRING));
			else if (function.equals("len")
					|| function.equals("round")
					|| function.equals("floor") || function.equals("ceiling")
					|| function.equals("rand")
					|| function.equals("square") || function.equals("sqrt")
					|| function.equals("exp")
					|| function.equals("abs"))
				elem.setAttribute("ID_TM",String.valueOf(helperConstant.TM_QUANTITY));
			//TODO esto se puede optimizar al incluirlo en las querys, distinguir entre double(quantity) y int(class)
			//tenerlo en cuenta en queryData para darle un idtmRuleengine apropiado util a la hora de crear el instance
		}
	}
	
	/*
	 * 3 casos:
	 * 
	 * 1.- no hay agrupacion:
	 * en todos los nodos -> nodoClass.setAttribute("MULTIVALUE","TRUE");  !nodoClass.setAttribute("GROUP","TRUE");
	 * no se agrupa por ninguno solo por el 1er (por el cual se agrupa siempre)
	 * 
	 * 2.- se agrupa tambien por nodos inferiores:
	 * Todos los nodos tendran MULTIVALUE="TRUE" excepto los hijos de los nodos que están en GROUPBY
	 *  	nodoClass.setAttribute("MULTIVALUE","TRUE");
	 * y en el nodo del GROUPBY:
	 *  	nodoClass.setAttribute("GROUP","TRUE");
	 * ya que una vez que agrupe el MULTIVALUE no sera necesario porque devuelve un unico registro por ID_O agrupado
	 * 
	 * 3.- se agrupa solo por el nodo root (caso actual en QueryXML)
	 * en todos los nodos -> !nodoClass.setAttribute("MULTIVALUE","TRUE");  nodoClass.setAttribute("GROUP","TRUE");
	 * este caso no es correcto, se agrupara solo por el root, entra dentro del caso 2
	 */
	//se le pasa un array con los siguientes ids para que sepa hasta donde iterar
	
	private static void adaptaMultivalueStructure(ArrayList<String> aIdsGroupImportado, Element nodoClass, /*String idView, */ArrayList<String> viewIds) {
		boolean existsGroup = aIdsGroupImportado.size()>0;
		adaptaMultivalueStructureCaso1(nodoClass, viewIds);
		if (!existsGroup)
			adaptaMultivalueStructureCaso1(nodoClass, viewIds);
		else {
			adaptaMultivalueStructureCaso1(nodoClass, viewIds);
			for (int i=0;i<aIdsGroupImportado.size();i++) {
				String[] names = new String[2];
				names[0]="CLASS";
				names[1]="XOR";
				Element group = jdomParser.findElementByAt(nodoClass, names, "ID", aIdsGroupImportado.get(i), true, true);
				group.setAttribute("GROUP","TRUE");
				adaptaMultivalueStructureCaso2(group, viewIds);
				//if (aIdsGroupImportado.size()==1 && StringUtils.equals(aIdsGroupImportado.get(0), idView))
					//adaptaMultivalueStructureCaso3(nodoClass, viewIds);
				//else
					//adaptaMultivalueStructureCaso2(aIdsGroupImportado, nodoClass, viewIds);
			}
		}
	}
	private static void adaptaMultivalueStructureCaso2(Element nodoClass, ArrayList<String> viewIds) {
		if (StringUtils.equals(nodoClass.getAttributeValue("MULTIVALUE"),"TRUE")) {  //para no volver a quitar multivalue 
																					 //de group que esten bajo group
			Iterator it = nodoClass.getChildren().iterator();
			while (it.hasNext()) {
				Element childClass = (Element) it.next();
				String name = childClass.getName();
				if (StringUtils.equals(name, "CLASS") || StringUtils.equals(name, "XOR")) {
					if (!viewIds.contains(childClass.getAttributeValue("ID"))) {
						adaptaMultivalueStructureCaso2(childClass, viewIds);
						childClass.removeAttribute("MULTIVALUE");
					}
				}
			}
		}
	}
	private static void adaptaMultivalueStructureCaso1(Element nodoClass, ArrayList<String> viewIds) {
		nodoClass.setAttribute("MULTIVALUE","TRUE");
		Iterator it = nodoClass.getChildren().iterator();
		while (it.hasNext()) {
			Element childClass = (Element) it.next();
			String name = childClass.getName();
			if (StringUtils.equals(name, "CLASS") || StringUtils.equals(name, "XOR")) {
				if (!viewIds.contains(childClass.getAttributeValue("ID")))
					adaptaMultivalueStructureCaso1(childClass, viewIds);
			}
		}
	}
	
	private static ArrayList<String> adaptaSelectPresentation(String idsSG, HashMap<String,Integer> ids, Element view) {
		String[] idsSGSpl = idsSG.split(",");
		ArrayList<String> aIdsImportados = new ArrayList<String>();
		for (int i=0;i<idsSGSpl.length;i++) {
			String id = idsSGSpl[i].replaceAll(" ", "");
			Integer idInt = ids.get(id);
			if (idInt!=null)
				aIdsImportados.add(String.valueOf(idInt));
		}
		view.setAttribute("SELECT",Auxiliar.arrayToString(aIdsImportados));
		return aIdsImportados;
	}
	private static ArrayList<String> adaptaGroupPresentation(String idsSG, HashMap<String,Integer> ids, ArrayList<String> idsAgregation, 
			Element nodoClass, Element view) {
		String[] idsSGSpl = idsSG.split(",");
		ArrayList<String> aIdsImportados = new ArrayList<String>();
		for (int i=0;i<idsSGSpl.length;i++) {
			String id = idsSGSpl[i].replaceAll(" ", "");
			Integer idInt = ids.get(id);
			if (idInt!=null) {
				String idIntStr = String.valueOf(idInt);
				if (!aIdsImportados.contains(idIntStr))
					aIdsImportados.add(idIntStr);
			}
		}
		//ahora se añaden los ids de los padres de los nodos agregation si no estan
		ArrayList<Element> attributesAgregados = jdomParser.findElementsByAt(nodoClass, "ATTRIBUTE", "ID", idsAgregation, true);
		for (int i=0;i<attributesAgregados.size();i++) {
			String idIntStr = attributesAgregados.get(i).getParent().getAttributeValue("ID");
			if (!aIdsImportados.contains(idIntStr))
				aIdsImportados.add(idIntStr);
		}
		view.setAttribute("GROUPBY",Auxiliar.arrayToString(aIdsImportados));
		return aIdsImportados;
	}

	private static void adaptaOrderPresentation(String idsO, ArrayList<String> aIdsSelectImportado, HashMap<String,Integer> ids, 
			Element view) {
		String[] idsOrderSpl = idsO.split(",");
		String idImportado = "";
		for (int i=0;i<idsOrderSpl.length;i++) {
			String idSentido = idsOrderSpl[i].replaceAll(" ", "");
			String[] idSentidoSpl = idSentido.split("#");
			String id = idSentidoSpl[0];
			Integer idInt = ids.get(id);
			if (idInt!=null) {
				String idStr = String.valueOf(idInt);
				if (aIdsSelectImportado.contains(idStr)) {
					if (idImportado.length()>0)
						idImportado += ",";
					idImportado += idStr;
					if (idSentidoSpl.length>1) {
						String sentido = idSentidoSpl[1];
						if (StringUtils.equals(sentido.toLowerCase(), "desc"))
							idImportado += "#" + helperConstant.ORDER_DESC;
						else //if (StringUtils.equals(sentido.toLowerCase(), "asc"))
							idImportado += "#" + helperConstant.ORDER_ASC;
					}
				}
			}
		}
		view.setAttribute("ORDERBY",idImportado);
	}
	
	private static void adaptaIDsOP(Element op, HashMap<String,Integer> ids) {
		String idOP = op.getAttributeValue("ID_OP").toLowerCase();
		if (StringUtils.equals(idOP, "and"))
			op.setAttribute("ID_OP",String.valueOf(helperConstant.OP_AND));
		else if (StringUtils.equals(idOP, "or"))
			op.setAttribute("ID_OP",String.valueOf(helperConstant.OP_OR));
		Iterator it = op.getChildren().iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			if (child.getName().equals("OP"))
				adaptaIDsOP(child, ids);
			else if (child.getName().equals("WHERE"))
				getIdNodo(child, "ID_WHERE", ids);
		}
	}
	
	private static void adaptaIDsAgregExpr(Element agregExpr, HashMap<String,Integer> ids, ArrayList<String> idsAgregation) {
		String op = "";
		String name = agregExpr.getName();
		if (StringUtils.equals(name, "AGREGATION"))
			op = agregExpr.getAttributeValue("OP_AGREG");
		else if (StringUtils.equals(name, "EXPRESION"))
			op = agregExpr.getAttributeValue("OP_EXPRES");
		
		adaptaIdNodo(agregExpr,ids);
		
		//"SUBSTRING({rdnsubCuenta1Debe},0,4)"
		String opImportado = "";
		if (opImportado.length()>0)
			opImportado += ";";
		boolean salir = false;
		int fin = 0;
		while (!salir) {
			int inicio = op.indexOf("{",fin);
			System.out.println("inicio " + inicio);
			System.out.println("fin " + fin);
			if (inicio!=-1) {
				opImportado += op.substring(fin,inicio+1);
				fin = op.indexOf("}",inicio);
				String idAttributeStr = op.substring(inicio+1, fin);
				Integer idAttribute = ids.get(idAttributeStr);
				String idAttributeMapStr = String.valueOf(idAttribute);
				idsAgregation.add(idAttributeMapStr);
				opImportado += idAttributeMapStr;
			} else {
				opImportado += op.substring(fin,op.length());
				salir = true;
			}
		}
		if (StringUtils.equals(name, "AGREGATION"))
			agregExpr.setAttribute("OP_AGREG",opImportado);
		else if (StringUtils.equals(name, "EXPRESION"))
			agregExpr.setAttribute("OP_EXPRES",opImportado);
	}
	
	//
	private static void getIdNodo(Element elem, String etiq, HashMap<String,Integer> ids) {
		String idStr = elem.getAttributeValue(etiq);
		if (idStr!=null) {
			Integer idInt = ids.get(idStr);
			if (idInt!=null)
				elem.setAttribute(etiq, String.valueOf(idInt));
		}
	}
	
	private static void adaptaIdNodo(Element elem, HashMap<String,Integer> ids) {
		String idStr = elem.getAttributeValue("ID");
		int nextId = getNextId();
		elem.setAttribute("ID", String.valueOf(nextId));
		if (idStr!=null)
			ids.put(idStr,nextId);
	}
	
	//Nodo Structure
	private static void adaptaIDsStructure(Element elem, HashMap<String,Integer> ids, FactoryConnectionDB fcdb) {
		String name = elem.getName();
		//adapta ID
		adaptaIdNodo(elem, ids);
		if (!StringUtils.equals(name, "XOR")) {
			//adapta PROP e incluye ID_TM
			adaptaPropIdTmStructure(elem, fcdb);
			
			if (StringUtils.equals(name, "CLASS")) {
				//adapta ID_TO
				adaptaIdToStructure(elem, fcdb);
				//añade REVERSED si la property está invertida
				adaptaReversedStructure(elem, fcdb);
			}
		}
		Iterator it = elem.getChildren().iterator();
		while (it.hasNext())
			adaptaIDsStructure((Element)it.next(), ids, fcdb);
	}
	
	private static void adaptaPropIdTmStructure(Element elem, FactoryConnectionDB fcdb) {
		String propS = elem.getAttributeValue("PROP");
		if (propS!=null) {
			ConnectionDB conDb = null;
			Statement st = null;
			ResultSet rs = null;
			System.out.println(propS);
			try {
				if (!Auxiliar.hasIntValue(propS)) {
					String sql = "SELECT prop, valuecls FROM properties WHERE name='" + propS + "'";
					conDb = fcdb.createConnection(true);
					st = conDb.getBusinessConn().createStatement();
					rs = st.executeQuery(sql);
					if (rs.next()) {
						int prop = rs.getInt(1);
						propS = (new Integer(prop)).toString();
						elem.setAttribute("PROP",propS);
						if (!StringUtils.equals(elem.getName(),"CLASS")) {
							String tm = rs.getString(2);
							elem.setAttribute("ID_TM",(new Integer(QueryXML.toTypeQuery(Integer.parseInt(tm)))).toString());
						}
					}
				} else if (!StringUtils.equals(elem.getName(),"CLASS")) {
					String sql = "SELECT valuecls FROM properties WHERE prop=" + propS;
					conDb = fcdb.createConnection(true);
					st = conDb.getBusinessConn().createStatement();
					rs = st.executeQuery(sql);
					if (rs.next()) {
						String tm = rs.getString(1);
						elem.setAttribute("ID_TM",(new Integer(QueryXML.toTypeQuery(Integer.parseInt(tm)))).toString());
					}
				}
			} catch(SQLException e) {
	      		System.out.println("ERROR SQLException:");
				e.printStackTrace();
			} catch(NamingException e) {
	      		System.out.println("ERROR NamingException:");
				e.printStackTrace();
			} finally {
				try {
					if (rs!=null)
						rs.close();
					if (st!=null)
						st.close();
					if (conDb!=null)
						conDb.close();
				} catch(SQLException e) {
		      		System.out.println("ERROR SQLException:");
					e.printStackTrace();
				}
			}
		} else if (elem.getText()!=null)
			elem.setAttribute("ID_TM",String.valueOf(helperConstant.TM_ATTRIBUTEVALUE));
	}
	
	private static void adaptaIdToStructure(Element elem, FactoryConnectionDB fcdb) {
		String idtoS = elem.getAttributeValue("ID_TO");
		if (idtoS!=null && !Auxiliar.hasIntValue(idtoS)) {
			ConnectionDB conDb = null;
			Statement st = null;
			ResultSet rs = null;
			String sql = "SELECT IDTO FROM Clases WHERE NAME='" + idtoS + "'";
			try {
				conDb = fcdb.createConnection(true);
				st = conDb.getBusinessConn().createStatement();
				rs = st.executeQuery(sql);
				if (rs.next()) {
					int idto = rs.getInt(1);
					idtoS = (new Integer(idto)).toString();
					elem.setAttribute("ID_TO",idtoS);
				}
			} catch(SQLException e) {
	      		System.out.println("ERROR SQLException:");
				e.printStackTrace();
			} catch(NamingException e) {
	      		System.out.println("ERROR NamingException:");
				e.printStackTrace();
			} finally {
				try {
					if (rs!=null)
						rs.close();
					if (st!=null)
						st.close();
					if (conDb!=null)
						conDb.close();
				} catch(SQLException e) {
		      		System.out.println("ERROR SQLException:");
					e.printStackTrace();
				}
			}
		}
	}
	
	private static Element getParentClass(Element root) {
		Element parent = null;
		String name = root.getName();
		if (StringUtils.equals(name, "XOR"))
			parent = getParentClass(root.getParent());
		else
			parent = root;
		return parent;
	}
	
	private static void adaptaReversedStructure(Element elem, FactoryConnectionDB fcdb) {
		String idtoS = elem.getAttributeValue("ID_TO");
		String propS = elem.getAttributeValue("PROP");
		if (idtoS!=null && propS!=null) {
			Element parent = getParentClass(elem.getParent());
			String idtoParentS = parent.getAttributeValue("ID_TO");
			//se comprueba si es null porque puede que el padre sea Structure
			if (idtoParentS!=null) {
				ConnectionDB conDb = null;
				Statement st = null;
				ResultSet rs = null;
				String sql = "SELECT count(*) FROM instances WHERE IDTO=" + idtoParentS + 
							" AND PROPERTY=" + propS + " AND VALUECLS=" + idtoS + " AND (OP='OR' OR OP='AND')";
				System.out.println("SQLREVERSED " + sql);
				try {
					conDb = fcdb.createConnection(true);
					st = conDb.getBusinessConn().createStatement();
					rs = st.executeQuery(sql);
					if (rs.next()) {
						int count = rs.getInt(1);
						if (count==0)
							elem.setAttribute("REVERSED","TRUE");
					}
				} catch(SQLException e) {
		      		System.out.println("ERROR SQLException:");
					e.printStackTrace();
				} catch(NamingException e) {
		      		System.out.println("ERROR NamingException:");
					e.printStackTrace();
				} finally {
					try {
						if (rs!=null)
							rs.close();
						if (st!=null)
							st.close();
						if (conDb!=null)
							conDb.close();
					} catch(SQLException e) {
			      		System.out.println("ERROR SQLException:");
						e.printStackTrace();
					}
				}
			}
		}
	}
}
