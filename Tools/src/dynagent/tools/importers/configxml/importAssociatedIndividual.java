package dynagent.tools.importers.configxml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.jdom.Element;

import dynagent.common.basicobjects.AssociatedIndividual;
import dynagent.server.database.dao.AssociatedIndividualDAO;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;

public class importAssociatedIndividual extends ObjectConfig {
	
	private LinkedList<AssociatedIndividual> listAssociatedIndividual;
	
	public importAssociatedIndividual(Element associatedIndividualXML, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(associatedIndividualXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listAssociatedIndividual=new LinkedList<AssociatedIndividual>();
	}

	@Override
	public boolean configData() throws Exception {
		return extractAllAssociatedIndividual();
	}
	@Override
	public void importData() throws Exception {
		importAllAssociatedIndividual();
	}
	
	private void importAllAssociatedIndividual() throws Exception{
		Iterator<AssociatedIndividual> itrp=this.listAssociatedIndividual.iterator();
		AssociatedIndividualDAO aiDAO=new AssociatedIndividualDAO();
		aiDAO.setCommit(false);
		aiDAO.open();
		while(itrp.hasNext()){
			AssociatedIndividual ai=itrp.next();
			aiDAO.insert(ai);
		}
		aiDAO.commit();
		aiDAO.close();
	}


	private boolean extractAllAssociatedIndividual() throws Exception{
		Iterator itr = getChildrenXml().iterator();
		boolean success=true;
		while(itr.hasNext()){
			Element rElem = (Element)itr.next();
			AssociatedIndividual ai = new AssociatedIndividual();
			try{
				if (rElem.getAttribute(ConstantsXML.CLASS_KEY)!=null){
					String idtoName=rElem.getAttributeValue(ConstantsXML.CLASS_KEY).toString();
					
					Integer idto=Auxiliar.getIdtoClass(idtoName, fcdb);
					if(idto!=null){
						ai.setIdtoKey(idto);
					}else{
						throw new ConfigException("Error: La Clase "+idtoName+" no existe en el modelo");
					}
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.CLASS_KEY+"' es obligatorio en el nodo");
				}
				
				if (rElem.getAttribute(ConstantsXML.CLASS_SUFIX)!=null){
					String idtosName=rElem.getAttributeValue(ConstantsXML.CLASS_SUFIX).toString();
					ArrayList<Integer> aIdtos = new ArrayList<Integer>();
					String[] idtosNameSpl = idtosName.split(",");
					for(int i=0;i<idtosNameSpl.length;i++) {
						String idtoName = idtosNameSpl[i];
						Integer idto=Auxiliar.getIdtoClass(idtoName, fcdb);
						if(idto!=null){
							aIdtos.add(idto);
						}else{
							throw new ConfigException("Error: La Clase "+idtoName+" no existe en el modelo");
						}
					}
					ai.setIdtoSufix(aIdtos);
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.CLASS_SUFIX+"' es obligatorio en el nodo");
				}

				if (rElem.getAttribute(ConstantsXML.ASSOCIATED_CLASS)!=null){
					String idtoName=rElem.getAttributeValue(ConstantsXML.ASSOCIATED_CLASS).toString();
					
					Integer idto=Auxiliar.getIdtoClass(idtoName, fcdb);
					if(idto!=null){
						ai.setAssociatedIdto(idto);
					}else{
						throw new ConfigException("Error: La Clase "+idtoName+" no existe en el modelo");
					}
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.ASSOCIATED_CLASS+"' es obligatorio en el nodo");
				}
				listAssociatedIndividual.add(ai);
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
