
//transformacion selekta
//E:\DESARROLLO\Workspace\Maca\Migration\src\xml\selekta\model\tr.xsl

package dynagent.tools.importers.migration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;



import dynagent.common.Constants;
import dynagent.common.knowledge.Category;
import dynagent.common.utils.Auxiliar;
import dynagent.server.dbmap.ClassInfo;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.IQueryInfo;
import dynagent.server.dbmap.Table;
import dynagent.server.dbmap.TableColumn;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.gestorsDB.GestorsDBConstants;
import dynagent.server.services.InstanceService;
import dynagent.server.services.XMLConstants;
import dynagent.tools.importers.IImporter;
import dynagent.tools.importers.ImporterFactory;
import dynagent.tools.importers.configxml.Menu;
import dynagent.tools.importers.owl.OWLParser;
import dynagent.tools.owl.ExtractOWLView;
import dynagent.tools.owl.OWL;
import dynagent.tools.owl.OWLIds;
import dynagent.tools.setup.ddbb.CreationDB;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.model.OWLModel;

public class CopyDataToRelationalModel {

	/**
	 * 
	 * <b>EJEMPLO PARAMETROS DEL MAIN</b><br>
	 * 
	 * -sourceip 192.168.1.3
	 * -sourceport 3310
	 * -sourcebns 3000
	 * -sourcegestor mySQL
	 * 
	 * -ip 192.168.1.3
	 * -port 5432
	 * -bns 13
	 * -gestor postgreSQL
	 * 
	 * -owlpath E:/DESARROLLO/ONTOLOGIA/
	 * -owlfile MODELO.owl
	 * 
	 * -xmlpath E:/DESARROLLO/Workspace/Maca/knowledge/src/config/
	 * -xmlfile configGenericoNEW.xml
	 * -pathImportReports Y:/jboss-4.0.5.GA-2/server/default/deploy/jbossweb-tomcat55.sar/ROOT.war/dyna/userFiles
	 * -reportpath E:/DESARROLLO/filesReport/ModeloRelacional/
	 * 
	 * @param args
	 *            Parámetros para el lanzamiento de la utilidad.
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// Host de origen de los datos a copiar
		String sourceIp = null;
		// Puerto en el que está el servidor de base de datos en el host de origen.
		Integer sourcePort = null;
		// Número de la base de datos de la que tenemos que coger los datos.
		Integer sourceBns = null;
		// Gestor de base de datos que almacena los datos de origen.
		String sourceGestor = null;
		// Host donde tenemos que crear la nueva base de datos.
		String ip = null;
		// Puerto donde tenemos que conectar en el host de la nueva base de datos.
		Integer port = null;
		// Número de la nueva base de datos que tenemos que crear.
		Integer bns = null;
		// Gestor de la nueva base de datos.
		String gestor = null;
		
		String tag = "";
		String menuParams = "";
		String pathOWL = "";
		String nameFileModel = "";
		
		boolean importReports = true;
		boolean importQuerys = false;
		boolean insertUserAndRoles = false;
		boolean insertIndex = false;
		
		String pathXML = null;
		boolean replica = false;
		String nameXmlFile = null;
		String pathReport = null;
		String pathQuery = null;
		String pathImportOtherXml = null;
		String pathImportReports = null;
		String pwd=null;
		for (String arg : args){
			if (arg.startsWith("-")){
				tag = arg;
			}else{
				if (tag.equalsIgnoreCase("-sourceip")){
					sourceIp = arg;
				}else if (tag.equalsIgnoreCase("-sourceport")){
					sourcePort = Integer.parseInt(arg);
				}else if (tag.equalsIgnoreCase("-sourcebns")){
					sourceBns = Integer.parseInt(arg);
				}else if (tag.equalsIgnoreCase("-sourcegestor")){
					sourceGestor = arg;
				}else if(tag.equalsIgnoreCase("-xmlpath")){
					pathXML=arg;
					menuParams += " -xmlpath " + arg;
				}else if(tag.equalsIgnoreCase("-xmlfile")){
					nameXmlFile=arg;
					menuParams += " -xmlfile " + arg;
				}else if(tag.equalsIgnoreCase("-replica")){
					replica=new Boolean(arg);
					menuParams += " -replica " + arg;
				}else if(tag.equalsIgnoreCase("-reportpath")){
					pathReport=arg;
					menuParams += " -reportpath " + arg;
				}else if(tag.equalsIgnoreCase("-querypath")){
					pathQuery=arg;
					menuParams += " -querypath " + arg;
				}else if(tag.equalsIgnoreCase("-pathimports")){
					pathImportOtherXml=arg;
					menuParams += " -pathimports " + arg;
				}else if(tag.equalsIgnoreCase("-pathImportReports")){
					pathImportReports=arg;
					menuParams += " -pathImportReports " + arg;
				}else if (tag.equalsIgnoreCase("-ip")){
					ip = arg;
					menuParams += " -ip " + arg;
				}else if (tag.equalsIgnoreCase("-port")){
					port = Integer.parseInt(arg);
					menuParams += " -port " + arg;
				}else if (tag.equalsIgnoreCase("-bns")){
					bns = Integer.parseInt(arg);
					menuParams += " -bns " + arg;
				}else if (tag.equalsIgnoreCase("-gestor")){
					gestor = arg;
					menuParams += " -gestor " + arg;
				}else if (tag.equalsIgnoreCase("-owlpath")){
					pathOWL = arg;
					menuParams += " -owlpath " + arg;
				}else if (tag.equalsIgnoreCase("-owlfile")){
					nameFileModel = arg;
					menuParams += " -owlfile " + arg;
				}else if (tag.equalsIgnoreCase("-pwd")){
					pwd = arg;
				}else{
					menuParams += " " + tag + " " + arg;
				}
			}
		}
		if(pathImportOtherXml==null)
			pathImportOtherXml=pathXML;
		if(pathImportReports!=null)//Le añadimos el nombre de la subcarpeta, que sera el numero de la base de datos
			pathImportReports=new File(pathImportReports,String.valueOf(bns)).getAbsolutePath();
		
		// Una vez hemos sacado los parámetros del array de entrada, procedemos a crear la base de datos
		FactoryConnectionDB fcdbOrigin = Menu.setConnection(String.valueOf(sourceBns), sourceIp, sourcePort, sourceGestor);
		fcdbOrigin.setPwd("domocenter28");
		if (sourceGestor.equals(GestorsDBConstants.mySQL)) {
			InputStream in = ODatosAtribToXml.class.getResourceAsStream("/dynagent/tools/setup/ddbb/mysql/mySQLAdapter.sql");
			File scriptFile = File.createTempFile("mySQLAdaptertemp", "sql");
			Auxiliar.inputStreamToFile(in, scriptFile);
			DBQueries.executeScript(fcdbOrigin, scriptFile);
			scriptFile.delete();
		} else if (sourceGestor.equals(GestorsDBConstants.SQLServer)) {
			InputStream in = ODatosAtribToXml.class.getResourceAsStream("/dynagent/tools/setup/ddbb/sqlserver/SQLServerAdapter.sql");
			File scriptFile = File.createTempFile("SQLServerAdapter", "sql");
			Auxiliar.inputStreamToFile(in, scriptFile);
			DBQueries.executeScript(fcdbOrigin, scriptFile);
			scriptFile.delete();
		}
		FactoryConnectionDB fcdbDestination = createDBWithSystemTablesAndIDs(ip, port, bns, gestor, fcdbOrigin, pwd);
		
		String uri = "file:///" + pathOWL + nameFileModel;
		OWLModel owlModel = ProtegeOWL.createJenaOWLModelFromURI(uri);
		ExtractOWLView 	extractowl=new ExtractOWLView(new OWL(owlModel),null,null);//"MÓDULO_PREVENCIÓN_RIESGOS_LABORALES;MÓDULO_FORMACIÓN;MÓDULO_INCOMPLETO");
		OWLModel protegeowl=extractowl.extract(false).getProtegeowlModel();
		OWLParser owlParserDestination = new OWLParser(protegeowl, fcdbDestination, new OWLIds(pathOWL));
		//importa manteniendo ids
		owlParserDestination.buildSystem();
		
		createTemporalTables(gestor, fcdbOrigin, fcdbDestination);
		
		DataBaseMap dataBaseMapOrigin = new DataBaseMap(fcdbOrigin, true);
		DataBaseMap dataBaseMapDestination = new DataBaseMap(fcdbDestination, true);
		//comparar y preguntar si hay transformacion
		//si hay clases nuevas y clases borradas -> posible transformacion
		//clases nuevas -> clase2 - clase1
		//clases borradas -> clase1 - instance2
		
		HashSet<String> setRdnsNewC = new HashSet<String>();
		HashSet<String> setRdnsDelC = new HashSet<String>();
		transformByTable(fcdbDestination, setRdnsNewC, setRdnsDelC, "clase", "idto");
		HashSet<String> setRdnsDelCData = onlyDataOfClases(fcdbOrigin, dataBaseMapOrigin, setRdnsDelC);
		
		//compruebo propiedades de la misma forma que con clases
		HashSet<String> setRdnsNewPD = new HashSet<String>();
		HashSet<String> setRdnsDelPD = new HashSet<String>();
		transformByTable(fcdbDestination, setRdnsNewPD, setRdnsDelPD, "propiedad_dato", "property");
		HashSet<String> setRdnsDelPDData = onlyDataOfProps(fcdbOrigin, dataBaseMapOrigin, setRdnsDelPD);
		
		HashSet<String> setRdnsNewPO = new HashSet<String>();
		HashSet<String> setRdnsDelPO = new HashSet<String>();
		transformByTable(fcdbDestination, setRdnsNewPO, setRdnsDelPO, "propiedad_objeto", "property");
		HashSet<String> setRdnsDelPOData = onlyDataOfProps(fcdbOrigin, dataBaseMapOrigin, setRdnsDelPO);
		
//		setIdsDelP.addAll(setIdsDelPD);
//		setIdsDelP.addAll(setIdsDelPO);
		
		//propiedades eliminadas para una clase en concreto
		HashSet<String> setIdsDelCP = new HashSet<String>();
		HashSet<String> setRdnsDelP = new HashSet<String>();
		setRdnsDelP.addAll(setRdnsDelPD);
		setRdnsDelP.addAll(setRdnsDelPO);
		//mostrar registros que estan en instances origin que no estan en instances y no sean de una clase o propiedad borrada
		HashMap<String,HashSet<String>> setClassesPropsDelP = new HashMap<String,HashSet<String>>();
		transformProps(fcdbDestination, setClassesPropsDelP, setRdnsDelC, setRdnsDelP, setIdsDelCP);
		HashMap<String,HashSet<String>> setIdtosPropsDelPData = onlyDataOfClasesProps(fcdbOrigin, dataBaseMapOrigin, setClassesPropsDelP, setIdsDelCP);
		
		HashSet<String> setRdnsOPToDP = new HashSet<String>();
		HashSet<String> setRdnsDPToOP = new HashSet<String>();
		transformPropsDPOPByTable(fcdbDestination, setRdnsOPToDP, setRdnsDPToOP);
		HashSet<String> setRdnsOPToDPData = onlyDataOfProps(fcdbOrigin, dataBaseMapOrigin, setRdnsOPToDP);
		HashSet<String> setRdnsDPToOPData = onlyDataOfProps(fcdbOrigin, dataBaseMapOrigin, setRdnsDPToOP);
		
		HashSet<String> setRdnsNoAbsToAbs = new HashSet<String>();
		transformNoAbstractToAbstract(fcdbDestination, setRdnsNoAbsToAbs);
		HashSet<String> setRdnsNoAbsToAbsData = onlyDataOfClases(fcdbOrigin, dataBaseMapOrigin, setRdnsNoAbsToAbs);
		
		File xsltFile = null;
		if (setRdnsNewC.size()>0 || setRdnsDelC.size()>0 ||
				setRdnsNewPD.size()>0 || setRdnsDelPD.size()>0 ||
				setRdnsNewPO.size()>0 || setRdnsDelPO.size()>0 ||
				setRdnsOPToDP.size()>0 || setRdnsDPToOP.size()>0 ||
				setRdnsNoAbsToAbs.size()>0 || setClassesPropsDelP.size()>0) {
			//pedir al usuario que cree X ficheros necesarios para las clases conflictivas y diga la carpeta donde se encuentran
			//cada uno que tenga como nombre de fichero el nombre de la clase
			String str = "\nEs posible que tenga que crear un fichero de transformación.\n";
			if (setRdnsNewC.size()>0 || setRdnsDelC.size()>0) {
				str += "Clases creadas: " + Auxiliar.hashSetToStringComillas(setRdnsNewC, ",") + "\n";
				str += "Clases borradas: " + Auxiliar.hashSetToStringComillas(setRdnsDelC, ",") + "\n";
				if (setRdnsDelCData.size()>0)
					str += "De las clases borradas se perderán datos en: " + Auxiliar.hashSetToStringComillas(setRdnsDelCData, ",") + "\n";
				else
					str += "De las clases borradas NO se perderán datos" + "\n";
				str += "\n";
			}
			if (setRdnsNoAbsToAbs.size()>0) {
				str += "Clases instanciables que han pasado a ser abstractas: " + Auxiliar.hashSetToStringComillas(setRdnsNoAbsToAbs, ",") + "\n";
				if (setRdnsNoAbsToAbsData.size()>0)
					str += "De las clases instanciables que han pasado a ser abstractas se perderán datos en: " + Auxiliar.hashSetToStringComillas(setRdnsNoAbsToAbsData, ",") + "\n";
				else
					str += "De las clases instanciables que han pasado a ser abstractas NO se perderán datos" + "\n";
				str += "\n";
			}
			if (setRdnsNewPD.size()>0 || setRdnsDelPD.size()>0) {
				str += "Propiedades dato creadas: " + Auxiliar.hashSetToStringComillas(setRdnsNewPD, ",") + "\n";
				str += "Propiedades dato borradas: " + Auxiliar.hashSetToStringComillas(setRdnsDelPD, ",") + "\n";
				if (setRdnsDelPDData.size()>0)
					str += "De las propiedades dato borradas se perderán datos en: " + Auxiliar.hashSetToStringComillas(setRdnsDelPDData, ",") + "\n";
				else
					str += "De las propiedades dato borradas NO se perderán datos" + "\n";
				str += "\n";
			}
			if (setRdnsNewPO.size()>0 || setRdnsDelPO.size()>0) {
				str += "Propiedades objeto creadas: " + Auxiliar.hashSetToStringComillas(setRdnsNewPO, ",") + "\n";
				str += "Propiedades objeto borradas: " + Auxiliar.hashSetToStringComillas(setRdnsDelPO, ",") + "\n";
				if (setRdnsDelPOData.size()>0)
					str += "De las propiedades objeto borradas se perderán datos en: " + Auxiliar.hashSetToStringComillas(setRdnsDelPOData, ",") + "\n";
				else
					str += "De las propiedades objeto borradas NO se perderán datos" + "\n";
				str += "\n";
			}
			if (setRdnsOPToDP.size()>0 || setRdnsDPToOP.size()>0) {
				str += "Propiedades objeto que han pasado a ser propiedades dato: " + Auxiliar.hashSetToStringComillas(setRdnsOPToDP, ",") + "\n";
				if (setRdnsOPToDPData.size()>0)
					str += "De las propiedades objeto que han pasado a ser propiedades dato se perderán datos en: " + Auxiliar.hashSetToStringComillas(setRdnsOPToDPData, ",") + "\n";
				else
					str += "De las propiedades objeto que han pasado a ser propiedades dato NO se perderán datos" + "\n";
				str += "Propiedades dato que han pasado a ser propiedades objeto: " + Auxiliar.hashSetToStringComillas(setRdnsDPToOP, ",") + "\n";
				if (setRdnsDPToOPData.size()>0)
					str += "De las propiedades dato que han pasado a ser propiedades objeto se perderán datos en: " + Auxiliar.hashSetToStringComillas(setRdnsDPToOPData, ",") + "\n";
				else
					str += "De las propiedades dato que han pasado a ser propiedades objeto NO se perderán datos" + "\n";
				str += "\n";
			}
			if (setClassesPropsDelP.size()>0) {
				str += "Clases con propiedades borradas: " + Auxiliar.hashMapSetStringToStringComillas(setClassesPropsDelP, " con las propiedades ", ",") + "\n";
				if (setIdtosPropsDelPData.size()>0)
					str += "De las clases con propiedades borradas se perderán datos en: " + Auxiliar.hashMapSetStringToStringComillas(setIdtosPropsDelPData, " con las propiedades ", ",") + "\n";
				else
					str += "De las clases con propiedades borradas NO se perderán datos" + "\n";
				str += "\n";
			}
			str += "¿Hay modificaciones en el modelo que afectan a los datos (cambios de nombre de clase o propiedad) " +
					"que no sean sólo creaciones y borrados? S/N";
			
			boolean correcto = false;
			while (!correcto) {
				String resDel = Auxiliar.leeTexto(str);
				if (resDel.equalsIgnoreCase("S") || resDel.equalsIgnoreCase("SI")) {
					str = "Detecte las modificaciones, y una vez creado el fichero de transformación introduzca la ruta:";
					String pathXSLT = Auxiliar.leeTexto(str);
					xsltFile = new File(pathXSLT);
					if (!xsltFile.exists())
						str = "El fichero no existe. ¿Está seguro de que hay modificaciones en el modelo que afectan a los datos? S/N";
					else
						correcto = true;
				} else {
					xsltFile = null;
					correcto = true;
				}
			}
		}
		HashSet<String> propsStructToNoStruct = getPropsStructToNoStruct(fcdbDestination);
		
		//importar sin mantener individuos
		//Menu.main(menuParams.trim().split(" "));
		// Sacamos los datos de la base de datos antigua.
		ODatosAtribToXml2.transform(dataBaseMapOrigin, dataBaseMapDestination, fcdbOrigin, fcdbDestination, setRdnsNewC, setRdnsDelC, setRdnsNoAbsToAbs, propsStructToNoStruct, xsltFile, owlParserDestination, 
				importReports, importQuerys, pathXML, replica, 
				nameXmlFile, pathReport, pathQuery, pathImportOtherXml, pathImportReports, 
				insertUserAndRoles, insertIndex);
	}
	
	private static HashSet<String> onlyDataOfClases(FactoryConnectionDB fcdbOrigin, DataBaseMap dataBaseMapOrigin,
			HashSet<String> setRdnsDelC) throws SQLException, NamingException {
		HashSet<String> setRdnsDelCData = new HashSet<String>();
		if (setRdnsDelC.size()>0) {
			HashSet<Integer> setIdsDelC = new HashSet<Integer>();
			for(String className:setRdnsDelC){
				ClassInfo classInfo=dataBaseMapOrigin.getClass(className);
				if(classInfo!=null){
					setIdsDelC.add(classInfo.getIdto());
				}
			}
			String sql = "select distinct id_to from o_datos_atrib where id_to in(" + Auxiliar.hashSetIntegerToString(setIdsDelC, ",") + ")";
			List<List<String>> lList = DBQueries.executeQuery(fcdbOrigin, sql);
			for (List<String> list : lList) {
				for (String idto : list) {
					setRdnsDelCData.add(dataBaseMapOrigin.getClass(new Integer(idto)).getName());
				}
			}
		}
		return setRdnsDelCData;
	}
	private static HashSet<String> onlyDataOfProps(FactoryConnectionDB fcdbOrigin, DataBaseMap dataBaseMapOrigin, 
			HashSet<String> setRdnsDelP) throws SQLException, NamingException {
		HashSet<String> setRdnsDelPData = new HashSet<String>();
		if (setRdnsDelP.size()>0) {
			HashSet<Integer> setIdsDelP = new HashSet<Integer>();
			for(String propName:setRdnsDelP){
				setIdsDelP.add(dataBaseMapOrigin.getPropertyId(propName));
			}
			String sql = "select distinct property from o_datos_atrib where property in(" + Auxiliar.hashSetIntegerToString(setIdsDelP, ",") + ")";
			List<List<String>> lList = DBQueries.executeQuery(fcdbOrigin, sql);
			for (List<String> list : lList) {
				for (String prop : list) {
					setRdnsDelPData.add(dataBaseMapOrigin.getPropertyName(new Integer(prop)));
				}
			}
		}
		return setRdnsDelPData;
	}
	private static HashMap<String,HashSet<String>> onlyDataOfClasesProps(FactoryConnectionDB fcdbOrigin, DataBaseMap dataBaseMapOrigin, 
			HashMap<String,HashSet<String>> setIdtosPropsDelP, HashSet<String> setIdsDelCP) throws SQLException, NamingException {
		HashMap<String,HashSet<String>> setIdtosPropsDelPData = new HashMap<String, HashSet<String>>();
		if (setIdsDelCP.size()>0) {
			String sql = "select distinct id_to, property from o_datos_atrib " +
					"where concat(id_to,';',property) in(" + Auxiliar.hashSetStringToString(setIdsDelCP, ",") + ")";
			List<List<String>> lList = DBQueries.executeQuery(fcdbOrigin, sql);
			Iterator it = lList.iterator();
			while (it.hasNext()) {
				List<String> list = (List<String>)it.next();
				Integer idto = Integer.parseInt(list.get(0));
				Integer prop = Integer.parseInt(list.get(1));
				ClassInfo classInfo = dataBaseMapOrigin.getClass(idto);
				String nameCls = classInfo.getName();
				HashSet<String> props = setIdtosPropsDelPData.get(nameCls);
				if (props==null) {
					props = new HashSet<String>();
					setIdtosPropsDelPData.put(nameCls, props);
				}
				String nameProp = dataBaseMapOrigin.getPropertyName(prop);
				props.add(nameProp);
			}
		}
		return setIdtosPropsDelPData;
	}
	
	private static HashSet<String> getPropsStructToNoStruct(FactoryConnectionDB fcdbDestination) throws SQLException, NamingException {
		HashSet<String> propsStructToNoStruct = new HashSet<String>();
		String sql = "select id, rdn from (" + "\n" + 
			"select id, rdn,'1' from propiedad_objeto where cat%" + Category.iStructural + "<>0 " + "\n" + 
			"union " + "\n" + 
			"select id, rdn,'2' from propiedad_objeto_origin where cat%" + Category.iStructural + "=0 " + 
			"order by id" + "\n" + 
			") as h " + "\n" + 
			"group by id,rdn " + "\n" + 
			"having count(*)>1";
		List<List<String>> lList = DBQueries.executeQuery(fcdbDestination, sql);
		Iterator it = lList.iterator();
		while (it.hasNext()) {
			List<String> list = (List<String>)it.next();
			propsStructToNoStruct.add(list.get(1));
		}
		return propsStructToNoStruct;
	}
	
	private static boolean transformByTable(FactoryConnectionDB fcdbDestination, 
			Set<String> setRdnsNew, Set<String> setRdnsDel,String table, String column) throws SQLException, NamingException {
		boolean transform = false;
		GenerateSQL gSQL = new GenerateSQL(fcdbDestination.getGestorDB());
		String sql = "select rdn from " + table + 
				"\n" + gSQL.getSubstract() + "\n" + 
				"select rdn from " + table + "_origin"; 
		List<List<String>> lListNew = DBQueries.executeQuery(fcdbDestination, sql);
		
//		if (lListNew.size()>0) {
			Iterator it = lListNew.iterator();
			while (it.hasNext()) {
				List<String> list = (List<String>)it.next();
				setRdnsNew.add(list.get(0));
			}
			
			sql = "select rdn from " + table + "_origin where id in(select distinct " + column + " from instances_origin)" + 
					"\n" + gSQL.getSubstract() + "\n" + 
				"select rdn from " + table + " where id in(select distinct " + column + " from instances)";
			List<List<String>> lListDel = DBQueries.executeQuery(fcdbDestination, sql);
			if (lListDel.size()>0) {
				if (lListNew.size()>0)
					transform = true;
				//al haber creaciones y borrados es posible que sean modificaciones
				//guardamos rdns  en listas
				it = lListDel.iterator();
				while (it.hasNext()) {
					List<String> list = (List<String>)it.next();
					setRdnsDel.add(list.get(0));
				}
			}
//		}
		return transform;
	}
	
	private static boolean transformPropsDPOPByTable(FactoryConnectionDB fcdbDestination, 
			Set<String> setRdnsOPToDP, Set<String> setRdnsDPToOP) throws SQLException, NamingException {
		boolean transform = false;
		//dataproperties que ahora son objectproperties
		String sql = "select rdn from propiedad_objeto " + 
			"\nwhere rdn in(" + 
			"select rdn from propiedad_dato_origin)";
		List<List<String>> lListDPToOP = DBQueries.executeQuery(fcdbDestination, sql);
		
		if (lListDPToOP.size()>0) {
			transform = true;
			Iterator it = lListDPToOP.iterator();
			while (it.hasNext()) {
				List<String> list = (List<String>)it.next();
				String rdn = list.get(0);
				setRdnsDPToOP.add(rdn);
			}
		}
		
		//objectproperties que ahora son dataproperties
		sql = "select rdn from propiedad_dato " + 
			"\nwhere rdn in(" + 
			"select rdn from propiedad_objeto_origin)";
		List<List<String>> lListOPToDP = DBQueries.executeQuery(fcdbDestination, sql);
		if (lListOPToDP.size()>0) {
			transform = true;
			//al haber creaciones y borrados es posible que sean modificaciones
			//guardamos rdns  en listas
			Iterator it = lListOPToDP.iterator();
			while (it.hasNext()) {
				List<String> list = (List<String>)it.next();
				String rdn = list.get(0);
				setRdnsOPToDP.add(rdn);
			}
		}
		return transform;
	}
	
	private static boolean transformProps(FactoryConnectionDB fcdbDestination, 
			HashMap<String,HashSet<String>> setClassesPropsDelP,HashSet<String> setRdnsDelC, HashSet<String> setRdnsDelP
			, HashSet<String> setIdsDelCP) throws SQLException, NamingException {
		boolean transform = false;
		//mostrar registros que estan en instances origin que no estan en instances y no sean de una clase o propiedad borrada
		String sql = "select clase_origin.rdn, v_propiedad_origin.id, cast(instances_origin.idto as varchar) || ';' || cast (instances_origin.property as varchar) " +
				"from instances_origin,clase_origin,v_propiedad_origin " +
			"\nwhere instances_origin.idto=clase_origin.id and instances_origin.property=v_propiedad_origin.id and " +
			"cast(instances_origin.idto as varchar) || ';' || cast (instances_origin.property as varchar) not in(" +
			"select cast(clase_origin.id as varchar) || ';' || cast (v_propiedad_origin.id as varchar) from instances,clase_origin,v_propiedad_origin where instances.idto=clase_origin.id and instances.property=v_propiedad_origin.id)";
		if (setRdnsDelC.size()>0)
			sql += " and instances_origin.idto not in(select id from clase_origin where rdn in ('" + Auxiliar.hashSetStringToString(setRdnsDelC, "','") + "'))";
		if (setRdnsDelP.size()>0)
			sql += " and instances_origin.property not in(" +
					"select id from propiedad_dato_origin where rdn in ('" + Auxiliar.hashSetStringToString(setRdnsDelP, "','") + "') union " +
					"select id from propiedad_objeto_origin where rdn in ('" + Auxiliar.hashSetStringToString(setRdnsDelP, "','") + "'))";
		List<List<String>> lListProps = DBQueries.executeQuery(fcdbDestination, sql);
		
		if (lListProps.size()>0) {
			transform = true;
			Iterator it = lListProps.iterator();
			while (it.hasNext()) {
				List<String> list = (List<String>)it.next();
				//System.out.println(list.get(0) + ";" + list.get(1) + ";" + list.get(2));
				String nameCls = list.get(0);
				String nameProp = list.get(1);
				String nameClsProp = "'" + list.get(2) + "'";
				
				HashSet<String> props = setClassesPropsDelP.get(nameCls);
				if (props==null) {
					props = new HashSet<String>();
					setClassesPropsDelP.put(nameCls, props);
				}
				props.add(nameProp);
			
				setIdsDelCP.add(nameClsProp);
			}
		}
		return transform;
	}
	
	private static boolean transformNoAbstractToAbstract(FactoryConnectionDB fcdbDestination, 
			Set<String> setRdnsNoAbsToAbs) throws SQLException, NamingException {
		boolean transform = false;
		//instanciables que ahora son abstractas
		String sql = "select rdn from clase where abstracta=true and rdn in( " + 
			"\nselect rdn from clase_origin where abstracta=false)";
		List<List<String>> lListAbstract = DBQueries.executeQuery(fcdbDestination, sql);
		
		if (lListAbstract.size()>0) {
			transform = true;
			Iterator it = lListAbstract.iterator();
			while (it.hasNext()) {
				List<String> list = (List<String>)it.next();
				String rdn = list.get(0);
				setRdnsNoAbsToAbs.add(rdn);
			}
		}
		return transform;
	}
	
	private static FactoryConnectionDB createDBWithSystemTablesAndIDs(String ip, Integer port, int nbusiness, 
			String gestorDB, FactoryConnectionDB fcdbOrigin,String pwd) throws SQLException, NamingException, URISyntaxException, IOException {
		IImporter importer = ImporterFactory.createImporter(gestorDB,pwd);
		//crear nueva base de datos temporal
		importer.setHost(ip);
		importer.dropDataBase(nbusiness);
		importer.createDataBase(nbusiness);
		
		//crear fabrica nueva
		FactoryConnectionDB fcdbDestination = Menu.setConnection(String.valueOf(nbusiness), ip, port, gestorDB);
		//copiar clases y properties en esta
		InputStream in = ODatosAtribToXml.class.getResourceAsStream("/dynagent/tools/setup/ddbb/postgres/TablesWithIds.sql");
		File scriptFile = File.createTempFile("TablesWithIds", "sql");
		Auxiliar.inputStreamToFile(in, scriptFile);
		DBQueries.executeScript(fcdbDestination, scriptFile);
		scriptFile.delete();
		
		//importer.createSystemViews(fcdbActual.getBusiness());
		return fcdbDestination;
	}

	private static void createTemporalTables(String gestorDB,
			FactoryConnectionDB fcdbOrigin, FactoryConnectionDB fcdbDestination)
			throws SQLException, NamingException {
		GenerateSQL gSQLOrigin = new GenerateSQL(fcdbOrigin.getGestorDB());
		String cBOrigin = gSQLOrigin.getCharacterBegin();
		String cEOrigin = gSQLOrigin.getCharacterEnd();
		GenerateSQL gSQLDestination = new GenerateSQL(gestorDB);
		String cBDestination = gSQLDestination.getCharacterBegin();
		String cEDestination = gSQLDestination.getCharacterEnd();
		
		String insert = null;
		
		String tableName = "clase";
		String sqlQuery = "select " + cBOrigin + "tableId" + cEOrigin + ", rdn, id, abstracta from " + tableName;
		List<List<String>> lList = DBQueries.executeQuery(fcdbOrigin, sqlQuery);
		for (List<String> list : lList) {
			int tableId = Integer.parseInt(list.get(0));
			String rdn = list.get(1);
			int id = Integer.parseInt(list.get(2));
			String abstracta = list.get(3);
			insert = "insert into " + tableName + "_origin(" + cBDestination + "tableId" + cEDestination + ", rdn, id, abstracta) " +
					"values(" + tableId + ",'" + rdn.replaceAll("'", "''") + "'," + id + "," + abstracta + ")";
			DBQueries.executeUpdate(fcdbDestination, insert);
		}
		/*if (lList.size()>0)
			alterSequence(fcdbDestination, gSQLDestination, tableName);
		insert = "insert into " + tableName + "_origin(" + cBDestination + "tableId" + cEDestination + ", rdn, id, abstracta) " +
			"select " + cBDestination + "tableId" + cEDestination + ", rdn, id, abstracta from " + tableName;
		DBQueries.executeUpdate(fcdbDestination, insert);
		*/
		tableName = "propiedad_dato";
		sqlQuery = "select " + cBOrigin + "tableId" + cEOrigin + ", rdn, id, valuecls, cat from " + tableName;
		lList = DBQueries.executeQuery(fcdbOrigin, sqlQuery);
		for (List<String> list : lList) {
			int tableId = Integer.parseInt(list.get(0));
			String rdn = list.get(1);
			int id = Integer.parseInt(list.get(2));
			Integer valueCls = null;
			if (list.get(3)!=null)
				valueCls = Integer.parseInt(list.get(3));
			Integer cat = null;
			if (list.get(4)!=null)
				cat = Integer.parseInt(list.get(4));
			insert = "insert into " + tableName + "_origin(" + cBDestination + "tableId" + cEDestination + ", rdn, id, valuecls, cat) " +
					"values(" + tableId + ",'" + rdn.replaceAll("'", "''") + "'," + id + "," + valueCls + "," + cat + ")";
			DBQueries.executeUpdate(fcdbDestination, insert);
		}
		/*if (lList.size()>0)
			alterSequence(fcdbDestination, gSQLDestination, tableName);
		insert = "insert into " + tableName + "_origin(" + cBDestination + "tableId" + cEDestination + ", rdn, id, valuecls, cat) " +
			"select " + cBDestination + "tableId" + cEDestination + ", rdn, id, valuecls, cat from " + tableName;
		DBQueries.executeUpdate(fcdbDestination, insert);*/
		
