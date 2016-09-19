package dynagent.server.dbmap.builders;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.NamingException;

import dynagent.common.Constants;
import dynagent.server.dbmap.AssocciationPair;
import dynagent.server.dbmap.ClassInfo;
import dynagent.server.dbmap.ColumnOverlapException;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.ForeignKey;
import dynagent.server.dbmap.ForeignKeyOverlapException;
import dynagent.server.dbmap.IQueryInfo;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.dbmap.PropertyInfo;
import dynagent.server.dbmap.Table;
import dynagent.server.dbmap.TableColumn;
import dynagent.server.dbmap.TypeErrorException;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.gestorsDB.GestorsDBConstants;

/**
 * Clase que se encarga de mapear las tablas de la base de datos y de construirlas de ser necesario.
 */
public class TableBuilder {
	
	/** Mapa de las clases indexadas por su idto. */
	private Map<Integer, ClassInfo> classes;
	/** Mapa de las tablas indexadas por su id. */
	private Map<Integer, Table> tables;
	/** Mapa de las propiedades inversas. */
	private Map<Integer, Integer> inverseProperties;
	/** Conjunto de las propiedades que son estructurales. */
	private Set<Integer> structuralProperties;
	/** Conjunto de las propiedades estructurales compartidas. */
	private Set<Integer> sharedProperties;
	/**
	 * Conjunto de todas las lineas que contiene la tabla t_assoc en base de
	 * datos, o deberia contener al crearse. La estructura de cada linea debe
	 * ser la siguiente:<br>
	 * <table border="1">
	 * <theader>
	 * <th>idDominio</th>
	 * <th>idRango</th>
	 * <th>t_name</th>
	 * <th>propertyColumn</th>
	 * <th>property</th>
	 * </theader> <tbody>
	 * <tr>
	 * <td>21</td>
	 * <td>214</td>
	 * <td>pedido_venta#línea_artículo</td>
	 * <td>false</td>
	 * <td>84</td>
	 * </tr>
	 * <tr>
	 * <td>...</td>
	 * <td>...</td>
	 * <td>...</td>
	 * <td>...</td>
	 * <td>...</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * 
	 */
	private List<Object[]> tAssocContent;
	/**
	 * Dadas dos tablas, devolvera el identificador de la tabla asociacion que
	 * las relaciona.
	 */
	private Map<AssocciationPair, Integer> assocPairAssignedTables;
	/** Proximo identificador a usar como id en una tabla que no representa a una clase. */
	private int nextId;
	/** Objeto que nos permite conectarnos a base de datos. */
	private FactoryConnectionDB fcdb;
	/**
	 * Indica si se esta importando y, por tanto, ciertos datos no estan
	 * disponibles en base de datos, como puede ser la tabla t_link
	 */
	private boolean importing;
	/** Indica si se ha producido algun error durante el mapeo. */
	private boolean error;
	/**
	 * Indica si se esta en modo de pruebas. Actualmente si se esta en modo de
	 * pruebas, se ponen restricciones de unicidad a las tablas cuando se
	 * importa.
	 */
	private boolean debugMode;
	
	/* Comparador que hace que las tablas se ordenen de tal manera que una
	 * tabla que referencia a otra, salga despues de esta.
	 */ 
	private Comparator<Table> tableComparator = new Comparator<Table>() {

		public int compare(Table o1, Table o2) {
			if (o1.equals(o2)) {
				return 0;
			}
			Set<Integer> referencedTables = o1.getAllReferencedTables();
			if (referencedTables.contains(o2)) {
				return 1;
			} else {
				return -1;
			}
		}
	};
	
	
	public TableBuilder(FactoryConnectionDB fcdb, int nextId, boolean importing, boolean debugMode){
		this.fcdb = fcdb;
		this.nextId = nextId;
		this.importing = importing;
		tables = new Hashtable<Integer, Table>();
		error = false;
		tAssocContent = new LinkedList<Object[]>();
		assocPairAssignedTables = new Hashtable<AssocciationPair, Integer>();
		this.debugMode = debugMode; 
	}

	/**
	 * Devuelve las tablas del modelo relacional.
	 * 
	 * @return Mapa de las tablas indexadas por su id.
	 */
	public Map<Integer, Table> getTables() {
		return tables;
	}

	/**
	 * Devuelve el siguiente id ficticio que se deberia usar.
	 * 
	 * @return id que todavia no se ha usado para identificar una tabla
	 *         asociacion.
	 */
	public int getNextId(){
		return nextId;
	}

