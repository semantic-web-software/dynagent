package dynagent.ejbengine.ejb;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

import javax.ejb.EJBObject;

import org.jdom.Document;

import dynagent.common.basicobjects.License;
import dynagent.server.dbmap.DataBaseMap;

public interface ServerEngine extends EJBObject {
	
	/**
	 * Manda la orden de hacer un run al motor.
	 */
	public void save() throws RemoteException;

	/**
	 * Muestra una p�gina con los detalles del objeto indicado.
	 * 
	 * @param operation
	 *            Tipo vista que se quiere realizar.
	 * @param idto
	 *            Identificador de la clase del objeto a buscar.
	 * @param tableId
	 *            Identificador del objeto de la clase que queremos buscar.
	 * @param className
	 *            Nombre de clase que casa con el xslt de transformaci�n
	 * @return Cadena con la p�gina HTML a mostrar al usuario.
	 * @throws RemoteException
	 *             Si se produce alg�n error durante el procesamiento de la
	 *             petici�n.
	 */
	public Document view(Integer idto, Integer tableId, String className) throws RemoteException;
	
	/**
	 * A�ade informaci�n al motor.
	 * 
	 * @param operation
	 *            Tipo de operaci�n solicitada por el usuario.
	 * @param queryParams
	 *            Par�metros enviados por el usuario para ser a�adidos al motor.
	 * @throws RemoteException
	 *             Si se produce alg�n error durante el procesamiento de la
	 *             petici�n.
	 */
	public void addData(Integer idParent,Integer idPropParent, int id, Map<String, String> queryParams) throws RemoteException;

	/**
	 * Inicia lo necesario para poder trabajar en el motor
	 */
	public void initObject(String className) throws RemoteException;
	
	/**
	 * Cancela todo lo creado en el motor
	 */
	public void cancel() throws RemoteException;
	
	public Integer getMainIdo() throws RemoteException;
	
	public Integer getIdo(String className, String rdn, boolean caseInsensitive) throws RemoteException;
	
	public Integer newIdo(String className,Map<String, String> propertiesMap) throws RemoteException;
	
	public ArrayList<Integer> getIdos(String className, Map<String,Object> mapPropertyValue, boolean caseInsensitive) throws RemoteException;
	
	public DataBaseMap getDataBaseMap() throws RemoteException;
	
	public String getUser() throws RemoteException;
	
	public String getError() throws RemoteException;
	
	public void setError(String error) throws RemoteException;
	
	public ArrayList<String> getData(int ido,String className,String propName) throws RemoteException;
	
	public void loadObject(int ido,String className) throws RemoteException;
	
	public License getLicense() throws RemoteException;
}