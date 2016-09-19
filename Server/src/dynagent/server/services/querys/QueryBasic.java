package dynagent.server.services.querys;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.jdomParser;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.IQueryInfo;
import dynagent.server.dbmap.IQueryInfoColumn;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.gestorsDB.GenerateSQL;

public class QueryBasic {
	
	private GenerateSQL gSQL;
	private DataBaseMap dataBaseMap;
	private IKnowledgeBaseInfo ik;
	//private int countRdn = -1;
	private Map<String, Select> hSelect;
	private Map<String, ConditionsWhere> hWhere;
	private Map<String, String> hCase;
	private HashMap<String,DataInfo> dataInfoXName;
	private int[] def;	//atributo para queryData
	private int[] defColSup;	//atributo para queryData
	private int countDef = 0;	//atributo para queryData
	private int countColumn = 1;
	//
		
	private class Select {
		private String field;
		private String alias;
		private String aliasIdto;
		private String idCase;
		private String idtoName;
		private Integer dataType;
		private String idNodeClassSup;
		
		public Select(String field, String alias, String aliasIdto, String idCase, String idtoName, Integer dataType, String idNodeClassSup) {
			this.field = field;
			this.alias = alias;
			this.aliasIdto = aliasIdto;
			this.idCase = idCase;
			this.idtoName = idtoName;
			this.dataType = dataType;
			this.idNodeClassSup = idNodeClassSup;
		}
		public String getField() {
			return field;
		}
		public String getAlias() {
			return alias;
		}
		public String getAliasIdto() {
			return aliasIdto;
		}
		public String getIdCase() {
			return idCase;
		}
		public String getIdtoName() {
			return idtoName;
		}
		public Integer getDataType() {
			return dataType;
		}
		public String getIdNodeClassSup() {
			return idNodeClassSup;
		}
		public String toString() {
			return "SELECT\nfield: " + field + ", aliasIdto: " + aliasIdto + ", alias: " + alias + ", idCase: " + idCase 
			+ ", idtoName :" + idtoName + ", dataType: " + dataType + ", idNodeClassSup: " + idNodeClassSup;
		}
	}
	
	public QueryBasic(IKnowledgeBaseInfo ik, GenerateSQL gSQL, DataBaseMap dataBaseMap) {
		this.ik = ik;
		this.gSQL = gSQL;
		this.dataBaseMap = dataBaseMap;
		this.hSelect = new HashMap<String, Select>();
		this.hWhere = new HashMap<String, ConditionsWhere>();
		this.hCase = new HashMap<String, String>();
		this.dataInfoXName = new HashMap<String, DataInfo>();
	}

	//metodos get necesarios.
	public int getCountDef() {
		return countDef;
	}
	public int[] getDef() {
		return def;
	}
	public int[] getDefColSup() {
		return defColSup;
	}
	public HashMap<String, DataInfo> getDataInfoXName() {
		return dataInfoXName;
	}

	/** metodo principal.*/
	public StructureQuery createQueryBasic(Element nodeStructure,boolean idosCompresed) throws NotFoundException, IncoherenceInMotorException, NoSuchColumnException, DataErrorException {
		//System.out.println("Inicio de la funcion createQueryBasic");
		StructureQuery sq = new StructureQuery();

		Element nodeClass = nodeStructure.getChild(QueryConstants.CLASS);
		if (nodeClass!=null) {
			setInnerAndIdtoOfFixedIdos(nodeClass);
			nodeClass(sq, nodeStructure, nodeClass,idosCompresed);
			
			Element nodePresentation = nodeStructure.getChild(QueryConstants.PRESENTATION);
			String selectIdo = "";
			String orderBy = null;
			String orderNCol = null;
			HashSet<Integer> hIdsFicticios = null;
			Element nodeViewRoot = null;
			if (nodePresentation!=null) {
				nodeViewRoot = nodePresentation.getChild(QueryConstants.VIEW);
				if (nodeViewRoot!=null) {
					String limitStr = nodeViewRoot.getAttributeValue(QueryConstants.LIMIT);
					if (limitStr!=null)
						sq.setLimit(Integer.parseInt(limitStr));
					
					selectIdo = nodeViewRoot.getAttributeValue(QueryConstants.SELECT_IDO);
					String idsFicticios = nodeViewRoot.getAttributeValue(QueryConstants.IDS_FICTICIOS);
					hIdsFicticios = Auxiliar.stringToHashSetInteger(idsFicticios, ",");
					orderBy = nodeViewRoot.getAttributeValue(QueryConstants.ORDERBY);
					if (orderBy==null)
						orderNCol = nodeViewRoot.getAttributeValue(QueryConstants.ORDERBY_NCOL);
				}
			}

			def = new int[selectIdo.length()*2];
			defColSup = new int[selectIdo.length()*2];
			buildSelectAll(sq, Auxiliar.stringToArray(selectIdo, ","), hIdsFicticios);
			buildExternalWhere(sq, nodeViewRoot);
			
			if (orderBy!=null) {
				StringBuffer order = sq.getOrder();
				subBuildOrderNCol(order, orderBy);
			} else if (orderNCol!=null) {
				StringBuffer order = sq.getOrder();
				subBuildOrder(order, orderNCol);
			}
		}
		//System.out.println("Fin de la funcion createQueryBasic");
		return sq;
	}
	
	/** metodo que construye la parte sql del order a partir de identificadores de nodo.*/
	private void subBuildOrder(StringBuffer orderExt, String order) {
		//System.out.println("Inicio de la funcion subBuildOrder");
		String[] orderSpl = order.split(",");
		for (int i=0;i<orderSpl.length;i++) {
			String nOrderSentidoStr = orderSpl[i];
			String[] idSentidoSpl = nOrderSentidoStr.split("#");
			String nOrderStr = idSentidoSpl[0];
//			if (Auxiliar.hasIntValue(nOrderStr)) {
				Select selectObject = hSelect.get(nOrderStr);
				if (selectObject!=null) {
					String etiq = gSQL.getCharacterBegin() + selectObject.getAlias() + gSQL.getCharacterEnd();
					if (orderExt.length()>0)
						orderExt.append(",");
					orderExt.append(etiq);
					if (idSentidoSpl.length>1) {
						String sentido = idSentidoSpl[1];
						if (Integer.parseInt(sentido)== QueryConstants.ORDER_ASC)
							orderExt.append(" ASC");
						else if (Integer.parseInt(sentido)== QueryConstants.ORDER_DESC)
							orderExt.append(" DESC");
					}
				/*} else {
					if (orderExt.length()>0)
						orderExt.append(",");
					orderExt.append(nOrd);
					if (idSentidoSpl.length>1) {
						String sentido = idSentidoSpl[1];
						if (Integer.parseInt(sentido)== QueryConstants.ORDER_ASC)
							orderExt.append(" ASC");
						else
							orderExt.append(" DESC");
					}*/
				}
//			}
		}
		//System.out.println("Fin de la funcion subBuildOrder");
	}

	/** metodo que construye la parte sql del order a partir de números de columna.*/
	private void subBuildOrderNCol(StringBuffer orderExt, String order) {
		//System.out.println("Inicio de la funcion subBuildOrderNCol");
		String[] orderSpl = order.split(",");
		for (int i=0;i<orderSpl.length;i++) {
			String nOrderSentidoStr = orderSpl[i];
			String[] idSentidoSpl = nOrderSentidoStr.split("#");
			String nOrderStr = idSentidoSpl[0];
			if (Auxiliar.hasIntValue(nOrderStr)) {
				int nOrd = Integer.parseInt(nOrderStr);
				if (orderExt.length()>0)
					orderExt.append(",");
				orderExt.append(nOrd);
				if (idSentidoSpl.length>1) {
					String sentido = idSentidoSpl[1];
					if (Integer.parseInt(sentido)== QueryConstants.ORDER_ASC)
						orderExt.append(" ASC");
					else if (Integer.parseInt(sentido)== QueryConstants.ORDER_DESC)
						orderExt.append(" DESC");
				}
			}
		}
		//System.out.println("Fin de la funcion subBuildOrderNCol");
	}

