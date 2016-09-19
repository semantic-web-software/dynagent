package dynagent.server.reports.clasificator;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.naming.NamingException;

import org.jdom.CDATA;
import org.jdom.Element;
import org.jdom.Namespace;

import dynagent.common.exceptions.DataErrorException;
import dynagent.common.utils.Auxiliar;
import dynagent.server.ejb.AuxiliarModel;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;

/**Clase que analiza los distintos nodos del diseño*/
public class ReportAnalizer {

	/**Variables globales para almacenar elementos que no se pretenden copiar en el nuevo diseño.*/
	/**Campos no validos segun el modelo.*/
	private HashSet<String> fieldsToRemove;
	/**parámetros no validos segun el modelo.*/
	private HashSet<String> parametersToRemove;
	/**Variables no validas segun el modelo.*/
	private HashSet<String> variablesToRemove;
	/**TextFields no validos segun el modelo.*/
	private HashSet<String> textFieldsToRemove;
	/**StaticTexts no validos segun el modelo.*/
	private HashSet<String> staticTextsToRemove;
	/**Frames no validos segun el modelo.*/
	private HashSet<String> framesToRemove;
	/**Tables no validos segun el modelo.*/
	//private HashSet<String> tablesToRemove;
	/**Groups no validos segun el modelo.*/
	private HashSet<String> groupsToRemove;
	/**Bands no validas segun el modelo.*/
	private HashSet<String> bandsToRemove;
	
	/**Dada una key de un elemento de tabla se mapean las coordenadas que tendran en el nuevo diseño.*/
	private HashMap<String,Coordinates> newCoordinatesInTable;
	/**Keys de todos los textFields y frames procesados. Se utiliza para verificar que las keys 
	 * de los staticTexts asociados a un textField o frame son correctas.*/
	private HashSet<String> allTextFieldsAndFrames;
	/**Keys de todos los elementos del diseño. Se utiliza para verificar que no hay keys repetidas.*/
	private HashSet<String> allKeys;
	/**Booleano que indica si el report no contiene ningun dato valido. Si es asi, puede ser eliminado. 
	 * Se puede dar en un subReport, en un report principal no tiene sentido.*/
	private boolean reportRemove;
	/**Mapa que por cada key de tabla almacena un conjunto de coordenadas de elementos en la tabla.*/
	private HashMap<String,Table> tablesUsed;
	
	/**Conjunto de entrada que contiene el atributo name de los subreports que no seran clonados 
	 * al nuevo diseño. El atributo name en el nodo jasperReport del subreport debe coincidir 
	 * con el atributo key dado al objeto en el report principal. tambien debe coincidir con 
	 * la continuación del parámetro paramsub...*/
	private HashSet<String> subreportsRemoved;
	/**Conjunto de entrada que almacena las variables eliminadas en los subreports. En el report 
	 * principal estas variables se corresponden con variables con otro nombre.*/
	private HashSet<String> variablesOfSubreportsRemoved;
	/**Mapa que guarda por cada grupo el conjunto de variables que dependen de el para ser clonadas o no.*/
	private HashMap<String,HashSet<String>> variablesDependGroup;
	/**Namespace usado al crear el diseño original.*/
	private Namespace nameSpace;
	
	private FactoryConnectionDB fcdb;

	public ReportAnalizer(FactoryConnectionDB fcdb, HashSet<String> subreportsRemoved, HashSet<String> variablesOfSubreportsRemoved, Namespace nameSpace) {
		this.fieldsToRemove = new HashSet<String>();
		this.parametersToRemove = new HashSet<String>();
		this.variablesToRemove = new HashSet<String>();
		this.textFieldsToRemove = new HashSet<String>();
		this.staticTextsToRemove = new HashSet<String>();
		this.framesToRemove = new HashSet<String>();
		//this.tablesToRemove = new HashSet<String>();
		this.groupsToRemove = new HashSet<String>();
		this.bandsToRemove = new HashSet<String>();
		
		this.allTextFieldsAndFrames = new HashSet<String>();
		this.allKeys = new HashSet<String>();
		
		this.newCoordinatesInTable = new HashMap<String, Coordinates>();
		
		this.tablesUsed = new HashMap<String,Table>();
		
		this.subreportsRemoved = subreportsRemoved;
		this.variablesOfSubreportsRemoved = variablesOfSubreportsRemoved;
		
		this.variablesDependGroup = new HashMap<String, HashSet<String>>();
		this.nameSpace = nameSpace;
		
		this.fcdb = fcdb;
	}
	/**Preprocesado para obtener los nombres de las variables en el subreport principal 
	 * que se corresponden con las variables que se han eliminado en el subreport.*/
	private void preProcess(Element jasperReport) {
		if (variablesOfSubreportsRemoved.size()>0) {
			//recorrer el xml viendo el name de la variable
			Iterator it = jasperReport.getChildren().iterator();
			while (it.hasNext()) {
				Element child = (Element)it.next();
				String nameChild = child.getName();
				if (nameChild.equals(ConstantsReport.group)) {
					Element groupHeader = child.getChild(ConstantsReport.groupHeader, nameSpace);
					if (groupHeader!=null) {
						preProcessBand(groupHeader.getChild(ConstantsReport.band, nameSpace));
					}
					Element groupFooter = child.getChild(ConstantsReport.groupFooter, nameSpace);
					if (groupFooter!=null) {
						preProcessBand(groupFooter.getChild(ConstantsReport.band, nameSpace));
					}
				} else if (nameChild.equals(ConstantsReport.background) ||
						nameChild.equals(ConstantsReport.title) ||
						nameChild.equals(ConstantsReport.pageHeader) || 
						nameChild.equals(ConstantsReport.columnHeader) || 
						nameChild.equals(ConstantsReport.detail) || 
						nameChild.equals(ConstantsReport.columnFooter) || 
						nameChild.equals(ConstantsReport.pageFooter) || 
						nameChild.equals(ConstantsReport.summary)) {
					preProcessBand(child.getChild(ConstantsReport.band, nameSpace));
				}
			}
		}
	}
	private void preProcessBand(Element band) {
		Iterator it = band.getChildren(ConstantsReport.subreport, nameSpace).iterator();
		while (it.hasNext()) {
			Element subreport = (Element)it.next();
			Iterator it2 = subreport.getChildren(ConstantsReport.returnValue, nameSpace).iterator();
			while (it2.hasNext()) {
				Element returnValue = (Element)it2.next();
				String subreportVariable = returnValue.getAttributeValue(ConstantsReport.subreportVariable);
				if (variablesOfSubreportsRemoved.contains(subreportVariable))
					variablesToRemove.add(returnValue.getAttributeValue(ConstantsReport.toVariable));
			}
		}
	}
	
