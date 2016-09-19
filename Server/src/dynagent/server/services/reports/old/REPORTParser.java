package dynagent.server.services.reports.old;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import net.sf.jasperreports.engine.JRException;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Access;
import dynagent.common.basicobjects.Alias;
import dynagent.common.basicobjects.Groups;
import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.basicobjects.Properties;
import dynagent.common.basicobjects.SReport;
import dynagent.common.basicobjects.SSubReport;
import dynagent.common.basicobjects.TClase;
import dynagent.common.basicobjects.T_Herencias;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.knowledge.Category;
import dynagent.common.knowledge.action;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.jdomParser;
import dynagent.server.database.dao.AccessDAO;
import dynagent.server.database.dao.AliasDAO;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.GroupsDAO;
import dynagent.server.database.dao.InstanceDAO;
import dynagent.server.database.dao.O_Datos_AttribDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.RolesDAO;
import dynagent.server.database.dao.SReportDAO;
import dynagent.server.database.dao.SSubReportDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.database.dao.T_HerenciasDAO;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.services.QueryReportParser;
import dynagent.server.services.ReportService;
import dynagent.server.services.reports.EditReports;
import dynagent.server.services.reports.FilterName;
import dynagent.server.services.reports.GenerateJRXML;
import dynagent.server.services.reports.ParQuery;


public class REPORTParser {
	private HashMap<Integer,String> hPropxType=null;
	
	public static void delete(String query,boolean deleteIds) throws SQLException, NamingException{
		TClaseDAO cDAO=new TClaseDAO();
		cDAO.open();
		TClase queryDB=cDAO.getTClaseByName(query);
		cDAO.close();
		
		if (queryDB!=null){
			cDAO.open();
			TClase paramQ=cDAO.getTClaseByName("Params_"+queryDB.getName());
			cDAO.close();
			
			if(deleteIds)
				deleteClase(queryDB);
			if (paramQ!=null){
				if(deleteIds){
					deleteClase(paramQ);
					deleteProperties(paramQ);
				}
				deleteInstances(paramQ);
				deleteHerencia(paramQ);
				deleteAccess(paramQ);
				deleteGroups(paramQ);
			}
			deleteInstances(queryDB);
			deleteAlias(queryDB);
			deleteSReport(queryDB);
			deleteSSubReport(queryDB);
			deleteHerencia(queryDB);
			deleteAccess(queryDB);
			deleteGroups(queryDB);
			System.out.println("EL REPORT: "+query+" HA SIDO BORRADO");
		}else{
			System.err.println("EL REPORT: "+query+" NO SE ENCUENTRA EN LA BASE DE DATOS");
		}
	}
	public static void deleteAll(String gestorDB,boolean deleteIds) throws SQLException, NamingException{
		deleteAlias(gestorDB);
		if(deleteIds){
			deleteClase();
			deleteProperties();
		}
		deleteInstances();
		deleteHerencia();
		deleteSReport();
		deleteSSubReport();
		deleteAccess();
		deleteGroups();
		System.out.println("TODOS LOS REPORTS HAN SIDO BORRADOS");
	}
	
	private static void deleteSSubReport(TClase clase) throws SQLException, NamingException {
		SSubReportDAO sDAO= new SSubReportDAO();
		sDAO.open();
		sDAO.deleteCond("ID_TO LIKE '"+clase.getIDTO()+"'");
		sDAO.close();
	}
	private static void deleteAlias(TClase clase) throws SQLException, NamingException {
		AliasDAO aDAO=new AliasDAO();
		aDAO.open();
		aDAO.deleteCond("UTASK LIKE '"+clase.getName()+"'");
		aDAO.close();
	}
	private static void deleteProperties(TClase clase) throws SQLException, NamingException {
		InstanceDAO iDAO=new InstanceDAO();
		iDAO.open();
		LinkedList<Object> li=iDAO.getAllCond("IDTO LIKE '"+clase.getIDTO()+"'");
		iDAO.close();
		Iterator<Object> iti=li.iterator();
		while(iti.hasNext()){
			Instance inst=(Instance) iti.next();
			PropertiesDAO pDAO=new PropertiesDAO();
			pDAO.open();
			pDAO.deleteCond("PROP LIKE '"+inst.getPROPERTY()+"'");
			pDAO.close();
			iDAO.open();
			iDAO.deleteCond("PROPERTY LIKE '"+inst.getPROPERTY()+"'");
			iDAO.close();
		}
	}
	private static void deleteClase(TClase clase) throws SQLException, NamingException {
		TClaseDAO cDAO= new TClaseDAO();
		cDAO.open();
		cDAO.deleteCond("IDTO LIKE '"+clase.getIDTO()+"'");
		cDAO.close();
	}
	private static void deleteInstances(TClase clase) throws SQLException, NamingException {
		InstanceDAO iDAO=new InstanceDAO();
		iDAO.open();
		iDAO.deleteCond("IDTO LIKE '"+clase.getIDTO()+"'");
		iDAO.close();
	}
	private static void deleteSReport(TClase clase) throws SQLException, NamingException {
		SReportDAO qDAO= new SReportDAO();
		qDAO.open();
		qDAO.deleteCond("ID_TO LIKE '"+clase.getIDTO()+"'");
		qDAO.close();
	}
	private static void deleteHerencia(TClase clase) throws SQLException, NamingException {
		T_HerenciasDAO hDAO=new T_HerenciasDAO();
		hDAO.open();
		hDAO.deleteCond("ID_TO LIKE '"+clase.getIDTO()+"'");
		hDAO.close();
	}
	private static void deleteAccess(TClase clase) throws SQLException, NamingException {
		AccessDAO aDAO=new AccessDAO();
		aDAO.open();
		aDAO.deleteCond("IDTO LIKE '"+clase.getIDTO()+"' OR TASK LIKE '"+clase.getIDTO()+"'");
		aDAO.close();
	}
	private static void deleteGroups(TClase clase) throws SQLException, NamingException {
		GroupsDAO aDAO=new GroupsDAO();
		aDAO.open();
		aDAO.deleteCond("UTASK LIKE '"+clase.getIDTO()+"' OR CLASS LIKE '"+clase.getIDTO()+"'");
		aDAO.close();
	}

	private static void deleteSSubReport() throws SQLException, NamingException {
		SSubReportDAO sDAO= new SSubReportDAO();
		sDAO.open();
		sDAO.deleteCond("1=1");
		sDAO.close();
	}
	private static void deleteAlias(String gestorDB) throws SQLException, NamingException {
		GenerateSQL gSQL = new GenerateSQL(gestorDB);
		AliasDAO aDAO=new AliasDAO();
		aDAO.open();
		aDAO.deleteCond("UTASK in (SELECT " + gSQL.getCharacterBegin() + "NAME" + gSQL.getCharacterEnd() + " FROM Clases WHERE IDTO in (SELECT IDTO FROM S_Report))");
		aDAO.close();
	}
	private static void deleteProperties() throws SQLException, NamingException {
		PropertiesDAO pDAO=new PropertiesDAO();
		pDAO.open();
		pDAO.deleteCond("PROP<0");
		pDAO.close();
	}
	private static void deleteClase() throws SQLException, NamingException {
		TClaseDAO cDAO= new TClaseDAO();
		cDAO.open();
		cDAO.deleteCond("IDTO<0");
		cDAO.close();
	}
	private static void deleteInstances() throws SQLException, NamingException {
		InstanceDAO iDAO=new InstanceDAO();
		iDAO.open();
		iDAO.deleteCond("IDTO<0");
		iDAO.close();
	}
	private static void deleteSReport() throws SQLException, NamingException {
		SReportDAO qDAO= new SReportDAO();
		qDAO.open();
		qDAO.deleteCond("1=1");
		qDAO.close();
	}
	private static void deleteHerencia() throws SQLException, NamingException {
		T_HerenciasDAO hDAO=new T_HerenciasDAO();
		hDAO.open();
		hDAO.deleteCond("ID_TO<0");
		hDAO.close();
	}
	private static void deleteAccess() throws SQLException, NamingException {
		AccessDAO aDAO=new AccessDAO();
		aDAO.open();
		aDAO.deleteCond("IDTO<0 OR TASK<0");
		aDAO.close();
	}
	private static void deleteGroups() throws SQLException, NamingException {
		GroupsDAO aDAO=new GroupsDAO();
		aDAO.open();
		aDAO.deleteCond("ID<0");
		aDAO.close();
	}

	public static void getDesign(String path,String query) throws SQLException, NamingException, IOException, JDOMException, ParseException, NotFoundException, IncoherenceInMotorException{
		TClaseDAO cDAO=new TClaseDAO();
		cDAO.open();
		TClase queryDB=cDAO.getTClaseByName(query);
		
		cDAO.close();
		
		if (queryDB!=null){
			SReportDAO qDAO=new SReportDAO();
			qDAO.open();
			System.out.println(queryDB.getIDTO());
			SReport sr= qDAO.getSReportByIDTO(String.valueOf(queryDB.getIDTO()));
			qDAO.close();
			System.out.println(sr);
			String queryS=sr.getQuery();
			String queryName=queryDB.getName();
			Element queryXML=jdomParser.readXML(queryS).getRootElement();
			HashMap<String, String> subreports=getSubReportDesign(queryXML,path, queryName);
			String designMaster=sr.getJrxml();
			boolean reemplaceM=true;
			String reportName = path+queryName+".jrxml";
			File f = new File(reportName);
			if (f.exists()){
				String resp=Auxiliar.leeTexto("El diseño " + reportName + " existe en la carpeta "+path+" . �Quiere sustituirlo? S/N");
				if (resp.equalsIgnoreCase("N"))
					reemplaceM=false;
			}
			
			if (reemplaceM){
				System.out.println(reportName);
				FileWriter fw = new FileWriter(f);
				
				fw.write(designMaster);
				fw.close();
			}
			
			SSubReportDAO sDAO=new SSubReportDAO();
			sDAO.open();
			LinkedList<Object> lsub=sDAO.getAllCond("ID_TO LIKE '"+queryDB.getIDTO()+"'");
			sDAO.close();
			Iterator<Object> its=lsub.iterator();
			while(its.hasNext()){
				SSubReport ssr=(SSubReport) its.next();
				String id= ssr.getId();
				String querySub=subreports.get(id);
				boolean reemplaceS=true;
				if (querySub!=null){
					String subReportName = path+querySub+".jrxml";
					File fsub = new File(subReportName);
					if (fsub.exists()){
						String resp=Auxiliar.leeTexto("El diseño " + subReportName + " existe en la carpeta "+path+" . �Quiere sustituirlo? S/N");
						if (resp.equalsIgnoreCase("N"))
							reemplaceS=false;
					}
					
					if (reemplaceS){
						System.out.println(subReportName);
						FileWriter fw = new FileWriter(fsub);
						
						fw.write(ssr.getSubreport());
						fw.close();
					}
				}else{
					System.out.println("Existe una incoherencia en motor y no se encuentra el subrreport con id="+id);
				}
			}
		}else{
			System.out.println("EL REPORT:"+query+" NO SE ENCUENTRA EN LA BASE DE DATOS");
		}
	}
	
