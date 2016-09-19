package dynagent.common.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;






import org.apache.commons.lang.time.DateFormatUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.communication.queryData;
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
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.StringValue;
import dynagent.common.properties.values.TimeValue;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.Session;
import dynagent.common.xml.QueryXML;


public  class Auxiliar {

	public static Long beginCrono=null;
	
	public static String readFile(String path) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path));
		StringBuffer res = new StringBuffer("");
		String line;
		while ((line = in.readLine()) != null){
			//System.out.println("LEYENDO..");
			res.append(line + "\n");
		}
		in.close();
		return res.toString();
	}
	
	public static String getDelegationFromFile(String path){
		File paramsFile=new File(path);
		String delegationRdn=null;
		System.err.println("Fichero params:"+paramsFile.getAbsolutePath());
		
		String line = "";
		String id,valor;
		
		BufferedReader reader=null;
		try{
			reader = new BufferedReader(new FileReader(paramsFile));
			while((line = reader.readLine()) != null){
				String[] data = line.split(" ");
				id="";
				if(data[0].contains("delegacion")){
					delegationRdn=data[1].replace("\"", "");
					System.out.println("Delegacion de Fichero "+delegationRdn);
					break;
				}
			}
		}catch(Exception e2){
			e2.printStackTrace();
		}
		finally{
			try{
			if(reader!=null) reader.close();
			}catch(Exception e3){e3.printStackTrace();}
		}
		return delegationRdn;
	}
	
	public static String[] exePostgreQuery(String sql,String postgrePath,int dbport,String databaseName,String user,String password,Runtime runtime) throws IOException{
		if(runtime==null){
			runtime = Runtime.getRuntime();
		}
		String envp[]=new String[2];
		envp[0]="PGUSER="+user;
		envp[1]="PGPASSWORD="+password;
		
		String[] prog = new String[9];
		prog[0] = "\""+postgrePath+"\\bin\\psql.exe\""; // The DOS external command we're using
		prog[1] = "-h";
		prog[2] = "localhost";
		prog[3] = "-p";
		prog[4] = ""+dbport;
		prog[5] = "-w";
		prog[6] = "-c";			
		prog[7] = sql;
		prog[8] = databaseName;
		System.out.println("SQL "+sql);
		Process process = runtime.exec(prog,envp); // Execute the program
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		String line,error="",outstr="";
		while ((line = reader.readLine()) != null){
			System.err.println(line);
			error+="\n"+line;
		}
		reader.close();
		reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		while ((line = reader.readLine()) != null){
			System.err.println(line);
			outstr+="\n"+line;
		}
		reader.close();
		return new String[]{outstr,error};
	}
	
	public static boolean incrementoRelevante(Double oldValue, Double newValue, double porciento_1_based){
		if((oldValue==null)!=(newValue==null)||oldValue==null||newValue==null) return true;
		double oldAbs=Math.abs(oldValue.doubleValue());
		double newAbs=Math.abs(newValue.doubleValue());
		
		if(Math.abs(newAbs-oldAbs)>=1) return true;
		
		return oldAbs+newAbs>porciento_1_based && (Math.abs(oldAbs-newAbs)>newAbs*porciento_1_based && Math.abs(oldAbs-newAbs)>oldAbs*porciento_1_based  || newValue*oldValue<0 );
	}
	
	public static boolean checkSystemProcess(String name,Runtime runtime) throws IOException, InterruptedException{
		if(runtime==null){
			runtime = Runtime.getRuntime();
		}
		String arg[]=new String[3];
		arg[0]="tasklist";
		arg[1]="/FI";
		arg[2]="Imagename eq "+name;
		
		String line;
		Process proc =runtime.exec(arg);			
		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		boolean existe=false;
					
		while ((line = reader.readLine()) != null){
			if(line.contains(name))	existe=true;
			System.out.println(line);
			line = line.toUpperCase();
		}
		System.out.println("Existe cloud "+existe);
		proc.waitFor();
		return existe;		
	}
	
	public static String[] checkSystemService(String name,Runtime runtime) throws IOException, InterruptedException{
		if(runtime==null){
			runtime = Runtime.getRuntime();
		}
		String arg[]=new String[4];
		arg[0]="sc";
		arg[1]="query";
		arg[2]="state=";
		arg[3]="all";
		
		
		String line;
		Process proc =runtime.exec(arg);			
		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		boolean iniBlock=false;
		boolean running=false;
		while ((line = reader.readLine()) != null){
			if(iniBlock && (line.contains("ESTADO")||line.contains("STATE"))){
				running= line.contains("RUNNING");
				System.out.println("checkSystemService: running "+running+" line:"+line);
				
				break;
			}
			if(!iniBlock && line.contains(name)){
				name=line.substring(line.indexOf(":")).trim();
				System.out.println("checkSystemService: NOMBRE SERVICIO "+name);
				iniBlock=true;
			}
			System.out.println(line);
			line = line.toUpperCase();
		}
		System.out.println("checkSystemService: Running "+name+" "+running);
		//proc.waitFor();
		return new String[]{name,""+running};
	}
	
	public static String extract_parametro_rdn_accion_procesado(String rdn, String parametro){
		// rdn no tiene nombre de accion. El formato es parametro1=valor.parametro2=valor
		String valor=null;
	    if( !rdn.contains(parametro) ) return null;
	    
	    int pos=rdn.indexOf(parametro);
	    int last=rdn.indexOf(".",pos+1);
	    int inivalor=rdn.indexOf("=",pos)+1;
	    	    
	    if(last>0) valor= rdn.substring(inivalor,last);
	    else valor= rdn.substring(inivalor);
	    System.err.println("Parametro "+parametro+ " valor "+valor+" input " +rdn);
	    
	    return valor;
	}
	
	public static String createSqlIdtoIdos(HashMap<Integer,HashSet<Integer>> hIdtoIdos, HashSet<Integer> hAllIdos, boolean and) {
		String sql = "";
		boolean first = true;
		if (and)
			sql += " AND (";
		Iterator it = hIdtoIdos.keySet().iterator();
		while (it.hasNext()) {
			Integer idto = (Integer)it.next();
			HashSet<Integer> hIdos = hIdtoIdos.get(idto);
			if (!first)
				sql += " OR ";
			else
				first = false;
			sql += "id_to=" + idto + " AND id_o IN(" + Auxiliar.hashSetIntegerToString(hIdos, ",") + ")";
			if (hAllIdos!=null)
				hAllIdos.addAll(hIdos);
		}
		if (and)
			sql +=")";
		return sql;
	}
	
	public static HashSet<Integer> getTableIdsHashSet(HashMap<Integer,HashSet<Integer>> idtoIdos) {
		HashSet<Integer> hAllTableIds = new HashSet<Integer>();
		for (HashSet<Integer> idos : idtoIdos.values()) {
			for (Integer ido : idos) {
				hAllTableIds.add(QueryConstants.getTableId(ido));
			}
		}
		return hAllTableIds;
	}
	public static String createSqlIdtoRdns(HashMap<Integer,HashSet<String>> hIdtoRdns, boolean and) {
		String sql = "";
		boolean first = true;
		if (and)
			sql += " AND (";
		Iterator it = hIdtoRdns.keySet().iterator();
		while (it.hasNext()) {
			Integer idto = (Integer)it.next();
			HashSet<String> hRdns = hIdtoRdns.get(idto);
			if (!first)
				sql += " OR ";
			else
				first = false;
			sql += "ID_TO=" + idto + " AND VAL_TEXTO IN(" + Auxiliar.hashSetToStringComillas(hRdns, ",") + ")";
		}
		if (and)
			sql +=")";
		return sql;
	}
	public static HashSet<Integer> getIdosHashSet(HashMap<Integer,HashSet<Integer>> idtoIdos) {
		HashSet<Integer> hAllIdos = new HashSet<Integer>();
		Iterator it = idtoIdos.keySet().iterator();
		while (it.hasNext()) {
			Integer idto = (Integer)it.next();
			HashSet<Integer> hIdos = idtoIdos.get(idto);
			hAllIdos.addAll(hIdos);
		}
		return hAllIdos;
	}
	public static boolean hasIntValue(String value){
		try{
			new Integer(value);
			return true;
		}
		catch(Exception ex){
			return false;
		}
	}
	
	/*public static String IBAN_check_code(){
		;
	}
	
	public static String AT_22_check_code(String pais,String input){
		input=input+pais+"00";
	}*/
	
	public static String id_persona_SEPA(String code_pais,String nif, String sufijo){
		return code_pais+modulo_97_10(nif+code_pais+"00")+sufijo+nif;
	}
	
	public static  String getIBAN(String codigoPais,String cuentaLocal)  {
		return codigoPais+modulo_97_10(cuentaLocal+codigoPais+"00")+cuentaLocal;
	}
	
	public static  String modulo_97_10(String code)  {
        int modulusResult = modulo_97(code);
        int charValue = (98 - modulusResult);
        System.out.println("mod adicional "+(charValue % 97));
        String checkDigit = Integer.toString(charValue);
        return (charValue > 9 ? checkDigit : "0" + checkDigit);
    }
    
	public static int modulo_97(String input){
        long total = 0;
        int MODULUS=97;
        long MAX = 999999999;
        for (int i = 0; i < input.length(); i++) {        	
            int charValue = Character.getNumericValue(input.charAt(i));            
            if (charValue >= 0 && charValue <= 35) {
                total = (charValue > 9 ? total * 100 : total * 10) + charValue;       
                if (total > MAX) {                	
                    total = (total % MODULUS);
                }
            }
        }        
        
        return (int)(total % MODULUS);
	}
	
	public static boolean hasFloatValue(String value){
		try{
			new Float(value);
			return true;
		}
		catch(Exception ex){
			return false;
		}
	}
	public static boolean hasDoubleValue(String value){
		try{
			new Double(value);
			return true;
		}
		catch(Exception ex){
			return false;
		}
	}
	
	/* Pasa un ArrayList a un String separando sus elementos por un separador.*/
	public static String arrayToString(ArrayList<String> ar, String separador) {
		//System.out.println("Inicio de la funcion arrayToString");
		String dev = "";
		if (ar!=null) {
			Iterator it = ar.iterator();
			while (it.hasNext()) {
				if (dev.length()>0)
					dev += separador;
				dev += ((String)it.next());
			}
		}
		//System.out.println("Fin de la funcion arrayToString");
		return dev;
	}
	public static String arrayToStringComillas(ArrayList<String> ar, String separador) {
		//System.out.println("Inicio de la funcion arrayToString");
		String dev = "";
		if (ar!=null) {
			Iterator it = ar.iterator();
			while (it.hasNext()) {
				if (dev.length()>0)
					dev += separador;
				dev += "'" + ((String)it.next()) + "'";
			}
		}
		//System.out.println("Fin de la funcion arrayToString");
		return dev;
	}
	/* Pasa un ArrayList a un String separando sus elementos por un separador.*/
	public static String hashSetToStringComillas(HashSet<String> ar, String separador) {
		//System.out.println("Inicio de la funcion hashSetToStringComillas");
		String dev = "";
		if (ar!=null) {
			Iterator it = ar.iterator();
			while (it.hasNext()) {
				if (dev.length()>0)
					dev += separador;
				dev += "'" + ((String)it.next()) + "'";
			}
		}
		//System.out.println("Fin de la funcion hashSetToStringComillas");
		return dev;
	}
	/* Pasa un ArrayList a un String separando sus elementos por un separador.*/
	public static String arrayIntegerToString(ArrayList<Integer> ar, String separador) {
		//System.out.println("Inicio de la funcion arrayIntegerToString");
		String dev = "";
		if (ar!=null)
			dev = iteratorIntegerToString(ar.iterator(), separador);
		//System.out.println("Fin de la funcion arrayIntegerToString");
		return dev;
	}
	/* Pasa un HashSet a un String separando sus elementos por un separador.*/
	public static String hashSetIntegerToString(HashSet<Integer> ar, String separador) {
		//System.out.println("Inicio de la funcion hashSetIntegerToString");
		String dev = "";
		if (ar!=null)
			dev = iteratorIntegerToString(ar.iterator(), separador);
		//System.out.println("Fin de la funcion hashSetIntegerToString");
		return dev;
	}
	/* Pasa un HashSet a un String separando sus elementos por un separador.*/
	public static String hashSetStringToString(HashSet<String> ar, String separador) {
		//System.out.println("Inicio de la funcion hashSetStringToString");
		String dev = "";
		if (ar!=null)
			dev = iteratorStringToString(ar.iterator(), separador);
		//System.out.println("Fin de la funcion hashSetStringToString");
		return dev;
	}
	/* Pasa un Iterator a un String separando sus elementos por un separador.*/
	public static String iteratorIntegerToString(Iterator<Integer> it, String separador) {
		//System.out.println("Inicio de la funcion iteratorIntegerToString");
		String dev = "";
		while (it.hasNext()) {
			if (dev.length()>0)
				dev += separador;
			dev += String.valueOf((Integer)it.next());
		}
		//System.out.println("Fin de la funcion iteratorIntegerToString");
		return dev;
	}
	/* Pasa un Iterator a un String separando sus elementos por un separador.*/
	public static String iteratorStringToString(Iterator<String> it, String separador) {
		//System.out.println("Inicio de la funcion iteratorIntegerToString");
		String dev = "";
		while (it.hasNext()) {
			if (dev.length()>0)
				dev += separador;
			dev += String.valueOf((String)it.next());
		}
		//System.out.println("Fin de la funcion iteratorIntegerToString");
		return dev;
	}
	/* Pasa un String con sus elementos unidos por un separador a un ArrayList.*/
	public static ArrayList<String> stringToArray(String ar, String separador) {
		//System.out.println("Inicio de la funcion stringToArray");
		ArrayList<String> dev = new ArrayList<String>();
		if (ar!=null && ar.length()>0) {
			String[] devSpl = ar.split(separador);
			for (int i=0;i<devSpl.length;i++)
				dev.add(devSpl[i]);
		}
		//System.out.println("Fin de la funcion stringToArray");
		return dev;
	}
	/* Pasa un String con sus elementos unidos por un separador a un ArrayList.*/
	public static ArrayList<Integer> stringToArrayInteger(String ar, String separador) {
		//System.out.println("Inicio de la funcion stringToArrayInteger");
		ArrayList<Integer> dev = new ArrayList<Integer>();
		if (ar!=null && ar.length()>0) {
			String[] devSpl = ar.split(separador);
			for (int i=0;i<devSpl.length;i++)
				dev.add(Integer.parseInt(devSpl[i]));
		}
		//System.out.println("Fin de la funcion stringToArrayInteger");
		return dev;
	}
	/* Pasa un String con sus elementos unidos por un separador a un HashSet.*/
	public static HashSet<Integer> stringToHashSetInteger(String ar, String separador) {
		//System.out.println("Inicio de la funcion stringToHashSetInteger");
		HashSet<Integer> dev = new HashSet<Integer>();
		if (ar!=null && ar.length()>0) {
			String[] devSpl = ar.split(separador);
			for (int i=0;i<devSpl.length;i++)
				dev.add(Integer.parseInt(devSpl[i]));
		}
		//System.out.println("Fin de la funcion stringToHashSetInteger");
		return dev;
	}
	/* Pasa un String con sus elementos unidos por un separador a un HashSet.*/
	public static HashSet<String> stringToHashSetString(String ar, String separador) {
		//System.out.println("Inicio de la funcion stringToHashSetString");
		HashSet<String> dev = new HashSet<String>();
		if (ar!=null && ar.length()>0) {
			String[] devSpl = ar.split(separador);
			for (int i=0;i<devSpl.length;i++)
				dev.add(devSpl[i]);
		}
		//System.out.println("Fin de la funcion stringToHashSetString");
		return dev;
	}

	public static ArrayList<String> stringArrayToArrayList(String[] ars) {
		ArrayList<String> ar= new ArrayList<String>();
		if(!ars[0].equals("")){
			for (int i=0;i<ars.length;i++){
				ar.add(ars[i]);
			}
		}
		return ar;
	}
	public static String integerArrayToString(Integer[] ars, String separador) {
		String result = "";
		if(!ars[0].equals("")){
			for (int i=0;i<ars.length;i++){
				if (result.length()>0)
					result += separador;
				result += String.valueOf(ars[i]);
			}
		}
		return result;
	}

   public static String hashMapSetStringToString(HashMap<String,HashSet<String>> hm, String separadorExterno, String separadorInterno){
	   String result = "";
	   if(hm!=null){
		   for(Iterator<String> it=hm.keySet().iterator();it.hasNext();){
			   String key=it.next();
			   HashSet<String> set = hm.get(key);
			   if (result.length()>0)
				   result += ";";
			   result += key + separadorExterno + Auxiliar.hashSetStringToString(set, separadorInterno);
		   }
	   }
	   return result;
   } 

   public static String hashMapSetStringToStringComillas(HashMap<String,HashSet<String>> hm, String separadorExterno, String separadorInterno){
	   String result = "";
	   if(hm!=null){
		   for(Iterator<String> it=hm.keySet().iterator();it.hasNext();){
			   String key=it.next();
			   HashSet<String> set = hm.get(key);
			   if (result.length()>0)
				   result += ";";
			   result += "'" + key + "'" + separadorExterno + Auxiliar.hashSetToStringComillas(set, separadorInterno);
		   }
	   }
	   return result;
   } 

	public static ArrayList<String> getNewElements(ArrayList<String> list1, ArrayList<String> list2) {
		ArrayList<String> result= new ArrayList<String>();
		Iterator<String> itl1=list1.iterator();
		while(itl1.hasNext()){
			String elem=itl1.next();
			if(!list2.contains(elem))
				result.add(elem);
		}
		return result;
	}
	public static String getStringNewElementsInteger(ArrayList<Integer> list1, ArrayList<Integer> list2, String separador) {
		String result = "";
		Iterator<Integer> itl1=list1.iterator();
		while(itl1.hasNext()){
			Integer elem=itl1.next();
			if(!list2.contains(elem)) {
				if (result.length()>0)
					result += separador;
				result += elem;
			}
		}
		return result;
	}
	
	public static String getDate() {
		long start = System.currentTimeMillis();
		Date dateActual = new Date(start);
		return DateFormatUtils.format(dateActual, "yyyy-MM-dd HH:mm:ss");
	}
	
	public static Date getNowDate() {
		long start = System.currentTimeMillis();
		Date dateActual = new Date(start);
		return dateActual;
	}
	
	/************************************************************************
    *	MÉTODO:		getNaturalNumber                                      *					  			
    *	DESCRIPCIÓN:*//**                                                   *
    *              La funcion LeerNumeroNatural se encarga de leer por
    *              teclado un numero natural(entero mayor o igual a 1)   <br>
    *  @return		numero natural	                                      <br>
    *  @date		21-Junio-2007                                        <br>
    *  @author 	 Jose Antonio Zamora Aguilera                          <br>
	 ************************************************************************/
	   public  static Integer getNaturalNumber() 
	   {
	    
	   	Integer natural = null;    	
	   	String entradaTeclado = null;   	
	   	boolean error=true; 
	   	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  
	   	do
		{		
		     try
		     {
		         //System.out.println("Introduzca un entero mayor o igual que 1:");
		         entradaTeclado = br.readLine();
		         if(!entradaTeclado.equals("")){
		        	 natural=Integer.parseInt(entradaTeclado); //cambiamos el tipo a entero
		        	 error=(natural<1);                        //chequeamos si es un natural
		         }
		     }
		     catch (NumberFormatException e)          //capturamos error en el formato
	            {            
		         System.out.println(" Entrada no es valida: introduzca un número entero o intro para null");
		         error=true;
		     }   
		     catch (IOException ioe)                    //capturamos error en IO
	         {
	             System.out.println("IOException en  getNaturalNumber()");   		
	         }
	     }while(error&&!entradaTeclado.equals(""));		 
	     
	   	return natural;
	    }
	   
	   
	   /************************************************************************
	    *	MÉTODO:		getFloatNumber                                      				  			
	    *	DESCRIPCIÓN:*//**                                                   
	    *              La funcion getFloatNumber se encarga de leer por
	    *              teclado un numero real   <br>
	    *  @return	    numero real leido                                 <br>
	    *  @date		21-Junio-2007                                     <br>
	    *  @author 	 Jose Antonio Zamora Aguilera                         <br>
		 ************************************************************************/
	   public static  Float getFloatNumber() 
	   {
	   		Float q = null;    	
	   		String entradaTeclado="";   	
	   		boolean error=true; 
	   		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  
		   	do
			{  try
			     {
			         //System.out.println("Introduzca un real");
			         entradaTeclado = br.readLine();
			         if(!entradaTeclado.equals("")){
			        	 q=Float.valueOf(entradaTeclado);
			         }
			     }catch (NumberFormatException e){            
			         System.out.println(" Entrada no válida: introduzca un número real o intro para null");
			         error=true;
			     }   
			     catch (IOException ioe)                    //capturamos error en IO
		         {
		             System.out.println("IOException en getFloatNumber");   		
		         }
		    }while(error&&!entradaTeclado.equals(""));		
		      
		   	return q;
		 }
	   
	   
	   /************************************************************************
	    *	MÉTODO:		hashMapToString					  			
	    *	DESCRIPCIÓN:*//**                                                   *
	    *             hashMapToString construye y devuelve una representación    
	    *             textual de un HashMap <br>
	    *  
	    *  @return	String con la representación textual	<br>
	    *  @param:  HashMap del que se quiere una representación
	    *  @param:  nameKey nombre que se quiere mostrar asociado al key del hashMap
	    *  @param:  nameValue nombre que se quiere mostrar asociado al value del hashMap
	    *  	                                      <br>
	    *  @date		25-Junio-2007                                        <br>
	    *  @author 	 Jose Antonio Zamora Aguilera                          <br>
		 ************************************************************************/
	   public static String  hashMapToString(HashMap hm,String nameKey,String nameValue ){
		   String result = null;
		   if(hm!=null){
			   for(Iterator it=hm.keySet().iterator();it.hasNext();){
				   Object key=it.next();
				   if(result!=null)
					   result+="\n"+nameKey+key.toString()+nameValue+hm.get(key);
				   else
					   result="\n  "+nameKey+key.toString()+nameValue+hm.get(key);
			   }
		   }
		   return result;
		   } 
	   
	   	public static String leeTexto(String mensaje){
			  InputStreamReader f=new InputStreamReader(System.in);
			  BufferedReader teclado = new BufferedReader(f); 
			  System.out.println(mensaje);
			  try{
				  String leido=teclado.readLine();
				  if(leido.equals(""))
						  return null;
				  else 
					  return leido;
		    }catch (Exception e){
		      System.out.println(e.getMessage());
		    }
		    return null;
	   	} 
	   
	   
	   /************************************************************************
	    *	MÉTODO:		 getChangesInLinkedList				  			
	    *	DESCRIPCIÓN:*//**                                                   *
	    *            Compara dos listas de objetos para ver cambios desde una
	    *            (la denominada oldList) con la otra (denominada newList)  
	    *            y construye tres listas:
	    *            una con los objetos comunes, otra con los nuevos y otra
	    *            con los viejos <br>
	    *  
	    *  @return HashMap cuyas claves son los identificadores de tipo de información
	    *  que agrupa (commons, olds, news) y los valores son las listas  con la información	<br>
	    *  @param: LinkedList oldList
	    *  @param: LinkedList newList
	    *  	                                      <br>
	    *  @date		27-Junio-2007                                        <br>
	    *  @author 	 Jose Antonio Zamora Aguilera                          <br>
		 ************************************************************************/
	   
	   	public static HashMap<String,LinkedList> getChangesInLinkedList(LinkedList oldList,LinkedList newList)
		{
	   		
	   	   HashMap <String,LinkedList> changesProcs=new HashMap();
		   LinkedList commons=new LinkedList();
		  
		   LinkedList olds=new LinkedList();
		   LinkedList news=new LinkedList();
		   for(int i=0;i<newList.size();i++){
			   if(oldList.contains(newList.get(i))){
				   commons.add(newList.get(i));
			   }
			   else{
				   news.add(newList.get(i));
			   }
		   }
		   for(int i=0;i<oldList.size();i++){
			   if(!newList.contains(oldList.get(i))){
				   olds.add(oldList.get(i));	   
			   }
		  }
		   changesProcs.put("olds", olds);
		   changesProcs.put("new", news);
		   changesProcs.put("commons", commons);
		   return changesProcs;
		   
		}
	   	
	   	
	   	
	   	
	 	public static HashMap<String,ArrayList> getChangesInArrays(ArrayList oldList,ArrayList newList)
		{
	   	   HashMap <String,ArrayList> changesProcs=new HashMap<String,ArrayList> ();
	   	   ArrayList commons=new  ArrayList();
	   	   ArrayList olds=new  ArrayList();
	   	   ArrayList news=new ArrayList();
		   for(int i=0;i<newList.size();i++){
			   if(oldList.contains(newList.get(i))){
				   commons.add(newList.get(i));
			   }
			   else{
				   news.add(newList.get(i));
			   }
		   }
		   for(int i=0;i<oldList.size();i++){
			   if(!newList.contains(oldList.get(i))){
				   olds.add(oldList.get(i));	   
			   }
		  }
		   changesProcs.put("olds", olds);
		   changesProcs.put("new", news);
		   changesProcs.put("commons", commons);
		   return changesProcs;
		   
		}
	   	
	 	public static String getValoresHashMapOrdenClave(HashMap<Integer,String> hm){
	 		String result=null;
	   		ArrayList ordenes=new ArrayList(hm.keySet());
	   		Collections.sort(ordenes);
	   		for(int i=0;i<ordenes.size();i++){
	   			if(result==null)
	   				result=hm.get(ordenes.get(i));
	   			else
	   				result+=","+hm.get(ordenes.get(i));
	   		}
	   		System.out.println("\n ORDENES: "+ordenes+" result:"+result);
	   		return result;
	 	}
   		
	 	
	 	
	 	
	   	//Main para pruebas de los métodos (no borrar)
	   	public static void main(String[]args){
	   		String input=Auxiliar.leeTexto("input?");
	   		System.out.println("input="+input+"  respuesta="+Auxiliar.fillWithSpacesAtEnd(input, 10)+"SecondWord");
	   		System.out.println("input="+input+"  respuesta="+input.matches("[a-zA-Z](q|Q)+[1-9]\\d?"));
	   		String a="AQ292";
	   		Calendar calendar = Calendar.getInstance();
	   		calendar.setTimeInMillis(Auxiliar.getFechaActual().getTime());
	   		System.out.println(calendar.get(Calendar.DAY_OF_MONTH)+"   "+calendar.get(Calendar.MONTH)+"  "+calendar.get(Calendar.YEAR));
	   		//System.out.println(a+"...."+a.matches("[a-zA-Z](q|Q)+[1-9]\\d?"));
	   		a="600250894 #7098# hola";
	   		System.out.println(a+"...."+a.matches(".*#[1-9]\\d?#.*"));
	   		String aux=a.substring(a.indexOf("#")+1);
	   		String sporcentaje=aux.substring(0, aux.indexOf("#"));
	   		System.out.println("\n  SPORCENTAJE:"+sporcentaje);
	   		System.out.println(a+"..>"+a.matches("\\w(q|Q)"));
	   		a="aq";
	   		System.out.println(a+"..>"+a.matches("\\w(q|Q)"));
	   		a="1q2";
	   		System.out.println(a+".."+a.matches("\\w(q|Q)"));	   		
	   		a="zzq2";
	   		System.out.println(a+".."+a.matches("\\w(q|Q)"));	   		
	   		
	   		HashMap<Integer,String> hm=new HashMap<Integer,String>(); 
	   		hm.put(6,"f");
	   		hm.put(2,"b");
	   		hm.put(4,"d");
	   		Auxiliar.getValoresHashMapOrdenClave(hm);
	   		
	   		/*LinkedList <Integer>l1=new LinkedList<Integer>();
	   		LinkedList <Integer>l2=new LinkedList<Integer>();
	   		l1.add(new Integer(1));
	   		l1.add(new Integer(2));
	   		l1.add(new Integer(3));
	   		l2.add(new Integer(2));
	   		l2.add(new Integer(7));
	   		
	   		HashMap <String,LinkedList> changesProcs=Auxiliar.getChangesInLinkedList(l1, l2);
	   		System.out.print(Auxiliar.hashMapToString(changesProcs, "nameKey", "nameValue"));*(
	   		/*Auxiliar.getTimeValueOfPeriod(new Date(System.currentTimeMillis()), 10);*/
	   		
	   		
	   				
	   	}
	   	
	   	

	   	
		public static LinkedList toLinkedList(HashSet hs){
			LinkedList l=new LinkedList();
			if(hs!=null){
				Iterator it=hs.iterator();
				while(it.hasNext()){
					l.add(it.next());
				}
				return l;
			}
			else{
				return null;
			}
		}
	  
		
		public static LinkedList <String> splitslots(String cond)
		{
			LinkedList <String> result = new LinkedList<String>();
			cond.replace(" ","");
			String [] temp =  {""};
			temp = cond.split(",");
			for(int i= 0; i<temp.length;i++)
			{
				String [] slot = {""};
				slot= temp[i].split("=");
				String slotresul = "";
				if(slot[0].equalsIgnoreCase("user") || slot[0].equalsIgnoreCase("value"))
				{
					slotresul="("+slot[0]+" \""+slot[1]+"\"|nil)";
				}
				else
				{
					slotresul="("+slot[0]+" "+slot[1]+"|nil)";
				}
				result.add(slotresul);
				
			}					
			return result;		
		}
		public static String createAccess(LinkedList <String> slotsList)
		{
			String result = "(access ";
			
			for(int i=0; i< slotsList.size();i++ )
			{
				result+=slotsList.get(i)+" ";
			}
			
			result+="))";
			
			return result;
		}
		
		 /************************************************************************
		    *	MÉTODO:		getAllValuesOfHashMap				  			
		    *	DESCRIPCIÓN:*//**                                                   *
		    *             Construye y devuelve una lista con todos los valores que tiene    
		    *             un HashMap <br>
		    *  
		    *  @return	LinkedList<Object> con los valores		<br>
		    *  @param:  HashMap 
		    *  	                                      <br>
		    *  @date		13-Septiembre-2007                                        <br>
		    *  @author 	 Jose Antonio Zamora Aguilera                          <br>
			 ************************************************************************/
		   public static LinkedList  getAllValuesOfHashMap(HashMap hm){
			   LinkedList allvalues = new LinkedList();
			   for(Iterator it=hm.keySet().iterator();it.hasNext();){
				   Object key=it.next();
				   allvalues.add(hm.get(key));
			   }
			   return allvalues;
		   } 
		   
		   
		   
		   /************************************************************************
		    *	MÉTODO:		getAllKeysOfHashMap				  			
		    *	DESCRIPCIÓN:*//**                                                   *
		    *             Construye y devuelve una listacon todas las key    
		    *             un HashMap <br>
		    *  
		    *  @return	LinkedList<Object> con los valores		<br>
		    *  @param:  HashMap 
		    *  	                                      <br>
		    *  @date		12-Noviembre-2007                                        <br>
		    *  @author 	 Jose Antonio Zamora Aguilera                          <br>
			 ************************************************************************/
		   public static ArrayList  getAllKeysOfHashMap(HashMap hm){
			   ArrayList allvalues = new ArrayList();
			   for(Iterator it=hm.keySet().iterator();it.hasNext();){
				   Object key=it.next();
				   allvalues.add(hm.get(key));
			   }
			   return allvalues;
		   } 
		   
		   
		   
		   public static HashMap<Integer,ArrayList<Integer>> cloneHashMap(HashMap<Integer,ArrayList<Integer>> hm){
			   HashMap<Integer,ArrayList<Integer>>hmCopy=new HashMap<Integer, ArrayList<Integer>>();
				Iterator<Integer> itr=hm.keySet().iterator();
				while(itr.hasNext()){
					int idto=itr.next();
					ArrayList<Integer> arrayCopy=new ArrayList<Integer>();
					Iterator<Integer> itrArray=hm.get(idto).iterator();
					while(itrArray.hasNext()){
						arrayCopy.add(itrArray.next());
						hmCopy.put(idto, arrayCopy);
					}
				}
				return hmCopy;
		   }
		   
		   
		   /************************************************************************
		    *	MÉTODO:		 cloneIterProperty				  			
		    *	DESCRIPCIÓN:*//**                                                   *
		    *             Clona las Property contenidas en un iterador de Property y     
		    *             devuelve un iterador con los clones. <br>
		    *  
		    *  @param	Iterator<Property> Iterador con las propiedades a clonar<br>
		    *  @return:  Iterator<Property> Iterador con las propiedades clonadas<br> 
		    *  @date		08-Noviembre-2007                                   <br>
		    *  @author 	 Jose Antonio Zamora Aguilera                          <br>
			 ************************************************************************/
		   public static Iterator<Property> cloneIterProperty(Iterator<Property> itProp){
			   ArrayList listClon=new ArrayList();
			   while(itProp.hasNext()){
				   listClon.add(itProp.next().clone());
			   }
			   return listClon.iterator();
		   }
		   
		   
		   public  static String IteratorToString(Iterator it){
			   String result="";
			   while(it.hasNext()){
				   result+=it.next().toString();
			   }
			return result;
		   }
		   
		   public  static String IteratorToStringByRows(Iterator it){
			   String result="";
			   while(it.hasNext()){
				   result+="\n"+it.next().toString();
			   }
			return result;
		   }
		   
		   public  static ArrayList IteratorToArrayList(Iterator it){
			   if(it!=null){
				   ArrayList result=new ArrayList();
				   while(it.hasNext()){
					   result.add(it.next());
				   }
				   return result;
			   }else 
				   return null;
		   }
		   
		   
		   public  static LinkedList IteratorToLinkedList(Iterator it){
			   if(it!=null){
				   LinkedList result=new LinkedList();
				   while(it.hasNext()){
					   result.add(it.next());
				   }
				   return result;
			   }else 
				   return null;
		   }
		   
		   
		   
		   /**
		    * Ordena un hashset de enteros numéricamente.
		    * @param listaint
		    * @return
		    * @date		04-Diciembre-2007                                      <br>
		    *  @author 	 Jose Antonio Zamora Aguilera                          <br>
		    */
		   public HashSet<Integer> getHashSetOrdered(HashSet<Integer> listaint){
			   HashSet<Integer> listaordered=new  HashSet<Integer>();
			   if(listaint.size()>1){
				   Object[] idsordenadas = listaint.toArray();
				   Arrays.sort(idsordenadas);
				   for (int i = 0; i < idsordenadas.length; i++) 
				   {
					listaordered.add((Integer) idsordenadas[i]);
				   }
				   return listaordered;
			   }
			   else
				   return listaint;
		   }
		   
		   
		   
		   /**
		    * Ordena un hashset de enteros numéricamente.
		    * @param listaint
		    * @return
		    * @date		05-Diciembre-2007                                      <br>
		    *  @author 	 Jose Antonio Zamora Aguilera                          <br>
		    */
		   public static LinkedList  getCommonsElementsOfArrays(LinkedList<LinkedList>listas){
			   LinkedList comunes=new LinkedList();
			   boolean oneEmpty=false;
			   HashSet allhs=new HashSet();
			   for(int i=0;i<listas.size()&&!oneEmpty;i++){
				   oneEmpty=listas.get(i).size()==0;
				   if(!oneEmpty){
					   allhs.addAll(listas.get(i));  
				   }
			   }
			   if(!oneEmpty){
				   ArrayList all=new ArrayList(allhs);
				   for(int i=0;i<all.size();i++){
					   boolean comun=true;
					   for(int j=0;j<listas.size()&&comun;j++){
						   comun=listas.get(j).contains(all.get(i));
					   }
					   if(comun){
						 comunes.add(all.get(i));
					   }
				   }
			   }
			   return comunes;
			   
		   }
		   
		   public static String secToDate(long fecha){
			   return (new Date(fecha*Constants.TIMEMILLIS)).toString();
		   }
		   
		   public static String secToDate(Double fecha){
			   return (new Date(fecha.longValue()*Constants.TIMEMILLIS)).toString();
		   }
		   public static String secToDate(int fecha){
			   return (new Date(fecha*Constants.TIMEMILLIS)).toString();
		   }
		   
		   public static HashSet  getUnionElementsOfArrays(LinkedList<LinkedList>listas){
			   HashSet union=new HashSet();
			   for(int i=0;i<listas.size();i++){
				   LinkedList lista_i=listas.get(i);
				   for(int j=0;j<lista_i.size();j++){
					   union.add(lista_i.get(j));
				   }
			   }
			   return union;
			   
		   }
		   
		   
		   
		   
		   
		   public static  double getSecondsExecucionFrom(double inicio){
		      double fin,tiempos;
			  fin=System.currentTimeMillis();
			  tiempos=(fin-inicio)/Constants.TIMEMILLIS;
			  return tiempos;
		   }
		   
		   
		   public static String LinkedListToString(LinkedList lista){
			   Iterator it=lista.iterator();
			   return Auxiliar.IteratorToStringByRows(it);
			   
		   }
		   
		   public static Double SUM(ArrayList<Double> listaval){
			   Double total=0.0;
			   for(int i=0;i<listaval.size();i++){
				   total+=listaval.get(i);
			   }
			   return total;
		   }
		   
		   public static String SUM_s(ArrayList<Double> listaval){
			   	return String.valueOf(Auxiliar.SUM(listaval));
		   }
		   
		   
		   public static void printCurrentStackTrace(){
			   Exception e=new Exception();
			   e.printStackTrace();
		   }
		   
		   public static LinkedList<Value> toValues(LinkedList values){	 
				LinkedList<Value> rvalues= new LinkedList<Value> ();
				for(int i=0;i<values.size();i++){
					rvalues.add((Value)values.get(i));
				}
				return rvalues;
			}
		   
		   
			public static boolean pointToFilter(IPropertyDef f,IKnowledgeBaseInfo ik){
				boolean result=false;
				
				String value=f.getVALUE()!=null?f.getVALUE():f.getSystemValue();
				if(ik.isObjectProperty(f.getPROP())&&value!=null){
					
					if(Auxiliar.hasIntValue(value)){
						int ivalue=new Integer(value);
						if(Constants.isIDTemporal(Integer.valueOf(value)))
							result=ik.getLevelOf(ivalue)==Constants.LEVEL_FILTER;
						else result=false;
					}
				}
				return result; 
			}
		   
		
		   public static boolean equals(Object valA , Object valB)
		   {   boolean result = false;
		  
		   	if(valA instanceof Integer[] && valB instanceof Integer[]){
			   valA= Arrays.asList((Integer[])valA);
			   Collections.sort((List)valA);
			   valB= Arrays.asList((Integer[])valB);
			   Collections.sort((List)valB);
		   	}
		   
		   if (valA == null && valB != null)
				return false;
			if (valA != null && valB == null)
				return false;
			if (valA == null && valB == null)
				return true;
		   
			if(valA.equals(valB))
				return true;		   		  
			   return result;
		   }
		   
		   
		   public static int  getMinPosition(LinkedList lista)
			{
			Long min  =Long.MAX_VALUE;
			int pos =-1;
				for (int i = 0; i <lista.size(); i++)
				{
				if (((Long)lista.get(i)) <min)
					{
					pos = i ;
					min = ((Long)lista.get(i));
					}
				}
			return pos;

		}
		   
		  /**
		   * Método para imprimir por consola las reglas que se van disparando e informacion
		   * importante para comprobar que la lógica de reglas son reales.
		   * @param mensaje
		   */ public static void print(String mensaje){
					System.err.println(mensaje);
			} 
		   
		   
		   public static Date getFechaActual(){
			   return new Date(System.currentTimeMillis());
			   
		   }
		   
		   public static long inicioSg(long fecha){
			   
			   Calendar cParam = Calendar.getInstance();
			   
			   cParam.setTimeInMillis(fecha*Constants.TIMEMILLIS);
			   
			   cParam.set(Calendar.HOUR_OF_DAY,0);
			   cParam.set(Calendar.MINUTE,0);
			   cParam.set(Calendar.SECOND,0);						
				
				return cParam.getTimeInMillis()/Constants.TIMEMILLIS;				
		   }
		   
		   public static long segundosPeriodoRelativo(String formatoFecha){
			   	//soporta definir un desfase relativo respecto a la fecha actual +/-xxxperiodo signo + o - seguido de valor numerico y seguido de periodo que representa dicho valor a=año, m=mes, d=dia
			   // ejemplo un año menos -1a
				if(formatoFecha.matches("\\+\\d+[amd]")||formatoFecha.matches("\\-\\d+[amd]")){
					
					int signo=formatoFecha.charAt(0)=='-'?-1:+1;
					
					char periodo=formatoFecha.charAt(formatoFecha.length()-1);
					
					int incr=signo*Integer.parseInt(formatoFecha.substring(1,formatoFecha.length()-1));
					
	 				long fecha= 0;
	 			 			
					if(periodo=='d'){
						fecha+= incr*86400;
					}
					if(periodo=='m') fecha+= incr*86400*30;
					if(periodo=='a') fecha+= incr*86400*365;
				
					return fecha;					
				}
			   return 0;
		   }
		   public static Double redondea(Double number, int num_decimales){
			      long factor = (long)Math.pow(10,num_decimales);
				   // Shift the decimal the correct number of places
				   // to the right.
			      number = number * factor;
				   // Round to the nearest integer.
				   long tmp = Math.round(number);
				   
				   // Shift the decimal the correct number of places
				   // back to the left.
				   return (double)tmp / (double)factor;
				      
		   }
		   
		   
		   public static Double getMinValueOfMap(HashMap<String,Double> mapa){
			   Double min=null;
			   Iterator it=mapa.keySet().iterator();
			   while(it.hasNext()){
				   double valor=(Double)mapa.get(it.next());
				   if(min==null||valor<min){
					   min=valor;
					 }
			   }
			   //System.err.println("\n..... DEBUG AUXILIAR.GETMINVALUEOFMAP="+mapa+"  calcula min="+min);
			   return min;   
		   } 
		   
		   
		   public static  boolean separadosAlMenosNdias(Date d1,Date d2, int ndias){
				boolean result=false;
				Long dif=Math.abs(d1.getTime()-d2.getTime());
				Long msecondsNdias=new Long(ndias*24*3600*Constants.TIMEMILLIS);
				return dif>msecondsNdias;
			}
		   
		   public static  boolean separadosAlMenosNhoras(Date d1,Date d2, int horas){
				boolean result=false;
				if(d2==null){
					 Calendar rightNow = Calendar.getInstance();
					 d2=rightNow.getTime();
				}
				Long dif=Math.abs(d1.getTime()-d2.getTime());
				Long msecondsSep=new Long(horas*3600*Constants.TIMEMILLIS);
				return dif>msecondsSep;
			}
		   
		   public static instance getIdoFromServer(int idto,String rdn,Integer idtoUserTask,IKnowledgeBaseInfo ik) throws DataErrorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
			   ArrayList<String> rdns=new ArrayList<String>();
			   rdns.add(rdn);
			   selectData select=getIdosFromServer(idto, rdns, idtoUserTask, ik);
			   if(select==null || !select.hasData())
				   return null;
			   
			   return select.getFirst();
		   }
		   
		   public static selectData getIdosFromServer(int idto,ArrayList<String> rdns,Integer idtoUserTask,IKnowledgeBaseInfo ik) throws DataErrorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
