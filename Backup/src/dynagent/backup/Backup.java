package dynagent.backup;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.naming.NamingException;

import dynagent.common.Constants;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.Email;
import dynagent.common.utils.Zip;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GestorsDBConstants;
import dynagent.server.services.InstanceService;

public class Backup {
	
	/**
	 * Crea backups automaticamente cada dia de la semana. Mantiene cada backups durante 7 dias(lo machaca con el del dia de la semana siguiente).
	 * Los backups se guardan con nombre dyna1_0.zip, dyna1_1.zip..., dyna1_6.zip. El numero que va aumentando se corresponde con domingo, lunes, ..., sabado.
	 *  
	 * @param args
	 * @throws SQLException
	 * @throws NamingException
	 */
	public static void main(final String[] args) throws SQLException, NamingException {
		
		try{
			
			/*
			 Si initdb y enddb no son el mismo numero(backups multiples) se ejecutara a la hora indicada en este parametro. Si no existe se ejecutara inmediatamente.
			 Si initdb y enddb son el mismo numero(un unico backup), la hora se esta cogiendo del parametro numerico BACKUP_hora_ejecucion de la base de datos de la que hacer el backup. En el caso de que no exista, se utilizaria el parametro hour indicado aqui. Si tampoco existe se ejecutara inmediatamente
			*/
			
			int initDB=0;//Numero de inicio de base de datos para la ejecucion del script
			int endDB=0;//Numero del fin de base de datos para la ejecucion del script
			Integer portdb=5432;
			String jboss=null;
			String postgres=null;
			String destinationPath=null;
			Integer hour=null;
			String ddbbUser="dynagent";
			String ddbbPass=null;
			Integer num_ficheros=null;
			
			String emailDest=null;
			String emailRemit=null;
			String emailPwd=null;
			String cloudName=null;
			
			
			String id="";
			for(int i=0;i<args.length;i++){
				//System.out.println("\n debug Menu param="+args[i]);
				if(args[i]==null||args[i].length()==0) continue;
				if(args[i].startsWith("-"))
					id=args[i];
				else{					
					if(id.equalsIgnoreCase("-initdb")){
						initDB=Integer.parseInt(args[i]);
					}else if(id.equalsIgnoreCase("-enddb")){
						endDB=Integer.parseInt(args[i]);
					}else if(id.equalsIgnoreCase("-jboss")){
						jboss=args[i];
					}else if(id.equalsIgnoreCase("-postgres")){
						postgres=args[i];
					}else if(id.equalsIgnoreCase("-portdb")){
						portdb=Integer.parseInt(args[i]);
					}else if(id.equalsIgnoreCase("-destinationpath")){
						destinationPath=args[i];
					}else if(id.equalsIgnoreCase("-hour")){
						hour=Integer.valueOf(args[i]);
					}else if(id.equalsIgnoreCase("-ddbbuser")){
						ddbbUser=args[i];
					}else if(id.equalsIgnoreCase("-ddbbpass")){
						ddbbPass=args[i];
					}else if(id.equalsIgnoreCase("-numero_ficheros")){
						num_ficheros=Integer.valueOf(args[i]);
					}else if(id.equalsIgnoreCase("-emaildest")){
						emailDest=args[i];
					}else if(id.equalsIgnoreCase("-emailremit")){
						emailRemit=args[i];
					}else if(id.equalsIgnoreCase("-emailpwd")){
						emailPwd=args[i];
					}else if(id.equalsIgnoreCase("-cloudname")){
						cloudName=args[i];
					}
				}
			}
			
			//startCloud();
			String user=ddbbUser;
			String password=ddbbPass;
			//Añadimos como variable de entorno el password de la base de datos
			Map<String,String> env=System.getenv();
			String envp[]=new String[env.size()+2];
			envp[0]="PGUSER="+user;
			envp[1]="PGPASSWORD="+password;
			int i=2;
			for(String key:env.keySet()){
				envp[i]=key+"="+env.get(key);
				i++;
			}
			FactoryConnectionDB fcdb = new FactoryConnectionDB(initDB,true,"localhost",GestorsDBConstants.postgreSQL);
			fcdb.setPwd(password);
			if (portdb!=null){
				fcdb.setPort(portdb);
			}
			String businessName=InstanceService.getBusinessName(fcdb);
			if(initDB==endDB){				
				Integer hourAux=getExecutionHour(fcdb,initDB);
				if(hourAux!=null){
					hour=hourAux;
				}
			}
			
			executeBackups(initDB, endDB, portdb, jboss, postgres, destinationPath, hour, envp,num_ficheros,user,emailDest,emailRemit,emailPwd,cloudName,businessName);
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	private static void executeBackups(final int initDB, final int endDB, final Integer portdb,	final String jboss, final String postgres, final String destinationPath, final Integer hour, final String[] envp,final Integer num_ficheros,final String user,
			final String emailDest,final String emailRemit,final String emailPwd,final String cloudName,final String businessName) {
		if(hour!=null){
			TimerTask task=new TimerTask(){
	
				@Override
				public void run() {
					createBackups(initDB, endDB, portdb, destinationPath, jboss, postgres, envp,num_ficheros,user,emailDest,emailRemit,emailPwd,cloudName,businessName);
				}
				
			};
			
			GregorianCalendar calendar=new GregorianCalendar();
			if(calendar.get(Calendar.HOUR_OF_DAY)>hour){
				calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR)+1);
			}
			calendar.set(Calendar.HOUR_OF_DAY, hour);
			calendar.set(Calendar.MINUTE, 0);
			Timer timer=new Timer();
			long executionPeriod=Constants.TIMEMILLIS*60*60*24;
			timer.schedule(task, calendar.getTime(), executionPeriod);
		}else{
			createBackups(initDB, endDB, portdb, destinationPath, jboss, postgres, envp,num_ficheros,user,emailDest,emailRemit,emailPwd,cloudName,businessName);
		}
	}

