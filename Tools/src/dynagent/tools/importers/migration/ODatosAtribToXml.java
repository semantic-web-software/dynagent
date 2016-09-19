package dynagent.tools.importers.migration;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.IllegalDataException;

import dynagent.common.exceptions.DataErrorException;
import dynagent.common.knowledge.action;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.services.FactsAdapter;

public class ODatosAtribToXml {


	/**
	 * @param args
	 * @throws URISyntaxException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws DataConversionException 
	 * @throws DataErrorException 
	 */
	public static Document transform(String host, int port, String dbManager, int bns) throws URISyntaxException, SQLException, NamingException, DataErrorException, DataConversionException {
		
		FactoryConnectionDB fcdb = new FactoryConnectionDB(bns, true, host, dbManager);
		fcdb.setPort(port);
		// Modificamos la base de datos para que tenga las tablas con las que trabaja el nuevo modelo.
		URI scriptURI = ODatosAtribToXml.class.getResource("../../setup/ddbb/mysql/mySQLAdapter.sql").toURI();
		File scriptFile = new File(scriptURI);
		DBQueries.executeScript(fcdb, scriptFile);
		
		long beginTime = System.currentTimeMillis();
		DataBaseMap dataBaseMap = new DataBaseMap(fcdb, true); // Cambiar si se hace sobre una base de datos que ya tiene el nuevo modelo.

		long mapConstructTime = System.currentTimeMillis();
		Document factsDocument = constructFactsXML(dataBaseMap, fcdb);
		long timeAfterFactConstruct = System.currentTimeMillis();
		Document resultXML;
		
		resultXML = FactsAdapter.factsXMLToDataXML(dataBaseMap, factsDocument);
		
		factsDocument = null;
		long timeForTraduction = System.currentTimeMillis();
		long endTime = System.currentTimeMillis();
		
		printTimeResults(beginTime, mapConstructTime, timeAfterFactConstruct, timeForTraduction, endTime);
		
		return resultXML;
	}

	private static void printTimeResults(long beginTime, long mapConstructTime,
			long timeAfterFactConstruct, long timeForTraduction, long endTime) {
		float mapTime = (mapConstructTime - beginTime) / 1000.0F;
		float factTime = (timeAfterFactConstruct - mapConstructTime) / 1000.0F;
		float traductionTime = (timeForTraduction - timeAfterFactConstruct) / 1000.0F;
		float writeFileTime = (endTime - timeForTraduction) / 1000.0F;
		float totalTime = (endTime - beginTime) / 1000.0F;
		
		System.out.println("Se ha tardado " + totalTime + " segundos en todo el proceso");
		System.out.println("\tSe ha tardado " + mapTime + " segundos en construir el mapeo de la base de datos");
		System.out.println("\tSe ha tardado " + factTime + " segundos en traducir o_datos_atrib al XML de Facts");
		System.out.println("\tSe ha tardado " + traductionTime + " segundos en traducir el XML de Facts al nuevo modelo de XML");
		System.out.println("\tSe ha tardado " + writeFileTime + " en construir el fichero resultante del XML del nuevo modelo");
	}

	private static Document constructFactsXML(DataBaseMap dataBaseMap, FactoryConnectionDB fcdb) {
		Element factsRootElement = new Element("FACTS");
		Document factsDocument = new Document(factsRootElement);
		String sqlGetODatosAtrib = "SELECT DISTINCT ID_TO, ID_O, PROPERTY, VAL_NUM, VAL_TEXTO, VALUE_CLS, Q_MAX, DESTINATION FROM o_datos_atrib WHERE id_to not in (" +
				"SELECT idto FROM clases WHERE name in ('MÓDULO_NEGOCIO'))";

		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement = connectionDB.getBusinessConn().createStatement();
			statement.setFetchSize(100);
			resultSet = statement.executeQuery(sqlGetODatosAtrib);
			
			while (resultSet.next()) {
				Integer idto = resultSet.getInt(1);
				if (resultSet.wasNull()) {
					idto = null;
				}
				Integer ido = resultSet.getInt(2);
				if (resultSet.wasNull()) {
					ido = null;
				}
				Integer property = resultSet.getInt(3);
				if (resultSet.wasNull()) {
					property = null;
				}
				Integer valNum = resultSet.getInt(4);
				if (resultSet.wasNull()) {
					valNum = null;
				}
				String valText = resultSet.getString(5);
				if (resultSet.wasNull()) {
					valText = null;
				}
				Integer valueCls = resultSet.getInt(6);
				if (resultSet.wasNull()) {
					valueCls = null;
				}
				Double qMax = resultSet.getDouble(7);
				if (resultSet.wasNull()) {
					qMax = null;
				}
				if (valNum == null && valText == null && qMax == null){
					// Entrada que solo tiene system value y que de momento vamos a ignorar.
					continue;
				}
				String destination = resultSet.getString(8);
				Element newElement = constructFactElement(ido, idto, property, valNum, valText, valueCls, qMax, destination, dataBaseMap, fcdb);
				factsRootElement.addContent(newElement);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return factsDocument;
	}

	private static Element constructFactElement(Integer ido, Integer idto, Integer property, Integer valNum,
			String valText, Integer valueCls, Double qMax, String destination, DataBaseMap dataBaseMap, FactoryConnectionDB fcdb) {
		Element factElement = new Element("FACT");
		Element newFactElement = new Element("NEW_FACT");
		factElement.addContent(newFactElement);
		newFactElement.setAttribute(new Attribute("IDO", "-" + ido.toString()));
		newFactElement.setAttribute(new Attribute("IDTO", idto.toString()));
		newFactElement.setAttribute(new Attribute("PROP", property.toString()));
		newFactElement.setAttribute(new Attribute("VALUECLS", valueCls.toString()));
		newFactElement.setAttribute(new Attribute("ORDER", String.valueOf(action.NEW)));
		if (destination != null && ! destination.isEmpty()){
			newFactElement.setAttribute(new Attribute("DESTINATION_SYSTEM", destination));
		}
		if (qMax != null) {
			newFactElement.setAttribute(new Attribute("QMAX", qMax.toString()));
		} else {
			String content;
			if (valNum != null) {
				content = "-" + valNum.toString();
			} else {
				content = valText;
			}
			try {
				// Esto se hace porque las contraseñas no se pueden añadir tal y como vienen de base de datos, hay que
				// desencriptarlas
				newFactElement.addContent(content);
			} catch (IllegalDataException e) {
				GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
				String sqlDecrypt = "select " + generateSQL.getDecryptFunction("dynamicIntelligent", "'" + content + "'");
				
				ConnectionDB connectionDB = fcdb.createConnection(true);
				Statement statement = null;
				ResultSet resultSet = null;

				try {
					statement = connectionDB.getBusinessConn().createStatement();
					resultSet = statement.executeQuery(sqlDecrypt);
					while (resultSet.next()) {
						content = generateSQL.getDecryptData(resultSet, 1);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
				} catch (NamingException e1) {
					e1.printStackTrace();
				} finally {
					if (statement != null) {
						try {
							statement.close();
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
					}
					if (resultSet != null) {
						try {
							resultSet.close();
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
					}
				}

				newFactElement.addContent(content);
			}
		}
		return factElement;
	}

}
