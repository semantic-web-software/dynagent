package dynagent.common.communication;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.BooleanValue;
import dynagent.common.properties.values.DoubleValue;
import dynagent.common.properties.values.IntValue;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.StringValue;
import dynagent.common.properties.values.TimeValue;
import dynagent.common.properties.values.UnitValue;
import dynagent.common.properties.values.Value;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.GIdRow;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.RowItem;
import dynagent.common.utils.jdomParser;

/**
 * Esta clase se encarga de construir la definición de la consulta y actualizar los datos de ésta.
 * <br>Básicamente define un protocolo, pasando la consulta a formato texto.
 * Se define el formato de cada instancia mediante las filas, columnas y el valor asignado.
 */

class DatosOPropClass extends Object {
	private int property;
	private String name;

	public DatosOPropClass(int property, String name) {
		this.property = property;
		this.name = name;
	}

	public int getProperty() {
		return property;
	}
	public void setProperty(int property) {
		this.property = property;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}

class DatosIndiv extends Object {
	private Integer ido;
	private Integer idto;   //útil para las object property class, ya que no sé el idto del padre
	private boolean tieneHermanoClass;
	private ArrayList<String> idObjPropClass;  //guarda los ids de las object property class que tenga como hijas
	private String idPadre;  //introduce el id del padre si estoy en una ObjectProperty class, esto es por el tema del orden
	
	public DatosIndiv(boolean tieneHClass, ArrayList<String> idObjPropClass, String idPadre) {
		this.tieneHermanoClass = tieneHClass;
		this.idObjPropClass = idObjPropClass;
		this.idPadre = idPadre;
	}

	public Integer getIdo() {
		return ido;
	}
	public void setIdo(Integer ido) {
		this.ido = ido;
	}
	
	public Integer getIdto() {
		return idto;
	}
	public void setIdto(Integer idto) {
		this.idto = idto;
	}

	public boolean isTieneHermanoClass() {
		return tieneHermanoClass;
	}
	public void setTieneHermanoClass(boolean tieneHermanoClass) {
		this.tieneHermanoClass = tieneHermanoClass;
	}

	public ArrayList<String> getIdObjPropClass() {
		return idObjPropClass;
	}
	public void setIdObjPropClass(ArrayList<String> idObjPropClass) {
		this.idObjPropClass = idObjPropClass;
	}

	public String getIdPadre() {
		return idPadre;
	}
	public void setIdPadre(String idPadre) {
		this.idPadre = idPadre;
	}
	public String toString() {
		String dev = "IDO: " + ido + ", IDTO: " + idto + ", TIENE_HERMANO_CLASS: " + tieneHermanoClass + ", ";
		if (idObjPropClass.size()>0) {
			dev += "ID_OBJPROPCLASS: ";
			Iterator<String> it = idObjPropClass.iterator();
			while (it.hasNext()) {
				String itN = it.next();
				dev += itN + ", ";
			}
		}
		dev += "ID_PADRE: " + idPadre;
		return dev;
	}
}

class Flag extends Object {
	private ArrayList<Integer> positionsList; //true si contiene valor, false si no lo contiene
	private HashSet<Integer> positionsNotNulls;

	public Flag() {
		this.positionsList = new ArrayList<Integer>();
		this.positionsNotNulls = new HashSet<Integer>();
	}
	
	public void addPosition(int position, int value) {
		if (value!=0) {
			this.positionsList.add(1);
			this.positionsNotNulls.add(position);
		} else
			this.positionsList.add(0);
	}
	
	public boolean getPosition(int position) {
		return this.positionsNotNulls.contains(position);
	}
	
	/*public String toHexString() {
		//ir cogiendo valores de 4 en 4
		StringBuffer hexString = new StringBuffer("");
		System.err.println("TO_HEXSTRING: " + positionsList);
		int sizeList = positionsList.size();
		System.err.println("sizeList " + sizeList);
		int indexMin = 0;
		while (indexMin<sizeList) {
			StringBuffer valueDec = new StringBuffer("");
			
			for (int i=0;i<4;i++) {
				if (indexMin+i<sizeList) {
					int val = positionsList.get(indexMin+i);
					valueDec.append(String.valueOf(val));
				}
			}
			String valueDecPad = StringUtils.rightPad(valueDec.toString(), 4, "0");
			
			long valueBin = Long.parseLong(String.valueOf(valueDecPad),2);
			String valueHex = Long.toHexString(valueBin);
			System.err.println("valueDecPad " + valueDecPad + ", valueBin " + valueBin + ", valueHex " + valueHex);
	    	
	    	hexString.append(valueHex);
	    	indexMin = indexMin + 4;
		}
		System.err.println("RESULT_HEXSTRING: " + hexString);
		return hexString.toString();
	}*/
	public String toHexString() {
		//ir cogiendo valores de 15*4 en 15*4
		int digitsBin = 15*4;
		StringBuffer hexString = new StringBuffer("");
		//System.err.println("TO_HEXSTRING: " + positionsList);
		int sizeList = positionsList.size();
		//System.err.println("sizeList " + sizeList);
		int indexMin = 0;
		while (indexMin<sizeList) {
			int digitsBinActual = digitsBin;
			int totalDigits = indexMin+digitsBin;
			if (totalDigits>sizeList) {
				digitsBinActual = sizeList-indexMin;
				int resto = digitsBinActual%4;
				if (resto!=0) {
					int groups = (digitsBinActual/4)+1;
					digitsBinActual = groups*4;
				}
			}
			//System.err.println("digitsBinActual " + digitsBinActual);

			StringBuffer valueDec = new StringBuffer("");
			for (int i=0;i<digitsBinActual;i++) {
				if (indexMin+i<sizeList) {
					int val = positionsList.get(indexMin+i);
					valueDec.append(val);
				}
			}
			String valueDecPad = StringUtils.rightPad(valueDec.toString(), digitsBinActual, "0");
			long valueBin = Long.parseLong(String.valueOf(valueDecPad),2);
			String valueHex = Long.toHexString(valueBin);
			String valueHexPad = StringUtils.leftPad(valueHex.toString(), digitsBinActual/4, "0");
			//System.err.println("valueDecPad " + valueDecPad + ", valueBin " + valueBin + ", valueHexPad " + valueHexPad);
	    	
	    	hexString.append(valueHexPad);
	    	indexMin = indexMin + digitsBin;
		}
		//System.err.println("RESULT_HEXSTRING: " + hexString);
		return hexString.toString();
	}
	
	public String toString() {
		String result = "ARRAYLIST positionsList: " + positionsList.toString() + "\nHASHSET positionsNotNulls: " + positionsNotNulls;
		return result;
	}
}


public class queryData extends Object {
	public final static int MODE_ROW = 1;  //usada en interfaz, un instance por fila
	public final static int MODE_ROOT = 2;
	public final static int MODE_ROW_ITEM = 3;
	public final static int MODE_FACT = 4;
	
