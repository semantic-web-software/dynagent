package dynagent.tools.importers.access;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import dynagent.common.basicobjects.Access;

/**
 * *
 * 
 * @author Hassan Una clase que se encarga de leer y procesar el fichero XML de
 *         entrada, donde se definen los accesos y los permisos. Esta clase
 *         tiene 3 atributos: que son business,IP y accesos. Accesos es una
 *         lista que contiene todos los accesos defenidos en el fichero. Los
 *         elementos de esta clase son del tipo Acceso defenido en
 *         dynagent...data.dao
 * 
 */
public class AccessParser {

	String filename;

	LinkedList<Access> accesos;

	String business = "";

	String IP = "";

	public AccessParser(String filename) {
		super();
		this.filename = filename;
	}

	

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * Parsear el fichero lo procesa y guarda en los atributos de la clase los
	 * valores leidos, tanto el numero del negocio como la dirección de la base
	 * de datos.
	 * 
	 * @param f
	 *            el nombre del fichero a parsear.
	 * @return una lista con todas los accesos que se han leido en el fichero
	 *         xml.
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	public LinkedList<Access> parseFile(String f)  {
		setFilename(f);
		return parseFile();
	}

	/**
	 * realiza lo mismo que en la funcion anterior pero en este caso se coge el
	 * fichero que se ha puesto al crear el objeto.
	 * 
	 * @return una lista con todas los accesos que se han leido en el fichero
	 *         xml
	 * 
	 * TODO: Tener en cuenta que todos los datos leidos son String menos uno que
	 * es entero. Eso es porque en la clase Access, los atributos que nos hacen
	 * falta estan puestos como Integer.
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */

	public LinkedList<Access> parseFile() {
		LinkedList<Access> result = new LinkedList<Access>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Access acceso = new Access();

		try {
			builder = factory.newDocumentBuilder();
			Document DOM = builder.parse(new InputSource(new FileReader(
					new File(this.filename))));
			NodeList nodes = DOM.getChildNodes().item(0).getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeName().equals("business"))
					this.business = nodes.item(i).getAttributes().item(0)
							.getTextContent();
				else if (nodes.item(i).getNodeName().equals("dir"))
					this.IP = nodes.item(i).getAttributes().item(0)
							.getTextContent();
				else if (nodes.item(i).getAttributes() != null
						&& nodes.item(i).getAttributes().getLength() != 0) {

					for (int j = 0; j < nodes.item(i).getAttributes()
							.getLength(); j++) {
						String nomnodo = nodes.item(i).getAttributes().item(j)
								.getNodeName();
						String contnodo = nodes.item(i).getAttributes().item(j)
								.getTextContent();

						if (nomnodo.equals("property")) {
							acceso.setPROP(new Integer(contnodo));

						} else if (nomnodo.equals("dennied")) {
							acceso.setDENNIED(new Integer(contnodo));
						} else if (nomnodo.equals("task")) {
							acceso.setTASK(new Integer(contnodo));
						} else if (nomnodo.equals("user")) {
							acceso.setUSER(contnodo);
						} else if (nomnodo.equals("userrol")) {
							acceso.setUSERROL(new Integer(contnodo));
						} else if (nomnodo.equals("accesstype")) {
							acceso.setACCESSTYPE(new Integer(contnodo));
						} else if (nomnodo.equals("idto")) {
							acceso.setIDTO(new Integer(contnodo));
						} else if (nomnodo.equals("ido")) {
							acceso.setIDO(new Integer(contnodo));

						
						} else if (nomnodo.equals("value")) {
							acceso.setVALUE(contnodo);
						} else if (nomnodo.equals("valuecls")) {
							acceso.setVALUECLS(new Integer(contnodo));
						} 
					}
																	
					this.accesos = result;
					result.add(acceso);
				}
			}
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (SAXException ex) {
			ex.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public LinkedList<Access> getAccesos() {
		return accesos;
	}

	public void setAccesos(LinkedList<Access> accesos) {
		this.accesos = accesos;
	}

	public String getBusiness() {
		return business;
	}

	public void setBusiness(String business) {
		this.business = business;
	}

	public String getIP() {
		return IP;
	}

	public void setIP(String ip) {
		IP = ip;
	}

}
