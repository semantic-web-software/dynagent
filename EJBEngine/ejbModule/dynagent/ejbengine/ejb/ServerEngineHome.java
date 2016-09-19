package dynagent.ejbengine.ejb;

import java.rmi.RemoteException;
import java.util.LinkedList;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface ServerEngineHome extends EJBHome {

	public ServerEngine create(String gestor, String host, int port, int bns, String user, LinkedList<String> rules) throws RemoteException, CreateException;
	
}
