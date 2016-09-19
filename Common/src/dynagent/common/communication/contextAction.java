package dynagent.common.communication;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.jdom.Document;
import org.jdom.Element;

import dynagent.common.utils.Auxiliar;

/**
 * Esta clase se encarga del contexto del mensaje.
 * <br>Posee métodos para acceder o modificar el contexto, identificador del objeto, instancia, tipo
 * de acción, acceso, proceso actual, para saber si está bloqueado o está en un contexto de root.
 */

public class contextAction extends message {

    private int TO_ctx = 0;
    private int IDO_ctx = 0;
    private HashMap<Integer,HashSet<Integer>> aID_ctx = null;
    private IndividualData indData;
    private Document xmlData;
    private boolean lock = false;

    public contextAction(int orderType){
       setType(message.MSG_OBJECT_TRAN);
       setOrderType(orderType);
    }

    public contextAction(int bns, String user, Integer uRol, int uTask, int type, IndividualData id ){
        setType(message.MSG_OBJECT_TRAN);
        setOrderType(type);
        setIndividualData(id);
        setUserTask(uTask);
        setUser(user);
        setUserRol(uRol);
        setBusiness(bns);
    }
    
    public contextAction(int bns, String user, Integer uRol, int uTask, int type, Document xmlData ){
    	this(bns, user, uRol, uTask, type, (IndividualData)null);
    	setXmlData(xmlData);
    }

    public boolean isLock() {
        return lock;
    }

    public IndividualData getIndividualData() {
        return indData;
    }
    
    public int getIDO_ctx() {
        return IDO_ctx;
    }

    public HashMap<Integer,HashSet<Integer>> getAID_ctx() {
        return aID_ctx;
    }
    
    public int getTO_ctx() {
        return TO_ctx;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public void setIndividualData(IndividualData id) {
        this.indData = id;
    }
    public void setIDO_ctx(int IDO_ctx) {
        this.IDO_ctx = IDO_ctx;
    }
    public void setAID_ctx(HashMap<Integer,HashSet<Integer>> aID_ctx) {
        this.aID_ctx = aID_ctx;
    }
    
    public void setTO_ctx(int TO_ctx) {
        this.TO_ctx = TO_ctx;
    }

    void toElementHeader(Element root){
        super.toElementHeader(root);
        if( TO_ctx!=0 )
            root.setAttribute("ID_TO",String.valueOf(TO_ctx));
        if( IDO_ctx!=0)
            root.setAttribute("TABLE_ID",String.valueOf(IDO_ctx));
        if( aID_ctx!=null) {
        	Iterator<Integer> it = aID_ctx.keySet().iterator();
        	while (it.hasNext()) {
        		Integer idto = it.next();
        		HashSet<Integer> idos = aID_ctx.get(idto);
        		Element idtoIdos = new Element("IDTO_TABLEIDS");
        		idtoIdos.setAttribute("ID_TO",String.valueOf(idto));
        		idtoIdos.setAttribute("TABLE_IDS",Auxiliar.hashSetIntegerToString(idos, ","));
        		root.addContent(idtoIdos);
        	}
        }
        if( lock )
            root.setAttribute("LOCK","TRUE");
    }

    void toElementContent( Element root ){
        super.toElementContent(root);
        if( indData!=null ){
            Element content= root.getChild("CONTENT");
            if( content==null ){
                content = new Element("CONTENT");
                root.addContent(content);
            }
            Element data = indData.toElement();
            content.addContent(data);
        }else if(xmlData!=null){
        	Element content= root.getChild("CONTENT");
            if( content==null ){
                content = new Element("CONTENT");
                root.addContent(content);
            }
            content.addContent(xmlData.getRootElement());
        }
    }

	public Document getXmlData() {
		return xmlData;
	}

	public void setXmlData(Document xmlData) {
		this.xmlData = xmlData;
	}
}
