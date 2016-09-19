package dynagent.tools.importers.model;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.NamingException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.Properties;
import dynagent.common.basicobjects.TClase;
import dynagent.common.basicobjects.T_Herencias;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.IDAO;
import dynagent.server.database.dao.InstanceDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.database.dao.T_HerenciasDAO;
import dynagent.server.ejb.FactoryConnectionDB;

public class DataBaseMapped {
	
	private String ip;
	private int business;
	private FactoryConnectionDB fcdb; 
	
	public DataBaseMapped(String ip, int business, String gestor){
		this.ip=ip;
		this.business=business;
		DAOManager.getInstance().setBusiness(new Integer(business).toString());
		FactoryConnectionDB fcdb = new FactoryConnectionDB(business,true,ip,gestor);
		DAOManager.getInstance().setFactConnDB(fcdb);
		DAOManager.getInstance().setCommit(true);
	}
	
	public DataBaseMapped(){//cd se usa este constructor se usara la ip y base de datos que hubiese establecido previamente 
		
	}
	
	public DataBaseMapped(FactoryConnectionDB fcdb){
		this.fcdb=fcdb;
		DAOManager.getInstance().setBusiness(new Integer(business).toString());
		DAOManager.getInstance().setFactConnDB(fcdb);
		DAOManager.getInstance().setCommit(true);
	}
	
	/*private void switchDataBase(){
		DAOManager.getInstance().setBusiness(new Integer(business).toString());
		FactoryConnectionDB fcdb = new FactoryConnectionDB(business,true,ip);
		DAOManager.getInstance().setFactConnDB(fcdb);
		DAOManager.getInstance().setCommit(true);
	}*/
	
	
	
	public static FactoryConnectionDB setConnection(String snbusiness,String ip,String gestor){
		DAOManager.getInstance().setBusiness(snbusiness);
		FactoryConnectionDB fcdb = new FactoryConnectionDB(new Integer(snbusiness),true,ip,gestor);
		DAOManager.getInstance().setFactConnDB(fcdb);
		DAOManager.getInstance().setCommit(true);
		return fcdb;
	}
	
	
	
	
	
	public int getBusiness() {
		return business;
	}



	public void setBusiness(int business) {
		this.business = business;
	}



	public String getIp() {
		return ip;
	}



	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * Construye un mapa con clave los nombres de las clases (incluyendo tipos basicos) y sus idto
	 * 
	 * @return HashMap<String, Integer> Mapa con los nombres de las clases y sus identificadores
	 * @throws NamingException 
	 * @throws SQLException 
	 */

	public HashMap<String, Integer> getIDClasses() throws SQLException, NamingException{
		
		//switchDataBase();
		HashMap<Integer, ArrayList<Instance>> instances = this.getInstances();
		HashMap<String, Integer> classes = new HashMap<String, Integer>();
		TClaseDAO cdao = new TClaseDAO();
		cdao.open();
		Iterator itClassesTable = cdao.getAll().iterator();
		while(itClassesTable.hasNext()){
			TClase tc = (TClase) itClassesTable.next();
			if(instances.get(tc.getIDTO())!=null)
				classes.put(tc.getName(), new Integer(tc.getIDTO()));
		}
		cdao.close();
	
		return classes;
		
	}
	
