package dynagent.ejb.old;
/*package dynagent.ejb;

 *
 * Copyright 2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 

//import java.lang.*;
import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.text.*;
import javax.ejb.*;
import javax.jms.*;
import javax.naming.*;
import javax.rmi.*;
import java.rmi.RemoteException;
import java.io.*;
import javax.transaction.*;

///BEGIN: para_JDOM
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
///END: para_JDOM

import dynagent.knowledge.*;
import dynagent.knowledge.instance.*;
import dynagent.communication.*;

//import helperConstant;

public class ProcessEJB implements MessageDrivenBean, MessageListener {
    private MessageDrivenContext sesionCtx;
    Context	EJBcontext;
    public ProcessEJB(){
        System.out.println("PROCESS CREATE");
    }

    public void ejbCreate(){
        System.out.println("PROCESS EJB CREATE");
    }

    public void inicializeProcess(){
        try {
            EJBcontext = new InitialContext();
        } catch (javax.naming.NamingException e) {
            error("PRocesEJB: Error creando ctx:" + e.getMessage(), e);
        } catch (Exception e) {
            error("PRocesEJB: Error JDOM creando IS:" + e.getMessage(), e);
        }
    }

   public Instance getInstanceRef()
           throws java.rmi.RemoteException,javax.ejb.CreateException,javax.naming.NamingException{
       Object boundObject = EJBcontext.lookup("miInstance");
       InstanceHome hInstance=(InstanceHome)PortableRemoteObject.narrow(boundObject,InstanceHome.class);
       return (Instance)hInstance.create();
   }

    public void onMessage(javax.jms.Message message) {
        String user = "", msg = "";
        try {
            if (message != null && message instanceof TextMessage) {
                msg = ((TextMessage) message).getText();
                String msgUID = message.getJMSCorrelationID();
                System.out.println("PROCESS,received:" + msg);
                //los 2 primeros param se utilizan para querys
                message propar= messageFactory.parseMsg(null,msg);
                /////////////////////////////////////////////
                if( propar.hasPropertie(properties.XALoop)){
                    testLoopBackPro(propar.getIntPropertie(properties.id), msgUID);
                    closeDB();
                    System.out.println("DEBUG LOOPBACK");
                    return;
                }

                ////////////////////////////////////////////////
                getInstanceRef().onProcessMessage(msgUID,propar);
            } else
                System.out.println("PROCESS:" + "Message of wrong type: " + msg);
        }catch (Exception e) {
            error("PROCESS ERROR PARSEO JDOM" + e.getMessage(), e);
        }
    }

   private void error(String error, Throwable t){
	System.out.println(error);
	if( t!=null )t.printStackTrace();
      throw new EJBException(error);
   }

   public void ejbRemove() {
       System.out.println("PROCESS CLOSE");
   }

*//**************************************//*
    private String returnXML(org.jdom.Document doc){
	String res="error";
      XMLOutputter outputter = new XMLOutputter("  ", true);
	res= outputter.outputString(doc);
	System.out.println(res);

 	return res;
    }

    private String returnXML(org.jdom.Element doc){
	String res="error";
      XMLOutputter outputter = new XMLOutputter("  ", true);
	res= outputter.outputString(doc);
 	return res;
    }
   private Document readXML(String str){
       try{
       SAXBuilder builder = new SAXBuilder();
	 StringReader sr= new StringReader(str);
       return builder.build(sr);
       }
       catch(JDOMException e){
           throw new EJBException(e.getMessage() + " EXPERT_ERROR_JDOM");
       }
   }

*//*********************** Database Routines *************************//*

   private void loadRow() throws SQLException {
   }

   private String getIntAttributeValue(Element nodo, String label){
       String val= nodo.getAttributeValue(label);
       if(val== null) return "null";
       return val;
   }

    public void setMessageDrivenContext(MessageDrivenContext
                                        messageDrivenContext) throws
            EJBException {
        System.out.println("PROCESS SET CONTEXT");
        this.sesionCtx = messageDrivenContext;
        inicializeProcess();
    }

}
*/