		tableName = "propiedad_objeto";
		sqlQuery = "select " + cBOrigin + "tableId" + cEOrigin + ", rdn, id, id_inversa, cat " +
				"from " + tableName;
		lList = DBQueries.executeQuery(fcdbOrigin, sqlQuery);
		for (List<String> list : lList) {
			int tableId = Integer.parseInt(list.get(0));
			String rdn = list.get(1);
			int id = Integer.parseInt(list.get(2));
			Integer idInversa = null;
			if (list.get(3)!=null)
				idInversa = Integer.parseInt(list.get(3));
			Integer cat = null;
			if (list.get(4)!=null)
				cat = Integer.parseInt(list.get(4));
			insert = "insert into " + tableName + "_origin(" + cBDestination + "tableId" + cEDestination + ", rdn, id, id_inversa, cat) " +
					"values(" + tableId + ",'" + rdn.replaceAll("'", "''") + "'," + id + "," + idInversa + "," + cat + ")";
			DBQueries.executeUpdate(fcdbDestination, insert);
		}
		/*if (lList.size()>0)
			alterSequence(fcdbDestination, gSQLDestination, tableName);
		insert = "insert into " + tableName + "_origin(" + cBDestination + "tableId" + cEDestination + ", rdn, id, id_inversa, cat) " +
			" select " + cBDestination + "tableId" + cEDestination + ", rdn, id, id_inversa, cat from " + tableName;
		DBQueries.executeUpdate(fcdbDestination, insert);
		*/
		