	private static  HashMap<String, String> getSubReportDesign(Element queryXML, String path, String queryName) throws ParseException, JDOMException, SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		HashMap<String, String> result=new HashMap<String, String>();
		//QueryReportParser qrp=new QueryReportParser(DAOManager.getFactConnDB(), true);
		//qrp.parserIDs(queryXML);
		ReportService rs=new ReportService(DAOManager.getFactConnDB(),null, false, true);
		String idMaster=getIdReport(queryXML);
		HashMap<String, ParQuery> hq = rs.Report(queryXML, idMaster);
		
		Set<String> ids=hq.keySet();
		Iterator<String> itids=ids.iterator();
		
		while(itids.hasNext()){
			String id=itids.next();
			if (!id.equals(idMaster)){
				ArrayList<Element> elements=jdomParser.elementsWithAt(queryXML, QueryConstants.ID, id.toString(), true);
				Element e=elements.get(0);
				String name= e.getAttributeValue(QueryConstants.NAME);
				if (name==null)
					name=id.toString();
				
				String nameJrxml=queryName+"_"+name;
				result.put(id,nameJrxml);
			}
		}
		return result;
	}
	
	public static void importReports(String nameFileConfiguration, String queryR, String path, 
			FactoryConnectionDB fcdb) throws IOException, JDOMException, ParseException, SQLException, NamingException, NotFoundException, IncoherenceInMotorException{
		boolean reemplace=false;
		
		//que no exista ninguna clase con ese nombre
		boolean existClass=false;
		String resp=null;
		if (queryR!=null) {
			TClaseDAO cDAO=new TClaseDAO();
			cDAO.open();
			TClase queryDB=cDAO.getTClaseByName(queryR);
			cDAO.close();
			if(queryDB!=null) {
				SReportDAO rDAO= new SReportDAO();
				rDAO.open();
				SReport rep=rDAO.getSReportByIDTO(String.valueOf(queryDB.getIDTO()));
				rDAO.close();
				if (rep==null)
					existClass=true;
			}
			if (!existClass)
				resp=Auxiliar.leeTexto("�DESEA SUSTITUIR ESTE REPORT SI ESTA EN LA BASE DE DATOS? S/N") ;
			else
				System.out.println("Ya existe en base de datos una clase de nombre " + queryR + ". Cambie el nombre al fichero de la query");
		} else
			resp=Auxiliar.leeTexto("�DESEA SUSTITUIR LOS REPORTS QUE HAY YA EN LA BASE DE DATOS? S/N") ;
		
		if (resp!=null) {
			if(resp.equalsIgnoreCase("S"))
				reemplace=true;
			
			//REPORTParser parser = new REPORTParser();
			//parser.run(path,reemplace,queryR,fcdb,null,false,1,true);

			//iteramos por un fichero de configuracion
			readConfigXML(nameFileConfiguration, path, fcdb, queryR, reemplace);
		}
	}
	private static void readConfigXML(String nameFileConfiguration, String path, 
			FactoryConnectionDB fcdb, String queryR, boolean reemplace) throws NotFoundException, IncoherenceInMotorException, IOException, JDOMException, ParseException, SQLException, NamingException {
		BufferedReader in = new BufferedReader(new FileReader(path+nameFileConfiguration)); 
		String dataS="", buff="";
		while(buff!= null){
			dataS+=buff;
			buff=in.readLine();
		}
		Document configDOC=jdomParser.readXML(dataS);
		Element reportsXML=configDOC.getRootElement();
		
		REPORTParser parser = new REPORTParser();

		if (queryR!=null)
			System.out.println("Informe concreto " + queryR);
		
		Iterator itr = reportsXML.getChildren().iterator();
		while(itr.hasNext()){
			Element rElem = (Element)itr.next();

			if (rElem.getAttribute("NAME")!=null) {
				String name = rElem.getAttributeValue("NAME");
				System.out.println(name);
				if (queryR==null || queryR.equalsIgnoreCase(name)) {
					System.out.println("encontrado");
					boolean directImpresion = false;
					if (rElem.getAttribute("DIRECT_IMPRESION")!=null){
						if (rElem.getAttributeValue("DIRECT_IMPRESION").equals("TRUE"))
							directImpresion = true;
					}
					
					boolean preView = false;
					if (!directImpresion) {
						if (rElem.getAttribute("PREVIEW")!=null){
							if (rElem.getAttributeValue("PREVIEW").equals("FALSE"))
								preView =true;
						} else
							preView =true;
					} else
						preView =true;

					int nCopies = 1;
					if (rElem.getAttribute("N_COPIES")!=null){
						nCopies = Integer.parseInt(rElem.getAttributeValue("N_COPIES"));
					}
					boolean displayPrintDialog = false;
					if (rElem.getAttribute("DISPLAY_PRINT_DIALOG")!=null){
						if (rElem.getAttributeValue("DISPLAY_PRINT_DIALOG").equals("TRUE"))
							displayPrintDialog = true;
					}
					
					ArrayList<String> formatList = new ArrayList<String>();
					if (rElem.getAttribute("FORMAT")!=null){
						String[] formatSpl = rElem.getAttributeValue("FORMAT").split(";");
						for (int i=0;i<formatSpl.length;i++) {
							String format = formatSpl[i];
							formatList.add(format);
						}
					} else {
						formatList.add(Constants.PDF);
						formatList.add(Constants.RTF);
					}

					String printOrder = rElem.getAttributeValue("PRINT_ORDER");
					ArrayList<String> userRolList=new ArrayList<String>(); 
					if (rElem.getAttribute("USERROL")!=null){
						String[] userRol=rElem.getAttributeValue("USERROL").split(";");
						RolesDAO rolesDAO=new RolesDAO();
						rolesDAO.open();
						for(int i=0;i<userRol.length;i++){
							String userRolName=userRol[i];
							LinkedList<Object> list=rolesDAO.getIDOByName(userRolName);
							if(!list.isEmpty())
								userRolList.add(userRolName);
							else System.err.println("ERROR:UserRol no se encuentra en base de datos!");
						}
						rolesDAO.close();
					}
					
					parser.run(path,reemplace,name,fcdb,null,directImpresion,preView,nCopies,displayPrintDialog,formatList,printOrder,userRolList);
				}
			} else
				System.out.println("Nodo sin name");
		}
	}
	public void run(String path, boolean reemplace, String queryR, FactoryConnectionDB fcdb, String group, 
			boolean directImpresion, boolean preView, int nCopies, boolean displayPrintDialog, 
			ArrayList<String> formatList, String printOrder, ArrayList<String> userRolList) throws IOException, JDOMException, ParseException, SQLException, NamingException, NotFoundException, IncoherenceInMotorException{
//		makeJrxml(business, idtoUtask, nuevo, query)
		HashMap<String,String> queryName=new HashMap<String, String>();
		HashMap<String,String> hqj=new HashMap<String, String>();
		createMapQueryJrxml(path,queryR,queryName,hqj);
//		LinkedList<File> querys=getQuerys(path);
		Set<String> querys=hqj.keySet();
		ArrayList<SReport> listSReport= new ArrayList<SReport>();
		LinkedList<SSubReport> listSubreport=new LinkedList<SSubReport>();
		HashMap<String, LinkedList<Properties>> reportProperties = new HashMap<String, LinkedList<Properties>>();
		HashMap<Integer, String> hPropCls = new HashMap<Integer, String>();

		Iterator<String> itq=querys.iterator();
		while(itq.hasNext()){
			hPropxType=new HashMap<Integer, String>();
			String query=itq.next();
			String jrxml=hqj.get(query);
			String name=queryName.get(query);
			if (reemplace)
				delete(name,false);
			
			LinkedList<Properties> lp=new LinkedList<Properties>();
			reportProperties.put(name, lp);
			
			Document queryDoc=jdomParser.readXML(query);
			Element queryXml=queryDoc.getRootElement();
			//boolean directImpresion = false;
			//String directImpresionStr = queryXml.getAttributeValue(QueryConstants.DIRECT_IMPRESION);
			//if (StringUtils.equals(directImpresionStr,"TRUE"))
			//directImpresion = true;
			SReport sr=createSReport(query,queryXml,jrxml,name,reportProperties,hPropCls,group,directImpresion,preView,nCopies,displayPrintDialog,formatList,printOrder);
			listSReport.add(sr);
			String nameR = name;
			HashMap<String,File> lsr=findSubReports(queryXml,sr,path,nameR,fcdb);
			
			LinkedList<SSubReport> lssr=createSubReport(sr,lsr);
			listSubreport.addAll(lssr);
			
			TClaseDAO cDAO=new TClaseDAO();
			cDAO.open();
			TClase utask=cDAO.getTClaseByName(name);
			if(utask==null){
				utask=new TClase();
				utask.setIDTO(sr.getIdto());
				utask.setName(name);
				cDAO.insert(utask);
			}
			T_HerenciasDAO hDAO=new T_HerenciasDAO();
			T_Herencias herencia=new T_Herencias();
			herencia.setID_TO_Padre(Constants.IDTO_UTASK);
			herencia.setID_TO(utask.getIDTO());
			hDAO.insert(herencia);
			herencia.setID_TO_Padre(Constants.IDTO_REPORT);
			herencia.setID_TO(utask.getIDTO());
			hDAO.insert(herencia);
			cDAO.close();
			LinkedList<Instance> litosave=createInstances(name,sr,reportProperties,hPropCls,directImpresion,preView,formatList,userRolList);
			saveInstance(litosave);
			
			List listDescription=queryXml.getChildren("DESCRIPTION");
			if(listDescription!=null){
				Iterator<Element> itr=listDescription.iterator();
				while(itr.hasNext()){
					Element descriptionXml=itr.next();
					String language=descriptionXml.getAttributeValue("LANGUAGE").toString();
					String description=jdomParser.returnXML(descriptionXml.getContent(),false);
					Statement st = null;
					
					ConnectionDB con = null;
					try {
						con = fcdb.createConnection(true);
						st = con.getBusinessConn().createStatement();
						
						String sql = "INSERT INTO HelpClasses (IDTO, DESCRIPTION, LANGUAGE) VALUES ("+sr.getIdto()+",'"+description+"','"+language+"')";
						System.out.println(sql);
						st.executeUpdate(sql);
						
					}finally{			
						if (st!=null)
							st.close();
					}
				}
			}else{
				System.err.println("WARNING: El report "+name+" no tiene el nodo DESCRIPTION pero sera importado");
			}
		}
		setInDB(listSReport, listSubreport);
	}

	private HashMap<String,File> findSubReports(Element queryXml, SReport sr, String path, String nameR, FactoryConnectionDB fcdb) throws ParseException, JDOMException, SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		HashMap<String, File> result=new HashMap<String, File>();
		//QueryReportParser qrp=new QueryReportParser(fcdb, true);
		//Element qparser=(Element) queryXml.clone();
		//qrp.parserIDs(queryXml);
		ReportService rs=new ReportService(fcdb,null, false, true);

		String idMaster=getIdReport(queryXml);
		HashMap<String, ParQuery> hq = rs.Report(queryXml, idMaster);

		Set<String> ids=hq.keySet();
		Iterator<String> itids=ids.iterator();
		
		while(itids.hasNext()){
			String id=itids.next();
			if (!id.equals(idMaster)){
				ArrayList<Element> elements=jdomParser.elementsWithAt(queryXml, QueryConstants.ID, id.toString(), true);

				Element e=elements.get(0);
				String name= e.getAttributeValue(QueryConstants.NAME);
				if (name==null)
					name=id.toString();
				
				String nameJrxml=nameR+"_"+name;
				
				File dir= new File(path);
				FilenameFilter filter=new FilterName(nameJrxml);
				File[] files= dir.listFiles(filter);
				for(int i=0; i<files.length; i++){
					result.put(id.toString(),files[i]);
				}
			}
		}
		return result;
	}

	private void saveInstance(LinkedList<Instance> litosave) throws SQLException, NamingException {
		InstanceDAO iDAO = new InstanceDAO();
		iDAO.setCommit(false);
		iDAO.open();
		Iterator<Instance> iti=litosave.iterator();
		while(iti.hasNext()){
			Instance i= iti.next();
			iDAO.insert(i);
		}
		iDAO.commit();
		iDAO.close();
	}

	private LinkedList<Instance> createInstances(String name, SReport sr, HashMap<String, LinkedList<Properties>> reportProperties, 
			HashMap<Integer, String> hPropCls, boolean directImpresion, boolean preView, ArrayList<String> formatList, ArrayList<String> userRolList) throws JDOMException, SQLException, NamingException {
		LinkedList<Properties> lp= reportProperties.get(name);
		Iterator<Properties> itp=lp.iterator();
		InstanceDAO iDAO = new InstanceDAO();
		LinkedList<Instance> litosave=new LinkedList<Instance>();
		iDAO.open();
		TClaseDAO cDAO=new TClaseDAO();
		TClase params=cDAO.getTClaseByName("Params_"+name);
		Integer idtoParams=null;
		if(params!=null)
			idtoParams=params.getIDTO();
		else idtoParams=cDAO.getNewPKVirtual();
	
		ArrayList<TClase> aIdtoTarget=searchTargetClassQuery(sr.getQuery());
		Iterator itIdtoTarget = aIdtoTarget.iterator();
		ArrayList<String> aOneOf = new ArrayList<String>();
		while (itIdtoTarget.hasNext()) {
			Integer idtoTarget = ((TClase)itIdtoTarget.next()).getIDTO();
			LinkedList<Object> linstance=iDAO.getAllCond("IDTO LIKE '"+String.valueOf(Constants.IDTO_UTASK)+"' OR (IDTO LIKE '"+String.valueOf(Constants.IDTO_REPORT)+"' AND PROPERTY<>" + Constants.IdPROP_RDN + ")");
			Iterator itinstance=linstance.iterator();
			while(itinstance.hasNext()){
				Instance i=(Instance) itinstance.next();
				//if(i.getIDTO().equals(String.valueOf(Constants.IDTO_UTASK)) || (i.getIDTO().equals(String.valueOf(Constants.IDTO_REPORT)) && !i.getPROPERTY().equals(String.valueOf(Constants.IdPROP_RDN)))){
					i.setIDTO(sr.getIdto().toString());
					i.setNAME(name);
					
					if(i.getPROPERTY().equals(String.valueOf((Constants.IdPROP_TARGETCLASS)))){
						if(i.getOP()==null){
							i.setOP(Constants.OP_INTERSECTION);
							i.setVALUECLS(idtoTarget.toString());
						}
					}
					
					if(!i.getOP().equals(Constants.OP_CARDINALITY)){
						if(i.getPROPERTY().equals(String.valueOf((Constants.IdPROP_MYFUNCTIONALAREA)))){
							LinkedList<Object> li=iDAO.getAllCond("VALUECLS LIKE '"+idtoTarget.toString()+"'");
							LinkedList<Integer> lfuncarea=getFuncArea(li);
							Iterator<Integer> itfuncarea=lfuncarea.iterator();
							boolean first=true;
							while(itfuncarea.hasNext()){
								Integer fa=itfuncarea.next();
								System.out.println("AREA FUNCIONAL=>"+fa);
								if (!aOneOf.contains(fa.toString())) {
									aOneOf.add(fa.toString());
									if(first){
										i.setVALUECLS(String.valueOf(Constants.IDTO_FUNCTIONAL_AREA));
										i.setOP(Constants.OP_ONEOF);
										i.setVALUE(fa.toString());
										first=false;
									}else{
										Instance inew= i.clone();
										i.setVALUE(fa.toString());
										litosave.add(inew);
									}
								}
							}
						} else if(i.getPROPERTY().equals(String.valueOf((Constants.IdPROP_DIRECTIMPRESION)))){
							i.setOP(Constants.OP_DEFAULTVALUE);
							String directImpresionStr=directImpresion?"1":"0";
							i.setQMIN(directImpresionStr);
							i.setQMAX(directImpresionStr);
						} else if(i.getPROPERTY().equals(String.valueOf((Constants.IdPROP_REPORT_PREVIEW)))){
							i.setOP(Constants.OP_DEFAULTVALUE);
							String preViewStr=preView?"1":"0";
							i.setQMIN(preViewStr);
							i.setQMAX(preViewStr);
						} else if(i.getPROPERTY().equals(String.valueOf((Constants.IdPROP_REPORT_FORMAT)))){
							
							String defaultFormat = null;
							if (formatList.contains(Constants.PDF))
								defaultFormat = Constants.PDF;
							else if (formatList.contains(Constants.EXCEL))
								defaultFormat = Constants.EXCEL;
							
							i.setOP(Constants.OP_ONEOF);
							i.setVALUECLS(String.valueOf(Constants.IDTO_REPORT_FORMAT));
							Iterator it = formatList.iterator();
							boolean first=true;
							while (it.hasNext()) {
								String format = (String)it.next();
								//obtener ido a partir de rdn format
								O_Datos_AttribDAO oDAO = new O_Datos_AttribDAO();
								System.out.println("FORMAT=>"+format);
								Integer ido = ((O_Datos_Attrib)oDAO.getAllCond("ID_TO=" + Constants.IDTO_REPORT_FORMAT + " AND PROPERTY=" + Constants.IdPROP_RDN + " AND VAL_TEXTO='" + format + "'").getFirst()).getIDO();
								
								if (format.equals(defaultFormat)) {
									Instance inew = i.clone();
									inew.setVALUE(String.valueOf(ido));
									inew.setOP(Constants.OP_DEFAULTVALUE);
									litosave.add(inew);
								}
								if (first) {
									i.setVALUE(String.valueOf(ido));
									first=false;
								}else{
									Instance inew = i.clone();
									i.setVALUE(String.valueOf(ido));
									litosave.add(inew);
								}
							}
						}
					}
					litosave.add(i);
					//Lo hacemos despues de insertarlo ya que el userRol que llega es el que tiene op AND, que hay que insertarlo de todas maneras
					if(i.getPROPERTY().equals(String.valueOf((Constants.IdPROP_USERROL)))){
						Iterator<String> itrUserRol=userRolList.iterator();
						RolesDAO rolesDAO=new RolesDAO();
						while(itrUserRol.hasNext()){
							String userRolName=itrUserRol.next();
							Instance inst=i.clone();
							inst.setOP(Constants.OP_ONEOF);
							LinkedList<Object> list=rolesDAO.getIDOByName(userRolName);
							inst.setVALUE(list.get(0).toString());
							litosave.add(inst);
						}
					}
				//}
			}
			if (!lp.isEmpty()){
				Instance i= new Instance();
				i.setIDTO(sr.getIdto().toString());
				i.setPROPERTY(String.valueOf(Constants.IdPROP_PARAMS));
				i.setNAME(name);
				
				i.setVALUECLS(idtoParams.toString());
				i.setOP(Constants.OP_INTERSECTION);
				
				litosave.add(i);
			}
			if (!lp.isEmpty()){
				if(params==null){
					params=new TClase();
					params.setIDTO(idtoParams);
					params.setName("Params_"+name);
					cDAO.insert(params);
				}
				T_HerenciasDAO hDAO=new T_HerenciasDAO();
				T_Herencias herencia=new T_Herencias();
				herencia.setID_TO_Padre(Constants.IDTO_PARAMS);
				herencia.setID_TO(idtoParams);
				hDAO.insert(herencia);
				AliasDAO aDAO= new AliasDAO();
				Alias a = new Alias();
				a.setUTaskName(name);
				a.setIdtoName(params.getName());
				a.setAlias("Parametros");
				aDAO.insert(a);
				while(itp.hasNext()){
					Properties p=itp.next();
					
					if (p.getVALUECLS()!=null) {
						Instance i= new Instance();
						i.setIDTO(idtoParams.toString());
						i.setNAME("Params_"+name);
						i.setPROPERTY(p.getPROP().toString());
						i.setOP(Constants.OP_INTERSECTION);
						i.setVALUECLS(p.getVALUECLS().toString());
						litosave.add(i);
					} else {
						String valueCls = hPropCls.get(p.getPROP());
						String[] str = valueCls.split(",");
						if (str.length==1) {
							Instance i= new Instance();
							i.setIDTO(idtoParams.toString());
							i.setNAME("Params_"+name);
							i.setPROPERTY(p.getPROP().toString());
							i.setOP(Constants.OP_INTERSECTION);
							i.setVALUECLS(valueCls);
							litosave.add(i);
						} else {
							for (int i=0;i<str.length;i++) {
								Instance ins = new Instance();
								ins.setIDTO(idtoParams.toString());
								ins.setNAME("Params_"+name);
								ins.setPROPERTY(p.getPROP().toString());
								ins.setOP(Constants.OP_UNION);
								ins.setVALUECLS(str[i]);
								litosave.add(ins);
							}
						}
					}
					
					/*Instance i= new Instance();
					i.setIDTO(idtoParams.toString());
				
					i.setNAME("Params_"+name);
					i.setPROPERTY(p.getPROP().toString());
	
					if (p.getVALUECLS()!=null) {
						i.setOP(Constants.OP_INTERSECTION);
						i.setVALUECLS(p.getVALUECLS().toString());
					} else {
						String valueCls = hPropCls.get(p.getPROP());
						
						String[] str = valueCls.split(",");
						if (str.length==1) {
							i.setOP(Constants.OP_INTERSECTION);
							i.setVALUECLS(valueCls);
						} else {
							i.setOP(Constants.OP_UNION);
							i.setVALUECLS(str[i]);
						}
					}*/
					if(p.getCAT().equals(Category.iObjectProperty) || p.getQMIN()!=null && p.getQMIN().equals(new Float(1))){
						Instance icard= new Instance();
						icard.setIDTO(idtoParams.toString());
						
						icard.setNAME("Params_"+name);
						icard.setPROPERTY(p.getPROP().toString());
						icard.setOP(Constants.OP_CARDINALITY);
						if (p.getQMIN()!=null && p.getQMIN().equals(new Float(1))) //Cardinalidad minima a 1 solo si es requerido
							icard.setQMIN("1");
						if (p.getQMAX()!=null && p.getQMAX().equals(new Float(1))) //Cardinalidad maxima != 1 solo si es cardinalidad multiple
							icard.setQMAX("1");
						litosave.add(icard);
					}
					if(p.getCAT().equals(Category.iObjectProperty)){
						
						LinkedList<Object> listh=hDAO.getAllCond("ID_TO IN( "+hPropCls.get(p.getPROP()) + ")");
						if (listh.isEmpty() && !hPropCls.get(p.getPROP()).equals(Constants.IDTO_ENUMERATED)){
							Access acc= new Access();
							acc.setDENNIED(1);
							acc.setPROP(p.getPROP());
							acc.setIDTO(idtoParams);
							acc.setACCESSTYPE(action.SET);
							AccessDAO accDAO= new AccessDAO();
							accDAO.insert(acc);
						}else{
							boolean enumerado=false;
							Iterator<Object> ith= listh.iterator();
							while(ith.hasNext() && !enumerado){
								T_Herencias heren= (T_Herencias) ith.next();
								if(heren.getID_TO_Padre()==Constants.IDTO_ENUMERATED)
									enumerado=true;
							}
							if(!enumerado){
								Access acc= new Access();
								acc.setDENNIED(1);
								acc.setPROP(p.getPROP());
								acc.setIDTO(idtoParams);
								acc.setACCESSTYPE(action.SET);
								AccessDAO accDAO= new AccessDAO();
								accDAO.insert(acc);
							}
						}
					}
					if (hPropxType.get(p.getPROP())!=null && hPropxType.get(p.getPROP()).equals("FILTER")){
						GroupsDAO gDAO=new GroupsDAO();
						gDAO.open();
						LinkedList<Object> lvo=gDAO.getAllCond(" NAME LIKE 'CONDICIONES_DE_BUSQUEDA'");
						Groups groupProp=new Groups();
						if(lvo.isEmpty())
							groupProp.setIdGroup(gDAO.getNewPKVirtual());
						else{
							Groups group=(Groups) lvo.getFirst();
							groupProp.setIdGroup(group.getIdGroup());
						}
						groupProp.setUTask(sr.getIdto());
						groupProp.setIdtoClass(idtoParams);
						groupProp.setNameGroup("CONDICIONES_DE_BUSQUEDA");
						groupProp.setIdProp(p.getPROP());
						gDAO.insert(groupProp);
						gDAO.close();
					}
				}
			}
		}
		iDAO.close();
		return litosave;
	}

	private LinkedList<Integer> getFuncArea(LinkedList<Object> li) throws SQLException {
		Iterator<Object> it=li.iterator();
		LinkedList<Integer> lfa= new LinkedList<Integer>();
		LinkedList<Integer> lut= new LinkedList<Integer>();
		while(it.hasNext()){
			Instance i= (Instance) it.next();
			if (i.getPROPERTY().equals(String.valueOf(Constants.IdPROP_TARGETCLASS))){
				lut.add(Integer.parseInt(i.getIDTO()));				
			}
		}
		Iterator<Integer> itut= lut.iterator();
		while(itut.hasNext()){
			InstanceDAO iDAO = new InstanceDAO();
			LinkedList<Object>l=iDAO.getAllCond("IDTO LIKE '"+itut.next().toString()+"' AND OP NOT LIKE 'CAR'");
			Iterator<Object> itl=l.iterator();
			while(itl.hasNext()){
				Instance iu=(Instance) itl.next();
				if(iu.getPROPERTY().equals(String.valueOf(Constants.IdPROP_MYFUNCTIONALAREA))){
					T_HerenciasDAO tDAO = new T_HerenciasDAO();
					LinkedList<Object>ll=tDAO.getAllCond("ID_TO=" + iu.getIDTO() + " and (ID_TO_Padre=" + Constants.IDTO_ACTION + 
							" or ID_TO_Padre=" + Constants.IDTO_REPORT + ")");
					System.out.println("INSTANCE " + iu);
					if (ll.size()==0)
						lfa.add(Integer.parseInt(iu.getVALUE()));
				}
			}
		}
		return lfa;
	}

	private ArrayList<TClase> searchTargetClassQuery(String query) throws JDOMException, SQLException {
		Document queryDoc=jdomParser.readXML(query);
		Element queryXml=queryDoc.getRootElement();
		
		String classesName = queryXml.getAttributeValue(QueryConstants.TARGET_CLASS);
		String classesNameParser = "";
		String[] devSpl = classesName.split(",");
		for (int i=0;i<devSpl.length;i++) {
			if (classesNameParser.length()>0)
				classesNameParser += ",";
			classesNameParser += "'" + devSpl[i] + "'";
		}

		TClaseDAO cDAO = new TClaseDAO();
		LinkedList<Object> lc=cDAO.getAllCond("NAME IN(" + classesNameParser + ")");
		Iterator it = lc.iterator();
		return Auxiliar.IteratorToArrayList(it);
	}

	private static void setInDB(ArrayList<SReport> listSReport, LinkedList<SSubReport> listSubreport) throws SQLException, NamingException {
		if (listSReport.isEmpty()){
			System.out.println("NO HAY REPORT A INSERTAR");
		}else{
			SReportDAO qDAO=new SReportDAO();
			qDAO.setCommit(false);
			qDAO.open();
			Iterator<SReport> its=listSReport.iterator();
			while(its.hasNext()){
				SReport sr= its.next();
				
				qDAO.insert(sr);
			}
			qDAO.commit();
			qDAO.close();
			SSubReportDAO sDAO=new SSubReportDAO();
			sDAO.setCommit(false);
			Iterator<SSubReport> itss=listSubreport.iterator();
			while(itss.hasNext()){
				SSubReport ssr= itss.next();
				sDAO.insert(ssr);
			}
			sDAO.commit();
			sDAO.close();
		}
	}

	public static String getIdReport(Element query) {
		String idMaster = "";
		Element structure = query.getChild(QueryConstants.STRUCTURE);
		Element presentation = structure.getChild(QueryConstants.PRESENTATION);
		Element view = presentation.getChild(QueryConstants.VIEW);
		String index = view.getAttributeValue(QueryConstants.ID);
		if (index!=null)
			idMaster = index;
		else
			idMaster = structure.getAttributeValue(QueryConstants.ID);
		return idMaster;
	}
	private SReport createSReport(String query, Element queryXml, String jrxml, String nameReport, HashMap<String, LinkedList<Properties>> reportProperties, 
			HashMap<Integer, String> hPropCls, String group, boolean directImpresion, boolean preView, int nCopies, boolean displayPrintDialog, ArrayList<String> formatList, String printOrder) 
			throws JDOMException, ParseException, SQLException, NamingException {
		Integer idto=null;
		String id=null;
		TClaseDAO cDAO=new TClaseDAO();
		cDAO.open();
		TClase classReport=cDAO.getTClaseByName(nameReport);
		if(classReport!=null)
			idto=classReport.getIDTO();
		else idto=cDAO.getNewPKVirtual();
		cDAO.close();
		
		Element mapXml=createMap(queryXml,nameReport,reportProperties,hPropCls);
		String mapString=jdomParser.returnXML(mapXml);

		id=getIdReport(queryXml);
		if (group==null){
			if (queryXml.getAttribute(QueryConstants.GROUP)!=null){
				group=queryXml.getAttribute(QueryConstants.GROUP).toString();
			}
		}
		SReport sr=new SReport(idto,id,query,jrxml,mapString,group,directImpresion,preView,nCopies,displayPrintDialog,formatList,printOrder);
		return sr;
	}

	private Element createMap(Element queryXml, String nameReport, HashMap<String, LinkedList<Properties>> reportProperties, 
			HashMap<Integer, String> hPropCls) throws ParseException, JDOMException, SQLException, NamingException {
		QueryReportParser qrp=new QueryReportParser(DAOManager.getFactConnDB(), true, false, true);
		qrp.parserIDs(queryXml);
		
		PropertiesDAO pDAO= new PropertiesDAO();
		AliasDAO aDAO= new AliasDAO();
//		QueryReportParser qrp=new QueryReportParser(DAOManager.getFactConnDB());
		
		//pDAO.open();
		Element map= new Element("MAPS_VALUE");
		
		ArrayList<Element> lclass=jdomParser.findElementsByAt(queryXml, QueryConstants.CLASS, QueryConstants.TABLE_ID, "(VALUE)", true);
		ArrayList<Element> lwhere=jdomParser.findElementsByContainsAt(queryXml, QueryConstants.WHERE, QueryConstants.VAL_MIN, "(VALUE)", true);
		ArrayList<Element> lattr=jdomParser.findElementsByContainsText(queryXml, QueryConstants.ATTRIBUTE, "(VALUE)", true);
		lwhere.addAll(getWhereWithContainsValue(queryXml));
		//Descomentar las lineas para que funcione los where de presentation
//		Iterator<Element> itwherecambios=lwhere.iterator();
//		while(itwherecambios.hasNext()){
//			Element where=itwherecambios.next();
//			where.setAttribute("MACA", where.getAttributeValue(QueryConstants.ID));
//		}
//		itwherecambios=lwherecontains.iterator();
//		while(itwherecambios.hasNext()){
//			Element where=itwherecambios.next();
//			where.setAttribute("MACA", where.getAttributeValue(QueryConstants.ID));
//		}
		//Element qparser=(Element) queryXml.clone();
//		qrp.parserIDs(queryXml);
		System.out.println(jdomParser.returnXML(queryXml));
//		Element presentation=jdomParser.element(queryXml, QueryConstants.PRESENTATION, true);
//		ArrayList<Element> lwherecontainsp=getWhereWithContainsValue(presentation);
//		ArrayList<Element> lwherepresentation=jdomParser.findElementsByAt(presentation, QueryConstants.WHERE, QueryConstants.VAL_MIN, "(VALUE)", true);
//		ArrayList<Element> lhavingcontainsp=getHavingWithContainsValue(presentation);
//		ArrayList<Element> lhavingpresentation=jdomParser.findElementsByAt(presentation, QueryConstants.HAVING, QueryConstants.VAL_MIN, "(VALUE)", true);
		LinkedList<String> idParamsList=new LinkedList<String>();
		if(!lclass.isEmpty()){

			Iterator<Element> itclass=lclass.iterator();
			while(itclass.hasNext()){
				Element prop=new Element("PROP");
				Element classq=itclass.next();
				String id=classq.getAttributeValue(QueryConstants.ID);
				String name=classq.getAttributeValue(QueryConstants.ALIAS);
				String idParams=classq.getAttributeValue(QueryConstants.ID_PARAM);
				String propName=id+"@"+nameReport;
				pDAO.open();
				Integer idProp=pDAO.getIdPropByName(propName);
				boolean insertProp=false;
				if(idProp==null){
					idProp=pDAO.getNewPKVirtual();
					insertProp=true;
				}
				pDAO.close();
				prop.setAttribute("PROP", idProp.toString());
				prop.setAttribute("ID_QUERY",id);
				Properties p=new Properties();
				p.setPROP(idProp);
				p.setNAME(propName);
				p.setCAT(Category.iObjectProperty);
				if (StringUtils.equals(classq.getAttributeValue(QueryConstants.SHOW_OBLIGATORY),"TRUE"))
					p.setQMIN(new Float(1));
				if (!StringUtils.equals(classq.getAttributeValue(QueryConstants.MULTIPLE_CARD),"TRUE"))
					p.setQMAX(new Float(1));
				String idtoString=classq.getAttributeValue(QueryConstants.ID_TO);
//				TClaseDAO cDAO= new TClaseDAO();
//				LinkedList<Object> lc=cDAO.getByName(className);
				//Integer idtoclass= Integer.parseInt(idtoString);
				hPropCls.put(idProp, idtoString);
			//	p.setVALUECLS(idtoclass);
				
				if (idParams!=null){
					if(!idParamsList.contains(idParams)){
						idParamsList.add(idParams);
						if (reportProperties!=null)
							reportProperties.get(nameReport).add(p);
						
						Alias a = new Alias();
						a.setUTaskName(nameReport);
						a.setPropName(p.getNAME());
						a.setAlias(name);
						aDAO.open();
						aDAO.insert(a);
						aDAO.close();
						hPropxType.put(p.getPROP(), "FILTER");
						if(insertProp){
							pDAO.open();
							pDAO.insert(p);
							pDAO.close();
						}
						map.addContent(prop);
					}
				}else{
					if (reportProperties!=null)
						reportProperties.get(nameReport).add(p);
					Alias a = new Alias();
					a.setUTaskName(nameReport);
					a.setPropName(p.getNAME());
					a.setAlias(name);
					aDAO.open();
					aDAO.insert(a);
					aDAO.close();
					hPropxType.put(p.getPROP(), "FILTER");
					if(insertProp){
						pDAO.open();
						pDAO.insert(p);
						pDAO.close();
					}
					map.addContent(prop);
				}
			}
		}
		if(!lwhere.isEmpty()){
			Iterator<Element> itwhere=lwhere.iterator();
			while(itwhere.hasNext()){
				Element prop=new Element("PROP");
				Element where=itwhere.next();
				String idWhere=where.getAttributeValue(QueryConstants.ID);
				String nameWhere=where.getAttributeValue(QueryConstants.ALIAS);
				String idParams=where.getAttributeValue(QueryConstants.ID_PARAM);
				String propName=idWhere+"@"+nameReport;
//				String propWhere=where.getAttributeValue(QueryConstants.PROP);
//
//				Integer idpropwhere = pDAO.getIdPropByName(propWhere);
//				
				//Element whereP=jdomParser.findElementByAt(qparser, QueryConstants.WHERE, "MACA", idWhere , true, true);
//				Element parent=where.getParent();
//				Element aux=jdomParser.findElementByAt(parent, QueryConstants.ATTRIBUTE, QueryConstants.PROP, propWhere, true, true);
//				String id=aux.getAttributeValue(QueryConstants.ID);
				pDAO.open();
				Integer idProp=pDAO.getIdPropByName(propName);
				boolean insertProp=false;
				if(idProp==null){
					idProp=pDAO.getNewPKVirtual();
					insertProp=true;
				}
				pDAO.close();
				prop.setAttribute("PROP", idProp.toString());
				prop.setAttribute("ID_QUERY",idWhere);
				Properties p=new Properties();
				p.setPROP(idProp);
//				Element attributeNP=jdomParser.findElementByAt(where.getParent(), QueryConstants.ATTRIBUTE, QueryConstants.PROP, propWhere, true, true);
//				String idpropbd=attributeNP.getAttributeValue(QueryConstants.ID);
				p.setNAME(propName);
				if (StringUtils.equals(where.getAttributeValue(QueryConstants.SHOW_OBLIGATORY),"TRUE"))
					p.setQMIN(new Float(1));
				if (!StringUtils.equals(where.getAttributeValue(QueryConstants.MULTIPLE_CARD),"TRUE"))
					p.setQMAX(new Float(1));

				String idTmRuleengine = where.getAttributeValue(QueryConstants.ID_TM_RULEENGINE);
				Integer type=Integer.parseInt(idTmRuleengine);
					
				System.out.println(jdomParser.returnNodeXML(where));
					
				p.setCAT(Category.iDataProperty);
				p.setVALUECLS(type);

				if (idParams!=null){
					if(!idParamsList.contains(idParams)){
						idParamsList.add(idParams);
						if (reportProperties!=null)
							reportProperties.get(nameReport).add(p);
						
						Alias a = new Alias();
						a.setUTaskName(nameReport);
						a.setPropName(p.getNAME());
						a.setAlias(nameWhere);
						aDAO.open();
						aDAO.insert(a);
						aDAO.close();
						hPropxType.put(p.getPROP(), "FILTER");
						if(insertProp){
							pDAO.open();
							pDAO.insert(p);
							pDAO.close();
						}
						map.addContent(prop);
					}
				}else{
					if (reportProperties!=null)
						reportProperties.get(nameReport).add(p);
					Alias a = new Alias();
					a.setUTaskName(nameReport);
					a.setPropName(p.getNAME());
					a.setAlias(nameWhere);
					aDAO.open();
					aDAO.insert(a);
					aDAO.close();
					hPropxType.put(p.getPROP(), "FILTER");
					if(insertProp){
						pDAO.open();
						pDAO.insert(p);
						pDAO.close();
					}
					map.addContent(prop);
					
				}
			}
		}
		if(!lattr.isEmpty()){
			Iterator<Element> itatt=lattr.iterator();
			while(itatt.hasNext()){
				Element prop=new Element("PROP");
				Element attr=itatt.next();
				String idAttr=attr.getAttributeValue(QueryConstants.ID);
				String nameAttr=attr.getAttributeValue(QueryConstants.ALIAS);
				String idParams=attr.getAttributeValue(QueryConstants.ID_PARAM);
				boolean isFilter = attr.getAttributeValue(QueryConstants.PROP)!=null;
				String propName=idAttr+"@"+nameReport;
//
//				Integer idpropwhere = pDAO.getIdPropByName(propWhere);
//				
				//Element whereP=jdomParser.findElementByAt(qparser, QueryConstants.WHERE, "MACA", idWhere , true, true);
//				Element parent=where.getParent();
//				Element aux=jdomParser.findElementByAt(parent, QueryConstants.ATTRIBUTE, QueryConstants.PROP, propWhere, true, true);
//				String id=aux.getAttributeValue(QueryConstants.ID);
				pDAO.open();
				Integer idProp=pDAO.getIdPropByName(propName);
				boolean insertProp=false;
				if(idProp==null){
					idProp=pDAO.getNewPKVirtual();
					insertProp=true;
				}
				pDAO.close();
				prop.setAttribute("PROP", idProp.toString());
				prop.setAttribute("ID_QUERY",idAttr);
				Properties p=new Properties();
				p.setPROP(idProp);
//				Element attributeNP=jdomParser.findElementByAt(where.getParent(), QueryConstants.ATTRIBUTE, QueryConstants.PROP, propWhere, true, true);
//				String idpropbd=attributeNP.getAttributeValue(QueryConstants.ID);
				p.setNAME(propName);

				Integer type=Constants.IDTO_STRING;
				if (StringUtils.equals(attr.getAttributeValue(QueryConstants.TYPE),QueryConstants.INT))
					type=Constants.IDTO_INT;
				else if (StringUtils.equals(attr.getAttributeValue(QueryConstants.TYPE),QueryConstants.DOUBLE))
					type=Constants.IDTO_DOUBLE;
				else if (StringUtils.equals(attr.getAttributeValue(QueryConstants.TYPE),QueryConstants.DATE))
					type=Constants.IDTO_DATE;
				else if (StringUtils.equals(attr.getAttributeValue(QueryConstants.TYPE),QueryConstants.MEMO))
					type=Constants.IDTO_MEMO;
				
				p.setCAT(Category.iDataProperty);
				p.setVALUECLS(type);
				if (StringUtils.equals(attr.getAttributeValue(QueryConstants.SHOW_OBLIGATORY),"TRUE"))
					p.setQMIN(new Float(1));
				if (!StringUtils.equals(attr.getAttributeValue(QueryConstants.MULTIPLE_CARD),"TRUE"))
					p.setQMAX(new Float(1));

				if (idParams!=null){
					if(!idParamsList.contains(idParams)){
						idParamsList.add(idParams);
						if (reportProperties!=null)
							reportProperties.get(nameReport).add(p);
						
						Alias a = new Alias();
						a.setUTaskName(nameReport);
						a.setPropName(p.getNAME());
						a.setAlias(nameAttr);
						aDAO.open();
						aDAO.insert(a);
						aDAO.close();
						if (isFilter)
							hPropxType.put(p.getPROP(), "FILTER");
						if(insertProp){
							pDAO.open();
							pDAO.insert(p);
							pDAO.close();
						}
						map.addContent(prop);
					}
				}else{
					if (reportProperties!=null)
						reportProperties.get(nameReport).add(p);
					Alias a = new Alias();
					a.setUTaskName(nameReport);
					a.setPropName(p.getNAME());
					a.setAlias(nameAttr);
					aDAO.open();
					aDAO.insert(a);
					aDAO.close();
					if (isFilter)
						hPropxType.put(p.getPROP(), "FILTER");
					if(insertProp){
						pDAO.open();
						pDAO.insert(p);
						pDAO.close();
					}
					map.addContent(prop);
				}
				
//				reportProperties.get(nameReport).add(p);
//				hPropxType.put(p.getPROP(), "ATTR");
//				
//				Alias a = new Alias();
//				a.setUTaskName(nameReport);
//				a.setPropName(p.getNAME());
//				a.setAlias(nameAttr);
//				aDAO.open();
//				aDAO.insert(a);
//				aDAO.close();
//				
//				pDAO.open();
//				pDAO.insert(p);
//				pDAO.close();
//				map.addContent(prop);
			}
		}
//		if(!lwherepresentation.isEmpty()){
//			Iterator<Element> itwhere=lwherepresentation.iterator();
//			while(itwhere.hasNext()){
//				Element prop=new Element("PROP");
//				Element where=itwhere.next();
//				String id_leftWhere=where.getAttributeValue(QueryConstants.ID_LEFT);
//				Element attributeNP=jdomParser.findElementByAt(queryXml, QueryConstants.ATTRIBUTE, QueryConstants.ID, id_leftWhere , true, true);
//				String propWhere=attributeNP.getAttributeValue(QueryConstants.PROP);
////				Element attributeNP=jdomParser.findElementByAt(where.getParent(), QueryConstants.ATTRIBUTE, QueryConstants.PROP, propWhere, true, true);
////				String idpropwhere=attributeNP.getAttributeValue(QueryConstants.PROP);
//				Integer idpropwhere = pDAO.getIdPropByName(propWhere);
//				
//				Element attribute=jdomParser.findElementByAt(qparser, QueryConstants.ATTRIBUTE, QueryConstants.PROP, idpropwhere.toString() , true, true);
//				Element parent=where.getParent();
//				Element aux=jdomParser.findElementByAt(parent, QueryConstants.ATTRIBUTE, QueryConstants.PROP, propWhere, true, true);
//				String id=aux.getAttributeValue(QueryConstants.ID);
//				Integer idProp=pDAO.getNewPKVirtual();
//				prop.setAttribute("PROP", idProp.toString());
//				prop.setAttribute("ID_QUERY",id);
//				Properties p=new Properties();
//				p.setPROP(idProp);
//				
//				String idpropbd=id_leftWhere;
//				p.setNAME(idpropbd);
//				Integer type=Integer.parseInt(attribute.getAttributeValue(QueryConstants.ID_TM_RULEENGINE));
//				p.setCAT(Category.iDataProperty);
//				p.setVALUECLS(type);
//				this.reportProperties.get(nameReport).add(p);
//				
//				AliasDAO aDAO= new AliasDAO();
//				Alias a = new Alias();
//				a.setUTaskName(nameReport);
//				a.setPropName(p.getNAME());
//				a.setAlias(idpropbd);
//				aDAO.insert(a);
//				
//				pDAO.insert(p);
//				map.addContent(prop);
//			}
//		}
//		if(!lwherecontainsp.isEmpty()){
//			Iterator<Element> itwhere=lwherecontainsp.iterator();
//			while(itwhere.hasNext()){
//				Element prop=new Element("PROP");
//				Element where=itwhere.next();
//				String id_leftWhere=where.getAttributeValue(QueryConstants.ID_LEFT);
//				Element attributeNP=jdomParser.findElementByAt(queryXml, QueryConstants.ATTRIBUTE, QueryConstants.ID, id_leftWhere , true, true);
//				String propWhere=attributeNP.getAttributeValue(QueryConstants.PROP);
////				Element attributeNP=jdomParser.findElementByAt(where.getParent(), QueryConstants.ATTRIBUTE, QueryConstants.PROP, propWhere, true, true);
////				String idpropwhere=attributeNP.getAttributeValue(QueryConstants.PROP);
//				Integer idpropwhere = pDAO.getIdPropByName(propWhere);
//				
//				Element attribute=jdomParser.findElementByAt(qparser, QueryConstants.ATTRIBUTE, QueryConstants.PROP, idpropwhere.toString() , true, true);
//				Element parent=where.getParent();
//				Element aux=jdomParser.findElementByAt(parent, QueryConstants.ATTRIBUTE, QueryConstants.PROP, propWhere, true, true);
//				String id=aux.getAttributeValue(QueryConstants.ID);
//				Integer idProp=pDAO.getNewPKVirtual();
//				prop.setAttribute("PROP", idProp.toString());
//				prop.setAttribute("ID_QUERY",id);
//				Properties p=new Properties();
//				p.setPROP(idProp);
//				
//				String idpropbd=id_leftWhere;
//				p.setNAME(idpropbd+"@"+nameReport);
//				Integer type=Integer.parseInt(attribute.getAttributeValue(QueryConstants.ID_TM_RULEENGINE));
//				p.setCAT(Category.iDataProperty);
//				p.setVALUECLS(type);
//				this.reportProperties.get(nameReport).add(p);
//				
//				AliasDAO aDAO= new AliasDAO();
//				Alias a = new Alias();
//				a.setUTaskName(nameReport);
//				a.setPropName(p.getNAME());
//				a.setAlias(idpropbd);
//				aDAO.insert(a);
//				
//				pDAO.insert(p);
//				map.addContent(prop);
//			}
//		}
//		if(!lhavingpresentation.isEmpty()){
//			Iterator<Element> ithaving=lhavingpresentation.iterator();
//			while(ithaving.hasNext()){
//				Element prop=new Element("PROP");
//				Element having=ithaving.next();
//				String id_leftWhere=having.getAttributeValue(QueryConstants.ID_LEFT);
//				Element attributeNP=jdomParser.findElementByAt(queryXml, QueryConstants.AGREGATION, QueryConstants.ID, id_leftWhere , true, true);
//				String propWhere=attributeNP.getAttributeValue(QueryConstants.PROP);
////				Element attributeNP=jdomParser.findElementByAt(where.getParent(), QueryConstants.ATTRIBUTE, QueryConstants.PROP, propWhere, true, true);
////				String idpropwhere=attributeNP.getAttributeValue(QueryConstants.PROP);
//				Integer idpropwhere = pDAO.getIdPropByName(propWhere);
//				
//				Element attribute=jdomParser.findElementByAt(qparser, QueryConstants.ATTRIBUTE, QueryConstants.PROP, idpropwhere.toString() , true, true);
//				Element parent=having.getParent();
//				Element aux=jdomParser.findElementByAt(parent, QueryConstants.ATTRIBUTE, QueryConstants.PROP, propWhere, true, true);
//				String id=aux.getAttributeValue(QueryConstants.ID);
//				Integer idProp=pDAO.getNewPKVirtual();
//				prop.setAttribute("PROP", idProp.toString());
//				prop.setAttribute("ID_QUERY",id);
//				Properties p=new Properties();
//				p.setPROP(idProp);
//				
//				String idpropbd=id_leftWhere;
//				p.setNAME(idpropbd);
//				Integer type=Integer.parseInt(attribute.getAttributeValue(QueryConstants.ID_TM_RULEENGINE));
//				p.setCAT(Category.iDataProperty);
//				p.setVALUECLS(type);
//				this.reportProperties.get(nameReport).add(p);
//				
//				AliasDAO aDAO= new AliasDAO();
//				Alias a = new Alias();
//				a.setUTaskName(nameReport);
//				a.setPropName(p.getNAME());
//				a.setAlias(idpropbd);
//				aDAO.insert(a);
//				
//				pDAO.insert(p);
//				map.addContent(prop);
//			}
//		}
//		if(!lhavingcontainsp.isEmpty()){
//			Iterator<Element> ithaving=lhavingcontainsp.iterator();
//			while(ithaving.hasNext()){
//				Element prop=new Element("PROP");
//				Element having=ithaving.next();
//				String id_leftWhere=having.getAttributeValue(QueryConstants.ID_LEFT);
//				Element attributeNP=jdomParser.findElementByAt(queryXml, QueryConstants.ATTRIBUTE, QueryConstants.ID, id_leftWhere , true, true);
//				String propWhere=attributeNP.getAttributeValue(QueryConstants.PROP);
////				Element attributeNP=jdomParser.findElementByAt(where.getParent(), QueryConstants.ATTRIBUTE, QueryConstants.PROP, propWhere, true, true);
////				String idpropwhere=attributeNP.getAttributeValue(QueryConstants.PROP);
//				Integer idpropwhere = pDAO.getIdPropByName(propWhere);
//				
//				Element attribute=jdomParser.findElementByAt(qparser, QueryConstants.ATTRIBUTE, QueryConstants.PROP, idpropwhere.toString() , true, true);
//				Element parent=having.getParent();
//				Element aux=jdomParser.findElementByAt(parent, QueryConstants.ATTRIBUTE, QueryConstants.PROP, propWhere, true, true);
//				String id=aux.getAttributeValue(QueryConstants.ID);
//				Integer idProp=pDAO.getNewPKVirtual();
//				prop.setAttribute("PROP", idProp.toString());
//				prop.setAttribute("ID_QUERY",id);
//				Properties p=new Properties();
//				p.setPROP(idProp);
//				
//				String idpropbd=id_leftWhere;
//				p.setNAME(idpropbd+"@"+nameReport);
//				Integer type=Integer.parseInt(attribute.getAttributeValue(QueryConstants.ID_TM_RULEENGINE));
//				p.setCAT(Category.iDataProperty);
//				p.setVALUECLS(type);
//				this.reportProperties.get(nameReport).add(p);
//				
//				AliasDAO aDAO= new AliasDAO();
//				Alias a = new Alias();
//				a.setUTaskName(nameReport);
//				a.setPropName(p.getNAME());
//				a.setAlias(idpropbd);
//				aDAO.insert(a);
//				
//				pDAO.insert(p);
//				map.addContent(prop);
//			}
//		}
		//pDAO.close();
		//SERVERParser.deleteAttributes(queryXml);
		return map;
	}

