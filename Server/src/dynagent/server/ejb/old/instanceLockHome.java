package dynagent.server.ejb;

import javax.ejb.EJBLocalHome;
import javax.ejb.CreateException;
import javax.ejb.FinderException;

public interface instanceLockHome extends EJBLocalHome{
    public instanceLock create(String id, String name)
            throws CreateException;
    public instanceLock findByPrimaryKey(String id)
            throws FinderException;
}