		tableName = "instances";
		sqlQuery = "select idto, ido, property, value, valuecls, qmin, qmax, name, op " +
				"from " + tableName + " as t inner join clase as c on(t.idto=c.id)";
		
		lList = DBQueries.executeQuery(fcdbOrigin, sqlQuery);
		for (List<String> list : lList) {
			int idto = Integer.parseInt(list.get(0));
			
			Integer ido = null;
			String idoStr = list.get(1);
			if (idoStr!=null)
				ido = Integer.parseInt(idoStr);
			
			int property = Integer.parseInt(list.get(2));
			String value = list.get(3);
			if (value!=null)
				value = "'" + value.replaceAll("'", "''") + "'";
			
			Integer valueCls = null;
			String valueClsStr = list.get(4);
			if (valueClsStr!=null)
				valueCls = Integer.parseInt(valueClsStr);
			
			Double qMin = null;
			String qMinStr = list.get(5);
			if (qMinStr!=null)
				qMin = Double.parseDouble(qMinStr);
			
			Double qMax = null;
			String qMaxStr = list.get(6);
			if (qMaxStr!=null)
				qMax = Double.parseDouble(qMaxStr);
			
			String name = list.get(7);
			if (name!=null)
				name = "'" + name.replaceAll("'", "''") + "'";
			String op = list.get(8);
			if (op!=null)
				op = "'" + op.replaceAll("'", "''") + "'";
			
			boolean virtual = false;
			
			insert = "insert into " + tableName + "_origin(idto, ido, property, value, valuecls, qmin, qmax, name, op, virtual) " +
					"values(" + idto + "," + ido + "," + property + "," + value + "," + valueCls + "," + qMin + "," + qMax + "," + name + "," + op + "," + virtual + ")";
			DBQueries.executeUpdate(fcdbDestination, insert);
		}
		
