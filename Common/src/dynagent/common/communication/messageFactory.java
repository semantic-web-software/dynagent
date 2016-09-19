package dynagent.common.communication;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.exceptions.DataErrorException;
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.QuantityDetail;
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.access;
import dynagent.common.knowledge.contextData;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.DomainProp;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.BooleanValue;
import dynagent.common.properties.values.DataValue;
import dynagent.common.properties.values.DoubleValue;
import dynagent.common.properties.values.IntValue;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.StringValue;
import dynagent.common.properties.values.TimeValue;
import dynagent.common.properties.values.UnitValue;
import dynagent.common.properties.values.Value;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.RowItem;
import dynagent.common.utils.jdomParser;

/**
 * Esta clase constituye un patrón Factoria sobre los mensajes.
 * <br>Se encarga de la aplicación de un parser sobre el mensaje inicialmente en XML para 
 * modificar el formato y de ir construyendo parte por parte el tipo de éste, datos, acciones, 
 * bloqueos, ...
 */

public class messageFactory {
    public static message parseMsg(Object param, String msg) throws ParseException,DataErrorException {
        //System.out.println("PARSE MSG "+msg);
        if( msg==null || msg.length()<5 )
           throw new DataErrorException("Paquete nulo o de tamaño menor mínimo");
        //int posCab = 50;
        //if (msg.length() < 50) {
          //  posCab = msg.length();
        //}
        if (msg.substring(0, 5).indexOf("ROOT_") >= 0) {
            //System.out.println("MSG REC:"+msg);
        	
        	int mode = Integer.parseInt(msg.substring(9, 10));
        	if (mode==queryData.MODE_FACT) {
	            long ini = System.currentTimeMillis();
	            IndividualData res = queryData.parseIndividualData((Element)param,msg);
	            //System.out.println("QUERY DATA, TIME:" + (System.currentTimeMillis() - ini));
	            contextAction act = new contextAction(message.MSG_QUERY);
	            act.setIndividualData(res);
	            try{
	            	Document xmlData= queryData.parseXmlData((Element)param,msg);
	            	act.setXmlData(xmlData);
	            } catch(JDOMException e) {
	            	e.printStackTrace();
	                throw new ParseException(e.getMessage(), 0);
	            }
	            return act;
        	} else if (mode==queryData.MODE_ROW_ITEM) {
	            long ini = System.currentTimeMillis();
	            ArrayList<RowItem> res = queryData.parseRowItem((Element)param,msg);
	            //System.out.println("QUERY DATA, TIME:" + (System.currentTimeMillis() - ini));
	            message msgObj= new message(message.MSG_QUERY);
	            msgObj.setContent(res);
	            return msgObj;
        	} else {
	            long ini = System.currentTimeMillis();
	            selectData res = queryData.parse((Element)param,msg);
	            //System.out.println("QUERY DATA, TIME:" + (System.currentTimeMillis() - ini));
	            message msgObj= new message(message.MSG_QUERY);
	            msgObj.setContent(res);
	            return msgObj;
        	}
        }/* else if (msg.substring(0, 8).indexOf("ATTRIBS:") >= 0) {
            message msgObj= new message(message.MSG_REPORT);
            msgObj.setContent(msg.substring(0,msg.length()));
            return msgObj;
        }*/ else {
            try {
                Element doc = jdomParser.readXML(msg).getRootElement();
                return parseMsg(doc);
            } catch(JDOMException e) {
            	e.printStackTrace();
                throw new ParseException(e.getMessage(), 0);
            }
        }
    }

