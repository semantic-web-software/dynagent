package dynagent.server.ejb;

import java.sql.SQLException;

import javax.naming.NamingException;

import dynagent.common.knowledge.IKnowledgeBaseInfo;

public interface IKnowledgeBaseInfoServer extends IKnowledgeBaseInfo {

//	public HashSet<Integer> getClassesIndexWithSpecialized();
//	public HashSet<Integer> getClassesIndex();
//	public HashSet<Integer> getManualClasses();
	public MiEmpresa getMiEmpresa(Integer tableId) throws SQLException, NamingException;
//	public void inicializeMiEmpresa() throws SQLException, NamingException;
//	public String getFormat(Integer ido);
//	public void putClassesIndex(Integer idto);
//	public void deleteClassesIndex(HashSet<Integer> idtos) throws SQLException, NamingException;
}
