package dynagent.server.web;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.rmi.PortableRemoteObject;

import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.Table;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.ejb.Instance;
import dynagent.server.ejb.InstanceHome;
import dynagent.server.ejb.Session;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.services.FactsAdapter;
import dynagent.server.services.InstanceService;

public class Ejb_support {
	public  static Instance getInstanceRef(int business,Context EJBcontext,boolean sharedBean) throws java.rmi.RemoteException, javax.naming.NamingException, InterruptedException, CreateException{
		String bean = "miInstance";
		if (business!=0 && !sharedBean)
			bean += business;
		System.out.println("Bean " + bean);
		Object boundObject = EJBcontext.lookup(bean);
		//InstanceHome hInstance=(InstanceHome)PortableRemoteObject.narrow(boundObject,InstanceHome.class);
		int i=0;
		InstanceHome hInstance=null;
		while (hInstance==null && i<5) {
			hInstance=(InstanceHome)PortableRemoteObject.narrow(boundObject,InstanceHome.class);
			System.out.println("Iteracion"+i);
			if (hInstance==null) {
				Thread.sleep(1000);
				System.out.println("Sleep Iteracion"+i);
				i++;
			}
		}
		Instance ins = (Instance)hInstance.create();
		return ins;
	}
}