	private static void createBackups(int initDB, int endDB, int port, String destinationPath, String jboss, String postgres, String[] envp,Integer num_ficheros,String user,
			String emailDest,String emailRemit,String emailPwd,String cloudName,String businessName) {
		for(int i=initDB;i<=endDB;i++){
			System.err.println("----------------Ejecutando backup en dyna"+i);
			String folder=destinationPath;
			File folderFile=new File(folder);
			if(!folderFile.exists()){
				folderFile.mkdir();
			}
			try{
				System.err.println("----------------Creando backup");
				
				File backupFile=new File(folderFile.getAbsolutePath()+"\\dyna"+i+".backup");
				if(createBackup(i, port, backupFile.getAbsolutePath(), null, null, postgres, envp, user,emailDest,emailRemit,emailPwd,cloudName,businessName)){
					if(jboss!=null){
						System.err.println("----------------Copiando userFiles");
						copyUserFiles(i,jboss,folderFile.getAbsolutePath()+"\\"+i);
					}
					/*
					System.err.println("----------------Creando zip en "+destinationPath);
					
					File destinationFile=new File(destinationPath);
					if(!destinationFile.exists()){
						destinationFile.mkdir();
					}
					
					int weekday=Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
					
					if(num_ficheros!=null && num_ficheros.intValue()==1){
						weekday=1;
					}
					String zipName=destinationFile.getAbsolutePath()+"\\"+"dyna"+i+"_"+weekday+".zip";
					Zip.zip(folderFile.getAbsolutePath(), zipName);*/
				}
				
			}catch(Exception ex){
				ex.printStackTrace();
			}finally{
				System.err.println("----------------Borrando archivos temporales");
				/*try {
					delete(folderFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				System.err.println("----------------Terminado");
			}
			
		}
	}
	
	private static void delete(File f) throws IOException {
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				delete(c);
		}
		f.delete();
	}

	private static void copyUserFiles(int bns,String jboss,String path) throws IOException, InterruptedException {
		Runtime runtime=Runtime.getRuntime();
		
		//If Jboss ends in / or \, we remove it because it is added later
		if(jboss.endsWith("/") || jboss.endsWith("\\")){
			jboss=jboss.substring(0, jboss.length()-1);
		}
		// Setup XCopy command
		String[] prog = new String[5];
		prog[0] = "XCopy"; // The DOS external command we're using
		prog[1] = jboss+"\\server\\default\\deploy\\jbossweb-tomcat55.sar\\ROOT.war\\dyna\\userFiles\\"+bns;
		prog[2] = path;//Copy all userFiles in the folder of this database 
		prog[3] = "/Y"; // Allow overwrite without prompt
		prog[4] = "/I"; // If target directory not exist then it is created
		
		System.out.println("Origen:"+prog[1]);
		System.out.println("Destino:"+prog[2]);
		Process process = runtime.exec(prog); // Copy the file
		
		InputStream stdin = process.getInputStream();
		InputStreamReader isr = new InputStreamReader(stdin);
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		System.out.println("<OUTPUT>");
		while ((line = br.readLine()) != null)
			System.out.println(line);
		System.out.println("</OUTPUT>");
         
		process.waitFor();
	}

	private static boolean createBackup(int bns, int port, String backupName, Set<String> includedTables, Set<String> excludedTables, String postgres, String[] envp,String user,
			String emailDest,String emailRemit,String emailPwd,String cloudName,String businessName) throws IOException, InterruptedException {
		String host="localhost";
		boolean success = true;
		Runtime runtime = Runtime.getRuntime();

		if(includedTables==null)
			includedTables=new HashSet<String>();
		if(excludedTables==null)
			excludedTables=new HashSet<String>();

		//If Jboss ends in / or \, we remove it because it is added later
		if(postgres.endsWith("/") || postgres.endsWith("\\")){
			postgres=postgres.substring(0, postgres.length()-1);
		}
		String dump[]= {postgres+"\\bin\\pg_dump.exe","-h", host,"-p",""+port,"-U",user,"-w","--format","custom","--file","\""+backupName+"\"","dyna" + bns};				
		
		/*for(String table:includedTables){
			command += " -t "+ table;
		}
		
		for(String table:excludedTables){
			command += " -T "+ table;
		}*/
		
		
		Process process = runtime.exec(dump,envp);
		BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		String line;
		while ((line = errorReader.readLine()) != null){
			System.out.println(line);
			line = line.toUpperCase();
			//if (line.contains("ERROR") || line.contains("FATAL")){ Lo hacemos mejor siempre, ya que si imprime algo es porque hay error
				success = false;
			//}
		}
		//process.waitFor();	
		//process.destroy();
		if(success && cloudName!=null){			
			if(!Auxiliar.checkSystemProcess(cloudName,runtime)){
				sendErrorEmail(emailDest,businessName+ " backup erroneo",businessName+ " backup erroneo",emailRemit,emailPwd);
			}
		}
		
		return success;
	}
	
	private static void sendErrorEmail(String emailDest,  String title,String message,String remitenteErrorsEmail, String remitenteErrorsEmailPassword) {
		System.out.println("Previo envio mail error");
						
		Email.sendEmail(emailDest, title, message, remitenteErrorsEmail, remitenteErrorsEmailPassword, false);
	}
	
	protected static void startCloud() {
		
		try {
			Runtime runtime = Runtime.getRuntime();
			System.out.println("[MAIN Info] Se procede a iniciar cloud");
			Process process = runtime.exec("C:\\Users\\Usuario1\\AppData\\Roaming\\Dropbox\\bin\\Dropbox.exe /home");			
			process.waitFor();						
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			String result=null;
			while ((line = inputReader.readLine()) != null){
				System.out.println(line);
				if(result==null){
					result=line;
				}else{
					result+=line;
				}
			}
			inputReader.close();
			
			System.out.println("[MAIN Info] Servicios iniciados");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Integer getExecutionHour(FactoryConnectionDB fcdb, int business) throws SQLException, NamingException{
		
		Integer hour=null;
		ConnectionDB conn=null;
		Statement st=null;
		ResultSet set=null;
		try{
			conn= fcdb.createConnection(true);
			st= conn.getBusinessConn().createStatement();
			set=st.executeQuery("SELECT valor_numerico FROM parametro_numerico WHERE rdn='BACKUP_hora_ejecucion'");
			if(set.next()){
				hour=set.getInt("valor_numerico");
			}
		}catch(SQLException e){
			;
		}finally{
			if(st!=null){
				st.close();
			}
			if(conn!=null){
				conn.closeStandAlone();
			}
			if(set!=null){
				set.close();
			}
		}
		
		return hour;
	}

}
