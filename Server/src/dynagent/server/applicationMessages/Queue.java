package dynagent.server.applicationMessages;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import dynagent.common.exceptions.DataErrorException;
import dynagent.server.ejb.AuxiliarEJB;
import dynagent.server.ejb.FactoryConnectionDB;

public abstract class Queue implements EntityBean, IQueue {

	private EntityContext context = null;
	
	public abstract String getDestinatary();      //primary key
    public abstract void setDestinatary(String destinatary);

    public String ejbCreate() throws CreateException{
        return null;
    }
    
    public void ejbPostCreate(){
    }
    
    public String ejbCreate(String destinatary) throws CreateException, NamingException{
        setDestinatary(destinatary);
        return null;
    }
    
    public void ejbPostCreate(String destinatary){
    }
    
	public void applicationEvent(Integer idEntity, String idEvent, ArrayList<AtomicEventInfo> aEvent, FactoryConnectionDB fcdb) {
		String destinatary = getDestinatary();
		try {
			IMessagesQueue messages = getMessagesRef(destinatary, idEntity, idEvent, true);
			for (int i=0;i<aEvent.size();i++)
				messages.messageEvent(aEvent.get(i), fcdb);
		}catch(java.rmi.RemoteException e){
			AuxiliarEJB.error("Queue, applicationEvent, error Remote:", e);
		}catch(javax.ejb.CreateException e){
			AuxiliarEJB.error("Queue, applicationEvent, error Create:", e);
		}catch(javax.naming.NamingException e){
			AuxiliarEJB.error("Queue, applicationEvent, error Naming:", e);
		} catch (SQLException e) {
			AuxiliarEJB.error("Queue, applicationEvent, error SQL:", e);
		}
	}
	
	public ArrayList<AtomicEventInfo> readEvents(Integer idEntity, String idEvent, FactoryConnectionDB fcdb) {
		ArrayList<AtomicEventInfo> arrayEvent = null;
		try {
			String destinatary = getDestinatary();
			IMessagesQueue messages = getMessagesRef(destinatary, idEntity, idEvent, false);
			arrayEvent = messages.readEvents(fcdb);
		} catch (RemoteException e) {
			AuxiliarEJB.error("Queue, applicationEvent, error Remote:", e);
		} catch (CreateException e) {
			AuxiliarEJB.error("Queue, applicationEvent, error Create:", e);
		} catch (NamingException e) {
			AuxiliarEJB.error("Queue, applicationEvent, error Naming:", e);
		} catch (SQLException e) {
			AuxiliarEJB.error("Queue, applicationEvent, error SQL:", e);
		}
		return arrayEvent;
	}

	public IMessagesQueue getMessagesRef(String destinatary, Integer idEntity, String idEvent, boolean isInsert) 
			throws java.rmi.RemoteException, javax.ejb.CreateException, javax.naming.NamingException, DataErrorException {
		Context EJBcontext = new InitialContext();
		Object boundObject = EJBcontext.lookup("applicationMessages/IMessagesQueueLocal");
		IMessagesQueueHome hMessages = (IMessagesQueueHome)PortableRemoteObject.narrow(boundObject,IMessagesQueueHome.class);
		IMessagesQueue messagesQueue = null;
		boolean exists = false;
		try {
			messagesQueue = hMessages.findByPrimaryKey(new MessagesPrimaryKey(destinatary, idEntity, idEvent));
			exists = true;
		} catch (FinderException e) {
			if (isInsert)
				messagesQueue = hMessages.create(destinatary, idEntity, idEvent);
		}
		if (exists && isInsert) {
			throw new DataErrorException("Error al insertar los datos. Ya existe un registro con clave primaria:" + destinatary + "," 
					+ idEntity + "," + idEvent + " en la tabla Messages");
		}
		return messagesQueue;
	}
	
	public void ejbActivate() throws EJBException, RemoteException {
		System.out.println("QUEUE:ACTIVATE");
	}

	public void ejbLoad() throws EJBException, RemoteException {
		System.out.println("QUEUE:LOAD");
	}

	public void ejbPassivate() throws EJBException, RemoteException {
		System.out.println("QUEUE:PASSIVATE");
	}

	public void ejbRemove() throws RemoveException, EJBException, RemoteException {
		System.out.println("QUEUE:REMOVE");
	}

	public void ejbStore() throws EJBException, RemoteException {
		System.out.println("QUEUE:STORE");
	}

	public void setEntityContext(EntityContext ctx) throws EJBException, RemoteException {
		System.out.println("QUEUE:SET_ENTITYC_ONTEXT");
		context = ctx;
	}

	public void unsetEntityContext() throws EJBException, RemoteException {
		System.out.println("QUEUE:UNSET_ENTITY_CONTEXT");
		context = null;
	}

	public EJBLocalHome getEJBLocalHome() throws EJBException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getPrimaryKey() throws EJBException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isIdentical(EJBLocalObject arg0) throws EJBException {
		// TODO Auto-generated method stub
		return false;
	}

	public void remove() throws RemoveException, EJBException {
		// TODO Auto-generated method stub
		
	}
}
