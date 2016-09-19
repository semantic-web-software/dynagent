package dynagent.server.gestorsDB;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import dynagent.common.Constants;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.IQueryInfo;
import dynagent.server.dbmap.Table;
import dynagent.server.dbmap.TableColumn;
import dynagent.server.ejb.FactoryConnectionDB;

public class GenerateSQL implements Serializable{

	private static final long serialVersionUID = 1314181695827682368L;
	private String gestor;
	private static int indexNumber = 1;
	
	private static int savePointIndex = 0;

	public GenerateSQL(String gestor) {
		this.gestor = gestor;
	}

	public String parseStringToInsert(String value) {
		String dev = null;
		if (value == null || value.equalsIgnoreCase("null")){
			dev = "NULL";
		}else if (gestor.equals(GestorsDBConstants.mySQL) || gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = "'" + value./*replaceAll("\\\\", "\\\\\\\\").*/replaceAll("'", "''") + "'";//COMENTAMOS EL REPLACEALL PORQUE PROVOCA QUE SE GUARDE SIEMPRE EN BASE DE DATOS CON EL DOBLE DE BARRAS QUE QUEREMOS INCLUSO CUANDO SOLO QUEREMOS UNA BARRA. NO ESTA CLARO PORQUE ESTO ERA ANTES NECESARIO, PERO AHORA NO TIENE SENTIDO
		} else if (gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = "'" + value.replaceAll("'", "''") + "'";
		}
		return dev;
	}
	
	public String startAutonumeric() {
		String dev = null;
		if (gestor.equals(GestorsDBConstants.mySQL)) {
			dev = "insert into o_reg_instancias_index(autonum,id_o,id_to,rdn) values(11000,11000,0,'inicio');";
			//dev = "ALTER TABLE o_reg_instancias_index AUTO_INCREMENT = 11000;"; cuando se reinicia deja de funcionar
		} else if (gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = "DBCC CHECKIDENT ('O_Reg_Instancias_Index', RESEED, 11000)";
		}/* else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = "CREATE SEQUENCE o_reg_instancias_index_seq;" +
					"SELECT setval( 'o_reg_instancias_index_seq', ( SELECT MAX(autonum) FROM o_reg_instancias_index ) );" +
					"ALTER TABLE o_reg_instancias_index ALTER COLUMN autonum SET DEFAULT nextval( 'o_reg_instancias_index_seq' )";
		}*/
		return dev;
	}

	/*public String getRegularExpresion(String etiqueta, String val, boolean positive) {
		String dev = null;
		if (gestor.equals(GestorsDBConstants.mySQL)) {
			if (positive)
				dev = etiqueta + " REGEXP " + "'" + val.replaceAll("'", "''")
						+ "'";
			else
				dev = "NOT(" + etiqueta + " REGEXP " + "'"
						+ val.replaceAll("'", "''") + "')";
		} else if (gestor.equals(GestorsDBConstants.SQLServer)) {
			if (positive)
				dev = "dbo.RegExMatch(" + etiqueta + "," + "'"
						+ val.replaceAll("'", "''") + "'" + ")=1";
			else
				dev = "dbo.RegExMatch(" + etiqueta + "," + "'"
						+ val.replaceAll("'", "''") + "'" + ")=0";
		}
		return dev;
	}*/

	public String getIsolationReadUncommited() {
		String dev = null;
		if (gestor.equals(GestorsDBConstants.mySQL)) {
			dev = "SET GLOBAL TRANSACTION ISOLATION LEVEL READ UNCOMMITTED";
		} else if (gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED";
		}
		return dev;
	}

	/*
	 * public String getIsolationRepeatableRead() { String dev = null; if
	 * (gestor.equals(GestorsDBConstants.mySQL)) { dev =
	 * "SET GLOBAL TRANSACTION ISOLATION LEVEL REPEATABLE READ"; } else if
	 * (gestor.equals(GestorsDBConstants.SQLServer)) { dev =
	 * "SET TRANSACTION ISOLATION LEVEL REPEATABLE READ"; } return dev; }
	 */
	public String getEncryptFunction(String key, String dataToEncr) {
		String dev = null;
		if (gestor.equals(GestorsDBConstants.mySQL)) {
			dev = "AES_ENCRYPT('" + dataToEncr.replaceAll("'", "''") + "', '" + key + "')";
		} else if (gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = "ENCRYPTBYPASSPHRASE('" + key + "', '" + dataToEncr.replaceAll("'", "''") + "')";
		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = "encrypt('" + dataToEncr.replaceAll("'", "''") + "','" + key + "','aes')";
		}
		return dev;
	}
	
