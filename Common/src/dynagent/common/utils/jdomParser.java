// 14-10-02 V1 JOB Añado findElementByAt
package dynagent.common.utils;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import dynagent.common.exceptions.DataErrorException;

public class jdomParser extends Object implements Serializable {
	HashMap atributos = new HashMap();

	String name;

	String doc = null;

	int end;

	int nameEnd, nameBegin, rootEnd = 0;

	public jdomParser(Element root) throws DataErrorException {
		try {
			doc = returnXML(root);
			build();
		} catch (JDOMException e) {
			throw new DataErrorException(e.getMessage());
		}
	}

	public Object clone() {
		try {
			return new jdomParser(returnXML());
		} catch (DataErrorException e) {
			System.out.println("CLONE JDOM PARSER:" + e.getMessage());
			return null;
		}
	}

	public String toHeaderString() {
		int endDoc = doc.length() - 1;
		boolean hasContent = endDoc > rootEnd + 5;
		if (!hasContent)
			return returnXML();
		//se supone que rootEnd apunta al caracter '>'
		return doc.substring(0, rootEnd) + "/>";
	}

	public void addContent(String content) {
		int endDoc = doc.length() - 1;
		boolean hasContent = endDoc > rootEnd + 5;
		if (!hasContent)
			doc = doc.substring(0, rootEnd) + ">" + content + "</" + name + ">";
		else
			doc = doc.substring(0, rootEnd + 1) + content
					+ doc.substring(rootEnd + 1);

	}

	private void build() throws DataErrorException {
		if (doc == null || doc.length() < 5)
			throw new DataErrorException(
					"JDOM PARSER:Cadena JDOM de longitud incorrecta");
		int begin = doc.indexOf('<');

		if (doc.substring(begin + 1, begin + 1 + 4).equals("?xml"))
			begin = doc.indexOf('<', begin + 1);
		int tmpEnd = doc.indexOf('>', begin + 1);
		if (tmpEnd == -1)
			throw new DataErrorException(
					"JDOM PARSER:Cadena JDOM corrupta, no se encontro el final \">\":"
							+ doc);
		end = doc.indexOf(' ', begin + 1);
		if (end >= tmpEnd || end == -1)
			end = Math.min(tmpEnd, doc.indexOf('/', begin + 1));

		name = doc.substring(begin + 1, end);
		nameEnd = end;
		nameBegin = begin + 1;
		String label, content;
		begin = end + 1;
		if (begin >= tmpEnd - 1) {
			System.out.println("NODO SIN ATRIBUTOS:" + doc);
			return;
		}
		while (true) {
			end = doc.indexOf('=', begin);
			if (end == -1 || end >= tmpEnd)
				break;

			label = doc.substring(begin, end);
			begin = end + 2; //me salto el = y la comilla
			end = doc.indexOf('"', begin);
			if (end == -1 || end >= tmpEnd)
				throw new DataErrorException(
						"JDOM PARSER:Cadena JDOM corrupta, "
								+ "no se encontro el final del atributo "
								+ label);
			content = doc.substring(begin, end);

			atributos.put(label, content);
			//System.out.println("PARSED AT:"+label+" VAL:"+content);
			begin = end + 2; //me salto el espacio tras la comilla
			rootEnd = end + 1;
		}
	}

	public jdomParser(String d) throws DataErrorException {
		this.doc = d;
		build();
	}

	public String getAttributeValue(String atributo) {
		if (!atributos.containsKey(atributo))
			return null;
		Object res = atributos.get(atributo);
		if (res != null)
			return (String) res;
		return null;
	}

	public static void print(String msg, Element nodo) {
		if (nodo == null) {
			System.out.println(msg + " PRINT JDOM ERROR. NODO NULO");
			return;
		}
		try {
			System.out.println(msg + returnXML(nodo));
		} catch (Exception e) {
			System.out.println(msg + " PRINT JDOM ERROR. EXCEPCION:"
					+ e.getMessage());
		}

	}

	public ArrayList getChildren(String label) throws DataErrorException {
		ArrayList res = new ArrayList();
		int end = rootEnd;
		while (true) {
			int begin = doc.indexOf('<' + label, end);
			if (begin < 0)
				break;
			end = doc.indexOf('>', begin) + 1;
			if (end <= 0)
				throw new DataErrorException(
						"JDOM getChildren, no se encontro el final '>':" + doc);

			jdomParser jd = new jdomParser(doc.substring(begin, end));
			res.add(jd);
		}
		return res;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		doc = doc.substring(0, nameBegin) + name + doc.substring(nameEnd);
		int oldNameLength = nameEnd - nameBegin;
		if (doc.length() > rootEnd + 10) { //tiene hijos
			int indexCloseRoot = doc.indexOf("/" + this.name, rootEnd);
			doc = doc.substring(0, indexCloseRoot + 1) + name
					+ doc.substring(indexCloseRoot + 1 + oldNameLength);
		}
		nameEnd = nameBegin + name.length();
		rootEnd = rootEnd + name.length() - oldNameLength;
		this.name = name;
	}

