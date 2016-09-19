package dynagent.common.communication;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.jdom.Element;


/**
 * Esta clase hereda de ({@link message}), y contiene una lista de mensajes.
 * <br>En la lista se guardan todos los mensajes que sean del tipo ({@link message#MSG_BLOCK}).
 * Asimismo, posee métodos para iterar sobre la lista y para trabajar con la cabecera y el contenido del mensaje.
 */

public class block extends message{
    ArrayList msgList=new ArrayList();

    public block(){
        super(message.MSG_BLOCK);
    }

    public void addMsg( message msg ){
        msgList.add(msg);
    }

    public Iterator getIterator(){
        return msgList.iterator();
    }

    public Object[] toArray(){
        return msgList.toArray();
    }

    void toElementHeader(Element root){
        super.toElementHeader(root);
        for( int i=0; i<msgList.size();i++){
            message msg=(message)msgList.get(i);
            root.addContent(msg.toElement());
        }
    }

    void toElementContent( Element root ){
        if( msgList.size()>0 ){
            Element content= new Element("CONTENT");
            root.addContent(content);
            for (int i = 0; i < msgList.size(); i++) {
                message msg = (message) msgList.get(i);
                content.addContent(msg.toElement());
            }
        }
    }
}
