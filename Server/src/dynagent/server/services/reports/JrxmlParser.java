package dynagent.server.services.reports;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.jdomParser;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.ejb.AuxiliarModel;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;
import dynagent.server.services.querys.DataInfo;

public class JrxmlParser {
	//private int pageHeaderHeight=172;
	private int columnHeaderHeight=0;
	private int detailHeight=13;
	private int titleHeight=30;
	private int separation=14;
	private static final int pageWidth=595;
	private static final int pageWidthNoMargen=535;
	private static final int pageHeight=826;
	private static final int pageHeightComplete=866;
	private ArrayList<PropertyJrxml> listProperties= new ArrayList<PropertyJrxml>();
	private ArrayList<ImportJrxml> listImports= new ArrayList<ImportJrxml>();
	private ArrayList<FieldJrxml> listFields= new ArrayList<FieldJrxml>();
	private ArrayList<ConditionJrxml> listConditions= new ArrayList<ConditionJrxml>();
	private ArrayList<TextFieldJrxml> listResults= new ArrayList<TextFieldJrxml>();
	private ArrayList<StaticTextJrxml> listTitleResults= new ArrayList<StaticTextJrxml>();
	private ArrayList<ParameterJrxml> listParameters= new ArrayList<ParameterJrxml>();
	private String sql;
	//private ArrayList<String> listcolumns=new ArrayList<String>();
	//private IKnowledgeBaseInfo ik=null;
	//private ArrayList<Integer> listClass=new ArrayList<Integer>();
	private double letra = 6; //5.2;
	private int maxContDatos = 100;
	
	//private int uTask=0;
	private String titleReport=null;
	//private Integer idtoTarget=0;

	private final static int sizeTitle = 12;
	private final static int sizeHeader = 10;
	private final static int sizeDate = 8;
	
	public JrxmlParser() {
	}
	
	public void createJrxmlParser(Element queryWhere, String sql, HashMap<String,DataInfo> infoResult, 
			ResultSet data, IKnowledgeBaseInfo ik, FactoryConnectionDB fcdb, DataBaseMap dataBaseMap) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException {
		/*for (String key : infoResult.keySet()) {
			DataInfo dataInfo = infoResult.get(key);
			System.out.println("key: " + key + " -> dataInfo: " + dataInfo);
		}*/
		PropertyJrxml prop1=new PropertyJrxml("ireport.scriptlethandling", "0");
		PropertyJrxml prop2=new PropertyJrxml("ireport.encoding", "iso-8859-15");
		this.listProperties.add(prop1);
		this.listProperties.add(prop2);
		ImportJrxml imp1=new ImportJrxml("java.util.*");
		ImportJrxml imp2=new ImportJrxml("net.sf.jasperreports.engine.*");
		ImportJrxml imp3=new ImportJrxml("net.sf.jasperreports.engine.data.*");
		this.listImports.add(imp1);
		this.listImports.add(imp2);
		this.listImports.add(imp3);
		//this.ik=ik;
		//this.uTask=uTask;
		this.sql = sql;
		ArrayList<Element> classes=jdomParser.elements(queryWhere, QueryConstants.CLASS, true);
		fillConditions(classes, ik, fcdb, dataBaseMap);
		fillResults(infoResult, data);
	}
	
	private void fillResults(HashMap<String,DataInfo> infoResult, ResultSet data) throws SQLException {
		Integer size=infoResult.size();
		LinkedHashMap<String,String> namextype=new LinkedHashMap<String, String>();
		Iterator it = infoResult.keySet().iterator();
		while (it.hasNext()){
			String nameAttribute = (String)it.next();
			DataInfo dataInfo = infoResult.get(nameAttribute);
			String typeAtrribute = dataInfo.getType();
			namextype.put(nameAttribute, typeAtrribute);
		}
		size=namextype.size();
		
		HashMap<String, Integer> sizeFields = getSizeFields(namextype, infoResult, data);

		//TODO TERMINAR PARA QUE NO COJA REPETIDO LOS CAMPOS
		//Integer width=pageWidthNoMargen /size;
		System.out.println("SYSOUAMI: SIZE DYNAMIC="+size);
		//calculo de height:
		int renglones = 1;
		Set<String> keyName1= namextype.keySet();
		Iterator<String> itname1= keyName1.iterator();
		while(itname1.hasNext()){
			String nameAttribute=itname1.next();
			//System.out.println("nameAttribute " + nameAttribute);
			int len = nameAttribute.length();
			//System.out.println("len " + len);
			Integer width=sizeFields.get(nameAttribute);
			double numLetras = width/letra;
			//System.out.println("numLetras " + numLetras);
			int renglonesTmp = new Double(len/numLetras+1).intValue();
			//System.out.println("renglonesTmp " + renglonesTmp);
			if (renglonesTmp>renglones)
				renglones = renglonesTmp;
		}
		columnHeaderHeight = 15+10*(renglones-1);
		
		Set<String> keyName= namextype.keySet();
		Iterator<String> itname= keyName.iterator();
		int i=0;
		Integer left = 0;
		while(itname.hasNext()){
			String nameAttribute=itname.next();
			String typeAtrribute=namextype.get(nameAttribute);
			System.out.println("SYSOUAMI: NAMEATTRIBUTE="+nameAttribute);
			System.out.println("SYSOUAMI: TYPEATTRIBUTE="+typeAtrribute);
			Integer width=sizeFields.get(nameAttribute);
			System.out.println("width " + width);
			//Integer left = 0+(width*i);
			createDesign(nameAttribute,nameAttribute,typeAtrribute,left,width,renglones);
			left += width;
			i++;
		}
	}