    public static message parseMsg(Element doc) throws ParseException{
        message msgRes = null;
        if (doc.getAttributeValue("MSG_TYPE") == null) {
            throw new ParseException("NOT MSG TYPE ATTRIBUTE", 0);
        }
        /*try {
			System.err.println(jdomParser.returnXML(doc));
		} catch (JDOMException e) {
			e.printStackTrace();
		}*/
        int msgType = parseMsgType(doc);
        System.out.println("MSG_TYPE " + msgType);
        int orderType= parseOrderType(doc);
        
        if( msgType== message.MSG_DESCRIPTION ||
        		msgType== message.MSG_PREPRINT || msgType== message.MSG_GETRDN || msgType== message.MSG_GETRDNLIST ||
        		msgType== message.MSG_RULER || msgType== message.MSG_DISCONNECTION || 
        		msgType== message.MSG_RESET_LOCKS || msgType== message.MSG_REPORT || 
        		msgType== message.MSG_REPORTS_CLASIFICATOR || msgType== message.MSG_CHANGE_MODE ||
        		msgType== message.MSG_DATE_NOW || msgType==message.MSG_UPDATE_LICENSE || msgType==message.MSG_CODE_LICENSE){
            msgRes= new message(msgType);
            buildMessageData(doc, msgRes);
            return msgRes;
//        } else if( msgType== message.MSG_SELECTION ){
//            msgRes= new message(message.MSG_SELECTION);
//            msgRes.setContent(buildSelectionData(doc));
//            return msgRes;
        } else if( msgType== message.MSG_QUERYRULES ){
            msgRes= new message(message.MSG_QUERYRULES);
            Element content = doc.getChild("CONTENT");
            if (content!=null)
            	msgRes.setContent(content);
            return msgRes;
        } else if( msgType== message.MSG_ERROR ||
            msgType== message.MSG_LOGIN ||
            msgType== message.MSG_LOGIN_OVERWRITTEN ||
            msgType== message.MSG_CONFIRM ||
            msgType== message.MSG_COMMIT ||
            msgType== message.MSG_MIGRATION ||
            msgType== message.MSG_QUERY ||
            msgType== message.MSG_UPDATE ||
            msgType==message.MSG_SEND_EMAIL ||
            msgType==message.MSG_SEND_SERVER_LOG_EMAIL) {
        	System.out.println("DENTRO UPDATE " + msgType);
            msgRes = new message(msgType);
            buildMessageData(doc, msgRes);
            buildContent( doc, msgRes );
            return msgRes;
        } else if (msgType == message.MSG_LOG_ERROR || msgType == message.MSG_LOG_ERROR_LOGIN) {
            msgRes= buildErrorTrace(doc, msgType);
            buildMessageData(doc,msgRes);
            return msgRes;
        }else if (msgType == message.MSG_BLOCK) {
            msgRes = new block();
            buildMessageData(doc, msgRes);
            if( doc.getChild("CONTENT")!=null ){
                Element cont=doc.getChild("CONTENT");
                Iterator iB = cont.getChildren().iterator();
                while (iB.hasNext()) {
                    Element child = (Element) iB.next();
                    ((block) msgRes).addMsg(messageFactory.parseMsg(child));
                }
            }
            return msgRes;
        //}else
        //if (msgType == message.MSG_SELECTION) {
            //msgRes = new message(message.MSG_SELECTION);
            //msgRes.setContent(doc);
            //return msgRes;
        }else if (msgType == message.MSG_OBJECT_TRAN || msgType == message.MSG_EXE_TRAN_ACTION) {
            msgRes = new contextAction(orderType);
            buildMessageData(doc,msgRes);
            //buildFlowData(doc, (flowAction) msgRes);
            buildContextActionData(doc, (contextAction) msgRes);
            //buildLockData(doc,(lockContainer)msgRes);
            
            return msgRes;
        } else if (msgType == message.MSG_FLOW) {
            if (doc.getAttributeValue("CURR_PARENT_TASK") != null ||
                doc.getAttributeValue("OLD_STATE") != null) {
                msgRes = new threadData();
                buildMessageData(doc,msgRes);
                buildThreadData(doc, (threadData) msgRes);
            } else {
                msgRes = new flowAction();
                buildMessageData(doc,msgRes);
            }
            buildFlowData(doc, (flowAction) msgRes);
            return msgRes;
        }else if (msgType == message.MSG_OWNING ) {
            msgRes = new owningAction();
            buildMessageData(doc,msgRes);
            buildFlowData(doc, (flowAction) msgRes);
            buildOwningData(doc, (owningAction)msgRes);
            return msgRes;
        }else if (msgType == message.MSG_LOCK ||
            msgType == message.MSG_UNLOCK) {
            msgRes = new lock(msgType == message.MSG_UNLOCK);
            buildMessageData(doc,msgRes);
            buildLockData(doc,(lockContainer)msgRes);
            return msgRes;
        }else if (msgType == message.MSG_POOLING) {
            msgRes = new pooling();
            buildMessageData(doc,msgRes);
            buildLockData(doc,(pooling)msgRes);
            return msgRes;
        }else if (msgType == message.MSG_CHANGES) {
            	msgRes = new message(message.MSG_CHANGES);
                msgRes.setContent(buildChanges(doc));
                return msgRes;
        } else if( msgType== message.MSG_RESERVE) {
            msgRes = new message(msgType);
            buildMessageData(doc, msgRes);
        	if (orderType==message.ACTION_RESULT_RESERVE) {
        		msgRes.setContent(buildResultReservation(doc));
        	} else {
        		msgRes.setContent(preBuildReservation(doc));
        	}
            return msgRes;
        }

        return msgRes;
    }

    public static int parseMsgType(Element root){
        return (root.getAttributeValue("MSG_TYPE")==null ? 0:
               Integer.parseInt(root.getAttributeValue("MSG_TYPE")));
    }
    public static int parseOrderType(Element root){
        return (root.getAttributeValue("ORDER_TYPE")==null ? 0:
               Integer.parseInt(root.getAttributeValue("ORDER_TYPE")));
    }
    public static int parseDataType(Element root){
        return (root.getAttributeValue("DATA_TYPE") == null ? 0 :
                Integer.parseInt(root.getAttributeValue("DATA_TYPE")));
    }
    public static int parseMsgType(jdomParser root){
        return (root.getAttributeValue("MSG_TYPE")==null ? 0:
               Integer.parseInt(root.getAttributeValue("MSG_TYPE")));
    }
    public static int parseOrderType(jdomParser root){
        return (root.getAttributeValue("ORDER_TYPE")==null ? 0:
               Integer.parseInt(root.getAttributeValue("ORDER_TYPE")));
    }

    private static message buildErrorTrace(Element doc, int msgType){
        Element eRoot= doc.getChild("CONTENT");
        if( eRoot==null ) return new errorTrace(msgType);
        String desc=null, debug=null, error=null;
        if( eRoot.getChild("DESC")!=null ){
            List lista=eRoot.getChild("DESC").getContent();
            if( lista.get(0) instanceof CDATA){
            	CDATA comm = (CDATA) lista.get(0);
                desc = comm.getText();
            }else
                desc="Error LOG CLASS DESC";
        }
        if( eRoot.getChild("DEBUG")!=null ){
            List lista=eRoot.getChild("DEBUG").getContent();
            boolean success=false;
            for( int i=0;i<lista.size();i++){
                if (lista.get(i) instanceof CDATA) {
                    success=true;
                    CDATA comm = (CDATA) lista.get(i);
                    debug = comm.getText() ;
                    break;
                }
            }
            if(!success)
                debug="'Error LOG CLASS DEBUG'";
        }
        if( eRoot.getChild("ERROR")!=null )
            error = eRoot.getChild("ERROR").getText();

        message msgRes = new errorTrace(debug,error,desc,msgType);
        return msgRes;
    }

