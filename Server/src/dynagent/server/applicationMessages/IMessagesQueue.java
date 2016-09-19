package dynagent.server.applicationMessages;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.ejb.EJBLocalObject;
import javax.naming.NamingException;

import dynagent.server.ejb.FactoryConnectionDB;

public interface IMessagesQueue extends EJBLocalObject {

	public String getDestinatary();
    public void setDestinatary(String destinatary);
	public Integer getIdEntity();
    public void setIdEntity(Integer idEntity);
	public String getIdEvent();
    public void setIdEvent(String idEvent);
    
	public void messageEvent(AtomicEventInfo event, FactoryConnectionDB fcdb) 
		throws NamingException, SQLException;
	
	public ArrayList<AtomicEventInfo> readEvents(FactoryConnectionDB fcdb)
		throws NamingException, SQLException;

}
