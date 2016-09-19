package dynagent.common.utils;

import com.sun.mail.smtp.SMTPTransport;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Date;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.InternetAddress;

public class Email {
    
	public enum TipoConexionSegura {Sin_seguridad, SSL, TLS};
		
	private boolean autentificacionPassSegura;
	private TipoConexionSegura tipoConexionSeguraSaliente;
	private String pass;
	private String user;
	private String direccionCE;
	private String nombreRemitente;
	private	int puertoSMTP;
	private String servidorSMTP;
	private boolean servidorAutentificacion;

	//public static String cierreMensajeHTML = "<br/>--<br/><br/> Este es el final del mensaje con la afoto: <img src=\"cid:imagen\">";
	public static String cierreMensajeHTMLDynagent = "<br/><br/> <a href='http://www.dynagent.es'>www.dynagent.es</a><br/> <br/> Dynagent Software";
	//public static File imagenCierre;
	public static String cierreMensajeDynagent = "\r\n\r\n\r\n www.dynagent.es \r\n\r\n Dynagent Software S.L.";
	
	private String mensajeUltimoEnvio;
	
	public Email(EmailConfiguration emailConfiguration){
		this(emailConfiguration.getEmail_remitente(),emailConfiguration.getPassword(),emailConfiguration.getEmail_remitente(),emailConfiguration.getNombre_remitente(),emailConfiguration.getServidor_SMTP(),
				emailConfiguration.getPuerto_SMTP(),emailConfiguration.isAutenticar(),emailConfiguration.getSeguridad_conexion(),emailConfiguration.isAutenticar());
	}
	
	public Email(String user, String pass, String direccionCERemitente, String nombreRemitente,
			String servidorSMTP, int puertoSMTP, boolean servidorAutentificacion, 
			TipoConexionSegura tipoConexionSeguraSaliente, boolean autentificacionPassSegura){
    	this.autentificacionPassSegura = autentificacionPassSegura;
    	this.tipoConexionSeguraSaliente = tipoConexionSeguraSaliente;
    	this.pass = pass;
    	this.user = user;
    	this.direccionCE = direccionCERemitente;
    	this.nombreRemitente = nombreRemitente;
    	this.puertoSMTP = puertoSMTP;
    	this.servidorSMTP = servidorSMTP;
    	this.servidorAutentificacion = servidorAutentificacion;
    	//imagenCierre = new File(getClass().getResource("/correoElectronico/cierre.jpg").getPath());
    }
    
