package dynagent.gui.utils;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Container;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

import javax.swing.JDialog;

import dynagent.gui.WindowComponent;

public class Converter {

	private Image out;

	public Converter(WindowComponent d) throws AWTException{

		Robot rbt;
		rbt = new Robot();
		Container content=d.getContentPane();
		Insets ins = content.getInsets();
		if(content.isShowing()){
			int x = content.getLocationOnScreen().x+ins.left;
			int y = content.getLocationOnScreen().y+ins.top;
			int finx = content.getSize().width-ins.left-ins.right;
			int finy = content.getSize().height-ins.bottom-ins.top;
			Image in = rbt.createScreenCapture( new Rectangle(x,y,finx,finy));
			out = modificarImagen(in, 1, content);
		}else{
			System.out.println("Warning (dynagent.gui.utils.Converter), component not showing");
		}
	}

	public Image modificarImagen(Image ent,int OPERACION, Container c) {
		int iniAlto=ent.getHeight(c);
		int iniAncho=ent.getWidth(c);

		int[] pix = new int[iniAncho * iniAlto];
		try {
			PixelGrabber pixeles=new PixelGrabber(ent,0,0,iniAncho,iniAlto,pix,0,iniAncho);

			if( pixeles.grabPixels() ){/*&& 
            ( (pixeles.getStatus() & ImageObserver.ALLBITS ) != 0 ) ) {*/
				if(OPERACION==0){
					for( int i=0; i < (iniAncho*iniAlto); i++ ) {
						Color colorObtenido=new Color(pix[i]);
						pix[i]=colorObtenido.brighter().getRGB();
					}
				}else if(OPERACION==1){
					for( int i=0; i < (iniAncho*iniAlto); i++ ) {
						Color colorObtenido=new Color(pix[i]);
						pix[i]=colorObtenido.darker().getRGB();
					}
				}
			}else {
				System.err.println( "Problemas al descomponer la imagen" );
			}
		} catch( InterruptedException e ) {
			System.err.println( e );
		}
		Image imagenNueva = Toolkit.getDefaultToolkit().createImage( new MemoryImageSource(
				iniAncho,iniAlto,pix,0,iniAncho ) );
		return imagenNueva;
	}

	public Image getOut() {
		return out;
	}
}
