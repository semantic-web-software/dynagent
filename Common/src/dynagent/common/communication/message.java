package dynagent.common.communication;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom.CDATA;
import org.jdom.Element;

import dynagent.common.knowledge.selectData;
import dynagent.common.properties.DomainProp;
import dynagent.common.utils.jdomParser;

/**
 * Esta clase se encarga de definir los mensajes.
 * <br>Contiene las siguientes tablas Hash: ({{@link #m_properties}}) y ({{@link #m_directAttributes}}), 
 * a las que se puede acceder o modificar mediante una clave, dependiendo del tipo de propiedad o atributo. 
 * Posee variables para especificar el tipo de mensajes, tipo de tareas, tipo de datos, ...
 * <br>Los mensajes estan formados por una cabecera y un contenido. En la cabecera se encuentra información 
 * sobre la identificación del mensaje, versión, tiempo de sesion, codigo de resultado,  tipo de mensaje, 
 * usuario, rol del usuario, tipo de datos,...
 */

public class message extends Object implements Serializable{

    //MSG TYPES
    public final static int MSG_OBJECT_TRAN=1;
    public final static int MSG_FLOW=2;
    public final static int MSG_OWNING=3;
    public final static int MSG_CONFIRM=4;
    public final static int MSG_QUERY=5;
    public final static int MSG_EXE_TRAN_ACTION=6;
    public final static int MSG_SELECTION=7;
    public final static int MSG_LOGIN=8;
    public final static int MSG_REPLICA=9;
    //public final static int MSG_LIVE=9;
    public final static int MSG_COMMIT=10;
    public final static int MSG_POOLING=11;
    public final static int MSG_ERROR=12;
    public final static int MSG_LOCK=13;//acepta ordenar lock y unlock
    public final static int MSG_UNLOCK=14;
    public final static int MSG_BLOCK=15;
    public final static int MSG_LOG_ERROR=16;    //registrar error en servidor
    public final static int MSG_CHANGES=17;
    public final static int MSG_REPORT=18;
    public final static int MSG_DISCONNECTION=19;
    public final static int MSG_RULER=20;
    public final static int MSG_LOG_ERROR_LOGIN=21;    //registrar error de logueo en servidor
    public final static int MSG_PREPRINT=22;
    public final static int MSG_GETRDN=23;
    public final static int MSG_RESET_LOCKS=24;
    public final static int MSG_DESCRIPTION=25;
    public final static int MSG_MIGRATION=26;
    public final static int MSG_QUERYRULES=27;
    public final static int MSG_REPORTS_CLASIFICATOR=28;
    public final static int MSG_RESERVE=29;
    public final static int MSG_CHANGE_MODE=30;
    public static final int MSG_DATE_NOW = 31;
    public final static int MSG_LOGIN_OVERWRITTEN=32;
    public final static int MSG_GETRDNLIST=33;
    public static final int MSG_UPDATE_LICENSE = 34;
    public static final int MSG_CODE_LICENSE = 35;
    public final static int MSG_UPDATE=36;
    
    //ORDER TYPES
    public final static int FLOW_NEW_PRO=1;
    public final static int FLOW_TRANSITION=2;
    public final static int FLOW_END_PRO=3;
    public final static int FLOW_NEW_TASK=4;
    public final static int FLOW_OWNING_NEW_TASK=5;
    public final static int FLOW_OWNING=6;
    public final static int FLOW_GROW=7;
    public final static int FLOW_DECREASE=8;

    public final static int ACTION_GET=9;
    public final static int ACTION_MODIFY=10;
    public final static int ACTION_GETRDN=11;
    public final static int ACTION_REPORT=12;
    public final static int ACTION_DESCRIPTION_CLASS=13;
    public final static int ACTION_DESCRIPTION_PROPERTY=14;
    public final static int ACTION_DESCRIPTION_PROPERTIES=15;
    public final static int ACTION_GET_JRXML = 16; // MODIFIED Añadida acción para pedir un archivo jrxml
    public final static int ACTION_NEW_RESERVE = 17;
    public final static int ACTION_DEL_RESERVE = 18;
    public final static int ACTION_RESULT_RESERVE = 19;
    public final static int ACTION_DESCRIPTION_INDIVIDUAL=20;
    public final static int ACTION_DESCRIPTION_INDIVIDUALS=21;
    public final static int ACTION_GETRDNLIST=22;
    