	/**Inicio del analisis. Primero se hace un preprocesado y a continuación se tratan los elementos 
	 * en el orden: campos, parámetros, variables y bandas. Las variables se procesan de forma recursiva 
	 * hasta que todas están tratadas, ya que una variable puede depender de otra.
	 * Una vez tratados los elementos, se actualizan las coordenadas de los que hayan cambiado.
	 * @throws IOException */
	public ElemsAnalized startAnalizer(Element jasperReport) throws SQLException, NamingException, IOException {
		System.out.println("analizando....");
		preProcess(jasperReport);
		
		String pathLogFile = "C:\\logClasificator.txt";
		FileWriter f = null;
		boolean someClassified = false;
		reportRemove = true;
		try {
			f = new FileWriter(pathLogFile);
			//1er iterar x campos
			HashSet<String> OPs = new HashSet<String>();
			HashSet<String> DPs = new HashSet<String>();
			Iterator it = jasperReport.getChildren(ConstantsReport.field, nameSpace).iterator();
			while (it.hasNext()) {
				Element field = (Element)it.next();
				someClassified = processField(field, OPs, DPs, f) || someClassified;
			}
			if (!reportRemove && someClassified) {
				//2 iteramos x los parametros
				//si un parametro tiene como contenido un campo
				//es xq hay un dataset que lo usa
				it = jasperReport.getChildren(ConstantsReport.parameter, nameSpace).iterator();
				while (it.hasNext()) {
					Element parameter = (Element)it.next();
					processParameter(parameter);
				}
				
				//3 iteramos x las variables
				boolean allProcess = false;
				HashSet<String> variablesResolved = new HashSet<String>();
				while (!allProcess) {
					allProcess = true;
					it = jasperReport.getChildren(ConstantsReport.variable, nameSpace).iterator();
					while (it.hasNext()) {
						Element variable = (Element)it.next();
						//ver si esta en variablesResolved
						String name = variable.getAttributeValue(ConstantsReport.name);
						if (!variablesResolved.contains(name))
							allProcess = processVariable(variable, variablesResolved) || allProcess;
					}
				}
				//despues iterar x el resto
				it = jasperReport.getChildren().iterator();
				while (it.hasNext()) {
					Element child = (Element)it.next();
					String nameChild = child.getName();
					/*if (nameChild.equals(ConstantsReportAdapter.subDataSet))
						processSubDataSet(child); //no hace falta xa procesar parametros
												  //xq tienen el mismo valor en el atributo name
												  //q los q se hayan eliminado
					else */
					if (nameChild.equals(ConstantsReport.group))
						processGroup(child);
					else if (nameChild.equals(ConstantsReport.background) ||
							nameChild.equals(ConstantsReport.title) ||
							nameChild.equals(ConstantsReport.pageHeader) || 
							nameChild.equals(ConstantsReport.columnHeader) || 
							nameChild.equals(ConstantsReport.detail) || 
							nameChild.equals(ConstantsReport.columnFooter) || 
							nameChild.equals(ConstantsReport.pageFooter) || 
							nameChild.equals(ConstantsReport.summary)) {
						if (nameChild.equals(ConstantsReport.detail) && child.getChildren(ConstantsReport.band, nameSpace).size()>1) { 
							int nBand = 1;
							boolean allDeleted = true;
							Iterator it2 = child.getChildren(ConstantsReport.band, nameSpace).iterator();
							while (it2.hasNext()) {
								Element band = (Element)it2.next();
								String nameBand = nameChild + "_" + nBand;
								boolean deleted = processBand(band, nameBand);
								if (deleted)
									bandsToRemove.add(nameBand);
								else
									allDeleted = false;
								nBand++;
							}
							if (allDeleted)
								bandsToRemove.add(nameChild);
						} else {
							boolean deleted = processBand(child.getChild(ConstantsReport.band, nameSpace), nameChild);
							if (deleted)
								bandsToRemove.add(nameChild);
						}
					}
				}
			}
			
			//key de elemento - coordenadas
			processNewCoordinatesExpanding();
		} finally {
			f.close();
		}
		ElemsAnalized elemsAnalized = new ElemsAnalized(reportRemove, someClassified, fieldsToRemove, parametersToRemove, 
				variablesToRemove, textFieldsToRemove, staticTextsToRemove, framesToRemove, 
				groupsToRemove, bandsToRemove, allTextFieldsAndFrames, newCoordinatesInTable);
		System.out.println("ELEMS_ANALIZED " + elemsAnalized);
		return elemsAnalized;
	}
	
