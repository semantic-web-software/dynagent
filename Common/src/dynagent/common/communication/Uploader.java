package dynagent.common.communication;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.ByteArrayBuffer;

import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;

public class Uploader {

	public static String uploadFile(String filePath, String host, int port, String user, String business) throws SystemException, RemoteSystemException{
		return upload(filePath, host, port, user, business, false, null, null);
	}
	
	public static String uploadImage(String filePath, String host, int port, String user, String business, int maxFileKBytesSize, Integer smallImageHeight) throws SystemException, RemoteSystemException {
		return upload(filePath, host, port, user, business, true, smallImageHeight, maxFileKBytesSize);
	}
	
	private static String upload(String filePath, String host, int port, String user, String business, boolean isImage, Integer smallImageHeight, Integer maxFileKBytes) throws SystemException, RemoteSystemException {
		HttpClient httpclient = new DefaultHttpClient(); 
	    httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1); 
	 
	    HttpPost httppost = new HttpPost("http://" + host + ":" + port +"/dynagent/HTTPGW");  
	    //String timeMillis=String.valueOf(System.currentTimeMillis());
	    File file = new File(filePath);
	    String idFile=null;
	    try{
		    MultipartEntity mpEntity = new MultipartEntity(); 
		    //ContentBody cbMilis = new StringBody(timeMillis);
		    ContentBody cbBns = new StringBody(business); 
		    ContentBody cbUser = new StringBody(user);
		    ContentBody cbSmallImageHeight = smallImageHeight!=null?new StringBody(String.valueOf(smallImageHeight)):null;
		    //ContentBody cbFile = new FileBody(file, "image/jpeg"); 
		    ContentBody cbFile;
		    if (isImage){
		    	cbFile = getImageContentBody(filePath, maxFileKBytes);
		    }else{
		    	cbFile = new FileBody(file);
		    }
		    
		    //mpEntity.addPart("milis", cbMilis); 
		    mpEntity.addPart("bns", cbBns); 
		    mpEntity.addPart("user", cbUser);
		    if(cbSmallImageHeight!=null)
		    	mpEntity.addPart("smallImageHeight", cbSmallImageHeight);
		    mpEntity.addPart("file", cbFile); 
		 
		    httppost.setEntity(mpEntity); 
		    //System.out.println("executing request " + httppost.getRequestLine()); 
		   
		    HttpResponse response = httpclient.execute(httppost); 
		    HttpEntity resEntity = response.getEntity(); 

		    //System.err.println(response.getStatusLine()); 
		    
		    //Obtenemos el nombre que ha sido asignado al fichero en el servidor
		    InputStream is = response.getEntity().getContent();
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(20);

             int current = 0;  
             while((current = bis.read()) != -1){  
                    baf.append((byte)current);  
             }  
               
            /* Convertimos los bytes leidos a String */  
            idFile = new String(baf.toByteArray()); 
            
            //Puede que no sea necesario hacer esto
		    if (resEntity != null) { 
		      resEntity.consumeContent(); 
		    } 
		    httpclient.getConnectionManager().shutdown(); 
		    
	    }catch(Exception ex){
	    	ex.printStackTrace();
	    	throw new SystemException(SystemException.ERROR_SISTEMA,/*e.getMessage()*/"ERROR DEL SISTEMA. No se ha podido enviar la imagen "+filePath);
	    }
	    
	    //Si no se nos ha enviado ningun nombre asignado en el servidor significara que no se ha podido guardar
	    if(idFile==null || idFile.isEmpty()){
	    	throw new RemoteSystemException(SystemException.ERROR_SISTEMA,/*e.getMessage()*/"ERROR DEL SISTEMA. No se ha podido almacenar en el servidor la imagen "+filePath+". Vuelva a intentarlo pasados unos segundos.");
	    }
	    
	    return idFile;
	}
	
	private static ContentBody getImageContentBody(String filePath, int maxFileKBytes) throws SystemException {
		File imageFile = new File(filePath);
		ContentBody imageContentBody;
		float originalKBytesSize = imageFile.length() / 1024.0F;
		if(originalKBytesSize > maxFileKBytes){
			// La imagen original es mayor que el tamaño máximo que podemos
			// subir al servidor, por lo que hay que reescalarla.
			System.out.println("Se procede a reescalar la imagen: " + filePath);
			ImageIcon imageIcon = new ImageIcon(filePath);
			Image image = imageIcon.getImage();
			// Calculamos a que tamaño hay que reescalar la imagen para que
			// tenga el tamaño en bytes adecuado.
			float ratio = originalKBytesSize / maxFileKBytes;
			int newHeight = (int) (image.getHeight(null) / ratio);
			// Escalamos la imagen
			Image scaledImg = image.getScaledInstance(-1, newHeight, Image.SCALE_SMOOTH);
			BufferedImage bufferedImage = toBufferedImage(scaledImg);
			
			// El código a continuación es una vuelta para poder conseguir un
			// InputStream desde la imagen que hemos reescalado
			String [] splitName = imageFile.getName().split("\\.");
			String extension = splitName[splitName.length - 1];
			ByteArrayOutputStream buffer_img = new ByteArrayOutputStream();
			try {
				ImageIO.write(bufferedImage, extension , buffer_img);
			} catch (IOException e) {
				SystemException sysException = new SystemException(SystemException.ERROR_INOUT, e.getCause().toString());
				sysException.setUserMessage("Fallo mientras se reescalaba la imagen " + filePath + " para mandarla al servidor");
				throw sysException;
			}
			byte[] bytes = buffer_img.toByteArray();
			imageContentBody = new ByteArrayBody(bytes, imageFile.getName());
		}else{
			imageContentBody = new FileBody(imageFile);
		}
		return imageContentBody;
	}

	private static BufferedImage toBufferedImage(Image image) {
		 /** miramos que la imagen no sea ya una instancia de BufferedImage */
		 if( image instanceof BufferedImage ) {
			 return( (BufferedImage)image );
       } else {
           /** nos aseguramos que la imagen está totalmente cargada */
           image = new ImageIcon(image).getImage();
           /** creamos la nueva imagen */
            BufferedImage bufferedImage = new BufferedImage(
                                                 image.getWidth(null),
                                                  image.getHeight(null),
                                                  BufferedImage.TYPE_INT_RGB );
            Graphics g = bufferedImage.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, image.getWidth(null), image.getHeight(null));//Ponemos el fondo blanco ya que si no aparece negro si la imagen tiene transparencia
            g.drawImage(image,0,0,null);
            g.dispose();
            return( bufferedImage );
        }
    }
}
