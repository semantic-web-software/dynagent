package dynagent.server.services.old;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.knowledge.Category;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.jdomParser;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.gestorsDB.GestorsDBConstants;
import dynagent.server.services.DebugParser;


public class QueryReportParser {
	private int id = 1;
	private boolean caseUnionReports = false;
	private DebugParser debug;
	private FactoryConnectionDB fcdb;
	private IDs ids;
	private boolean exceptionErrors;
	private boolean isUnion;
	private boolean isReport;    //si no es report mostrara los idos
	private boolean toInstance;   //parseo de una query que posteriormente se transformara en instance
	
	private class IDs {
		//mapa en el que las claves seran los nombres de los nodos y como valor guardara una relacion idString-idInteger
		private HashMap<String,HashMap<String,IdDetail>> ids;
		private HashMap<String,HashSet<String>> idClasses;
		private HashMap<String,ArrayList<Element>> nodesNoUsed;
		
		private class IdDetail extends Object {
			private Integer tm;	//util solo para ATTRIBUTE
			private String name;	//util solo para nodos mostrables
			private boolean used;
			private boolean real;
			private String idParent;	//util solo para nodos mostrables
			private boolean last;	//util solo para CLASS
			
			public IdDetail() {
			}
			public Integer getTm() {
				return tm;
			}
			public void setTm(Integer tm) {
				this.tm = tm;
			}
			public boolean isUsed() {
				return used;
			}
			public void setUsed(boolean used) {
				this.used = used;
			}
			public boolean isReal() {
				return real;
			}
			public void setReal(boolean real) {
				this.real = real;
			}
			public String getName() {
				return name;
			}
			public void setName(String name) {
				this.name = name;
			}
			public String getIdParent() {
				return idParent;
			}
			public void setIdParent(String idParent) {
				this.idParent = idParent;
			}
			public boolean isLast() {
				return last;
			}
			public void setLast(boolean last) {
				this.last = last;
			}
			public String toString() {
				String dev = "name :" + name;
				if (tm!=null)
					dev += ", tm :" + tm;
				dev += ", used: " + used;
				dev += ", idParent: " + idParent;
				dev += ", last: " + last;
				return dev;
			}
		}
		
		public IDs() {
			ids = new HashMap<String,HashMap<String,IdDetail>>();
			nodesNoUsed = new HashMap<String,ArrayList<Element>>();  //nodos que no tienen atributo ID
			idClasses = new HashMap<String,HashSet<String>>();
		}
		
		public HashSet<String> getIdClasses(String idStructure) {
			return idClasses.get(idStructure);
		}

		////nodesNoUsed
		//add
		public void addNodeNoUsed(Element node) {
			String nameNode = node.getName();
			ArrayList<Element> aElem = nodesNoUsed.get(nameNode);
			if (aElem==null)
				aElem = new ArrayList<Element>();
			aElem.add(node);
			nodesNoUsed.put(nameNode, aElem);
		}
		//get
		public ArrayList<Element> getNodesNoUsed(String nameNode) {
			ArrayList<Element> nodes = nodesNoUsed.get(nameNode);
			if (nodes==null)
				nodes = new ArrayList<Element>();
			return nodes;
		}
		public ArrayList<Element> getNodesNoUsed(String[] namesNode) {
			ArrayList<Element> all = new ArrayList<Element>();
			for (int i=0;i<namesNode.length;i++)
				all.addAll(getNodesNoUsed(namesNode[i]));
			return all;
		}
		
		//ids
		//getIdDetail
		private IdDetail getDetail(String idStr, String nameNode, boolean setUsed) {
			IdDetail idD = null;
			HashMap<String,IdDetail> idsXName = ids.get(nameNode);
			if (idsXName!=null) {
				idD = idsXName.get(idStr);
				if (idD!=null) {
					if (setUsed)
						idD.setUsed(true);
				}
			}
			return idD;
		}
		public IdDetail getDetailShow(String idStr, boolean setUsed) {
			IdDetail det = getDetail(idStr, QueryConstants.ATTRIBUTE, setUsed);
			if (det==null) {
				det = getDetail(idStr, QueryConstants.AGREGATION, setUsed);
				if (det==null)
					det = getDetail(idStr, QueryConstants.EXPRESION, setUsed);
			}
			return det;
		}

		//getNoUsed
		public ArrayList<String> getNoUsed(String nameNode) {
			ArrayList<String> nu = new ArrayList<String>();
			HashMap<String,IdDetail> idsXName = ids.get(nameNode);
			if (idsXName!=null) {
				Iterator it = idsXName.keySet().iterator();
				while (it.hasNext()) {
					String key = (String)it.next();
					IdDetail idD = idsXName.get(key);
					if (idD!=null) {
						if (!idD.isUsed() && idD.isReal())
							nu.add(key);
					}
				}
			}
			return nu;
		}
		
		public String getNameNodeShow(String idStr) {
			String nameNode = null;
			if (this.is(idStr, QueryConstants.ATTRIBUTE, false))
				nameNode = QueryConstants.ATTRIBUTE;
			else if (this.is(idStr, QueryConstants.AGREGATION, false))
				nameNode = QueryConstants.AGREGATION;
			else if (this.is(idStr, QueryConstants.EXPRESION, false))
				nameNode = QueryConstants.EXPRESION;
			return nameNode;
		}
		//put
		public boolean put(Element elem, String idStr, String nameNode, String name, 
				String idParent, String idStructure, boolean real) {
			boolean insert = false;
			//antes de insertar ver si ya está, si es asi dev false
			boolean esta = false;
			Iterator it = ids.keySet().iterator();
			while (it.hasNext() && !esta) {
				String nameNodeIds = (String)it.next();
				if (!nameNodeIds.equals(nameNode)) {
					HashMap<String,IdDetail> idsXName = ids.get(nameNodeIds);
					if (idsXName==null)
						idsXName = new HashMap<String,IdDetail>();
					IdDetail idD = idsXName.get(idStr);
					if (idD!=null)
						esta = true;
				}
			}
			if (!esta) {
				HashMap<String,IdDetail> idsXName = ids.get(nameNode);
				if (idsXName==null)
					idsXName = new HashMap<String,IdDetail>();
				IdDetail idD = idsXName.get(idStr);
				if (idD==null) {
					idD = new IdDetail();
					idD.setName(name);
					idD.setReal(real);
					if (idParent!=null)
						idD.setIdParent(idParent);
					idsXName.put(idStr, idD);
					ids.put(nameNode, idsXName);
					if /*(*/(nameNode.equals(QueryConstants.CLASS) || 
							nameNode.equals(QueryConstants.XOR)) {
							//&& jdomParser.findAttributeWhereIdoReqNull(elem, true, true)!=null)
						HashSet<String> hStr = idClasses.get(idStructure);
						if (hStr==null) {
							hStr = new HashSet<String>();
							idClasses.put(idStructure,hStr);
						}
						hStr.add(idStr);
					}
					insert = true;
				}
			}
			return insert;
		}
		
		//last
		public void setLastTrue(String idStr, String nameNode) {
			HashMap<String,IdDetail> idsXName = ids.get(nameNode);
			if (idsXName!=null) {
				IdDetail idD = idsXName.get(idStr);
				if (idD!=null)
					idD.setLast(true);
			}
		}
		public boolean getLast(String idStr) {
			boolean last = false;
			boolean setLast = false;
			//sera CLASS o XOR
			HashMap<String,IdDetail> idsXName = ids.get(QueryConstants.CLASS);
			if (idsXName!=null) {
				IdDetail idD = idsXName.get(idStr);
				if (idD!=null) {
					last = idD.isLast();
					setLast = true;
				}
			}
			if (!setLast) {
				idsXName = ids.get(QueryConstants.XOR);
				if (idsXName!=null) {
					IdDetail idD = idsXName.get(idStr);
					if (idD!=null)
						last = idD.isLast();
				}
			}
			return last;
		}
		
		//tm
		public void setTm(String idStr, String nameNode, int tm) {
			HashMap<String,IdDetail> idsXName = ids.get(nameNode);
			if (idsXName!=null) {
				IdDetail idD = idsXName.get(idStr);
				if (idD!=null)
					idD.setTm(tm);
			}
		}
		public Integer getTm(String idStr, String nameNode) {
			Integer tm = null;
			HashMap<String,IdDetail> idsXName = ids.get(nameNode);
			if (idsXName!=null) {
				IdDetail idD = idsXName.get(idStr);
				if (idD!=null)
					tm = idD.getTm();
			}
			return tm;
		}
		/*public Integer getTmShow(String idStr) {
			Integer tm = getTm(idStr, QueryConstants.ATTRIBUTE);
			if (tm==null) {
				tm = getTm(idStr, QueryConstants.AGREGATION);
				if (tm==null)
					tm = getTm(idStr, QueryConstants.EXPRESION);
			}
			return tm;
		}*/
		
		//true si el id idStr pertenece a un nodo con nombre name
		public boolean is(String idStr, String nameNode, boolean setUsed) {
			boolean isNameNode = false;
			HashMap<String,IdDetail> idsXName = ids.get(nameNode);
			if (idsXName!=null) {
				IdDetail idD = idsXName.get(idStr);
				if (idD!=null) {
					if (setUsed)
						idD.setUsed(true);
					isNameNode = true;
				}
			}
			return isNameNode;
		}
		public boolean is(String idStr, String[] namesNode, boolean setUsed) {
			boolean isNameNode = false;
			for (int i=0;i<namesNode.length;i++) {
				isNameNode = is(idStr, namesNode[i], setUsed);
				if (isNameNode)
					break;
			}
			return isNameNode;
		}
		public boolean isShow(String idStr, boolean setUsed) {
			return (this.is(idStr, QueryConstants.ATTRIBUTE, setUsed) || this.is(idStr, QueryConstants.AGREGATION, setUsed) ||
					this.is(idStr, QueryConstants.EXPRESION, setUsed));
		}
		
		//funciones auxiliares
		//ver si tiene algun nodo con nombre name
		public boolean has(String nameNode) {
			boolean has = false;
			HashMap<String,IdDetail> idsXName = ids.get(nameNode);
			if (idsXName!=null && idsXName.size()>0)
				has = true;
			else {
				ArrayList<Element> aElem = nodesNoUsed.get(nameNode);
				if (aElem!=null && aElem.size()>0)
					has = true;
			}
			return has;
		}
		
		public String toString() {
			String dev = "MAPA DE IDS:\n";
			Iterator it = ids.keySet().iterator();
			while (it.hasNext()) {
				String nameNode = (String)it.next();
				HashMap<String,IdDetail> iMap = ids.get(nameNode);
				dev += nameNode + ": \n";
				String idsXName = toStringIdsXNameMap(iMap);
				if (idsXName.length()>0)
					dev += idsXName + "\n";
			}
			return dev;
		}
		private String toStringIdsXNameMap(HashMap<String,IdDetail> iMap) {
			String dev = "";
			Iterator it = iMap.keySet().iterator();
			while (it.hasNext()) {
				String idStr = (String)it.next();
				IdDetail idD = iMap.get(idStr);
				dev += "\t\t" + idStr + " ---> " + idD + "\n";
			}
			return dev;
		}
	}
	
	private class HasChildren extends Object {
		private boolean attrib;
		private boolean required;
		
		public HasChildren() {
			this.attrib = false;
			this.required = false;
		}
		
		public boolean hasAttrib() {
			return attrib;
		}
		public void setAttrib(boolean attrib) {
			this.attrib = attrib;
		}
		public boolean hasRequired() {
			return required;
		}
		public void setRequired(boolean required) {
			this.required = required;
		}
	}
	
	private class CheckParam extends Object {
		private String name;
		private String alias;
		
		public CheckParam(String name, String alias) {
			this.name = name;
			this.alias = alias;
		}
		public String getAlias() {
			return alias;
		}
		public String getName() {
			return name;
		}
	}
	
	private class IdTmRuleengine extends Object {
		private int tm;
		private String id;
		
		public IdTmRuleengine(int tm, String id) {
			this.tm = tm;
			this.id = id;
		}

		public int getTm() {
			return tm;
		}
		public void setTm(int tm) {
			this.tm = tm;
		}

		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String toString() {
			return "id: " + id + ", tm: " + tm;
		}
	}
	
	public QueryReportParser(FactoryConnectionDB fcdb, boolean isReport, boolean toInstance, boolean exceptionErrors) {
		this.fcdb = fcdb;
		this.debug = new DebugParser();
		this.ids = new IDs();
		this.isReport = isReport;
		this.toInstance = toInstance;
		this.exceptionErrors = exceptionErrors;
	}

	private void setCaseUnionReports(Element query) {
		int numStructure = 0;
		//que sean structure o union
		Iterator it = query.getChildren().iterator();
		while (it.hasNext()) {
			Element nodoStructure = (Element)it.next();
			if (nodoStructure.getName().equals(QueryConstants.UNION) || nodoStructure.getName().equals(QueryConstants.STRUCTURE)) {
				numStructure++;
				if (numStructure>1) {
					caseUnionReports = true;
					break;
				}
			}
		}
	}
	
	private int getNextId() {
		int nextId = 0;
		//se reserva el id=1 para el master
		if (caseUnionReports && id==1)
			id++;
		nextId = id;
		id++;
		return nextId;
	}
	
	public void parserIDs(Element query) throws ParseException {
		setCaseUnionReports(query);
		boolean hasChildQuery = false;
		ConnectionDB conDb = null;
		String idQuery = null;
		try {
			//System.out.println("QUERY ANTES DE PARSEAR" +jdomParser.returnXML(query));
			if (isReport && query.getAttributeValue(QueryConstants.TARGET_CLASS)==null)
				debug.addError("El atributo " + QueryConstants.TARGET_CLASS + " es obligatorio en el nodo " + QueryConstants.QUERY);
			if (query.getChildren().size()>1)
				debug.addWarning("Se usara un " + QueryConstants.ID + " por defecto para la query principal porque no se ha indicado en el nodo " + QueryConstants.QUERY + " con el atributo " + QueryConstants.ID);
			conDb = fcdb.createConnection(true);
			HashMap<String,ArrayList<CheckParam>> nameParams = new HashMap<String,ArrayList<CheckParam>>();
			Iterator it = query.getChildren().iterator();
			while (it.hasNext()) {
				Element union = (Element)it.next();
				if (idQuery==null)
					idQuery = union.getAttributeValue(QueryConstants.ID);
				hasChildQuery = true;
				String nodeOrig = jdomParser.returnNodeXML(union);
				if (union.getName().equals(QueryConstants.UNION)) {
					isUnion = true;
					ArrayList<ArrayList<IdTmRuleengine>> tmsSelect = new ArrayList<ArrayList<IdTmRuleengine>>();
					ArrayList<Element> nodesNullSelect = new ArrayList<Element>();
					//ArrayList<ArrayList<IdTmRuleengine>> indexTmSelect = new ArrayList<ArrayList<IdTmRuleengine>>();
					int childUnion = 0;
					Iterator it2 = union.getChildren(QueryConstants.STRUCTURE).iterator();
					while (it2.hasNext()) {
						Element structure = (Element)it2.next();
						ArrayList<String> aNamesSelectAll = new ArrayList<String>();
						String idStructure = structure.getAttributeValue(QueryConstants.ID);
						if (childUnion==0)
							parserIdNode(union, nodeOrig, null, false, nameParams, idStructure);
						else
							parserIdNode(union, nodeOrig, null, true, nameParams, idStructure);

						String structureOrig = jdomParser.returnNodeXML(structure);
						childUnion++;
						parserIdNode(structure, structureOrig, null, false, nameParams, idStructure);
						//Integer idParentUnion = null;
						//if (union.getAttributeValue(QueryConstants.INDEX)!=null)
						//idParentUnion = Integer.parseInt(union.getAttributeValue(QueryConstants.INDEX));
						idStructure = structure.getAttributeValue(QueryConstants.ID);
						tmsSelect.add(parserIDsStructurePresentation(conDb, structure, structureOrig, nodesNullSelect, aNamesSelectAll, nameParams, idStructure));
						HashMap<String,Boolean> aNames = new HashMap<String,Boolean>();
						namesRepetidos(aNamesSelectAll, aNames);
						debugNames(aNames);
					}
					if (childUnion<=1)
						debug.addWarning("El nodo " + nodeOrig + " debe tener al menos 2 nodos hijo " + QueryConstants.STRUCTURE);
					resolveNullsAndTmSelect(tmsSelect, nodesNullSelect, nodeOrig);
					HashMap<String,Integer> idsPosic = new HashMap<String,Integer>();
					for (int i=0;i<tmsSelect.size();i++)
						getOrderPositions(tmsSelect.get(i), idsPosic);
					Element order = union.getChild(QueryConstants.ORDER);
					if (order!=null) {
						if (union.getChildren(QueryConstants.ORDER).size()>1)
							debug.addWarning("Bajo el nodo " + nodeOrig + " solo debe haber un unico nodo " + QueryConstants.ORDER);
						//getIdNode(order, QueryConstants.INDEX, ids);
						String idsOrder = order.getAttributeValue(QueryConstants.ORDERBY);
						if (idsOrder!=null)
							parserOrderPresentationUnion(idsOrder, idsPosic, order);
					}
				} else if (union.getName().equals(QueryConstants.STRUCTURE)) {
					isUnion = false;
					ArrayList<String> aNamesSelectAll = new ArrayList<String>();
					String idStructure = union.getAttributeValue(QueryConstants.ID);
					parserIdNode(union, nodeOrig, null, false, nameParams, idStructure);
					parserIDsStructurePresentation(conDb, union, nodeOrig, null, aNamesSelectAll, nameParams, idStructure);
					HashMap<String,Boolean> aNames = new HashMap<String,Boolean>();
					namesRepetidos(aNamesSelectAll, aNames);
					debugNames(aNames);
				} else
					debug.addWarning("El nodo " + nodeOrig + " no es valido. Bajo un nodo " + QueryConstants.QUERY + 
							" solo debe haber nodos " + QueryConstants.STRUCTURE + " y/o " + QueryConstants.UNION);
			}
			debugNameParams(nameParams);
		} catch (SQLException e) {
            throw new ParseException(e.getMessage(), 0);
		} catch (NamingException e) {
            throw new ParseException(e.getMessage(), 0);
		} catch (JDOMException e) {
            throw new ParseException(e.getMessage(), 0);
		} catch (DataErrorException e) {
            throw new ParseException(e.getMessage(), 0);
		} finally {
			try {
				if (conDb!=null)
					fcdb.close(conDb);
			} catch (SQLException e) {
	            throw new ParseException(e.getMessage(), 0);
			}
		}
		//System.out.println(ids.toString());
		if (!hasChildQuery)
			debug.addWarning("El nodo " + QueryConstants.QUERY + " debe tener al menos un nodo hijo " + 
					QueryConstants.STRUCTURE + " o " + QueryConstants.UNION);
		if (exceptionErrors && debug.hasErrors()) {
			System.out.println("QUERY " + idQuery);
			throw new ParseException("\n" + debug.toString(),0);
		} else 
			System.out.println(debug.toString());
	}
	