	/** metodo que construye la parte sql del select usando los mapas hSelect y hCase.*/
	private void buildSelectAll(StructureQuery sq, ArrayList<String> viewSelect, HashSet<Integer> hIdsFicticios) {
		//System.out.println("Inicio de la funcion buildSelectAll");
		StringBuffer select = sq.getSelect();
		HashMap<String,Integer> hColByNode = new HashMap<String, Integer>(); //dado un id de node devuelve la columna que ocupa
		
		System.out.println(viewSelect);
		Iterator it = viewSelect.iterator();
		while (it.hasNext()) {
			String id = (String)it.next();
			System.out.println("id actual " + id);
			Select selectObject = hSelect.get(id);
			String idto = selectObject.getIdtoName();
			boolean isIdo = idto!=null;
			String idCase = selectObject.getIdCase();
			String fieldSelect = selectObject.getField();
			String aliasSelect = selectObject.getAlias();
			String aliasIdtoSelect = selectObject.getAliasIdto();
			
			if (!(isIdo && hIdsFicticios.contains(id))) { //excluir idos ficticios
				Integer dataType = selectObject.getDataType();
				String idTopNode = selectObject.getIdNodeClassSup();
				int colSup = 0;
				if (!isIdo)
					colSup = hColByNode.get(idTopNode);
				if (select.length()>0)
					select.append(",");
				if (idCase==null) {
					select.append(fieldSelect);
					addCol(aliasSelect, dataType, colSup);
				} else {
					String caseSelect = hCase.get(idCase);
					select.append("case when " + caseSelect + " then " + fieldSelect + " else null end");
					addCol(aliasSelect, dataType, colSup);
				}
				select.append(" as " + gSQL.getCharacterBegin() + aliasSelect + gSQL.getCharacterEnd());
				if (isIdo) {
					hColByNode.put(id, countDef);
					//si se trata de un ido, hay que poner en select su idto
					select.append("," + idto);
					select.append(" as " + gSQL.getCharacterBegin() + aliasIdtoSelect + gSQL.getCharacterEnd());
					addCol(aliasIdtoSelect, dataType, colSup);
				}
			} else {
				addCol(aliasSelect, QueryConstants.TM_IDO_FICTICIO, 0);
				hColByNode.put(id, countDef);
				addCol(aliasIdtoSelect, QueryConstants.TM_IDO_FICTICIO, 0);
			}
		}
		//System.out.println("Fin de la funcion buildSelectAll");
	}
	
	/** Construye el whereExt uniendo las condiciones para los nodos REQUIRED junto con las obtenidas en la 
	 * funcion subBuildWhere, si existe el nodo OP, o en subBuildWhereOpt, si no existe.*/
	private void buildExternalWhere(StructureQuery sq, Element nodeViewRoot) {
		//System.out.println("Inicio de la funcion buildExternalWhere");
		StringBuffer where = sq.getWhere();
		Element nodoOp = null;
		if (nodeViewRoot!=null) {
			Element logicaWhere = nodeViewRoot.getChild(QueryConstants.LOGIC_WHERE);
			if (logicaWhere!=null)
				nodoOp = logicaWhere.getChild(QueryConstants.OP);
		}
		if (nodoOp!=null) {
			StringBuffer tmp = new StringBuffer("");
			subBuildWhere(nodeViewRoot, nodoOp, tmp);
			if (where.length()>0)
				where.append(" AND ");
			boolean parentesis = false;
			if (StringUtils.equals(nodoOp.getAttributeValue(QueryConstants.ID_OP),String.valueOf(QueryConstants.OP_OR))) {
				parentesis = true;
				where.append("(");
			}
			where.append(tmp);
			if (parentesis)
				where.append(")");
		} else
			subBuildWhereOpt(nodeViewRoot, where);
		
		//System.out.println("Fin de la funcion buildExternalWhere");
	}
	
	/** Construye el where exterior cuando no existe el nodo OP, une con AND los where de los ids de 
	 * nodos CONDITION que haya en hWhere.*/
	private void subBuildWhereOpt(Element nodoView, StringBuffer where) {
		//System.out.println("Inicio de la funcion subBuildWhereOpt");
		Iterator it = hWhere.keySet().iterator();
		while(it.hasNext()) {
			String id = (String)it.next();
			ConditionsWhere cWhere = hWhere.get(id);
			buildConditionSQL(cWhere, where, false);
		}
		//System.out.println("Fin de la funcion subBuildWhereOpt");
	}

	/** funcion recursiva que construye el where exterior utilizando el nodo OP, uniendo los where 
	 * de cada id del nodo CONDITION indicado con la operación establecida en el nodo OP superior.*/
	private void subBuildWhere(Element nodoView, Element nodoOp, StringBuffer whereHaving) {
		//System.out.println("Inicio de la funcion subBuildWhere");
		String operacion = nodoOp.getAttributeValue(QueryConstants.ID_OP);
		Iterator it = nodoOp.getChildren().iterator();
		while(it.hasNext()) {
			Element elem = (Element) it.next();
			if (elem.getName().equals(QueryConstants.CONDITION)) {
				String id = elem.getAttributeValue(QueryConstants.ID_CONDITION);
				boolean isnot = StringUtils.equals(elem.getAttributeValue(QueryConstants.NOT),"TRUE");
				ConditionsWhere cWhere = hWhere.get(id);
				StringBuffer conditionBf = new StringBuffer("");
				buildConditionSQL(cWhere, conditionBf, isnot);
				String whereTemp = conditionBf.toString();
				if (whereTemp.length()>0) {
					if (whereHaving.length()>0) {
						if (StringUtils.equals(operacion, String.valueOf(QueryConstants.OP_AND)))
							whereHaving.append(" AND " + whereTemp);
						else if (StringUtils.equals(operacion, String.valueOf(QueryConstants.OP_OR)))
							whereHaving.append(" OR " + whereTemp);
					} else
						whereHaving.append(whereTemp);
				}
			} else if (elem.getName().equals(QueryConstants.OP)) {
				StringBuffer whereTemp = new StringBuffer("");
				subBuildWhere(nodoView, elem, whereTemp);
				boolean isor = StringUtils.equals(elem.getAttributeValue(QueryConstants.ID_OP),String.valueOf(QueryConstants.OP_OR));
				
				if (StringUtils.equals(operacion, String.valueOf(QueryConstants.OP_AND))) {
					if (whereHaving.length()>0)
						whereHaving.append(" AND ");
					if (isor)
						whereHaving.append("(");
					whereHaving.append(whereTemp);
					if (isor)
						whereHaving.append(")");
				} else if (StringUtils.equals(operacion, String.valueOf(QueryConstants.OP_OR))) {
					if (whereHaving.length()>0)
						whereHaving.append(" OR ");
					whereHaving.append(whereTemp);
				}
			}
		}
		//System.out.println("Fin de la funcion subBuildWhere");
	}
	
	private Integer getIdtoOfFixedIdos(Element nodeClass) {
		boolean continuar = true;
		Integer idto = null;
		String idos = nodeClass.getAttributeValue(QueryConstants.ID_O);
		if (idos!=null) {
			HashSet<Integer> hInt = Auxiliar.stringToHashSetInteger(idos, ",");
			Iterator<Integer> it = hInt.iterator();
			while (it.hasNext() && continuar) {
				int ido = it.next();
				if (ido<0)
					continuar = false;
				else if (idto==null)
					idto = QueryConstants.getIdto(ido);
				else if (!idto.equals(QueryConstants.getIdto(ido))) {
					continuar = false;
				}
			}
		}
		if (!continuar)
			idto = null;
		return idto;
	}
	
