/***
 * XMIParser.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 */

package dynagent.tools.parsers.xmi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMIParser {
	
	private Document document = null;
	
	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	/**
	 * parse
	 * @param inputdoc represents the path of the input document
	 */
	public void parse(String inputdoc) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder;
	    try {
	         builder = factory.newDocumentBuilder();
	         Document DOM = builder.parse(new InputSource(new FileReader(new File(inputdoc))));
	         setDocument(DOM);
	    } catch (ParserConfigurationException ex) {
	         ex.printStackTrace();
	    } catch (FileNotFoundException ex) {
	         ex.printStackTrace();
	    } catch (IOException ex) {
	         ex.printStackTrace();
	    } catch (SAXException ex) {
	        ex.printStackTrace();
	    } 
	    if(getDocument() != null)
	    	parseDoc();
	    else{
	    	System.out.println("[ERROR]: Document not found");
	    }
	}
	
	/***
	 * parseDoc
	 */
	public void parseDoc(){
		JenaObjectGen jenadoc = new JenaObjectGen();
		
		NodeList nc = document.getElementsByTagName("UML:Class");
				
		for(int i = 0; i < nc.getLength(); i++){
			Node n = nc.item(i);
			if(n.getAttributes() != null){
				if(n.getAttributes().getNamedItem("name") != null){
					JClass c = jenadoc.createClass();
					c.setName(n.getAttributes().getNamedItem("name").getTextContent());
					if(n.getFirstChild()!= null && n.getFirstChild().getNextSibling() != null){
						Node ns = n.getFirstChild().getNextSibling();
						if(ns.getNodeName().equals("UML:ModelElement.stereotype")){
							if(ns.getFirstChild()!= null && ns.getFirstChild().getNextSibling() != null){
								ns = ns.getFirstChild().getNextSibling();
								String id = ns.getAttributes().getNamedItem("xmi.idref").getTextContent();
								Node d = getStereotypeByID(id);
								if(d != null)
									c.setType(d.getAttributes().getNamedItem("name").getTextContent());
								else{
									System.out.println("[XMI][ERROR] idref not consistency");
									break;
								}
							}
						}
					}
					if(c.getType()!=null){
						if(c.getType().equals("owl"))
							jenadoc.getClasses().add(c);
						else if(c.getType().equals("role"))
							jenadoc.getRoles().add(c);
						else if(c.getType().equals("process"))
							jenadoc.getProcesses().add(c);	
					}else{
						System.out.println("[XMI][ERROR]: No stereotyped class");
					}
					
				}
			}
		}
		nc = document.getElementsByTagName("UML:Generalization");
		
		for(int i = 0; i < nc.getLength(); i++){
			Node n = nc.item(i);
			if(n.getFirstChild() != null && n.getFirstChild().getNextSibling() != null){
				Node ns = n.getFirstChild().getNextSibling();
				Subclass s = jenadoc.createSubclass();
				if(ns.getNodeName().equals("UML:Generalization.child")){
					if(ns.getFirstChild() != null && ns.getFirstChild().getNextSibling() != null){
						Node nss = ns.getFirstChild().getNextSibling();
						String idchild = nss.getAttributes().getNamedItem("xmi.idref").getTextContent();
						Node child = getNodeByID(idchild);
						s.setChild(child.getAttributes().getNamedItem("name").getTextContent());
					}
				}
				Node np = ns.getNextSibling().getNextSibling();
				if(np.getNodeName().equals("UML:Generalization.parent")){
					if(np.getFirstChild() != null && np.getFirstChild().getNextSibling() != null){
						Node npp = np.getFirstChild().getNextSibling();
						String idparent = npp.getAttributes().getNamedItem("xmi.idref").getTextContent();
						Node parent = getNodeByID(idparent);
						s.setParent(parent.getAttributes().getNamedItem("name").getTextContent());
					}
				}
				if(s.getChild() != null && s.getParent() != null)
					jenadoc.getSubclasses().add(s);
			}
		}
		
		nc = document.getElementsByTagName("UML:Association.connection");
		
		for(int i = 0; i < nc.getLength(); i++){
			Node n = nc.item(i);
			Property s = jenadoc.createProperty();
			if(n.getFirstChild() != null && 
			   n.getFirstChild().getNextSibling() != null &&
			   n.getFirstChild().getNextSibling().getFirstChild() != null &&
			   n.getFirstChild().getNextSibling().getFirstChild().getNextSibling() != null &&
			   n.getFirstChild().getNextSibling().getFirstChild().getNextSibling().getNextSibling() != null &&
			   n.getFirstChild().getNextSibling().getFirstChild().getNextSibling().getNextSibling().getNextSibling() != null
			   ){
				Node ns = n.getFirstChild().getNextSibling().getFirstChild().getNextSibling().getNextSibling().getNextSibling();
				if(ns.getNodeName().equals("UML:AssociationEnd.participant")){
					if(ns.getFirstChild() != null && ns.getFirstChild().getNextSibling() != null){
						Node npp = ns.getFirstChild().getNextSibling();
						String idchild = npp.getAttributes().getNamedItem("xmi.idref").getTextContent();
						Node child = getNodeByID(idchild);
						s.setRange(child.getAttributes().getNamedItem("name").getTextContent());
					}
				}
			}
			if(n.getFirstChild() != null && 
			   n.getFirstChild().getNextSibling() != null &&
			   n.getFirstChild().getNextSibling().getNextSibling() != null &&
			   n.getFirstChild().getNextSibling().getNextSibling().getNextSibling() != null &&
			   n.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getFirstChild() != null &&
			   n.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getNextSibling() != null &&
			   n.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getNextSibling().getNextSibling() != null &&
			   n.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getNextSibling().getNextSibling().getNextSibling() != null
			   ){
				Node ns = n.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getNextSibling().getNextSibling().getNextSibling();
				if(ns.getNodeName().equals("UML:AssociationEnd.participant")){
					if(ns.getFirstChild() != null && ns.getFirstChild().getNextSibling() != null){
						Node npp = ns.getFirstChild().getNextSibling();
						String idparent = npp.getAttributes().getNamedItem("xmi.idref").getTextContent();
						Node parent = getNodeByID(idparent);
						s.setDomain(parent.getAttributes().getNamedItem("name").getTextContent());
					}
				}
				if(s.getRange() != null && s.getDomain()!= null)
					jenadoc.getProperties().add(s);
			}
		}		
		jenadoc.generate();
	}
	
	/***
	 * 
	 */
	public Node getNodeByID(String id){
		Node n = null;
		boolean flag = false;
		NodeList nc = document.getElementsByTagName("UML:Class");
		for(int i = 0; i < nc.getLength() && flag == false; i++){
			n = nc.item(i);
			if(n.getAttributes() != null){
				if(n.getAttributes().getNamedItem("xmi.id") != null){
					if(n.getAttributes().getNamedItem("xmi.id").getTextContent().equals(id)){
						flag = true;
					}
				}
			}
		}
		if(flag == false)
			n = null;
		return n;
	}
	
	/***
	 * 
	 */
	public Node getStereotypeByID(String id){
		Node n = null;
		boolean flag = false;
		NodeList nc = document.getElementsByTagName("UML:Stereotype");
		for(int i = 0; i < nc.getLength() && flag == false; i++){
			n = nc.item(i);
			if(n.getAttributes() != null){
				if(n.getAttributes().getNamedItem("xmi.id") != null){
					if(n.getAttributes().getNamedItem("xmi.id").getTextContent().equals(id)){
						flag = true;
					}
				}
			}
		}
		if(flag == false)
			n = null;
		return n;
	}
}
