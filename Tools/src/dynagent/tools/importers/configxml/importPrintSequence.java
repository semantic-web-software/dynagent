package dynagent.tools.importers.configxml;

import java.util.Iterator;
import java.util.LinkedList;

import org.jdom.Element;

import dynagent.common.basicobjects.PrintSequence;
import dynagent.server.database.dao.PrintSequenceDAO;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;

public class importPrintSequence extends ObjectConfig{
	
	private LinkedList<PrintSequence> listprintSequence;
	
	public importPrintSequence(Element printSequenceXML, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(printSequenceXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listprintSequence=new LinkedList<PrintSequence>();
	}

	@Override
	public boolean configData() throws Exception {
		return extractAllPrintSequence();
	}
	@Override
	public void importData() throws Exception {
		importAllPrintSequence();
	}
	
	private void importAllPrintSequence() throws Exception{
		Iterator<PrintSequence> itrp=this.listprintSequence.iterator();
		PrintSequenceDAO ppDAO=new PrintSequenceDAO();
		ppDAO.setCommit(false);
		ppDAO.open();
		while(itrp.hasNext()){
			PrintSequence pp=itrp.next();
			ppDAO.insert(pp);
		}
		ppDAO.commit();
		ppDAO.close();
	}


	private boolean extractAllPrintSequence() throws Exception{
		Iterator itr = getChildrenXml().iterator();
		boolean success=true;
		while(itr.hasNext()){
			Element rElem = (Element)itr.next();
			PrintSequence pp = new PrintSequence();
			try{
				if (rElem.getAttribute(ConstantsXML.ORDER)!=null){
					pp.setOrder(rElem.getAttributeValue(ConstantsXML.ORDER));
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.ORDER+"' es obligatorio en el nodo");
				}
				if (rElem.getAttribute(ConstantsXML.SEQUENCE)!=null){
					pp.setSequence(rElem.getAttributeValue(ConstantsXML.SEQUENCE));
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.SEQUENCE+"' es obligatorio en el nodo");
				}
				if (rElem.getAttribute(ConstantsXML.PREPRINT)!=null){
					pp.setPrePrint(Boolean.parseBoolean(rElem.getAttributeValue(ConstantsXML.PREPRINT)));
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.PREPRINT+"' es obligatorio en el nodo");
				}
				listprintSequence.add(pp);
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
