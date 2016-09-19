package dynagent.tools.importers.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.naming.NamingException;

import org.jdom.JDOMException;

import dynagent.common.utils.Auxiliar;
import dynagent.server.ejb.FactoryConnectionDB;




public class QUERYParser {
	
	public static void delete(String name) throws SQLException, NamingException{
		deleteSQuery(name);
	}
	
	private static void deleteSQuery(String name) throws SQLException, NamingException {
		QueryDAO qDAO= new QueryDAO();
		qDAO.open();
		qDAO.deleteCond("NAME LIKE '"+name+"'");
		qDAO.close();
	}
	
	public static void importQuery(String queryR, String path, FactoryConnectionDB fcdb) throws IOException, JDOMException, ParseException, SQLException, NamingException{
		boolean reemplace = false;
		String resp = "";
		if(queryR!=null)
			resp = Auxiliar.leeTexto("¿DESEA SUSTITUIR ESTA QUERY SI ESTA EN LA BASE DE DATOS? S/N") ;
		else
			resp = Auxiliar.leeTexto("¿DESEA SUSTITUIR LAS QUERYS QUE HAY YA EN LA BASE DE DATOS? S/N") ;
		if(resp.equalsIgnoreCase("S"))
			reemplace = true;
		
		run(path, reemplace, queryR, fcdb);
	}
	
	public static void run(String path, boolean reemplace, String queryR, FactoryConnectionDB fcdb) throws IOException, JDOMException, ParseException, SQLException, NamingException{
		HashMap<String,String> queryName = new HashMap<String, String>();
		createMapQuery(path, queryR, queryName);
		Set<String> querys = queryName.keySet();
		ArrayList<SQuery> listSQuery =  new ArrayList<SQuery>();
		
		Iterator<String> itq = querys.iterator();
		while(itq.hasNext()){
			String query = itq.next();
			String name = queryName.get(query);
			if (reemplace)
				delete(name);

			SQuery sq = createSQuery(name, query);
			listSQuery.add(sq);
		}
		setInDB(listSQuery);
	}

	private static void setInDB(ArrayList<SQuery> listSQuery) throws SQLException, NamingException {
		if (listSQuery.isEmpty()){
			System.out.println("NO HAY QUERY A INSERTAR");
		}else{
			QueryDAO qDAO = new QueryDAO();
			qDAO.open();
			Iterator<SQuery> its = listSQuery.iterator();
			while(its.hasNext()){
				SQuery sr = its.next();
				qDAO.insert(sr);
			}
			qDAO.close();
		}
	}

	private static SQuery createSQuery(String name, String query) throws JDOMException, ParseException, SQLException, NamingException {
		SQuery sr = new SQuery(name,query);
		return sr;
	}

	private static void createMapQuery(String path, String queryR, HashMap<String,String> queryName) throws IOException {
		File dir = new File(path);
		ArrayList<File> querys = new ArrayList<File>();
		
		System.out.println("Directorio:"+dir.getAbsolutePath());
		File[] files = dir.listFiles();
		for(int i=0; i<files.length; i++){
			if (files[i].isFile()){
				File f = files[i];
				System.out.println(f.getName());
				String fileName = f.getName().toLowerCase();
				
				if (queryR!=null){
					if(fileName.equalsIgnoreCase(queryR+".query"))
						querys.add(f);
				} else {
					if(fileName.endsWith(".query"))
						querys.add(f);
				}
			}
		}
		
		Iterator<File> itquery = querys.iterator();
		while(itquery.hasNext()){
			File fq = itquery.next();
			if (fq!=null){
				BufferedReader bfq = new BufferedReader(new FileReader(fq));
				StringBuffer query = new StringBuffer("");
				String sLine = "";
				while((sLine = bfq.readLine())!=null) {
					query.append(sLine);
				}
				String[] s = fq.getName().toLowerCase().split("[.]");
				String nameq = s[0];
				queryName.put(query.toString(), nameq);
			}
		}
	}
}
