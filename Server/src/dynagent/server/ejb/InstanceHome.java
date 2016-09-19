package dynagent.server.ejb;
/*
 *
 * Copyright 2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

import java.rmi.RemoteException;
import javax.ejb.*;


public interface InstanceHome extends EJBHome {

    public Instance create()
        throws RemoteException, CreateException;
    
}