//		    	System.err.println("ido -> " + ido);
		    	int ido=-1;
		    	int idProp=Constants.IdPROP_RDN;
		    	ArrayList <SelectQuery> sq = new ArrayList <SelectQuery>();
				SelectQuery sq1 = new SelectQuery(String.valueOf(ido),idProp, null, null);
				sq.add(sq1);
				instance filter = new instance(idto, ido);
				filter.setRdn("", true);//Esto inserta la property y luego la usamos para darle los valores del usuario
				LinkedList<Value> values=new LinkedList<Value>();
				Iterator<String> itr=rdns.iterator();
				while(itr.hasNext()){
					String rdn=itr.next();
					StringValue value=new StringValue(rdn);
					values.add(value);
				}
				filter.getProperty(ido, idProp).setValues(values);
				
				QueryXML query=ik.getQueryXML();
				query.setSelect(sq);
				Element xml = query.toQueryXML(filter,null);
				
				selectData sd = ik.getServer().serverGetQuery( xml, idtoUserTask, queryData.MODE_ROW);
				return sd;
		    }
		   
		   
		   public static String arrayToStringConSaltoLinea(ArrayList array){
			   String result="";
			   for(int i=0;i<array.size();i++){
				   result+="\n"+array.get(i);
			   }
			   return result;
			   
			   
		   }

		   
		   public static Element getXml(String file) throws IOException, JDOMException{
				BufferedReader in = new BufferedReader(new FileReader(file)); 
				String dataS="", buff="";
				while(buff!= null){
					dataS+=buff;
					buff=in.readLine();
				}
				
				Document configDOC=jdomParser.readXML(dataS);
				return configDOC.getRootElement();
			}
		   
		   public static boolean printAndReturnTrue(String mensaje){
		   		System.out.println(mensaje);
		   		return true;
			}
		   
		   
		   public static TimeValue getTimeValueOfPeriod(Date date,int ndiasperiodo){
			   Long secondsdate=((Date)date).getTime()/Constants.TIMEMILLIS;
			   Long relativeSecondsMin=secondsdate-ndiasperiodo*24*3600;
			   Long relativeSecondsMax=secondsdate+ndiasperiodo*24*3600;
			   //System.out.println("\n DEBUG DATE relativeSecondsMin"+new Date(relativeSecondsMin*Constants.TIMEMILLIS));
			   //System.out.println("\n DEBUG DATE relativeSecondsMin"+new Date(relativeSecondsMax*Constants.TIMEMILLIS));	
			   //System.out.println("\n DEbug date:"+date);
			   return new TimeValue(relativeSecondsMin,relativeSecondsMax); 
			}
		   
		   
		 /**
		  * Permite comparar los elementos de dos iteradores analizando elementos de cada uno sin repetición
		  * y elementos de cada uno repetidos y que elementos son exclusivos de un iterator por no estar en el otro
		  * @param it1
		  * @param it2
		  */public static void  compareIterators(Iterator it1,Iterator it2){  
			 ArrayList  elementos1sinrepeticion= new ArrayList ();
			 ArrayList  elementos2sinrepeticion= new ArrayList ();		
			 ArrayList  elementos1repetidos= new ArrayList ();
			 ArrayList  elementos2repetidos= new ArrayList ();			 

			while(it1.hasNext()){
				Object elemento=it1.next();
				if(!elementos1sinrepeticion.contains(elemento)){
					elementos1sinrepeticion.add(elemento);
				}else{
					elementos1repetidos.add(elemento);
				}
			}
			
			while(it2.hasNext()){
				Object elemento=it2.next();
				if(!elementos2sinrepeticion.contains(elemento)){
					elementos2sinrepeticion.add(elemento);
				}else{
					elementos2repetidos.add(elemento);
				}
			}
			
			 ArrayList exclusivos1=new ArrayList(); 
			 ArrayList exclusivos2=new ArrayList();
			
			//elementos que solo estan en el iterator 1
			for(int i=0;i<elementos1sinrepeticion.size();i++){
				if(!elementos2sinrepeticion.contains(elementos1sinrepeticion.get(i))){
					exclusivos1.add(elementos1sinrepeticion.get(i));
				}
			}
						
//			elementos que solo estan en el iterator 2
			for(int i=0;i<elementos2sinrepeticion.size();i++){
				if(!elementos1sinrepeticion.contains(elementos2sinrepeticion.get(i))){
					exclusivos2.add(elementos2sinrepeticion.get(i));
				}
			}

			
			System.out.println("\n\n AUXILIAR.compareIterators:==========================");
			System.out.println("Elementos 1 sinrepeticion:"+elementos1sinrepeticion.size()+"  Elementos 1 repetidos:"+elementos1repetidos.size());
			System.out.println("Elementos 2 sinrepeticion:"+elementos1sinrepeticion.size()+"  Elementos 2 repetidos:"+elementos1repetidos.size());			 
			System.out.println("Elementos exclusivos de 1: "+exclusivos1.size()+"\n    "+exclusivos1);
			System.out.println("Elementos exclusivos de 2: "+exclusivos2.size()+"\n    "+exclusivos2);
		 }
		   
		   
		 

	/**
	 * Convierte la lista en un String
	 * 
	 * @param list
	 *            Lista a convertir
	 * @param divider
	 *            Separador que se va a introducir entre los objetos.<br>
	 *            Si el separador indtroducido es <code>null</code> se usarán
	 *            <code>','</code> como separador.
	 * @return String con la conversión.<br>
	 *         Si la lista era nula o estaba vacía, se devuelve un String vacío.
	 *         Nunca devuelve <code>null</code>.
	 */
	public static String listToString(List<?> list, String divider) {
		if (list == null || list.isEmpty()){
			   return "";
		}
		if (divider == null){
			divider = ",";
		}
		String result = "";
		for (Object object : list) {
			if (! result.isEmpty()){
				result += divider;
			}
			result += object != null ? object.toString() : "null";
		}
		return result;
	}

	/**
	 * Convierte el conjunt en un String
	 * 
	 * @param set
	 *            Conjunto a convertir
	 * @param divider
	 *            Separador que se va a introducir entre los objetos.<br>
	 *            Si el separador indtroducido es <code>null</code> se usarán
	 *            <code>','</code> como separador.
	 * @return String con la conversión.<br>
	 *         Si el conjunto era nulo o estaba vacío, se devuelve un String vacío.
	 *         Nunca devuelve <code>null</code>.
	 */
	public static String setToString(Set<?> set, String divider) {
		if (set == null || set.isEmpty()){
			   return "";
		}
		if (divider == null){
			divider = ",";
		}
		String result = "";
		for (Object object : set) {
			if (! result.isEmpty()){
				result += divider;
			}
			result += object != null ? object.toString() : "null";
		}
		return result;
	}
	
	public static String removeStringAccents(String s){
		return (s.replaceAll("á", "a").replaceAll("é", "e").
				replaceAll("í", "i").replaceAll("ó", "o").
				replaceAll("ú", "u").replaceAll("ñ", "n").
				replaceAll("Á", "A").replaceAll("É", "E").
				replaceAll("Í", "I").replaceAll("Ó", "O").
				replaceAll("Ú", "U").replaceAll("Ñ", "N"));
	}
	
	public static void copyFile(File in, File out) throws IOException     
	{    
		FileChannel inChannel = new   
		FileInputStream(in).getChannel();    
		FileChannel outChannel = new   
		FileOutputStream(out).getChannel();    

		try {    
			inChannel.transferTo(0, inChannel.size(), outChannel);    
		}     
		catch (IOException e) {    
			throw e;    
		}    
		finally {    
			if (inChannel != null) inChannel.close();    
			if (outChannel != null) outChannel.close();    
		}
	} 
	
	public static Map<Integer, Set<Integer>> convertToIdtoTableIds(HashMap<Integer, HashSet<Integer>> idtosIdos){
		//System.err.println("ENTRA en convert:"+idtosIdos);
		Map<Integer, Set<Integer>> idtoTableIds = new HashMap<Integer, Set<Integer>>();
		for (Integer idto : idtosIdos.keySet()) {
			Set<Integer> tableIds = new HashSet<Integer>();
			HashSet<Integer> idos = idtosIdos.get(idto);
			if(idos != null){
				for (Integer ido : idos) {
					tableIds.add(QueryConstants.getTableId(ido));
				}
				idtoTableIds.put(idto, tableIds);
			}else{
				idtoTableIds.put(idto, null);
			}
		}
		//System.err.println("SALE en convert:"+idtoTableIds);
		return idtoTableIds;
	}
	
	 public static void beginCrono(){
		 //System.err.println("..beginCrono..");
		 if(beginCrono!=null){
			 //System.err.println("\n\n WARNING: llamada a begin crono sin haber terminado el cronometraje anterior, se resetea el anterior:"); 
			 beginCrono=null;
		 }
			beginCrono=System.currentTimeMillis();
	 }
	 
	 public static double  stopCrono(){
		 	//System.err.println("..beginCrono..");
			long fin=System.currentTimeMillis();
			double tiempoMiliseconds=(fin-beginCrono);
			beginCrono=null;
			return tiempoMiliseconds;
	 }
	 
	 public static String fillWithSpacesAtEnd(String valor, int longitud){
			int lengthSpaces=longitud-valor.length();
			return String.format("%1$-"+longitud+"s",valor);
	}
	 
	public static void inputStreamToFile(InputStream entrada,File f) throws IOException{
	   OutputStream salida=new FileOutputStream(f);
	   byte[] buf =new byte[1024];//Actualizado me olvide del 1024
	   int len;
	   while((len=entrada.read(buf))>0){
	      salida.write(buf,0,len);
	   }
	   salida.close();
	   entrada.close();
	}

}

		   

