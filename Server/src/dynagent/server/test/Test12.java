package dynagent.server.test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.communication.queryData;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.instance;
import dynagent.common.utils.jdomParser;
import dynagent.ruleengine.Constants;
import dynagent.ruleengine.Exceptions.CardinalityExceedException;
import dynagent.ruleengine.Exceptions.IncoherenceInMotorException;
import dynagent.ruleengine.Exceptions.IncompatibleValueException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.Exceptions.OperationNotPermitedException;
import dynagent.ruleengine.alias.IAlias;
import dynagent.ruleengine.meta.api.DataProperty;
import dynagent.ruleengine.meta.api.ObjectProperty;
import dynagent.ruleengine.meta.api.ObjectValue;
import dynagent.ruleengine.meta.api.StringValue;
import dynagent.ruleengine.meta.api.Value;
import dynagent.ruleengine.src.xml.QueryXML;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;

public class Test12 {
	
	private InstanceService m_IS=null;
	private FactoryConnectionDB fcdb = null;
	
	public void test(){
		try {
			//pruebas QueryXML
			ArrayList<SelectQuery> select = new ArrayList<SelectQuery>();
			SelectQuery sq = new SelectQuery("-1000",1, null, null);
			select.add(sq);
			SelectQuery sq2 = new SelectQuery("-3000",2, null, null);
			select.add(sq2);
			SelectQuery sq3 = new SelectQuery("-4000",5, null, null);
			select.add(sq3);
			
			instance ins = new instance(13,-1000);
			DataProperty dp = new DataProperty();
			dp.setIdo(-1000);
			dp.setIdto(13);
			dp.setIdProp(1);
			dp.setDataType(Constants.IDTO_STRING);
			StringValue iv = new StringValue();
			iv.setValue("texto");
			LinkedList<Value> ldv2 = new LinkedList<Value>();
			ldv2.add(iv);
			dp.setValues(ldv2);
			ins.addProperty(-1000, dp);
			
			ObjectProperty op = new ObjectProperty();
			op.setIdo(-1000);
			op.setIdto(13);
			op.setIdProp(7);
			LinkedList<Integer> lov = new LinkedList<Integer>();
			lov.add(new Integer(-2000));
			op.setRangoList(lov);
			LinkedList<ObjectValue> lev = new LinkedList<ObjectValue>();
			ObjectValue ev = new ObjectValue();
			ev.setValue(9000);
			ev.setValueCls(14);
			lev.add(ev);
			op.setExcluList(lev);
			ins.addProperty(-1000, op);
			
			ObjectProperty op2 = new ObjectProperty();
			op2.setIdo(-2000);
			op2.setIdto(14);
			op2.setIdProp(8);
			op2.setCardMax(new Integer(0));
			LinkedList<Integer> lov2 = new LinkedList<Integer>();
			lov2.add(new Integer(-3000));
			op2.setRangoList(lov2);
			ins.addProperty(-2000, op2);

			ObjectProperty op3 = new ObjectProperty();
			op3.setIdo(-3000);
			op3.setIdto(15);
			op3.setIdProp(9);
			LinkedList<Integer> lov3 = new LinkedList<Integer>();
			lov3.add(new Integer(-4000));
			op3.setRangoList(lov3);
			ins.addProperty(-3000, op3);

			DataProperty dp2 = new DataProperty();
			dp2.setIdo(-3000);
			dp2.setIdto(15);
			dp2.setIdProp(2);
			dp2.setDataType(Constants.IDTO_STRING);
			StringValue iv2 = new StringValue();
			iv2.setValue("texto2");
			LinkedList<Value> ldv3 = new LinkedList<Value>();
			ldv3.add(iv);
			dp2.setValues(ldv3);
			ins.addProperty(-3000, dp2);

			ObjectProperty op4 = new ObjectProperty();
			op4.setIdo(-4000);
			op4.setIdto(16);
			op4.setIdProp(10);
			LinkedList<Integer> lov4 = new LinkedList<Integer>();
			lov4.add(new Integer(-5000));
			op4.setRangoList(lov4);
			ins.addProperty(-4000, op4);

			IAlias alias = null;
			QueryXML qxml = new QueryXML(m_IS.getIk(), alias);
			try {
				qxml.setSelect(select);
				Element xml = qxml.toQueryXML(ins, null);
				System.out.println("\n\nXML generado de nuevo con QueryXML: \n" + jdomParser.returnXML(xml));
				queryData qd = m_IS.query(xml, queryData.MODE_ROW);
				System.out.println(qd.toString());
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchElementException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NamingException e) {
				e.printStackTrace();
			} catch (IncoherenceInMotorException e) {
				e.printStackTrace();
			} catch (IncompatibleValueException e) {
				e.printStackTrace();
			} catch (CardinalityExceedException e) {
				e.printStackTrace();
			} catch (SystemException e) {
				e.printStackTrace();
			} catch (RemoteSystemException e) {
				e.printStackTrace();
			} catch (CommunicationException e) {
				e.printStackTrace();
			} catch (InstanceLockedException e) {
				e.printStackTrace();
			} catch (ApplicationException e) {
				e.printStackTrace();
			} catch (OperationNotPermitedException e) {
				e.printStackTrace();
			}
		} catch (DataErrorException e) {
			e.printStackTrace();
		}
	}

	public void start(int business, String gestor) {
		String databaseIP = "localhost";
		fcdb = new FactoryConnectionDB(business, true, null, gestor);
		m_IS = new InstanceService(fcdb, null, false);
		Connect.connectRuler(databaseIP, fcdb, m_IS);
	}

	public static void main(String[] args) {
		try{			
			Test12 test12 = new Test12();
			System.out.println("dbg0");
			int business = Integer.parseInt(args[0]);
			String gestor = args[1];
			test12.start(business, gestor);
			test12.test();
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
