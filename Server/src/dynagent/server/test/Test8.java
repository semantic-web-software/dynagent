package dynagent.server.test;

import java.sql.SQLException;
import java.util.LinkedList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jdom.JDOMException;

import dynagent.common.knowledge.action;
import dynagent.common.knowledge.instance;
import dynagent.ruleengine.Constants;
import dynagent.ruleengine.meta.api.DataProperty;
import dynagent.ruleengine.meta.api.DataValue;
import dynagent.ruleengine.meta.api.ObjectProperty;
import dynagent.ruleengine.meta.api.ObjectValue;
import dynagent.ruleengine.meta.api.StringValue;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;

public class Test8 {

	private InstanceService m_IS;
	private FactoryConnectionDB factConnDB;
	
	public void test() {
		try {
			System.out.println("dbg1");
			
			instance ins = new instance(0,0);
			DataProperty dp = new DataProperty();
			dp.setIdo(10003);
			dp.setIdto(13);
			dp.setIdProp(1);
			dp.setDataType(Constants.IDTO_STRING);
			StringValue iv = new StringValue();
			iv.setOrder(action.SET);
			iv.setValue("MARIA");
			LinkedList<DataValue> ldv2 = new LinkedList<DataValue>();
			ldv2.add(iv);
			dp.setValueList(ldv2);
			ins.addProperty(10003, dp);
			
			ObjectProperty op = new ObjectProperty();
			op.setIdo(10001);
			op.setIdto(13);
			op.setIdProp(7);
			ObjectValue ov = new ObjectValue();
			ov.setOrder(action.DEL_OBJECT);
			ov.setValue(10011);
			ov.setValueCls(14);
			LinkedList<ObjectValue> lov = new LinkedList<ObjectValue>();
			lov.add(ov);
			op.setValueList(lov);
			ins.addProperty(10001, op);
			
			/*ObjectProperty op2 = new ObjectProperty();
			op2.setIdo(10001);
			op2.setIdto(13);
			op2.setIdProp(Constants.IdPROP_LEVEL);
			ObjectValue ov2 = new ObjectValue();
			ov2.setOrder(action.DEL);
			LinkedList<ObjectValue> lov2 = new LinkedList<ObjectValue>();
			lov2.add(ov2);
			op2.setValueList(lov2);
			ins.addProperty(10001, op2);*/

			DataProperty dp2 = new DataProperty();
			dp2.setIdo(10011);
			dp2.setIdProp(1515151);
			dp2.setIdto(13);
			dp2.setDataType(Constants.IDTO_STRING);
			StringValue sv = new StringValue();
			sv.setOrder(action.NEW);
			sv.setValue("pruebaNew");
			LinkedList<DataValue> ldv = new LinkedList<DataValue>();
			ldv.add(sv);
			dp2.setValueList(ldv);
			ins.addProperty(10011, dp2);

			//y si añadimos la property la actualizaria:
			DataProperty dp3 = new DataProperty();
			dp3.setIdo(34);
			dp3.setIdProp(7);
			dp3.setIdto(13);
			dp3.setDataType(Constants.IDTO_STRING);
			StringValue sv3 = new StringValue();
			sv3.setOrder(action.NEW);
			sv3.setValue("pruebaIndex");
			LinkedList<DataValue> ldv3 = new LinkedList<DataValue>();
			ldv3.add(sv3);
			dp3.setValueList(ldv3);
			ins.addProperty(34, dp3);

			//para probar hay que poner este metodo como publico
			m_IS.processIndex(null, "Loli", 34, 13, ins, null);
			System.out.println(ins.toString());
		} catch(Exception e) {
			System.out.println("Exception:"+e.getMessage());
			e.printStackTrace();
			System.exit(0);
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
			m_IS = new InstanceService(factConnDB, cont, false);
		} catch (NamingException e) {
			e.printStackTrace();
		}
		Connect.connectRuler(databaseIP, factConnDB, m_IS);
	}

	public static void main(String[] args) {
		try{			
			Test8 test8 = new Test8();
			System.out.println("dbg0");
			int business = Integer.parseInt(args[0]);
			String gestor = args[1];
			test8.start(business, gestor);
			test8.test();
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
