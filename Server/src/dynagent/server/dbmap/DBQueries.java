package dynagent.server.dbmap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import java.sql.Connection;

import dynagent.common.Constants;
import dynagent.common.basicobjects.PredefinedUnit;
import dynagent.common.basicobjects.PropertyUnit;
import dynagent.common.knowledge.Category;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.gestorsDB.GestorsDBConstants;

/**
 * Clase que contiene todos los metodos de consulta a base de datos que vamos a necesitar a la hora de constuir las
 * tablas y cachear la estructura construida para posteriormente poder saber como extraer los datos de la BD.
 */
public abstract class DBQueries {

	/**
	 * Objeto que va a tener cacheados todos los nombres de las propiedades asociados a cada identificador de propiedad;
	 * separandolos por base de datos a la que pertenecen.
	 */
	private static Hashtable<String, Hashtable<Integer, String>> propertiesNamesByBusiness = new Hashtable<String, Hashtable<Integer, String>>();
	private static Hashtable<String, Set<Integer>> structuralPropertiesByBusiness = new Hashtable<String, Set<Integer>>();
	private static Hashtable<String, Set<Integer>> sharedPropertiesByBusiness = new Hashtable<String, Set<Integer>>();
	private static Hashtable<String, Hashtable<Integer, String>> classNamesByBusiness = new Hashtable<String, Hashtable<Integer, String>>();

