package dynagent.server.ejb;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;

public interface ISessionHome extends EJBLocalHome {
	
    public ISession create(String id) throws CreateException;
    
    public ISession findByPrimaryKey(String id) throws FinderException;

}
