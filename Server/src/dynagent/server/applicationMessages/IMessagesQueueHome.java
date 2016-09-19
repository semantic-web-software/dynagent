package dynagent.server.applicationMessages;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;

public interface IMessagesQueueHome extends EJBLocalHome {
	
	public IMessagesQueue create(String destinatary, Integer idEntity, String idEvent) throws CreateException;

	public IMessagesQueue findByPrimaryKey(MessagesPrimaryKey messagePK) throws FinderException;

}