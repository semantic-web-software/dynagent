package dynagent.tools.parsers.uni.auxiliar;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.NamingException;


/**
 * 
 * @author alvarez
 * En esta clase obtenemos todos los datos útiles de la base de datos
 * en forma de listas.
 */

public class ProcesamientoBD {
	
	/**
	 * tablaHerenciasBD contiene la tabla Herencias de la BD
	 * tablaClasesBD contiene la tabla Clases de la BD
	 * tablaPropertiesBD contiene la tabla Properties de la BD
	 * 
	 */
	
	public ArrayList tablaHerenciasBD = null;
	public ArrayList tablaClasesBD = null;
	public ArrayList tablaPropertiesBD = null;
	
	
	/**
	 *  Constructor de la clase
	 * @throws NamingException 
	 * @throws SQLException 
	 *
	 */
	
	public ProcesamientoBD() throws SQLException, NamingException{
		THerenciasAdvanced tH = new THerenciasAdvanced();
		tablaHerenciasBD = tH.getHerenciasDB();
		TClaseAdvanced tC = new TClaseAdvanced();
		tablaClasesBD = tC.getClasesDB();
		TPropertiesAdvanced tP = new TPropertiesAdvanced();
		tablaPropertiesBD = tP.getPropertiesDB();
	}
	

}