    //DATA TYPES
    public final static int DATA_META=1;
    public final static int DATA_THREAD=2;
    public final static int DATA_INSTANCE=3;
    public final static int DATA_OWNING=4;
    public final static int DATA_TASK_STATE=5;
    //public final static int DATA_TASK_PREASIGNED=6;
    //public final static int DATA_TASK_ASIGNED=7;
    public final static int DATA_INDIVIDUAL=6;
    public final static int DATA_INDIVIDUAL_CLASS=7;
    public final static int DATA_INDIVIDUAL_CLASS_SPECIALIZED=8;
    public static final int DATA_REPORT_JRXML = 9; // MODIFIED Añadido tipo de datos para solicitar un archivo jrxml


    //RESULT_CODE
    public final static int SUCCESSFULL=1;
    public static final int NEW_INDEX=2;
    public static final int OWNING_CHANGED=3;
    
    public final static int ERROR_DATA=100;
    public final static int ERROR_CONCURRENT=101;
    public final static int ERROR_DISCONNECTED=102;
    public final static int ERROR_VERSION=103;
    public static final int ERROR_TIME_OUT = 104;
    public static final int ERROR_LOCKED = 105;
    public static final int ERROR_PERMISSION = 106;
    public static final int ERROR_SYSTEM = 107;
    public final static int ERROR_DATA_REMOTE=108;
    public final static int ERROR_LOGIN=109;
    public final static int ERROR_LICENSE_USER=110;
    public final static int ERROR_LICENSE_DATE=111;
    public final static int ERROR_LICENSE_MISSING=112;
    public final static int ERROR_REMOTE_IP=113;
    public final static int ERROR_RULER=114;
    public final static int ERROR_SESSION=115;
    public final static int ERROR_USER_ALREADY_LOGGED=116;
    public final static int ERROR_LICENSE_CORRUPT=117;
    public final static int ERROR_LICENSE_CODE_UNAVAILABLE=118;
	public static final int MSG_SEND_EMAIL = 119;
	public static final int MSG_SEND_SERVER_LOG_EMAIL = 120;
	

    //VARIABLES
    private String user = null;
    private String userRols = null;
    private int business=0;
    private String mode = null;
    private String subscription = null;
    private Integer userTask=null;
    private Integer userRol=null;
    private int type = 0;
    private int resultCode = SUCCESSFULL;
    private String msguid = null;
    private int orderType = 0;

    Object content;
    private String version =null;
    private String clientSession = null;
    private Integer windowSession = null;
    private HashMap m_properties= new HashMap();
    private HashMap m_directAttributes= new HashMap();
    private int dataType = 0;

    public boolean equals(Object obB){
        if( !(obB instanceof message) )
            return false;
        message mB=(message)obB;
        return  StringUtils.equals(user,mB.getUser()) &&
                type==mB.getType() &&
                orderType==mB.getOrderType();
    }

    public void addPropertie( int key, int value ){
        m_properties.put(new Integer(key), String.valueOf(value));
    }
    
    public void addPropertie(int key, long value) {
    	m_properties.put(new Integer(key), String.valueOf(value));
    }

    public void addPropertie( int key, boolean value ){
        addPropertie( key, (value ? 1:0));
    }

    public HashMap getM_properties() {
		return m_properties;
	}

	public void addAllDirectAttribute( int key, ArrayList value ){
        Integer iKey= new Integer(key);
        ArrayList res=(ArrayList)m_directAttributes.get(iKey);
        if( res==null )
            setDirectAttribute( key, value );
        else
            res.addAll(value);
    }
    public void addAllDirectAttribute( String key, ArrayList value ){
        Integer iKey= new Integer(key);
        ArrayList res=(ArrayList)m_directAttributes.get(iKey);
        if( res==null )
            setDirectAttribute( key, value );
        else
            res.addAll(value);
    }
    
    public void setDirectAttribute( HashMap m_directAttributes ){
        this.m_directAttributes = m_directAttributes;
    }
    public void setDirectAttribute( int key, ArrayList value ){
        m_directAttributes.put(new Integer(key), value);
    }
    public void setDirectAttribute( String key, ArrayList value ){
        m_directAttributes.put(key, value);
    }
    public HashMap getDirectAttribute(){
        return m_directAttributes;
    }

