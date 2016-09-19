package dynagent.gui.forms.utils;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.NamingException;

import org.jdom.JDOMException;

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
import dynagent.common.sessions.Session;
import dynagent.gui.KnowledgeBaseAdapter;

public interface IColumnProperties {

	public ArrayList<Column> getColumns(int ido,int idto,int idtoTable,Integer idtoParent,Integer userRol,Integer idtoUserTask,Session sess,KnowledgeBaseAdapter kba,boolean filterMode,boolean tree,boolean structural,boolean onlyFirstLevel,HashMap<String,String> aliasMap) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException;

	public boolean isPathVersion();
	
}