	public Integer getDefaultPort() {
		Integer port = null;
		if (gestor.equals(GestorsDBConstants.mySQL)) {
			port = 3306;
		} else if (gestor.equals(GestorsDBConstants.SQLServer)) {
			port = 1433;
		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			port = 5432;
		}
		return port;
	}
	
	public String getDecryptFunction(String key, String field) {
		String dev = null;
		if (gestor.equals(GestorsDBConstants.mySQL)) {
			dev = "AES_DECRYPT(" + field + ",'" + key + "')";
		} else if (gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = "CONVERT (VARCHAR(50), DECRYPTBYPASSPHRASE('" + key + "'," + field + "))";
		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = "decrypt(cast (" + field + " as bytea),'" + key + "','aes')";
		}
		return dev;
	}
	public String getDecryptData(ResultSet rs, int position) throws SQLException {
		String dev = null;
		if (gestor.equals(GestorsDBConstants.mySQL) || gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = rs.getString(position);
		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = new String(rs.getBytes(position));
		}
		return dev;
	}

	/*
	 * public String addIsolation(String sql) { String dev =
	 * getIsolationReadUncommited() + "\n"; dev += sql + ";\n"; dev +=
	 * getIsolationRepeatableRead(); return dev; } public String
	 * addIsolation(StringBuffer sql) { StringBuffer dev = new
	 * StringBuffer(getIsolationReadUncommited()+ "\n"); dev.append(sql +
	 * ";\n"); dev.append(getIsolationRepeatableRead()); return dev.toString();
	 * }
	 */

	public String getCollateUpperCase() {
		String dev = "";
		if (gestor.equals(GestorsDBConstants.mySQL)) {
			dev = " COLLATE Latin1_General_CS";
		} else if (gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = " COLLATE SQL_Latin1_General_CP1_CS_AS";
		}
		return dev;
	}

	public String getCharacterDB() {
		String dev = null;
		if (gestor.equals(GestorsDBConstants.mySQL)) {
			dev = "";
		} else if (gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = ".dbo";
		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = ".public";
		}
		return dev;
	}

	public String getCharacterBegin() {
		String dev = null;
		if (gestor.equals(GestorsDBConstants.mySQL)) {
			dev = "`";
		} else if (gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = "[";
		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = "\"";
		}
		return dev;
	}

	public String getCharacterEnd() {
		String dev = null;
		if (gestor.equals(GestorsDBConstants.mySQL)) {
			dev = "`";
		} else if (gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = "]";
		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = "\"";
		}
		return dev;
	}

	public String getIdentityInsertOn() {
		String dev = "";
		if (gestor.equals(GestorsDBConstants.SQLServer) || gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = "SET IDENTITY_INSERT O_Reg_Instancias_Index ON";
		}
		return dev;
	}

	public String getIdentityInsertOff() {
		String dev = "";
		if (gestor.equals(GestorsDBConstants.SQLServer) || gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = "SET IDENTITY_INSERT O_Reg_Instancias_Index OFF";
		}
		return dev;
	}
	
	public String getLike() {
		String dev = null;
		if (gestor.equals(GestorsDBConstants.mySQL) || gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = " LIKE ";
		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = " ILIKE ";
		}
		return dev;
	}
	
	public static String getLastTableId(String tableName) throws SQLException, NamingException {
		return "select max(\"tableId\") from \"" + tableName + "\";";
	}
	