    public void addAllPropertie( int key, ArrayList value ){
        Integer iKey= new Integer(key);
        ArrayList res=(ArrayList)m_properties.get(iKey);
        if( res==null )
            setPropertie( key, value );
        else
            res.addAll(value);
    }

    public void setPropertie( int key, ArrayList value ){
        m_properties.put(new Integer(key), value);
    }

    public void addPropertie( int key, String value ) {
        m_properties.put(new Integer(key), value);
    }

    public Iterator getMultivaluePropertie( int key ) throws NoSuchFieldException{
        ArrayList res=(ArrayList)m_properties.get(new Integer(key));
        if( res==null || !(res instanceof ArrayList)) throw new NoSuchFieldException();
        else return res.iterator();
    }

    public Iterator getMultivalueDirectAttribute( int key ) throws NoSuchFieldException{
        ArrayList res=(ArrayList)m_directAttributes.get(new Integer(key));
        if( res==null || !(res instanceof ArrayList)) throw new NoSuchFieldException();
        else return res.iterator();
    }
    public Iterator getMultivalueDirectAttribute( String key ) throws NoSuchFieldException{
        ArrayList res=(ArrayList)m_directAttributes.get(key);
        if( res==null || !(res instanceof ArrayList)) throw new NoSuchFieldException();
        else return res.iterator();
    }
    
    public int getIntPropertie(int key) throws NoSuchFieldException{
        String res=(String)m_properties.get(new Integer(key));
        if( res==null ) throw new NoSuchFieldException();
        else return Integer.parseInt(res);
    }
    
    public long getLongPropertie(int key) throws NoSuchFieldException {
    	String res=(String)m_properties.get(new Integer(key));
        if( res==null ) throw new NoSuchFieldException();
        else return Long.parseLong(res);
	}
    
    public boolean getBoolPropertie( int key ){
        Object res=m_properties.get(new Integer(key));
        if( res==null ) return false;
        try{
            return getIntPropertie(key) == 1 ? true : false;
        }catch(NoSuchFieldException e){
            e.printStackTrace();
        }
        return false;
    }

    public String getStrPropertie(int key) throws NoSuchFieldException{
        Integer iKey=new Integer(key);
        if( !hasPropertie(key) )
            throw new NoSuchFieldException();
        return (String)m_properties.get(iKey);
    }
    
    public ArrayList getListPropertie(int key) throws NoSuchFieldException{
    	if (! hasPropertie(key)){
    		throw new NoSuchFieldException();
    	}
    	return (ArrayList)m_properties.get(key);
    }

    public boolean hasPropertie( int key ){
        Integer iKey=new Integer(key);
        return m_properties.containsKey(iKey);
    }

    public void addDirectAttribute( int key, int value ){
        m_directAttributes.put(new Integer(key), String.valueOf(value));
    }

    public void addDirectAttribute( int key, String value ){
        m_directAttributes.put(new Integer(key), value);
    }

    public void addDirectAttribute( String key, String value ){
        m_directAttributes.put(key, value);
    }
    
    public int getIntDirectAttribute(int key) throws NoSuchFieldException{
        String res=(String)m_directAttributes.get(new Integer(key));
        if( res==null )  throw new NoSuchFieldException();
        else return Integer.parseInt(res);
    }

    public String getStrDirectAttribute(int key) throws NoSuchFieldException{
        Integer iKey=new Integer(key);
        if( !m_directAttributes.containsKey(iKey) )
            throw new NoSuchFieldException();
        return (String)m_directAttributes.get(iKey);
    }

    public int getIntDirectAttribute(String key) throws NoSuchFieldException{
        String res=(String)m_directAttributes.get(key);
        if( res==null )  throw new NoSuchFieldException();
        else return Integer.parseInt(res);
    }

    public String getStrDirectAttribute(String key) throws NoSuchFieldException{
        if( !m_directAttributes.containsKey(key) )
            throw new NoSuchFieldException();
        return (String)m_directAttributes.get(key);
    }

    public boolean getSuccess(){
        return resultCode< 100;
    }
    public message(){;}

    public message(int type){
        setType(type);
    }

    public Object clone(){
        message msg= new message();;
        cloneData(msg);
        return msg;
    }

