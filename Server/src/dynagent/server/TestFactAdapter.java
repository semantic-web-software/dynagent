package dynagent.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import dynagent.common.exceptions.DataErrorException;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GestorsDBConstants;
import dynagent.server.services.FactsAdapter;

public class TestFactAdapter {
	
	public static void main (String [] args){
		FactoryConnectionDB fcdb = new FactoryConnectionDB(3, true, "127.0.0.1", GestorsDBConstants.mySQL);
		fcdb.setPort(3306);
		
		DataBaseMap dataBaseMap = new DataBaseMap(fcdb, false);
		SAXBuilder builder = new SAXBuilder();
		Document document;
		try {
			File file = new File("C:/pruebaXML.xml");
			document = builder.build(file);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		Document resultDocument;
		try {
			resultDocument = FactsAdapter.factsXMLToDataXML(dataBaseMap, document);
		} catch (DataErrorException e1) {
			e1.printStackTrace();
			return;
		} catch (DataConversionException e1) {
			e1.printStackTrace();
			return;
		}
		XMLOutputter xmlOutputter = new XMLOutputter("\t", true,"iso-8859-1");
		try {
			xmlOutputter.output(resultDocument, new BufferedWriter(new FileWriter(new File("C:/resultXML.xml"))));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
