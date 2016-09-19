package dynagent.server.test;

import java.sql.SQLException;

import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.server.ejb.Asigned;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;

public class Test7 {

	private InstanceService m_IS;
	private FactoryConnectionDB factConnDB;

	public void test() {
		int idoUTask = 15;
//		int idtoUTask = 133;
		try {
			Asigned.asign(m_IS, idoUTask, "lola", null, factConnDB, m_IS.getIk());
//			Asigned.preAsign(m_IS, idoUTask, idtoUTask, "lola", null, factConnDB, m_IS.getIk());
		} catch (ApplicationException e) {
			e.printStackTrace();
		} catch (EJBException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (InstanceLockedException e) {
			e.printStackTrace();
		}
	}
	
	public void start(int business, String gestor) {
		String databaseIP = "localhost";
		factConnDB = new FactoryConnectionDB(business, true, null, gestor);
		try {
			//prueba valida solo con JBoss,
			//para probar el metodo habria que cambiar el constructor y 
			//el uso de las funciones de bloqueo y desbloqueo en processIndex
			Context cont = new InitialContext();
			m_IS = new InstanceService(factConnDB,cont, false);
		} catch (NamingException e) {
			e.printStackTrace();
		}
		Connect.connectRuler(databaseIP, factConnDB, m_IS);
	}

	public static void main(String[] args) {
		try{			
			Test7 test7 = new Test7();
			System.out.println("dbg0");
			int business = Integer.parseInt(args[0]);
			String gestor = args[1];
			test7.start(business, gestor);
			test7.test();
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
