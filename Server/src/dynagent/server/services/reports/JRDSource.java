package dynagent.server.services.reports;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import dynagent.common.Constants;
import dynagent.common.utils.QueryConstants;

/**
 * Esta clase implementa la clase JRDataSource del paquete de JasperReport.<br>
 * Se va a encargar de ser la conexión de datos entre la base de datos y el informe, se basa en iterar
 * sobre un ResultSet y extraer de ahi la informacion necesaria por el informe.
 * @author Dynagent - David
 *
 */
public class JRDSource implements JRRewindableDataSource{
	/**
	 * conexión activa con la base de datos.
	 */	
	private Connection connection;
	private Statement st;
	/**
	 * ResultSet con los datos de la ejecucion de la consulta.
	 */
	private ResultSet data;
	private Map<String, String> typeMap= new HashMap<String, String>();
	private boolean emptyMaster;
	/**
	 * Mapa con la informacion completa de la consulta y subconsultas.
	 * @see ParQuery
	 */
	private Map<String, ParQuery> map;
	/**
	 * Mapa con la informacion de los campos visibles de una consulta.<br><br>
	 * <i>Ejemplo: ("nombre empresa", 2) - (nombre_campo, columna)</i>
	 */
	private Map<String, Integer> showMap= new HashMap<String, Integer>();
	/**
	 * Mapa con la informacion de los campos ocultos de una consulta.<br><br>
	 * <i>Ejemplo: ("id", 1) - (nombre_campo, columna)</i>
	 */
	private Map<String, Integer> hideMap= new HashMap<String, Integer>();
	/**
	 * Mapa con la informacion completa de los subreport de la consulta actual.
	 * @see ParSubreport
	 */
	private Map<String, ParSubreport> subreportMap= new HashMap<String, ParSubreport>();
	/**
	 * Mapa donde almacenamos el valor actual de cada columna de la consulta.<br><br>
	 * <i>Ejemplo: (2, "Dynagent") - (columna, valor_actual)</i>
	 */
	private Map<Integer, String> currentValues=new HashMap<Integer, String>();
	
	//private ArrayList<Integer> columnEnum=new ArrayList<Integer>();
	
	//private IKnowledgeBaseInfo ik;

	/** 
	 * <p><b>Constructor</b></p>
	 * <br>Mediante la conexión crea un nuevo Statement, y con el mapa de ParQuery
	 * y el mapa de valores a reemplazar, reemplaza los valores del where en la consulta.<br>
	 * Rellena los distintos mapas con los metodos {@link #fillHideMap(String[])}, {@link #fillShowMap(String[])} y {@link #fillSubreport(String[])}<br>
	 * Remplaza los valores en el where de la consulta mediante el metodo {@link #replaceQuery(String, Map)}
	 * @param con conexión a la base de datos.
	 * @param replaces Mapa con pares (ni columna, valor a sustituir)
	 * @param map Mapa con pares (indice de consulta, ParQuery de la consulta)
	 * @throws SQLException Excepcion lanzada por fallo en la creación del Statement.
	 * 
	 */
	public JRDSource(Connection con, Map<Integer, String> replaces, Map<String, ParQuery> map, String index/*, IKnowledgeBaseInfo ik*/) 
			throws SQLException {
		this.connection=con;
		this.st=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		this.map=map;
		String query=map.get(index).getQuery();
		//System.out.println("SYSOUAMI:index " + index);
		//System.out.println("SYSOUAMI:QUERY " + query);
		query=replaceQuery(query,replaces);
		//System.out.println("SYSOUAMI:QUERYReplaces " + query);
		if (query.length()>0)
			this.data=st.executeQuery(query);
		else
			this.emptyMaster=true;
		this.showMap=fillShowMap(map.get(index).getShow());
		this.hideMap=fillHideMap(map.get(index).getHide());
		this.subreportMap=fillSubreport(map.get(index).getIds());
		/*System.out.println("SYSOUAMI: ******* SHOWMAP *******");
		Set<String> keys=showMap.keySet();
		Iterator<String> itkeys=keys.iterator();
		while(itkeys.hasNext()){
			String key=itkeys.next();
			System.out.println("SYSOUAMI: KEY->"+key+" VALUE->"+showMap.get(key));
		}
		
		System.out.println("SYSOUAMI: ******* HIDEMAP *******");
		keys=hideMap.keySet();
		itkeys=keys.iterator();
		while(itkeys.hasNext()){
			String key=itkeys.next();
			System.out.println("SYSOUAMI: KEY->"+key+" VALUE->"+hideMap.get(key));
		}
		
		System.out.println("SYSOUAMI: ******* SUBREPORTMAP *******");
		keys=subreportMap.keySet();
		itkeys=keys.iterator();
		while(itkeys.hasNext()){
			String key=itkeys.next();
			System.out.println("SYSOUAMI: KEY->"+key+" VALUE->"+subreportMap.get(key));
		}*/
		
		
		//this.ik = ik;
	}
	