	/**Procesa campos del diseño. Comprueba que está bien escrito viendo si las clases que componen su 
	 * nombre están en tabla clases y las properties en properties. 
	 * Un nodo de un campo se clonara si no se ha excluido.
	 * Si ningun campo es correcto el report se elimina (reportRemove = true).
	 * @throws NamingException 
	 * @throws IOException */
	private boolean processField(Element field, HashSet<String> OPs, HashSet<String> DPs, FileWriter f) 
			throws SQLException, NamingException, IOException {
		//System.out.println("INICIO processField");
		boolean classified = false;
		String name = field.getAttributeValue(ConstantsReport.name);
		
		Element fieldProperty = field.getChild(ConstantsReport.property, nameSpace);
		if (fieldProperty!=null) {
			//(clase@propiedad;clase@propiedad:clase@propiedad;clase@propiedad)OR(clase@propiedad;clase@propiedad:clase@propiedad;clase@propiedad)
			String description = fieldProperty.getAttributeValue(ConstantsReport.value);
			//String description = ((CDATA)fieldDescription.getContent().get(0)).getText();
			if (description!=null) {
				classified = true;
				System.out.println("descriptionField " + description);
				boolean allDeleteOr = true;
				String[] spFieldOr = description.split(ConstantsReport.separatorFieldsOr);
				for (int m=0;m<spFieldOr.length;m++) {
					String fieldComponentOr = spFieldOr[m];
					if (fieldComponentOr.startsWith("("))
						fieldComponentOr = fieldComponentOr.substring(1);
					else if (fieldComponentOr.endsWith(")"))
						fieldComponentOr = fieldComponentOr.substring(0,fieldComponentOr.length()-1);
					
					boolean allDeleteAnd = false;
					String[] spField = fieldComponentOr.split(ConstantsReport.separatorFieldsAnd);
					for (int i=0;i<spField.length;i++) {
						String fieldComponent = spField[i];
						//System.out.println("fieldComponent " + fieldComponent);
						
						HashSet<Integer> valuesCls = null;
						String[] spClassPropGroups = fieldComponent.split(ConstantsReport.separatorClassPropGroups);
						for(int j=0;j<spClassPropGroups.length;j++) {
							String classPropGroups = spClassPropGroups[j];
							System.out.println("classPropGroups " + classPropGroups);
							String[] spClassProp = classPropGroups.split(ConstantsReport.separatorClassProp);
							String classesName = spClassProp[0];
							String propName = spClassProp[1];
							String valuesClsName = null;
							
							int next = j+1;
							if (next<spClassPropGroups.length) {
								String[] spClassPropGroupsNext = spClassPropGroups[next].split(ConstantsReport.separatorClassProp);
								valuesClsName = spClassPropGroupsNext[0];
							}
							boolean comprobado = false;
							if (valuesClsName==null)
								comprobado = DPs.contains(classesName + ConstantsReport.separatorClassProp + propName);
							else
								comprobado = OPs.contains(classesName + ConstantsReport.separatorClassProp + propName + ConstantsReport.separatorClassPropGroups + valuesClsName);
							
							//System.out.println("comprobado " + comprobado);
							if (!comprobado) {
								if (!propName.equals("tableId") && !propName.equals("rdn")) {
									//comprobar que esta en properties
									Integer prop = AuxiliarModel.getPropertyByNameInInstances(propName, fcdb);
									if (prop==null) {
										String error = "No existe la property " + propName;
										System.err.println(error);
										f.write(error + "\n");
									} else {
										//comprobar que esta en clases
										String[] spClassesName = classesName.split(ConstantsReport.separatorClass);
										
										HashSet<Integer> idtos = new HashSet<Integer>();
										if (valuesCls==null) { //este es el valuesCls de la iteracion anterior
											for (int h=0;h<spClassesName.length;h++) {
												String className = spClassesName[h];
												Integer idto = AuxiliarModel.getClassByNameInInstances(className, fcdb);
												if (idto==null) {
													String error = "No existe la clase " + className;
													System.err.println(error);
													f.write(error + "\n");
												} else {
													idtos.add(idto);
												}
											}
										} else
											idtos = (HashSet<Integer>)valuesCls.clone();
		
										valuesCls = new HashSet<Integer>();
										if (valuesClsName!=null) {
											String[] valuesClsNameSpl = valuesClsName.split(ConstantsReport.separatorClass);
											for (int h=0;h<valuesClsNameSpl.length;h++) {
												String valueClsName = valuesClsNameSpl[h];
												Integer valueCls = AuxiliarModel.getClassByNameInInstances(valueClsName, fcdb);
												if (valueCls==null) {
													String error = "No existe la clase " + valueClsName;
													System.err.println(error);
													f.write(error + "\n");
												} else {
													valuesCls.add(valueCls);
												}
											}
											OPs.add(classesName + ConstantsReport.separatorClassProp + propName + ConstantsReport.separatorClassPropGroups + valuesClsName);
										} else
											DPs.add(classesName + ConstantsReport.separatorClassProp + propName);
										
										//optimizar
										//1 - añadir mapas para no comprobar lo resuelto, almacenar correcto para un idto,prop,valueCls
										//2 - sacar valueCls fuera para no comprobarlo con idto en la siguiente iteracion
										
										if (idtos.size()>0) {
											//1er mirar en instances que esta bien esta clase con esta propiedad y valueCls, si no, error
											String sql = "select * from instances where ";
											sql += "idto in(" + Auxiliar.hashSetIntegerToString(idtos, ",") + ") and property=" + prop;
											if (valuesCls!=null && valuesCls.size()>0) {
												String valuesClsStr = Auxiliar.hashSetIntegerToString(valuesCls,",");
												sql += " and valuecls in(select id_to_padre from t_herencias where id_to in(" + valuesClsStr + ")" +
																		" union " +
																		"select id_to from t_herencias where id_to_padre in(" + valuesClsStr + "))";
											}
											System.out.println(sql);
											boolean hasRows = AuxiliarModel.hasRows(fcdb, sql);
											boolean hasError = false;
											if (!hasRows) {
												if (valuesCls!=null && valuesCls.size()>0) {
													//mirar la inversa
													String idtosStr = Auxiliar.hashSetIntegerToString(idtos,",");
													sql = "select * from instances where ";
													sql += "idto in(" + Auxiliar.hashSetIntegerToString(valuesCls,",") + ") and property=" + prop;
													sql += " and valuecls in(select id_to_padre from t_herencias where id_to in(" + idtosStr + ")" +
																			" union " +
																			"select id_to from t_herencias where id_to_padre in(" + idtosStr + "))";
													System.out.println(sql);
													hasRows = AuxiliarModel.hasRows(fcdb, sql);
													hasError = !hasRows;
												} else
													hasError = true;
											}
											if (hasError) {
												String error = null;
												if (valuesCls!=null && valuesCls.size()>0)
													error = "No existe la relación a traves de la propiedad " + propName + 
														" para la/s clase/s " + classesName + " con valueClsName/s " + valuesClsName;
												else
													error = "No existe la propiedad " + propName + " para la/s clase/s " + classesName;
												System.err.println(error);
												f.write(error + "\n");
											}
											//si no se ha producido excepción -> está en instance
											//mirar si está excluido
											if (allDeleteAnd || isExcluded(Auxiliar.hashSetIntegerToString(idtos, ","), prop)) {
												 //si algun elemento AND se borra -> esta parte es borrable
												allDeleteAnd = true;
												//break;
											}
										}
									}
								}
							}
						}
					}
					allDeleteOr = allDeleteOr && allDeleteAnd; //si todos los elementos OR se borran -> se borra el campo
				}
				if (allDeleteOr) { //eliminarlo
					System.out.println("Eliminando " + name);
					fieldsToRemove.add(name);
				} else
					reportRemove = false;
			} else {
				System.out.println("WARNING: El campo " + name + " no tiene descripción");
				reportRemove = false;
			}
		} else {
			System.out.println("WARNING: El campo " + name + " no tiene descripción");
			reportRemove = false;
		}
		//System.out.println("FIN processField");
		return classified;
	}
	private boolean isExcluded(String idtos, int prop) throws SQLException, NamingException {
		boolean excluded = false;
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String cB = gSQL.getCharacterBegin();
		String cE = gSQL.getCharacterEnd();
		
		String sql = "select cl.id, prop.id " +
				"from " + cB + "v_exclusión" + cE + " as excl " + 
				"left join clase as cl on(excl.dominio=cl." + cB + "tableId" + cE + ") " + 
				"left join v_propiedad as prop on(excl.propiedad=prop." + cB + "tableId" + cE + " and excl." + cB + "propiedadIdto" + cE + "=prop.idto) " +  
				"where cl.id in(" + idtos + ") and excl." + cB + "propiedad" + cE + " is null" + 
				" or excl.dominio is null and prop.id=" + prop + 
				" or cl.id in(" + idtos + ") and prop.id=" + prop;
		
		ConnectionDB con = fcdb.createConnection(true);
		Statement st = null;
		ResultSet rs = null;
		System.out.println("sql excluded->"+sql);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				excluded = true;
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		return excluded;
	}
	