//	private static void deleteAttributes(Element queryXml) {
//		ArrayList<Element> nodesWithAttribute = jdomParser.elementsWithAt(queryXml, "MACA", true);
//		Iterator<Element> itnodes = nodesWithAttribute.iterator();
//		while(itnodes.hasNext()){
//			Element node = itnodes.next();
//			node.removeAttribute("MACA");
//		}
//	}

	/*public ArrayList<Element> getHavingWithContainsValue(Element presentation) {
		ArrayList<Element> result = new ArrayList<Element>();
		ArrayList<Element> aux=jdomParser.elements(presentation, QueryConstants.HAVING, true);
		Iterator<Element> itaux= aux.iterator();
		while(itaux.hasNext()){
			Element e=itaux.next();
			if (e.getText().equals("(VALUE)")){
				result.add(e);
			}
		}
		return result;
	}*/

	public static ArrayList<Element> getWhereWithContainsValue(Element queryXml) {
		ArrayList<Element> result = new ArrayList<Element>();
		ArrayList<Element> aux = jdomParser.elements(queryXml, QueryConstants.WHERE, true);
		Iterator<Element> itaux = aux.iterator();
		while(itaux.hasNext()){
			Element e = itaux.next();
			if (e.getText().contains("(VALUE)") || e.getAttributeValue(QueryConstants.VALUE)!=null && e.getAttributeValue(QueryConstants.VALUE).contains("(VALUE)")){
				result.add(e);
			}
		}
		return result;
	}
	
	private LinkedList<SSubReport> createSubReport(SReport sr, HashMap<String,File> lsr) throws IOException {
		Set<String> setid=lsr.keySet();
		LinkedList<SSubReport> result= new LinkedList<SSubReport> ();
		Iterator<String> itid=setid.iterator();
		while (itid.hasNext()) {
			String id=itid.next();
			File f= lsr.get(id);
			BufferedReader bfq = new BufferedReader(new FileReader(f));
			StringBuffer jrxml=new StringBuffer("");
			String sLine="";
			while((sLine = bfq.readLine())!=null){
				jrxml.append(sLine);
			}
			SSubReport subreport= new SSubReport(sr.getIdto(),id,jrxml.toString());
			result.add(subreport);
			
		}
		return result;
	}

	private void createMapQueryJrxml(String path, String queryR, 
			HashMap<String,String> queryName, HashMap<String,String> hqj) throws IOException, JDOMException {
		File dir = new File(path);
		//TODO optimizable: solo habra una query y un jrxml
		ArrayList<File> querys=new ArrayList<File>();
		ArrayList<File> jrxmls=new ArrayList<File>();
		//System.out.println("queryR " + queryR);
		
		String jrxmlR = null;
		if (queryR!=null) {
			String pathQuery = dir + "\\" + queryR+".query";
			System.out.println("pathQuery:"+pathQuery);
			try {
				BufferedReader bfq = new BufferedReader(new FileReader(pathQuery));
				StringBuffer query=new StringBuffer("");
				String sLine="";
				while((sLine = bfq.readLine())!=null){
					query.append(sLine);
				}
				Element queryXML=jdomParser.readXML(query.toString()).getRootElement();
				jrxmlR = queryR;
			} catch (IOException e) {
				;
			}
		}
		System.out.println("Directorio:"+dir.getAbsolutePath());
		System.out.println("queryR " + queryR+".query");
		System.out.println("jrxmlR " + jrxmlR+".jrxml");
		File[] files= dir.listFiles();
		for(int i=0; i<files.length; i++){
			if (files[i].isFile()){
				File f= files[i];
				//System.out.println(f.getName());
				String fileName= f.getName().toLowerCase();
				
				if (queryR!=null){
					//System.out.println("fileName " + fileName);
					
					if(fileName.equalsIgnoreCase(jrxmlR+".jrxml")) {
						jrxmls.add(f);
					}else if(fileName.equalsIgnoreCase(queryR+".query")){
						querys.add(f);
					}
				} else {
					if(fileName.endsWith(".jrxml")) {
						jrxmls.add(f);
					}else if(fileName.endsWith(".query")){
						querys.add(f);
					}
				}
			}
		}
		//iterar por querys y obtener el nombre de sus jrxmls
		
		Iterator<File> itquerys=querys.iterator();
		while(itquerys.hasNext()){
			File fq=itquerys.next();
			String nameFq = fq.getName();
			//System.out.println("nameFq " + nameFq);
			BufferedReader bfq = new BufferedReader(new FileReader(fq));
			StringBuffer query=new StringBuffer("");
			String sLine="";
			while((sLine = bfq.readLine())!=null){
				query.append(sLine);
			}
			String nameFj = null;
			String s1[]=nameFq.split("[.]");
			nameFj=s1[0];
			
			//System.out.println("nameFj " + nameFj);
			File fj=searchReport(nameFj,jrxmls);
			if (fj!=null){
				BufferedReader bfj = new BufferedReader(new FileReader(fj));
				StringBuffer jrxml=new StringBuffer("");
				sLine="";
				while((sLine = bfj.readLine())!=null){
					jrxml.append(sLine);
				}
				String[] s=fq.getName().split("[.]");
				String nameq=s[0];
				hqj.put(query.toString(), jrxml.toString());
				queryName.put(query.toString(), nameq);
			}
		}
	}
	
	private File searchQuery(String nameFj, ArrayList<File> querys) {
		Iterator<File> itq=querys.iterator();
		while(itq.hasNext()){
			File fq=itq.next();
	
			String s[]=nameFj.split("[.]");
			String namej=s[0];
			s=fq.getName().split("[.]");
			String nameq=s[0];
			if (namej.equalsIgnoreCase(nameq)){
				return fq;
			}
		}
		return null;
	}
	private File searchReport(String nameFj, ArrayList<File> jrxmls) {
		Iterator<File> itj=jrxmls.iterator();
		while(itj.hasNext()){
			File fj=itj.next();
	
			//String s[]=nameFr.split("[.]");
			//String namej=s[0];
			String namej=nameFj;
			String s[]=fj.getName().split("[.]");
			String nameq=s[0];
			if (namej.equalsIgnoreCase(nameq)){
				return fj;
			}
		}
		return null;
	}

	public static void updateDesign(String path, String queryU, String pathIreport) throws SQLException, NamingException, IOException, JDOMException, ParseException, JRException, InterruptedException, NotFoundException, IncoherenceInMotorException {
		REPORTParser parser= new REPORTParser();
		parser.runUpdate(path,queryU, pathIreport);
	}
	private void runUpdate(String path, String queryU, String pathIreport) throws SQLException, NamingException, IOException, JDOMException, ParseException, JRException, InterruptedException, NotFoundException, IncoherenceInMotorException {
		File fqupdate = new File(path+queryU+".query");
		if (fqupdate.exists()){
			
			getDesign(path, queryU);
			
			BufferedReader bfq = new BufferedReader(new FileReader(fqupdate));
			StringBuffer query=new StringBuffer("");
			String sLine="";
			while((sLine = bfq.readLine())!=null){
				query.append(sLine);
			}
			Element queryXMLUpdate=jdomParser.readXML(query.toString()).getRootElement();
			TClaseDAO cDAO=new TClaseDAO();
			cDAO.open();
			TClase queryDB=cDAO.getTClaseByName(queryU);
			cDAO.close();
			
			Element queryXMLDB = null;
			if (queryDB==null) {
				String resp=Auxiliar.leeTexto("LA CONSULTA NO SE ENCUENTRA EN BASE DE DATOS, �DESEA OBTENERLA DE algun ARCHIVO LOCAL EN LA RUTA " + path + "? S/N") ;
				if(resp.equalsIgnoreCase("S")){
					//obtener la query antigua de otro fichero
					String queryR=Auxiliar.leeTexto("Introduzca el nombre de la query");
					queryR=queryR.toLowerCase();
					File fquery = new File(path+queryR+".query");
					if (fquery.exists()){
						bfq = new BufferedReader(new FileReader(fquery));
						query=new StringBuffer("");
						sLine="";
						while((sLine = bfq.readLine())!=null){
							query.append(sLine);
						}
						queryXMLDB=jdomParser.readXML(query.toString()).getRootElement();
					} else {
						System.err.println("La query "+queryR+" no se encuentra en "+path);
					}
				}
			} else {
				SReportDAO qDAO=new SReportDAO();
				qDAO.open();
				SReport sr= qDAO.getSReportByIDTO(String.valueOf(queryDB.getIDTO()));
				qDAO.close();
				String queryS=sr.getQuery();
				queryXMLDB=jdomParser.readXML(queryS).getRootElement();
			}
			if (queryXMLDB!=null) {
				//obtener nameReport de queryXMLUpdate
				//QueryReportParser qrp=new QueryReportParser(DAOManager.getFactConnDB(), false);
				ReportService rs=new ReportService(DAOManager.getFactConnDB(),null, false, true);
				//Element qparserDB=(Element) queryXMLDB.clone();
				//qrp.parserIDs(queryXMLDB);
				HashMap<String, ParQuery> hrqDB=rs.Report(queryXMLDB, getIdReport(queryXMLDB));
	//			Element qparserUpdate=(Element) queryXMLUpdate.clone();
				//qrp=new QueryReportParser(DAOManager.getFactConnDB(), true);
				//qrp.parserIDs(queryXMLUpdate);
				HashMap<String, ParQuery> hrqUpdate=rs.Report(queryXMLUpdate, getIdReport(queryXMLUpdate));
				Set<String> ids=hrqUpdate.keySet();
				Iterator<String> itids=ids.iterator();
				HashMap<String,String> jrxmls= new HashMap<String, String>();
				while(itids.hasNext()){
					String id= itids.next();
					if (hrqDB.containsKey(id)){
						updateConcreteDesign(hrqDB.get(id), hrqUpdate.get(id), id, getIdReport(queryXMLUpdate),path,queryXMLUpdate, queryU);
						String name= path;
						if (id.equals(getIdReport(queryXMLUpdate)))
							name+=queryU+".jrxml";
						else{
							name+=queryU+"_"+id+".jrxml";
						}
						jrxmls.put(id, name);
					}else{
						makeDesignNewSubreport(hrqUpdate.get(id),path,queryU, id,queryXMLUpdate);
						String name=path+queryU+"_"+id+".jrxml";
						jrxmls.put(id, name);
					}
				}
				EditReports.edit(jrxmls, pathIreport);
			}
		}else{
			System.err.println("La query "+queryU+" no se encuentra en "+path);
		}
	}

	private void makeDesignNewSubreport(ParQuery query, String path, String queryU, String id, Element queryXMLUpdate) throws IOException {
		String[] shows=query.getShow();
		ArrayList<String> listshow=dynagent.common.utils.Auxiliar.stringArrayToArrayList(shows);
		Map<String, String> attrs= new HashMap<String, String>();
		Iterator<String> itAtt=listshow.iterator();
		
		Element nodoSub=jdomParser.findElementByAt(queryXMLUpdate, QueryConstants.ID, id, true);
		String nameSub=nodoSub.getAttributeValue(QueryConstants.NAME);
		if(nameSub==null)
			nameSub=id;
		while(itAtt.hasNext()){
			String temp[]=itAtt.next().split("_");
			String nombre="";
			for(int j=1;j<temp.length-1;j++){
				if (j==temp.length-2)
					nombre=nombre.concat(temp[j]);
				else
					nombre=nombre.concat(temp[j])+" ";
			}
			String type = temp[temp.length-1];
			if (type.equals("Enumerado"))
				type = "String";
			attrs.put(nombre, type);
		}
		String name="sub"+id;
		String nameFile=path+queryU+"_"+nameSub+".jrxml";
		String[] ids=query.getIds();
		ArrayList<String> listids=dynagent.common.utils.Auxiliar.stringArrayToArrayList(ids);
		GenerateJRXML.makeJRXML(attrs, name, nameFile, listids, true, new ArrayList<String>());
	}

	private void updateConcreteDesign(ParQuery queryDB, ParQuery queryUpdate, String id, String idReport, String path, Element queryXMLUpdate, String queryU) throws IOException, JDOMException {
		File fdesign=null;
		Element nodoSub=jdomParser.findElementByAt(queryXMLUpdate, QueryConstants.ID, id, true);
		String nameSub=nodoSub.getAttributeValue(QueryConstants.NAME);
		if(nameSub==null)
			nameSub=id;
		if (id.equals(idReport)){
			fdesign = new File(path+queryU+".jrxml");
		}else{
			fdesign = new File(path+queryU+"_"+nameSub+".jrxml");
		}
		BufferedReader bfd = new BufferedReader(new FileReader(fdesign));
		StringBuffer design=new StringBuffer("");
		String sLine="";
		while((sLine = bfd.readLine())!=null){
			design.append(sLine);
		}
		Element designXML=jdomParser.readXML(design.toString()).getRootElement();
		String[] showsDB=queryDB.getShow();
		String[] showsUpdate=queryUpdate.getShow();
		String[] idsDB=queryDB.getIds();
		String[] idsUpdate=queryUpdate.getIds();
		ArrayList<String> listidsDB=dynagent.common.utils.Auxiliar.stringArrayToArrayList(idsDB);
		ArrayList<String> listidsUpdate=dynagent.common.utils.Auxiliar.stringArrayToArrayList(idsUpdate);
		ArrayList<String> listshowDB=dynagent.common.utils.Auxiliar.stringArrayToArrayList(showsDB);
		ArrayList<String> listshowUpdate=dynagent.common.utils.Auxiliar.stringArrayToArrayList(showsUpdate);
		ArrayList<String> listnewSubreports=dynagent.common.utils.Auxiliar.getNewElements(listidsUpdate,listidsDB);
		ArrayList<String> listoldSubreports=dynagent.common.utils.Auxiliar.getNewElements(listidsDB,listidsUpdate);
		ArrayList<String> listnewAtrributes=dynagent.common.utils.Auxiliar.getNewElements(getNameType(listshowUpdate),getNameType(listshowDB));
		ArrayList<String> listoldAtrributes=dynagent.common.utils.Auxiliar.getNewElements(getNameType(listshowDB),getNameType(listshowUpdate));
		
		Map<String, String> attrsNew= new HashMap<String, String>();
		Iterator<String> itnewAtt=listnewAtrributes.iterator();
		while(itnewAtt.hasNext()){
			String temp[]=itnewAtt.next().split("_");
			String nombre="";
			for(int j=0;j<temp.length-1;j++){
				if (j==temp.length-2)
					nombre=nombre.concat(temp[j]);
				else
					nombre=nombre.concat(temp[j])+" ";
			}
			String type = temp[temp.length-1];
			if (type.equals("Enumerado"))
				type = "String";
			attrsNew.put(nombre, type);
		}
		insertNewAttr(designXML,attrsNew);
		
		Map<String, String> attrsOld= new HashMap<String, String>();
		Iterator<String> itoldAtt=listoldAtrributes.iterator();
		while(itoldAtt.hasNext()){
			String temp[]=itoldAtt.next().split("_");
			String nombre="";
			for(int j=0;j<temp.length-1;j++){
				if (j==temp.length-2)
					nombre=nombre.concat(temp[j]);
				else
					nombre=nombre.concat(temp[j])+" ";
			}
			String type = temp[temp.length-1];
			if (type.equals("Enumerado"))
				type = "String";
			attrsOld.put(nombre, type);
		}
		deleteOldAttr(designXML,attrsOld);
		
		insertNewSub(designXML,listnewSubreports);		
		deleteOldSub(designXML,listoldSubreports);
		String newDesign=jdomParser.returnXML(designXML);
		
		FileWriter fw = new FileWriter(fdesign);
		
		fw.write(newDesign);
		fw.close();
		
		System.out.println("Salvado el nuevo diseño:"+fdesign.getName());
		
	}

	private ArrayList<String> getNameType(ArrayList<String> list) {
		ArrayList<String> listNames = new ArrayList<String>();
		Iterator<String> it=list.iterator();
		while(it.hasNext()){
			String elem=it.next();
			String[] elemSpl = elem.split("_");
			String name = "";
			//Quitamos numero de columna
			for (int i=1;i<elemSpl.length;i++) {
				if (i>1)
					name += "_";
				name += elemSpl[i];
			}
			listNames.add(name);
		}
		return listNames;
	}
	
	private void deleteOldSub(Element designXML, ArrayList<String> listoldSubreports) {
		Iterator<String> itoldSub=listoldSubreports.iterator();
		while(itoldSub.hasNext()){
			String sub=itoldSub.next();
			Element elementF=jdomParser.findElementByAt(designXML,"field", "name", "sub"+sub, true);
			designXML.removeContent(elementF);
			Element elementP=jdomParser.findElementByAt(designXML,"parameter", "name", "paramsub"+sub, true);
			designXML.removeContent(elementP);
			Element elementSub=jdomParser.findElementByCDATA(designXML, "subreportExpression", "$P{paramsub"+sub+"}", true);
			if (elementSub!=null) {
				Element elementSubP=elementSub.getParent();
				elementSubP.getParent().removeContent(elementSubP);
			}
		}
	}

	private void insertNewSub(Element designXML, ArrayList<String> listnewSubreports) {
		Iterator<String> itnewSub=listnewSubreports.iterator();
		while(itnewSub.hasNext()){
			String sub=itnewSub.next();
			Element param= new Element("parameter");
			param.setAttribute("name", "paramsub"+sub);
			param.setAttribute("isForPrompting", "true");
			param.setAttribute("class", "net.sf.jasperreports.engine.JasperReport");
			designXML.addContent(param);
			
			Element field= new Element("field");
			field.setAttribute("name", "sub"+sub);
			
			field.setAttribute("class", "java.lang.Object");
			designXML.addContent(field);
			
		}
	}

	private void deleteOldAttr(Element designXML, Map<String, String> attrsOld) {
		Set<String> names=attrsOld.keySet();
		Iterator<String> itnames= names.iterator();
		while(itnames.hasNext()){
			String name= itnames.next();
			Element element=jdomParser.findElementByAt(designXML,"field", "name", name, true);
			designXML.removeContent(element);
			/*Element elementText=jdomParser.findElementByCDATA(designXML, "textFieldExpression", "$F{"+name+"}", true);
			if (elementText!=null) {
				elementText.removeAttribute("class");
				ArrayList<CDATA> lCData = new ArrayList<CDATA>();
				lCData.add(new CDATA("ERROR: Campo no encontrado"));
				elementText.setContent(lCData);
//				Element elementTextField = elementText.getParent();
//				elementTextField.getParent().removeContent(elementTextField);
			}*/
		}
	}

	private void insertNewAttr(Element designXML, Map<String, String> attrs) {
		Set<String> names=attrs.keySet();
		Iterator<String> itnames= names.iterator();
		while(itnames.hasNext()){
			String name= itnames.next();
			String type= attrs.get(name);
			Element attr= new Element("field");
			attr.setAttribute("name", name);
			attr.setAttribute("class", "java.lang."+type);
			Element element=jdomParser.element(designXML,"jasperReport", true, true);
			element.addContent(attr);
		}
	}

}
