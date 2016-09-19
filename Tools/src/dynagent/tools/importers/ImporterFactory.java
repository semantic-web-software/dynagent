package dynagent.tools.importers;

import dynagent.server.gestorsDB.GestorsDBConstants;

/**
 * Clase que se encarga de proporcionar el objeto IImporter adecuado para cada gestor de base de datos.
 */
public abstract class ImporterFactory {

	/**
	 * Crea el importador específico para el gestor indicado.
	 * 
	 * @param gestor
	 *            Gestor de Base de datos con el que queremos trabajar.
	 * @return Devuelve el importador correcto para el gestor indicado o
	 *         <code>null</code> si no se conoce el importador para el gestor
	 *         dado.
	 */
	public static IImporter	createImporter(String gestor,String pwd){
		assert gestor != null : "No se ha indicado un gestor";
		IImporter result = null;
		if (gestor.equals(GestorsDBConstants.SQLServer)){
			result = new SQLServerImporter();
		}else if (gestor.equals(GestorsDBConstants.mySQL)){
			result = new MySQLImporter();
		}else if (gestor.equals(GestorsDBConstants.postgreSQL)){
			result = new PostgreSQLImporter(pwd);
		}else{
			System.err.println("No se tiene un comportamiento asociado para el gestor: " + gestor);
		}
		return result;
	}

}