	public String getNextValRemote(String dbRemote, String table, String params, String conditions, String dataTypes,String pwd) {
		String dev = null;
		if (gestor.equals(GestorsDBConstants.mySQL) || gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = "SELECT " + params + " FROM " + dbRemote + getCharacterDB() + "." + table;
			if (conditions!=null)
				dev += " WHERE " + conditions;
		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = "SELECT * FROM dblink('dbname=" + dbRemote + " user=dynagent password="+pwd+"'," +
					"'SELECT " + params + " FROM " + table;
			if (conditions!=null)
				dev += " WHERE " + conditions.replaceAll("'", "''");
			dev += "') as (" + dataTypes + ")";
		}
		return dev;
	}
	public String getSelectRemote(String dbRemote, String table, String params, String conditions, String dataTypes,String pwd) {
		String dev = null;
		if (gestor.equals(GestorsDBConstants.mySQL) || gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = "SELECT " + params + " FROM " + dbRemote + getCharacterDB() + "." + table;
			if (conditions!=null)
				dev += " WHERE " + conditions;
		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = "SELECT * FROM dblink('dbname=" + dbRemote + " user=dynagent password="+pwd+"'," +
					"'SELECT " + params + " FROM " + table;
			if (conditions!=null)
				dev += " WHERE " + conditions.replaceAll("'", "''");
			dev += "') as (" + dataTypes + ")";
		}
		return dev;
	}
	public String getUpdateRemote(String dbRemote, String table, String set, String conditions,String pwd) {
		String dev = null;
		if (gestor.equals(GestorsDBConstants.mySQL) || gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = "UPDATE " + dbRemote + getCharacterDB() + "." + table + 
				" SET " + set + " WHERE " + conditions;
		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = "SELECT * FROM dblink('dbname=" + dbRemote + " user=dynagent password="+pwd+"'," +
					"'UPDATE " + table + " SET " + set.replaceAll("'","''") + " WHERE " + conditions.replaceAll("'","''") + "') as (rows varchar)";
		}
		return dev;
	}
	public String getInsertRemote(String dbRemote, String table, String columns, String values,String pwd) {
		String dev = null;
		if (gestor.equals(GestorsDBConstants.mySQL) || gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = "INSERT INTO " + dbRemote + getCharacterDB() + "." + table + "(" + columns + ") " +
					"VALUES(" + values + ")";
		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = "SELECT * FROM dblink('dbname=" + dbRemote + " user=dynagent password="+pwd+"'," +
					"'INSERT INTO " + table + " (" + columns + ") VALUES ( " + values.replaceAll("'","''") + ")') as (rows varchar)";
		}
		return dev;
	}
	public String getDeleteRemote(String dbRemote, String table, String conditions,String pwd) {
		String dev = null;
		if (gestor.equals(GestorsDBConstants.mySQL) || gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = "DELETE FROM " + dbRemote + getCharacterDB() + "." + table + 
				" WHERE " + conditions;
		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = "SELECT * FROM dblink('dbname=" + dbRemote + " user=dynagent password="+pwd+"'," +
					"'DELETE FROM " + table + " WHERE " + conditions.replaceAll("'","''") + "') as (rows varchar)";
		}
		return dev;
	}

	/**
	 * Construye la sentencia de creación de un índice segun el gestor de base
	 * de datos con el que se está trabajando.
	 * 
	 * @param tableName
	 *            Nombre de la tabla sobre la que se va a crear el índice
	 * @param columnName
	 *            Nombre de la columna sobre la que se va a aplicar el índice
	 * @param unique
	 *            Indica si el índice que se ha de crear ha de exigir unicidad
	 *            de los valores.
	 * @param columnDataType
	 *            Tipo del dato que contiene la columna. Si es tipo entero se
	 *            usara HASH como metodo de indexación si no, se usara el BTREE
	 * @return Cadena con la sentencia de creación del índice.
	 */
	public String getCreateIndexStatement(String tableName, String columnName,
			boolean unique, int columnDataType) {
		String sql = null;
		List<String> columnNames = new ArrayList<String>();
		columnNames.add(columnName);
		if (gestor.equals(GestorsDBConstants.mySQL)){
			sql = getCreateIndexStatementMySQL(tableName, columnNames, unique, columnDataType == Constants.IDTO_INT);
		}else if (gestor.equals(GestorsDBConstants.postgreSQL)){
			sql = getCreateIndexStatementPostgreSQL(tableName, columnNames, unique, columnDataType == Constants.IDTO_INT);
		}
		return sql;
	}