	public String returnXML() {
		return doc;
	}

	public static String buildMultivalue(Object[] lista) {
		String res = "";
		for (int i = 0; i < lista.length; i++) {
			String content = lista[i].toString().replaceAll(";", ";;");
			if (res.length() > 0)
				res += ";";
			res += content;
		}
		return res;
	}

	public static Object[] parseMultivalue(String val) {
		if (val == null)
			return null;
		int begin = 0;
		int end = val.indexOf(";");
		ArrayList lista = new ArrayList();

		while (end != -1 && end < val.length() - 1) {
			if (val.charAt(end + 1) == ';') {
				if (end + 1 >= val.length() - 1)
					break;
				val = val.substring(begin, end + 1) + val.substring(end + 2);
				end = val.indexOf(";", end + 1);
				continue;
			}
			lista.add(val.substring(begin, end));
			begin = end + 1;
			end = val.indexOf(";", begin);
		}
		lista.add(val.substring(begin));
		return lista.toArray();
	}

	public void setAttribute(String at, String val) {
		int indAt = doc.indexOf(" " + at + "=") + 1;
		if (indAt < rootEnd && indAt > 0) {
			int ini = doc.indexOf("\"", indAt);
			int end = doc.indexOf("\"", ini + 1);
			int oldAtLength = end - ini - 1;
			doc = doc.substring(0, ini + 1) + val + doc.substring(end);
			rootEnd = rootEnd + val.length() - oldAtLength;
		} else {
			doc = doc.substring(0, nameEnd) + " " + at + "=\"" + val + "\""
					+ doc.substring(nameEnd);
			rootEnd += 4 + at.length() + val.length();
		}
		atributos.put(at, val);
	}

	public static String returnXML(org.jdom.Document doc) throws JDOMException {
		String res = "error";
		XMLOutputter outputter = new XMLOutputter("  ", true, "UTF-8");
		res = outputter.outputString(doc);
		return res;
	}

	public static String returnNodeXML(org.jdom.Element doc) throws JDOMException {
		String res = "error";
		XMLOutputter outputter = new XMLOutputter("  ", true, "UTF-8");
		res = outputter.outputString(jdomParser.cloneNode(doc));
		return res;
	}
	public static String returnXML(org.jdom.Element doc) throws JDOMException {
		String res = "error";
		XMLOutputter outputter = new XMLOutputter("  ", true, "UTF-8");
		res = outputter.outputString(doc);
		return res;
	}
	public static String returnXML(List content,boolean newlines) throws JDOMException {
		String res = "error";
		XMLOutputter outputter = new XMLOutputter("  ", newlines, "UTF-8");
		outputter.setTextNormalize(newlines?false:true);
		res = outputter.outputString(content);
		return res;
	}

	public static Document readXML(String str) throws JDOMException {
		org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
		StringReader sr = new StringReader(str);
		return builder.build(sr);
	}
	public static Document readXML(File f) throws JDOMException {
		org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
		return builder.build(f);
	}
	
