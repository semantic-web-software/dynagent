package dynagent.gui.forms;

import gdev.gen.AssignValueException;

import java.awt.Dimension;
import java.sql.SQLException;
import java.text.ParseException;

import javax.naming.NamingException;
import javax.swing.JPanel;

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
import dynagent.common.knowledge.PropertyValue;
import dynagent.common.properties.Property;
import dynagent.common.sessions.Session;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.WindowComponent;

public class DataTransferControl extends transitionControl{
	
	public DataTransferControl(Session ses,
			Integer userRol,
			int ido,
			int idto,
			Integer idtoUserTask,
			int operation,
			Dimension dim,
			JPanel botonera, KnowledgeBaseAdapter kba, WindowComponent dialog) throws NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, OperationNotPermitedException{
		
		super(ses, userRol, null, ido, idto, idtoUserTask, operation, dim, botonera, kba, dialog, null, null, true, true, null);
	}
	
	public String confirm() throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, OperationNotPermitedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException{
		String mensajeRespuesta=null;
		try {
			m_kba.checkCoherenceObject(m_ido, m_userRol, m_kba.getServer().getUser(), m_idtoUserTask, m_session);
			stopEdition(m_ido,m_session);
			// No nos interesa que compruebe si el usuario o el sistema han hecho cambios
			askCancel=false;
			cancel();
		} catch (CardinalityExceedException e) {
			Property prop=e.getProp();
			String message=e.getUserMessage();
			if (prop!=null){
				if (prop.getIdo()!=m_ido){
					message+=": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_idtoUserTask) + " de "+m_kba.getLabelClass(prop.getIdto(), m_idtoUserTask)+" '"+m_kba.getValueData(m_kba.getRDN(prop.getIdo(), prop.getIdto(), m_userRol, m_idtoUserTask, m_session))+"'";	
				}else{
					message+=": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_idtoUserTask);
				}
				
			}
			Singleton.getInstance().getMessagesControl().showErrorMessage(message,dialog.getComponent());
		}

		return mensajeRespuesta;
	}
	
}
