/***
 * IBusinessConnection.java
 * @author Ildefonso Montero Perez - monteroperez@us.es
 * @description It represents a bridge to dynagent.ejb.businessConnection to use the poolDB class
 */

package dynagent.server.database;

public interface IBusinessConnection { // extends dynagent.ejb.businessConnection{
	 
	 /*     Es necesario introducir de alguna forma 
	 * 		la clase poolDB ya que es la que nos facilita el 
	 *      puente a la base de datos dynagent en SQLServer
	 *      asi reutilizamos lo existente. 
	 *      Hay que modificar esta clase para que tire de ficheros
	 *      de tipo properties ya que tiene hardcodeado los
	 *      parametros de conexion. 						
	 */
}