	private void createDesign(String nameAttribute, String name,String typeAtribute, Integer left, Integer width, int renglones) {
		//if (typeAtrribute.equals("Double") && nameAttribute.contains("MAXIMO") && !this.listcolumns.contains(normalizeName)){
			StaticTextJrxml titleAttribute=new StaticTextJrxml(name,left,0,width,15+10*(renglones-1),"0.0", sizeDate, true);
			this.listTitleResults.add(titleAttribute);
			
			String textAlignment = null;
			String pattern = null;
			boolean isBoolean = false;
			boolean isDate = false;
			if (typeAtribute.equals("Double")) {
				textAlignment = "Right";
//				if (typeAtrribute.equals("Double"))
//					pattern = "##0.00";
			} else if (typeAtribute.equals("Long")) {
				typeAtribute = "Long";
				textAlignment = "Center";
				pattern = "dd/MM/yyyy";
				isDate = true;
//			} else if (typeAtribute.equals("Date") || typeAtribute.equals("Time") || typeAtribute.equals("DateTime")) {
//				typeAtribute = "String";
//				textAlignment = "Center";
			} else if(typeAtribute.equals("String")) {
				typeAtribute = "String";
				textAlignment = "Left";
			} else if(typeAtribute.equals("Boolean")) {
				typeAtribute = "Boolean";
				textAlignment = "Center";
				isBoolean = true;
			}

			FieldJrxml attribute=new FieldJrxml(nameAttribute,typeAtribute);
			this.listFields.add(attribute);
			//this.listcolumns.add(normalizeName);
			
			if(isBoolean) {
				String expression = "($F{" + nameAttribute + "}.equals(new Double(1)))?\"SI\":\"No\"";
				TextFieldJrxml result=new TextFieldJrxml(expression, "String",left,0,width-2,this.detailHeight-1,"0.0", textAlignment, pattern, sizeDate);
				this.listResults.add(result);
			} else if(isDate) {
				String expression = "new Date($F{" + nameAttribute + "}.longValue()*1000)";
				TextFieldJrxml result=new TextFieldJrxml(expression, "Date",left,0,width-2,this.detailHeight-1,"0.0", textAlignment, pattern, sizeDate);
				this.listResults.add(result);				
			} else {
				TextFieldJrxml result=new TextFieldJrxml(attribute,left,0,width-2,this.detailHeight-1,"0.0", textAlignment, pattern, sizeDate);
				this.listResults.add(result);
			}
	//	}
	}
	
	public static String normalizeLabel(String label){
		if(label!=null){
			StringBuffer buffer=new StringBuffer(label.toLowerCase());
			buffer.replace(0, 1, (String.valueOf(buffer.charAt(0))).toUpperCase());
			int index;
			while((index=buffer.indexOf("_"))!=-1){
				buffer.replace(index, index+1, " ");
			}
			int indexNextChar=0;
			while((index=buffer.indexOf(" ",indexNextChar))!=-1){
				indexNextChar=index+1;
				if(buffer.length()>indexNextChar){
					int indexProx=buffer.indexOf(" ",indexNextChar);
					if((indexProx==-1 && indexNextChar+2<=label.length()-1) || (indexProx!=-1 && indexProx>indexNextChar+2))
						buffer.replace(indexNextChar, indexNextChar+1, (String.valueOf(buffer.charAt(indexNextChar))).toUpperCase());
				}else break;
			}
			return buffer.toString().trim();
		}else return null;
	}
	