	/**
	 * Construye un mapa con clave los nombres de los individuos (incluyendo tipos basicos) y sus ido
	 * 
	 * @return HashMap<String, Integer> Mapa con los nombres de las individuos y sus identificadores
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	
	public HashMap<String, Integer> getIDIndividuals() throws SQLException, NamingException{
		
		//switchDataBase();
		
		HashMap<String, Integer> individuals = new HashMap<String, Integer>();
		HashMap<Integer, ArrayList<Instance>> instances = this.getInstances();
		TClaseDAO cdao = new TClaseDAO();
		cdao.open();
		Iterator itClassesTable = cdao.getAll().iterator();
		while(itClassesTable.hasNext()){
			TClase tc = (TClase) itClassesTable.next();
			if(instances.get(tc.getIDTO())==null)
				individuals.put(tc.getName(), new Integer(tc.getIDTO()));
		}
		
		cdao.close();
		return individuals;
	}
	
	
	/**
	 * Construye un mapa con clave los nombres de las properties (incluyendo tipos basicos) y sus idprop
	 * 
	 * @return HashMap<String, Integer> Mapa con los nombres de las properties y sus identificadores
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	
	public HashMap<String, Integer> getIDProperties() throws SQLException, NamingException{
		
		//switchDataBase();
		
		HashMap<String, Integer> properties = new HashMap<String, Integer>();
		PropertiesDAO pdao = new PropertiesDAO();
		pdao.open();
		Iterator itPropertiesTable = pdao.getAll().iterator();
		while(itPropertiesTable.hasNext()){
			Properties pr = (Properties) itPropertiesTable.next();
			properties.put(pr.getNAME(), pr.getPROP());
		}
		
		pdao.close();
		return properties;
	}
	
	/**
	 * Construye un mapa para la tabla T_Herencias, donde la clave es un identificador "hijo" y como objetos <br>
	 * todos sus padres
	 * 
	 * @return HashMap<String, Integer> Mapa con los idto de los hijos y una lista de sus padres
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	
	public HashMap<Integer, ArrayList<Integer>> getHierarchies() throws SQLException, NamingException{
		//switchDataBase();
		
		HashMap<Integer, ArrayList<Integer>> herencias = new HashMap<Integer, ArrayList<Integer>>();
		T_HerenciasDAO tdao = new T_HerenciasDAO();
		tdao.open();
		Iterator itHierarchyTable = tdao.getAll().iterator();
		while(itHierarchyTable.hasNext()){
			T_Herencias hr = (T_Herencias) itHierarchyTable.next();
			ArrayList<Integer> padres = herencias.get(hr.getID_TO());
			if(padres==null)
				padres = new ArrayList<Integer>();
			padres.add(hr.getID_TO_Padre());
			herencias.put(new Integer(hr.getID_TO()), padres);
		}
		
		tdao.close();
		return herencias;
	}
	
	/**
	 * Construye un mapa para la tabla instances, donde la clave es el idto de cada clase, conteniendo cada clave
	 * un arraylist con todas las instancias asociadas a ella.
	 * 
	 * @return
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	
	public HashMap<Integer, ArrayList<Instance>> getInstances() throws SQLException, NamingException{
		
	//	switchDataBase();
		
		HashMap<Integer, ArrayList<Instance>> instances = new HashMap<Integer, ArrayList<Instance>>();
		InstanceDAO idao = new InstanceDAO();
		idao.open();
		Iterator itInstancesTable = idao.getAll().iterator();
		while(itInstancesTable.hasNext()){
			Instance ins = (Instance) itInstancesTable.next();
			if(ins.getIDTO()!=null){
				ArrayList<Instance> l = instances.get(Integer.parseInt(ins.getIDTO()));
				if(l==null){
					l = new ArrayList<Instance>();
				}
				l.add(ins);
				instances.put(Integer.parseInt(ins.getIDTO()), l);
			}
		}
		
		idao.close();
		return instances;
	}
	
	
	
	
	public static Integer getIdPropiedad(String name) throws SQLException, NamingException{
		int idProp;
		IDAO idao = DAOManager.getInstance().getDAO("properties");
		idao.open();
		dynagent.common.basicobjects.Properties tProperties;
	    PropertiesDAO propertiesDao = (PropertiesDAO) idao.getDAO();
	    tProperties = propertiesDao.getPropertyByName(name); 
	    Properties pp = new Properties();
    	if(tProperties == null){ //No se encuentra la propiedad en la tabla Properties. Obtenemos un idProp disponible y la insertamos
    		Integer cidProp=Constants.getIdConstantProp(name);
    		if(cidProp!=null){
    			idProp=cidProp;	
    			System.out.println("   idProp cte="+idProp);
    		}
    		else{
    			idProp=new Integer(propertiesDao.getLastPK(Constants.MIN_IdPROP_MODEL)).intValue()+1;
    			System.out.println("   idProp lastpk="+idProp);
    		}
		    pp.setPROP(idProp);
   			propertiesDao.insert(pp);
    	}
    	else{  //La property ya está en la tabla, obtenemos su idProp y actualizamos sus campos con la información del modelo
    		pp.setPROP(tProperties.getPROP());
   			idProp=tProperties.getPROP();
    	}
    	return idProp;
	}

}
