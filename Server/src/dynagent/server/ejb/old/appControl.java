/*
 * appControl.java
 *
 * Created on 4 de noviembre de 2002, 9:37
 */

/**
 *
 * @author  A_GONZALEZ
 */
package dynagent.ejb;

import org.jdom.Element;
import javax.swing.JDialog;
import dynagent.exceptions.SystemException;
import dynagent.exceptions.RemoteSystemException;
import dynagent.exceptions.CommunicationException;;

public interface appControl {

    /** Creates a new instance of appControl */

    //No debe entrar en colision con los codigos de operaciones de access
    static int CANCEL= 200;
    static int PROCESAR_TRAN_REQ= 201;
    static int FILTRADO_INTERNO= 202;
    static int SET_INTERNAL_FILTER= 203;
    static int TRANSFORMATION= 204;
    static int OBJECT_TRANSITION= 205;
    static int CASCADING_UPDATE=206;
    //ESTADOS
    static int REPOSE=300;
    static int WAITING=301;

    public Object userEvent(Object target, Object subtype, int event)
            throws SystemException,RemoteSystemException,CommunicationException;
    public void setMainDialog(JDialog dlg);
    public JDialog getControlDialog();
    /*public void addLockedInstance( int ido);
    public void removeLockedInstance( int ido);
    public void unlockObjects() throws SystemException,RemoteSystemException,CommunicationException;*/
    public void queryDocument( String name, String type, Element detalle );
}