		fcdbDestination.removeConnections();
	}
	private static Integer getLastTableId(FactoryConnectionDB fcdbTmp, GenerateSQL gSQL, String tableName) throws SQLException, NamingException {
		String sql = gSQL.getLastTableId(tableName);
		List<List<String>> queryResult = DBQueries.executeQuery(fcdbTmp, sql);
		Integer tableIdValue = Integer.parseInt(queryResult.get(0).get(0));
		return tableIdValue;
	}
	private static void alterSequence(FactoryConnectionDB fcdbTmp, GenerateSQL gSQL, String tableName) throws SQLException, NamingException {
		Integer lastSeq = getLastTableId(fcdbTmp, gSQL, tableName);
		String nameSeq = tableName + "_tableId_seq";
		String sqlAlter = "ALTER SEQUENCE " + gSQL.getCharacterBegin() + nameSeq + gSQL.getCharacterEnd() + " RESTART WITH " + Integer.valueOf(lastSeq+1);
		DBQueries.executeUpdate(fcdbTmp, sqlAlter);
	}

	private static boolean compareIds(FactoryConnectionDB fcdbOrigin, FactoryConnectionDB fcdbDestination) throws SQLException, NamingException {
		boolean dataTransform = false;
		GenerateSQL gSQL = new GenerateSQL(fcdbDestination.getGestorDB());
		//si instance2 contiene instance1 -> instance1 - instance2 = 0
		//si lo contiene es posible que se hayan añadido clases pero eso no es problema para el importador
		//contenido si la siguiente consulta devuelve 0 registros
		String sql = "select idto, ido, property, value, valuecls, qmin, qmax, name, op, virtual from instances where virtual=false" + 
			"\n" + gSQL.getSubstract() + "\n" + 
		"select idto, ido, property, value, valuecls, qmin, qmax, name, op, virtual from instances where virtual=false";
	
		List<List<String>> lListInst = DBQueries.executeQuery(fcdbDestination, sql);
		if (lListInst.size()>0)
			dataTransform = true;
		
		if (!dataTransform) {
			sql = "select " + gSQL.getCharacterBegin() + "tableId" + gSQL.getCharacterEnd() + ", rdn, cat, id_inversa from v_propiedad" + 
				"\n" + gSQL.getSubstract() + "\n" + 
			"select " + gSQL.getCharacterBegin() + "tableId" + gSQL.getCharacterEnd() + ", rdn, cat, id_inversa from v_propiedad";
	
			List<List<String>> lListProp = DBQueries.executeQuery(fcdbDestination, sql);
			if (lListProp.size()>0)
				dataTransform = true;
			else
				dataTransform = false;
		}
		
		if (!dataTransform) {
			sql = "select " + gSQL.getCharacterBegin() + "tableId" + gSQL.getCharacterEnd() + ", rdn, abstracta from clase" + 
				"\n" + gSQL.getSubstract() + "\n" + 
			"select " + gSQL.getCharacterBegin() + "tableId" + gSQL.getCharacterEnd() + ", rdn, abstracta from clase";
	
			List<List<String>> lListClase = DBQueries.executeQuery(fcdbDestination, sql);
			if (lListClase.size()>0)
				dataTransform = true;
			else
				dataTransform = false;
		}
		return dataTransform;
	}
	/**
	 * Pre-procesa el XML de o_datos_atrib para ver si alguno de los elementos
	 * que vienen de la otra base de datos ya existe en la nueva por ser
	 * individuos del sistema.
	 * 
	 * @param document
	 *            XML a analizar.
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a base de datos.
	 * @param dataBaseMap
	 *            Mapa de la nueva base de datos.
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	private static void preprocessDocument(Document document, FactoryConnectionDB fcdb, DataBaseMap dataBaseMap) throws SQLException, NamingException {
		// TODO Auto-generated method stub
		Map<Integer, Element> elementsByIdNode = new Hashtable<Integer, Element>();
		Element objectsElement = document.getRootElement().getChild(XMLConstants.TAG_OBJECTS);
		@SuppressWarnings("unchecked")
		List<Element> children = new LinkedList<Element>(objectsElement.getChildren());
		for (Element child : children) {
			processChild(elementsByIdNode, child);
		}
		searchInDB(elementsByIdNode, fcdb, dataBaseMap);
	}

	/**
	 * Busca para los objetos del mapa, a ver si ya existe alguno con el mismo
	 * rdn. Esta comprobación solo se hace para los objetos no apuntados por
	 * estructurales.
	 * 
	 * @param elementsByIdNode
	 *            Mapa con los elementos que contienen datos indexados por su
	 *            id_node
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a la base de datos.
	 * @param dataBaseMap
	 *            Mapa de la nueva base de datos.
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	private static Set<Integer> searchInDB(Map<Integer, Element> elementsByIdNode, FactoryConnectionDB fcdb, DataBaseMap dataBaseMap) throws SQLException, NamingException {
		Set<Integer> modifiedIdNodes = new HashSet<Integer>();
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		for (Integer idNode : elementsByIdNode.keySet()) {
			Element element = elementsByIdNode.get(idNode);
			Attribute rdnAttribute = element.getAttribute(Constants.PROP_RDN);
			if (rdnAttribute == null){
				// No podemos comprobar si hay un objeto con el mismo rdn si el
				// elemento no define uno.
				continue;
			}
			
			ClassInfo classInfo = dataBaseMap.getClass(element.getName());
			Table table = dataBaseMap.getTable(classInfo.getIdto());
			if (table.hasForeignProperties()){
				// Es apuntada por estructurales.
				continue;
			}
			
			TableColumn column = table.getDataPropertyColumn(Constants.IdPROP_RDN);
			// Realizamos la consulta para saber si hay objetos de la clase del elemento con el mismo rdn.
			String sql = "SELECT " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + " FROM " + cB + table.getName() + cE + " WHERE " + cB + column.getColumnName() + cE + "=" + gSQL.parseStringToInsert(rdnAttribute.getValue());
			List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sql);
			String tableId = null;
			for (List<String> objectData : queryResult) {
				tableId = objectData.get(0);
			}
			if (tableId != null){
				// Si entramos aquí, es que había un objeto en base de datos con
				// el mismo rdn, modificamos el elemento para que la acción sea
				// set y le asignamos con tableId el que hemos encontrado.
				// Además, añadimos el id_node al conjunto de identificadores que
				// existen.
				element.setAttribute(XMLConstants.ATTRIBUTE_TABLEID, tableId);
				element.setAttribute(XMLConstants.ATTRIBUTE_ACTION, XMLConstants.ACTION_SET);
				modifiedIdNodes.add(idNode);
			}
		}
		return modifiedIdNodes;
	}

	/**
	 * Comprueba recusivamente un elemento y todos sus hijos para ver si son un
	 * elemento de datos, es decir, tienen el atributo id_node. Si un elemento
	 * tiene el atributo id_node, se explorarán sus hijos también.
	 * 
	 * @param elementsByIdNode
	 *            Mapa donde vamos almacenando los elementos con id_node.
	 * @param element
	 *            Elemento a analizar.
	 */
	private static void processChild(Map<Integer, Element> elementsByIdNode, Element element) {
		Attribute idNodeAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDNODE);
		try {
			if (idNodeAttribute != null){
				int idNode = idNodeAttribute.getIntValue();
				elementsByIdNode.put(idNode, element);
				@SuppressWarnings("unchecked")
				List<Element> children = new LinkedList<Element>(element.getChildren());
				for (Element child : children) {
					processChild(elementsByIdNode, child);
				}
			}
		} catch (DataConversionException e) {
			System.err.println(element);
			e.printStackTrace();
			System.exit(1);
		}
	}
}