    void cloneData(message msg){
        msg.setUser(user);
        msg.setBusiness(business);
        msg.setMode(mode);
        msg.setSubscription(subscription);
        msg.setUserTask(userTask);
        msg.setUserRol(userRol);
        msg.setType(type);
        msg.setResultCode(resultCode);
        msg.setMsguid(msguid);
        msg.setOrderType(orderType);
        msg.setUserRols(userRols);
        msg.setContent(getContent());
    }

    public void setSubscription(String subscription) {
		this.subscription=subscription;
	}
    
    public String getSubscription(){
    	return subscription;
    }

	public void setContent(Object obj){
        content=obj;
    }

    public Object getContent(){
        return content;
    }

    public String getUser() {
        return user;
    }

    public int getBusiness() {
        return business;
    }

    public String getMode() {
        return mode;
    }
    
    public Integer getUserTask() {
        return userTask;
    }
    
    public Integer getUserRol() {
        return userRol;
    }

    public int getType() {
        return type;
    }

    public int getResultCode() {
        return resultCode;
    }

    public String getMsguid() {
        return msguid;
    }

    public int getOrderType() {
        return orderType;
    }

    public String getVersion() {
        return version;
    }

    public String getClientSession() {
        return clientSession;
    }

    public Integer getWindowSession() {
        return windowSession;
    }

    public int getDataType() {
        return dataType;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
        if( !getSuccess() && getType()==0 )
            setType(MSG_ERROR);
    }

