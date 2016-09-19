package dynagent.framework.utilidades;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.PixelGrabber;
import java.awt.image.MemoryImageSource;
import java.awt.image.ImageObserver;
import java.awt.Color;

/**
 * Clase que permite crear y manipular iconos
 * @author Francisco Javier Martinez Navarro
 */
public class CreadorIconos {

    /**
     * Constante que indica que se quiere realizar la operacion de aclarar un icono
     */
    public static final int ACLARAR=0;
    /**
     * Constante que indica que se quiere realizar la operacion de oscurecer un icono
     */
    public static final int OSCURECER=1;

    /**
     * Crear un ImageIcon
     * @param rutaImagen Ruta en la que se encuentra la imagen del icono
     * @param sizeIcono Dimension del icono, null para el tamaño original. Para hacer una redimension no
     * distorsionada uno de los parametros de Dimension se puede establecer a -1
     * @return ImageIcon o null si la ruta no es valida
     */
    public static javax.swing.ImageIcon crearIcono(String rutaImagen, Dimension sizeIcono) {        
        java.net.URL imageURL = CreadorIconos.class.getResource(rutaImagen);

        if (imageURL == null) {
            System.err.println(CreadorIconos.class+":Imagen no encontrada: "+ rutaImagen);
            return null;
        } else {
            //Image imagen=new ImageIcon(imageURL).getImage().getScaledInstance(tamanoImagen,-1,Image.SCALE_DEFAULT);
            Image imagen=Toolkit.getDefaultToolkit().createImage(imageURL);
            if(sizeIcono!=null)
                imagen=imagen.getScaledInstance((int)sizeIcono.getWidth(),(int)sizeIcono.getHeight(),Image.SCALE_SMOOTH);
            if(imagen!=null)
            	return new ImageIcon(imagen);
            else return new ImageIcon();
        }
    } 
    
    /**
     * Redimensionar un ImageIcon
     * @param icono ImageIcon original
     * @param sizeIcono Nueva Dimension del icono. Para hacer una redimension no distorsionada uno de los
     * parametros de Dimension se puede establecer a -1
     * @return ImageIcon redimensionado.
     */
    public static javax.swing.ImageIcon redimensionarIcono(ImageIcon icono, Dimension sizeIcono) {
        if(sizeIcono!=null){
            Image imagenRedimensionada=icono.getImage().getScaledInstance((int)sizeIcono.getWidth(),(int)sizeIcono.getHeight(),Image.SCALE_SMOOTH);
            if(imagenRedimensionada!=null)
            	return new ImageIcon(imagenRedimensionada);
            else return new ImageIcon();
        }else return null;
    } 

    /**
     * Modificar un ImageIcon aplicandole una determinada operacion
     * @param icono ImageIcon original
     * @param OPERACION Operacion a realizar sobre el icono.
     * Sus valores pueden ser:
     *    CreadorIconos.ACLARAR
     *    CreadorIconos.OSCURECER
     * @return ImageIcon modificado
     */
    public static javax.swing.ImageIcon modificarIcono(javax.swing.ImageIcon icono,int OPERACION) {
        int iniAlto=icono.getIconHeight();
        int iniAncho=icono.getIconWidth();
        int[] pix = new int[iniAncho * iniAlto];
        //PixelGrabber pixeles=new PixelGrabber(imageIcono.getImage(),0,0,iniAncho,iniAlto,pix,0,iniAncho);
        //pixeles.getColorModel().
        try {
          // Se instancia un objeto de tipo PixelGrabber, pasándole como
          // parámetro el array de pixels en donde queremos guardar la 
          // representación numérica de la imagen que vamos manipular
          PixelGrabber pixeles=new PixelGrabber(icono.getImage(),0,0,iniAncho,iniAlto,pix,0,iniAncho);

          // Se invoca ahora el método grabPixels() sobre el objeto de tipo
          // PixelGrabber que se acaba de instanciar, para la imagen se
          // convierta en un array de pixels. también se comprueba que el
          // proceso se realiza satisfactoriamente
          if( pixeles.grabPixels() && 
            ( (pixeles.getStatus() & ImageObserver.ALLBITS ) != 0 ) ) {
            // Ahora manipulamos la imagen, realizando la operacion
            // para los pixeles de la imagen
            if(OPERACION==ACLARAR){
                for( int i=0; i < (iniAncho*iniAlto); i++ ) {
                    Color colorObtenido=new Color(pix[i]);
                    pix[i]=colorObtenido.brighter().getRGB();
                }
            }else if(OPERACION==OSCURECER){
                for( int i=0; i < (iniAncho*iniAlto); i++ ) {
                    Color colorObtenido=new Color(pix[i]);
                    pix[i]=colorObtenido.darker().getRGB();
                }
            }
          }else {
            System.err.println( "Problemas al descomponer la imagen" );
            }
        } catch( InterruptedException e ) {
          System.out.println( e );
          }
        Image imagenNueva = Toolkit.getDefaultToolkit().createImage( new MemoryImageSource(
          iniAncho,iniAlto,pix,0,iniAncho ) );
        return new ImageIcon(imagenNueva);
    }
 }
