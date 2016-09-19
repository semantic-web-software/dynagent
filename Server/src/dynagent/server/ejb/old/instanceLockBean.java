package dynagent.server.ejb;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;

public abstract class instanceLockBean implements EntityBean, instanceLock {
    //public Integer ID;
    private EntityContext context;

    public String ejbCreate() throws CreateException{
        return null;
    }

    public String ejbCreate(String id, String name) throws CreateException{
		System.out.println("LOCK:CREATE");
        setId(id);
        setUser(name);
        return null;
    }

    public void ejbPostCreate(){
    }

    public void ejbPostCreate(String id, String name){
    }

    public void setEntityContext(EntityContext entityContext)  {
		System.out.println("LOCK:SET_ENTITY_CONTEXT");
		context = entityContext;
    }

    public void unsetEntityContext()  {
		System.out.println("LOCK:UNSET_ENTITY_CONTEXT");
		context = null;
    }

    public void ejbRemove() {
		System.out.println("LOCK:REMOVE");
    }

    public void ejbActivate()  {
		System.out.println("LOCK:ACTIVATE");
    }

    public void ejbPassivate() {
		System.out.println("LOCK:PASSIVATE");
    }

    public void ejbLoad()  {
		System.out.println("LOCK:LOAD");
    }

    public void ejbStore() {
		System.out.println("LOCK:STORE");
    }

	public EJBLocalHome getEJBLocalHome() throws EJBException {
		return null;
	}

	public Object getPrimaryKey() throws EJBException {
		return null;
	}

	public boolean isIdentical(EJBLocalObject arg0) throws EJBException {
		return false;
	}

	public void remove() throws RemoveException, EJBException {
	}

    public abstract void setId(String id);
    public abstract String getId();
    public abstract void setUser(String user);
    public abstract String getUser();
}
