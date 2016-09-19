package dynagent.tools.setup.ddbb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

import javax.naming.NamingException;

import dynagent.tools.importers.IImporter;
import dynagent.tools.importers.ImporterFactory;

public class CreationDB {

	/**
	 * @param args
	 * 
	 * 	Ejemplo:
	 	-initNew 203
		-endNew 204
		-backup E:\register.backup
		-gestor postgreSQL
		-ip 192.168.1.3
		-jboss F:\jboss-4.0.5.GA-1\
		-userFiles E:\files
		
	 */
	public static void main(String[] args) {
		int initNew=0;//Numero de inicio de creacion de nuevas bases de datos
		int endNew=0;//Numero final de creacion de nuevas bases de datos
		int initLink=0;//Numero de inicio de insercion de enlaces a base de datos en mssql.xml y ejb-jar.xml
		int endLink=0;//Numero final de insercion de enlaces a base de datos en mssql.xml y ejb-jar.xml
		String backup=null;//Nombre del backup que se utilizara como fuente para las nuevas bases de datos
		String gestor=null;//Gestor de base de datos:postgreSQL, mySQL...
		String ip=null;//Dirección ip donde crear las bases de datos
		String userFiles=null;
		String jboss=null;
		String pwd=null;
		String id="";
		for(int i=0;i<args.length;i++){
			//System.out.println("\n debug Menu param="+args[i]);
			if(args[i].startsWith("-"))
				id=args[i];
			else{
				if(id.equalsIgnoreCase("-initNew")){
					initNew=Integer.parseInt(args[i]);
				}else if(id.equalsIgnoreCase("-endNew")){
					endNew=Integer.parseInt(args[i]);
				}else if(id.equalsIgnoreCase("-initLink")){
					initLink=Integer.parseInt(args[i]);	
				}else if(id.equalsIgnoreCase("-endLink")){
					endLink=Integer.parseInt(args[i]);
				}else if(id.equalsIgnoreCase("-backup")){
					backup=args[i];
				}else if(id.equalsIgnoreCase("-userFiles")){
					userFiles=args[i];
				}else if(id.equalsIgnoreCase("-jboss")){
					jboss=args[i];
				}else if(id.equalsIgnoreCase("-gestor")){
					gestor=args[i];
				}else if(id.equalsIgnoreCase("-ip")){
					ip=args[i];
				}else if(id.equalsIgnoreCase("-pwd")){
					pwd=args[i];
				}
			}
		}
		
		Runtime runtime=Runtime.getRuntime();
		// Setup XCopy command
		String[] prog = new String[5];
		prog[0] = "XCopy"; // The DOS external command we're using
		prog[1] = userFiles;
		prog[3] = "/Y"; // Allow overwrite without prompt
		prog[4] = "/I"; // If target directory not exist then it is created
		
		//If Jboss ends in / or \, we remove it because it is added later
		if(jboss.endsWith("/") || jboss.endsWith("\\")){
			jboss=jboss.substring(0, jboss.length()-1);
		}
		
		IImporter importer=ImporterFactory.createImporter(gestor,pwd);
		importer.setHost(ip);
		for(int i=initNew;i<=endNew;i++){
			try {
				if(importer.createSchema(i, -1)){
					importer.restoreBackup(i, backup, false);
					
					//Copy all userFiles in the folder of this database 
					prog[2] = jboss+"\\server\\default\\deploy\\jbossweb-tomcat55.sar\\ROOT.war\\dyna\\userFiles\\"+i; 
					
					Process process = runtime.exec(prog); // Copy the file
				}else{
					System.err.println("dyna"+i+" ya existe por lo que no será creada ni restaurado el backup sobre ella");
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}
		
		
		
	}

}