	/**
	 * Establece las clases del modelo con las que se ha de trabajar.
	 * 
	 * @param classes
	 *            Mapa de las clases del modelo que queremos que se analicen.<br>
	 *            <b>No puede ser <code>null</code></b>.
	 */
	public void setClasses(Map<Integer, ClassInfo> classes) {
		this.classes = classes;
	}

	/**
	 * Fija las propiedades que tienen inversa en el modelo.
	 * 
	 * @param inverseProperties
	 *            Mapa de las propiedades inversas del modelo donde el valor es
	 *            el idProperty de la propiedad inversa.<br>
	 *            <b>No puede ser <code>null</code></b>.
	 */
	public void setInverseProperties(Map<Integer, Integer> inverseProperties) {
		this.inverseProperties = inverseProperties;
	}

	/**
	 * Fija el conjunto de las propiedades estructurales del modelo.
	 * 
	 * @param structuralProperties
	 *            Conjunto de las propiedades estructurales.<br>
	 *            <b>No puede ser <code>null</code></b>.
	 */
	public void setStructuralProperties(Set<Integer> structuralProperties) {
		this.structuralProperties = structuralProperties;
	}

	/**
	 * Fija las propiedades estructurales compartidas del modelo.
	 * 
	 * @param sharedProperties
	 *            Conjunto de los idProperty de las propieades estrucurales
	 *            compartidas.
	 */
	public void setSharedProperties(Set<Integer> sharedProperties) {
		this.sharedProperties = sharedProperties;
	}
	
	public boolean createTables(){
		createVoidTables();
		createTableLinks();
		List<Table> tables = new LinkedList<Table>(this.tables.values());
		for (Table table : tables) {
			if (!table.isAssociation()) {
				fillTableColumns(table);
			}
		}
		return error;
	}
	