	/**Procesa variables del diseño. Un nodo de una variable se clonara si sus expresiones 
	 * (initialValueExpression, variableExpression), si están rellenas, son correctas de acuerdo al modelo. 
	 * Una variable estara procesada si las variables en sus expresiones están resueltas.
	 * Depende de un grupo si sus atributos resetGroup o incrementGroup están rellenos.*/
	private boolean processVariable(Element variable, HashSet<String> variablesResolved) {
		boolean processed = true;
		String name = variable.getAttributeValue(ConstantsReport.name);
		System.out.println("variableName " + name);
		boolean deleted = false;
		//si es de tipo system ver si su subreport se elimino
		//esto habria sido valido si la variable en el subreport tuviera el mismo nombre que en el report principal
		/*if (StringUtils.equals(variable.getAttributeValue(ConstantsReport.calculation),ConstantsReport.system)) {
			if (variablesOfSubreportsRemoved.contains(name)) {
				variablesToRemove.add(name);
				deleted = true;
			}
		}*/
		Element initialValueExpression = variable.getChild(ConstantsReport.initialValueExpression, nameSpace);
		if (!deleted && initialValueExpression!=null) {
			//parsear xa ver si tiene algun field eliminado $F{...} o alguna variable eliminada
			//si una variable depende de otra -> recursion
			//System.out.println("initialValueExpression " + ((CDATA)initialValueExpression.getContent().get(0)).getText());
			HashSet<String> variablesUsed = new HashSet<String>();
			if (initialValueExpression.getContent().size()>0) {
				deleted = processVariableExpresion(name, ((CDATA)initialValueExpression.getContent().get(0)).getText(), variablesUsed);
				Iterator it = variablesUsed.iterator();
				while (processed && it.hasNext()) {
					String variableUsed = (String)it.next();
					if (!variablesResolved.contains(variableUsed))
						processed = false;
				}
			}
		}
		Element variableExpression = variable.getChild(ConstantsReport.variableExpression, nameSpace);
		if (!deleted && variableExpression!=null) {
			//System.out.println("variableExpression " + ((CDATA)variableExpression.getContent().get(0)).getText());
			HashSet<String> variablesUsed = new HashSet<String>();
			deleted = processVariableExpresion(name, ((CDATA)variableExpression.getContent().get(0)).getText(), variablesUsed);
			Iterator it = variablesUsed.iterator();
			while (processed && it.hasNext()) {
				String variableUsed = (String)it.next();
				if (!variablesResolved.contains(variableUsed))
					processed = false;
			}
		}
		//procesamiento de grupos
		if (!deleted) {
			String resetGroup = variable.getAttributeValue(ConstantsReport.resetGroup);
			if (resetGroup!=null) {
				//de momento, la variable es correcta pero si se elimina el grupo se elimina esta variable
				HashSet<String> hVariablesDependGroup = variablesDependGroup.get(resetGroup);
				if (hVariablesDependGroup==null) {
					hVariablesDependGroup = new HashSet<String>();
					variablesDependGroup.put(resetGroup, hVariablesDependGroup);
				}
				hVariablesDependGroup.add(name);
			}
			String incrementGroup = variable.getAttributeValue(ConstantsReport.incrementGroup);
			if (incrementGroup!=null) {
				HashSet<String> hVariablesDependGroup = variablesDependGroup.get(incrementGroup);
				if (hVariablesDependGroup==null) {
					hVariablesDependGroup = new HashSet<String>();
					variablesDependGroup.put(incrementGroup, hVariablesDependGroup);
				}
				hVariablesDependGroup.add(name);
			}
		}
		if (processed)
			variablesResolved.add(name);
		//que pasa si una variable depende de otra que depende de un grupo y el grupo se elimina?
		//tendria que almacenar dependencias
		//AMPLIACION xq esto no se da
		//si depende de otra, la otra suele estar agrupada
			//mapa: name_group  variables
			//mapa: variable    variables_implicadas
		
		return processed;
	}

	/**Procesa parámetros del diseño. Un nodo de un parámetro se clonara si no pertenece un subreport 
	 * no clonable y su expresion defaultValueExpression, si está rellena, es correcta de acuerdo al modelo.*/
	private void processParameter(Element parameter) throws SQLException {
		boolean deleted = false;
		String name = parameter.getAttributeValue(ConstantsReport.name);
		if (subreportsRemoved.contains(name))
			deleted = true;
		Element defaultValueExpression = parameter.getChild(ConstantsReport.defaultValueExpression, nameSpace);
		if (defaultValueExpression!=null) {
			if (!deleted)
				deleted = processExpresionWithField(((CDATA)defaultValueExpression.getContent().get(0)).getText());
		}
		if (deleted)
			parametersToRemove.add(parameter.getAttributeValue(ConstantsReport.name));
	}
	