	public String getTitleReport() {
		return titleReport;
	}
	public void setTitleReport(String titleReport) {
		this.titleReport = titleReport;
	}

	private HashMap<String, Integer> getSizeFields(LinkedHashMap<String,String> namextype, 
			HashMap<String,DataInfo> infoResult, ResultSet data) throws SQLException {
		HashMap<String, Integer> sizeFields = new HashMap<String, Integer>();
		HashMap<String, Integer> sizeMinFields = new HashMap<String, Integer>();
		
		Set<String> keyName1 = namextype.keySet();
		Iterator<String> itname1= keyName1.iterator();
		while(itname1.hasNext()){
			String fieldName = itname1.next();
			//System.out.println("nameAttribute " + nameAttribute);
			
			//tener en cuenta palabras
			String[] words = fieldName.split(" ");
			double[] sizeWords = new double[words.length];
			double maxWord = 0;
			for (int i=0;i<words.length;i++) {
				String word = words[i];
				double sizeWord = word.length()*letra;
				if (sizeWord>maxWord)
					maxWord = sizeWord;
				sizeWords[i] = sizeWord;
			}
			maxWord = maxWord + 4;
			int length = new Double(maxWord).intValue();
			sizeFields.put(fieldName,length);
			sizeMinFields.put(fieldName,length);
			//establecer un minimo por si despues hay que restar, nos basaremos en el titulo
		}
		
		Integer size=namextype.size();
		int cont = 0;
		while (data.next()) {
			Iterator it = namextype.keySet().iterator();
			while (it.hasNext()) {
				String fieldName = (String)it.next();
				
				Object value = this.getFieldValue(namextype, infoResult, data, fieldName);
				int length = 0;
				if (value!=null) {
					String fieldType = namextype.get(fieldName);
					//System.out.println("fieldName " + fieldName + ", fieldType " + fieldType);
					if (fieldType.equals("Boolean"))
						length = 2; //SI/No
					else
						length = value.toString().length();
					//System.out.println("value " + value + ", length " + length);
					length = new Double(length*letra).intValue();
				}
				int sizeField = sizeFields.get(fieldName);
				if (sizeField<length)
					sizeFields.put(fieldName,length);
			}
			cont++;
			if (cont==maxContDatos)
				break;
		}
		data.beforeFirst();
		//iterar por sizeField para ajustar tamaños
		//sumarlos todos
		Integer totalSize = 0;
		Iterator itS = sizeFields.keySet().iterator();
		while (itS.hasNext()) {
			String fieldName = (String)itS.next();
			Integer sizeField = sizeFields.get(fieldName);
			totalSize += sizeField;
		}
		
		//si totalSize > pageWidthNoMargen -> quitar a campos String que ocupen mas espacio
		//TODO basarnos en sizeMinField y en que sea String
		//si aun asi necesitamos mas espacio quitar a cualquiera proporcional sin hacerlo negativo? o mostrar un error
		
		int add = pageWidthNoMargen-totalSize;
		//System.out.println("pageWidthNoMargen " + pageWidthNoMargen);
		//System.out.println("totalSize " + totalSize);
		//System.out.println("add " + add);
		int addField = add/size;
		if (addField<0) {
			int resto = quitar(add, size, sizeFields, sizeMinFields, namextype); //metodo recursivo
			if (resto<0)
				proporcional(resto, size, sizeFields);
		} else
			proporcional(add, size, sizeFields);
		return sizeFields;
	}
	
