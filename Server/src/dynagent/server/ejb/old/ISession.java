package dynagent.server.ejb;

import java.net.UnknownHostException;

import javax.ejb.EJBLocalObject;

public interface ISession extends EJBLocalObject {

	public String getId();      //primary key
    public void setId(String id);
    
    public boolean isConnected(String id) throws UnknownHostException;

}