	/**Procesa un grupo del diseño. En principio, un grupo no debe quitarse del diseño, pero se haraa 
	 * si su expresion groupExpression no es correcta de acuerdo al modelo. 
	 * Se procesan sus bandas groupHeader y groupFooter.*/
	private void processGroup(Element group) throws SQLException, DataErrorException {
		String name = group.getAttributeValue(ConstantsReport.name);
		Element groupExpression = group.getChild(ConstantsReport.groupExpression, nameSpace);
		boolean deleted = processExpresionWithFieldVariableParameter(((CDATA)groupExpression.getContent().get(0)).getText());
		//if (!deleted) {
			Element groupHeader = group.getChild(ConstantsReport.groupHeader, nameSpace);
			if (groupHeader!=null) {
				String nameBandHeader = name + "_" + ConstantsReport.groupHeader;
				boolean deletedHeader = processBand(groupHeader.getChild(ConstantsReport.band, nameSpace), nameBandHeader);
				if (deletedHeader) {
					cleanBand(groupHeader);
					bandsToRemove.add(nameBandHeader);
				}
			}
			Element groupFooter = group.getChild(ConstantsReport.groupFooter, nameSpace);
			if (groupFooter!=null) {
				String nameBandFooter = name + "_" + ConstantsReport.groupFooter;
				boolean deletedFooter = processBand(groupFooter.getChild(ConstantsReport.band, nameSpace), nameBandFooter);
				if (deletedFooter) {
					cleanBand(groupFooter);
					bandsToRemove.add(nameBandFooter);
				}
			}
		//}
		if (deleted) {
			groupsToRemove.add(name);
			HashSet<String> variables = variablesDependGroup.get(name);
			if (variables!=null) //TODO para hacer esto de forma correcta deberia haber un preprocesado
								 //x si hay algun campo que dependa de esta variable
								 //no se amplia xq no se deben borrar grupos
				variablesToRemove.addAll(variables);
			
//			Element groupHeader = group.getChild(ConstantsReport.groupHeader, nameSpace);
			if (groupHeader!=null)
				cleanBand(groupHeader.getChild(ConstantsReport.band, nameSpace));
//			Element groupFooter = group.getChild(ConstantsReport.groupFooter, nameSpace);
			if (groupFooter!=null)
				cleanBand(groupFooter.getChild(ConstantsReport.band, nameSpace));
		}
	}
	
	private void cleanBand(Element band) {
		//añadir a los mapas los elementos eliminados en la banda
		System.out.println("limpiando banda");
		Iterator it2 = band.getChildren().iterator();
		while (it2.hasNext()) {
			Element child = (Element)it2.next();
			String nameChild = child.getName();
			System.out.println("añadiendo " + nameChild);
			if (nameChild.equals(ConstantsReport.textField)) {
				Element reportElement = child.getChild(ConstantsReport.reportElement, nameSpace);
				String key = reportElement.getAttributeValue(ConstantsReport.key);
				if (key!=null)
					textFieldsToRemove.add(key);
			} else if (nameChild.equals(ConstantsReport.staticText)) {
				Element reportElement = child.getChild(ConstantsReport.reportElement, nameSpace);
				String key = reportElement.getAttributeValue(ConstantsReport.key);
				if (key!=null)
					staticTextsToRemove.add(key);
			} else if (nameChild.equals(ConstantsReport.frame)) {
				Element reportElement = child.getChild(ConstantsReport.reportElement, nameSpace);
				String key = reportElement.getAttributeValue(ConstantsReport.key);
				if (key!=null)
					framesToRemove.add(key);
			}
		}
	}
	
	/**Procesa los elementos de una banda del diseño. 
	 * La banda se clonara si tiene algun elemento correcto con respecto al modelo.
	 * Almacena en el conjunto allKeys las claves de todos los elementos. Si la clave de un elemento ya 
	 * estaba en este conjunto estamos ante un error de escritura.
	 * Almacena en el conjunto allTextFieldsAndFrames si se trata de un textField o frame.
	 * Trata posibles elementos de tabla si se trata de textField, staticText, line o rectangle.*/
	private boolean processBand(Element band, String nameBand) throws SQLException, DataErrorException {
		boolean deleted = true;
		//if (band!=null) {
			Iterator it = band.getChildren().iterator();
			while (it.hasNext()) {
				Element child = (Element)it.next();
				String nameChild = child.getName();
				if (nameChild.equals(ConstantsReport.textField) || 
						nameChild.equals(ConstantsReport.staticText) || 
						nameChild.equals(ConstantsReport.line) || 
						nameChild.equals(ConstantsReport.rectangle) ||
						nameChild.equals(ConstantsReport.frame) || 
						nameChild.equals(ConstantsReport.subreport) || 
						nameChild.equals(ConstantsReport.image)) {
					Element reportElement = child.getChild(ConstantsReport.reportElement, nameSpace);
					String key = reportElement.getAttributeValue(ConstantsReport.key);
					if (key!=null) {
						if (allKeys.contains(key)) continue;
							//throw new DataErrorException("La clave " + key + " está repetida");
						else
							allKeys.add(key);
						if (nameChild.equals(ConstantsReport.textField) || 
								nameChild.equals(ConstantsReport.frame))
							allTextFieldsAndFrames.add(key);
					}
					if (nameChild.equals(ConstantsReport.textField) || 
							nameChild.equals(ConstantsReport.staticText) || 
							nameChild.equals(ConstantsReport.line) || 
							nameChild.equals(ConstantsReport.rectangle)) {
						//si empieza por table-X@... añadir a Table de esa key
						putElementTable(reportElement, nameBand);
						if (nameChild.equals(ConstantsReport.textField))
							deleted = processTextField(child, nameBand) && deleted;
						else if (nameChild.equals(ConstantsReport.staticText))
							deleted = processStaticText(child, nameBand) && deleted;
					} //else if (nameChild.equals(ConstantsReport.componentElement))
						//deleted = processTable(child) && deleted;
					else if (nameChild.equals(ConstantsReport.frame))
						deleted = processFrame(child, nameBand) && deleted;
					else if (nameChild.equals(ConstantsReport.subreport))
						deleted = processSubreport(child) && deleted;
					else if (nameChild.equals(ConstantsReport.image))
						deleted = false;
				}
			}
		//}
		return deleted;
	}
	