    private static int buildMessageData(Element root, message msg) {
        int msgType= parseMsgType(root);
        msg.setType(msgType);

        if (root.getAttributeValue("BNS") != null)
            msg.setBusiness(Integer.parseInt(root.getAttributeValue("BNS")));
        if (root.getAttributeValue("UTASK") != null)
            msg.setUserTask(Integer.parseInt(root.getAttributeValue("UTASK")));
        if (root.getAttributeValue("MSGUID") != null)
            msg.setMsguid(root.getAttributeValue("MSGUID"));
        if (root.getAttributeValue("USER") != null)
            msg.setUser(root.getAttributeValue("USER"));
        if (root.getAttributeValue("USERROLS") != null)
            msg.setUserRols(root.getAttributeValue("USERROLS"));
        if (root.getAttributeValue("USER_ROL") != null)
            msg.setUserRol(Integer.parseInt(root.getAttributeValue("USER_ROL")));
        if (root.getAttributeValue("MODE") != null)
            msg.setMode(root.getAttributeValue("MODE"));
        if (root.getAttributeValue("SUBSCRIPTION") != null)
            msg.setSubscription(root.getAttributeValue("SUBSCRIPTION"));
        if (root.getAttributeValue("VERSION") != null)
            msg.setVersion(root.getAttributeValue("VERSION"));
        if (root.getAttributeValue("CNXID") != null)
            msg.setClientSession(root.getAttributeValue("CNXID"));
        if (root.getAttributeValue("WINID") != null)
            msg.setWindowSession(Integer.parseInt(root.getAttributeValue("WINID")));
        if (root.getAttributeValue("RESULT_CODE") != null)
            msg.setResultCode(Integer.parseInt(root.getAttributeValue("RESULT_CODE")));
        if (root.getAttributeValue("DATA_TYPE") != null)
            msg.setDataType(Integer.parseInt(root.getAttributeValue("DATA_TYPE")));
        msg.setOrderType(parseOrderType(root));
        if( root.getChild("PROPERTIES")!=null ){
            Iterator itr= root.getChild("PROPERTIES").getChildren().iterator();
            while(itr.hasNext()){
                Element prop=(Element)itr.next();
                String keyStr= prop.getAttributeValue("KEY");
                if( prop.getName().equals("PROP") ){
                	Integer key = Integer.parseInt(keyStr);
                    if( prop.getChild("ITEM")!=null ){
                        ArrayList<String> pplist=new ArrayList<String>();
                        Iterator iI=prop.getChildren("ITEM").iterator();
                        while(iI.hasNext()){
                            Element item=(Element)iI.next();
                            pplist.add(item.getText());
                        }
                        msg.addAllPropertie(key, pplist);
                    }else
                        msg.addPropertie(key, prop.getAttributeValue("VALUE"));
                } else 
                if( prop.getName().equals("AT") )
                	if (Auxiliar.hasIntValue(keyStr)) {
                    	Integer key = Integer.parseInt(keyStr);
                    	msg.addDirectAttribute(key,prop.getAttributeValue("VALUE"));
                	} else
                		msg.addDirectAttribute(keyStr,prop.getAttributeValue("VALUE"));
            }
        }
        return msgType;
    }

    private static void buildFlowData(Element root, flowAction act) {
        if (root.getAttributeValue("CURR_PROCESS") != null)
            act.setCurrProcess(Integer.parseInt(root.getAttributeValue("CURR_PROCESS")));
        if (root.getAttributeValue("CURR_TASK") != null)
            act.setCurrTask(Integer.parseInt(root.getAttributeValue("CURR_TASK")));
        if (root.getAttributeValue("PROCESS_TYPE") != null)
            act.setProcessType(Integer.parseInt(root.getAttributeValue("PROCESS_TYPE")));
        if (root.getAttributeValue("TASK_TYPE") != null)
            act.setTaskType(Integer.parseInt(root.getAttributeValue("TASK_TYPE")));
        if (root.getAttributeValue("PARENT_TASK") != null)
            act.setCurrParentTask(Integer.parseInt(root.getAttributeValue("PARENT_TASK")));
        if (root.getAttributeValue("EXE_DATE") != null)
            act.setExeDate(Long.parseLong(root.getAttributeValue("EXE_DATE")));
        if (root.getAttributeValue("TASK_TRANSITION") != null)
            act.setCurrTaskTrans(Integer.parseInt(root.getAttributeValue("TASK_TRANSITION")));
        if (root.getAttributeValue("TASK_STATE") != null)
            act.setCurrTaskState(Integer.parseInt(root.getAttributeValue("TASK_STATE")));
        if( act.getType()==message.MSG_EXE_TRAN_ACTION ){
            Element eContent= root.getChild("CONTENT");
            if(eContent!=null){
                Iterator ic = eContent.getChildren().iterator();
                if (ic.hasNext())
                    act.setContent((Element) ic.next());
            }
        }
    }