    private StringBuffer content;
    private int currColumn=0;
    private final static char SEP = '#';
    private final static char SEPSEP = '.';
    private final static String FIN = "ED";
    private int rows = 0;
    private int columns=0;
    //private int idFilter=0;
    private Flag valueIndex;
    private int valueIndexHexSize=0;
    private int index;
    private int indexRowIni=0;
    
	private static Flag toFlag(String hex) {
		//System.err.println("TO_FLAG: " + hex);
		//ir cogiendo valores de 15 en 15
		int digitsHex = 15;
		Flag flag = new Flag();
		int sizeHex = hex.length();
		//System.err.println("sizeHex " + sizeHex);
		
		int indexMin = 0;
		while (indexMin<sizeHex) {
			int indexMax = (sizeHex>indexMin+digitsHex)?indexMin+digitsHex:sizeHex;
			//System.err.println("indexMin " + indexMin + ", indexMax " + indexMax);
			
			String hexActual = hex.substring(indexMin, indexMax);
			int sizeHexActual = hexActual.length();
			
			long valueDec = Long.parseLong(hexActual, 16);
			String valueBin = Long.toBinaryString(valueDec);
			String valueBinPad = StringUtils.leftPad(String.valueOf(valueBin), sizeHexActual*4, "0");
			//System.err.println("valueHex " + hex + ", valueDec " + valueDec + ", valueBinPad " + valueBinPad);
			for (int i=0;i<sizeHexActual*4;i++)
				flag.addPosition((indexMin*4)+i, Integer.parseInt(String.valueOf(valueBinPad.charAt(i))));
			indexMin = indexMin + digitsHex;
		}
		//System.err.println("RESULT_FLAG: " + flag);
		return flag;
	}

	public queryData(int rows, int columns, AttribValue[] aV, boolean initContent, int mode) {
        //this.idFilter=idFilter;
        this.rows = rows;
        this.columns = columns;
        index = 0;
        content = new StringBuffer("");
        if (initContent) {
	        content.append("ROOT_");
	        content.append("MODE" + mode + "_");
	        //index = 5;
	        index = 11;
	        if (aV!=null) {
		        for (int i=0;i<aV.length;i++) {
		        	String attrib = aV[i].getAttribute();
		        	String value = aV[i].getValue();
		        	if (i>0) {
		            	content.append(SEP);
		            	index += 1;
		        	}
		        	content.append(attrib + "=" + value);
		        	index += attrib.length() + 1 + value.length();
		        }
	        }
	    	content.append("@@");
	    	index += 2;
        }
        valueIndexHexSize = getValueIndexSize(columns);
    }
    
	public StringBuffer getContent() {
		return content;
	}
	public void addQueryData(queryData qd) {
		this.content.append(qd.getContent());
		this.updateIndex(qd.getIndex());
	}
	private void updateIndex(int index) {
		this.index += index;
	}
    public int getIndex() {
		return index;
	}
    
    public boolean hasData() {
    	boolean has = false;
    	
    	int finAtribsValues = content.indexOf("@@") + 2;
    	int relleno = finAtribsValues + FIN.length();
    	//System.err.println("relleno " + relleno);
    	//System.err.println("content.length " + content.length());
    	if (content.length()>relleno)
    		has = true;
    	
    	return has;
    }
    
    static private int getValueIndexSize(int columns){
        int raiz = (int)Math.floor((double)columns/4.0d);
        int res = raiz*4 <columns ? raiz+1:raiz;
        //System.err.println("VALUE HEX INDEX, cols "+columns+",res "+res );
        return res;
    }

    public String toString() {
    	return content.toString();
    }

    public int getColumns() {
		return columns;
	}
	public void setColumns(int columns) {
		this.columns = columns;
	}

	public int getRows() {
		return rows;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}

    public void newRow(){
    	currColumn = 0;
        valueIndex = new Flag();
    	indexRowIni = index;
        index += valueIndexHexSize;
        for (int i=0;i<valueIndexHexSize;i++)
        	content.append("*");
    }

    public void endRow(){
    	String valIndexHex = valueIndex.toHexString();
    	String valuePos = "";
        int offset = valueIndexHexSize-valIndexHex.length();
        if (offset>0) {
            for (int i=0;i<offset;i++)
            	valuePos += '0';
        }
        valuePos += valIndexHex;
        content.replace(indexRowIni, indexRowIni+valueIndexHexSize, valuePos);
    }

    public void nullColumn(){
    	valueIndex.addPosition(currColumn, 0);
        currColumn++;
    }

    public void newVal(int val) {
    	valueIndex.addPosition(currColumn, 1);
        currColumn++;
        String sV=String.valueOf(val);
        index += sV.length() + 1;
        content.append(sV + SEP);
    }

    public void newVal(double val) {
    	valueIndex.addPosition(currColumn, 1);
        currColumn++;
        String sV=String.valueOf(val);
        index += sV.length() + 1;
        content.append(sV + SEP);
    }

    public void newVal(String val) {
        if( val==null || val.length()==0 ){
            nullColumn();
            return;
        }
        if( currColumn>0){
            if (val.charAt(0) == SEP)
                val = SEPSEP + val;
            if( val.charAt(val.length()-1)==SEP )
                val+=SEPSEP;
        }

    	valueIndex.addPosition(currColumn, 1);
        currColumn++;
        val= val.replaceAll("#","##");
        index += val.length() + 1;
        content.append(val + SEP);
    }

    public void endFile() {
    	content.append(FIN);
    }

    public selectData toSelectData(Element filter) throws DataErrorException, JDOMException{
        return parse(filter, toString());
    }
    
	private static void putOP(HashMap<String,DatosOPropClass> mapDatosOPropClass, Element root, ArrayList<String> aObjPropClass) {
		Iterator iterador = root.getChildren(QueryConstants.CLASS).iterator();
		while (iterador.hasNext()) {
			Element child = (Element)iterador.next();
			String propStr = child.getAttributeValue(QueryConstants.PROP);
			if (propStr!=null) {
				int prop = Integer.parseInt(propStr);
				String idOP = child.getAttributeValue(QueryConstants.ID);
				aObjPropClass.add(idOP);
				DatosOPropClass datosOPropClass = new DatosOPropClass(prop, child.getAttributeValue(QueryConstants.NAME_PROP));
				mapDatosOPropClass.put(idOP, datosOPropClass);
			}
		}
	}

