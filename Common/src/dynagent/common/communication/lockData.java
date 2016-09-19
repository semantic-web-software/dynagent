package dynagent.common.communication;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.jdom.Element;

import dynagent.common.utils.Auxiliar;

/**
 * Esta clase se encarga del bloqueo y desbloqueo de mensajes.
 * <br>Posee dos lista una para bloqueos y otra para los desbloqueos. Tiene métodos para añadir
 * al contenido del mensaje si es un elemento de LOCKS o no.
 */

class lockData implements Serializable{
	
    HashMap<Integer,HashSet<Integer>> lockList= new HashMap<Integer,HashSet<Integer>>();
    HashMap<Integer,HashSet<Integer>> unlockList= new HashMap<Integer,HashSet<Integer>>();
    
    public HashSet<Integer> getLock( int idto ) {
    	return lockList.get(idto);
    }
    public HashSet<Integer> getUnLock( int idto ) {
    	return unlockList.get(idto);
    }
    public void addLockOrder( int ido, int idto ){
		HashSet<Integer> hIdos = lockList.get(idto);
		if (hIdos==null) {
			hIdos = new HashSet<Integer>();
			lockList.put(idto,hIdos);
		}
		hIdos.add(ido);
    }
    
    public void addlockOrder( HashSet<Integer> lista, int idto ){
    	HashSet<Integer> hIdosLock = lockList.get(idto);
		if (hIdosLock==null) {
			hIdosLock = new HashSet<Integer>();
			lockList.put(idto,hIdosLock);
		}
		hIdosLock.addAll(lista);
    }

    public void addLockOrder( HashMap<Integer,HashSet<Integer>> lista ){
    	Iterator it = lista.keySet().iterator();
		while (it.hasNext()) {
			Integer idto = (Integer)it.next();
			HashSet<Integer> hIdos = lista.get(idto);
			
	    	HashSet<Integer> hIdosLock = lockList.get(idto);
			if (hIdosLock==null) {
				hIdosLock = new HashSet<Integer>();
				lockList.put(idto,hIdosLock);
			}
			hIdosLock.addAll(hIdos);
		}
    }
    
    public void addUnlockOrder( int ido, int idto ){
		HashSet<Integer> hIdos = unlockList.get(idto);
		if (hIdos==null) {
			hIdos = new HashSet<Integer>();
			unlockList.put(idto,hIdos);
		}
		hIdos.add(ido);
    }
    
    public void addUnlockOrder( HashSet<Integer> lista, int idto ){
    	HashSet<Integer> hIdosLock = unlockList.get(idto);
		if (hIdosLock==null) {
			hIdosLock = new HashSet<Integer>();
			unlockList.put(idto,hIdosLock);
		}
		hIdosLock.addAll(lista);
    }

    public void addUnlockOrder( HashMap<Integer,HashSet<Integer>> lista ){
    	Iterator it = lista.keySet().iterator();
		while (it.hasNext()) {
			Integer idto = (Integer)it.next();
			HashSet<Integer> hIdos = lista.get(idto);
			
	    	HashSet<Integer> hIdosLock = unlockList.get(idto);
			if (hIdosLock==null) {
				hIdosLock = new HashSet<Integer>();
				unlockList.put(idto,hIdosLock);
			}
			hIdosLock.addAll(hIdos);
		}
    }
    
    public Iterator getLockIterator(){
        return lockList.keySet().iterator();
    }
    public Iterator getUnLockIterator(){
        return unlockList.keySet().iterator();
    }

    void toElementHeader(Element root){
    }

    void toElementContent( Element root ){
        if(lockList.size()>0){
            Element lock= new Element("LOCKS");
            root.addContent(lock);

            Iterator it = lockList.keySet().iterator();
			while (it.hasNext()) {
				Integer idto = (Integer)it.next();
				HashSet<Integer> hIdos = lockList.get(idto);
				
                Element pp = new Element("ITEM");
                lock.addContent(pp);
                pp.setAttribute("ID_TO", String.valueOf(idto));
                pp.setAttribute("ID_OS", Auxiliar.hashSetIntegerToString(hIdos, ","));
			}
        }
        if(unlockList.size()>0){
            Element unlock= new Element("UNLOCKS");
            root.addContent(unlock);

            Iterator it = unlockList.keySet().iterator();
			while (it.hasNext()) {
				Integer idto = (Integer)it.next();
				HashSet<Integer> hIdos = unlockList.get(idto);
				
                Element pp = new Element("ITEM");
                unlock.addContent(pp);
                pp.setAttribute("ID_TO", String.valueOf(idto));
                pp.setAttribute("ID_OS", Auxiliar.hashSetIntegerToString(hIdos, ","));
			}
        }
    }
}
