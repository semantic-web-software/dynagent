package dynagent.server.services.reports;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;



/*import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;*/


public class Principal {
public static Map<Integer, ParQuery> a = new HashMap<Integer, ParQuery>();
	
	public static void main (String args[]) throws IOException, Exception {
/*		ParQuery par1 = new ParQuery("SELECT inmobiliarias.id, "+
												"inmobiliarias.[nombre empresa] "+
												"FROM inmobiliarias "+
												"ORDER BY inmobiliarias.id;",
												"2","2_nombre empresa_String,1_id_Integer","");
		ParQuery par2 = new ParQuery("SELECT notas.nota1, notas.nota2 "+
				  								 "FROM notas "+
				  								 "WHERE (((notas.id)=__1__)) "+
				  								 "ORDER BY notas.id;","","1_nota1_String,2_nota2_String","");
*/		
/*		ParQuery par1 = new ParQuery("SELECT O_Datos_Atrib.id_o as ido, "+
				"O_Datos_Atrib.val_texto as nombre "+
				"FROM O_Datos_Atrib "+
				"ORDER BY O_Datos_Atrib.id_o;",
				"","2_nombre_String,1_ido_Integer","");*/
		
		ParQuery par1 = new ParQuery("SELECT IDO1,IDOVN1,COL0 AS RDN,COL2 AS Nombre,COL3 AS Comportamiento,QMIN17 AS PruebaQ_MIN," +
				"QMAX17 AS PruebaQ_MAX,QVNUM17 AS PruebaQ_CLASE,SUM((QMIN17+QMAX17)/2)*SUM((QMIN17+QMAX17)/2*10) AS SueldoMIN," +
				"QVNUM17 AS Value FROM (SELECT MAX(CASE WHEN V1.PROPERTY=150 THEN V1.VAL_NUM ELSE NULL END) AS IDOVN1," +
				"MAX(V1.ID_O) AS IDO1, MAX(CASE WHEN V1.PROPERTY=2 THEN V1.VAL_TEXTO ELSE NULL END) AS COL0," +
				"MAX(CASE WHEN V1.PROPERTY=1 THEN V1.VAL_TEXTO ELSE NULL END) AS COL2," +
				"MAX(CASE WHEN V1.PROPERTY=4 THEN V1.VAL_TEXTO ELSE NULL END) AS COL3," +
				"MAX(CASE WHEN V1.PROPERTY=277 THEN V1.Q_MIN ELSE NULL END) AS QMIN17," +
				"MAX(CASE WHEN V1.PROPERTY=277 THEN V1.Q_MAX ELSE NULL END) AS QMAX17," +
				"MAX(CASE WHEN V1.PROPERTY=277 THEN V1.VAL_NUM ELSE NULL END) AS QVNUM17 " +
				"FROM O_Datos_Atrib AS V1 WITH(NOLOCK) WHERE (V1.PROPERTY IN(2,1,4,277,150)) AND V1.ID_TO IN(13) " +
				"GROUP BY V1.ID_O, V1.ID_O) AS H WHERE IDOVN1 IS NOT NULL " +
				"GROUP BY IDO1,IDOVN1,COL0,COL2,COL3,QMIN17,QMAX17,QVNUM17", "4", 
				"4_Nombre_String,5_Comportamiento_String,6_PruebaQ_MIN_Double,7_PruebaQ_MAX_Double,8_PruebaQ_CLASE_Integer," +
				"9_SueldoMIN_Double,10_Value_Integer","1_IDO1_Integer,2_IDOVN1_Integer,3_RDN_String");

		ParQuery par2 = new ParQuery("SELECT IDO4,COL0 AS RDN,IDO7,COL8 AS pr222 FROM (SELECT MAX(V1.ID_O) AS IDO4, " +
				"MAX(CASE WHEN V1.PROPERTY=2 THEN V1.VAL_TEXTO ELSE NULL END) AS COL0, " +
				"MAX(V2.ID_O) AS IDO7, MAX(CASE WHEN V2.PROPERTY=20 THEN V2.VAL_TEXTO ELSE NULL END) AS COL8 " +
				"FROM O_Datos_Atrib AS V1 WITH(NOLOCK) " +
				"LEFT JOIN O_Datos_Atrib AS V2 WITH(NOLOCK) ON (V1.PROPERTY=5 AND V2.ID_O IN(678) AND V1.VAL_NUM IN(678) " +
				"AND V1.VALUE_CLS IN(40) AND V2.ID_TO IN(40) AND V2.PROPERTY IN(20)) " +
				"WHERE ((V1.PROPERTY IN(2) OR V1.PROPERTY=5 AND V1.VALUE_CLS IN(40) AND V1.VAL_NUM IN(678))) " +
				"AND V1.ID_TO IN(25) AND V1.ID_O=__2__" +
				"GROUP BY V1.ID_O) AS H WHERE IDO7 IS NOT NULL GROUP BY IDO4,COL0,IDO7,COL8", "", 
				"4_pr222_String", "1_IDO4_Integer,2_RDN_String,3_IDO7_Integer");
			
		int idMaster = 1;
		a.put(idMaster, par1);
		a.put(4, par2);
		Connection con=connectDB();
		/*JasperPrint pruebaPrint = null;	
		JasperReport pruebaReport=null;
		JasperReport pruebaSubReport=null;*/
		JRDSource prueba=null;	
		//Map<Object,JasperReport> parametros=new HashMap<Object,JasperReport>();
		/*prueba=new JRDSource(con, null, a, idMaster);
		Map<Integer, String> jrxmls=GenerateJRXML.make(a, idMaster);
		ViewReports.view(jrxmls, a, prueba, idMaster);*/
		/*pruebaReport = JasperCompileManager.compileReport("C:\\tmp\\prueba2.jrxml");
		
		pruebaSubReport=JasperCompileManager.compileReport("C:\\tmp\\pruebasubreport.jrxml");
		parametros.put("SUBREPORT_DIR", pruebaSubReport);
		pruebaPrint=JasperFillManager.fillReport(pruebaReport, parametros, prueba);
		JasperViewer.viewReport(pruebaPrint, false);
		*/
		con.close();
		
				
	}
	
	public static Connection connectDB() throws SQLException{
		
		Connection con =null;
		try{
//			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver"); 
//			con= DriverManager.getConnection("jdbc:odbc:Inmobiliarias", "david", "david");
			
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String url = "jdbc:sqlserver://192.168.1.3;databaseName=dyna5;user=sa;password=dynagent";
            con = DriverManager.getConnection(url, "sa", "dynagent");
        }
		catch(Exception e){
			con.close();
			System.err.println("No se ha podido conectar con la base de datos");
			
		}
				
		return con;
	}

}
