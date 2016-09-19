package dynagent.common.communication;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;


/**
 * Interfaz implementada por ({@link lock}}) y ({@link pooling}}) para el bloqueo y desbloqueo.
 */

public interface lockContainer {
    public HashSet<Integer> getLock( int idto );
    public HashSet<Integer> getUnLock( int idto );
    public void addLockOrder( int ido, int idto );
    public void addLockOrder( HashSet<Integer> lista, int idto );
    public void addLockOrder( HashMap<Integer,HashSet<Integer>> lista );
    public void addUnlockOrder( int ido, int idto );
    public void addUnLockOrder( HashSet<Integer> lista, int idto );
    public void addUnlockOrder( HashMap<Integer,HashSet<Integer>> lista );
    public Iterator getLockIterator();
    public Iterator getUnLockIterator();
}