	private HashMap<String,Integer> getOrderPositions(ArrayList<IdTmRuleengine> tmsSelect, HashMap<String,Integer> idsPosic) {
		int contPos = 1;
		for (int i=0;i<tmsSelect.size();i++) {
			IdTmRuleengine tmSelect = tmsSelect.get(i);
			//int index = tmSelect.getIndex();
			String id = tmSelect.getId();
			//si ya hay un id es un error si en idsPosic ya existia un id con otro nombre
			//no se va a controlar porque se van a modificar los ids para que sean unicos en toda la query
			int tm = tmSelect.getTm();
			//System.out.println("tm " + tm);
			if (tm==QueryConstants.TM_VALUE_RDN)
				contPos++;
			idsPosic.put(id, new Integer(contPos));
			//incrementar dependiendo del tipo de datos que es
			if (tm==Constants.IDTO_STRING || tm==Constants.IDTO_MEMO)
				contPos++;
			else if (tm==Constants.IDTO_UNIT || 
					tm==Constants.IDTO_INT || 
					tm==Constants.IDTO_DOUBLE || 
					tm==Constants.IDTO_BOOLEAN || 
					tm==Constants.IDTO_TIME || 
					tm==Constants.IDTO_DATETIME || 
					tm==Constants.IDTO_DATE || 
					tm==QueryConstants.TM_VALUE_RDN || 
					tm==QueryConstants.TM_ID) {
				//System.out.println("idShow " + id);
				//String nameNode = ids.getNameNodeShow(id);
				//if (tm!=QueryConstants.TM_ID && nameNode.equals(QueryConstants.EXPRESION))
					//contPos++;
				//else
				//if (nameNode.equals(QueryConstants.AGREGATION) || nameNode.equals(QueryConstants.EXPRESION))
					contPos = contPos+2;
				//else
					//contPos = contPos+3;
			}
		}
		return idsPosic;
	}
	//se guarda indice y tipo para buscar los nodos nulos y asignarselo
	private void resolveNullsAndTmSelect(ArrayList<ArrayList<IdTmRuleengine>> tmsSelect, ArrayList<Element> nodesNull, String nodeOrig) 
			throws DataErrorException {
		int sizeNull = nodesNull.size();
		ArrayList<IdTmRuleengine> elems1 = null;
		HashMap<String,Integer> indexTm = new HashMap<String,Integer>();
		int sizeTmsSelect = tmsSelect.size();
		if (sizeTmsSelect>0)
			elems1 = tmsSelect.get(0);
		boolean igual = true;
		for (int i=1;i<sizeTmsSelect;i++) {
			//comparar 1er y 2
			//despues 2 y 3
			//y asi sucesivamente
			ArrayList<IdTmRuleengine> elems2 = tmsSelect.get(i);
			igual = nullsAndSelect(indexTm, elems1, elems2);
			if (!igual) {
				debug.addError("Los nodos a los que se hace referencia en el atributo " + QueryConstants.SELECT + 
						" de los nodos " + QueryConstants.VIEW + " bajo " + nodeOrig + 
						" deben tener el mismo número de identificadores y del mismo tipo");
				break;
			}
			elems1 = elems2;
		}
		if (igual) {
			elems1 = tmsSelect.get(0);
			//si no estan todos los element resueltos, tamaño de nulos!=tamaño del mapa
			//volver a iterar
			int sizeMapOld = 0;
			int sizeMapNew = indexTm.size();
			while (sizeNull!=sizeMapNew && sizeMapOld!=sizeMapNew) {
				sizeMapOld = sizeMapNew;
				for (int i=1;i<sizeTmsSelect;i++) {
					ArrayList<IdTmRuleengine> elems2 = tmsSelect.get(i);
					nullsAndSelect(indexTm, elems1, elems2);
					elems1 = elems2;
				}
				sizeMapNew = indexTm.size();
			}
			for (int i=0;i<sizeNull;i++) {
				Element nulo = nodesNull.get(i);
				String id = nulo.getAttributeValue(QueryConstants.ID);
				if (id!=null) {
					Integer tm = indexTm.get(id);
					if (tm!=null) {
						nulo.setAttribute(QueryConstants.ID_TM_RULEENGINE, String.valueOf(tm));
						nulo.setAttribute(QueryConstants.ID_TM, String.valueOf(QueryConstants.toIdTmQuery(tm)));
					} else {
						debug.addError("No es posible deducir el tipo de datos en uno o más nodos " + QueryConstants.ATTRIBUTE + 
								" con su valor a NULL");
						break;
					}
				}
			}
		}
	}
	
	private boolean nullsAndSelect(HashMap<String,Integer> idsTmNull, ArrayList<IdTmRuleengine> elems1, ArrayList<IdTmRuleengine> elems2) {
		boolean igual = true;
		if (elems1!=null && elems2!=null) {
			if (elems1.size()!=elems2.size()) {
				igual = false;
				System.out.println("fallo de tamaño: " + elems1.size() + " - " + elems2.size());
				ArrayList<IdTmRuleengine> elems = null;
				if (elems1.size()>elems2.size())
					elems = elems2;
				else
					elems = elems1;
				for (int i=0;i<elems.size();i++) {
					System.out.println("tm1 " + elems1.get(i));
					System.out.println("tm2 " + elems2.get(i));
				}
				if (elems1.size()>elems2.size()) {
					for (int i=elems.size();i<elems1.size();i++)
						System.out.println("tm " + elems1.get(i));
				} else if (elems1.size()<elems2.size()) {
					for (int i=elems.size();i<elems2.size();i++)
						System.out.println("tm " + elems2.get(i));
				}
			} else {
				//comparar uno a uno los identificadores
				for (int i=0;i<elems1.size();i++) {
					IdTmRuleengine idtm1 = elems1.get(i);
					IdTmRuleengine idtm2 = elems2.get(i);
					Integer tm1 = idtm1.getTm();
					Integer tm2 = idtm2.getTm();
					
					if (tm1!=null && tm2!=null) {
						//System.out.println("tm1 " + tm1);
						//System.out.println("tm2 " + tm2);
						if (!tm1.equals(tm2)) {
							//ver si uno de los 2 es null
							if (tm1==QueryConstants.TM_NULL) {
								tm1 = tm2;
								idsTmNull.put(idtm1.getId(), tm1);
							} else if (tm2==QueryConstants.TM_NULL)	{
								tm2 = tm1;
								idsTmNull.put(idtm2.getId(), tm2);
							} else {
								igual = false;
								break;
							}
						}
					}
				}
			}
		}
		return igual;
	}
	
	private void debugNameParams(HashMap<String,ArrayList<CheckParam>> nameParams) {
		Iterator it = nameParams.keySet().iterator();
		while (it.hasNext()) {
			String key = (String)it.next();
			ArrayList<CheckParam> aParam = nameParams.get(key);
			if (aParam.size()==1)
				debug.addError("Solo existe un nodo con el atributo " + QueryConstants.ID_PARAM + " con el valor " + key);
			else {
				//ver que todos tengan el mismo name y alias, ya que tienen el mismo id_param
				Iterator it2 = aParam.iterator();
				String nameAnt = null;
				String aliasAnt = null;
				while (it2.hasNext()) {
					CheckParam cp = (CheckParam)it2.next();
					String name = cp.getName();
					String alias = cp.getAlias();
					if (nameAnt!=null && aliasAnt!=null && name!=null && alias!=null) {
						if (!name.equals(nameAnt) || !alias.equals(aliasAnt))
							debug.addError("Los nodos con el mismo valor en el atributo " + QueryConstants.ID_PARAM + " debe tener los mismos valores en " + QueryConstants.NAME + " y " + QueryConstants.ALIAS);
					}
					nameAnt = name;
					aliasAnt = alias;
				}
			}
		}
//		if (namesRepetidos.length()>0)
//			debug.addWarning("Existen parámetros con los mismos valores en el atributo " + QueryConstants.NAME + ", " +
//					"lo cual provocara que en la aplicación se le pida al usuario los mismos nombres para dichos parámetros. " +
//					"Esto ocurre con el/los nombre/s " + namesRepetidos);
	}
	private void namesRepetidos(ArrayList<String> aNamesSelectAll, HashMap<String,Boolean> aNames) {
		for (int i=0;i<aNamesSelectAll.size();i++) {
			String name = aNamesSelectAll.get(i);
			if (aNames.get(name)!=null)
				aNames.put(name, new Boolean(true));
			else
				aNames.put(name, new Boolean(false));
		}
	}
	private void debugNames(HashMap<String,Boolean> aNames) {
		String namesRepetidos = "";
		Iterator it = aNames.keySet().iterator();
		while (it.hasNext()) {
			String name = (String)it.next();
			boolean repetido = aNames.get(name);
			if (repetido) {
				if (namesRepetidos.length()>0)
					namesRepetidos += ", ";
				namesRepetidos += name;
			}
		}
		if (namesRepetidos.length()>0)
			debug.addError("No es posible mostrar varios nodos con el mismo atributo " + QueryConstants.NAME + ". Esto ocurre con " +
					"el/los nombre/s " + namesRepetidos + " que están repetidos en el xml");
	}
	
	private void debugIdsNoUsed(String name) {
		ArrayList<String> idsNoUsed = ids.getNoUsed(name);
		if (idsNoUsed.size()>0) {
			String warning = "El/Los nodos/s " + name + " con el/los atributo/s " + QueryConstants.ID + ": ";
			warning += Auxiliar.arrayToString(idsNoUsed, ", ");
			warning += " no está/n siendo usado/s";
			if (name.equals(QueryConstants.WHERE) || name.equals(QueryConstants.HAVING)) {
				warning += " ya que no está/n referenciado/s bajo el nodo ";
				if (name.equals(QueryConstants.WHERE))
					warning += QueryConstants.LOGIC_WHERE;
				else
					warning += QueryConstants.LOGIC_HAVING;
				warning += " existente";
			} else
				warning += " en el atributo " + QueryConstants.SELECT + ". Este/os nodo/s es/son borrable/s si no tiene/n un atributo "
					+ QueryConstants.REQUIRED + " o " + QueryConstants.NULL + " a TRUE o se trata de un parámetro";
			debug.addWarning(warning);
		}
	}
	private void debugNodesNoUsed(ArrayList<Element> aNodes, String name) throws JDOMException {
		if (aNodes.size()>0) {
			String warning = "El/Los nodo/s ";
			for (int i=0;i<aNodes.size();i++) {
				if (i>0)
					warning += ", ";
				warning += jdomParser.returnNodeXML(aNodes.get(i));
			}
			warning += " que no tiene/n especificado su atributo " + QueryConstants.ID;
			warning += " no está/n siendo usado/s";
			if (name!=null && (name.equals(QueryConstants.WHERE) || name.equals(QueryConstants.HAVING))) {
				warning += " ya que no está/n referenciado/s bajo el nodo ";
				if (name.equals(QueryConstants.WHERE))
					warning += QueryConstants.LOGIC_WHERE;
				else
					warning += QueryConstants.LOGIC_HAVING;
				warning += " existente";
			} else
				warning += " en el atributo " + QueryConstants.SELECT + ". Este/os nodo/s es/son borrable/s si no tiene/n un atributo "
					+ QueryConstants.REQUIRED + " o " + QueryConstants.NULL + " a TRUE";
			debug.addWarning(warning);
		}
	}

