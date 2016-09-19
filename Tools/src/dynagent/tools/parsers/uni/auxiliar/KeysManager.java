package dynagent.tools.parsers.uni.auxiliar;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.NamingException;

import dynagent.common.Constants;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;

public class KeysManager {
	
	
	/**
	 * 
	 * @param propiedades Lista de nombres de las propiedades que vamos a insertar
	 * @return HashMap<String, Integer> : Mapa en el que se asigna para cada String un identificador de propiedad,
	 * pudiendo ser un identificador predefinido si el nombre de dicha propiedad también es un nombre predefinido
	 * @throws SQLException 
	 */
	public static HashMap<String,Integer> buildKeyProperties(ArrayList<String> propiedades) throws SQLException{
		
		HashMap<String, Integer> keys = new HashMap<String, Integer>();
		Iterator it = propiedades.iterator();
		
		PropertiesDAO pdao = new PropertiesDAO();
		String spk = pdao.getLastPK();
		Integer inp = Integer.parseInt(spk);
		int propPk = inp.intValue() + 1;
		if(propPk<Constants.MIN_IdPROP_MODEL)
			propPk = Constants.MIN_IdPROP_MODEL;

		while(it.hasNext()){
			String name = (String) it.next();
			int idtoAinsertar;
			
			
			
			if(name.equals(Constants.PROP_MYFUNCTIONALAREA)){
        		 idtoAinsertar=Constants.IdPROP_MYFUNCTIONALAREA;
        	 }
			
			else if(name.equals(Constants.PROP_RDN)){
        		 idtoAinsertar=Constants.IdPROP_RDN;
        	 }
			
			else if(name.equals(Constants.PROP_TARGETCLASS)){
        		 idtoAinsertar=Constants.IdPROP_TARGETCLASS;
        	 }
		
			else if(name.equals(Constants.PROP_LOGO)){
        		 idtoAinsertar=Constants.IdPROP_LOGO;
        	 }
			else if(name.equals(Constants.PROP_USERROL)){
        		 idtoAinsertar=Constants.IdPROP_USERROL;
        	 }
			
			else
				idtoAinsertar=propPk++;
			
			keys.put(name, new Integer(idtoAinsertar));
		}
		return keys;
	}
	
	/**
	 * 
	 * @param clases Lista de nombres de las las clases que vamos a insertar
	 * @return HashMap<String, Integer> : Mapa en el que se asigna para cada String un IDTO,
	 * pudiendo ser un identificador predefinido si el nombre de dicha clase también es un nombre predefinido
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	
	public static HashMap<String, Integer> buildKeyClasses(ArrayList<String> clases) throws SQLException, NamingException{
		
		HashMap<String, Integer> keys = new HashMap<String, Integer>();
		Iterator it = clases.iterator();
		
		TClaseDAO tc = new TClaseDAO();
		tc.open();
		String cad = tc.getLastPK("6", new Integer(Constants.MAX_ID_CLASS).toString());
		tc.close();
		Integer i = Integer.parseInt(cad);
		int nextPk = i.intValue() + 1;
		int idtoAinsertar = -1;
		while(it.hasNext()){
			String name = (String) it.next();
			if(name.equals(Constants.CLS_THING)){
       		 idtoAinsertar=Constants.IDTO_THING;
	       	 }
	       	
	       	
	       	 else if(name.equals(Constants.CLS_UNIDADES)){
	       		 idtoAinsertar=Constants.IDTO_UNIDADES;
	       	 }
	    	
	    	
	       	 else if(name.equals(Constants.CLS_FUNCTIONAL_AREA)){
	       		 idtoAinsertar=Constants.IDTO_FUNCTIONAL_AREA;
	       	 }
	       	 else if(name.equals(Constants.CLS_UTASK)){
	       		 idtoAinsertar=Constants.IDTO_UTASK;
	       	 }
	       	 else if(name.equals(Constants.DATA_BOOLEAN)){
	       		 idtoAinsertar=Constants.IDTO_BOOLEAN;
	       	 }
	       	 else if(name.equals(Constants.DATA_DATETIME)){
	       		 idtoAinsertar=Constants.IDTO_DATETIME;
	       	 }
	       	 else if(name.equals(Constants.DATA_DOUBLE)){
	       		 idtoAinsertar=Constants.IDTO_DOUBLE;
	       	 }
	       	 else if(name.equals(Constants.DATA_STRING)){
	       		 idtoAinsertar=Constants.IDTO_STRING;
	       	 }
	       	 else if(name.equals(Constants.DATA_TIME)){
	       		 idtoAinsertar=Constants.IDTO_TIME;
	       	 }
	       	 else if(name.equals(Constants.DATA_INT)){
	       		 idtoAinsertar=Constants.IDTO_INT;
	       	 }
	       	 else
	       		 idtoAinsertar = nextPk++;
				keys.put(name, new Integer(idtoAinsertar));
				
		}
		return keys;
		
	}
	/**
	 * 
	 * @param individuals : Lista de nombre de los individuos que vamos a asignar
	 * @return HashMap<String, Integer> : Mapa en el que asignamos a cada String un IDO
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	public static HashMap<String, Integer> buildKeyIndividuals(ArrayList<String> individuals) throws SQLException, NamingException{
		HashMap<String, Integer> keys = new HashMap<String, Integer>();
		Iterator it = individuals.iterator();
		
		TClaseDAO tc = new TClaseDAO();
		tc.open();
		String cad2 = tc.getLastPK(new Integer(Constants.MIN_ID_INDIVIDUAL).toString(), new Integer(Constants.MAX_ID_INDIVIDUAL).toString());
		tc.close();
		Integer i2 = Integer.parseInt(cad2);
		int nextPkInd = i2.intValue() + 1;
		
		while(it.hasNext()){
			String s = (String) it.next();
			keys.put(s, new Integer(nextPkInd++));
		}
		return keys;
		
	}
}