	private static int buildColumnDef(Element nodeClass, Element root, ArrayList<String> viewSelect, ArrayList<String> ordenacion, HashMap<String, columnDef> mColumnDefAttribute, 
    		HashMap<String,DatosIndiv> mapDatosIndiv, HashMap<String,DatosOPropClass> mapDatosOPropClass, 
    		ArrayList<String> aIdos) {
    	int countPropIDO = 0;
    	String name = root.getName();
    	if (name.equals(QueryConstants.ATTRIBUTE)) {
    		Element parent = root.getParent();
    		columnDef cds = new columnDef();
			int prop = Integer.parseInt(root.getAttributeValue(QueryConstants.PROP));
			int idTmRuleengine = Integer.parseInt(root.getAttributeValue(QueryConstants.ID_TM_RULEENGINE));
			cds.setIdFilter(parent.getAttributeValue(QueryConstants.ID_CLASS));

			if (StringUtils.equals(root.getAttributeValue(QueryConstants.RDN_TEMP_NO_SQ),"TRUE")) {
				cds.setRdnTmpNoSQ(true);
			}
			cds.setProp(prop);
			cds.setTm(idTmRuleengine);
    		String idSup = parent.getAttributeValue(QueryConstants.ID);
			cds.setId(idSup);
			String nameAt = root.getAttributeValue(QueryConstants.NAME_PROP);
			if (nameAt!=null)
				cds.setName(nameAt);
			String idCase = root.getAttributeValue(QueryConstants.ID_CASE);
			if (idCase!=null) {
				//si tiene un id_case buscar el case para obtener su propFilter y valueFilter
				Element nodeFilter = jdomParser.findElementByAt(nodeClass, QueryConstants.ID, idCase, true);
				String propF = nodeFilter.getAttributeValue(QueryConstants.PROP);
				cds.setPropF(Integer.parseInt(propF));
				String valueF = nodeFilter.getAttributeValue(QueryConstants.VAL_MIN)!=null ? 
						nodeFilter.getAttributeValue(QueryConstants.VAL_MIN) 
						: nodeFilter.getAttributeValue(QueryConstants.VALUE)==null ? nodeFilter.getText()
						: nodeFilter.getAttributeValue(QueryConstants.VALUE);
				cds.setValueF(valueF);
			}
			mColumnDefAttribute.put(root.getAttributeValue(QueryConstants.ID), cds);
    	} else if (StringUtils.equals(name, QueryConstants.CLASS)) {
    		Element parent = root.getParent();
    		//si es class guardar el id
    		String id = root.getAttributeValue(QueryConstants.ID);
			//iterar para ver si tiene obj prop class
			//si las tiene guardar en un mapa
			ArrayList<String> aObjPropClass = new ArrayList<String>();
			putOP(mapDatosOPropClass, root, aObjPropClass);
			String idPadre = null;
			boolean tieneHClass = false;
			if (StringUtils.equals(name, QueryConstants.CLASS) && !parent.getName().equals(QueryConstants.STRUCTURE)) {
				idPadre = parent.getAttributeValue(QueryConstants.ID);
				String nodoActual = root.getAttributeValue(QueryConstants.ID);
				Iterator it = parent.getChildren(QueryConstants.CLASS).iterator();
				while(it.hasNext()) {
					Element hijoCls = (Element)it.next();
					String nodo = hijoCls.getAttributeValue(QueryConstants.ID);
					if (!StringUtils.equals(nodoActual, nodo)) {
						tieneHClass = true;
						break;
					}
				}
			}
			
			if (ordenacion.contains(id)) {
				DatosIndiv datosIndiv = new DatosIndiv(tieneHClass,aObjPropClass,idPadre);
				mapDatosIndiv.put(id, datosIndiv);
		    	//tenga atribs o no, de todas formas guardo el ido
				countPropIDO++;
	    		columnDef cds = new columnDef();
	    		cds.setId(id);
    			cds.setIdFilter(root.getAttributeValue(QueryConstants.ID_CLASS));
    			cds.setProp(0);
				mColumnDefAttribute.put(id, cds);
	    		aIdos.add(id);
			}
			//si no hay más atributos debajo no sigo iterando
    		if (jdomParser.findAttributeSelOrWhereOrIdo(root, viewSelect, false, true)!=null) {
		    	Iterator iterador = root.getChildren().iterator();
		    	while (iterador.hasNext()) {
		    		Element child = (Element)iterador.next();
	    			countPropIDO += buildColumnDef(nodeClass, child, viewSelect, ordenacion, mColumnDefAttribute, mapDatosIndiv, mapDatosOPropClass, 
	    					aIdos);
		    	}
    		}
    	}
    	return countPropIDO;
    }

    private static columnDef[] buildQueryDef(Element root, ArrayList<SelectQuery> aSQ, HashMap<String,DatosIndiv> mapDatosIndiv, 
    		HashMap<String,DatosOPropClass> mapDatosOPropClass, ArrayList<String> aIdos, String atribsValues, int contPropFict) {
    	//ir recorriendo el nodo presentacion
    	//si no en el orden en el que llegan
    	String ordenacion = "";
		String[] aVs = atribsValues.split("#");
		for (int i=0;i<aVs.length;i++) {
			String aV = aVs[i];
			String[] avSep = aV.split("=");
			if (StringUtils.equals(avSep[0], QueryConstants.SELECT_IDO))
				ordenacion = avSep[1];
		}
		Element nodoStructure = root.getChild(QueryConstants.STRUCTURE);
    	Element presentation = nodoStructure.getChild(QueryConstants.PRESENTATION);
    	Element view = null;
    	String viewSelect = "";
		if (presentation!=null) {
			view = presentation.getChild(QueryConstants.VIEW);
			viewSelect = view.getAttributeValue(QueryConstants.SELECT);
		}
		HashMap<String, columnDef> mColumnDefAttribute = new HashMap<String, columnDef>();
		//pasar el mapa de individuo, en el no se rellena el ido
		Iterator it = nodoStructure.getChildren(QueryConstants.CLASS).iterator();
		while (it.hasNext()) {
			Element primerNodo = (Element)it.next();
			buildColumnDef(primerNodo, primerNodo, Auxiliar.stringToArray(viewSelect, ","), Auxiliar.stringToArray(ordenacion, ","), mColumnDefAttribute, mapDatosIndiv, 
					mapDatosOPropClass, aIdos);
		}

		/*System.err.println("mColumnDefAttribute ");
		Iterator it2 = mColumnDefAttribute.keySet().iterator();
		while (it2.hasNext()) {
			String id = (String)it2.next();
			columnDef cd = mColumnDefAttribute.get(id);
			System.err.println(id + ":" + cd);
		}
		System.err.println("fin mColumnDefAttribute ");*/

		//System.err.println("ordenacion " + ordenacion);
    	String[] numOrden = ordenacion.split(",");
		int tamano = numOrden.length;
		//columnDef -> prop, to y tm
		columnDef[] def = new columnDef[tamano];
		for (int i=0;i<tamano;i++) {
			String id = numOrden[i];
			columnDef colD = mColumnDefAttribute.get(id);
			if (colD!=null) {
				//System.err.println("colD " + colD);
				String sup = colD.getId();
				int tmRuleengine = colD.getTm();
				columnDef cd = new columnDef();
				//cd.setTo(colD.getTo());
				cd.setName(colD.getName());
				cd.setIdFilter(colD.getIdFilter());
				if (sup!=null) {
					cd.setId(sup);
					if (colD.getProp()==0) { //si prop es 0 es un ido
						cd.setProp(0);
					} else {
						//cd.setName(colD.getName());
						int prop = colD.getProp();
						if (!colD.isRdnTmpNoSQ()) {
							SelectQuery sq = new SelectQuery(colD.getIdFilter(),prop, colD.getPropF(), colD.getValueF());
							//System.err.println("sq " + sq);
							aSQ.add(sq);
						}
						cd.setTm(tmRuleengine);
						cd.setProp(colD.getProp());
						cd.setPropF(colD.getPropF());
						cd.setValueF(colD.getValueF());
					}
				} else {
					SelectQuery sq = new SelectQuery(colD.getIdFilter(),colD.getProp(), null, null);
					aSQ.add(sq);
					//cd.setName(colD.getName());
					cd.setTm(tmRuleengine);
					cd.setProp(colD.getProp());   //property nueva -ID
				}
				def[i] = cd;
			}
		}
        return def;
    }