    private static void buildContextActionData(Element root, contextAction act) {
        if (root.getAttributeValue("ID_TO") != null) {
			int idto = Integer.parseInt(root.getAttributeValue("ID_TO"));
			act.setTO_ctx(idto);
		}
        if (root.getAttributeValue("TABLE_ID") != null) {
			int tableId = Integer.parseInt(root.getAttributeValue("TABLE_ID"));
			act.setIDO_ctx(tableId);
		}
        if (root.getChildren("IDTO_TABLEIDS").size()>0) {
            HashMap<Integer,HashSet<Integer>> hIdtoIdos = new HashMap<Integer, HashSet<Integer>>();
            Iterator<Element> it = root.getChildren("IDTO_TABLEIDS").iterator();
	    	while (it.hasNext()) {
	    		Element child = it.next();
	    		Integer idto = Integer.parseInt(child.getAttributeValue("ID_TO"));
	    		HashSet<Integer> idos = Auxiliar.stringToHashSetInteger(child.getAttributeValue("TABLE_IDS"), ",");
	    		hIdtoIdos.put(idto, idos);
	    	}
	        act.setAID_ctx(hIdtoIdos);
        }
        if (root.getAttributeValue("LOCK") != null)
            act.setLock( root.getAttributeValue("LOCK").equals("TRUE"));
        Element content = root.getChild("CONTENT");
        if( content!=null ){
            if( act.getType()== message.MSG_EXE_TRAN_ACTION ) {
                if( content.hasChildren() )
                	act.setContent(content.getChildren().iterator().next());
            } else{
	        	act.setContent(content);
	            //if( act.getType()!=message.MSG_EXE_TRAN_ACTION ) {
	                IndividualData id = buildIPropertyDef(content);
	                act.setIndividualData(id);
	                if(id==null){
	                	act.setXmlData(new Document((Element)content.getChild("datos").clone()));
	                }
	                /*Element data = content.getChild("FACTS");
	                if (data != null) {
	                    IndividualData id = buildIPropertyDef(data);
	                    act.setIndividualData(id);
	//                    act.setIDO_ctx(ins.getIDO());
	//                    act.setTO_ctx(ins.getIdTo());
	//                    if( ins.getRdn()!=null )
	//                        act.setRdn_ctx(ins.getRdn());
	                }*/
	            //}
            }
        }
    }

    private static void buildContent( Element root, message msg ){
        Element content = root.getChild("CONTENT");
         if( content!=null && content.hasChildren() ){
             Element data = (Element) content.getChildren().iterator().next();
             msg.setContent(data);
         }
    }

    private static void elemtoProperty(Property p, Element property) {
		p.setName(property.getAttributeValue("NAME"));
		if (property.getAttributeValue("PROP")!=null)
			p.setIdProp(Integer.parseInt(property.getAttributeValue("PROP")));
		if (property.getAttributeValue("CARD_MIN")!=null)
			p.setCardMin(Integer.parseInt(property.getAttributeValue("CARD_MIN")));
		if (property.getAttributeValue("CARD_MAX")!=null)
			p.setCardMax(Integer.parseInt(property.getAttributeValue("CARD_MAX")));
		if (property.getAttributeValue("ACCESS")!=null)
			p.setTypeAccess(new access(Integer.parseInt(property.getAttributeValue("ACCESS"))));
    	if (StringUtils.equals(property.getAttributeValue("VALUES_FIXED"),"TRUE"))
			p.setValuesFixed(true);
    }
    
    private static void elemListToDataValue(LinkedList<Value> lldv, Element list) {
		Iterator it2 = list.getChildren("DATA_VALUE").iterator();
		while (it2.hasNext()) {
			Element dataValue = (Element)it2.next();
			DataValue dv = elemToDataValue(dataValue);
			if (dv!=null)
				lldv.add(dv);
		}
    }
    private static void elemListToDataValue2(LinkedList<DataValue> lldv, Element list) {
		Iterator it2 = list.getChildren("DATA_VALUE").iterator();
		while (it2.hasNext()) {
			Element dataValue = (Element)it2.next();
			DataValue dv = elemToDataValue(dataValue);
			if (dv!=null)
				lldv.add(dv);
		}
    }
    private static DataValue elemToDataValue(Element dataValue) {
    	if (StringUtils.equals(dataValue.getAttributeValue("UNIT_VALUE"),"TRUE")) {
    		UnitValue uv = new UnitValue();
    		if (dataValue.getAttributeValue("UNIT")!=null)
    			uv.setUnit(Integer.parseInt(dataValue.getAttributeValue("UNIT")));
    		if (dataValue.getAttributeValue("VALUE_MIN")!=null)
    			uv.setValueMin(Double.parseDouble(dataValue.getAttributeValue("VALUE_MIN")));
    		if (dataValue.getAttributeValue("VALUE_MAX")!=null)
    			uv.setValueMax(Double.parseDouble(dataValue.getAttributeValue("VALUE_MAX")));
    		return uv;
    	} else if (StringUtils.equals(dataValue.getAttributeValue("DOUBLE_VALUE"),"TRUE")) {
    		DoubleValue fv = new DoubleValue();
    		if (dataValue.getAttributeValue("VALUE_MIN")!=null)
    			fv.setValueMin(Double.parseDouble(dataValue.getAttributeValue("VALUE_MIN")));
    		if (dataValue.getAttributeValue("VALUE_MAX")!=null)
    			fv.setValueMax(Double.parseDouble(dataValue.getAttributeValue("VALUE_MAX")));
    		return fv;
    	} else if (StringUtils.equals(dataValue.getAttributeValue("INT_VALUE"),"TRUE")) {
    		IntValue iv = new IntValue();
    		if (dataValue.getAttributeValue("VALUE_MIN")!=null)
    			iv.setValueMin(Integer.parseInt(dataValue.getAttributeValue("VALUE_MIN")));
    		if (dataValue.getAttributeValue("VALUE_MAX")!=null)
    			iv.setValueMax(Integer.parseInt(dataValue.getAttributeValue("VALUE_MAX")));
    		return iv;
    	} else if (StringUtils.equals(dataValue.getAttributeValue("BOOLEAN_VALUE"),"TRUE")) {
    		BooleanValue bv = new BooleanValue();
    		if (dataValue.getAttributeValue("VALUE")!=null)
    			bv.setBvalue(Boolean.parseBoolean(dataValue.getAttributeValue("VALUE")));
    		bv.setComment(dataValue.getText());
    		return bv;
    	} else if (StringUtils.equals(dataValue.getAttributeValue("STRING_VALUE"),"TRUE")) {
    		StringValue sv = new StringValue();
    		sv.setValue(dataValue.getText());
    		return sv;
    	} else if (StringUtils.equals(dataValue.getAttributeValue("TIME_VALUE"),"TRUE")) {
    		TimeValue tv = new TimeValue();
    		//tv.setReferenceInstant(dataValue.getText());
    		if (dataValue.getAttributeValue("RELATIVE_SECONDS_MIN")!=null)
    			tv.setRelativeSecondsMin(Long.parseLong(dataValue.getAttributeValue("RELATIVE_SECONDS_MIN")));
    		if (dataValue.getAttributeValue("RELATIVE_SECONDS_MAX")!=null)
    			tv.setRelativeSecondsMax(Long.parseLong(dataValue.getAttributeValue("RELATIVE_SECONDS_MAX")));
    		return tv;
    	}
    	return null;
    }
    
    
    private static void elemToObjectValue2(LinkedList<ObjectValue> llov, Element list) {
		Iterator it2 = list.getChildren("OBJECT_VALUE").iterator();
		while (it2.hasNext()) {
			Element objectValue = (Element)it2.next();
    		ObjectValue ov = elemToObjectValue(objectValue);
    		llov.add(ov);
		}
    }
    private static void elemListToObjectValue(LinkedList<Value> llov, Element list) {
		Iterator it2 = list.getChildren("OBJECT_VALUE").iterator();
		while (it2.hasNext()) {
			Element objectValue = (Element)it2.next();
    		ObjectValue ov = elemToObjectValue(objectValue);
    		llov.add(ov);
		}
    }
    private static ObjectValue elemToObjectValue(Element objectValue) {
		ObjectValue ov = new ObjectValue();
		if (objectValue.getAttributeValue("VALUE")!=null)
			ov.setValue(Integer.parseInt(objectValue.getAttributeValue("VALUE")));
		if (objectValue.getAttributeValue("VALUE_CLS")!=null)
			ov.setValueCls(Integer.parseInt(objectValue.getAttributeValue("VALUE_CLS")));
		if (objectValue.getAttributeValue("Q")!=null)
			ov.setQ(Integer.parseInt(objectValue.getAttributeValue("Q")));
		return ov;
    }
    
