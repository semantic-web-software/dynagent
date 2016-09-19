package dynagent.common.xml;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Access;
import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.basicobjects.Properties;
import dynagent.common.basicobjects.T_Herencias;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.utils.jdomParser;

public class GetData {
	private Element dataTestXML=null;
	
	public GetData(String nameDataXML){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document DOM;
		try {
			builder = factory.newDocumentBuilder();
			DOM = builder.parse(new InputSource(new FileReader(new File(Constants.relativePathOWLFILES+nameDataXML))));
			Element e1 = new Element(DOM.getElementsByTagName("METADATA").item(0).getNodeName());
			for(int i = 0 ; i < DOM.getElementsByTagName("METADATA").item(0).getAttributes().getLength();i++)
			{
				e1.setAttribute(DOM.getElementsByTagName("METADATA").item(0).getAttributes().item(i).getNodeName(),DOM.getElementsByTagName("METADATA").item(0).getAttributes().item(i).getNodeValue());
			}
			Element e2 = new Element(DOM.getElementsByTagName("INSTANCES").item(0).getNodeName());
			e1.addContent(e2);
			for(int i = 0 ; i < DOM.getElementsByTagName("INSTANCE").getLength();i++)
			{	Element e3 = new Element("INSTANCE");
				Node n1 = DOM.getElementsByTagName("INSTANCE").item(i);
				for(int j = 0 ; j < n1.getAttributes().getLength();j++)
				{
					Node n2 = n1.getAttributes().item(j);
					e3.setAttribute(n2.getNodeName(),n2.getNodeValue());
				}
				e2.addContent(e3);
			}
			
			Element e4 = new Element(DOM.getElementsByTagName("PROPERTIES").item(0).getNodeName());
			e1.addContent(e4);
								
			
			for(int i = 0 ; i < DOM.getElementsByTagName("PROPERTY").getLength();i++)
				
			{	Element e5 = new Element("PROPERTY");
				Node n1 = DOM.getElementsByTagName("PROPERTY").item(i);
				for(int j = 0 ; j < n1.getAttributes().getLength();j++)
				{
					Node n2 = n1.getAttributes().item(j);
					e5.setAttribute(n2.getNodeName(),n2.getNodeValue());
				
				}
				e4.addContent(e5);
			}
			
			
			Element e6 = new Element(DOM.getElementsByTagName("HIERARCHIES").item(0).getNodeName());
			e1.addContent(e6);
								
			
			for(int i = 0 ; i < DOM.getElementsByTagName("HIERARCHY").getLength();i++)
				
			{	Element e7 = new Element("HIERARCHY");
				Node n1 = DOM.getElementsByTagName("HIERARCHY").item(i);
				for(int j = 0 ; j < n1.getAttributes().getLength();j++)
				{
					Node n2 = n1.getAttributes().item(j);
					e7.setAttribute(n2.getNodeName(),n2.getNodeValue());
				
				}
				e6.addContent(e7);
			}
			
			Element e8 = new Element(DOM.getElementsByTagName("ACCESSES").item(0).getNodeName());
			e1.addContent(e8);
								
			
			for(int i = 0 ; i < DOM.getElementsByTagName("ACCESS").getLength();i++)
				
			{	Element e9 = new Element("ACCESS");
				Node n1 = DOM.getElementsByTagName("ACCESSES").item(i);
				for(int j = 0 ; j < n1.getAttributes().getLength();j++)
				{
					Node n2 = n1.getAttributes().item(j);
					e9.setAttribute(n2.getNodeName(),n2.getNodeValue());
				
				}
				e8.addContent(e9);
			}

			
			Element e10 = new Element(DOM.getElementsByTagName("BUSINESS_CLASSES").item(0).getNodeName());
			e1.addContent(e10);
								
			
			for(int i = 0 ; i < DOM.getElementsByTagName("BUSINESS_CLASS").getLength();i++)
				
			{	Element e11 = new Element("BUSINESS_CLASS");
				Node n1 = DOM.getElementsByTagName("BUSINESS_CLASSES").item(i);
				for(int j = 0 ; j < n1.getAttributes().getLength();j++)
				{
					Node n2 = n1.getAttributes().item(j);
					e11.setAttribute(n2.getNodeName(),n2.getNodeValue());
				
				}
				e10.addContent(e11);
			}
			
			this.dataTestXML=e1;
				System.out.println(jdomParser.returnXML(this.dataTestXML));
			
		} catch (ParserConfigurationException e) {
			
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (SAXException e) {
		
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (JDOMException e) {
		
			e.printStackTrace();
		}
	}
	
	public GetData(Element dataTestXML){
		this.dataTestXML=dataTestXML;
	} 
	
	public LinkedList<Instance> getInstances() throws SystemException, RemoteSystemException, CommunicationException{
		metaComClient m = new metaComClient(this.dataTestXML);
		LinkedList<Instance> instances = null;
		Element instancesElem = this.dataTestXML.getChild("INSTANCES");
		if (instancesElem!=null)
			instances = m.buildModel(instancesElem);
		else
			instances = new LinkedList<Instance>();	
		return instances;
	}
	
	public LinkedList<Access> getAccesses(String user, HashSet<String> roles) throws SystemException, RemoteSystemException, CommunicationException{
		metaComClient m = new metaComClient(this.dataTestXML);
		LinkedList<Access> accesses = null;
		Element accessesElem = this.dataTestXML.getChild("ACCESSES");
		if (accessesElem!=null)
			accesses = m.buildAccess(accessesElem,user,roles);
		else
			accesses = new LinkedList<Access>();
		return accesses;
	}
	
	public LinkedList<Properties> getProperties() throws SystemException, RemoteSystemException, CommunicationException{
		metaComClient m = new metaComClient(this.dataTestXML);
		LinkedList<Properties> properties = null;
		Element propertiesElem = this.dataTestXML.getChild("PROPERTIES");
		if (propertiesElem!=null)
			properties = m.buildProp(propertiesElem);
		else
			properties = new LinkedList<Properties>();
		return  properties;
	}
	
	
	public LinkedList<T_Herencias> getHierarchies() throws SystemException, RemoteSystemException, CommunicationException{
		metaComClient m = new metaComClient(this.dataTestXML);
		LinkedList<T_Herencias> hierarchies = null;
		Element hierarchiesElem = this.dataTestXML.getChild("HIERARCHIES");
		if (hierarchiesElem!=null)
			hierarchies = m.buildParents(hierarchiesElem);
		else
			hierarchies = new LinkedList<T_Herencias>();
		return  hierarchies;
	}
		
	
	public LinkedList<O_Datos_Attrib> getBusinessClasses() throws SystemException, RemoteSystemException, CommunicationException{
		metaComClient m = new metaComClient(this.dataTestXML);
		LinkedList<O_Datos_Attrib> businessClasses = null;
		Element businessClassesElem = this.dataTestXML.getChild("BUSINESS_CLASSES");
		if (businessClassesElem!=null)
			businessClasses = m.buildBusinessClass(businessClassesElem);
		else
			businessClasses = new LinkedList<O_Datos_Attrib>();
		return  businessClasses;
	}

	
	public void addDataToRuler(String nameFichData){
		
	}

}
