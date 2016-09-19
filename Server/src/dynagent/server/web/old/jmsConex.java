package dynagent.web;

import javax.jms.*;
import dynagent.ejb.helperConstant;

public class jmsConex extends Object{
    boolean outCommitted=false,inCommitted=false;
    public QueueSession sessIn, sessOut;
    private String MSGID=null, user=null;
    private QueueReceiver receiver=null;

    jmsConex(QueueSession sessIn, QueueSession sessOut, String user, String id){
        this.sessIn=sessIn;
        this.sessOut=sessOut;
        MSGID=id;
        this.user=user;
    }

    public void print(){
        System.out.println("JMSCONN: USER " + user +", ID "+MSGID);
        System.out.println("..... OUT "+outCommitted+", IN " + inCommitted );
    }

    public void commitOut() throws JMSException{
        if(!outCommitted){
            outCommitted=true;
            sessOut.commit();
        }
    }
    public void commitIn() throws JMSException{
        if(!inCommitted){
            inCommitted=true;
            sessIn.commit();
        }
    }
    public void rollbackIn() throws JMSException{
        if(!inCommitted && sessIn!=null){
            sessIn.rollback();
        }
    }

    public boolean isInCommitted(){
        return inCommitted;
    }
    public boolean isOutCommitted(){
        return outCommitted;
    }
    public boolean isAllCommitted(){
        return inCommitted && outCommitted;
    }

    public void close() throws JMSException{
        if( receiver!=null ) receiver.close();
        sessIn.close();
        sessOut.close();
    }

    public QueueReceiver getReceiver( Queue queueIn, String user, String msgid ) throws JMSException{
        if( receiver==null )
            if(msgid != null)
                receiver= sessIn.createReceiver(queueIn,"JMSCorrelationID='"+msgid+"'");
            else
                receiver= sessIn.createReceiver(queueIn,"user='"+user.replaceAll("'","''")+"'");
        return receiver;
    }

    public TextMessage createSendMessage() throws JMSException{
        return sessOut.createTextMessage();
    }
    public TextMessage createReceiveMessage() throws JMSException{
        return sessIn.createTextMessage();
    }
}
