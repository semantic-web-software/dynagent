package dynagent.server.services.reports;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.IDN;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Esta clase se encarga de generar los ficheros .jrxml que contienen el diseño de los informes
 * y subinformes.
 * 
 * @author Dynagent - David
 */
public class GenerateJRXML {
	/**
	 * Cabecera de los documentos jrxmls, se separa en 2 para poner el nombre dinamicamente.
	 */
	private static String head="<?xml version=\"1.0\" encoding=\"UTF-8\"  ?>\n"+
							"<!DOCTYPE jasperReport PUBLIC \"//JasperReports//DTD Report Design//EN\" \"./jasperreport.dtd\">\n"+
							"<jasperReport\n" +
							"		name=\"";
	/**
	 * Segunda parte de la cabecera.
	 */
	
	private static String head2 ="\"\n" +
							"		columnCount=\"1\"\n" +
							"		printOrder=\"Vertical\"\n" +
							"		orientation=\"Portrait\"\n" +
							"		pageWidth=\"595\"\n" +
							"		pageHeight=\"842\"\n" + 
							"		columnWidth=\"535\"\n" +
							"		columnSpacing=\"0\"\n" +
							"		leftMargin=\"30\"\n" +
							"		rightMargin=\"30\"\n" +
							"		topMargin=\"20\"\n"+
							"		bottomMargin=\"20\"\n"+
							"		whenNoDataType=\"AllSectionsNoDetail\"\n"+
							"		isTitleNewPage=\"false\"\n"+
							"		isSummaryNewPage=\"false\">\n"+
							"	<property name=\"ireport.scriptlethandling\" value=\"0\" />\n" +
							"	<property name=\"ireport.encoding\" value=\"UTF-8\" />\n"+	
							"	<import value=\"java.util.*\" />\n"+
							"	<import value=\"net.sf.jasperreports.engine.*\" />\n"+
							"	<import value=\"net.sf.jasperreports.engine.data.*\" />\n\n";
	
	/**
	 * Cuerpo comun de los ficheros jrxmls master
	 */
	private static String bodymaster="	<background>\n"+
								"			<band height=\"0\"  splitType=\"Stretch\" >\n"+
								"			</band>\n"+
								"		</background>\n"+
								"		<title>\n"+
								"			<band height=\"50\"  splitType=\"Stretch\" >\n"+
								"			</band>\n"+
								"		</title>\n"+
								"		<pageHeader>\n"+
								"			<band height=\"50\"  splitType=\"Stretch\" >\n"+
								"			</band>\n"+
								"		</pageHeader>\n"+
								"		<columnHeader>\n"+
								"			<band height=\"30\"  splitType=\"Stretch\" >\n"+
								"			</band>\n"+
								"		</columnHeader>\n"+
								"		<detail>\n"+
								"			<band height=\"173\"  splitType=\"Stretch\" >\n"+
								"			</band>\n"+
								"		</detail>\n"+
								"		<columnFooter>\n"+
								"			<band height=\"30\"  splitType=\"Stretch\" >\n"+
								"			</band>\n"+
								"		</columnFooter>\n"+
								"		<pageFooter>\n"+
								"			<band height=\"50\"  splitType=\"Stretch\" >\n"+
								"			</band>\n"+
								"		</pageFooter>\n"+
								"		<lastPageFooter>\n"+
								"			<band height=\"50\"  splitType=\"Stretch\" >\n"+
								"			</band>\n"+
								"		</lastPageFooter>\n"+
								"		<summary>\n"+
								"			<band height=\"50\"  splitType=\"Stretch\" >\n"+
								"			</band>\n"+
								"		</summary>\n"+
								"</jasperReport>";
	/**
	 * Cuerpo comun de los ficheros jrxmls subreport
	 */
	private static String bodysubreport ="		<background>\n"+	
									  "			<band height=\"0\"  splitType=\"Stretch\" >\n"+
									  "			</band>\n"+
									  "		</background>\n"+
									  "		<title>\n"+
									  "			<band height=\"0\"  splitType=\"Stretch\" >\n"+
									  "			</band>\n"+
									  "		</title>\n"+
									  "		<pageHeader>\n"+
									  "			<band height=\"0\"  splitType=\"Stretch\" >\n"+
									  "			</band>\n"+
									  "		</pageHeader>\n"+
									  "		<columnHeader>\n"+
									  "			<band height=\"0\"  splitType=\"Stretch\" >\n"+
									  "			</band>\n"+
									  "		</columnHeader>\n"+
									  "		<detail>\n"+
									  "			<band height=\"163\"  splitType=\"Stretch\" >\n"+
									  "			</band>\n"+
									  "		</detail>\n"+
									  "		<columnFooter>\n"+
									  "			<band height=\"0\"  splitType=\"Stretch\" >\n"+
									  "			</band>\n"+
									  "		</columnFooter>\n"+
									  "		<pageFooter>\n"+
									  "			<band height=\"0\"  splitType=\"Stretch\" >\n"+
									  "			</band>\n"+
									  "		</pageFooter>\n"+
									  "		<summary>\n"+
									  "			<band height=\"0\"  splitType=\"Stretch\" >\n"+
									  "			</band>\n"+
									  "		</summary>\n"+
									  "</jasperReport>";
	
