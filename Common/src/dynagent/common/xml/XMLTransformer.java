package dynagent.common.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.SAXOutputter;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.JDOMResult;

import dynagent.common.exceptions.FileException;
import dynagent.common.utils.jdomParser;



public final class XMLTransformer {

	/** Mapa de los ficheros XSLT cacheados indexados por la ruta absoluta. */
	private static Map<String, Source> usedXSLT = new Hashtable<String, Source>();
	
	/**
	 * A partir del documento de datos dado, construye el documento XML
	 * transformado después de aplicarle la transformación XSLT
	 * 
	 * @return Documento con la transformación aplicada.
	 * @throws FileException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerConfigurationException
	 * @throws JDOMException
	 * @throws IOException 
	 */
	
	public static Document getTransformedDocument(File xmlFile,File xsltFile,String[] header) throws FileException, TransformerConfigurationException, TransformerFactoryConfigurationError, JDOMException, IOException{
		Document document = DocumentFactory.createDocument(xmlFile.getAbsolutePath(),header);
		
		return getTransformedDocument(document, xsltFile);
	}
	
	public static Document getTransformedDocument(Document document,File xsltFile) throws FileException, TransformerConfigurationException, TransformerFactoryConfigurationError, JDOMException, IOException{
		if (xsltFile == null){
			return document;
		}
//		XMLOutputter xmlOutputter = new XMLOutputter("\t", true);
//		xmlOutputter.output(document, System.out);
		String xsltPath = xsltFile.getCanonicalPath();
		Source xsltSource = usedXSLT.get(xsltPath);
		if (xsltSource == null){
			xsltSource = new  StreamSource(xsltFile);
			usedXSLT.put(xsltPath, xsltSource);
		}
		
		return getTransformedDocument(document, xsltSource);
	}
	
	public static Document getTransformedDocument(File xmlFile,InputStream xsltInput,String[] header) throws FileException, TransformerConfigurationException, TransformerFactoryConfigurationError, JDOMException, IOException{
		Document document = DocumentFactory.createDocument(xmlFile.getAbsolutePath(),header);
		
		if (xsltInput == null){
			return document;
		}
		XMLOutputter xmlOutputter = new XMLOutputter("\t", true);
		xmlOutputter.output(document, System.out);
		//String xsltPath = xsltFile.getCanonicalPath();
		Source xsltSource = null;//usedXSLT.get(xsltPath);
		if (xsltSource == null){
			xsltSource = new StreamSource(xsltInput);
			//usedXSLT.put(xsltPath, xsltSource);
		}
		
		document= getTransformedDocument(document, xsltSource);
		Element root=document.getRootElement();
		setNoderef(root);
		
		return document;
		
	}
	
	public static void setNoderef(Element root){
		ArrayList<Element> lista=jdomParser.elements(root,"*",true);
		HashMap<String,String> rdnMap=new HashMap<String,String>();
		int nodecount=1;
		for(Element node:lista){
			Iterator itrAtt=node.getAttributes().iterator();
			//boolean isIncremental=false;
			while(itrAtt.hasNext()){
				org.jdom.Attribute att=(org.jdom.Attribute)itrAtt.next();
				String value=att.getValue();
				if(value!=null){
					if(value.equals("[currenttime]")){
						att.setValue(""+System.currentTimeMillis()/1000);
					}
					//TODO, comprobar que la propiedad es realmente numerica, no solo por la expresion
					//if(value.matches("\\+\\d+(\\.\\d+)?")) isIncremental=true;
				}
			}
			String key=node.getAttribute("rdn")==null?null:node.getName()+":"+node.getAttribute("rdn");
			String mapedId=key==null?null:rdnMap.get(key);			
			
			String id=node.getAttributeValue("id_node");
			if(id==null){
				id=""+nodecount++;
				node.setAttribute("id_node",id);
				node.setAttribute("tableId",""+(-nodecount));
			}else{
				//el id podría ya venir fijado del xsl
				nodecount++;
			}
			if(mapedId!=null ){//&& !isIncremental){
				node.setAttribute("ref_node",mapedId);				
			}else{
				rdnMap.put(key, id);
			}
			
			if(node.getAttribute("rdn")==null && !node.getName().equals("objects")){
				//comando &idx& para generar rdn automatico esta motivado porque es necesario sabe el idto de la clase, y el importador xsl no lo sabe, asi que server lo hara
				node.setAttribute("rdn","&idx&");
			}
			
		}
	}
	
	public static Document getTransformedDocument(Document document,Source xsltSource) throws TransformerFactoryConfigurationError, TransformerConfigurationException, JDOMException, IOException {
				
		
		JDOMResult result = new JDOMResult();
		System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
		TransformerFactory transFact = TransformerFactory.newInstance();
		if (transFact.getFeature(SAXTransformerFactory.FEATURE)) {
			SAXTransformerFactory stf = (SAXTransformerFactory) transFact;
			TransformerHandler transHand = stf.newTransformerHandler(xsltSource);

			//System.out.print(jdomParser.returnXML(document));
			// result is a Result instance
			transHand.setResult(result);
			SAXOutputter saxOut = new SAXOutputter(transHand);
			// the 'jdomDoc' parameter is an instance of JDOM's
			// org.jdom.Document class. In contains the XML data
			saxOut.output(document);
		} else {
			System.err.println("SAXTransformerFactory is not supported");
		}
		Document resultDocument = result.getDocument();
		return resultDocument;
	}
	
	
}
