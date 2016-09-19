package dynagent.common.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.Set;

import it.sauronsoftware.ftp4j.FTPFile;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
/**
 * Clase que se encarga de conectar mediante FTP.
 */
public class FTP4 {

//	private final static String url = "ftp.h1694559.stratoserver.net";
//	private final static String user = "celop";
//	private final static String pass = "Clp777";
	public String url = "server.dynagent.es";
	public String user = "XXX";
	public String pass = "YYY";
	public int port=21;
//	private final static String remoteFolderPath= "images";
	private FTPClient ftpClient;
	private File pathLocal;

	public FTP4(String url, String user, String pass,int port){
		this.url=url;
		this.user = user;
		this.pass = pass;
		this.port=port;
		this.ftpClient = new FTPClient();
		
		//Necesario en windows vista y 7 ya que el firewall de windows hace que falle. Supuestamente sacaran una actualizacion de windows para esto y ya no haria falta.
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("ftp4j.passiveDataTransfer.useSuggestedAddress", "false");
		ftpClient.setPassive(true);
		
		//ftpClient.enterLocalPassiveMode();
		
	}
	

	public void setBinary() throws IOException {
		//se usará sólo para la transferencia de imágenes
		ftpClient.setType(FTPClient.TYPE_BINARY);
		//ftpClient.setFileTransferMode(FTP.BLOCK_TRANSFER_MODE);
	}

	public boolean connect() throws IOException, IllegalStateException, FTPIllegalReplyException, FTPException{
		System.out.println("[FTPCONNECTOR Info] Se procede a conectar con el Servidor "+url+" "+port);
		//ftpClient.setConnectTimeout(5000);
		ftpClient.connect(url,port);
		ftpClient.login(user, pass); 
		return true;
	}
	
	/////////////////////
	//Métodos de subida//
	/////////////////////
	/**
	 * Envía ficheros de una ruta local a remoto.
	 * 
	 * @return <code>true</code> si no ha habido errores.
	 */
	/*public boolean sendXMLData(String data, String ftpDestinoPath) throws Exception{
		boolean exito=false;
		String msg="";
		try {		
			// Intentamos almacenar el fichero.
			System.out.println("ENVIANDO RUTA "+ftpDestinoPath);
			OutputStreamWriter out=new OutputStreamWriter(ftpClient.storeFileStream(ftpDestinoPath),"UTF-8");
			System.out.println("OUT PUT STREAM");
			PrintWriter writer = new PrintWriter(out, false);
			System.out.println("PRINT WRITTER");
			writer.write(data);
			System.out.println("FIN WRITE");
			writer.close();
			System.out.println("CLOSE");
			ftpClient.logout();
			System.out.println("FIN ENVIO FTP");
			exito=true;
		} catch (SocketException e) {
			System.out.println("Error al intentar conectar con el servidor de actualizaciones");
			e.printStackTrace();
			msg=e.getMessage();
		} catch (IOException e) {
			System.out.println("Error al intentar listar los ficheros del directorio celop del servidor");
			e.printStackTrace();
			msg=e.getMessage();
		} catch(Exception e){			
			e.printStackTrace();
			msg=e.getMessage();
		}finally {		
			try {
				ftpClient.disconnect();
			} catch (IOException e) {
				System.out.println("[CONNECTION Error] Error al intentar desconectar.");
				e.printStackTrace();
			}
			if(!exito) throw new Exception("sendXMLData: error ftp "+msg);
		}
		return true;
	}*/
	
	public boolean sendFiles(Set<String> filesNames, String ftpDestinoPath,String pathLocal){
		boolean success = true;
		this.pathLocal = new File(pathLocal);
		try {
			connect();
			setBinary();
			System.out.println("[FTP4 Info] Comprobamos los ficheros que se encuentran bajo la carpeta local");			
			if( ftpDestinoPath!=null ) ftpClient.changeDirectory(ftpDestinoPath);
			for (String fileName : filesNames) {				
				File file = new File(pathLocal + "/" + fileName);
				success = uploadFile(file);
				if(!success) break;
				System.out.println("YA REALIZADO UPLOAD exito:"+success+" ruta local:"+pathLocal + "/" + fileName+" destino:"+ftpDestinoPath);
			}
			
			//success=ftpClient.completePendingCommand();			
		} catch (SocketException e) {
			System.out.println("Error al intentar conectar con el servidor de actualizaciones");
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			System.out.println("Error al intentar listar los ficheros del directorio del servidor");
			e.printStackTrace();
			return false;
		} finally {
			try {
				ftpClient.disconnect(false);
			} catch (Exception e) {
				System.err.println("[CONNECTION Error] Error al intentar desconectar.");
				e.printStackTrace();
				return false;
			}
		}

		return success;
	}