	public Object getFieldValue(LinkedHashMap<String,String> namextype, HashMap<String,DataInfo> infoResult, 
			ResultSet data, String fieldName) {
		Object value=null;
		int column = infoResult.get(fieldName).getColumn();
		try {
			value=data.getObject(column);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//System.out.println("SYSOUAMI: CAMPO-->"+fieldName);
		//System.out.println("SYSOUAMI: VALUE-->"+value);
		if (value!=null) {
			if (namextype.get(fieldName)!=null){
				//System.out.println("SYSOUAMI: CLASE-->"+value.getClass());
				//System.out.println("SYSOUAMI: TIPO-->"+this.typeMap.get(fieldName));
				
				if (namextype.get(fieldName).equals("Double")) {
					//en mySQL el cast a unsigned devuelve java.math.BigInteger
					//cuando me hace falta que sea java.lang.Integer
					if (value instanceof java.math.BigInteger) {
						java.math.BigInteger valueBInt = (java.math.BigInteger)value;
						Integer valueInt = Integer.parseInt(valueBInt.toString());
						value = valueInt;
					}
				} else if (namextype.get(fieldName).equals("Date"))
					value=QueryConstants.secondsToDate(String.valueOf(value),QueryConstants.getPattern(Constants.IDTO_DATE));
				else if (namextype.get(fieldName).equals("DateTime"))
					value=QueryConstants.secondsToDate(String.valueOf(value),QueryConstants.getPattern(Constants.IDTO_TIME));
				else if (namextype.get(fieldName).equals("Time"))
					value=QueryConstants.secondsToDate(String.valueOf(value),QueryConstants.getPattern(Constants.IDTO_DATETIME));
			}
		}
		return value;
	}

	
	private int quitar(int add, int size, HashMap<String, Integer> sizeFields, 
			HashMap<String, Integer> sizeMinFields, LinkedHashMap<String,String> namextype) {
		int addField = (add/size);
		//System.out.println("addField " + addField);
		boolean reducible = false;
		int resto = add - (addField*size);
		//System.out.println("resto inicial " + resto);
		Iterator itS = sizeFields.keySet().iterator();
		while (itS.hasNext()) {
			String fieldName = (String)itS.next();
			String type = namextype.get(fieldName);
			if (type.equals("String")) {
				//System.out.println(fieldName + " es String");
				Integer sizeField = sizeFields.get(fieldName) + addField;
				int sizeMinField = sizeMinFields.get(fieldName);
				//System.out.println("sizeField " + sizeField);
				//System.out.println("sizeMinField " + sizeMinField);
				if (sizeMinField>sizeField) {
					resto += sizeField-sizeMinField;
					sizeField = sizeMinField;
					//int p = sizeField-sizeMinField;
					//System.out.println("sizeField es = a sizeMinField, resto incrementado en " + p);
				} else
					reducible = true;
				//System.out.println(fieldName + " sizeFieldFinal " + sizeField);
				sizeFields.put(fieldName,sizeField);
			} else {
				//System.out.println(fieldName + " no es String");
				resto += addField;
				//System.out.println("resto incrementado en " + addField);
			}
		}
		if (reducible && resto<-size) {
			//System.out.println("itera con resto " + resto);
			resto = quitar(resto,size,sizeFields,sizeMinFields,namextype);
		}
		
		//System.out.println("resto final " + resto);
		return resto;
	}
	
	private void proporcional(int add, int size, HashMap<String, Integer> sizeFields) {
		int addField = (add/size);
		//System.out.println("addField " + addField);
		int resto = 0;
		if (addField!=0) {
			Iterator itS = sizeFields.keySet().iterator();
			while (itS.hasNext()) {
				String fieldName = (String)itS.next();
				Integer sizeField = sizeFields.get(fieldName) + addField;
				sizeFields.put(fieldName,sizeField);
			}
			resto = add - (addField*size);
			if (add<0)
				resto = -resto;
		} else
			resto = add;

		//ahora reparto el resto:
		//System.out.println("resto proporcional " + resto);
		Iterator itS = sizeFields.keySet().iterator();
		while (itS.hasNext()) {
			String fieldName = (String)itS.next();
			Integer sizeField = sizeFields.get(fieldName);
			//System.out.println("sizeField antes " + sizeField);
			if (resto<0)
				sizeField = sizeField-1;
			else
				sizeField = sizeField+1;
			//System.out.println("sizeField despues " + sizeField);
			sizeFields.put(fieldName,sizeField);
			if (resto<0)
				resto++;
			else
				resto--;
			if (resto==0)
				break;
		}
	}
	
	public ArrayList<String> fillConditions(ArrayList<Element> classes, IKnowledgeBaseInfo ik, FactoryConnectionDB fcdb, DataBaseMap dataBaseMap) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		Integer numOfCondition=0;
		ArrayList<String> conditions = new ArrayList<String>();
		Iterator<Element> itclasses=classes.iterator();
		boolean first=true;
		
		String idTarget = null;
		while(itclasses.hasNext()){
			Element classXml=itclasses.next();
			if(first){
				if (titleReport==null) {
					String name = classXml.getAttributeValue(QueryConstants.NAME);
					if (name!=null)
						this.titleReport=name.replaceAll("_", " ").toUpperCase()/*this.alias.getLabelClass(idto, this.uTask)*/;
				}
				first=false;
				//Integer idto=Integer.parseInt(classXml.getAttributeValue(QueryConstants.ID_TO));
				//this.idtoTarget=idto;
				idTarget = classXml.getAttributeValue(QueryConstants.ID);
			}
			if(classXml.getAttribute(QueryConstants.ID_O)!=null){
				String idos = classXml.getAttributeValue(QueryConstants.ID_O);
				String[] idosSpl = idos.split(",");
				String operation=QueryConstants.IGUAL;
				String value=null;
				for (int i=0;i<idosSpl.length;i++) {
					Integer ido=Integer.parseInt(idosSpl[i]);
					String rdn = InstanceService.getRdn(fcdb, dataBaseMap, QueryConstants.getTableIdNoCompress(ido), QueryConstants.getIdtoNoCompress(ido));
					if (value==null)
						value=rdn;
					else
						value+="," + rdn;
				}
				String nameProp = classXml.getAttributeValue(QueryConstants.NAME_PROP);
				int arrobaPosition = nameProp.indexOf("@");
				String name = null;
				if (arrobaPosition==-1)
					name = nameProp;
				else
					name = nameProp.substring(0, nameProp.indexOf("@"));
				ConditionJrxml cond= new ConditionJrxml(value,null,name,operation,null,sizeDate);
				conditions.add(cond.getStaticText().getName());

				cond.setPositionText(15,35+numOfCondition*this.separation,  pageWidthNoMargen-15, 14);
				//cond.setPositionTextResult(197,35+numOfCondition*this.separation, 319, 14);
				
				if(this.titleHeight+19<pageHeight-this.detailHeight){
					if(30+numOfCondition*this.separation+10>this.titleHeight){
						this.titleHeight=this.titleHeight+14;
					}
					this.listConditions.add(cond);
				}
				numOfCondition++;
			}
			List childrenofclass= classXml.getChildren(QueryConstants.WHERE);
			Iterator itchildren=childrenofclass.iterator();
			while(itchildren.hasNext()){
				Element child= (Element) itchildren.next();
				Integer idProp=Integer.parseInt(child.getAttributeValue(QueryConstants.PROP));
				
				if(idProp!=Constants.IdPROP_BUSINESSCLASS){
					System.out.println("Property where=>"+ idProp);
//					String nameProp= null;
					String nameProp = child.getAttributeValue(QueryConstants.NAME_PROP);
					System.out.println("nameProp " + nameProp);
					String name = null;
					int arrobaPosition = nameProp.indexOf("@");
					if (arrobaPosition==-1)
						name = nameProp;
					else
						name = nameProp.substring(0, nameProp.indexOf("@"));
					
					ConditionJrxml cond=null;
					String operation=child.getAttributeValue(QueryConstants.OP);
					String value=null;
					if(ik.isObjectProperty(idProp)){
						value=child.getAttributeValue(QueryConstants.VALUE)!=null?child.getAttributeValue(QueryConstants.VALUE):child.getText();
						if (!StringUtils.equals(child.getAttributeValue(QueryConstants.IS_RDN),"TRUE")) {
							String[] idos=value.split(";");
							System.out.println("Value=>"+value);
							for(int o=0;o<idos.length; o++){
								System.out.println("ido=>"+idos[o]);
							}
							value="";
							for(int k=0;k<idos.length; k++){
								Integer ido=Integer.parseInt(idos[k]);
								String rdn = InstanceService.getRdn(fcdb, dataBaseMap, QueryConstants.getTableId(ido), QueryConstants.getIdto(ido));
								if(value.equals("")){
									value=value+rdn;
								}else{
									value=value+", "+rdn;
								}
							}
						}
						cond= new ConditionJrxml(value,null,name,operation,null,sizeDate);
					}else{
						if (operation.equals(QueryConstants.BETWEEN)){
							String valuemin=child.getAttributeValue(QueryConstants.VAL_MIN);
							String valuemax=child.getAttributeValue(QueryConstants.VAL_MAX);
							valuemin = getDate(child, valuemin);
							valuemax = getDate(child, valuemax);
							cond= new ConditionJrxml(valuemin,valuemax,name,operation,null,sizeDate);
						}else{
							if (child.getText()!=null && child.getText().length()>0){
								value=child.getText();
							}else{
								value=child.getAttributeValue(QueryConstants.VALUE);
							}
							value = getDate(child, value);
							cond= new ConditionJrxml(value,null,name,operation,null,sizeDate);
						}
					}
					conditions.add(cond.getStaticText().getName());
					cond.setPositionText(15,35+numOfCondition*this.separation,  pageWidthNoMargen-15, 14);
					//cond.setPositionTextResult(197,35+numOfCondition*this.separation, 319, 14);
					
					if(this.titleHeight+19<pageHeight-this.detailHeight){
						if(30+numOfCondition*this.separation+10>this.titleHeight){
							this.titleHeight=this.titleHeight+14;
						}
						this.listConditions.add(cond);
					}
					numOfCondition++;
				}
			}
		}
		if (numOfCondition>0)
			this.titleHeight=this.titleHeight+10;
		return conditions;
	}

