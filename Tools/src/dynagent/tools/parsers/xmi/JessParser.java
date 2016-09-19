/***
 * JessParser.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 * @description OWL2Jess Transformation based on XSLT Template
 */

package dynagent.tools.parsers.xmi;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class JessParser {
	public void parse(){
    	
    	TransformerFactory tFactory = TransformerFactory.newInstance();
    	Transformer transformer;
		try {
			
			String PROPERTIES_FILE = "ruleengine.properties";
			FileInputStream fis;
			fis = new FileInputStream(PROPERTIES_FILE);
			Properties properties = new Properties();
			properties.load(fis);
			fis.close();
			
			String xslFileName = properties.getProperty("XSLTPATH")+"/OWL2Jess.xsl";
			String input = properties.getProperty("OWLMODEL");
	    	String output = properties.getProperty("OWLMODEL")+".clp";
			
			transformer = tFactory.newTransformer(new StreamSource(xslFileName));
			transformer.transform(new StreamSource(input), new StreamResult(new FileOutputStream(output)));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