	/**
	 * Devuelve el contenido de la tabla t_links, que contiene la informacion de como se han hecho los vinculos de las
	 * ObjectProperties con cardinalidad mayor que 1
	 * 
	 * @param fcdb Objeto que nos permite conectarnos a la base de datos.
	 * @return Lista con el contenido de la tabla t_links estructurado de la siguente manera:
	 * <table border>
	 * <theader>
	 * <th>idDominio</th>
	 * <th>idRango</th>
	 * <th>t_name</th>
	 * <th>propertyColumn</th>
	 * <th>idProperty</th>
	 * <theader>
	 * <tbody>
	 * <tr>
	 * <td>Integer</td>
	 * <td>Integer</td>
	 * <td>String</td>
	 * <td>Boolean</td>
	 * <td>Integer</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * @throws SQLException
	 * @throws NamingException
	 */
	public static List<Object[]> getAssociationTables(FactoryConnectionDB fcdb) throws SQLException, NamingException {
		List<Object[]> result = new LinkedList<Object[]>();
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		String characterBegin = generateSQL.getCharacterBegin();
		String characterEnd = generateSQL.getCharacterEnd();
		String sqlQuery = "SELECT " + characterBegin + "idtoDominio" + characterEnd + "," + characterBegin + "idtoRango" + characterEnd + ",t_name," + characterBegin + "propertyColumn" + characterEnd + "," + characterBegin + "idProperty" + characterEnd + " FROM t_link ORDER BY " + characterBegin + "idtoDominio" + characterEnd + "";

		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connectionDB.getBusinessConn().createStatement();
			resultSet = statement.executeQuery(sqlQuery);
			while (resultSet.next()) {
				int idDominio = resultSet.getInt(1);
				int idRango = resultSet.getInt(2);
				String tableName = resultSet.getString(3);
				boolean hasPropertyColumn = resultSet.getBoolean(4);
				int idProperty = resultSet.getInt(5);
				result.add(new Object[] { idDominio, idRango, tableName, hasPropertyColumn, idProperty});
			}
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
			if (connectionDB!=null) {
				fcdb.close(connectionDB);
			}
		}
		return result;
	}

	/**
	 * Devuelve un mapa con los nombres de las clases referenciados por su IDTO.
	 * 
	 * @param fcdb
	 *            Objeto que permite conectarse a la base de datos.
	 * @return Mapa de los nombres referenciados por su idto. Si no se han encontrado clases el mapa estara vacio.
	 * @throws SQLException
	 * @throws NamingException
	 */
	
	public static int getIdto(FactoryConnectionDB fcdb,String clase) throws SQLException,NamingException {
		String tablaClase= fcdb.getGestorDB().equals("mySQL") ? "clases":"clase";
		String sqlQuery = "SELECT id FROM "+tablaClase+ " WHERE rdn='"+clase+"';";
		
		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connectionDB.getBusinessConn().createStatement();
			resultSet = statement.executeQuery(sqlQuery);
			if (!resultSet.next()) {
				return 0;
			}
			return resultSet.getInt(1);			
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
			if (connectionDB!=null) {
				fcdb.close(connectionDB);
			}
		}
	}

	
	public static Hashtable<Integer, String> getClassNames(FactoryConnectionDB fcdb) throws SQLException,
			NamingException {
		// Consultamos primero el mapa de nombres de clases por empresa, para asegurarnos de que no hemos leido el dato
		// ya.
		String businessAndGestor=fcdb.getBusiness()+"#"+fcdb.getGestorDB();
		Hashtable<Integer, String> classNames = classNamesByBusiness.get(businessAndGestor);
		if (classNames != null) {
			return classNames;
		}
		classNames = new Hashtable<Integer, String>();
		String sqlQuery = "SELECT id, rdn FROM clase WHERE id > 0 ORDER BY id;";

		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connectionDB.getBusinessConn().createStatement();
			resultSet = statement.executeQuery(sqlQuery);
			if (!resultSet.next()) {
				return classNames;
			}
			do {
				classNames.put(resultSet.getInt(1), resultSet.getString(2));
			} while (resultSet.next());
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
			if (connectionDB!=null) {
				fcdb.close(connectionDB);
			}
		}

		return classNames;
	}

	/**
	 * Devuelve un mapa con todas las propiedades que tienen inversa de tal manera que si por ejemplo tenemos que las
	 * propiedades 213 y 12 son inversas entre si en el mapa apareceraan como: <center>
	 * <table border="1">
	 * <theader>
	 * <th>Clave</th>
	 * <th>Valor</th>
	 * </theader> <tbody>
	 * <tr>
	 * <td>12</td>
	 * <td>213</td>
	 * </tr>
	 * <tr>
	 * <td>213</td>
	 * <td>12</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * </center>
	 * 
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a la base de datos.
	 * @return Mapa con las referencias a las propiedades a usar en las tablas asociacion cuando nos encontramos con una
	 *         inversa.
	 * @throws SQLException
	 *             Si se produce algun error en la ejecucion de la sentencia SQL
	 * @throws NamingException
	 */
	public static Hashtable<Integer, Integer> getInversePropertiesMap(FactoryConnectionDB fcdb) throws SQLException,
			NamingException {
		Hashtable<Integer, Integer> inversePropertiesMap = new Hashtable<Integer, Integer>();
		String sqlQuery = "SELECT id, id_inversa FROM propiedad_objeto WHERE id_inversa IS NOT NULL AND id > id_inversa;";

		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			//System.err.println("ConnectionDB " + connectionDB);
			//System.err.println("BusinessConn: " + connectionDB.getBusinessConn());
			statement = connectionDB.getBusinessConn().createStatement();
			resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {
				int prop = resultSet.getInt(1);
				int inv = resultSet.getInt(2);
				inversePropertiesMap.put(prop, inv);
				inversePropertiesMap.put(inv, prop);
			}
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
			if (connectionDB!=null) {
				fcdb.close(connectionDB);
			}
		}

		return inversePropertiesMap;
	}

	/**
	 * Se encarga de obtener todas las propiedades asociadas a la clase indicada devolviendo una lista de objetos que
	 * contienen toda la informacion relevante de las mismas.
	 * 
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a la base de datos.
	 * @param classIdto
	 *            número que identifica a la clase de la que queremos saber las propiedades
	 * @return Lista con los objetos que contienen la informacion de las propiedades. Si está vacía es que la clase no
	 *         tiene propiedades.
	 * @throws SQLException
	 * @throws NamingException
	 */
	public static List<PropertyInfo> getPropertiesForClass(FactoryConnectionDB fcdb, int classIdto)
			throws SQLException, NamingException {
		// Comprobamos si ya se han cacheado los nombres asociados a cada idProperty para esta base de datos, y si no es
		// asi, lo hacemos
		String businessAndGestor=fcdb.getBusiness()+"#"+fcdb.getGestorDB();
		if (propertiesNamesByBusiness.get(businessAndGestor) == null) {
			getPropertiesNames(fcdb);
		}
		// Objeto a devolver y objeto que indica los números de las properties que ya se han añadido.
		Hashtable<Integer, PropertyInfo> propertiesTable = new Hashtable<Integer, PropertyInfo>();
		// Obtenemos los nombres asociados a cada idProperty para esta base de datos.
		Hashtable<Integer, String> propertiesNames = propertiesNamesByBusiness.get(businessAndGestor);
		String sqlQuery = "SELECT DISTINCT property,op,qmin,qmax,valuecls FROM instances WHERE idto=" + classIdto
				+ " AND op IS NOT NULL ORDER BY property;";

		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connectionDB.getBusinessConn().createStatement();
			resultSet = statement.executeQuery(sqlQuery);
			while (resultSet.next()) {
				// Obtenemos todos los datos de la property
				int idProperty = resultSet.getInt(1);
				String operation = resultSet.getString(2);
				Integer minCardinality, maxCardinality;
				minCardinality =  (int) resultSet.getDouble(3);
				if (resultSet.wasNull()) {
					minCardinality = 0;
				}
				maxCardinality = (int) resultSet.getDouble(4);
				if (resultSet.wasNull()) {
					maxCardinality = Integer.MAX_VALUE;
				}
				Integer propertyType = resultSet.getInt(5);
				if (resultSet.wasNull()) {
					propertyType = null;
				}
				// Consultamos si ya habiamos añadido esta property para ver si tenemos que modificar su cardinalidad y
				// si no la creamos nueva
				PropertyInfo propertyInfo = propertiesTable.get(idProperty);
				if (propertyInfo == null) {
					String propertyName = propertiesNames.get(idProperty);
					if (propertyName == null){
						System.err.println("No se conoce el nombre de la propiedad " + idProperty);
					}
					propertyInfo = new PropertyInfo(propertyName, idProperty, minCardinality,
							maxCardinality);
					propertiesTable.put(idProperty, propertyInfo);
				} 
				if (propertyInfo != null && operation.equals("CAR")) {
					propertyInfo.setMinCardinality(minCardinality);
					propertyInfo.setMaxCardinality(maxCardinality);
				}
				if (propertyType != null) {
					propertyInfo.addPropertyType(propertyType);
				}
			}
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
			if (connectionDB!=null) {
				fcdb.close(connectionDB);
			}
		}
		// Generamos el objeto de retorno.
		List<PropertyInfo> returnList = new LinkedList<PropertyInfo>();
		returnList.addAll(propertiesTable.values());
		return returnList;
	}

	/**
	 * Consulta todas las clases que se instancian en la aplicacion que no sean de tipo basico.
	 * 
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a la base de datos.
	 * @return Lista con los identificadores de todas las clases que se referencian en la base de datos. Si está vacía
	 *         es que no hay clases referenciadas.
	 * @throws SQLException
	 * @throws NamingException
	 */
	public static Set<Integer> getReferencedClasses(FactoryConnectionDB fcdb) throws SQLException, NamingException {
		Set<Integer> classes = new HashSet<Integer>();
		//String sqlQuery = "SELECT DISTINCT valuecls FROM instances WHERE valuecls IS NOT NULL AND valuecls > 0 ORDER BY valuecls;";
		
		//Quitamos todos lo que no queremos que se procese para la creación de tablas.
		//SYSTEM_CLASS lo quitamos porque la mayoria de esas clases heredan de ella, y si no se quita luego se intenta procesar sus especializadas
		String sqlQuery = "SELECT DISTINCT idto FROM instances WHERE name<>'SYSTEM_CLASS' and " +
				"idto not in("+Constants.IDTO_UTASK+","+Constants.IDTO_ACTION+","+Constants.IDTO_ACTION_BATCH+","+Constants.IDTO_ACTION_PARAMS+","+Constants.IDTO_ACTION_BATCH_PARAMS+","+Constants.IDTO_AUX_PARAMS+","+Constants.IDTO_PARAMS+","+Constants.IDTO_IMPORTEXPORT_PARAMS+","+Constants.IDTO_IMPORT+","+Constants.IDTO_EXPORT+","+Constants.IDTO_RESULT_BATCH+") and idto not in(" +
				"Select Distinct id_to from t_herencias where id_to_padre in("+Constants.IDTO_UTASK+","+Constants.IDTO_ACTION+","+Constants.IDTO_ACTION_BATCH+","+Constants.IDTO_ACTION_PARAMS+","+Constants.IDTO_ACTION_BATCH_PARAMS+","+Constants.IDTO_AUX_PARAMS+","+Constants.IDTO_PARAMS+","+Constants.IDTO_IMPORTEXPORT_PARAMS+","+Constants.IDTO_IMPORT+","+Constants.IDTO_EXPORT+"))" +
						"ORDER BY idto;";
		//System.out.println(sqlQuery);
		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connectionDB.getBusinessConn().createStatement();
			resultSet = statement.executeQuery(sqlQuery);
			while (resultSet.next()) {
				int classIdto = resultSet.getInt(1);
				if (!Constants.isDataType(classIdto)) {
					classes.add(classIdto);
				}
			}

		} finally {
			if (statement != null) {
				statement.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
			if (connectionDB!=null) {
				fcdb.close(connectionDB);
			}
		}
		return classes;
	}

	/**
	 * Indica si la clase pasada por parámetro es abstrata o no.
	 * 
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a la base de datos.
	 * @param classIdto
	 *            Identificador de la clase de la que queremos consultar si es abstracta.
	 * @return <code>true</code> si es una clase abstracta.
	 * @throws SQLException
	 * @throws NamingException
	 */
	public static boolean isAbstractClass(FactoryConnectionDB fcdb, int classIdto) throws SQLException, NamingException {
		boolean result = false;
		String gestorDB = fcdb.getGestorDB();
		String sqlQuery = null;
		if (gestorDB.equals(GestorsDBConstants.postgreSQL))
			sqlQuery = "SELECT abstracta FROM clase WHERE id=" + classIdto;
		else if (gestorDB.equals(GestorsDBConstants.mySQL))
			sqlQuery = "select * from access where idto=" + classIdto + " and accesstype=" + Constants.ACCESS_ABSTRACT;
		
		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connectionDB.getBusinessConn().createStatement();
			resultSet = statement.executeQuery(sqlQuery);
			// Si el tamaño es vacio, significa que no se ha declarado como abstracta la clase en ningun momento.
			if (!resultSet.next()) {
				return result;
			}else{
				if (gestorDB.equals(GestorsDBConstants.postgreSQL))
					result = resultSet.getBoolean(1);
				else if (gestorDB.equals(GestorsDBConstants.mySQL))
					result = true;
			}
			
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
			if (connectionDB!=null) {
				fcdb.close(connectionDB);
			}
		}

		return result;
	}

	/**
	 * Devuelve el conjunto de propiedades que son estructurales en la base de datos a la que apunta el
	 * FactoryConnectionDB
	 * 
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a la base de datos.
	 * @return Conjunto de las propiedades que son estructurales. Si no hay estructurales estara vacía.
	 * @throws SQLException
	 * @throws NamingException
	 */
	public static Set<Integer> getStructuralProperties(FactoryConnectionDB fcdb) throws SQLException, NamingException {
		String businessAndGestor=fcdb.getBusiness()+"#"+fcdb.getGestorDB();
		Set<Integer> structuralProperties = structuralPropertiesByBusiness.get(businessAndGestor);
		if (structuralProperties == null) {
			getPropertiesNames(fcdb);
		}
		return structuralPropertiesByBusiness.get(businessAndGestor);
	}
	
	
	public static ArrayList<PropertyUnit> getPropertyUnits(FactoryConnectionDB fcdb) throws SQLException, NamingException{
		ArrayList<PropertyUnit> res= new ArrayList<PropertyUnit>();
		String sql="select u.rdn as unidad,um.rdn as magnitud,factor_conversion_exponente_10 as factor from unidades as u inner join unidad_magnitud as um on u.magnitud=um.\"tableId\"";
		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		ResultSet resultSet = null;
		try{
			statement = connectionDB.getBusinessConn().createStatement();
			resultSet = statement.executeQuery(sql);
			// Si el tamaño es vacio, significa que no se ha declarado como abstracta la clase en ningun momento.
			while (resultSet.next()) {
				PropertyUnit pu=new PropertyUnit();
				pu.unit=resultSet.getString(1);
				pu.magnitude=resultSet.getString(2);
				pu.exponent=resultSet.getInt(3);	
				res.add(pu);
			}
			
		}finally {
			if (statement != null) {
				statement.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
			if (connectionDB!=null) {
				fcdb.close(connectionDB);
			}
		}
				
		return res;
	}
	
	public static HashMap<String,ArrayList<PredefinedUnit>> getPropertyMagnitude(FactoryConnectionDB fcdb) throws SQLException, NamingException{
		HashMap<String,ArrayList<PredefinedUnit>> res=new HashMap<String,ArrayList<PredefinedUnit>>();
		String sql=
			"select pd.rdn,clase.rdn,u.rdn as unidad,um.rdn as magnitud,factor_conversion_exponente_10 as factor 			\n"+
			"from 																											\n"+

			"	unidad_predefinida as up 																	left join 		\n"+
			"	(\"unidad_predefinida#clase\" prec 															inner join 		\n"+
			"	clase		on clase.\"tableId\"=prec.\"claseId\"															\n"+
			"	)			on \"unidad_predefinidaId\"=up.\"tableId\"										inner join		\n"+
			"	\"unidad_predefinida#propiedad_dato\" prep on prep.\"unidad_predefinidaId\"=up.\"tableId\"	inner join		\n"+
			"	propiedad_dato as pd on pd.\"tableId\"=prep.\"propiedad_datoId\"							inner join		\n"+
			"	unidades as u on up.unidad=u.\"tableId\" 													inner join		\n"+
			"	unidad_magnitud as um on u.magnitud=um.\"tableId\"															";
		
		ResultSet rs=null;
		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		try{
					
		statement = connectionDB.getBusinessConn().createStatement();
		rs = statement.executeQuery(sql);
				
		while(rs.next()){
			PredefinedUnit predunit=new PredefinedUnit();
			predunit.property=rs.getString(1);
			predunit.classname=rs.getString(2);
			predunit.unit=rs.getString(3);
			String magnitude=rs.getString(4);
			ArrayList<PredefinedUnit> preArr=res.get(magnitude);
			if(preArr==null){
				preArr=new ArrayList<PredefinedUnit>();
				res.put(magnitude, preArr);
			}
			preArr.add(predunit);
		}	
		}finally{
			if (statement != null) {
				statement.close();
			}
			if (rs != null) {
				rs.close();
			}
			if (connectionDB!=null) {
				fcdb.close(connectionDB);
			}
		}
		return res;
	}
	

	/**
	 * Devuelve el conjunto de propiedades que son estructurales compartidas en
	 * la base de datos a la que apunta el FactoryConnectionDB.
	 * 
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a la base de datos.
	 * @return Conjunto con los identificadores de las propiedades que están
	 *         declaradas como compartidas.
	 * @throws SQLException
	 * @throws NamingException
	 */
	public static Set<Integer> getSharedProperties(FactoryConnectionDB fcdb) throws SQLException, NamingException {
		String businessAndGestor=fcdb.getBusiness()+"#"+fcdb.getGestorDB();
		Set<Integer> sharedProperties = sharedPropertiesByBusiness.get(businessAndGestor);
		if (sharedProperties == null){
			getPropertiesNames(fcdb);
			sharedProperties = sharedPropertiesByBusiness.get(businessAndGestor);
		}
		return sharedProperties; 
	}

	/**
	 * Devuelve un mapa de todas las clases especializadas organizadas por la clase a la que especializan.
	 * @param configurationMode Tiene en cuenta o no las clases excluidas al devolver el resultado
	 * 
	 * @return Mapa de especializados. Si está vacio es que no hay especializados.
	 * @throws NamingException
	 * @throws SQLException
	 */
	public static Hashtable<Integer, Set<Integer>> getSpecializedClassesMap(FactoryConnectionDB fcdb, boolean configurationMode)
			throws SQLException, NamingException {
		Hashtable<Integer, Set<Integer>> result = new Hashtable<Integer, Set<Integer>>();
		String sqlQuery = "SELECT id_to,id_to_padre FROM t_herencias t ";
		if(!configurationMode){
			GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
			String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
			sqlQuery+=" INNER JOIN clase c ON (t.id_to=c.id)" +
			" LEFT JOIN " + cB + "clase_excluída" + cE + " ce ON (c." + cB + "tableId" + cE + "=ce.dominio)" +
			" WHERE id_to > 0 AND ce." + cB + "tableId" + cE + " IS NULL";
		}else{
			sqlQuery+="WHERE id_to > 0;";
		}

		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connectionDB.getBusinessConn().createStatement();
			resultSet = statement.executeQuery(sqlQuery);
			while (resultSet.next()) {
				int idtoChild = resultSet.getInt(1);
				int idtoParent = resultSet.getInt(2);
				Set<Integer> childClasses = result.get(idtoParent);
				if (childClasses == null) {
					childClasses = new HashSet<Integer>();
					result.put(idtoParent, childClasses);
				}
				childClasses.add(idtoChild);
			}
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
			if (connectionDB!=null) {
				fcdb.close(connectionDB);
			}
		}

		return result;
	}
	

	public static void execute(FactoryConnectionDB fcdb, String sql) throws SQLException, NamingException {
		//System.out.println("DBQueries.execute() : " + sql);
		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		try {
			statement = connectionDB.getBusinessConn().createStatement();
			statement.setFetchSize(100);
			statement.execute(sql);
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (connectionDB!=null) {
				fcdb.close(connectionDB);
			}
		}
	}

	/**
	 * Las inserciones con PostgreSQL se van a hacer con la clausula RETURNING
	 * para devolver el tableId con el que se han insertado los nuevos datos.
	 * 
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a base de datos.
	 * @param sql
	 *            Sentencia SQL a ejecutar.
	 * @return TableId asignado al nuevo registro insertado o <code>null</code>
	 *         si no se ha asignado ninguno o ha habido un error.
	 * @throws SQLException
	 * @throws NamingException
	 */
	public static Integer executePostgreInsert(FactoryConnectionDB fcdb, String sql) throws SQLException, NamingException{
		System.out.println("DBQueries.executePostgreInsert() : " + sql);
		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		ResultSet resultSet = null;
		Integer id = null;
		try {
			statement = connectionDB.getBusinessConn().createStatement();
			resultSet = statement.executeQuery(sql);
			if (resultSet.next()) {
				id = resultSet.getInt(1);
				if (resultSet.wasNull()){
					id = null;
				}
			}
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
			if (connectionDB!=null) {
				fcdb.close(connectionDB);
			}
		}
		return id;
	}
	
	public static int executeUpdate(FactoryConnectionDB fcdb, String sql) throws SQLException, NamingException{
		System.out.println("DBQueries.executeUpdate() : " + sql);
		int rows = 0;
		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connectionDB.getBusinessConn().createStatement();
			rows = statement.executeUpdate(sql);
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
			if (connectionDB!=null) {
				fcdb.close(connectionDB);
			}
		}
		return rows;
	}

	/**
	 * Obtiene los nombres asociados a cada identificador de propiedad para la base de datos a la que apunta el objeto
	 * de conexion a base de datos en ese momento.
	 * 
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a la base de datos y que indica de que base de datos tenemos que
	 *            consultar las propiedades.
	 * @return Mapa con los nombres de las propiedades indexadas por su identificardor numero.
	 * @throws SQLException
	 * @throws NamingException
	 */
	public static Hashtable<Integer,String> getPropertiesNames(FactoryConnectionDB fcdb) throws SQLException, NamingException {
		int business = fcdb.getBusiness();
		String businessAndGestor=fcdb.getBusiness()+"#"+fcdb.getGestorDB();
		Hashtable<Integer, String> propertiesNames = propertiesNamesByBusiness.get(businessAndGestor);
		if (propertiesNames != null){
			return propertiesNames;
		}else{
			propertiesNames =  new Hashtable<Integer, String>();
		}
		Set<Integer> structuralProperties = new HashSet<Integer>();
		Set<Integer> sharedProperties = new HashSet<Integer>();
		String sqlQuery = "SELECT id,rdn,cat FROM v_propiedad;";

		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connectionDB.getBusinessConn().createStatement();
			resultSet = statement.executeQuery(sqlQuery);
			while (resultSet.next()) {
				int idProp = resultSet.getInt(1);
				propertiesNames.put(idProp, resultSet.getString(2));
				int categoryInt = resultSet.getInt(3);
				Category category = new Category(categoryInt);
				if (category.isStructural()) {
					structuralProperties.add(idProp);
				}
				if (category.isShared()){
					sharedProperties.add(idProp);
				}
			}
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
			if (connectionDB!=null) {
				fcdb.close(connectionDB);
			}
		}
		propertiesNamesByBusiness.put(businessAndGestor, propertiesNames);
		structuralPropertiesByBusiness.put(businessAndGestor, structuralProperties);
		sharedPropertiesByBusiness.put(businessAndGestor, sharedProperties);
		return propertiesNames;
	}

	/**
	 * Realiza la consulta a base de datos indicada y monta la matriz para
	 * representar los datos obtenidos.
	 * 
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a base de datos.
	 * @param sql
	 *            Consulta SQL que queremos que se ejecute.
	 * @return Cada uno de los elementos de la lista representa un registro y
	 *         registro viene representado por una lista donde cada elemento
	 *         representara un dato.<br>
	 *         Todos los datos se habran representado como una cadena. Si en
	 *         base de datos el dato era NULL, la posicion en la cadena
	 *         contendra un <code>null</code>. Los datos se devuelven en el
	 *         orden que venian especificados en la consulta.
	 * @throws SQLException
	 * @throws NamingException
	 */
	
	public static List<List<String>> executeQuery(FactoryConnectionDB fcdb, String sql) throws SQLException, NamingException{
		return executeQuery(fcdb,sql,false);
	}
	public static List<List<String>> executeQuery(FactoryConnectionDB fcdb, String sql,boolean update) throws SQLException, NamingException{
		System.out.println("DBQueries.executeQuery() [Consulta: " + sql + "]");
		List<List<String>> result = new LinkedList<List<String>>();
		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connectionDB.getBusinessConn().createStatement();
			resultSet = statement.executeQuery(sql);
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int numberOfColumns = resultSetMetaData.getColumnCount();
			while (resultSet.next()) {
				List<String> rowValues = new ArrayList<String>();
				for (int column = 1 ; column <= numberOfColumns ; column ++){
					String columnValue;
//					System.out.println("columnType " + resultSetMetaData.getColumnType(column));
					switch (resultSetMetaData.getColumnType(column)) {
					case Types.BOOLEAN:
						columnValue = String.valueOf(resultSet.getBoolean(column));
						break;
					case Types.BIT:
						columnValue = String.valueOf(resultSet.getBoolean(column));
						break;
					case Types.INTEGER:
						columnValue = String.valueOf(resultSet.getInt(column));
						break;
					case Types.DECIMAL:
						columnValue = String.valueOf(resultSet.getInt(column));
						break;
					case Types.NUMERIC:
						columnValue = String.valueOf(resultSet.getInt(column));
						break;
					case Types.BIGINT:
						columnValue = String.valueOf(resultSet.getLong(column));
						break;
					case Types.DOUBLE:
						columnValue = String.valueOf(resultSet.getDouble(column));
						break;
					case Types.FLOAT:
						columnValue = String.valueOf(resultSet.getFloat(column));
						break;
					case Types.VARCHAR:
						columnValue = resultSet.getString(column);
						break;
					case Types.VARBINARY:
						columnValue = resultSet.getString(column);
						break;
					case Types.BINARY:
						InputStream is = resultSet.getBinaryStream(column);
						if(is==null){
							columnValue = null;
						}else{
						BufferedReader reader = new BufferedReader(new InputStreamReader(is));
						try {
							columnValue = "";
							boolean entra = false;
							String line;
							while((line = reader.readLine()) != null){
								entra = true;
								columnValue += line;
							}
							if (!entra) {
								columnValue = null;
							}
						} catch (IOException e) {
							columnValue = null;
						}
						}
						break;
					case Types.TIMESTAMP:
						columnValue = resultSet.getString(column);
						break;
					case Types.DATE:
						// XXX Sin implementar. En base de datos todavia no hay Date.
						columnValue = null;
						break;
					default:
						columnValue = null;
						break;
					}
					if (resultSet.wasNull()){
						columnValue = null;
					}
					rowValues.add(columnValue);
				}
				result.add(rowValues);
			}
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
			if (connectionDB!=null) {
				fcdb.close(connectionDB);
			}
		}
		
		return result;
	}

	/**
	 * Ejecuta el script del fichero dado.
	 * 
	 * @param scriptFile
	 *            Objeto que representa el fichero donde está el script. No se hacen comprobaciones de que exista pues
	 *            se supone que ya se ha verificado con anterioridad.
	 * @return <code>true</code> si se ha conseguido ejecutar el script con exito.
	 * @throws SQLException
	 * @throws NamingException
	 */
	public static void executeScript(FactoryConnectionDB fcdb,File scriptFile) throws SQLException, NamingException{
		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		ResultSet resultSet = null;
		System.out.println(fcdb.getGestorDB());
		List<String> scriptLines = parseFile(scriptFile,fcdb.getGestorDB().equals("mySQL"));
		try {
			statement = connectionDB.getBusinessConn().createStatement();
			for (String sql : scriptLines) {
				System.out.println(sql);
				statement.execute(sql);
			}
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
			if (connectionDB!=null) {
				fcdb.close(connectionDB);
			}
		}
	}
	
	/**
	 * Ejecuta el script del fichero dado.
	 * 
	 * @param scriptFile
	 *            Objeto que representa el fichero donde está el script. No se hacen comprobaciones de que exista pues
	 *            se supone que ya se ha verificado con anterioridad.
	 * @return <code>true</code> si se ha conseguido ejecutar el script con exito.
	 * @throws SQLException
	 * @throws NamingException
	 */
	public static void executeScript(Connection conn,File scriptFile) throws SQLException, NamingException{
		Statement statement = null;
		ResultSet resultSet = null;
		List<String> scriptLines = parseFile(scriptFile,false);
		try {
			statement = conn.createStatement();
			for (String sql : scriptLines) {
				statement.execute(sql);
			}
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
		}
	}
	
	/**
	 * Borra la informacion almacenada para una base de datos.
	 * 
	 * @param bns
	 *            Identificador numero de la base de datos de la que queremos
	 *            dejar de almacenar informacion.
	 */
	public static void removeBnsInfo(int bns,String gestor){
		String businessAndGestor=bns+"#"+gestor;
		classNamesByBusiness.remove(businessAndGestor);
		propertiesNamesByBusiness.remove(businessAndGestor);
		sharedPropertiesByBusiness.remove(businessAndGestor);
		structuralPropertiesByBusiness.remove(businessAndGestor);
	}

	/**
	 * Parsea el script para construir una lista de todas las sentencias que se han de ejecutar.
	 * 
	 * @param scriptFile
	 *            Objeto que identifica al fichero donde se encuentra el script
	 * @return Lista con todas las sentencias que contiene el script, donde cada uno de los elementos de la lista es una
	 *         sentencia completa.
	 */
	public static List<String> parseFile(File scriptFile,boolean split) {
		List<String> sqlStatements = new LinkedList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(scriptFile));
			String sql = "";
			String line;
			if(split){
				while ((line = reader.readLine()) != null){
					if (line.length() == 0 || line.charAt(0) == '-' || line.charAt(0) == '/'){
						continue;
					}
					sql += " " + line;
					if (sql.charAt(sql.length() - 1) == ';'){
						sqlStatements.add(sql);
						sql = "";
					}
				}
			}else{
				while ((line = reader.readLine()) != null){
					sql += "\n"+line;
				}
				sqlStatements.add(sql);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("[IO ERROR] Error parseando el fichero de script.");
		} catch (IOException e) {
			System.err.println("[IO ERROR] Error parseando el fichero de script");
			e.printStackTrace();
		}
		return sqlStatements;
	}
	
	
}