	public static String readXMLToString(File file) throws JDOMException{
		StringWriter stringWriter = new StringWriter();
		XMLOutputter xmlOutputter = new XMLOutputter("\t", true, "UTF-8");
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(file);
		
		try {
			xmlOutputter.output(document, stringWriter);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return stringWriter.toString();
	}
	
	public static void writeXML_ToFile(String ruta,org.jdom.Document doc) throws IOException{			
		XMLOutputter xmlOutput = new XMLOutputter("\t", true, "UTF-8");	 		
		FileOutputStream fileOutput = new FileOutputStream(new File(ruta));
		xmlOutput.output(doc, fileOutput);		
	}

	//V1:ADD
	
	public static org.jdom.Element findElementByAtAndTextAt(org.jdom.Element root,
			String at, String[] vals, String at2, String text, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getAttributeValue(at) != null
					&& (child.getAttributeValue(at2) != null && child.getAttributeValue(at2).equals(text) 
							|| child.getText()!=null && child.getText().equals(text)))
				for (int i=0;i<vals.length;i++) {
					if (child.getAttributeValue(at).equals(vals[i])) 
						return child;
				}
			if (profundizar) {
				Element res = findElementByAtAndTextAt(child, at, vals, at2, text, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}
	public static org.jdom.Element findElementByAtsAndTextAt(org.jdom.Element root,
			String at, String[] vals, String[] ats, HashSet<String> vals2, String at3, String text, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getAttributeValue(at) != null
					&& (child.getAttributeValue(at3) != null && child.getAttributeValue(at3).equals(text) 
							|| child.getText()!=null && child.getText().equals(text))) {
				for (int i=0;i<vals.length;i++) {
					if (child.getAttributeValue(at).equals(vals[i])) {
						for (int j=0;j<ats.length;j++) {
							if (child.getAttributeValue(ats[j])!=null) {
								Iterator it = vals2.iterator();
								while (it.hasNext()) {
									if (child.getAttributeValue(ats[j]).equals((String)it.next()))
										return child;
								}
							}
						}
					}
				}
			}
			if (profundizar) {
				Element res = findElementByAtsAndTextAt(child, at, vals, ats, vals2, at3, text, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}
	
	public static org.jdom.Element findElementByAt(org.jdom.Element root,
			String name, String at, String val, boolean profundizar) {
		if (name == null)
			return null;
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if ((name.equals("*") || child.getName().equals(name))
					&& val != null && child.getAttributeValue(at) != null
					&& child.getAttributeValue(at).equals(val))

				return child;
			if (profundizar) {
				Element res = findElementByAt(child, name, at, val, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}
	
	public static org.jdom.Element findElementByAt(org.jdom.Element root,
			String at, String val, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (val != null && child.getAttributeValue(at) != null
					&& child.getAttributeValue(at).equals(val))
				return child;
			if (profundizar) {
				Element res = findElementByAt(child, at, val, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}

	public static Element findElementByAt(org.jdom.Element root,
			String at, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getAttributeValue(at) != null)
				return child;
			if (profundizar) {
				Element res = findElementByAt(child, at, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}
	
	public static org.jdom.Element findElementByAtExcluded(org.jdom.Element root,
			String at, String val, String excluded, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getName().equals(excluded)) {
				Element res = findElementByAtExcluded(child, at, val, excluded, true);
				if (res != null)
					return res;
			} else {
				if (val != null && child.getAttributeValue(at) != null
						&& child.getAttributeValue(at).equals(val))
					return child;
				if (profundizar) {
					Element res = findElementByAtExcluded(child, at, val, excluded, true);
					if (res != null)
						return res;
				}
			}
		}
		return null;
	}
	
	public static org.jdom.Element findElementByAt(org.jdom.Element root,
			String name, String at, String val, boolean scanRoot,
			boolean profundizar) {
		if (name == null)
			return null;
		if (scanRoot && (name.equals("*") || root.getName().equals(name))
				&& val != null && root.getAttributeValue(at) != null
				&& root.getAttributeValue(at).equals(val))

			return root;

		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if ((name.equals("*") || child.getName().equals(name))
					&& val != null && child.getAttributeValue(at) != null
					&& child.getAttributeValue(at).equals(val))

				return child;
			if (profundizar) {
				Element res = findElementByAt(child, name, at, val, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}

	public static org.jdom.Element findElementByAt(org.jdom.Element root,
			String[] names, String at, String val, boolean scanRoot,
			boolean profundizar) {
		if (names == null || names.length == 0)
			return null;

		for (int i = 0; i < names.length; i++)
			if (scanRoot
					&& (names[i].equals("*") || root.getName().equals(names[i]))
					&& val != null && root.getAttributeValue(at) != null
					&& root.getAttributeValue(at).equals(val))
				return root;

		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			for (int i = 0; i < names.length; i++)
				if ((names[i].equals("*") || child.getName().equals(names[i]))
						&& val != null && child.getAttributeValue(at) != null
						&& child.getAttributeValue(at).equals(val))
					return child;
			if (profundizar) {
				Element res = findElementByAt(child, names, at, val, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}
	public static org.jdom.Element findElementByAt(org.jdom.Element root,
			String[] names, String at, String val, boolean profundizar) {
		if (names == null)
			return null;
		if (names.length == 0)
			return null;
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			for (int i = 0; i < names.length; i++)
				if ((names[i].equals("*") || child.getName().equals(names[i]))
						&& val != null && child.getAttributeValue(at) != null
						&& child.getAttributeValue(at).equals(val))
					return child;
			if (profundizar) {
				Element res = findElementByAt(child, names, at, val, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}

	public static org.jdom.Element firstElementWithAt(org.jdom.Element root,
			String at, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getAttributeValue(at) != null)
				return child;
			if (profundizar) {
				Element res = firstElementWithAt(child, at, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}

	public static ArrayList elementsWithAt(org.jdom.Element root, String at, String val,
			boolean profundizar) {
		ArrayList res = new ArrayList();
		if (root == null)
			return res;
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (StringUtils.equals(child.getAttributeValue(at),val))
				res.add(child);
			if (profundizar) {
				res.addAll((Collection) elementsWithAt(child, at, val, true));
			}
		}
		return res;
	}
	
	public static ArrayList elementsWithAt(org.jdom.Element root, String at,
			boolean profundizar) {
		ArrayList res = new ArrayList();
		if (root == null)
			return res;
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getAttributeValue(at) != null)
				res.add(child);
			if (profundizar) {
				res.addAll((Collection) elementsWithAt(child, at, true));
			}
		}
		return res;
	}

	public static ArrayList elements(org.jdom.Element root, String name,
			boolean profundizar) {
		ArrayList res = new ArrayList();
		if (root == null)
			return res;
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getName().equals(name)||name.equals("*"))
				res.add(child);
			if (profundizar) {
				res.addAll((Collection) elements(child, name, true));
			}
		}
		return res;
	}

	public static Element element(org.jdom.Element root, String name,
			boolean profundizar) {
		if (root == null)
			return null;
		Element res = null;
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getName().equals(name))
				return child;
			if (profundizar) {
				res = element(child, name, true);
				if (res != null)
					return res;
			}
		}
		return res;
	}

	public static Element element(org.jdom.Element root, String name,
			boolean scanRoot, boolean profundizar) {
		if (root == null)
			return null;
		if (scanRoot && root.getName().equals(name))
			return root;
		Element res = null;
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getName().equals(name))
				return child;
			if (profundizar) {
				res = element(child, name, true);
				if (res != null)
					return res;
			}
		}
		return res;
	}
	
	public static void removeNode(Element root, String name, String at, 
			ArrayList<String> val, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getName().equals(name)
					&& child.getAttributeValue(at) != null
					&& val.contains(child.getAttributeValue(at))) {
				child.detach();
			}
			if (profundizar) 
				removeNode(child, name, at, val, true);
		}
	}

	public static void indexaNodesPorIntAt(HashMap res, org.jdom.Element root,
			String name, String at, boolean profundizar) {
		if (root == null)
			return;
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getName().equals(name)
					&& child.getAttributeValue(at) != null)
				res.put(new Integer(child.getAttributeValue(at)), child);
			if (profundizar) {
				indexaNodesPorIntAt(res, child, name, at, true);
			}
		}
	}