	/** 
	 * metodo encargado de rellenar el mapa de los subreport.({@link #subreportMap})<br><br>
	 * Este mapa contendra como clave, un String con el nombre del subreport,
	 * de la siguiente manera "sub"+id, y como valor, una instancia de la clase {@link ParSubreport}.
	 * <br>
	 * <ol>
	 * <li>Creamos un mapa 'result' de tipo <String, ParSubreport></li>
	 * <li>Mientras hayan ids de subreportes
	 * 		<ol><li>Obtener lista con las columnas necesarias (mediante {@link #getColumns(String)}) de dicho
	 * 		 subreport.</li>
	 * 		<li>Rellenamos el mapa 'm' de la forma (num columna, null), posteriormente
	 * 		 se actualizara con los datos actuales.</li>
	 * 		<li>Creamos un objeto ParSubreport, con el nombre del subreport ("sub"+id)
	 * 		 y el mapa 'm'</li>
	 * 		<li>Añadimos el objeto al mapa 'result'</li>
	 * 		</ol>
	 * </li>
	 * <li>Devolvemos 'result'</li>
	 * </ol>
	 * @see ParSubreport
	 * @param ids Array de String con los distintos subreport de la consulta actual.
	 * @return Mapa con la informacion de los subreports ({@link #subreportMap}).
	 */
	private Map<String, ParSubreport> fillSubreport(String[] ids) {
		Map<String, ParSubreport> result= new HashMap<String,ParSubreport>();
		if(!ids[0].equals("")){
			for (int i=0;i<ids.length;i++){
				List<String> requiredColumn=getColumns(ids[i]);
				Map<Integer,String> m= new HashMap<Integer,String>();
				Iterator<String> it = requiredColumn.listIterator();
				while(it.hasNext()){
					m.put(new Integer(it.next()).intValue(), null);
				}
				ParSubreport p = new ParSubreport(ids[i], m);
				result.put("sub"+ids[i], p);
			}
		}
		return result;
	}
		
	/**
	 * 
	 * metodo encargada de extraer las columnas requeridas por el where de la consulta.<br><br>
	 * Con el parametro index, extraemos la consulta del mapa con la informacion completa ({@link #map})
	 * usando el metodo {@link ParQuery#getQuery()}.<br> Posteriormente extraemos las columnas necesarias
	 * del where mediante el metodo split de String, y añadimos dicha columna a la lista.<br><br>
	 * <ol>
	 * <li>Extraemos la consulta y creamos un array de String mediante el split por la cadena "__"<br>
	 * 	   <i>(En el where de la consulta las columnas esta representadas por "__columna__")</i></li>
	 * <li>Iterando en el array de String, hacemos:
	 * 		<ol>
	 * 		<li>Pasamos el String a Integer</li>
	 * 		<li>Se comprueba si salta la excepción NumberFormatException</li>
	 * 		<li>Si salta, no hacer nada, en caso contrario, añadir el String a la lista</li>
	 * 		</ol></li>
	 * <li>Devolver la lista de Strings</li> 
	 * </ol>
	 * <br><br>
	 * <p style="font-size: 11pt;">
	 * <b>Aclaración:<br><br></b>
	 * <i>Caputarar la excepción NumberFormatException nos sirve para comprobar que lo que habia entre "__"
	 * es un número, ya que en el nombre tambien pueden ir "_"</i>
	 * </p>
	 * @param index Indice de la consulta.
	 * @return Lista de Strings con las distintas columnas.
	 */
	private List<String> getColumns(String index) {
		List<String> result = new ArrayList<String>();
		String query=this.map.get(index).getQuery();
		String subreportindex[]= query.split("__");
		int i=0;
		while (i<subreportindex.length){
			try{
				new Integer(subreportindex[i]).intValue();
				result.add(subreportindex[i]);
			}catch (NumberFormatException e){
			}
			i++;
		}
		return result;
	}