	private static String getDate(Element child, String value) {
		String idTmRuleengineStr = child.getAttributeValue(QueryConstants.ID_TM_RULEENGINE);
		if (idTmRuleengineStr!=null) {
			int idTmRuleengine = Integer.parseInt(idTmRuleengineStr);
			
			if (idTmRuleengine==Constants.IDTO_DATE ||
					idTmRuleengine==Constants.IDTO_DATETIME ||
					idTmRuleengine==Constants.IDTO_TIME) {
				value = QueryConstants.secondsToDate(value, QueryConstants.getPattern(idTmRuleengine));
			}
		}
		return value;
	}
	
	/*public Dimension getDimString( String value, boolean bold )
    {
        if (value == null)
        {
            return new Dimension(0, 0);
        }
        //getStringBounds no es una funcion que garantice totalmente que la cadena ocupa exactamente ese tamaño,
        //es mas bien una estimación. Para evitar errores podriamos hacer value+" " pq hay casos en el que no se muestra
        //la cadena completa usando el espacio calculado. Además de esta manera evitariamos que un label quede demasiado
        //cerca del componente. Sin embargo hay casos en el que queda demasiado alejado. De momento multiplicamos el ancho
        //por un valor que se cumpla en la fuente que estemos usando, para la fuente actual Comic Sans MS 1,06 es el valor que utilizamos.
        Font font=new Font()
        Rectangle2D rect = m_fontRegular.getStringBounds(value, m_fontRender);
        Dimension size=rect.getBounds().getSize();
        size.width=(int)(size.width*1.06);
        return size;
        
    }*/
	