	public boolean constructTablesOnDB(Set<Integer> excludedIdtos, boolean restriction){
		if (excludedIdtos == null){
			excludedIdtos = new HashSet<Integer>();
		}
		if (!importing || error) {
			System.err.println("No se han podido construir las tablas de datos porque no se esta importando o porque se ha producido algun error en el mapeo.");
			return false;
		}
		
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		Set<Table> tablesSet = new TreeSet<Table>(tableComparator);
		tablesSet.addAll(tables.values());
		String sqlCreationStatement = null;
		String restrictionStatement = null;
		List<String> indexCreationStatements = null;
		try {
			for (Table table : tablesSet) {
				if (excludedIdtos.contains(table.getId()) || excludedIdtos.containsAll(table.getOriginalTableIdtos())){
					continue;
				}
				//if (! table.hasForeignProperties()){//Comentamos ya que, ahora, queremos que sean unicos tambien los rdn de las clases que vienen de un estructural
					TableColumn column = table.getDataPropertyColumn(Constants.IdPROP_RDN);
					if (column != null && !table.getName().equals("subinforme")/*En los subinformes no forzamos a que sea unico el rdn*/){
						column.setUnique(true);
					}
				//}
				String dropTableStatement = "DROP TABLE IF EXISTS " + generateSQL.getCharacterBegin() + table.getName() + generateSQL.getCharacterEnd() + ";";
				String dropViewStatement = "DROP VIEW IF EXISTS " + generateSQL.getCharacterBegin() + table.getName() + generateSQL.getCharacterEnd() + ";";
				sqlCreationStatement = table.getSQLCreationStatement(fcdb.getBusiness(), fcdb.getGestorDB());
				DBQueries.execute(fcdb, dropTableStatement);
				DBQueries.execute(fcdb, dropViewStatement);
				DBQueries.execute(fcdb, sqlCreationStatement);
			}
			sqlCreationStatement = null;
			for (Table table : tablesSet) {
				if(restriction){
					restrictionStatement = table.getSQLRestrictionsStatement(fcdb.getBusiness(), fcdb.getGestorDB());
				}
				indexCreationStatements = table.getSQLIndexesCreationStatements(fcdb.getGestorDB());
				if (restrictionStatement != null) {
					DBQueries.execute(fcdb, restrictionStatement);
				}
				if (indexCreationStatements != null && ! indexCreationStatements.isEmpty()){
					for (String indexCreationStatement : indexCreationStatements) {
						DBQueries.execute(fcdb, indexCreationStatement);
					}
				}
				
			}
		} catch (TypeErrorException e) {
			e.printStackTrace();
			error = true;
		} catch (SQLException e) {
			System.err.println("Creation St: " + sqlCreationStatement);
			System.err.println("Restriction St: " + restrictionStatement);
			System.err.println("Index St: " + indexCreationStatements);
			e.printStackTrace();
			error = true; 
		} catch (NamingException e) {
			e.printStackTrace();
			error = true;
		}
		return constructTLinkTable();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// .........................Metodos privados.............................//
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Se crean tablas vacias en las que todavia no se indica ninguna columna.
	 * Solo se crearon las tablas de aquellas clases que no sean abstractas.<br>
	 * <b>Se presupone que no hay dos clases con el mismo nombre ni con el mismo
	 * identificador.</b>
	 */
	private void createVoidTables() {
		Set<Integer> propertiesToRemove = new HashSet<Integer>();
		for (Integer integer : structuralProperties) {
			Integer inverseProperty = inverseProperties.get(integer);
			if (inverseProperty != null){
				propertiesToRemove.add(inverseProperty);
			}
		}
		for (ClassInfo classInfo : classes.values()) {
			if (!classInfo.isAbstractClass()) {
				Table table = new Table(classInfo,classes);
				tables.put(classInfo.getIdto(), table);
				table.removeProperties(propertiesToRemove);
			}
		}
	}
	
	/**
	 * Metodo que se encarga de crear todos los vinculos entre las distintas
	 * tablas. Se pueden dar dos casos:<br>
	 * <ul>
	 * <li>ObjectProperties con cardinalidad mayor que 1
	 * <ul>
	 * <li>Si no son estructurales se crea tabla asociacion.</li>
	 * <li>En caso contrario, se traslada la property a la tabla objetivo.</li>
	 * </ul>
	 * </li>
	 * <li>DataProperties con cardinalidad mayor que 1
	 * <ul>
	 * <li>Se crea una tabla auxiliar que contendra dichos datos, donde habra
	 * una columna para identificar un elemento dentro de dicha tabla, una
	 * columna con los datos de la DataProperty y por ultimo una columna con la
	 * referencia al objeto al que pertenecen.<br>
	 * En este tipo de tablas se implementar DELETE CASCADE y UPDATE CASCADE<br>
	 * <b>Este tipo de casos se tratar en la creacion de las columnas de una
	 * tabla que representa la clase.</b></li>
	 * </ul>
	 * </li>
	 * </ul>
	 * Tambien hay que diferenciar entre si se esta importando o no, pues si se
	 * esta importando toda la informacion de las tablas asociacion se podra
	 * sacar de t_assoc, pero lo referente a las DataProperties tendra que
	 * seguir tratandose igual que si se estuviera importando.
	 */
	private void createTableLinks() {
		if (!importing) {
			createTableLinksFromDB();
		} else {
			createTableLinksFromModel();
		}

	}
	
	/**
	 * Metodo que crear el resto de columnas necesarias en cada una de las
	 * tablas. Se presupone que el metodo constructor de una tabla normal crea
	 * dos columnas por defecto: <i>tableId</i> e <i>ido</i>.
	 */
	private void fillTableColumns(Table table) {
		ClassInfo classInfo = classes.get(table.getId());
		if (classInfo.isAbstractClass()) {
			// Las clases abstractas no tienen tablas asociadas.
			return;
		}
		try {
			for (PropertyInfo property : classInfo.getAllProperties()) {
				int idProperty = property.getIdProp();
				Integer inverseProperty = inverseProperties.get(idProperty);
				if (inverseProperty != null && structuralProperties.contains(inverseProperty)){
					continue;
				}
				// Una DataProperty solo puede tener un tipo.
				boolean isBasicType = false;
				Integer dataType = null;
				for (Integer propertyType : property.getPropertyTypes()) {
					isBasicType = Constants.isDataType(propertyType);
					if (isBasicType) {
						dataType = propertyType;
						break;
					}
				}
				if (property.getMaxCardinality() > 1 && !isBasicType) {
					// Se trata de una ObjectProperty con cardinalidad mayor que
					// 1, con lo cual ya ha sido tratada
					// anteriormente.
					continue;
				}
				if (property.getMaxCardinality() > 1) {
					// Se trata de una DataProperty con cardinalidad mayor que
					// 1, con lo cual hay que construir una
					// tabla adicional para guardar sus datos.
					// Columna que apunta a la tabla actual.
					Table dataPropertyTable = new Table(classInfo, property, nextId);
					
					TableColumn[] domainColumn = dataPropertyTable.getObjectPropertyColumn(IQueryInfo.ID_DOMAIN, classInfo.getIdto());
					domainColumn[0].setNotNull(true);
					domainColumn[0].setUnsigned(true);
					dataPropertyTable.addForeignKey(domainColumn[0].getColumnName(), table.getName(), Table.COLUMN_NAME_TABLEID, ForeignKey.CASCADE, ForeignKey.CASCADE);
					table.addExternalizedProperty(property.getIdProp(), nextId);
					tables.put(nextId, dataPropertyTable);
					nextId++;
				} else {
					// Se puede tratar de una propiedad con cardinalidad maxima 1
					if (isBasicType) {
						// Es una DataProperty
						boolean isNotNull = property.getMinCardinality() > 0 && debugMode;
						TableColumn column = table.addDataPropertyColumn(property.getName(), dataType, property.getIdProp());
						column.setNotNull(isNotNull);
					} else {
						// Es una ObjectProperty y tenemos que crear una columna
						// por cada uno de los tipos a los que se
						// apunta, lo que es peor, una columna por cada uno de
						// los especializados del tipo al que se
						// apunta.
						Set<Integer> targetTablesSet = new HashSet<Integer>();
						for (Integer rangeType : property.getPropertyTypes()) {
							ClassInfo rangeClassInfo = classes.get(rangeType);
							if (!rangeClassInfo.isAbstractClass()) {
								targetTablesSet.add(rangeType);
							}
							//System.out.println("DataBaseMap.fillTableColumns() [rangeClassInfo=" + rangeClassInfo + "; property=" + idProperty + "]");
							targetTablesSet.addAll(getAllNonAbstractChildrenFromClass(rangeClassInfo));
						}
						for (Integer propertyTargetTable : targetTablesSet) {
							Table targetTable = tables.get(propertyTargetTable);
							String columnName = property.getName();
							if (targetTablesSet.size() > 1) {
								columnName += targetTable.getName().toUpperCase();
							}
							TableColumn column = table.addObjectPropertyColumn(columnName, property.getIdProp(), propertyTargetTable);
							column.setUnsigned(true);
							table.addForeignKey(columnName, targetTable.getName(), Table.COLUMN_NAME_TABLEID, ForeignKey.DONOTHING, ForeignKey.CASCADE);
						}
					}
				}
			}
		} catch (ColumnOverlapException e) {
			e.printStackTrace();
			error = true;
		} catch (NoSuchColumnException e) {
			error = true;
			e.printStackTrace();
		} catch (ForeignKeyOverlapException e) {
			error = true;
			e.printStackTrace();
		}
	}
	
	/**
	 * Consulta la tabla t_links para crear todas las tablas asociacion que se
	 * hubieran creado durante la importacion. Esto nos ahorra tener que volver
	 * a calcular todas las asociaciones.
	 */
	private void createTableLinksFromDB() {
		try {
			tAssocContent = DBQueries.getAssociationTables(fcdb);
		} catch (SQLException e) {
			error = true;
			e.printStackTrace();
			return;
		} catch (NamingException e) {
			error = true;
			e.printStackTrace();
			return;
		}
		createLinksFromTAssocContent();
	}
	
	/**
	 * Se encarga de crear los vinculos de las object property partiendo de los
	 * datos que se han obtenido de las distintas clases.<br>
	 * Solo se van a contemplar dos casos:
	 * <ul>
	 * <li>La property es estructural pero no inversa: <i>Se crea una columna en
	 * la tabla apuntada.</i></li>
	 * <li>Cualquier otro caso: <i>Se crea una tabla asociacion</i>
	 */
	private void createTableLinksFromModel() {
		for (ClassInfo domainClass : classes.values()) {
			if (domainClass.isAbstractClass()) {
				continue;
			}
			Table domainTable = tables.get(domainClass.getIdto());
			for (Integer rangeClassIdto : domainClass.getReferencedClasses()) {
				ClassInfo rangeClass = classes.get(rangeClassIdto);
				if (rangeClass == null) {
					continue;
				}
				// Construimos un conjunto de todas las clases no abstractas a
				// las que puede apuntar la property en
				// realidad. Este conjunto estar formado por la clase padre que
				// nos hemos encontrado que es
				// referenciada y todas sus hijas. (Si no son abstractas)
				Set<Integer> nonAbstractRangeClasses = new HashSet<Integer>();
				if (!rangeClass.isAbstractClass()) {
					nonAbstractRangeClasses.add(rangeClassIdto);
				}
				for (Integer child : rangeClass.getChildClasses()) {
					ClassInfo childClass = classes.get(child);
					if (childClass != null && !childClass.isAbstractClass()) {
						nonAbstractRangeClasses.add(child);
					}
				}
				// Cogemos todas las propiedades referenciado a la clase padre
				// en el rango.
				Set<Integer> properties = domainClass.getPropertiesReferencingClass(rangeClassIdto);

				boolean hasPropertyColumn = properties.size() > 1;
				boolean areStructuralProperties = true;
				boolean hasInverseStructuralProperty = false;
				for (Integer idProperty : properties) {
					// Hay que ampliar el procesamiento para que entre:
					/*
					 * 					La propiedad 
					 * 		  			es estructural
					 * 		-------------------------------------------------
					 * 		si												no 
					 * 		| 												| 
					 * 		|										  La propiedad
					 * 		| 										  tiene inversa
					 * 		|								-------------------------------------
					 * 		|								|	 								| 
					 * 		|								si 									no  
					 * 		|								| 									| 
					 * 		|							La inversa es							|
					 * 		|							estructural								|
					 * 		|					-----------------------------					|
					 * 		| 					|							|					|
					 * 		|					si  						no					|
					 * 		|	 				|							| 					|
					 * Poner las columnas   Ignorar esta					---------------------
					 * en la tabla a		propiedad									|
					 * la que apunta 										Construir la tabla asociacion
					 * la property
					 * estructural
					 */
					areStructuralProperties = areStructuralProperties && structuralProperties.contains(idProperty);
					Integer inverseProperty = inverseProperties.get(idProperty);
					if (!areStructuralProperties && inverseProperty != null && structuralProperties.contains(inverseProperty)) {
						hasInverseStructuralProperty = true;
					}
				}
				// 
				if (hasInverseStructuralProperty) {
					// Cuando se procese la propiedad inversa ya se añadira la
					// relacion.
					continue;
				}
				for (Integer targetTableIdto : nonAbstractRangeClasses) {
					Table targetTable = tables.get(targetTableIdto);
					for (Integer property : properties) {
						PropertyInfo propertyInfo = domainClass.getProperty(property);
						if (propertyInfo.getMaxCardinality() == 1) {
							continue;
						}
						// TESTING prueba para que la propiedad linea no se
						// ponga en una tabla asociacion
						if (areStructuralProperties /* && !areInverseProperties */) {
							// el vinculo se pone en la target class
							tAssocContent.add(new Object[] {domainTable.getId(), targetTableIdto, targetTable.getName(), hasPropertyColumn, property });
						} else {
							// el vinculo se pone en una tabla asociacion.
							String associationTableName = domainTable.getName() + "#" + targetTable.getName();
							tAssocContent.add(new Object[] {domainTable.getId(), targetTableIdto, associationTableName, hasPropertyColumn, property });
						}
					}
				}
			}
		}
		createLinksFromTAssocContent();
	}
	
	/**
	 * Metodo que se encarga de construir, recursivamente, el conjunto de todos
	 * los hijos de una clase dada. Todos los hijos añadidos a dicho conjunto
	 * seran no abstractos.
	 * 
	 * @param parentClass
	 *            Clase de la que se parte para buscar sus hijos
	 * @return Conjunto de los hijos no abstractos encontrados. En ningun caso
	 *         devolvera <code>null</code>.
	 */
	private Set<Integer> getAllNonAbstractChildrenFromClass(ClassInfo parentClass) {
		Set<Integer> result = new HashSet<Integer>();
		if (parentClass == null || parentClass.getChildClasses() == null) {
			return result;
		}
		
		// Codigo suponiendo que t_herencias tiene las herencias a todos los niveles.
		for (Integer childClassIdto : parentClass.getChildClasses()){
			ClassInfo childClass = classes.get(childClassIdto);
			if (! childClass.isAbstractClass() && ! childClassIdto.equals(parentClass.getIdto())){
				result.add(childClassIdto);
			}
		}
		
		// Codigo por si en t_herencias no se insertasen las herencias a todos los niveles.
//		for (Integer childClassIdto : parentClass.getChildClasses()) {
//			ClassInfo childClassInfo = classesByIdto.get(childClassIdto);
//			if (childClassIdto.equals(parentClass.getIdto())){
//				// Ignoramos la herencia de una clase consigo misma.
//				continue;
//			}
//			if (!childClassInfo.isAbstractClass()) {
//				result.add(childClassIdto);
//			}
//			//System.out.println("PARENTCLASS=" + parentClass.getName() + "; CHILDCLASS=" + childClassInfo.getName());
//			result.addAll(getAllNonAbstractChildrenFromClass(childClassInfo));
//		}
		return result;
	}
	
	/**
	 * Los objetivos son:
	 * <ol>
	 * <li>Averiguar si la columna que guarda los datos de la propiedad se
	 * encuentra en en la tabla a la que apunta la propiedad o si se encuentra
	 * en una tabla asociacion.</li>
	 * <li>Indicarle a la tabla del dominio que tiene una propiedad
	 * externalizada a la tabla que hayamos obtenido.</li>
	 * <li>Crear las tablas asociacion que hagan falta.</li>
	 * </ol>
	 */
	private void createLinksFromTAssocContent() {
		// TODO En el caso de estructurales externalizadas, si una clase apunta
		// al mismo rango mediante dos estructurales y una de ellas es
		// compartida y la otra no, el comportamiento de la foreign key va a ser
		// aleatorio. Habria que mejorarlo para que se ponga la foreign key
		// menos restrictiva.
		for (Object[] objects : tAssocContent) {
			int idDominio = (Integer) objects[0];
			int idRango = (Integer) objects[1];
			String tableName = (String) objects[2];
			boolean hasPropertyColumn = (Boolean) objects[3];
			int idProperty = (Integer) objects[4];
			Table rangeTable = tables.get(idRango);
			Table domainTable = tables.get(idDominio);
			//aunque la base de datos es previa a la importacion, y puede que no haya algunas clases nuevas, las "tables" se sacan de la ontologia nueva, por lo 
			//que puede que no exista, por eso el continue
			if(domainTable==null) continue;
			ClassInfo domainClass = classes.get(idDominio);
			if (rangeTable.getName().equals(tableName)) {
				// Si entramos aqui significa que la propiedad se ha trasladado
				// a la tabla objetivo.
				//System.out.println("tabla "+tableName+" idDominio:"+idDominio+" idRango:"+idRango);
				String columnName = domainTable.getName() + IQueryInfo.SUFFIX_ID;
				// Se comprueba que no se haya creado ya la columna, pues si se
				// intenta crear una columna ya creada
				// saltara una excepcion.
				if (domainTable.referencesTable(rangeTable.getId())) {					
					TableColumn existingColumn = rangeTable.getColumnByName(columnName);
					if (existingColumn != null){
						// Cambiar el nombre de la columna para que añada el nombre de la propiedad.
						PropertyInfo existingProperty = domainClass.getProperty(existingColumn.getIdProperty());
						rangeTable.alterColumnName(existingColumn, columnName + existingProperty.getName().toUpperCase());
					}
					// Ya existe una columna con el mismo dominio en la tabla
					// del rango, con lo que tenemos que crear otra columna con
					// un nombre diferente.
					PropertyInfo property = domainClass.getProperty(idProperty);
					columnName += property.getName().toUpperCase();
				}
				try {
					Set<Integer> rangeSet = new HashSet<Integer>();
					rangeSet.add(idDominio);
					rangeTable.addObjectProperty(domainClass.getProperty(idProperty), rangeSet);
					TableColumn domainColumn = rangeTable.addObjectPropertyColumn(columnName, idProperty, idDominio);
					domainColumn.setUnsigned(true);
					if (structuralProperties.contains(idProperty)) {
						if (! sharedProperties.contains(idProperty)) {
							rangeTable.addForeignKey(columnName, domainTable.getName(), Table.COLUMN_NAME_TABLEID, ForeignKey.CASCADE, ForeignKey.CASCADE);
						} else {
							rangeTable.addForeignKey(columnName, domainTable.getName(), Table.COLUMN_NAME_TABLEID, ForeignKey.SETNULL, ForeignKey.CASCADE);
						}
					} else {
						// Este caso en teoria nunca se va a dar, pues si
						// una ObjectProperty relaciona dos elementos
						// con cardinalidad mayor que 1 y dicha propiedad no
						// es estructural, se va a crear una tabla
						// asociacion en la importacion.
						System.err.println("Se ha entrado por una rama en teoria inalcanzable en el metodo CreateTableLinksFromDB;");
					}
				} catch (ColumnOverlapException e) {
					error = true;
					e.printStackTrace();
					return;
				} catch (NoSuchColumnException e) {
					error = true;
					e.printStackTrace();
					return;
				} catch (ForeignKeyOverlapException e) {
					error = true;
					e.printStackTrace();
					return;
				}
				domainTable.addExternalizedProperty(idProperty, rangeTable.getId());
			} else {
				// En este caso se debe crear la tabla asociacion entre las dos
				// clases si no existe ya. El unico caso en
				// que una tabla asociacion aparezca dos veces en los datos es
				// porque las dos clases se relacionen por
				// mas de una property.
				// Como logica para este tipo de tablas se va a seguir que si se
				// borra el individuo del dominio se borra
				// el enlace pero si se intenta borrar el objeto del rango no se
				// permite mientras exista el enlace.
				Integer assocTableIdto = assocPairAssignedTables.get(new AssocciationPair(idDominio, idRango));
				Table assocTable;
				if (assocTableIdto == null) {
					String domainColumnName = domainTable.getName() + IQueryInfo.SUFFIX_ID;
					String rangeColumnName = rangeTable.getName() + IQueryInfo.SUFFIX_ID;
					if (idDominio == idRango) {
						rangeColumnName += "Rango";
					}
					assocTable = new Table(new ClassInfo[]{classes.get(domainTable.getId()), classes.get(rangeTable.getId())}, nextId);
					if (importing){
						objects[2] = assocTable.getName();
					}else{
						assocTable.setName(tableName);
					}
					try {
						// Creamos el vinculo con la tabla del dominio
						TableColumn domainColumn = assocTable.addObjectPropertyColumn(domainColumnName, IQueryInfo.ID_DOMAIN, idDominio);
						domainColumn.setNotNull(true);
						domainColumn.setUnsigned(true);
						assocTable.addForeignKey(domainColumn.getColumnName(), domainTable.getName(), Table.COLUMN_NAME_TABLEID, ForeignKey.RESTRICT, ForeignKey.CASCADE);
						domainTable.addExternalizedProperty(idProperty, nextId);
						// Creamos el vinculo con la tabla del rango.
						Set<Integer> rangeSet = new HashSet<Integer>();
						rangeSet.add(idRango);
						assocTable.addObjectProperty(domainClass.getProperty(idProperty), rangeSet);
						TableColumn rangeColumn = assocTable.addObjectPropertyColumn(rangeColumnName, idProperty, idRango);
						rangeColumn.setUnsigned(true);
						rangeColumn.setNotNull(true);
						assocTable.addForeignKey(rangeColumnName, rangeTable.getName(), Table.COLUMN_NAME_TABLEID, ForeignKey.CASCADE, ForeignKey.CASCADE);

						if (hasPropertyColumn) {
							assocTable.addPropertyColumn();
						}

						// Creamos un indice por cada una de las columnas de
						// identificadores de objetos.
						Set<String> indexSet = new HashSet<String>();
						indexSet.add(domainColumnName);
						assocTable.addIndex(indexSet);
						indexSet = new HashSet<String>();
						indexSet.add(rangeColumnName);
						assocTable.addIndex(indexSet);
					} catch (ColumnOverlapException e) {
						error = true;
						e.printStackTrace();
						return;
					} catch (NoSuchColumnException e) {
						error = true;
						e.printStackTrace();
						return;
					} catch (ForeignKeyOverlapException e) {
						error = true;
						e.printStackTrace();
						return;
					}
					// Guardamos toda la informacion generada en las distintas
					// estructuras donde debe estar.
					assocPairAssignedTables.put(new AssocciationPair(idDominio, idRango), nextId);
					tables.put(nextId, assocTable);
					nextId++;
				} else {
					// Si la tabla asociacion ya existia lo unico que se va a
					// hacer es decirle a la tabla del dominio
					// que tiene su property externalizda a dicha tabla
					// asociacion.
					domainTable.addExternalizedProperty(idProperty, assocTableIdto);
					PropertyInfo property = domainClass.getProperty(idProperty);
					assocTable = tables.get(assocTableIdto);
					Set<Integer> rangeSet = new HashSet<Integer>();
					rangeSet.add(idRango);
					assocTable.addObjectProperty(property, rangeSet);
					assocTable.addPropertyToRange(idProperty);
					
				}
			}
		}
	}
	
	/**
	 * Crea la tabla que se encarga de mantener todas las relaciones de
	 * objectProperties externalizadas.
	 * 
	 * @return <code>false</code> si se ha producido algun error durante la
	 *         ejecucion de las ordenes necesarias.
	 */
	private boolean constructTLinkTable() {
		if (!importing || error) {
			return false;
		}
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		String characterBegin = generateSQL.getCharacterBegin();
		String characterEnd = generateSQL.getCharacterEnd();
		String dropTableStatement = "DROP TABLE IF EXISTS " + characterBegin + "t_link" + characterEnd + ";";
		String dropViewStatement = "DROP VIEW IF EXISTS " + characterBegin + "t_link" + characterEnd + ";";
		String sqlCreationStatement = "CREATE TABLE " + characterBegin + "t_link" + characterEnd + " (" + characterBegin + "idtoDominio" + characterEnd + (fcdb.getGestorDB().equals(GestorsDBConstants.postgreSQL) ? "SERIAL," : " INT UNSIGNED NOT NULL, ") + characterBegin + "idtoRango" + characterEnd + (fcdb.getGestorDB().equals(GestorsDBConstants.postgreSQL) ? "SERIAL," :  " INT UNSIGNED NOT NULL, ") + characterBegin + "t_name" + characterEnd + " VARCHAR(100) NOT NULL, " + characterBegin + "propertyColumn" + characterEnd + " BOOLEAN NOT NULL, " + characterBegin + "idProperty" + characterEnd + " INT NOT NULL, PRIMARY KEY (" + characterBegin + "idtoDominio" + characterEnd + ", " + characterBegin + "idtoRango" + characterEnd + ", " + characterBegin + "t_name" + characterEnd + ", " + characterBegin + "idProperty" + characterEnd + "))" + (fcdb.getGestorDB().equals(GestorsDBConstants.postgreSQL) ? "WITHOUT OIDS" : "ENGINE=InnoDB;");
		String dataInsertionStatement = "INSERT INTO " + characterBegin + "t_link" + characterEnd + " (" + characterBegin + "idtoDominio" + characterEnd + ", " + characterBegin + "idtoRango" + characterEnd + ", " + characterBegin + "t_name" + characterEnd + ", " + characterBegin + "propertyColumn" + characterEnd + ", " + characterBegin + "idProperty" + characterEnd + ") VALUES";

		boolean first = true;
		for (Object[] tLinkEntry : tAssocContent) {
			String tLinkLine = " (" + tLinkEntry[0] + ", " + tLinkEntry[1]
					+ ", '" + tLinkEntry[2] + "', " + tLinkEntry[3] + ", "
					+ tLinkEntry[4] + ")";
			if (dataInsertionStatement.indexOf(tLinkLine) != -1) {
				continue;
			}
			if (!first) {
				dataInsertionStatement += ",";
			}
			dataInsertionStatement += tLinkLine;
			first = false;
		}
		if(tAssocContent.size()>0)	dataInsertionStatement += ";";
		else dataInsertionStatement=";";
		
		try {
			DBQueries.execute(fcdb, dropTableStatement);
			DBQueries.execute(fcdb, dropViewStatement);
			DBQueries.execute(fcdb, sqlCreationStatement);
			DBQueries.execute(fcdb, dataInsertionStatement);
		} catch (SQLException e) {
			error = true;
			System.out.println(dataInsertionStatement);
			e.printStackTrace();
		} catch (NamingException e) {
			error = true;
			e.printStackTrace();
		} finally {
			if (error) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Se encarga de asignar las restricciones de foreing key sobre todas las tablas
	 */
	public void putRestrictions(){
		Set<Table> tablesSet = new TreeSet<Table>(tableComparator);
		tablesSet.addAll(tables.values());
		String restrictionStatement = null;
		try {
			for (Table table : tablesSet) {
				restrictionStatement = table.getSQLRestrictionsStatement(fcdb.getBusiness(), fcdb.getGestorDB());
				if (restrictionStatement != null) {
					DBQueries.execute(fcdb, restrictionStatement);
				}
			}
		} catch (SQLException e) {
			System.err.println("Restriction St: " + restrictionStatement);
			e.printStackTrace();
			error = true; 
		} catch (NamingException e) {
			e.printStackTrace();
			error = true;
		}
	}
}