	/** metodo llamado cuando NO se trata del primer nodo CLASS del XML, es decir, ya se ha empezado a construir la consulta.*/
	private void nodeClassExistsLast(StructureQuery sq, Element nodeClass,boolean idosCompresed) throws NotFoundException, IncoherenceInMotorException, NoSuchColumnException, DataErrorException {
		//System.out.println("Inicio de la funcion nodeClassExistsLast");
		StringBuffer from = sq.getFrom();
		boolean reversed = StringUtils.equals(nodeClass.getAttributeValue(QueryConstants.REVERSED),"TRUE");
		
		//datos del nodo CLASS actual
		int idto = Integer.parseInt(nodeClass.getAttributeValue(QueryConstants.ID_TO));
		boolean isView = true;
		IQueryInfo tableView = dataBaseMap.getView(idto);
		if (tableView==null) {
			tableView = dataBaseMap.getTable(idto);
			isView = false;
		}
		String tableName = gSQL.getCharacterBegin() + tableView.getName() + gSQL.getCharacterEnd();
		
		String ident = nodeClass.getAttributeValue(QueryConstants.NAME)==null?"tb":nodeClass.getAttributeValue(QueryConstants.NAME).replaceAll(" ", "_");
		String aliasTableNameNoCharacter = ident + nodeClass.getAttributeValue(QueryConstants.ID);
		String aliasTableName = gSQL.getCharacterBegin() + aliasTableNameNoCharacter + gSQL.getCharacterEnd();
		
		//datos del nodo CLASS superior
		Element parent = nodeClass.getParent();
		int idtoParent = Integer.parseInt(parent.getAttributeValue(QueryConstants.ID_TO));
		String identParent = parent.getAttributeValue(QueryConstants.NAME)==null?"tb":parent.getAttributeValue(QueryConstants.NAME).replaceAll(" ", "_");
		String aliasTableNameParent = gSQL.getCharacterBegin() + identParent + parent.getAttributeValue(QueryConstants.ID) + gSQL.getCharacterEnd();
		
		boolean isViewParent = true;
		IQueryInfo tableViewParent = dataBaseMap.getView(idtoParent);
		if (tableViewParent==null) {
			tableViewParent = dataBaseMap.getTable(idtoParent);
			isViewParent = false;
		}
		
		//antes de hacer nada ver como estan unidas las tablas/vistas por si tengo que hacer join con una tabla/vista asociacion
		int prop = Integer.parseInt(nodeClass.getAttributeValue(QueryConstants.PROP));
		//ver en que tablas está esta property, primero ver si está externalizada
		System.out.println("idtoParent " + idtoParent + ", prop " + prop + ", idto " + idto);
		
		System.out.println("reversed " + reversed);
		boolean isInverseStructural = false;
		boolean isOP = false;
		if (!reversed)
			isOP = tableViewParent.isObjectProperty(prop);
		else
			isOP = tableView.isObjectProperty(prop);
		boolean isExternalizedProperty = false;
		Integer propInverse = null;
		System.out.println("isOP " + isOP);
		if (!isOP) {
			//ver si tiene inversa y esta es estructural
			propInverse = ik.getPropertyInverse(prop);
			if (propInverse!=null && ik.getCategory(propInverse).isStructural()) {
				System.out.println("ENTRA en property inversa structural");
				prop = propInverse;
				reversed = !reversed;
				isInverseStructural = true;
			}
		}
		if (!reversed) {
			if (!isViewParent && isView) //la relacion tabla-vista siempre esta externalizada
				isExternalizedProperty = true;
			else {
				System.out.println("tableViewParent " + tableViewParent.getName() + ", prop " + prop);
				isExternalizedProperty = tableViewParent.isExternalizedProperty(prop);
			}
		} else {
			if (!isView && isViewParent) //la relacion tabla-vista siempre esta externalizada
				isExternalizedProperty = true;
			else {
				System.out.println("tableView " + tableView.getName() + ", prop " + prop);
				isExternalizedProperty = tableView.isExternalizedProperty(prop);
			}
		}
		System.out.println("isExternalizedProperty " + isExternalizedProperty);
		
		if (!isExternalizedProperty) {
			String aliasTableWithColumn = aliasTableNameParent;
			String aliasTableWithoutColumn = aliasTableName;
				
			boolean isViewWithColumn = isViewParent;
			boolean isViewWithoutColumn = isView;
			int idtoWithColumn = idtoParent;
			int idtoWithoutColumn = idto;
			
			if (isInverseStructural) {
				String tmp = aliasTableWithColumn;
				aliasTableWithColumn = aliasTableWithoutColumn;
				aliasTableWithoutColumn = tmp;
			}
			System.out.println("reversed " + reversed);
			IQueryInfoColumn[] columns = null;
			if (!reversed) {
				columns = tableViewParent.getObjectPropertyColumn(prop, idto);
				if (columns==null || columns[0]==null)
					throw new DataErrorException("No está definido el enlace entre " + tableViewParent.getName() + "(" + idtoParent + ") y " + tableView.getName() + "(" + idto + ") a traves de " + ik.getPropertyName(prop) + "(" + prop + ")");
			} else {
				columns = tableView.getObjectPropertyColumn(prop, idtoParent);
				if (columns==null || columns[0]==null)
					throw new DataErrorException("No está definido el enlace entre " + tableView.getName() + "(" + idto + ") y " + tableViewParent.getName() + "(" + idtoParent + ") a traves de " + ik.getPropertyName(prop) + "(" + prop + ")");
			}
			String columnName = columns[0].getColumnName();
			String columnNameIdto = null;
			IQueryInfoColumn columnIdto = columns[1];
			if (columnIdto!=null)
				columnNameIdto = columnIdto.getColumnName();
			if (columnName == null) {
				if (!reversed)
					System.err.println("No se ha encontrado la columna del rango en la tabla/vista " + tableViewParent);
				else
					System.err.println("No se ha encontrado la columna del rango en la tabla/vista " + tableView);
			} else {
				// columnName tiene el nombre de la columna que busco y esa columna tiene el tableId del objeto en la otra tabla/vista.
				buildDirectJoin(from, sq.getWhere(), nodeClass, isView, tableName, aliasTableName, aliasTableWithColumn, aliasTableWithoutColumn, columnName, columnNameIdto, 
						idtoWithColumn, idtoWithoutColumn, isViewWithColumn, isViewWithoutColumn,idosCompresed);
			}
		} else {
			// La property está en otra tabla/vista, puede ser asociacion o puede ser la tabla/vista a la que apuntas directamente
			Set<Integer> idsTableParent = null;
			if (!reversed)
				idsTableParent = tableViewParent.getExternalizedPropertyLocations(prop);
			else
				idsTableParent = tableView.getExternalizedPropertyLocations(prop);
			
			//devuelve un conjunto debido a los rangos
			//itero y obtengo una de ellas para decidir si el idto esta en rango o en tabla/vista asociacion
			Integer propertyLocation = null;
			Iterator<Integer> it = idsTableParent.iterator();
			while (it.hasNext() && propertyLocation == null) {
				Integer propertyLocationTmp = (Integer)it.next();
				IQueryInfo iqi = dataBaseMap.getView(propertyLocationTmp);
				if (iqi == null){
					iqi = dataBaseMap.getTable(propertyLocationTmp);
				} else if (ik.getCategory(prop).isStructural()) {
					IQueryInfo iqi2 = dataBaseMap.getTable(propertyLocationTmp);
					if (iqi2!=null) {
						//si es estructural y existe la tabla y la vista xa la misma clase -> 
						//la vista no tiene la columna y en querys yo siempre usare la vista antes que la tabla xq busco x especializados, 
						//continuar xq hay una tabla asociacion que los une
						continue;
					}
				}
				if (iqi.isAssociation()){
					// Si es asociacion, tenemos que comprobar que haya una
					// columna que contenga informacion de la propiedad que está
					// apuntando al rango deseado.
					IQueryInfoColumn [] columns = null;
					if (! reversed){
						columns = iqi.getObjectPropertyColumn(prop, idto);
					}else{
						columns = iqi.getObjectPropertyColumn(prop, idtoParent);
					}
					if (columns != null){
						propertyLocation = propertyLocationTmp;
					}
				} else {
					// Si no es una asociacion, significa que la propiedad es
					// estructural y su informacion está externalizada a la
					// clase del rango de la propiedad. Nos interesa que el idto
					// sea el del rango deseado.
					if (! reversed && propertyLocationTmp.equals(idto)){
						propertyLocation = propertyLocationTmp;
					}else if (reversed && propertyLocationTmp.equals(idtoParent)){
						propertyLocation = propertyLocationTmp;
					}
				}
			}
			
			if (propertyLocation==null && !ik.getCategory(prop).isStructural()) {
				if (!isInverseStructural)
					propInverse = ik.getPropertyInverse(prop);
				//si no es estructural, pero su inversa si, la que estara en base de datos sera su inversa
				if (propInverse!=null && ik.getCategory(propInverse).isStructural()) {
					if (!isInverseStructural)
						prop = propInverse;
					if (!reversed)
						idsTableParent = tableView.getExternalizedPropertyLocations(prop);
					else
						idsTableParent = tableViewParent.getExternalizedPropertyLocations(prop);
					it = idsTableParent.iterator();
					if (it.hasNext())
						propertyLocation = (Integer)it.next();
				}
			}
			
			System.out.println("propertyLocation " + propertyLocation);
			IQueryInfo referencedTableView = dataBaseMap.getView(propertyLocation);
			if (referencedTableView==null) {
				referencedTableView = dataBaseMap.getTable(propertyLocation);
			}
			if (referencedTableView.isAssociation()) {
				System.out.println("hay asociacion");
				//tengo que obtener los nombres de las columnas de la tabla/vista asociacion
				//ColumnNames solo tiene un nombre de columna que es el nombre de la columna que apunta al objeto del rango, tienes que fijar que la otra columna que tenga dominio tenga el tableId del objeto del dominio.
				//List<String> columnNames = referencedTableView.getColumnNamesContainingProperty(prop);
				//String rangeColumn = gSQL.getCharacterBegin() + columnNames.get(0) + gSQL.getCharacterEnd();
				IQueryInfoColumn[] columnsDomain = null;
				if (!reversed) {
					columnsDomain = referencedTableView.getObjectPropertyColumn(IQueryInfo.ID_DOMAIN, idtoParent);
					if (columnsDomain==null || columnsDomain[0]==null)
						throw new DataErrorException("No está definido el enlace entre " + tableViewParent.getName() + "(" + idtoParent + ") y " + referencedTableView.getName() + " a traves de IQueryInfo.ID_DOMAIN");
				} else {
					columnsDomain = referencedTableView.getObjectPropertyColumn(IQueryInfo.ID_DOMAIN, idto);
					if (columnsDomain==null || columnsDomain[0]==null)
						throw new DataErrorException("No está definido el enlace entre " + tableView.getName() + "(" + idto + ") y  " + referencedTableView.getName() + " a traves de IQueryInfo.ID_DOMAIN");
				}
				String domainColumn = columnsDomain[0].getColumnName();
				String domainColumnIdto = null;
				IQueryInfoColumn columnIdto = columnsDomain[1];
				if (columnIdto!=null)
					domainColumnIdto = columnIdto.getColumnName();
				if (domainColumn == null) {
					System.err.println("No se ha encontrado la columna del dominio en la tabla/vista " + referencedTableView);
				} else {
					IQueryInfoColumn[] columnsRange = null;
					if (!reversed) {
						columnsRange = referencedTableView.getObjectPropertyColumn(prop, idto);
						if (columnsRange==null || columnsRange[0]==null)
							throw new DataErrorException("No está definido el enlace entre " + referencedTableView.getName() + " y " + tableView.getName() + " a traves de " + ik.getPropertyName(prop) + "(" + prop + ")");
					} else {
						columnsRange = referencedTableView.getObjectPropertyColumn(prop, idtoParent);
						if (columnsRange==null || columnsRange[0]==null)
							throw new DataErrorException("No está definido el enlace entre " + referencedTableView.getName() + " y " + tableViewParent.getName() + " a traves de " + ik.getPropertyName(prop) + "(" + prop + ")");
					}
					String rangeColumn = columnsRange[0].getColumnName();
					String rangeColumnIdto = null;
					IQueryInfoColumn columnIdtoR = columnsRange[1];
					if (columnIdtoR!=null)
						rangeColumnIdto = columnIdtoR.getColumnName();
					if (rangeColumn == null) {
						System.err.println("No se ha encontrado la columna del rango en la tabla/vista " + referencedTableView);
					} else {
						//preguntamos x la DataProperty idProperty
						IQueryInfoColumn columnIdProperty = referencedTableView.getDataPropertyColumn(IQueryInfo.ID_PROPERTY);
						String columnIdPropertyName = null;
						if (columnIdProperty!=null)
							columnIdPropertyName = columnIdProperty.getColumnName();
						
						if (isInverseStructural) {
							String tmp = domainColumn;
							domainColumn = rangeColumn;
							rangeColumn = tmp;
							tmp = domainColumnIdto;
							domainColumnIdto = rangeColumnIdto;
							rangeColumnIdto = tmp;
						}
						
						//columnas en referencedTable
						String referencedTableName = gSQL.getCharacterBegin() + referencedTableView.getName() + gSQL.getCharacterEnd();
						String aliasReferencedTableName = gSQL.getCharacterBegin() + "asoc" + parent.getAttributeValue(QueryConstants.ID) + 
							"_" + nodeClass.getAttributeValue(QueryConstants.ID) + gSQL.getCharacterEnd();
						buildAssocciationJoin(from, sq.getWhere(), nodeClass, isView, isViewParent, tableName, aliasTableName, aliasTableNameParent, referencedTableName, aliasReferencedTableName, 
								domainColumn, domainColumnIdto, rangeColumn, rangeColumnIdto, columnIdPropertyName, prop,idosCompresed);
					}
				}
			} else {
				String aliasTableWithColumn = aliasTableName;
				String aliasTableWithoutColumn = aliasTableNameParent;
				boolean isViewWithColumn = isView;
				boolean isViewWithoutColumn = isViewParent;
				int idtoWithColumn = idto;
				int idtoWithoutColumn = idtoParent;
				
				if (isInverseStructural) {
					String tmp = aliasTableWithColumn;
					aliasTableWithColumn = aliasTableWithoutColumn;
					aliasTableWithoutColumn = tmp;
				}
				IQueryInfoColumn[] columns = null;
				if (!reversed) {
					columns = tableView.getObjectPropertyColumn(prop, idtoParent);
					if (columns==null || columns[0]==null)
						throw new DataErrorException("No está definido el enlace entre " + tableViewParent.getName() + "(" + idtoParent + ") y " + tableView.getName() + "(" + idto + ") a traves de " + ik.getPropertyName(prop) + "(" + prop + ")");
				} else {
					columns = tableViewParent.getObjectPropertyColumn(prop, idto);
					if (columns==null || columns[0]==null)
						throw new DataErrorException("No está definido el enlace entre " + tableView.getName() + "(" + idto + ") y " + tableViewParent.getName() + "(" + idtoParent + ") a traves de " + ik.getPropertyName(prop) + "(" + prop + ")");
				}
				String columnName = columns[0].getColumnName();
				String columnNameIdto = null;
				IQueryInfoColumn columnIdto = columns[1];
				if (columnIdto!=null)
					columnNameIdto = columnIdto.getColumnName();
				if (columnName == null) {
					if (!reversed)
						System.err.println("No se ha encontrado la columna del rango en la tabla/vista " + tableView);
					else
						System.err.println("No se ha encontrado la columna del rango en la tabla/vista " + tableViewParent);
				} else {
					// columnName tiene el nombre de la columna que busco y esa columna tiene el tableId del objeto en la otra tabla/vista.
					buildDirectJoin(from, sq.getWhere(), nodeClass, isView, tableName, aliasTableName, aliasTableWithColumn, aliasTableWithoutColumn, columnName, columnNameIdto, 
							idtoWithColumn, idtoWithoutColumn, isViewWithColumn, isViewWithoutColumn,idosCompresed);
				}
			}
		}
		buildSelect(nodeClass, from, isView, idto, tableView, aliasTableName, aliasTableNameNoCharacter);
		keepConditions(nodeClass, aliasTableName, tableView);
		//System.out.println("Fin de la funcion nodeClassExistsLast");
	}
	
