package dynagent.tools.setup.ddbb;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.naming.NamingException;

import dynagent.server.dbmap.DBQueries;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;

public class ScriptsDB {

	/**
	 * @param args
	 * 
	 * 	Ejemplo:
	 	-initDB 10
		-endDB 21
		-file E:\script.sql
		-gestor postgreSQL
	 * @throws IOException 
	 * @throws NamingException 
	 * @throws SQLException 
		
	 */
	public static void main(String[] args){
		int initDB=0;//Numero de inicio de base de datos para la ejecucion del script
		int endDB=0;//Numero del fin de base de datos para la ejecucion del script
		String scriptFile=null;//Nombre del fichero que se utilizara como fuente de sentencias sql
		String gestor=null;//Gestor de base de datos:postgreSQL, mySQL...
		Integer port=5432;
		String pwd=null;
		
		String id="";
		for(int i=0;i<args.length;i++){
			//System.out.println("\n debug Menu param="+args[i]);
			if(args[i].startsWith("-"))
				id=args[i];
			else{
				if(id.equalsIgnoreCase("-initdb")){
					initDB=Integer.parseInt(args[i]);
				}else if(id.equalsIgnoreCase("-enddb")){
					endDB=Integer.parseInt(args[i]);
				}else if(id.equalsIgnoreCase("-file")){
					scriptFile=args[i];
				}else if(id.equalsIgnoreCase("-gestor")){
					gestor=args[i];
				}else if(id.equalsIgnoreCase("-port")){
					port=Integer.parseInt(args[i]);
				}else if(id.equalsIgnoreCase("-pwd")){
					pwd=args[i];
				}
			}
		}
		
		File file=new File(scriptFile);
		try{
			for(int i=initDB;i<=endDB;i++){
				System.err.println("----------------Ejecutando script "+scriptFile+" en dyna"+i);
				executeScript(file, i, gestor, port, "dynagent", pwd);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		
		
	}
	
	/**
	 * Ejecuta el script del fichero dado.
	 * 
	 * @param scriptFile
	 *            Objeto que representa el fichero donde está el script. No se hacen comprobaciones de que exista pues
	 *            se supone que ya se ha verificado con anterioridad.
	 * @return <code>true</code> si se ha conseguido ejecutar el script con éxito.
	 * @throws SQLException
	 * @throws NamingException
	 * @throws IOException 
	 */
	private static void executeScript(File scriptFile,int bns,String gestor,Integer port,String userBD,String pwdBD) throws SQLException, NamingException, IOException{
		FactoryConnectionDB fcdb = new FactoryConnectionDB(bns, true, "localhost", gestor);
		fcdb.setPort(port);
		fcdb.setUsuario(userBD);
		fcdb.setPwd(pwdBD);
		
		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		ResultSet resultSet = null;
		List<String> scriptLines = DBQueries.parseFile(scriptFile,gestor.equals("mySQL"));
		//System.out.println("[FTPCONNECTOR Info] lineas SQL "+scriptLines.size());
		try {
			statement = connectionDB.getBusinessConn().createStatement();
			for (String sql : scriptLines) {
				//System.out.println("Ejecutando "+sql);
				statement.execute(sql);
			}
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
			if (connectionDB!=null) {
				fcdb.close(connectionDB);
			}
		}
	}

}
