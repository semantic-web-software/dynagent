
/*
 *
 * Copyright 2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */
package dynagent.web;

import java.util.*;
import dynagent.ejb.*;
import javax.naming.*;
import javax.ejb.*;
import javax.jms.*;
import javax.naming.InitialContext;
import dynagent.ejb.jdomParser;
import dynagent.ejb.helperConstant;


import dynagent.ejb.helperConstant;


public class jmsPool extends Object {

	HashMap m_conex= new HashMap();
	QueueConnectionFactory m_ftry;
        QueueConnection connection = null;

      	javax.jms.Queue queueOut = null;//cola de envio al ruler
	javax.jms.Queue	queueIn = null;//cola de recepcion

	public jmsPool(String queueInLabel, String queueOutLabel )
		throws NamingException, JMSException{
		Hashtable props = new Hashtable();
		props.put(	Context.INITIAL_CONTEXT_FACTORY,
				"org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		props.put(Context.PROVIDER_URL, "tcp://localhost:61616");

		Context JMScontext = new InitialContext(props);

		//m_ftry = (QueueConnectionFactory)JMScontext.lookup("java:/activemq/QueueConnectionFactory");
                m_ftry = (QueueConnectionFactory)JMScontext.lookup("QueueConnectionFactory");
			//anteriormente jms/theQueueConnectionFactory

	 	queueOut = (javax.jms.Queue)JMScontext.lookup("dynamicQueues/"+queueOutLabel);
	 	queueIn = (javax.jms.Queue)JMScontext.lookup("dynamicQueues/"+queueInLabel);

                connection = m_ftry.createQueueConnection();
		connection.start();
	}

	private jmsConex getConex( String user, String msgid, boolean reset )
		throws JMSException{
		jmsConex con=null;
                boolean existed=m_conex.containsKey( user );
		if( reset || !existed ){
			//QueueConnection conect = m_ftry.createQueueConnection();
                        if( existed )
                            closeSession(user);
                        System.out.println("NEW JMS CONEX, user "+user +", ID "+msgid);
			QueueSession sessIn= connection.createQueueSession(true,Session.AUTO_ACKNOWLEDGE);
			QueueSession sessOut= connection.createQueueSession(true,Session.AUTO_ACKNOWLEDGE);
                        con= new jmsConex(sessIn,sessOut,user,msgid);
			m_conex.put( user, con );
		}else{
                    con = (jmsConex) m_conex.get(user);
                    System.out.println("JMS CONEX ya existia...");
                    con.print();
                }
		return con;
	}

	public void commitSender( String user )
		throws JMSException{
            if(m_conex.containsKey( user )){
                jmsConex con = (jmsConex) m_conex.get(user);
                con.commitOut();
                if (con.isAllCommitted())
                    clearConex(user);
            }
	}

	public void commitReceiver( String user )
		throws JMSException{
            if(m_conex.containsKey( user )){
                jmsConex con= (jmsConex)m_conex.get( user );
                con.commitIn();
                if(con.isAllCommitted())
                    clearConex(user);
            }
        }

        public void closeSession( String user )
                throws JMSException{
            if(user!=null && m_conex.containsKey( user )){
                jmsConex con= (jmsConex)m_conex.get( user );
                con.commitIn();
                con.commitOut();
                clearConex(user);
            }
        }

        public void rollbackReceiver( String user )
        throws JMSException{
            if(m_conex.containsKey( user )){
                jmsConex con = (jmsConex) m_conex.get(user);
                con.rollbackIn();
            }
        }
        private void clearConex(String user)throws JMSException{
            if (m_conex.containsKey(user)) {
                jmsConex con = (jmsConex) m_conex.get(user);
                con.close();
                m_conex.remove(user);
                System.out.println("POOLJMS, fianlizada session " + user);
            }
        }

        public TextMessage createSendMessage(String user,String msgid, boolean reset) throws JMSException{
            	jmsConex con= getConex(user,msgid,reset);
                return con.createSendMessage();
        }


        public void send( String user, TextMessage msg ) throws JMSException{
            jmsConex con= getConex(user, null,false);
            QueueSender send= con.sessOut.createSender(queueOut);
            con.outCommitted=false;
            send.send(msg);
        }

        public Message receive( String user, long timeout, String mgid, boolean reset )
                throws JMSException, java.util.ConcurrentModificationException{
            jmsConex con= getConex(user,null,reset);
            QueueReceiver tmpRec=con.getReceiver(queueIn,user,mgid);
            con.inCommitted=false;
            return tmpRec.receive(timeout);
        }
}