	/**Almacena coordenadas de elementos de tabla. Si contiene separatorTable pertenece a una tabla.*/
	private void putElementTable(Element reportElement, String band) {
		String key = reportElement.getAttributeValue(ConstantsReport.key);
		if (key!=null) {
			if (key.contains(ConstantsReport.separatorTable)) {
				String keyTable = key.substring(0, key.indexOf(ConstantsReport.separatorTable));
				Table table = tablesUsed.get(keyTable);
				if (table==null) {
					table = new Table();
					tablesUsed.put(keyTable, table);
				}
				int left = Integer.parseInt(reportElement.getAttributeValue(ConstantsReport.x));
				int top = Integer.parseInt(reportElement.getAttributeValue(ConstantsReport.y));
				int width = Integer.parseInt(reportElement.getAttributeValue(ConstantsReport.width));
				int height = Integer.parseInt(reportElement.getAttributeValue(ConstantsReport.height));
				
				Coordinates coordinates = new Coordinates(key, band, left, top, width, height);
				table.addElement(coordinates);
			}
		}
	}
	
	/**Procesa textFields del diseño. Un nodo de un textField se clonara si sus expresiones 
	 * (textFieldExpression, printWhenExpression), si están rellenas, son correctas de acuerdo al modelo.*/
	private boolean processTextField(Element textField, String nameBand) throws SQLException {
		boolean deleted = false;
		Element reportElement = textField.getChild(ConstantsReport.reportElement, nameSpace);
		String key = reportElement.getAttributeValue(ConstantsReport.key);
		if (key!=null) {
			Element textFieldExpression = textField.getChild(ConstantsReport.textFieldExpression, nameSpace);
			if (textFieldExpression!=null) {
				deleted = processExpresionWithFieldVariableParameter(((CDATA)textFieldExpression.getContent().get(0)).getText());
				if (deleted)
					textFieldsToRemove.add(key);
			}
			if (!deleted) {
				Element printWhenExpression = reportElement.getChild(ConstantsReport.printWhenExpression, nameSpace);
				if (printWhenExpression!=null) {
					deleted = processExpresionWithFieldVariableParameter(((CDATA)printWhenExpression.getContent().get(0)).getText());
					if (deleted)
						textFieldsToRemove.add(key);
				}
			}
		}
		return deleted;
	}
	
	/**Procesa staticTexts del diseño. Un nodo de un staticText se clonara si su expresion 
	 * printWhenExpression, si está rellena, es correcta de acuerdo al modelo. 
	 * Si empieza por 'label-' no influye en la decision de eliminar un superior (frame o banda), 
	 * por lo que aunque se almacene en staticTextsToRemove devuelve true.*/
	private boolean processStaticText(Element staticText, String nameBand) throws SQLException {
		boolean deleted = false;
		 //si no empieza por label-
		Element reportElement = staticText.getChild(ConstantsReport.reportElement, nameSpace);
		String key = reportElement.getAttributeValue(ConstantsReport.key);
		if (key!=null) {
			Element printWhenExpression = reportElement.getChild(ConstantsReport.printWhenExpression, nameSpace);
			if (printWhenExpression!=null) {
				deleted = processExpresionWithFieldVariableParameter(((CDATA)printWhenExpression.getContent().get(0)).getText());
				if (deleted)
					staticTextsToRemove.add(key);
			}
			if (key.contains(ConstantsReport.separatorTable))
				key = key.substring(key.indexOf(ConstantsReport.separatorTable),key.length());
			if (key.startsWith("label-")) //si empieza por label- no influye en la decision de eliminar un frame o banda
				deleted = true;
		}
		return deleted;
	}
	/*private boolean processTable(Element table) throws SQLException {
		boolean deleted = true;
		Element jrTable = table.getChild(ConstantsReport.jr_table, nameSpace);
		Iterator it = jrTable.getChildren().iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			if (child.getName().equals(ConstantsReport.jr_column)) {
				Element jrColumn = child;
				deleted = processJrColumn(jrColumn) && deleted;
			} else if (child.getName().equals(ConstantsReport.jr_columnGroup)) {
				Element jrColumnGroup = child;
				Iterator it2 = jrColumnGroup.getChildren().iterator();
				while (it2.hasNext()) {
					Element child2 = (Element)it2.next();
					if (child2.getName().equals(ConstantsReport.jr_groupHeader) || child2.getName().equals(ConstantsReport.jr_groupFooter)) {
						Element jrColumnHeader = child2;
						Element cell = jrColumnHeader.getChild(ConstantsReport.jr_cell, nameSpace);
						deleted = processBand(cell) && deleted;
					} else if (child2.getName().equals(ConstantsReport.jr_column)) {
						Element jrColumn = child2;
						deleted = processJrColumn(jrColumn) && deleted;
					}
				}
			}
		}
		
		if (deleted) {
			String name = table.getChild(ConstantsReport.reportElement, nameSpace).getAttributeValue(ConstantsReport.key);
			tablesToRemove.add(name);
		}
		return deleted;
	}
	private boolean processJrColumn(Element jrColumn) throws SQLException {
		boolean deleted = false;
		Iterator it2 = jrColumn.getChildren().iterator();
		while (it2.hasNext()) {
			Element child = (Element)it2.next();
			if (child.getName().equals(ConstantsReport.jr_groupHeader) || child.getName().equals(ConstantsReport.jr_groupFooter)) {
				Element cell = child.getChild(ConstantsReport.jr_cell, nameSpace);
				deleted = processBand(cell) && deleted;
			} else {			//if (child.getName().equals(ConstantsReport.jr_columnHeader))
				Element band = child;
				deleted = processBand(band) && deleted;
			}
		}
		return deleted;
	}*/
	
