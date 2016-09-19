package dynagent.server.applicationMessages;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;
import javax.naming.NamingException;

public interface IQueueHome extends EJBLocalHome { 

	public IQueue create(String destinatary) throws CreateException, NamingException;
	
    public IQueue findByPrimaryKey(String name_destinatary) throws FinderException;

}