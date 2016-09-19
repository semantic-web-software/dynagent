package dynagent.common.communication;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.jdom.Element;

/**
 * Esta clase se encarga de los mensajes de pooling, dando la orden de bloqueo o desbloqueo.
 */

public class pooling extends message implements lockContainer{
    lockData data= new lockData();
    public pooling(){
        super(message.MSG_POOLING);
    }
    public HashSet<Integer> getLock( int idto ) {
    	return data.getLock(idto);
    }
    public HashSet<Integer> getUnLock( int idto ) {
    	return data.getUnLock(idto);
    }
    public void addLockOrder( int ido, int idto ){
        data.addLockOrder(ido, idto);
    }
    public void addLockOrder( HashSet<Integer> lista, int idto ){
        data.addlockOrder(lista, idto);
    }
    public void addLockOrder( HashMap<Integer,HashSet<Integer>> lista ){
        data.addLockOrder(lista);
    }
    public void addUnlockOrder( int ido, int idto ){
        data.addUnlockOrder(ido, idto);
    }
    public void addUnLockOrder( HashSet<Integer> lista, int idto ){
        data.addUnlockOrder(lista, idto);
    }
    public void addUnlockOrder( HashMap<Integer,HashSet<Integer>> lista ){
        data.addUnlockOrder(lista);
    }
    public Iterator getLockIterator(){
        return data.getLockIterator();
    }
    public Iterator getUnLockIterator(){
        return data.getUnLockIterator();
    }
    void toElementHeader(Element root){
        super.toElementHeader(root);
    }
    void toElementContent( Element root ){
        super.toElementContent(root);
        data.toElementContent(root);
    }
}