	/**
	 * Construye la sentencia de creación de un índice para PostgreSQL segun los
	 * parámetros indicados.
	 * 
	 * @param tableName
	 *            Nombre de la tabla sobre la que se aplica el índice.
	 * @param columnNames
	 *            Nombres de las columnas que participan en el índice.
	 * @param unique
	 *            Indica si el índice es de tipo UNIQUE
	 * @param useHash
	 *            Indica si se ha de usar el metodo HASH para indexar las
	 *            columnas.
	 * @return Sentencia de creación del índice.
	 */
	private String getCreateIndexStatementPostgreSQL(String tableName,
			List<String> columnNames, boolean unique, boolean useHash) {
		String sql = "CREATE ";
		if (unique){
			sql += "UNIQUE ";
		}
		sql += "INDEX idx_" + indexNumber + " ON "+ getCharacterBegin() + tableName + getCharacterEnd();
		indexNumber ++;
		if (useHash){
			sql += " USING HASH";
		}else{
			sql += " USING BTREE";
		}
		sql += " (";
		boolean first = true;
		for (String columnName : columnNames) {
			if (! first){
				sql += ", ";
			}
			sql += getCharacterBegin() + columnName + getCharacterEnd();
			first = false;
		}
		sql += ");";
		return sql;
	}

	/**
	 * Crea la sentencia de creación de un índice para MySQL con los parámetros
	 * dados.
	 * 
	 * @param tableName
	 *            Nombre de la tabla sobre la que se ha de crear el índice.
	 * @param columnNames
	 *            Nombres de las columnas que participan en el índice
	 * @param unique
	 *            Si el índice es de tipo UNIQUE
	 * @param useHash
	 *            Indica si se ha de usar el metodo HASH para indexar las columnas.
	 * @return Sentencia de creación del índice.
	 */
	private String getCreateIndexStatementMySQL(String tableName,
			List<String> columnNames, boolean unique, boolean useHash) {
		String sql = "CREATE ";
		if (unique) {
			sql += "UNIQUE ";
		}
		sql += "INDEX idx_" + indexNumber + " ON " + getCharacterBegin()
				+ tableName + getCharacterEnd() + " (";
		indexNumber ++;
		boolean first = true;
		for (String columnName : columnNames) {
			if (!first) {
				sql += ", ";
			}
			sql += getCharacterBegin() + columnName + getCharacterEnd();
			first = false;
		}
		if (useHash) {
			sql += " USING HASH;";
		} else {
			sql += " USING BTREE;";
		}
		return sql;
	}