    private static Changes buildChanges(Element root) {
    	Changes changes = new Changes();
    	Element content = root.getChild("CONTENT");
        if (content!=null) {
        	Element changesElem = content.getChild("CHANGES");
	        if (changesElem!=null){
	            Iterator itr = changesElem.getChildren("OBJECT_CHANGED").iterator();
	            while( itr.hasNext() ){
	                Element item = (Element) itr.next();
	                ObjectChanged oi = buildObjectIndex(item);
	                changes.addObjectChanged(oi);
	            }
	        }
        }
        return changes;
    }
    private static ObjectChanged buildObjectIndex(Element root) {
		ObjectChanged oi = new ObjectChanged();
		if (root.getAttributeValue("CLS")!=null)//CLS se utiliza en replicador para enviar mail cuando no existia un rdn
			oi.clsname=root.getAttributeValue("CLS");
		if (root.getAttributeValue("OLD_IDO")!=null)
			oi.setOldIdo(Integer.parseInt(root.getAttributeValue("OLD_IDO")));
		oi.setNewIdo(Integer.parseInt(root.getAttributeValue("NEW_IDO")));
		Element newValue = root.getChild("NEW_VALUE");
		if (root.getAttributeValue("PROP")!=null) {
			oi.setProp(Integer.parseInt(root.getAttributeValue("PROP")));
			Element oldValue = root.getChild("OLD_VALUE");
			if (oldValue!=null) {
				Iterator itr = oldValue.getChildren().iterator();
				while (itr.hasNext()) {
					Element childOldValue = (Element)itr.next();
					if (childOldValue.getName().equals("OBJECT_VALUE"))
						oi.setOldValue(elemToObjectValue(childOldValue));
					else if (childOldValue.getName().equals("DATA_VALUE"))
						oi.setOldValue(elemToDataValue(childOldValue));
				}
			}
//			if (newValue!=null) {
			Iterator itr = newValue.getChildren().iterator();
			while (itr.hasNext()) {
				Element childNewValue = (Element)itr.next();
				if (childNewValue.getName().equals("OBJECT_VALUE"))
					oi.setNewValue(elemToObjectValue(childNewValue));
				else if (childNewValue.getName().equals("DATA_VALUE"))
					oi.setNewValue(elemToDataValue(childNewValue));
			}
		}
		return oi;
    }
    