	/** metodo que construye inner/left join entre 2 tablas/vistas en las que una de ellas tiene el idTable de la otra en una de sus columnas (no se usa tabla/vista asociacion).*/
	private void buildDirectJoin(StringBuffer from, StringBuffer where, Element nodeClass, boolean isView, String tableName, String aliasTableName, String aliasTableWithColumn, 
			String aliasTableWithoutColumn, String columnName, String columnNameIdto, int idtoWithColumn, int idtoWithoutColumn, boolean isViewWithColumn, boolean isViewWithoutColumn,boolean idosCompresed) {
		//System.out.println("Inicio de la funcion buildDirectJoin");
		columnName = gSQL.getCharacterBegin() + columnName + gSQL.getCharacterEnd();
		columnNameIdto = gSQL.getCharacterBegin() + columnNameIdto + gSQL.getCharacterEnd();
		String nameTableId = gSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_TABLEID + gSQL.getCharacterEnd();
		String nameIdto = gSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_IDTO + gSQL.getCharacterEnd();
		
		boolean isInner = isInner(nodeClass);
		if (isInner)
			from.append("\nINNER");
		else
			from.append("\nLEFT");
		from.append(" JOIN " + tableName + " AS " + aliasTableName);
		from.append(" ON (");
		from.append(aliasTableWithColumn + "." + columnName + "=" + aliasTableWithoutColumn + "." + nameTableId);
		if (isViewWithColumn) { //el elemento que tiene la columna es vista
			if (isViewWithoutColumn) { //el elemento que no tiene la columna es vista
				//relacion entre vistas
				from.append(" AND " + aliasTableWithColumn + "." + columnNameIdto + "=" + aliasTableWithoutColumn + "." + nameIdto);
			} else {
				//relacion vista-tabla
				from.append(" AND " + aliasTableWithColumn + "." + columnNameIdto + "=" + idtoWithoutColumn);
			}
		} else {
			if (isViewWithoutColumn) {
				//relacion tabla-vista
				from.append(" AND " + aliasTableWithoutColumn + "." + nameIdto + "=" + idtoWithColumn);
			} else {
				//relacion tabla-tabla
				;
			}
		}
		if (isInner)
			buildWhereOfClass(nodeClass, aliasTableName, isView, from, false,idosCompresed);
		else
			buildWhereOfClass(nodeClass, aliasTableName, isView, where, true,idosCompresed);
		from.append(")");
		//System.out.println("Fin de la funcion buildDirectJoin");
	}
	