    public static selectData parse(Element root, String data) throws DataErrorException {
    	selectData sD = new selectData();
    	//System.err.println("mode---" + data.substring(9, 10) + "---");
        int mode = Integer.parseInt(data.substring(9, 10));
   		parse(root, data, sD, null, null, true, mode);
   		return sD;
    }
    public static IndividualData parseIndividualData(Element root, String data) throws DataErrorException {
    	IndividualData ind = new IndividualData();
    	ArrayList<IPropertyDef> aipd = new ArrayList<IPropertyDef>();
   		parse(root, data, null, aipd, null, true, queryData.MODE_ROOT);
   		ind.addAIPropertyDef(aipd);
   		return ind;
    }
    public static ArrayList<RowItem> parseRowItem(Element root, String data) throws DataErrorException {
    	ArrayList<RowItem> ai = new ArrayList<RowItem>();
   		parse(root, data, null, null, ai, true, queryData.MODE_ROW_ITEM);
   		return ai;
    }

    public static void parse(Element root, String data, selectData res, ArrayList<IPropertyDef> aipd, 
    		ArrayList<RowItem> ai, boolean resultInSD, int mode) throws DataErrorException {
    	
        int max = data.length();
        //int pos = data.indexOf("ROOT_") + 5;
        int finAtribsValues = data.indexOf("@@");
    	String atribsValues = data.substring(11,finAtribsValues);
    	//System.err.println("atribsValues " + atribsValues);

        int pos = finAtribsValues + 2;
        //System.err.println("data " + data);
        /*try {
			System.err.println("root " + jdomParser.returnXML(root));
		} catch (JDOMException e) {
			e.printStackTrace();
		}*/
        //System.err.println("PARSER:" + max + "," + pos);

        if (pos>0 && max-pos>=4) {
	        HashMap<String,DatosIndiv> mapDatosIndiv = new HashMap<String,DatosIndiv>();
	        HashMap<String,DatosOPropClass> mapDatosOPropClass = new HashMap<String,DatosOPropClass>();
	        ArrayList<String> aIdos = new ArrayList<String>();
	        ArrayList<SelectQuery> aSQ = new ArrayList<SelectQuery>();
	        int contPropFict = -1;
	        columnDef[] def = buildQueryDef(root, aSQ, mapDatosIndiv, mapDatosOPropClass, aIdos, atribsValues, contPropFict);
	        //de buildQueryDef se puede sacar el selectQuery
	        if (res!=null)
	        	res.setSelect(aSQ);
	        /*Iterator it = aSQ.iterator();
	        while (it.hasNext()) {
				System.err.println("sq " + ((SelectQuery)it.next()));
	        }*/
	        int columns = def.length;
        	/*System.err.println("def");
	        for (int i=0;i<columns;i++)
	        	System.err.println(def[i]);
        	System.err.println("fin def");*/
	        //se le suma (aIdos.size() + 1) porque muestra el idto
	        int columnsTotal = (aIdos.size()) + columns;
	        
	        //System.err.println("columnsTotal " + columnsTotal);
	        int currColumn=0;
	        int currColumnBit=0;
	        Flag valueIndex=null;
	
	        int valueIndexHexSize=getValueIndexSize(columnsTotal);
	        //System.err.println("valueIndexHexSize " + valueIndexHexSize);
	        
	        instance obj = null;
	        RowItem rI = null;
	        int columnsRowItem = 0;
	        if (ai!=null)
	        	columnsRowItem = aSQ.size()+2; //1ª posicion es null, ultima posicion es idto
	        
	        //recorro por filas, por cada fila creo un instance
	        boolean inicialized = false;
	        while (max-pos>=4) {
	            //System.err.println("ITERA POS "+pos+",COL "+currColumn+",valueIndexHexSize "+valueIndexHexSize);
	            /*if (max - pos < 4) {
	                break;
	            }*/
	            if( currColumn==0 ){
	            	//obtener aquí el número añadido al principio de la fila
	            	//System.err.println("substring " + data.substring(pos,pos + valueIndexHexSize));
	            	String valueIndexHex = data.substring(pos,pos + valueIndexHexSize);
	            	valueIndex = queryData.toFlag(valueIndexHex);
	            	//System.err.println("valueIndexHexSize " + valueIndexHexSize + ", valueIndex " + valueIndex);
	                pos+=valueIndexHexSize;
	            }
	            columnDef colD = def[currColumn];
	            boolean entrar = false;
	            int tmRuleengine = 0;
				if (colD.getProp()!=0)
					tmRuleengine = colD.getTm();
	        	//System.err.println("currColumn " + currColumn);
	        	//System.err.println("currColumnBit " + currColumnBit);
                if(valueIndex.getPosition(currColumnBit))
                	entrar = true;

				//System.err.println("entrar " + entrar);
	        	if (entrar) {
	//            if(( bitToInt[currColumnBit] & valueIndexPos )!=0 ){
	                //Si es cero la columna viene nula
    	        	if (tmRuleengine==Constants.IDTO_STRING || tmRuleengine==Constants.IDTO_MEMO)
    	        		if( data.charAt(pos)==SEPSEP && data.charAt(pos+1)==SEP && data.charAt(pos+2)==SEP)
    	        			pos++;
    	        	
	                int next = data.indexOf(SEP, pos);
	                //System.err.println("NEXT "+next);
	                int desp=1;
	
    	        	if (tmRuleengine==Constants.IDTO_STRING || tmRuleengine==Constants.IDTO_MEMO) {
		                while( max - pos > 3 && data.charAt(next+desp)==SEP ){
		                    //Tengo que saltarme sep por parejas
		                    //System.err.println("SEP DUPLI "+data.substring(pos,next+1));
		                    if( data.charAt(next+desp+1)!=SEP ){
		                        //no actuaba como separador, busco siguiente
		                        int tmpNext=data.indexOf(SEP, next+desp+1);
		                        next = tmpNext;
		                        desp=1;
		                    }else{
		                        desp += 2;
		                    }
		                }
    	        	}
	                //System.err.println("colD " + colD);
                	//int idto = colD.getTo();
	            	if (colD.getProp()==0) {
	                	//puede que el ultimo IDO no sea el correspondiente a la property, depende del orden
	                	//por eso hay guardar el id en la estructura y antes crear un mapa en el que se guarde el valor del ido junto con su id
	                	Integer tableIdActual = Integer.parseInt(data.substring(pos, next));
	                	Integer idtoActual = null;
	                	
	                	pos = next + 1;
                        next = data.indexOf(SEP, pos);
                        idtoActual = Integer.parseInt(data.substring(pos, next));
	                	Integer idoActual = QueryConstants.getIdo(tableIdActual, idtoActual);
	                	//System.err.println("tableIdActual " + tableIdActual + ", idtoActual " + idtoActual + ", idoActual " + idoActual);
		                
	                	if (currColumn==0) {
		                    if(res!=null) {
		                    	if (!inicialized) {
			                        //creo el instance
		                    		if (mode==queryData.MODE_ROOT) {
		                    			//buscar instance en res
		                    			obj = res.get(idoActual);
		                    			if (obj==null) {
					                        obj = new instance(idtoActual,idoActual);
					                        res.addInstance(obj);
		                    			}
		                    		} else {
				                        obj = new instance(idtoActual,idoActual);
				                        res.addInstance(obj);
		                    		}
			                        inicialized = true;
		                    	} else {
		                    		break;
		                    	}
		                    } else if (ai!=null) {
		                    	if (!inicialized) {
		                    		rI = new RowItem(ai.size(), columnsRowItem, new ArrayList());
		                    		GIdRow gId = new GIdRow(idoActual, idtoActual, null);
		                    		rI.setIdRow(gId);
		                    		rI.setColumnData(0, null);
		                    		rI.setColumnData(columnsRowItem-1, idtoActual);
		                    		ai.add(rI);
		                    		inicialized = true;
		                    	} else {
		                    		break;
		                    	}
		                    }
	                	}
	                	//actualiza el mapa con los idos, para que cuando obtenga properties de este individuo pueda saber su ido
	                	DatosIndiv datosIndiv = mapDatosIndiv.get(colD.getId());
	                	if (datosIndiv!=null) {
		                	datosIndiv.setIdo(idoActual);
		                	datosIndiv.setIdto(idtoActual);
		                	boolean compruebaInstance = false;
		                	LinkedList<Integer> lls = new LinkedList<Integer>();
		                	//con las Object Property
		                	ArrayList<String> idsObjPropClass = datosIndiv.getIdObjPropClass();
		                	for (int i=0;i<idsObjPropClass.size();i++) {
		                		String idOP = idsObjPropClass.get(i);
		                		DatosOPropClass datosOPropClass = mapDatosOPropClass.get(idOP);
		                		if (datosOPropClass!=null) {
			                		int prop = datosOPropClass.getProperty();
			                		if (lls.contains(prop)) {
			                			compruebaInstance = true;
			                			break;
			                		} else
			                			lls.add(prop);
		                		}
		                	}
		                	for (int i=0;i<idsObjPropClass.size();i++) {
		                		String idOP = idsObjPropClass.get(i);
		                		DatosIndiv indivOProp = mapDatosIndiv.get(idOP);
		                		if (indivOProp!=null) {
			                		DatosOPropClass datosOPropClass = mapDatosOPropClass.get(idOP);
		                    		insertObjectProperty(obj, aipd, rI, idoActual, idtoActual, colD.getIdFilter(), indivOProp.getIdo(), 
		                    				indivOProp.getIdto(), datosOPropClass.getProperty(), compruebaInstance, 
		                    				datosOPropClass.getName(), null, null, null, mode);
		                		}
		                	}
		                	String idPadre = datosIndiv.getIdPadre();
		                	if (idPadre!=null) {
		                		String id = colD.getId();
		                		DatosOPropClass datosOPropClass = mapDatosOPropClass.get(id);
		                		if (datosOPropClass!=null) {
			                		DatosIndiv padre = mapDatosIndiv.get(idPadre);
			                		if (padre!=null) {
				                		Integer idoPadre = padre.getIdo();
				                		Integer idtoPadre = padre.getIdto();
			                    		insertObjectProperty(obj, aipd, rI, idoPadre, idtoPadre, colD.getIdFilter(), idoActual, 
			                    				idtoActual, datosOPropClass.getProperty(), datosIndiv.isTieneHermanoClass(), 
			                    				datosOPropClass.getName(), null, null, null, mode);
			                		}
		                		}
		                	}
	                	}
	                } else {
	                	String datos = null;
	                    if(valueIndex.getPosition(currColumnBit))
	                		datos = data.substring(pos, next);
	                	//coger el idoActual del mapa
	                	String idSup = colD.getId();
	                	Integer idoActual = -1;
	                	Integer idtoActual = -1;
	                	if (idSup!=null) {
		                	DatosIndiv datosIndiv = mapDatosIndiv.get(idSup);
		                	if (datosIndiv!=null) {
			                	idoActual = datosIndiv.getIdo();
			                	idtoActual = datosIndiv.getIdto();
		                	}
	                	}
	                	if(!inicialized) {
	                		if (res!=null) {
		                		if (idoActual!=null && idoActual.equals(-1)) {
			                        //creo el instance
			                        obj = new instance(-1,-1);
			                        res.addInstance(obj);
			                        inicialized = true;
		                    	} else {
		                    		break;
		                    	}
	                		}
	                	}
	                	//System.err.println("datos " + datos);
	                	//System.err.println("tmRuleengine " + tmRuleengine);
	                	if (datos!=null) {
		                	insertDataProperty(obj, aipd, rI, idoActual, idtoActual, colD.getIdFilter(), colD.getProp(), tmRuleengine,
		                			datos, true, colD.getName(), colD.getPropF(), colD.getValueF(), aSQ, mode);
	                	}
	                }
	                pos = next + 1;
	            }
	            if( currColumn<columns-1 ) {
	                currColumn++;
	            	currColumnBit++;
	            	if (colD.getProp()==0)
	            		currColumnBit++;
	            } else {
	            	inicialized = false;
	                currColumn=0;
	            	currColumnBit=0;
	            	for (int i=0;i<aIdos.size();i++) {
	            		DatosIndiv datosIndiv = mapDatosIndiv.get(aIdos.get(i));
	            		datosIndiv.setIdo(null);
	            		datosIndiv.setIdto(null);
	                }
	            }
	        }
        }
//        if (ai!=null) {
//        	for (RowItem rowItem : ai) {
//				System.err.println(rowItem);
//			}
//        }
//        System.err.println("instance devuelto " + res.toString());
    }