	public static org.jdom.Element findElementSinAt(org.jdom.Element root,
			String name, String at, boolean profundizar) {
		if (name == null)
			return null;
		Iterator itr = root.getChildren(name).iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getAttributeValue(at) == null)
				return child;
			if (profundizar) {
				Element res = findElementSinAt(child, name, at, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}

	public static org.jdom.Element findElementByAt(org.jdom.Element root,
			String name, String at1, String val1, String at2, String val2,
			boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getAttributeValue(at1) != null
					&& child.getAttributeValue(at2) != null
					&& child.getAttributeValue(at1).equals(val1)
					&& child.getAttributeValue(at2).equals(val2)
					&& (name.equals("*") || child.getName().equals(name)))
				return child;
			if (profundizar) {
				Element res = findElementByAt(child, name, at1, val1, at2, val2, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}

	public static org.jdom.Element findElementByAtValAtNull(org.jdom.Element root,
			String name, String at1, String val1, String at2,
			boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getAttributeValue(at1) != null
					&& child.getAttributeValue(at1).equals(val1)
					&& child.getAttributeValue(at2)==null
					&& (name.equals("*") || child.getName().equals(name)))
				return child;
			if (profundizar) {
				Element res = findElementByAtValAtNull(child, name, at1, val1, at2, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}
	public static org.jdom.Element findElementByAtValAtsNotNull(org.jdom.Element root,
			String name, String at1, String val1, String[] ats, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getAttributeValue(at1) != null && child.getAttributeValue(at1).equals(val1)
					&& (name.equals("*") || child.getName().equals(name))) {
				for (int i=0;i<ats.length;i++) {
					if (child.getAttributeValue(ats[i])!=null)
						return child;
				}
			}
			if (profundizar) {
				Element res = findElementByAtValAtsNotNull(child, name, at1, val1, ats, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}
	public static org.jdom.Element findElementByAtValAndTextAtNotNull(org.jdom.Element root,
			String name, String at1, String val1, String at2, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if ((child.getAttributeValue(at1) != null && child.getAttributeValue(at1).equals(val1) || 
					child.getText()!=null && child.getText().equals(val1))
					&& child.getAttributeValue(at2)!=null
					&& (name.equals("*") || child.getName().equals(name)))
				return child;
			if (profundizar) {
				Element res = findElementByAtValAndTextAtNotNull(child, name, at1, val1, at2, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}
	public static ArrayList findElementsByAt(org.jdom.Element root,
			String name, String at1, String val1, String at2, String val2,
			boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		ArrayList result = new ArrayList();

		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getAttributeValue(at1) != null
					&& child.getAttributeValue(at2) != null
					&& child.getAttributeValue(at1).equals(val1)
					&& child.getAttributeValue(at2).equals(val2)
					&& child.getName().equals(name))
				result.add(child);
			if (profundizar) {
				result.addAll((Collection) findElementsByAt(child, name, at1, val1, at2, val2, true));
			}
		}
		return result;
	}
	
	public static ArrayList findElementsByAt(org.jdom.Element root,
			String name, String at, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		ArrayList result = new ArrayList();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getAttributeValue(at) != null
					&& child.getName().equals(name))
				result.add(child);
			if (profundizar) {
				result.addAll((Collection) findElementsByAt(child, name, at, true));
			}
		}
		return result;
	}
	
	public static ArrayList findElementsByAtOrText(org.jdom.Element root,
			String at, String value, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		ArrayList result = new ArrayList();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (value!=null) {
				if (child.getAttributeValue(at) != null
						&& child.getAttributeValue(at).equals(value))
					result.add(child);
				else if (child.getText().equals(value))
					result.add(child);
			}
			if (profundizar) {
				result.addAll((Collection) findElementsByAtOrText(child, at, value, true));
			}
		}
		return result;
	}
	
	public static Element findElementByText(org.jdom.Element root,
			String name, String text, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (text != null && name!=null 
					&& child.getName().equals(name) && child.getText().equals(text))
				return child;
			if (profundizar) {
				Element res= findElementByText(child, name, text, true);
				if(res!=null) return res;
			}
		}
		return null;
		
	}
	public static ArrayList<Element> findElementsByText(org.jdom.Element root,
			String name, String text, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		ArrayList<Element> result = new ArrayList<Element>();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (text != null && name!=null 
					&& child.getName().equals(name) && child.getText().equals(text))
				result.add(child);
			if (profundizar) {
				result.addAll(findElementsByText(child, name, text, true));
			}
		}
		return result;
	}
	public static ArrayList<Element> findElementsByName(org.jdom.Element root,
			String name, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		ArrayList<Element> result = new ArrayList<Element>();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (name!=null && child.getName().equals(name))
				result.add(child);
			if (profundizar) {
				result.addAll(findElementsByName(child, name, true));
			}
		}
		return result;
	}
	public static ArrayList<Element> findElementsByContainsText(org.jdom.Element root,
			String name, String text, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		ArrayList<Element> result = new ArrayList<Element>();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (text != null && name!=null 
					&& child.getName().equals(name) && child.getText().contains(text))
				result.add(child);
			if (profundizar) {
				result.addAll(findElementsByText(child, name, text, true));
			}
		}
		return result;
	}
	
	public static ArrayList findElementsByAt(org.jdom.Element root,
			String[] name, String at, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		ArrayList result = new ArrayList();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			for (int i = 0; i < name.length; i++)
				if (child.getAttributeValue(at) != null
						&& child.getName().equals(name[i])) {
					result.add(child);
					break;
				}
			if (profundizar) {
				result.addAll((Collection) findElementsByAt(child, name, at, true));
			}
		}
		return result;
	}

	public static ArrayList findElementsByAt(org.jdom.Element root,
			String name, String at, String val, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		ArrayList result = new ArrayList();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getAttributeValue(at) != null
					&& child.getAttributeValue(at).equals(val)
					&& child.getName().equals(name))
				result.add(child);
			if (profundizar) {
				result.addAll((Collection) findElementsByAt(child, name, at, val, true));
			}
		}
		return result;
	}
	public static ArrayList findElementsByContainsAt(org.jdom.Element root,
			String name, String at, String val, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		ArrayList result = new ArrayList();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getAttributeValue(at) != null
					&& child.getAttributeValue(at).contains(val)
					&& child.getName().equals(name))
				result.add(child);
			if (profundizar) {
				result.addAll((Collection) findElementsByContainsAt(child, name, at, val, true));
			}
		}
		return result;
	}

	public static ArrayList findElementsWithParam(org.jdom.Element root, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		ArrayList result = new ArrayList();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getName().equals(QueryConstants.CLASS) && StringUtils.equals(child.getAttributeValue(QueryConstants.ID_O),"(VALUE)")
			|| child.getName().equals(QueryConstants.WHERE) && 
				(StringUtils.equals(child.getAttributeValue(QueryConstants.VAL_MIN),"(VALUE)") || 
						StringUtils.equals(child.getAttributeValue(QueryConstants.VALUE),"(VALUE)") ||
						child.getText()!=null && child.getText().equals("(VALUE)") )
			|| child.getName().equals(QueryConstants.ATTRIBUTE) && child.getText()!=null && child.getText().equals("(VALUE)") )
				result.add(child);
			if (profundizar) {
				result.addAll((Collection) findElementsWithParam(child, true));
			}
		}
		return result;
	}
	
	public static ArrayList findElementsByAt(org.jdom.Element root,
			String name, String[] ats, String val, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		ArrayList result = new ArrayList();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getName().equals(name)) {
				for (int j=0;j<ats.length;j++)
					if (child.getAttributeValue(ats[j]) != null
							&& child.getAttributeValue(ats[j]).equals(val)) {
						result.add(child);
						break;
					}
			}
			if (profundizar) {
				result.addAll((Collection) findElementsByAt(child, name, ats, val, true));
			}
		}
		return result;
	}
	
	public static ArrayList findElementsByAt(org.jdom.Element root,
			String name, String at, ArrayList<String> val, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		ArrayList result = new ArrayList();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getAttributeValue(at) != null
					&& val.contains(child.getAttributeValue(at))
					&& child.getName().equals(name))
				result.add(child);
			if (profundizar) {
				result.addAll((Collection) findElementsByAt(child, name, at, val, true));
			}
		}
		return result;
	}
	public static HashMap<String,Element> findElementsByAtHM(org.jdom.Element root,
			String name, String at, ArrayList<String> val, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		HashMap<String,Element> result = new HashMap<String,Element>();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getAttributeValue(at) != null
					&& val.contains(child.getAttributeValue(at))
					&& child.getName().equals(name))
				result.put(child.getAttributeValue(at),child);
			if (profundizar) {
				result.putAll(findElementsByAtHM(child, name, at, val, true));
			}
		}
		return result;
	}
	/*public static ArrayList findElementsByAt(org.jdom.Element root,
			String[] names, String at, ArrayList<String> vals, boolean scanRoot,
			boolean profundizar) {
		ArrayList result = new ArrayList();
		if (names == null || names.length == 0)
			return result;

		for (int i = 0; i < names.length; i++)
			if (scanRoot
					&& (names[i].equals("*") || root.getName().equals(names[i]))
					&& vals != null && root.getAttributeValue(at) != null
					&& vals.contains(root.getAttributeValue(at)))
				result.add(root);

		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			for (int i = 0; i < names.length; i++)
				if ((names[i].equals("*") || child.getName().equals(names[i]))
						&& vals != null && child.getAttributeValue(at) != null
						&& vals.contains(child.getAttributeValue(at)))
					result.add(child);
			if (profundizar) {
				result.addAll((Collection)findElementsByAt(child, names, at, vals, true));
			}
		}
		return result;
	}*/
	public static ArrayList findElementsByAt(org.jdom.Element root,
			String[] names, String at, ArrayList<String> vals, boolean profundizar) {
		ArrayList result = new ArrayList();
		if (names == null)
			return result;
		if (names.length == 0)
			return result;
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			for (int i = 0; i < names.length; i++)
				if ((names[i].equals("*") || child.getName().equals(names[i]))
						&& vals != null && child.getAttributeValue(at) != null
						&& vals.contains(child.getAttributeValue(at)))
					result.add(child);
			if (profundizar) {
				result.addAll((Collection)findElementsByAt(child, names, at, vals, true));
			}
		}
		return result;
	}

	public static ArrayList findElementsHasAtsOrText(org.jdom.Element root,
			String[] names, String[] ats, String value, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		ArrayList result = new ArrayList();
		while (itr.hasNext()) {
			Element child = (Element) itr.next();
			for (int j=0;j<names.length;j++) {
				if (child.getName().equals(names[j])) {
					if (child.getText()!=null && child.getText().length()>0 && child.getText().contains(value))
						result.add(child);
					else {
						for (int i=0;i<ats.length;i++) {
							if (child.getAttributeValue(ats[i])!=null && child.getAttributeValue(ats[i]).contains(value)) {
								result.add(child);
								break;
							}
						}
					}
					break;
				}
			}
			if (profundizar)
				result.addAll((Collection) findElementsHasAtsOrText(child, names, ats, value, true));
		}
		return result;
	}
	
	public static ArrayList findElementsByAts(org.jdom.Element root,
			String[] names, List<Attribute> ats, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		ArrayList result = new ArrayList();
		while (itr.hasNext()) {
			Element child = (Element) itr.next();
			String name = child.getName();
			boolean continuar = false;
			for (int i=0;i<names.length;i++) {
				if (names[i].equals(name)) {
					continuar = true;
					break;
				}
			}
			if (continuar) {
				boolean allAtrib = allAtribs(ats, child);
				if (allAtrib)
					result.add(child);
				if (profundizar) {
					result.addAll((Collection) findElementsByAts(child, names, ats, true));
				}
			}
		}
		return result;
	}
	
	public static Element cloneNode(Element nodo) {
		Element nodoClone = (Element)nodo.clone();
		nodoClone.detach();
		nodoClone.removeChildren();
		
		return nodoClone;
	}
	
	public static Element cloneTree(Element nodo) {
		Element nodoClone = (Element)nodo.clone();
		return nodoClone;
	}
	
	public static boolean allAtribs(List<Attribute> ats, Element child) {
		boolean allAtrib = true;
		for (int i=0;i<ats.size();i++) {
			Attribute atributo = (Attribute)ats.get(i);
			String nameAtrib = atributo.getName();
			String value = child.getAttributeValue(nameAtrib);
			if (!(value!=null && (nameAtrib.equals(QueryConstants.ID) || StringUtils.equals(value, atributo.getValue())))) {
				allAtrib = false;
				break;
			}
		}
		return allAtrib;
	}
	public static ArrayList findElementsByAt(org.jdom.Element root,
			String[] name, String at, String val, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		ArrayList result = new ArrayList();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			for (int i = 0; i < name.length; i++)
				if (child.getAttributeValue(at) != null
						&& child.getAttributeValue(at).equals(val)
						&& child.getName().equals(name[i])) {
					result.add(child);
					break;
				}
			if (profundizar) {
				result.addAll((Collection) findElementsByAt(child, name, at,
						val, true));
			}
		}
		return result;
	}
	
	public static ArrayList findElementsByContainsAt(org.jdom.Element root,
			String[] name, String[] at, String valContains, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		ArrayList result = new ArrayList();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			for (int i = 0; i < name.length; i++) {
				if (child.getName().equals(name[i])) {
					for (int j = 0; j < at.length; j++)
						if (child.getAttributeValue(at[j]) != null
								&& child.getAttributeValue(at[j]).contains(valContains)) {
							result.add(child);
							break;
						}
					break;
				}
			}
			if (profundizar) {
				result.addAll((Collection) findElementsByContainsAt(child, name, at,
						valContains, true));
			}
		}
		return result;
	}

	public static org.jdom.Element findFirstOf(org.jdom.Element root,
			String[] names, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			for (int i = 0; i < names.length; i++) {
				if (child.getName().equals(names[i]))
					return child;
			}
			if (profundizar) {
				Element res = findFirstOf(child, names, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}
	
	public static org.jdom.Element findFirstOf(org.jdom.Element root,
			String name, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getName().equals(name))
				return child;
			if (profundizar) {
				Element res = findFirstOf(child, name, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}
//	(hijo, QueryConstants.ATTRIBUTE, QueryConstants.WHERE, QueryConstants.ID_O);
//	name con atrib y where, name4 con id_o
	public static org.jdom.Element findAttributeSelOrWhereOrIdo(org.jdom.Element root, ArrayList<String> viewSelect, 
			boolean actual, boolean profundizar) {
		if (actual) {
			Element rootC = find(root, viewSelect);
			if (rootC!=null)
				return rootC;
		}
		return findAttributeSelOrWhereOrIdoRec(root, viewSelect, profundizar);
	}
	private static org.jdom.Element find(org.jdom.Element root, ArrayList<String> viewSelect) {
		String name = root.getName();
		if (root.getAttributeValue(QueryConstants.ID_O)!=null || root.getAttributeValue(QueryConstants.NOT_ID_O)!=null || 
				StringUtils.equals(root.getAttributeValue(QueryConstants.REQUIRED), "TRUE") || 
				StringUtils.equals(root.getAttributeValue(QueryConstants.NULL), "TRUE"))
			return root;
		if (name.equals(QueryConstants.ATTRIBUTE) && viewSelect.contains(root.getAttributeValue(QueryConstants.ID)) 
				|| name.equals(QueryConstants.WHERE))
//				|| (name.equals(QueryConstants.WHERE) && (root.getAttributeValue(QueryConstants.PROP)==null || 
//						Integer.parseInt(root.getAttributeValue(QueryConstants.PROP))!=Constants.IdPROP_BUSINESSCLASS) && 
//				(root.getAttributeValue(QueryConstants.VALUE)==null || !StringUtils.equals(root.getAttributeValue(QueryConstants.VALUE),"0"))))
			return root;
		return null;
	}
	private static org.jdom.Element findAttributeSelOrWhereOrIdoRec(org.jdom.Element root, ArrayList<String> viewSelect, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			Element childC = find(child, viewSelect);
			if (childC!=null)
				return childC;
			if (profundizar) {
				Element res = findAttributeSelOrWhereOrIdoRec(child, viewSelect, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}

	public static org.jdom.Element findByNameOrIdoReqNull(org.jdom.Element root, boolean actual, String[] names, boolean profundizar) {
		if (actual) {
			Element rootC = find(root, names);
			if (rootC!=null)
				return rootC;
		}
		return findByNameOrIdoReqNullRec(root, names, profundizar);
	}
	private static org.jdom.Element find(org.jdom.Element root, String[] names) {
		String name = root.getName();
		if (root.getAttributeValue(QueryConstants.ID_O)!=null || root.getAttributeValue(QueryConstants.NOT_ID_O)!=null || 
				StringUtils.equals(root.getAttributeValue(QueryConstants.REQUIRED), "TRUE") || 
				StringUtils.equals(root.getAttributeValue(QueryConstants.NULL), "TRUE"))
			return root;
		for (int i = 0; i < names.length; i++)
			if (name.equals(names[i]))
				return root;
//		if (name.equals(QueryConstants.ATTRIBUTE) || name.equals(QueryConstants.WHERE))
//				|| (name.equals(QueryConstants.WHERE) && (root.getAttributeValue(QueryConstants.PROP)==null || 
//						Integer.parseInt(root.getAttributeValue(QueryConstants.PROP))!=Constants.IdPROP_BUSINESSCLASS) && 
//				(root.getAttributeValue(QueryConstants.VALUE)==null || !StringUtils.equals(root.getAttributeValue(QueryConstants.VALUE),"0"))))
//			return root;
		return null;
	}
	private static org.jdom.Element findByNameOrIdoReqNullRec(org.jdom.Element root, String[] names, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			Element childC = find(child, names);
			if (childC!=null)
				return childC;
			if (profundizar) {
				Element res = findByNameOrIdoReqNullRec(child, names, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}

	/*public static org.jdom.Element findFirstOfFindByAt(org.jdom.Element root,
			String name1, String name1b, String name2, String name3, String at,
			String val, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getName().equals(name1)
					|| child.getName().equals(name1b)
					|| ((child.getName().equals(name2) || child.getName()
							.equals(name3)) && StringUtils.equals(child
							.getAttributeValue(at), val)))
				return child;
			if (profundizar) {
				Element res = findFirstOfFindByAt(child, name1, name1b, name2,
						name3, at, val, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}*/

	public static Element findElementByCDATA(Element root, String name, String text, boolean profundizar) {
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (name!=null && child.getName().equals(name)) {
				CDATA CData = (CDATA)child.getContent().get(0);
				String textNode = CData.getText();
				if (StringUtils.equals(textNode, text))
					return child;
			}
			if (profundizar) {
				Element res = findElementByCDATA(child, name, text, true);
				if (res != null)
					return res;
			}
		}
		return null;
	}

	/*public static void buildJavaDataArraysFromQueryResponse(ArrayList dataArr,
			ArrayList parArr, metaData md, selectData data, Element filter) {

		Iterator iObject = data.getIterator();
		while (iObject.hasNext()) {
			instance object = (instance) iObject.next();
			ArrayList rowData = new ArrayList();
			ArrayList rowPar = new ArrayList();
			buildJavaDataArrayROW(rowPar, rowData, md, object, filter);
			dataArr.add(rowData);
			parArr.add(rowPar);
		}
	}

	public static void buildJavaDataArrayROW(ArrayList rowPar,
			ArrayList rowData, metaData md, instance object, Element filter) {

		try {
			Integer Idto = new Integer(object.getType());
			rowPar.add(String.valueOf(object.getIDO()));
			rowPar.add(Idto.toString());
			rowPar.add(object.getRdn());

			Element eMetaTO = md.getMetaTO(Idto);

			Iterator iVirt = jdomParser.getFilterAtList(filter, true)
					.iterator();//antes no adaptado
			while (iVirt.hasNext()) {
				Element eVirt = (Element) iVirt.next();
				int ref = Integer.parseInt(eVirt.getAttributeValue("REF"));
				Integer IVTO = new Integer(eVirt.getAttributeValue("ID_TO"));
				Element eMetaVirtTO = md.getMetaTO(IVTO);
				Integer tapos = new Integer(eVirt.getAttributeValue("TA_POS"));
				Object value = object.getAttributeValue(tapos.intValue(), ref);
				//un nulo lo indicamos como un espacio
				if (value == null) {
					rowData.add(null);
					continue;
				} else
					rowData.add(value);

			}
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"TX:JDOM,buildFormTableFromResponse, error numerico:"
							+ ex.getMessage());
		}
	}*/
	//V1:END
}
