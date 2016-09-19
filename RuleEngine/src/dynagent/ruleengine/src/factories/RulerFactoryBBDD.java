/***
 * RulerFactoryBDD
 * @author: Ildefonso Montero Perez - monteroperez@us.es
 */

package dynagent.ruleengine.src.factories;



import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.drools.rule.Package;
import org.jdom.JDOMException;

import dynagent.common.basicobjects.Access;
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
import dynagent.common.knowledge.KnowledgeAdapter;
import dynagent.common.utils.Auxiliar;

public class RulerFactoryBBDD extends RulerFactoryCommon{

	public RulerFactoryBBDD(IDataFromDataBase dataFromDataBase,int bussiness, docServer server, String engine,String user, ArrayList<String> rulesFiles) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SQLException, NamingException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		super(bussiness,  server, engine,user, false, null, null, null, true);
		//System.out.println(".....inicio constructor RulerFactoryBBDD");
		double inicio=System.currentTimeMillis();
		this.inicializeInfoFromBBDD(dataFromDataBase,user, rulesFiles);
		//System.out.println("   <time constructor RulerFactoryXML="+Auxiliar.getSecondsExecucionFrom(inicio));
	}
	

	public RulerFactoryBBDD(ArrayList<Package>  rulesPackage,IDataFromDataBase dataFromDataBase,int bussiness, docServer server, String engine,String user)throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SQLException, NamingException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException
	{

			super(bussiness,  server, engine,user, false, null, null, null, true);
		//System.out.println(".....inicio constructor RulerFactoryBBDD");
		double inicio=System.currentTimeMillis();
		this.inicializeInfoFromBBDD(dataFromDataBase,user, rulesPackage);
		//System.out.println("   <time constructor RulerFactoryXML="+Auxiliar.getSecondsExecucionFrom(inicio));
	}

	
	//rules puede ser un array con los nombres de los paquetes o los paquetes en si
	public void inicializeInfoFromBBDD(IDataFromDataBase dataFromDataBase,String user, ArrayList rules) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		this.arrayURoles=dataFromDataBase.getURolesFromDatabase(user);		
		this.accesses=new LinkedList<Access>();//dataFromDataBase.getAccessFromDatabase(); TODO Este metodo no esta implementado
		this.herencias=dataFromDataBase.getHierarchyFromDatabase();
		this.properties=dataFromDataBase.getPropertiesFromDatabase();
		this.instances=dataFromDataBase.getInstancesFromDatabase();		
		this.instances.addAll(KnowledgeAdapter.toInstance(dataFromDataBase.getEnumeratedFromDatabase().getAIPropertyDef()));
		
		if(rules.get(0) instanceof String)
			this.rulesFiles=rules;//this.getRulesFromDatabase(sourceRuler);
		else
			this.rulesPackages=rules;
	}
	


}
