package dynagent.server.ejb;

import javax.ejb.EJBLocalObject;

public interface instanceLock extends EJBLocalObject{
    public String getUser();
    public void setUser(String name);
}
