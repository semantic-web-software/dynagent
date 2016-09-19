package dynagent.server.ejb;

import java.net.UnknownHostException;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.naming.NamingException;

public abstract class SessionBean implements EntityBean, ISession {
	
	private EntityContext context = null;
	
	public abstract String getId();      //primary key: user bns mode
    public abstract void setId(String id);
    
    public String ejbCreate() throws CreateException {
        return null;
    }
    
    public void ejbPostCreate() {
    }
    
    public String ejbCreate(String id) throws CreateException, NamingException, UnknownHostException {
		System.out.println("SESSION:CREATE");
    	setId(id);
        return null;
    }
    
    public void ejbPostCreate(String id) {
    }
    
    public boolean isConnected(String id) throws UnknownHostException{
        return (id.equals(getId()));
    }
    
	public void ejbActivate() throws EJBException, RemoteException {
		System.out.println("SESSION:ACTIVATE");
	}

	public void ejbLoad() throws EJBException, RemoteException {
		System.out.println("SESSION:LOAD");
	}

	public void ejbPassivate() throws EJBException, RemoteException {
		System.out.println("SESSION:PASSIVATE");
	}

	public void ejbRemove() throws RemoveException, EJBException, RemoteException {
		System.out.println("SESSION:REMOVE");
	}

	public void ejbStore() throws EJBException, RemoteException {
		System.out.println("SESSION:STORE");
	}

	public void setEntityContext(EntityContext ctx) throws EJBException, RemoteException {
		System.out.println("SESSION:SET_ENTITY_CONTEXT");
		context = ctx;
	}

	public void unsetEntityContext() throws EJBException, RemoteException {
		System.out.println("SESSION:UNSET_ENTITY_CONTEXT");
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
