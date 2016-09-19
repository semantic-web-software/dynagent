package dynagent.tools.importers.testServer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

import dynagent.server.dbmap.DBQueries;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;

public class TestResultQuery {

	public static void main(String[] args) {
		try{			
			TestResultQuery test = new TestResultQuery();
			String databaseIP = args[0];
			int business = Integer.parseInt(args[1]);
			String gestor = args[2];
			int port = Integer.parseInt(args[3]);
			
			FactoryConnectionDB fcdb = new FactoryConnectionDB(business, true, databaseIP, gestor);
			fcdb.setPort(port);
			
			test.testResultQuery(fcdb);
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void testResultQuery(FactoryConnectionDB fcdb) throws SQLException, NamingException, IOException {
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String cB = gSQL.getCharacterBegin();
		String cE = gSQL.getCharacterEnd();
		
		String sql = "SELECT " + cB + "userRol" + cE + ", " + cB + "rdn" + cE + ", " +
				"decrypt(cast (" + cB + "password" + cE + " as bytea),'dynamicIntelligent','aes'), " + 
				cB + "r_destination" + cE + ", " + cB + "tableId" + cE + ", " + cB + "email" + cE + " FROM " + cB + "user" + cE;
		List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sql);
		int j = 1;
		for (List<String> objectData : queryResult) {
			// Vamos a crear el objeto ficticio que representará los datos que sacamos de BD
			System.out.println("FILA " + j);
			int i = 1;
			for (String value : objectData) {
				System.out.println("valor " + i + "->" + value);
				i++;
			}
			j++;
		}
	}
}
