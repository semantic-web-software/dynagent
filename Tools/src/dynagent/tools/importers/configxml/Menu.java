package dynagent.tools.importers.configxml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.drools.compiler.PackageBuilder;
import org.drools.rule.Package;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;

import dynagent.common.Constants;
import dynagent.common.communication.IndividualData;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.action;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.jdomParser;
import dynagent.ruleengine.ConceptLogger;
import dynagent.server.database.dao.ConfigurationDAO;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.InstanceDAO;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.IQueryInfo;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.dbmap.Table;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.ejb.ServerEngine;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.gestorsDB.GestorsDBConstants;
import dynagent.server.reports.clasificator.ReportClasificator;
import dynagent.server.services.DeletableObject;
import dynagent.server.services.InstanceService;
import dynagent.server.services.querys.AuxiliarQuery;
import dynagent.tools.importers.Connect;
import dynagent.tools.importers.IImporter;
import dynagent.tools.importers.ImporterFactory;
import dynagent.tools.importers.owl.OWLParser;
import dynagent.tools.owl.ExtractOWLView;
import dynagent.tools.owl.OWL;
import dynagent.tools.owl.OWLDroolsRuler;
import dynagent.tools.owl.OWLIds;
import dynagent.tools.replica.importers.ImportReplica;
import dynagent.tools.replica.importers.ParserReplicaXML;
import dynagent.tools.updaters.model.XMLModelUpdate;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.model.OWLModel;

public class Menu {