	/**
	 * Almacena el fichero y sustituye al fichero remoto.
	 * 
	 * @param remotePath
	 *            Ruta relativa desde la raíz de la carpeta remota en la cual se encontrará el fichero que se ha de
	 *            almacenar
	 * @param localFile
	 *            Fichero que tenemos que almacenar en remoto.
	 */
	private boolean uploadFile(File localFile) {
		boolean success = false;
		try {
			// Intentamos almacenar el fichero.
			System.out.println("Creando Stream");
			FileInputStream inputStream = new FileInputStream(localFile);
			System.out.println("upload");
			ftpClient.upload(localFile);
			System.out.println("Cambia permisos");
			ftpClient.sendSiteCommand("chmod " + "777 " +localFile.getName());
			
			System.out.println("FIN STORE "+localFile.getName());
			success=true;
		}catch(Throwable e){
			e.printStackTrace();
			System.out.println("Excepcion ");
			//No encuentra quizas una foto que no se importo por error
		}
		if (!success){
			// El tamaño del fichero remoto es distinto del que se ha almacenado, con lo cual no lo hemos subido
			// bien. 
			System.out.println("Error en la subida del fichero " + localFile.getAbsolutePath());
		}
		return success;
	}
	public boolean getFiles(Set<String> filesNames, String remotePath, String localPath){
		boolean success = true;
		this.pathLocal = new File(localPath);
		try {
			connect();
			
			System.out.println("[FTPCONNECTOR Info] Comprobamos los ficheros que se encuentran bajo la carpeta remota");
			success = success && downloadImagesFiles(filesNames,remotePath);
			
			//ftpClient.logout();
		} catch (Exception e) {
			System.out.println("Error al intentar listar los ficheros del directorio celop del servidor");
			return false;
		} finally {
			try {
				ftpClient.disconnect(true);
			} catch (Exception e) {
				System.err.println("[CONNECTION Error] Error al intentar desconectar.");
				e.printStackTrace();
			}
		}

		return success;
	}
	
	/**
	 * Comprueba todos los ficheros bajo la carpeta en el servidor FTP para ver si hay que sustituir algún fichero
	 * local.
	 * @param remotePath 
	 * 
	 * @return <code>true</code> si no se ha producido ningún error durante la descarga de todos los ficheros.
	 */
	private boolean downloadImagesFiles(Set<String> filesNames, String remotePath){
		boolean success = true;
		for (String fileName : filesNames) {
			File file = new File(pathLocal + "/" + fileName);			
			success = downloadFile(remotePath+"/"+fileName, file);
		}
		return success;
	}
	
	/**
	 * Descarga el fichero y sustituye al fichero local.
	 * 
	 * @param remotePath
	 *            Ruta relativa desde la raíz de la carpeta remota en la cual se encuentra el fichero que se ha de
	 *            descargar
	 * @param localFile
	 *            Fichero que tenemos que sustituir con el fichero descargado.
	 */
	private boolean downloadFile(String remotePath, File localFile) {
		System.out.println("[FTPCONNECTOR Info] Se va a descargar el fichero " + remotePath);
		boolean success = true;
		try {
			// Nos intentamos descargar el fichero.
			System.err.println("absolutePath:"+localFile.getAbsolutePath());
			boolean exist = localFile.createNewFile();
			ftpClient.download(remotePath, localFile);
		} catch(Exception e){
			e.printStackTrace();
			success = false;
		}
		if (!success){
			System.out.println("Error en la descarga del fichero " + localFile.getAbsolutePath());
		}
		return success;
	}
	
	public boolean getAllFiles(String remotePath, String localPath,String filesType,boolean overwrite){
		boolean success = true;
		this.pathLocal = new File(localPath);
		try {
			connect();
			System.out.println("[FTPCONNECTOR Info] Comprobamos los ficheros que se encuentran bajo la carpeta remota");
			success = success && downloadAllFiles(remotePath,filesType,overwrite);
			
			//ftpClient.logout();
		} catch (Exception e) {
			System.out.println("Error al intentar listar los ficheros del directorio del servidor");
			e.printStackTrace();
			return false;
		} finally {
			try {
				ftpClient.disconnect(true);
			} catch (Exception e) {
				System.err.println("[CONNECTION Error] Error al intentar desconectar.");
				e.printStackTrace();
			}
		}

		return success;
	}
	
	private boolean downloadAllFiles(final String remotePath, final String filesType,final boolean overwrite) {
		System.out.println("[FTPCONNECTOR Info] Se van a descargar todos los ficheros");
		boolean success = true;
		try {
			
			FTPFile[] ftpFiles =ftpClient.list(remotePath);

	        if (ftpFiles.length == 0) {
	            System.out.println("no files");
	            success=false;
	        }
	        else
	        {
	        	System.out.println("Files and sub directories inside the directory");
	        	for (FTPFile ftpFile : ftpFiles)
		        {
		            if (ftpFile.getType()==FTPFile.TYPE_FILE)
		            {
		            	File localFile=new File(pathLocal+"\\"+ftpFile.getName());
		            	boolean accept=true;
		            	if(filesType!=null){
							accept=ftpFile.getName().toLowerCase().endsWith(filesType.toLowerCase());
						}
		            	
		            	if(accept && !overwrite){
							accept=!localFile.exists();
						}
		            	
		            	if(accept){
			            	String remoteFilePath=remotePath+"\\"+ftpFile.getName();
			                System.out.println("File Name :"+remoteFilePath);
			                downloadFile(remoteFilePath, localFile);
		            	}
		            }
		        }
	        }   
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}
		
		return success;
	}
}