	/**
	 * metodo encargado de rellenar el mapa de los campos visibles de la consulta.
	 * ({@link #showMap})<br><br>
	 *  Por cada valor 'show', dividimos la cadena en dos usando split con "_", luego
	 *  rellenemos el mapa resultado con cada uno de los campos del resultado del split.
	 * 
	 * @param show Array de String con los campos visibles<br><i>
	 * Ejemplo: {"2_Nombre Empresa_String",..} - {"columna_nombre_tipo",..}</i>
	 * @return Mapa con la informacion de los campos visibles ({@link #showMap}).
	 */
	private Map<String, Integer> fillShowMap(String[] show) {
		Map<String,Integer> result = new HashMap<String, Integer>();
		if (!show[0].equals("")){
			for (int i=0;i<show.length;i++){
				String showValue[]=show[i].split("_");
				String nombre="";
				for(int j=1;j<showValue.length-1;j++){
					if (j==showValue.length-2)
						nombre=nombre.concat(showValue[j]);
					else
						nombre=nombre.concat(showValue[j])+" ";
				}
				
				int id = new Integer(showValue[0]).intValue();
				result.put(nombre, id);
				String type = showValue[showValue.length-1];
				this.typeMap.put(nombre,type);
				//if (type.equals("Enumerado"))
					//columnEnum.add(id);
			}
		}
		return result;
	}

	/**
	 * metodo encargado de rellenar el mapa de los campos ocultos de la consulta.
	 * ({@link #hideMap})<br><br>
	 *  Por cada valor 'hide', dividimos la cadena en dos usando split con "_", luego
	 *  rellenemos el mapa resultado con cada uno de los campos del resultado del split.
	 * 
	 * @param hide Array de String con los campos visibles<br><i>
	 * Ejemplo: {"1_Id_Integer",..} - {"columna_nombre_tipo",..}</i>
	 * @return Mapa con la informacion de los campos ocultos ({@link #hideMap}).
	 */
	private Map<String, Integer> fillHideMap(String[] hide) {
		Map<String,Integer> result = new HashMap<String, Integer>();
		if (!hide[0].equals("")){
			for (int i=0;i<hide.length;i++){
				String hideValue[]=hide[i].split("_");
				String nombre="";
				for(int j=1;j<hideValue.length-1;j++){
					nombre=nombre.concat(hideValue[j]);
				}
				result.put(nombre, new Integer(hideValue[0]).intValue());
			}
		}
		return result;
	}
	/**
	 * metodo encargado de sustituir los valores del where en la consulta.<br><br>
	 * Realizamos split sobre 'query' con "__" de forma que obtenemos un array de String, con
	 * este array miramos si lo que el contenido de la posición i-esima es un número, si es asi,
	 * este indicara la clave para sacar el valor de 'values'.
	 * <br><br>
	 * <ol>
	 * <li>Creamos un array mediante el split en la query</li>
	 * <li>Iteramos sobre el array haciendo:
	 * <ul><li>Pasamos el String a Integer</li>
	 * 		<li>Se comprueba si salta la excepción NumberFormatException</li>
	 * 		<li>Si salta, no hacer nada, en caso contrario, cambiamos el valor del array por el<br>
	 * 			del mapa 'values'.</li>
	 * 		</ul></li>
	 * <li>Devolvemos la concatenación del array (Devolvemos un String, no un String[]</li>
	 * </ol>
	 * <br>
	 *   
	 * @param query Cadena con la sentencia SQL de la consulta.
	 * @param values Mapa con los valores a sustituir.<br><i>
	 * Ejemplo: (2, "Dynagent") - (num columna, valor)</i>
	 * @return String con la consulta modificada con los valores actuales.
	 */
	private String replaceQuery(String query, Map<Integer, String> values){
		String result="";
		String subreportindex[]= query.split("__");
		int i=0;
		while (i<subreportindex.length){
			try{
				new Integer(subreportindex[i]).intValue();
				subreportindex[i]=values.get(new Integer(subreportindex[i]).intValue());
			}catch (NumberFormatException e){
			}
			i++;
		}
		for(int j=0;j<subreportindex.length;j++){
			if (subreportindex[j]==null)
				result=result.concat("NULL");
			else
				result=result.concat(subreportindex[j]);
		}
		
		return result;
	}
	