    public boolean envioCorreoElectronico(String[] direccionesDestino, String[] nombresDestino, String asunto, String mensaje, ArrayList<String> fileNames, boolean esHTML){
    	mensajeUltimoEnvio=null;
    	if(nombresDestino!=null && direccionesDestino.length!=nombresDestino.length)
        	return false;
    	
        /*KeyStore ks;
		try {
			ks = KeyStore.getInstance(KeyStore.getDefaultType());
		       // get user password and file input stream
	        char[] password = "DynaGentKy".toCharArray();

	        java.io.FileInputStream fis = null;
	        try {
	            try {
	            	String fileName="dynagentks";
	            	File f = new File(fileName);
	            	if(!f.exists() || !f.isDirectory()){
	            		ks.load(null, password);
	            	    java.io.FileOutputStream fos = null;
	            	    try {
	            	        fos = new java.io.FileOutputStream(fileName);
	            	        ks.store(fos, password);
	            	    } finally {
	            	        if (fos != null) {
	            	            fos.close();
	            	        }
	            	    }	            	    
	            	}else{
	            		fis = new java.io.FileInputStream(fileName);
	            		ks.load(fis, password);
	            	}
	            	
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
	        } finally {
	            if (fis != null) {
	                fis.close();
	            }
	        }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
         
        MimeMultipart multipart = new MimeMultipart();
        
        Properties props = System.getProperties();
        Session session = null;
       	props.put("mail.smtp.host", servidorSMTP);
       	props.put("mail.smtp.port", puertoSMTP);
       	props.put("mail.smtps.host", servidorSMTP);
        props.put("mail.smtps.port", puertoSMTP);
        
        if(tipoConexionSeguraSaliente==TipoConexionSegura.TLS)
        	props.put("mail.smtp.starttls.enable","true");
        else props.put("mail.smtp.starttls.enable","false");
        
        if(tipoConexionSeguraSaliente==TipoConexionSegura.SSL){
        	props.put("mail.smtp.ssl.enable","true");
            props.put("mail.smtp.ssl.checkserveridentity", "false");
            props.put("mail.smtp.ssl.trust", "*");
        }
                	        
       	if(servidorAutentificacion){
       		props.put("mail.smtp.auth", "true");
       		props.put("mail.smtps.auth", "true");
       		AuthenticatorCorreo auth = new AuthenticatorCorreo(user, pass);
       		session = Session.getInstance(props, auth);
       	}else{
       		props.put("mail.smtp.auth", "false");
       		props.put("mail.smtps.auth", "false");
       		session = Session.getInstance(props, null);
       	}
        
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(direccionCE,nombreRemitente));
            
            boolean first=true;
            for(int i=0;i<direccionesDestino.length;i++) {
            	Address bccAddress = nombresDestino!=null?new InternetAddress(direccionesDestino[i], nombresDestino[i]):new InternetAddress(direccionesDestino[i]);
            	if(first){
            		msg.addRecipient(Message.RecipientType.TO, bccAddress);
            		first=false;
            	}else{
            		msg.addRecipient(Message.RecipientType.BCC, bccAddress);
            	}
            }
            
            msg.setSubject(asunto);
            msg.setSentDate(new Date());
            
            MimeBodyPart mbp = new MimeBodyPart();
            MimeBodyPart mbpFoto = null;
            if(Auxiliar.equals(this.nombreRemitente,"Dynagent")){
	            if(esHTML){
	            	mbp.setContent(mensaje+cierreMensajeHTMLDynagent, "text/html");
	            	//mbpFoto = new MimeBodyPart();
	            	//DataSource fds = new FileDataSource(imagenCierre);
	            	//mbpFoto.setDataHandler(new DataHandler(fds));
	            	//mbpFoto.setHeader("Content-ID", "imagen");
	            }else
	            	mbp.setText(mensaje+cierreMensajeDynagent);
            }else{
            	if(esHTML){
	            	mbp.setContent(mensaje, "text/html");
	            	//mbpFoto = new MimeBodyPart();
	            	//DataSource fds = new FileDataSource(imagenCierre);
	            	//mbpFoto.setDataHandler(new DataHandler(fds));
	            	//mbpFoto.setHeader("Content-ID", "imagen");
	            }else
	            	mbp.setText(mensaje);
            }
            
            multipart.addBodyPart(mbp);
            
            //if(mbpFoto!=null)
            	//multipart.addBodyPart(mbpFoto);
            
            MimeBodyPart mbpFile = null;
            
            if(fileNames!=null){
            	String nameToFileIfPdf="documento";//Nombre usado para los reports ya que no queremos que se envie con nombre: nombreDeUsuario.pdf
            	int i=0;
	            for(String fileName:fileNames){
		            mbpFile=new MimeBodyPart();
		         // attach the file to the message
		            FileDataSource fds = new FileDataSource(fileName);
		            mbpFile.setDataHandler(new DataHandler(fds));
		            if(fileName.endsWith(".pdf")){
		            	mbpFile.setFileName(/*fds.getName()*/nameToFileIfPdf+".pdf");
		            	nameToFileIfPdf="documento"+i;
		            }else{
		            	mbpFile.setFileName(fds.getName());
		            }
		            
		            multipart.addBodyPart(mbpFile);
		            
		            i++;
		            
	            }
            }
            
            msg.setContent(multipart);
            
            SMTPTransport t = null;
            if(tipoConexionSeguraSaliente.equals(TipoConexionSegura.Sin_seguridad))
            	t = (SMTPTransport)session.getTransport("smtp");
            else
            	t = (SMTPTransport)session.getTransport("smtps");
            
            t.connect(servidorSMTP, user, pass);
            t.sendMessage(msg, msg.getAllRecipients());
            t.close();
        } catch (Exception mex){
        	mex.printStackTrace();
        	System.err.println("Excepcion:"+mex.toString());
            System.err.println(mex);
            mensajeUltimoEnvio=mex.toString();
            if(mensajeUltimoEnvio.length()>100){//El campo de base de datos solo admite de maximo 100 caracteres
            	mensajeUltimoEnvio=mensajeUltimoEnvio.substring(0, 99);
            }
            return false;
        }
        return true;
    }
    
	public String getMensajeUltimoEnvio() {
		return mensajeUltimoEnvio;
	}
	
	
	public static void sendEmail(String destinatarioEmail, String title, String message) {
		sendEmail(destinatarioEmail, title, message, true);
	}
	
	public static void sendEmail(String destinatarioEmail, String title, String message, boolean html) {
		sendEmail(destinatarioEmail, title, message, html, null);
	}
	
	public static void sendEmail(String destinatarioEmail, String title, String message, boolean html, ArrayList<String> files) {
		sendEmail(destinatarioEmail, title, message, "dynagent@gmail.com", "DynaGentKy", html, files);
	}
	
	public static void sendEmail(String destinatarioEmail, String title, String message, String remitenteEmail, String remitenteEmailPassword) {
		sendEmail(destinatarioEmail, title, message, remitenteEmail, remitenteEmailPassword, true);
	}
	
	public static void sendEmail(String destinatarioEmail, String title, String message, String remitenteEmail, String remitenteEmailPassword, boolean html) {
		if(remitenteEmail!=null && remitenteEmail.equals("info@dynagent.es")){
			long time=System.currentTimeMillis();
			Long dif=Math.round(time/100.0)-Math.round(time/1000.0)*10;
			System.out.println("TIME "+time+" DIF:"+dif);
			if(dif.intValue()>0 && dif.intValue()<10){
				remitenteEmail="info"+dif.intValue()+"@dynagent.es";
				System.out.println("New remitente "+remitenteEmail);
			}
		}
		sendEmail(destinatarioEmail, title, message, remitenteEmail, remitenteEmailPassword, html, new ArrayList<String>());
	}
	
	public static void sendEmail(String destinatarioEmail, String title, String message, String remitenteEmail, String remitenteEmailPassword, boolean html, ArrayList<String> files) {
		System.out.println("Pre Creada clase email..."+remitenteEmail);
		Email email=new Email(remitenteEmail,remitenteEmailPassword,remitenteEmail,"Dynagent", "smtp.dynagent.es", 587, true, Email.TipoConexionSegura.TLS, true);
		System.out.println("Creada clase\nEnviando "+remitenteEmail+" "+remitenteEmailPassword);
		String[] correosDestino = {destinatarioEmail};
		String[] nombresDestino = {"dynagent"};
		if(email.envioCorreoElectronico(correosDestino, nombresDestino, title, message, files, html)){
			System.out.println("Enviado email");
		}else{
			System.out.println("NO Enviado email");
		}
	}
	
	public static void sendEmail(String destinatarioEmail, String title, String message, String remitenteEmail, String remitenteEmailPassword, boolean html, String servidorSMTP) {
		Email email=new Email(remitenteEmail,remitenteEmailPassword,remitenteEmail,"Dynagent", servidorSMTP, 587, true, Email.TipoConexionSegura.TLS, true);
		System.out.println("Creada clase\nEnviando...");
		String[] correosDestino = {destinatarioEmail};
		String[] nombresDestino = {"dynagent"};
		if(email.envioCorreoElectronico(correosDestino, nombresDestino, title, message, null, html)){
			System.out.println("Enviado email");
		}else{
			System.out.println("NO Enviado email");
		}
	}
    
    private static class AuthenticatorCorreo extends Authenticator{
    	
    	private String user;
    	private String pass;
    	
    	public AuthenticatorCorreo(String user, String pass){
    		this.user=user;
    		this.pass=pass;
    	}
    	
    	public PasswordAuthentication getPasswordAuthentication(){
    		return new PasswordAuthentication(user, pass);
    	}
    }
}

