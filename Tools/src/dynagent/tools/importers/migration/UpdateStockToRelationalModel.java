
//transformacion selekta
//E:\DESARROLLO\Workspace\Maca\Migration\src\xml\selekta\model\tr.xsl

package dynagent.tools.importers.migration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.NamingException;

import dynagent.common.exceptions.DataErrorException;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;

public class UpdateStockToRelationalModel {

	/**
	 * 
	 * <b>EJEMPLO PARAMETROS DEL MAIN</b><br>
	 * 
	 * -sourceip 192.168.1.3
	 * -sourceport 3310
	 * -sourcebns 3000
	 * -sourcegestor mySQL
	 * 
	 * -ip 192.168.1.3
	 * -port 5432
	 * -bns 13
	 * -gestor postgreSQL
	 * 
	 * -owlpath E:/DESARROLLO/ONTOLOGIA/
	 * -owlfile MODELO.owl
	 * 
	 * -xmlpath E:/DESARROLLO/Workspace/Maca/knowledge/src/config/
	 * -xmlfile configGenericoNEW.xml
	 * -pathImportReports Y:/jboss-4.0.5.GA-2/server/default/deploy/jbossweb-tomcat55.sar/ROOT.war/dyna/userFiles
	 * -reportpath E:/DESARROLLO/filesReport/ModeloRelacional/
	 * 
	 * @param args
	 *            Parámetros para el lanzamiento de la utilidad.
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// Host de origen de los datos a copiar
		String sourceIp = null;
		// Puerto en el que está el servidor de base de datos en el host de origen.
		Integer sourcePort = null;
		// Número de la base de datos de la que tenemos que coger los datos.
		Integer sourceBns = null;
		// Gestor de base de datos que almacena los datos de origen.
		String sourceGestor = null;
		// Host donde tenemos que actualizar los datos.
		String ip = null;
		// Puerto donde tenemos que conectar en el host de la nueva base de datos.
		Integer port = null;
		// Número de la base de datos donde copiar los datos
		Integer bns = null;
		// Host de la central donde tenemos que actualizar los datos.
		String ipCentral = null;
		// Puerto de la central donde tenemos que conectar en el host de la nueva base de datos.
		Integer portCentral = null;
		// Número de la base de datos de la central donde actualizar los datos
		Integer bnsCentral = null;
		// Gestor de la nueva base de datos.
		String gestor = null;
		
		String tag = "";
		String menuParams = "";
		
		for (String arg : args){
			if (arg.startsWith("-")){
				tag = arg;
			}else{
				if (tag.equalsIgnoreCase("-sourceip")){
					sourceIp = arg;
				}else if (tag.equalsIgnoreCase("-sourceport")){
					sourcePort = Integer.parseInt(arg);
				}else if (tag.equalsIgnoreCase("-sourcebns")){
					sourceBns = Integer.parseInt(arg);
				}else if (tag.equalsIgnoreCase("-sourcegestor")){
					sourceGestor = arg;
				}else if (tag.equalsIgnoreCase("-ip")){
					ip = arg;
				}else if (tag.equalsIgnoreCase("-port")){
					port = Integer.parseInt(arg);
				}else if (tag.equalsIgnoreCase("-bns")){
					bns = Integer.parseInt(arg);
				}else if (tag.equalsIgnoreCase("-ipcentral")){
					ipCentral = arg;
				}else if (tag.equalsIgnoreCase("-portcentral")){
					portCentral = Integer.parseInt(arg);
				}else if (tag.equalsIgnoreCase("-bnscentral")){
					bnsCentral = Integer.parseInt(arg);
				}else if (tag.equalsIgnoreCase("-gestor")){
					gestor = arg;
				}else{
					menuParams += " " + tag + " " + arg;
				}
			}
		}
		
		// Una vez hemos sacado los parámetros del array de entrada, procedemos a crear la base de datos
		FactoryConnectionDB fcdbOrigin = setConnection(sourceBns, sourceIp, sourceGestor, sourcePort);
		fcdbOrigin.setPwd("domocenter28");
		HashMap<String,Integer> mapRdnStockQuantity=getStockQuantity(fcdbOrigin);
		
		FactoryConnectionDB fcdbDestinationLocal = setConnection(bns, ip, gestor, port);
		FactoryConnectionDB fcdbDestinationRemote = setConnection(bnsCentral, ipCentral, gestor, portCentral);
		
		String updaterSql="";
		Iterator<String> itr=mapRdnStockQuantity.keySet().iterator();
		while(itr.hasNext()){
			String rdn=itr.next();
			Integer quantity=mapRdnStockQuantity.get(rdn);
			
			updaterSql+="update stock set cantidad="+quantity+" where rdn='"+rdn+"';\n";
		}
		
		System.err.println("Actualizando base de datos local");
		DBQueries.executeUpdate(fcdbDestinationLocal, updaterSql);
		System.err.println("Actualizando base de datos remota");
		DBQueries.executeUpdate(fcdbDestinationRemote, updaterSql);
	}
	
	public static HashMap<String,Integer> getStockQuantity(FactoryConnectionDB fcdb) throws DataErrorException, SQLException, NamingException {
		HashMap<String,Integer> mapRdnStockQuantity=new HashMap<String, Integer>();

		String sql ="	select  concat(a.val_texto,'#',p.val_texto) as strdn,sq.q_min as q					\n"+
					"	from    o_datos_atrib as sq inner join				\n"+
					"	o_datos_atrib as sp on(sq.id_o=sp.id_o and sp.property=(select prop from properties where name='producto')) inner join 				\n"+
					" 	o_datos_atrib as p on(p.id_o=sp.val_num)  inner join																					\n"+
					"	o_datos_atrib as sa on(sa.id_o=sq.id_o and sa.property=(select prop from properties where name='almacén_stock')) inner join					\n"+
					"	o_datos_atrib as a on(sa.val_num=a.id_o )																											\n"+
					"	where sq.id_to=(select idto from clases where name='STOCK') and sq.property=(select prop from properties where name='cantidad') and p.property=2 and a.property=2;";
		
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true); 
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()){
				String rdn=rs.getString(1);
				Integer quantity=rs.getInt(2);
				mapRdnStockQuantity.put(rdn, quantity);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		
		return mapRdnStockQuantity;
	}

	public static FactoryConnectionDB setConnection(int bns, String ip, String gestor, int port) {
		FactoryConnectionDB fcdb = new FactoryConnectionDB(bns, true, ip, gestor);
		fcdb.setPort(port);
		return fcdb;
	}

}
