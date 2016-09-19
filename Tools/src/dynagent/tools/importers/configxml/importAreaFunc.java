package dynagent.tools.importers.configxml;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;



import org.jdom.Element;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Alias;
import dynagent.common.basicobjects.AreaFunc;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.DeletableObject;
import dynagent.server.services.InstanceService;

public class importAreaFunc extends ObjectConfig{

	private LinkedList<AreaFunc> listaf=new LinkedList<AreaFunc>();
	public importAreaFunc(Element afXML, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport,String pathImportOtherXml) throws Exception{
		super(afXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listaf=new LinkedList<AreaFunc>();
	}
	
	@Override
	public boolean configData() throws Exception{
		deleteAreaFunc();
		return extractAllAreaFunc();
	}
	@Override
	public void importData() throws Exception {
		insertAreaFunc();
	}
	
	private void deleteAreaFunc() throws SQLException, NamingException, NoSuchColumnException{
		DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(Constants.CLS_FUNCTIONAL_AREA, fcdb), instanceService.getDataBaseMap(), fcdb);
	}

	private void insertAreaFunc() throws SQLException, NamingException {
		Iterator<AreaFunc> itaf=this.listaf.iterator();
		while(itaf.hasNext()){
			AreaFunc af=itaf.next();
			//System.out.println("---> "+ af.toString());
			Auxiliar.createAreaFunc(af,fcdb);
			//System.out.println("---> OK");
		}
	}

	private boolean extractAllAreaFunc() throws Exception{
		Iterator itaf = getChildrenXml().iterator();
		boolean success=true;
		while(itaf.hasNext()){
			try{	
				Element afElem = (Element)itaf.next();
				AreaFunc af = new AreaFunc();
				if(afElem.getAttribute(ConstantsXML.NAME)!=null){
					String nameaf=afElem.getAttributeValue(ConstantsXML.NAME).toString();
					af.setName(nameaf);
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.NAME+"' es obligatorio en el nodo");
				}
				listaf.add(af);
			}catch(ConfigException ex){
				System.err.println(ex.getMessage());
				success=false;
			}catch(Exception ex){
				throw ex;
			}
		}
		return success;
	}
	
	
}