    public static IndividualData buildIPropertyDef(Element root) {
    	IndividualData aipd = null;
    	Element facts = root.getChild("FACTS");
        if (facts != null) {
        	aipd = new IndividualData();
	    	Iterator it = facts.getChildren("FACT").iterator();
	    	while (it.hasNext()) {
	    		Element child = (Element)it.next();
	    		Element newFact = child.getChild("NEW_FACT");
	    		FactInstance fi = buildFactInstance(newFact);
	    		Element initialChild = child.getChild("INITIAL_FACT");
	    		if (initialChild!=null)
	    			fi.setInitialValues(buildFactInstance(initialChild));
	    		aipd.addIPropertyDef(fi);
	    	}
	    	it = facts.getChildren("RESERVATION").iterator();
	    	while (it.hasNext()) {
	    		Element child = (Element)it.next();
	    		int ido = Integer.parseInt(child.getAttributeValue("IDO"));
	    		int idProp = Integer.parseInt(child.getAttributeValue("IDPROP"));
	    		double reservation = Double.parseDouble(child.getAttributeValue("RESERVATION"));
	    		double available = Double.parseDouble(child.getAttributeValue("AVAILABLE"));
	    		Reservation reserv = new Reservation(ido, idProp, reservation, available);
	    		aipd.addReservation(reserv);
	    	}
        }
    	return aipd;
    }
    private static FactInstance buildFactInstance(Element child) {
		FactInstance fi = new FactInstance();
		if (child.getAttributeValue("NAME")!=null)
			fi.setCLASSNAME(child.getAttributeValue("NAME"));
		if (child.getAttributeValue("IDTO")!=null)
			fi.setIDTO(Integer.parseInt(child.getAttributeValue("IDTO")));
		if (child.getAttributeValue("IDO")!=null)
			fi.setIDO(Integer.parseInt(child.getAttributeValue("IDO")));
		if (child.getAttributeValue("PROP")!=null)
			fi.setPROP(Integer.parseInt(child.getAttributeValue("PROP")));
		/*Element value = child.getChild("VALUE");
		if (value!=null) {
			CDATA CData = (CDATA)value.getContent().get(0);
			System.out.println("MACADATAFACTORY");
			System.out.println(CData.getText());
			fi.setVALUE(CData.getText());
		}*/
		if (child.getText()!=null && child.getText().length()>0)
			fi.setVALUE(child.getText());
		if (child.getAttributeValue("VALUECLS")!=null)
			fi.setVALUECLS(Integer.parseInt(child.getAttributeValue("VALUECLS")));
		if (child.getAttributeValue("QMIN")!=null)
			fi.setQMIN(Double.parseDouble(child.getAttributeValue("QMIN")));
		if (child.getAttributeValue("QMAX")!=null)
			fi.setQMAX(Double.parseDouble(child.getAttributeValue("QMAX")));
		if (child.getAttributeValue("OP")!=null)
			fi.setOP(child.getAttributeValue("OP"));
		if (child.getAttributeValue("SYSTEM_VALUE")!=null)
			fi.setSystemValue(child.getAttributeValue("SYSTEM_VALUE"));
		if (child.getAttributeValue("APPLIED_SYSTEM_VALUE")!=null)
			fi.setAppliedSystemValue((Boolean.parseBoolean(child.getAttributeValue("APPLIED_SYSTEM_VALUE"))));
		fi.setIncremental(Boolean.parseBoolean(child.getAttributeValue("INCREMENTAL")));
		if (child.getAttributeValue("DESTINATION_SYSTEM")!=null)
			fi.setDestinationSystem(child.getAttributeValue("DESTINATION_SYSTEM"));
		if (child.getAttributeValue("RDN")!=null)
			fi.setRdn(child.getAttributeValue("RDN"));
		if (child.getAttributeValue("RDNVALUE")!=null)
			fi.setRdnValue(child.getAttributeValue("RDNVALUE"));
		//if (child.getAttributeValue("RDNVALUE")!=null)
		//	fi.setRdnValue(child.getAttributeValue("RDNVALUE"));
		fi.setExistia_BD(Boolean.parseBoolean(child.getAttributeValue("EXISTIA_BD")));
		fi.setOrder(Integer.parseInt(child.getAttributeValue("ORDER")));
    	return fi;
    }
    
