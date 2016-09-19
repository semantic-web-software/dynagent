package dynagent.common.knowledge;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.JDOMException;

import dynagent.common.basicobjects.Access;
import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.Properties;
import dynagent.common.basicobjects.T_Herencias;
import dynagent.common.communication.IndividualData;
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

public interface IDataFromDataBase {

	public IndividualData getEnumeratedFromDatabase() throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException, SystemException, IncompatibleValueException, CardinalityExceedException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException;
	public HashSet<String> getURolesFromDatabase(String user) throws SQLException, NamingException;
	
	/*public String getRulesFromDatabase(String sourceRuler) throws SQLException, NamingException{
		String reglas = MetaData.getRulerFile(this.factConnDB, sourceRuler);
		return reglas;
	}*/

	
	
	public LinkedList<Instance> getInstancesFromDatabase() throws SQLException, NamingException;
	
	public LinkedList<Properties> getPropertiesFromDatabase() throws SQLException, NamingException;
	
	public LinkedList<T_Herencias> getHierarchyFromDatabase() throws SQLException, NamingException;
	
	public LinkedList<Access>  getAccessFromDatabase() throws SQLException, NamingException;		
}
