package dynagent.server.applicationMessages;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.naming.NamingException;

import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.querys.AuxiliarQuery;

public abstract class MessagesQueue implements EntityBean, IMessagesQueue {

	EntityContext context = null;
	
	public abstract String getDestinatary();
    public abstract void setDestinatary(String destinatary);
	public abstract Integer getIdEntity();
    public abstract void setIdEntity(Integer idEntity);
	public abstract String getIdEvent();
    public abstract void setIdEvent(String idEvent);
	
    public MessagesPrimaryKey ejbCreate() throws CreateException{
        return null;
    }
    
    public void ejbPostCreate(){
    }

	public MessagesPrimaryKey ejbCreate(String destinatary, Integer idEntity, String idEvent) throws CreateException {
		setDestinatary(destinatary);
		setIdEntity(idEntity);
		setIdEvent(idEvent);
        return new MessagesPrimaryKey(destinatary, idEntity, idEvent);
	}
	
	public void ejbPostCreate(String destinatary, Integer idEntity, String idEvent){
    }
	
	public void messageEvent(AtomicEventInfo event, FactoryConnectionDB fcdb) throws NamingException, SQLException {
		String sql = "UPDATE MESSAGES SET TYPE_OBJECT = '" + event.getType() + "',ID_O = " + event.getId() + ",PROP = '" + event.getNameProp() + 
			"',VALUE = '" + event.getValue() + "',OLD_VALUE = '" + event.getOldValue() + "',OPERATION = '" + event.getOperation() + "'" +
			" WHERE NAME_DESTINATARY = '" + this.getDestinatary() + "' AND ID_ENTITY = " + this.getIdEntity() + " AND ID_EVENT = '" + this.getIdEvent() + "'";
		AuxiliarQuery.dbExecUpdate(fcdb, sql);
	}

	public ArrayList<AtomicEventInfo> readEvents(FactoryConnectionDB fcdb) 
			throws NamingException, SQLException {
		ArrayList<AtomicEventInfo> arrayEvent = new ArrayList<AtomicEventInfo>();
		String sqlComprobac = "select TYPE_OBJECT,ID_O,PROP,VALUE,OLD_VALUE,OPERATION from MESSAGES WITH(NOLOCK) " +
				"where NAME_DESTINATARY='" + this.getDestinatary() + "' and ID_ENTITY=" + this.getIdEntity() + 
				"and ID_EVENT='" + this.getIdEvent() + "'";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlComprobac);
			while (rs.next()) {
				String type = rs.getString(1);
				int ido = rs.getInt(2);
				String prop = rs.getString(3);
				String value = rs.getString(4);
				String oldValue = rs.getString(5);
				String operacion = rs.getString(6);
				
				AtomicEventInfo event = new AtomicEventInfo(ido, type, prop, value, oldValue, operacion);
				arrayEvent.add(event);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			con.close();
		}
		return arrayEvent;
	}
	
	public void ejbActivate() throws EJBException, RemoteException {
		System.out.println("MESSAGESQUEUE:ACTIVATE");
	}

	public void ejbLoad() throws EJBException, RemoteException {
		System.out.println("MESSAGESQUEUE:LOAD");
	}

	public void ejbPassivate() throws EJBException, RemoteException {
		System.out.println("MESSAGESQUEUE:PASSIVATE");
	}

	public void ejbRemove() throws RemoveException, EJBException, RemoteException {
		System.out.println("MESSAGESQUEUE:REMOVE");
	}

	public void ejbStore() throws EJBException, RemoteException {
		System.out.println("MESSAGESQUEUE:STORE");
	}

	public void setEntityContext(EntityContext ctx) throws EJBException, RemoteException {
		System.out.println("MESSAGESQUEUE:SETENTITYCONTEXT");
		context = ctx;
	}

	public void unsetEntityContext() throws EJBException, RemoteException {
		System.out.println("MESSAGESQUEUE:UNSETENTITYCONTEXT");
		context = null;
	}

	public EJBLocalHome getEJBLocalHome() throws EJBException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getPrimaryKey() throws EJBException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isIdentical(EJBLocalObject arg0) throws EJBException {
		// TODO Auto-generated method stub
		return false;
	}

	public void remove() throws RemoveException, EJBException {
		// TODO Auto-generated method stub
		
	}

}
