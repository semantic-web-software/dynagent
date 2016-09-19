package dynagent.server.replication;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;

import dynagent.common.basicobjects.GlobalClases;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IPropertyDef;

public interface IReplication {

	public boolean seReplica (int idto);
	
	public Set<Integer> getNoModifyDB() throws SQLException, NamingException;
	
	public String getActualSystem();
	public void setActualSystem(String value);
	public String getSufix();
	public String getIPCentral();
	public Integer getPortCentral();
	public int getBusinessCentral();
	public GlobalClases getGlobalClass(int uTask, HashSet<Integer> idtosRoot);
	public boolean isAssociatedIdto(int idto);
	//public boolean isPartOfAssociatedIdto(int idto, int associatedIdto);
	public boolean isKeyOrSpecOfAssociatedIdto(int idto, int associatedIdto, IKnowledgeBaseInfo ik) throws NotFoundException, IncoherenceInMotorException;
	public boolean isSufixOfAssociatedIdto(int idto, int associatedIdto);
	public Integer getKeyOfAssociatedIdto(int idto);
	public ArrayList<Integer> getSufixOfAssociatedIdto(int idto);
	//public boolean isGlobalIndividual(int idto);

	public boolean getIdtosReplication(int ido, Integer idoNeg, int idto, ArrayList<IPropertyDef> aipd) 
			throws SQLException, NamingException;
}