	private static boolean compInteger(Integer a, Integer b) {
		boolean iguales = false;
		if (a!=null && b!=null && a.equals(b) || a==null && b==null)
			iguales = true;
		return iguales;
	}

	/*Estos 2 métodos deben estar sincronizados con la lógica utilizada en KnowledgeAdapter para la creación de 
	 * ObjectProperty y DataProperty.*/
    private static void insertObjectProperty(instance obj, ArrayList<IPropertyDef> aipd, RowItem rI, Integer idoIndiv, Integer idtoIndiv, String idoFilter, 
    		Integer idoOP, Integer claseOP, int prop, boolean compruebaInstance, /*boolean isEnumerated, */String name, 
    		Integer propF, String valueF, ArrayList<SelectQuery> aSQ, int mode) {
    	boolean encontrada = false;
    	//System.err.println("idoIndiv " + idoIndiv);
    	//System.err.println("idoOP " + idoOP);
        //System.err.println("prop " + prop);
    	if (idoIndiv!=null && idoOP!=null) {
    		if (compruebaInstance || mode==queryData.MODE_ROOT) {
    	        if (obj!=null) {
	    			Iterator it = obj.getRelationIterator(idoIndiv);
	    			if (it!=null) {
		    			while (it.hasNext()) {
		    				ObjectProperty op = (ObjectProperty)it.next();
		    				if (op.getIdo()!=null && op.getIdo().equals(idoIndiv) && 
		    						compInteger(op.getIdto(), idtoIndiv) && 
		    						op.getIdProp()!=null && op.getIdProp().equals(prop)) {
		    			        LinkedList<Value> lv = op.getValues();
		    			        //iterar xa comprobar si esta ya en la lista
		    			        Iterator itLv = lv.iterator();
		    			        while(itLv.hasNext()) {
		    			        	ObjectValue val = (ObjectValue)itLv.next();
		    			        	if (val.getValue().equals(idoOP) && val.getValueCls()==claseOP)
		    			        		encontrada = true;
		    			        }
		    			        if (!encontrada) {
									ObjectValue ov = new ObjectValue();
							        ov.setValue(idoOP);
							        ov.setValueCls(claseOP);
							        lv.add(ov);
							        encontrada = true;
		    			        }
		    					break;
		    				}
		    			}
	    			}
    	        } else if (aipd!=null) {
    				Iterator it = aipd.iterator();
        			while (it.hasNext()) {
        				FactInstance fact = (FactInstance)it.next();
        				if (fact.getIDO()!=null && fact.getIDO().equals(idoIndiv) && 
        						compInteger(fact.getIDTO(), idtoIndiv) && 
        						fact.getPROP()==prop) {
        					FactInstance ipd = new FactInstance(idtoIndiv,idoIndiv,prop,String.valueOf(idoOP),claseOP,null,null,null,name);
    						ipd.setExistia_BD(true);
				        	aipd.add(ipd);
				        	encontrada = true;
        					break;
        				}
    				}
    	        }
    		}
    		if (!encontrada) {
		        ObjectProperty p = new ObjectProperty();
		        if (idoIndiv!=-1)
		        	p.setIdo(idoIndiv);
		        p.setIdto(idtoIndiv);
		        p.setIdProp(prop);
		        p.setName(name);
    			/*if (!isEnumerated) {
    				LinkedList<Integer> li = new LinkedList<Integer>();
			        li.add(idoFilter);
			        p.setRangoList(li);
    			}*/
		        LinkedList<Value> lov = new LinkedList<Value>();
		        ObjectValue ov = new ObjectValue();
		        ov.setValue(idoOP);
		        ov.setValueCls(claseOP);
//		        ov.setOrder(action.GET);
		        if (aipd!=null) {
					FactInstance ipd = new FactInstance(idtoIndiv,idoIndiv,prop,String.valueOf(idoOP),claseOP,null,null,null,name);
					ipd.setExistia_BD(true);
		        	aipd.add(ipd);
		        }

		        if (idoFilter!=null && aSQ!=null) {
		        	if (obj!=null || rI!=null) {
			        	int col = 1;
			        	Iterator<SelectQuery> it = aSQ.iterator();
			        	while (it.hasNext()) {
			        		SelectQuery sq = it.next();
			        		if (StringUtils.equals(sq.getIdObject(),idoFilter) && sq.getIdProp()==prop) {
			        			if (propF==null && sq.getPropFilter()==null) {
			        				if (obj!=null)
			        					obj.addValueSQ(sq.toString(), ov);
			        				else if (rI!=null)
			        					rI.setColumnData(col, idoOP);
			        			} else if (sq.getPropFilter()!=null && propF!=null && sq.getPropFilter()==propF && 
			        				StringUtils.equals(sq.getValueFilter(), valueF)) {
			        				if (obj!=null)
			        					obj.addValueSQ(sq.toString(), ov);
			        				else if (rI!=null)
			        					rI.setColumnData(col, idoOP);
			        			}
			        		}
			        		col++;
			        	}
		        	}
		        }
		        
		        lov.add(ov);
		        p.setValues(lov);
		        //String key = idoIndiv==-1?"-1":idoFilter;
		        //idoFilter puede que sea nulo si no viene de QueryXML (en APS)
		        if (obj!=null) {
			        //if (idoFilter!=null)
			        	//obj.addProperty(idoIndiv, idoFilter, p);
			        //else
			        	obj.addProperty(idoIndiv, p);
		        }
    		}
    	}
    }