	private void getAttribRequiredOrNullOp(Element op, HashMap<String,Boolean> aRequired, HashMap<String,Boolean> aNull) {
		Iterator it = op.getChildren().iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			if (child.getName().equals(QueryConstants.OP) && child.getAttributeValue(QueryConstants.ID_OP)!=null && 
					child.getAttributeValue(QueryConstants.ID_OP).toLowerCase().equals("and"))
				getAttribRequiredOrNullOp(child, aRequired, aNull);
			else if (child.getName().equals(QueryConstants.CONDITION)) {
				if (StringUtils.equals(child.getAttributeValue(QueryConstants.REQUIRED), "TRUE")) {
					String idCondition = child.getAttributeValue(QueryConstants.ID_CONDITION);
					if (aRequired.get(idCondition)!=null)
						aRequired.put(idCondition, new Boolean(true));
					else
						aRequired.put(idCondition, new Boolean(false));
				}
				if (StringUtils.equals(child.getAttributeValue(QueryConstants.NULL), "TRUE")) {
					String idCondition = child.getAttributeValue(QueryConstants.ID_CONDITION);
					if (aNull.get(idCondition)!=null)
						aNull.put(idCondition, new Boolean(true));
					else
						aNull.put(idCondition, new Boolean(false));
				}
			}
		}
	}
	
	private void getAttribRequiredOrNullPresentation(Element structure, HashMap<String,Boolean> aRequired, HashMap<String,Boolean> aNull) {
		Element presentation = structure.getChild(QueryConstants.PRESENTATION);
		if (presentation!=null) {
			Element view = presentation.getChild(QueryConstants.VIEW);
			if (view!=null) {
				Element logicaWhere = view.getChild(QueryConstants.LOGIC_WHERE);
				if (logicaWhere!=null)
					getAttribRequiredOrNullOp(logicaWhere, aRequired, aNull);
				Element logicaHaving = view.getChild(QueryConstants.LOGIC_HAVING);
				if (logicaHaving!=null)
					getAttribRequiredOrNullOp(logicaHaving, aRequired, aNull);
			}
		}
	}
	
	private void debugRequiredAndNull(HashMap<String,Boolean> aRequired, HashMap<String,Boolean> aNull) {
		String requiredRep = "";
		Iterator it = aRequired.keySet().iterator();
		while (it.hasNext()) {
			String name = (String)it.next();
			boolean repetido = aRequired.get(name);
			if (repetido) {
				if (requiredRep.length()>0)
					requiredRep += ", ";
				requiredRep += name;
			}
		}
		if (aRequired.size()>0)
			debug.addWarning("El/Los nodo/s con el/los atributo/s " + QueryConstants.ID + ": " + requiredRep
					+ " está/n puesto/s varias veces como requerido/s, es decir, con el atributo " + QueryConstants.REQUIRED + " a TRUE");
		String nullRep = "";
		it = aNull.keySet().iterator();
		while (it.hasNext()) {
			String name = (String)it.next();
			boolean repetido = aNull.get(name);
			if (repetido) {
				if (nullRep.length()>0)
					nullRep += ", ";
				nullRep += name;
			}
		}
		if (aNull.size()>0)
			debug.addWarning("El/Los nodo/s con el/los atributo/s " + QueryConstants.ID + ": " + nullRep
					+ " está/n puesto/s varias veces como requerido/s, es decir, con el atributo " + QueryConstants.NULL + " a TRUE");
	}
	
	private void getIdViewsLink(Element view, ArrayList<String> idViews) {
		String idView = view.getAttributeValue(QueryConstants.ID);
		if (idView!=null && !StringUtils.equals(view.getAttributeValue(QueryConstants.LINK), "FALSE"))
			idViews.add(idView);
		Iterator it = view.getChildren(QueryConstants.VIEW).iterator();
		while (it.hasNext())
			getIdViewsLink((Element)it.next(), idViews);
	}
	private ArrayList<IdTmRuleengine> parserIDsStructurePresentation(ConnectionDB conDb, Element structure, String structureOrig, 
			ArrayList<Element> nodesNullSelect, ArrayList<String> aNamesSelectAll, HashMap<String,ArrayList<CheckParam>> nameParams, String idStructure) 
				throws JDOMException, NamingException, SQLException, DataErrorException, ParseException {
		boolean hasChildClass = false;
		boolean hasAttrib = false;
		//para añadir los identificadores de los nodos con REQUIRED bajo el 1er AND que estan bajo PRESENTATION
		HashMap<String,Boolean> aRequired = new HashMap<String,Boolean>();
		HashMap<String,Boolean> aNull = new HashMap<String,Boolean>();
		getAttribRequiredOrNullPresentation(structure, aRequired, aNull);
		ArrayList<Element> aNodesNull = new ArrayList<Element>();
		
		//aqui se calcula el array de ids de views
		ArrayList<String> idViews = new ArrayList<String>();
		Element presentation = structure.getChild(QueryConstants.PRESENTATION);
		if (presentation!=null) {
			Element view = presentation.getChild(QueryConstants.VIEW);
			if (view!=null)
				getIdViewsLink(view, idViews);
		}
		
		String idRoot = null; 
		Iterator it3 = structure.getChildren().iterator();
		while (it3.hasNext()) {
			Element nodoClass = (Element)it3.next();
			if (nodoClass.getName().equals(QueryConstants.CLASS)) {
				idRoot = nodoClass.getAttributeValue(QueryConstants.ID);
				if (idRoot==null && !isReport)
					debug.addError("El nodo CLASS superior debe tener atributo " + QueryConstants.ID);
				hasChildClass = true;
				//que haya al menos un attribute
				/*String className = nodoClass.getAttributeValue(QueryConstants.CLASS_NAME);
				if (className==null)
					debug.addError("El atributo " + QueryConstants.CLASS_NAME + " en el nodo " + QueryConstants.CLASS + " superior es obligatorio");
				else if(className.split(",").length>1 && (!className.startsWith("[") || !className.endsWith("]")))
					debug.addError("El atributo " + QueryConstants.CLASS_NAME + " en el nodo " + QueryConstants.CLASS + " superior no puede ser multiple");*/
				
				ArrayList<Element> idsAttributesWithCase = new ArrayList<Element>();
				hasAttrib = parserIDsStructure(conDb, nodoClass, idViews, idsAttributesWithCase, aNodesNull, aRequired, aNull, nameParams, idStructure).hasAttrib() || hasAttrib;
				//processInner(conDb, nodoClass);
				parserIDsCaseStructure(idsAttributesWithCase);
			} else if (!nodoClass.getName().equals(QueryConstants.PRESENTATION)) {
				debug.addWarning("El nodo " + jdomParser.returnNodeXML(nodoClass) + " no es correcto porque " +
						"bajo un nodo " + QueryConstants.STRUCTURE + " solo debe haber nodos " + QueryConstants.CLASS + 
						" y un unico nodo " + QueryConstants.PRESENTATION + ". este no sera parseado");
			}
		}
		debugRequiredAndNull(aRequired, aNull);
		if (!hasChildClass)
			debug.addError("El nodo " + structureOrig + " debe tener al menos un nodo hijo " + QueryConstants.CLASS);
		if (!hasAttrib)
			debug.addError("algun nodo " + QueryConstants.CLASS + " de los que hay bajo el nodo " + structureOrig + 
					" debe tener como hijo al menos un nodo " + QueryConstants.ATTRIBUTE);
		ArrayList<IdTmRuleengine> aTmsSelect = new ArrayList<IdTmRuleengine>();
		
		if (presentation!=null) {
			if (structure.getChildren(QueryConstants.PRESENTATION).size()>1)
				debug.addWarning("Bajo el nodo " + structureOrig + " solo debe haber un unico nodo " + QueryConstants.PRESENTATION);
			Element view = presentation.getChild(QueryConstants.VIEW);
			if (view!=null) {
				if (presentation.getChildren(QueryConstants.VIEW).size()>1)
					debug.addWarning("Bajo un nodo " + QueryConstants.PRESENTATION + " solo debe haber un unico nodo " + QueryConstants.VIEW);
				String viewOrig = jdomParser.returnNodeXML(view);
				getIdNodeView(view, structure, true);
				//tendra un unico presentation
				Element parent = structure.getParent();
				HashMap<String,String> viewIndexIdsBrothers = new HashMap<String,String>();
				ArrayList<String> viewIndexBrothers = new ArrayList<String>();
				if (parent.getName().equals(QueryConstants.UNION))
					aTmsSelect = parserIDsPresentation(view, viewOrig, idRoot, viewIndexBrothers, viewIndexIdsBrothers, parent, aNamesSelectAll, nameParams, idStructure);
				else
					aTmsSelect = parserIDsPresentation(view, viewOrig, idRoot, viewIndexBrothers, viewIndexIdsBrothers, structure, aNamesSelectAll, nameParams, idStructure);
				//devolver solos los nodos nulos que se muestran
				if (nodesNullSelect!=null) {
					for (int i=0;i<aNodesNull.size();i++) {
						Element nodeNull = aNodesNull.get(i);
						//System.out.println(jdomParser.returnNodeXML(nodeNull));
						String id = nodeNull.getAttributeValue(QueryConstants.ID);
						for (int j=0;j<aTmsSelect.size();j++) {
							String idSelect = aTmsSelect.get(j).getId();
							if (id.equals(idSelect)) {
								nodesNullSelect.add(nodeNull);
								break;
							}
						}
					}
				}
			} else {
				debug.addError("El nodo " + presentation + " debe tener un nodo hijo " + QueryConstants.VIEW);
				//TODO una mejora podria ser mostrar un warning, crear el nodo view
			}
		} else {
			debug.addError("El nodo " + structureOrig + " debe tener un nodo hijo " + QueryConstants.PRESENTATION);
			//TODO una mejora podria ser mostrar un warning, crear el nodo presentation con su view
		}
		debugIdsNoUsed(QueryConstants.ATTRIBUTE);
		debugIdsNoUsed(QueryConstants.AGREGATION);
		debugIdsNoUsed(QueryConstants.EXPRESION);
		
		String[] nodes = new String[3];
		nodes[0] = QueryConstants.ATTRIBUTE;
		nodes[1] = QueryConstants.AGREGATION;
		nodes[2] = QueryConstants.EXPRESION;
		ArrayList<Element> nodesNoUsed = ids.getNodesNoUsed(nodes);
		debugNodesNoUsed(nodesNoUsed, null);
		
		return aTmsSelect;
	}
	
	//Nodo Presentation
	private ArrayList<IdTmRuleengine> parserIDsPresentation(Element view, String viewOrig, String idRoot, ArrayList<String> viewIndexBrothers, 
			HashMap<String,String> viewIndexIdsBrothers, Element union, ArrayList<String> aNamesSelectAll, HashMap<String,ArrayList<CheckParam>> nameParams, String idStructure) 
				throws JDOMException, ParseException {
		ArrayList<String> idsAgregation = new ArrayList<String>();
		Iterator it = view.getChildren(QueryConstants.AGREGATION).iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			String childOri = jdomParser.returnNodeXML(child);
			parserIDsAgregExpr(child, childOri, idsAgregation, nameParams, idStructure);
			getIdNode(child, QueryConstants.ID_CASE, QueryConstants.CASE);
			parserIdTmAgreg(child);
			parserName(child, childOri);
		}
		Element nodoClass = null;
		if (union.getName().equals(QueryConstants.UNION) && !view.getParent().getName().equals(QueryConstants.VIEW))
			nodoClass = union;
		else {
			if (view.getAttributeValue(QueryConstants.ID)!=null) {
				//que sea class o structure
				String[] names = new String[2];
				names[0] = QueryConstants.CLASS;
				names[1] = QueryConstants.STRUCTURE;
				boolean mirarActual = union.getName().equals(QueryConstants.STRUCTURE);
				nodoClass = jdomParser.findElementByAt(union, names, QueryConstants.ID, 
						view.getAttributeValue(QueryConstants.ID), mirarActual, true);
				if (nodoClass==null) {
					if (union.getAttributeValue(QueryConstants.ID)!=null)
						debug.addError("No se encuentra ningun nodo " + QueryConstants.STRUCTURE + " o " + QueryConstants.CLASS + 
								" bajo el nodo con " + QueryConstants.ID + " " + union.getAttributeValue(QueryConstants.ID) + 
								" con el identificador indicado por el atributo " + QueryConstants.ID + " en el nodo " + viewOrig);
					else
						debug.addError("No se encuentra ningun nodo " + QueryConstants.STRUCTURE + " o " + QueryConstants.CLASS + 
								" con el identificador indicado por el atributo " + QueryConstants.ID + " en el nodo " + viewOrig);
				}
			} else if (union.getName().equals(QueryConstants.STRUCTURE)) {
				nodoClass = union;
				view.setAttribute(QueryConstants.ID, union.getAttributeValue(QueryConstants.ID));
			} else
				debug.addError("El nodo " + viewOrig + " debe tener un atributo " + QueryConstants.ID + 
					" para referenciar al nodo " + QueryConstants.CLASS + " o " + QueryConstants.XOR + " en el que comienza la subconsulta");
		}
		ArrayList<IdTmRuleengine> aTmsSelect = new ArrayList<IdTmRuleengine>();
		if (nodoClass!=null) {
			String name = nodoClass.getAttributeValue(QueryConstants.NAME);
			if (name==null && view.getParent().getName().equals(QueryConstants.VIEW))
				debug.addWarning("El nodo apuntado por " + viewOrig + " no lleva el atributo " + QueryConstants.NAME + " que se utiliza " +
						"para dar nombre a los subreports. En este caso se usara un nombre por defecto");
			//si bajo class esta alguno de los nodos view hermanos -> error. Presentation mal construido
			String[] names = new String[2];
			names[0] = QueryConstants.CLASS;
			names[1] = QueryConstants.STRUCTURE;
			ArrayList<Element> aNodosChild = jdomParser.findElementsByAt(nodoClass, names, QueryConstants.ID, viewIndexBrothers, true);
			ArrayList<String> idsIncorrectos = new ArrayList<String>();
			for(int i=0;i<aNodosChild.size();i++) {
				Element nodoClassChild = aNodosChild.get(i);
				idsIncorrectos.add(viewIndexIdsBrothers.get(nodoClassChild.getAttributeValue(QueryConstants.ID)));
			}
			if (idsIncorrectos.size()>0)
				debug.addError("La estructura de nodos " + QueryConstants.VIEW + " que hay bajo " + QueryConstants.PRESENTATION + 
						" no es correcta. El/Los nodo/s " + QueryConstants.VIEW + " con " + QueryConstants.ID + " " + Auxiliar.arrayToString(idsIncorrectos, ", ") + 
						" está/n bajo el nodo " + viewOrig);
			
			it = view.getChildren(QueryConstants.EXPRESION).iterator();
			while (it.hasNext()) {
				Element child = (Element)it.next();
				String childOri = jdomParser.returnNodeXML(child);
				parserIDsAgregExpr(child, childOri, new ArrayList<String>(), nameParams, idStructure);
				getIdNode(child, QueryConstants.ID_CASE, QueryConstants.CASE);
				parserIdTmExpr(child, childOri, nodoClass);
				parserName(child, childOri);
			}
			it = view.getChildren().iterator();
			while (it.hasNext()) {
				Element child = (Element)it.next();
				if (child.getName().equals(QueryConstants.WHERE) || child.getName().equals(QueryConstants.HAVING)) {
					String whereOri = jdomParser.returnNodeXML(child);
					parserIdTmWhere(child, nodoClass, whereOri);
					parserIDsWhere(child, whereOri, nameParams, idStructure);
				}
			}
			String idsSelect = view.getAttributeValue(QueryConstants.SELECT);
			ArrayList<String> aIdsSelectImportado = new ArrayList<String>();
			if (idsSelect!=null) {
				aIdsSelectImportado = parserSelectPresentation(idsSelect, idRoot, view, aTmsSelect, aNamesSelectAll, idStructure);
			} else if (nodoClass.getName().equals(QueryConstants.UNION))
				debug.addError("En el nodo " + viewOrig + " que está bajo un nodo "+ QueryConstants.UNION + 
						" debe haber un atributo " + QueryConstants.SELECT + " donde referenciar a los nodos " + 
						QueryConstants.ATTRIBUTE + ", " + QueryConstants.AGREGATION + " o " + QueryConstants.EXPRESION + 
						" que se quieren mostrar");
			else
				debug.addWarning("El nodo " + viewOrig + " debe tener un atributo " + QueryConstants.SELECT + 
						" donde referenciar a los nodos " + QueryConstants.ATTRIBUTE + ", " + QueryConstants.AGREGATION + " o " + 
							QueryConstants.EXPRESION + " que se quieren mostrar");
			
			String idsGroup = view.getAttributeValue(QueryConstants.GROUPBY);
			HashSet<String> aIdsGroupImportado = parserGroupPresentation(idsGroup, idsAgregation, nodoClass, view);
			
			String idsOrder = view.getAttributeValue(QueryConstants.ORDERBY);
			if (idsOrder!=null)
				parserOrderPresentation(idsOrder, aIdsSelectImportado, view);
			
			Element logicaWhere = view.getChild(QueryConstants.LOGIC_WHERE);
			if (logicaWhere!=null) {
				if (view.getChildren(QueryConstants.LOGIC_WHERE).size()>1)
					debug.addWarning("Bajo el nodo " + viewOrig + " solo debe haber un unico nodo " + QueryConstants.LOGIC_WHERE);
				Element child = logicaWhere.getChild(QueryConstants.OP);
				if (child!=null) {
					if (logicaWhere.getChildren(QueryConstants.OP).size()>1)
						debug.addWarning("Bajo el nodo " + QueryConstants.LOGIC_WHERE + " solo debe haber un unico nodo " + QueryConstants.OP 
								+ ". Solo se tendra en cuenta uno de ellos");
					parserIDsOP(child, false);
				} else
					debug.addWarning("Bajo el nodo " + QueryConstants.LOGIC_WHERE + " debe haber un nodo " + QueryConstants.OP);
			}
			Element logicaHaving = view.getChild(QueryConstants.LOGIC_HAVING);
			if (logicaHaving!=null) {
				if (view.getChildren(QueryConstants.LOGIC_HAVING).size()>1)
					debug.addWarning("Bajo el nodo " + viewOrig + " solo debe haber un unico nodo " + QueryConstants.LOGIC_HAVING);
				boolean hasHaving = false;
				Element child = logicaHaving.getChild(QueryConstants.OP);
				if (child!=null) {
					if (logicaHaving.getChildren(QueryConstants.OP).size()>1)
						debug.addWarning("Bajo el nodo " + QueryConstants.LOGIC_HAVING + " solo debe haber un unico nodo " + QueryConstants.OP 
								+ ". Solo se tendra en cuenta uno de ellos");
					hasHaving = parserIDsOP(child, true) || hasHaving;
				} else
					debug.addWarning("Bajo el nodo " + QueryConstants.LOGIC_HAVING + " debe haber un nodo " + QueryConstants.OP);
				if (!hasHaving)
					debug.addError("Bajo el nodo " + QueryConstants.LOGIC_HAVING + " debe haber al menos una referencia a un nodo " + 
							QueryConstants.HAVING);
			}
			
			//si existe logic_where ver los where declarados que no se han usado
			if (logicaWhere!=null) {
				debugIdsNoUsed(QueryConstants.WHERE);
				ArrayList<Element> aNodes = ids.getNodesNoUsed(QueryConstants.WHERE);
				debugNodesNoUsed(aNodes, QueryConstants.WHERE);
			} else {
				//si hay where en el mapa
				//decir que al no haber una logicaWhere se uniran todos con and
				if (ids.has(QueryConstants.WHERE))
					debug.addWarning("Al no existir un nodo " + QueryConstants.LOGIC_WHERE + " bajo el nodo " + viewOrig + " todos " +
							"los nodos " + QueryConstants.WHERE + " declarados se uniran mediante la operación and");
			}
			//si existe logic_having ver los having no usados
			if (logicaHaving!=null) {
				debugIdsNoUsed(QueryConstants.HAVING);
				ArrayList<Element> aNodes = ids.getNodesNoUsed(QueryConstants.HAVING);
				debugNodesNoUsed(aNodes, QueryConstants.HAVING);
			} else {
				//si hay having en el mapa decir que al no haber una logicaHaving se uniran todos con and
				if (ids.has(QueryConstants.HAVING))
					debug.addWarning("Al no existir un nodo " + QueryConstants.LOGIC_HAVING + " bajo el nodo " + viewOrig + " todos " +
							"los nodos " + QueryConstants.HAVING + " declarados se uniran mediante la operación and");
			}
			
			HashMap<String,String> viewIndexIdsNewBrothers = new HashMap<String,String>();
			ArrayList<String> viewIndexNewBrothers = new ArrayList<String>();
			it = view.getChildren(QueryConstants.VIEW).iterator();
			while (it.hasNext()) {
				Element child = (Element)it.next();
				String id = child.getAttributeValue(QueryConstants.ID);
				getIdNodeView(child, null, false);
				if (id!=null) {
					viewIndexNewBrothers.add(child.getAttributeValue(QueryConstants.ID));
					viewIndexIdsNewBrothers.put(child.getAttributeValue(QueryConstants.ID), id);
				}
			}
			
			it = view.getChildren(QueryConstants.VIEW).iterator();
			while (it.hasNext()) {
				Element child = (Element)it.next();
				Element childTemp = jdomParser.cloneNode(child);
				viewOrig = jdomParser.returnNodeXML(childTemp);
				if (caseUnionReports)
					debug.addWarning("El nodo " + viewOrig + " tiene nodos " + QueryConstants.VIEW + " hijos. Cuando hay 2 o más nodos " + 
							QueryConstants.STRUCTURE + " o " + QueryConstants.UNION + " bajo " + QueryConstants.QUERY + 
							" no puede haber subreports, es decir, no puede haber nodos " + QueryConstants.VIEW + " anidados. " +
							"No se mostraran en el report los datos de las consultas de los subreports");
				parserIDsPresentation(child, viewOrig, idRoot, viewIndexNewBrothers, viewIndexIdsNewBrothers, nodoClass, aNamesSelectAll, nameParams, idStructure);
			}
			//añade multivalue cuando sea necesario
			parserMultivalueStructure(aIdsGroupImportado, nodoClass, /*view.getAttributeValue(QueryConstants.INDEX), */viewIndexNewBrothers);
		}
		return aTmsSelect;
	}
	
	private void parserIdTmWhere(Element where, Element nodoClass, String whereOri) throws JDOMException {
		String idAttributeStr = where.getAttributeValue(QueryConstants.ID_LEFT);
		if (idAttributeStr!=null) {
			Element attribute = jdomParser.findElementByAt(nodoClass, QueryConstants.ID, idAttributeStr, true);
			if (attribute!=null) {
				if (attribute.getAttributeValue(QueryConstants.ID_TM)!=null) {
					where.setAttribute(QueryConstants.ID_TM,attribute.getAttributeValue(QueryConstants.ID_TM));
					String idtmRule = attribute.getAttributeValue(QueryConstants.ID_TM_RULEENGINE);
					if (idtmRule!=null) {
						where.setAttribute(QueryConstants.ID_TM_RULEENGINE,idtmRule);
						String idStr = where.getAttributeValue(QueryConstants.ID);
						if (idStr!=null)
							ids.setTm(idStr.replaceAll(" ", ""), where.getName(), Integer.parseInt(idtmRule));
					}
				} else
					debug.addError("No se ha podido añadir el tipo de datos en el nodo " + whereOri);
			} else {
				attribute = jdomParser.findElementByAt(where.getParent(), QueryConstants.ID, idAttributeStr, true);
				if (attribute!=null && (attribute.getName().equals(QueryConstants.AGREGATION) || attribute.getName().equals(QueryConstants.EXPRESION))) {
					where.setAttribute(QueryConstants.ID_TM,attribute.getAttributeValue(QueryConstants.ID_TM));
					String idtmRule = attribute.getAttributeValue(QueryConstants.ID_TM_RULEENGINE);
					if (idtmRule!=null) {
						where.setAttribute(QueryConstants.ID_TM_RULEENGINE,idtmRule);
						String idStr = where.getAttributeValue(QueryConstants.ID);
						if (idStr!=null)
							ids.setTm(idStr.replaceAll(" ", ""), where.getName(), Integer.parseInt(idtmRule));
					}
				}
			}
		}
	}
	
	private boolean setIsExpression(Element elem, String etiq) {
		boolean isExpresion = false;
		String idStr = elem.getAttributeValue(etiq);
		//System.out.println("etiq " + etiq);
		//System.out.println("idStr " + idStr);
		if (idStr!=null) {
			isExpresion = ids.is(idStr, QueryConstants.EXPRESION, false);
			if (isExpresion)
				elem.setAttribute(QueryConstants.IS_EXPRESION, "TRUE");
		}
		return isExpresion;
	}
	
	private void parserIDsWhere(Element where, String whereOri, HashMap<String,ArrayList<CheckParam>> nameParams, String idStructure) throws JDOMException, ParseException {
		boolean isExpresion = setIsExpression(where, QueryConstants.ID_RIGHT);
		if (!isExpresion) isExpresion = setIsExpression(where, QueryConstants.ID_RIGHT_MIN);
		if (!isExpresion) isExpresion = setIsExpression(where, QueryConstants.ID_RIGHT_MAX);
		if (!isExpresion) isExpresion = setIsExpression(where, QueryConstants.ID_LEFT);
		parserIdNode(where, whereOri, null, false, nameParams, idStructure);
		getIdNodeShow(where, QueryConstants.ID_RIGHT, whereOri);
		getIdNodeShow(where, QueryConstants.ID_RIGHT_MIN, whereOri);
		getIdNodeShow(where, QueryConstants.ID_RIGHT_MAX, whereOri);
		getIdNodeShow(where, QueryConstants.ID_LEFT, whereOri);
		//idRight puede ser opcional, pro idLeft no
		if (where.getAttributeValue(QueryConstants.ID_LEFT)==null)
			debug.addError("El atributo " + QueryConstants.ID_LEFT + " no debe ser nulo en el nodo " + whereOri + 
					" que está bajo " + QueryConstants.PRESENTATION);
		
		String op = where.getAttributeValue(QueryConstants.OP);
		if (op!=null) {
			String tm = where.getAttributeValue(QueryConstants.ID_TM);
			if (op.equals(QueryConstants.BETWEEN)) {
				parserDateTimeBoolean(where, whereOri, QueryConstants.VAL_MIN);
				parserDateTimeBoolean(where, whereOri, QueryConstants.VAL_MAX);
				String valMin = where.getAttributeValue(QueryConstants.VAL_MIN);
				String valMax = where.getAttributeValue(QueryConstants.VAL_MAX);
				if (tm!=null && Integer.parseInt(tm)==QueryConstants.TM_QUANTITY) {
					if (valMin!=null && !(valMin.contains("(VALUE)") || Auxiliar.hasIntValue(valMin)))
						debug.addError("El atributo " + QueryConstants.VAL_MIN + " debe ser el parámetro (VALUE) o un número " +
								"en el nodo " + whereOri + " ya que el dato es una cantidad");
					if (valMax!=null && !(valMax.contains("(VALUE)") || Auxiliar.hasIntValue(valMax)))
						debug.addError("El atributo " + QueryConstants.VAL_MAX + " debe ser el parámetro (VALUE) o un número " +
								"en el nodo " + whereOri + " ya que el dato es una cantidad");
				}
				if (valMin==null && where.getAttributeValue(QueryConstants.ID_RIGHT_MIN)==null)
					debug.addError("El nodo " + whereOri + " debe tener un atributo " + 
							QueryConstants.VAL_MIN + " o un atributo " + QueryConstants.ID_RIGHT_MIN + 
							" para que se imponga la condición");
				if (valMax==null && where.getAttributeValue(QueryConstants.ID_RIGHT_MAX)==null)
					debug.addError("El nodo " + whereOri + " debe tener un atributo " + 
							QueryConstants.VAL_MAX + " y/o un atributo " + QueryConstants.ID_RIGHT_MAX + 
							" para que se imponga la condición");
			} else {
				if (!(op.equals(QueryConstants.DISTINTO) || op.equals(QueryConstants.DISTINTO_VALIDO) || 
						op.equals(QueryConstants.IGUAL) || op.equals(QueryConstants.LIKE) ||
						op.equals(QueryConstants.CONTAINS) || op.equals(QueryConstants.MENOR) ||
						op.equals(QueryConstants.MENOR_IGUAL) || op.equals(QueryConstants.MAYOR) ||
						op.equals(QueryConstants.MAYOR_IGUAL) || op.equals(QueryConstants.REG_EXPR) || 
						op.equals(QueryConstants.NOT_REG_EXPR)))
					debug.addError("La operación introducida en el atributo " + QueryConstants.OP + 
							" del nodo " + whereOri + " no es valida. Debe ser una de las siguientes: " + 
							QueryConstants.DISTINTO + "," + QueryConstants.DISTINTO_VALIDO + "," + QueryConstants.IGUAL + "," + 
							QueryConstants.LIKE + "," + QueryConstants.CONTAINS + "," + QueryConstants.MENOR + "," + 
							QueryConstants.MENOR_IGUAL + "," + QueryConstants.MAYOR + "," + QueryConstants.MAYOR_IGUAL + "," + 
							QueryConstants.REG_EXPR + " o " + QueryConstants.NOT_REG_EXPR);
				parserDateTimeBoolean(where, whereOri, QueryConstants.VALUE);
				String val = where.getAttributeValue(QueryConstants.VALUE) == null ? where.getText()
						: where.getAttributeValue(QueryConstants.VALUE);
				if (tm!=null && Integer.parseInt(tm)==QueryConstants.TM_QUANTITY) {
					if (val!=null && val.length()>0 && !(val.contains("(VALUE)") || Auxiliar.hasIntValue(val)))
						debug.addError("El atributo " + QueryConstants.VALUE + " debe ser el parámetro (VALUE) o un número " +
								"en el nodo " + whereOri + " ya que el dato es una cantidad");
				}
				if ((val==null || val.length()==0) && where.getAttributeValue(QueryConstants.ID_RIGHT)==null)
					debug.addError("El nodo " + whereOri + " debe tener un atributo " + QueryConstants.VALUE + 
							" o un contenido en el nodo o un atributo " + QueryConstants.ID_RIGHT + 
							" para que se imponga la condición");
			}
		} else
			debug.addWarning("El nodo " + whereOri + " debe tener un atributo " + QueryConstants.OP + 
					" para que se imponga la condición");
	}
	
	private void parserIdTmAgreg(Element elem) throws JDOMException {
		String idStr = elem.getAttributeValue(QueryConstants.ID);
		if (idStr!=null)
			ids.setTm(idStr.replaceAll(" ", ""), elem.getName(), Constants.IDTO_DOUBLE);
	}
	
	private void parserIdTmExpr(Element elem, String elemOri, Element nodoClass) throws JDOMException {
		//nota ir descartando parentesis que están a la izquierda
		String op = elem.getAttributeValue(QueryConstants.OP_EXPRES);
		if (op==null) {
			GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
			op = elem.getAttributeValue(gSQL.getOpExpres());
		}
		if (op!=null) {
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
				Element attribute = jdomParser.findElementByAt(nodoClass, QueryConstants.ID, idAttributeStr, true);
				if (attribute!=null) {
					if (attribute.getAttributeValue(QueryConstants.ID_TM)!=null) {
						elem.setAttribute(QueryConstants.ID_TM,attribute.getAttributeValue(QueryConstants.ID_TM));
						String idtmRule = attribute.getAttributeValue(QueryConstants.ID_TM_RULEENGINE);
						if (idtmRule!=null) {
							elem.setAttribute(QueryConstants.ID_TM_RULEENGINE,idtmRule);
							String idStr = elem.getAttributeValue(QueryConstants.ID);
							if (idStr!=null)
								ids.setTm(idStr.replaceAll(" ", ""), elem.getName(), Integer.parseInt(idtmRule));
						}
					} else
						debug.addError("No se ha podido añadir el tipo de datos en el nodo " + jdomParser.returnNodeXML(attribute));
				} else {
					attribute = jdomParser.findElementByAt(elem.getParent(), QueryConstants.ID, idAttributeStr, true);
					if (attribute!=null && attribute.getName().equals(QueryConstants.AGREGATION)) {
						elem.setAttribute(QueryConstants.ID_TM,String.valueOf(QueryConstants.TM_QUANTITY));
						String idtmRule = attribute.getAttributeValue(QueryConstants.ID_TM_RULEENGINE);
						if (idtmRule!=null) {
							elem.setAttribute(QueryConstants.ID_TM_RULEENGINE,idtmRule);
							String idStr = elem.getAttributeValue(QueryConstants.ID);
							if (idStr!=null)
								ids.setTm(idStr.replaceAll(" ", ""), elem.getName(), Integer.parseInt(idtmRule));
						}
					} //else debug ya tratado al parsear la operacion
				}
			} else {
				int fin = op.indexOf('(',inicio+1);
				if (fin!=-1) {
					String function = op.substring(inicio, fin).toLowerCase(); 
					if (function.equals("substring") 
							|| function.equals("max") || function.equals("min")
							|| function.equals("lower") || function.equals("upper")
							|| function.equals("rtrim") || function.equals("ltrim")
							|| function.equals("right") || function.equals("left")
							|| function.equals("reverse") || function.equals("concat")
							|| function.equals("group_concat")
							|| function.equals("space")
							|| function.equals("replicate")
							|| function.equals("stuff")
							|| function.startsWith("case")) {
						String idStr = elem.getAttributeValue(QueryConstants.ID);
						if (idStr!=null) {
							if (function.equals("max") || function.equals("min")) {
								elem.setAttribute(QueryConstants.ID_TM,String.valueOf(QueryConstants.TM_QUANTITY));
								elem.setAttribute(QueryConstants.ID_TM_RULEENGINE,String.valueOf(Constants.IDTO_INT));
								ids.setTm(idStr.replaceAll(" ", ""), elem.getName(), Constants.IDTO_INT);
							} else {
								elem.setAttribute(QueryConstants.ID_TM,String.valueOf(QueryConstants.TM_STRING));
								elem.setAttribute(QueryConstants.ID_TM_RULEENGINE,String.valueOf(Constants.IDTO_STRING));
								ids.setTm(idStr.replaceAll(" ", ""), elem.getName(), Constants.IDTO_STRING);
							}
						}
					} else if (function.equals("len")
							|| function.equals("round")
							|| function.equals("floor") || function.equals("ceiling")
							|| function.equals("rand")
							|| function.equals("square") || function.equals("sqrt")
							|| function.equals("exp")
							|| function.equals("day")
							|| function.equals("month")
							|| function.equals("year")
							|| function.equals("convert") || function.equals("cast")
							|| function.equals("abs")) {
						elem.setAttribute(QueryConstants.ID_TM,String.valueOf(QueryConstants.TM_QUANTITY));
						elem.setAttribute(QueryConstants.ID_TM_RULEENGINE,String.valueOf(Constants.IDTO_DOUBLE));
						String idStr = elem.getAttributeValue(QueryConstants.ID);
						if (idStr!=null)
							ids.setTm(idStr.replaceAll(" ", ""), elem.getName(), Constants.IDTO_DOUBLE);
						//TODO esto se puede optimizar al incluirlo en las querys, distinguir entre double(quantity) y int(class)
						//tenerlo en cuenta en queryData para darle un idtmRuleengine apropiado util a la hora de crear el instance
					} else
						debug.addError("Se está usando una operación no permitida: " + function + " en el atributo " + QueryConstants.OP_EXPRES + 
								" del nodo " + elemOri);
				} else
					debug.addError("El operación introducida en el atributo " + QueryConstants.OP_EXPRES + " del nodo " + 
							elemOri + " está mal construida");
			}
		} //else debug ya tratado al parsear la operacion
	}
	
	/*
	 * 3 casos:
	 * 
	 * 1.- no hay agrupacion:
	 * en todos los nodos -> nodoClass.setAttribute(QueryConstants.MULTIVALUE,"TRUE");  !nodoClass.setAttribute(QueryConstants.GROUP,"TRUE");
	 * no se agrupa por ninguno solo por el 1er (por el cual se agrupa siempre)
	 * 
	 * 2.- se agrupa tambien por nodos inferiores:
	 * Todos los nodos tendran MULTIVALUE="TRUE" excepto los hijos de los nodos que están en GROUPBY
	 *  	nodoClass.setAttribute(QueryConstants.MULTIVALUE,"TRUE");
	 * y en el nodo del GROUPBY:
	 *  	nodoClass.setAttribute(QueryConstants.GROUP,"TRUE");
	 * ya que una vez que agrupe el MULTIVALUE no sera necesario porque devuelve un unico registro por ID_O agrupado
	 * 
	 * 3.- se agrupa solo por el nodo root (caso actual en QueryXML)
	 * en todos los nodos -> !nodoClass.setAttribute(QueryConstants.MULTIVALUE,"TRUE");  nodoClass.setAttribute(QueryConstants.GROUP,"TRUE");
	 * este caso no es correcto, se agrupara solo por el root, entra dentro del caso 2
	 */
	//se le pasa un array con los siguientes ids para que sepa hasta donde iterar
	
	//	V2 -> GROUP no tiene sentido sin MULTIVALUE y viceversa
	private void parserMultivalueStructure(HashSet<String> aIdsGroupImportado, Element nodoClass, /*String idView, */
			ArrayList<String> viewIds) {
		//System.out.println("aIdsGroupImportado " + Auxiliar.hashSetStringToString(aIdsGroupImportado, ","));
		boolean existsGroup = aIdsGroupImportado.size()>0;
		//todo multivalue
		parserMultivalueStructureCase1Rec(nodoClass, viewIds);
		//if (!nodoClass.getParent().getName().equals(QueryConstants.STRUCTURE))
			//nodoClass.setAttribute(QueryConstants.GROUP,"TRUE");
		if (existsGroup) {
			Iterator itG = aIdsGroupImportado.iterator();
			while (itG.hasNext()) {
				String idGroup = (String)itG.next();
				String[] names = new String[2];
				names[0] = QueryConstants.CLASS;
				names[1] = QueryConstants.XOR;
				Element group = jdomParser.findElementByAt(nodoClass, names, QueryConstants.ID, idGroup, true, true);
				//group.setAttribute(QueryConstants.GROUP,"TRUE");
				
				boolean quitGroup = true;
				Iterator itG2 = aIdsGroupImportado.iterator();
				while (itG2.hasNext()) {
					String idGroup2 = (String)itG2.next();
					Element subGroup = jdomParser.findElementByAt(group, names, QueryConstants.ID, idGroup2, false, true);
					if (subGroup!=null) {
						quitGroup = false;
						break;
					}
				}
				//si no existe ningun nodo debajo por el que agrupar quitar los group
				if (quitGroup)
					parserMultivalueStructureCase2(group, viewIds);
				
				//quitar GROUP hacia arriba en nodos que no tengan atributos en select
				//y un unico nodo class/xor como hijo
				Element parent = group.getParent();
				while (!parent.getName().equals(QueryConstants.STRUCTURE)) {
					if (!parent.getParent().equals(QueryConstants.STRUCTURE)) {
						//mirar si tiene hijos que esten en select
						boolean hasAttrShow = false;
						int sumaClass = parent.getChildren(QueryConstants.CLASS).size() + 
								parent.getChildren(QueryConstants.XOR).size();
						if (sumaClass==1) {
							Iterator it = parent.getChildren(QueryConstants.ATTRIBUTE).iterator();
							while (it.hasNext()) {
								Element attr = (Element)it.next();
								String idStr = attr.getAttributeValue(QueryConstants.ID);
								boolean isShow = ids.isShow(idStr, true);
								if (isShow)
									hasAttrShow = true;
							}
							if (!hasAttrShow)
								parent.removeAttribute(QueryConstants.GROUP);
						}
					}
					parent = parent.getParent();
				}
				
				//if (aIdsGroupImportado.size()==1 && StringUtils.equals(aIdsGroupImportado.get(0), idView))
					//adaptaMultivalueStructureCaso3(nodoClass, viewIds);
				//else
					//adaptaMultivalueStructureCaso2(aIdsGroupImportado, nodoClass, viewIds);
			}
		}
	}
	private void parserMultivalueStructureCase2(Element nodoClass, ArrayList<String> viewIds) {
		if (StringUtils.equals(nodoClass.getAttributeValue(QueryConstants.GROUP),"TRUE")) {  //para no volver a quitar multivalue 
																					 //de group que esten bajo group
			Iterator it = nodoClass.getChildren().iterator();
			while (it.hasNext()) {
				Element childClass = (Element) it.next();
				String name = childClass.getName();
				if (name.equals(QueryConstants.CLASS) || name.equals(QueryConstants.XOR)) {
					if (!viewIds.contains(childClass.getAttributeValue(QueryConstants.ID))) {
						parserMultivalueStructureCase2(childClass, viewIds);
						childClass.removeAttribute(QueryConstants.GROUP);
					}
				}
			}
		}
	}
	private void parserMultivalueStructureCase1Rec(Element nodoClass, ArrayList<String> viewIds) {
		if (nodoClass.getName().equals(QueryConstants.UNION)) {
			nodoClass.setAttribute(QueryConstants.GROUP,"TRUE");
			Iterator it = nodoClass.getChildren().iterator();
			while (it.hasNext()) {
				Element child = (Element)it.next();
				if (child.getName().equals(QueryConstants.STRUCTURE))
					parserMultivalueStructureCase1(child, viewIds);
				else if (child.getName().equals(QueryConstants.UNION))
					parserMultivalueStructureCase1Rec(child, viewIds);
			}
		} else
			parserMultivalueStructureCase1(nodoClass, viewIds);
	}

	private void parserMultivalueStructureCase1(Element nodoClass, ArrayList<String> viewIds) {
		nodoClass.setAttribute(QueryConstants.GROUP,"TRUE");
		Iterator it = nodoClass.getChildren().iterator();
		while (it.hasNext()) {
			Element childClass = (Element) it.next();
			String name = childClass.getName();
			if (name.equals(QueryConstants.CLASS) || name.equals(QueryConstants.XOR)) {
				if (!viewIds.contains(childClass.getAttributeValue(QueryConstants.ID)))
					parserMultivalueStructureCase1(childClass, viewIds);
			}
		}
	}

	private ArrayList<String> parserSelectPresentation(String idsSG, String idRoot, Element view, ArrayList<IdTmRuleengine> aTmsSelect, 
			ArrayList<String> aNamesSelect, String idStructure) {
		String[] idsSGSpl = idsSG.split(",");
		ArrayList<String> aIdsImportados = new ArrayList<String>();
		ArrayList<String> aIdsImportadosIdos = new ArrayList<String>();
		ArrayList<String> aIdsNoImportados = new ArrayList<String>();
		ArrayList<String> aIdos = new ArrayList<String>();
		if (!isReport && !toInstance) {
			aIdsImportadosIdos.add(idRoot);
			IdTmRuleengine tmSelect = new IdTmRuleengine(QueryConstants.TM_ID, idRoot);
			aTmsSelect.add(tmSelect);
		}
		for (int i=0;i<idsSGSpl.length;i++) {
			String id = idsSGSpl[i].replaceAll(" ", "");
			IDs.IdDetail idD = ids.getDetailShow(id, true);
			if (idD!=null) {
				String name = idD.getName();
				//System.out.println("name " + name);
				if (name!=null)
					aNamesSelect.add(name);
					aIdsImportados.add(id);
					String idParent = idD.getIdParent();
					if (idParent!=null && !aIdos.contains(idParent)) {
						if (!isReport) {
							//if (idRoot.equals(idParent)) {
								aIdsImportadosIdos.add(idParent);
								IdTmRuleengine tmSelect = new IdTmRuleengine(QueryConstants.TM_ID, idParent);
								aTmsSelect.add(tmSelect);
								aIdos.add(idParent);
							/*} else {
								aIdsImportadosIdos.add(idParent);
								IdTmRuleengine tmSelect = new IdTmRuleengine(QueryConstants.TM_ID, idParent);
								aTmsSelect.add(tmSelect);
							}*/
						} else {
//							ver last
							boolean last = ids.getLast(idParent);
							if (last) {
								//System.out.println("last " + last);
								aIdsImportadosIdos.add(idParent);
								IdTmRuleengine tmSelect = new IdTmRuleengine(QueryConstants.TM_ID, idParent);
								aTmsSelect.add(tmSelect);
								aIdos.add(idParent);
							}
						}
					}
					aIdsImportadosIdos.add(id);
					Integer tm = idD.getTm();
					if (tm!=null) {
						IdTmRuleengine tmSelect = new IdTmRuleengine(tm, id);
						aTmsSelect.add(tmSelect);
					}
			} else
				aIdsNoImportados.add(id);
		}
		//si no es report añadir los idos del resto de nodos no tratados, aunque no tengan atributos
		if (!isReport && !toInstance) {
			HashSet<String> allIdClass = ids.getIdClasses(idStructure);
			Iterator it = allIdClass.iterator();
			while (it.hasNext()) {
				String idNode = (String)it.next();
				if (!aIdos.contains(idNode)) {
					aIdsImportadosIdos.add(idNode);
					IdTmRuleengine tmSelect = new IdTmRuleengine(QueryConstants.TM_ID, idNode);
					aTmsSelect.add(tmSelect);
					aIdos.add(idNode);
				}
			}
		} else {
			//añadir los nodos con last que no se hayan añadido
			HashSet<String> allIdClass = ids.getIdClasses(idStructure);
			Iterator it = allIdClass.iterator();
			while (it.hasNext()) {
				String idNode = (String)it.next();
				boolean last = ids.getLast(idNode);
				if (last) {
					aIdsImportadosIdos.add(idNode);
					IdTmRuleengine tmSelect = new IdTmRuleengine(QueryConstants.TM_ID, idNode);
					aTmsSelect.add(tmSelect);
					aIdos.add(idNode);
				}
			}
		}
		if (aIdsNoImportados.size()>0) {
			String warning = "El/Los identificador/es ";
			warning += Auxiliar.arrayToString(aIdsNoImportados, ", ");
			warning += " expuesto/s en el atributo " + QueryConstants.SELECT + " no hace/n referencia a ningun nodo " + 
					QueryConstants.ATTRIBUTE + ", " + QueryConstants.AGREGATION + " o " + QueryConstants.EXPRESION;
			debug.addWarning(warning);
		}
		view.setAttribute(QueryConstants.SELECT,Auxiliar.arrayToString(aIdsImportados, ","));
		//System.out.println("select " + Auxiliar.arrayToString(aIdsImportados, ","));
		//System.out.println("selectIdos " + Auxiliar.arrayToString(aIdsImportadosIdos, ","));
		view.setAttribute(QueryConstants.SELECT_IDO,Auxiliar.arrayToString(aIdsImportadosIdos, ","));
		return aIdsImportados;
	}
	private HashSet<String> parserGroupPresentation(String idsSG, ArrayList<String> idsAgregation, Element nodoClass, 
			Element view) {
		//System.out.println("idsSG " + idsSG);
		//System.out.println("idsAgregation " + Auxiliar.arrayToString(idsAgregation, ","));
		HashSet<String> aIdsImportados = new HashSet<String>();
		ArrayList<String> aIdsNoImportados = new ArrayList<String>();
		if (idsSG!=null) {
			String[] idsSGSpl = idsSG.split(",");
			for (int i=0;i<idsSGSpl.length;i++) {
				String id = idsSGSpl[i].replaceAll(" ", "");
				boolean is = ids.is(id, QueryConstants.CLASS, true);
				if (is) {
					aIdsImportados.add(id);
				} else
					aIdsNoImportados.add(id);
			}
			if (aIdsNoImportados.size()>0) {
				String warning = "";
				warning = "El/Los identificador/es ";
				warning += Auxiliar.arrayToString(aIdsNoImportados, ", ");
				warning += " expuesto/s en el atributo " + QueryConstants.GROUPBY + " no hace/n referencia a ningun nodo " + 
						QueryConstants.CLASS;
				debug.addWarning(warning);
			}
		}
		//ahora se añaden los ids de los padres de los nodos agregation si no estan
		ArrayList<Element> attributesAgregados = jdomParser.findElementsByAt(nodoClass, QueryConstants.ATTRIBUTE, 
				QueryConstants.ID, idsAgregation, true);
		for (int i=0;i<attributesAgregados.size();i++) {
			Element parent = attributesAgregados.get(i).getParent();
//			if (!parent.getParent().getName().equals(QueryConstants.STRUCTURE)) { //añade si no es el nodo principal
				String id = parent.getAttributeValue(QueryConstants.ID);
				aIdsImportados.add(id);
//			}
		}
		if (aIdsImportados.size()>0)
			view.setAttribute(QueryConstants.GROUPBY,Auxiliar.hashSetStringToString(aIdsImportados, ","));
		return aIdsImportados;
	}

	private void parserOrderPresentation(String idsO, ArrayList<String> aIdsSelectImportado, Element view) {
		String[] idsOrderSpl = idsO.split(",");
		String idImportado = "";
		ArrayList<String> idsNoSelect = new ArrayList<String>();
		ArrayList<String> idsSentidoIncorrecto = new ArrayList<String>();
		for (int i=0;i<idsOrderSpl.length;i++) {
			String idSentido = idsOrderSpl[i].replaceAll(" ", "");
			String[] idSentidoSpl = idSentido.split("#");
			String id = idSentidoSpl[0];
			boolean isShow = ids.isShow(id, true);
			if (isShow) {
				if (aIdsSelectImportado.contains(id)) {
					if (idImportado.length()>0)
						idImportado += ",";
					idImportado += id;
					if (idSentidoSpl.length>1) {
						String sentido = idSentidoSpl[1];
						if (StringUtils.equals(sentido.toLowerCase(), "desc"))
							idImportado += "#" + QueryConstants.ORDER_DESC;
						else if (StringUtils.equals(sentido.toLowerCase(), "asc"))
							idImportado += "#" + QueryConstants.ORDER_ASC;
						else
							idsSentidoIncorrecto.add(idSentido);
						}
				} else
					idsNoSelect.add(idSentido);
			} else
				idsNoSelect.add(idSentido);
		}
		debugIDsOrderPresentation(idsSentidoIncorrecto, idsNoSelect);
		view.setAttribute(QueryConstants.ORDERBY,idImportado);
	}
	private void debugIDsOrderPresentation(ArrayList<String> idsSentidoIncorrecto, ArrayList<String> idsNoSelect) {
		if (idsSentidoIncorrecto.size()>0) {
			String warning = "";
			warning = "El sentido indicado en el/los identificador/es ";
			warning += Auxiliar.arrayToString(idsSentidoIncorrecto, ", ");
			warning += " no es/son correcto/s. solo se permite asc y desc";
			debug.addWarning(warning);
		}
		if (idsNoSelect.size()>0) {
			String warning = "";
			warning = "El/Los identificador/es ";
			warning += Auxiliar.arrayToString(idsNoSelect, ", ");
			warning += " por el/los que se quiere ordenar en el atributo " + QueryConstants.ORDERBY + 
				" no está/n presente/s en el atributo " + QueryConstants.SELECT + " o no son validos, es decir, no son " + 
				QueryConstants.ATTRIBUTE + ", " + QueryConstants.AGREGATION + " o " + QueryConstants.EXPRESION;
			debug.addWarning(warning);
		}
	}
	private void parserOrderPresentationUnion(String idsO, HashMap<String,Integer> idsPosic, Element order) {
//		buscar en las select los numeros de columnas o añadirlos en el mapa de: id + columna  (idsPosic)
		String[] idsOrderSpl = idsO.split(",");
		String idImportado = "";
		boolean errorColumn = false;
		//array de <column, <array de identif>>
		ArrayList<Integer> columnNoRepeat = new ArrayList<Integer>();
		ArrayList<String> idsNoSelect = new ArrayList<String>();
		ArrayList<String> idsSentidoIncorrecto = new ArrayList<String>();
		HashMap<Integer,ArrayList<String>> mColumn = new HashMap<Integer,ArrayList<String>>();

		for (int i=0;i<idsOrderSpl.length;i++) {
			String idSentido = idsOrderSpl[i].replaceAll(" ", "");
			String[] idSentidoSpl = idSentido.split("#");
			String id = idSentidoSpl[0];
			Integer nCol = idsPosic.get(id);
			if (nCol!=null) {
				if (!columnNoRepeat.contains(nCol)) {
					columnNoRepeat.add(nCol);
					ArrayList<String> ident = new ArrayList<String>();
					ident.add(id);
					mColumn.put(nCol, ident);
					if (idImportado.length()>0)
						idImportado += ",";
					idImportado += nCol;
					if (idSentidoSpl.length>1) {
						String sentido = idSentidoSpl[1];
						if (StringUtils.equals(sentido.toLowerCase(), "desc"))
							idImportado += "#" + QueryConstants.ORDER_DESC;
						else if (StringUtils.equals(sentido.toLowerCase(), "asc"))
							idImportado += "#" + QueryConstants.ORDER_ASC;
						else
							idsSentidoIncorrecto.add(idSentido);
					}
				} else {
					ArrayList<String> idents = mColumn.get(nCol);
					idents.add(id);
					errorColumn = true;
				}
			} else
				idsNoSelect.add(idSentido);
		}
		debugIDsOrderPresentation(idsSentidoIncorrecto, idsNoSelect);
		if (errorColumn) {
			String error = "Se pretende ordenar por identificadores que hacen referencia a la misma columna de la consulta:";
			Iterator it = mColumn.keySet().iterator();
			String errorCont = "";
			while (it.hasNext()) {
				Integer key = (Integer)it.next();
				ArrayList<String> idents = mColumn.get(key);
				if (errorCont.length()>0)
					errorCont += "; ";
				errorCont += Auxiliar.arrayToString(idents, ", ") + " hacen referencia a la columna " + key;
			}
			debug.addError(error + errorCont);
		}
		order.setAttribute(QueryConstants.ORDERBY_NCOL,idImportado);
		order.removeAttribute(QueryConstants.ORDERBY);
	}
	
	private boolean parserIDsOP(Element op, boolean logicHaving) throws JDOMException {
		boolean hasHaving = false;
		String idOP = op.getAttributeValue(QueryConstants.ID_OP).toLowerCase();
		if (idOP!=null) {
			if (StringUtils.equals(idOP, "and"))
				op.setAttribute(QueryConstants.ID_OP,String.valueOf(QueryConstants.OP_AND));
			else if (StringUtils.equals(idOP, "or"))
				op.setAttribute(QueryConstants.ID_OP,String.valueOf(QueryConstants.OP_OR));
			else
				debug.addWarning("La operación " + idOP + " no es valida. solo se permiten las operaciones or y and en el nodo " + 
						QueryConstants.OP);
		} else
			debug.addWarning("El nodo " + jdomParser.returnNodeXML(op) + " no tiene atributo " + QueryConstants.ID_OP + 
					". Este nodo no se tendra en cuenta");
			
		Iterator it = op.getChildren().iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			if (child.getName().equals(QueryConstants.OP))
				hasHaving = parserIDsOP(child, logicHaving) || hasHaving;
			else if (child.getName().equals(QueryConstants.CONDITION)) {
				String idCondition = child.getAttributeValue(QueryConstants.ID_CONDITION);
				if (ids.is(idCondition, QueryConstants.WHERE, false)) {
					getIdNode(child, QueryConstants.ID_CONDITION, QueryConstants.WHERE);
					if (StringUtils.equals(child.getAttributeValue(QueryConstants.REQUIRED), "TRUE"))
						debug.addWarning("El atributo " + QueryConstants.REQUIRED + " no va en el nodo " + QueryConstants.CONDITION 
								+ " cuando se hace referencia a un nodo " + QueryConstants.WHERE + ". En este caso al nodo " + 
								QueryConstants.WHERE + " con " + QueryConstants.ID + " " + idCondition);
					else if (StringUtils.equals(child.getAttributeValue(QueryConstants.NULL), "TRUE"))
						debug.addWarning("El atributo " + QueryConstants.NULL + " no va en el nodo " + QueryConstants.CONDITION 
								+ " cuando se hace referencia a un nodo " + QueryConstants.WHERE + ". En este caso al nodo " + 
								QueryConstants.WHERE + " con " + QueryConstants.ID + " " + idCondition);
				} else if (logicHaving && ids.is(idCondition, QueryConstants.HAVING, false)) {
					getIdNode(child, QueryConstants.ID_CONDITION, QueryConstants.HAVING);
					hasHaving = true;
					if (StringUtils.equals(child.getAttributeValue(QueryConstants.REQUIRED), "TRUE"))
						debug.addWarning("El atributo " + QueryConstants.REQUIRED + " no va en el nodo " + QueryConstants.CONDITION 
								+ " cuando se hace referencia a un nodo " + QueryConstants.HAVING + ". En este caso al nodo " + 
								QueryConstants.HAVING + " con " + QueryConstants.ID + " " + idCondition);
					if (StringUtils.equals(child.getAttributeValue(QueryConstants.NULL), "TRUE"))
						debug.addWarning("El atributo " + QueryConstants.NULL + " no va en el nodo " + QueryConstants.CONDITION 
								+ " cuando se hace referencia a un nodo " + QueryConstants.HAVING + ". En este caso al nodo " + 
								QueryConstants.HAVING + " con " + QueryConstants.ID + " " + idCondition);
				} else if (ids.is(idCondition, QueryConstants.ATTRIBUTE, false)) {
					getIdNode(child, QueryConstants.ID_CONDITION, QueryConstants.ATTRIBUTE);
					if (!StringUtils.equals(child.getAttributeValue(QueryConstants.REQUIRED), "TRUE") && 
							!StringUtils.equals(child.getAttributeValue(QueryConstants.NULL), "TRUE"))
						debug.addWarning("Cuando se utiliza una referencia a un nodo " + QueryConstants.ATTRIBUTE + " en un nodo " + 
								QueryConstants.CONDITION + " debe ponerse en este nodo el atributo " + QueryConstants.REQUIRED + 
								" o el atributo " + QueryConstants.NULL + " a TRUE, ya que si este atributo no se pone con este nodo no " +
								"se establecera ninguna condición. En este caso se hace referencia al nodo con " + QueryConstants.ID + " " + idCondition);
					else if (StringUtils.equals(child.getAttributeValue(QueryConstants.REQUIRED), "TRUE") && 
							StringUtils.equals(child.getAttributeValue(QueryConstants.NULL), "TRUE"))
						debug.addError("Cuando se utiliza una referencia a un nodo " + QueryConstants.ATTRIBUTE + " en un nodo " + 
								QueryConstants.CONDITION + " debe ponerse en este nodo el atributo " + QueryConstants.REQUIRED + 
								" o el atributo " + QueryConstants.NULL + " a TRUE, no los dos ya que se establece una contradicción. " +
								"En este caso se hace referencia al nodo con " + QueryConstants.ID + " " + idCondition);
				} else {
					if (logicHaving)
						debug.addError("El atributo " + QueryConstants.ID_CONDITION + " del nodo " + 
								jdomParser.returnNodeXML(child) + " no hace referencia a ningun nodo " + QueryConstants.ATTRIBUTE + 
								", " + QueryConstants.WHERE + " o " + QueryConstants.HAVING + ". Este nodo no se tendra en cuenta");
					else
						debug.addError("El atributo " + QueryConstants.ID_CONDITION + " del nodo " + 
								jdomParser.returnNodeXML(child) + " no hace referencia a ningun nodo " + QueryConstants.ATTRIBUTE + 
								" o " + QueryConstants.WHERE + ". Este nodo no se tendra en cuenta");
				}
			}
		}
		return hasHaving;
	}
	
	private void parserIDsAgregExpr(Element agregExpr, String agregExprOri, ArrayList<String> idsAgregation, HashMap<String,ArrayList<CheckParam>> nameParams, String idStructure) {
		String op = "";
		String name = agregExpr.getName();
		String opName = "";
		if (name.equals(QueryConstants.AGREGATION))
			opName = QueryConstants.OP_AGREG;
		else if (name.equals(QueryConstants.EXPRESION))
			opName = QueryConstants.OP_EXPRES;
		op = agregExpr.getAttributeValue(opName);
		if (op==null && opName.equals(QueryConstants.OP_EXPRES)) {
			GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
			op = agregExpr.getAttributeValue(gSQL.getOpExpres());
		}

		parserIdNode(agregExpr, agregExprOri, null, false, nameParams, idStructure);
		
		//si es agregado o expresion debe tener al menos una columna a agregar
		//"SUBSTRING({rdnsubCuenta1Debe},0,4)"
		if (op!=null) {
			if (!op.equals("COUNT")) {
				boolean salir = false;
				int fin = 0;
				//si alguno tiene idTm date o dateTime es fecha
				boolean isDate = false;
				int countColumn = 0;
				while (!salir) {
					int inicio = op.indexOf("{",fin);
					//System.out.println("inicio " + inicio);
					//System.out.println("fin " + fin);
					if (inicio!=-1) {
						fin = op.indexOf("}",inicio);
						if (fin!=-1) {
							String idAttributeStr = op.substring(inicio+1, fin);
							boolean is = false;
							if (name.equals(QueryConstants.AGREGATION)) {
								is = ids.is(idAttributeStr, QueryConstants.ATTRIBUTE, true);
								countColumn++;
							} else if (name.equals(QueryConstants.EXPRESION)) {
								String[] names = new String[2];
								names[0] = QueryConstants.ATTRIBUTE;
								names[1] = QueryConstants.AGREGATION;
								is = ids.is(idAttributeStr, names, true);
								countColumn++;
							}
							if (is) {
								idsAgregation.add(idAttributeStr);
								Integer idtmRuleengineTemp = ids.getTm(idAttributeStr, QueryConstants.ATTRIBUTE);
								if (idtmRuleengineTemp!=null && (idtmRuleengineTemp.equals(Constants.IDTO_DATE) || 
																 idtmRuleengineTemp.equals(Constants.IDTO_DATETIME)))
									isDate = true;
							} else {
								if (name.equals(QueryConstants.AGREGATION))
									debug.addError("El identificador " + idAttributeStr + " usado en el atributo " + opName + 
										" del nodo " + agregExprOri + " no hace referencia a un nodo " + QueryConstants.ATTRIBUTE);
								else if (name.equals(QueryConstants.EXPRESION))
									debug.addError("El identificador " + idAttributeStr + " usado en el atributo " + opName + 
											" del nodo " + agregExprOri + " no hace referencia a un nodo " + QueryConstants.ATTRIBUTE
											+ " o " + QueryConstants.AGREGATION);
							}
						} else {
							debug.addError("La operación introducida en el atributo " + opName + " del nodo " + 
									agregExprOri + " está mal construida. Problema con las llaves");
							salir = true;
						}
					} else {
						salir = true;
					}
				}
				if (countColumn==0) {
					if (name.equals(QueryConstants.AGREGATION))
						debug.addError("En nodos " + QueryConstants.AGREGATION + " debe haber al menos una referencia a un nodo " + 
								QueryConstants.ATTRIBUTE + " para hacer agrupación de datos. Esto no se cumple en el nodo " + agregExprOri);
					else if (name.equals(QueryConstants.EXPRESION))
						debug.addError("En nodos " + QueryConstants.EXPRESION + " debe haber al menos una referencia a un nodo " + 
								QueryConstants.ATTRIBUTE + " o " + QueryConstants.AGREGATION + " para hacer uso de una expresian. " +
								"Esto no se cumple en el nodo " + agregExprOri);
				}
				if (isDate)
					agregExpr.setAttribute(QueryConstants.ID_TM_RULEENGINE, String.valueOf(Constants.IDTO_DATETIME));
				else
					agregExpr.setAttribute(QueryConstants.ID_TM_RULEENGINE, String.valueOf(Constants.IDTO_DOUBLE));
			}
		} else
			debug.addError("El nodo " + agregExprOri + " deberia tener un atributo " + opName + 
					" para indicar la operación del nodo " + name);
	}
	
	//
	private void getIdNodeView(Element view, Element structure, boolean deleteId) throws JDOMException {
		boolean isRoot = view.getParent().getName().equals(QueryConstants.PRESENTATION);
		String id = view.getAttributeValue(QueryConstants.ID);
		if (id!=null) {
			if (id.length()==0)
				debug.addError("El atributo " + QueryConstants.ID + " en un nodo " + QueryConstants.VIEW + 
						" no puede contener una cadena vacía");

			id = id.replaceAll(" ", "");
			boolean comprueba = false;
			if (isRoot) {
				Element parentStructure = structure.getParent();
				if (parentStructure.getName().equals(QueryConstants.UNION)) {
					boolean is = ids.is(id, QueryConstants.UNION, true);
					if (is || (parentStructure.getAttributeValue(QueryConstants.ID)!=null && 
										parentStructure.getAttributeValue(QueryConstants.ID)!=id))
						debug.addWarning("El nodo " + jdomParser.returnNodeXML(view) + " que está bajo " + QueryConstants.PRESENTATION + 
								" debe hacer referencia al nodo " + QueryConstants.UNION + 
								", aunque referencie a otro nodo internamente se tratara como si referenciara a " + QueryConstants.UNION);
				} //else
					//comprueba = true;
			} else
				comprueba = true;
				
			if (comprueba) {
				String[] nodes = new String[3];
				nodes[0] = QueryConstants.STRUCTURE;
				nodes[1] = QueryConstants.CLASS;
				nodes[2] = QueryConstants.XOR;
				boolean is = ids.is(id, nodes, true);
				if (!is)
					debug.addError("El atributo " + QueryConstants.ID + " con el valor " + id + " del nodo " + 
							jdomParser.returnNodeXML(view) + " no hace referencia a un nodo " + QueryConstants.STRUCTURE + ", " + 
							QueryConstants.CLASS + " o " + QueryConstants.XOR);
			}
		} else if (isRoot && caseUnionReports /*|| !isRoot*/)
			debug.addError("El nodo " + jdomParser.returnNodeXML(view) + " deberaa tener un atributo " + QueryConstants.ID);
	}
	
	private void getIdNode(Element elem, String etiq, String name) throws JDOMException {
		String idStr = elem.getAttributeValue(etiq);
		if (idStr!=null) {
			boolean is = ids.is(idStr, name, true);
			if (!is)
				debug.addError("El atributo " + etiq + " con el valor " + idStr + " del nodo " + jdomParser.returnNodeXML(elem) 
						+ " no se encuentra bajo el nodo " + QueryConstants.STRUCTURE + " en un nodo " + name);
		}
	}
	private void getIdNodeCase(Element elem, ArrayList<Element> idsAttributesWithCase) {
		String idStr = elem.getAttributeValue(QueryConstants.ID_CASE);
		if (idStr!=null) {
			boolean is = ids.is(idStr, QueryConstants.CASE, true);
			if (!is)
				idsAttributesWithCase.add(elem);
		}
	}
	
	private void getIdNodeShow(Element elem, String etiq, String elemOri) {
		String idStr = elem.getAttributeValue(etiq);
		if (idStr!=null) {
			boolean isShow = ids.isShow(idStr, true);
			if (!isShow)
				debug.addError("El atributo " + etiq + " con el valor " + idStr + " del nodo " + elemOri 
						+ " no se encuentra bajo el nodo " + QueryConstants.STRUCTURE + " en un nodo " + QueryConstants.ATTRIBUTE + 
						", " + QueryConstants.AGREGATION + " o " + QueryConstants.EXPRESION);
		}
	}
	
	private void addIdClass(Element elem, String idStrOrig, String elemOri) {
		String idTm = elem.getAttributeValue(QueryConstants.ID_TM);
		String nameNode = elem.getName();
		boolean putIdClass = toInstance && (nameNode.equals(QueryConstants.CLASS) || 
				nameNode.equals(QueryConstants.XOR) || 
				(nameNode.equals(QueryConstants.ATTRIBUTE) && idTm!=null && Integer.parseInt(idTm)==QueryConstants.TM_VALUE_RDN));

		if (putIdClass) {
			if (idStrOrig!=null) {
				if (!Auxiliar.hasIntValue(idStrOrig))
					elem.setAttribute(QueryConstants.ID_CLASS, idStrOrig);
			} else
				debug.addError("Para pasar a instance es necesario que los nodos " + QueryConstants.CLASS + " lleven el atributo " + QueryConstants.ID
						+ ". Esto no sucede con el nodo " + elemOri);
		}
	}
	private void parserIdNode(Element elem, String elemOri, ArrayList<String> idViews, boolean isNextNodeStructureUnion, HashMap<String,ArrayList<CheckParam>> nameParams, String idStructure) {
		String idStrOrig = elem.getAttributeValue(QueryConstants.ID);
		String nameNode = elem.getName();
		if (idStrOrig!=null) {
			if (Auxiliar.hasIntValue(idStrOrig))
				debug.addError("No es posible usar atributos " + QueryConstants.ID + " numeros. Esto ocurre con el atributo " + idStrOrig);
		} else {
			elem.setAttribute(QueryConstants.ID, String.valueOf(getNextId()));
		}
		if (nameNode.equals(QueryConstants.CLASS) || nameNode.equals(QueryConstants.XOR)) {
			Element parent = elem.getParent();
			boolean isLast = StringUtils.equals(parent.getAttributeValue(QueryConstants.LAST),"TRUE");
			if (!isLast) {
				if (StringUtils.equals(parent.getAttributeValue(QueryConstants.HAS_SUBREPORT),"TRUE") || 
						idViews!=null && idStrOrig!=null && idViews.contains(idStrOrig)) {
					//si esta contenido en el array de views obtener el id del padre y modificar el last en xml y en nodo padre
					String idParent = parent.getAttributeValue(QueryConstants.ID);
					String nameNodeParent = parent.getName();
					ids.setLastTrue(idParent, nameNodeParent);
					parent.setAttribute(QueryConstants.LAST, "TRUE");
				}
			}
		}
		
		boolean hasValue = false;
		if (!isNextNodeStructureUnion) {
			if (nameNode.equals(QueryConstants.WHERE) || nameNode.equals(QueryConstants.CASE)) {
				if (elem.getAttributeValue(QueryConstants.VAL_MIN)!=null && elem.getAttributeValue(QueryConstants.VAL_MIN).contains("(VALUE)") 
						|| elem.getAttributeValue(QueryConstants.VAL_MAX)!=null && elem.getAttributeValue(QueryConstants.VAL_MAX).contains("(VALUE)") 
						|| elem.getAttributeValue(QueryConstants.VALUE)!=null && elem.getAttributeValue(QueryConstants.VALUE).contains("(VALUE)") 
						|| elem.getText()!=null && elem.getText().contains("(VALUE)"))
					hasValue = true;
			} else if (nameNode.equals(QueryConstants.ATTRIBUTE) && elem.getText()!=null && elem.getText().contains("(VALUE)")) {
				hasValue = true;
			} else if (nameNode.equals(QueryConstants.CLASS)) {
				if (elem.getAttributeValue(QueryConstants.ID_O)!=null && elem.getAttributeValue(QueryConstants.ID_O).equals("(VALUE)") 
						|| elem.getAttributeValue(QueryConstants.NOT_ID_O)!=null && elem.getAttributeValue(QueryConstants.NOT_ID_O).equals("(VALUE)"))
					hasValue = true;
			}
		}
		//TODO falta añadir comprobacion de value a ATTRIBUTE_CASE
		if (hasValue) {
			if (nameNode.equals(QueryConstants.CLASS)) {
				//tiene que tener idto no multiple
				String className = elem.getAttributeValue(QueryConstants.CLASS_NAME);
				if (className==null)
					debug.addError("El atributo " + QueryConstants.CLASS_NAME + " en un nodo " + QueryConstants.CLASS + " con el parámetro (VALUE) es obligatorio. " +
							"Esto no se cumple en el nodo " + elemOri);
				/*else if(className.split(",").length>1 && (!className.startsWith("[") || !className.endsWith("]")))
					debug.addError("El atributo " + QueryConstants.CLASS_NAME + " en un nodo " + QueryConstants.CLASS + " con el parámetro (VALUE) no puede ser multiple. " +
							"Esto no se cumple en el nodo " + elemOri);*/
			}
			String name = elem.getAttributeValue(QueryConstants.NAME);
			//el id debe ser unico en toda la query y obligatorio
			//el name y el alias deben ser obligatorio
			String alias = elem.getAttributeValue(QueryConstants.ALIAS);
			if (name!=null && alias!=null) {
				String idParam = elem.getAttributeValue(QueryConstants.ID_PARAM);
				
				//hasmap idParam con array de name y alias
				if (idParam!=null) {
					//System.out.println("idParam " + idParam);
					ArrayList<CheckParam> aParam = nameParams.get(idParam);
					if (aParam==null) {
						aParam = new ArrayList<CheckParam>();
						nameParams.put(idParam, aParam);
					}
					CheckParam cp = new CheckParam(name, alias);
					aParam.add(cp);
				}
			}
			if (name==null || alias==null || idStrOrig==null) {
				String error = "";
				if (name==null && idStrOrig==null)
					error += "Los atributos ";
				else
					error += "El atributo ";
				if (name==null) {
					error += QueryConstants.NAME;
					if (alias==null) {
						if (idStrOrig!=null)
							error += " y " + QueryConstants.ALIAS;
						else
							error += ", " + QueryConstants.ALIAS;
					}
				}else if (alias==null)
					error += QueryConstants.ALIAS;
				if (idStrOrig==null) {
					if (name==null || alias==null)
						error += " e ";
					error += QueryConstants.ID;
				}
				error += " en nodos que usan el parámetro (VALUE)";
				if (name==null && idStrOrig==null)
					error += " son obligatorios. ";
				else
					error += " es obligatorio. ";
				error += "Esto no se cumple en el nodo " + elemOri;
				debug.addError(error);
			}
		}
		boolean real = false;
		if (idStrOrig!=null) {
			String idStr = idStrOrig;
			if (idStrOrig.contains(" ")) {
				idStr = idStrOrig.replaceAll(" ", "");
				if (!isNextNodeStructureUnion)
					debug.addWarning("El atributo " + QueryConstants.ID + " " + idStrOrig + " contiene espacios. " +
							"Los espacios seran eliminados");
			}
			
			String name = null;
			String idParent = null;
//			comprobar tamb el atributo name
			//daria error solo si se muestran 2 nodos o mas con el mismo nombre
			if (nameNode.equals(QueryConstants.ATTRIBUTE) ||
					nameNode.equals(QueryConstants.AGREGATION) || nameNode.equals(QueryConstants.EXPRESION)) {
				name = elem.getAttributeValue(QueryConstants.NAME);
			}
			if (!nameNode.equals(QueryConstants.AGREGATION) && !nameNode.equals(QueryConstants.EXPRESION))
				idParent = elem.getParent().getAttributeValue(QueryConstants.ID);
			//System.out.println(idStr + " tiene el padre " + elem.getParent().getAttributeValue(QueryConstants.ID));
			real = true;
			boolean insert = ids.put(elem, idStr, nameNode, name, idParent, idStructure, real);
			if (!isNextNodeStructureUnion && !insert)
				debug.addError("El atributo " + QueryConstants.ID + " " + idStrOrig + " está repetido. " +
						"Los identificadores deben ser unicos en todo el xml");
		} else {
			ids.put(elem, elem.getAttributeValue(QueryConstants.ID), nameNode, null, null, idStructure, real);
			ids.addNodeNoUsed(jdomParser.cloneNode(elem));
		}
	}
	
	private void parserName(Element elem, String elemOri) {
		String name = elem.getAttributeValue("NAME");
		if (StringUtils.contains(name, 'á') || StringUtils.contains(name, 'é') || StringUtils.contains(name, 'í') || 
				StringUtils.contains(name, 'ó') || StringUtils.contains(name, 'ú') || 
				StringUtils.contains(name, 'Á') || StringUtils.contains(name, 'É') || StringUtils.contains(name, 'Í') || 
				StringUtils.contains(name, 'Ó') || StringUtils.contains(name, 'Ú')) {
			debug.addError("El atributo " + QueryConstants.NAME + " no puede contener acentos, ya que este atributo se usara en " +
					"iReport como nombre de los campos y este no soporta los acentos. Esto ocurre en el nodo " + elemOri);
		}
	}
	
	//Nodo Structure
	private void parserIDsCaseStructure(ArrayList<Element> idsAttributesWithCase) throws JDOMException {
		for (int i=0;i<idsAttributesWithCase.size();i++) {
			Element attrib = idsAttributesWithCase.get(i);
			getIdNode(attrib, QueryConstants.ID_CASE, QueryConstants.CASE);
		}
	}
	/*private void processInner(ConnectionDB conDb, Element elem) throws SQLException, NamingException {
		String elemName = elem.getName();
		if (elemName.equals(QueryConstants.CLASS)) {
			Element parent = elem.getParent();
			if (parent.getName().equals(QueryConstants.STRUCTURE)) {
				//si todos sus hijos tienen cardMinMayorIgualQue1 
				//y no tiene hermanos CLASS o son 2 hermanos y uno de ellos es mi_empresa
				//entonces es inner
				int childs = parent.getChildren(QueryConstants.CLASS).size();
				if (childs==2) {
					//si uno de ellos es mi_empresa -> entra = true
					Iterator itPr = parent.getChildren(QueryConstants.CLASS).iterator();
					while (itPr.hasNext()) {
						Element child = (Element)itPr.next();
						
						String idtoStr = child.getAttributeValue(QueryConstants.ID_TO);
						if (idtoStr!=null) {
							String nameClass = getClassName(conDb, idtoStr);
							if (nameClass.equals("MI_EMPRESA"))
								child.setAttribute(QueryConstants.INNER, "TRUE");
						}
					}
				}
				boolean allInner = true;
				Iterator itCl = elem.getChildren(QueryConstants.CLASS).iterator();
				while (itCl.hasNext()) {
					Element child = (Element)itCl.next();
					String idtoS = null;
					if(StringUtils.equals(child.getAttributeValue(QueryConstants.REVERSED),"TRUE"))
						idtoS = child.getAttributeValue(QueryConstants.ID_TO);
					else
						idtoS = elem.getAttributeValue(QueryConstants.ID_TO);
					
					if (idtoS!=null) {
						boolean allCard = true;
						String idtoSpl[] = idtoS.split(",");
						Integer idProp = Integer.parseInt(child.getAttributeValue(QueryConstants.PROP));
						for (int i=0;i<idtoSpl.length;i++) {
							String idtoi = idtoSpl[i];
							Integer idto = Integer.parseInt(idtoi);
							boolean cardMinMayorIgualQue1 = isCardMinimaMayorIgualQue1(conDb, idto, idProp);
							if (!cardMinMayorIgualQue1)
								allCard = false;
						}
						if (!allCard) {
							allInner = false;
							child.setAttribute(QueryConstants.NO_INNER, "TRUE");
							break;
						} else if (!StringUtils.equals(child.getAttributeValue(QueryConstants.REVERSED),"TRUE")) {
							child.setAttribute(QueryConstants.INNER, "TRUE");
						}
					}
				}
				if (allInner) {
					elem.setAttribute(QueryConstants.INNER, "TRUE");
				}
			}
			else {
				if (StringUtils.equals(elem.getAttributeValue(QueryConstants.NO_INNER), "TRUE")) {
					elem.removeAttribute(QueryConstants.NO_INNER);
				} else if (!StringUtils.equals(elem.getAttributeValue(QueryConstants.INNER), "TRUE")) {
					//Element parentP = parent.getParent();
					//esto ya se fija al iterar
					//if (parentP.getName().equals(QueryConstants.STRUCTURE) || 
					//		StringUtils.equals(parent.getAttributeValue(QueryConstants.INNER), "TRUE")) {
						String idtoS = null;
						if(StringUtils.equals(elem.getAttributeValue(QueryConstants.REVERSED),"TRUE")) {
							if (StringUtils.equals(parent.getAttributeValue(QueryConstants.INNER), "TRUE"))
								idtoS = elem.getAttributeValue(QueryConstants.ID_TO);
						} else
							idtoS = parent.getAttributeValue(QueryConstants.ID_TO);
						
						if (idtoS!=null) {
							boolean allCard = true;
							String idtoSpl[] = idtoS.split(",");
							Integer idProp = Integer.parseInt(elem.getAttributeValue(QueryConstants.PROP));
							for (int i=0;i<idtoSpl.length;i++) {
								String idtoi = idtoSpl[i];
								Integer idto = Integer.parseInt(idtoi);
								boolean cardMinMayorIgualQue1 = isCardMinimaMayorIgualQue1(conDb, idto, idProp);
								if (!cardMinMayorIgualQue1)
									allCard = false;
							}
							if (allCard)
								elem.setAttribute(QueryConstants.INNER, "TRUE");
						}
					//}
				}
			}
			if (parent.getName().equals(QueryConstants.STRUCTURE) || 
					StringUtils.equals(elem.getAttributeValue(QueryConstants.INNER), "TRUE")) {
				Iterator it = elem.getChildren(QueryConstants.CLASS).iterator();
				while (it.hasNext()) {
					Element child = (Element)it.next();
					processInner(conDb, child);
				}
			}
		}
	}*/
	
	private HasChildren parserIDsStructure(ConnectionDB conDb, Element elem, ArrayList<String> idViews, ArrayList<Element> idsAttributesWithCase, 
			ArrayList<Element> nodesNull, HashMap<String, Boolean> aRequired, HashMap<String, Boolean> aNull, HashMap<String,ArrayList<CheckParam>> nameParams, String idStructure) 
				throws JDOMException, NamingException, SQLException, DataErrorException, ParseException {
		//adapta ID
		HasChildren hasChildren = new HasChildren();
		boolean hasAttrib = false;
		boolean hasRequired = false;
		String elemOri = jdomParser.returnNodeXML(elem);
		String idStrOrig = elem.getAttributeValue(QueryConstants.ID);
		parserIdNode(elem, elemOri, idViews, false, nameParams, idStructure);
		//adapta ID_O
		parserIdOStructure(elem, elemOri);
		parserNotIdOStructure(elem, elemOri);
		
		String elemName = elem.getName();
		if (!elemName.equals(QueryConstants.XOR)) {
			if (elemName.equals(QueryConstants.CLASS) || elemName.equals(QueryConstants.ATTRIBUTE) || 
					elemName.equals(QueryConstants.WHERE) || elemName.equals(QueryConstants.CASE)) {
			
				HashMap<Integer,String> hProp = new HashMap<Integer, String>();
				if (elemName.equals(QueryConstants.CLASS)) {
					if (StringUtils.equals(elem.getAttributeValue(QueryConstants.REQUIRED), "TRUE")
							|| StringUtils.equals(elem.getAttributeValue(QueryConstants.REQUIRED_IF_FIXED), "TRUE"))
						hasRequired = true;
					String propS = elem.getAttributeValue(QueryConstants.PROP);
//					adapta PROP e incluye ID_TM
					parserPropIdTmStructure(conDb, elem, elemOri, idStrOrig, elem, elemOri, hProp);
					//adapta ID_TO
					String idtoS = elem.getAttributeValue(QueryConstants.CLASS_NAME);
					HashSet<Integer> aIdtos = parserIdToStructure(conDb, elem, elemOri);
					String propIntS = elem.getAttributeValue(QueryConstants.PROP);
					Integer prop = null;
					if (propIntS!=null && Auxiliar.hasIntValue(propIntS)) {
						prop = Integer.parseInt(propIntS);
						//añade REVERSED si la property está invertida
						parserReversedStructure(conDb, elem, propS, prop, idtoS, aIdtos);
					}
				} else if (elemName.equals(QueryConstants.ATTRIBUTE) || elemName.equals(QueryConstants.WHERE) || 
						elemName.equals(QueryConstants.CASE)) {
					//chequear property
					if (elemName.equals(QueryConstants.ATTRIBUTE) && elem.getChild(QueryConstants.ATTRIBUTE_CASE)==null ||
							elemName.equals(QueryConstants.WHERE) || elemName.equals(QueryConstants.CASE)) {
//						adapta PROP e incluye ID_TM
						parserPropIdTmStructure(conDb, elem, elemOri, idStrOrig, elem, elemOri, hProp);
						String idtoS = elem.getAttributeValue(QueryConstants.CLASS_NAME);
						if (idtoS!=null)
							parserIdToStructure(conDb, elem, elemOri);
					} else {
						Iterator it = elem.getChildren(QueryConstants.ATTRIBUTE_CASE).iterator();
						boolean failIdTm = false;
						while (it.hasNext()) {
							Element child = (Element)it.next();
							String childOri = jdomParser.returnNodeXML(child);
//							adapta PROP e incluye ID_TM
							failIdTm = failIdTm || parserPropIdTmStructure(conDb, elem, elemOri, idStrOrig, child, childOri, hProp);
							parserIdToStructure(conDb, child, childOri);
						}
						//comprobacion de tm
						if (failIdTm)
							debug.addError("El tipo de datos debe ser el mismo en cada nodo " + QueryConstants.ATTRIBUTE_CASE + " hermanos. " +
									"Esto no ocurre en los nodos que están bajo " + elemOri);
					}
					String tm = elem.getAttributeValue(QueryConstants.ID_TM);
					boolean isValueRdn = tm!=null && Integer.parseInt(tm)==QueryConstants.TM_VALUE_RDN;
					if (isValueRdn) {
						Element parent = getParentClass(elem.getParent());
						String idtoParentS = null;
						if (elem.getChild(QueryConstants.ATTRIBUTE_CASE)!=null) {
							//iterar por los hijos
							Iterator it = elem.getChildren(QueryConstants.ATTRIBUTE_CASE).iterator();
							while (it.hasNext()) {
								Element child = (Element)it.next();
//								parserIdToStructure(conDb, child, childOri);
								idtoParentS = child.getAttributeValue(QueryConstants.ID_TO);
								if (tm!=null) {
									String propIntS = child.getAttributeValue(QueryConstants.PROP);
									Integer prop = null;
									if (propIntS!=null && Auxiliar.hasIntValue(propIntS)) {
										prop = Integer.parseInt(propIntS);
										//comprobacion de que este idto esta en el nodo class superior: parent -> no es necesario
										if (idtoParentS!=null/* && Auxiliar.hasIntValue(idtoParentS)*/)
											checkProp(conDb, hProp, prop, idtoParentS);
									}
								}
							}
						} else {
							idtoParentS = parent.getAttributeValue(QueryConstants.ID_TO);
							//se comprueba si es null porque puede que el padre sea Structure
							if (idtoParentS!=null/* && Auxiliar.hasIntValue(idtoParentS)*/) {
								String propIntS = elem.getAttributeValue(QueryConstants.PROP);
								Integer prop = null;
								if (propIntS!=null && Auxiliar.hasIntValue(propIntS)) {
									prop = Integer.parseInt(propIntS);
									checkProp(conDb, hProp, prop, idtoParentS);
								}
							}
						}
					}
					
					if (StringUtils.equals(elem.getAttributeValue(QueryConstants.REQUIRED), "TRUE")) {
						if (idStrOrig!=null && aRequired.get(idStrOrig)!=null)
							aRequired.put(idStrOrig, new Boolean(true));
						hasRequired = true;
					} else if (idStrOrig!=null && aRequired.get(idStrOrig)!=null) {
						hasRequired = true;
						elem.setAttribute(QueryConstants.REQUIRED_PRES, "TRUE");
					}
					if (StringUtils.equals(elem.getAttributeValue(QueryConstants.NULL), "TRUE")) {
						if (hasRequired)
							debug.addError("El nodo " + elemOri + " no puede ponerse a la vez con los atributos " + QueryConstants.REQUIRED + " y " + 
									QueryConstants.NULL + " a TRUE, ya que es contradictorio");
						else {
							if (idStrOrig!=null && aNull.get(idStrOrig)!=null)
								aNull.put(idStrOrig, new Boolean(true));
						}
					}
					if (elemName.equals(QueryConstants.ATTRIBUTE))  {
						hasAttrib = true;
						if (elem.getChild(QueryConstants.ATTRIBUTE_CASE)==null)
							getIdNodeCase(elem, idsAttributesWithCase);
						/*else {
							Iterator it = elem.getChildren(QueryConstants.ATTRIBUTE_CASE).iterator();
							while (it.hasNext()) {
								getIdNodeCase((Element)it.next(), idsAttributesWithCase);
							}
						}*/
						
						if (StringUtils.equals(elem.getAttributeValue(QueryConstants.NULL), "TRUE")) {
							if (nodesNull!=null)
								nodesNull.add(elem);
							else
								debug.addError("El nodo " + elemOri + " es incorrecto porque solo es util usar un nodo " + QueryConstants.ATTRIBUTE
										+ " de valor NULL si se está bajo un nodo " + QueryConstants.UNION);
						}
						if (isReport)
							parserName(elem, elemOri);
					} else if (elemName.equals(QueryConstants.WHERE) || elemName.equals(QueryConstants.CASE)) {
						parserWhereCaseStructure(elem, elemOri);
						if (isValueRdn && isReport) {
							String value = elem.getAttributeValue(QueryConstants.VALUE)==null?elem.getText():elem.getAttributeValue(QueryConstants.VALUE);
							if (value!=null && !value.equals("(VALUE)")) //cuando es parametro, QueryXML devuelve el ido
								elem.setAttribute(QueryConstants.IS_RDN, "TRUE");
						}
						//if (isValueRdn)
						//	parserEnumeratedStructure(conDb, elem, elemOri);
					}
				}
			}
		}
		addIdClass(elem, idStrOrig, elemOri);
		int classCount = 0;
		Iterator it = elem.getChildren().iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			if (elemName.equals(QueryConstants.CLASS) || elemName.equals(QueryConstants.XOR)) {
				String childName = child.getName();
				if (childName.equals(QueryConstants.CLASS) || childName.equals(QueryConstants.XOR) || 
						childName.equals(QueryConstants.ATTRIBUTE) || childName.equals(QueryConstants.WHERE) 
						|| childName.equals(QueryConstants.CASE)) {
					HasChildren hasChildrenTemp = parserIDsStructure(conDb, child, idViews, idsAttributesWithCase, nodesNull, aRequired, 
							aNull, nameParams, idStructure);
					hasAttrib = hasChildrenTemp.hasAttrib() || hasAttrib;
					hasRequired = hasChildrenTemp.hasRequired() || hasRequired;
					if (childName.equals(QueryConstants.CLASS) || childName.equals(QueryConstants.XOR))
						classCount++;
				} else
					debug.addWarning("El nodo " + jdomParser.returnNodeXML(child) + " no es correcto porque " +
							"bajo un nodo " + elemName + " no puede haber un nodo " + childName + ". este no sera parseado");
			} else if (elemName.equals(QueryConstants.ATTRIBUTE)) {
				String childName = child.getName();
				if (!childName.equals(QueryConstants.ATTRIBUTE_CASE))
					debug.addWarning("El nodo " + jdomParser.returnNodeXML(child) + " no es correcto porque " +
							"bajo un nodo " + elemName + " no puede haber un nodo " + childName + ". este no sera parseado");
				break;
			} else {
				debug.addWarning("El nodo " + elemOri + " no puede tener nodos hijo. �stos no seran parseados");
				break;
			}
		}
		if (elemName.equals(QueryConstants.CLASS)) {
			if (hasRequired) {
				//elem.setAttribute(QueryConstants.INNER, "TRUE");
				if (StringUtils.equals(elem.getAttributeValue(QueryConstants.NULL), "TRUE"))
					debug.addError("El nodo " + elemOri + " no puede ponerse a la vez con los atributos " + QueryConstants.REQUIRED + " y " + 
							QueryConstants.NULL + " a TRUE, ya que es contradictorio");
			}
			if (StringUtils.equals(elem.getAttributeValue(QueryConstants.NULL), "TRUE")) {
				if (elem.hasChildren())
					//error por si se trata de un report en el que se haya hecho un subreport por estos nodos que ya no se estan tratando
					debug.addError("Los nodos hijos del nodo " + elemOri + " seran ignorados al obtener el resultado de la consulta (no seran " +
							"recorridos ni mostrados aunque se indique en " + QueryConstants.SELECT + ") ya que tiene el atributo " + 
							QueryConstants.NULL + " a TRUE");
			}
		}
		if (elemName.equals(QueryConstants.XOR) && classCount<=1)
			debug.addWarning("El nodo " + elemOri + " al ser un nodo " + QueryConstants.XOR + " debe tener al menos 2 nodos hijo " + QueryConstants.CLASS);
		
		hasChildren.setAttrib(hasAttrib);
		hasChildren.setRequired(hasRequired);
		return hasChildren;
	}
	
	private void checkProp(ConnectionDB conDb, HashMap<Integer,String> hProp, int prop, String idtos) throws SQLException, NamingException {
		Statement st = null;
		ResultSet rs = null;
		HashSet<Integer> dominio = new HashSet<Integer>();
		
		String sql = "SELECT IDTO FROM instances WHERE PROPERTY=" + prop + " AND (OP='OR' OR OP='AND' OR OP='ONEOF')";
		//System.out.println("SQL_CHECKPROP " + sql);
		try {
			st = conDb.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				//obtengo los especializados del dominio
				int idtoInstance = rs.getInt(1);
				if (!rs.wasNull())
					putSpecialized(conDb, idtoInstance, dominio);
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			if (conDb!=null)
				fcdb.close(conDb);
		}
		boolean contieneUna = false;
		String[] aIdtos = idtos.split(",");
		for (int i=0;i<aIdtos.length;i++) {
			Integer idto = Integer.parseInt(aIdtos[i]);
			if (dominio.contains(idto))
				contieneUna = true;
		}
		
		//if (!dominio.contains(idto))
		if (!contieneUna) {
			String propIni = hProp.get(prop);
			debug.addError("La property " + propIni + " no está declarada para la/s clase/s " + getClassName(conDb, idtos) + 
					", no está expresada en el modelo");
		}
	}
	
	private void parserDateTimeBoolean(Element elem, String elemOri, String etiq) throws ParseException {
		String tmRule = elem.getAttributeValue(QueryConstants.ID_TM_RULEENGINE);
		if (tmRule!=null) {
			Integer tmRuleInt = Integer.parseInt(tmRule);
			if (tmRuleInt==Constants.IDTO_UNIT || 
					tmRuleInt==Constants.IDTO_INT || 
					tmRuleInt==Constants.IDTO_DOUBLE || 
					tmRuleInt==Constants.IDTO_BOOLEAN || 
					tmRuleInt==Constants.IDTO_TIME || 
					tmRuleInt==Constants.IDTO_DATETIME || 
					tmRuleInt==Constants.IDTO_DATE) {
				String value = elem.getAttributeValue(etiq);
				boolean text = false;
				if (value==null && StringUtils.equals(etiq, QueryConstants.VALUE) && elem.getText().length()>0) {
					value = elem.getText();
					text = true;
				}

				if (value!=null && !Auxiliar.hasFloatValue(value) && !value.contains("(VALUE)")) {
					String op = elem.getAttributeValue(QueryConstants.OP);
					if (tmRuleInt==Constants.IDTO_DATE || tmRuleInt==Constants.IDTO_DATETIME || tmRuleInt==Constants.IDTO_TIME) {
						String pattern = QueryConstants.getPattern(tmRuleInt);
						Long valueP = QueryConstants.dateToSeconds(pattern, value);
						if (valueP!=null) {
							if (text)
								elem.setText(String.valueOf(valueP));
							else
								elem.setAttribute(etiq, String.valueOf(valueP));
						} else
							debug.addError("El atributo " + etiq + " en el nodo " + elemOri + " no contiene una fecha en " +
									"el formato correcto " + pattern);
					} else if (tmRuleInt==Constants.IDTO_BOOLEAN) {
						if (StringUtils.lowerCase(value).equals("true")) {
							if (text)
								elem.setText("1");
							else
								elem.setAttribute(etiq,"1");
						} else if (StringUtils.lowerCase(value).equals("false")) {
							if (text)
								elem.setText("0");
							else
								elem.setAttribute(etiq,"0");
						} else
							debug.addError("El atributo " + etiq + " o el contenido del nodo " + elemOri + " debe ser de tipo Float o Boolean, ya " +
									"que se trata de una DataProperty de tipo Unit, Int, Double, Boolean sin comentario, Time, DateTime o Date");
					} else if (!op.equals(QueryConstants.REG_EXPR) && !op.equals(QueryConstants.NOT_REG_EXPR))
						debug.addError("En el nodo " + elemOri + " el valor introducido en " + etiq + " no es correcto, ya que " +
								"no es de tipo Float");
				}
			}
		}
	}
	
	private void parserWhereCaseStructure(Element elem, String elemOri) throws ParseException {
		String prop = elem.getAttributeValue(QueryConstants.PROP);
		if (prop==null)
			debug.addError("El nodo " + elemOri + " no es correcto. Debe llevar un atributo " + QueryConstants.PROP);
		String op = elem.getAttributeValue(QueryConstants.OP);
		//coger idtmruleengine y ver de q tipo es
		//si es fecha o boolean y no es un entero -> parsear -> funcion parserDateTimeBoolean
		if (op!=null) {
			if (op.equals(QueryConstants.BETWEEN)) {
				String valMin = elem.getAttributeValue(QueryConstants.VAL_MIN);
				String valMax = elem.getAttributeValue(QueryConstants.VAL_MAX);
				if (valMin==null || valMax==null)
					debug.addWarning("El nodo " + elemOri + " debe tener un atributo " + 
							QueryConstants.VAL_MIN + " y un atributo " + QueryConstants.VAL_MAX + 
							" para que se imponga la condición");
				parserDateTimeBoolean(elem, elemOri, QueryConstants.VAL_MIN);
				parserDateTimeBoolean(elem, elemOri, QueryConstants.VAL_MAX);
			} else {
				if (!(op.equals(QueryConstants.DISTINTO) || op.equals(QueryConstants.DISTINTO_VALIDO) || 
						op.equals(QueryConstants.IGUAL) || op.equals(QueryConstants.LIKE) ||
						op.equals(QueryConstants.CONTAINS) || op.equals(QueryConstants.MENOR) ||
						op.equals(QueryConstants.MENOR_IGUAL) || op.equals(QueryConstants.MAYOR) ||
						op.equals(QueryConstants.MAYOR_IGUAL) || op.equals(QueryConstants.REG_EXPR) || 
						op.equals(QueryConstants.NOT_REG_EXPR)))
					debug.addError("La operación introducida en el atributo " + QueryConstants.OP + 
							" del nodo " + elemOri + " no es valida. Debe ser una de las siguientes: " + 
							QueryConstants.DISTINTO + "," + QueryConstants.DISTINTO_VALIDO + "," + QueryConstants.IGUAL + "," + 
							QueryConstants.LIKE + "," + QueryConstants.CONTAINS + "," + QueryConstants.MENOR + "," + 
							QueryConstants.MENOR_IGUAL + "," + QueryConstants.MAYOR + "," + QueryConstants.MAYOR_IGUAL + "," + 
							QueryConstants.REG_EXPR + " o " + QueryConstants.NOT_REG_EXPR);
				String val = elem.getAttributeValue(QueryConstants.VALUE) == null ? elem.getText()
						: elem.getAttributeValue(QueryConstants.VALUE);
				if (val==null)
					debug.addWarning("El nodo " + elemOri + " debe tener un atributo " + 
							QueryConstants.VALUE + " o un contenido en el nodo para que se imponga la condición");
				parserDateTimeBoolean(elem, elemOri, QueryConstants.VALUE);
			}
		} else
			debug.addWarning("El nodo " + elemOri + " debe tener un atributo " + QueryConstants.OP + 
					" para que se imponga la condición");
	}
	
	/*private void parserEnumeratedStructure(ConnectionDB conDb, Element elem, String elemOri) throws SQLException, NamingException {
		if (elem.getAttributeValue(QueryConstants.OP).equals(QueryConstants.LIKE))
			elem.setAttribute(QueryConstants.OP,QueryConstants.IGUAL);
		
		//TODO en QueryXML es posible generar en VALUE varios valores numeros separados por ';'
		//aqui solo se va a permitir un valor de tipo String, ya que split por ';' puede ocasionar errores
		String val = elem.getAttributeValue(QueryConstants.VALUE) == null ? elem.getText()
				: elem.getAttributeValue(QueryConstants.VALUE);
		//System.out.println(val);
		Statement st = null;
		ResultSet rs = null;
		try {
			//String sql = "SELECT IDO FROM instances WHERE VALUE='" + val + "'";
			//antes buscaba en O_Reg_Instancias
			String sql = "SELECT ID_O FROM o_datos_atrib WHERE VAL_TEXTO='" + val + "' AND PROPERTY=" + Constants.IdPROP_RDN;
			st = conDb.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				Integer ido = rs.getInt(1);
				if (!rs.wasNull()) {
					elem.setText(null);
					elem.setAttribute(QueryConstants.VALUE, String.valueOf(ido));
				} else 
					debug.addError("El atributo " + QueryConstants.VALUE + " del nodo " + elemOri + 
							" no se encuentra en el modelo");
			} //else 
				//debug.addError("El atributo " + QueryConstants.VALUE + " del nodo " + elemOri + 
				//" no se encuentra en el modelo");
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			if (conDb!=null)
				fcdb.close(conDb);
		}
	}*/

	private boolean parserPropIdTmStructure(ConnectionDB conDb, Element elem, String elemOri, String idStr, Element elemProp, 
			String elemPropOri, HashMap<Integer,String> hProp) throws JDOMException, NamingException, SQLException, DataErrorException {
		boolean failIdTm = false;
		String propS = elemProp.getAttributeValue(QueryConstants.PROP);
		//System.out.println("idStr " + idStr);
		if (propS!=null) {
			Statement st = null;
			ResultSet rs = null;
			//System.out.println("propS " + propS);
			try {
				if (!Auxiliar.hasIntValue(propS)) {
					String sql = "SELECT PROP, CAT, VALUECLS FROM properties WHERE NAME='" + propS + "'";
					st = conDb.getBusinessConn().createStatement();
					rs = st.executeQuery(sql);
					if (rs.next()) {
						Integer prop = rs.getInt(1);
						hProp.put(prop, propS);
						elemProp.setAttribute(QueryConstants.PROP,String.valueOf(prop));
						elemProp.setAttribute(QueryConstants.NAME_PROP,propS);
//						if (StringUtils.equals(elem.getAttributeValue(QueryConstants.NULL), "TRUE") && elem.getName().equals(QueryConstants.ATTRIBUTE)) {
//							if (idStr!=null)
//								ids.setTm(idStr.replaceAll(" ", ""), elem.getName(), QueryConstants.TM_NULL);
//						} else {
//							if (StringUtils.equals(elem.getAttributeValue(QueryConstants.NULL), "TRUE"))
//								debug.addWarning("El atributo " + QueryConstants.NULL + " no es correcto en nodos " + elem.getName() + 
//										". Este atributo sera ignorado en el nodo " + elemOri);
							if (!elem.getName().equals(QueryConstants.CLASS)) {
								String cat = rs.getString(2);
								if (cat!=null) {
									Category category = new Category(Integer.parseInt(cat));
									if (category.isObjectProperty()) {
										//si no es una OProperty se trata de un enumerado
										if (elem.getAttributeValue(QueryConstants.ID_TM)!=null) {
											if (!elem.getAttributeValue(QueryConstants.ID_TM).equals(String.valueOf(QueryConstants.TM_VALUE_RDN)))
												failIdTm = true;
										} else {
											//comprobar aqui que la cardinalidad maxima sea 1, sino mostrar error
											if (elem.getParent().getAttributeValue(QueryConstants.ID_TO)==null)
												debug.addError("No es posible comprobar la cardinalidad máxima para el nodo " + elemOri + " porque su nodo padre no tiene informacion de la clase");
											else {
												String idtos = elem.getParent().getAttributeValue(QueryConstants.ID_TO);
												String[] idtosSpl = idtos.split(",");
												for (int i=0;i<idtosSpl.length;i++) {
													Integer idto = Integer.parseInt(idtosSpl[i]);
													if (!isCardMaxima1(conDb, idto, prop)) {
														debug.addError("Para usar un nodo " + QueryConstants.ATTRIBUTE + ", " + QueryConstants.WHERE + " o " + QueryConstants.CASE + 
																" como ObjectProperty, su property para la clase del nodo superior debe tener cardinalidad máxima 1. " +
																"Esto no se cumple en el nodo " + elemOri);
														break;
													}
												}
											}
											elem.setAttribute(QueryConstants.ID_TM,String.valueOf(QueryConstants.TM_VALUE_RDN));
											//elem.setAttribute(QueryConstants.ID_TM_RULEENGINE,String.valueOf(Constants.IDTO_ENUMERATED));
											if (idStr!=null)
												ids.setTm(idStr.replaceAll(" ", ""), elem.getName(), QueryConstants.TM_VALUE_RDN);
												//ids.setTm(idStr.replaceAll(" ", ""), elem.getName(), Constants.IDTO_ENUMERATED);
										}
									} else if (category.isDataProperty()) {
										String tm = rs.getString(3);
										failIdTm = parserIdTmStructure(elem, elemOri, tm, idStr);
									}
								} else
									debug.addError("El atributo " + QueryConstants.PROP + " y el tipo de datos del nodo " + 
											elemPropOri + " no se pueden obtener del modelo");
//							}
						}
							
						//codigo temporal
						String sqlTmp = "SELECT PROPERTY FROM instances WHERE PROPERTY=" + prop;
						st = conDb.getBusinessConn().createStatement();
						rs = st.executeQuery(sqlTmp);
						if (!rs.next())
							debug.addError("El atributo " + QueryConstants.PROP + " del nodo " + elemPropOri + " no se encuentra en el modelo");
						//

					} else
						debug.addError("El atributo " + QueryConstants.PROP + " del nodo " + elemPropOri + " no se encuentra en el modelo");
				} else {
//					if (StringUtils.equals(elem.getAttributeValue(QueryConstants.NULL), "TRUE") && elem.getName().equals(QueryConstants.ATTRIBUTE)) {
//						if (idStr!=null)
//							ids.setTm(idStr.replaceAll(" ", ""), elem.getName(), QueryConstants.TM_NULL);
//					} else {
//						if (StringUtils.equals(elem.getAttributeValue(QueryConstants.NULL), "TRUE"))
//							debug.addWarning("El atributo " + QueryConstants.NULL + " no es correcto en nodos " + elem.getName() + 
//									". Este atributo sera ignorado en el nodo " + elemOri);
						if (!elem.getName().equals(QueryConstants.CLASS)) {
							String sql = "SELECT VALUECLS FROM properties WHERE PROP=" + propS;
							st = conDb.getBusinessConn().createStatement();
							rs = st.executeQuery(sql);
							if (rs.next()) {
								String tm = rs.getString(1);
								failIdTm = parserIdTmStructure(elem, elemOri, tm, idStr);
							}
						}
//					}
				}
			} finally {
				if (rs!=null)
					rs.close();
				if (st!=null)
					st.close();
				if (conDb!=null)
					fcdb.close(conDb);
			}
		} else {
			if (elem.getName().equals(QueryConstants.ATTRIBUTE)) {
				String content = elemProp.getText();
				if (content!=null) {
					if (elemProp.getName().equals(QueryConstants.ATTRIBUTE_CASE)) {
						debug.addError("No se permiten nodos " + QueryConstants.ATTRIBUTE_CASE + " con contenido. " +
								"Esto ocurre con el nodo " + elemPropOri);
					} else {
						if (content.equals("NULL")) {
							if (isUnion) {
								if (idStr!=null)
									ids.setTm(idStr.replaceAll(" ", ""), elem.getName(), QueryConstants.TM_NULL);
								elem.setAttribute(QueryConstants.SHOW_NULL,"TRUE");
								elem.setAttribute(QueryConstants.ID_TM, String.valueOf(QueryConstants.TM_NULL));
		//						if (elem.getText()!=null)
		//							debug.addWarning("No se tendra en cuenta el contenido del nodo en " + elemOri);
							} else
								debug.addError("Un nodo con contenido NULL no tiene sentido en nodos que no están bajo un nodo " + 
										QueryConstants.UNION + ". Esto ocurre con el nodo " + elemPropOri);
						} else {
							elem.setAttribute(QueryConstants.ID_TM, String.valueOf(QueryConstants.TM_ATTRIBUTEVALUE));
							elem.setAttribute(QueryConstants.ID_TM_RULEENGINE,String.valueOf(Constants.IDTO_STRING));
							if (idStr!=null) {
								if (StringUtils.equals(elem.getAttributeValue(QueryConstants.TYPE), QueryConstants.DOUBLE))
									ids.setTm(idStr.replaceAll(" ", ""), elem.getName(), Constants.IDTO_DOUBLE);
								else if (StringUtils.equals(elem.getAttributeValue(QueryConstants.TYPE), QueryConstants.INT))
									ids.setTm(idStr.replaceAll(" ", ""), elem.getName(), Constants.IDTO_INT);
								else if (StringUtils.equals(elem.getAttributeValue(QueryConstants.TYPE), QueryConstants.DATE))
									ids.setTm(idStr.replaceAll(" ", ""), elem.getName(), Constants.IDTO_DATE);
								else if (StringUtils.equals(elem.getAttributeValue(QueryConstants.TYPE), QueryConstants.MEMO))
									ids.setTm(idStr.replaceAll(" ", ""), elem.getName(), Constants.IDTO_MEMO);
								else
									ids.setTm(idStr.replaceAll(" ", ""), elem.getName(), Constants.IDTO_STRING);
							}
						}
					}
				} else
					debug.addError("No se ha puesto el atributo " + QueryConstants.PROP + " en el nodo " + elemPropOri);
			} else if (!elem.getParent().getName().equals(QueryConstants.STRUCTURE))
				debug.addError("No se ha puesto el atributo " + QueryConstants.PROP + " en el nodo " + elemPropOri);
		}
		return failIdTm;
	}
	private boolean isCardMaxima1(ConnectionDB conDb, int idto, int prop) throws SQLException, NamingException {
		boolean qMax1 = false;
		Statement st = null;
		ResultSet rs = null;
		String sql = "SELECT MIN(QMAX) FROM instances WHERE OP='CAR' AND IDTO=" + idto + " AND PROPERTY=" + prop;
		//System.out.println("SQLCARDMAXIMA " + sql);
		try {
			st = conDb.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				//obtengo los especializados del dominio
				Integer qMax = rs.getInt(1);
				if (qMax!=null && qMax==1)
					qMax1 = true;
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			if (conDb!=null)
				fcdb.close(conDb);
		}
		return qMax1;
	}
	/*private boolean isCardMinimaMayorIgualQue1(ConnectionDB conDb, int idto, int prop) throws SQLException, NamingException {
		boolean qMin1 = false;
		Statement st = null;
		ResultSet rs = null;
		String sql = "SELECT MIN(QMIN) FROM instances WHERE OP='CAR' AND IDTO=" + idto + " AND PROPERTY=" + prop;
		System.out.println("SQLCARDMINIMA " + sql);
		try {
			st = conDb.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				//obtengo los especializados del dominio
				Integer qMin = rs.getInt(1);
				if (qMin!=null && qMin>=1)
					qMin1 = true;
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			if (conDb!=null)
				fcdb.close(conDb);
		}
		return qMin1;
	}*/
	private boolean parserIdTmStructure(Element elem, String elemOri, String tmRule, String idStr) throws JDOMException, DataErrorException {
		boolean failIdTm = false;
		if (tmRule!=null) {
			int tmIntRule = Integer.parseInt(tmRule);
			int tmInt = QueryConstants.toIdTmQuery(tmIntRule);
			String tm = (new Integer(tmInt)).toString();
			if (elem.getAttributeValue(QueryConstants.ID_TM)!=null) {
				if (!elem.getAttributeValue(QueryConstants.ID_TM).equals(tm))
					failIdTm = true;
			} else {
				elem.setAttribute(QueryConstants.ID_TM,tm);
				//mirar si hereda de unidades
				if (!Constants.isBasicType(tmIntRule))
					tmRule = String.valueOf(Constants.IDTO_DOUBLE);
				elem.setAttribute(QueryConstants.ID_TM_RULEENGINE,tmRule);
				//System.out.println("tmIntRule " + tmIntRule);
				if (idStr!=null)
					ids.setTm(idStr.replaceAll(" ", ""), elem.getName(), tmIntRule);
			}
		} else
			debug.addError("El tipo de datos del nodo " + elemOri + " no se puede obtener del modelo");
		return failIdTm;
	}
	
	private void parserIdOStructure(Element elem, String elemOri) {
		String idoS = elem.getAttributeValue(QueryConstants.ID_O);
		if (idoS!=null) {
			if (elem.getName().equals(QueryConstants.CLASS)) {
				boolean incorrecto = false;
				if (!idoS.equals("(VALUE)")) {
					ArrayList<String> aIdos = Auxiliar.stringToArray(idoS, ",");
					for (int i=0;i<aIdos.size();i++) {
						String ido = aIdos.get(i);
						if (!Auxiliar.hasIntValue(ido))
							incorrecto = true;
					}
				}
				if (incorrecto)
					debug.addError("El atributo " + QueryConstants.ID_O + " debe ser el parámetro (VALUE), un número o estar compuesto " +
							"por números separados por ',' si se quiere restringir a varios " + QueryConstants.ID_O + ". " +
							"Esto no se cumple en el nodo " + elemOri);
			} else
				debug.addWarning("El nodo " + elemOri + " no deberaa llevar el atributo " + QueryConstants.ID_O + ". " +
						"Debe ir en nodos " + QueryConstants.CLASS + ". este no sera considerado");
		}
	}
	private void parserNotIdOStructure(Element elem, String elemOri) {
		String notIdoS = elem.getAttributeValue(QueryConstants.NOT_ID_O);
		if (notIdoS!=null) {
			if (elem.getName().equals(QueryConstants.CLASS)) {
				boolean incorrecto = false;
				ArrayList<String> aNotIdos = null;
				if (!notIdoS.equals("(VALUE)")) {
					aNotIdos = Auxiliar.stringToArray(notIdoS, ",");
					for (int i=0;i<aNotIdos.size();i++) {
						if (!Auxiliar.hasIntValue(aNotIdos.get(i)))
							incorrecto = true;
					}
				}
				if (incorrecto) {
					debug.addError("El atributo " + QueryConstants.NOT_ID_O + " debe ser numero o estar compuesto por números " +
							"separados por ',' si se quiere restringir a varios " + QueryConstants.NOT_ID_O + ". Esto no se " +
							"cumple en el nodo " + elemOri);
				} else if (!notIdoS.equals("(VALUE)")){
					String idoS = elem.getAttributeValue(QueryConstants.ID_O);
					if (idoS!=null) {
						//si ido=not_ido -> error (entra dntro del 1er)
						//si not_ido contiene ido -> error
						//si ido contiene not_ido -> warning
						ArrayList<String> aIdos = Auxiliar.stringToArray(idoS, ",");
						if (aNotIdos.containsAll(aIdos)) {
							debug.addError("Se está indicando en el atributo " + QueryConstants.NOT_ID_O + " identificadores fijados en el " +
									"atributo " + QueryConstants.ID_O + " en el nodo " + elemOri + ". Esta contradicción no mostrar�a resultados");
						} else if (aIdos.containsAll(aNotIdos)) {
							debug.addWarning("Se está indicando en el atributo " + QueryConstants.ID_O + " identificadores fijados en el " +
									"atributo " + QueryConstants.NOT_ID_O + " en el nodo " + elemOri + " por lo que algunos identificadores indicados en "
									+ QueryConstants.ID_O + " seran excluidos");
						}
					}
				}
			} else
				debug.addWarning("El nodo " + elemOri + " no deberaa llevar el atributo " + QueryConstants.NOT_ID_O + ". " +
						"Debe ir en nodos " + QueryConstants.CLASS + ". este no sera considerado");
		}
	}
	private HashSet<Integer> parserIdToStructure(ConnectionDB conDb, Element elem, String elemOri) throws NamingException, SQLException {
		String idtoS = elem.getAttributeValue(QueryConstants.CLASS_NAME);
		HashSet<Integer> aIdtos = new HashSet<Integer>();
		if (idtoS!=null) {
			String[] sIdtos = idtoS.split(",");
			String idtos = "";
			int i=0;
			while (i<sIdtos.length) {
				if (idtos.length()>0)
					idtos += ",";
				String idtoAct = sIdtos[i];
				if (idtoAct.startsWith("[")) {
					idtoAct = idtoAct.substring(1, idtoAct.length()+1);
					i++;
					if (i<sIdtos.length) {
						String idtoTrozo = sIdtos[i];
						while (!idtoTrozo.endsWith("]")) {
							idtoAct += idtoTrozo;
							i++;
							if (i<sIdtos.length) {
								idtoTrozo = sIdtos[i];
							} else {
								debug.addError("El valor para el atributo " + QueryConstants.ID_TO + " en el nodo " + 
										elemOri + " no es correcto. Tiene corchetes no cerrados");
							}
						}
						idtoAct += idtoTrozo.substring(0, idtoAct.length());
					} else {
						debug.addError("El valor para el atributo " + QueryConstants.ID_TO + " en el nodo " + 
								elemOri + " no es correcto. Tiene corchetes no cerrados");
					}
				}
				idtos += "'" + idtoAct.replaceAll(" ", "") + "'";
				i++;
			}
			Statement st = null;
			ResultSet rs = null;
			HashSet<String> aNames = new HashSet<String>();
			String sql = "SELECT DISTINCT IDTO, NAME FROM instances WHERE NAME IN(" + idtos + ")";
			try {
				st = conDb.getBusinessConn().createStatement();
				rs = st.executeQuery(sql);
				while (rs.next()) {
					int idto = rs.getInt(1);
					String name = rs.getString(2);
					aIdtos.add(idto);
					aNames.add(name);
				} 
				ArrayList<String> aNamesIncorr = new ArrayList<String>();
				for (int j=0;j<sIdtos.length;j++) {
					String idtoAct = sIdtos[j];
					if (!aNames.contains(idtoAct))
						aNamesIncorr.add(idtoAct);
				}
				if (aNamesIncorr.size()>0)
					debug.addError("El/Los valor/es del atributo " + QueryConstants.CLASS_NAME + ": " + 
							Auxiliar.arrayToString(aNamesIncorr, ",") + " del nodo " + elemOri + " no se encuentra/n en el modelo");
				else {
					elem.setAttribute(QueryConstants.ID_TO,Auxiliar.hashSetIntegerToString(aIdtos, ","));
					elem.removeAttribute(QueryConstants.CLASS_NAME);
				}
			} finally {
				if (rs!=null)
					rs.close();
				if (st!=null)
					st.close();
				if (conDb!=null)
					fcdb.close(conDb);
			}
		} else
			//debug.addError("No se ha puesto el atributo " + QueryConstants.CLASS_NAME + " en el nodo " + elemOri);
			debug.addWarning("No se ha puesto el atributo " + QueryConstants.CLASS_NAME + " en el nodo " + elemOri + ". " +
					"La consulta devolvera datos sin restringir por la clase de este nodo");
		return aIdtos;
	}
	
	private Element getParentClass(Element root) {
		Element parent = null;
		String name = root.getName();
		if (name.equals(QueryConstants.XOR))
			parent = getParentClass(root.getParent());
		else
			parent = root;
		return parent;
	}
	private void parserReversedStructure(ConnectionDB conDb, Element elem, String propStr, Integer prop, String idtoStr, 
			HashSet<Integer> aIdtos) throws SQLException, NamingException {
//		if (prop!=null) {
			Element parent = getParentClass(elem.getParent());
			if (parent.getName().equals(QueryConstants.CLASS)) {
				Statement st = null;
				ResultSet rs = null;
				//select en instance por prop -> obtengo dominio y rango
				HashSet<Integer> dominio = new HashSet<Integer>();
				HashSet<Integer> rango = new HashSet<Integer>();
				
				String sql = "SELECT IDTO, VALUECLS FROM instances WHERE PROPERTY=" + prop + " AND (OP='OR' OR OP='AND' OR OP='ONEOF')";
				//System.out.println("SQLREVERSED " + sql);
				try {
					st = conDb.getBusinessConn().createStatement();
					rs = st.executeQuery(sql);
					while (rs.next()) {
						//obtengo los especializados del dominio
						int idto = rs.getInt(1);
						if (!rs.wasNull())
							putSpecialized(conDb, idto, dominio);
						//obtengo los especializados del rango
						int valueCls = rs.getInt(2);
						if (!rs.wasNull())
							putSpecialized(conDb, valueCls, rango);
					}
				} finally {
					if (rs!=null)
						rs.close();
					if (st!=null)
						st.close();
					if (conDb!=null)
						fcdb.close(conDb);
				}
				String idtoS = elem.getAttributeValue(QueryConstants.ID_TO);
				String idtoParentS = parent.getAttributeValue(QueryConstants.ID_TO);
				//System.out.println("idtoParent: " + idtoParentS + ", idtoChild: " + idtoS);
				//System.out.println("dominio: " + Auxiliar.hashSetIntegerToString(dominio, ", "));
				//System.out.println("rango: " + Auxiliar.hashSetIntegerToString(rango, ", "));
				//si esta idtoParent en dominio -> NoReversed1
				//si esta idtoParent en rango -> Reversed1
				//si esta idto en rango -> NoReversed2
				//si esta idto en dominio -> Reversed2
				
				//NoReversed si NoReversed1 y NoReversed2
				//Reversed si Reversed1 y Reversed2
				//si !NoReversed y !Reversed -> error
				
				if (idtoS!=null && idtoParentS!=null) {
					//puede que sean varios -> un rango multiple
					//split del actual, split del padre -> pasarlos a arrays
					ArrayList<Integer> aIdtosParent = Auxiliar.stringToArrayInteger(idtoParentS, ",");
					//int idtoParent = Integer.parseInt(idtoParentS);
					//int idtoChild = Integer.parseInt(idtoS);
					if (!(dominio.containsAll(aIdtosParent) && rango.containsAll(aIdtos))) {
						if (rango.containsAll(aIdtosParent) && dominio.containsAll(aIdtos))
							elem.setAttribute(QueryConstants.REVERSED,"TRUE");
						else //if (!(dominio.contains(idtoParent) && rango.contains(idtoChild)))
							debug.addWarning("La relación entre las clases " + idtoStr + " y " + getClassName(conDb, idtoParentS) + 
									" para la property " + propStr + " no es correcta, no está expresada en el modelo");
					}
				} else if (idtoParentS==null && elem.getAttributeValue(QueryConstants.CLASS_NAME)==null) {
					ArrayList<Integer> aIdtosParent = Auxiliar.stringToArrayInteger(idtoParentS, ",");
					//int idtoParent = Integer.parseInt(idtoParentS);
					if (!dominio.containsAll(aIdtosParent)) {
						if (rango.containsAll(aIdtosParent))
							elem.setAttribute(QueryConstants.REVERSED,"TRUE");
					} else
						debug.addWarning("La clase " + getClassName(conDb, idtoParentS) + " no tiene relación para la property " + propStr + ", " +
								"no está expresado en el modelo");
				} else if (idtoS==null && parent.getAttributeValue(QueryConstants.CLASS_NAME)==null) {
					//int idtoChild = Integer.parseInt(idtoS);
					if (!rango.containsAll(aIdtos)) {
						if (dominio.containsAll(aIdtos))
							elem.setAttribute(QueryConstants.REVERSED,"TRUE");
					} else
						debug.addWarning("La clase " + idtoStr + " no tiene relación para la property " + propStr + ", " +
								"no está expresado en el modelo");
				}
			}
//		}
	}
	private String getClassName(ConnectionDB conDb, String idtos) throws SQLException, NamingException {
		String sql = "select NAME from Clases where IDTO IN(" + idtos + ")";
		
		String claseName = "";
		Statement st = null;
		ResultSet rs = null;
		try {
			st = conDb.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				if (claseName.length()>0)
					claseName += ",";
				claseName += rs.getString(1);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (conDb!=null)
				fcdb.close(conDb);
		}
		return claseName;
	}
	/*private String getClassName(ConnectionDB conDb, int idto) throws SQLException, NamingException {
		String sql = "select NAME from Clases where IDTO=" + idto;
		
		String claseName = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			st = conDb.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next())
				claseName = rs.getString(1);
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (!fcdb.isStandAloneApp() && conDb!=null)
				conDb.close();
		}
		return claseName;
	}*/
	private void putSpecialized(ConnectionDB conDb, int idtoPadre, HashSet<Integer> aSpec) 
			throws SQLException, NamingException {
		aSpec.add(idtoPadre);
		String sql = "select ID_TO from T_Herencias where ID_TO_Padre=" + idtoPadre;
		
		Statement st = null;
		ResultSet rs = null;
		try {
			st = conDb.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				int idto = rs.getInt(1);
				aSpec.add(idto);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
		}
	}
}