	public String toString(){
		String cad="";
		cad=cad+reportMaster(pageWidth);
		Iterator<PropertyJrxml> itproperties=this.listProperties.iterator();
		while(itproperties.hasNext()){
			PropertyJrxml property=itproperties.next();
			cad=cad+property.toString();
		}
		Iterator<ImportJrxml> itimports=this.listImports.iterator();
		while(itimports.hasNext()){
			ImportJrxml importjrxml=itimports.next();
			cad=cad+importjrxml.toString();
		}
		Iterator<ParameterJrxml> itparameters=this.listParameters.iterator();
		while(itparameters.hasNext()){
			ParameterJrxml parameter=itparameters.next();
			cad= cad+parameter.toString();
		}
		cad=cad+"<queryString><![CDATA["+this.sql+"]]></queryString>";
		Iterator<FieldJrxml> itfields=this.listFields.iterator();
		while(itfields.hasNext()){
			FieldJrxml field=itfields.next();
			cad=cad+field.toString();
		}
		cad=cad+pageHeaderMaster(this.titleHeight,this.titleReport);
		Iterator<ConditionJrxml> itconditions=this.listConditions.iterator();
		while(itconditions.hasNext()){
			ConditionJrxml condition=itconditions.next();
			cad=cad+condition.toString();
		}
		cad=cad+footPageHeaderMaster();
		cad=cad+columnMaster(this.columnHeaderHeight);
		Iterator<StaticTextJrxml> ittitleresults=this.listTitleResults.iterator();
		while(ittitleresults.hasNext()){
			StaticTextJrxml titleResult=ittitleresults.next();
			cad=cad+titleResult.toString();
		}
		cad=cad+footColumnMaster(this.columnHeaderHeight);
		cad=cad+bodyMaster(this.detailHeight);
		Iterator<TextFieldJrxml> itresults=this.listResults.iterator();
		while(itresults.hasNext()){
			TextFieldJrxml result=itresults.next();
			cad=cad+result.toString();
		}
		cad=cad + "<line direction=\"TopDown\">"+
				"	<reportElement "+
				"		x=\"0\" "+
				"		y=\""+(this.detailHeight-1)+"\" " +
				"		width=\""+pageWidthNoMargen+"\" "+
				"		height=\"0\" "+
				"		forecolor=\"#CCCCCC\" "+
				"		backcolor=\"#CCCCCC\" "+
				"		key=\"line-1\" "+
				"		positionType=\"Float\"/> "	+
				"	<graphicElement>"+
				"		<pen lineWidth=\"0.5\" lineStyle=\"Solid\"/>"+
				"	</graphicElement>"+
				"</line> ";
		cad=cad+footBodyMaster();
		//cad=cad+this.footerPage();
		cad=cad+footReportMaster();
		System.out.println("pageHeader=>"+this.titleHeight);
		
		return cad;
	}

