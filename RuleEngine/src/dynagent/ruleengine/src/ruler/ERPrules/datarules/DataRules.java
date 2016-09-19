package dynagent.ruleengine.src.ruler.ERPrules.datarules;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.SQLException;
import java.text.ParseException;

import javax.naming.NamingException;

import org.drools.FactHandle;
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
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.sessions.Session;
import dynagent.common.sessions.Sessionable;
import dynagent.ruleengine.src.ruler.IPropertyChangeDrools;
import dynagent.ruleengine.src.sessions.SessionController;

public abstract class DataRules implements Sessionable,IPropertyChangeDrools{
	private FactHandle factHandle = null;
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public DataRules(IKnowledgeBaseInfo ik){
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
	}
	public boolean commit(Session s) throws ApplicationException, NotFoundException {
		//Propagar a la session Madre
		int idMadre = s.getIDMadre();
		if(idMadre!=-1)
		{
			SessionController.getInstance().getSession(idMadre).addSessionable(this);
		}
		return true;
	}

	public void rollBack(Session s) throws ApplicationException, NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		
	}

	public void rollBackOfPropagation(Session sChild) throws ApplicationException, NotFoundException {
		if (!SessionController.getInstance().getSession(sChild.getID()).getSesionables().contains(this))
		{
			SessionController.getInstance().getSession(sChild.getID()).addSessionable(this);
		}
		/*else
			System.out.println("NO AÑADIR : Fact ya esta en HIJA, no se añade ");*/
		
		Session sessionPadre=SessionController.getInstance().getSession(sChild.getIDMadre());
		sessionPadre.getSesionables().remove(this);
	}


	public FactHandle getFactHandle() {
		return factHandle;
	}

	public void setFactHandle(FactHandle factHandle) {
		this.factHandle = factHandle;
	}

	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		pcs.addPropertyChangeListener(pcl);
	}

	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		pcs.removePropertyChangeListener(pcl);
	}
	
	public void removePropertyChangeListeners() {
		PropertyChangeListener[] p=pcs.getPropertyChangeListeners();
		for(int i=0;i<p.length;i++){
			removePropertyChangeListener(p[i]);
		}
	}
}