    private static void insertDataProperty(instance obj, ArrayList<IPropertyDef> aipd, RowItem rI, Integer idoActual, Integer idtoActual, String idoFilter, int prop, 
    		int tmRuleengine, String value, boolean isIDO, String name, Integer propF, String valueF, ArrayList<SelectQuery> aSQ, int mode) {
        if (prop == 0 || idoActual==null) return;
        if(isIDO && (tmRuleengine==Constants.IDTO_STRING || tmRuleengine==Constants.IDTO_MEMO) &&
            value.indexOf(SEP)!=-1 )
        	value = value.replaceAll(""+SEP+SEP,""+SEP);

        //TODO buscar en SelectQuery
        //si coincide la property, el ido FICTICIO, propF y valueF -> meter en mapa SelectQuery y value
        //idoFilter puede que sea nulo si no viene de QueryXML (en APS)
        boolean encontrada = false;
    	if (mode==queryData.MODE_ROOT) {
    		if (obj!=null) {
				Iterator<Property> it = obj.getAllPropertyIterator(idoActual);
				if (it!=null) {
	    			while (it.hasNext()) {
	    				Property p = it.next();
	    				if (p instanceof DataProperty) {
							DataProperty dp = (DataProperty) p;
		    				if (dp.getIdo()!=null && dp.getIdo().equals(idoActual) && 
		    						compInteger(dp.getIdto(), idtoActual) && 
		    						dp.getIdProp()!=null && dp.getIdProp().equals(prop)) {
						        encontrada = true;
		    					break;
		    				}
						}
	    			}
				}
    		} else {
				Iterator it = aipd.iterator();
    			while (it.hasNext()) {
    				FactInstance fact = (FactInstance)it.next();
    				if (fact.getIDO()!=null && fact.getIDO().equals(idoActual) && 
    						compInteger(fact.getIDTO(), idtoActual) && 
    						fact.getPROP()==prop) {
				        encontrada = true;
    					break;
    				}
				}
    		}
    	}
   		if (!encontrada) {
	        DataProperty p = new DataProperty();
	        p.setIdo(idoActual);
	        p.setIdto(idtoActual);
	        p.setIdProp(prop);
	        p.setName(name);
	        
	        if (isIDO) {
	        	Object valueRI = null;
		    	p.setDataType(tmRuleengine);
		    	
		        LinkedList<Value> ldv = new LinkedList<Value>();
		        Value v = null;
		        //guardar en los q
		        if (tmRuleengine==Constants.IDTO_UNIT) {
		            v = new UnitValue();
		        	if (value!=null) {
		        		((UnitValue)v).setValueMin(Double.parseDouble(value));
		        		((UnitValue)v).setValueMax(Double.parseDouble(value));
		        	}
		        	valueRI = Double.parseDouble(value);
		        	//no se usa
		            //if (clase!=null)
		        	//	((UnitValue)v).setUnit(Integer.parseInt(clase));
	//		        dv.setOrder(action.GET);
		            if (aipd!=null) {
						FactInstance ipd = new FactInstance(idtoActual, idoActual, prop, null, tmRuleengine, Double.parseDouble(value), Double.parseDouble(value), null, name);
						ipd.setExistia_BD(true);
						aipd.add(ipd);
		            }
		        } else if (tmRuleengine==Constants.IDTO_INT) {
		            v = new IntValue();
		        	if (value!=null) {
		        		((IntValue)v).setValueMin((int)Double.parseDouble(value));
		        		((IntValue)v).setValueMax((int)Double.parseDouble(value));
		        	}
		        	valueRI = (int)Double.parseDouble(value);
	//	        	iv.setOrder(action.GET);
		            if (aipd!=null) {
		            	FactInstance ipd = new FactInstance(idtoActual, idoActual, prop, null, tmRuleengine, Double.parseDouble(value), Double.parseDouble(value), null, name);
						ipd.setExistia_BD(true);
		            	aipd.add(ipd);
		            }
		        } else if (tmRuleengine==Constants.IDTO_DOUBLE) {
		        	v = new DoubleValue();
		        	if (value!=null) {
		        		((DoubleValue)v).setValueMin(Double.parseDouble(value));
		        		((DoubleValue)v).setValueMax(Double.parseDouble(value));
		        	}
		        	valueRI = Double.parseDouble(value);
	//	   	     fv.setOrder(action.GET);
		            if (aipd!=null) {
		            	FactInstance ipd = new FactInstance(idtoActual, idoActual, prop, null, tmRuleengine, Double.parseDouble(value), Double.parseDouble(value), null, name);
						ipd.setExistia_BD(true);
		            	aipd.add(ipd);
		            }
		        } else if (tmRuleengine==Constants.IDTO_BOOLEAN) {
		            v = new BooleanValue();
		        	if (value!=null) {
			            if ((int)Double.parseDouble(value)==1) {
			            	((BooleanValue)v).setBvalue(true);
				        	valueRI = true;
			            } else if ((int)Double.parseDouble(value)==0) {
			            	((BooleanValue)v).setBvalue(false);
				        	valueRI = false;
			            }
		        	}
	//	    	    bv.setOrder(action.GET);
		            if (aipd!=null) {
		            	FactInstance ipd = new FactInstance(idtoActual, idoActual, prop, null, tmRuleengine, Double.parseDouble(value), Double.parseDouble(value), null, name);
						ipd.setExistia_BD(true);
		            	aipd.add(ipd);
		            }
		        } else if (tmRuleengine==Constants.IDTO_TIME || tmRuleengine==Constants.IDTO_DATETIME || tmRuleengine==Constants.IDTO_DATE) {
		            v = new TimeValue();
		            if (value!=null) {
		            	((TimeValue)v).setRelativeSecondsMin((long)Double.parseDouble(value));
		            	((TimeValue)v).setRelativeSecondsMax((long)Double.parseDouble(value));
		            }
		        	valueRI = (long)Double.parseDouble(value)*Constants.TIMEMILLIS;
	//	  	      tv.setOrder(action.GET);
		            if (aipd!=null) {
		            	FactInstance ipd = new FactInstance(idtoActual, idoActual, prop, null, tmRuleengine, Double.parseDouble(value), Double.parseDouble(value), null, name);
						ipd.setExistia_BD(true);
		            	aipd.add(ipd);
		            }
		        } else if (tmRuleengine==Constants.IDTO_STRING || tmRuleengine==Constants.IDTO_MEMO || tmRuleengine==Constants.IDTO_IMAGE || tmRuleengine==Constants.IDTO_FILE) {
		            v = new StringValue();
		            ((StringValue)v).setValue(value);
		        	valueRI = value;
	//	 	       dv.setOrder(action.GET);
		            if (aipd!=null) {
		            	FactInstance ipd = new FactInstance(idtoActual, idoActual, prop, value, tmRuleengine, null, null, null, null, name);
						ipd.setExistia_BD(true);
		            	aipd.add(ipd);
		            }
		        }
//		        System.err.println("valueRI " + valueRI);

		        if (idoFilter!=null) {
			        if (obj!=null || rI!=null) {
			        	int col = 1;
			        	Iterator<SelectQuery> it = aSQ.iterator();
			        	while (it.hasNext()) {
			        		SelectQuery sq = it.next();
			        		if (StringUtils.equals(sq.getIdObject(),idoFilter) && sq.getIdProp()==prop) {
			        			if (propF==null && sq.getPropFilter()==null) {
			        				if (obj!=null)
			        					obj.addValueSQ(sq.toString(), v);
			        				else if (rI!=null)
			        					rI.setColumnData(col, valueRI);
			        			} else if (sq.getPropFilter()!=null && propF!=null && sq.getPropFilter()==propF && 
			        				StringUtils.equals(sq.getValueFilter(), valueF)) {
			        				if (obj!=null)
			        					obj.addValueSQ(sq.toString(), v);
			        				else if (rI!=null)
			        					rI.setColumnData(col, valueRI);
			        			}
			        		}
			        		col++;
			        	}
			        }
		        }
	            ldv.add(v);
		        p.setValues(ldv);
	        }
	        //String key = idoActual==-1?"-1":idoFilter;
	        //idoFilter puede que sea nulo si no viene de QueryXML (en APS)
	        if (obj!=null) {
	        	if (idoFilter!=null)
		        	obj.addProperty(idoActual, idoFilter, p);
		        else
		        	obj.addProperty(idoActual, p);
	        }
   		}
    }
    