	/**
	 * metodo encargado de acutalizar el mapa de los valores actuales.({@link #currentValues})<br><br>
	 * Simplemente realiza un put en {@link #currentValues} con los parametros column y value
	 * @param column Clave del mapa (num de la columna en la base de datos)
	 * @param value Valor actual para dicha columna.
	 */
	private void updateCurrentValues(int column, Object value) {
		String valueStr = null;
		if (value!=null)
			valueStr = value.toString();
		//System.out.println("Column "+ column + " value "+valueStr);
		this.currentValues.put(column, valueStr);
	}
	
	/**
	 * metodo encargado de actualizar los valores a sustituir de cada subreport.<br><br>
	 *
	 * Iteramos sobre 'keysReplace' y para cada clave sacamos el valor del mapa 'values' y lo almacenamos
	 * en un nuevo mapa posteriormente devolverlo el metodo.
	 * @param values Mapa con los valores actuales.
	 * @param keysReplace Conjunto con las columnas requeridas por el subreport determinado.
	 * @return Mapa con las columnas y el valor actual (solo de las columnas requeridas por el subreport).
	 */
	private Map<Integer, String> updateSubreportValues(Map<Integer, String> values, Set<Integer> keysReplace) {
		Map<Integer,String> result= new HashMap<Integer, String>();
		Iterator<Integer> it = keysReplace.iterator();
		while (it.hasNext()){
			int key=it.next();
			result.put(key, values.get(key));
		}
		return result;
	}
	
	/**
	 *  metodo de la interface JRDataSource, se encarga de coger los valores de {@link #data}.<br><br>
	 *	
	 *	Mira si 'field' es un subreport, para ello comprueba que este en {@link #subreportMap},
	 *	si es, crea los valores a reemplazar en el where de la consulta con {@link #updateSubreportValues(Map, Set)}
	 *	y asigna a 'value' la instancia de un nuevo JRDSource con los datos adecuados.<br>
	 *	Si no es un subreport, extrae la columna del mapa {@link #showMap} y asigna a 'value'
	 *	el valor de la columna extraida del {@link #data}, posteriormente actualiza los datos
	 *	actuales con 'value' ({@link #updateCurrentValues(int, Object)}).<br><br>
	 *
	 * 	<ol>
	 * 	<li>Extraemos el nombre de 'field' y lo almacenamos en 'fieldName'</li>
	 * 	<li>Si {@link #subreportMap} contiene 'fieldName'
	 * 		<ul><li>Recogemos la informacion del subreport y creamos un conjunto con las columnas requeridas<br>
	 * 				<i>Mediante la funcion {@link ParSubreport#getReplaceValues()}</i></li>
	 * 		<li>Almacenamos en 'replacesValues' el mapa con los valores a reemplazar ({@link #updateSubreportValues(Map, Set)})</li>
	 * 		<li>Asignamos a 'value' la instancia de un objeto JRDSource, al cual le pasamos como
	 * 			parámetros {@link #connection}, 'replacesValues', {@link #map},
	 * 			y el indice del subreport ({@link ParSubreport#getIndex()})
	 * 		</li></ul></li>
	 * 	<li>Si no, miramos si {@link #showMap} contiene 'fieldName'
	 * 		<ul><li>Extremos la columna del {@link #showMap}, almacenandola en 'column'</li>
	 * 		<li>Asignamos a 'value' el valor de la columna del ResultSet ({@link #data})</li>
	 * 		<li>Finalmente actualizamos el valor actual pasandole como parámetro
	 * 			'value' y 'column' a {@link #updateCurrentValues(int, Object)}</li></ul></li>
	 * 	<li>Devolvemos 'value'</li></ol>
	 *  @param field Tipo de dato JRField con la informacion del campo a imprimir en el informe.
	 *  @return Object 'value' con el valor extraido de {@link #data}.
	 *  @throws JRException
	 */
	public Object getFieldValue(JRField field) throws JRException {
		String fieldName= field.getName();
		return getFieldValue(fieldName);
	}
	