	private static instance buildInstance(instance ins, Element instancias) {
        int ido = Integer.parseInt(instancias.getAttributeValue("ID_O"));
        int to = Integer.parseInt(instancias.getAttributeValue("ID_TO"));
        if( ins==null ) {
            ins = new instance(to, ido);
        	ins.setRdn(instancias.getAttributeValue("RDN"), false);
        }
        
        Iterator it = instancias.getChildren("SQ_VALUE").iterator();
        while (it.hasNext()) {
        	Element sqValue = (Element)it.next();
        	Iterator it2 = sqValue.getChildren().iterator();
            while (it2.hasNext()) {
            	Element value = (Element)it2.next();
            	Value v = elemToDataValue(value);
            	if (v==null)
                	v = elemToObjectValue(value);
            	ins.addValueSQ(sqValue.getAttributeValue("SELECT_QUERY"), v);
            }
        }
        it = instancias.getChildren("IDO_FILTER_X_IDO").iterator();
        while (it.hasNext()) {
        	Element idoFXIdo = (Element)it.next();
        	String idoFMap = idoFXIdo.getAttributeValue("IDO_FILTER");
            int idoMap = Integer.parseInt(idoFXIdo.getAttributeValue("IDO"));
        	ins.addIdoFilterXIdo(idoFMap, idoMap);
        }
        it = instancias.getChildren("PROPERTIES").iterator();
        while (it.hasNext()) {
        	Element properties = (Element)it.next();
        	ArrayList<Property> lp = new ArrayList<Property>();
        	Integer idoP = null;
    		if (properties.getAttributeValue("ID_O")!=null)
    			idoP = Integer.parseInt(properties.getAttributeValue("ID_O"));
        	Integer idtoP = null;
    		if (properties.getAttributeValue("ID_TO")!=null)
    			idtoP = Integer.parseInt(properties.getAttributeValue("ID_TO"));
	        Iterator it2 = properties.getChildren("PROPERTY").iterator();
	        while (it2.hasNext()) {
	        	Element property = (Element)it2.next();
	        	if (StringUtils.equals(property.getAttributeValue("DPROP"),"TRUE")) {
	        		DataProperty dp = new DataProperty();
	        		dp.setIdo(idoP);
	        		dp.setIdto(idtoP);
	        		elemtoProperty(dp, property);
	        		if (property.getAttributeValue("DATA_TYPE")!=null)
	        			dp.setDataType(Integer.parseInt(property.getAttributeValue("DATA_TYPE")));
	
	        		Element valueList = property.getChild("VALUES");
	        		if (valueList!=null) {
	                	LinkedList<Value> lldv = new LinkedList<Value>();
	                	elemListToDataValue(lldv, valueList);
	                	dp.setValues(lldv);
	        		}
	        		Element enumList = property.getChild("ENUM_LIST");
	        		if (enumList!=null) {
	                	LinkedList<DataValue> lldv = new LinkedList<DataValue>();
	                	elemListToDataValue2(lldv, enumList);
	                	dp.setEnumList(lldv);
	        		}
	        		Element excluList = property.getChild("EXCLU_LIST");
	        		if (excluList!=null) {
	                	LinkedList<DataValue> lldv = new LinkedList<DataValue>();
	                	elemListToDataValue2(lldv, excluList);
	                	dp.setExcluList(lldv);
	        		}
	        		lp.add(dp);
	        	} else if (StringUtils.equals(property.getAttributeValue("OPROP"),"TRUE")) {
	        		ObjectProperty op = new ObjectProperty();
	        		op.setIdo(idoP);
	        		op.setIdto(idtoP);
	        		elemtoProperty(op, property);
	        		Element quantityDetailList = property.getChild("QUANTITY_DETAIL_LIST");
	        		if (quantityDetailList!=null) {
	                	LinkedList<QuantityDetail> llqd = new LinkedList<QuantityDetail>();
	           			Iterator it3 = quantityDetailList.getChildren("QUANTITY_LIST").iterator();
	           			while (it3.hasNext()) {
	           				Element quantityDetail = (Element)it3.next();
	           				QuantityDetail qd = new QuantityDetail();
	               			if (quantityDetail.getAttributeValue("VALUE")!=null)
	               				qd.setValue(Integer.parseInt(quantityDetail.getAttributeValue("VALUE")));
	               			if (quantityDetail.getAttributeValue("VALUE_CLS")!=null)
	               				qd.setValueCls(Integer.parseInt(quantityDetail.getAttributeValue("VALUE_CLS")));
	               			if (quantityDetail.getAttributeValue("CARD_ESP_MIN")!=null)
	               				qd.setCardinalityEspecifyMin(Integer.parseInt(quantityDetail.getAttributeValue("CARD_ESP_MIN")));
	               			if (quantityDetail.getAttributeValue("CARD_ESP_MAX")!=null)
	               				qd.setCardinalityEspecifyMax(Integer.parseInt(quantityDetail.getAttributeValue("CARD_ESP_MAX")));
	               			llqd.add(qd);
	           			}
	        		}
	        		if (property.getAttributeValue("RANGO_LIST")!=null) {
	        			LinkedList<Integer> lli = new LinkedList<Integer>();
	        			String[] rl = property.getAttributeValue("RANGO_LIST").split(",");
	        			for (int i=0;i<rl.length;i++)
	        				lli.add(Integer.parseInt(rl[i]));
	        			op.setRangoList(lli);
	        		}
	        		
	        		Element valueList = property.getChild("VALUES");
	        		if (valueList!=null) {
	                	LinkedList<Value> lldv = new LinkedList<Value>();
	                	elemListToObjectValue(lldv, valueList);
	                	op.setValues(lldv);
	        		}
	        		Element enumList = property.getChild("ENUM_LIST");
	        		if (enumList!=null) {
	                	LinkedList<ObjectValue> lldv = new LinkedList<ObjectValue>();
	                	elemToObjectValue2(lldv, enumList);
	                	op.setEnumList(lldv);
	        		}
	        		Element excluList = property.getChild("EXCLU_LIST");
	        		if (excluList!=null) {
	                	LinkedList<ObjectValue> lldv = new LinkedList<ObjectValue>();
	                	elemToObjectValue2(lldv, excluList);
	                	op.setExcluList(lldv);
	        		}
	        		lp.add(op);
	        	}
	        }
    		if (idoP!=null)
    			ins.addProperties(idoP, lp);
        }
        return ins;
    }

    private static void buildThreadData(Element root, threadData act) {
        if (root.getAttributeValue("CURR_PARENT_TASK") != null) {
            act.setCurrParentTask(Integer.parseInt(root.getAttributeValue("CURR_PARENT_TASK")));
        }
        if (root.getAttributeValue("OLD_STATE") != null) {
            act.setOldState(Integer.parseInt(root.getAttributeValue("OLD_STATE")));
        }
    }

    private static void buildOwningData(Element root, owningAction act) {
        if (root.getAttributeValue("OW_LEVEL") != null) {
            act.setOwningLevel(Integer.parseInt(root.getAttributeValue("OW_LEVEL")));
        }
    }

    private static void buildLockData(Element root, lockContainer act) {
        if( root.getChild("LOCKS")!=null ){
            Iterator itr = root.getChild("LOCKS").getChildren("ITEM").iterator();
            while (itr.hasNext()) {
                Element item = (Element) itr.next();
                int idto = Integer.parseInt(item.getAttributeValue("ID_TO"));
                HashSet<Integer> hIdos = Auxiliar.stringToHashSetInteger(item.getAttributeValue("ID_OS"),",");
                act.addLockOrder(hIdos, idto);
            }
        }
        if( root.getChild("UNLOCKS")!=null ){
            Iterator itr = root.getChild("UNLOCKS").getChildren("ITEM").iterator();
            while (itr.hasNext()) {
                Element item = (Element) itr.next();
                int idto = Integer.parseInt(item.getAttributeValue("ID_TO"));
                HashSet<Integer> hIdos = Auxiliar.stringToHashSetInteger(item.getAttributeValue("ID_OS"),",");
                act.addUnLockOrder(hIdos, idto);
            }
        }
    }

