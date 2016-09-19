package dynagent.tools.importers.configxml;


import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.Element;


import dynagent.common.Constants;
import dynagent.common.basicobjects.AreaFuncRes;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;

public class importAreaFuncRes extends ObjectConfig{

	private LinkedList<AreaFuncRes> listaf;

	public importAreaFuncRes(Element areafuncXML, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(areafuncXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listaf=new LinkedList<AreaFuncRes>();
	}

	@Override
	public boolean configData() throws Exception {
		return extractAllAreaFunc();
	}
	@Override
	public void importData() throws Exception {
		insertAreaFunc();
	}

	public boolean extractAllAreaFunc() throws Exception{
		Iterator itaf = getChildrenXml().iterator();
		boolean success=true;
		while(itaf.hasNext()){
			Element afElem = (Element)itaf.next();
			AreaFuncRes af = new AreaFuncRes();
			try{
				if(afElem.getAttribute(ConstantsXML.NAME)!=null){
					String nameaf=afElem.getAttributeValue(ConstantsXML.NAME).toString();
					af.setArea_func(nameaf);
					Integer idoaf=Auxiliar.getIdo(Constants.IDTO_FUNCTIONAL_AREA, nameaf, fcdb, instanceService.getDataBaseMap());
					if(idoaf!=null){
						af.setIdo_area_func(idoaf);
					}else{
						throw new ConfigException("Error: No existe el area funcional '"+nameaf+"' en el modelo");
					}
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.NAME+"' es obligatorio en el nodo");
				}
				if(afElem.getAttribute(ConstantsXML.ACTIVE)!=null){
					String active=afElem.getAttributeValue(ConstantsXML.ACTIVE).toString();
					af.setActivo(active);
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.ACTIVE+"' es obligatorio en el nodo");
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
	
	private void insertAreaFunc() throws SQLException, NamingException {
		Iterator<AreaFuncRes> itaf=this.listaf.iterator();
		
		while(itaf.hasNext()){
			AreaFuncRes a =itaf.next();
			System.out.println("---> "+a.toString());
			LinkedList<Integer> lut=Auxiliar.getAllUTask(a.getIdo_area_func(), fcdb);
			if(!lut.isEmpty()){
				Iterator<Integer> itUt=lut.iterator();
				while(itUt.hasNext()){
					Integer ut=itUt.next();
					//System.out.println("---> Borrando UTask "+ut+" ya que pertenece al area funcional");
					Auxiliar.deleteUtask(ut,fcdb);
					//System.out.println("---> OK");
				}
			}
			
			//System.out.println("---> OK");
		}
		
	}
}