    public static selectData parse(Element root, ResultSet rs, IKnowledgeBaseInfo ik, int mode) throws DataErrorException, SQLException {
    	boolean init = false;
    	//int mode = queryData.MODE_ROOT;
    	selectData res = new selectData();
    	HashMap<String,DatosIndiv> mapDatosIndiv = new HashMap<String,DatosIndiv>();
        HashMap<String,DatosOPropClass> mapDatosOPropClass = new HashMap<String,DatosOPropClass>();
        ArrayList<String> aIdos = new ArrayList<String>();
        ArrayList<SelectQuery> aSQ = new ArrayList<SelectQuery>();
        int columnsTotal = 0;
        columnDef[] def = null;
        instance obj = null;
        while (rs.next()) {
            int currColumn = 0;
            boolean inicialized = false;
    		if (!init) {
	    		init = true;
	    		Element viewRoot = root.getChild(QueryConstants.STRUCTURE).getChild(QueryConstants.PRESENTATION).getChild(QueryConstants.VIEW);
	    		String atribsValues = QueryConstants.SELECT_IDO + "=" + viewRoot.getAttributeValue(QueryConstants.SELECT_IDO);
		        int contPropFict = -1;
		        def = buildQueryDef(root, aSQ, mapDatosIndiv, mapDatosOPropClass, aIdos, atribsValues, contPropFict);
		        //de buildQueryDef se puede sacar el selectQuery
		        res.setSelect(aSQ);
		        int columns = def.length;
		        //se le suma (aIdos.size() + 1) porque muestra el idto
		        columnsTotal = (aIdos.size()) + columns;
		        if (mode==queryData.MODE_ROW)
		        	columnsTotal++;
		        //System.err.println("tam.getTamanoAgreg() " + tam.getTamanoAgreg());
		        //System.err.println("columns " + columns);
		        //System.err.println("aIdos.size() " + aIdos.size());
		        //System.err.println("columnsTotal " + columnsTotal);
    		}
			//qd.newRow();
			int c = 1;
			for (int countWithEnum = 1; countWithEnum <= columnsTotal; countWithEnum++) {
				//System.err.println("countWithEnum " + countWithEnum);
				//System.err.println("currColumn " + currColumn);
	    		columnDef colD = def[currColumn];
	    		if (colD.getProp()==0) {
					//estamos ante un ido, el siguiente es un idto
					//System.err.println("cIdo " + c);
	    			Integer idoActual = null;
					Integer tableIdActual = rs.getInt(c);
					if (rs.wasNull())
						tableIdActual = null;
					c++;
					countWithEnum++;
					Integer idtoActual = rs.getInt(c);
					if (rs.wasNull())
						idtoActual = null;
					if (tableIdActual!=null)
	                	idoActual = QueryConstants.getIdo(tableIdActual, idtoActual);
					if(currColumn==0){
                    	if (idoActual!=null && !inicialized) {
	                        //creo el instance
	                        obj = new instance(idtoActual,idoActual);
	                        res.addInstance(obj);
	                        inicialized = true;
                    	} else {
                    		break;
                    	}
                    }
					if (idoActual!=null) {
	                	//actualiza el mapa con los idos, para que cuando obtenga properties de este individuo pueda saber su ido
	                	DatosIndiv datosIndiv = mapDatosIndiv.get(colD.getId());
	                	if (datosIndiv!=null) {
		                	datosIndiv.setIdo(idoActual);
		                	datosIndiv.setIdto(idtoActual);
		                	boolean compruebaInstance = false;
		                	LinkedList<Integer> lls = new LinkedList<Integer>();
		                	//con las Object Property
		                	ArrayList<String> idsObjPropClass = datosIndiv.getIdObjPropClass();
		                	for (int i=0;i<idsObjPropClass.size();i++) {
		                		String idOP = idsObjPropClass.get(i);
		                		DatosOPropClass datosOPropClass = mapDatosOPropClass.get(idOP);
		                		if (datosOPropClass!=null) {
			                		int prop = datosOPropClass.getProperty();
			                		if (lls.contains(prop)) {
			                			compruebaInstance = true;
			                			break;
			                		} else
			                			lls.add(prop);
		                		}
		                	}
		                	for (int i=0;i<idsObjPropClass.size();i++) {
		                		String idOP = idsObjPropClass.get(i);
		                		DatosIndiv indivOProp = mapDatosIndiv.get(idOP);
		                		if (indivOProp!=null) {
			                		DatosOPropClass datosOPropClass = mapDatosOPropClass.get(idOP);
		                    		insertObjectProperty(obj, null, null, idoActual, idtoActual, colD.getIdFilter(), indivOProp.getIdo(), 
		                    				indivOProp.getIdto(), datosOPropClass.getProperty(), compruebaInstance, 
		                    				datosOPropClass.getName(), null, null, null, mode);
		                		}
		                	}
		                	String idPadre = datosIndiv.getIdPadre();
		                	if (idPadre!=null) {
		                		String id = colD.getId();
		                		DatosOPropClass datosOPropClass = mapDatosOPropClass.get(id);
		                		if (datosOPropClass!=null) {
			                		DatosIndiv padre = mapDatosIndiv.get(idPadre);
			                		if (padre!=null) {
				                		Integer idoPadre = padre.getIdo();
				                		Integer idtoPadre = padre.getIdto();
			                    		insertObjectProperty(obj, null, null, idoPadre, idtoPadre, colD.getIdFilter(), idoActual, 
			                    				idtoActual, datosOPropClass.getProperty(), datosIndiv.isTieneHermanoClass(), 
			                    				datosOPropClass.getName(), null, null, null, mode);
			                		}
		                		}
		                	}
	                	}
                	}
				} else {
		            int tmRuleengine = colD.getTm();
					//System.err.println("cDato " + c);
					String datos = null;
					//System.err.println("columna " + c);
	                if (tmRuleengine == Constants.IDTO_STRING || tmRuleengine == Constants.IDTO_MEMO) {
						//System.err.println("string");
						String str = rs.getString(c);
						if (!rs.wasNull())
							datos = str;
					} else if (tmRuleengine == QueryConstants.TM_ID) {
						//System.err.println("int");
						int val = rs.getInt(c);
						if (!rs.wasNull())
							datos = String.valueOf(val);
	                } else {
						//System.err.println("double");
						double val = rs.getDouble(c);
						if (!rs.wasNull())
							datos = String.valueOf(val);
					}
	                
					boolean entrar = datos!=null;
					if (entrar) {
//						coger el idoActual del mapa
	                	String idSup = colD.getId();
	                	Integer idoActual = -1;
	                	Integer idtoActual = -1;
	                	//System.err.println("idSup " + idSup);
	                	if (idSup!=null) {
		                	DatosIndiv datosIndiv = mapDatosIndiv.get(idSup);
		                	if (datosIndiv!=null) {
			                	idoActual = datosIndiv.getIdo();
			                	idtoActual = datosIndiv.getIdto();
		                	}
	                	}
	                	if(!inicialized) {
	                		if (idoActual!=null && idoActual.equals(-1)) {
		                        //creo el instance
		                        obj = new instance(-1,-1);
		                        res.addInstance(obj);
		                        inicialized = true;
	                    	} else {
	                    		break;
	                    	}
	                	}
	                	//System.err.println("datos " + datos);
	                	if (datos!=null) {
		                	insertDataProperty(obj, null, null, idoActual, idtoActual, colD.getIdFilter(), colD.getProp(), tmRuleengine,
		                			datos, true, colD.getName(), colD.getPropF(), colD.getValueF(), aSQ, mode);
	                	}
	                }
				}
				c++;
				currColumn++;
			}
			//qd.endRow();
        	for (int i=0;i<aIdos.size();i++) {
        		DatosIndiv datosIndiv = mapDatosIndiv.get(aIdos.get(i));
        		datosIndiv.setIdo(null);
        		datosIndiv.setIdto(null);
            }
		}
        return res;
    }

	public static Document parseXmlData(Element root, String data) throws DataErrorException, JDOMException {
		Document doc = ((Element)jdomParser.readXML(data).getContent().get(0)).getDocument();
   		return doc;
	}
}