	public Object getFieldValue(String fieldName) throws JRException {
		Object value=null;
		Map <Integer,String> replacesValues=new HashMap<Integer,String>();
		if(this.subreportMap.containsKey(fieldName)){
			ParSubreport subreport=this.subreportMap.get(fieldName);
			Set<Integer> keysSubreport = subreport.getReplaceValues().keySet();
			replacesValues=updateSubreportValues(this.currentValues, keysSubreport);
			try {
				/*Iterator it = replacesValues.keySet().iterator();
				System.out.println("REPLACES");
				while (it.hasNext()) {
					Integer inte=(Integer) it.next();
					System.out.println("SYSOUAMI:"+inte+" -> "+replacesValues.get(inte));
				}*/
				
				value= new JRDSource(this.connection,replacesValues,this.map,subreport.getIndex()/*,this.ik*/);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else if(this.showMap.containsKey(fieldName)){
			int column=showMap.get(fieldName);
			try {
				value=data.getObject(column);
				updateCurrentValues(column,value);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		//System.out.println("SYSOUAMI: CAMPO-->"+fieldName);
		//System.out.println("SYSOUAMI: VALUE-->"+value);
		if (value!=null) {
			if (!fieldName.contains("sub") && this.typeMap.get(fieldName)!=null){
				//System.out.println("SYSOUAMI: CLASE-->"+value.getClass());
				//System.out.println("SYSOUAMI: TIPO-->"+this.typeMap.get(fieldName));
				
				if (this.typeMap.get(fieldName).equals("Double")) {
					//en mySQL el cast a unsigned devuelve java.math.BigInteger
					//cuando me hace falta que sea java.lang.Integer
					if (value instanceof java.math.BigInteger) {
						java.math.BigInteger valueBInt = (java.math.BigInteger)value;
						Integer valueInt = Integer.parseInt(valueBInt.toString());
						value = valueInt;
					}
				} else if (this.typeMap.get(fieldName).equals("Date"))
					value=QueryConstants.secondsToDate(String.valueOf(value),QueryConstants.getPattern(Constants.IDTO_DATE));
				else if (this.typeMap.get(fieldName).equals("DateTime"))
					value=QueryConstants.secondsToDate(String.valueOf(value),QueryConstants.getPattern(Constants.IDTO_TIME));
				else if (this.typeMap.get(fieldName).equals("Time"))
					value=QueryConstants.secondsToDate(String.valueOf(value),QueryConstants.getPattern(Constants.IDTO_DATETIME));
			}
		}
		return value;
	}

	
	/**
	 * metodo de la interface JRDataSource, se encarga de iterar sobre {@link #data}.<br><br>
	 * 
	 * Además de iterar sobre {@link #data}, recorre {@link #hideMap} actualizando cada uno
	 * de los valores de los campos ocultos con mediante el metodo {@link #updateCurrentValues(int, Object)}.
	 * @return logico indica si se a realizado el next o no.
	 * @throws JRException
	 */
	public boolean next() throws JRException{
		boolean res=false;
		if (data==null) {
			if (emptyMaster) {
				res=true;
				emptyMaster=false;
			}
		}else {
			try {
				res = data.next();
				Collection<Integer> values=this.hideMap.values();
				Iterator<Integer> it = values.iterator();
				while(it.hasNext() && res){
					int column=it.next();
					updateCurrentValues(column, data.getObject(column));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		//System.out.println("SYSOUAMI: Next()->"+res);
		return res;
	}
	public void moveFirst() throws JRException {
		try {
			data.beforeFirst();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
