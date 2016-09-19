package dynagent.server.database;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.JDOMException;

import dynagent.common.basicobjects.Access;
import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.Properties;
import dynagent.common.basicobjects.T_Herencias;
import dynagent.common.communication.IndividualData;
import dynagent.common.communication.docServer;
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
import dynagent.common.knowledge.IDataFromDataBase;
import dynagent.common.utils.Auxiliar;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.IDAO;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.ejb.MetaData;

public class DataBaseForRuler implements IDataFromDataBase{
		
	private FactoryConnectionDB factConnDB;
	
	private DataBaseMap dataBaseMap; 

	public DataBaseForRuler(FactoryConnectionDB fcdb, DataBaseMap dataBaseMap, int business){
		this.factConnDB=fcdb;
		this.dataBaseMap = dataBaseMap;
		dynagent.server.database.dao.DAOManager.getInstance().setFactConnDB(factConnDB);
		//dynagent.server.databases.dao.DAOManager.getInstance().setCommit(true);
		dynagent.server.database.dao.DAOManager.getInstance().setBusiness(new Integer(business).toString());
	}

	public IndividualData getEnumeratedFromDatabase() throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException, SystemException, IncompatibleValueException, CardinalityExceedException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		return MetaData.getEnumerated(factConnDB, dataBaseMap);
	}
	public HashSet<String> getURolesFromDatabase(String user) throws SQLException, NamingException{
		HashSet<String> roles = new HashSet<String>();
		if(user!=null){
			HashSet<Integer> idoRoles = new HashSet<Integer>();
			MetaData.getRolesUsuario(user, roles, idoRoles, factConnDB);
		}
		return roles;
	}
	
	/*public String getRulesFromDatabase(String sourceRuler) throws SQLException, NamingException{
		String reglas = MetaData.getRulerFile(this.factConnDB, sourceRuler);
		return reglas;
	}*/

	
	
	public LinkedList<Instance> getInstancesFromDatabase() throws SQLException, NamingException{
		IDAO idao = DAOManager.getInstance().getDAO("instances");
		LinkedList<Instance>instances=new LinkedList<Instance>();
		idao.open();
		Iterator it =idao.getAll().iterator();
		idao.close();
		while(it.hasNext()){
			instances.add((Instance)it.next());
		}
		return instances;
	}
	
	
	
	
	public LinkedList<Properties> getPropertiesFromDatabase() throws SQLException, NamingException{
		IDAO pdao = DAOManager.getInstance().getDAO("properties");
		pdao.open();
		LinkedList<Properties> properties=new LinkedList<Properties>();
		Iterator it = pdao.getAll().iterator();
		pdao.close();
		while(it.hasNext()){
			properties.add((Properties)it.next());
		}
		return properties;
		
	}
	
	public LinkedList<T_Herencias> getHierarchyFromDatabase() throws SQLException, NamingException{
		IDAO hdao = DAOManager.getInstance().getDAO("T_Herencias");
		hdao.open();
		LinkedList<T_Herencias> herencias=new LinkedList<T_Herencias>();
		Iterator it = hdao.getAll().iterator();
		hdao.close();
		while(it.hasNext()){
			herencias.add((T_Herencias)it.next());
		}
		return herencias;
	}
	
	public LinkedList<Access>  getAccessFromDatabase() throws SQLException, NamingException{
//		IDAO adao = DAOManager.getInstance().getDAO("Access");
//		adao.open();
//		LinkedList<Access> accesses=new LinkedList<Access>();
//		Iterator it = adao.getAll().iterator();
//		adao.close();
//		while(it.hasNext()){
//			accesses.add((Access)it.next());
//		}
//		return accesses;
		return null;
	}		
	
}