	//si hay un attribute/where con required se pone inner hacia arriba
	//si un class tiene required se pondra inner solo si su superior lo tiene
	/**metodo que establece los inner join entre los nodos.*/
	private void setInnerAndIdtoOfFixedIdos(Element node) {
		setRequired(node);
		setInnerRecursive(node);
		try {
			System.out.println("CLASS CON INNER: " + jdomParser.returnXML(node));
		} catch (JDOMException e) {
			e.printStackTrace();
		}
	}
	/*optimizacion comentada, falla en idto=cliente prop=forma_pago valuecls=aplazamiento.*/
	private void setIdtoOfFixedIdos(Element nodeClass) {
		Integer idto = getIdtoOfFixedIdos(nodeClass);
		if (idto!=null)
			nodeClass.setAttribute(QueryConstants.ID_TO,String.valueOf(idto));
	}
	private void setInnerRecursive(Element node) {
		if (node.getName().equals(QueryConstants.CLASS)) {
			//setIdtoOfFixedIdos(node);
			if (StringUtils.equals(node.getAttributeValue(QueryConstants.REQUIRED),"TRUE")) {
				Element parent = node.getParent();
				//si soy required y mi padre es el root, es decir, el padre del padre no es un class
				//si mi padre tiene inner
				if (!parent.getParent().getName().equals(QueryConstants.CLASS)) {
					if (StringUtils.equals(node.getAttributeValue(QueryConstants.REQUIRED),"TRUE"))
						node.setAttribute(QueryConstants.INNER,"TRUE");
				} else if (StringUtils.equals(parent.getAttributeValue(QueryConstants.INNER),"TRUE")) {
					node.setAttribute(QueryConstants.INNER,"TRUE");
				}
			}
			Iterator it = node.getChildren().iterator();
			while (it.hasNext())
				setInnerRecursive((Element)it.next());
		}
	}
	