	/**Procesa frames del diseño. Un nodo de un frame se clonara si su expresion 
	 * printWhenExpression, si está rellena, es correcta de acuerdo al modelo y si los elementos 
	 * que hay bajo el son correctos con respecto al modelo.*/
	private boolean processFrame(Element frame, String nameBand) throws SQLException, DataErrorException {
		Element reportElement = frame.getChild(ConstantsReport.reportElement, nameSpace);
		boolean deleted = false;
		Element printWhenExpression = reportElement.getChild(ConstantsReport.printWhenExpression, nameSpace);
		if (printWhenExpression!=null)
			deleted = processExpresionWithFieldVariableParameter(((CDATA)printWhenExpression.getContent().get(0)).getText());
		//if (!deleted) {
			boolean deleteBand = processBand(frame, nameBand);
			if (deleteBand)
				cleanBand(frame);
		//}
		if (deleted) {
			String key = reportElement.getAttributeValue(ConstantsReport.key);
			if (key!=null)
				framesToRemove.add(key);
			cleanBand(frame);
		}
		return deleted;
	}
	
	/**Procesa subreports del diseño. Un nodo de un subreport se clonara si no está en el conjunto 
	 * subreportsRemoved, lo cual indicara que este subreport no tenia elementos.*/
	private boolean processSubreport(Element subreport) throws SQLException {
		boolean deleted = false;
		String key = subreport.getChild(ConstantsReport.reportElement, nameSpace).getAttributeValue(ConstantsReport.key);
		if (key!=null && subreportsRemoved.contains(key))
			deleted = true;
		return deleted;
	}
	
	/**metodo que reparte de forma proporcional el espacio obtenido de haber eliminado campos 
	 * en una tabla. Para ello, usa el metodo que los alinea a la izquierda sin reducir la línea 
	 * separatoria entre filas, posteriormente itera por cada campo clonable ampliando su parte 
	 * proporcional y desplazando los que están a su derecha.*/
	private void processNewCoordinatesExpanding() {
		//iterar x tablesUsed y ver si el elemento ha sido borrado
		//repartir el espacio de forma proporcional
		//1er ver cuanto espacio queda
		//2 repartirlo
		Iterator it = tablesUsed.keySet().iterator();
		while (it.hasNext()) {
			String keyTable = (String)it.next();
			HashMap<String,Integer> sizeElemDeleted = new HashMap<String, Integer>();
			int widthTable = subProcessNewCoordinatesMoving(keyTable, sizeElemDeleted, false);
			
			System.out.println("*****Tras primer procesamiento");
			Iterator itCM = newCoordinatesInTable.keySet().iterator();
			while (itCM.hasNext()) {
				String key = (String)itCM.next();
				Coordinates coord = newCoordinatesInTable.get(key);
				System.out.println("newCoordinatesInTable->field " + key + ", coord " + coord);
			}
			System.out.println("*****");
			
			//itero de nuevo, ahora sabiendo cuanto tengo q repartir, sabiendo el ancho total de la tabla 
			//y sabiendo que los campos validos estan pegados a la izquierda
			Table table = tablesUsed.get(keyTable);
			HashSet<Coordinates> hCoord = table.getHElements();
			Iterator it2 = hCoord.iterator();
			while (it2.hasNext()) {
				Coordinates coord = (Coordinates)it2.next();
				String key = coord.getKey();
				boolean delete = false;
				if (textFieldsToRemove.contains(key)) {
					delete = true;
				} else if (staticTextsToRemove.contains(key)) {
					delete = true;
				}
				if (!delete) {
					//lo amplio
					String keyElemDeleted = coord.getBand() + "#" + coord.getTop();
					Integer sumWidthDeleted = sizeElemDeleted.get(keyElemDeleted);
					if(sumWidthDeleted!=null) { //caso en el que no es una linea
						int width = coord.getWidth(); //se coge el ancho del original xq en la funcion mover no se ha modificado el ancho
						float restoTabla = widthTable-sumWidthDeleted;
						float f = sumWidthDeleted.floatValue()/restoTabla*(new Float(width).floatValue());
						int addWidth = Math.round(f);
						Coordinates newCoord = newCoordinatesInTable.get(coord.getKey());
						if (newCoord==null) {
							newCoord = new Coordinates(coord.getKey(), coord.getBand(), coord.getLeft(), coord.getTop(), coord.getWidth() + addWidth, coord.getHeight());
							newCoordinatesInTable.put(coord.getKey(), newCoord);
						} else {
							newCoord.setWidth(newCoord.getWidth() + addWidth);
						}
						//desplazo hacia la derecha los otros campos
						HashSet<Coordinates> sameTop = table.getByTop(coord.getBand(), coord.getTop());
						Iterator it3 = sameTop.iterator();
						while (it3.hasNext()) {
							Coordinates coordTop = (Coordinates)it3.next();
							if(coordTop.getLeft()>coord.getLeft()) {
								//desplazar
								Coordinates newCoordTop = newCoordinatesInTable.get(coordTop.getKey());
								if (newCoordTop==null) {
									newCoordTop = new Coordinates(coordTop.getKey(), coordTop.getBand(), coordTop.getLeft() + addWidth, coordTop.getTop(), coordTop.getWidth(), coordTop.getHeight());
									newCoordinatesInTable.put(coordTop.getKey(), newCoordTop);
								} else
									newCoordTop.setLeft(newCoordTop.getLeft() + addWidth);
							}
						}
					}
				}
			}
		}
	}

