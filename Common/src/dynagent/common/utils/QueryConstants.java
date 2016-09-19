package dynagent.common.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.lang.time.DateFormatUtils;

import dynagent.common.Constants;

public class QueryConstants {
	
	//exclusivo para atributos de reports a applet
   	public static final String PATH_FILE="PATH_FILE";
   	public static final String DIRECT_IMPRESION="DIRECT_IMPRESION";
   	public static final String N_COPIES="N_COPIES";
   	public static final String DISPLAY_PRINT_DIALOG="DISPLAY_PRINT_DIALOG";
   	public static final String PREPRINT_SEQUENCE="PREPRINT_SEQUENCE";
   	public static final String POSTPRINT_SEQUENCE="POSTPRINT_SEQUENCE";
   	
	//constantes para id_tm en query
	public static final int TM_ID=4;
	public static final int TM_IDO_FICTICIO=8;
	
	//operacion en where
   	public static final int OP_AND=1;
   	public static final int OP_OR=2;
   	public static final String NOT="NOT";
   	//order: ascendente, descendente
   	public static final int ORDER_ASC=1;
   	public static final int ORDER_DESC=2;
   	
   	//nodos
   	public static final String QUERY="QUERY";
   	public static final String STRUCTURE="STRUCTURE";
   	public static final String CLASS="CLASS";
   	public static final String ATTRIBUTE="ATTRIBUTE";
   	public static final String WHERE="WHERE";
   	public static final String CASE="CASE";
   	public static final String PRESENTATION="PRESENTATION";
   	public static final String VIEW="VIEW";
   	public static final String CONDITION="CONDITION";
   	//public static final String LOGIC="LOGIC";
   	public static final String LOGIC_WHERE="LOGIC_WHERE";
   	public static final String OP="OP";  //también usado como atributo en nodos WHERE
   	
   	//atributos
   	public static final String TARGET_CLASS="TARGET_CLASS";   //exclusivo para reports
   	public static final String FORMAT="FORMAT";   //exclusivo para reports
   	public static final String EXCEL="EXCEL";   //exclusivo para reports
   	public static final String CLASS_NAME="CLASS_NAME";
   	public static final String ID_O="ID_O";
   	//public static final String TABLE_ID="TABLE_ID";
   	public static final String NOT_ID_O="NOT_ID_O";
   	//public static final String NOT_TABLE_ID="NOT_TABLE_ID";
   	public static final String PROP="PROP";
   	public static final String NAME="NAME";
   	public static final String ALIAS="ALIAS";
   	public static final String VALUE="VALUE";
   	public static final String VAL_MIN="VAL_MIN";
   	public static final String VAL_MAX="VAL_MAX";
   	public static final String SELECT="SELECT";
   	public static final String ORDERBY="ORDERBY";
   	public static final String ID_OP="ID_OP";
   	public static final String ID_CONDITION="ID_CONDITION";
   	public static final String ID_CASE="ID_CASE";
   	public static final String ID="ID";
   	public static final String REQUIRED="REQUIRED";  //que no sea nulo
   	public static final String NULL="NULL";    //que sea nulo
   	public static final String INNER="INNER";
   	public static final String TYPE="TYPE";
   	public static final String INT="INT";
	public static final String DOUBLE="DOUBLE";
   	public static final String DATE="DATE";
   	public static final String MEMO="MEMO";
   	public static final String ISNULL="ISNULL";
   	
   	//atributos internos
   	public static final String ID_TO="ID_TO";	//es CLASS_NAME adaptado
   	public static final String NAME_PROP="NAME_PROP";
   	public static final String ID_TM_RULEENGINE="ID_TM_RULEENGINE";
   	//public static final String INDEX="INDEX";	//se asigna a los ids
   	public static final String ID_CLASS="ID_CLASS";	//se obtiene a partir del id
   	//public static final String RDN_TEMP="RDN_TEMP";
   	public static final String RDN_TEMP_NO_SQ="RDN_TEMP_NO_SQ";
   	public static final String REVERSED="REVERSED";
   	public static final String SELECT_IDO="SELECT_IDO";
   	public static final String ORDERBY_NCOL="ORDERBY_NCOL";
   	public static final String LIMIT="LIMIT";
   	public static final String ID_PARAM="ID_PARAM";
   	//public static final String ID_TO_ABSTRACT="ID_TO_ABSTRACT";
   	public static final String IDS_FICTICIOS="IDS_FICTICIOS";
   	public static final String LINK="LINK";
   	
   	//operaciones en where y having
   	public static final String BETWEEN="BETWEEN";
   	public static final String DISTINTO="!=";
   	public static final String DISTINTO_VALIDO="<>";
   	public static final String IGUAL="=";
   	public static final String LIKE="LIKE";
   	public static final String CONTAINS="CONTAINS";
   	public static final String MENOR="<";
   	public static final String MENOR_IGUAL="<=";
   	public static final String MAYOR=">";
   	public static final String MAYOR_IGUAL=">=";
   	public static final String REG_EXPR="REG_EXPR";
   	public static final String NOT_REG_EXPR="NOT_REG_EXPR";
   	
