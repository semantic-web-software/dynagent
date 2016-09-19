package dynagent.server.applicationMessages;

import java.util.ArrayList;

import javax.ejb.EJBLocalObject;

import dynagent.server.ejb.FactoryConnectionDB;

public interface IQueue extends EJBLocalObject {

	public String getDestinatary();      //primary key
    public void setDestinatary(String destinatary);
    
	public void applicationEvent(Integer idEntity, String idEvent, ArrayList<AtomicEventInfo> aEvent, 
			FactoryConnectionDB fcdb);
	public ArrayList<AtomicEventInfo> readEvents(Integer idEntity, String idEvent, FactoryConnectionDB fcdb);

}