	private String pageHeaderMaster(int title,String titleReport){
		String result = 
		"<background>"+
		"			<band height=\"0\"  splitType=\"Stretch\" >"+
		"			</band>"+
		"		</background>"+
		"		<title>"+
		"		<band height=\"0\"  splitType=\"Stretch\" >"+
		"		</band>"+
		"		</title>"+
		"		<pageHeader>"+
		"		<band height=\""+title+"\"  splitType=\"Stretch\" >"+
		"			<staticText>"+
		"				<reportElement "+
		"					x=\"114\" "+
		"					y=\"1\" " +
		"					width=\""+(pageWidthNoMargen-114*2)+"\" "+
//		"					x=\"9\""+
//		"					y=\"7\""+
//		"					width=\"507\""+
		"					height=\"18\""+
		"					key=\"staticText-8\"/>"+
		"				<textElement textAlignment=\"Center\" rotation=\"None\">"+
		"					<font pdfFontName=\"Helvetica-Bold\" size=\"" + sizeTitle + "\" isBold=\"true\" isStrikeThrough=\"false\" />"+
		"				</textElement>"+
		"			<text><![CDATA[LISTADO DE "+titleReport+"]]></text>"+
		"			</staticText>";
		
		result += "<textField isStretchWithOverflow=\"false\" pattern=\"dd/MM/yyyy\" isBlankWhenNull=\"false\" evaluationTime=\"Report\" hyperlinkType=\"None\"  hyperlinkTarget=\"Self\" >"+
		"<reportElement"+
		"	x=\"1\""+
		"	y=\"1\" " +
		"	width=\"113\""+
		"	height=\"18\""+
		"	key=\"textField-8\""+
		"	positionType=\"Float\"/>"+
		"	<box>"+
		"		<topPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
		"		<leftPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
		"		<bottomPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
		"		<rightPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
		"	</box>"+
		"<textElement>"+
		"	<font size=\"" + sizeHeader + "\" />"+
		"</textElement>"+ new TextFieldExpression("new java.util.Date()","Date") + 
//		"<textFieldExpression   class=\"java.util.Date\"><![CDATA[new java.util.Date()]]></textFieldExpression>"+
		"</textField>";
		
		result += "<textField isStretchWithOverflow=\"false\" isBlankWhenNull=\"false\" evaluationTime=\"Page\" hyperlinkType=\"None\"  hyperlinkTarget=\"Self\" >"+
		"<reportElement"+
		"	x=\""+(pageWidthNoMargen-114)+"\" " +
		"	y=\"1\" " +
		"	width=\"70\""+
		"	height=\"18\""+
		"	key=\"textField-9\"/>"+
		"	<box>"+
		"		<topPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
		"		<leftPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
		"		<bottomPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
		"		<rightPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
		"	</box>"+
		"<textElement textAlignment=\"Right\">"+
		"	<font size=\"" + sizeHeader + "\" />"+
		"</textElement>"+ new TextFieldExpression("\"Página \" + $V{PAGE_NUMBER} + \" de \"","String") + 
//		"<textFieldExpression   class=\"java.util.Date\"><![CDATA[new java.util.Date()]]></textFieldExpression>"+
		"</textField>";
		
		result += "<textField isStretchWithOverflow=\"false\" isBlankWhenNull=\"false\" evaluationTime=\"Report\" hyperlinkType=\"None\"  hyperlinkTarget=\"Self\" >"+
		"<reportElement"+
		"	x=\""+(pageWidthNoMargen-114+70)+"\" " +
		"	y=\"1\" " +
		"	width=\"44\""+
		"	height=\"18\""+
		"	key=\"textField-10\"/>"+
		"	<box>"+
		"		<topPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
		"		<leftPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
		"		<bottomPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
		"		<rightPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
		"	</box>"+
		"<textElement>"+
		"	<font size=\"" + sizeHeader + "\" />"+
		"</textElement>"+ new TextFieldExpression("\" \" + $V{PAGE_NUMBER}","String") + 
//		"<textFieldExpression   class=\"java.lang.String\"><![CDATA[\" \" + $V{PAGE_NUMBER}]]></textFieldExpression>"+
		"</textField>";

	
	
		/*"<staticText>"+
		"<reportElement "+
		" x=\"3\" "+
//		" x=\"9\""+
		" y=\"57\""+
		" width=\"207\""+
		" height=\"14\""+
		" key=\"staticText-10\" positionType=\"Float\"/>"+
		"<textElement>"+
		"<font pdfFontName=\"Helvetica-Bold\" size=\"10\" isBold=\"true\"/>"+
		"</textElement>"+
		"<text><![CDATA[CONDICIONES DE LA BUSQUEDA:]]></text>"+
		"</staticText>";*/
		return result;
	}
	private String footPageHeaderMaster(){
		return 
		"		</band>"+
		"	</pageHeader>";
	}
	