    public void setMsguid(String msguid) {
        this.msguid = msguid;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setBusiness(int business) {
        this.business = business;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setUserTask(Integer userTask) {
        this.userTask = userTask;
    }
    
    public void setUserRol(Integer userRol) {
        this.userRol = userRol;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setClientSession(String clientSession) {
        this.clientSession = clientSession;
    }

    public void setWindowSession(Integer windowSession) {
        this.windowSession = windowSession;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public String toString(){
        Element msg= toElement();
        try{
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+jdomParser.returnXML(msg);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public String toHeaderString(){
        Element msg= toElement(true);
        try{
            return jdomParser.returnXML(msg);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    Element toElement(boolean justHeader){
        if(!justHeader)
            return toElement();
        else{
            Element root= new Element("MSG");
            toElementHeader(root);
            return root;
        }
    }

    Element toElement(){
        Element root= new Element("MSG");
        toElementHeader(root);
        toElementContent(root);
        return root;
    }

    void toElementHeader(Element root){
        if(business!=0)
            root.setAttribute("BNS",String.valueOf(business));
        if(userTask!=null)
            root.setAttribute("UTASK",String.valueOf(userTask));
        if(msguid!=null)
            root.setAttribute("MSGUID",msguid);
        if(version!=null)
            root.setAttribute("VERSION",version);
        if(clientSession!=null)
            root.setAttribute("CNXID",clientSession);
        if(windowSession!=null)
            root.setAttribute("WINID",String.valueOf(windowSession));
        if(resultCode!=0)
            root.setAttribute("RESULT_CODE",String.valueOf(resultCode));
        if(type!=0)
            root.setAttribute("MSG_TYPE",String.valueOf(type));
        if(orderType!=0)
            root.setAttribute("ORDER_TYPE",String.valueOf(orderType));
        if(user!=null)
            root.setAttribute("USER",user);
        if(userRols!=null)
            root.setAttribute("USERROLS",userRols);
        if(mode!=null)
            root.setAttribute("MODE",mode);
        if(subscription!=null)
            root.setAttribute("SUBSCRIPTION",subscription);
        if( userRol!=null && userRol!=0 )
            root.setAttribute("USER_ROL",String.valueOf(userRol));
        if( dataType!=0 )
            root.setAttribute("DATA_TYPE",String.valueOf(dataType));
        if (!m_properties.isEmpty() || !m_directAttributes.isEmpty()) {
	        Element pps= new Element("PROPERTIES");
	        root.addContent(pps);
	        if (!m_properties.isEmpty()) {
		        Iterator itr= m_properties.keySet().iterator();
		        while(itr.hasNext()){
		            Integer key=(Integer)itr.next();
		            Object val=m_properties.get(key);
		            Element pp= new Element("PROP");
		            pps.addContent(pp);
		            pp.setAttribute("KEY",key.toString());
		            if( val instanceof ArrayList){
		            	ArrayList list = (ArrayList) val;
		                for( Object obj : list){
		                    Element item= new Element("ITEM");
		                    pp.addContent(item);
		                    //item.setAttribute("VALUE",((ArrayList)val).get(i).toString());
		                    item.setText(obj.toString());
		                }
		            }else{
		                pp.setAttribute("VALUE",val.toString());
		            	//pp.setText(val.toString());
		            }
		        }
	        }
	        if (!m_directAttributes.isEmpty()) {
	        	Iterator itr= m_directAttributes.keySet().iterator();
		        while(itr.hasNext()){
		        	Object key = (Object)itr.next();
		        	if (key instanceof Integer) {
		        		Integer keyInt = (Integer)key;
			            Object val=m_directAttributes.get(keyInt);
			            Element pp= new Element("AT");
			            pps.addContent(pp);
			            pp.setAttribute("KEY",keyInt.toString());
			            pp.setAttribute("VALUE",val.toString());
					} else {
						String keyStr = (String)key;
			            Object val=m_directAttributes.get(keyStr);
			            Element pp= new Element("AT");
			            pps.addContent(pp);
			            pp.setAttribute("KEY",keyStr);
			            pp.setAttribute("VALUE",val.toString());
					}
		        }
	        }
        }
    }

    private Element getChildContentElement(Element root) {
    	Element eC=root.getChild("CONTENT");
        if(eC==null){
            eC=new Element("CONTENT");
            root.addContent(eC);
        }
        return eC;
    }
    
    void toElementContent( Element root ){
//        if( getContent() instanceof selectData ){
//            Element eC = getChildContentElement(root);
//            eC.addContent(((selectData)getContent()).toElement());
//        } else 
        if( getContent() instanceof Changes ){
            Element eC = getChildContentElement(root);
            eC.addContent(((Changes)getContent()).toElement());
        } else if( getContent() instanceof Element ){
            Element eC = getChildContentElement(root);
            if(((Element)getContent()).getParent()!=null)
            	((Element)getContent()).detach();
            eC.addContent((Element)getContent());
        } else if( getType()==message.MSG_RESERVE){
        	if (getOrderType()==message.ACTION_RESULT_RESERVE) {
                Element eC = getChildContentElement(root);
        		HashMap<DomainProp,Double> hDomainProp = (HashMap<DomainProp,Double>)getContent();
	            Iterator<DomainProp> it = hDomainProp.keySet().iterator();
	            while(it.hasNext()) {
	            	DomainProp domProp = it.next();
	            	Double reservation = hDomainProp.get(domProp);
	            	Element domPropElem = domProp.toElement();
	            	domPropElem.setAttribute("RESERVATION", String.valueOf(reservation));
	            	eC.addContent(domPropElem);
	            }
        	} else {
                Element eC = getChildContentElement(root);
	            ArrayList<Reservation> aReservation = (ArrayList<Reservation>)getContent();
	            Iterator<Reservation> it = aReservation.iterator();
	            while(it.hasNext()) {
	            	Reservation reservation = it.next();
	            	eC.addContent(reservation.toElement());
	            }
        	}
        } else if( getType()==message.MSG_QUERYRULES /*&& getContent() instanceof List*/ ){
            Element eC = getChildContentElement(root);
            List<List<String>> rows = (List<List<String>>)getContent();
            Iterator it = rows.iterator();
            while(it.hasNext()) {
            	Element row = new Element("R");
            	eC.addContent(row);
            	List<String> columns = (List<String>)it.next();
                Iterator<String> it2 = columns.iterator();
                while(it2.hasNext()) {
                	Element column = new Element("C");
                	row.addContent(column);
                	String value = it2.next();
                	column.addContent(new CDATA(value));
                }
            }
        }
    }
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    public void addPropertyChangeListener(PropertyChangeListener pcl){
        pcs.addPropertyChangeListener(pcl);
    }
    public void removePropertyChangeListener(PropertyChangeListener pcl){
        pcs.removePropertyChangeListener(pcl);
    }

	public void setUserRols(String userRols) {
		this.userRols=userRols;
	}

	public String getUserRols() {
		return userRols;
	}

}