	/**
	 * metodo encargado de generar un fichero .jrxml dependiendo de los parámetros pasados.<br><br>
	 * 
	 * Creamos un fichero con la ruta que se nos pasa en 'name', escribimos en el fichero {@link #head},
	 * seguido de 'name' y posteriormente escribimos {@link #head2}.<br>
	 * Rellenamos el fichero con los parámetros para cada subreport mediante el metodo {@link #subreportsParameters(FileWriter, List)}.<br>
	 * Luego rellenamos el fichero con los campos y sus tipos, mediante el metodo {@link #fillFields(FileWriter, Map)}.<br>
	 * Por último comprobamos si el fichero jrxml que estamos creando es un master o un subreport; si es un master
	 * escribimos en el fichero {@link #bodymaster}, sino {@link #bodysubreport}.
	 * @param nameTypes Mapa con pares nombre y tipo de dato.<br>
	 * 					<i>Ejemplo: ("nombre empresa", "String") - (nombre_campo, tipo_campo)</i>
	 * @param name Nombre del fichero jrxml a crear.
	 * @param subreports Lista con los ids de los subrerpors (si tiene).
	 * @param reemplace 
	 * @param paramsConditions 
	 * @throws IOException Se produce si no encuentra los ficheros o la ruta de los ficheros es incorrecta.
	 */
	public static void makeJRXML(Map<String,String> nameTypes, String name, String nameFile, List<String> subreports, boolean reemplace, ArrayList<String> paramsConditions) throws IOException{
		
		File fJrxml=new File(nameFile);
		if (fJrxml.exists() && !reemplace){
			
		}else {
			FileWriter jrxml=new FileWriter(fJrxml);
			jrxml.write(head);
			jrxml.write(name);
			jrxml.write(head2);
			subreportsParameters(jrxml,subreports);
			if(name.equals("master")){
				paramsConditions(jrxml,paramsConditions);
			}
			
			jrxml.write("\n");
			fillFields(jrxml,nameTypes);
			subreportsFileds(jrxml, subreports);
			jrxml.write("\n");
			if(name.equals("master")){
				jrxml.write(bodymaster);
			}
			else{
				jrxml.write(bodysubreport);
			}
			jrxml.close();
		}
	}
	
	private static void paramsConditions(FileWriter jrxml, ArrayList<String> paramsConditions) throws IOException {
		Iterator<String> it=paramsConditions.listIterator();
		while(it.hasNext()){
			String params=it.next();
			jrxml.write("	<parameter name=\"");
			jrxml.write(params);
			jrxml.write("\" isForPrompting=\"true\" class=\"java.lang.String\"/>\n");
		}
	}

	/**
	 * metodo principal, el cual se encarga de crear todos los ficheros jrxml llamando al metodo {@link #makeJRXML(Map, String, List)}.<br><br>
	 * 
	 * <ol>
	 * <li>Iteramos sobre el conjunto de claves de 'map' haciendo:</li>
	 * <ol>
	 * 		<li>Almacenamos en 'key' la clave</li>
	 * 		<li>Almacenamos en 'shows' los campos visibles de la consulta asociada a 'key'</li>
	 * 		<li>Para cada valor de 'shows'
	 * 		<ul>
	 * 			<li>Hacemos split con "_" almacenando el nombre en 'nombre' y insertando en
	 * 			un mapa 'types' el par 'nombre' y tipo del campo</li></ul>
	 * 		</li>
	 * 		<li>Extraemos los ids de los subreports asociados a 'key', almacenandolos en
	 * 		una lista llamada 'subreports'.</li>
	 * 		<li>Llamamos a {@link #makeJRXML(Map, String, List)} con 'types', la ruta del fichero
	 * 		y 'subreports'</li>
	 * </ol></li>
	 * </ol>
	 * @param map Mapa con toda la informacion de los reports ({@link ParQuery}).
	 * @param reemplace 
	 * @param paramsConditions 
	 * @return Mapa con los indices de las consultas y la ruta del fichero jrxml creado para cada una.
	 * @throws IOException Salta si hay algun error en {@link #makeJRXML(Map, String, List)}.
	 */
/*	public static Map<String,String> make(Map<String, ParQuery> map, String nameR, String idMaster,String path,boolean reemplace, ArrayList<String> paramsConditions) throws IOException{
		Map<String,String> result= new HashMap<String, String>();
		Set<String> keys=map.keySet();
		Iterator<String> it=keys.iterator();
		while (it.hasNext()){
			String key=it.next();
			Map<String, String> types= new HashMap<String, String>();
			List<String> subreports= new ArrayList<String>();
			String shows[]=map.get(key).getShow();
			for(int i=0;i<shows.length;i++){
				String temp[]=shows[i].split("_");
				String nombre="";
				for(int j=1;j<temp.length-1;j++){
					if (j==temp.length-2)
						nombre=nombre.concat(temp[j]);
					else
						nombre=nombre.concat(temp[j])+" ";
				}
				String type = temp[temp.length-1];
				if (type.equals("Enumerado"))
					type = "String";
				types.put(nombre, type);
			}
			String ids[]=map.get(key).getIds();
			if(!ids[0].equals("")){
				for(int i=0;i<ids.length;i++){
					subreports.add(ids[i]);
				}
			}
			boolean isMaster = key.equals(idMaster);
			String name = getName( key, isMaster);
			String nameFile = getNameFile(nameR, key, isMaster,path);
			System.out.println("ami: "+nameFile);
			makeJRXML(types,name,nameFile,subreports,reemplace,paramsConditions);
			result.put(key,nameFile);
		}
		
		return result;
	}
*/
	private static String getName( String key, boolean isMaster) {
		String name = null;
		if (isMaster)
			name = "master";
		else
			name = "sub"+key;
		return name;
	}
	public static String getNameFile(String nameR, String key, boolean isMaster, String path) {
		String name = path;
		if (isMaster)
			name += nameR;
		else
			name += nameR+"_"+key;
		name += ".jrxml";
		return name;
	}
	
