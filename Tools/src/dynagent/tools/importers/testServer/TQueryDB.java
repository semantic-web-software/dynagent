package dynagent.tools.importers.testServer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.Value;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;
import dynagent.tools.importers.Connect;

public class TQueryDB {
	
	private InstanceService m_IS=null;
	private FactoryConnectionDB fcdb = null;
	
	public selectData test(String nameQuery) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		HashMap<String,String> hIdValue = new HashMap<String,String>();
		//xa sumCantidad_Producto:
		//hIdValue.put("articulo", null);
		//hIdValue.put("almacen", null);
		//hIdValue.put("fecha_inicio", null);
		//hIdValue.put("fecha_fin", null);
		selectData sd = m_IS.serverGetQueryDB(nameQuery, hIdValue);
		//System.out.println(sd.toString() + "\n");
		
		/*System.out.println("RESULTADO");
		Iterator it = sd.getIterator();
		while (it.hasNext()) {
			instance ins = (instance)it.next();
			
			System.out.println(ins.idoFilterXIdoToString());
			//System.out.println(ins.getPropertyQuery("artículo",101));
			//System.out.println(ins.getPropertyQuery("proveedor",133));
			
			
			Property p = ins.getPropertyQuery("alumno",109);
			System.out.println("ALUMNO " + ins.getPropertyQuery("alumno",109));
			if (p!=null) {
				System.out.println("VALUE ");
				LinkedList<Value> lVal = p.getValues();
				Iterator it2 = lVal.iterator();
				while (it2.hasNext()) {
					Value val = (Value)it2.next();
					System.out.println(val);
				}
			}
		}*/
		return sd;
	}

	public void start(int business, String gestor, String databaseIP) {
		fcdb = new FactoryConnectionDB(business, true, databaseIP, gestor);
		m_IS = new InstanceService(fcdb, null, false);
		Connect.connectRuler(fcdb, m_IS);
	}

	public static void main(String[] args) {
		try{			
			TQueryDB test = new TQueryDB();
			System.out.println("dbg0");
			String databaseIP = args[0];
			int business = Integer.parseInt(args[1]);
			String gestor = args[2];
			System.out.println("dbg1");
			test.start(business, gestor, databaseIP);
			String nameQuery = args[3];
			System.out.println("dbg2");
			test.test(nameQuery);
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