	/**metodo que busca todas las hojas no class con required y pone required hacia nodos superiores.*/
	private void setRequired(Element node) {
		Iterator it = node.getChildren().iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			if (child.getName().equals(QueryConstants.CLASS)) {
				setRequired(child);
			} else if (StringUtils.equals(child.getAttributeValue(QueryConstants.REQUIRED),"TRUE")) {
				setInnerUp(node);
			}
		}
	}
	private void setInnerUp(Element node) {
		if (node.getName().equals(QueryConstants.CLASS)) {
			node.setAttribute(QueryConstants.REQUIRED,"TRUE");
			setInnerUp(node.getParent());
		}
	}
	
	/** metodo que indica si entre dos tablas/vistas debe establecerse un inner join.*/
	private boolean isInner(Element nodeClass) {
		//System.out.println("Inicio de la funcion isInner");
		//VERSION REQUERIDO EN BASE A OBLIGATORIEDAD
			boolean isInner = StringUtils.equals(nodeClass.getAttributeValue(QueryConstants.INNER),"TRUE");
		//FIN VERSION

		//VERSION ANTIGUA
//		boolean isInner = false;
//		if (StringUtils.equals(nodeClass.getAttributeValue(QueryConstants.REQUIRED),"TRUE")) {
//			isInner = true;
//		} else {
//			Iterator it = nodeClass.getChildren().iterator();
//			while (it.hasNext()) {
//				Element child = (Element)it.next();
//				if (!child.getName().equals(QueryConstants.CLASS)) {
//					if (StringUtils.equals(child.getAttributeValue(QueryConstants.REQUIRED),"TRUE")) {
//						isInner = true;
//						break;
//					}
//				}
//			}
//		}
		//FIN VERSION
		//System.out.println("Fin de la funcion isInner");
		return isInner;
	}
	/** metodo que construye inner/left join entre 2 tablas/vistas con una tabla/vista asociacion.*/
	private void buildAssocciationJoin(StringBuffer from, StringBuffer where, Element nodeClass, boolean isView, boolean isViewParent, String tableName, String aliasTableName, 
			String aliasTableNameParent, String referencedTableName, String aliasReferencedTableName, 
			String domainColumn, String domainColumnIdto, String rangeColumn, String rangeColumnIdto, String columnIdPropertyName, Integer idProperty,boolean idosCompresed) {
		//System.out.println("Inicio de la funcion buildAssocciationJoin");
		boolean isInner = isInner(nodeClass);
		domainColumn = gSQL.getCharacterBegin() + domainColumn + gSQL.getCharacterEnd();
		rangeColumn = gSQL.getCharacterBegin() + rangeColumn + gSQL.getCharacterEnd();
		String nameTableId = gSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_TABLEID + gSQL.getCharacterEnd();
		String nameIdto = gSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_IDTO + gSQL.getCharacterEnd();
		
		if (isInner)
			from.append("\nINNER");
		else
			from.append("\nLEFT");
		from.append(" JOIN " + referencedTableName + " AS " + aliasReferencedTableName);
		from.append(" ON (");
		from.append(aliasReferencedTableName + "." + domainColumn + "=" + aliasTableNameParent + "." + nameTableId);
		if (domainColumnIdto!=null && isViewParent) {
			//y aliasTableNameParent tiene la columna idto
			domainColumnIdto = gSQL.getCharacterBegin() + domainColumnIdto + gSQL.getCharacterEnd();
			from.append(" AND " + aliasReferencedTableName + "." + domainColumnIdto + "=" + aliasTableNameParent + "." + nameIdto);
		}
		if (columnIdPropertyName!=null) {
			columnIdPropertyName = gSQL.getCharacterBegin() + columnIdPropertyName + gSQL.getCharacterEnd();
			from.append(" AND " + aliasReferencedTableName + "." + columnIdPropertyName + "=" + idProperty);
		}
		from.append(")");
		
		if (isInner)
			from.append("\nINNER");
		else
			from.append("\nLEFT");
		from.append(" JOIN " + tableName + " AS " + aliasTableName);
		from.append(" ON (");
		from.append(aliasReferencedTableName + "." + rangeColumn + "=" + aliasTableName + "." + nameTableId);
		if (rangeColumnIdto!=null && isView) {
			//y aliasTableName tiene la columna idto
			rangeColumnIdto = gSQL.getCharacterBegin() + rangeColumnIdto + gSQL.getCharacterEnd();
			from.append(" AND " + aliasReferencedTableName + "." + rangeColumnIdto + "=" + aliasTableName + "." + nameIdto);
		}
		if (isInner)
			buildWhereOfClass(nodeClass, aliasTableName, isView, from, false,idosCompresed);
		else
			buildWhereOfClass(nodeClass, aliasTableName, isView, where, true,idosCompresed);
		from.append(")");
		//System.out.println("Fin de la funcion buildAssocciationJoin");
	}
	
	/** metodo llamado cuando se trata del primer nodo CLASS del XML, es decir, todavia no se ha empezado a construir la consulta.
	 * @throws NotFoundException 
	 * @throws IncoherenceInMotorException 
	 * @throws DataErrorException 
	 * @throws NoSuchColumnException */
	private void nodeClassNoExistsLast(StructureQuery sq, Element nodeClass,boolean idosCompresed) throws NotFoundException, IncoherenceInMotorException, DataErrorException, NoSuchColumnException {
		//System.out.println("Inicio de la funcion nodeClassNoExistsLast");
		StringBuffer where = sq.getWhere();
		StringBuffer from = sq.getFrom();
		
		int idto = Integer.parseInt(nodeClass.getAttributeValue(QueryConstants.ID_TO));
		System.out.println("idto " + idto);
		boolean isView = true;
		IQueryInfo tableView = dataBaseMap.getView(idto);
		if (tableView==null) {
			isView = false;
			tableView = dataBaseMap.getTable(idto);
		}
		String tableName = gSQL.getCharacterBegin() + tableView.getName() + gSQL.getCharacterEnd();
		
		String ident = nodeClass.getAttributeValue(QueryConstants.NAME)==null?"tb":nodeClass.getAttributeValue(QueryConstants.NAME).replaceAll(" ", "_");
		String aliasTableNameNoCharacter = ident + nodeClass.getAttributeValue(QueryConstants.ID);
		String aliasTableName = gSQL.getCharacterBegin() + aliasTableNameNoCharacter + gSQL.getCharacterEnd();
		from.append(tableName + " AS " + aliasTableName);
		
		buildWhereOfClass(nodeClass, aliasTableName, isView, where, false,idosCompresed);
		buildSelect(nodeClass, where, isView, idto, tableView, aliasTableName, aliasTableNameNoCharacter);
		keepConditions(nodeClass, aliasTableName, tableView);
		//System.out.println("Fin de la funcion nodeClassNoExistsLast");
	}
	
	/** funcion que itera por nodos case y where y los almacena en las variables hWhere y hCase
	 * @throws NotFoundException 
	 * @throws DataErrorException 
	 * @throws NoSuchColumnException */
	private void keepConditions(Element nodeClass, String aliasTableName, IQueryInfo tableView) throws DataErrorException, NotFoundException, NoSuchColumnException {
		//System.out.println("Inicio de la funcion keepConditions");
		Iterator it = nodeClass.getChildren().iterator();
		boolean multipleConditions=false;
		while (it.hasNext()) {
			Element child = (Element)it.next();
			if(!multipleConditions && it.hasNext()) multipleConditions=true;
			if (child.getName().equals(QueryConstants.WHERE)) {
				buildConditionWhere(child, aliasTableName, tableView,multipleConditions);
			} else if (child.getName().equals(QueryConstants.CASE)) {
				buildConditionCase(child, aliasTableName, tableView);
			}
		}
		//System.out.println("Fin de la funcion keepConditions");
	}
	
	/** metodo que se encarga de construir la parte del filtrado que se observa en el nodo CLASS. 
	 * Las condiciones pueden ser de varios tipos: 
	 * - Fijando el tableId a uno o varios valores. Se observa en un nodo CLASS con el atributo TABLE_ID/NOT_TABLE_ID
	 * - Condiciones sobre datos. Se observa en un nodo WHERE 
	 * - Condiciones sobre la existencia/no existencia. Se observa en un nodo ATTRIBUTE o CLASS en los atributos REQUIRED/NULL*/
	private void buildWhereOfClass(Element root, String aliasTableName, boolean isView, StringBuffer where, boolean allowNulls,boolean idosCompresed) {
		//System.out.println("Inicio de la funcion buildWhereOfClass");
		String ido = root.getAttributeValue(QueryConstants.ID_O);
		String notIdo = root.getAttributeValue(QueryConstants.NOT_ID_O);
		
		String nameTableId = gSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_TABLEID + gSQL.getCharacterEnd();
		String nameIdto = gSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_IDTO + gSQL.getCharacterEnd();
		if (ido!=null) {
			HashMap<Integer, HashSet<Integer>> hIdtoTableIds = QueryConstants.getIdtoTableIds(Auxiliar.stringToHashSetInteger(ido, ","),idosCompresed);
			boolean first = true;
			Iterator it = hIdtoTableIds.keySet().iterator();
			while (it.hasNext()) {
				int idto = (Integer)it.next();
				HashSet<Integer> tableIds = hIdtoTableIds.get(idto);
				if (first) {
					if (where.length()>0)
						where.append(" AND ");
					where.append("(");
					first = false;
				} else {
					where.append(" OR ");
				}
				if (allowNulls)
					where.append("(" + aliasTableName + "." + nameTableId + " is null OR ");
				where.append(aliasTableName + "." + nameTableId + " IN(" + Auxiliar.hashSetIntegerToString(tableIds, ",") + ")");
				if (isView)
					where.append(" AND " + aliasTableName + "." + nameIdto + "=" + idto);
				if (allowNulls)
					where.append(")");
			}
			if (!first)
				where.append(")");
		}
		if (notIdo!=null) {
			HashMap<Integer, HashSet<Integer>> hIdtoTableIds = QueryConstants.getIdtoTableIds(Auxiliar.stringToHashSetInteger(notIdo, ","),idosCompresed);
			Iterator it = hIdtoTableIds.keySet().iterator();
			while (it.hasNext()) {
				int idto = (Integer)it.next();
				HashSet<Integer> tableIds = hIdtoTableIds.get(idto);
				if (where.length()>0)
					where.append(" AND ");
				if (allowNulls)
					where.append("(" + aliasTableName + "." + nameTableId + " is null OR ");
				where.append("NOT(" + aliasTableName + "." + nameTableId + " IN(" + Auxiliar.hashSetIntegerToString(tableIds, ",") + ")");
				if (isView)
					where.append(" AND " + aliasTableName + "." + nameIdto + "=" + idto + ")");
				else
					where.append(")");
				if (allowNulls)
					where.append(")");
			}
		}
		boolean isNull = StringUtils.equals(root.getAttributeValue(QueryConstants.NULL),"TRUE");
		if (isNull) {
			if (where.length()>0)
				where.append(" AND ");
			where.append(aliasTableName + "." + nameTableId + " IS NULL");
		}
		//System.out.println("Fin de la funcion buildWhereOfClass");
	}
	
	/** metodo encargado de almacenar el select de la consulta con los campos que se muestran.
	 * Además modifica where si alguno de los ATTRIBUTE es REQUIRED/NULL.
	 * @throws NotFoundException 
	 * @throws DataErrorException 
	 * @throws NoSuchColumnException */
	private void buildSelect(Element root, StringBuffer where, boolean isView, int idto, IQueryInfo tableView, String aliasTableName, String aliasTableNameNoCharacter) throws DataErrorException, NotFoundException, NoSuchColumnException {
		//System.out.println("Inicio de la funcion buildSelect");
		String idRoot = root.getAttributeValue(QueryConstants.ID);
		String fieldRoot = aliasTableName + "." + gSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_TABLEID + gSQL.getCharacterEnd();
		String aliasRoot = aliasTableNameNoCharacter + "_" + IQueryInfo.COLUMN_NAME_TABLEID;
		String aliasIdtoRoot = aliasTableNameNoCharacter + "_idto";
		String idtoName = "(case when " + fieldRoot + " is not null then ";
		if (isView) {
			String nameIdto = gSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_IDTO + gSQL.getCharacterEnd();
			idtoName += aliasTableName + "." + nameIdto;
		} else {
			idtoName += "'" + idto + "'";
		}
		idtoName += " else null end)";

		Select selectObjectRoot = new Select(fieldRoot, aliasRoot, aliasIdtoRoot, null, idtoName, QueryConstants.TM_ID, null);
		System.out.println("idRoot " + idRoot + ", selectObjectRoot " + selectObjectRoot);
		hSelect.put(idRoot,selectObjectRoot);

		Iterator it = root.getChildren(QueryConstants.ATTRIBUTE).iterator();
		while (it.hasNext()) {
			Element attribute = (Element)it.next();
			int prop = Integer.parseInt(attribute.getAttributeValue(QueryConstants.PROP));
			System.out.println("propSelect " + prop + " en la tabla/vista " + tableView.getName());
			List<String> columnsName = tableView.getColumnNamesContainingProperty(prop);
			if (columnsName==null || columnsName.size()==0 || columnsName.get(0)==null)
				throw new DataErrorException("No existe una columna para la property " + ik.getPropertyName(prop) + " en la tabla/vista " + tableView.getName());
			String columnNameNoCharacter = columnsName.get(0);
			String columnName = gSQL.getCharacterBegin() + columnNameNoCharacter + gSQL.getCharacterEnd();
			
			int tmRuleengine = Integer.parseInt(attribute.getAttributeValue(QueryConstants.ID_TM_RULEENGINE));
			String field = aliasTableName + "." + columnName;
			String alias = attribute.getAttributeValue(QueryConstants.NAME)==null?
					aliasTableNameNoCharacter + "_" + columnNameNoCharacter:
					attribute.getAttributeValue(QueryConstants.NAME);
			Select selectObject = new Select(field, alias, null, attribute.getAttributeValue(QueryConstants.ID_CASE), null, tmRuleengine, idRoot);
			System.out.println("id " + attribute.getAttributeValue(QueryConstants.ID) + ", selectObject " + selectObject);
			hSelect.put(attribute.getAttributeValue(QueryConstants.ID),selectObject);
			
			if (attribute.getAttributeValue(QueryConstants.REQUIRED)!=null) {
				if (where.length()>0)
					where.append(" AND ");
				where.append(aliasTableName + "." + columnName + " IS NOT NULL");
			} else if (attribute.getAttributeValue(QueryConstants.NULL)!=null) {
				if (where.length()>0)
					where.append(" AND ");
				where.append(aliasTableName + "." + columnName + " IS NULL");
			}
		}
		//System.out.println("Fin de la funcion buildSelect");
	}
	
	/** Construye la parte de la consulta afectada por el nodo CLASS que le llega como parámetro.*/
	private void nodeClass(StructureQuery sq, Element nodeStructure, Element nodeClass,boolean idosCompresed) throws NotFoundException, IncoherenceInMotorException, NoSuchColumnException, DataErrorException {
		//System.out.println("Inicio de la funcion nodeClass");
		//como entrada puede llegar la CLASS superior que tiene como nodo superior STRUCTURE
		//o una object property CLASS
		
		//si ese nodo no tiene datos mostrar rdn. Esto se podria añadir en QueryXML
		//Rdn.insertRDNClass(QueryConstants.ATTRIBUTE, nodeStructure, nodeClass, null, true, String.valueOf(countRdn));
		//countRdn--;
		if (nodeClass.getParent().getName().equals(QueryConstants.STRUCTURE))
			nodeClassNoExistsLast(sq, nodeClass,idosCompresed);
		else
			nodeClassExistsLast(sq, nodeClass,idosCompresed);
		
		if (nodeClass.hasChildren())
			iteraClass(sq, nodeStructure, nodeClass,idosCompresed);
		//System.out.println("Fin de la funcion nodeClass");	
	}
	
	/** metodo que almacena condiciones where en hWhere.
	 * @throws NotFoundException 
	 * @throws DataErrorException 
	 * @throws NoSuchColumnException */
	private void buildConditionWhere(Element node, String aliasTableName, IQueryInfo tableView,boolean multipleConditions) throws DataErrorException, NotFoundException, NoSuchColumnException {
		//System.out.println("Inicio de la funcion buildConditionWhere");
		ConditionsWhere cWhere = buildObjectCondition(multipleConditions,node, aliasTableName, tableView);
		hWhere.put(node.getAttributeValue(QueryConstants.ID), cWhere);
		//System.out.println("Fin de la funcion buildConditionWhere");
	}
	/** metodo que almacena condiciones case en hWhere.
	 * @throws NotFoundException 
	 * @throws DataErrorException 
	 * @throws NoSuchColumnException */
	private void buildConditionCase(Element node, String aliasTableName, IQueryInfo tableView) throws DataErrorException, NotFoundException, NoSuchColumnException {
		//System.out.println("Inicio de la funcion buildConditionCase");
		ConditionsWhere cWhere = buildObjectCondition(false,node, aliasTableName, tableView);
		StringBuffer conditionBf = new StringBuffer("");
		buildConditionSQL(cWhere, conditionBf, false);
		String condition = conditionBf.toString();
		hCase.put(node.getAttributeValue(QueryConstants.ID), condition);
		//System.out.println("Fin de la funcion buildConditionCase");
	}
	
	/** metodo que dado un objeto ConditionsWhere almacena en un StringBuffer la codición.*/
	private static void buildConditionSQL(ConditionsWhere conditionStr, StringBuffer where, boolean isnot) {
		//System.out.println("Inicio de la funcion buildCondition");
		if (conditionStr!=null) {
			StringBuffer condition1 = conditionStr.getCondition1();
			StringBuffer condition2 = conditionStr.getCondition2();
			boolean hayCondition1 = condition1.length()>0;
			boolean hayCondition2 = condition2.length()>0;
			if (hayCondition1 || hayCondition2) {
				if (where.length()>0)
					where.append(" AND ");
			
				if (hayCondition1 && hayCondition2)
					where.append("(");
				if (hayCondition1) {
					where.append(condition1);
					if (hayCondition2)
						where.append(" OR ");
				}
				boolean parentesis = false;
				if (isnot) {
					parentesis = !condition2.substring(0, 1).equals("(");
					where.append("NOT");
					//mira si el 1er elemento es un parentesis 
					if (parentesis)
						where.append("(");
				}
				if (hayCondition2)
					where.append(condition2);
				if (parentesis)
					where.append(")");
				if (hayCondition1 && hayCondition2)
					where.append(")");
			}
		}
		//System.out.println("Fin de la funcion buildCondition");
	}

	/** metodo que dado un nodo where/case construye un objeto ConditionsWhere.
	 * @throws NotFoundException 
	 * @throws DataErrorException 
	 * @throws NoSuchColumnException */
	private ConditionsWhere buildObjectCondition(boolean multipleConditions,Element node, String aliasTableName, IQueryInfo tableView) throws DataErrorException, NotFoundException, NoSuchColumnException {
		//System.out.println("Inicio de la funcion subBuildCondition");
		int tmRuleengine = Integer.parseInt(node.getAttributeValue(QueryConstants.ID_TM_RULEENGINE));
		
		int prop = Integer.parseInt(node.getAttributeValue(QueryConstants.PROP));

		List<String> columnsName = tableView.getColumnNamesContainingProperty(prop);
		if (columnsName==null || columnsName.size()==0 || columnsName.get(0)==null)
			throw new DataErrorException("No existe una columna para la property " + ik.getPropertyName(prop) + " en la tabla/vista " + tableView.getName());

		String columnName = aliasTableName + "." + 
					gSQL.getCharacterBegin() + columnsName.get(0) + gSQL.getCharacterEnd();

		StringBuffer where1 = new StringBuffer("");
		StringBuffer where2 = new StringBuffer("");
		//ver si es requerido
		boolean required = StringUtils.equals(node.getAttributeValue(QueryConstants.REQUIRED), "TRUE");
		String op = node.getAttributeValue(QueryConstants.OP);
		if (op != null) {
			if (!required)
				where1.append(columnName + " IS NULL");
			
			String cmll = "'";
			Pattern p = Pattern.compile(".*[^;](;{1}|;{3}|;{5}|;{7}|;{9})[^;].*");
			if (op.equals(QueryConstants.BETWEEN)) {
				String vMin = node.getAttributeValue(QueryConstants.VAL_MIN);
				String vMax = node.getAttributeValue(QueryConstants.VAL_MAX);
				
				//nunca sera string ni memo
				/*if (tmRuleengine == Constants.IDTO_STRING || tmRuleengine == Constants.IDTO_MEMO) {
					if (vMin != null)
						where2.append(columnName + QueryConstants.MAYOR_IGUAL + cmll + vMin.replaceAll("'", "''") + cmll);
					if (vMax != null) {
						if (vMin != null)
							where2.append(" AND ");
						where2.append(columnName + QueryConstants.MENOR_IGUAL + cmll + vMax.replaceAll("'", "''") + cmll);
					}
				} else {*/
					if (vMin != null) {
						boolean isMultiMin = p.matcher(vMin).matches();
						if (!isMultiMin)
							where2.append(columnName + QueryConstants.MAYOR_IGUAL + vMin);
						else {
							where2.append("(");
							Object[] lista = jdomParser.parseMultivalue(vMin);
							for (int iter = 0; iter < lista.length; iter++) {
								if (iter > 0)
									where2.append(" OR ");
								where2.append(columnName + QueryConstants.MAYOR_IGUAL + lista[iter]);
							}
							where2.append(")");
						}
					}
					if (vMax != null) {
						if (vMin != null)
							where2.append(" AND ");
						boolean isMultiMax = p.matcher(vMax).matches();
						if (!isMultiMax) {
							where2.append(columnName + QueryConstants.MENOR_IGUAL + vMax);
						} else {
							where2.append("(");
							Object[] lista = jdomParser.parseMultivalue(vMax);
							for (int iter = 0; iter < lista.length; iter++) {
								if (iter > 0)
									where2.append(" OR ");
								where2.append(columnName + QueryConstants.MENOR_IGUAL + lista[iter]);
							}
							where2.append(")");
						}
					}
				//}
			} else {
				String val = node.getAttributeValue(QueryConstants.VALUE) == null ? node.getText()
						: node.getAttributeValue(QueryConstants.VALUE);
				if (val!=null) {
					boolean isMulti = p.matcher(val).matches();
					if (op.equals(QueryConstants.DISTINTO)) {
						op = QueryConstants.DISTINTO_VALIDO;
					}
					String rVal = "";
					if (tmRuleengine == Constants.IDTO_STRING || tmRuleengine == Constants.IDTO_MEMO) {
						/*if (op.equals(QueryConstants.REG_EXPR) || op.equals(QueryConstants.NOT_REG_EXPR)) {
							if (tm == QueryConstants.TM_STRING || tm == QueryConstants.TM_MEMO) {
								//si no es required se le da la posibilidad de que sea nulo mediante un or, por lo tanto no 
								//llega a la funcion RegExMatch un valor nulo; sin embargo, si es required no se le da esa 
								//posibilidad por lo que hay que indicar explicitamente que no sea nulo
								//ya si llega un nulo a la funcion RegExMatch falla
								//if (required)
									//where2.append(etiqueta + " IS NOT NULL AND ");
								boolean pos = (op.equals(QueryConstants.REG_EXPR));
								where2.append(gSQL.getRegularExpresion(columnName,val, pos));
							}
						} else {*/
							if (tmRuleengine == Constants.IDTO_STRING || tmRuleengine == Constants.IDTO_MEMO) {
								if (op.equals(QueryConstants.CONTAINS)) {
									System.out.println("Es contains");
									if(val.matches(":.+,.+")&&tmRuleengine == Constants.IDTO_STRING){
										rVal = build_IN_SQL(val.substring(1),cmll);
									}else{
										rVal = gSQL.getLike() + cmll + "%" + val.replaceAll("'", "''") + "%" + cmll;
									}
								} else {
									System.out.println("Es igual "+multipleConditions);
									if(val.matches(":.+,.+")&&tmRuleengine == Constants.IDTO_STRING){
										rVal = build_IN_SQL(val.substring(1),cmll);
									}else{
										if (	!(multipleConditions && val!=null && !val.contains("%")) &&
												(op.equals(QueryConstants.IGUAL) || op.equals(QueryConstants.LIKE)))
											op = gSQL.getLike();
										rVal = op + cmll + val.replaceAll("'", "''") + cmll;	
									}									
								}
							}
							where2.append(columnName + rVal);
						//}
					} else {
						if (!isMulti) {
							if (val != null) {
								if (tmRuleengine == Constants.IDTO_BOOLEAN) {
									if (val.equals("1"))
										val = "true";
									else
										val = "false";
								}
								rVal = op + val;
								if (op.equals(QueryConstants.DISTINTO_VALIDO))
									where2.append(columnName + " IS NOT NULL AND ");
								
								/*if (op.equals(QueryConstants.REG_EXPR))
									where2.append(gSQL.getRegularExpresion(columnName,val,true));
								else if (op.equals(QueryConstants.NOT_REG_EXPR))
									where2.append(gSQL.getRegularExpresion(columnName,val,false));
								else*/
									where2.append(columnName + rVal);
							}
						} else {
							where2.append("(");
							Object[] lista = jdomParser.parseMultivalue(val);
							for (int iter = 0; iter < lista.length; iter++) {
								if (iter > 0)
									where2.append(" OR ");
								if (op.equals(QueryConstants.DISTINTO_VALIDO))
									where2.append(columnName + " IS NOT NULL AND ");
								val = (String)lista[iter];
								if (tmRuleengine == Constants.IDTO_BOOLEAN) {
									if (val.equals("1"))
										val = "true";
									else
										val = "false";
								}
								where2.append(columnName + op + val);
							}
							where2.append(")");
						}
					}
				}
			}
		} else if (required) {
			where1.append(columnName + " IS NOT NULL");
		} else {
			boolean nulo = StringUtils.equals(node.getAttributeValue(QueryConstants.NULL), "TRUE");
			if (nulo)
				where1.append(columnName + " IS NULL");
		}
		ConditionsWhere where = new ConditionsWhere(where1, where2);
		return where;
		//System.out.println("Fin de la funcion subBuildCondition");
	}
	
	private String build_IN_SQL(String val,String cmll){
		String rVal = "IN(";
		String[] valores=val.replaceAll("'", "''").split(",");
		for(int i=0;i<valores.length;i++){
			if(valores[i]==null||valores[i].length()==0) continue;
			
			if(i>0) rVal+=",";
			rVal+=cmll+valores[i]+cmll;
		}
		rVal +=")";
		return rVal;
	}
	/** Itera por sus nodos CLASS hijos.*/
	private void iteraClass(StructureQuery sq, Element nodeStructure, Element root,boolean idosCompresed) throws NotFoundException, IncoherenceInMotorException, NoSuchColumnException, DataErrorException {
		//System.out.println("Inicio de la funcion iteraClass");
		Iterator iterador = root.getChildren().iterator();
		while (iterador.hasNext()) {
			Element hj = (Element)iterador.next();
			String nameChild = hj.getName();
			if (nameChild.equals(QueryConstants.CLASS))
				nodeClass(sq, nodeStructure, hj,idosCompresed);
		}
		//System.out.println("Fin de la funcion iteraClass");
	}
	
	/** añade la columna en las variables def y defColSup.*/
	private void addCol(String name, int tmRuleengine, int colSup) {
		//System.out.println("Inicio de la funcion addCol");
		def[countDef] = tmRuleengine;
		defColSup[countDef] = colSup;
		if (colSup!=0) {
			DataInfo dataInfo = new DataInfo(getType(tmRuleengine),countDef+1,countColumn);
			countColumn++;
			dataInfoXName.put(name, dataInfo);
		}
		//System.out.println(tmRuleengine);
		countDef++;
		//System.out.println("Fin de la funcion addCol");
	}
	private String getType(Integer tmRuleEngine) {
		//System.out.println("Inicio de la funcion getType");
		String type = "";
		if (tmRuleEngine==Constants.IDTO_STRING || tmRuleEngine==Constants.IDTO_MEMO || tmRuleEngine==Constants.IDTO_IMAGE) {
			type = "String";
		} else if (tmRuleEngine.equals(Constants.IDTO_DATE) || tmRuleEngine.equals(Constants.IDTO_TIME) ||
				tmRuleEngine.equals(Constants.IDTO_DATETIME)) {
			type = "Long";
			//TODO este cambio afectaria a todos los reports, pero seraa lo correcto. 
			//Revisar todos los reports cuando se cambie para que tengan espacio o formatear la hora
			/*if (tmRuleengine.equals(Constants.IDTO_DATE))
				type = "Date";
			else if (tmRuleengine.equals(Constants.IDTO_TIME))
				type = "Time";
			else if (tmRuleengine.equals(Constants.IDTO_DATETIME))
				type = "DateTime";*/
		} else if (tmRuleEngine.equals(Constants.IDTO_BOOLEAN)) {
			type = "Boolean";
		} else {
			type = "Double";
		}
		//System.out.println("Fin de la funcion getType");
		return type;
	}
}