	/*public static void putMaster(Map<String, String> jrxmls,  String nameR, String key,HashMap<String,String> idNameSub,String path) throws IOException{
		String nameFile = getNameFile(nameR,key, true,idNameSub,path);
		putJRXML(nameFile,nameR);
		jrxmls.put(key,nameFile);
	}
	public static void put(Map<String, String> jrxmls,  String nameR, String key,HashMap<String,String> idNameSub,String path) throws IOException{
		String nameFile = getNameFile(nameR,key, false,idNameSub,path);
		putJRXML(nameFile,nameR);
		jrxmls.put(key,nameFile);
	}

	private static void putJRXML(String nameFile, String reportStr) throws IOException{
		File fJrxml=new File(nameFile);
		if (fJrxml.exists())
			throw new IOException("Los ficheros de diseño del report al que se intenta acceder ya están en uso");
		else {
			FileWriter jrxml=new FileWriter(fJrxml);
			jrxml.write(reportStr);
			jrxml.close();
		}
	}*/

	/**
	 * metodo encargado de rellenar el fichero jrxml con los parametros de los subreports.<br><br>
	 * 
	 * Iteramos sobre 'subreports' escribiendo en el fichero la cadena<br>
	 * <i><parameter name=id_subreport isForPrompting='true' class='net.sf.jasperreports.engine.JasperReport'/></i>
	 * @param jrxml Fichero donde escribir esta sentencia.
	 * @param subreports Lista con los ids de los subreports.
	 * @throws IOException Salta si hay algun fallo con el fichero jrxml.
	 */
	private static void subreportsParameters(FileWriter jrxml, List<String> subreports) throws IOException {
		Iterator<String> it=subreports.listIterator();
		while(it.hasNext()){
			String subreport=it.next();
			jrxml.write("	<parameter name=\"");
			jrxml.write(subreport);
			jrxml.write("\" isForPrompting=\"true\" class=\"net.sf.jasperreports.engine.JasperReport\"/>\n");
		}
		
	}
	
	/**
	 * metodo encargado de rellenar el fichero jrxml con los campos de los subreports (origen de datos en el informe principal).<br><br>
	 * 
	 * Iteramos sobre 'subreports' escribiendo en el fichero la cadena<br>
	 * <i><field name='sub'+id_subreport class='java.lang.Object'/>
	 * @param jrxml Fichero donde escribir esta sentencia.
	 * @param subreports Lista con los ids de los subreports.
	 * @throws IOException Salta si hay algun fallo con el fichero jrxml.
	 */
	private static void subreportsFileds(FileWriter jrxml, List<String> subreports) throws IOException {
		Iterator<String> it=subreports.listIterator();
		while(it.hasNext()){
			String subreport=it.next();
			jrxml.write("	<field name=\"");
			jrxml.write("sub"+subreport);
			jrxml.write("\" class=\"java.lang.Object\"/>\n");
		}
	}
	
	/**
	 * metodo encargado de rellenar el fichero jrxml con los campos.<br><br>
	 * 
	 * Iteramos sobre el conjunto de las claves de 'nameTypes' escribiendo en el fichero la cadena<br>
	 * <i><field name=nombre class='java.lang.Object'/>
	 * @param jrxml Fichero donde escribir esta sentencia.
	 * @param nameTypes Mapa con los nombres y tipos de los campos.
	 * @throws IOException Salta si hay algun fallo con el fichero jrxml.
	 */
	private static void fillFields(FileWriter jrxml, Map<String, String> nameTypes) throws IOException {
		Set<String> keys=nameTypes.keySet();
		Iterator<String> it=keys.iterator();
		while(it.hasNext()){
			String key=it.next();
			jrxml.write("	<field name=\"");
			jrxml.write(key);
			String type = nameTypes.get(key);
			jrxml.write("\" class=\"java.lang.");
			if (type.equals("Date") || type.equals("Time") || type.equals("DateTime"))
				jrxml.write("String");
			else if (type.equals("Boolean"))
				jrxml.write("Double");
			else
				jrxml.write(type);
			jrxml.write("\"/>\n");
		}
	}
	
}