	//public static final String VALCLS = "VALCLS";
	//public static final String ID_QUERY = "ID_QUERY";
	//public static final String PARAMS_REPORTS = "PARAMS_REPORTS";
	//public static final String PARAMS = "PARAMS";
	public static final String IDTO_REPORT = "IDTO_REPORT";
	
	// TODO Asignar los valores correctos a las constantes creadas temporalmente a null.
	public static final String ID_RIGHT = null;
	public static final String AGREGATION = null;
	public static final String EXPRESION = null;
	public static final String GROUP = null;
	public static final String ATTRIBUTE_CASE = null;
	public static final String GROUPBY = null;
	public static final String HAS_SUBREPORT = null;
	public static final Object HAVING = null;
	public static final String ID_LEFT = null;
	public static final String ID_RIGHT_MIN = null;
	public static final String ID_RIGHT_MAX = null;
	public static final String IS_EXPRESION = null;
	public static final String IS_RDN = null;
	public static final String LAST = null;
	public static final String LOGIC_HAVING = null;
	public static final String MULTIPLE_CARD = null;
	public static final String OP_AGREG = null;
	public static final String OP_EXPRES = null;
	public static final String OP_EXPRES_MYSQL = null;
	public static final String OP_EXPRES_SQLSERVER = null;
	public static final String ORDER = null;
	public static final String REPORT = null;
	public static final String REQUIRED_IF_FIXED = null;
	public static final String REQUIRED_PRES = null;
	public static final String SHOW_NULL = null;
	public static final String SHOW_OBLIGATORY = null;
	public static final String SIG_IDS = null;
	public static final int TM_NULL =-1;
	public static final int TM_VALUE_RDN = -1;
	public static final String UNION = null;
	public static final int XOR = -1;
	
	public static final int TABLEIDOFFSET = 100;
	
	public static final int[] clscode={125,133,198,338,404,427,628}; 
	//125= Ticket venta,
	//133= COBRO_ANTICIPO
	//198= LÍNEA_MATERIA
	//338= STOCK
	//404= DESGLOSE_IVA
	//427= LÍNEA_ARTÍCULOS_MATERIA
	//628= LINEA_ROTACION


	/*Estas dos funciones de conversion ido-tableId se usan:
	 * Para las búsquedas: en QueryXML y queryData -> instance lleva ido y xml de query lleva tableId
	 * Para get: en communicator antes de enviar mensaje a server
	 * Antes de llamar a la función getRdn de server*/
/*VERSION CODIFICA IDOS*/
	public static int getTableId(int ido) {		
		if(ido<0)  return getTableIdNoCompress(ido);
		if(ido%2==1){
			ido=ido-1;
			ido=ido/2;			
			int res=ido/10;
			return res-TABLEIDOFFSET;
		}else{
			ido=ido/2;
			//System.out.println("GET TABLEID C2 "+ido+" id "+ ido/1000);
			return ido/1000;
		}			   		
   	}
	public static int getIdto(int ido) {
		if(ido<0) return getIdtoNoCompress(ido);
		
		if(ido%2==1){
			ido=ido-1;
			ido=ido/2;
			int clase= ido%10;			
			return clscode[clase];									
		}else{
			ido=ido/2;			
			return ido%1000;
		}	
   	}
   	public static int getIdo(int tableId, int idto) {
   		if(tableId>0){
   			int clspos= Arrays.binarySearch(clscode,idto);
   			int clase=idto;
   			if(clspos>=0){
   	   			int tableIdOff=tableId+TABLEIDOFFSET;//para que no colisiones con ids de clases que son menor de 1000
   				clase=clspos;   				 
   				return (tableIdOff*10+clase)*2+1;   			
   			}else{
   				if((tableId*1000L+clase)*2>Integer.MAX_VALUE){
   					System.out.println("ERROR, el table id supera el integer maximo y no tiene codificacion especial (tableid, idto: "+ tableId+","+ idto);
   				}
   				return (tableId*1000+clase)*2;
   			}
   		}else   		
   			return getIdoNoCompress(tableId,idto);
   	}
   	public static int compressIdo(int ido){
   		int tid=getTableIdNoCompress(ido);
   		int idto=getIdtoNoCompress(ido);
   		return getIdo(tid,idto);
   	}
   	public static int decompressIdo(int ido){
   		int tid=getTableId(ido);
   		int idto=getIdto(ido);
   		return getIdoNoCompress(tid,idto);
   	}
	public static int getTableIdNoCompress(int ido) {
		//if(ido<0) return ido;
   		return ido/1000;
   	}
	public static int getIdtoNoCompress(int ido) {
		int tableId = getTableIdNoCompress(ido);
		if(ido<0) return -(Math.abs(ido)-Math.abs(tableId)*1000);
   		return ido-(tableId*1000);
   	}
   	public static int getIdoNoCompress(int tableId, int idto) {
   		int ido= (Math.abs(tableId) * 1000 + idto)*(tableId<0?-1:1);
   		return ido;
   	}
   	