	public static void main(String args[]) throws Exception{
		ConceptLogger.getLogger().cleanFile(); 
		ConceptLogger.getLogger().writeln("Inicio de sesión: " + Calendar.getInstance().getTime());
		
		/*EJEMPLO PARAMETROS DEL MAIN
		 * 
		 * -ip 192.168.1.3
		 * -bns 60
		 * -endBns 70
		 * -gestor mySQL
		 * -owlpath E:/DESARROLLO/Workspace/Francisco/Knowledge/src/owl/
		 * -xmlpath E:/DESARROLLO/Workspace/Francisco/CelopJoven/src/config/ 
		 * -owlfile MODELO_V1.0.owl 
		 * -xmlfile ConfigCELOP.xml 
		 * -replica true 
		 * -port 3306 
		 * -reportpath E:/DESARROLLO/Workspace/Francisco/CelopJoven/src/reports/ 
		 * -querypath  E:/DESARROLLO/Workspace/Francisco/CelopJoven/src/queries/ 
		 */

		String ip=null;
		int nbusiness=-1;
		String snbusiness=null;
		String gestor=null;
		String pathOWL=null;
		String pathXML=null;
		String pathQuery=null;
		String pathReport=null;
		String nameFileModel=null;//ejemplo "ERP.owl";  //en pathOWL
		String nameXmlFile=null;//ejemplo"ConfigGamito.xml";  //en pathXML
		boolean replica=false;
		String pathIreport=null;//args[9];
		String pathDesignReports=null;//args[10];
		String pathImportReports=null;
		
		String nameUpdateReplica=null;
		String nameUpdateModel =null;
		Integer port = null;
		String pwd=null;
		
		String pathImportOtherXml = null;
		String pathIdsTxt = null;
		
		String pathMigrationLogFile = "C:/logMigration.txt";
		String pathDeletedLogFile = "C:/deletedLog.txt";
		
		String modulosIncompletos=null;
		boolean import_help_views=true;
		String BusinessFunctions=null;
		//NUEVA VERSIÓN NO SE EXCLUYEN NI INCLUYEN BAJO DEMANDA AL IMPORTAR 		
//		String modulesInclude= null;
//		String modulesExclude= null;
		
		//if( args.length>10)
		//nameUpdateModel =args[10]; //args[10] ejemplo "updateModel.xml";  //en pathXML

		String id="";
		for(int i=0;i<args.length;i++){
			//System.out.println("\n debug Menu param="+args[i]);
			if(args[i].startsWith("-"))
				id=args[i];
			else{
				if(id.equalsIgnoreCase("-ip")){
					ip=args[i];
				}else if(id.equalsIgnoreCase("-bns")){
					nbusiness=Integer.parseInt(args[i]);
					snbusiness= String.valueOf(nbusiness);
				}else if(id.equalsIgnoreCase("-gestor")){
					gestor=args[i];
				}else if(id.equalsIgnoreCase("-idspath")){
					pathIdsTxt=args[i];		
					if(!pathIdsTxt.endsWith("/")) pathIdsTxt+="/";
				}else if(id.equalsIgnoreCase("-owlpath")){
					pathOWL=args[i];
					if(!pathOWL.endsWith("/")) pathOWL+="/";
				}else if(id.equalsIgnoreCase("-xmlpath")){
					pathXML=args[i];
					if(!pathXML.endsWith("/")) pathXML+="/";
				}else if(id.equalsIgnoreCase("-replica")){
					replica=new Boolean(args[i]);
				}else if(id.equalsIgnoreCase("-port")){
					port=Integer.parseInt(args[i]);
				}else if(id.equalsIgnoreCase("-owlfile")){
					nameFileModel=args[i];
				}else if(id.equalsIgnoreCase("-xmlfile")){
					nameXmlFile=args[i];
				}else if(id.equalsIgnoreCase("-updatexmlfile")){
					nameUpdateReplica=args[i];
				}else if(id.equalsIgnoreCase("-reportpath")){
					pathReport=args[i];
				}else if(id.equalsIgnoreCase("-querypath")){
					pathQuery=args[i];
				}else if(id.equalsIgnoreCase("-pathIReport")){
					pathIreport=args[i];
				}else if(id.equalsIgnoreCase("-pathDesignReports")){
					pathDesignReports=args[i];
				}else if(id.equalsIgnoreCase("-pathimports")){
					pathImportOtherXml=args[i];
				}else if(id.equalsIgnoreCase("-pwd")){
					pwd=args[i];
				}else if(id.equalsIgnoreCase("-excluir")){
					modulosIncompletos=args[i];
				}else if(id.equalsIgnoreCase("-import_help_views")){
					import_help_views=new Boolean(args[i]);
				}else if(id.equalsIgnoreCase("-BusinessFunctions")){
					BusinessFunctions=args[i];
				}
//				else if(id.equalsIgnoreCase("-modulesInclude")){
//					modulesInclude=args[i];
//				}
//				else if(id.equalsIgnoreCase("-modulesExclude")){
//					modulesExclude=args[i];
//				}
				if(id.equalsIgnoreCase("-modulesInclude")||id.equalsIgnoreCase("-modulesExclude")){
					System.err.println("\n\n ------------>> INFO:  usted ha indicado en los parámetros modulos: "+args[i]+" sin ningún efecto.\n En la nueva versión solo se excluyen automaticamente los modulos imcompletos ("+modulosIncompletos+")\n.. la clasificación ahora  se gestionan desde la aplicación dynagent\n\n_______________"); 
					
				}
				
				else if(id.equalsIgnoreCase("-pathImportReports")){
					pathImportReports=args[i];
				}
				else if(id.equalsIgnoreCase("-pathMigrationLog")){
					pathMigrationLogFile=args[i];
				}
				else if(id.equalsIgnoreCase("-pathDeletedLog")){
					pathDeletedLogFile=args[i];
				}
			}
		}
		if(pathIdsTxt==null) pathIdsTxt=pathOWL;
		if (gestor.equals(GestorsDBConstants.mySQL)) {
			if(port==null){
				System.err.println("Al seleccionar mysql hay que indicar el puerto con -port");
				return;
			}
		}
		
		if(pathImportOtherXml==null)
			pathImportOtherXml=pathXML;
		
		//se da la posibilidad de cambiar los paramatros en tiempo de ejecución:
		String parametros = "POR DEFECTO:\n import_help_views="+import_help_views+"\n fileOWL="+nameFileModel+"\n nameXmlFile="+nameXmlFile+"\n pathOWL="+pathOWL+"\n pathXML="+pathXML+"\n pathReport="+pathReport+"\n pathQuery="+pathQuery+"\n n. base datos="+nbusiness+"\n gestor="+gestor;
		
		
		//parametros+="\n modulesInclude="+modulesInclude+"  modulesExclude="+modulesExclude;
		if (port!=null)
			parametros+="\n puerto="+port;	
		parametros+="\n PWD "+pwd;
		parametros+="\n path log "+pathMigrationLogFile;
		parametros+="\n IP="+ip+"\n¿DESEA MODIFICARLO(S/N)?";
		String resp=Auxiliar.leeTexto(parametros);
		if(resp.equalsIgnoreCase("SI")||resp.equalsIgnoreCase("S")){
			gestor=Auxiliar.leeTexto("Introduzca el gestor de base de datos");
			do {
				snbusiness = Auxiliar.leeTexto("Introduzca el número de empresa con el que desea trabajar");
			} while (!Auxiliar.hasIntValue(snbusiness));
			nbusiness = new Integer(snbusiness);
			nameFileModel=Auxiliar.leeTexto("Introduzca el nombre del fichero owl que contiene el modelo de negocio");
			nameXmlFile=Auxiliar.leeTexto("Introduzca el nombre del fichero xml que contiene la configuración");
			gestor=Auxiliar.leeTexto("Introduzca el gestor de base de datos que quiere utilizar");
		}
		
		FactoryConnectionDB fcdb=Menu.setConnection(snbusiness, ip, port, gestor);
		if(pwd!=null) fcdb.setPwd(pwd);
		else pwd=fcdb.pwd;
		
		fcdb.setUsuario("dynagent");
		System.out.println("---------> PWD "+pwd+" " + DAOManager.getInstance().getBusiness());
			
		if(pathImportReports!=null)//Le añadimos el nombre de la subcarpeta, que sera el numero de la base de datos
			pathImportReports=new File(pathImportReports,snbusiness).getAbsolutePath();
		
		System.out.println("\n======================= INFO: Se va a  importar el owl="+nameFileModel+" del pathOWL="+pathOWL+" y el xml="+ nameXmlFile+"  del  pathXML"+pathXML+"  en la bbdd=dyna"+nbusiness+"  con el gestorBBDD="+gestor+" IP="+ip);
		
		
		System.out.println("---------> " + DAOManager.getInstance().getBusiness());
		char opcion;
		Menu menu = new Menu();
		System.out.println("INFO: Estas usando la Base de datos " + snbusiness);
		boolean menuContinue;//Se sale del menú si ha habido error
		
				
		
		do {
			opcion = menu.dameOpcion();
			menuContinue=false;
			OWLModel owlModel=null;
			
			switch (opcion) {
			
			case 'C':
				//menuContinue=CARGAR ONTOLOGIA EN MOTOR Y EJECUTAR REGLAS //menu.importGeneric(modulos,includeModules,hasImportReports(),hasImportQuerys(),pathOWL,pathXML,replica,nameFileModel,nameUpdateModel,nameUpdateReplica,fcdb,ip,nameXmlFile,pathReport,pathQuery,pathImportOtherXml);
				String uri = "file:///" + pathOWL + nameFileModel;
				ArrayList<Package> rulespackage=new ArrayList<Package>();
				HashSet<String> rules=new HashSet<String>();
				rules.add("CheckModel.drl");
				File currentParentFolder = new File("").getAbsoluteFile().getParentFile();
				File knowledgeFolder = new File(currentParentFolder, "Knowledge/src/rules");
				rulespackage.add(Menu.getRulesPackage(rules, knowledgeFolder.getAbsolutePath()+"/"));
				OWLDroolsRuler owlruler=null;
				
				//extraemos los modulos incompletos que daran la ontología base sobre la que hacer combinaciones de modulos
				owlModel= ProtegeOWL.createJenaOWLModelFromURI(uri);
				
				ExtractOWLView extractowl=null;
				//				ExtractOWLView extractowl=new ExtractOWLView(new OWL(owlModel),null,modulosIncompletos);
//				OWL owllimpiado=extractowl.extract();//modelo quitandole los modulos incompletos
				//protegeowl modelo completo una vez quitados los modulos incompletos o modulos a eliminar
				//System.out.println("\n\n.... Testeando modelo completo limpiado===============");
				//owlruler=new OWLDroolsRuler(owllimpiado,rulespackage);
				
				owlruler=new OWLDroolsRuler(new OWL(owlModel),rulespackage);
				
//prueba modulo servicio excluimos para ver como queda comerciales
//				OWL owlSinServicio=new ExtractOWLView(new OWL(owlModel),null,"MÓDULO_SERVICIO").extract();//modelo quitandole los modulos incompletos
//				owlSinServicio.saveInFile(Constants.pathVersionModelImported, "modeloSinServicio.owl");
//				 owlruler=new OWLDroolsRuler(owlSinServicio,rulespackage);

				
//TESTEAR TODOS LOS MODULOS				
				//TESTEAMOS TODOS LOS MODULOS UNO A UNO DE DOS FORMAS:
				//1) INCLUYENDO SOLO ESE MODULO 
				//2) INCLUYENDO TODOS LOS MODULOS EXCEPTO ESE (O LO QUE ES LO MISMO EXCLUYENDO ESE MODULO");
//				ArrayList<OWLIndividual> modulos=owllimpiado.getInstancias("MÓDULO_NEGOCIO");
//				for(int i=0;i<modulos.size();i++){
//					 System.out.println("\n\n\n.........Testeando MODULO: "+modulos.get(i).getName());
//					 owlModel= ProtegeOWL.createJenaOWLModelFromURI(uri);
//					 extractowl=new ExtractOWLView(new OWL(owlModel),modulos.get(i).getName(),null);
//					 OWL owlTransf=extractowl.extract();
//					 owlruler=new OWLDroolsRuler(owlTransf,rulespackage);
//					 
//					
//					 owlModel= ProtegeOWL.createJenaOWLModelFromURI(uri);
//					 extractowl=new ExtractOWLView(new OWL(owlModel),null,modulos.get(i).getName());
//					 owlTransf=extractowl.extract();					 
//					 owlruler=new OWLDroolsRuler(owlTransf,rulespackage);
//				}
				break;
				
			
			
			case 'G':
				boolean importReports=false;
				boolean importQueries=false;
				
				boolean deleteIndividuals = false;
				
		    	String resDel= Auxiliar.leeTexto("¿DESEA BORRAR LOS INDIVIDUOS DE ESTA BD? S/N");
				if (resDel.equalsIgnoreCase("S")||resDel.equalsIgnoreCase("SI")){
					resDel = Auxiliar.leeTexto("ESTO BORRARA TODOS LOS DATOS DE INDIVIDUOS SIN POSIBILIDAD DE MARCHA ATRAS. ¿CONFIRMA EL BORRADO? S/N");
					if (resDel.equalsIgnoreCase("S")||resDel.equalsIgnoreCase("SI")){
						deleteIndividuals = true;
					}
				}
				
				boolean importXMLConfiguration = deleteIndividuals;
				boolean insertUserAndRoles = deleteIndividuals;
				boolean insertIndex = deleteIndividuals;
				boolean deleteModulesAndExclusions = false;

				if(!deleteIndividuals){
					resDel= Auxiliar.leeTexto("¿DESEA SOBREESCRIBIR LOS MÓDULOS Y EXCLUSIONES ACTUALES? S/N");
					if (resDel.equalsIgnoreCase("S")||resDel.equalsIgnoreCase("SI")){
						deleteModulesAndExclusions = true;
					}
					resDel= Auxiliar.leeTexto("¿DESEA IMPORTAR EL XML DE CONFIGURACIÓN? S/N");
					if (resDel.equalsIgnoreCase("S")||resDel.equalsIgnoreCase("SI")){
						importXMLConfiguration = true;
						resDel= Auxiliar.leeTexto("LOS INDICES NO SERAN IMPORTADOS. ¿DESEA INSERTAR USUARIOS Y ROLES? S/N");
						if (resDel.equalsIgnoreCase("S")||resDel.equalsIgnoreCase("SI")){
							resDel = Auxiliar.leeTexto("ESTO PROVOCARÁ QUE SE BORREN LOS USUARIOS Y ROLES ACTUALES. ¿CONFIRMA LA INSERCIÓN? S/N");
							if (resDel.equalsIgnoreCase("S")||resDel.equalsIgnoreCase("SI")){
								insertUserAndRoles = true;
							}
						}
					}
				}
				if(importXMLConfiguration){
					importReports=hasImportReports();
					//importQueries=hasImportQuerys();
				}
				
				
				 uri = "file:///" + pathOWL + nameFileModel;
				 
				//modulos,includeModules,
				 owlModel= ProtegeOWL.createJenaOWLModelFromURI(uri);
				 extractowl=new ExtractOWLView(new OWL(owlModel),null,modulosIncompletos);
				 OWLModel protegeowl=extractowl.extract(modulosIncompletos!=null).getProtegeowlModel();
				
				//novedad versionamos el modelo 
				 OWL owlimportar=new OWL(protegeowl);
				 Date now =new Date(System.currentTimeMillis());
				 SimpleDateFormat formateador = new SimpleDateFormat("dd-MM-yyyy-hh-mm-a");
				 String nombreVersion=formateador.format(now)+"_"+nameFileModel;
 			     //owlimportar.versionarModelo(Constants.pathVersionModelImported,nombreVersion);
				 DataBaseMap dataBaseMap=null;
 			     if(deleteModulesAndExclusions){
 			    	dataBaseMap = new DataBaseMap(fcdb, false);
 					DeletableObject.deleteAllObjects(dataBaseMap.getClass("PROPIEDAD_EXCLUÍDA").getIdto(), dataBaseMap, fcdb);
 					DeletableObject.deleteAllObjects(dataBaseMap.getClass("PROPIEDAD_EN_CLASE_EXCLUÍDA").getIdto(), dataBaseMap, fcdb);
 					DeletableObject.deleteAllObjects(dataBaseMap.getClass("CLASE_EXCLUÍDA").getIdto(), dataBaseMap, fcdb);
 					DeletableObject.deleteAllObjects(dataBaseMap.getClass("MÓDULO_NEGOCIO").getIdto(), dataBaseMap, fcdb);
 					fcdb.removeConnections();//Remuevo las conexiones porque si no, por alguna extraña razon, luego da error al intentar usarlas en la creacion del dataBaseMap de migrator.processMigration 
 			     }
 			     
 			     if (deleteIndividuals&&import_help_views) {
					OWLParser owlParser = new OWLParser(owlModel, fcdb, new OWLIds(pathIdsTxt));
					menuContinue=menu.importGeneric(BusinessFunctions,import_help_views,owlParser, protegeowl, importReports, importQueries, pathXML, replica, nameUpdateModel, nameUpdateReplica, fcdb, nameXmlFile, pathReport, pathQuery, pathImportOtherXml, pathImportReports, deleteIndividuals, importXMLConfiguration, insertUserAndRoles, insertIndex, fcdb.getBusiness(),pwd);
 			     } else {
					AuxiliarMigrator migrator = new AuxiliarMigrator();
					
					menuContinue=migrator.processMigration(BusinessFunctions,import_help_views,protegeowl, importReports, importQueries, pathOWL, pathIdsTxt, pathXML, replica, nameUpdateModel, nameUpdateReplica, fcdb,dataBaseMap, nameXmlFile, pathReport, pathQuery, pathImportOtherXml, pathImportReports, importXMLConfiguration, insertUserAndRoles, insertIndex, pathMigrationLogFile, pathDeletedLogFile,pwd);
				}
 			    System.out.println("---> Fin de la importación");
				System.exit(0);
				break;
				
			case 'I':
				importReports = false;
				importQueries = false;
				deleteIndividuals = false;
				importXMLConfiguration = false;
				insertUserAndRoles = false;
				insertIndex = false;
				
		    	int sourceBns= Integer.valueOf(Auxiliar.leeTexto("INDIQUE EL NÚMERO DE LA BASE DE DATOS QUE CONTIENE LOS DATOS DE ORIGEN"));
				
				 uri = "file:///" + pathOWL + nameFileModel;
				//modulos,includeModules,
				 owlModel= ProtegeOWL.createJenaOWLModelFromURI(uri);
				 extractowl=new ExtractOWLView(new OWL(owlModel),null,modulosIncompletos);
				 protegeowl=extractowl.extract(modulosIncompletos!=null).getProtegeowlModel();
				 OWLParser owlParser = new OWLParser(owlModel, fcdb, new OWLIds(pathIdsTxt));
					
				//novedad versionamos el modelo 
				 owlimportar=new OWL(protegeowl);
				 now =new Date(System.currentTimeMillis());
				 formateador = new SimpleDateFormat("dd-MM-yyyy-hh-mm-a");
				 nombreVersion=formateador.format(now)+"_"+nameFileModel;
			     owlimportar.versionarModelo(Constants.pathVersionModelImported,nombreVersion);
				 
				menuContinue=menu.importGeneric(BusinessFunctions,import_help_views,owlParser, protegeowl,importReports,importQueries,pathXML,replica,nameUpdateModel,nameUpdateReplica,fcdb,nameXmlFile,pathReport,pathQuery,pathImportOtherXml,pathImportReports,deleteIndividuals,importXMLConfiguration,insertUserAndRoles,insertIndex,sourceBns,pwd);
 			    System.out.println("---> Fin de la importación");
				System.exit(0);
				break;
			case 'E':	
				insertIndex=false;
				insertUserAndRoles=false;
				resDel= Auxiliar.leeTexto("LOS INDICES NO SERAN IMPORTADOS. ¿DESEA INSERTAR USUARIOS Y ROLES? S/N");
				if (resDel.equalsIgnoreCase("S")||resDel.equalsIgnoreCase("SI")){
					resDel = Auxiliar.leeTexto("ESTO PROVOCARÁ QUE SE BORREN LOS USUARIOS Y ROLES ACTUALES. ¿CONFIRMA LA INSERCIÓN? S/N");
					if (resDel.equalsIgnoreCase("S")||resDel.equalsIgnoreCase("SI")){
						insertUserAndRoles = true;
					}
				}
				menuContinue=menu.importXML(hasImportReports(),/*hasImportQuerys()*/false,pathXML,replica,fcdb,nameXmlFile, insertUserAndRoles, insertIndex, pathReport, pathQuery, pathImportOtherXml,pathImportReports);
				break;
			case 'F':
				insertIndex=true;
				insertUserAndRoles=false;
				resDel= Auxiliar.leeTexto("LOS INDICES SERAN IMPORTADOS MACHACANDO EL CONTADOR, SIENDO ESTO UN PROBLEMA SI YA TIENE INDIVIDUOS EN BASE DE DATOS. ¿DESEA CONTINUAR? S/N");
				if (!resDel.equalsIgnoreCase("S")&&!resDel.equalsIgnoreCase("SI")){
					continue;
				}
				resDel= Auxiliar.leeTexto("¿DESEA INSERTAR USUARIOS Y ROLES? S/N");
				if (resDel.equalsIgnoreCase("S")||resDel.equalsIgnoreCase("SI")){
					resDel = Auxiliar.leeTexto("ESTO PROVOCARÁ QUE SE BORREN LOS USUARIOS Y ROLES ACTUALES. ¿CONFIRMA LA INSERCIÓN? S/N");
					if (resDel.equalsIgnoreCase("S")||resDel.equalsIgnoreCase("SI")){
						insertUserAndRoles = true;
					}
				}
				menuContinue=menu.importXML(hasImportReports(),/*hasImportQuerys()*/false,pathXML,replica,fcdb,nameXmlFile, insertUserAndRoles, insertIndex, pathReport, pathQuery, pathImportOtherXml,pathImportReports);
//			case 'F':
//				String clienteConfig=Auxiliar.leeTexto("Introduzca el nombre del cliente");
//				clienteConfig=clienteConfig.toLowerCase();
//				menuContinue=menu.importSpecific(hasImportReports(),hasImportQuerys(),pathXML,replica,clienteConfig,fcdb,ip,nameXmlFile, false, false, pathReport, pathQuery, pathImportOtherXml,pathImportReports);
//				break;
			case 'R':
				String cliente=null;
				if(pathDesignReports!=null){
					String[] str = pathDesignReports.split("/");
					cliente = str[str.length-1];
				}
				menuContinue=true;
				do {
					opcion = menu.dameOpcionReport(snbusiness, gestor, cliente, port, nameXmlFile);
					try {
						switch (opcion) {
						case 'S':
							System.out.println("Salir");
			
							break;
						case 'R':
							
							//REPORTParser.importReports(nameFileConfiguration,null,path,fcdb);
							
							//No borramos antes todos los reports porque puede haber reports que no esten en el xml que no queremos borrar.
							//Asi que si quiere borrar los actuales lo indicamos mediante la opcion borrar todos los reports
							menu.importReportsXML(pathXML,pathReport,fcdb,ip,nameXmlFile,null,pathImportOtherXml,pathImportReports);
							break;
						case 'I':
							String report=Auxiliar.leeTexto("Introduzca el nombre del report");
							
							//REPORTParser.importReports(nameFileConfiguration, queryR, path,fcdb);
							menu.importReportsXML(pathXML,pathReport,fcdb,ip,nameXmlFile,report,pathImportOtherXml,pathImportReports);
							break;
//						case 'M':
//							
//							MakeJrxmls.createDesign(nbusiness,fcdb, pathDesignReports,pathIreport);
//			
//							break;
						case 'D':
							String name=Auxiliar.leeTexto("Introduzca el nombre del report a borrar");
							deleteReport(name,fcdb, null);
							break;
						case 'A':
							String respRAll=Auxiliar.leeTexto("¿ESTA SEGURO QUE DESEA BORRAR TODOS LOS REPORTS?");
							if (respRAll.equalsIgnoreCase("S")||respRAll.equalsIgnoreCase("SI"))
								deleteReports(fcdb);
							break;
						case 'C':
							ReportClasificator rClasificator = new ReportClasificator(fcdb);
							rClasificator.setPathUserFiles(pathImportReports);
							rClasificator.startClasificator();
							break;
//						case 'G':
//							
//							String queryG=Auxiliar.leeTexto("Introduzca el nombre de la query");
//							queryG=queryG.toLowerCase();
//							REPORTParser.getDesign(pathDesignReports, queryG);
//							break;
//						case 'U':
//							
//							String queryU=Auxiliar.leeTexto("Introduzca el nombre de la query");
//							queryG=queryU.toLowerCase();
//							REPORTParser.updateDesign(pathDesignReports, queryG,pathIreport);
//							break;
			
						default:
							System.out.println("La opcion es incorreta");
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} while (opcion != 'S');
				break;
			default:
				if(opcion != 'S')
					System.out.println("La opcion es incorreta");
				break;
			}
		} while (opcion != 'S' && menuContinue);

		fcdb.removeConnections();
		System.out.println("*****bye******");
		ConceptLogger.getLogger().writeln(
				"Fin de sesión: " + Calendar.getInstance().getTime());
	}
	private static void deleteReport(String name, FactoryConnectionDB fcdb, InstanceService instanceService) throws Exception {
		boolean transaction = false;
		if (instanceService==null)
			transaction = true;
		if (transaction) {
			DBQueries.execute(fcdb, "START TRANSACTION;");
			instanceService = new InstanceService(fcdb, null, false);
			Connect.connectRuler(fcdb, instanceService);
			instanceService.setIk(new ServerEngine(fcdb));
		}
		//No se elimina si hay permisos, no se eliminan los permisos
		importAccess iac=new importAccess(null,fcdb,instanceService,null,null);
		//iac.deleteAccessReports(name);
		importAlias ial=new importAlias(null,fcdb,instanceService,null,null);
		//ial.deleteAliasReports(name);
		
		int idto=instanceService.getIk().getIdClass(Constants.CLS_REPORT_INDIVIDUAL);
		Integer ido=dynagent.tools.importers.configxml.Auxiliar.getIdo(idto, name, fcdb, instanceService.getDataBaseMap());
		if(ido!=null){
			ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>(); 
			list.add(new FactInstance(idto, ido, Constants.IdPROP_OBJECTDELETED, null, Constants.IDTO_BOOLEAN,Constants.DATA_BOOLEAN, null, null, null, Constants.CLS_REPORT_INDIVIDUAL, action.DEL_OBJECT));
			instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
			System.err.println("Borrado el informe "+name);
		}else{
			System.err.println("WARNING: No se ha borrado el informe "+name+" ya que no existe en base de datos");
		}
		
		if (transaction)
			DBQueries.execute(fcdb, "COMMIT;");
	}
	
	public static void deleteReports(FactoryConnectionDB fcdb) throws Exception {
		DBQueries.execute(fcdb, "START TRANSACTION;");
		InstanceService instanceService = new InstanceService(fcdb, null, false);
		Connect.connectRuler(fcdb, instanceService);
		instanceService.setIk(new ServerEngine(fcdb));
		
		importAccess iac=new importAccess(null,fcdb,instanceService,null,null);
		iac.deleteAccessReports(null);
		importAlias ial=new importAlias(null,fcdb,instanceService,null,null);
		ial.deleteAliasReports(null);
		
		int idto=instanceService.getIk().getIdClass(Constants.CLS_REPORT_INDIVIDUAL);
		ArrayList<Integer> idosList=dynagent.tools.importers.configxml.Auxiliar.getIdos(idto, fcdb, instanceService.getDataBaseMap());
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();
		for(int ido:idosList){ 
			list.add(new FactInstance(idto, ido, Constants.IdPROP_OBJECTDELETED, null, Constants.IDTO_BOOLEAN,Constants.DATA_BOOLEAN, null, null, null, Constants.CLS_REPORT_INDIVIDUAL, action.DEL_OBJECT));
		}
		
		instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
		
		DBQueries.execute(fcdb, "COMMIT;");
	}

	boolean importGeneric(String BusinessFunctions,boolean import_help_views,OWLParser owlParser, OWLModel owlModel, boolean importReports, boolean importQuerys, 
			String pathXML, boolean replica, String nameUpdateModel, String nameUpdateReplica, 
			FactoryConnectionDB fcdb, String nameXmlFile, String pathReport, 
			String pathQuery, String pathImportOtherXml, String pathImportReports, 
			boolean deleteIndividuals, boolean importXmlConfiguration, boolean insertUserAndRoles, 
			boolean insertIndex, int sourceBns,String pwd) throws Exception {
		boolean success=false;

		ParserReplicaXML parserRepl = new ParserReplicaXML();
		HashSet<Integer> idtosReplica = null;
		boolean updateReplica = false;
		Element xmlReplica = null;
		boolean replicaEnTienda = false;
		ImportReplica importRepl = null;
		if (nameUpdateReplica!=null) {
			String pathUpdateReplica = pathXML + nameUpdateReplica;
			xmlReplica = parserRepl.readXML(pathUpdateReplica);
			replicaEnTienda = !parserRepl.isSoyCentral();
			importRepl = new ImportReplica(/*parserRepl.getIdtos(), parserRepl.getHReplication()*/);
			if (deleteIndividuals) {
				updateReplica = true;
				if (replicaEnTienda)
					idtosReplica = importRepl.getIdtosReplicaXML(xmlReplica, false);
			} else {
				String res= Auxiliar.leeTexto("¿Va a CONFIGURAR LA RÉPLICA DEFINIDA EN EL FICHERO "+ nameUpdateReplica+"? (S/N)");
				if (res.equalsIgnoreCase("S")|| res.equalsIgnoreCase("SI")) {
					updateReplica = true;
					if (replicaEnTienda)
						idtosReplica = importRepl.getIdtosReplicaXML(xmlReplica, false);
				} else {
					if (replicaEnTienda)
						idtosReplica = importRepl.getIdtosReplicaDB(false);
				}
				System.out.println("idtosReplica " + idtosReplica);
			}
		}
		
		//String resp=Auxiliar.leeTexto("\n  ¿DESEA MANTENER EL MODELO OWL IMPORTADO?");
		//if(resp.equalsIgnoreCase("N")||resp.equalsIgnoreCase("NO")){
			insertOWLGeneric(BusinessFunctions,import_help_views,owlParser, owlModel, fcdb, deleteIndividuals, replica, idtosReplica, replicaEnTienda, sourceBns,pwd);
		//}
		
		if(importXmlConfiguration&&import_help_views){
			success=importXML(importReports, importQuerys, pathXML,replica, fcdb,nameXmlFile, insertUserAndRoles, insertIndex, pathReport, pathQuery, pathImportOtherXml, pathImportReports);
		}else{
			success=true;
		}
		
		if (nameUpdateModel!=null) {
			String res= Auxiliar.leeTexto("¿Desea MIGRAR LOS DATOS CONFORME A UNA TRANSFORMACIÓN DE DATOS DEFINIDA EN EL FICHERO "+ nameUpdateModel+"? (S/N)");
			if (res.equalsIgnoreCase("S")|| res.equalsIgnoreCase("SI")) {
				String pathUpdateModel = pathXML + nameUpdateModel;
				XMLModelUpdate.modelUpdate(pathUpdateModel, fcdb);
			}
		}
		
		if (updateReplica) {
			/*if (deleteIds) {
				System.out.println("Ya que decidió borrar los individuos, se borrarán también los individuos replicados");
				importRepl.deleteDataReplica(fcdb);
			}*/
			parserRepl.startUpdateConfiguration(xmlReplica);
			/*String resp=Auxiliar.leeTexto("¿Quiere actualizar las tablas de réplica con los datos de o_datos_atrib? Útil para control");
			if(resp.equalsIgnoreCase("SI")||resp.equalsIgnoreCase("S")){
				importRepl.startUpdateData();
			}*/
		}
		
		return success;
	}
	
	
	private void insertOWLGeneric(String BusinessFunctions,boolean import_help_views,OWLParser owlParser, OWLModel owlModel,FactoryConnectionDB fcdb, boolean deleteIds, 
			boolean replica, HashSet<Integer> idtosReplica, boolean replicaEnTienda, 
			int sourceBns,String pwd) throws Exception {
		//System.out.println("---> Insertando modelo genérico");
		//OWLParser.ImportarOWL(owlModel, fcdb, deleteIds, replica, idtosReplica, replicaEnTienda);
		//System.out.println("---> Modelo genérico insertado");
		
		IImporter importer = ImporterFactory.createImporter(GestorsDBConstants.postgreSQL,pwd);
		importer.setHost(fcdb.getDatabaseIP());
		if(!deleteIds){
			System.out.println("Creando backup de todo");
			if(!importer.createBackup(sourceBns, "all.backup", null, null))
				throw new Exception("No se ha podido crear el backup antes de la importación");
			
			HashSet<String> included=new HashSet<String>();
			HashSet<String> excluded=new HashSet<String>();
			included.add(Constants.CLS_CLASS.toLowerCase());
			included.add(Constants.CLS_DATA_PROPERTY.toLowerCase());
			included.add(Constants.CLS_OBJECT_PROPERTY.toLowerCase());
			included.add(Constants.CLS_DATA_PROPERTY.toLowerCase()+"#"+Constants.CLS_CLASS.toLowerCase());
			included.add(Constants.CLS_OBJECT_PROPERTY.toLowerCase()+"#"+Constants.CLS_CLASS.toLowerCase());
			
			System.out.println("Creando backup de clases y propiedades");
			if(!importer.createBackup(sourceBns, "idData.backup", included, excluded))
				throw new Exception("No se ha podido crear el backup de clases y propiedades");
			
			excluded=getSystemTablesNames();
			
			System.out.println("Creando backup de datos de negocio");
			if(!importer.createBackup(sourceBns, "businessData.backup", null, excluded))
				throw new Exception("No se ha podido crear el backup de datos de negocio");
		}
		
		try{
			importer.dropDataBase(fcdb.getBusiness());
			importer.createDataBase(fcdb.getBusiness());
			
//			System.out.println("Creando backup vacio de sistema");
//			success=importer.createBackup(fcdb.getBusiness(), "system.backup", null, null);
			
			if(!deleteIds){
				System.out.println("Restaurando datos de clases y propiedades");
				if(!importer.restoreBackup(fcdb.getBusiness(), "idData.backup", true)){
					/*System.err.println("No se han podido restaurar los datos de clases y propiedades");
					String res= Auxiliar.leeTexto("¿Desea continuar con la importación? (S/N)");
					if (!res.equalsIgnoreCase("S")&& !res.equalsIgnoreCase("SI")) {*/
						throw new Exception("Abortada importacion ya que no se han podido restaurar los datos de clases y propiedades");
					/*}*/
				}
			}
			
			owlParser.buildSystem();
			owlParser.buildDataBase(BusinessFunctions,import_help_views,deleteIds);
			
//			System.out.println("Creando backup vacio de todo");
//			success=importer.createBackup(fcdb.getBusiness(), "empty.backup", null, null);
			
			if(!deleteIds){
				System.out.println("Restaurando datos de negocio");
				/*if(!importer.restoreBackup(fcdb.getBusiness(), "businessData.backup", true)){
					System.err.println("No se han podido restaurar los datos de negocio");
					String res= Auxiliar.leeTexto("¿Desea continuar con la importación? (S/N)");
					if (!res.equalsIgnoreCase("S")&& !res.equalsIgnoreCase("SI")) //{
						throw new Exception("Abortada importacion ya que no se han podido restaurar los datos de negocio");				
				}*/
				owlParser.putDataBaseRestrictions();//Asignamos ahora las restricciones de la base de datos porque si estuviera de antes no sería posible restaurar los datos del backup, ya que inserta alguna tabla de relacion antes de que exista alguna tabla de objeto
			}
			
			owlParser.insertModelIndividuals();
			owlParser.insertHelp();//Lo insertamos despues de los individuos ya que para estos necesitamos saber su ido real
			
			System.out.println("---> Fin de la inserción de modelo");
		}catch(Exception ex){
			ex.printStackTrace();
			if(!deleteIds && fcdb.getBusiness().equals(sourceBns)){//Solo damos estas opciones si se intentaron mantener los datos trabajando sobre la misma base de datos
				System.out.println("A continuación podrá restaurar el original o mantener tanto el original como la importación para poder comparar las diferencias");
				String res= Auxiliar.leeTexto("¿Desea mantener lo importado en dyna"+fcdb.getBusiness()+"? (S/N)");
				if (res.equalsIgnoreCase("S")|| res.equalsIgnoreCase("SI")) {
					System.out.println("Se va a restaurar la base de datos original en dyna-"+fcdb.getBusiness());
					if(importer.restoreBackup(-fcdb.getBusiness(), "all.backup", false)){
						System.out.println("Backup restaurado correctamente en dyna"+(-fcdb.getBusiness()));
					}
				}else{
					System.out.println("Se va a restaurar la base de datos original en dyna"+fcdb.getBusiness());
					if(importer.restoreBackup(fcdb.getBusiness(), "all.backup", false)){
						System.out.println("Backup restaurado correctamente en dyna"+fcdb.getBusiness());
					}
				}
			}
			System.exit(0);
		}
	}
	
	
	public static HashSet<String> getSystemTablesNames() {
		HashSet<String> names = new HashSet<String>();
		names.add(Constants.CLS_CLASS.toLowerCase());
		names.add(Constants.CLS_DATA_PROPERTY.toLowerCase());
		names.add(Constants.CLS_OBJECT_PROPERTY.toLowerCase());
		names.add(Constants.CLS_DATA_PROPERTY.toLowerCase()+"#"+Constants.CLS_CLASS.toLowerCase());
		names.add(Constants.CLS_OBJECT_PROPERTY.toLowerCase()+"#"+Constants.CLS_CLASS.toLowerCase());
		names.add("helpclasses");
		names.add("helpproperties");
		names.add("instances");
		names.add("license");
		names.add("nomodifydb");//TODO Habria que excluirla o no dependiendo de si se ha vuelto a configurar las replicas
		names.add("t_herencias");
		names.add("t_link");
		names.add("sessions");
		names.add("locksid");
		names.add("operationsid");
		return names;
	}
	
	private boolean importSpecific(boolean importReports, boolean importQuerys, String pathXML, boolean replica, String cliente, FactoryConnectionDB fcdb, String nameXmlFile, boolean insertUserAndRoles, boolean insertIndex, String pathReport,String pathQuery,String pathImportOtherXml, String pathImportReports) throws Exception {
		String pathFinal=pathXML+cliente+"\\";
		return importXML(importReports, importQuerys, pathFinal, replica, fcdb, nameXmlFile, insertUserAndRoles, insertIndex, pathReport, pathQuery, pathImportOtherXml, pathImportReports);
	}
	
	public boolean importXML(boolean importReports, boolean importQuerys, String path, boolean replica, FactoryConnectionDB fcdb, String nameXmlFile, boolean insertUserAndRoles, boolean insertIndex, String pathReport,String pathQuery,String pathImportOtherXml, String pathImportReports) throws Exception{
		DBQueries.execute(fcdb, "START TRANSACTION;");
		InstanceService instanceService = new InstanceService(fcdb, null, false);
		Connect.connectRuler(fcdb, instanceService);
		IKnowledgeBaseInfo ik=instanceService.getIk();
		instanceService.setIk(new ServerEngine(fcdb));
		Element configXML=Auxiliar.getXml(path+nameXmlFile); 
		
		//System.out.println("---> Cargado XML de configuración");
		Element usuariosXML=configXML.getChild(ConstantsXML.USUARIOS);
		Element rolesXML=configXML.getChild(ConstantsXML.ROLES);
		Element usuarioRolesXML=configXML.getChild(ConstantsXML.USUARIO_ROLES);
		
		Element accessXML=configXML.getChild(ConstantsXML.ACCESS_ROOT);
		Element aliasXML=configXML.getChild(ConstantsXML.ALIAS_ROOT);
		//Element areafuncrestXML=configXML.getChild(ConstantsXML.AREAS_FUNC_REST);
		Element cardXML=configXML.getChild(ConstantsXML.CARDINALITIES);
		Element cardmedXML=configXML.getChild(ConstantsXML.CARDINALITIES_MED);
		Element columnpropXML=configXML.getChild(ConstantsXML.COLUMN_PROPERTIES);
		Element groupsXML=configXML.getChild(ConstantsXML.GROUPS);
		Element orderpropXML=configXML.getChild(ConstantsXML.ORDER_PROPERTIES);
		Element utasksXML=configXML.getChild(ConstantsXML.UTASKS);
		Element importexportXML=configXML.getChild(ConstantsXML.IMPORTEXPORTS);
		Element areafunXML=configXML.getChild(ConstantsXML.AREAS_FUNC);
		Element indexXML=configXML.getChild(ConstantsXML.INDEX_ROOT);
		Element rangesXML=configXML.getChild(ConstantsXML.RANGES);
		Element classesXML=configXML.getChild(ConstantsXML.CLASSES_ROOT);
		Element defValuesXML=configXML.getChild(ConstantsXML.DEFAULT_VALUES);
		Element essentialPropXML=configXML.getChild(ConstantsXML.ESSENTIAL_PROPERTIES);
		Element propertiesXML=configXML.getChild(ConstantsXML.PROPERTIES);
		Element globalClasesXML=configXML.getChild(ConstantsXML.GLOBAL_CLASES);
		Element requiredsXML=configXML.getChild(ConstantsXML.REQUIRED_PROPERTIES);
		Element reportsXML=null;
		//if (importReports)//Comentado ya que si hay permisos que dijeran algo sobre estos daría error al configurarlos(aunque luego no se insertan en access) al no existir
			reportsXML=configXML.getChild(ConstantsXML.REPORTS);
		if(importReports && reportsXML!=null && pathReport==null){
			System.err.println("ERROR: El archivo de configuración tiene Reports pero no se ha indicado el parametro -reportpath al ejecutar el importador");
			return false;
		}
		
		if(importReports && reportsXML!=null && pathImportReports==null){
			System.err.println("ERROR: El archivo de configuración tiene Reports pero no se ha indicado el parametro -pathImportReports al ejecutar el importador");
			return false;
		}
		
		Element querysXML=configXML.getChild(ConstantsXML.QUERYS);
		if(importQuerys && querysXML!=null && pathQuery==null){
			System.err.println("ERROR: El archivo de configuración tiene Query pero no se ha indicado el parametro -querypath al ejecutar el importador");
			return false;
		}
		Element printSequenceXML=configXML.getChild(ConstantsXML.PRINT_SEQUENCE);
		Element associatedIndividualXML=configXML.getChild(ConstantsXML.ASSOCIATED_INDIVIDUAL);
		Element individualXML=configXML.getChild(ConstantsXML.INDIVIDUALS);
		Element masksXML=configXML.getChild(ConstantsXML.MASKS);
		
		ConfigData configImport=new ConfigData();
		String sufixIndex = null;
		if (replica)
			sufixIndex = getSufixIndex();
		importUsuarios iu=null;
		importUsuarioRoles iur=null;
		if (insertUserAndRoles) {
			iu=new importUsuarios(usuariosXML,fcdb,instanceService,sufixIndex,configImport,pathImportOtherXml);
			iur=new importUsuarioRoles(usuarioRolesXML,fcdb,instanceService,configImport,pathImportOtherXml);
		}
		importRoles irl=new importRoles(rolesXML,fcdb,instanceService,sufixIndex,configImport,pathImportOtherXml);
		importGroups igp=new importGroups(groupsXML,fcdb,instanceService,configImport,pathImportOtherXml);
		importAreaFunc iaf=new importAreaFunc(areafunXML,fcdb,instanceService,configImport,pathImportOtherXml);
		importUtask iut=new importUtask(utasksXML,fcdb,instanceService,configImport,pathImportOtherXml);
		importImportExport iie=new importImportExport(importexportXML,fcdb,instanceService,configImport,pathImportOtherXml);
		importAccess iac=new importAccess(accessXML,fcdb,instanceService,configImport,pathImportOtherXml);
		importAlias ial=new importAlias(aliasXML,fcdb,instanceService,configImport,pathImportOtherXml);
		importEssentialProperties iep=new importEssentialProperties(essentialPropXML,fcdb,instanceService,configImport,pathImportOtherXml); 
		importCardMed icm=new importCardMed(cardmedXML,fcdb,instanceService,configImport,pathImportOtherXml);
		importColumnProperties icp=new importColumnProperties(columnpropXML,fcdb,instanceService,configImport,pathImportOtherXml,ik);
		importOrderProp iop=new importOrderProp(orderpropXML,fcdb,instanceService,configImport,pathImportOtherXml);
		importReports irp=null;
		importCard icd=new importCard(cardXML,fcdb,instanceService,configImport,pathImportOtherXml);
		importMask imk=new importMask(masksXML,fcdb,instanceService,configImport,pathImportOtherXml);
		//importAreaFuncRes iafr=new importAreaFuncRes(areafuncrestXML,fcdb);
		importIndex ind=null;
		if(insertIndex)
			ind=new importIndex(indexXML,fcdb,instanceService,sufixIndex,insertIndex,configImport,pathImportOtherXml);
		importProperties iProp=new importProperties(propertiesXML,fcdb,instanceService,configImport,pathImportOtherXml);
		importClasses icls=new importClasses(classesXML,fcdb,instanceService,configImport,pathImportOtherXml);
		importGlobalClases igc=new importGlobalClases(globalClasesXML,fcdb,instanceService,configImport,pathImportOtherXml);
		importRequired irq=new importRequired(requiredsXML,fcdb,instanceService,configImport,pathImportOtherXml); 
		
		//Lo hacemos aqui por si algo de lo anterior influye en lo que necesitamos del motor
//		InstanceService instanceService = new InstanceService(fcdb, null, false);
//		Connect.connectRuler(fcdb, instanceService);
//		IKnowledgeBaseInfo ik=instanceService.getIk();
		
		importDefValues idef=new importDefValues(defValuesXML,fcdb, instanceService, sufixIndex,configImport,pathImportOtherXml,ik);
		importRanges ir=new importRanges(rangesXML,fcdb, instanceService, ik, configImport,pathImportOtherXml);
		importPrintSequence ipp=new importPrintSequence(printSequenceXML,fcdb,instanceService,configImport,pathImportOtherXml);
		importAssociatedIndividual iai=new importAssociatedIndividual(associatedIndividualXML,fcdb,instanceService,configImport,pathImportOtherXml);
		//if (importReports)
			irp=new importReports(reportsXML,pathReport,fcdb,instanceService,configImport,pathImportOtherXml,pathImportReports);
		importQuerys iqr=new importQuerys(querysXML,pathQuery,fcdb,instanceService,configImport,pathImportOtherXml);
		importIndividual iI=new importIndividual(individualXML,fcdb,instanceService,configImport,pathImportOtherXml,ik);
		
		boolean success=true;
		
		//TODO Abrimos la conexion InstanceDao por ejemplo ya que todos los daos comparten la misma conexion.
		// Esto lo hemos hecho asi porque daba un error al intentar importar ya que en Auxiliar se abria y
		// se cerraban demasiadas veces los daos. Ahora en auxiliar no se cierra en ningun sitio por lo que
		// la conexion que creamos aqui no es machacada. Queda pendiente estructurarlo mejor para que DAOManager
		// maneje todo esto, creando todos los daos y abriendo-cerrando una unica conexion
		InstanceDAO dao=new InstanceDAO();
		dao.open();
		
		if (insertUserAndRoles) {
			if (usuariosXML!=null){
				System.out.println("---> Configurando los usuarios");
				if(!iu.configData())
					success=false;
			}
			if (rolesXML!=null){
				System.out.println("---> Configurando los roles");
				if(!irl.configData())
					success=false;
			}
			if (usuarioRolesXML!=null){
				System.out.println("---> Configurando los roles para usuarios");
				if(!iur.configData())
					success=false;
			}
		}
		
//		if (areafunXML!=null){
//			System.out.println("---> Configurando las areas funcionales");
//			//if(iaf.configData())
//				//System.out.println("---> Areas funcionales configuradas");
//			//else 
//			if(!iaf.configData())
//				success=false;
//		}
//		
		if (utasksXML!=null){
			System.out.println("---> Configurando las Utasks");
			if(!iut.configData())
				success=false;
		}
		if (importexportXML!=null){
			System.out.println("---> Configurando los Import/Export");
			if(!iie.configData())
				success=false;
		}
//		if (printSequenceXML!=null){
//			System.out.println("---> Configurando las secuencias de Impresion");
//			if(!ipp.configData())
//				success=false;
//		}
//		if (associatedIndividualXML!=null){
//			System.out.println("---> Configurando los individuos asociados");
//			if(!iai.configData())
//				success=false;
//		}
		if (reportsXML!=null){
			System.out.println("---> Configurando los reports");
			if(importReports && !irp.configData())
				success=false;
		}
		if (querysXML!=null){
			System.out.println("---> Configurando las querys");
			if(importQuerys && !iqr.configData())
				success=false;
		}
		if (accessXML!=null){
			System.out.println("---> Configurando los permisos");
			if(!iac.configData())
				success=false;
		}
		if (groupsXML!=null){
			System.out.println("---> Configurando los grupos");
			if(!igp.configData())
				success=false;
		}
		if (aliasXML!=null){
			System.out.println("---> Configurando los alias");
			if(!ial.configData())
				success=false;
		}
		if (essentialPropXML!=null){
			System.out.println("---> Configurando las essential properties");
			if(!iep.configData())
				success=false;
		}
		if (cardmedXML!=null){
			System.out.println("---> Configurando las cardinalidades medias");
			if(!icm.configData())
				success=false;
		}
		if (columnpropXML!=null){
			System.out.println("---> Configurando las column properties");
			if(!icp.configData())
				success=false;
		}
		
		if (orderpropXML!=null){
			System.out.println("---> Configurando las order properties");
			if(!iop.configData())
				success=false;
		}
		if (cardXML!=null){
			System.out.println("---> Configurando las cardinalidades");
			if(!icd.configData())
				success=false;
		}
		if (masksXML!=null){
			System.out.println("---> Configurando las mascaras");
			if(!imk.configData())
				success=false;
		}
//		/*if (areafuncrestXML!=null){
//			System.out.println("---> Configurando las restricciones de las areas funcionales");
//			if(iafr.configData())
//				System.out.println("---> Restricciones de las areas funcionales configuradas");
//			else success=false;
//		}*/
		if(insertIndex){
			if (indexXML!=null){
				System.out.println("---> Configurando los indices");
				if(!ind.configData())
					success=false;
			}
		}
//		if (rangesXML!=null){
//			System.out.println("---> Configurando los rangos");
//			if(!ir.configData())
//				success=false;
//		}
		if (defValuesXML!=null){
			System.out.println("---> Configurando los valores por defecto");
			if(!idef.configData())
				success=false;
		}
//		if (propertiesXML!=null){
//			System.out.println("---> Configurando las properties");
//			if(!iProp.configData())
//				success=false;
//		}
//		if (classesXML!=null){
//			System.out.println("---> Configurando las clases");
//			if(!icls.configData())
//				success=false;
//		}
//		if (globalClasesXML!=null){
//			System.out.println("---> Configurando las clases globales");
//			if(!igc.configData())
//				success=false;
//		}
//		if (individualXML!=null){
//			System.out.println("---> Configurando los individuos");
//			if(!iI.configData())
//				success=false;
//		}
		if (requiredsXML!=null){
			System.out.println("---> Configurando los campos requeridos");
			if(!irq.configData())
				success=false;
		}

		dao.close();
		
		if(success){
			System.out.println("---> Insertando la configuración");
			
			boolean importSuccess=false;
			boolean successReports=true;
			try{
				if (insertUserAndRoles) {
					iu.importData();
					irl.importData();
					iur.importData();
				} else
					irl.setRolesFromDB(fcdb);
				
				iaf.importData();
				if (importQuerys)
					iqr.importData();
				
	//			HashMap<String,Integer> hRoles = irl.getRoles();
	//			
	//			iut.setRoles(hRoles);
				iut.importData();
				
	//			iie.setRoles(hRoles);
				iie.importData();
				
				ipp.importData();
				iai.importData();
				
				if (importReports){
					try{
						HashSet<String> listR = irp.configImport.getReportList();
						for (String reportName : listR) {
							deleteReport(reportName, fcdb, instanceService);
						}
						irp.importData();
					}catch(Exception ex){
						ex.printStackTrace();
						successReports=false;
					}
				}
				iac.importData();
				igp.importData();
				ial.importData();
				iep.importData();
				icm.importData();
				icp.importData();
				iop.importData();
				icd.importData();
				imk.importData();
				//iafr.importData();
				if(insertIndex)
					ind.importData();
				ir.importData();
				idef.importData();
				irq.importData();
				iProp.importData();
				icls.importData();
				iI.importData();
				if (replica)
					igc.importData();
				importSuccess=true;
			}finally{
				if(importSuccess)
					DBQueries.execute(fcdb, "COMMIT;");
				else DBQueries.execute(fcdb, "ROLLBACK;");
			}
			System.out.println("---> Fin de la configuración");
			if(!successReports){
				System.err.println("WARNING: No se han importado todos los reports correctamente");
				return false;
			}else return true;
		}else{
			DBQueries.execute(fcdb, "ROLLBACK;");
			System.err.println("ERROR: Configuración con errores. Importacion no realizada.");
			return false;
		}
	}
	private String getSufixIndex() throws SQLException, NamingException {
		String sufixIndex = null;
		ConfigurationDAO cDao = new ConfigurationDAO();
		cDao.open();
		LinkedList<Object> lObj = cDao.getByID("sufix_tienda");
		if (lObj.size()>0) {
			System.out.println("sufix_tienda " + lObj.getFirst());
			sufixIndex = (String)lObj.getFirst();
		}
		cDao.close();
		return sufixIndex;
	}
	private void importReportsXML(String path, String pathReport, FactoryConnectionDB fcdb, String ip,String nameXmlFile, String uniqueReportInclude,String pathImportOtherXml,String pathImportReports) throws Exception{
		BufferedReader in = new BufferedReader(new FileReader(path+nameXmlFile)); 
		String dataS="", buff="";
		while(buff!= null){
			dataS+=buff;
			buff=in.readLine();
		}
		
		DBQueries.execute(fcdb, "START TRANSACTION;");
		InstanceService instanceService = new InstanceService(fcdb, null, false);
		Connect.connectRuler(fcdb, instanceService);
		instanceService.setIk(new ServerEngine(fcdb));
		
		Document configDOC=jdomParser.readXML(dataS);
		Element configXML=configDOC.getRootElement();
		System.out.println("---> Cargado XML de configuración");
		Element aliasXML=configXML.getChild(ConstantsXML.ALIAS_ROOT);
		Element accessXML=configXML.getChild(ConstantsXML.ACCESS_ROOT);
		Element reportsXML=configXML.getChild(ConstantsXML.REPORTS);
		
		ConfigData configImport=new ConfigData();
		importAlias ial=new importAlias(aliasXML,fcdb,instanceService,configImport,pathImportOtherXml);
		importAccess iac=new importAccess(accessXML,fcdb,instanceService,configImport,pathImportOtherXml);
		importReports irp=new importReports(reportsXML,pathReport,fcdb,instanceService,configImport,pathImportOtherXml, pathImportReports);
		
		boolean success=true;
		
		if (reportsXML!=null){
			System.out.println("---> Configurando los reports");
			if(!irp.configData(uniqueReportInclude))
				success=false;
		}
		if (accessXML!=null){
			System.out.println("---> Configurando los permisos");
			if(!iac.configData(true)/*Solo nos interesan los accesos de los reports que estamos configurando*/)
				success=false;
		}
		if (aliasXML!=null){
			System.out.println("---> Configurando los alias");
			if(!ial.configData(true)/*Solo nos interesan los alias de los reports que estamos configurando*/)
				success=false;
		}
		
		if(success){
			boolean importSuccess=false;
			try{
				if (uniqueReportInclude!=null)
					deleteReport(uniqueReportInclude, fcdb, instanceService);
				else {
					HashSet<String> listR = irp.configImport.getReportList();
					for (String reportName : listR) {
						deleteReport(reportName, fcdb, instanceService);
					}
				}
				irp.importData();
				iac.importData();
				ial.importData();
				importSuccess=true;
			}finally{
				if(importSuccess)
					DBQueries.execute(fcdb, "COMMIT;");
				else DBQueries.execute(fcdb, "ROLLBACK;");
			}
			System.out.println("---> Fin de la configuración");
		}else{
			DBQueries.execute(fcdb, "ROLLBACK;");
			System.err.println("ERROR: Configuración con errores. Importacion no realizada.");
		}
	}
	
	public static FactoryConnectionDB setConnection(String snbusiness, String ip, Integer port, String gestor){
		DAOManager.getInstance().setBusiness(snbusiness);
		FactoryConnectionDB fcdb = new FactoryConnectionDB(new Integer(snbusiness),true,ip,gestor);
		if ((gestor.equals(GestorsDBConstants.mySQL) || gestor.equals(GestorsDBConstants.postgreSQL))&& port!=null)
			fcdb.setPort(port);
		setConnectionDAO(fcdb);
		return fcdb;
	}
	public static InstanceService setConnection(FactoryConnectionDB fcdb, DataBaseMap dataBaseMap) {
		InstanceService m_IS = new InstanceService(fcdb, null, false);
		m_IS.setDataBaseMap(dataBaseMap);
		//Connect.connectRulerServer(fcdb, m_IS);
		return m_IS;
	}
	public static void setConnectionDAO(FactoryConnectionDB fcdb){
		DAOManager.getInstance().setFactConnDB(fcdb);
		DAOManager.getInstance().setCommit(true);
	}
	
	public char dameOpcion() {
		System.out.println("\n\n===========================================================================================");
		System.out.println("\n           DYNAGENT CONFIG IMPORT   \n   ");
		System.out.println("\n OPCIONES:");
		System.out.println("C.-Chequear modelo");
		System.out.println("G.-Importar modelo, xml y reports");
		System.out.println("E.-Importar xml y reports");
		System.out.println("F.-Importar xml con índices y reports");
		System.out.println("I.-Importar modelo restaurando datos desde otra base de datos");
		//System.out.println("F.-Importar configuración especifica ");
		System.out.println("R.-Gestionar Reports");
		System.out.println("\nS.-SALIR");
		System.out.println("\n===========================================================================================");
		System.out.println("Recuerde que antes de importar debe tener bien definida la tabla Configuration");

		String texto = Auxiliar.leeTexto("SELECCIONE UNA OPCIÓN");
		char opcion;
		if (texto!=null)
			opcion = texto.toUpperCase().charAt(0);
		else
			opcion='0';
		return opcion;
	}
	
	public char dameOpcionReport(String db, String gestor, String cliente, Integer port, String nameConfigXML) {
		System.out.println("\n\n===========================================================================================");
		System.out.println("\n           DYNAGENT REPORT IMPORT   \n    @autor: David Amian Valle  - david.amian.valle@gmail.com");
		String dbStr = "\nBase de datos: " + db + " Gestor: " + gestor;
		if (port!=null)
			dbStr += " Port:" + port;
		dbStr += " ConfigXML:" + nameConfigXML;
		System.out.println(dbStr);
		System.out.println("Cliente Diseños: " + cliente);
		System.out.println("\n OPCIONES:");
//		System.out.println("M.-Crear Diseños de Reports");
		System.out.println("I.-Importar un Report concreto");
		System.out.println("R.-Importar todos los Reports ");
		System.out.println("D.-Borrar Report");
		System.out.println("A.-Borrar todos los Reports");
		System.out.println("C.-Clasificar Reports");
//		System.out.println("G.-Recoger Diseño");
//		System.out.println("U.-Actualizar Diseños");
		System.out.println("\nS.-SALIR");
		System.out.println("\n===========================================================================================");
		String texto = Auxiliar.leeTexto("SELECCIONE UNA OPCIÓN");
		char opcion;
		if (texto!=null)
			opcion = texto.toUpperCase().charAt(0);
		else
			opcion='0';
		return opcion;
	}
	
	public static boolean hasImportReports(){
		boolean importReports = false;
		String respR=Auxiliar.leeTexto("¿DESEA IMPORTAR LOS REPORTS DEFINIDOS EN EL XML DE CONFIGURACIÓN?");
		if(respR.equalsIgnoreCase("S")||respR.equalsIgnoreCase("SI"))
			importReports = true;
		
		return importReports;
	}
	public static boolean wantDeleteReports(){
		boolean deleteReports = false;
		String respR=Auxiliar.leeTexto("HA HECHO TRANSFORMACION DE MODELO Y ES POSIBLE QUE LOS REPORTS NO ESTEN ACORDE A EL, ¿DESEA BORRARLOS?");
		if(respR.equalsIgnoreCase("S")||respR.equalsIgnoreCase("SI"))
			deleteReports = true;
		
		return deleteReports;
	}
	public static boolean hasImportQuerys(){
		boolean importQuerys = false;
		String respR=Auxiliar.leeTexto("¿DESEA RESTAURAR LA TABLA DE QUERYS CON LAS DEFINIDAS EN EL XML DE CONFIGURACIÓN?");
		if(respR.equalsIgnoreCase("S")||respR.equalsIgnoreCase("SI"))
			importQuerys = true;
		
		return importQuerys;
	}
	
	public  static Package getRulesPackage(HashSet<String>rules,String pathfiles) throws Exception{
		PackageBuilder builder=new PackageBuilder();
		Iterator it=rules.iterator();
		while(it.hasNext()){
			String ruleFile=(String)it.next();
			System.out.println( "Loading file: " + ruleFile );   
			FileReader fr=new FileReader(pathfiles+ruleFile);
			builder.addPackageFromDrl(fr );
			if(builder.hasErrors())
				System.out.println(builder.getErrors());
		}
		Package pkg = builder.getPackage();
		return  pkg;		
	}
	
	/**
	 * Copia la licencia desde la base de datos fcdb a la base de datos fcdb2
	 * @param fcdb
	 * @param fcdb2
	 * @throws SQLException
	 * @throws NamingException
	 */
	public static void copyLicense(FactoryConnectionDB fcdb, FactoryConnectionDB fcdb2) throws SQLException, NamingException {
		System.out.println("Copiando licencia de dyna"+fcdb.getBusiness()+" a dyna"+fcdb2.getBusiness());
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		String sql = "select users,fechamax,"+generateSQL.getCharacterBegin()+"type"+generateSQL.getCharacterEnd()+" from license";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		String userMax=null;
		String fechaMax=null;
		String type=null;

		con = fcdb.createConnection(true); 
		st = con.getBusinessConn().createStatement();
		rs = st.executeQuery(sql);
		try{
			if (rs.next()){
				userMax = rs.getString(1);
				fechaMax = rs.getString(2);
				type = rs.getString(3);
				
				String sqlDelete = "delete from license";
				AuxiliarQuery.dbExecUpdate(fcdb2, sqlDelete, true);
				
				String sqlInsert = "insert into license(users,fechamax,"+generateSQL.getCharacterBegin()+"type"+generateSQL.getCharacterEnd()+")";
				sqlInsert+=" values('"+userMax+"','"+fechaMax+"','"+type+"')";
				AuxiliarQuery.dbExecUpdate(fcdb2, sqlInsert, true);
			}
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					fcdb.close(con);
			} catch (SQLException e) {
				System.out.println("FALLO AL Copiar licencia");
				e.printStackTrace();
			}
		}
	}
}
