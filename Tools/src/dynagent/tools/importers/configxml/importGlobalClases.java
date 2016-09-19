package dynagent.tools.importers.configxml;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.Element;

import dynagent.common.basicobjects.Access;
import dynagent.common.basicobjects.GlobalClases;
import dynagent.server.database.dao.GlobalClasesDAO;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;

public class importGlobalClases extends ObjectConfig{
	
	private LinkedList<GlobalClases> listGlobalClases;
	private HashMap<GlobalClases,String> globalClassesUtask;
	
	public importGlobalClases(Element globalClasesXML, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(globalClasesXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listGlobalClases=new LinkedList<GlobalClases>();
		this.globalClassesUtask=new HashMap<GlobalClases, String>();
	}

	@Override
	public boolean configData() throws Exception {
		return extractAllGlobalClases();
	}
	@Override
	public void importData() throws Exception {
		deleteGlobalClases();
		insertGlobalClases();
	}

	private boolean extractAllGlobalClases() throws Exception{
		Iterator itut = xml.getChildren(ConstantsXML.GC).iterator();
		boolean success=true;
		while(itut.hasNext()){
			Element gcElem = (Element)itut.next();
			GlobalClases gc = new GlobalClases();
			try{
				if (gcElem.getAttribute(ConstantsXML.UTASK_ATRB)!=null){
					String uTask=gcElem.getAttributeValue(ConstantsXML.UTASK_ATRB).toString();
					Integer idtoUTask=Auxiliar.getIdtoClass(uTask, fcdb);
					if (idtoUTask!=null){
						gc.setUserTask(idtoUTask);
					}else{
						// Miramos que existe esa userTask en el xml ya que en base de datos aun no existe
						if(configImport.containsUserTask(uTask)){
							globalClassesUtask.put(gc, uTask);
						}else throw new ConfigException("Error: No se encontro la userTask "+ uTask);
					}
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.UTASK_ATRB+"' es obligatorio en el nodo");
				}
				if (gcElem.getAttribute(ConstantsXML.IDTO_ROOT)!=null){
					String nameRoot=gcElem.getAttributeValue(ConstantsXML.IDTO_ROOT).toString();
					Integer idtoRoot=Auxiliar.getIdtoClass(nameRoot, fcdb);
					if (idtoRoot!=null){
						gc.setIdtoRoot(idtoRoot);
					}else{
						throw new ConfigException("Error: No se encontro la clase "+ nameRoot);
					}
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.IDTO_ROOT+"' es obligatorio en el nodo");
				}
				
				if (gcElem.getAttribute(ConstantsXML.CENTRALIZED)!=null){
					boolean centralized=new Boolean(gcElem.getAttributeValue(ConstantsXML.CENTRALIZED).toString());
					gc.setCentralized(centralized);
				}
				listGlobalClases.add(gc);
			}catch(ConfigException ex){
				System.err.println(ex.getMessage());
				success=false;
			}catch(Exception ex){
				throw ex;
			}
		}
		return success;
	}
	
	private void deleteGlobalClases() throws SQLException, NamingException {
		GlobalClasesDAO gDAO = new GlobalClasesDAO();
		gDAO.open();
		gDAO.deleteAll();
		gDAO.close();
	}
	private void insertGlobalClases() throws SQLException, NamingException {
		GlobalClasesDAO gDAO = new GlobalClasesDAO();
		gDAO.setCommit(false);
		gDAO.open();
		Iterator<GlobalClases> itgc=this.listGlobalClases.iterator();
		while(itgc.hasNext()){
			GlobalClases gc=itgc.next();
			if(globalClassesUtask.containsKey(gc))
				gc.setUserTask(Auxiliar.getIdtoClass(globalClassesUtask.get(gc),fcdb));
			System.out.println("---> "+gc.toString());
			gDAO.insert(gc);
			System.out.println("OK");
		}
		gDAO.commit();
		gDAO.close();
	}
}
