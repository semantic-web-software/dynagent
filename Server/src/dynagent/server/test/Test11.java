package dynagent.server.test;

import java.util.ArrayList;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import dynagent.server.applicationMessages.AtomicEventInfo;
import dynagent.server.applicationMessages.IQueue;
import dynagent.server.applicationMessages.IQueueHome;
import dynagent.server.ejb.FactoryConnectionDB;

public class Test11 {

	public static void pruebaBeansMensajes(Context initCtx, FactoryConnectionDB factConnDB) throws NamingException, CreateException {
		Object boundObject = initCtx.lookup("applicationMessages/IQueueLocal");
		IQueueHome home = (IQueueHome)PortableRemoteObject.narrow (boundObject, IQueueHome.class);
		//comprobacion al crear llamando a findByPrimaryKey x si esta, si excepcion crear, si no usarlo
		
		IQueue queue = null;
		try {
			queue = home.findByPrimaryKey("prueba");
		} catch (FinderException e) {
			queue = home.create("prueba");
		}
		Integer idEntity = 1;
		String idEvent = "1";
		ArrayList<AtomicEventInfo> aaei = new ArrayList<AtomicEventInfo>();
		AtomicEventInfo aei = new AtomicEventInfo(5,"type","name","value","oldValue","op");
		aaei.add(aei);
		queue.applicationEvent(idEntity, idEvent, aaei, factConnDB);
		queue.readEvents(idEntity, idEvent, factConnDB);
	}
}