	public String getIsNumeric(String fieldSql) {
		String dev = null;
		if (gestor.equals(GestorsDBConstants.mySQL) || gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = "isnumeric(" + fieldSql + ")=1";
		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = "isnumeric(" + fieldSql + ")";
		}
		return dev;
	}
	public String getLength(String field) {
		String dev = null;
		if (gestor.equals(GestorsDBConstants.mySQL) || gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = "LENGTH(" + field + ")";
		} else if (gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = "LEN(" + field + ")";
		}
		return dev;
	}
	public String getTypeInt() {
		String dev = null;
		if (gestor.equals(GestorsDBConstants.mySQL)) {
			dev = "unsigned";
		} else if (gestor.equals(GestorsDBConstants.SQLServer) || gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = "numeric";
		}
		return dev;
	}
	public String getSubstract() {
		String dev = null;
		if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = "except";
		} else if (gestor.equals(GestorsDBConstants.SQLServer) || gestor.equals(GestorsDBConstants.mySQL)) {
			dev = "minus";
		}
		return dev;
	}

	/**
	 * Dadas las columnas que deben formar parte del índice, mira si se trata de
	 * un índice que exige unicidad de los datos en las columnas y si dichas
	 * columnas son de datos enteros para poder usar como metodo de indexación
	 * el HASH en vez de el BTREE. Una vez sabido esto, obtiene la cadena que
	 * representa la sentencia de creación del índice adapatado al sistema
	 * gestor de base de datos con el que se está trabajando.
	 * 
	 * @param tableName
	 *            Nombre de la tabla con la que se está trabajando y a la que
	 *            hay que aplicar el índice.
	 * @param columns
	 *            Objetos que representan las columnas sobre las que deberaa
	 *            aplicarse el índice.
	 * @return Sentencia de creación del índice.
	 */
	public String getCreateIndexStatement(String tableName,
			ArrayList<TableColumn> columns) {
		boolean unique = true;
		boolean useHash = false;//no usar hash por que no se replica en streaming
		List<String> columnNames = new ArrayList<String>();
		for (TableColumn tableColumn : columns) {
			unique &= tableColumn.isUnique();
			useHash &= tableColumn.getColumnDataType() == Constants.IDTO_INT;
			columnNames.add(tableColumn.getColumnName());
		}
		String result = null;
		if (gestor.equals(GestorsDBConstants.postgreSQL)){
			result = getCreateIndexStatementPostgreSQL(tableName, columnNames, unique, useHash);
		}else if (gestor.equals(GestorsDBConstants.mySQL)){
			result = getCreateIndexStatementPostgreSQL(tableName, columnNames, unique, useHash);
		}
		return result;
	}

	/**
	 * Devuelve la sentencia para insertar en una tabla del modelo relacional
	 * 
	 * @param tableName
	 *            Nombre de la tabla en la que se quieren insertar datos.
	 * @param fields
	 *            Columnas en las que se insertaran datos. Los nombres de las
	 *            columnas deben ir debidamente formateados para que funcionen
	 *            con el gestor de base de datos actual.
	 * @param values
	 *            Datos a insertar. Deben estar debidamente formateados para el
	 *            gestor de base de datos que se está usando. La cadena tiene
	 *            que contener los parentesis correspondientes.
	 * @return Cadena que se puede usar para insertar los datos en la tabla.<br>
	 *         Si el gestor que se está usando es PostgreSQL, la sentencia de
	 *         creación tambien incluye un RETURNING que devolvera el
	 *         <code>tableId</code> con el que se han insertado los datos.
	 */
	public String getInsertStatement(String tableName, String fields, String values, boolean returning){
		String result = "INSERT INTO " + getCharacterBegin() + tableName + getCharacterEnd() + " " + fields + " VALUES " + values;
		if (returning && gestor.equals(GestorsDBConstants.postgreSQL)){
			result += " RETURNING " + getCharacterBegin()+ Table.COLUMN_NAME_TABLEID + getCharacterEnd();
		}
		result += ";";
		return result;
	}

	/**
	 * Ejecuta una inserción segura en postgre para que se pueda continuar la
	 * transacción si se produce un error.
	 * 
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a base de datos.
	 * @param sql
	 *            Sentencia con el insert para PostgreSQL
	 * @return tableId asignado al objeto insertado.
	 * @throws SQLException
	 * @throws NamingException
	 */
	public Integer executeSecurePostgreInsert(FactoryConnectionDB fcdb, String sql) throws SQLException, NamingException{
		if (savePointIndex < Integer.MAX_VALUE){
			savePointIndex ++;
		}else{
			savePointIndex = 0;
		}
		Integer result = null;
		String savePointName = "SP_" + savePointIndex;
		String savePointCreation = "SAVEPOINT " + savePointName + ";";
		String savePointRollback = "ROLLBACK TO SAVEPOINT " + savePointName + ";";
		DBQueries.execute(fcdb, savePointCreation);
		try{
			result = DBQueries.executePostgreInsert(fcdb, sql);
		}catch (SQLException e){
			DBQueries.execute(fcdb, savePointRollback);
			throw e;
		}
		return result;
	}
	
	public String getNullValueWithDataType(int dataType){
		String result = "NULL";
		if (gestor.equals(GestorsDBConstants.postgreSQL)){
			switch (dataType) {
			case Constants.IDTO_DATETIME:
			case Constants.IDTO_DATE:
			case Constants.IDTO_TIME:
				result += "::BIGINT";
				break;
			case Constants.IDTO_BOOLEAN:
				result += "::BOOLEAN";
				break;
			case Constants.IDTO_INT:
				result += "::INTEGER";
				break;
			case Constants.IDTO_DOUBLE:
				result += "::FLOAT8";
				break;
			case Constants.IDTO_FILE:
			case Constants.IDTO_IMAGE:
			case Constants.IDTO_STRING:
				result += "::CHARACTER VARYING(100)";
				break;
			case Constants.IDTO_MEMO:
					result += "::TEXT";
				break;
			default:
				System.err.println("No se conoce el tipo de datos " + dataType);
			}
		}
		return result;
	}
}
