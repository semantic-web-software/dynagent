package dynagent.tools.importers.configxml;

import java.io.IOException;
import java.util.ArrayList;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;

public abstract class ObjectConfig {
	protected ConfigData configImport;
	protected FactoryConnectionDB fcdb;
	protected Element xml;
	protected String pathImportOtherXml;
	protected InstanceService instanceService;
	
	public ObjectConfig(Element xml, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport, String pathImportOtherXml){
		this.xml=xml;
		this.fcdb=fcdb;
		this.configImport=configImport;
		this.pathImportOtherXml=pathImportOtherXml;
		this.instanceService=instanceService;
	}
	
	//Nos da los hijos de xml importados y los propios
	protected ArrayList<Element> getChildrenXml() throws IOException, JDOMException{
		ArrayList<Element> listChildren=new ArrayList<Element>();
		if(xml.getAttribute(ConstantsXML.IMPORT_CONFIG)!=null){
			String[] namesXML=xml.getAttributeValue(ConstantsXML.IMPORT_CONFIG).split(";");
			for(int i=0;i<namesXML.length;i++){
				Element xmlAux=dynagent.common.utils.Auxiliar.getXml(pathImportOtherXml+namesXML[i]+".xml");
				Element xmlAuxColumn=xmlAux.getChild(xml.getName());
				listChildren.addAll(xmlAuxColumn.getChildren());
			}
		}
		listChildren.addAll(xml.getChildren());
		return listChildren;
	}
	public abstract boolean configData() throws Exception;
	public abstract void importData() throws Exception;
}
