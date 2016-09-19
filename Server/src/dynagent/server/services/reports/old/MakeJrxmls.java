package dynagent.server.services.reports.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
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

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.basicobjects.SReport;
import dynagent.common.basicobjects.TClase;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.jdomParser;
import dynagent.server.database.dao.SReportDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.ReportService;
import dynagent.server.services.reports.EditReports;
import dynagent.server.services.reports.ExtensionFilter;
import dynagent.server.services.reports.GenerateJRXML;
import dynagent.server.services.reports.ParQuery;

public class MakeJrxmls {

	private HashMap<String, String> queryName= new HashMap<String, String>();

	public static void createDesign(int nb, FactoryConnectionDB fcdb, String path, String pathIreport) throws IOException, JDOMException, ParseException, JRException, InterruptedException, SQLException, NamingException, NotFoundException, IncoherenceInMotorException{
		MakeJrxmls make = new MakeJrxmls();
		String name=Auxiliar.leeTexto("Introduzca el nombre de la query ha dise�ar:");
		//que no exista ninguna clase con ese nombre
		TClaseDAO cDAO=new TClaseDAO();
		cDAO.open();
		TClase queryDB=cDAO.getTClaseByName(name);
		cDAO.close();
		
		boolean existClass=false;
		if(queryDB!=null){
			SReportDAO rDAO= new SReportDAO();
			rDAO.open();
			SReport rep=rDAO.getSReportByIDTO(String.valueOf(queryDB.getIDTO()));
			rDAO.close();
			if (rep==null){
				existClass=true;
			}
		}
		if (!existClass){
			make.run(path,nb,fcdb,name,pathIreport);
		} else {
			System.out.println("Ya existe en base de datos una clase de nombre " + name + ". Cambie el nombre al fichero de la query");
		}
	}

	private void makeJrxml( Element query, String nameR,Integer business,String path, FactoryConnectionDB fcdb, boolean reemplace, String pathIreport) throws JRException, IOException,
			InterruptedException, JDOMException, SQLException, NamingException,
			ParseException, NotFoundException, IncoherenceInMotorException {

		Map<String, String> jrxmls = new HashMap<String, String>();
	
		if (query != null) {
			
			jrxmls = reports(query, nameR, business, path, fcdb,reemplace);
			EditReports.edit(jrxmls, pathIreport);
		}
		
	}

	private Map<String, String> reports(Element query, String nameR, int business, String path,FactoryConnectionDB fcdb, boolean reemplace) throws ParseException,	JDOMException, SQLException, NamingException, IOException, NotFoundException, IncoherenceInMotorException {
		Map<String, String> jrxmls = null;
		HashMap<String, ParQuery> hrq = null;
		ReportService rs = new ReportService(fcdb, null, false, true);
		//QueryReportParser qrp = new QueryReportParser(fcdb, true);
		//qrp.parserIDs(query);
		String idMaster = REPORTParser.getIdReport(query);
		hrq = rs.Report(query, idMaster);
		Set<String> ids=hrq.keySet();
		Iterator<String> itids=ids.iterator();
		while(itids.hasNext()){
			ParQuery pq=hrq.get(itids.next());
			List<String> subreports= new ArrayList<String>();
			String idssub[]=pq.getIds();
			if(!idssub[0].equals("")){
				for(int i=0;i<idssub.length;i++){
					subreports.add(idssub[i]);
				}
			}
		}
		ArrayList<String> paramsConditions = getParamsConditions(query);
		
		jrxmls = GenerateJRXML.make(hrq,nameR,idMaster,path,reemplace,paramsConditions);
		return jrxmls;
	}
	private ArrayList<String> getParamsConditions(Element map) {
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<Element> list = jdomParser.findElementsWithParam(map, true);
		LinkedList<String> idParamsList = new LinkedList<String>();
		
		if(!list.isEmpty()) {
			Iterator<Element> it = list.iterator();
			while(it.hasNext()) {
				Element elem = it.next();
				String name = elem.getAttributeValue(QueryConstants.NAME);
				if(name==null) {
					System.err.println("ERROR EL ATRIBUTO NAME DEBE SER OBLIGATORIO EN LOS NODOS QUE LLEVEN VALUE");
					try {
						throw new Exception();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				String idParams=elem.getAttributeValue(QueryConstants.ID_PARAM);
				if (idParams!=null) {
					if (!idParamsList.contains(idParams)) {
						idParamsList.add(idParams);
						result.add(name);
					}
				} else
					result.add(name);
			}
		}
		return result;
	}
	
	private void getQuerys(String path, String name) throws IOException {
		File dir= new File(path);
		LinkedList<File> querys= new LinkedList<File>();
		FilenameFilter extensionfilter=new ExtensionFilter(".query",false);
		File[] files= dir.listFiles(extensionfilter);
		for(int i=0; i<files.length; i++){
			querys.add(files[i]);
		}
		Iterator<File> itq=querys.iterator();
		while(itq.hasNext()){
			File fq=itq.next();
			String sLine="";
			BufferedReader bfq = new BufferedReader(new FileReader(fq));
			StringBuffer query=new StringBuffer("");
			sLine="";
			while((sLine = bfq.readLine())!=null){
				query.append(sLine);
			}
			String[] s=fq.getName().toLowerCase().split("[.]");
			String nameq=s[0];
			this.queryName.put(query.toString(), nameq);
						
		}
	}


	private void run(String path,Integer nb, FactoryConnectionDB fcdb, String name, String pathIreport) throws IOException, JDOMException, ParseException, JRException, InterruptedException, SQLException, NamingException, NotFoundException, IncoherenceInMotorException{
		
		//getQuerys(path,name);
		File query= new File(path+"//"+name+".query");
		if (query.exists()){
			
//			Set<String> querys=this.queryName.keySet();
//			Iterator<String> itq= querys.iterator();
			boolean reemplace=false;
//			while(itq.hasNext()){
			String queryString= readQuery(query);
			System.out.println("QUERY DE FICHERO:"+queryString);
//			String nameq= this.queryName.get(query);
			Document queryDoc=jdomParser.readXML(queryString);
			Element queryXml=queryDoc.getRootElement();
			String resp=Auxiliar.leeTexto("�Desea sustituir los diseños de la query si estos existen? (S/N)");
			if(resp.equalsIgnoreCase("S")){
				reemplace=true;
			}
			makeJrxml(queryXml, name, nb, path,fcdb,reemplace, pathIreport);
//			}
		}else{
			System.out.println("La query '"+name+"' no se encuentra en el directorio '"+path+"'");
		}
	}

	private String readQuery(File query) throws IOException {
		String sLine="";
		BufferedReader bfq = new BufferedReader(new FileReader(query));
		StringBuffer queryS=new StringBuffer("");
		sLine="";
		while((sLine = bfq.readLine())!=null){
			queryS.append(sLine);
		}
		return queryS.toString();
	}



}