    private static selectData buildSelectionData(Element root) {
        selectData sd= new selectData();
        if( root.getChild("CONTENT")!=null &&
            root.getChild("CONTENT").getChild("SELECTION")!=null &&
            root.getChild("CONTENT").getChild("SELECTION").hasChildren() ){
        	Element selection = root.getChild("CONTENT").getChild("SELECTION");
            Iterator itr = selection.getChildren("INSTANCE").iterator();
            while( itr.hasNext() ){
                Element item = (Element) itr.next();
                instance ins = null;
                if (item.getAttributeValue("CURR_PROCESS") != null)
                    ins = buildContextData(item, dynagent.common.knowledge.action.GET );

                ins = buildInstance( ins, item );
                sd.addInstance(ins);
            }
            Element eSelect = selection.getChild("SELECT");
            if (eSelect!=null) {
	        	ArrayList<SelectQuery> aSq = new ArrayList<SelectQuery>();
	        	Iterator it = eSelect.getChildren("SELECT_QUERY").iterator();
	        	while (it.hasNext()) {
	        		Element eSq = (Element)it.next();
	        	    SelectQuery sq = buildSelectQuery(eSq);
	        		aSq.add(sq);
	        	}
	        	sd.setSelect(aSq);
            }
        }
        return sd;
    }

    private static SelectQuery buildSelectQuery(Element eSq) {
    	Integer propFilter = null;
    	if (eSq.getAttributeValue("PROP_FILTER")!=null)
    		propFilter = Integer.parseInt(eSq.getAttributeValue("PROP_FILTER"));
	    SelectQuery sq = new SelectQuery(eSq.getAttributeValue("ID_OBJECT"), Integer.parseInt(eSq.getAttributeValue("ID_PROP")), propFilter, eSq.getAttributeValue("VALUE_FILTER"));
	    return sq;
    }
    
    private static List<List<String>> buildListData(Element root) {
        List<List<String>> rows = new ArrayList<List<String>>();
        if (root.getChild("CONTENT")!=null) {
        	Element content = root.getChild("CONTENT");
        	Iterator<Element> it = content.getChildren("R").iterator();
            while (it.hasNext()) {
                Element rowElem = (Element)it.next();
                List<String> columns = new ArrayList<String>();
                
            	Iterator<Element> it2 = rowElem.getChildren("C").iterator();
                while (it2.hasNext()) {
                    Element columnElem = (Element)it2.next();
                    CDATA cData = (CDATA)columnElem.getContent().get(0);
                    String value = null;
                    if (cData.getText().length()>0)
                    	value = cData.getText();
                    columns.add(value);
                }
                rows.add(columns);
            }
        }
        return rows;
    }
    
    private static HashMap<DomainProp,Double> buildResultReservation(Element root) {
    	HashMap<DomainProp,Double> hDomainProp = new HashMap<DomainProp,Double>();
        if (root.getChild("CONTENT")!=null) {
        	Element content = root.getChild("CONTENT");
        	Iterator<Element> it = content.getChildren("DOMAIN_PROP").iterator();
            while (it.hasNext()) {
            	Element domainPropElem = (Element)it.next();
            	int ido = Integer.parseInt(domainPropElem.getAttributeValue("IDO"));
            	int idto = Integer.parseInt(domainPropElem.getAttributeValue("IDTO"));
            	int prop = Integer.parseInt(domainPropElem.getAttributeValue("PROP"));
            	DomainProp dProp = new DomainProp(ido, idto, prop);
            	double reservation = Double.parseDouble(domainPropElem.getAttributeValue("RESERVATION"));
            	hDomainProp.put(dProp, reservation);
            }
        }
        return hDomainProp;
    }
    private static ArrayList<Reservation> preBuildReservation(Element root) {
    	ArrayList<Reservation> aReservation = null;
        if (root.getChild("CONTENT")!=null) {
        	Element content = root.getChild("CONTENT");
        	aReservation = buildReservation(content);
        } else
        	aReservation = new ArrayList<Reservation>();
        return aReservation;
    }
    public static ArrayList<Reservation> buildReservation(Element content) {
    	ArrayList<Reservation> aReservation = new ArrayList<Reservation>();
        Iterator<Element> it = content.getChildren("RESERVATION").iterator();
        while (it.hasNext()) {
        	Element reservationElem = (Element)it.next();
        	int ido = Integer.parseInt(reservationElem.getAttributeValue("IDO"));
        	int prop = Integer.parseInt(reservationElem.getAttributeValue("IDPROP"));
        	double amoungReserved = Double.parseDouble(reservationElem.getAttributeValue("RESERVATION"));
        	double available = Double.parseDouble(reservationElem.getAttributeValue("AVAILABLE"));
        	
    		Reservation reservation = new Reservation(ido, prop, amoungReserved, available);
    		aReservation.add(reservation);
        }
        return aReservation;
    }
    
    private static contextData buildContextData(Element data, int operation) {
        contextData cd = new contextData(Integer.parseInt(data.getAttributeValue("ID_TO")),
        								 Integer.parseInt(data.getAttributeValue("ID_O")),
                                         Integer.parseInt(data.getAttributeValue("ID_CONTEXT")),
                                         Integer.parseInt(data.getAttributeValue("ID_FILTER")),
                                         Integer.parseInt(data.getAttributeValue("CURR_TASK")));
        if (data.getAttributeValue("CURR_PROCESS") != null)
            cd.setCurrProcess(Integer.parseInt(data.getAttributeValue("CURR_PROCESS")));
        if (data.getAttributeValue("ID_DOM") != null)
            cd.setIdDom(Integer.parseInt(data.getAttributeValue("ID_DOM")));
        if (data.getAttributeValue("TASK_TYPE") != null)
            cd.setTaskType(Integer.parseInt(data.getAttributeValue("TASK_TYPE")));
        if (data.getAttributeValue("TASK_TRANSITION") != null)
            cd.setTransition(Integer.parseInt(data.getAttributeValue("TASK_TRANSITION")));
        if (data.getAttributeValue("TASK_STATE") != null)
            cd.setTaskState(Integer.parseInt(data.getAttributeValue("TASK_STATE")));
        return cd;
    }


}