   	public static HashMap<Integer, HashSet<Integer>> getIdtoTableIds(HashSet<Integer> idos, boolean compresed) {
   		HashMap<Integer, HashSet<Integer>> idtoTableIds = new HashMap<Integer, HashSet<Integer>>();
   		Iterator it = idos.iterator();
   		while (it.hasNext()) {
   			Integer ido = (Integer)it.next();
   			addMap(idos, ido, idtoTableIds,compresed);
   		}
   		return idtoTableIds;
   	}
   	private static void addMap(HashSet<Integer> idos, int ido, HashMap<Integer, HashSet<Integer>> idtoTableIds, boolean compresed) {
   		int tableId = compresed? QueryConstants.getTableId(ido):QueryConstants.getTableIdNoCompress(ido);
   		int idto =  compresed? QueryConstants.getIdto(ido):QueryConstants.getIdtoNoCompress(ido);
   		HashSet<Integer> hsTableIds = idtoTableIds.get(idto);
   		if (hsTableIds==null) {
   			hsTableIds = new HashSet<Integer>();
   			idtoTableIds.put(idto, hsTableIds);
   		}
		hsTableIds.add(tableId);
   	}
   	
   	public static String getPattern(int idTmRuleEngine) {
		String pattern = "";
		if (idTmRuleEngine==Constants.IDTO_DATE) pattern = "dd/MM/yy";
		else if (idTmRuleEngine==Constants.IDTO_DATETIME) pattern = "dd/MM/yy HH:mm:ss";
		else if (idTmRuleEngine==Constants.IDTO_TIME) pattern = "HH:mm:ss";
		
		return pattern;
   	}
   	
   	public static Long dateToSeconds(String pattern, String value) throws ParseException {
		Long seconds = null;
		if (value!=null) {
			if (value.equals("TODAY_INI")) {
				seconds = System.currentTimeMillis()/Constants.TIMEMILLIS;
				value = secondsToDate(String.valueOf(seconds),"dd/MM/yy");
				value += " 00:00:00";
				pattern = "dd/MM/yy HH:mm:ss";
			} else if (value.equals("TODAY_FIN")) {
				seconds = System.currentTimeMillis()/Constants.TIMEMILLIS;
				value = secondsToDate(String.valueOf(seconds),"dd/MM/yy");
				value += " 23:59:59";
				pattern = "dd/MM/yy HH:mm:ss";
			}
			SimpleDateFormat dateFormat=new SimpleDateFormat(pattern);
			seconds = dateFormat.parse(value).getTime()/Constants.TIMEMILLIS;
		}
		return seconds;
	}
   	
   	public static String secondsToDate(String text, String pattern){
		String dateInic="";
//		System.out.println("text "+text);
		long time=Double.valueOf(text).longValue();
		dateInic = DateFormatUtils.format(new Date(time*Constants.TIMEMILLIS), pattern);
//		System.out.println("DateINI "+dateInic);
		return dateInic;
	}
	
   	/*public static Double parserDate(String value) {
		Double fecha = null;
		String[] fechaSpl = value.split("/");
		if (fechaSpl.length==3) {
			String diaStr = fechaSpl[0];
			String mesStr = fechaSpl[1];
			String añoStr = fechaSpl[2];
			if (Auxiliar.hasIntValue(diaStr) && Auxiliar.hasIntValue(mesStr) && Auxiliar.hasIntValue(añoStr)) {
				Integer dia = Integer.parseInt(diaStr);
				Integer mes = Integer.parseInt(mesStr);
				Integer año = Integer.parseInt(añoStr);
				if (dia>=1 && dia<=31 && mes>=1 && mes<=12) {
					Calendar fechaInicio = Calendar.getInstance();
					fechaInicio.set(año, mes-1, dia);
					fecha = fechaInicio.getTimeInMillis()/Constants.TIMEMILLIS;
				}
			}
		}
		return fecha;
	}
   	public static Double parserTime(String value) throws EvalError {
		Double time = null;
		String[] timeSpl = value.split(":");
		if (timeSpl.length==3) {
			String horaStr = timeSpl[0];
			String minStr = timeSpl[1];
			String segStr = timeSpl[2];
			if (Auxiliar.hasIntValue(horaStr) && Auxiliar.hasIntValue(minStr) && Auxiliar.hasIntValue(segStr)) {
				Integer hora = Integer.parseInt(horaStr);
				Integer min = Integer.parseInt(minStr);
				Integer seg = Integer.parseInt(segStr);
				if (hora>=0 && hora<=60 && min>=0 && min<=60 && seg>=0 && seg<=60) {
					Interpreter i=new Interpreter();
					time = Double.parseDouble(String.valueOf((Integer)i.eval(hora + "*3600+" + min + "*60+" + seg)));
				}
			}
		}
		return time;
	}
   	public static Double parserDateTime(String value) throws EvalError {
		String[] fechaTimeSpl = value.split("-");
		Double dateTime = null;
		if (fechaTimeSpl.length==2) {
			dateTime = parserDate(fechaTimeSpl[0]);
			if (dateTime!=null) {
				Double dateTimeTemp = parserTime(fechaTimeSpl[1]);
				if (dateTimeTemp!=null)
					dateTime += dateTimeTemp;
			}
		}
		return dateTime;
	}*/
   	
}
