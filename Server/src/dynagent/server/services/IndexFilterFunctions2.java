package dynagent.server.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

import dynagent.common.Constants;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.utils.Auxiliar;
import dynagent.server.dbmap.ClassInfo;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.dbmap.Table;
import dynagent.server.dbmap.TableColumn;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;

public class IndexFilterFunctions2 {

	public static int NOTHING = 0;
	public static int RESTORE = 1;
	public static int SEARCH_LAST = 2;

	private FactoryConnectionDB fcdb;
	private DataBaseMap dataBaseMap;

	/**
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a la base de datos.
	 * @param dataBaseMap
	 *            Mapa de todo el modelo y la base de datos.
	 */
	public IndexFilterFunctions2(FactoryConnectionDB fcdb, DataBaseMap dataBaseMap) {
		this.fcdb = fcdb;
		this.dataBaseMap = dataBaseMap;
	}

	/**
	 * Busca entre los filtros entregados cual es el que se tiene que aplicar para el elemento que se nos ha pasado por
	 * parámetro.
	 * 
	 * La logica que se sigue es la siguiente:
	 * <ul>
	 * <li><i>En debug:</i> Si la acción es nueva y el campo sobre el que se aplica el índice no aparece en el objeto
	 * recibido, se da un aviso de error y se sale del metodo.</li>
	 * <li>Se busca si el índice tiene una propiedad por la que se filtra:
	 * <ul>
	 * <li>Si no tiene ningun campo por el que se filtra, se trata de un índice generico que se aplica a todos los
	 * objetos de la clase</li>
	 * <li>Si tiene un campo por el que se filtra:
	 * <ul>
	 * <li>Se busca dicho campo en el objeto, si no aparece, saltamos al siguiente índice.</li>
	 * <li>Si el campo aparece en el objeto, se comprueba el valor:
	 * <ul>
	 * <li>Si coincide con el valor que tiene el índice para filtrar, se pone el como el filtro se continua evaluando el
	 * filtro</li>
	 * <li>Si no coincide con el valor del filtro, se pasa al siguiente índice.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * </li>
	 * </ul>
	 * <li>Se mira si el índice filtra por la empresa:
	 * <ul>
	 * <li>Si no filtra por ninguna empresa:
	 * <ul>
	 * <li>Si todavia no se habia encontrado ningun índice, se pone como el índice más adecuado por el momento.</li>
	 * <li>Si el filtro que se habia encontrado no filtraba y este si, este es el más adecuado.</li>
	 * <li>Si el filtro que se tenia hasta ahora filtraba por empresa, se mantiene el que se tenia antes de este.</li>
	 * </ul>
	 * </li>
	 * <li>Si se filtra por una empresa:
	 * <ul>
	 * <li>Si el indice que se tenia hasta ahora no filtraba por propiedad y este si, se pone el índice actual como
	 * filtro más adecuado.</li>
	 * <li>Si el indice que se tenia hasta ahora filtraba por propiedad y este no, se pasa al siguiente índice.</li>
	 * <li><i>En debug:</i> Si se tenia un índice que filtraba por propiedad y empresa que era adecuado y este cumple
	 * las mismas condiciones, los índices son ambiguos y se trata de un error.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * </li>
	 * </ul>
	 * 
	 * @param objectElement
	 *            Elemento que contiene la informacion del objeto.
	 * @param elementsByIdNode
	 *            Mapa de los elementos que aparecen en el XML indexados por su id_node por si en el objectElement
	 *            aparecen objectProperties como vinculos.
	 * @param filters
	 *            Filtros aplicables a la clase a la que pertenece el objectElement.
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a la base de datos.
	 * @return Filtro que se ajusta más al objeto dado o <code>null</code> si no hay ningun filtro que se pueda aplicar.
	 * @throws DataConversionException
	 *             Si hay un error el el formato del valor de un atributo del XML
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws NoSuchColumnException 
	 */
	@SuppressWarnings("unchecked")
	public IndexFilter2 getApplicableFilter(Element objectElement, Map<Integer, Element> elementsByIdNode,
			List<IndexFilter2> filters, boolean canBeInDB, boolean getOld) throws DataErrorException, DataConversionException, SQLException, NamingException, NoSuchColumnException {
		IndexFilter2 mostSuitableFilter = null;
		boolean myBusinessFound = false;
		boolean filterFieldFound = false;
		boolean myBusinessNotUsable = false;
		Integer myBusinessTableId = null;

		/*Attribute actionAttribute = objectElement.getAttribute(XMLConstants.ATTRIBUTE_ACTION);
		String actionValue = actionAttribute.getValue();
		if (!actionValue.equals(XMLConstants.ACTION_NEW)) {
			throw new DataErrorException("Este metodo solo se puede aplicar a elementos que tienen como acción "
					+ XMLConstants.ACTION_NEW);
		}*/

		for (IndexFilter2 currentIndex : filters) {
			//System.out.println("currentIndex " + currentIndex);
			if ((myBusinessFound || myBusinessNotUsable) && filterFieldFound) {
				// Hemos encontrado el filtro más especifico que podemos encontrar. Dejamos de buscar.
				break;
			}
			boolean filterFieldFoundInCurrentIndex = false;

			/* ESTA COMPROBACION SERIA VALIDA SI ESTE METODO SOLO SE USARA EN CREACION
			 * String indexedField = currentIndex.getIndexedField();
			Attribute indexedFieldAttribute = null;
			if (getOld)
				indexedFieldAttribute = objectElement.getAttribute(indexedField + XMLConstants.OLD_PROPERTY);
			if (indexedFieldAttribute==null)
				indexedFieldAttribute = objectElement.getAttribute(indexedField);
			if (DatabaseManager.debugMode && indexedFieldAttribute == null) {
				System.err.println("El campo " + indexedField
						+ " al que se le aplica un índice no aparece en un objeto que se está creando.");
			}*/

			String filterField = currentIndex.getFilterField();
			if (filterField != null) {
				String filterFieldAttributeValue = null;
				if (getOld) {
					Attribute filterFieldAttribute = objectElement.getAttribute(filterField + XMLConstants.OLD_PROPERTY);
					if (filterFieldAttribute == null) {
						//obtenerlo de base de datos
						filterFieldAttributeValue = searchPropertyValueInDb(filterField, objectElement);
						if (filterFieldAttributeValue == null) {
							// El valor del filtro es una dataproperty sin valor. De momento,
							// consideramos que el filtro no es aplicable.
							continue;
						}
					} else {
						filterFieldAttributeValue = filterFieldAttribute.getValue();
					}
				} else {
					Attribute filterFieldAttribute = objectElement.getAttribute(filterField);
					if (filterFieldAttribute == null) {
						if (canBeInDB) {
							//obtenerlo de base de datos
							filterFieldAttributeValue = searchPropertyValueInDb(filterField, objectElement);
							if (filterFieldAttributeValue == null) {
								// El valor del filtro es una dataproperty sin valor. De momento,
								// consideramos que el filtro no es aplicable.
								continue;
							}
						} else {
							// El valor del filtro es una dataproperty no especificada o es una objectproperty. De momento,
							// consideramos que el filtro no es aplicable.
							continue;
						}
					} else {
						filterFieldAttributeValue = filterFieldAttribute.getValue();
					}
				}
				if (filterFieldAttributeValue.equals(currentIndex.getFilterValue())) {
					filterFieldFoundInCurrentIndex = true;
				} else {
					// El objeto tenia el campo pero el valor no coincidia.
					continue;
				}
			} else if (filterField == null && filterFieldFound) {
				// En una iteración anterior ya habiamos encontrado un filtro más especifico que el actual, por lo cual
				// saltamos al siguiente índice.
				continue;
			}

			// Ahora tenemos que comprobar si el índice filtra por empresa para hacer las comprobaciones pertinentes.
			if (currentIndex.getMyBusiness() != null) {
				if (myBusinessNotUsable) {
					// Se ha hecho ya una busqueda de la empresa relacionada con el objeto representado por el XML y se
					// ha detectado que o no tiene una empresa vinculada o dicha empresa se está creando en este momento
					// con lo cual no puede aparecer como filtro para un índice todavia, con lo cual no tiene sentido
					// mirar un índice que filtra por mi empresa.
					continue;
				}
				// Buscamos si tiene una referencia a un objeto de mi empresa.
				if (myBusinessTableId == null) {
					Element myBusinessElement = objectElement.getChild(Constants.CLS_MI_EMPRESA);
					if (getOld) {
						if (myBusinessElement != null) {
							for (Element child : new LinkedList<Element>(objectElement.getChildren(Constants.CLS_MI_EMPRESA))) {
								myBusinessElement = child;
								Attribute myBusinessIdNodeAttribute = myBusinessElement.getAttribute(XMLConstants.ATTRIBUTE_IDNODE);
								myBusinessElement = elementsByIdNode.get(myBusinessIdNodeAttribute.getIntValue());
								Attribute myBussinesTableIdAttribute = myBusinessElement.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);
								
								if (myBussinesTableIdAttribute == null) {
									throw new DataErrorException(
											"Todos los elementos que representan a una clase tienen que venir especificados con un tableId. Elemento implicito: "
													+ myBusinessElement);
								}
								Attribute propertyAttribute = myBusinessElement.getAttribute(XMLConstants.ATTRIBUTE_PROPERTYm);
								String propertyValue = propertyAttribute.getValue();
								
								boolean get = false;
								if (propertyValue.equals(Constants.prop_mi_empresa + XMLConstants.OLD_PROPERTY)) {
									get = true;
								} else {
									Attribute actionAttribute = myBusinessElement.getAttribute(XMLConstants.ATTRIBUTE_ACTION);
									String actionValue = actionAttribute.getValue();
									if (actionValue.equals(XMLConstants.ACTION_DEL))
										get = true;
								}
								if (get) {
									myBusinessTableId = myBussinesTableIdAttribute.getIntValue();
									break;
								}
							}
						}
						if (myBusinessTableId==null) {
							//obtenerlo de base de datos
							myBusinessTableId = getBusinessFromDB(objectElement);
							if (myBusinessTableId==null) {
								// Este índice no es aplicable porque el elemento no tiene declarada una empresa.
								myBusinessNotUsable = true;
								continue;
							}
						}
					} else {
						if (myBusinessElement == null) {
							if (canBeInDB) {
								//obtenerlo de base de datos
								myBusinessTableId = getBusinessFromDB(objectElement);
								if (myBusinessTableId==null) {
									// Este índice no es aplicable porque el elemento no tiene declarada una empresa.
									myBusinessNotUsable = true;
									continue;
								}
							} else {
								// Este índice no es aplicable porque el elemento no tiene declarada una empresa.
								myBusinessNotUsable = true;
								continue;
							}
						}
						for (Element child : new LinkedList<Element>(objectElement.getChildren(Constants.CLS_MI_EMPRESA))) {
							myBusinessElement = child;
							Attribute myBusinessIdNodeAttribute = myBusinessElement.getAttribute(XMLConstants.ATTRIBUTE_IDNODE);
							if(myBusinessIdNodeAttribute==null) myBusinessIdNodeAttribute = myBusinessElement.getAttribute(XMLConstants.ATTRIBUTE_REFNODE);
							myBusinessElement = elementsByIdNode.get(myBusinessIdNodeAttribute.getIntValue());
							Attribute myBussinesTableIdAttribute = myBusinessElement.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);
							
							if (myBussinesTableIdAttribute == null) {
								throw new DataErrorException(
										"Todos los elementos que representan a una clase tienen que venir especificados con un tableId. Elemento implicito: "
												+ myBusinessElement);
							}
							Attribute actionAttribute = myBusinessElement.getAttribute(XMLConstants.ATTRIBUTE_ACTION);
							String actionValue = null;
							if (actionAttribute!=null)
								actionValue = actionAttribute.getValue();
							Attribute propertyAttribute = myBusinessElement.getAttribute(XMLConstants.ATTRIBUTE_PROPERTYm);
							String propertyValue = propertyAttribute.getValue();
							if (propertyValue.equals(Constants.prop_mi_empresa) && 
									(actionValue==null || actionValue.equals(XMLConstants.ACTION_NEW) || actionValue.equals(XMLConstants.ACTION_SET))) {
								myBusinessTableId = myBussinesTableIdAttribute.getIntValue();
								break;
							}
						}
						if (myBusinessTableId==null || myBusinessTableId < 0) {
							// El elemento no tiene mi empresa o 
							// la empresa se está creando en este momento, con lo cual no es usable.
							myBusinessNotUsable = true;
							continue;
						}
					}
				}
				if (myBusinessTableId.equals(currentIndex.getMyBusiness())) {
					myBusinessFound = true;
					filterFieldFound = filterFieldFoundInCurrentIndex;
					mostSuitableFilter = currentIndex;
				}
			} else {
				if (mostSuitableFilter == null) {
					// Si todavia no se ha encontrado ningun índice, nos ahorramos más comprobaciones y establecemos el
					// actual como índice más adecuado por el momento.
					mostSuitableFilter = currentIndex;
					filterFieldFound = filterFieldFoundInCurrentIndex;
					continue;
				}

				if (myBusinessFound) {
					// Si el índice que se ha escogido ya, filtra por empresa, este índice aunque encaja, es más
					// generico y no lo queremos.
					continue;
				}
				if (DatabaseManager.debugMode && filterFieldFound && filterFieldFoundInCurrentIndex) {
					System.err.println("Existe ambiguedad entre los filtros de los índices para la clase="
							+ currentIndex.getDomain()
							+ " ya que se puede aplicar más de un indice con filtrado por property.");
					continue;
				}
				if (filterFieldFoundInCurrentIndex) {
					// El filtro actual es más especifico que el que hubiera hasta el momento porque filtra por
					// propiedad.
					filterFieldFound = true;
					mostSuitableFilter = currentIndex;
				} else if (DatabaseManager.debugMode && !filterFieldFoundInCurrentIndex && !filterFieldFound) {
					System.err.println("Existe más de un filtro generico para la clase=" + currentIndex.getDomain());
				}
			}
		}

		return mostSuitableFilter;
	}

	/**
	 * Devuelve un booleano indicando si un idto está o no indexado.
	 */
	public boolean isIndex(int idto) throws SQLException, NamingException {
		boolean is = false;
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String sqlIsIndex = "SELECT * FROM " + gSQL.getCharacterBegin() + Constants.CLS_INDICE.toLowerCase() + gSQL.getCharacterEnd() + 
			" WHERE dominio=(select "+gSQL.getCharacterBegin()+"tableId"+gSQL.getCharacterEnd()+" from clase where id="+idto+")";
		//System.out.println("sqlIsIndex " + sqlIsIndex);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlIsIndex);
			if (rs.next()) {
				is = true;
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con != null)
				fcdb.close(con);
		}
		return is;
	}
	
	/**
	 * Devuelve un mapa con todos los filtros que son aplicables a los idtos indicados
	 * 
	 * @param usedIdtos
	 *            Conjunto de los idtos que son usados en el XML. De esta forma se restringe la busqueda y solo se saca
	 *            la informacion justa de la base de datos.
	 * 
	 * @return
	 * @throws NamingException
	 *             Si hay algun error en la comunicación con la base de datos.
	 * @throws SQLException
	 *             Si hay algun error en la comunicación con la base de datos.
	 */
	public Map<Integer, List<IndexFilter2>> getIndexsByIdto(Set<Integer> usedIdtos) throws SQLException,
			NamingException {
		Map<Integer, List<IndexFilter2>> filters = new Hashtable<Integer, List<IndexFilter2>>();
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());

		String sqlGetIndexs = "SELECT c.id as dominioIdto, c.rdn as dominio, pd.rdn as campo_filtro, form.valor_filtro, " +
				"case when pd3.rdn is not null then pd3.rdn else po.rdn end as campo_en_prefijo, " + 
				"form.prefijo, form.sufijo, form.inicio_contador, " + 
				"form." + generateSQL.getCharacterBegin() + "último_prefijo_temporal" + generateSQL.getCharacterEnd() + ", " +
				"form." + generateSQL.getCharacterBegin() + "máscara_prefijo_temporal" + generateSQL.getCharacterEnd() + ", pd2.rdn as campo_en_prefijo_temporal, " + 
				"form." + generateSQL.getCharacterBegin() + "contador_año" + generateSQL.getCharacterEnd() + ", form.mi_empresa, me.rdn as rdn_mi_empresa, " + 
				"form." + generateSQL.getCharacterBegin() + Table.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd() + ", " + 
				"form." + generateSQL.getCharacterBegin() + "dígitos_mínimos" + generateSQL.getCharacterEnd() + 
				" FROM " + generateSQL.getCharacterBegin() + Constants.CLS_INDICE.toLowerCase() + generateSQL.getCharacterEnd() + " as form" + 
				" LEFT JOIN mi_empresa as me on(form.mi_empresa=me." + generateSQL.getCharacterBegin() + Table.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd() + ")" + 
				" INNER JOIN clase as c on(form.dominio=c." + generateSQL.getCharacterBegin() + Table.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd() + ")" + 
				" LEFT JOIN propiedad_dato as pd on(form.campo_filtro=pd." + generateSQL.getCharacterBegin() + Table.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd() + ")" + 
				" LEFT JOIN propiedad_dato as pd2 on(form.campo_en_prefijo_temporal=pd2." + generateSQL.getCharacterBegin() + Table.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd() + ")" + 
				" LEFT JOIN propiedad_dato as pd3 on(form." + generateSQL.getCharacterBegin() + "campo_en_prefijoPROPIEDAD_DATO" + generateSQL.getCharacterEnd() + "=pd3." + generateSQL.getCharacterBegin() + Table.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd() + ") " + 
				" LEFT JOIN propiedad_objeto as po on(form." + generateSQL.getCharacterBegin() + "campo_en_prefijoPROPIEDAD_OBJETO" + generateSQL.getCharacterEnd() + "=po." + generateSQL.getCharacterBegin() + Table.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd() + ")" +  
				" WHERE dominio IN (" +
					"select " + generateSQL.getCharacterBegin() + "tableId" + generateSQL.getCharacterEnd() +
					" from clase where id IN(" + Auxiliar.setToString(usedIdtos, ",") + ")";
		sqlGetIndexs += ") ORDER BY dominio;";

		//System.out.println("sqlGetIndexs " + sqlGetIndexs);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlGetIndexs);
			String domain = "";
			List<IndexFilter2> filtersForCurrentDomain = null;
			while (rs.next()) {
				Integer idtoCurrentDomain = rs.getInt(1);
				String currentDomain = rs.getString(2);
				if (!domain.equals(currentDomain)) {
					// Si el dominio es distinto del actual, guardamos todos los filtros que hemos ido acumulando para
					// la clase actual, creamos una nueva lista de filtros vacía y actualizamos el dominio para que se
					// acumulen los nuevos filtros.
					filtersForCurrentDomain = new LinkedList<IndexFilter2>();
					filters.put(idtoCurrentDomain, filtersForCurrentDomain);
					domain = currentDomain;
				}

				String filterField = rs.getString(3);
				String filterValue = rs.getString(4);
				String prefixField = rs.getString(5);
				String prefixValue = rs.getString(6);
				String sufixValue = rs.getString(7);
				Integer indexValue = rs.getInt(8);
				if (rs.wasNull())
					indexValue = null;
				String lastTemporalPrefix = rs.getString(9);
				String temporalPrefixMask = rs.getString(10);
				String temporalPrefixField = rs.getString(11);
				Integer yearCount = null;
				String yearCountStr = rs.getString(12);
				if (!rs.wasNull())
					yearCount = Integer.parseInt(yearCountStr);
				Integer myBusiness = rs.getInt(13);
				if (rs.wasNull())
					myBusiness = null;
				String rdnMyBusiness = rs.getString(14);
				Integer tableId = rs.getInt(15);
				Integer minDigits = rs.getInt(16);
				if (rs.wasNull())
					minDigits = null;
				filtersForCurrentDomain.add(new IndexFilter2(tableId, filterValue, indexValue,
						temporalPrefixField, temporalPrefixMask, prefixField, prefixValue, sufixValue, yearCount,
						lastTemporalPrefix, filterField, currentDomain, myBusiness, rdnMyBusiness, minDigits));
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con != null)
				fcdb.close(con);
		}

		for (Integer idtoKey : filters.keySet()) {
			List<IndexFilter2> lFilter = filters.get(idtoKey);
			for (IndexFilter2 filter2 : lFilter) {
				System.out.println("idto " + idtoKey + ", filter " + filter2);
			}
		}
		
		return filters;
	}
	
	public String searchPropertyValueInDb(String field, Element element) throws DataErrorException, SQLException, NamingException, DataConversionException {
		Attribute tableIdAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);
		Integer tableId = tableIdAttribute.getIntValue();

		Attribute idtoAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);
		Integer idtoValue = idtoAttribute.getIntValue();
		
		return searchPropertyValueInDb(field, idtoValue, tableId);
	}

	/**
	 * Busca el tableId de la empresa teniendo solo informacion de un objeto que está relacionado con la tabla
	 * mi_empresa.
	 * 
	 * @param element
	 *            Elemento que representa al objeto.
	 * @return tableId de la empresa o <code>null</code> si no se ha encontrado dicho identificador o no está
	 *         relacionado con ninguna empresa.
	 * @throws DataConversionException
	 *             Si un atributo tiene un formato incorrecto para su valor.
	 * @throws DataErrorException
	 *             Si hay algun error en los datos del XML.
	 * @throws NoSuchColumnException
	 *             Si se pregunta a una tabla por una columna que no contiene.
	 * @throws NamingException
	 * @throws SQLException
	 */
	private Integer getBusinessFromDB(Element element) throws DataConversionException, DataErrorException,
			SQLException, NamingException, NoSuchColumnException {
		Integer result = null;
		String sql = constructMyBusinessQuery(element);

		if (sql == null) {
			throw new DataErrorException(DataErrorException.ERROR_DATA, "No se ha podido encontrar la property "
					+ Constants.prop_mi_empresa + " para la clase " + element.getName());
		}

		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);

			if (rs.next()){
				result = rs.getInt(1);
				if (rs.wasNull()) {
					result = null;
				}
			}

		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con != null)
				fcdb.close(con);
		}

		return result;
	}

	/**
	 * Construye, a partir de un elemento, la consulta que obtiene el TableId de la empresa relacionada con el elemento
	 * dado.<br>
	 * 
	 * @param element
	 *            Elemento que representa al objeto del que se quiere saber con que empresa está relacionado.
	 * @throws DataConversionException
	 *             Si el valor de un atributo no tiene el formato esperado.
	 * @throws NoSuchColumnException
	 *             Si se pregunta a una tabla por una columna que no contiene.
	 * @throws DataErrorException
	 *             Si la informacion de miEmpresa se encuentra en una tabla asociacion, lo que implicaria que la
	 *             propiedad mi_empresa para esta clase tiene cardinalidad mayor que 1.
	 */
	private String constructMyBusinessQuery(Element element) throws DataConversionException,
			DataErrorException, NoSuchColumnException {
		Attribute idtoAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);
		Integer idtoValue = idtoAttribute.getIntValue();

		ClassInfo myBusinessClassInfo = dataBaseMap.getClass(Constants.CLS_MI_EMPRESA);
		Integer myBusinessIdto = myBusinessClassInfo.getIdto();

		Attribute elementTableIdAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);
		Integer elmentTableId = elementTableIdAttribute.getIntValue();

		Table elementTable = dataBaseMap.getTable(idtoValue);
		Integer idPropertyMyBusiness = dataBaseMap.getPropertyId(Constants.prop_mi_empresa);
		String sql = null;
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		
		if (! elementTable.isExternalizedProperty(idPropertyMyBusiness)){
			List<String> columnNames = elementTable.getColumnNamesContainingProperty(idPropertyMyBusiness);
			for (String columnName : columnNames) {
				Integer columnDomain = elementTable.getColumnDomain(columnName);
				if (columnDomain.equals(myBusinessIdto)) {
					sql = "SELECT " + gSQL.getCharacterBegin() + columnName + gSQL.getCharacterEnd() + 
					" FROM " + gSQL.getCharacterBegin() + elementTable.getName() + gSQL.getCharacterEnd() + 
					" WHERE " + gSQL.getCharacterBegin() + Table.COLUMN_NAME_TABLEID + gSQL.getCharacterEnd() + "=" + elmentTableId + ";";
					break;
				}
			}
		}else{
			Set<Integer> myBusinessPropertyLocations = elementTable.getExternalizedPropertyLocations(idPropertyMyBusiness);
			for (Integer tableIdto : myBusinessPropertyLocations) {
				if (tableIdto.equals(myBusinessIdto)) {
					// La informacion de la propiedad está en la tabla de mi empresa.
					Table myBusinessTable = dataBaseMap.getTable(myBusinessClassInfo.getIdto());
					TableColumn [] domainColumns = myBusinessTable.getObjectPropertyColumn(idPropertyMyBusiness, idtoValue);
					if (domainColumns == null || domainColumns[0] == null){
						throw new DataErrorException("No se ha encontrado el vinculo con la clase " + element.getName() + " en la tabla " + myBusinessTable.getName());
					}
					sql = "SELECT " + gSQL.getCharacterBegin() + Table.COLUMN_NAME_TABLEID + gSQL.getCharacterEnd() + 
						" FROM " + gSQL.getCharacterBegin() + myBusinessTable.getName() + gSQL.getCharacterEnd() + 
						" WHERE " + gSQL.getCharacterBegin() + domainColumns[0].getColumnName() + gSQL.getCharacterEnd() + "=" + elmentTableId + ";";
					break;
				} else {
					throw new DataErrorException("La property " + Constants.prop_mi_empresa + " tiene cardinalidad mayor que 1 en la relación entre " 
							+ element.getName() + " y " + myBusinessClassInfo.getName());
				}
			}
		}
		return sql;
	}

	/**
	 * Busca el valor de la propiedad indicada en base de datos para ver si tiene valor.
	 * 
	 * @param propertyName
	 *            Nombre de la propiedad de la que se quiere buscar el valor. Solo se van a buscar valores de
	 *            DataProperties.
	 * @param tableIdto
	 *            Identificador de la tabla que debe contener la propiedad.
	 * @return
	 * @throws DataErrorException
	 *             Si no se encuentra una columna que contenga informacion de la propiedad dada.
	 * @throws SQLException
	 * @throws NamingException
	 */
	private String searchPropertyValueInDb(String propertyName, Integer tableIdto, Integer tableId)
			throws DataErrorException, SQLException, NamingException {
		String result = null;
		Table table = dataBaseMap.getTable(tableIdto);
		TableColumn column = table.getColumnByName(propertyName);
		
		boolean tomarDelegacionDeApp=false;
		if(column == null && propertyName.equals("delegación")){
			table = dataBaseMap.getTable(2);//idto aplicacion
			column = table.getColumnByName(propertyName);
			tomarDelegacionDeApp=true;
		}
		if (column == null) {
			throw new DataErrorException("La tabla " + table.getName() + " no contiene la columna " + propertyName);
		}
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());

		String sql = "SELECT " + gSQL.getCharacterBegin() + column.getColumnName() + gSQL.getCharacterEnd() + 
		" FROM " + gSQL.getCharacterBegin() + table.getName() + gSQL.getCharacterEnd();
		
		if(!tomarDelegacionDeApp) sql+= " WHERE "+ gSQL.getCharacterBegin() + Table.COLUMN_NAME_TABLEID + gSQL.getCharacterEnd() + "=" + tableId + ";";

		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);

			if(rs.next()){
				switch (column.getColumnDataType()) {
					case Constants.IDTO_STRING:
						result = rs.getString(1);
						break;
					case Constants.IDTO_INT:
						result = String.valueOf(rs.getInt(1));
						break;
					case Constants.IDTO_DATE:
					case Constants.IDTO_DATETIME:
						result = String.valueOf(rs.getLong(1));
						break;
					case Constants.IDTO_DOUBLE:
						result = String.valueOf(rs.getDouble(1));
						break;
					case Constants.IDTO_BOOLEAN:
						result = String.valueOf(rs.getBoolean(1));
						break;
					default:
						break;
				}
				if (rs.wasNull()) {
					result = null;
				}
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con != null)
				fcdb.close(con);
		}
		return result;
	}
	

	/**
	 * Almacena en un array de String: la acción a tomar para el siguiente índice 
	 * (reiniciar, continuar o buscar el último) y 
	 * el valor para el prefijo temporal dado un tiempo y una máscara.
	 */
	public void getValueTemp(long temporalFieldLongValue, 
			String mascPrefixTemp, Integer contYear, String lastPrefixTemp, 
			String[] returned) throws DataErrorException {
		//System.out.println("Inicio de la funcion getValueTemp");
		System.out.println("Parametros: valuePropPrefixTemp " + temporalFieldLongValue + 
				", mascPrefixTemp " + mascPrefixTemp + ", contYear " + contYear + 
				", lastPrefixTemp " + lastPrefixTemp);
		
		if (mascPrefixTemp==null) {
			throw new DataErrorException(DataErrorException.ERROR_DATA,"El índice declarado tiene incompleto el prefijo temporal: deben estar rellenos tanto el campo en prefijo temporal como la máscara en prefijo temporal .");
		}
		int result = NOTHING;
		
//		if (valuePropPrefixTemp!=null) {
			//obtener mes y año de la fecha
//			long time=Double.valueOf(valuePropPrefixTemp).longValue();
			Date dateTime = new Date(temporalFieldLongValue);

			Calendar cal = Calendar.getInstance();
			cal.setTime(dateTime);
			int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			//SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
			if(dayOfWeek==1)
				dayOfWeek = 7;
			else if (dayOfWeek==2)
				dayOfWeek = 1;
			else if (dayOfWeek==3)
				dayOfWeek = 2;
			else if (dayOfWeek==4)
				dayOfWeek = 3;
			else if (dayOfWeek==5)
				dayOfWeek = 4;
			else if (dayOfWeek==6)
				dayOfWeek = 5;
			else if (dayOfWeek==7)
				dayOfWeek = 6;
				
			int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
			int month = cal.get(Calendar.MONTH)+1;
			int year = cal.get(Calendar.YEAR);

			System.out.println("dayOfYear " + dayOfYear);
			System.out.println("dayOfWeek " + dayOfWeek);
			System.out.println("month " + month);
			System.out.println("year " + year);
			
			//valores posibles de la mascara:
			//aamm, amm, a, aa, mm, aaddd, aassd
			String lastYearStr = null;
			String lastMonthStr = null;
			String lastDayOfYearStr = null;
			String lastDayOfWeekStr = null;
			String lastWeekOfYearStr = null;
			
			String dayOfYearStr = String.valueOf(dayOfYear);
			String dayOfWeekStr = String.valueOf(dayOfWeek);
			String weekOfYearStr = String.valueOf(weekOfYear);
			String monthStr = String.valueOf(month);
			String yearStr = String.valueOf(year);
			
			String valueTemp = null;
			String valueTempCont = null;
			String yearStrCont = null;
			if (contYear!=null)
				yearStrCont = String.valueOf(contYear);

			if (mascPrefixTemp.equals("aamm") || mascPrefixTemp.equals("amm") || mascPrefixTemp.equals("aaddd") || mascPrefixTemp.equals("aassd")) {
				if (mascPrefixTemp.equals("aamm") || mascPrefixTemp.equals("aaddd") || mascPrefixTemp.equals("aassd")) {
					yearStr = yearStr.substring(2);
					if (contYear!=null)
						yearStrCont = StringUtils.leftPad(yearStrCont, 2, '0');
					if (lastPrefixTemp!=null) {
						lastYearStr = lastPrefixTemp.substring(0, 2);
						System.out.println("lastYearStr " + lastYearStr);
						if (mascPrefixTemp.equals("aamm")) {
							lastMonthStr = lastPrefixTemp.substring(2, 4);
							System.out.println("lastMonthStr " + lastMonthStr);
						} else if (mascPrefixTemp.equals("aaddd")) {
							lastDayOfYearStr = lastPrefixTemp.substring(2, 5);
							System.out.println("lastDayOfYearStr " + lastDayOfYearStr);
						} else if (mascPrefixTemp.equals("aassd")) {
							lastWeekOfYearStr = lastPrefixTemp.substring(2, 4);
							System.out.println("lastWeekOfYearStr " + lastWeekOfYearStr);
							lastDayOfWeekStr = lastPrefixTemp.substring(4, 5);
							System.out.println("lastDayOfWeekStr " + lastDayOfWeekStr);
						}
					}
					System.out.println("yearStr " + yearStr);
				} else if (mascPrefixTemp.equals("amm")) {
					yearStr = yearStr.substring(3);
					if (contYear!=null)
						yearStrCont = StringUtils.leftPad(yearStrCont, 1, '0');
					if (lastPrefixTemp!=null) {
						lastYearStr = lastPrefixTemp.substring(0, 1);
						System.out.println("lastYearStr " + lastYearStr);
						lastMonthStr = lastPrefixTemp.substring(1, 3);
						System.out.println("lastMonthStr " + lastMonthStr);
					} 
					System.out.println("yearStr " + yearStr);
				}
				if (mascPrefixTemp.equals("aamm") || mascPrefixTemp.equals("amm")) {
					monthStr = StringUtils.leftPad(monthStr, 2, '0');
					System.out.println("monthStr " + monthStr);
					valueTemp = yearStr + monthStr;
					//ver contador de año
					if (contYear!=null)
						valueTempCont = yearStrCont + monthStr;
				} else if (mascPrefixTemp.equals("aaddd")) {
					dayOfYearStr = StringUtils.leftPad(dayOfYearStr, 3, '0');
					System.out.println("dayOfYearStr " + dayOfYearStr);
					valueTemp = yearStr + dayOfYearStr;
					if (contYear!=null)
						valueTempCont = yearStrCont + dayOfYearStr;					
				} else if (mascPrefixTemp.equals("aassd")) {
					weekOfYearStr = StringUtils.leftPad(weekOfYearStr, 2, '0');
					System.out.println("weekOfYearStr " + weekOfYearStr);
					System.out.println("dayOfWeekStr " + dayOfWeekStr);
					valueTemp = yearStr + weekOfYearStr + dayOfWeekStr;
					if (contYear!=null)
						valueTempCont = yearStrCont + weekOfYearStr + dayOfWeekStr;					
				}
			} else if (mascPrefixTemp.equals("a") || mascPrefixTemp.equals("aa")) {
				if (mascPrefixTemp.equals("a")) {
					yearStr = yearStr.substring(3);
					valueTemp = yearStr;
					if (contYear!=null) {
						yearStrCont = StringUtils.leftPad(yearStrCont, 1, '0');
						valueTempCont = yearStrCont;
					}
				} else if (mascPrefixTemp.equals("aa")) {
					yearStr = yearStr.substring(2);
					valueTemp = yearStr;
					if (contYear!=null) {
						yearStrCont = StringUtils.leftPad(yearStrCont, 2, '0');
						valueTempCont = yearStrCont;
					}
				}
				if (lastPrefixTemp!=null)
					lastYearStr = lastPrefixTemp;
			} else if (mascPrefixTemp.equals("mm")) {
				monthStr = StringUtils.leftPad(monthStr, 2, '0');
				valueTemp = monthStr;
				if (contYear!=null)
					valueTempCont = monthStr;
				if (lastPrefixTemp!=null)
					lastMonthStr = lastPrefixTemp;
			} else
				throw new DataErrorException(DataErrorException.ERROR_DATA,"Mascara incorrecta " + mascPrefixTemp);
			
			//si el año del ultimo prefijo almacenado no coincide con el formado ahora
			//reiniciar indice
			boolean reiniciar = false;
			boolean searchLastIndex = false;
			Integer yearIncrement = null;
			if (lastYearStr!=null) {
				Integer lastYear = Integer.parseInt(lastYearStr);
				System.out.println("lastYear " + lastYear);
				year = Integer.parseInt(yearStr);
				System.out.println("year " + year);
				if (lastYear<year) {
					reiniciar = true;
					if (contYear!=null)
						yearIncrement = year-lastYear;
				} else if (lastYear>year) {
					searchLastIndex = true;
					if (contYear!=null)
						yearIncrement = year-lastYear;
				} else if (lastYear==year) {
					if (lastMonthStr!=null) {
						Integer lastMonth = Integer.parseInt(lastMonthStr);
						if (lastMonth<month)
							reiniciar = true;
						else if (lastMonth>month)
							searchLastIndex = true;
					} else if (lastDayOfYearStr!=null) {
						Integer lastDayOfYear = Integer.parseInt(lastDayOfYearStr);
						if (lastDayOfYear<dayOfYear)
							reiniciar = true;
						else if (lastDayOfYear>dayOfYear)
							searchLastIndex = true;
					} else if (lastWeekOfYearStr!=null) {
						Integer lastWeekOfYear = Integer.parseInt(lastWeekOfYearStr);
						if (lastWeekOfYear<weekOfYear)
							reiniciar = true;
						else if (lastWeekOfYear>weekOfYear)
							searchLastIndex = true;
						else if (lastWeekOfYear==weekOfYear) {
							if (lastDayOfWeekStr!=null) {
								Integer lastDayOfWeek = Integer.parseInt(lastDayOfWeekStr);
								if (lastDayOfWeek<dayOfWeek)
									reiniciar = true;
								else if (lastDayOfWeek>dayOfWeek)
									searchLastIndex = true;
							}
						}
					}
				}
				if (contYear!=null) {
					String newYear = null;
					if (yearIncrement!=null) {
						System.out.println("yearIncrement " + yearIncrement);
						newYear = String.valueOf(Integer.parseInt(yearStrCont) + yearIncrement);
						if (mascPrefixTemp.equals("aamm") || mascPrefixTemp.equals("aa"))
							newYear = StringUtils.leftPad(newYear, 2, '0');
						else if (mascPrefixTemp.equals("a") || mascPrefixTemp.equals("amm"))
							newYear = StringUtils.leftPad(newYear, 1, '0');
					} else
						newYear = yearStrCont;
	
					valueTempCont = newYear;
					if (monthStr!=null)
						valueTempCont += monthStr;
				}
			} else if (lastMonthStr!=null) {
				Integer lastMonth = Integer.parseInt(lastMonthStr);
				if (lastMonth<month)
					reiniciar = true;
				else if (lastMonth>month)
					searchLastIndex = true;
			} else if (lastDayOfYearStr!=null) {
				Integer lastDayOfYear = Integer.parseInt(lastDayOfYearStr);
				if (lastDayOfYear<dayOfYear)
					reiniciar = true;
				else if (lastDayOfYear>dayOfYear)
					searchLastIndex = true;
			} else if (lastWeekOfYearStr!=null) {
				Integer lastWeekOfYear = Integer.parseInt(lastWeekOfYearStr);
				if (lastWeekOfYear<weekOfYear)
					reiniciar = true;
				else if (lastWeekOfYear>weekOfYear)
					searchLastIndex = true;
				else if (lastWeekOfYear==weekOfYear) {
					if (lastDayOfWeekStr!=null) {
						Integer lastDayOfWeek = Integer.parseInt(lastDayOfWeekStr);
						if (lastDayOfWeek<dayOfWeek)
							reiniciar = true;
						else if (lastDayOfWeek>dayOfWeek)
							searchLastIndex = true;
					}
				}
			} else if (lastDayOfWeekStr!=null) {
				Integer lastDayOfWeek = Integer.parseInt(lastDayOfWeekStr);
				if (lastDayOfWeek<dayOfWeek)
					reiniciar = true;
				else if (lastDayOfWeek>dayOfWeek)
					searchLastIndex = true;
			}
			returned[1] = valueTemp;
			if (contYear!=null) {
				returned[2] = valueTempCont;
				returned[3] = String.valueOf(yearIncrement);
			}
			if (reiniciar) result = RESTORE;
			if (searchLastIndex) result = SEARCH_LAST;
//		}
		System.out.println("result " + result);
		returned[0] = String.valueOf(result);
		System.out.println("Fin de la funcion getValueTemp");
	}

}