	/**metodo que usa el metodo que alinea los campos a la izquierda.*/
	private void processNewCoordinatesMoving() {
		//iterar x tablesUsed y ver si el elemento ha sido borrado
		Iterator it = tablesUsed.keySet().iterator();
		while (it.hasNext()) {
			String keyTable = (String)it.next();
			HashMap<String,Integer> sizeElemDeleted = new HashMap<String, Integer>();
			subProcessNewCoordinatesMoving(keyTable, sizeElemDeleted, true);
		}
	}
	/**metodo que desplaza a la izquierda los campos validos haciendo más pequeña la tabla si 
	 * hay algun elemento incorrecto de acuerdo al modelo. La línea separatoria entre filas la 
	 * reduce si el parámetro de entrada moveLines es true.*/
	private int subProcessNewCoordinatesMoving(String keyTable, HashMap<String,Integer> sizeElemDeleted, 
			boolean moveLines) {
		Table table = tablesUsed.get(keyTable);
		//estoy en una de las tablas del informe
		HashSet<Coordinates> hCoord = table.getHElements();
		int widthRemoved = 0;
		int widthTable = 0;
		Iterator it2 = hCoord.iterator();
		while (it2.hasNext()) {
			Coordinates coord = (Coordinates)it2.next();
			int width = coord.getWidth();
			int left = coord.getLeft();
			String key = coord.getKey();
			//System.out.println("key " + key);
			boolean desplazar = false;
			int widthTableTmp = left+width;
			if (widthTableTmp>widthTable)
				widthTable = widthTableTmp;
			if (textFieldsToRemove.contains(key)) {
				desplazar = true;
				widthRemoved = widthRemoved+width;
			} else if (staticTextsToRemove.contains(key))
				desplazar = true;
			else {
				//ver si es un staticText asociado a un elemento eliminado
				String smallKey = key;
				if (smallKey.contains(ConstantsReport.separatorTable))
					smallKey = key.substring(key.indexOf(ConstantsReport.separatorTable)+1,key.length());
				if (smallKey.startsWith("label-")) {
					//se elimina si su textField o frame asociado se ha eliminado, 
					String keyContent = keyTable + ConstantsReport.separatorTable + smallKey.substring(6,smallKey.length());
					if (textFieldsToRemove.contains(keyContent) || framesToRemove.contains(keyContent)) {
						staticTextsToRemove.add(key);
						desplazar = true;
					}
				}
			}
			
			if (desplazar) {
				//sumo el ancho del que elimino
				String keyElemDeleted = coord.getBand() + "#" + coord.getTop();
				Integer sumWidthDeleted = sizeElemDeleted.get(keyElemDeleted);
				if (sumWidthDeleted==null) sumWidthDeleted = 0;
				sizeElemDeleted.put(keyElemDeleted, sumWidthDeleted+width);
				
				//busco los que estan en la misma linea xa desplazarlos
				HashSet<Coordinates> sameTop = table.getByTop(coord.getBand(), coord.getTop());
				Iterator it3 = sameTop.iterator();
				while (it3.hasNext()) {
					Coordinates coordTop = (Coordinates)it3.next();
					if(coordTop.getLeft()>left) {
						String newKey = coordTop.getKey();
						//System.out.println("newKey " + newKey);
						Coordinates coordSaved = newCoordinatesInTable.get(newKey);
						//desplazar
						if (coordSaved==null) {
							Coordinates newCoord = new Coordinates(newKey, coord.getBand(), coordTop.getLeft()-width, coord.getTop(), coordTop.getWidth(), coordTop.getHeight());
							newCoordinatesInTable.put(newKey, newCoord);
						} else {
							coordSaved.setLeft(coordSaved.getLeft()-width);
						}
						//Si tuviera lineas verticales las desplazaria si tienen el mismo top que los campos
					}
				}
			}
		}
		if (moveLines && widthRemoved>0) {
			//acortar lineas horizontales(height=1)
			it2 = hCoord.iterator();
			while (it2.hasNext()) {
				Coordinates coord = (Coordinates)it2.next();
				if (coord.getHeight()==1) {
					Coordinates newCoord = new Coordinates(coord.getKey(), coord.getBand(), coord.getLeft(), coord.getTop(), coord.getWidth()-widthRemoved, 1);
					newCoordinatesInTable.put(coord.getKey(), newCoord);
				}
			}
		}
		return widthTable;
	}
	
	/**metodos auxiliares de procesado de expresiones.*/
	/**Procesa la expresion de una variable buscando campos y variables.
	 * Almacena las variables usadas.*/
	private boolean processVariableExpresion(String name, String expresion,
			HashSet<String> variablesUsed) {
		//buscamos los fields
		boolean deleted = processExpresionWithField(expresion);
		if (!deleted) //buscamos las variables
			deleted = processExpresionWithVariable(name, expresion, variablesUsed);
		if (deleted)
			variablesToRemove.add(name);
		return deleted;
	}
	private boolean processExpresionWithVariable(String name, String expresion, HashSet<String> variablesUsed) {
		//buscamos las variables
		boolean deleted = false;
		boolean salir = false;
		int fin = 0;
		while (!salir) {
			int inicio = expresion.indexOf("$V{",fin);
			if (inicio!=-1) {
				if (fin!=0) fin++;
				fin = expresion.indexOf("}",inicio);
				String variableUsed = expresion.substring(inicio+3, fin);
				if (!variableUsed.equals(name)) {
					if (variablesToRemove.contains(variableUsed)) {
						deleted = true;
						salir = true;
					} else
						variablesUsed.add(variableUsed);
				}
			} else
				salir = true;
		}
		return deleted;
	}
	
	/**Procesa una expresion buscando campos.*/
	private boolean processExpresionWithField(String expresion) {
		return processExpresionWith(expresion, fieldsToRemove, "F");
	}
	/**Procesa una expresion buscando variables.*/
	private boolean processExpresionWithVariable(String expresion) {
		return processExpresionWith(expresion, variablesToRemove, "V");
	}
	/**Procesa una expresion buscando parámetros.*/
	private boolean processExpresionWithParameter(String expresion) {
		return processExpresionWith(expresion, parametersToRemove, "P");
	}
	/**metodo generico que procesa una expresion.*/
	private boolean processExpresionWith(String expresion, HashSet<String> setToRemove, String charIni) {
		//buscamos los fields
		boolean deleted = false;
		boolean salir = false;
		int fin = 0;
		while (!salir) {
			int inicio = expresion.indexOf("$" + charIni + "{",fin);
			if (inicio!=-1) {
				if (fin!=0) fin++;
				fin = expresion.indexOf("}",inicio);
				String fieldUsed = expresion.substring(inicio+3, fin);
				if (setToRemove.contains(fieldUsed)) {
					deleted = true;
					salir = true;
				}
			} else
				salir = true;
		}
		return deleted;
	}

	/**Procesa una expresion buscando campos, variables y parámetros.*/
	private boolean processExpresionWithFieldVariableParameter(String expresion) {
		//buscamos los fields
		boolean deleted = processExpresionWithField(expresion);
		if (!deleted) //buscamos las variables
			deleted = processExpresionWithVariable(expresion);
		if (!deleted) //buscamos los parametros
			deleted = processExpresionWithParameter(expresion);
		return deleted;
	}
	
}