	private String columnMaster(int columHeaderHeight){
		return 
		"		<columnHeader>"+
		"			<band height=\""+columnHeaderHeight+"\"  splitType=\"Stretch\" >";
//		"				<staticText>"+
//		"					<reportElement x=\"3\" y=\""+(this.detailHeight-1)+"\" width=\""+pageWidthNoMargen+"\" height=\"18\" key=\"staticText-7\" positionType=\"Float\"/>"+
//		"					<textElement>"+
//		"						<font pdfFontName=\"Helvetica-Bold\" size=\"10\" isBold=\"true\" isUnderline=\"true\"/>"+
//		"					</textElement>"+
//		"				<text><![CDATA[RESULTADOS DE LA BUSQUEDA]]></text>"+
//		"				</staticText>";
	}
	private String footColumnMaster(int columHeaderHeight){
		return "</band>"+
		"	</columnHeader>";
	}
	
	private String bodyMaster(int detailHeight){
		return 
		"	<detail>"+
		"		<band height=\""+detailHeight+"\" >";
	}
	private String footBodyMaster() {
		return 
		"		</band>"+
		"	</detail>";
	}
	private String reportMaster(int pageWidth){
		return "<?xml version=\"1.0\" encoding=\"iso-8859-15\"  ?>"+
		"<jasperReport"+
		"		 xmlns=\"http://jasperreports.sourceforge.net/jasperreports\""+
		"		 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+
		"		 xsi:schemaLocation=\"http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd\""+
		"		 name=\"master\""+
		"		 columnCount=\"1\""+
		"		 printOrder=\"Vertical\""+
		"		 orientation=\"Portrait\""+
		"		 pageWidth=\""+pageWidth+"\""+
		"		 pageHeight=\""+pageHeightComplete+"\""+
		"		 columnWidth=\"535\""+
		"		 columnSpacing=\"0\""+
		"		 leftMargin=\"30\""+
		"		 rightMargin=\"30\""+
		"		 topMargin=\"20\""+
		"		 bottomMargin=\"20\""+
		"		 whenNoDataType=\"AllSectionsNoDetail\""+
		"		 isTitleNewPage=\"false\""+
		"		 isSummaryNewPage=\"false\">";
	}
	private String footReportMaster(){
		return "</jasperReport>";
	}

	private String footerPage(){
		return "<pageFooter> "+
			   "<band height=\"20\"  splitType=\"Stretch\" > "+
			   "<staticText> "+
					"<reportElement "+
							"x=\"403\" "+
							"y=\"4\" "+
							"width=\"53\" "+ 
							"height=\"14\" "+
					"key=\"staticText-11\" positionType=\"Float\"/> "+
					"	<box>"+
					"		<topPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
					"		<leftPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
					"		<bottomPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
					"		<rightPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
					"	</box>"+
				"<textElement> "+
					"<font/> "+
				"</textElement> "+
				"<text><![CDATA[Página ]]></text> "+
				"</staticText> "+
				"<textField isStretchWithOverflow=\"false\" isBlankWhenNull=\"false\" evaluationTime=\"Now\" hyperlinkType=\"None\"  hyperlinkTarget=\"Self\" > "+
				"<reportElement "+
					"x=\"463\" "+
					"y=\"4\" "+
					"width=\"57\" "+
					"height=\"14\" "+
					"key=\"textField\" positionType=\"Float\"/> "+
					"	<box>"+
					"		<topPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
					"		<leftPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
					"		<bottomPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
					"		<rightPen lineWidth=\"0.0\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
					"	</box>"+
				"<textElement> "+
					"<font/> "+
				"</textElement> "+
			"<textFieldExpression   class=\"java.lang.Integer\"><![CDATA[$V{PAGE_NUMBER}]]></textFieldExpression> "+
			"</textField> "+
			"</band> "+
			"</pageFooter> ";
	}
	
}
