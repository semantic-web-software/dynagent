package dynagent.server.importers;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.knowledge.Category;
import dynagent.common.knowledge.action;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.jdomParser;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.DebugParser;

public class IndividualParser {

	private FactoryConnectionDB fcdb;
	private DebugParser debug;
	private String namePropRdn;

	class IdtoRdn {
		private int idto;
		private String rdn;
		
		public IdtoRdn(int idto, String rdn) {
			this.idto = idto;
			this.rdn = rdn;
		}
		public int getIdto() {
			return idto;
		}
		public void setIdto(int idto) {
			this.idto = idto;
		}
		public String getRdn() {
			return rdn;
		}
		public void setRdn(String rdn) {
			this.rdn = rdn;
		}
	}
	
	public IndividualParser(int business, String databaseIP, String gestor) {
		this.fcdb = new FactoryConnectionDB(business, true, databaseIP, gestor);
		this.debug = new DebugParser();
	}

	private void setRdn(ConnectionDB con) throws SQLException, NamingException {
		Statement st = null;
		ResultSet rs = null;
		String sql = "SELECT NAME FROM properties WHERE PROP=" + Constants.IdPROP_RDN;
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				namePropRdn = rs.getString(1);
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
		}
	}
	public void parserIDs(Element facts) throws ParseException {
		try {
			boolean hasNoFacts = false;
			ConnectionDB conDb = null;
			try {
				conDb = fcdb.createConnection(true);
				setRdn(conDb);
				HashMap<Integer,HashSet<String>> idoIdtos = new HashMap<Integer,HashSet<String>>();

				Iterator it = facts.getChildren("FACT").iterator();
				while (it.hasNext()) {
					Element fact = (Element)it.next();
					if (fact.getName().equals("FACT"))
						parserFact(conDb, facts, fact, idoIdtos);
					else
						hasNoFacts = true;
				}
				parserIdoIdtos(conDb, idoIdtos);
			} catch (NamingException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					if (conDb!=null)
						conDb.close();
				} catch(SQLException e) {
		      		System.out.println("ERROR SQLException_finally:");
					e.printStackTrace();
				}
			}
			if (hasNoFacts)
				debug.addWarning("El nodo FACTS debe tener nodos hijo FACT. Nodos con otro nombre no seran procesados");
		} catch (JDOMException e) {
            throw new ParseException(e.getMessage(), 0);
		}
		if (debug.hasErrors()) {
			throw new ParseException("\n" + debug.toString(),0);
		} else 
			System.out.println(debug.toString());
	}
	
	private void parserIdoIdtos(ConnectionDB con, HashMap<Integer,HashSet<String>> idoIdtos) throws SQLException, NamingException {
		if (idoIdtos.size()>0) {
			String idos = "";
			Iterator it = idoIdtos.keySet().iterator();
			while (it.hasNext()) {
				Integer ido = (Integer)it.next();
				if (idos.length()>0)
					idos += ",";
				idos += ido;
			}
			HashMap<Integer,Integer> idoIdtosInst = new HashMap<Integer,Integer>();
			Statement st = null;
			ResultSet rs = null;
			String sql = "SELECT ID_O, ID_TO FROM O_Reg_Instancias WHERE ID_O IN(" + idos + ")";
			try {
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sql);
				while (rs.next()) {
					int ido = rs.getInt(1);
					int idto = rs.getInt(2);
					idoIdtosInst.put(ido, idto);
				}
			} finally {
				if (rs!=null)
					rs.close();
				if (st!=null)
					st.close();
			}
			HashSet<Integer> idosNoDb = new HashSet<Integer>();
			HashMap<Integer, HashSet<String>> idosIdtosNoDb = new HashMap<Integer, HashSet<String>>();
			it = idoIdtos.keySet().iterator();
			while (it.hasNext()) {
				Integer ido = (Integer)it.next();
				Integer idtoInstance = idoIdtosInst.get(ido);
				if (idtoInstance!=null) {
					HashSet<String> aIdto = idoIdtos.get(ido);
					Iterator it2 = aIdto.iterator();
					while (it2.hasNext()) {
						Integer idto = Integer.parseInt((String)it2.next());
						//idto de oreginst puede ser un especializado de valuecls
						HashSet<String> aSpec = new HashSet<String>();
						putSpecialized(con, idto, aSpec);
						if (!aSpec.contains(String.valueOf(idtoInstance))) {
							//ERROR la clase no es correcta
							addIdto(idosIdtosNoDb, ido, String.valueOf(idto));
						}
					}
				} else {
					//ERROR el ido no esta en bd
					idosNoDb.add(ido);
				}
			}
			parserIdosNoDb(con, idosNoDb, idosIdtosNoDb);
		}
	}

	private void parserIdosNoDb(ConnectionDB con, HashSet<Integer> idosNoDb, HashMap<Integer, HashSet<String>> idosIdtosNoDb) 
			throws SQLException, NamingException {
		if (idosNoDb.size()>0)
			debug.addError("El/Los individuo/s " + Auxiliar.hashSetIntegerToString(idosNoDb, ", ") + " usados no están en base de datos");
		
		Iterator it = idosIdtosNoDb.keySet().iterator();
		while (it.hasNext()) {
			Integer ido = (Integer)it.next();
			HashSet<String> clases = idosIdtosNoDb.get(ido);
			debug.addError("La/s clases/s " + Auxiliar.hashSetStringToString(parserNamesClasses(con, clases, null), ",") + " no están en base de datos para el individuo " + ido);
		}
	}
	
	private void parserFact(ConnectionDB con, Element facts, Element fact, HashMap<Integer,HashSet<String>> idoIdtos) 
			throws JDOMException, SQLException, NamingException, ParseException {
		Element newFact = fact.getChild("NEW_FACT");
		if (newFact!=null) {
			String factOri = jdomParser.returnNodeXML(newFact);
			Integer order = parserAtributesFact(con, facts, newFact, factOri, idoIdtos);
			Element initialFact = fact.getChild("INITIAL_FACT");
			if (initialFact!=null) {
				String initialFactOri = jdomParser.returnNodeXML(initialFact);
				parserAtributesFact(con, facts, initialFact, initialFactOri, idoIdtos);
				if (order!=null && order!=action.SET)
					debug.addWarning("El atributo ORDER del fact " + factOri + " no es SET, por lo que no deberaa tener valores " +
							"iniciales fijados");
			} else {
				if (order!=null && order==action.SET)
					debug.addError("El atributo ORDER del fact " + factOri + " es SET y no tiene valores iniciales fijados");
			}
		} else
			debug.addError("Bajo un nodo FACT debe existir un nodo NEW_FACT con los atributos propios de un Fact");
	}
	
	private void checkProp(ConnectionDB con, String propIni, String idtoIni, int prop, Integer idto) throws SQLException, NamingException {
		if (idto!=null) {
			Statement st = null;
			ResultSet rs = null;
			HashSet<String> dominio = new HashSet<String>();
			
			String sql = "SELECT IDTO FROM instances WHERE PROPERTY=" + prop + " AND (OP='OR' OR OP='AND')";
			System.out.println("SQL_CHECKPROP " + sql);
			try {
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sql);
				while (rs.next()) {
					//obtengo los especializados del dominio
					int idtoInstance = rs.getInt(1);
					if (!rs.wasNull())
						putSpecialized(con, idtoInstance, dominio);
				}
			} finally {
				if (rs!=null)
					rs.close();
				if (st!=null)
					st.close();
			}
			if (!(dominio.contains(String.valueOf(idto))))
				debug.addError("La property " + propIni + " no está declarada para la clase " + idtoIni + 
						", no está expresada en el modelo");
		}
	}
	private void putSpecialized(ConnectionDB conDb, int idtoPadre, HashSet<String> aSpec) 
			throws SQLException, NamingException {
		aSpec.add(String.valueOf(idtoPadre));
		String sql = "select ID_TO from T_Herencias where ID_TO_Padre=" + idtoPadre;
		Statement st = null;
		ResultSet rs = null;
		try {
			st = conDb.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				int idto = rs.getInt(1);
				aSpec.add(String.valueOf(idto));
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
		}
	}
	
	private Integer parserAtributesFact(ConnectionDB con, Element facts, Element fact, String factOri, HashMap<Integer,HashSet<String>> idoIdtos) 
			throws JDOMException, SQLException, NamingException, ParseException {
		Integer order = parserOrder(fact, factOri);
		
		String idtoIni = fact.getAttributeValue("CLASS")!=null?fact.getAttributeValue("CLASS"):fact.getAttributeValue("IDTO");
		Integer idto = parserIdto(con, fact, factOri);
		idto = parserIdo(con, facts, fact, factOri, idto, idtoIni, idoIdtos);
		idto = parserIdtoB(con, fact, factOri, idto);
		
		String propS = fact.getAttributeValue("PROP");
		Integer valueClsDB = null;
		boolean isOP = false;
		boolean isDP = false;
		if (propS!=null) {
			if (!Auxiliar.hasIntValue(propS)) {
				Statement st = null;
				ResultSet rs = null;
				String sql = "SELECT PROP, CAT, VALUECLS FROM properties WHERE NAME='" + propS + "'";
				try {
					st = con.getBusinessConn().createStatement();
					rs = st.executeQuery(sql);
					if (rs.next()) {
						int prop = rs.getInt(1);
						//comprobar que pertenece a la clase idto
						checkProp(con, propS, idtoIni, prop, idto);
						//ver si idto es un especializado o coincide con alguno de los idto que hay para esa prop
						fact.setAttribute("PROP",(new Integer(prop)).toString());
						String cat = rs.getString(2);
						if (cat!=null) {
							Category category = new Category(Integer.parseInt(cat));
							if (category.isObjectProperty()) {
								isOP = true;
							} else if (category.isDataProperty()) {
								isDP = true;
								String valueClsDBS = rs.getString(3);
								if (Auxiliar.hasIntValue(valueClsDBS))
									valueClsDB = Integer.parseInt(valueClsDBS);
							} else
								debug.addError("La property " + prop + " no está clasificada ni como DataProperty ni como " +
										"ObjectProperty en el modelo");
						}
					} else {
						debug.addError("No se encuentra en la tabla properties la prop con name " + propS + ", necesaria para deducir "
								+ "la property en el Fact " + factOri);
					}
				} finally {
					if (rs!=null)
						rs.close();
					if (st!=null)
						st.close();
				}
			}
		} else
			debug.addError("No se ha puesto el atributo PROP en el nodo " + factOri);
		
		//valueClsDB solo tiene valor en DataProperty
		//pero si se indica valueRdn, se puede hallar el valueCls en la ObjectProperty
		String valueClsIni = fact.getAttributeValue("VALUECLS");
		valueClsDB = parserValueCls(con, fact, factOri, valueClsDB);
		valueClsDB = parserValue(con, facts, fact, factOri, isOP, isDP, valueClsDB, valueClsIni, idoIdtos);
		valueClsDB = parserValueClsB(con, fact, factOri, isDP, isOP, valueClsDB);
		parserQ(fact, factOri, isDP, isOP);
		
		return order;
	}
	
	private void addIdto(HashMap<Integer,HashSet<String>> idoIdtos, int ido, String idto) {
		HashSet<String> idtos = idoIdtos.get(ido);
		if (idtos==null)
			idtos = new HashSet<String>();
		if (idto!=null)
			idtos.add(idto);
		idoIdtos.put(ido,idtos);
	}
	
	private void parserQ(Element fact, String factOri, boolean isDP, boolean isOP) throws JDOMException, ParseException {
		String qMinS = fact.getAttributeValue("QMIN");
		String qMaxS = fact.getAttributeValue("QMAX");
		String valueClsS = fact.getAttributeValue("VALUECLS");
		
		if (valueClsS!=null && Auxiliar.hasIntValue(valueClsS)) {
			Integer valueCls = Integer.parseInt(valueClsS);
			if (qMinS!=null || qMaxS!=null) {
				if (isDP) {
					//si es String o Memo debe ser null
					if (valueCls==Constants.IDTO_STRING || valueCls==Constants.IDTO_MEMO)
						debug.addWarning("En el nodo " + factOri + " se está introduciendo datos en QMIN y en QMAX cuando " +
								"es de tipo String o Memo");
					else if (valueCls==Constants.IDTO_UNIT || 
							valueCls==Constants.IDTO_INT || 
							valueCls==Constants.IDTO_DOUBLE || 
							valueCls==Constants.IDTO_BOOLEAN || 
							valueCls==Constants.IDTO_TIME || 
							valueCls==Constants.IDTO_DATETIME || 
							valueCls==Constants.IDTO_DATE) {
						
						if (qMinS!=null && !Auxiliar.hasFloatValue(qMinS)) {
							if (valueCls==Constants.IDTO_DATE || valueCls==Constants.IDTO_DATETIME || valueCls==Constants.IDTO_TIME) {
								String pattern = QueryConstants.getPattern(valueCls);
								Long qMin = QueryConstants.dateToSeconds(pattern, qMinS);
								if (qMin!=null)
									fact.setAttribute("QMIN",String.valueOf(qMin));
								else
									debug.addError("El atributo QMIN en el nodo " + factOri + " no contiene una fecha en " +
											"el formato correcto " + pattern);
							} else
								debug.addError("En el nodo " + factOri + " el valor introducido en QMIN no es correcto, ya que " +
										"no es de tipo Float");
						}
						if (qMaxS!=null && !Auxiliar.hasFloatValue(qMaxS)) {
							if (valueCls==Constants.IDTO_DATE || valueCls==Constants.IDTO_DATETIME || valueCls==Constants.IDTO_TIME) {
								String pattern = QueryConstants.getPattern(valueCls);
								Long qMax = QueryConstants.dateToSeconds(pattern, qMaxS);
								if (qMax!=null)
									fact.setAttribute("QMAX",String.valueOf(qMax));
								else
									debug.addError("El atributo QMAX en el nodo " + factOri + " no contiene una fecha en " +
											"el formato correcto " + pattern);
							} else
								debug.addError("En el nodo " + factOri + " el valor introducido en QMAX no es correcto, ya que " +
										"no es de tipo Float");
						}
					}
				} else if (isOP && (qMinS!=null || qMaxS!=null))
					debug.addWarning("Se han indicado los atributos QMIN y QMAX en un Fact cuya property es de la clase ObjectProperty " 
							+ factOri + ". Estos atributos no seran considerados");
			} else if (isDP) {
				if (valueCls==Constants.IDTO_UNIT || 
						valueCls==Constants.IDTO_INT || 
						valueCls==Constants.IDTO_DOUBLE || 
						valueCls==Constants.IDTO_BOOLEAN || 
						valueCls==Constants.IDTO_TIME || 
						valueCls==Constants.IDTO_DATETIME || 
						valueCls==Constants.IDTO_DATE)
					debug.addWarning("En el nodo " + factOri + " se está introduciendo valores nulos en VALUE, QMIN y QMAX cuando " +
								"es de tipo Unit, Int, Double, Boolean, Time, DateTime o Date");
			}
		}
	}
	
	private Integer parserOrder(Element fact, String factOri) throws JDOMException {
		Integer order = null;
		String orderS = fact.getAttributeValue("ORDER");
		if (orderS!=null) {
			if (!Auxiliar.hasIntValue(orderS)) {
				if (orderS.equals("NEW")) {
					order = action.NEW;
					fact.setAttribute("ORDER", String.valueOf(order));
				} else if (orderS.equals("SET")) {
					order = action.SET;
					fact.setAttribute("ORDER", String.valueOf(order));
				} else if (orderS.equals("DEL")) {
					order = action.DEL;
					fact.setAttribute("ORDER", String.valueOf(order));
				} else if (orderS.equals("DEL_OBJECT")) {
					order = action.DEL_OBJECT;
					fact.setAttribute("ORDER", String.valueOf(order));
				} else {
					order = -1;
					debug.addError("El atributo ORDER no es correcto. solo se permiten los valores: NEW, SET, DEL, DEL_OBJECT");
				}
			}
		} else {
			order = action.NEW;
			fact.setAttribute("ORDER", String.valueOf(order));
//			debug.addError("No se ha puesto el atributo ORDER en el nodo " + factOri);
		}
		return order;
	}
	
	private Integer parserIdo(ConnectionDB con, Element facts, Element fact, String factOri, Integer idtoDB, String idtoIni, 
			HashMap<Integer,HashSet<String>> idoIdtos) throws JDOMException, SQLException, NamingException {
		String idoS = fact.getAttributeValue("IDO");
		if (idoS!=null) {
			if (!Auxiliar.hasIntValue(idoS))
				debug.addError("El atributo IDO en el nodo " + factOri + " debe ser numero");
			else {
				int ido = Integer.parseInt(idoS);
				if (!Constants.isIDTemporal(ido))
					addIdto(idoIdtos, ido, String.valueOf(idtoDB));
			}
		} else {
			String idoRdnS = fact.getAttributeValue("IDORDN");
			if (idoRdnS!=null)
				idtoDB = parserIdoValueRdn(con, facts, fact, factOri, idoRdnS, "IDO", idtoDB, idtoIni);
			else
				debug.addError("El atributo IDO del nodo " + factOri + " no debe ser nulo");
		}
		return idtoDB;
	}
	
	private Integer parserIdto(ConnectionDB con, Element fact, String factOri) throws JDOMException, SQLException, NamingException {
		String idtoS = fact.getAttributeValue("CLASS")!=null?fact.getAttributeValue("CLASS"):fact.getAttributeValue("IDTO");
		Integer idto = null;
		if (idtoS!=null) {
			if (!Auxiliar.hasIntValue(idtoS)) {
				idto = parserClass(con, fact, factOri, "IDTO", idtoS);
				if (idto!=null)
					fact.removeAttribute("CLASS");
			} else {
				idto = Integer.parseInt(idtoS);
				fact.setAttribute("IDTO",idtoS);
				fact.removeAttribute("CLASS");
			}
		}
		return idto;
	}
	private Integer parserIdtoB(ConnectionDB con, Element fact, String factOri, Integer idtoDB) throws JDOMException, SQLException, NamingException {
		String idtoS = fact.getAttributeValue("CLASS")!=null?fact.getAttributeValue("CLASS"):fact.getAttributeValue("IDTO");
		if (idtoS==null || (idtoDB!=null && !StringUtils.equals(idtoS,String.valueOf(idtoDB)))) {
			if (idtoDB!=null) {
				//se comprueba si son distintos porque puede que se haya obtenido un especializado
				boolean correcto = false;
				if (idtoS!=null && Auxiliar.hasIntValue(idtoS)) {
					HashSet<String> aSpec = new HashSet<String>();
					putSpecialized(con, Integer.parseInt(idtoS), aSpec);
					if (aSpec.contains(String.valueOf(idtoDB)))
						correcto = true;
				} else
					correcto = true;
				if (correcto) {
					fact.setAttribute("IDTO", String.valueOf(idtoDB));
					fact.removeAttribute("CLASS");
				}
			} else {
				String idoS = fact.getAttributeValue("IDO");
				if (idoS!=null && Auxiliar.hasIntValue(idoS)) {
					if (Integer.parseInt(idoS)>0)
						idtoDB = parserClassORegInstancias(con, fact, factOri, Integer.parseInt(idoS), "IDTO");
					else {
						//buscar en los Facts por id_o y por value (el valueRdn seria null xq en caso contrario valueClsDB no seria null)
						//que no tengan el idTo o el valueCls null
						//1 por ido sin idto null
						String[] ats = new String[2];
						ats[0] = "IDTO";
						ats[1] = "CLASS";
						Element factValueCls = jdomParser.findElementByAtValAtsNotNull(fact, "*", "IDO", idoS, ats, true);
						String factIdto = null;
						if (factValueCls!=null) {
							factIdto = factValueCls.getAttributeValue("IDTO")!=null?factValueCls.getAttributeValue("IDTO"):factValueCls.getAttributeValue("CLASS");
						} else {
							factValueCls = jdomParser.findElementByAtValAndTextAtNotNull(fact, "*", "VALUE", idoS, "VALUECLS", true);
							if (factValueCls!=null)
								factIdto = factValueCls.getAttributeValue("VALUECLS");
						}
						if (factIdto!=null) {
							if (Auxiliar.hasIntValue(factIdto)) {
								idtoDB = Integer.parseInt(factIdto);
								fact.setAttribute("IDTO", factIdto);
							} else
								idtoDB = parserClass(con, fact, factOri, "IDTO", factIdto);
							fact.removeAttribute("CLASS");
						} else {
							debug.addError("Debe indicar el atributo IDTO o CLASS ya que no es deducible a partir de los datos indicados en el Fact " + factOri);
						}
					}
				} else {
					debug.addError("Debe indicar el atributo IDTO o CLASS ya que no es deducible a partir de los datos indicados en el Fact " + factOri);
				}
			}
		}
		return idtoDB;
	}
	
	private Integer parserClass(ConnectionDB con, Element fact, String factOri, String nameAttribute, String idtoS) 
			throws JDOMException, SQLException, NamingException {
		Integer idto = null;
		Statement st = null;
		ResultSet rs = null;
		String sql = "SELECT IDTO FROM Clases WHERE NAME='" + idtoS + "'";
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				idto = rs.getInt(1);
				if (fact!=null)
					fact.setAttribute(nameAttribute,(new Integer(idto)).toString());
			} else if (factOri!=null)
				debug.addError("No se encuentra en la tabla Clases la clase con name " + idtoS + ", necesaria para deducir "
						+ "la clase en el Fact " + factOri);
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
		}
		return idto;
	}

	/*private String parserNameClass(ConnectionDB con, String clase, String factOri) throws SQLException, NamingException {
		//puede que sea un entero y haya que obtener el name porque en los facts se da la posibilidad de poner el name o la clase
		if (clase!=null) {
			if (Auxiliar.hasIntValue(clase)) {
				Statement st = null;
				ResultSet rs = null;
				String sql = "SELECT NAME FROM Clases WHERE IDTO=" + clase;
				try {
					st = con.getBusinessConn().createStatement();
					rs = st.executeQuery(sql);
					if (rs.next()) {
						clase = rs.getString(1);
					} else if (factOri!=null)
						debug.addError("No se encuentra en la tabla Clases la clase " + clase + ", necesaria para deducir "
								+ "el nombre de la clase en el Fact " + factOri);
				} finally {
					if (rs!=null)
						rs.close();
					if (st!=null)
						st.close();
				}
			}
		}
		return clase;
	}*/
	private HashSet<String> parserNamesClasses(ConnectionDB con, HashSet<String> clases, String factOri) throws SQLException, NamingException {
		HashSet<String> names = new HashSet<String>();
		Statement st = null;
		ResultSet rs = null;
		String sql = "SELECT IDTO, NAME FROM Clases WHERE IDTO IN(" + Auxiliar.hashSetStringToString(clases, ", ") + ")";
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String name = rs.getString(2);
				if (name!=null) {
					names.add(name);
				} else {
					debug.addError("No se encuentra en la tabla Clases la clase " + rs.getInt(1));
					if (factOri!=null)
						debug.addError(", necesaria para deducir el nombre de la clase o de alguno de sus especializados en el Fact " + factOri);
				}
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
		}
		return names;
	}
	
	private Integer parserValue(ConnectionDB con, Element facts, Element fact, String factOri, boolean isOP, boolean isDP, 
			Integer valueCls, String valueClsIni, HashMap<Integer,HashSet<String>> idoIdtos) 
			throws JDOMException, SQLException, NamingException, ParseException {
		//si es ObjectProperty el valueS debe ser numero
		String valueS = fact.getAttributeValue("VALUE")!=null?fact.getAttributeValue("VALUE"):fact.getText();
		if (isOP) {
			if (valueS!=null && valueS.length()>0) {
				if (fact.getAttributeValue("VALUE")!=null) {
					fact.setText(valueS);
					fact.removeAttribute("VALUE");
				}
				if (!Auxiliar.hasIntValue(valueS))
					debug.addError("El atributo VALUE o el contenido del nodo " + factOri + " debe ser numero, ya que se trata de una ObjectProperty");
				else {
					int value = Integer.parseInt(valueS);
					if (!Constants.isIDTemporal(value))
						addIdto(idoIdtos, value, String.valueOf(valueCls));
				}
			} else {
				String valueRdnS = fact.getAttributeValue("VALUERDN");
				if (valueRdnS!=null)
					valueCls = parserIdoValueRdn(con, facts, fact, factOri, valueRdnS, "VALUE", valueCls, valueClsIni);
				else
					debug.addError("El atributo VALUE, VALUERDN o el contenido del nodo " + factOri + " no debe ser nulo, ya que se trata de una ObjectProperty");
			}
		} else if (isDP && valueCls!=null) {
			if (valueS!=null && valueS.length()>0) {
				if (valueCls==Constants.IDTO_UNIT || 
						valueCls==Constants.IDTO_INT || 
						valueCls==Constants.IDTO_DOUBLE || 
						valueCls==Constants.IDTO_TIME || 
						valueCls==Constants.IDTO_DATETIME || 
						valueCls==Constants.IDTO_DATE || 
						valueCls==Constants.IDTO_BOOLEAN && fact.getAttributeValue("QMIN")==null && fact.getAttributeValue("QMAX")==null) {
//					debug.addWarning("En el nodo " + factOri + " se está introduciendo datos en VALUE cuando el VALUECLS es " + valueClsS);
					if (Auxiliar.hasFloatValue(valueS)) {
						fact.setAttribute("QMIN",valueS);
						fact.setAttribute("QMAX",valueS);
						fact.removeAttribute("VALUE");
					} else {
						if (valueCls==Constants.IDTO_BOOLEAN) {
							if (StringUtils.lowerCase(valueS).equals("true")) {
								fact.setAttribute("QMIN","1");
								fact.setAttribute("QMAX","1");
								fact.removeAttribute("VALUE");
							} else if (StringUtils.lowerCase(valueS).equals("false")) {
								fact.setAttribute("QMIN","0");
								fact.setAttribute("QMAX","0");
								fact.removeAttribute("VALUE");
							} else {
								debug.addError("El atributo VALUE o el contenido del nodo " + factOri + " debe ser de tipo Float o Boolean, ya que se trata de una " +
									"DataProperty de tipo Unit, Int, Double, Boolean sin comentario, Time, DateTime o Date");
							}
						} else if (valueCls==Constants.IDTO_UNIT || 
								valueCls==Constants.IDTO_INT || 
								valueCls==Constants.IDTO_DOUBLE || 
								valueCls==Constants.IDTO_BOOLEAN || 
								valueCls==Constants.IDTO_TIME || 
								valueCls==Constants.IDTO_DATETIME || 
								valueCls==Constants.IDTO_DATE) {
							
							if (valueS!=null && !Auxiliar.hasFloatValue(valueS)) {
								if (valueCls==Constants.IDTO_DATE || valueCls==Constants.IDTO_DATETIME || valueCls==Constants.IDTO_TIME) {
									String pattern = QueryConstants.getPattern(valueCls);
									Long value = QueryConstants.dateToSeconds(pattern, valueS);
									if (value!=null) {
										fact.setAttribute("QMIN",String.valueOf(value));
										fact.setAttribute("QMAX",String.valueOf(value));
										fact.removeAttribute("VALUE");
									} else
										debug.addError("El atributo VALUE o el contenido del nodo " + factOri + " no contiene una fecha en " +
												"el formato correcto " + pattern);
								} else
									debug.addError("El atributo VALUE o el contenido del nodo " + factOri + " debe ser de tipo Float, ya que se " +
											"trata de una DataProperty de tipo Unit, Int, Double, Boolean sin comentario, Time, DateTime o Date");
							}
						}
					}
				} else {
					if (fact.getAttributeValue("VALUE")!=null) {
						fact.setText(valueS);
						fact.removeAttribute("VALUE");
					}
				}
			} else {
				//si es String o Memo -> warning
				if (valueCls==Constants.IDTO_STRING || valueCls==Constants.IDTO_MEMO)
					debug.addWarning("En el nodo " + factOri + " se está introduciendo un valor nulo en VALUE y en su contenido cuando el " +
							"VALUECLS es String o Memo");
			}
			//boolean no estara en ninguno de los 2 casos xq puede ser un comentario al booleano
		}
		return valueCls;
	}
	
	private Integer parserIdoValueRdn(ConnectionDB con, Element facts, Element fact, String factOri, String valueRdnS, String etiq, 
			Integer valueCls, String valueClsIni) throws JDOMException, SQLException, NamingException {
		//buscar en BD
		//si no esta buscar en el Element
		HashSet<String> valueClsDBSpecialized = null;
		if (valueCls!=null) {
			valueClsDBSpecialized = new HashSet<String>();
			putSpecialized(con, valueCls, valueClsDBSpecialized);
		}
		System.out.println("valueClsDBSpecialized " + Auxiliar.hashSetStringToString(valueClsDBSpecialized, ","));
		Integer valueClsDB = parserValueRdnDB(con, fact, valueRdnS, valueClsDBSpecialized, etiq);
		if (valueClsDB==null)
			valueClsDB = parserIdoValueRdnFacts(con, facts, fact, factOri, valueRdnS, valueClsDBSpecialized, /*valueClsIni, */etiq);
		if (valueClsDB==null)
			debug.addError("No se ha podido deducir el atributo " + etiq + " del atributo " + etiq + "RDN en el nodo " + factOri);
		return valueClsDB;
	}
	
	private Integer parserIdoValueRdnFacts(ConnectionDB con, Element facts, Element fact, String factOri, String valueRdnS, 
			HashSet<String> valueClsSpecialized, /*String valueClsIni,*/ String etiq) throws JDOMException, SQLException, NamingException {
		Integer idtoValueClsDB = null;
		Element factRdn = null;
		String[] vals = new String[2];
		vals[0] = namePropRdn;
		vals[1] = String.valueOf(Constants.IdPROP_RDN);
		if (valueClsSpecialized==null)
			factRdn = jdomParser.findElementByAtAndTextAt(facts, "PROP", vals, "VALUE", valueRdnS, true);
		else {
			String[] ats = new String[2];
			ats[0] = "IDTO";
			ats[1] = "CLASS";
			//if (valueClsSpecialized!=null)
				valueClsSpecialized.addAll(parserNamesClasses(con, valueClsSpecialized, factOri));
			//else if (valueClsIni!=null) {
				//valueClsIni = parserNameClass(con, valueClsIni, factOri);
				//valueClsSpecialized.add(valueClsIni);
			//}
			factRdn = jdomParser.findElementByAtsAndTextAt(facts, "PROP", vals, ats, valueClsSpecialized, "VALUE", valueRdnS, true);
		}
		if (factRdn!=null) {
			String idtoS = factRdn.getAttributeValue("IDTO")!=null?factRdn.getAttributeValue("IDTO"):factRdn.getAttributeValue("CLASS");
			if (idtoS!=null) {
				if (Auxiliar.hasIntValue(idtoS))
					idtoValueClsDB = Integer.parseInt(idtoS);
				else
					idtoValueClsDB = parserClass(con, null, null, null, idtoS);
			}
			String idoS = factRdn.getAttributeValue("IDO");
			if (idoS!=null && idoS.length()>0) {
				if (etiq.equals("VALUE")) {
					fact.setText(idoS);
					fact.removeAttribute("VALUERDN");
				} else if (etiq.equals("IDO")) {
					fact.setAttribute("IDO", idoS);
					fact.removeAttribute("IDORDN");
				}
			}
		}
		return idtoValueClsDB;
	}
	
	private Integer parserValueRdnDB(ConnectionDB con, Element fact, String valueRdnS, HashSet<String> valueClsSpecialized, String etiq) throws JDOMException, SQLException, NamingException {
		Integer valueClsDB = null;
		Statement st = null;
		ResultSet rs = null;
		//String sql = "SELECT ID_O, ID_TO FROM O_Datos_Atrib WHERE VAL_TEXTO='" + valueRdnS.replaceAll("'", "''") + "' AND PROPERTY=" + Constants.IdPROP_RDN;
		String sql = "SELECT ID_O, ID_TO FROM O_Reg_Instancias WHERE RDN='" + valueRdnS.replaceAll("'", "''") + "'";
		if (valueClsSpecialized!=null)
			sql += " AND ID_TO IN(" + Auxiliar.hashSetStringToString(valueClsSpecialized, ", ") + ")";
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				int ido = rs.getInt(1);
				valueClsDB = rs.getInt(2);
				if (etiq.equals("VALUE")) {
					fact.setText((new Integer(ido)).toString());
					fact.removeAttribute("VALUERDN");
				} else if (etiq.equals("IDO")) {
					fact.setAttribute("IDO",(new Integer(ido)).toString());
					fact.removeAttribute("IDORDN");
				}
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
		}
		return valueClsDB;
	}
	
	private Integer parserValueCls(ConnectionDB con, Element fact, String factOri, Integer valueClsDB) 
			throws JDOMException, SQLException, NamingException {
		String valueClsS = fact.getAttributeValue("VALUECLS");
		Integer valueCls = valueClsDB;
		if (valueClsS!=null) {
			if (Auxiliar.hasIntValue(valueClsS)) {
				valueCls = Integer.parseInt(valueClsS);
				//ver si coincide
				if (valueClsDB!=null && !valueCls.equals(valueClsDB))
					debug.addError("El valor indicado en el atributo VALUECLS del nodo " + factOri + " no es correcto");
				//TODO en las OP podriamos comprobar que coincida si el value es positivo con el que haya en O_Reg_Instancias 
				//y si no con los Facts que haya, pero es necesario?
				//(no tiene porque estar en O_Reg_Instancias si el individuo es nuevo y no se ha insertado todavia)
			} else
				//una query como la que se hace con el idto
				valueCls = parserClass(con, fact, factOri, "VALUECLS", valueClsS);
		}
		return valueCls;
	}
	private Integer parserValueClsB(ConnectionDB con, Element fact, String factOri, boolean isDP, boolean isOP, Integer valueClsDB) 
			throws JDOMException, SQLException, NamingException {
		String valueClsS = fact.getAttributeValue("VALUECLS");
		if (valueClsS==null || (valueClsDB!=null && !StringUtils.equals(valueClsS,String.valueOf(valueClsDB)))) {
			if (isDP)
				fact.setAttribute("VALUECLS", String.valueOf(valueClsDB));
			else if (isOP) {
				if (valueClsDB!=null) {
					//se comprueba si son distintos porque puede que se haya obtenido un especializado
					boolean correcto = false;
					if (valueClsS!=null && Auxiliar.hasIntValue(valueClsS)) {
						HashSet<String> aSpec = new HashSet<String>();
						putSpecialized(con, Integer.parseInt(valueClsS), aSpec);
						if (aSpec.contains(String.valueOf(valueClsDB)))
							correcto = true;
					} else
						correcto = true;
					if (correcto)
						fact.setAttribute("VALUECLS", String.valueOf(valueClsDB));
				} else {
					//puede deducirse a partir de O_Reg_Instancias si el value es positivo
					String valueS = fact.getAttributeValue("VALUE")!=null?fact.getAttributeValue("VALUE"):fact.getText();
					if (valueS!=null && Auxiliar.hasIntValue(valueS)) {
						if (Integer.parseInt(valueS)>0)
							valueClsDB = parserClassORegInstancias(con, fact, factOri, Integer.parseInt(valueS), "VALUECLS");
						else {
							//buscar en los Facts por id_o y por value (el valueRdn seria null xq en caso contrario valueClsDB no seria null)
							//que no tengan el idTo o el valueCls null
							//1 por ido sin idto null
							String[] ats = new String[2];
							ats[0] = "IDTO";
							ats[1] = "CLASS";
							Element factValueCls = jdomParser.findElementByAtValAtsNotNull(fact, "*", "IDO", valueS, ats, true);
							String idtoS = null;
							if (factValueCls!=null) {
								idtoS = factValueCls.getAttributeValue("IDTO")!=null?factValueCls.getAttributeValue("IDTO"):factValueCls.getAttributeValue("CLASS");
							} else {
								factValueCls = jdomParser.findElementByAtValAndTextAtNotNull(fact, "*", "VALUE", valueS, "VALUECLS", true);
								idtoS = factValueCls.getAttributeValue("VALUECLS");
							}
							if (idtoS!=null) {
								if (Auxiliar.hasIntValue(idtoS)) {
									valueClsDB = Integer.parseInt(idtoS);
									fact.setAttribute("VALUECLS", idtoS);
								} else
									valueClsDB = parserClass(con, fact, factOri, "VALUECLS", idtoS);
							} else {
								debug.addError("Debe indicar el VALUECLS ya que no es deducible a partir de los datos indicados en el Fact " + factOri);
							}
						}
					} else {
						debug.addError("Debe indicar el VALUECLS ya que no es deducible a partir de los datos indicados en el Fact " + factOri);
					}
				}
			}
		}
		return valueClsDB;
	}
	
	private Integer parserClassORegInstancias(ConnectionDB con, Element fact, String factOri, Integer value, String etiq) throws JDOMException, SQLException, NamingException {
		Integer idtoDB = null;
		Statement st = null;
		ResultSet rs = null;
		String sql = "SELECT ID_TO FROM O_Reg_Instancias WHERE ID_O=" + value;
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				int idto = rs.getInt(1);
				idtoDB = idto;
				fact.setAttribute(etiq,String.valueOf(idtoDB));
			} else
				debug.addError("No se encuentra en la tabla O_Reg_Instancias el individuo con ido " + value + ", necesario para deducir "
						+ "la clase en el Fact " + factOri);
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
		}
		return idtoDB;
	}
}
